@echo off
chcp 65001 >nul 2>&1
setlocal enabledelayedexpansion

REM ============================================================
REM Java 环境管理脚本 (Windows)
REM ============================================================

set "SCRIPT_DIR=%~dp0"
for %%i in ("%SCRIPT_DIR%\..\..") do set "BIN_DIR=%%~fi"
call "%BIN_DIR%\common.bat"

set "JAVA_INSTALL_DIR=%BASE_DIR%\java"

if "%~1"=="" goto :show_info
if "%~1"=="-h" goto :show_help
if "%~1"=="--help" goto :show_help
if "%~1"=="-l" goto :list_java
if "%~1"=="-i" goto :install_java
if "%~1"=="-u" goto :uninstall_java
if "%~1"=="-d" goto :set_default_java
goto :show_help

:show_help
echo.
echo Java 环境管理
echo.
echo 用法: %~nx0 [命令]
echo.
echo 命令:
echo   (无参数)          显示当前 Java 信息
echo   -i ^<version^>     下载并安装指定版本 Java
echo   -l               列出已安装的 Java 版本
echo   -u               卸载当前 Java
echo   -d ^<path^>        设置默认 Java 路径
echo.
echo 选项:
echo   -h, --help       显示帮助信息
echo.
echo 支持的版本: 8, 11, 17, 21
echo.
echo 示例:
echo   %~nx0                      显示 Java 信息
echo   %~nx0 -i 21               安装 JDK 21
echo   %~nx0 -l                  列出已安装版本
echo   %~nx0 -d "C:\jdk21"       设置默认 Java
echo.
goto :eof

:show_info
echo.
echo ╔══════════════════════════════════════════════════════════════╗
echo ║                  Java 环境信息                               ║
echo ╚══════════════════════════════════════════════════════════════╝
echo.

call :check_java
if errorlevel 1 (
    echo   状态: 未配置
    echo.
    goto :eof
)

echo 【当前配置】
echo ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
echo   Java 路径:     %JAVA_CMD%
echo   Java 版本:     %JAVA_VERSION%

if defined JAVA_HOME (
    echo   JAVA_HOME:     %JAVA_HOME%
)

if exist "%JAVA_PATH_FILE%" (
    set /p SAVED_JAVA=<"%JAVA_PATH_FILE%"
    echo   持久化路径:   !SAVED_JAVA!
)
echo.

REM 显示详细版本信息
echo 【详细版本】
echo ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
"%JAVA_CMD%" -version 2>&1
echo.
goto :eof

:list_java
echo.
echo ╔══════════════════════════════════════════════════════════════╗
echo ║                  已安装的 Java 版本                          ║
echo ╚══════════════════════════════════════════════════════════════╝
echo.

set "FOUND=false"

REM 检查本地安装目录
if exist "%JAVA_INSTALL_DIR%" (
    for /d %%d in ("%JAVA_INSTALL_DIR%\*") do (
        if exist "%%d\bin\java.exe" (
            set "FOUND=true"
            echo   [本地] %%~nxd
            for /f "tokens=3" %%v in ('"%%d\bin\java.exe" -version 2^>^&1 ^| findstr /i "version"') do (
                echo          版本: %%~v
            )
            echo.
        )
    )
)

REM 检查 JAVA_HOME
if defined JAVA_HOME (
    if exist "%JAVA_HOME%\bin\java.exe" (
        set "FOUND=true"
        echo   [JAVA_HOME] %JAVA_HOME%
        for /f "tokens=3" %%v in ('"%JAVA_HOME%\bin\java.exe" -version 2^>^&1 ^| findstr /i "version"') do (
            echo          版本: %%~v
        )
        echo.
    )
)

if "%FOUND%"=="false" (
    echo   未找到已安装的 Java
    echo.
)
goto :eof

:install_java
set "VERSION=%~2"
if "%VERSION%"=="" (
    echo [错误] 请指定版本号
    echo 用法: %~nx0 -i ^<version^>
    echo 支持: 8, 11, 17, 21
    goto :eof
)

