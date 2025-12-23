@echo off
chcp 65001 >nul 2>&1
setlocal enabledelayedexpansion

REM ============================================================
REM 应用管理主脚本 (Windows)
REM 版本: 3.0.0
REM ============================================================

set "SCRIPT_DIR=%~dp0"
set "SCRIPT_DIR=%SCRIPT_DIR:~0,-1%"
call "%SCRIPT_DIR%\common.bat"

REM ============================================================
REM 帮助信息
REM ============================================================
:show_help
echo.
echo ╔══════════════════════════════════════════════════════════════╗
echo ║              %APP_NAME% 管理脚本 v3.0                        ║
echo ╚══════════════════════════════════════════════════════════════╝
echo.
echo 用法: %~nx0 ^<命令^> [选项]
echo.
echo 基础命令:
echo   start              启动应用 (默认)
echo   stop               停止应用
echo   restart            重启应用
echo   status             查看应用状态
echo.
echo 子命令: (使用 '%~nx0 ^<子命令^> -h' 查看详细帮助)
echo   info               显示应用信息
echo   logs               日志管理
echo   java               Java 环境管理
echo   arthas             Arthas 诊断工具
echo   skywalking         Skywalking Agent 管理
echo   backup             备份管理
echo   restore            还原管理
echo   upgrade            升级管理
echo   service            Windows 服务管理
echo   dump               转储管理
echo   gc                 GC 触发
echo   top                CPU/内存分析
echo   jvm                JVM 诊断与调优
echo.
echo 启动选项:
echo   -f                 启动后实时跟踪日志
echo   -j ^<path^>          指定 Java 路径 (自动持久化)
echo   --skywalking       启用 Skywalking Agent
echo   --dev              开发环境 (spring.profiles.active=dev)
echo   --prod             生产环境 (默认)
echo   --test             测试环境
echo   --profile ^<name^>   自定义环境
echo   --debug            调试模式
echo   --debug-port ^<port^> 调试端口 (默认: %DEFAULT_DEBUG_PORT%)
echo   --jvm ^<opts^>       自定义 JVM 参数 (追加)
echo   -h, --help         显示帮助信息
echo.
echo 示例:
echo   %~nx0 start                    启动应用
echo   %~nx0 start -f                 启动并跟踪日志
echo   %~nx0 start --dev              开发环境启动
echo   %~nx0 start -j "C:\jdk21\bin\java.exe"  指定 Java 启动
echo   %~nx0 start --debug            调试模式启动
echo   %~nx0 stop                     停止应用
echo   %~nx0 restart                  重启应用
echo   %~nx0 status                   查看状态
echo   %~nx0 info                     查看应用信息
echo   %~nx0 logs -f                  实时跟踪日志
echo.
goto :eof

REM ============================================================
REM 启动应用
REM ============================================================
:do_start
call :get_pid
if defined APP_PID (
    echo [警告] %APP_NAME% 已在运行 (PID: !APP_PID!)
    goto :eof
)

if not exist "%LOG_DIR%" mkdir "%LOG_DIR%"
call :check_java
if errorlevel 1 goto :eof
call :build_loader_path
call :get_local_ip

REM 构建启动参数
set "START_OPTS=--spring.config.additional-location=file:%CONFIG_DIR%/,file:%CONFIG_DIR%/config/"
set "START_OPTS=%START_OPTS% --spring.profiles.active=%SPRING_PROFILE%"
set "START_OPTS=%START_OPTS% --spring.output.ansi.enabled=always"

