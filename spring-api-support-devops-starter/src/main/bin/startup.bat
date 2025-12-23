@echo off
chcp 65001 >nul 2>&1
setlocal enabledelayedexpansion

REM ============================================================
REM 统一应用管理脚本 (Windows)
REM 支持动态名称替换 (Maven 资源过滤)
REM 版本: 2.0.0
REM ============================================================

REM 基础配置
set "SCRIPT_DIR=%~dp0"
set "SCRIPT_DIR=%SCRIPT_DIR:~0,-1%"
for %%i in ("%SCRIPT_DIR%\..") do set "APP_HOME=%%~fi"

REM Maven 资源过滤替换
if not defined APP_NAME set "APP_NAME=@project.artifactId@"
if not defined APP_VERSION set "APP_VERSION=@project.version@"
set "JAR_NAME=%APP_NAME%-%APP_VERSION%.jar"

REM 目录配置
set "CONFIG_DIR=%APP_HOME%\config"
set "LIB_DIR=%APP_HOME%\lib"
set "LOG_DIR=%APP_HOME%\logs"
set "PID_FILE=%APP_HOME%\%APP_NAME%.pid"

REM 日志文件
set "STARTUP_LOG=%LOG_DIR%\startup.log"

REM JVM 配置
if not defined JAVA_OPTS set "JAVA_OPTS=-Xms512m -Xmx1024m -XX:+UseG1GC"
set "JAVA_OPTS=%JAVA_OPTS% -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=%LOG_DIR%"

REM Spring 配置
if not defined SPRING_PROFILE set "SPRING_PROFILE=prod"

REM 服务端口配置
if not defined SERVER_PORT set "SERVER_PORT=8080"

REM 调试配置
set "DEBUG_PORT=5005"
set "DEBUG_MODE=false"
set "TAIL_LOG=false"
set "SHOW_LOGS="

REM 解析命令
set "COMMAND=start"
set "TOP_N=10"
set "GC_TYPE=full"
set "DUMP_MODE=all"
if "%~1"=="" goto :show_help
set "COMMAND=%~1"

REM 参数解析
:parse_args
shift
if "%~1"=="" goto :execute
if "%~1"=="--dev" (set "SPRING_PROFILE=dev" & goto :parse_args)
if "%~1"=="--prod" (set "SPRING_PROFILE=prod" & goto :parse_args)
if "%~1"=="--test" (set "SPRING_PROFILE=test" & goto :parse_args)
if "%~1"=="--tail" (set "TAIL_LOG=true" & goto :parse_args)
if "%~1"=="--debug-port" (set "DEBUG_PORT=%~2" & shift & goto :parse_args)
if "%~1"=="--profile" (set "SPRING_PROFILE=%~2" & shift & goto :parse_args)
if "%~1"=="--logs" (set "SHOW_LOGS=%~2" & shift & goto :parse_args)
if "%~1"=="--java" (set "CUSTOM_JAVA=%~2" & shift & goto :parse_args)
if "%~1"=="--jvm-opts" (set "JAVA_OPTS=%~2" & shift & goto :parse_args)
if "%~1"=="--output" (set "DUMP_OUTPUT=%~2" & shift & goto :parse_args)
if "%~1"=="--dump-path" (set "DUMP_OUTPUT=%~2" & shift & goto :parse_args)
if "%~1"=="--package" (set "UPGRADE_PACKAGE=%~2" & shift & goto :parse_args)
if "%~1"=="--no-restart" (set "NO_RESTART=true" & goto :parse_args)
if "%~1"=="--skywalking" (set "SKYWALKING_ENABLED=true" & goto :parse_args)
if "%~1"=="--sw-backend" (set "SW_BACKEND=%~2" & shift & goto :parse_args)
if "%~1"=="--sw-name" (set "SW_SERVICE_NAME=%~2" & shift & goto :parse_args)
if "%~1"=="--help" goto :show_help
if "%~1"=="-h" goto :show_help
REM GC 类型
if "%~1"=="young" (set "GC_TYPE=young" & goto :parse_args)
if "%~1"=="full" (set "GC_TYPE=full" & goto :parse_args)
REM Dump 模式 / 备份模式
if "%~1"=="heap" (set "DUMP_MODE=heap" & set "EXTRA_MODE=heap" & goto :parse_args)
if "%~1"=="thread" (set "DUMP_MODE=thread" & set "EXTRA_MODE=thread" & goto :parse_args)
if "%~1"=="all" (set "DUMP_MODE=all" & set "EXTRA_MODE=all" & goto :parse_args)
if "%~1"=="config" (set "EXTRA_MODE=config" & goto :parse_args)
if "%~1"=="jar" (set "EXTRA_MODE=jar" & goto :parse_args)
REM Skywalking 子命令
if "%~1"=="install" (set "SW_COMMAND=install" & goto :parse_args)
if "%~1"=="check" (set "SW_COMMAND=check" & goto :parse_args)
if "%~1"=="clean" (set "SW_COMMAND=clean" & goto :parse_args)
if "%~1"=="info" (
    if "%COMMAND%"=="skywalking" (set "SW_COMMAND=info" & goto :parse_args)
)
if "%~1"=="config" (
    if "%COMMAND%"=="skywalking" (set "SW_COMMAND=config" & goto :parse_args)
)
REM 数字参数 (Top N)
echo %~1| findstr /r "^[0-9]*$" >nul && (set "TOP_N=%~1" & goto :parse_args)
goto :parse_args

