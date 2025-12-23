@echo off
chcp 65001 >nul 2>&1
setlocal enabledelayedexpansion

REM ============================================================
REM 应用信息脚本 (Windows)
REM ============================================================

set "SCRIPT_DIR=%~dp0"
for %%i in ("%SCRIPT_DIR%\..\..") do set "BIN_DIR=%%~fi"
call "%BIN_DIR%\common.bat"

REM 帮助信息
if "%~1"=="-h" goto :show_help
if "%~1"=="--help" goto :show_help

set "SHOW_ALL=false"
set "SHOW_CPU=false"
set "SHOW_MEM=false"

:parse_args
if "%~1"=="" goto :show_info
if "%~1"=="-a" (set "SHOW_ALL=true" & shift & goto :parse_args)
if "%~1"=="-c" (set "SHOW_CPU=true" & shift & goto :parse_args)
if "%~1"=="-m" (set "SHOW_MEM=true" & shift & goto :parse_args)
shift
goto :parse_args

:show_help
echo.
echo 应用信息
echo.
echo 用法: %~nx0 [选项]
echo.
echo 选项:
echo   -a          显示详细信息
echo   -c          显示 CPU 信息
echo   -m          显示内存信息
echo   -h, --help  显示帮助信息
echo.
goto :eof

:show_info
call :get_pid

echo.
echo ╔══════════════════════════════════════════════════════════════╗
echo ║                    应用信息                                  ║
echo ╚══════════════════════════════════════════════════════════════╝
echo.

echo 【基础信息】
echo ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
echo   应用名称:     %APP_NAME%
echo   应用版本:     %APP_VERSION%
echo   JAR 文件:     %JAR_NAME%
echo   应用目录:     %BASE_DIR%
echo   配置目录:     %CONFIG_DIR%
echo   日志目录:     %LOG_DIR%
echo.

echo 【运行状态】
echo ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
if defined APP_PID (
    echo   状态:         运行中
    echo   进程 ID:      %APP_PID%
    
    REM 获取进程信息
    for /f "tokens=*" %%a in ('wmic process where "ProcessId=%APP_PID%" get WorkingSetSize /value 2^>nul ^| find "="') do (
        set "%%a"
    )
    if defined WorkingSetSize (
        set /a "MEM_MB=!WorkingSetSize! / 1024 / 1024"
        echo   内存使用:     !MEM_MB! MB
    )
) else (
    echo   状态:         已停止
)
echo.

if "%SHOW_ALL%"=="true" (
    echo 【Java 环境】
    echo ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    call :check_java
    if not errorlevel 1 (
        echo   Java 路径:    %JAVA_CMD%
        echo   Java 版本:    %JAVA_VERSION%
    )
    echo.
)

if "%SHOW_CPU%"=="true" (
    if defined APP_PID (
        echo 【CPU 信息】
        echo ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        wmic path win32_perfformatteddata_perfproc_process where "IDProcess=%APP_PID%" get PercentProcessorTime 2>nul
        echo.
    )
)

if "%SHOW_MEM%"=="true" (
    if defined APP_PID (
        echo 【内存详情】
        echo ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        wmic process where "ProcessId=%APP_PID%" get WorkingSetSize,VirtualSize,PageFileUsage /format:list 2>nul
        echo.
    )
)

echo 【日志文件】
echo ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
if exist "%STARTUP_LOG%" (
    echo   启动日志:     %STARTUP_LOG%
    for %%f in ("%STARTUP_LOG%") do echo   文件大小:     %%~zf bytes
) else (
    echo   启动日志:     (不存在)
)
echo.

endlocal
