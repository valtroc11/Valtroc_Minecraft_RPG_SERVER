param(
    [string]$WorldName = "world"
)

$ErrorActionPreference = "Stop"

$serverDir = [System.IO.Path]::GetFullPath((Join-Path $PSScriptRoot "server"))
$propertiesFile = Join-Path $serverDir "server.properties"
$timestamp = Get-Date -Format "yyyyMMdd-HHmmss"
$backupDir = Join-Path $serverDir "world-backups\$timestamp"
$worldDir = Join-Path $serverDir $WorldName
$datapacksDir = Join-Path $worldDir "datapacks"

if (-not (Test-Path $propertiesFile)) {
    throw "No existe server\server.properties. Instala Paper primero."
}

if (Get-Process java -ErrorAction SilentlyContinue) {
    throw "Hay un proceso Java activo. Deten el servidor antes de regenerar el mundo."
}

function Assert-ServerChildPath {
    param([string]$Path)

    $resolved = [System.IO.Path]::GetFullPath($Path)
    if (-not $resolved.StartsWith("$serverDir\", [System.StringComparison]::OrdinalIgnoreCase)) {
        throw "Ruta fuera de server: $resolved"
    }
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

$existingWorlds = @(
    (Join-Path $serverDir $WorldName),
    (Join-Path $serverDir "${WorldName}_nether"),
    (Join-Path $serverDir "${WorldName}_the_end")
) | Where-Object { Test-Path $_ }

if ($existingWorlds.Count -gt 0) {
    Assert-ServerChildPath -Path $backupDir
    New-Item -ItemType Directory -Path $backupDir -Force | Out-Null

    foreach ($existingWorld in $existingWorlds) {
        Assert-ServerChildPath -Path $existingWorld
        Move-Item -LiteralPath $existingWorld -Destination $backupDir
        Write-Host "Respaldado: $existingWorld"
    }
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
    },
    @{
        Name = "Dungeons and Taverns"
        ProjectId = "tpehi7ww"
        FileName = "Dungeons-and-Taverns.zip"
    },
    @{
        Name = "Structory"
        ProjectId = "aKCwCJlY"
        FileName = "Structory.zip"
    },
    @{
        Name = "Structory Towers"
        ProjectId = "j3FONRYr"
        FileName = "Structory-Towers.zip"
    },
    @{
        Name = "DnT Enchant Disabler"
        ProjectId = "jr7t09l6"
        FileName = "DnT-Enchant-Disabler.zip"
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

$properties = Get-Content $propertiesFile
$properties = $properties -replace '^level-name=.*$', "level-name=$WorldName"
Set-Content -Path $propertiesFile -Value $properties -Encoding ascii

Write-Host ""
Write-Host "Mundo activo: $WorldName"
if ($existingWorlds.Count -gt 0) {
    Write-Host "Respaldo anterior: $backupDir"
}
Write-Host "Ejecuta .\start-server.ps1 para generar el nuevo mapa."