:execute
if "%COMMAND%"=="start" goto :start_app
if "%COMMAND%"=="stop" goto :stop_app
if "%COMMAND%"=="restart" goto :restart_app
if "%COMMAND%"=="status" goto :check_status
if "%COMMAND%"=="info" goto :show_info
if "%COMMAND%"=="logs" goto :show_logs
if "%COMMAND%"=="tail" goto :tail_logs
if "%COMMAND%"=="debug" (set "DEBUG_MODE=true" & goto :start_app)
if "%COMMAND%"=="jvm" goto :show_jvm_info
if "%COMMAND%"=="gc" goto :run_gc
if "%COMMAND%"=="top-cpu" goto :run_top_cpu
if "%COMMAND%"=="top-mem" goto :run_top_mem
if "%COMMAND%"=="dump" goto :run_dump
if "%COMMAND%"=="install-service" goto :install_service
if "%COMMAND%"=="uninstall-service" goto :uninstall_service
if "%COMMAND%"=="service-status" goto :show_service_status
if "%COMMAND%"=="backup" goto :run_backup
if "%COMMAND%"=="restore" goto :run_restore
if "%COMMAND%"=="upgrade" goto :run_upgrade
if "%COMMAND%"=="rollback" goto :run_rollback
if "%COMMAND%"=="rollback-status" goto :run_rollback_status
if "%COMMAND%"=="arthas" goto :start_arthas
if "%COMMAND%"=="skywalking" goto :run_skywalking
if "%COMMAND%"=="help" goto :show_help
if "%COMMAND%"=="--help" goto :show_help
if "%COMMAND%"=="-h" goto :show_help
goto :show_help

