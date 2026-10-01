# package.ps1 - build + package the oauth deploy bundle (Windows dev machine).
#
# ASCII-only on purpose: PowerShell 5.1 parses a non-ASCII .ps1 by the system
# ANSI code page and can fail with ParserError before param() is even reached.
# See the repo AGENTS.md encoding rules.
#
# Output: dist/oauth-app.tar.gz containing
#   <ver>.jar, lib/, config/, deploy/
# Upload + deploy (see deploy/README.md):
#   pscp -batch -pw <pass> dist/oauth-app.tar.gz <user>@<host>:/tmp/oauth-app.tar.gz
#   ssh <user>@<host> "sudo BUNDLE=/tmp/oauth-app.tar.gz bash /home/kali/oauth/app/deploy/deploy.sh"
#
# Usage: pwsh -File deploy/package.ps1
[CmdletBinding()]
param(
    [string]$Maven = "mvn"
)

$ErrorActionPreference = "Stop"

$moduleDir = Split-Path -Parent $PSScriptRoot
$jar = "spring-api-support-oauth-starter-4.0.0.42.jar"
$target = Join-Path $moduleDir "target"
$dist = Join-Path $moduleDir "dist"
$out = Join-Path $dist "oauth-app.tar.gz"

Write-Host "==> build $jar"
& $Maven -f (Join-Path $moduleDir "pom.xml") clean package -DskipTests
if ($LASTEXITCODE -ne 0) { throw "maven build failed (exit=$LASTEXITCODE)" }

if (-not (Test-Path (Join-Path $target $jar))) { throw "build produced no $jar" }
if (-not (Test-Path (Join-Path $target "lib"))) { throw "build produced no target/lib" }
if (-not (Test-Path (Join-Path $target "config"))) { throw "build produced no target/config" }

Write-Host "==> package"
New-Item -ItemType Directory -Path $dist -Force | Out-Null
Remove-Item $out -Force -ErrorAction SilentlyContinue

# Tar straight from the source dirs (no Copy-Item): copying hundreds of jars
# has been observed to produce all-NUL files on this host.
& tar -czf $out -C $target $jar lib config -C $moduleDir deploy
if ($LASTEXITCODE -ne 0) { throw "tar failed (exit=$LASTEXITCODE)" }

$fi = Get-Item $out
$sha = (Get-FileHash $out -Algorithm SHA256).Hash.ToLower()
Write-Host ("==> done: {0}" -f $out)
Write-Host ("    size={0:N1} MB  sha256={1}" -f ($fi.Length / 1MB), $sha)
