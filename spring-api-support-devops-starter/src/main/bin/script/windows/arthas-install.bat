@echo off
chcp 65001 >nul 2>&1
setlocal enabledelayedexpansion

REM ============================================================
REM Arthas 安装和启动脚本 (Windows)
REM ============================================================

set "PID=%~1"
set "APP_HOME=%~2"

set "ARTHAS_VERSION=3.7.2"
set "ARTHAS_DIR=%APP_HOME%\arthas"
set "ARTHAS_BOOT_JAR=%ARTHAS_DIR%\arthas-boot.jar"
set "DOWNLOAD_URL=https://arthas.aliyun.com/arthas-boot.jar"
set "MIRROR_URL=https://maven.aliyun.com/repository/public/com/taobao/arthas/arthas-boot/%ARTHAS_VERSION%/arthas-boot-%ARTHAS_VERSION%.jar"

echo.
echo ╔══════════════════════════════════════════════════════════════╗
echo ║                    Arthas 诊断工具                           ║
echo ╚══════════════════════════════════════════════════════════════╝
echo.

REM 检查 Java
set "JAVA_CMD=java"
if defined JAVA_HOME set "JAVA_CMD=%JAVA_HOME%\bin\java.exe"

where "%JAVA_CMD%" >nul 2>&1
if errorlevel 1 (
    echo [错误] 未找到 Java 环境
    exit /b 1
)

REM 检查/安装 Arthas
if not exist "%ARTHAS_DIR%" mkdir "%ARTHAS_DIR%"

if not exist "%ARTHAS_BOOT_JAR%" (
    echo [步骤 1] 下载 Arthas...
    echo   下载地址: %DOWNLOAD_URL%
    
    REM 尝试使用 curl
    where curl >nul 2>&1
    if not errorlevel 1 (
        curl -L -o "%ARTHAS_BOOT_JAR%" "%DOWNLOAD_URL%" 2>nul
        if exist "%ARTHAS_BOOT_JAR%" goto :download_done
    )
    
    REM 尝试使用 PowerShell
    echo   使用 PowerShell 下载...
    powershell -Command "(New-Object Net.WebClient).DownloadFile('%DOWNLOAD_URL%', '%ARTHAS_BOOT_JAR%')"
    
    if exist "%ARTHAS_BOOT_JAR%" (
        goto :download_done
    )
    
    REM 尝试使用 winget 安装 curl
    echo [提示] 下载失败，尝试安装 curl...
    where winget >nul 2>&1
    if not errorlevel 1 (
        winget install curl.curl --silent
        curl -L -o "%ARTHAS_BOOT_JAR%" "%DOWNLOAD_URL%" 2>nul
    )
    
    if not exist "%ARTHAS_BOOT_JAR%" (
        echo [错误] Arthas 下载失败
        echo        请手动下载: %DOWNLOAD_URL%
        echo        保存到: %ARTHAS_BOOT_JAR%
        exit /b 1
    )
)

:download_done
echo   √ Arthas 已就绪: %ARTHAS_BOOT_JAR%
echo.

REM 启动 Arthas
if defined PID (
    echo [步骤 2] 连接到进程 %PID%...
    echo.
    "%JAVA_CMD%" -jar "%ARTHAS_BOOT_JAR%" %PID%
) else (
    echo [步骤 2] 启动 Arthas (自动选择进程)...
    echo.
    "%JAVA_CMD%" -jar "%ARTHAS_BOOT_JAR%"
)

endlocal
