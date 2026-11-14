#!/usr/bin/env bash
set -euo pipefail

deploy_dir="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
root_dir="$(cd -- "$deploy_dir/../.." && pwd)"
artifact="${1:-}"

source "$deploy_dir/../ssh-common.sh"

if [[ -z "$artifact" || ! -f "$artifact" ]]; then
  echo "Informe o caminho do JAR construído." >&2
  exit 1
fi

if [[ ! -f "$deploy_dir/biblioteca-viva.service" || ! -f "$deploy_dir/backend-nginx.conf" ]]; then
  echo "Arquivos de deploy do backend ausentes." >&2
  exit 1
fi

cd "$root_dir"
validate_deploy_id
init_ssh

remote_jar="/tmp/biblioteca-viva-${DEPLOY_ID}.jar"
remote_service="/tmp/biblioteca-viva.service"
remote_nginx="/tmp/biblioteca-viva-backend-nginx.conf"

scp "${SSH_OPTIONS[@]}" "$artifact" "${SSH_TARGET}:${remote_jar}"
scp "${SSH_OPTIONS[@]}" "$deploy_dir/biblioteca-viva.service" "${SSH_TARGET}:${remote_service}"
scp "${SSH_OPTIONS[@]}" "$deploy_dir/backend-nginx.conf" "${SSH_TARGET}:${remote_nginx}"

ssh "${SSH_OPTIONS[@]}" "$SSH_TARGET" "DEPLOY_ID=$(printf '%q' "$DEPLOY_ID") bash -s" <<'REMOTE'
set -euo pipefail

app_dir="/opt/biblioteca-viva"
jar_tmp="/tmp/biblioteca-viva-${DEPLOY_ID}.jar"
service_tmp="/tmp/biblioteca-viva.service"
nginx_tmp="/tmp/biblioteca-viva-backend-nginx.conf"

cleanup_remote() {
  local status=$?
  sudo rm -f -- "$jar_tmp" "$service_tmp" "$nginx_tmp"
  return "$status"
}
trap cleanup_remote EXIT

for file in "$jar_tmp" "$service_tmp" "$nginx_tmp"; do
  if [[ ! -f "$file" ]]; then
    echo "Artefato de backend ausente no servidor." >&2
    exit 1
  fi
done

java -version 2>&1 | grep -q '21\.' || {
  echo "Java 21 não está instalado no servidor." >&2
  exit 1
}
sudo test -f /etc/biblioteca-viva/backend.env
sudo grep -q '^DB_URL=jdbc:postgresql://127.0.0.1:5432/' /etc/biblioteca-viva/backend.env || {
  echo "DB_URL deve apontar para o PostgreSQL em 127.0.0.1:5432." >&2
  exit 1
}
sudo docker volume inspect biblioteca-postgres-data >/dev/null
sudo docker exec biblioteca-postgres pg_isready >/dev/null

sudo install -d -o root -g biblioteca -m 0750 "$app_dir" /etc/biblioteca-viva
sudo install -o root -g biblioteca -m 0640 "$jar_tmp" "$app_dir/app.jar.new"
sudo mv -f "$app_dir/app.jar.new" "$app_dir/app.jar"
sudo install -o root -g root -m 0644 "$service_tmp" /etc/systemd/system/biblioteca-viva.service
sudo install -o root -g root -m 0644 "$nginx_tmp" /etc/nginx/sites-available/biblioteca-viva-api.conf
sudo ln -sf /etc/nginx/sites-available/biblioteca-viva-api.conf /etc/nginx/sites-enabled/biblioteca-viva-api.conf
if [[ -e /etc/nginx/sites-enabled/biblioteca-viva-web.conf ]]; then
  sudo rm -f /etc/nginx/sites-enabled/biblioteca-viva.duckdns.org.conf
  sudo rm -f /etc/nginx/sites-available/biblioteca-viva.duckdns.org.conf
fi
sudo nginx -t
sudo systemctl reload nginx
sudo systemctl daemon-reload
sudo systemctl enable biblioteca-viva.service >/dev/null
sudo systemctl restart biblioteca-viva.service

backend_status="000"
for attempt in $(seq 1 30); do
  backend_status="$(curl --silent --output /dev/null --write-out '%{http_code}' http://127.0.0.1:8080/ || true)"
  if [[ "$backend_status" == "401" ]]; then
    break
  fi
  sleep 2
done
if [[ "$backend_status" != "401" ]]; then
  echo "Backend não respondeu com o status esperado após o deploy." >&2
  exit 1
fi

printf 'Backend: systemd ativo, HTTP %s\n' "$backend_status"
REMOTE
