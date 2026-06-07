param(
    [int]$ServerPort = 25565
)

$ErrorActionPreference = "Stop"

$paperProcesses = Get-CimInstance Win32_Process |
    Where-Object { $_.Name -eq "java.exe" -and $_.CommandLine -like "*paper.jar*" }

$ngrokProcesses = Get-CimInstance Win32_Process |
    Where-Object { $_.Name -eq "ngrok.exe" -and $_.CommandLine -like "*tcp $ServerPort*" }

if ($paperProcesses) {
    $paperProcesses | ForEach-Object { Stop-Process -Id $_.ProcessId -Force }
    Write-Host "Paper detenido." -ForegroundColor Green
}
else {
    Write-Host "Paper no estaba corriendo." -ForegroundColor Yellow
}

if ($ngrokProcesses) {
    $ngrokProcesses | ForEach-Object { Stop-Process -Id $_.ProcessId -Force }
    Write-Host "Ngrok detenido." -ForegroundColor Green
}
else {
    Write-Host "Ngrok no estaba corriendo." -ForegroundColor Yellow
}
