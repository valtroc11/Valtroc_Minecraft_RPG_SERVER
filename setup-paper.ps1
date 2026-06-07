$ErrorActionPreference = "Stop"

$minecraftVersion = "1.21.11"
$userAgent = "servidro-mx-local-setup/0.1 ([email protected])"
$serverDir = Join-Path $PSScriptRoot "server"
$paperJar = Join-Path $serverDir "paper.jar"
$buildsApi = "https://fill.papermc.io/v3/projects/paper/versions/$minecraftVersion/builds"

New-Item -ItemType Directory -Force -Path $serverDir | Out-Null

Write-Host "Consultando el ultimo build estable de Paper $minecraftVersion..."
$headers = @{ "User-Agent" = $userAgent }
$builds = Invoke-RestMethod -Uri $buildsApi -Headers $headers
$stableBuild = $builds |
    Where-Object { $_.channel -eq "STABLE" } |
    Select-Object -First 1

if (-not $stableBuild) {
    throw "No se encontro un build estable de Paper para Minecraft $minecraftVersion."
}

$downloadUrl = $stableBuild.downloads."server:default".url
if (-not $downloadUrl) {
    throw "El build estable no incluye una descarga server:default."
}

Write-Host "Descargando Paper build $($stableBuild.id)..."
Invoke-WebRequest -Uri $downloadUrl -Headers $headers -OutFile $paperJar

@"
Minecraft version: $minecraftVersion
Paper build: $($stableBuild.id)
Downloaded from: $downloadUrl
"@ | Set-Content -Path (Join-Path $serverDir "paper-build.txt") -Encoding ascii

Write-Host ""
Write-Host "Paper instalado en $paperJar"
Write-Host "Antes de iniciar, lee el EULA:"
Write-Host "https://aka.ms/MinecraftEULA"
Write-Host ""
Write-Host "Si lo aceptas, ejecuta: .\accept-eula.ps1"
Write-Host "Luego inicia con:      .\start-server.ps1"

