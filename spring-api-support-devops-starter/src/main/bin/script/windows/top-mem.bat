@echo off
chcp 65001 >nul 2>&1
setlocal enabledelayedexpansion

REM ============================================================
REM 高内存对象分析脚本 (Windows)
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
set "JMAP=jmap"
set "JCMD=jcmd"
if defined JAVA_HOME (
    set "JMAP=%JAVA_HOME%\bin\jmap.exe"
    set "JCMD=%JAVA_HOME%\bin\jcmd.exe"
)

echo.
echo ╔══════════════════════════════════════════════════════════════╗
echo ║              高内存对象分析 (Top %TOP_N%)                        ║
echo ╚══════════════════════════════════════════════════════════════╝
echo.
echo   进程 PID: %PID%
echo   应用名称: %APP_NAME%
echo.

REM 尝试使用 jcmd (更安全)
echo [步骤 1] 获取对象直方图...
set "TEMP_HISTO=%TEMP%\histo_%PID%.txt"

"%JCMD%" %PID% GC.class_histogram > "%TEMP_HISTO%" 2>nul
if not exist "%TEMP_HISTO%" (
    echo 使用 jmap 获取...
    "%JMAP%" -histo:live %PID% > "%TEMP_HISTO%" 2>nul
)

if not exist "%TEMP_HISTO%" (
    echo [错误] 无法获取对象直方图
    exit /b 1
)

echo.
echo [步骤 2] 分析高内存对象...
echo.
echo   排名   实例数          占用字节            类名
echo ────────────────────────────────────────────────────────────────

REM 使用 PowerShell 解析并格式化输出
powershell -Command ^
    "$lines = Get-Content '%TEMP_HISTO%' | Where-Object { $_ -match '^\s*\d+:' } | Select-Object -First %TOP_N%; " ^
    "foreach ($line in $lines) { " ^
    "    if ($line -match '^\s*(\d+):\s+(\d+)\s+(\d+)\s+(.+)$') { " ^
    "        $num = $matches[1]; " ^
    "        $instances = $matches[2]; " ^
    "        $bytes = [long]$matches[3]; " ^
    "        $className = $matches[4].Trim(); " ^
    "        $sizeHuman = ''; " ^
    "        if ($bytes -ge 1073741824) { $sizeHuman = '{0:F2} GB' -f ($bytes / 1073741824) } " ^
    "        elseif ($bytes -ge 1048576) { $sizeHuman = '{0:F2} MB' -f ($bytes / 1048576) } " ^
    "        elseif ($bytes -ge 1024) { $sizeHuman = '{0:F2} KB' -f ($bytes / 1024) } " ^
    "        else { $sizeHuman = \"$bytes B\" }; " ^
    "        $output = '  {0,4}   {1,12}   {2,12}   {3}' -f $num, $instances, $sizeHuman, $className; " ^
    "        if ($className -match '^(com\.|cn\.|org\..*\.model|org\..*\.entity|org\..*\.dto)') { " ^
    "            Write-Host $output -ForegroundColor Green; " ^
    "        } else { " ^
    "            Write-Host $output; " ^
    "        } " ^
    "    } " ^
    "}"

echo ────────────────────────────────────────────────────────────────

REM 显示总计
echo.
echo 【内存概览】
for /f "tokens=2,3" %%a in ('findstr /r "^Total" "%TEMP_HISTO%" 2^>nul') do (
    echo   总实例数: %%a
    powershell -Command ^
        "$bytes = [long]%%b; " ^
        "if ($bytes -ge 1073741824) { Write-Host ('  总占用:   {0:F2} GB' -f ($bytes / 1073741824)) } " ^
        "elseif ($bytes -ge 1048576) { Write-Host ('  总占用:   {0:F2} MB' -f ($bytes / 1048576)) } " ^
        "else { Write-Host ('  总占用:   {0:F2} KB' -f ($bytes / 1024)) }"
)

REM 清理临时文件
del "%TEMP_HISTO%" 2>nul

echo.
echo [完成] 分析结束
echo 提示: 绿色高亮的是业务类，通常需要重点关注
echo       如需更详细分析，请使用: startup.bat dump heap

endlocal
