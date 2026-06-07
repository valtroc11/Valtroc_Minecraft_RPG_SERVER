param(
    [string]$WorldName = "preview_terralith_tectonic"
)

$ErrorActionPreference = "Stop"

$serverDir = Join-Path $PSScriptRoot "server"
$propertiesFile = Join-Path $serverDir "server.properties"
$worldDir = Join-Path $serverDir $WorldName
$datapacksDir = Join-Path $worldDir "datapacks"

if (-not (Test-Path $propertiesFile)) {
    throw "No existe server\server.properties. Instala Paper primero."
}

if (Get-Process java -ErrorAction SilentlyContinue) {
    throw "Hay un proceso Java activo. Deten el servidor antes de cambiar el mundo."
}

if (Test-Path (Join-Path $worldDir "level.dat")) {
    throw "El mundo '$WorldName' ya fue generado. No se modifico para evitar mezclar generadores."
}

function Get-CompatibleDatapack {
    param(
        [string]$ProjectId,
        [string]$DisplayName
    )

    $uri = "https://api.modrinth.com/v2/project/$ProjectId/version?loaders=[%22datapack%22]&game_versions=[%221.21.11%22]"
    $response = Invoke-RestMethod -Uri $uri
    $versions = @($response)
    if ($versions.Count -eq 0) {
        throw "No se encontro una version datapack de $DisplayName para Minecraft 1.21.11."
    }

    return $versions | Select-Object -First 1
}

New-Item -ItemType Directory -Path $datapacksDir -Force | Out-Null

$packs = @(
    @{
        Name = "Terralith"
        ProjectId = "8oi3bsk5"
        FileName = "Terralith.zip"
    },
    @{
        Name = "Tectonic"
        ProjectId = "lWDHr9jE"
        FileName = "Tectonic.zip"
    }
)

foreach ($pack in $packs) {
    $version = Get-CompatibleDatapack -ProjectId $pack.ProjectId -DisplayName $pack.Name
    $file = $version.files | Where-Object { $_.primary } | Select-Object -First 1
    if (-not $file) {
        $file = $version.files | Select-Object -First 1
    }

    $destination = Join-Path $datapacksDir $pack.FileName
    Invoke-WebRequest -Uri $file.url -OutFile $destination
    Write-Host "$($pack.Name) $($version.version_number) instalado en $destination"
}

$backupFile = "$propertiesFile.before-world-preview"
if (-not (Test-Path $backupFile)) {
    Copy-Item -Path $propertiesFile -Destination $backupFile
}

$properties = Get-Content $propertiesFile
$properties = $properties -replace '^level-name=.*$', "level-name=$WorldName"
Set-Content -Path $propertiesFile -Value $properties -Encoding ascii

Write-Host ""
Write-Host "Mundo activo: $WorldName"
Write-Host "El mundo anterior permanece intacto en server\world."
Write-Host "Ejecuta .\start-server.ps1 para generar la previsualizacion."
