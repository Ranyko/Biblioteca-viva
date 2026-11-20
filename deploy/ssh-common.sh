#!/usr/bin/env bash

init_ssh() {
  local name

  for name in DEPLOY_HOST DEPLOY_USER DEPLOY_SSH_PRIVATE_KEY DEPLOY_SSH_KNOWN_HOSTS; do
    if [[ -z "${!name:-}" ]]; then
      printf 'A variável %s é obrigatória para o deploy.\n' "$name" >&2
      return 1
    fi
  done

  SSH_DIR="$(mktemp -d)"
  install -m 600 /dev/null "$SSH_DIR/deploy_key"
  printf '%s\n' "$DEPLOY_SSH_PRIVATE_KEY" > "$SSH_DIR/deploy_key"
  install -m 600 /dev/null "$SSH_DIR/known_hosts"
  printf '%s\n' "$DEPLOY_SSH_KNOWN_HOSTS" > "$SSH_DIR/known_hosts"
  SSH_OPTIONS=(-i "$SSH_DIR/deploy_key" -o BatchMode=yes -o StrictHostKeyChecking=yes -o "UserKnownHostsFile=$SSH_DIR/known_hosts" -o GlobalKnownHostsFile=/dev/null)
  SSH_TARGET="${DEPLOY_USER}@${DEPLOY_HOST}"
  trap cleanup_ssh EXIT
}

cleanup_ssh() {
  local status=$?
  rm -rf -- "$SSH_DIR"
  return "$status"
}

validate_deploy_id() {
  DEPLOY_ID="${DEPLOY_SHA:-$(git rev-parse --short HEAD)}"
  if [[ ! "$DEPLOY_ID" =~ ^[A-Za-z0-9._-]+$ ]]; then
    echo "DEPLOY_SHA contém caracteres inválidos." >&2
    return 1
  fi
}
