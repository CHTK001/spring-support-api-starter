[CmdletBinding()]
param(
  [ValidateSet("static", "full")]
  [string]$Mode = "static"
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"
$ProgressPreference = "SilentlyContinue"

$script:BackendRoot = Split-Path -Parent (Split-Path -Parent $PSScriptRoot)
$script:WorkspaceRoot = Split-Path -Parent $script:BackendRoot
$script:FrontendRoot = Join-Path $script:WorkspaceRoot "vue-support-parent-starter"
$script:RuntimeRoot = Join-Path $script:BackendRoot ".runtime"
$script:LogRoot = Join-Path $script:RuntimeRoot "logs"

function Write-Step {
  param([string]$Message)
  Write-Host "[dev-stack] $Message"
}

function Test-PortListening {
  param([int]$Port)

  return [bool](Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue)
}

function Test-HttpReady {
  param([string]$Url)

  try {
    $response = Invoke-WebRequest -Uri $Url -UseBasicParsing -TimeoutSec 5
    return $response.StatusCode -ge 200 -and $response.StatusCode -lt 500
  } catch {
    return $false
  }
}

function Wait-Until {
  param(
    [string]$Name,
    [scriptblock]$Condition,
    [int]$TimeoutSeconds = 180
  )

  $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
  while ((Get-Date) -lt $deadline) {
    if (& $Condition) {
      return
    }
    Start-Sleep -Seconds 2
  }

  throw "$Name 未在 $TimeoutSeconds 秒内就绪。"
}

function Get-Java25Home {
  $candidates = New-Object System.Collections.Generic.List[string]
  if ($env:JAVA_HOME) {
    $candidates.Add($env:JAVA_HOME)
  }
  $candidates.Add("C:\Program Files\Amazon Corretto\jdk25.0.1_8")
  $candidates.Add("C:\Program Files\Amazon Corretto\jdk25")
  $candidates.Add("C:\Program Files\Java\jdk-25")
  $candidates.Add("C:\Program Files\Java\jdk-25.0.1")
  $candidates.Add("C:\Program Files\Java\jdk-25.0.2")

  foreach ($candidate in ($candidates | Select-Object -Unique)) {
    if ([string]::IsNullOrWhiteSpace($candidate)) {
      continue
    }

    $javaExe = Join-Path $candidate "bin\java.exe"
    if (-not (Test-Path $javaExe)) {
      continue
    }

    $versionOutput = cmd /c """$javaExe"" -version 2>&1" | Out-String
    if ($versionOutput -match 'version "25(\.|")' -or $versionOutput -match 'openjdk 25') {
      return $candidate
    }
  }

  throw "未检测到 Java 25。请先安装 JDK 25，或将 JAVA_HOME 指向 Java 25。"
}

function Start-BackgroundProcess {
  param(
    [string]$Name,
    [string]$WorkDir,
    [string]$Command,
    [string]$LogPath,
    [hashtable]$Environment = @{}
  )

  $envLines = foreach ($entry in $Environment.GetEnumerator()) {
    $escapedValue = $entry.Value.Replace("'", "''")
    "`$env:$($entry.Key) = '$escapedValue'"
  }

  $escapedWorkDir = $WorkDir.Replace("'", "''")
  $escapedLogPath = $LogPath.Replace("'", "''")

  $launcher = @(
    '$ErrorActionPreference = ''Stop'''
    '$ProgressPreference = ''SilentlyContinue'''
    '$PSNativeCommandUseErrorActionPreference = $false'
  ) + $envLines + @(
    "Set-Location '$escapedWorkDir'"
    "& { $Command } *>> '$escapedLogPath'"
  )

  return Start-Process `
    -FilePath "powershell.exe" `
    -ArgumentList @("-NoProfile", "-ExecutionPolicy", "Bypass", "-Command", ($launcher -join [Environment]::NewLine)) `
    -WorkingDirectory $WorkDir `
    -WindowStyle Hidden `
    -PassThru
}

function Ensure-Service {
  param(
    [string]$Name,
    [int]$Port,
    [string]$HealthUrl,
    [string]$WorkDir,
    [string]$Command,
    [string]$LogPath,
    [hashtable]$Environment = @{},
    [int]$TimeoutSeconds = 180
  )

  if (Test-PortListening -Port $Port) {
    if ($HealthUrl -and -not (Test-HttpReady -Url $HealthUrl)) {
      throw "$Name 端口 $Port 已被占用，但健康检查失败。请先释放端口或手动确认占用进程。"
    }

    Write-Step "$Name 已在端口 $Port 运行，跳过启动。"
    return
  }

  Write-Step "启动 $Name ..."
  $process = Start-BackgroundProcess -Name $Name -WorkDir $WorkDir -Command $Command -LogPath $LogPath -Environment $Environment
  Write-Step "$Name 已拉起，PID=$($process.Id)，日志: $LogPath"

  if ($HealthUrl) {
    Wait-Until -Name $Name -TimeoutSeconds $TimeoutSeconds -Condition { Test-HttpReady -Url $HealthUrl }
  } else {
    Wait-Until -Name $Name -TimeoutSeconds $TimeoutSeconds -Condition { Test-PortListening -Port $Port }
  }
}

if (-not (Test-Path $script:FrontendRoot)) {
  throw "未找到前端仓库: $script:FrontendRoot"
}

New-Item -ItemType Directory -Force -Path $script:LogRoot | Out-Null

$javaHome = Get-Java25Home
$javaBin = Join-Path $javaHome "bin"
$javaPath = "$javaBin;$env:Path"
$systemProfile = if ($Mode -eq "static") { "dev-static" } else { "dev" }
$systemPort = 18175
$systemHealthUrl = "http://127.0.0.1:$systemPort/system/api/actuator/health"
$systemDocUrl = "http://127.0.0.1:$systemPort/system/api/doc.html"

$systemLog = Join-Path $script:LogRoot "system-$Mode.log"
$oauthLog = Join-Path $script:LogRoot "oauth-$Mode.log"
$frontendLog = Join-Path $script:LogRoot "frontend-$Mode.log"

$systemCommand = "& mvn '-pl' 'spring-api-support-system-starter' 'spring-boot:run' '-Dspring-boot.run.profiles=$systemProfile' '-Dspring-boot.run.arguments=--server.port=$systemPort'"
$oauthCommand = "& mvn '-pl' 'spring-api-support-oauth-starter' 'spring-boot:run' '-Dspring-boot.run.profiles=dev'"
$frontendCommand = "& pnpm '--filter' 'vue-support-system-starter' 'dev' '--' '--host' '127.0.0.1' '--port' '8848'"

$javaEnvironment = @{
  JAVA_HOME = $javaHome
  PATH = $javaPath
}

$env:JAVA_HOME = $javaHome
$env:Path = $javaPath

Write-Step "预构建 system 上游模块，确保使用最新本地依赖 ..."
Push-Location $script:BackendRoot
try {
  & mvn '-pl' 'spring-api-support-common-system-starter' '-am' '-Dmaven.test.skip=true' '-P!dist' 'install'
  if ($LASTEXITCODE -ne 0) {
    throw "system 上游模块构建失败。"
  }
} finally {
  Pop-Location
}

if ($Mode -eq "full") {
  Ensure-Service `
    -Name "oauth" `
    -Port 19180 `
    -HealthUrl "http://127.0.0.1:19180/oauth/swagger-ui/index.html" `
    -WorkDir $script:BackendRoot `
    -Command $oauthCommand `
    -LogPath $oauthLog `
    -Environment $javaEnvironment
}

Ensure-Service `
  -Name "system" `
  -Port $systemPort `
  -HealthUrl $systemHealthUrl `
  -WorkDir $script:BackendRoot `
  -Command $systemCommand `
  -LogPath $systemLog `
  -Environment $javaEnvironment

Ensure-Service `
  -Name "frontend" `
  -Port 8848 `
  -HealthUrl "http://127.0.0.1:8848" `
  -WorkDir $script:FrontendRoot `
  -Command $frontendCommand `
  -LogPath $frontendLog

Write-Host ""
Write-Step "启动完成"
Write-Host "  mode      : $Mode"
Write-Host "  javaHome  : $javaHome"
Write-Host "  frontend  : http://127.0.0.1:8848"
Write-Host "  system    : $systemDocUrl"
if ($Mode -eq "full") {
  Write-Host "  oauth     : http://127.0.0.1:19180/oauth/swagger-ui/index.html"
}
Write-Host "  logs      : $script:LogRoot"
