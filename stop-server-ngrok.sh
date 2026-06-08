#!/usr/bin/env bash
set -euo pipefail

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SERVER_DIR="$PROJECT_ROOT/server"
NGROK_PUBLIC_FILE="$SERVER_DIR/ngrok-public.txt"
SERVER_PORT="${SERVER_PORT:-25565}"

if pgrep -af "java.*paper\\.jar" >/dev/null 2>&1; then
  pkill -f "java.*paper\\.jar"
  echo "Paper detenido."
else
  echo "Paper no estaba corriendo."
fi

if pgrep -f "ngrok tcp $SERVER_PORT" >/dev/null 2>&1; then
  pkill -f "ngrok tcp $SERVER_PORT"
  echo "Ngrok detenido."
else
  echo "Ngrok no estaba corriendo."
fi

rm -f "$NGROK_PUBLIC_FILE"
