param(
    [string]$ServerDir = (Join-Path $PSScriptRoot "server")
)

$ErrorActionPreference = "Stop"

$source = Join-Path $PSScriptRoot "content\\oraxen"
$target = Join-Path $ServerDir "plugins\\Oraxen"

if (-not (Test-Path $source)) {
    throw "No existe la carpeta de contenido Oraxen en $source"
}

if (-not (Test-Path $target)) {
    throw "No existe Oraxen en $target. Instala el plugin primero."
}

$folders = @("items", "recipes", "pack")
foreach ($folder in $folders) {
    $src = Join-Path $source $folder
    $dst = Join-Path $target $folder
    New-Item -ItemType Directory -Path $dst -Force | Out-Null
    robocopy $src $dst /E /NFL /NDL /NJH /NJS /NC /NS | Out-Null
}

Write-Host "Contenido Oraxen sincronizado en $target" -ForegroundColor Green
