@echo off
REM ============================================================
REM Docker 管理脚本 (Windows)
REM 作者: CH
REM 版本: 1.0.0
REM ============================================================
setlocal enabledelayedexpansion

set SCRIPT_DIR=%~dp0
cd /d %SCRIPT_DIR%..\..
set APP_HOME=%cd%

REM 默认配置
set APP_NAME=@project.artifactId@
set APP_VERSION=@project.version@
set DOCKER_PORT=8080
set HOST_PORT=8080
set IMAGE_TAG=%APP_VERSION%

REM 解析参数
:parse_args
if "%~1"=="" goto run_command
if /i "%~1"=="build" set COMMAND=build& shift& goto parse_args
if /i "%~1"=="deploy" set COMMAND=deploy& shift& goto parse_args
if /i "%~1"=="start" set COMMAND=start& shift& goto parse_args
if /i "%~1"=="stop" set COMMAND=stop& shift& goto parse_args
if /i "%~1"=="restart" set COMMAND=restart& shift& goto parse_args
if /i "%~1"=="status" set COMMAND=status& shift& goto parse_args
if /i "%~1"=="logs" set COMMAND=logs& shift& goto parse_args
if /i "%~1"=="clean" set COMMAND=clean& shift& goto parse_args
if /i "%~1"=="help" set COMMAND=help& shift& goto parse_args
if /i "%~1"=="--name" set APP_NAME=%~2& shift& shift& goto parse_args
if /i "%~1"=="--version" set APP_VERSION=%~2& shift& shift& goto parse_args
if /i "%~1"=="--port" set DOCKER_PORT=%~2& shift& shift& goto parse_args
if /i "%~1"=="--host-port" set HOST_PORT=%~2& shift& shift& goto parse_args
if /i "%~1"=="--tag" set IMAGE_TAG=%~2& shift& shift& goto parse_args
if /i "%~1"=="--lines" set LOG_LINES=%~2& shift& shift& goto parse_args
shift
goto parse_args

:run_command
if "%COMMAND%"=="" set COMMAND=help
goto %COMMAND%

:help
echo ============================================================
echo   Docker 管理脚本
echo ============================================================
echo.
echo 用法: %~nx0 [命令] [选项]
echo.
echo 命令:
echo   build      构建 Docker 镜像
echo   deploy     构建并部署 (一键发布)
echo   start      启动容器
echo   stop       停止容器
echo   restart    重启容器
echo   status     查看容器状态
echo   logs       查看容器日志
echo   clean      清理旧镜像
echo   help       显示帮助
echo.
echo 选项:
echo   --name ^<name^>      应用名称
echo   --version ^<ver^>    应用版本
echo   --port ^<port^>      容器端口 (默认: 8080)
echo   --host-port ^<port^> 主机端口 (默认: 8080)
echo   --tag ^<tag^>        镜像标签
echo   --lines ^<n^>        日志行数 (默认: 100)
echo.
goto end

:build
echo ============================================================
echo   构建 Docker 镜像
echo ============================================================
echo   应用: %APP_NAME%
echo   版本: %APP_VERSION%
echo   标签: %IMAGE_TAG%
echo ============================================================

REM 检查 Docker
where docker >nul 2>nul
if %errorlevel% neq 0 (
    echo [错误] 未找到 Docker
    goto end
)

REM 检查 Dockerfile
if not exist "%APP_HOME%\Dockerfile" (
    echo [创建] 生成 Dockerfile...
    (
        echo FROM eclipse-temurin:21-jre
        echo LABEL maintainer="CH"
        echo WORKDIR /app
        echo COPY %APP_NAME%-%APP_VERSION%.jar app.jar
        echo COPY config/ config/
        echo EXPOSE %DOCKER_PORT%
        echo ENTRYPOINT ["java", "-jar", "app.jar", "--spring.config.location=classpath:/,file:config/"]
    ) > "%APP_HOME%\Dockerfile"
)

docker build -t %APP_NAME%:%IMAGE_TAG% "%APP_HOME%"
if %errorlevel% equ 0 (
    echo [成功] 镜像构建完成: %APP_NAME%:%IMAGE_TAG%
) else (
    echo [失败] 镜像构建失败
)
goto end

:deploy
call :build
if %errorlevel% neq 0 goto end
call :stop
call :start
goto end

:start
echo [启动] 容器 %APP_NAME%...
docker run -d --name %APP_NAME% -p %HOST_PORT%:%DOCKER_PORT% %APP_NAME%:%IMAGE_TAG%
if %errorlevel% equ 0 (
    echo [成功] 容器已启动
    echo   访问: http://localhost:%HOST_PORT%
) else (
    echo [失败] 启动失败
)
goto end

:stop
echo [停止] 容器 %APP_NAME%...
docker stop %APP_NAME% 2>nul
docker rm %APP_NAME% 2>nul
echo [完成] 容器已停止
goto end

:restart
call :stop
call :start
goto end

:status
echo ============================================================
echo   容器状态
echo ============================================================
docker ps -a --filter "name=%APP_NAME%" --format "table {{.ID}}\t{{.Image}}\t{{.Status}}\t{{.Ports}}"
goto end

:logs
if "%LOG_LINES%"=="" set LOG_LINES=100
echo [日志] 最后 %LOG_LINES% 行
docker logs --tail %LOG_LINES% %APP_NAME%
goto end

:clean
echo [清理] 旧镜像...
for /f "tokens=3" %%i in ('docker images %APP_NAME% --format "{{.ID}} {{.Tag}}" ^| find /v "%IMAGE_TAG%"') do (
    docker rmi %%i 2>nul
)
echo [完成] 清理完成
goto end

:end
endlocal
