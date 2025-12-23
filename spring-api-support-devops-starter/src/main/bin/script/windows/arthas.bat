@echo off
chcp 65001 >nul 2>&1
setlocal enabledelayedexpansion

REM ============================================================
REM Arthas 诊断工具脚本 (Windows)
REM ============================================================

set "SCRIPT_DIR=%~dp0"
for %%i in ("%SCRIPT_DIR%\..\..") do set "BIN_DIR=%%~fi"
call "%BIN_DIR%\common.bat"

set "ARTHAS_VERSION=3.7.2"
set "ARTHAS_DIR=%BASE_DIR%\arthas"
set "ARTHAS_BOOT_JAR=%ARTHAS_DIR%\arthas-boot.jar"
set "DOWNLOAD_URL=https://arthas.aliyun.com/arthas-boot.jar"
set "MIRROR_URL=https://maven.aliyun.com/repository/public/com/taobao/arthas/arthas-boot/%ARTHAS_VERSION%/arthas-boot-%ARTHAS_VERSION%.jar"

if "%~1"=="-h" goto :show_help
if "%~1"=="--help" goto :show_help
if "%~1"=="" goto :start_arthas
if "%~1"=="-i" goto :install_arthas
if "%~1"=="-c" goto :check_arthas
if "%~1"=="-u" goto :uninstall_arthas
goto :show_help

:show_help
echo.
echo Arthas 诊断工具
echo.
echo 用法: %~nx0 [命令]
echo.
echo 命令:
echo   (无参数)          启动 Arthas 并连接到应用
echo   -i               安装/更新 Arthas
echo   -c               检查安装状态
echo   -u               卸载 Arthas
echo.
echo 选项:
echo   -h, --help       显示帮助信息
echo.
echo 示例:
echo   %~nx0                      启动并连接
echo   %~nx0 -i                   安装 Arthas
echo   %~nx0 -c                   检查状态
echo.
goto :eof

:start_arthas
echo.
echo ╔══════════════════════════════════════════════════════════════╗
echo ║                  Arthas 诊断工具                             ║
echo ╚══════════════════════════════════════════════════════════════╝
echo.

call :check_java
if errorlevel 1 goto :eof

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
call :get_pid
if defined APP_PID (
    echo [步骤 2] 连接到进程 %APP_PID%...
    echo.
    "%JAVA_CMD%" -jar "%ARTHAS_BOOT_JAR%" %APP_PID%
) else (
    echo [步骤 2] 启动 Arthas (自动选择进程)...
    echo.
    "%JAVA_CMD%" -jar "%ARTHAS_BOOT_JAR%"
)
goto :eof

:install_arthas
echo.
echo ╔══════════════════════════════════════════════════════════════╗
echo ║                  安装 Arthas                                   ║
echo ╚══════════════════════════════════════════════════════════════╝
echo.

if not exist "%ARTHAS_DIR%" mkdir "%ARTHAS_DIR%"

if exist "%ARTHAS_BOOT_JAR%" (
    echo [提示] Arthas 已安装，重新下载...
    del "%ARTHAS_BOOT_JAR%" 2>nul
)

echo [步骤] 下载 Arthas...
echo   URL: %DOWNLOAD_URL%

where curl >nul 2>&1
if not errorlevel 1 (
    curl -L -o "%ARTHAS_BOOT_JAR%" "%DOWNLOAD_URL%" 2>nul
) else (
    powershell -Command "(New-Object Net.WebClient).DownloadFile('%DOWNLOAD_URL%', '%ARTHAS_BOOT_JAR%')"
)

if exist "%ARTHAS_BOOT_JAR%" (
    echo.
    echo [成功] Arthas 已安装
    echo   路径: %ARTHAS_BOOT_JAR%
) else (
    echo [错误] 安装失败
)
echo.
goto :eof

:check_arthas
echo.
echo ╔══════════════════════════════════════════════════════════════╗
echo ║                  Arthas 状态                                   ║
echo ╚══════════════════════════════════════════════════════════════╝
echo.
echo   版本: %ARTHAS_VERSION%
echo   目录: %ARTHAS_DIR%
echo.

if exist "%ARTHAS_BOOT_JAR%" (
    echo   状态: 已安装 √
    for %%f in ("%ARTHAS_BOOT_JAR%") do (
        set /a "SIZE_KB=%%~zf/1024"
        echo   大小: !SIZE_KB! KB
    )
) else (
    echo   状态: 未安装 ×
    echo.
    echo   使用 '%~nx0 -i' 安装
)
echo.
goto :eof

:uninstall_arthas
echo.
if not exist "%ARTHAS_DIR%" (
    echo [提示] Arthas 未安装
    goto :eof
)

set /p "CONFIRM=确认卸载 Arthas? [y/N] "
if /i not "%CONFIRM%"=="y" (
    echo 已取消
    goto :eof
)

rd /s /q "%ARTHAS_DIR%" 2>nul
if not exist "%ARTHAS_DIR%" (
    echo [成功] Arthas 已卸载
) else (
    echo [错误] 卸载失败
)
echo.
goto :eof

endlocal
