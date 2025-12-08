@echo off
REM ============================================================
REM 运维监控系统启动脚本 (Windows)
REM 作者: CH
REM 创建时间: 2024/12/08
REM 版本: 1.0.0
REM ============================================================
setlocal enabledelayedexpansion

REM 基础配置
set SCRIPT_DIR=%~dp0
cd /d %SCRIPT_DIR%..
set APP_HOME=%cd%
set APP_NAME=spring-api-support-monitor-starter
if "%APP_VERSION%"=="" set APP_VERSION=4.0.0
set JAR_NAME=%APP_NAME%-%APP_VERSION%.jar

REM 目录配置
set CONFIG_DIR=%APP_HOME%\config
set LIB_DIR=%APP_HOME%\lib
set LOG_DIR=%APP_HOME%\logs
set PID_FILE=%APP_HOME%\%APP_NAME%.pid

REM 默认端口配置
set DEFAULT_PORT=8080
set DEFAULT_DEBUG_PORT=5005

REM 初始化参数
set COMMAND=start
set APP_PORT=
set DEBUG_PORT=%DEFAULT_DEBUG_PORT%
set DEBUG_MODE=false

REM 解析命令行参数
:parse_args
if "%~1"=="" goto end_parse
if /i "%~1"=="start" (
    set COMMAND=start
    shift
    goto parse_args
)
if /i "%~1"=="stop" (
    set COMMAND=stop
    shift
    goto parse_args
)
if /i "%~1"=="restart" (
    set COMMAND=restart
    shift
    goto parse_args
)
if /i "%~1"=="status" (
    set COMMAND=status
    shift
    goto parse_args
)
if /i "%~1"=="debug" (
    set COMMAND=debug
    shift
    goto parse_args
)
if /i "%~1"=="--port" (
    set APP_PORT=%~2
    shift
    shift
    goto parse_args
)
if /i "%~1"=="--debug-port" (
    set DEBUG_PORT=%~2
    shift
    shift
    goto parse_args
)
if /i "%~1"=="--help" goto show_help
if /i "%~1"=="-h" goto show_help
echo [错误] 未知参数: %~1
goto show_help

:end_parse

REM 调试模式处理
if /i "%COMMAND%"=="debug" (
    set DEBUG_MODE=true
    set COMMAND=start
)

REM 执行命令
if /i "%COMMAND%"=="start" goto start_app
if /i "%COMMAND%"=="stop" goto stop_app
if /i "%COMMAND%"=="restart" goto restart_app
if /i "%COMMAND%"=="status" goto check_status
goto show_help

REM ============================================================
REM 显示帮助信息
REM ============================================================
:show_help
echo ============================================================
echo   %APP_NAME% 管理脚本
echo ============================================================
echo.
echo 用法:
echo   %~nx0 [命令] [选项]
echo.
echo 命令:
echo   start       启动应用 (默认)
echo   stop        停止应用
echo   restart     重启应用
echo   status      查看应用状态
echo   debug       以调试模式启动
echo.
echo 选项:
echo   --port ^<port^>         指定应用端口 (默认: %DEFAULT_PORT%)
echo   --debug-port ^<port^>   指定调试端口 (默认: %DEFAULT_DEBUG_PORT%)
echo   --help, -h            显示帮助信息
echo.
echo 示例:
echo   %~nx0 start                        # 启动应用
echo   %~nx0 start --port 9090            # 使用指定端口启动
echo   %~nx0 debug --debug-port 8000      # 以调试模式启动
echo   %~nx0 restart                      # 重启应用
echo   %~nx0 stop                         # 停止应用
echo.
echo 环境变量:
echo   APP_VERSION       应用版本号 (默认: 4.0.0)
echo   JAVA_HOME         Java安装目录
echo   JAVA_OPTS         JVM参数
echo   SPRING_PROFILE    Spring配置文件 (默认: prod)
echo.
goto end

REM ============================================================
REM 检查应用状态
REM ============================================================
:check_status
if exist "%PID_FILE%" (
    set /p PID=<"%PID_FILE%"
    tasklist /fi "PID eq !PID!" 2>nul | find "!PID!" >nul
    if !errorlevel! equ 0 (
        echo [运行中] %APP_NAME% (PID: !PID!)
        goto end
    )
)
REM 通过窗口标题查找
for /f "tokens=2" %%i in ('tasklist /fi "WINDOWTITLE eq %APP_NAME%" /fo list 2^>nul ^| find "PID:"') do (
    echo [运行中] %APP_NAME% (PID: %%i)
    goto end
)
echo [已停止] %APP_NAME%
goto end

REM ============================================================
REM 启动应用
REM ============================================================
:start_app
REM 检查是否已运行
if exist "%PID_FILE%" (
    set /p PID=<"%PID_FILE%"
    tasklist /fi "PID eq !PID!" 2>nul | find "!PID!" >nul
    if !errorlevel! equ 0 (
        echo [警告] %APP_NAME% 已在运行 (PID: !PID!)
        goto end
    )
)

