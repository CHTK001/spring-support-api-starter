@echo off
REM ============================================================
REM Skywalking Agent 安装管理脚本 (Windows)
REM 作者: CH
REM 版本: 1.0.0
REM ============================================================
setlocal enabledelayedexpansion

set SCRIPT_DIR=%~dp0
cd /d %SCRIPT_DIR%..\..
set APP_HOME=%cd%
set SKYWALKING_DIR=%APP_HOME%\skywalking
set SKYWALKING_AGENT_DIR=%SKYWALKING_DIR%\skywalking-agent
set SKYWALKING_AGENT_JAR=%SKYWALKING_AGENT_DIR%\skywalking-agent.jar

if "%SKYWALKING_VERSION%"=="" set SKYWALKING_VERSION=9.0.0
if "%SW_AGENT_COLLECTOR_BACKEND_SERVICES%"=="" set SW_AGENT_COLLECTOR_BACKEND_SERVICES=127.0.0.1:11800

set DOWNLOAD_URL=https://archive.apache.org/dist/skywalking/%SKYWALKING_VERSION%/apache-skywalking-java-agent-%SKYWALKING_VERSION%.tgz

if "%~1"=="" goto check
if /i "%~1"=="install" goto install
if /i "%~1"=="check" goto check
if /i "%~1"=="clean" goto clean
if /i "%~1"=="info" goto info
if /i "%~1"=="help" goto help
if /i "%~1"=="--help" goto help
if /i "%~1"=="-h" goto help
echo [错误] 未知命令: %~1
goto help

:help
echo ============================================================
echo   Skywalking Agent 管理脚本
echo ============================================================
echo.
echo 用法: %~nx0 [命令]
echo.
echo 命令:
echo   install     下载并安装 Skywalking Agent
echo   check       检查 Skywalking Agent 是否已安装
echo   clean       清理/删除 Skywalking Agent
echo   info        显示 Skywalking 配置信息
echo   help        显示帮助信息
echo.
echo 环境变量:
echo   SKYWALKING_VERSION                  版本号 (默认: 9.0.0)
echo   SW_AGENT_COLLECTOR_BACKEND_SERVICES OAP地址 (默认: 127.0.0.1:11800)
echo.
goto end

:check
if exist "%SKYWALKING_AGENT_JAR%" (
    echo [已安装] Skywalking Agent
    echo   路径: %SKYWALKING_AGENT_JAR%
) else (
    echo [未安装] Skywalking Agent
    echo   运行 '%~nx0 install' 进行安装
)
goto end

:install
if exist "%SKYWALKING_AGENT_JAR%" (
    echo [警告] Skywalking Agent 已存在: %SKYWALKING_AGENT_JAR%
    set /p CONFIRM="是否重新下载? [y/N]: "
    if /i not "!CONFIRM!"=="y" (
        echo [跳过] 保留现有安装
        goto end
    )
    rd /s /q "%SKYWALKING_DIR%" 2>nul
)

if not exist "%SKYWALKING_DIR%" mkdir "%SKYWALKING_DIR%"

echo ============================================================
echo   下载 Skywalking Agent
echo ============================================================
echo   版本: %SKYWALKING_VERSION%
echo   下载地址: %DOWNLOAD_URL%
echo   安装目录: %SKYWALKING_DIR%
echo.

set TMP_FILE=%SKYWALKING_DIR%\skywalking-agent.tgz

echo [下载中...]
where curl >nul 2>nul
if %errorlevel% equ 0 (
    curl -L -o "%TMP_FILE%" "%DOWNLOAD_URL%"
) else (
    echo [错误] 未找到 curl，请手动下载
    echo 下载地址: %DOWNLOAD_URL%
    goto end
)

if not exist "%TMP_FILE%" (
    echo [错误] 下载失败
    goto end
)

echo [解压中...]
where tar >nul 2>nul
if %errorlevel% equ 0 (
    tar -xzf "%TMP_FILE%" -C "%SKYWALKING_DIR%"
) else (
    echo [错误] 未找到 tar 命令，请手动解压
    goto end
)

del "%TMP_FILE%" 2>nul

if exist "%SKYWALKING_AGENT_JAR%" (
    echo [成功] Skywalking Agent 安装完成
    echo   Agent JAR: %SKYWALKING_AGENT_JAR%
) else (
    echo [错误] 解压后未找到 Agent JAR
)
goto end

:clean
if exist "%SKYWALKING_DIR%" (
    echo [清理] 删除 Skywalking 目录...
    rd /s /q "%SKYWALKING_DIR%"
    echo [完成] 清理完成
) else (
    echo [信息] Skywalking 目录不存在
)
goto end

:info
echo ============================================================
echo   Skywalking 配置信息
echo ============================================================
echo.
echo   版本:        %SKYWALKING_VERSION%
echo   安装目录:    %SKYWALKING_DIR%
echo   Agent JAR:   %SKYWALKING_AGENT_JAR%
echo   下载地址:    %DOWNLOAD_URL%
echo.
echo   Agent 配置:
echo     OAP地址:   %SW_AGENT_COLLECTOR_BACKEND_SERVICES%
echo.
if exist "%SKYWALKING_AGENT_JAR%" (
    echo   状态: 已安装
) else (
    echo   状态: 未安装
)
echo.
goto end

:end
endlocal
