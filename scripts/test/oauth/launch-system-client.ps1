param(
    [Parameter(Mandatory = $true)]
    [string]$Protocol,

    [Parameter(Mandatory = $true)]
    [string]$EnableEncryption,

    [Parameter(Mandatory = $true)]
    [string]$Address
)

$ErrorActionPreference = 'Stop'

$root = 'H:\workspace\2\spring-support-api-parent'
$logDir = Join-Path $root '.codex-logs\oauth'
$runner = Join-Path $root 'scripts\test\oauth\start-system-client.ps1'

New-Item -ItemType Directory -Force -Path $logDir | Out-Null

$prefix = "system-$Protocol-$EnableEncryption"
$stdout = Join-Path $logDir "$prefix.out.log"
$stderr = Join-Path $logDir "$prefix.err.log"
Remove-Item $stdout, $stderr -ErrorAction SilentlyContinue

$process = Start-Process -FilePath 'powershell.exe' `
    -ArgumentList '-NoLogo', '-NoProfile', '-ExecutionPolicy', 'Bypass', '-File', $runner, '-Protocol', $Protocol, '-EnableEncryption', $EnableEncryption, '-Address', $Address `
    -RedirectStandardOutput $stdout `
    -RedirectStandardError $stderr `
    -PassThru

$process.Id | Set-Content (Join-Path $logDir "$prefix.pid")
$process.Id
