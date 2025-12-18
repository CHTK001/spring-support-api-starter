@echo off
REM ============================================================
REM JVM 信息查看脚本 (Windows)
REM 作者: CH
REM 版本: 1.0.0
REM ============================================================
setlocal enabledelayedexpansion

set PID=%~1
set APP_NAME=%~2
if "%APP_NAME%"=="" set APP_NAME=Java应用

if "%PID%"=="" (
    echo [错误] 未提供进程ID
    echo 用法: %~nx0 ^<PID^> [应用名]
    exit /b 1
)

REM 检查进程是否存在
tasklist /fi "PID eq %PID%" 2>nul | find "%PID%" >nul
if %errorlevel% neq 0 (
    echo [错误] 进程 %PID% 不存在
    exit /b 1
)

REM 获取 Java 工具路径
set JAVA_BIN=
if defined JAVA_HOME (
    if exist "%JAVA_HOME%\bin\java.exe" set JAVA_BIN=%JAVA_HOME%\bin
)
if "%JAVA_BIN%"=="" (
    where java >nul 2>nul
    if %errorlevel% equ 0 (
        for /f "tokens=*" %%i in ('where java') do set JAVA_BIN=%%~dpi
    )
)

echo ============================================================
echo   %APP_NAME% JVM 信息
echo ============================================================
echo   进程ID: %PID%
echo   时间: %date% %time%
echo ============================================================

echo.
echo [进程信息]
wmic process where "ProcessId=%PID%" get ProcessId,WorkingSetSize,CreationDate /format:list 2>nul | find "="

echo.
echo [JVM版本]
if defined JAVA_BIN (
    "%JAVA_BIN%java" -version 2>&1 | find "version"
)

echo.
echo [JVM参数]
if defined JAVA_BIN (
    if exist "%JAVA_BIN%jcmd.exe" (
        "%JAVA_BIN%jcmd" %PID% VM.flags 2>nul
    )
)

echo.
echo [堆内存信息]
if defined JAVA_BIN (
    if exist "%JAVA_BIN%jcmd.exe" (
        "%JAVA_BIN%jcmd" %PID% GC.heap_info 2>nul
    )
)

echo.
echo [GC统计]
if defined JAVA_BIN (
    if exist "%JAVA_BIN%jstat.exe" (
        "%JAVA_BIN%jstat" -gcutil %PID% 2>nul
        echo S0/S1=Survivor E=Eden O=Old M=Meta YGC/FGC=GC次数 GCT=总耗时(秒)
    )
)

echo.
echo ============================================================
endlocal
