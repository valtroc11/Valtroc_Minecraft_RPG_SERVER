#!/usr/bin/env bash
set -euo pipefail

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PLUGIN_DIR="$PROJECT_ROOT/custom-plugins/servidro-rpg"
CLASSES_DIR="$PLUGIN_DIR/build/classes"
JAR_FILE="$PLUGIN_DIR/build/ServidroRpg.jar"
SERVER_JAR="$PROJECT_ROOT/server/plugins/ServidroRpg.jar"

rm -rf "$CLASSES_DIR"
mkdir -p "$CLASSES_DIR"

CLASSPATH="$(
  find "$PROJECT_ROOT/server/libraries" "$PROJECT_ROOT/server/plugins" -name '*.jar' -type f 2>/dev/null |
  paste -sd ':' -
)"

mapfile -t SOURCES < <(find "$PLUGIN_DIR/src/main/java" -name '*.java' -type f)

javac -proc:none -encoding UTF-8 -cp "$CLASSPATH" -d "$CLASSES_DIR" "${SOURCES[@]}"
cp -f "$PLUGIN_DIR"/src/main/resources/* "$CLASSES_DIR"/
jar --create --file "$JAR_FILE" -C "$CLASSES_DIR" .

mkdir -p "$(dirname "$SERVER_JAR")"
cp -f "$JAR_FILE" "$SERVER_JAR"
rm -f "$PROJECT_ROOT/server/plugins/.paper-remapped/ServidroRpg.jar"

echo "ServidroRpg compilado e instalado en $SERVER_JAR"
