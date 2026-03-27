[CmdletBinding()]
param(
  [ValidateSet("scheduler", "payment")]
  [string]$App
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"
$ProgressPreference = "SilentlyContinue"

$script:BackendRoot = Split-Path -Parent (Split-Path -Parent $PSScriptRoot)
$script:RuntimeRoot = Join-Path $script:BackendRoot ".runtime\spring-pages"

function Write-Step {
  param([string]$Message)
  Write-Host "[simple-console-smoke] $Message"
}

function Assert-Code {
  param(
    [string]$Name,
    [object]$Payload
  )

  if ($null -eq $Payload) {
    throw "$Name 返回为空。"
  }

  $code = [string]$Payload.code
  if ($code -notin @("00000", "200", "0")) {
    throw "$Name 返回码异常: $code"
  }
}

New-Item -ItemType Directory -Force -Path $script:RuntimeRoot | Out-Null
$resultPath = Join-Path $script:RuntimeRoot "$App-simple-console-smoke.json"
$session = New-Object Microsoft.PowerShell.Commands.WebRequestSession
$resultChecks = New-Object System.Collections.Generic.List[object]

if ($App -eq "scheduler") {
  $baseUrl = "http://127.0.0.1:18083/scheduler/api"
  $statusPath = "/job-console/auth/status"
  $loginPath = "/job-console/auth/login"
  $protectedJsonPaths = @(
    "/job-console/api/namespaces",
    "/job-console/api/default/task/list?pageNum=1&pageSize=5",
    "/job-console/api/payment/task/list?pageNum=1&pageSize=5"
  )
  $protectedHtmlPaths = @()
} else {
  $baseUrl = "http://127.0.0.1:18190/payment/api"
  $statusPath = "/payment-console/auth/status"
  $loginPath = "/payment-console/auth/login"
  $protectedJsonPaths = @(
    "/api/ops/overview",
    "/api/ops/scheduler/tasks",
    "/api/ops/notify/log/page?pageNum=1&pageSize=5"
  )
  $protectedHtmlPaths = @(
    "/payment-console/index.html"
  )
}

Write-Step "检查未登录状态 ..."
$statusPayload = Invoke-RestMethod -Uri ($baseUrl + $statusPath) -Method Get -WebSession $session -TimeoutSec 10
Assert-Code -Name "status-before-login" -Payload $statusPayload
$resultChecks.Add([ordered]@{
  name = "status-before-login"
  code = [string]$statusPayload.code
  authenticated = $statusPayload.data.authenticated
}) | Out-Null

Write-Step "执行默认账号密码登录 ..."
$loginPayload = Invoke-RestMethod `
  -Uri ($baseUrl + $loginPath) `
  -Method Post `
  -WebSession $session `
  -TimeoutSec 10 `
  -ContentType "application/json" `
  -Body (@{ username = "admin"; password = "admin123456" } | ConvertTo-Json)
Assert-Code -Name "login" -Payload $loginPayload
$resultChecks.Add([ordered]@{
  name = "login"
  code = [string]$loginPayload.code
  authenticated = $loginPayload.data.authenticated
}) | Out-Null

foreach ($path in $protectedJsonPaths) {
  Write-Step "校验 JSON 接口 $path ..."
  $payload = Invoke-RestMethod -Uri ($baseUrl + $path) -Method Get -WebSession $session -TimeoutSec 10
  Assert-Code -Name $path -Payload $payload
  $resultChecks.Add([ordered]@{
    name = $path
    code = [string]$payload.code
    hasData = $null -ne $payload.data
  }) | Out-Null
}

foreach ($path in $protectedHtmlPaths) {
  Write-Step "校验页面 $path ..."
  $response = Invoke-WebRequest -Uri ($baseUrl + $path) -Method Get -WebSession $session -TimeoutSec 10 -UseBasicParsing
  if ($response.StatusCode -ne 200) {
    throw "$path 返回状态异常: $($response.StatusCode)"
  }

  $resultChecks.Add([ordered]@{
    name = $path
    statusCode = $response.StatusCode
    contentLength = $response.Content.Length
  }) | Out-Null
}

$result = [ordered]@{
  app = $App
  executedAt = (Get-Date).ToString("yyyy-MM-dd HH:mm:ss")
  checks = $resultChecks
}

$result | ConvertTo-Json -Depth 6 | Set-Content -Path $resultPath -Encoding UTF8
Write-Step "冒烟验证通过。结果已写入 $resultPath"
Write-Host (Get-Content $resultPath -Raw)