REM ============================================================
REM 帮助信息
REM ============================================================
:show_help
echo.
echo ╔══════════════════════════════════════════════════════════════╗
echo ║              %APP_NAME% 管理脚本                    ║
echo ║              版本: %APP_VERSION%                              ║
echo ╚══════════════════════════════════════════════════════════════╝
echo.
echo 用法: %~nx0 [命令] [选项]
echo.
echo 基础命令:
echo   start              启动应用 (默认)
echo   stop               停止应用
echo   restart            重启应用
echo   status             查看应用状态
echo   info               显示详细运行信息
echo.
echo 日志命令:
echo   logs               查看最近日志
echo   tail               实时跟踪日志
echo.
echo 诊断命令:
echo   jvm                查看 JVM 信息
echo   gc [young/full]    触发 GC
echo   top-cpu [N]        分析高 CPU 线程 (Top N, 默认 10)
echo   top-mem [N]        分析高内存对象 (Top N, 默认 10)
echo   dump [heap/thread/all]  生成转储文件
echo   arthas             启动 Arthas 诊断工具
echo.
echo 服务管理命令:
echo   install-service    安装为 Windows 服务
echo   uninstall-service  卸载 Windows 服务
echo   service-status     查看服务状态
echo.
echo 运维命令:
echo   backup             备份配置/JAR (config/jar/all)
echo   restore            还原配置/JAR
echo   upgrade            从压缩包升级 (zip/tar.gz)
echo   rollback           回滚到上一个还原点
echo   rollback-status    查看还原点状态
echo.
echo Skywalking 命令:
echo   skywalking         管理 Skywalking Agent (install/check/clean/info/config)
echo.
echo 选项:
echo   --java ^<path^>       指定 Java 路径
echo   --jvm-opts ^<opts^>   自定义 JVM 参数
echo   --debug-port ^<port^> 指定调试端口 (默认: 5005)
echo   --output ^<path^>     dump 输出目录
echo   --package ^<path^>    升级包路径 (zip/tar.gz)
echo   --no-restart        升级后不自动重启
echo   --skywalking        启用 Skywalking APM
echo   --sw-backend ^<addr^> 指定 Skywalking OAP 地址
echo   --sw-name ^<name^>    指定 Skywalking 服务名
echo   --dev               使用开发环境
echo   --prod              使用生产环境 (默认)
echo   --test              使用测试环境
echo   --profile ^<name^>    指定自定义环境
echo   --logs ^<N^>          显示最近 N 条日志
echo   --tail              启动后实时跟踪日志
echo   --help, -h          显示帮助信息
echo.
echo 示例:
echo.
echo   基础命令:
echo     %~nx0 start                           启动应用
echo     %~nx0 start --dev                     开发环境启动
echo     %~nx0 start --prod                    生产环境启动
echo     %~nx0 start --test                    测试环境启动
echo     %~nx0 start --profile staging         自定义环境启动
echo     %~nx0 start --tail                    启动并实时跟踪日志
echo     %~nx0 start --logs 200                启动后显示最近 200 条日志
echo     %~nx0 start --java "C:\jdk17\bin\java.exe"  指定 Java 路径
echo     %~nx0 stop                            停止应用
echo     %~nx0 restart                         重启应用
echo     %~nx0 status                          查看状态
echo     %~nx0 info                            查看详细信息
echo.
echo   日志命令:
echo     %~nx0 logs                            查看最近 100 条日志
echo     %~nx0 logs --logs 50                  查看最近 50 条日志
echo     %~nx0 tail                            实时跟踪日志
echo.
echo   调试命令:
echo     %~nx0 debug                           调试模式启动 (端口 5005)
echo     %~nx0 debug --debug-port 5006         调试模式启动 (指定端口)
echo.
echo   JVM 诊断:
echo     %~nx0 jvm                             查看 JVM 信息
echo     %~nx0 gc                              触发 Full GC
echo     %~nx0 gc young                        触发 Young GC
echo     %~nx0 top-cpu                         高 CPU 线程分析 (Top 10)
echo     %~nx0 top-cpu 20                      高 CPU 线程分析 (Top 20)
echo     %~nx0 top-mem                         高内存对象分析 (Top 10)
echo     %~nx0 top-mem 20                      高内存对象分析 (Top 20)
echo.
echo   Dump 转储:
echo     %~nx0 dump                            生成堆+线程转储
echo     %~nx0 dump heap                       仅堆转储
echo     %~nx0 dump thread                     仅线程转储
echo     %~nx0 dump --output D:\dump           指定输出目录
echo.
echo   服务管理 (需管理员权限):
echo     %~nx0 install-service                 安装为 Windows 服务
echo     %~nx0 uninstall-service               卸载 Windows 服务
echo     %~nx0 service-status                  查看服务状态
echo.
echo   运维命令:
echo     %~nx0 backup                          备份配置和 JAR
echo     %~nx0 backup config                   仅备份配置
echo     %~nx0 backup jar                      仅备份 JAR
echo     %~nx0 restore                         还原配置或 JAR
echo     %~nx0 upgrade --package D:\app.zip   从压缩包升级
echo     %~nx0 upgrade --package D:\app.tar.gz --no-restart  升级但不重启
echo     %~nx0 rollback                        回滚到上一版本
echo     %~nx0 rollback-status                 查看还原点状态
echo     %~nx0 arthas                          启动 Arthas 诊断
echo.
echo   Skywalking APM:
echo     %~nx0 skywalking install              安装 Skywalking Agent
echo     %~nx0 skywalking check                检查 Agent 安装状态
echo     %~nx0 skywalking clean                清理 Agent 缓存
echo     %~nx0 skywalking info                 显示 Agent 配置
echo     %~nx0 skywalking config               配置 Agent
echo     %~nx0 start --skywalking              启动并启用 Skywalking
echo     %~nx0 start --skywalking --sw-backend 192.168.1.100:11800
echo     %~nx0 start --skywalking --sw-name MyApp
echo.
goto :eof

