param(
    [string]$NgrokPath,
    [string]$JavaPath = "C:\Program Files\Eclipse Adoptium\jdk-21.0.11.10-hotspot\bin\java.exe",
    [int]$ServerPort = 25565
)

$ErrorActionPreference = "Stop"

$projectRoot = $PSScriptRoot
$serverDir = Join-Path $projectRoot "server"
$paperJar = Join-Path $serverDir "paper.jar"
$eulaFile = Join-Path $serverDir "eula.txt"
$serverOutLog = Join-Path $serverDir "codex-server.out.log"
$serverErrLog = Join-Path $serverDir "codex-server.err.log"
$ngrokOutLog = Join-Path $serverDir "ngrok.log"
$ngrokErrLog = Join-Path $serverDir "ngrok.err.log"

function Resolve-NgrokExecutable {
    param([string]$PreferredPath)

    if ($PreferredPath -and (Test-Path $PreferredPath)) {
        return (Resolve-Path $PreferredPath).Path
    }

    $command = Get-Command ngrok -ErrorAction SilentlyContinue
    if ($command) {
        return $command.Source
    }

    $commonPaths = @(
        (Join-Path $env:LOCALAPPDATA "ngrok\ngrok.exe"),
        (Join-Path $env:ProgramData "chocolatey\bin\ngrok.exe"),
        (Join-Path $env:USERPROFILE "scoop\shims\ngrok.exe"),
        (Join-Path $env:USERPROFILE "Downloads\ngrok.exe")
    )

    foreach ($candidate in $commonPaths) {
        if (Test-Path $candidate) {
            return (Resolve-Path $candidate).Path
        }
    }

    return $null
}

function Get-PaperProcess {
    Get-CimInstance Win32_Process |
        Where-Object { $_.Name -eq "java.exe" -and $_.CommandLine -like "*paper.jar*" } |
        Select-Object -First 1
}

function Get-NgrokProcess {
    param([int]$Port)

    Get-CimInstance Win32_Process |
        Where-Object { $_.Name -eq "ngrok.exe" -and $_.CommandLine -like "*tcp $Port*" } |
        Select-Object -First 1
}

function Get-NgrokTcpAddress {
    try {
        $response = Invoke-RestMethod -Uri "http://127.0.0.1:4040/api/tunnels" -TimeoutSec 3
        $tcpTunnel = $response.tunnels | Where-Object { $_.proto -eq "tcp" } | Select-Object -First 1
        if ($tcpTunnel -and $tcpTunnel.public_url) {
            return ($tcpTunnel.public_url -replace "^tcp://", "")
        }
    }
    catch {
    }

    if (Test-Path $ngrokOutLog) {
        $line = Select-String -Path $ngrokOutLog -Pattern "url=tcp://([^ ]+)" | Select-Object -Last 1
        if ($line -and $line.Matches.Count -gt 0) {
            return $line.Matches[0].Groups[1].Value
        }
    }

    return $null
}

if (-not (Test-Path $paperJar)) {
    throw "Paper no esta instalado. Ejecuta primero .\setup-paper.ps1"
}

if (-not (Test-Path $eulaFile)) {
    throw "Falta aceptar el EULA. Lee https://aka.ms/MinecraftEULA y ejecuta .\accept-eula.ps1 si estas de acuerdo."
}

if (-not (Test-Path $JavaPath)) {
    throw "No encontre Java en '$JavaPath'. Ajusta -JavaPath o instala Java 21."
}

$resolvedNgrok = Resolve-NgrokExecutable -PreferredPath $NgrokPath
if (-not $resolvedNgrok) {
    throw "No encontre ngrok. Instala ngrok, ejecuta 'ngrok config add-authtoken <tu_token>' y vuelve a correr este script."
}

$paperProcess = Get-PaperProcess
if (-not $paperProcess) {
    Write-Host "Iniciando Paper en segundo plano..." -ForegroundColor Cyan
    $paperStart = Start-Process -FilePath $JavaPath `
        -ArgumentList '-Djavax.net.ssl.trustStoreType=Windows-ROOT','-Djavax.net.ssl.trustStore=NUL','-Xms2G','-Xmx4G','-jar','paper.jar','--nogui' `
        -WorkingDirectory $serverDir `
        -RedirectStandardOutput $serverOutLog `
        -RedirectStandardError $serverErrLog `
        -PassThru `
        -WindowStyle Hidden
    Start-Sleep -Seconds 10
    $paperProcess = Get-PaperProcess
    if (-not $paperProcess) {
        throw "Paper no quedo corriendo. Revisa $serverOutLog y $serverErrLog."
    }
    Write-Host ("Paper iniciado. PID: {0}" -f $paperProcess.ProcessId) -ForegroundColor Green
}
else {
    Write-Host ("Paper ya estaba corriendo. PID: {0}" -f $paperProcess.ProcessId) -ForegroundColor Yellow
}

$listener = Get-NetTCPConnection -State Listen -LocalPort $ServerPort -ErrorAction SilentlyContinue | Select-Object -First 1
if (-not $listener) {
    Write-Warning "Paper aun no esta escuchando en el puerto $ServerPort. El tunel se iniciara igual, pero conviene revisar latest.log si tarda mucho."
}

$ngrokProcess = Get-NgrokProcess -Port $ServerPort
if (-not $ngrokProcess) {
    Write-Host "Iniciando ngrok tcp $ServerPort..." -ForegroundColor Cyan
    $ngrokStart = Start-Process -FilePath $resolvedNgrok `
        -ArgumentList "tcp", "$ServerPort", "--log=stdout" `
        -WorkingDirectory $serverDir `
        -RedirectStandardOutput $ngrokOutLog `
        -RedirectStandardError $ngrokErrLog `
        -PassThru `
        -WindowStyle Hidden
    Start-Sleep -Seconds 5
    $ngrokProcess = Get-NgrokProcess -Port $ServerPort
    if (-not $ngrokProcess) {
        throw "Ngrok no quedo corriendo. Revisa $ngrokOutLog y $ngrokErrLog."
    }
    Write-Host ("Ngrok iniciado. PID: {0}" -f $ngrokProcess.ProcessId) -ForegroundColor Green
}
else {
    Write-Host ("Ngrok ya estaba corriendo. PID: {0}" -f $ngrokProcess.ProcessId) -ForegroundColor Yellow
}

Start-Sleep -Seconds 2
$publicAddress = Get-NgrokTcpAddress

Write-Host ""
Write-Host "Estado actual" -ForegroundColor Cyan
Write-Host ("- Minecraft: localhost:{0}" -f $ServerPort)
if ($publicAddress) {
    Write-Host ("- Ngrok TCP: {0}" -f $publicAddress) -ForegroundColor Green
}
else {
    Write-Host ("- Ngrok TCP: aun sin URL publica visible. Revisa {0}" -f $ngrokOutLog) -ForegroundColor Yellow
}
