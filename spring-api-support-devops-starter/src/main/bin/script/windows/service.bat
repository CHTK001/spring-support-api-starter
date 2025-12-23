@echo off
chcp 65001 >nul 2>&1
setlocal enabledelayedexpansion

REM ============================================================
REM Windows 服务管理脚本
REM 使用 sc.exe 或 nssm 管理服务
REM ============================================================

set "ACTION=%~1"
set "APP_NAME=%~2"
set "APP_HOME=%~3"
set "JAVA_CMD=%~4"
set "JAR_NAME=%~5"
set "JAVA_OPTS=%~6"

if "%ACTION%"=="" (
    echo [错误] 请提供操作: install, uninstall, status
    exit /b 1
)

set "SERVICE_NAME=%APP_NAME%"

REM 检查是否以管理员身份运行
net session >nul 2>&1
if errorlevel 1 (
    if /i not "%ACTION%"=="status" (
        echo [错误] 需要以管理员身份运行此命令
        echo 请右键点击命令提示符，选择"以管理员身份运行"
        exit /b 1
    )
)

if /i "%ACTION%"=="install" goto :install_service
if /i "%ACTION%"=="uninstall" goto :uninstall_service
if /i "%ACTION%"=="status" goto :show_status
echo [错误] 未知操作: %ACTION%
exit /b 1

REM ============================================================
REM 安装服务
REM ============================================================
:install_service
echo.
echo ════════════════════════════════════════════════════════════════
echo               安装 Windows 服务
echo ════════════════════════════════════════════════════════════════
echo.
echo   服务名称: %SERVICE_NAME%
echo   应用目录: %APP_HOME%
echo   JAR 文件: %JAR_NAME%
echo.

REM 检查服务是否已存在
sc query "%SERVICE_NAME%" >nul 2>&1
if not errorlevel 1 (
    echo [警告] 服务已存在: %SERVICE_NAME%
    set /p "CONFIRM=是否覆盖? (y/N): "
    if /i not "!CONFIRM!"=="y" (
        echo 已取消
        exit /b 0
    )
    call :uninstall_service
)

REM 检查是否安装了 nssm
where nssm >nul 2>&1
if not errorlevel 1 (
    echo 使用 NSSM 安装服务...
    call :install_with_nssm
) else (
    echo 使用 sc.exe 安装服务...
    echo [提示] 推荐安装 NSSM 以获得更好的服务管理体验
    echo        下载地址: https://nssm.cc/download
    call :install_with_sc
)
goto :eof

:install_with_nssm
REM 使用 NSSM 安装服务 (推荐)
nssm install "%SERVICE_NAME%" "%JAVA_CMD%" %JAVA_OPTS% -jar "%APP_HOME%\%JAR_NAME%" --spring.profiles.active=prod
nssm set "%SERVICE_NAME%" AppDirectory "%APP_HOME%"
nssm set "%SERVICE_NAME%" DisplayName "%APP_NAME% Service"
nssm set "%SERVICE_NAME%" Description "Spring Boot Application - %APP_NAME%"
nssm set "%SERVICE_NAME%" Start SERVICE_AUTO_START
nssm set "%SERVICE_NAME%" AppStdout "%APP_HOME%\logs\service-stdout.log"
nssm set "%SERVICE_NAME%" AppStderr "%APP_HOME%\logs\service-stderr.log"
nssm set "%SERVICE_NAME%" AppRotateFiles 1
nssm set "%SERVICE_NAME%" AppRotateBytes 10485760

if not errorlevel 1 (
    echo.
    echo [成功] 服务已安装: %SERVICE_NAME%
    echo.
    echo 可用命令:
    echo   nssm start %SERVICE_NAME%      # 启动服务
    echo   nssm stop %SERVICE_NAME%       # 停止服务
    echo   nssm restart %SERVICE_NAME%    # 重启服务
    echo   nssm status %SERVICE_NAME%     # 查看状态
    echo   nssm edit %SERVICE_NAME%       # 编辑配置
    echo   sc query %SERVICE_NAME%        # 查看服务详情
) else (
    echo [错误] 服务安装失败
)
goto :eof

:install_with_sc
REM 使用 sc.exe 安装服务 (基础方式)
REM 注意: sc.exe 不能直接运行 JAR，需要包装器

REM 创建启动脚本
set "START_SCRIPT=%APP_HOME%\bin\service-start.bat"
echo @echo off > "%START_SCRIPT%"
echo cd /d "%APP_HOME%" >> "%START_SCRIPT%"
echo "%JAVA_CMD%" %JAVA_OPTS% -jar "%APP_HOME%\%JAR_NAME%" --spring.profiles.active=prod >> "%START_SCRIPT%"

REM 使用 sc.exe 创建服务
sc create "%SERVICE_NAME%" binPath= "cmd.exe /c \"%START_SCRIPT%\"" start= auto DisplayName= "%APP_NAME% Service"
sc description "%SERVICE_NAME%" "Spring Boot Application - %APP_NAME%"

if not errorlevel 1 (
    echo.
    echo [成功] 服务已安装: %SERVICE_NAME%
    echo.
    echo [警告] 使用 sc.exe 创建的服务功能有限
    echo        推荐安装 NSSM 以获得更好的服务管理
    echo.
    echo 可用命令:
    echo   sc start %SERVICE_NAME%        # 启动服务
    echo   sc stop %SERVICE_NAME%         # 停止服务
    echo   sc query %SERVICE_NAME%        # 查看状态
    echo   net start %SERVICE_NAME%       # 启动服务
    echo   net stop %SERVICE_NAME%        # 停止服务
) else (
    echo [错误] 服务安装失败
)
goto :eof

REM ============================================================
REM 卸载服务
REM ============================================================
:uninstall_service
echo.
echo 卸载服务: %SERVICE_NAME%...

REM 先停止服务
sc stop "%SERVICE_NAME%" >nul 2>&1
timeout /t 2 /nobreak >nul

REM 检查是否使用 nssm
where nssm >nul 2>&1
if not errorlevel 1 (
    nssm remove "%SERVICE_NAME%" confirm
) else (
    sc delete "%SERVICE_NAME%"
)

if not errorlevel 1 (
    echo [成功] 服务已卸载
) else (
    echo [警告] 服务可能不存在或卸载失败
)
goto :eof

REM ============================================================
REM 查看服务状态
REM ============================================================
:show_status
echo.
echo ════════════════════════════════════════════════════════════════
echo               Windows 服务状态
echo ════════════════════════════════════════════════════════════════
echo.

sc query "%SERVICE_NAME%" >nul 2>&1
if errorlevel 1 (
    echo [警告] 服务未安装: %SERVICE_NAME%
    echo 使用 'startup.bat install-service' 安装服务
    exit /b 0
)

sc query "%SERVICE_NAME%"

REM 检查是否使用 nssm
where nssm >nul 2>&1
if not errorlevel 1 (
    echo.
    echo 【NSSM 状态】
    nssm status "%SERVICE_NAME%" 2>nul
)
goto :eof

endlocal
