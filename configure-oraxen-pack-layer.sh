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

cp "$SETTINGS_FILE" "$SETTINGS_FILE.bak"

python3 - "$SETTINGS_FILE" "$LAYER_ID" <<'PY'
from pathlib import Path
import re
import sys

settings_path = Path(sys.argv[1])
layer_id = sys.argv[2]
content = settings_path.read_text(encoding="utf-8")

content = re.sub(
    r"(?m)^(\s*layer:\s*).*$",
    rf"\1'{layer_id}'",
    content,
)
content = re.sub(
    r"(?m)^(\s*protection:\s*)true(\s*(?:#.*)?)$",
    r"\1false\2",
    content,
)

settings_path.write_text(content, encoding="utf-8")
PY

echo "Layer de Oraxen ajustado a '$LAYER_ID' en $SETTINGS_FILE"
echo "Proteccion del pack Oraxen ajustada a false para depuracion/compatibilidad"