REM ============================================================
REM 检查 Java
REM ============================================================
:check_java
if defined CUSTOM_JAVA (
    if exist "%CUSTOM_JAVA%" (
        set "JAVA_CMD=%CUSTOM_JAVA%"
        goto :java_found
    )
    echo [错误] 指定的 Java 路径不存在: %CUSTOM_JAVA%
    exit /b 1
)
if defined JAVA_HOME (
    if exist "%JAVA_HOME%\bin\java.exe" (
        set "JAVA_CMD=%JAVA_HOME%\bin\java.exe"
        goto :java_found
    )
)
where java >nul 2>&1
if %errorlevel%==0 (
    set "JAVA_CMD=java"
    goto :java_found
)
echo [错误] 未找到 Java 环境
echo 请设置 JAVA_HOME 或使用 --java 指定 Java 路径
exit /b 1

:java_found
for /f "tokens=3" %%i in ('"%JAVA_CMD%" -version 2^>^&1 ^| findstr /i "version"') do set "JAVA_VERSION=%%~i"
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
REM 获取服务端口
REM ============================================================
:get_server_port
set "ACTUAL_PORT=%SERVER_PORT%"
if exist "%CONFIG_DIR%\application.yml" (
    for /f "tokens=2 delims=:" %%a in ('findstr /r "^[ ]*port:" "%CONFIG_DIR%\application.yml" 2^>nul') do (
        for /f "tokens=*" %%b in ("%%a") do set "ACTUAL_PORT=%%b"
    )
)
set "ACTUAL_PORT=%ACTUAL_PORT: =%"
if "%ACTUAL_PORT%"=="" set "ACTUAL_PORT=8080"
exit /b 0

REM ============================================================
REM 获取 context-path
REM ============================================================
:get_context_path
set "CONTEXT_PATH="
if exist "%CONFIG_DIR%\application.yml" (
    for /f "tokens=2 delims=:" %%a in ('findstr /c:"context-path:" "%CONFIG_DIR%\application.yml" 2^>nul') do (
        for /f "tokens=*" %%b in ("%%a") do set "CONTEXT_PATH=%%b"
    )
)
set "CONTEXT_PATH=%CONTEXT_PATH: =%"
exit /b 0

REM ============================================================
REM 检测 Swagger
REM ============================================================
:check_swagger
set "SWAGGER_ENABLED=false"
if exist "%CONFIG_DIR%" (
    findstr /s /i /c:"swagger" /c:"springdoc" /c:"knife4j" "%CONFIG_DIR%\*.yml" >nul 2>&1
    if not errorlevel 1 set "SWAGGER_ENABLED=true"
)
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
REM 状态检查
REM ============================================================
:check_status
call :get_pid
if defined APP_PID (
    echo [运行中] %APP_NAME% (PID: %APP_PID%)
) else (
    echo [已停止] %APP_NAME%
)
goto :eof

REM ============================================================
REM 显示详细信息
REM ============================================================
:show_info
echo.
echo ╔══════════════════════════════════════════════════════════════╗
echo ║                    应用运行信息                              ║
echo ╚══════════════════════════════════════════════════════════════╝
echo.
echo 【基础信息】
echo   应用名称:     %APP_NAME%
echo   应用版本:     %APP_VERSION%
echo   JAR 文件:     %JAR_NAME%
echo   应用目录:     %APP_HOME%
echo   配置目录:     %CONFIG_DIR%
echo   日志目录:     %LOG_DIR%
echo.

call :get_pid
if defined APP_PID (
    echo 【运行状态】
    echo   状态:         运行中
    echo   进程 ID:      %APP_PID%
) else (
    echo 【运行状态】
    echo   状态:         已停止
)
echo.
echo 【日志文件】
if exist "%STARTUP_LOG%" (
    echo   启动日志:     %STARTUP_LOG%
)
echo.
goto :eof