REM 创建日志目录
if not exist "%LOG_DIR%" mkdir "%LOG_DIR%"

REM 检查Java环境
if "%JAVA_HOME%"=="" (
    set JAVA_CMD=java
) else (
    set JAVA_CMD=%JAVA_HOME%\bin\java
)
where %JAVA_CMD% >nul 2>nul
if %errorlevel% neq 0 (
    echo [错误] 未找到Java环境，请设置JAVA_HOME或确保java在PATH中
    goto end
)

REM JVM配置
if "%JAVA_OPTS%"=="" set JAVA_OPTS=-Xms1024m -Xmx2048m -XX:+UseG1GC
set JAVA_OPTS=%JAVA_OPTS% -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=%LOG_DIR%
set JAVA_OPTS=%JAVA_OPTS% -Dlogging.file.path=%LOG_DIR%

REM 调试模式
if "%DEBUG_MODE%"=="true" (
    set JAVA_OPTS=%JAVA_OPTS% -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:%DEBUG_PORT%
)

REM Spring配置
set SPRING_OPTS=--spring.config.location=classpath:/,file:%CONFIG_DIR%/
if "%SPRING_PROFILE%"=="" set SPRING_PROFILE=prod
set SPRING_OPTS=%SPRING_OPTS% --spring.profiles.active=%SPRING_PROFILE%
if not "%APP_PORT%"=="" set SPRING_OPTS=%SPRING_OPTS% --server.port=%APP_PORT%

REM 构建loader.path
set LOADER_PATH=%CONFIG_DIR%
if exist "%LIB_DIR%" set LOADER_PATH=%LOADER_PATH%,%LIB_DIR%

REM 显示启动信息
echo ==========================================
echo   %APP_NAME% 启动中...
echo ==========================================
echo   版本: %APP_VERSION%
if "%APP_PORT%"=="" (
    echo   端口: %DEFAULT_PORT%
) else (
    echo   端口: %APP_PORT%
)
if "%DEBUG_MODE%"=="true" echo   调试端口: %DEBUG_PORT%
echo ==========================================

REM 启动应用 (使用 start 命令在新窗口运行)
start "%APP_NAME%" /b cmd /c ""%JAVA_CMD%" %JAVA_OPTS% -Dloader.path="%LOADER_PATH%" -jar "%APP_HOME%\%JAR_NAME%" %SPRING_OPTS% > "%LOG_DIR%\startup.log" 2>&1"

REM 获取并保存PID
timeout /t 2 /nobreak >nul
for /f "tokens=2" %%i in ('tasklist /fi "WINDOWTITLE eq %APP_NAME%" /fo list 2^>nul ^| find "PID:"') do (
    echo %%i > "%PID_FILE%"
    echo [成功] %APP_NAME% 已启动 (PID: %%i)
    echo 日志文件: %LOG_DIR%\startup.log
    goto end
)

REM 尝试通过进程名查找
for /f "tokens=2 delims=," %%i in ('wmic process where "commandline like '%%%JAR_NAME%%%'" get processid /format:csv 2^>nul ^| find /v "ProcessId" ^| find /v ""') do (
    echo %%i > "%PID_FILE%"
    echo [成功] %APP_NAME% 已启动 (PID: %%i)
    echo 日志文件: %LOG_DIR%\startup.log
    goto end
)

echo [成功] %APP_NAME% 启动命令已执行
echo 日志文件: %LOG_DIR%\startup.log
goto end

REM ============================================================
REM 停止应用
REM ============================================================
:stop_app
set FOUND_PID=

REM 从PID文件获取
if exist "%PID_FILE%" (
    set /p FOUND_PID=<"%PID_FILE%"
    tasklist /fi "PID eq !FOUND_PID!" 2>nul | find "!FOUND_PID!" >nul
    if !errorlevel! neq 0 set FOUND_PID=
)

REM 通过进程查找
if "%FOUND_PID%"=="" (
    for /f "tokens=2 delims=," %%i in ('wmic process where "commandline like '%%%JAR_NAME%%%'" get processid /format:csv 2^>nul ^| find /v "ProcessId"') do (
        set FOUND_PID=%%i
    )
)

if "%FOUND_PID%"=="" (
    echo [警告] %APP_NAME% 未在运行
    if exist "%PID_FILE%" del "%PID_FILE%"
    goto end
)

echo 正在停止 %APP_NAME% (PID: %FOUND_PID%)...
taskkill /PID %FOUND_PID% /F >nul 2>&1
if exist "%PID_FILE%" del "%PID_FILE%"
echo [成功] %APP_NAME% 已停止
goto end

REM ============================================================
REM 重启应用
REM ============================================================
:restart_app
call :stop_app
timeout /t 2 /nobreak >nul
goto start_app

:end
endlocal
