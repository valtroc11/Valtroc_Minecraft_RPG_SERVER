$ErrorActionPreference = "Stop"

$serverDir = Join-Path $PSScriptRoot "server"
$paperJar = Join-Path $serverDir "paper.jar"
$eulaFile = Join-Path $serverDir "eula.txt"

if (-not (Test-Path $paperJar)) {
    throw "Paper no esta instalado. Ejecuta primero .\setup-paper.ps1"
}

if (-not (Test-Path $eulaFile)) {
    throw "Falta aceptar el EULA. Lee https://aka.ms/MinecraftEULA y ejecuta .\accept-eula.ps1 si estas de acuerdo."
}

Push-Location $serverDir
try {
    java `
        "-Djavax.net.ssl.trustStoreType=Windows-ROOT" `
        "-Djavax.net.ssl.trustStore=NUL" `
        -Xms2G `
        -Xmx4G `
        -jar paper.jar `
        --nogui
}
finally {
    Pop-Location
}