REM ============================================================
REM 显示日志
REM ============================================================
:show_logs
set "LOG_FILE=%STARTUP_LOG%"
if not exist "%LOG_FILE%" (
    echo [警告] 日志文件不存在: %LOG_FILE%
    goto :eof
)
echo ════════════════════════════════════════════════════════════════
echo   最近日志 - %LOG_FILE%
echo ════════════════════════════════════════════════════════════════
if defined SHOW_LOGS (
    powershell -Command "Get-Content '%LOG_FILE%' -Tail %SHOW_LOGS%"
) else (
    powershell -Command "Get-Content '%LOG_FILE%' -Tail 100"
)
goto :eof

REM ============================================================
REM 实时跟踪日志
REM ============================================================
:tail_logs
set "LOG_FILE=%STARTUP_LOG%"
if not exist "%LOG_FILE%" (
    echo [警告] 日志文件不存在: %LOG_FILE%
    echo 等待日志文件生成...
    timeout /t 5 /nobreak >nul
)
if exist "%LOG_FILE%" (
    echo ════════════════════════════════════════════════════════════════
    echo   实时日志跟踪 - %LOG_FILE%
    echo   按 Ctrl+C 退出
    echo ════════════════════════════════════════════════════════════════
    powershell -Command "Get-Content '%LOG_FILE%' -Wait -Tail 50"
) else (
    echo [错误] 日志文件仍不存在
)
goto :eof

REM ============================================================
REM 启动应用
REM ============================================================
:start_app
call :get_pid
if defined APP_PID (
    echo [警告] %APP_NAME% 已在运行 (PID: %APP_PID%)
    goto :eof
)

if not exist "%LOG_DIR%" mkdir "%LOG_DIR%"
call :check_java
if errorlevel 1 goto :eof
call :get_local_ip
call :get_server_port
call :get_context_path
call :check_swagger

REM 构建 loader path
set "LOADER_PATH=%CONFIG_DIR%"
if exist "%LIB_DIR%" set "LOADER_PATH=%LOADER_PATH%,%LIB_DIR%"

REM 启动参数
set "START_OPTS=--spring.config.location=classpath:/,file:%CONFIG_DIR%/"
set "START_OPTS=%START_OPTS% --spring.profiles.active=%SPRING_PROFILE%"

echo.
echo ╔══════════════════════════════════════════════════════════════╗
echo ║                     应用启动中...                            ║
echo ╚══════════════════════════════════════════════════════════════╝
echo.
echo ┌─────────────────────────────────────────────────────────────┐
echo │ 应用名称    │ %APP_NAME%
echo │ 应用版本    │ %APP_VERSION%
echo │ 运行环境    │ %SPRING_PROFILE%
echo │ 服务地址    │ http://%LOCAL_IP%:%ACTUAL_PORT%%CONTEXT_PATH%
echo │ 本地地址    │ http://localhost:%ACTUAL_PORT%%CONTEXT_PATH%
if "%SWAGGER_ENABLED%"=="true" echo │ Swagger     │ http://localhost:%ACTUAL_PORT%%CONTEXT_PATH%/doc.html
echo │ Java 版本   │ %JAVA_VERSION%
echo │ Java 路径   │ %JAVA_CMD%
echo │ 配置目录    │ %CONFIG_DIR%
echo │ 日志目录    │ %LOG_DIR%
echo │ 启动时间    │ %date% %time%
if "%DEBUG_MODE%"=="true" echo │ 调试端口    │ %DEBUG_PORT%
echo └─────────────────────────────────────────────────────────────┘
echo.

set "FINAL_OPTS=%JAVA_OPTS%"
if "%DEBUG_MODE%"=="true" (
    set "FINAL_OPTS=%FINAL_OPTS% -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:%DEBUG_PORT%"
)

