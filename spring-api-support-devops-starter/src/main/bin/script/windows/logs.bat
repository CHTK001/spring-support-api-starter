@echo off
chcp 65001 >nul 2>&1
setlocal enabledelayedexpansion

REM ============================================================
REM 日志管理脚本 (Windows)
REM ============================================================

set "SCRIPT_DIR=%~dp0"
for %%i in ("%SCRIPT_DIR%\..\..") do set "BIN_DIR=%%~fi"
call "%BIN_DIR%\common.bat"

REM 默认值
set "FOLLOW=false"
set "LINES=100"
set "LOG_TYPE=startup"
set "FILTER="
set "TIME_FILTER="

if "%~1"=="-h" goto :show_help
if "%~1"=="--help" goto :show_help

:parse_args
if "%~1"=="" goto :execute
if "%~1"=="-f" (set "FOLLOW=true" & shift & goto :parse_args)
if "%~1"=="-n" (set "LINES=%~2" & shift & shift & goto :parse_args)
if "%~1"=="--filter" (set "FILTER=%~2" & shift & shift & goto :parse_args)
if "%~1"=="-t" (set "TIME_FILTER=%~2" & shift & shift & goto :parse_args)
if "%~1"=="--startup" (set "LOG_TYPE=startup" & shift & goto :parse_args)
if "%~1"=="--app" (set "LOG_TYPE=app" & shift & goto :parse_args)
shift
goto :parse_args

:show_help
echo.
echo 日志管理
echo.
echo 用法: %~nx0 [选项]
echo.
echo 选项:
echo   -f              实时跟踪日志
echo   -n ^<N^>          显示最近 N 行 (默认: 100)
echo   --filter ^<str^>  过滤包含指定字符串的日志
echo   -t ^<time^>       时间过滤 (如: 2025-01-01)
echo   --startup       显示启动日志 (默认)
echo   --app           显示应用日志
echo   -h, --help      显示帮助信息
echo.
echo 示例:
echo   %~nx0                        显示最近 100 行日志
echo   %~nx0 -f                     实时跟踪日志
echo   %~nx0 -n 200                 显示最近 200 行
echo   %~nx0 --filter ERROR         过滤 ERROR 日志
echo   %~nx0 -t 2025-01-01          过滤指定日期日志
echo.
goto :eof

:execute
REM 确定日志文件
if "%LOG_TYPE%"=="startup" (
    set "LOG_FILE=%STARTUP_LOG%"
) else (
    set "LOG_FILE=%LOG_DIR%\%APP_NAME%\app.log"
)

if not exist "%LOG_FILE%" (
    echo [警告] 日志文件不存在: %LOG_FILE%
    goto :eof
)

echo.
echo ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
echo   日志文件: %LOG_FILE%
echo ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
echo.

if "%FOLLOW%"=="true" (
    echo 实时跟踪日志... (按 Ctrl+C 退出)
    echo.
    if defined FILTER (
        powershell -Command "Get-Content '%LOG_FILE%' -Wait -Tail 50 | Where-Object { $_ -match '%FILTER%' }"
    ) else (
        powershell -Command "Get-Content '%LOG_FILE%' -Wait -Tail 50"
    )
) else (
    if defined FILTER (
        if defined TIME_FILTER (
            powershell -Command "Get-Content '%LOG_FILE%' -Tail %LINES% | Where-Object { $_ -match '%FILTER%' -and $_ -match '%TIME_FILTER%' }"
        ) else (
            powershell -Command "Get-Content '%LOG_FILE%' -Tail %LINES% | Where-Object { $_ -match '%FILTER%' }"
        )
    ) else if defined TIME_FILTER (
        powershell -Command "Get-Content '%LOG_FILE%' -Tail %LINES% | Where-Object { $_ -match '%TIME_FILTER%' }"
    ) else (
        powershell -Command "Get-Content '%LOG_FILE%' -Tail %LINES%"
    )
)

endlocal
