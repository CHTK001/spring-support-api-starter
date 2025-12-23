@echo off
chcp 65001 >nul 2>&1
setlocal enabledelayedexpansion

REM ============================================================
REM GC 触发脚本 (Windows)
REM ============================================================

set "SCRIPT_DIR=%~dp0"
for %%i in ("%SCRIPT_DIR%\..\..") do set "BIN_DIR=%%~fi"
call "%BIN_DIR%\common.bat"

if "%~1"=="-h" goto :show_help
if "%~1"=="--help" goto :show_help
if "%~1"=="" goto :run_gc
if "%~1"=="-f" goto :run_gc_full
if "%~1"=="-s" goto :show_status
if "%~1"=="-w" goto :watch_gc
goto :show_help

:show_help
echo.
echo GC 管理
echo.
echo 用法: %~nx0 [命令]
echo.
echo 命令:
echo   (无参数)          触发 GC 并显示前后对比
echo   -f               强制触发 Full GC
echo   -s               显示 GC 状态
echo   -w ^<seconds^>     持续监控 GC 状态
echo.
echo 选项:
echo   -h, --help       显示帮助信息
echo.
echo 示例:
echo   %~nx0                      触发 GC
echo   %~nx0 -f                   触发 Full GC
echo   %~nx0 -s                   查看 GC 状态
echo   %~nx0 -w 5                 每5秒监控
echo.
goto :eof

:run_gc
:run_gc_full
echo.
echo ╔══════════════════════════════════════════════════════════════╗
echo ║                  触发 GC                                     ║
echo ╚══════════════════════════════════════════════════════════════╝
echo.

call :get_pid
if not defined APP_PID (
    echo [警告] 应用未运行
    goto :eof
)

call :check_java
if errorlevel 1 goto :eof

set "JCMD=%JAVA_CMD%"
set "JCMD=!JCMD:java.exe=jcmd.exe!"
set "JSTAT=%JAVA_CMD%"
set "JSTAT=!JSTAT:java.exe=jstat.exe!"

echo 应用 PID: %APP_PID%
echo.

REM GC 前状态
echo 【GC 前状态】
echo ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
if exist "!JSTAT!" (
    "!JSTAT!" -gcutil %APP_PID% 2>nul
) else (
    "!JCMD!" %APP_PID% GC.heap_info 2>nul
)
echo.

REM 触发 GC
echo [执行] 触发 GC...
"!JCMD!" %APP_PID% GC.run 2>nul

timeout /t 2 /nobreak >nul

REM GC 后状态
echo.
echo 【GC 后状态】
echo ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
if exist "!JSTAT!" (
    "!JSTAT!" -gcutil %APP_PID% 2>nul
) else (
    "!JCMD!" %APP_PID% GC.heap_info 2>nul
)
echo.
echo [完成] GC 已触发
echo.
goto :eof

:show_status
echo.
echo ╔══════════════════════════════════════════════════════════════╗
echo ║                  GC 状态                                     ║
echo ╚══════════════════════════════════════════════════════════════╝
echo.

call :get_pid
if not defined APP_PID (
    echo [警告] 应用未运行
    goto :eof
)

call :check_java
if errorlevel 1 goto :eof

set "JSTAT=%JAVA_CMD%"
set "JSTAT=!JSTAT:java.exe=jstat.exe!"
set "JCMD=%JAVA_CMD%"
set "JCMD=!JCMD:java.exe=jcmd.exe!"

echo 应用 PID: %APP_PID%
echo.

echo 【GC 概要】
echo ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
if exist "!JSTAT!" (
    "!JSTAT!" -gcutil %APP_PID% 2>nul
    echo.
    echo 【GC 详情】
    echo ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    "!JSTAT!" -gc %APP_PID% 2>nul
) else (
    "!JCMD!" %APP_PID% GC.heap_info 2>nul
)
echo.
goto :eof

:watch_gc
set "INTERVAL=%~2"
if "%INTERVAL%"=="" set "INTERVAL=5"

echo.
echo GC 监控模式 (每 %INTERVAL% 秒刷新, Ctrl+C 退出)
echo.

call :get_pid
if not defined APP_PID (
    echo [警告] 应用未运行
    goto :eof
)

call :check_java
if errorlevel 1 goto :eof

set "JSTAT=%JAVA_CMD%"
set "JSTAT=!JSTAT:java.exe=jstat.exe!"

if exist "!JSTAT!" (
    "!JSTAT!" -gcutil %APP_PID% %INTERVAL%000
) else (
    echo [错误] jstat 不可用
)
goto :eof

endlocal
