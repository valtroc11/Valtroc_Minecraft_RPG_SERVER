#!/usr/bin/env bash
set -euo pipefail

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SERVER_DIR="$PROJECT_ROOT/server"
LATEST_LOG="$SERVER_DIR/logs/latest.log"
NGROK_LOG="$SERVER_DIR/ngrok.log"
NGROK_PUBLIC_FILE="$SERVER_DIR/ngrok-public.txt"
SERVER_PORT="${SERVER_PORT:-25565}"

paper_running() {
  pgrep -af "java.*paper\\.jar" >/dev/null 2>&1
}

get_ngrok_public_url() {
  local api_url
  api_url="$(curl -s http://127.0.0.1:4040/api/tunnels 2>/dev/null | grep -o 'tcp://[^\" ]*' | head -n1 | sed 's#tcp://##')"
  if [[ -n "${api_url:-}" ]]; then
    printf '%s\n' "$api_url"
    return 0
  fi

  if [[ -f "$NGROK_PUBLIC_FILE" ]]; then
    local cached_url
    cached_url="$(tail -n1 "$NGROK_PUBLIC_FILE" 2>/dev/null)"
    if [[ -n "${cached_url:-}" ]]; then
      printf '%s\n' "$cached_url"
      return 0
    fi
  fi

  if [[ -f "$NGROK_LOG" ]]; then
    local log_url
    log_url="$(grep -o 'tcp://[^ ]*' "$NGROK_LOG" 2>/dev/null | tail -n1 | sed 's#tcp://##')"
    if [[ -n "${log_url:-}" ]]; then
      printf '%s\n' "$log_url"
      return 0
    fi
  fi

  return 1
}

if paper_running; then
  echo "Minecraft corriendo."
else
  echo "Minecraft no esta corriendo."
fi

if ss -ltn 2>/dev/null | grep -q ":$SERVER_PORT "; then
  echo "Puerto escuchando: $SERVER_PORT"
else
  echo "Puerto $SERVER_PORT no esta escuchando."
fi

if paper_running && [[ -f "$LATEST_LOG" ]]; then
  DONE_LINE="$(grep 'Done (' "$LATEST_LOG" | tail -n1 || true)"
  if [[ -n "${DONE_LINE:-}" ]]; then
    echo "Paper listo: $DONE_LINE"
  else
    echo "Paper aun no reporta 'Done (...)!'."
  fi
fi

if pgrep -f "ngrok tcp $SERVER_PORT" >/dev/null 2>&1; then
  echo "Ngrok corriendo."
else
  echo "Ngrok no esta corriendo."
fi

if PUBLIC_URL="$(get_ngrok_public_url)"; then
  echo "IP publica ngrok: $PUBLIC_URL"
else
  echo "Ngrok no expuso aun una direccion TCP visible."
fi
