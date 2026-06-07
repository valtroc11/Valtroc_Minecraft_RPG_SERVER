$ErrorActionPreference = "Stop"

$pluginRoot = Join-Path $PSScriptRoot "custom-plugins\servidro-rpg"
$sourceDir = Join-Path $pluginRoot "src\main\java"
$resourceDir = Join-Path $pluginRoot "src\main\resources"
$buildDir = Join-Path $pluginRoot "build"
$classesDir = Join-Path $buildDir "classes"
$jarFile = Join-Path $buildDir "ServidroRpg.jar"
$pluginsDir = Join-Path $PSScriptRoot "server\plugins"

if (Test-Path $classesDir) {
    Remove-Item -LiteralPath $classesDir -Recurse -Force
}
New-Item -ItemType Directory -Force -Path $classesDir | Out-Null

$libraryJars = Get-ChildItem (Join-Path $PSScriptRoot "server\libraries") -Recurse -Filter *.jar |
    ForEach-Object { $_.FullName }
$pluginJars = @(
    (Join-Path $pluginsDir "VaultUnlocked.jar")
)
$classpath = ($libraryJars + $pluginJars) -join [IO.Path]::PathSeparator
$sources = Get-ChildItem $sourceDir -Recurse -Filter *.java | ForEach-Object { $_.FullName }

Write-Host "Compilando ServidroRpg..."
& javac -encoding UTF-8 -classpath $classpath -d $classesDir $sources
if ($LASTEXITCODE -ne 0) {
    throw "La compilacion fallo."
}

Copy-Item (Join-Path $resourceDir "*") $classesDir -Recurse -Force
Push-Location $classesDir
try {
    & jar --create --file $jarFile .
    if ($LASTEXITCODE -ne 0) {
        throw "No se pudo crear el jar."
    }
}
finally {
    Pop-Location
}

Copy-Item $jarFile (Join-Path $pluginsDir "ServidroRpg.jar") -Force
Write-Host "Plugin instalado en server\plugins\ServidroRpg.jar"

