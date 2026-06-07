$ErrorActionPreference = "Stop"

$serverDir = Join-Path $PSScriptRoot "server"
if (-not (Test-Path (Join-Path $serverDir "paper.jar"))) {
    throw "Paper no esta instalado. Ejecuta primero .\setup-paper.ps1"
}

@"
# Al cambiar eula a true confirmas que aceptas el EULA de Minecraft:
# https://aka.ms/MinecraftEULA
eula=true
"@ | Set-Content -Path (Join-Path $serverDir "eula.txt") -Encoding ascii

Write-Host "EULA registrado. Ya puedes iniciar con .\start-server.ps1"

