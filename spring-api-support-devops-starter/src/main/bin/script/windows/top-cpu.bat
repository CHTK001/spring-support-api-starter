@echo off
chcp 65001 >nul 2>&1
setlocal enabledelayedexpansion

REM ============================================================
REM 高 CPU 线程分析脚本 (Windows)
REM 分析占用 CPU 最高的线程并显示堆栈
REM ============================================================

set "PID=%~1"
set "TOP_N=%~2"
set "APP_NAME=%~3"

if "%PID%"=="" (
    echo [错误] 请提供进程 PID
    exit /b 1
)
if "%TOP_N%"=="" set "TOP_N=10"

REM 检查 JAVA_HOME
set "JSTACK=jstack"
set "JCMD=jcmd"
if defined JAVA_HOME (
    set "JSTACK=%JAVA_HOME%\bin\jstack.exe"
    set "JCMD=%JAVA_HOME%\bin\jcmd.exe"
)

REM 临时文件
set "TEMP_JSTACK=%TEMP%\jstack_%PID%.txt"

echo.
echo ╔══════════════════════════════════════════════════════════════╗
echo ║               高 CPU 线程分析 (Top %TOP_N%)                      ║
echo ╚══════════════════════════════════════════════════════════════╝
echo.
echo   进程 PID: %PID%
echo   应用名称: %APP_NAME%
echo.

REM 获取线程堆栈
echo [步骤 1] 获取线程堆栈...
"%JSTACK%" -l %PID% > "%TEMP_JSTACK%" 2>nul
if not exist "%TEMP_JSTACK%" (
    echo [错误] 无法获取线程堆栈
    exit /b 1
)

REM 使用 PowerShell 分析高 CPU 线程
echo [步骤 2] 分析高 CPU 线程...
echo.
echo ────────────────────────────────────────────────────────────────

powershell -Command ^
    "$ErrorActionPreference = 'SilentlyContinue'; " ^
    "$process = Get-Process -Id %PID%; " ^
    "if (-not $process) { Write-Host '[错误] 进程不存在'; exit 1 }; " ^
    "$threads = $process.Threads | Sort-Object -Property TotalProcessorTime -Descending | Select-Object -First %TOP_N%; " ^
    "$jstackContent = Get-Content '%TEMP_JSTACK%' -Raw; " ^
    "$count = 0; " ^
    "foreach ($t in $threads) { " ^
    "    $count++; " ^
    "    $tid = $t.Id; " ^
    "    $nid = '{0:x}' -f $tid; " ^
    "    $cpu = $t.TotalProcessorTime.TotalSeconds; " ^
    "    Write-Host \"`n#$count 线程 ID: $tid (0x$nid)  CPU时间: $($cpu.ToString('F2'))s\" -ForegroundColor Yellow; " ^
    "    $pattern = '\"[^\"]*\".*nid=0x' + $nid + '[\s\S]*?(?=\n\n|\n\"|\z)'; " ^
    "    $match = [regex]::Match($jstackContent, $pattern, 'IgnoreCase'); " ^
    "    if ($match.Success) { " ^
    "        $lines = $match.Value -split \"`n\" | Select-Object -First 16; " ^
    "        foreach ($line in $lines) { " ^
    "            if ($line -match 'at (com\.|cn\.|org\..*\.service|org\..*\.controller)') { " ^
    "                Write-Host $line -ForegroundColor Green; " ^
    "            } else { " ^
    "                Write-Host $line; " ^
    "            } " ^
    "        } " ^
    "    } else { " ^
    "        Write-Host '  无法找到线程堆栈' -ForegroundColor Gray; " ^
    "    } " ^
    "    Write-Host '────────────────────────────────────────────────────────────────' -ForegroundColor Blue; " ^
    "}"

REM 清理临时文件
del "%TEMP_JSTACK%" 2>nul

echo.
echo [完成] 分析结束
echo 提示: 绿色高亮的是业务代码，通常需要重点关注

endlocal
