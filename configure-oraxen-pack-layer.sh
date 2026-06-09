#!/usr/bin/env bash
set -euo pipefail

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SERVER_DIR="${1:-$PROJECT_ROOT/server}"
SETTINGS_FILE="$SERVER_DIR/plugins/Oraxen/settings.yml"
LAYER_ID="${2:-excalibur_base}"

if [[ ! -f "$SETTINGS_FILE" ]]; then
  echo "No encontre $SETTINGS_FILE"
  echo "Oraxen todavia no genero su settings.yml en este runtime."
  exit 0
fi

if grep -qE "^[[:space:]]*layer:[[:space:]]*'$LAYER_ID'[[:space:]]*$" "$SETTINGS_FILE"; then
  echo "Oraxen ya usa layer '$LAYER_ID' en $SETTINGS_FILE"
  exit 0
fi

cp "$SETTINGS_FILE" "$SETTINGS_FILE.bak"
sed -i -E "s/^([[:space:]]*layer:[[:space:]]*).*/\\1'$LAYER_ID'/" "$SETTINGS_FILE"

echo "Layer de Oraxen ajustado a '$LAYER_ID' en $SETTINGS_FILE"
