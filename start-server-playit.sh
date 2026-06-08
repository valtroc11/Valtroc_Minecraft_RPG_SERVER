#!/usr/bin/env bash
set -euo pipefail

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SERVER_DIR="$PROJECT_ROOT/server"
PAPER_JAR="$SERVER_DIR/paper.jar"
EULA_FILE="$SERVER_DIR/eula.txt"
SERVER_LOG="$SERVER_DIR/server.log"
LATEST_LOG="$SERVER_DIR/logs/latest.log"
SERVER_PORT="${SERVER_PORT:-25565}"
JAVA_BIN="${JAVA_BIN:-java}"
STARTUP_TIMEOUT_SECONDS="${STARTUP_TIMEOUT_SECONDS:-180}"

paper_running() {
  pgrep -af "java.*paper\\.jar" >/dev/null 2>&1
}

playit_running() {
  systemctl is-active --quiet playit
}

wait_for_paper_ready() {
  local waited=0

  while (( waited < STARTUP_TIMEOUT_SECONDS )); do
    if ! paper_running; then
      echo "Paper se cerro durante el arranque. Revisa $SERVER_LOG o $LATEST_LOG"
      return 1
    fi

    if [[ -f "$LATEST_LOG" ]] && grep -q 'Done (' "$LATEST_LOG"; then
      return 0
    fi

    sleep 2
    waited=$((waited + 2))
  done

  echo "Paper no termino de arrancar dentro de ${STARTUP_TIMEOUT_SECONDS}s. Revisa $LATEST_LOG"
  return 1
}

if [[ ! -f "$PAPER_JAR" ]]; then
  echo "Paper no esta instalado. Copia paper.jar al directorio server/."
  exit 1
fi

if [[ ! -f "$EULA_FILE" ]]; then
  echo "Falta aceptar el EULA. Genera server/eula.txt con eula=true."
  exit 1
fi

if ! command -v "$JAVA_BIN" >/dev/null 2>&1; then
  echo "No encontre Java en PATH. Ajusta JAVA_BIN o instala Java 21."
  exit 1
fi

if ! command -v playit >/dev/null 2>&1; then
  echo "No encontre playit en PATH. Instala el agente antes de usar este script."
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

if playit_running; then
  echo "Playit ya estaba corriendo."
else
  echo "Iniciando servicio playit..."
  sudo systemctl start playit
fi

echo
echo "Estado actual"
echo "- Minecraft: localhost:$SERVER_PORT"
if [[ -n "${DONE_LINE:-}" ]]; then
  echo "- Paper listo: $DONE_LINE"
fi
if playit_running; then
  echo "- Playit: activo"
else
  echo "- Playit: no activo"
fi
echo "- Consulta el dominio publico con ./server-status-playit.sh"
