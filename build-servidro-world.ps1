$ErrorActionPreference = "Stop"

$pluginDir = Join-Path $PSScriptRoot "custom-plugins\servidro-world"
$classes = Join-Path $pluginDir "build\classes"
$jar = Join-Path $pluginDir "build\ServidroWorld.jar"
$serverJar = Join-Path $PSScriptRoot "server\plugins\ServidroWorld.jar"

Remove-Item $classes -Recurse -Force -ErrorAction SilentlyContinue
New-Item -ItemType Directory -Path $classes -Force | Out-Null

$jars = Get-ChildItem (Join-Path $PSScriptRoot "server\libraries"), (Join-Path $PSScriptRoot "server\plugins") -Recurse -Filter *.jar |
    ForEach-Object { $_.FullName }
$classpath = $jars -join [IO.Path]::PathSeparator
$sources = Get-ChildItem (Join-Path $pluginDir "src\main\java") -Recurse -Filter *.java |
    ForEach-Object { $_.FullName }

javac -proc:none -encoding UTF-8 -cp $classpath -d $classes $sources
if ($LASTEXITCODE -ne 0) {
    throw "javac fallo"
}

Copy-Item (Join-Path $pluginDir "src\main\resources\*") $classes -Force
jar --create --file $jar -C $classes .
Copy-Item $jar $serverJar -Force

Remove-Item (Join-Path $PSScriptRoot "server\plugins\.paper-remapped\ServidroWorld.jar") -Force -ErrorAction SilentlyContinue
Write-Host "ServidroWorld compilado e instalado en $serverJar" -ForegroundColor Green
