@echo off
chcp 65001 >nul 2>&1
setlocal enabledelayedexpansion

REM ============================================================
REM GC 触发脚本 (Windows)
REM ============================================================

set "PID=%~1"
set "GC_TYPE=%~2"
set "APP_NAME=%~3"

if "%PID%"=="" (
    echo [错误] 请提供进程 PID
    exit /b 1
)
if "%GC_TYPE%"=="" set "GC_TYPE=full"

REM 检查 JAVA_HOME
set "JCMD=jcmd"
set "JSTAT=jstat"
if defined JAVA_HOME (
    set "JCMD=%JAVA_HOME%\bin\jcmd.exe"
    set "JSTAT=%JAVA_HOME%\bin\jstat.exe"
)

echo.
echo ════════════════════════════════════════════════════════════════
echo                     触发 GC
echo ════════════════════════════════════════════════════════════════
echo.
echo   PID:      %PID%
echo   GC 类型:  %GC_TYPE%
echo.

REM GC 前状态
echo 【GC 前状态】
"%JSTAT%" -gcutil %PID% 2>nul
echo.

REM 触发 GC
if /i "%GC_TYPE%"=="young" (
    echo 触发 Young GC...
) else (
    echo 触发 Full GC...
)
"%JCMD%" %PID% GC.run 2>nul

timeout /t 2 /nobreak >nul

REM GC 后状态
echo.
echo 【GC 后状态】
"%JSTAT%" -gcutil %PID% 2>nul
echo.
echo [完成] GC 已触发

endlocal
