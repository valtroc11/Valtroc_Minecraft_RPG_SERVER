#!/usr/bin/env bash
set -euo pipefail

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SERVER_DIR="${1:-$PROJECT_ROOT/server}"
WORLD_NAME="${2:-}"
BACKUP_ROOT="$SERVER_DIR/world-backups"
TIMESTAMP="$(date +%Y%m%d-%H%M%S)"

if pgrep -af 'java.*paper\.jar' >/dev/null 2>&1; then
  echo "Hay un proceso Paper activo. Deten el servidor antes de regenerar el Overworld."
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

mkdir -p "$BACKUP_ROOT/$TIMESTAMP"

for dir in "$WORLD_NAME" "${WORLD_NAME}_nether" "${WORLD_NAME}_the_end"; do
  TARGET="$SERVER_DIR/$dir"
  if [[ -e "$TARGET" ]]; then
    echo "Respaldando $TARGET..."
    mv "$TARGET" "$BACKUP_ROOT/$TIMESTAMP/$dir"
  fi
done

mkdir -p "$SERVER_DIR/$WORLD_NAME/datapacks"
echo "Overworld '$WORLD_NAME' movido a backup: $BACKUP_ROOT/$TIMESTAMP"
echo "Arranca Paper para generar el Overworld desde cero."
