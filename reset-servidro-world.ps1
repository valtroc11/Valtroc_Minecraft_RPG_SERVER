param(
    [string]$ServerDir = (Join-Path $PSScriptRoot "server")
)

$ErrorActionPreference = "Stop"

if (Get-Process java -ErrorAction SilentlyContinue) {
    throw "Hay un proceso Java activo. Deten el servidor antes de regenerar servidro_helado."
}

$worldDir = Join-Path $ServerDir "servidro_helado"
if (Test-Path $worldDir) {
    Remove-Item -LiteralPath $worldDir -Recurse -Force
    Write-Host "Mundo eliminado para regeneracion: $worldDir" -ForegroundColor Yellow
}

Write-Host "El mundo servidro_helado se regenerara al arrancar Paper." -ForegroundColor Green
