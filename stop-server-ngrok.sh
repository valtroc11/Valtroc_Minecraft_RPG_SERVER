#!/usr/bin/env bash
set -euo pipefail

SERVER_PORT="${SERVER_PORT:-25565}"

if pgrep -f "paper.jar" >/dev/null 2>&1; then
  pkill -f "paper.jar"
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
