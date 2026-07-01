#!/usr/bin/env bash
set -euo pipefail

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
POWERSHELL_BIN="${POWERSHELL_BIN:-pwsh}"

if ! command -v "$POWERSHELL_BIN" >/dev/null 2>&1; then
  echo "No encontre pwsh. Ejecuta build-servidro-overworld-terralith.ps1 localmente y publica content/worldgen/zz_servidro_overworld_terralith."
  exit 1
fi

"$POWERSHELL_BIN" -NoProfile -ExecutionPolicy Bypass -File "$PROJECT_ROOT/build-servidro-overworld-terralith.ps1"
