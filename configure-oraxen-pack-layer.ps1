param(
    [string]$ServerDir = (Join-Path $PSScriptRoot "server"),
    [string]$LayerId = "excalibur_base"
)

$ErrorActionPreference = "Stop"

$settingsFile = Join-Path $ServerDir "plugins\\Oraxen\\settings.yml"
if (-not (Test-Path $settingsFile)) {
    Write-Host "No encontre $settingsFile" -ForegroundColor Yellow
    Write-Host "Oraxen todavia no genero su settings.yml en este runtime." -ForegroundColor Yellow
    exit 0
}

Copy-Item -Path $settingsFile -Destination "$settingsFile.bak" -Force
$content = Get-Content -Path $settingsFile -Raw
$updated = [regex]::Replace($content, '(?m)^(\s*layer:\s*).*$' , "`$1'$LayerId'")
$updated = [regex]::Replace($updated, '(?m)^(\s*protection:\s*)true(\s*(?:#.*)?)$' , "`$1false`$2")
Set-Content -Path $settingsFile -Value $updated -Encoding UTF8

Write-Host "Layer de Oraxen ajustado a '$LayerId' en $settingsFile" -ForegroundColor Green
Write-Host "Proteccion del pack Oraxen ajustada a false para depuracion/compatibilidad" -ForegroundColor Green
