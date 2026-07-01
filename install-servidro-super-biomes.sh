#!/usr/bin/env bash
set -euo pipefail

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SERVER_DIR="${1:-$PROJECT_ROOT/server}"
WORLD_NAME="${2:-}"

if pgrep -af 'java.*paper\.jar' >/dev/null 2>&1; then
  echo "Hay un proceso Paper activo. Deten el servidor antes de instalar datapacks."
  exit 1
fi

if [[ -z "$WORLD_NAME" ]]; then
  PROPERTIES_FILE="$SERVER_DIR/server.properties"
  if [[ ! -f "$PROPERTIES_FILE" ]]; then
    echo "No existe $PROPERTIES_FILE. Indica el nombre de mundo como segundo argumento."
    exit 1
  fi
  WORLD_NAME="$(grep -E '^level-name=' "$PROPERTIES_FILE" | head -n1 | cut -d= -f2-)"
fi

SOURCE="$PROJECT_ROOT/content/worldgen/zzz_servidro_super_biomes"
TARGET="$SERVER_DIR/$WORLD_NAME/datapacks/zzz_servidro_super_biomes"

if [[ ! -d "$SOURCE" ]]; then
  "$PROJECT_ROOT/build-servidro-super-biomes.sh"
fi

mkdir -p "$TARGET"
rsync -a --delete "$SOURCE"/ "$TARGET"/
echo "Overlay super biomes instalado en $TARGET"
echo "Afecta solo chunks nuevos del Overworld."