REM Skywalking Agent 支持
if "%SKYWALKING_ENABLED%"=="true" (
    set "SW_AGENT_HOME=%APP_HOME%\skywalking-agent"
    set "SW_AGENT_JAR=!SW_AGENT_HOME!\skywalking-agent.jar"
    if exist "!SW_AGENT_JAR!" (
        echo │ Skywalking   │ 已启用
        echo │ SW Agent     │ !SW_AGENT_JAR!
        set "SW_OPTS=-javaagent:!SW_AGENT_JAR!"
        if defined SW_BACKEND (
            set "SW_OPTS=!SW_OPTS! -Dskywalking.collector.backend_service=%SW_BACKEND%"
            echo │ SW Backend   │ %SW_BACKEND%
        )
        if defined SW_SERVICE_NAME (
            set "SW_OPTS=!SW_OPTS! -Dskywalking.agent.service_name=%SW_SERVICE_NAME%"
            echo │ SW Service   │ %SW_SERVICE_NAME%
        ) else (
            set "SW_OPTS=!SW_OPTS! -Dskywalking.agent.service_name=%APP_NAME%"
            echo │ SW Service   │ %APP_NAME%
        )
        set "FINAL_OPTS=!SW_OPTS! !FINAL_OPTS!"
    ) else (
        echo [警告] Skywalking Agent 未安装，请先运行: %~nx0 skywalking install
        echo [提示] 本次将不启用 Skywalking 监控
    )
)

REM 启动应用
start /b "" "%JAVA_CMD%" %FINAL_OPTS% -Dloader.path="%LOADER_PATH%" -jar "%APP_HOME%\%JAR_NAME%" %START_OPTS% > "%STARTUP_LOG%" 2>&1

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
        goto :tail_logs
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
:stop_app
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
:restart_app
call :stop_app
timeout /t 2 /nobreak >nul
goto :start_app

REM ============================================================
REM JVM 信息
REM ============================================================
:show_jvm_info
call :get_pid
if not defined APP_PID (
    echo [警告] %APP_NAME% 未在运行
    goto :eof
)
if exist "%SCRIPT_DIR%\script\windows\jvm-info.bat" (
    call "%SCRIPT_DIR%\script\windows\jvm-info.bat" "%APP_PID%" "%APP_NAME%"
) else (
    echo [提示] JVM 信息脚本不存在
)
goto :eof

REM ============================================================
REM GC 触发
REM ============================================================
:run_gc
call :get_pid
if not defined APP_PID (
    echo [错误] %APP_NAME% 未在运行
    goto :eof
)
if exist "%SCRIPT_DIR%\script\windows\gc.bat" (
    call "%SCRIPT_DIR%\script\windows\gc.bat" "%APP_PID%" "%GC_TYPE%" "%APP_NAME%"
) else (
    echo [提示] GC 脚本不存在
)
goto :eof

REM ============================================================
REM 高 CPU 线程分析
REM ============================================================
:run_top_cpu
call :get_pid
if not defined APP_PID (
    echo [错误] %APP_NAME% 未在运行
    goto :eof
)
if exist "%SCRIPT_DIR%\script\windows\top-cpu.bat" (
    call "%SCRIPT_DIR%\script\windows\top-cpu.bat" "%APP_PID%" "%TOP_N%" "%APP_NAME%"
) else (
    echo [提示] top-cpu 脚本不存在
)
goto :eof

REM ============================================================
REM 高内存对象分析
REM ============================================================
:run_top_mem
call :get_pid
if not defined APP_PID (
    echo [错误] %APP_NAME% 未在运行
    goto :eof
)
if exist "%SCRIPT_DIR%\script\windows\top-mem.bat" (
    call "%SCRIPT_DIR%\script\windows\top-mem.bat" "%APP_PID%" "%TOP_N%" "%APP_NAME%"
) else (
    echo [提示] top-mem 脚本不存在
)
goto :eof

REM ============================================================
REM Dump 转储
REM ============================================================
:run_dump
call :get_pid
if not defined APP_PID (
    echo [错误] %APP_NAME% 未在运行
    goto :eof
)
set "DUMP_DIR=%LOG_DIR%\dump"
if defined DUMP_OUTPUT set "DUMP_DIR=%DUMP_OUTPUT%"
if exist "%SCRIPT_DIR%\script\windows\dump.bat" (
    call "%SCRIPT_DIR%\script\windows\dump.bat" "%APP_PID%" "%DUMP_MODE%" "%DUMP_DIR%" "%APP_NAME%"
) else (
    echo [提示] dump 脚本不存在
)
goto :eof

REM ============================================================
REM 安装服务
REM ============================================================
:install_service
call :check_java
if errorlevel 1 goto :eof
if exist "%SCRIPT_DIR%\script\windows\service.bat" (
    call "%SCRIPT_DIR%\script\windows\service.bat" install "%APP_NAME%" "%APP_HOME%" "%JAVA_CMD%" "%JAR_NAME%" "%JAVA_OPTS%"
) else (
    echo [提示] 服务管理脚本不存在
)
goto :eof

