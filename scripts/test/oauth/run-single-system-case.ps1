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
$launch = Join-Path $root 'scripts\test\oauth\launch-system-client.ps1'
$flowScript = Join-Path $root 'scripts\test\oauth\run-system-flow.ps1'
$prefix = "system-$Protocol-$EnableEncryption"

function Stop-SystemProcess {
    $pidFile = Join-Path $logDir "$prefix.pid"
    if (Test-Path $pidFile) {
        $processId = Get-Content $pidFile | Select-Object -First 1
        if ($processId) {
            try { Stop-Process -Id ([int]$processId) -Force -ErrorAction Stop } catch {}
        }
        Remove-Item $pidFile -ErrorAction SilentlyContinue
    }

    Get-NetTCPConnection -State Listen -ErrorAction SilentlyContinue |
        Where-Object { $_.LocalPort -eq 18170 } |
        Select-Object -ExpandProperty OwningProcess -Unique |
        ForEach-Object { try { Stop-Process -Id $_ -Force -ErrorAction Stop } catch {} }

    Start-Sleep -Seconds 2
}

function Wait-LoginReady([int]$timeoutSec) {
    $deadline = (Get-Date).AddSeconds($timeoutSec)
    while ((Get-Date) -lt $deadline) {
        try {
            $body = @{
                username = 'sa'
                password = '0192023a7bbd73250516f069df18b500'
                verifyCodeKey = ''
                verifyCodeUlid = ''
                loginType = 'SYSTEM'
            } | ConvertTo-Json

            $resp = Invoke-RestMethod -Uri 'http://127.0.0.1:18170/system/api/v2/user/login' -Method Post -ContentType 'application/json' -Body $body
            if ($resp.code -eq '00000') {
                return $true
            }
        } catch {}
        Start-Sleep -Seconds 3
    }
    return $false
}

Stop-SystemProcess

powershell.exe -NoLogo -NoProfile -ExecutionPolicy Bypass -File $launch -Protocol $Protocol -EnableEncryption $EnableEncryption -Address $Address | Out-Null

if (-not (Wait-LoginReady 240)) {
    Write-Host "System app login not ready for $prefix"
    Get-Content (Join-Path $logDir "$prefix.out.log") -Tail 120
    Get-Content (Join-Path $logDir "$prefix.err.log") -Tail 120
    Stop-SystemProcess
    exit 1
}

try {
    $flow = powershell.exe -NoLogo -NoProfile -ExecutionPolicy Bypass -File $flowScript | ConvertFrom-Json
    [pscustomobject]@{
        protocol = $Protocol
        enableEncryption = [bool]::Parse($EnableEncryption)
        loginCode = $flow.loginCode
        loginUser = $flow.loginUser
        meCode = $flow.meCode
        meUser = $flow.meUser
        menuCode = $flow.menuCode
        menuCount = $flow.menuCount
        refreshCode = $flow.refreshCode
        refreshTokenPresent = $flow.refreshTokenPresent
        upgradeCode = $flow.upgradeCode
        upgradeTokenPresent = $flow.upgradeTokenPresent
        temporaryCode = $flow.temporaryCode
        temporaryTokenPresent = $flow.temporaryTokenPresent
        logoutCode = $flow.logoutCode
    } | ConvertTo-Json -Depth 6
} finally {
    Stop-SystemProcess
}
