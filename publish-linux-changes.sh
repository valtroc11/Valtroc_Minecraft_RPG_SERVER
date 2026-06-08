#!/usr/bin/env bash
set -euo pipefail

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
COMMIT_MESSAGE="${1:-}"

cd "$PROJECT_ROOT"

if [[ -z "$COMMIT_MESSAGE" ]]; then
  echo "Uso: ./publish-linux-changes.sh \"mensaje del commit\""
  exit 1
fi

echo "Sincronizando con origin/main..."
git pull --rebase --autostash origin main

echo "Estado actual:"
git status --short

echo "Agregando cambios versionables..."
git add .gitignore README.md content custom-plugins *.sh *.ps1 2>/dev/null || true

if git diff --cached --quiet; then
  echo "No hay cambios versionables para publicar."
  exit 0
fi

git commit -m "$COMMIT_MESSAGE"
git push origin main

echo "Cambios publicados a origin/main."
