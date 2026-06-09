#!/usr/bin/env bash
set -euo pipefail

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SERVER_DIR="${1:-$PROJECT_ROOT/server}"
PROPERTIES_FILE="$SERVER_DIR/server.properties"
PACK_URL="https\\://cdn.modrinth.com/data/hJAzl1Bs/versions/ScfKVGaF/Excalibur_V26.1_01.zip"
PACK_SHA1="f14f03d76c48e6c99a02ccefff0070c4fe3f110b"

if [[ ! -f "$PROPERTIES_FILE" ]]; then
  echo "No encontre $PROPERTIES_FILE"
  exit 0
fi

cp "$PROPERTIES_FILE" "$PROPERTIES_FILE.bak"
perl -0pi -e "s/^require-resource-pack=.*/require-resource-pack=false/m; s|^resource-pack=.*|resource-pack=$PACK_URL|m; s/^resource-pack-sha1=.*/resource-pack-sha1=$PACK_SHA1/m" "$PROPERTIES_FILE"

echo "Pack base Excalibur configurado en $PROPERTIES_FILE"
