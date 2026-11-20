#!/usr/bin/env bash
set -euo pipefail

deploy_dir="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
root_dir="$(cd -- "$deploy_dir/../.." && pwd)"
artifact_dir="${1:-}"
nginx_config="$deploy_dir/frontend-nginx.conf"

source "$deploy_dir/../ssh-common.sh"

if [[ -z "$artifact_dir" || ! -d "$artifact_dir" || ! -f "$artifact_dir/index.html" ]]; then
  echo "Informe o diretório do build do frontend." >&2
  exit 1
fi

if [[ ! -f "$nginx_config" ]]; then
  echo "Configuração Nginx do frontend ausente." >&2
  exit 1
fi

cd "$root_dir"
validate_deploy_id
init_ssh

work_dir="$(mktemp -d)"
cleanup_work() {
  local status=$?
  rm -rf -- "$work_dir"
  cleanup_ssh || status=$?
  return "$status"
}
trap cleanup_work EXIT

frontend_archive="$work_dir/frontend.tar.gz"
tar -czf "$frontend_archive" -C "$artifact_dir" .
remote_frontend="/tmp/biblioteca-viva-frontend-${DEPLOY_ID}.tar.gz"
remote_nginx="/tmp/biblioteca-viva-frontend-nginx.conf"

scp "${SSH_OPTIONS[@]}" "$frontend_archive" "${SSH_TARGET}:${remote_frontend}"
scp "${SSH_OPTIONS[@]}" "$nginx_config" "${SSH_TARGET}:${remote_nginx}"

ssh "${SSH_OPTIONS[@]}" "$SSH_TARGET" "DEPLOY_ID=$(printf '%q' "$DEPLOY_ID") bash -s" <<'REMOTE'
set -euo pipefail

web_dir="/var/www/biblioteca-viva"
frontend_tmp="/tmp/biblioteca-viva-frontend-${DEPLOY_ID}.tar.gz"
nginx_tmp="/tmp/biblioteca-viva-frontend-nginx.conf"
staging_dir="$(mktemp -d "/tmp/biblioteca-viva-frontend-${DEPLOY_ID}.XXXXXX")"

cleanup_remote() {
  local status=$?
  sudo rm -rf -- "$staging_dir"
  sudo rm -f -- "$frontend_tmp" "$nginx_tmp"
  return "$status"
}
trap cleanup_remote EXIT

for file in "$frontend_tmp" "$nginx_tmp"; do
  if [[ ! -f "$file" ]]; then
    echo "Artefato de frontend ausente no servidor." >&2
    exit 1
  fi
done

sudo install -d -o www-data -g www-data -m 0755 "$web_dir"
sudo tar -xzf "$frontend_tmp" -C "$staging_dir"
sudo test -s "$staging_dir/index.html"
sudo chown -R www-data:www-data "$staging_dir"
if ! command -v rsync >/dev/null 2>&1; then
  echo "rsync é necessário para atualizar os arquivos do frontend." >&2
  exit 1
fi
sudo install -o root -g root -m 0644 "$nginx_tmp" /etc/nginx/sites-available/biblioteca-viva-web.conf
sudo ln -sf /etc/nginx/sites-available/biblioteca-viva-web.conf /etc/nginx/sites-enabled/biblioteca-viva-web.conf
if [[ -e /etc/nginx/sites-enabled/biblioteca-viva-api.conf ]]; then
  sudo rm -f /etc/nginx/sites-enabled/biblioteca-viva.duckdns.org.conf
  sudo rm -f /etc/nginx/sites-available/biblioteca-viva.duckdns.org.conf
fi
sudo nginx -t
sudo systemctl reload nginx

sudo rsync --archive --delete "$staging_dir/" "$web_dir/"
sudo chown -R www-data:www-data "$web_dir"

frontend_status="$(curl --silent --output /dev/null --write-out '%{http_code}' --resolve biblioteca-viva.duckdns.org:443:127.0.0.1 https://biblioteca-viva.duckdns.org/)"
if [[ "$frontend_status" != "200" ]]; then
  echo "Frontend não respondeu com HTTP 200 após o deploy." >&2
  exit 1
fi

printf 'Frontend: Nginx ativo, HTTPS %s\n' "$frontend_status"
REMOTE
