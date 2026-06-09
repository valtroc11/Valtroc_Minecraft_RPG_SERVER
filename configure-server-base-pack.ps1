param(
    [string]$ServerDir = (Join-Path $PSScriptRoot "server")
)

$ErrorActionPreference = "Stop"

$propertiesFile = Join-Path $ServerDir "server.properties"
$packUrl = "https\://cdn.modrinth.com/data/hJAzl1Bs/versions/ScfKVGaF/Excalibur_V26.1_01.zip"
$packSha1 = "f14f03d76c48e6c99a02ccefff0070c4fe3f110b"

if (-not (Test-Path $propertiesFile)) {
    Write-Host "No encontre $propertiesFile" -ForegroundColor Yellow
    exit 0
}

Copy-Item -Path $propertiesFile -Destination "$propertiesFile.bak" -Force
$content = Get-Content -Path $propertiesFile -Raw
$content = [regex]::Replace($content, '(?m)^require-resource-pack=.*$', 'require-resource-pack=false')
$content = [regex]::Replace($content, '(?m)^resource-pack=.*$', "resource-pack=$packUrl")
$content = [regex]::Replace($content, '(?m)^resource-pack-sha1=.*$', "resource-pack-sha1=$packSha1")
Set-Content -Path $propertiesFile -Value $content -Encoding UTF8

Write-Host "Pack base Excalibur configurado en $propertiesFile" -ForegroundColor Green
