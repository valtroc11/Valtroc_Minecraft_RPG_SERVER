#!/usr/bin/env bash
set -euo pipefail

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SERVER_DIR="$PROJECT_ROOT/server"
NGROK_LOG="$SERVER_DIR/ngrok.log"
SERVER_PORT="${SERVER_PORT:-25565}"

if pgrep -f "paper.jar" >/dev/null 2>&1; then
  echo "Minecraft corriendo."
else
  echo "Minecraft no está corriendo."
fi

if ss -ltn 2>/dev/null | grep -q ":$SERVER_PORT "; then
  echo "Puerto escuchando: $SERVER_PORT"
else
  echo "Puerto $SERVER_PORT no está escuchando."
fi

if pgrep -f "ngrok tcp $SERVER_PORT" >/dev/null 2>&1; then
  echo "Ngrok corriendo."
else
  echo "Ngrok no está corriendo."
fi

PUBLIC_URL="$(curl -s http://127.0.0.1:4040/api/tunnels 2>/dev/null | grep -o 'tcp://[^\" ]*' | head -n1 | sed 's#tcp://##')"
if [[ -n "${PUBLIC_URL:-}" ]]; then
  echo "IP pública ngrok: $PUBLIC_URL"
elif [[ -f "$NGROK_LOG" ]]; then
  FALLBACK_URL="$(grep -o 'tcp://[^ ]*' "$NGROK_LOG" | tail -n1 | sed 's#tcp://##')"
  if [[ -n "${FALLBACK_URL:-}" ]]; then
    echo "IP pública ngrok: $FALLBACK_URL"
  else
    echo "Ngrok no expuso aún una dirección TCP visible."
  fi
else
  echo "Ngrok no expuso aún una dirección TCP visible."
fi
