$ErrorActionPreference = "Stop"

$projectRoot = $PSScriptRoot
$terralithZip = Join-Path $projectRoot "server\world\datapacks\Terralith.zip"
$outRoot = Join-Path $projectRoot "content\worldgen\zzz_servidro_super_biomes"
$outRouterDir = Join-Path $outRoot "data\minecraft\worldgen\density_function\overworld\noise_router"

if (-not (Test-Path $terralithZip)) {
    throw "No encontre Terralith.zip en $terralithZip"
}

Remove-Item $outRoot -Recurse -Force -ErrorAction SilentlyContinue
New-Item -ItemType Directory -Path $outRouterDir -Force | Out-Null

@'
{
  "pack": {
    "min_format": 94,
    "max_format": 94,
    "description": "Servidro super biomes overlay for Terralith/Tectonic"
  }
}
'@ | Set-Content -Path (Join-Path $outRoot "pack.mcmeta") -Encoding ascii

$routerFunctions = @(
    "temperature",
    "vegetation",
    "erosion",
    "continents"
)

$tmp = Join-Path $env:TEMP "servidro_super_biomes_$(Get-Random)"
New-Item -ItemType Directory -Path $tmp -Force | Out-Null

try {
    foreach ($name in $routerFunctions) {
        $sourcePath = "data/minecraft/worldgen/density_function/overworld_large_biomes/noise_router/$name.json"
        tar -xf $terralithZip -C $tmp $sourcePath
        $sourceFile = Join-Path $tmp ($sourcePath -replace '/', [IO.Path]::DirectorySeparatorChar)
        Copy-Item $sourceFile (Join-Path $outRouterDir "$name.json") -Force
    }
}
finally {
    Remove-Item $tmp -Recurse -Force -ErrorAction SilentlyContinue
}

Write-Host "Overlay super biomes generado en $outRoot" -ForegroundColor Green
Write-Host "Funciones climáticas redirigidas a Terralith large_biomes: $($routerFunctions -join ', ')" -ForegroundColor Green
