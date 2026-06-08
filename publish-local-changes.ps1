param(
    [Parameter(Mandatory = $true)]
    [string]$Message
)

$ErrorActionPreference = "Stop"

$repo = $PSScriptRoot
Push-Location $repo
try {
    git pull --rebase --autostash origin main
    git add .gitignore README.md content custom-plugins *.sh *.ps1

    $hasChanges = git diff --cached --name-only
    if (-not $hasChanges) {
        Write-Host "No hay cambios versionables para publicar." -ForegroundColor Yellow
        exit 0
    }

    git commit -m $Message
    git push origin main
    Write-Host "Cambios publicados a origin/main." -ForegroundColor Green
}
finally {
    Pop-Location
}
