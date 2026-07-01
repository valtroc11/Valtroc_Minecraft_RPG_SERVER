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

& (Join-Path $PSScriptRoot "build-servidro-super-biomes.ps1")

$source = Join-Path $PSScriptRoot "content\worldgen\zzz_servidro_super_biomes"
$target = Join-Path $ServerDir "$WorldName\datapacks\zzz_servidro_super_biomes"

New-Item -ItemType Directory -Path $target -Force | Out-Null
robocopy $source $target /E /NFL /NDL /NJH /NJS /NC /NS | Out-Null

Write-Host "Overlay super biomes instalado en $target" -ForegroundColor Green
Write-Host "Afecta solo chunks nuevos del Overworld. Para verlo de forma limpia, regenera el Overworld." -ForegroundColor Yellow
