#!/usr/bin/env bash
set -euo pipefail

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PLUGIN_BUILD_JAR="$PROJECT_ROOT/custom-plugins/servidro-rpg/build/ServidroRpg.jar"
SERVER_PLUGIN_JAR="$PROJECT_ROOT/server/plugins/ServidroRpg.jar"

cd "$PROJECT_ROOT"

echo "Actualizando repo..."
git pull --ff-only

if [[ -f "$PLUGIN_BUILD_JAR" ]]; then
  echo "Sincronizando plugin compilado..."
  cp -f "$PLUGIN_BUILD_JAR" "$SERVER_PLUGIN_JAR"
else
  echo "No encontre $PLUGIN_BUILD_JAR"
  echo "Si el cambio toco el plugin Java, compila primero o sube tambien el jar."
fi

chmod +x start-server-ngrok.sh stop-server-ngrok.sh server-status.sh || true
chmod +x start-server-playit.sh stop-server-playit.sh server-status-playit.sh || true

echo "Listo."
