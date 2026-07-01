param(
    [string]$WorldName = "",
    [string]$ServerDir = (Join-Path $PSScriptRoot "server")
)

$ErrorActionPreference = "Stop"

$source = Join-Path $PSScriptRoot "content\worldgen\servidro_worldgen_poc"
$propertiesFile = Join-Path $ServerDir "server.properties"

if ([string]::IsNullOrWhiteSpace($WorldName)) {
    if (-not (Test-Path $propertiesFile)) {
        throw "No existe server.properties en $ServerDir. Indica -WorldName manualmente."
    }

    $levelLine = Get-Content $propertiesFile | Where-Object { $_ -match '^level-name=' } | Select-Object -First 1
    if (-not $levelLine) {
        throw "No encontre level-name en $propertiesFile. Indica -WorldName manualmente."
    }

    $WorldName = $levelLine -replace '^level-name=', ''
}

$target = Join-Path $ServerDir "$WorldName\datapacks\servidro_worldgen_poc"

if (-not (Test-Path $source)) {
    throw "No existe el datapack POC en $source"
}

if (Get-Process java -ErrorAction SilentlyContinue) {
    throw "Hay un proceso Java activo. Deten el servidor antes de instalar o actualizar datapacks."
}

New-Item -ItemType Directory -Path $target -Force | Out-Null
robocopy $source $target /E /NFL /NDL /NJH /NJS /NC /NS | Out-Null

Write-Host "Worldgen POC instalado en $target" -ForegroundColor Green
Write-Host "Arranca el servidor y prueba: /execute in servidro:poc_helado run tp <jugador> 0 160 0" -ForegroundColor Green