echo.
echo ╔══════════════════════════════════════════════════════════════╗
echo ║                  安装 Java %VERSION%                         ║
echo ╚══════════════════════════════════════════════════════════════╝
echo.

REM 确定下载 URL (使用 Eclipse Temurin)
set "ARCH=x64"
if "%PROCESSOR_ARCHITECTURE%"=="ARM64" set "ARCH=aarch64"

set "DOWNLOAD_URL=https://api.adoptium.net/v3/binary/latest/%VERSION%/ga/windows/%ARCH%/jdk/hotspot/normal/eclipse"

if not exist "%JAVA_INSTALL_DIR%" mkdir "%JAVA_INSTALL_DIR%"

set "TEMP_FILE=%TEMP%\jdk-%VERSION%.zip"

echo [步骤] 下载 JDK %VERSION%...
echo   URL: %DOWNLOAD_URL%
echo.

powershell -Command "& {[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri '%DOWNLOAD_URL%' -OutFile '%TEMP_FILE%'}" 2>nul

if not exist "%TEMP_FILE%" (
    echo [错误] 下载失败
    goto :eof
)

echo [步骤] 解压...
powershell -Command "Expand-Archive -Path '%TEMP_FILE%' -DestinationPath '%JAVA_INSTALL_DIR%' -Force"

del "%TEMP_FILE%" 2>nul

REM 查找解压后的目录
for /d %%d in ("%JAVA_INSTALL_DIR%\*jdk*%VERSION%*") do (
    set "NEW_JAVA_HOME=%%d"
)

if defined NEW_JAVA_HOME (
    echo.
    echo [成功] JDK %VERSION% 已安装
    echo   路径: !NEW_JAVA_HOME!
    echo.
    
    set /p "SET_DEFAULT=是否设为默认? [Y/n] "
    if /i "!SET_DEFAULT!"=="Y" (
        call :save_java_path "!NEW_JAVA_HOME!\bin\java.exe"
    )
    if "!SET_DEFAULT!"=="" (
        call :save_java_path "!NEW_JAVA_HOME!\bin\java.exe"
    )
) else (
    echo [错误] 安装失败
)
goto :eof

:uninstall_java
echo.
echo ╔══════════════════════════════════════════════════════════════╗
echo ║                  卸载 Java                                   ║
echo ╚══════════════════════════════════════════════════════════════╝
echo.

if not exist "%JAVA_INSTALL_DIR%" (
    echo [警告] 本地 Java 目录不存在
    goto :eof
)

echo 已安装的 Java:
set "INDEX=0"
for /d %%d in ("%JAVA_INSTALL_DIR%\*") do (
    set /a "INDEX+=1"
    echo   !INDEX!. %%~nxd
)

if %INDEX%==0 (
    echo   (无)
    goto :eof
)

echo.
set /p "CHOICE=选择要卸载的编号 (1-%INDEX%): "

set "INDEX=0"
for /d %%d in ("%JAVA_INSTALL_DIR%\*") do (
    set /a "INDEX+=1"
    if "!INDEX!"=="%CHOICE%" (
        echo 正在删除: %%d
        rmdir /s /q "%%d"
        echo [成功] 已卸载
    )
)
goto :eof

:set_default_java
set "JAVA_PATH=%~2"
if "%JAVA_PATH%"=="" (
    echo [错误] 请指定 Java 路径
    goto :eof
)

if not exist "%JAVA_PATH%" (
    REM 尝试添加 bin\java.exe
    if exist "%JAVA_PATH%\bin\java.exe" (
        set "JAVA_PATH=%JAVA_PATH%\bin\java.exe"
    ) else (
        echo [错误] 路径不存在: %JAVA_PATH%
        goto :eof
    )
)

call :save_java_path "%JAVA_PATH%"
echo [成功] 默认 Java 已设置
goto :eof

endlocal
