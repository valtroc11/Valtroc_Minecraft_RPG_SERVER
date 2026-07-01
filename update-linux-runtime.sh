#!/usr/bin/env bash
set -euo pipefail

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PLUGIN_BUILD_JAR="$PROJECT_ROOT/custom-plugins/servidro-rpg/build/ServidroRpg.jar"
SERVER_PLUGIN_JAR="$PROJECT_ROOT/server/plugins/ServidroRpg.jar"
WORLD_PLUGIN_BUILD_JAR="$PROJECT_ROOT/custom-plugins/servidro-world/build/ServidroWorld.jar"
WORLD_SERVER_PLUGIN_JAR="$PROJECT_ROOT/server/plugins/ServidroWorld.jar"

cd "$PROJECT_ROOT"

echo "Actualizando repo..."
git pull --ff-only

if [[ -f "$PROJECT_ROOT/configure-server-base-pack.sh" ]]; then
  echo "Ajustando pack base medieval en server.properties..."
  "$PROJECT_ROOT/configure-server-base-pack.sh" "$PROJECT_ROOT/server"
fi

if [[ -f "$PLUGIN_BUILD_JAR" ]]; then
  echo "Sincronizando plugin compilado..."
  cp -f "$PLUGIN_BUILD_JAR" "$SERVER_PLUGIN_JAR"
else
  echo "No encontre $PLUGIN_BUILD_JAR"
  echo "Si el cambio toco el plugin Java, compila primero o sube tambien el jar."
fi

if [[ -f "$WORLD_PLUGIN_BUILD_JAR" ]]; then
  echo "Sincronizando plugin ServidroWorld compilado..."
  cp -f "$WORLD_PLUGIN_BUILD_JAR" "$WORLD_SERVER_PLUGIN_JAR"
else
  echo "No encontre $WORLD_PLUGIN_BUILD_JAR"
  echo "Si el cambio toco ServidroWorld, compila primero o sube tambien el jar."
fi

if [[ -d "$PROJECT_ROOT/content/worldgen/servidro_worldgen_poc" ]]; then
  echo "Instalando datapack Servidro worldgen POC..."
  "$PROJECT_ROOT/install-worldgen-poc.sh" "$PROJECT_ROOT/server"
fi

if [[ -d "$PROJECT_ROOT/content/worldgen/zz_servidro_overworld_terralith" ]]; then
  echo "Instalando integracion Servidro/Terralith para Overworld..."
  "$PROJECT_ROOT/install-servidro-overworld-terralith.sh" "$PROJECT_ROOT/server"
fi

if [[ -d "$PROJECT_ROOT/content/worldgen/zzz_servidro_super_biomes" ]]; then
  echo "Instalando overlay super biomes para Overworld..."
  "$PROJECT_ROOT/install-servidro-super-biomes.sh" "$PROJECT_ROOT/server"
fi

if [[ -d "$PROJECT_ROOT/content/oraxen" ]]; then
  echo "Sincronizando contenido Oraxen..."
  "$PROJECT_ROOT/sync-oraxen-content.sh" "$PROJECT_ROOT/server"
fi

if [[ -f "$PROJECT_ROOT/configure-oraxen-pack-layer.sh" ]]; then
  echo "Ajustando layer de Oraxen para coexistir con el pack medieval base..."
  "$PROJECT_ROOT/configure-oraxen-pack-layer.sh" "$PROJECT_ROOT/server"
fi

chmod +x start-server-ngrok.sh stop-server-ngrok.sh server-status.sh || true
chmod +x start-server-playit.sh stop-server-playit.sh server-status-playit.sh || true
chmod +x configure-server-base-pack.sh || true
chmod +x configure-oraxen-pack-layer.sh || true
chmod +x build-servidro-rpg.sh build-servidro-world.sh || true
chmod +x install-worldgen-poc.sh install-servidro-overworld-terralith.sh install-servidro-super-biomes.sh reset-overworld-linux.sh || true
chmod +x build-servidro-overworld-terralith.sh build-servidro-super-biomes.sh || true
chmod +x deploy-fresh-overworld-linux.sh || true

echo "Listo."
