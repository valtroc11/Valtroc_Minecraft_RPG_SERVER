$ErrorActionPreference = "Stop"

Push-Location $PSScriptRoot
try {
    git pull --rebase --autostash origin main
    Write-Host "Repositorio local sincronizado con origin/main." -ForegroundColor Green
}
finally {
    Pop-Location
}
