#!/usr/bin/env bash
set -euo pipefail

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SERVER_DIR="$PROJECT_ROOT/server"
WORLD_NAME="${WORLD_NAME:-}"

cd "$PROJECT_ROOT"

if [[ -z "$WORLD_NAME" ]]; then
  if [[ ! -f "$SERVER_DIR/server.properties" ]]; then
    echo "No existe server/server.properties. Define WORLD_NAME o instala Paper primero."
    exit 1
  fi
  WORLD_NAME="$(grep -E '^level-name=' "$SERVER_DIR/server.properties" | head -n1 | cut -d= -f2-)"
fi

echo "Deteniendo servidor y tunel si estan activos..."
"$PROJECT_ROOT/stop-server-playit.sh" || true

echo "Actualizando repo..."
git pull --ff-only

echo "Compilando plugins..."
"$PROJECT_ROOT/build-servidro-rpg.sh"
"$PROJECT_ROOT/build-servidro-world.sh"

if [[ -d "$PROJECT_ROOT/content/oraxen" ]]; then
  echo "Sincronizando Oraxen..."
  "$PROJECT_ROOT/sync-oraxen-content.sh" "$SERVER_DIR"
fi

if [[ -f "$PROJECT_ROOT/configure-server-base-pack.sh" ]]; then
  "$PROJECT_ROOT/configure-server-base-pack.sh" "$SERVER_DIR"
fi

if [[ -f "$PROJECT_ROOT/configure-oraxen-pack-layer.sh" ]]; then
  "$PROJECT_ROOT/configure-oraxen-pack-layer.sh" "$SERVER_DIR"
fi

echo "Regenerando Overworld desde cero: $WORLD_NAME"
BASE_DATAPACK_BACKUP="$(mktemp -d)"
if [[ -d "$SERVER_DIR/$WORLD_NAME/datapacks" ]]; then
  find "$SERVER_DIR/$WORLD_NAME/datapacks" -maxdepth 1 -type f -name '*.zip' -exec cp -f {} "$BASE_DATAPACK_BACKUP"/ \;
fi
"$PROJECT_ROOT/reset-overworld-linux.sh" "$SERVER_DIR" "$WORLD_NAME"

echo "Instalando datapacks en el Overworld nuevo..."
if compgen -G "$BASE_DATAPACK_BACKUP/*.zip" >/dev/null; then
  cp -f "$BASE_DATAPACK_BACKUP"/*.zip "$SERVER_DIR/$WORLD_NAME/datapacks"/
fi
rm -rf "$BASE_DATAPACK_BACKUP"
"$PROJECT_ROOT/install-worldgen-poc.sh" "$SERVER_DIR" "$WORLD_NAME"
"$PROJECT_ROOT/install-servidro-overworld-terralith.sh" "$SERVER_DIR" "$WORLD_NAME"
"$PROJECT_ROOT/install-servidro-super-biomes.sh" "$SERVER_DIR" "$WORLD_NAME"

echo "Arrancando servidor..."
"$PROJECT_ROOT/start-server-playit.sh"

echo "Despliegue fresh Overworld completado."
