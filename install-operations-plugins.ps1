$ErrorActionPreference = "Stop"

$minecraftVersion = "1.21.11"
$pluginsDir = Join-Path $PSScriptRoot "server\plugins"
$coreProtectUrl = "https://www.patreon.com/file?h=158272081&m=663342224"

New-Item -ItemType Directory -Force -Path $pluginsDir | Out-Null

Write-Host "Buscando EssentialsX para Paper $minecraftVersion..."
$essentialsApi = "https://api.modrinth.com/v2/project/essentialsx/version?loaders=[%22paper%22]&game_versions=[%22$minecraftVersion%22]"
$versions = Invoke-RestMethod -Uri $essentialsApi
$version = $versions | Where-Object { $_.version_type -eq "release" } | Select-Object -First 1

if (-not $version) {
    throw "No se encontro una version estable de EssentialsX para Paper $minecraftVersion."
}

$file = $version.files | Where-Object { $_.primary } | Select-Object -First 1
if (-not $file) {
    $file = $version.files | Select-Object -First 1
}

Write-Host "Descargando EssentialsX $($version.version_number)..."
Invoke-WebRequest -Uri $file.url -OutFile (Join-Path $pluginsDir "EssentialsX.jar")

Write-Host "Descargando CoreProtect Community Edition 23.2..."
Invoke-WebRequest -Uri $coreProtectUrl -OutFile (Join-Path $pluginsDir "CoreProtect.jar")

Write-Host ""
Write-Host "Plugins operativos instalados."
Write-Host "Inicia el servidor con: .\start-server.ps1"

