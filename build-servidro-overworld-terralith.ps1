$ErrorActionPreference = "Stop"

$projectRoot = $PSScriptRoot
$terralithZip = Join-Path $projectRoot "server\world\datapacks\Terralith.zip"
$sourceBiomeDir = Join-Path $projectRoot "content\worldgen\servidro_worldgen_poc\data\servidro\worldgen\biome"
$outRoot = Join-Path $projectRoot "content\worldgen\zz_servidro_overworld_terralith"
$outDatapack = Join-Path $outRoot "data\minecraft\worldgen\multi_noise_biome_source_parameter_list"
$outBiomeDir = Join-Path $outRoot "data\servidro\worldgen\biome"
$outTagDir = Join-Path $outRoot "data\minecraft\tags\worldgen\biome"

if (-not (Test-Path $terralithZip)) {
    throw "No encontre Terralith.zip en $terralithZip"
}
if (-not (Test-Path $sourceBiomeDir)) {
    throw "No encontre biomas fuente en $sourceBiomeDir"
}

Remove-Item $outRoot -Recurse -Force -ErrorAction SilentlyContinue
New-Item -ItemType Directory -Path $outDatapack -Force | Out-Null
New-Item -ItemType Directory -Path $outBiomeDir -Force | Out-Null
New-Item -ItemType Directory -Path $outTagDir -Force | Out-Null

@'
{
  "pack": {
    "min_format": 94,
    "max_format": 94,
    "description": "Servidro + Terralith Overworld integration"
  }
}
'@ | Set-Content -Path (Join-Path $outRoot "pack.mcmeta") -Encoding ascii

Copy-Item (Join-Path $sourceBiomeDir "cordilleras_heladas.json") $outBiomeDir -Force
Copy-Item (Join-Path $sourceBiomeDir "profundidades_heladas.json") $outBiomeDir -Force

@'
{
  "replace": false,
  "values": [
    "servidro:cordilleras_heladas",
    "servidro:profundidades_heladas"
  ]
}
'@ | Set-Content -Path (Join-Path $outTagDir "is_overworld.json") -Encoding ascii

@'
{
  "replace": false,
  "values": [
    "servidro:cordilleras_heladas",
    "servidro:profundidades_heladas"
  ]
}
'@ | Set-Content -Path (Join-Path $outTagDir "is_cold.json") -Encoding ascii

@'
{
  "replace": false,
  "values": [
    "servidro:cordilleras_heladas"
  ]
}
'@ | Set-Content -Path (Join-Path $outTagDir "is_mountain.json") -Encoding ascii

$tmp = Join-Path $env:TEMP "terralith_overworld_$(Get-Random).json"
tar -xOf $terralithZip data/minecraft/worldgen/multi_noise_biome_source_parameter_list/overworld.json > $tmp
$json = Get-Content $tmp -Raw | ConvertFrom-Json
$entries = @($json.'lithostitched:biomes')

$targetBiomes = @(
    "minecraft:snowy_slopes",
    "terralith:alpine_highlands",
    "terralith:glacial_chasm"
)

$changed = 0
foreach ($entry in $entries) {
    if ($changed -ge 18) {
        break
    }
    if ($targetBiomes -notcontains $entry.biome) {
        continue
    }

    $p = $entry.parameters
    $tempMin = [double]$p.temperature[0]
    $tempMax = [double]$p.temperature[1]
    $contMax = [double]$p.continentalness[1]
    $erosionMax = [double]$p.erosion[1]
    $depthMin = [double]$p.depth[0]
    $depthMax = [double]$p.depth[1]

    $isSurface = $depthMin -le 0 -and $depthMax -ge -0.005
    $isCold = $tempMin -le -0.45 -or $tempMax -le -0.155
    $isMountain = $contMax -ge 0.03 -and $erosionMax -le 0.4

    if ($isSurface -and $isCold -and $isMountain) {
        $entry.biome = "servidro:cordilleras_heladas"
        $changed++
    }
}

if ($changed -lt 1) {
    throw "No se encontro ningun rango frio/montañoso para reemplazar."
}

$json | ConvertTo-Json -Depth 100 | Set-Content -Path (Join-Path $outDatapack "overworld.json") -Encoding ascii
Remove-Item $tmp -Force -ErrorAction SilentlyContinue

Write-Host "Integracion generada en $outRoot" -ForegroundColor Green
Write-Host "Entradas Overworld reemplazadas por servidro:cordilleras_heladas: $changed" -ForegroundColor Green
