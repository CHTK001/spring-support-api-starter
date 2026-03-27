$ErrorActionPreference = 'Stop'

$root = 'H:\workspace\2\spring-support-api-parent'
$logDir = Join-Path $root '.codex-logs\oauth'
$runner = Join-Path $root 'scripts\test\oauth\start-oauth-server.ps1'

New-Item -ItemType Directory -Force -Path $logDir | Out-Null

$stdout = Join-Path $logDir 'oauth-server.out.log'
$stderr = Join-Path $logDir 'oauth-server.err.log'
Remove-Item $stdout, $stderr -ErrorAction SilentlyContinue

$process = Start-Process -FilePath 'powershell.exe' `
    -ArgumentList '-NoLogo', '-NoProfile', '-ExecutionPolicy', 'Bypass', '-File', $runner `
    -RedirectStandardOutput $stdout `
    -RedirectStandardError $stderr `
    -PassThru

$process.Id | Set-Content (Join-Path $logDir 'oauth-server.pid')
$process.Id
