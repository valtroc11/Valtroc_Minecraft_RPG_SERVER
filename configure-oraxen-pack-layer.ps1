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

$content = Get-Content -Path $settingsFile -Raw
$expected = "layer: '$LayerId'"
if ($content -match "(?m)^\s*layer:\s*'$([regex]::Escape($LayerId))'\s*$") {
    Write-Host "Oraxen ya usa layer '$LayerId' en $settingsFile" -ForegroundColor Green
    exit 0
}

Copy-Item -Path $settingsFile -Destination "$settingsFile.bak" -Force
$updated = [regex]::Replace($content, '(?m)^(\s*layer:\s*).*$' , "`$1'$LayerId'")
Set-Content -Path $settingsFile -Value $updated -Encoding UTF8

Write-Host "Layer de Oraxen ajustado a '$LayerId' en $settingsFile" -ForegroundColor Green
