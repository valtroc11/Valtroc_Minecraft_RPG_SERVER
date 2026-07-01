param(
    [string]$WorldName = "",
    [string]$ServerDir = (Join-Path $PSScriptRoot "server")
)

$ErrorActionPreference = "Stop"

if (Get-Process java -ErrorAction SilentlyContinue) {
    throw "Hay un proceso Java activo. Deten el servidor antes de instalar datapacks de worldgen."
}

if ([string]::IsNullOrWhiteSpace($WorldName)) {
    $propertiesFile = Join-Path $ServerDir "server.properties"
    if (-not (Test-Path $propertiesFile)) {
        throw "No existe server.properties en $ServerDir. Indica -WorldName manualmente."
    }
    $levelLine = Get-Content $propertiesFile | Where-Object { $_ -match '^level-name=' } | Select-Object -First 1
    if (-not $levelLine) {
        throw "No encontre level-name en $propertiesFile. Indica -WorldName manualmente."
    }
    $WorldName = $levelLine -replace '^level-name=', ''
}

& (Join-Path $PSScriptRoot "build-servidro-overworld-terralith.ps1")

$source = Join-Path $PSScriptRoot "content\worldgen\zz_servidro_overworld_terralith"
$target = Join-Path $ServerDir "$WorldName\datapacks\zz_servidro_overworld_terralith"

New-Item -ItemType Directory -Path $target -Force | Out-Null
robocopy $source $target /E /NFL /NDL /NJH /NJS /NC /NS | Out-Null

Write-Host "Integracion Servidro/Terralith instalada en $target" -ForegroundColor Green
Write-Host "Afecta solo chunks nuevos del Overworld. Los chunks ya generados no cambian." -ForegroundColor Yellow
