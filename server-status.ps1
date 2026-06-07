param(
    [int]$ServerPort = 25565
)

$ErrorActionPreference = "Stop"

$serverDir = Join-Path $PSScriptRoot "server"
$ngrokOutLog = Join-Path $serverDir "ngrok.log"

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

$paper = Get-PaperProcess
$ngrok = Get-NgrokProcess -Port $ServerPort
$listener = Get-NetTCPConnection -State Listen -LocalPort $ServerPort -ErrorAction SilentlyContinue | Select-Object -First 1
$publicAddress = Get-NgrokTcpAddress

if ($paper) {
    Write-Host ("Minecraft corriendo. PID: {0}" -f $paper.ProcessId) -ForegroundColor Green
}
else {
    Write-Host "Minecraft no esta corriendo." -ForegroundColor Red
}

if ($listener) {
    Write-Host ("Puerto escuchando: {0}" -f $ServerPort) -ForegroundColor Green
}
else {
    Write-Host ("Puerto {0} no esta escuchando." -f $ServerPort) -ForegroundColor Yellow
}

if ($ngrok) {
    Write-Host ("Ngrok corriendo. PID: {0}" -f $ngrok.ProcessId) -ForegroundColor Green
}
else {
    Write-Host "Ngrok no esta corriendo." -ForegroundColor Red
}

if ($publicAddress) {
    Write-Host ("IP publica ngrok: {0}" -f $publicAddress) -ForegroundColor Cyan
}
else {
    Write-Host "Ngrok no expuso aun una direccion TCP visible." -ForegroundColor Yellow
}