REM 显示启动信息
echo.
echo ╔══════════════════════════════════════════════════════════════╗
echo ║                     应用启动中...                            ║
echo ╚══════════════════════════════════════════════════════════════╝
echo.
echo ┌─────────────────────────────────────────────────────────────┐
echo │ 应用名称    │ %APP_NAME%
echo │ 应用版本    │ %APP_VERSION%
echo │ 运行环境    │ %SPRING_PROFILE%
echo │ Java 版本   │ %JAVA_VERSION%
echo │ Java 路径   │ %JAVA_CMD%
echo │ JVM 参数    │ %JAVA_OPTS%
if "%DEBUG_MODE%"=="true" echo │ 调试端口    │ %DEBUG_PORT%
if "%SKYWALKING_ENABLED%"=="true" echo │ Skywalking  │ 已启用
echo │ Loader 路径 │ %LOADER_PATH%
echo │ 配置目录    │ %CONFIG_DIR%
echo │ 日志目录    │ %LOG_DIR%
echo │ 启动时间    │ %date% %time%
echo └─────────────────────────────────────────────────────────────┘
echo.

REM 构建最终 JVM 参数
set "FINAL_OPTS=%JAVA_OPTS%"
if defined CUSTOM_JVM_OPTS set "FINAL_OPTS=%FINAL_OPTS% %CUSTOM_JVM_OPTS%"
if "%DEBUG_MODE%"=="true" (
    set "FINAL_OPTS=%FINAL_OPTS% -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:%DEBUG_PORT%"
)

REM Skywalking 支持
if "%SKYWALKING_ENABLED%"=="true" (
    set "SW_JAR=%BASE_DIR%\skywalking\skywalking-agent\skywalking-agent.jar"
    if not exist "!SW_JAR!" (
        echo [警告] Skywalking Agent 未安装，尝试自动下载...
        call "%SUBSCRIPT_DIR%\skywalking.bat" install
    )
    if exist "!SW_JAR!" (
        set "FINAL_OPTS=-javaagent:!SW_JAR! -Dskywalking.agent.service_name=%APP_NAME% !FINAL_OPTS!"
    )
)

REM 启动应用
start /b "" "%JAVA_CMD%" %FINAL_OPTS% -Dloader.path="%LOADER_PATH%" -jar "%BASE_DIR%\%JAR_NAME%" %START_OPTS% > "%STARTUP_LOG%" 2>&1

REM 等待并获取 PID
timeout /t 2 /nobreak >nul
call :get_pid

if defined APP_PID (
    echo %APP_PID%> "%PID_FILE%"
    echo ╔══════════════════════════════════════════════════════════════╗
    echo ║                    √ 启动成功                                ║
    echo ╚══════════════════════════════════════════════════════════════╝
    echo.
    echo   进程 ID:    %APP_PID%
    echo   日志文件:   %STARTUP_LOG%
    echo.
    
    if "%TAIL_LOG%"=="true" (
        echo 开始实时跟踪日志...
        powershell -Command "Get-Content '%STARTUP_LOG%' -Wait -Tail 50"
    )
) else (
    echo ╔══════════════════════════════════════════════════════════════╗
    echo ║                    × 启动失败                                ║
    echo ╚══════════════════════════════════════════════════════════════╝
    echo.
    if exist "%STARTUP_LOG%" (
        echo 错误日志:
        powershell -Command "Get-Content '%STARTUP_LOG%' -Tail 30"
    )
)
goto :eof

REM ============================================================
REM 停止应用
REM ============================================================
:do_stop
call :get_pid
if not defined APP_PID (
    echo [警告] %APP_NAME% 未在运行
    goto :eof
)

echo 正在停止 %APP_NAME% (PID: %APP_PID%)...
taskkill /PID %APP_PID% /F >nul 2>&1

timeout /t 2 /nobreak >nul
if exist "%PID_FILE%" del "%PID_FILE%"
echo [成功] %APP_NAME% 已停止
goto :eof

REM ============================================================
REM 重启应用
REM ============================================================
:do_restart
call :do_stop
timeout /t 2 /nobreak >nul
goto :do_start

REM ============================================================
REM 查看状态
REM ============================================================
:do_status
call :get_pid
call :build_loader_path

