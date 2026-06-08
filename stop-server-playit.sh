#!/usr/bin/env bash
set -euo pipefail

if pgrep -af "java.*paper\\.jar" >/dev/null 2>&1; then
  echo "Deteniendo Paper..."
  pkill -f "java.*paper\\.jar" || true
else
  echo "Paper no estaba corriendo."
fi

if systemctl is-active --quiet playit; then
  echo "Deteniendo playit..."
  sudo systemctl stop playit
else
  echo "Playit no estaba corriendo."
fi

echo "Listo."
