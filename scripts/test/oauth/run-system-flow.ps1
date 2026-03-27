$ErrorActionPreference = 'Stop'

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
} | ConvertTo-Json -Depth 6
