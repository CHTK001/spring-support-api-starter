@echo off
REM ============================================================
REM 公共函数库 (Windows)
REM ============================================================

REM 初始化路径
if not defined SCRIPT_DIR (
    set "SCRIPT_DIR=%~dp0"
    set "SCRIPT_DIR=!SCRIPT_DIR:~0,-1!"
)
for %%i in ("%SCRIPT_DIR%\..") do set "BASE_DIR=%%~fi"

REM 应用配置 (支持 Maven 资源过滤)
if not defined APP_NAME set "APP_NAME=@project.artifactId@"
if not defined APP_VERSION set "APP_VERSION=@project.version@"
set "JAR_NAME=%APP_NAME%-%APP_VERSION%.jar"

REM 目录配置
set "CONFIG_DIR=%BASE_DIR%\config"
set "LIB_DIR=%BASE_DIR%\lib"
set "LOG_DIR=%BASE_DIR%\logs"
set "BACKUP_DIR=%BASE_DIR%\backup"
set "SUBSCRIPT_DIR=%SCRIPT_DIR%\script\windows"

REM 文件配置
set "PID_FILE=%BASE_DIR%\%APP_NAME%.pid"
set "STARTUP_LOG=%LOG_DIR%\startup.log"
set "APP_ENV_FILE=%BASE_DIR%\.app.env"
set "JAVA_PATH_FILE=%BASE_DIR%\.java_path"

REM 默认配置
set "DEFAULT_JAVA_OPTS=-Xms512m -Xmx1024m -XX:+UseG1GC"
set "DEFAULT_SPRING_PROFILE=prod"
set "DEFAULT_DEBUG_PORT=5005"

goto :eof

REM ============================================================
REM 检查 Java
REM ============================================================
:check_java
REM 先检查持久化的 Java 路径
if exist "%JAVA_PATH_FILE%" (
    set /p JAVA_CMD=<"%JAVA_PATH_FILE%"
    if exist "!JAVA_CMD!" goto :java_found
)
REM 检查自定义 Java
if defined CUSTOM_JAVA (
    if exist "%CUSTOM_JAVA%" (
        set "JAVA_CMD=%CUSTOM_JAVA%"
        goto :java_found
    )
    echo [错误] 指定的 Java 路径不存在: %CUSTOM_JAVA%
    exit /b 1
)
REM 检查 JAVA_HOME
if defined JAVA_HOME (
    if exist "%JAVA_HOME%\bin\java.exe" (
        set "JAVA_CMD=%JAVA_HOME%\bin\java.exe"
        goto :java_found
    )
)
REM 检查 PATH
where java >nul 2>&1
if %errorlevel%==0 (
    set "JAVA_CMD=java"
    goto :java_found
)
echo [错误] 未找到 Java 环境
echo 请设置 JAVA_HOME 或使用 -j 指定 Java 路径
exit /b 1

:java_found
for /f "tokens=3" %%i in ('"%JAVA_CMD%" -version 2^>^&1 ^| findstr /i "version"') do set "JAVA_VERSION=%%~i"
exit /b 0

REM ============================================================
REM 保存 Java 路径
REM ============================================================
:save_java_path
echo %~1> "%JAVA_PATH_FILE%"
set "JAVA_CMD=%~1"
echo [信息] Java 路径已保存: %~1
exit /b 0

REM ============================================================
REM 获取 PID
REM ============================================================
:get_pid
set "APP_PID="
if exist "%PID_FILE%" (
    set /p APP_PID=<"%PID_FILE%"
    REM 验证进程是否存在
    tasklist /FI "PID eq !APP_PID!" 2>nul | find "!APP_PID!" >nul
    if errorlevel 1 set "APP_PID="
)
if not defined APP_PID (
    for /f "tokens=2" %%i in ('tasklist /FI "IMAGENAME eq java.exe" /FO LIST 2^>nul ^| find "PID:"') do (
        wmic process where "ProcessId=%%i" get CommandLine 2>nul | find "%JAR_NAME%" >nul
        if not errorlevel 1 set "APP_PID=%%i"
    )
)
exit /b 0

REM ============================================================
REM 获取本机 IP
REM ============================================================
:get_local_ip
set "LOCAL_IP=127.0.0.1"
for /f "tokens=2 delims=:" %%a in ('ipconfig ^| findstr /c:"IPv4"') do (
    for /f "tokens=*" %%b in ("%%a") do (
        set "LOCAL_IP=%%b"
        goto :got_ip
    )
)
:got_ip
set "LOCAL_IP=%LOCAL_IP: =%"
exit /b 0

REM ============================================================
REM 构建 Loader Path
REM ============================================================
:build_loader_path
set "LOADER_PATH=%CONFIG_DIR%"
if exist "%LIB_DIR%" set "LOADER_PATH=%LOADER_PATH%,%LIB_DIR%"
exit /b 0

REM ============================================================
REM 打印标题
REM ============================================================
:print_title
echo.
echo ╔══════════════════════════════════════════════════════════════╗
echo ║  %~1
echo ╚══════════════════════════════════════════════════════════════╝
echo.
exit /b 0
