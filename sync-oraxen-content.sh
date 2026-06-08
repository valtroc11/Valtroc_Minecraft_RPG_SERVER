#!/usr/bin/env bash
set -euo pipefail

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SERVER_DIR="${1:-$PROJECT_ROOT/server}"
SOURCE_DIR="$PROJECT_ROOT/content/oraxen"
TARGET_DIR="$SERVER_DIR/plugins/Oraxen"

if [[ ! -d "$SOURCE_DIR" ]]; then
  echo "No existe la carpeta de contenido Oraxen en $SOURCE_DIR"
  exit 1
fi

if [[ ! -d "$TARGET_DIR" ]]; then
  echo "No existe Oraxen en $TARGET_DIR. Instala el plugin primero."
  exit 1
fi

mkdir -p "$TARGET_DIR/items" "$TARGET_DIR/recipes" "$TARGET_DIR/pack"
cp -r "$SOURCE_DIR/items/." "$TARGET_DIR/items/"
cp -r "$SOURCE_DIR/recipes/." "$TARGET_DIR/recipes/"
cp -r "$SOURCE_DIR/pack/." "$TARGET_DIR/pack/"

echo "Contenido Oraxen sincronizado en $TARGET_DIR"
