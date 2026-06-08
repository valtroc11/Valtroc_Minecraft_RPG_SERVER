#!/usr/bin/env bash
set -euo pipefail

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SERVER_DIR="$PROJECT_ROOT/server"
PAPER_JAR="$SERVER_DIR/paper.jar"
EULA_FILE="$SERVER_DIR/eula.txt"
SERVER_LOG="$SERVER_DIR/server.log"
LATEST_LOG="$SERVER_DIR/logs/latest.log"
NGROK_LOG="$SERVER_DIR/ngrok.log"
NGROK_ERR_LOG="$SERVER_DIR/ngrok.err.log"
NGROK_PUBLIC_FILE="$SERVER_DIR/ngrok-public.txt"
SERVER_PORT="${SERVER_PORT:-25565}"
JAVA_BIN="${JAVA_BIN:-java}"
NGROK_BIN="${NGROK_BIN:-ngrok}"
STARTUP_TIMEOUT_SECONDS="${STARTUP_TIMEOUT_SECONDS:-180}"

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

wait_for_paper_ready() {
  local waited=0

  while (( waited < STARTUP_TIMEOUT_SECONDS )); do
    if ! paper_running; then
      echo "Paper se cerró durante el arranque. Revisa $SERVER_LOG o $LATEST_LOG"
      return 1
    fi

    if [[ -f "$LATEST_LOG" ]] && grep -q 'Done (' "$LATEST_LOG"; then
      return 0
    fi

    sleep 2
    waited=$((waited + 2))
  done

  echo "Paper no terminó de arrancar dentro de ${STARTUP_TIMEOUT_SECONDS}s. Revisa $LATEST_LOG"
  return 1
}

wait_for_ngrok_public_url() {
  local waited=0
  local public_url=""

  while (( waited < 30 )); do
    if public_url="$(get_ngrok_public_url)"; then
      printf '%s\n' "$public_url" | tee "$NGROK_PUBLIC_FILE" >/dev/null
      return 0
    fi
    sleep 2
    waited=$((waited + 2))
  done

  return 1
}

if [[ ! -f "$PAPER_JAR" ]]; then
  echo "Paper no esta instalado. Ejecuta primero ./setup-paper.ps1 o copia paper.jar al directorio server/."
  exit 1
fi

if [[ ! -f "$EULA_FILE" ]]; then
  echo "Falta aceptar el EULA. Revisa https://aka.ms/MinecraftEULA y genera server/eula.txt con eula=true."
  exit 1
fi

if ! command -v "$JAVA_BIN" >/dev/null 2>&1; then
  echo "No encontre Java en PATH. Ajusta JAVA_BIN o instala Java 21."
  exit 1
fi

if ! command -v "$NGROK_BIN" >/dev/null 2>&1; then
  echo "No encontre ngrok en PATH. Instálalo y ejecuta: ngrok config add-authtoken TU_TOKEN"
  exit 1
fi

cd "$SERVER_DIR"

if paper_running; then
  echo "Paper ya estaba corriendo."
else
  echo "Iniciando Paper en segundo plano..."
  nohup "$JAVA_BIN" -Xms2G -Xmx4G -jar "$PAPER_JAR" --nogui >"$SERVER_LOG" 2>&1 &
fi

echo "Esperando a que Paper termine de cargar..."
wait_for_paper_ready
DONE_LINE="$(grep 'Done (' "$LATEST_LOG" | tail -n1 || true)"

if pgrep -f "ngrok tcp $SERVER_PORT" >/dev/null 2>&1; then
  echo "Ngrok ya estaba corriendo."
else
  echo "Iniciando ngrok tcp $SERVER_PORT..."
  : > "$NGROK_LOG"
  : > "$NGROK_ERR_LOG"
  nohup "$NGROK_BIN" tcp "$SERVER_PORT" --log=stdout >"$NGROK_LOG" 2>"$NGROK_ERR_LOG" &
fi

echo "Esperando la URL pública de ngrok..."
PUBLIC_URL=""
if PUBLIC_URL="$(wait_for_ngrok_public_url 2>/dev/null; cat "$NGROK_PUBLIC_FILE" 2>/dev/null | tail -n1)"; then
  :
fi

echo
echo "Estado actual"
echo "- Minecraft: localhost:$SERVER_PORT"
if [[ -n "${DONE_LINE:-}" ]]; then
  echo "- Paper listo: $DONE_LINE"
fi
if [[ -n "${PUBLIC_URL:-}" ]]; then
  echo "- Ngrok TCP: $PUBLIC_URL"
else
  echo "- Ngrok TCP: aun sin URL publica visible. Revisa $NGROK_LOG"
fi
