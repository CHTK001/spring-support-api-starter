[CmdletBinding()]
param(
  [ValidateSet("scheduler", "payment", "all")]
  [string]$App = "all"
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

function Resolve-AppConfig {
  param([string]$AppName)

  switch ($AppName) {
    "scheduler" {
      return [pscustomobject]@{
        Port = 18083
        LogPrefix = "scheduler-job-console"
      }
    }
    "payment" {
      return [pscustomobject]@{
        Port = 18190
        LogPrefix = "payment-console-browser-job"
      }
    }
    default {
      throw "不支持的应用: $AppName"
    }
  }
}

function Stop-ByPidFile {
  param([string]$PidFile)

  if (-not (Test-Path $PidFile)) {
    return $false
  }

  $pidText = (Get-Content $PidFile -Raw).Trim()
  if ([string]::IsNullOrWhiteSpace($pidText)) {
    Remove-Item $PidFile -Force -ErrorAction SilentlyContinue
    return $false
  }

  $targetPid = [int]$pidText
  try {
    Stop-Process -Id $targetPid -Force -ErrorAction Stop
    Remove-Item $PidFile -Force -ErrorAction SilentlyContinue
    return $true
  } catch {
    Remove-Item $PidFile -Force -ErrorAction SilentlyContinue
    return $false
  }
}

function Stop-ByPort {
  param([int]$Port)

  $stopped = $false
  $connections = Get-NetTCPConnection -LocalPort $Port -ErrorAction SilentlyContinue |
    Select-Object -Property OwningProcess -Unique
  foreach ($connection in $connections) {
    try {
      Stop-Process -Id $connection.OwningProcess -Force -ErrorAction Stop
      $stopped = $true
    } catch {
    }
  }

  return $stopped
}

$targets = if ($App -eq "all") { @("scheduler", "payment") } else { @($App) }

foreach ($target in $targets) {
  $config = Resolve-AppConfig -AppName $target
  $pidPath = Join-Path $script:RuntimeRoot "$($config.LogPrefix).pid"
  $stoppedByPid = Stop-ByPidFile -PidFile $pidPath
  Start-Sleep -Milliseconds 500
  $stoppedByPort = Stop-ByPort -Port $config.Port
  $stopped = $stoppedByPid -or $stoppedByPort
  if ($stopped) {
    Write-Step "$target 已停止。"
  } else {
    Write-Step "$target 未发现运行中的控制台进程。"
  }
}
