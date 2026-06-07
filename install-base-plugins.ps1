$ErrorActionPreference = "Stop"

$minecraftVersion = "1.21.11"
$pluginsDir = Join-Path $PSScriptRoot "server\plugins"
$projects = @(
    @{ Slug = "luckperms"; FileName = "LuckPerms.jar" },
    @{ Slug = "chunky"; FileName = "Chunky.jar" },
    @{ Slug = "placeholderapi"; FileName = "PlaceholderAPI.jar" },
    @{ Slug = "griefprevention"; FileName = "GriefPrevention.jar" }
)

New-Item -ItemType Directory -Force -Path $pluginsDir | Out-Null

foreach ($project in $projects) {
    $slug = $project.Slug
    $apiUrl = "https://api.modrinth.com/v2/project/$slug/version?loaders=[%22paper%22]&game_versions=[%22$minecraftVersion%22]"

    Write-Host "Buscando $slug para Paper $minecraftVersion..."
    $versions = Invoke-RestMethod -Uri $apiUrl
    $version = $versions | Select-Object -First 1

    if (-not $version) {
        throw "No se encontro una version compatible de $slug para Paper $minecraftVersion."
    }

    $file = $version.files | Where-Object { $_.primary } | Select-Object -First 1
    if (-not $file) {
        $file = $version.files | Select-Object -First 1
    }

    if (-not $file.url) {
        throw "No se encontro una descarga para $slug."
    }

    $destination = Join-Path $pluginsDir $project.FileName
    Write-Host "Descargando $slug $($version.version_number)..."
    Invoke-WebRequest -Uri $file.url -OutFile $destination
}

Write-Host ""
Write-Host "Plugins base instalados."
Write-Host "CoreProtect se instala manualmente desde su canal oficial:"
Write-Host "https://hangar.papermc.io/CORE/CoreProtect"
Write-Host ""
Write-Host "Inicia el servidor con: .\start-server.ps1"

