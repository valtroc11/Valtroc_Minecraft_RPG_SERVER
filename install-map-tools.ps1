$ErrorActionPreference = "Stop"

$minecraftVersion = "1.21.11"
$pluginsDir = Join-Path $PSScriptRoot "server\plugins"
$projects = @(
    @{ Slug = "worldedit"; Version = "7.3.19"; FileName = "WorldEdit.jar" },
    @{ Slug = "worldguard"; Version = "7.0.16"; FileName = "WorldGuard.jar" }
)

New-Item -ItemType Directory -Force -Path $pluginsDir | Out-Null

foreach ($project in $projects) {
    $slug = $project.Slug
    $apiUrl = "https://api.modrinth.com/v2/project/$slug/version?loaders=[%22paper%22]&game_versions=[%22$minecraftVersion%22]"

    Write-Host "Buscando $slug para Paper $minecraftVersion..."
    $versions = Invoke-RestMethod -Uri $apiUrl
    $version = $versions |
        Where-Object { $_.version_number -eq $project.Version } |
        Select-Object -First 1

    if (-not $version) {
        throw "No se encontro $slug $($project.Version) para Paper $minecraftVersion."
    }

    $file = $version.files | Where-Object { $_.primary } | Select-Object -First 1
    if (-not $file) {
        $file = $version.files | Select-Object -First 1
    }

    if (-not $file.url) {
        throw "No se encontro una descarga para $slug."
    }

    Write-Host "Descargando $slug $($version.version_number)..."
    Invoke-WebRequest -Uri $file.url -OutFile (Join-Path $pluginsDir $project.FileName)
}

Write-Host ""
Write-Host "Herramientas de mapa instaladas."
Write-Host "Inicia el servidor con: .\start-server.ps1"
