@echo off
REM ============================================================
REM Arthas 在线安装脚本 (Windows)
REM 作者: CH
REM 版本: 1.0.0
REM ============================================================
setlocal enabledelayedexpansion

set SCRIPT_DIR=%~dp0
cd /d %SCRIPT_DIR%..\..
set APP_HOME=%cd%
set ARTHAS_DIR=%APP_HOME%\arthas
set ARTHAS_BOOT=%ARTHAS_DIR%\arthas-boot.jar
set PID=%~1

set JAVA_CMD=java
if defined JAVA_HOME (
    if exist "%JAVA_HOME%\bin\java.exe" set JAVA_CMD=%JAVA_HOME%\bin\java
)

if "%~1"=="install" goto install
if "%~1"=="help" goto help
if "%~1"=="--help" goto help
if "%~1"=="-h" goto help
goto start

:help
echo ============================================================
echo   Arthas 在线安装脚本
echo ============================================================
echo.
echo 用法: %~nx0 [PID^|install^|help]
echo.
echo 命令:
echo   ^<PID^>     附加到指定进程
echo   install   仅下载安装 Arthas
echo   help      显示帮助信息
echo.
goto end

:install
if not exist "%ARTHAS_DIR%" mkdir "%ARTHAS_DIR%"
echo [下载 Arthas...]
where curl >nul 2>nul
if %errorlevel% equ 0 (
    curl -L -o "%ARTHAS_BOOT%" "https://arthas.aliyun.com/arthas-boot.jar"
) else (
    echo [错误] 未找到 curl，请手动下载
    echo 下载地址: https://arthas.aliyun.com/arthas-boot.jar
    goto end
)
if exist "%ARTHAS_BOOT%" (
    echo [成功] Arthas 已安装
) else (
    echo [失败] 下载失败
)
goto end

:start
if not exist "%ARTHAS_BOOT%" (
    echo [提示] Arthas 未安装，开始下载...
    call :install
)

echo ============================================================
echo   启动 Arthas
echo ============================================================

if "%PID%"=="" (
    "%JAVA_CMD%" -jar "%ARTHAS_BOOT%"
) else (
    "%JAVA_CMD%" -jar "%ARTHAS_BOOT%" %PID%
)
goto end

:end
endlocal
