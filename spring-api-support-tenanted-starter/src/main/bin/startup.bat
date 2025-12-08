@echo off
REM ============================================================
REM 租户系统启动脚本 (Windows)
REM 作者: CH
REM 创建时间: 2024/12/08
REM 版本: 1.0.0
REM ============================================================
setlocal enabledelayedexpansion

set SCRIPT_DIR=%~dp0
cd /d %SCRIPT_DIR%..
set APP_HOME=%cd%
set APP_NAME=spring-api-support-tenanted-starter
if "%APP_VERSION%"=="" set APP_VERSION=4.0.0
set JAR_NAME=%APP_NAME%-%APP_VERSION%.jar

set CONFIG_DIR=%APP_HOME%\config
set LIB_DIR=%APP_HOME%\lib
set LOG_DIR=%APP_HOME%\logs
set PID_FILE=%APP_HOME%\%APP_NAME%.pid

set DEFAULT_PORT=8080
set DEFAULT_DEBUG_PORT=5005
set COMMAND=start
set APP_PORT=
set DEBUG_PORT=%DEFAULT_DEBUG_PORT%
set DEBUG_MODE=false

:parse_args
if "%~1"=="" goto end_parse
if /i "%~1"=="start" (set COMMAND=start& shift& goto parse_args)
if /i "%~1"=="stop" (set COMMAND=stop& shift& goto parse_args)
if /i "%~1"=="restart" (set COMMAND=restart& shift& goto parse_args)
if /i "%~1"=="status" (set COMMAND=status& shift& goto parse_args)
if /i "%~1"=="debug" (set COMMAND=debug& shift& goto parse_args)
if /i "%~1"=="--port" (set APP_PORT=%~2& shift& shift& goto parse_args)
if /i "%~1"=="--debug-port" (set DEBUG_PORT=%~2& shift& shift& goto parse_args)
if /i "%~1"=="--help" goto show_help
if /i "%~1"=="-h" goto show_help
echo [错误] 未知参数: %~1
goto show_help

:end_parse
if /i "%COMMAND%"=="debug" (set DEBUG_MODE=true& set COMMAND=start)
if /i "%COMMAND%"=="start" goto start_app
if /i "%COMMAND%"=="stop" goto stop_app
if /i "%COMMAND%"=="restart" goto restart_app
if /i "%COMMAND%"=="status" goto check_status
goto show_help

:show_help
echo ============================================================
echo   %APP_NAME% 管理脚本
echo ============================================================
echo 用法: %~nx0 [命令] [选项]
echo 命令: start, stop, restart, status, debug
echo 选项: --port ^<port^>, --debug-port ^<port^>, --help
goto end

:check_status
if exist "%PID_FILE%" (
    set /p PID=<"%PID_FILE%"
    tasklist /fi "PID eq !PID!" 2>nul | find "!PID!" >nul
    if !errorlevel! equ 0 (echo [运行中] %APP_NAME% ^(PID: !PID!^)& goto end)
)
echo [已停止] %APP_NAME%
goto end

:start_app
if exist "%PID_FILE%" (
    set /p PID=<"%PID_FILE%"
    tasklist /fi "PID eq !PID!" 2>nul | find "!PID!" >nul
    if !errorlevel! equ 0 (echo [警告] %APP_NAME% 已在运行 ^(PID: !PID!^)& goto end)
)
if not exist "%LOG_DIR%" mkdir "%LOG_DIR%"
if "%JAVA_HOME%"=="" (set JAVA_CMD=java) else (set JAVA_CMD=%JAVA_HOME%\bin\java)
if "%JAVA_OPTS%"=="" set JAVA_OPTS=-Xms512m -Xmx1024m -XX:+UseG1GC
set JAVA_OPTS=%JAVA_OPTS% -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=%LOG_DIR% -Dlogging.file.path=%LOG_DIR%
if "%DEBUG_MODE%"=="true" set JAVA_OPTS=%JAVA_OPTS% -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:%DEBUG_PORT%
set SPRING_OPTS=--spring.config.location=classpath:/,file:%CONFIG_DIR%/
if "%SPRING_PROFILE%"=="" set SPRING_PROFILE=prod
set SPRING_OPTS=%SPRING_OPTS% --spring.profiles.active=%SPRING_PROFILE%
if not "%APP_PORT%"=="" set SPRING_OPTS=%SPRING_OPTS% --server.port=%APP_PORT%
set LOADER_PATH=%CONFIG_DIR%
if exist "%LIB_DIR%" set LOADER_PATH=%LOADER_PATH%,%LIB_DIR%

echo ==========================================
echo   %APP_NAME% 启动中...
echo   版本: %APP_VERSION%
if "%APP_PORT%"=="" (echo   端口: %DEFAULT_PORT%) else (echo   端口: %APP_PORT%)
if "%DEBUG_MODE%"=="true" echo   调试端口: %DEBUG_PORT%
echo ==========================================

start "%APP_NAME%" /b cmd /c ""%JAVA_CMD%" %JAVA_OPTS% -Dloader.path="%LOADER_PATH%" -jar "%APP_HOME%\%JAR_NAME%" %SPRING_OPTS% > "%LOG_DIR%\startup.log" 2>&1"
timeout /t 2 /nobreak >nul
echo [成功] %APP_NAME% 启动命令已执行
goto end

:stop_app
set FOUND_PID=
if exist "%PID_FILE%" (
    set /p FOUND_PID=<"%PID_FILE%"
    tasklist /fi "PID eq !FOUND_PID!" 2>nul | find "!FOUND_PID!" >nul
    if !errorlevel! neq 0 set FOUND_PID=
)
if "%FOUND_PID%"=="" (
    for /f "tokens=2 delims=," %%i in ('wmic process where "commandline like '%%%JAR_NAME%%%'" get processid /format:csv 2^>nul ^| find /v "ProcessId"') do set FOUND_PID=%%i
)
if "%FOUND_PID%"=="" (echo [警告] %APP_NAME% 未在运行& if exist "%PID_FILE%" del "%PID_FILE%"& goto end)
echo 正在停止 %APP_NAME% ^(PID: %FOUND_PID%^)...
taskkill /PID %FOUND_PID% /F >nul 2>&1
if exist "%PID_FILE%" del "%PID_FILE%"
echo [成功] %APP_NAME% 已停止
goto end

:restart_app
call :stop_app
timeout /t 2 /nobreak >nul
goto start_app

:end
endlocal
