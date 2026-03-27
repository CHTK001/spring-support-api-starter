[CmdletBinding()]
param(
  [ValidateSet("scheduler", "payment")]
  [string]$App,
  [int]$HeapMb = 768,
  [int]$MetaspaceMb = 256,
  [switch]$BuildIfMissing,
  [switch]$Restart
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"
$ProgressPreference = "SilentlyContinue"

$script:BackendRoot = Split-Path -Parent (Split-Path -Parent $PSScriptRoot)
$script:RuntimeRoot = Join-Path $script:BackendRoot ".runtime\spring-pages"

function Write-Step {
  param([string]$Message)
  Write-Host "[simple-console] $Message"
}

function Test-PortListening {
  param([int]$Port)
  return [bool](Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue)
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

function Invoke-JsonGet {
  param([string]$Url)

  try {
    return Invoke-RestMethod -Uri $Url -Method Get -TimeoutSec 5
  } catch {
    return $null
  }
}

function Test-ConsoleReady {
  param(
    [string]$AppName,
    [string]$ReadyUrl
  )

  $result = Invoke-JsonGet -Url $ReadyUrl
  if ($null -eq $result) {
    return $false
  }

  if ($AppName -eq "scheduler") {
    return $result.code -eq "00000" -and $null -ne $result.data
  }

  return $result.code -eq "00000"
}

function Get-JavaHome {
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
    if (Test-Path $javaExe) {
      return $candidate
    }
  }

  throw "未检测到可用 JDK。请先安装 JDK 并配置 JAVA_HOME。"
}

function Resolve-AppConfig {
  param([string]$AppName)

  switch ($AppName) {
    "scheduler" {
      return [pscustomobject]@{
        Module = "spring-api-support-scheduler-starter"
        Port = 18083
        Profiles = "browser"
        ReadyUrl = "http://127.0.0.1:18083/scheduler/api/job-console/auth/status"
        LogPrefix = "scheduler-job-console"
        Description = "scheduler browser 控制台"
      }
    }
    "payment" {
      return [pscustomobject]@{
        Module = "spring-api-support-payment-starter"
        Port = 18190
        Profiles = "browser,job"
        ReadyUrl = "http://127.0.0.1:18190/payment/api/payment-console/auth/status"
        LogPrefix = "payment-console-browser-job"
        Description = "payment browser+job 控制台"
      }
    }
    default {
      throw "不支持的应用: $AppName"
    }
  }
}

function Get-TargetJar {
  param(
    [string]$ModuleName,
    [switch]$BuildWhenMissing,
    [string]$JavaHome
  )

  $targetDir = Join-Path $script:BackendRoot "$ModuleName\target"
  $jar = Get-ChildItem -Path $targetDir -Filter "*.jar" -File -ErrorAction SilentlyContinue |
    Where-Object { $_.Name -notlike "original-*.jar" } |
    Sort-Object LastWriteTime -Descending |
    Select-Object -First 1

  if ($null -ne $jar) {
    return $jar.FullName
  }

  if (-not $BuildWhenMissing) {
    throw "未找到 $ModuleName 的可执行 jar，请先构建或加上 -BuildIfMissing。"
  }

  Write-Step "未找到 jar，开始低内存构建 $ModuleName ..."
  $env:JAVA_HOME = $JavaHome
  $env:Path = "$(Join-Path $JavaHome 'bin');$env:Path"
  $env:MAVEN_OPTS = "-Xmx${HeapMb}m -XX:MaxMetaspaceSize=${MetaspaceMb}m"
  Push-Location $script:BackendRoot
  try {
    & mvn "-pl" $ModuleName "-am" "-DskipTests" "package"
    if ($LASTEXITCODE -ne 0) {
      throw "$ModuleName 构建失败。"
    }
  } finally {
    Pop-Location
  }

  $jar = Get-ChildItem -Path $targetDir -Filter "*.jar" -File |
    Where-Object { $_.Name -notlike "original-*.jar" } |
    Sort-Object LastWriteTime -Descending |
    Select-Object -First 1

  if ($null -eq $jar) {
    throw "$ModuleName 构建完成后仍未找到可执行 jar。"
  }

  return $jar.FullName
}

function Stop-PortProcess {
  param([int]$Port)

  $connections = Get-NetTCPConnection -LocalPort $Port -ErrorAction SilentlyContinue |
    Select-Object -Property OwningProcess -Unique
  foreach ($connection in $connections) {
    try {
      Stop-Process -Id $connection.OwningProcess -Force -ErrorAction Stop
    } catch {
    }
  }
}

function Wait-PortReleased {
  param(
    [int]$Port,
    [int]$TimeoutSeconds = 15
  )

  $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
  while ((Get-Date) -lt $deadline) {
    if (-not (Test-PortListening -Port $Port)) {
      return
    }
    Start-Sleep -Seconds 1
  }
}

New-Item -ItemType Directory -Force -Path $script:RuntimeRoot | Out-Null

$config = Resolve-AppConfig -AppName $App
$javaHome = Get-JavaHome
$javaExe = Join-Path $javaHome "bin\java.exe"
$jarPath = Get-TargetJar -ModuleName $config.Module -BuildWhenMissing:$BuildIfMissing -JavaHome $javaHome
$argsPath = Join-Path $script:RuntimeRoot "$($config.LogPrefix).args"
$outLog = Join-Path $script:RuntimeRoot "$($config.LogPrefix).out.log"
$errLog = Join-Path $script:RuntimeRoot "$($config.LogPrefix).err.log"
$pidPath = Join-Path $script:RuntimeRoot "$($config.LogPrefix).pid"

if ($Restart) {
  Write-Step "检测到 -Restart，先释放端口 $($config.Port) ..."
  Stop-PortProcess -Port $config.Port
  Wait-PortReleased -Port $config.Port
}

if (Test-PortListening -Port $config.Port) {
  if (Test-ConsoleReady -AppName $App -ReadyUrl $config.ReadyUrl) {
    Write-Step "$($config.Description) 已在端口 $($config.Port) 运行，跳过启动。"
    Write-Host "  url  : $($config.ReadyUrl)"
    Write-Host "  logs : $outLog"
    return
  }

  throw "端口 $($config.Port) 已被占用，但 $($config.Description) 未就绪。请先释放端口或加 -Restart。"
}

$javaArgs = @(
  "-Xmx${HeapMb}m"
  "-XX:MaxMetaspaceSize=${MetaspaceMb}m"
  "-jar"
  $jarPath
  "--spring.profiles.active=$($config.Profiles)"
)

Set-Content -Path $argsPath -Value ($javaArgs -join [Environment]::NewLine) -Encoding ASCII

Write-Step "启动 $($config.Description) ..."
$process = Start-Process `
  -FilePath $javaExe `
  -ArgumentList $javaArgs `
  -WorkingDirectory $script:BackendRoot `
  -RedirectStandardOutput $outLog `
  -RedirectStandardError $errLog `
  -WindowStyle Hidden `
  -PassThru

Set-Content -Path $pidPath -Value $process.Id -Encoding ASCII

Wait-Until `
  -Name $config.Description `
  -TimeoutSeconds 180 `
  -Condition { Test-ConsoleReady -AppName $App -ReadyUrl $config.ReadyUrl }

Write-Step "启动完成。"
Write-Host "  pid     : $($process.Id)"
Write-Host "  port    : $($config.Port)"
Write-Host "  profile : $($config.Profiles)"
Write-Host "  jar     : $jarPath"
Write-Host "  ready   : $($config.ReadyUrl)"
Write-Host "  stdout  : $outLog"
Write-Host "  stderr  : $errLog"
