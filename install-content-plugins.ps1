$ErrorActionPreference = "Stop"

$minecraftVersion = "1.21.11"
$pluginsDir = Join-Path $PSScriptRoot "server\plugins"
$projects = @(
    @{ Slug = "mythicmobs"; FileName = "MythicMobs.jar" }
)

New-Item -ItemType Directory -Force -Path $pluginsDir | Out-Null

foreach ($project in $projects) {
    $slug = $project.Slug
    $apiUrl = "https://api.modrinth.com/v2/project/$slug/version?loaders=[%22paper%22]&game_versions=[%22$minecraftVersion%22]"

    Write-Host "Buscando $slug para Paper $minecraftVersion..."
    $versions = Invoke-RestMethod -Uri $apiUrl
    $version = $versions | Where-Object { $_.version_type -eq "release" } | Select-Object -First 1

    if (-not $version) {
        throw "No se encontro una version estable de $slug para Paper $minecraftVersion."
    }

    $file = $version.files | Where-Object { $_.primary } | Select-Object -First 1
    if (-not $file) {
        $file = $version.files | Select-Object -First 1
    }

    Write-Host "Descargando $slug $($version.version_number)..."
    Invoke-WebRequest -Uri $file.url -OutFile (Join-Path $pluginsDir $project.FileName)
}

$mythicDir = Join-Path $pluginsDir "MythicMobs"
$contentDir = Join-Path $PSScriptRoot "content\mythicmobs"
New-Item -ItemType Directory -Force -Path (Join-Path $mythicDir "mobs") | Out-Null
New-Item -ItemType Directory -Force -Path (Join-Path $mythicDir "skills") | Out-Null
Copy-Item (Join-Path $contentDir "mobs\*") (Join-Path $mythicDir "mobs") -Force
Copy-Item (Join-Path $contentDir "skills\*") (Join-Path $mythicDir "skills") -Force

$exampleDialogs = Join-Path $mythicDir "dialogs\ExampleDialogs.yml"
if (Test-Path $exampleDialogs) {
    Remove-Item -LiteralPath $exampleDialogs -Force
}

Write-Host ""
Write-Host "Plugins de contenido instalados."
Write-Host "Inicia el servidor con: .\start-server.ps1"