REM ============================================================
REM 卸载服务
REM ============================================================
:uninstall_service
if exist "%SCRIPT_DIR%\script\windows\service.bat" (
    call "%SCRIPT_DIR%\script\windows\service.bat" uninstall "%APP_NAME%" "%APP_HOME%" "" "" ""
) else (
    echo [提示] 服务管理脚本不存在
)
goto :eof

REM ============================================================
REM 服务状态
REM ============================================================
:show_service_status
if exist "%SCRIPT_DIR%\script\windows\service.bat" (
    call "%SCRIPT_DIR%\script\windows\service.bat" status "%APP_NAME%" "%APP_HOME%" "" "" ""
) else (
    echo [提示] 服务管理脚本不存在
)
goto :eof

REM ============================================================
REM 备份
REM ============================================================
:run_backup
if not defined EXTRA_MODE set "EXTRA_MODE=all"
if exist "%SCRIPT_DIR%\script\windows\backup-restore.bat" (
    call "%SCRIPT_DIR%\script\windows\backup-restore.bat" backup "%EXTRA_MODE%" "%APP_NAME%" "%JAR_NAME%" "%APP_HOME%"
) else (
    echo [提示] 备份脚本不存在
)
goto :eof

REM ============================================================
REM 还原
REM ============================================================
:run_restore
if not defined EXTRA_MODE set "EXTRA_MODE=all"
if exist "%SCRIPT_DIR%\script\windows\backup-restore.bat" (
    call "%SCRIPT_DIR%\script\windows\backup-restore.bat" restore "%EXTRA_MODE%" "%APP_NAME%" "%JAR_NAME%" "%APP_HOME%"
) else (
    echo [提示] 备份脚本不存在
)
goto :eof

REM ============================================================
REM 升级
REM ============================================================
:run_upgrade
if exist "%SCRIPT_DIR%\script\windows\upgrade-rollback.bat" (
    call "%SCRIPT_DIR%\script\windows\upgrade-rollback.bat" upgrade "%APP_NAME%" "%APP_HOME%" "%JAR_NAME%" "%UPGRADE_PACKAGE%" "%NO_RESTART%"
) else (
    echo [提示] 升级脚本不存在
)
goto :eof

REM ============================================================
REM 回滚
REM ============================================================
:run_rollback
if exist "%SCRIPT_DIR%\script\windows\upgrade-rollback.bat" (
    call "%SCRIPT_DIR%\script\windows\upgrade-rollback.bat" rollback "%APP_NAME%" "%APP_HOME%" "%JAR_NAME%" "" ""
) else (
    echo [提示] 升级脚本不存在
)
goto :eof

REM ============================================================
REM 还原点状态
REM ============================================================
:run_rollback_status
if exist "%SCRIPT_DIR%\script\windows\upgrade-rollback.bat" (
    call "%SCRIPT_DIR%\script\windows\upgrade-rollback.bat" status "%APP_NAME%" "%APP_HOME%" "%JAR_NAME%" "" ""
) else (
    echo [提示] 升级脚本不存在
)
goto :eof

REM ============================================================
REM Arthas 诊断
REM ============================================================
:start_arthas
call :get_pid
if exist "%SCRIPT_DIR%\script\windows\arthas-install.bat" (
    call "%SCRIPT_DIR%\script\windows\arthas-install.bat" "%APP_PID%" "%APP_HOME%"
) else (
    echo [提示] Arthas 脚本不存在
)
goto :eof

REM ============================================================
REM Skywalking 管理
REM ============================================================
:run_skywalking
if not defined SW_COMMAND set "SW_COMMAND=help"
if exist "%SCRIPT_DIR%\script\windows\skywalking.bat" (
    call "%SCRIPT_DIR%\script\windows\skywalking.bat" "%SW_COMMAND%" "%APP_HOME%" "%APP_NAME%" "%SW_BACKEND%" "%SW_SERVICE_NAME%"
) else (
    echo [提示] Skywalking 脚本不存在
    echo 请检查: %SCRIPT_DIR%\script\windows\skywalking.bat
)
goto :eof

endlocal
