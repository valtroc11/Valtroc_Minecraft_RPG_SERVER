#!/usr/bin/env bash
set -euo pipefail

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SERVER_DIR="$PROJECT_ROOT/server"
LATEST_LOG="$SERVER_DIR/logs/latest.log"
SERVER_PORT="${SERVER_PORT:-25565}"

paper_running() {
  pgrep -af "java.*paper\\.jar" >/dev/null 2>&1
}

playit_running() {
  systemctl is-active --quiet playit
}

latest_boot_start_line() {
  if [[ ! -f "$LATEST_LOG" ]]; then
    return 1
  fi

  grep -n 'Starting minecraft server version' "$LATEST_LOG" | tail -n1 | cut -d: -f1
}

latest_boot_done_line() {
  local start_line
  start_line="$(latest_boot_start_line || true)"

  if [[ -z "${start_line:-}" ]]; then
    return 1
  fi

  tail -n +"$start_line" "$LATEST_LOG" | grep 'Done (' | tail -n1
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
  DONE_LINE="$(latest_boot_done_line || true)"
  if [[ -n "${DONE_LINE:-}" ]]; then
    echo "Paper listo: $DONE_LINE"
  else
    echo "Paper aun no reporta 'Done (...)!'."
  fi
fi

if playit_running; then
  echo "Playit corriendo."
else
  echo "Playit no esta corriendo."
fi

PLAYIT_DOMAIN="$(sudo tail -n 200 /var/log/playit/playit.log 2>/dev/null | grep -Eo '[a-z0-9-]+\\.[a-z0-9-]+\\.(joinmc\\.link|ply\\.gg)(:[0-9]+)?' | tail -n1 || true)"

if [[ -n "${PLAYIT_DOMAIN:-}" ]]; then
  echo "Dominio publico playit: $PLAYIT_DOMAIN"
else
  echo "No pude leer aun el dominio publico desde los logs de playit."
fi
