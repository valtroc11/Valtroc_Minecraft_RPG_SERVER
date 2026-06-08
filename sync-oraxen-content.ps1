param(
    [string]$ServerDir = (Join-Path $PSScriptRoot "server")
)

$ErrorActionPreference = "Stop"

$source = Join-Path $PSScriptRoot "content\\oraxen"
$target = Join-Path $ServerDir "plugins\\Oraxen"

if (-not (Test-Path $source)) {
    throw "No existe la carpeta de contenido Oraxen en $source"
}

$folders = @("items", "recipes", "pack")
foreach ($folder in $folders) {
    $src = Join-Path $source $folder
    $dst = Join-Path $target $folder
    New-Item -ItemType Directory -Path $dst -Force | Out-Null
    robocopy $src $dst /E /NFL /NDL /NJH /NJS /NC /NS | Out-Null
}

Write-Host "Contenido Oraxen sincronizado en $target" -ForegroundColor Green

$jar = Join-Path $ServerDir "plugins\\Oraxen.jar"
if (Test-Path $jar) {
    Write-Host "Oraxen.jar detectado. El contenido ya puede ser consumido por el plugin." -ForegroundColor Green
} else {
    Write-Host "No encontre Oraxen.jar en $($ServerDir)\\plugins." -ForegroundColor Yellow
    Write-Host "El runtime quedo preparado, pero las texturas/items no se activaran hasta instalar el plugin." -ForegroundColor Yellow
}
