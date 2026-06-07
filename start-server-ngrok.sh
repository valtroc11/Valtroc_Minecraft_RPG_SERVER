#!/usr/bin/env bash
set -euo pipefail

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SERVER_DIR="$PROJECT_ROOT/server"
PAPER_JAR="$SERVER_DIR/paper.jar"
EULA_FILE="$SERVER_DIR/eula.txt"
SERVER_LOG="$SERVER_DIR/server.log"
NGROK_LOG="$SERVER_DIR/ngrok.log"
NGROK_ERR_LOG="$SERVER_DIR/ngrok.err.log"
SERVER_PORT="${SERVER_PORT:-25565}"
JAVA_BIN="${JAVA_BIN:-java}"
NGROK_BIN="${NGROK_BIN:-ngrok}"

if [[ ! -f "$PAPER_JAR" ]]; then
  echo "Paper no está instalado. Ejecuta primero ./setup-paper.ps1 o copia paper.jar al directorio server/."
  exit 1
fi

if [[ ! -f "$EULA_FILE" ]]; then
  echo "Falta aceptar el EULA. Revisa https://aka.ms/MinecraftEULA y genera server/eula.txt con eula=true."
  exit 1
fi

if ! command -v "$JAVA_BIN" >/dev/null 2>&1; then
  echo "No encontré Java en PATH. Ajusta JAVA_BIN o instala Java 21."
  exit 1
fi

if ! command -v "$NGROK_BIN" >/dev/null 2>&1; then
  echo "No encontré ngrok en PATH. Instálalo y ejecuta: ngrok config add-authtoken TU_TOKEN"
  exit 1
fi

cd "$SERVER_DIR"

if pgrep -f "paper.jar" >/dev/null 2>&1; then
  echo "Paper ya estaba corriendo."
else
  echo "Iniciando Paper en segundo plano..."
  nohup "$JAVA_BIN" -Xms2G -Xmx4G -jar "$PAPER_JAR" --nogui >"$SERVER_LOG" 2>&1 &
  sleep 10
fi

if pgrep -f "ngrok tcp $SERVER_PORT" >/dev/null 2>&1; then
  echo "Ngrok ya estaba corriendo."
else
  echo "Iniciando ngrok tcp $SERVER_PORT..."
  nohup "$NGROK_BIN" tcp "$SERVER_PORT" --log=stdout >"$NGROK_LOG" 2>"$NGROK_ERR_LOG" &
  sleep 5
fi

echo
echo "Estado actual"
echo "- Minecraft: localhost:$SERVER_PORT"

PUBLIC_URL="$(curl -s http://127.0.0.1:4040/api/tunnels 2>/dev/null | grep -o 'tcp://[^\" ]*' | head -n1 | sed 's#tcp://##')"
if [[ -n "${PUBLIC_URL:-}" ]]; then
  echo "- Ngrok TCP: $PUBLIC_URL"
else
  echo "- Ngrok TCP: aún sin URL pública visible. Revisa $NGROK_LOG"
fi
