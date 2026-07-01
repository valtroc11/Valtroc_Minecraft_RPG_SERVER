param(
    [string]$ServerDir = (Join-Path $PSScriptRoot "server")
)

$ErrorActionPreference = "Stop"

if (Get-Process java -ErrorAction SilentlyContinue) {
    throw "Hay un proceso Java activo. Deten el servidor antes de regenerar el mundo POC."
}

$worldDir = Join-Path $ServerDir "world_servidro_poc_helado"
if (Test-Path $worldDir) {
    Remove-Item -LiteralPath $worldDir -Recurse -Force
    Write-Host "Mundo POC eliminado para regeneracion: $worldDir" -ForegroundColor Yellow
} else {
    Write-Host "No existia el mundo POC: $worldDir" -ForegroundColor Yellow
}

Write-Host "Arranca el servidor y usa /pochelado para generar terreno nuevo." -ForegroundColor Green
