$ErrorActionPreference = 'Stop'

$root = 'H:\workspace\2\spring-support-api-parent'
$launch = Join-Path $root 'scripts\test\oauth\launch-system-client.ps1'
$logDir = Join-Path $root '.codex-logs\oauth'

function Stop-SystemProcess([string]$prefix) {
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

function Wait-Port([int]$port, [int]$timeoutSec) {
    $deadline = (Get-Date).AddSeconds($timeoutSec)
    while ((Get-Date) -lt $deadline) {
        try {
            $client = [System.Net.Sockets.TcpClient]::new()
            $iar = $client.BeginConnect('127.0.0.1', $port, $null, $null)
            if ($iar.AsyncWaitHandle.WaitOne(1000)) {
                $client.EndConnect($iar)
                $client.Close()
                return $true
            }
            $client.Close()
        } catch {}
        Start-Sleep -Seconds 2
    }
    return $false
}

function Invoke-SystemFlow() {
    $loginBody = @{
        username = 'sa'
        password = '0192023a7bbd73250516f069df18b500'
        verifyCodeKey = ''
        verifyCodeUlid = ''
        loginType = 'SYSTEM'
    } | ConvertTo-Json

    $login = Invoke-RestMethod -Uri 'http://127.0.0.1:18170/system/api/v2/user/login' -Method Post -ContentType 'application/json' -Body $loginBody
    $token = $login.data.accessToken
    $refresh = $login.data.refreshToken
    $headers = @{ Authorization = "Bearer $token" }
    $me = Invoke-RestMethod -Uri 'http://127.0.0.1:18170/system/api/v2/user/me' -Headers $headers
    $menu = Invoke-RestMethod -Uri 'http://127.0.0.1:18170/system/api/v2/user/menu' -Headers $headers
    $refreshResp = Invoke-RestMethod -Uri 'http://127.0.0.1:18170/system/api/v2/user/refresh-token' -Method Post -ContentType 'application/json' -Body (@{ refreshToken = $refresh } | ConvertTo-Json)
    $refreshToken = $refreshResp.data.accessToken
    $upgrade = Invoke-RestMethod -Uri 'http://127.0.0.1:18170/system/api/v2/user/upgrade' -Method Post -Headers @{ 'x-oauth-token' = $refreshToken; 'x-oauth-upgrade-type' = 'VERSION' }
    $upgradeToken = $upgrade.data.accessToken
    $temp = Invoke-RestMethod -Uri 'http://127.0.0.1:18170/system/api/v2/user/temporary-token' -Method Post -Headers @{ 'x-oauth-token' = $upgradeToken } -ContentType 'application/json' -Body (@{ expireTime = 60; allowedUrls = @('/system/api/v2/user/me') } | ConvertTo-Json)
    $logout = Invoke-RestMethod -Uri 'http://127.0.0.1:18170/system/api/v2/user/logout' -Method Delete -Headers @{ 'x-oauth-token' = $upgradeToken }

    [pscustomobject]@{
        loginCode = $login.code
        loginUser = $login.data.userInfo.sysUserUsername
        meCode = $me.code
        meUser = $me.data.sysUserUsername
        menuCode = $menu.code
        menuCount = @($menu.data).Count
        refreshCode = $refreshResp.code
        refreshTokenPresent = [bool]$refreshResp.data.accessToken
        upgradeCode = $upgrade.code
        upgradeTokenPresent = [bool]$upgrade.data.accessToken
        temporaryCode = $temp.code
        temporaryTokenPresent = [bool]$temp.data
        logoutCode = $logout.code
    }
}

$matrix = @(
    @{ protocol = 'armeria'; enable = 'true'; address = 'http://127.0.0.1:19180' },
    @{ protocol = 'armeria'; enable = 'false'; address = 'http://127.0.0.1:19180' },
    @{ protocol = 'rsocket'; enable = 'true'; address = 'rsocket://127.0.0.1:7000' },
    @{ protocol = 'rsocket'; enable = 'false'; address = 'rsocket://127.0.0.1:7000' }
)

$results = @()
foreach ($item in $matrix) {
    $prefix = "system-$($item.protocol)-$($item.enable)"
    Stop-SystemProcess $prefix
    powershell.exe -NoLogo -NoProfile -ExecutionPolicy Bypass -File $launch -Protocol $item.protocol -EnableEncryption $item.enable -Address $item.address | Out-Null

    if (-not (Wait-Port 18170 240)) {
        Write-Host "System app did not start for $prefix"
        Get-Content (Join-Path $logDir "$prefix.out.log") -Tail 120
        Get-Content (Join-Path $logDir "$prefix.err.log") -Tail 120
        exit 1
    }

    Start-Sleep -Seconds 6

    try {
        $flow = Invoke-SystemFlow
        $results += [pscustomobject]@{
            protocol = $item.protocol
            enableEncryption = [bool]::Parse($item.enable)
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
        }
    } catch {
        Write-Host "System flow failed for $prefix"
        Write-Host $_
        Get-Content (Join-Path $logDir "$prefix.out.log") -Tail 120
        Get-Content (Join-Path $logDir "$prefix.err.log") -Tail 120
        exit 1
    } finally {
        Stop-SystemProcess $prefix
    }
}

$results | ConvertTo-Json -Depth 6
