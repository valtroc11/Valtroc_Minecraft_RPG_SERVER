param(
    [string]$ServerDir = (Join-Path $PSScriptRoot "server")
)

$ErrorActionPreference = "Stop"

$source = Join-Path $PSScriptRoot "content\mythicmobs"
$target = Join-Path $ServerDir "plugins\MythicMobs"

if (-not (Test-Path $source)) {
    throw "No existe la carpeta de contenido MythicMobs en $source"
}

$folders = @("mobs", "skills")
foreach ($folder in $folders) {
    $src = Join-Path $source $folder
    $dst = Join-Path $target $folder
    New-Item -ItemType Directory -Path $dst -Force | Out-Null
    robocopy $src $dst /E /NFL /NDL /NJH /NJS /NC /NS | Out-Null
}

Write-Host "Contenido MythicMobs sincronizado en $target" -ForegroundColor Green