if defined APP_PID (
    echo [运行中] %APP_NAME% (PID: %APP_PID%)
) else (
    echo [已停止] %APP_NAME%
)
echo   Loader 路径: %LOADER_PATH%
goto :eof

REM ============================================================
REM 子命令路由
REM ============================================================
:route_subcommand
set "SUB_CMD=%~1"
shift
set "SUB_SCRIPT=%SUBSCRIPT_DIR%\%SUB_CMD%.bat"

if exist "%SUB_SCRIPT%" (
    call "%SUB_SCRIPT%" %1 %2 %3 %4 %5 %6 %7 %8 %9
) else (
    echo [错误] 子命令 '%SUB_CMD%' 的脚本不存在: %SUB_SCRIPT%
    exit /b 1
)
goto :eof

REM ============================================================
REM 参数解析
REM ============================================================
set "COMMAND="
set "TAIL_LOG=false"
set "DEBUG_MODE=false"
set "DEBUG_PORT=%DEFAULT_DEBUG_PORT%"
set "SKYWALKING_ENABLED=false"
set "SPRING_PROFILE=%DEFAULT_SPRING_PROFILE%"
set "JAVA_OPTS=%DEFAULT_JAVA_OPTS%"
set "JAVA_OPTS=%JAVA_OPTS% -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=%LOG_DIR%"
set "JAVA_OPTS=%JAVA_OPTS% -Dlogging.file.path=%LOG_DIR%"
set "CUSTOM_JVM_OPTS="

REM 子命令列表
set "SUBCOMMANDS=info logs java arthas skywalking backup restore upgrade service dump gc top jvm"

if "%~1"=="" goto :show_help

:parse_loop
if "%~1"=="" goto :execute_command

REM 检查是否是子命令
for %%s in (%SUBCOMMANDS%) do (
    if "%~1"=="%%s" (
        call :route_subcommand %*
        goto :end
    )
)

REM 主命令
if "%~1"=="start" (set "COMMAND=start" & shift & goto :parse_loop)
if "%~1"=="stop" (set "COMMAND=stop" & shift & goto :parse_loop)
if "%~1"=="restart" (set "COMMAND=restart" & shift & goto :parse_loop)
if "%~1"=="status" (set "COMMAND=status" & shift & goto :parse_loop)

REM 选项
if "%~1"=="-f" (set "TAIL_LOG=true" & shift & goto :parse_loop)
if "%~1"=="-j" (
    set "CUSTOM_JAVA=%~2"
    if exist "%~2" call :save_java_path "%~2"
    shift & shift & goto :parse_loop
)
if "%~1"=="--skywalking" (set "SKYWALKING_ENABLED=true" & shift & goto :parse_loop)
if "%~1"=="--dev" (set "SPRING_PROFILE=dev" & shift & goto :parse_loop)
if "%~1"=="--prod" (set "SPRING_PROFILE=prod" & shift & goto :parse_loop)
if "%~1"=="--test" (set "SPRING_PROFILE=test" & shift & goto :parse_loop)
if "%~1"=="--profile" (set "SPRING_PROFILE=%~2" & shift & shift & goto :parse_loop)
if "%~1"=="--debug" (set "DEBUG_MODE=true" & shift & goto :parse_loop)
if "%~1"=="--debug-port" (set "DEBUG_PORT=%~2" & shift & shift & goto :parse_loop)
if "%~1"=="--jvm" (set "CUSTOM_JVM_OPTS=%~2" & shift & shift & goto :parse_loop)
if "%~1"=="-h" goto :show_help
if "%~1"=="--help" goto :show_help
if "%~1"=="help" goto :show_help

echo [错误] 未知命令: %~1
echo.
goto :show_help

:execute_command
if not defined COMMAND set "COMMAND=start"

if "%COMMAND%"=="start" goto :do_start
if "%COMMAND%"=="stop" goto :do_stop
if "%COMMAND%"=="restart" goto :do_restart
if "%COMMAND%"=="status" goto :do_status

:end
endlocal
