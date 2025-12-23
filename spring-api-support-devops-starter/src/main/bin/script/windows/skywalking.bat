@echo off
chcp 65001 >nul 2>&1
setlocal enabledelayedexpansion

REM ============================================================
REM Skywalking Agent 管理脚本 (Windows)
REM 支持安装、检查、清理、配置 Skywalking Agent
REM ============================================================

set "ACTION=%~1"
set "APP_HOME=%~2"
set "APP_NAME=%~3"

if "%ACTION%"=="" set "ACTION=info"

REM Skywalking 配置
set "SKYWALKING_VERSION=9.0.0"
set "SKYWALKING_DIR=%APP_HOME%\skywalking"
set "SKYWALKING_AGENT_DIR=%SKYWALKING_DIR%\skywalking-agent"
set "SKYWALKING_AGENT_JAR=%SKYWALKING_AGENT_DIR%\skywalking-agent.jar"
set "SKYWALKING_CONFIG=%SKYWALKING_AGENT_DIR%\config\agent.config"

REM 下载地址
set "DOWNLOAD_BASE=https://archive.apache.org/dist/skywalking/java-agent"
set "DOWNLOAD_URL=%DOWNLOAD_BASE%/%SKYWALKING_VERSION%/apache-skywalking-java-agent-%SKYWALKING_VERSION%.tgz"
set "MIRROR_URL=https://mirrors.aliyun.com/apache/skywalking/java-agent/%SKYWALKING_VERSION%/apache-skywalking-java-agent-%SKYWALKING_VERSION%.tgz"

if /i "%ACTION%"=="install" goto :install_agent
if /i "%ACTION%"=="check" goto :check_agent
if /i "%ACTION%"=="clean" goto :clean_agent
if /i "%ACTION%"=="info" goto :show_info
if /i "%ACTION%"=="config" goto :show_config
if /i "%ACTION%"=="set" goto :set_config
echo [错误] 未知操作: %ACTION%
echo 支持的操作: install, check, clean, info, config, set
exit /b 1

REM ============================================================
REM 安装 Agent
REM ============================================================
:install_agent
echo.
echo ╔══════════════════════════════════════════════════════════════╗
echo ║              安装 Skywalking Agent                           ║
echo ╚══════════════════════════════════════════════════════════════╝
echo.
echo   版本:     %SKYWALKING_VERSION%
echo   安装目录: %SKYWALKING_DIR%
echo.

if exist "%SKYWALKING_AGENT_JAR%" (
    echo [警告] Skywalking Agent 已安装
    set /p "CONFIRM=是否重新安装? (y/N): "
    if /i not "!CONFIRM!"=="y" (
        echo 已取消
        goto :eof
    )
    echo 清理旧版本...
    rd /s /q "%SKYWALKING_DIR%" 2>nul
)

if not exist "%SKYWALKING_DIR%" mkdir "%SKYWALKING_DIR%"

REM 下载 Agent
echo [步骤 1] 下载 Skywalking Agent...
set "DOWNLOAD_FILE=%SKYWALKING_DIR%\skywalking-agent.tgz"

REM 尝试使用 curl
where curl >nul 2>&1
if not errorlevel 1 (
    echo   尝试从 Apache 官方下载...
    curl -L -o "%DOWNLOAD_FILE%" "%DOWNLOAD_URL%" --progress-bar 2>nul
    if exist "%DOWNLOAD_FILE%" (
        for %%f in ("%DOWNLOAD_FILE%") do if %%~zf GTR 1000000 goto :download_success
    )
    echo   尝试从阿里云镜像下载...
    curl -L -o "%DOWNLOAD_FILE%" "%MIRROR_URL%" --progress-bar 2>nul
    if exist "%DOWNLOAD_FILE%" (
        for %%f in ("%DOWNLOAD_FILE%") do if %%~zf GTR 1000000 goto :download_success
    )
)

REM 尝试使用 PowerShell
echo   使用 PowerShell 下载...
powershell -Command ^
    "$ProgressPreference = 'SilentlyContinue'; " ^
    "try { " ^
    "    Invoke-WebRequest -Uri '%DOWNLOAD_URL%' -OutFile '%DOWNLOAD_FILE%' -UseBasicParsing; " ^
    "} catch { " ^
    "    try { " ^
    "        Invoke-WebRequest -Uri '%MIRROR_URL%' -OutFile '%DOWNLOAD_FILE%' -UseBasicParsing; " ^
    "    } catch { " ^
    "        Write-Host '下载失败'; " ^
    "        exit 1; " ^
    "    } " ^
    "}"

if not exist "%DOWNLOAD_FILE%" (
    echo [错误] 下载失败
    echo        请手动下载: %DOWNLOAD_URL%
    echo        保存到: %DOWNLOAD_FILE%
    exit /b 1
)

:download_success
for %%f in ("%DOWNLOAD_FILE%") do set "FILE_SIZE=%%~zf"
set /a "FILE_SIZE_MB=%FILE_SIZE% / 1048576"
echo   √ 下载完成 (%FILE_SIZE_MB% MB)

REM 解压
echo.
echo [步骤 2] 解压 Agent...

REM 尝试使用 tar (Windows 10+)
where tar >nul 2>&1
if not errorlevel 1 (
    tar -xzf "%DOWNLOAD_FILE%" -C "%SKYWALKING_DIR%" 2>nul
    if exist "%SKYWALKING_AGENT_JAR%" goto :extract_success
)

REM 尝试使用 7z
where 7z >nul 2>&1
if not errorlevel 1 (
    7z x "%DOWNLOAD_FILE%" -so 2>nul | 7z x -aoa -si -ttar -o"%SKYWALKING_DIR%" 2>nul
    if exist "%SKYWALKING_AGENT_JAR%" goto :extract_success
)

REM 使用 PowerShell 解压 (需要先解压 .gz 再解压 .tar)
echo   使用 PowerShell 解压...
powershell -Command ^
    "$gzFile = '%DOWNLOAD_FILE%'; " ^
    "$tarFile = '%SKYWALKING_DIR%\agent.tar'; " ^
    "$inStream = [System.IO.File]::OpenRead($gzFile); " ^
    "$gzStream = New-Object System.IO.Compression.GZipStream($inStream, [System.IO.Compression.CompressionMode]::Decompress); " ^
    "$outStream = [System.IO.File]::Create($tarFile); " ^
    "$gzStream.CopyTo($outStream); " ^
    "$outStream.Close(); $gzStream.Close(); $inStream.Close(); " ^
    "tar -xf $tarFile -C '%SKYWALKING_DIR%'; " ^
    "Remove-Item $tarFile -Force;"

if not exist "%SKYWALKING_AGENT_JAR%" (
    echo [错误] 解压失败，请安装 7-Zip
    echo        winget install 7zip.7zip
    del "%DOWNLOAD_FILE%" 2>nul
    exit /b 1
)

:extract_success
echo   √ 解压完成

REM 清理下载文件
del "%DOWNLOAD_FILE%" 2>nul

REM 配置 Agent
echo.
echo [步骤 3] 配置 Agent...
if exist "%SKYWALKING_CONFIG%" (
    REM 设置默认服务名
    powershell -Command ^
        "(Get-Content '%SKYWALKING_CONFIG%') -replace 'agent.service_name=\${SW_AGENT_NAME:Your_ApplicationName}', 'agent.service_name=${SW_AGENT_NAME:%APP_NAME%}' | Set-Content '%SKYWALKING_CONFIG%'"
    echo   √ 已设置默认服务名: %APP_NAME%
)

echo.
echo ╔══════════════════════════════════════════════════════════════╗
echo ║              √ Skywalking Agent 安装完成                     ║
echo ╚══════════════════════════════════════════════════════════════╝
echo.
echo   Agent JAR:  %SKYWALKING_AGENT_JAR%
echo   配置文件:   %SKYWALKING_CONFIG%
echo.
echo 使用方式:
echo   startup.bat start --skywalking
echo   startup.bat start --skywalking --sw-backend 192.168.1.100:11800
echo   startup.bat start --skywalking --sw-name my-service
echo.
goto :eof

REM ============================================================
REM 检查 Agent
REM ============================================================
:check_agent
echo.
echo ════════════════════════════════════════════════════════════════
echo                 检查 Skywalking Agent
echo ════════════════════════════════════════════════════════════════
echo.

if exist "%SKYWALKING_AGENT_JAR%" (
    echo   状态:       已安装 √
    for %%f in ("%SKYWALKING_AGENT_JAR%") do (
        echo   Agent JAR:  %%~dpnxf
        set /a "JAR_SIZE=%%~zf / 1024"
        echo   文件大小:   !JAR_SIZE! KB
    )
    
    REM 检查版本
    if exist "%SKYWALKING_AGENT_DIR%\LICENSE" (
        for /f "tokens=*" %%a in ('findstr /i "version" "%SKYWALKING_AGENT_DIR%\LICENSE" 2^>nul ^| findstr /r "[0-9]\.[0-9]"') do (
            echo   版本信息:   检测到 Skywalking Agent
        )
    )
    
    echo   配置文件:   %SKYWALKING_CONFIG%
    if exist "%SKYWALKING_CONFIG%" (
        echo   配置状态:   存在 √
    ) else (
        echo   配置状态:   缺失 ×
    )
    
    REM 检查插件
    set "PLUGIN_COUNT=0"
    for %%f in ("%SKYWALKING_AGENT_DIR%\plugins\*.jar") do set /a "PLUGIN_COUNT+=1"
    echo   插件数量:   !PLUGIN_COUNT! 个
    
) else (
    echo   状态:       未安装 ×
    echo.
    echo   使用以下命令安装:
    echo     startup.bat skywalking install
)
echo.
goto :eof

REM ============================================================
REM 清理 Agent
REM ============================================================
:clean_agent
echo.
echo 清理 Skywalking Agent...

if not exist "%SKYWALKING_DIR%" (
    echo [提示] Skywalking 目录不存在
    goto :eof
)

set /p "CONFIRM=确认删除 Skywalking Agent? (y/N): "
if /i not "%CONFIRM%"=="y" (
    echo 已取消
    goto :eof
)

rd /s /q "%SKYWALKING_DIR%" 2>nul
if not exist "%SKYWALKING_DIR%" (
    echo [成功] Skywalking Agent 已清理
) else (
    echo [错误] 清理失败，请手动删除: %SKYWALKING_DIR%
)
goto :eof

REM ============================================================
REM 显示信息
REM ============================================================
:show_info
echo.
echo ╔══════════════════════════════════════════════════════════════╗
echo ║                  Skywalking Agent 信息                       ║
echo ╚══════════════════════════════════════════════════════════════╝
echo.
echo   配置版本:   %SKYWALKING_VERSION%
echo   安装目录:   %SKYWALKING_DIR%
echo   Agent JAR:  %SKYWALKING_AGENT_JAR%
echo.

if exist "%SKYWALKING_AGENT_JAR%" (
    echo   安装状态:   已安装 √
    echo.
    echo 可用命令:
    echo   startup.bat skywalking check     检查 Agent 状态
    echo   startup.bat skywalking config    查看配置
    echo   startup.bat skywalking clean     清理 Agent
    echo.
    echo 启动方式:
    echo   startup.bat start --skywalking
    echo   startup.bat start --skywalking --sw-backend ^<oap-address^>:11800
    echo   startup.bat start --skywalking --sw-name ^<service-name^>
) else (
    echo   安装状态:   未安装 ×
    echo.
    echo 安装命令:
    echo   startup.bat skywalking install
)
echo.
goto :eof

REM ============================================================
REM 显示配置
REM ============================================================
:show_config
echo.
echo ════════════════════════════════════════════════════════════════
echo                 Skywalking Agent 配置
echo ════════════════════════════════════════════════════════════════
echo.

if not exist "%SKYWALKING_CONFIG%" (
    echo [错误] 配置文件不存在: %SKYWALKING_CONFIG%
    echo        请先安装 Agent: startup.bat skywalking install
    goto :eof
)

echo 配置文件: %SKYWALKING_CONFIG%
echo.
echo 关键配置项:
echo ────────────────────────────────────────────────────────────────

REM 显示关键配置
for %%k in (
    "agent.service_name"
    "agent.namespace"
    "collector.backend_service"
    "agent.sample_n_per_3_secs"
    "agent.authentication"
    "logging.level"
) do (
    for /f "tokens=1,* delims==" %%a in ('findstr /b "%%~k" "%SKYWALKING_CONFIG%" 2^>nul') do (
        echo   %%a = %%b
    )
)
echo ────────────────────────────────────────────────────────────────
echo.
echo 完整配置请查看: %SKYWALKING_CONFIG%
goto :eof

REM ============================================================
REM 设置配置
REM ============================================================
:set_config
set "CONFIG_KEY=%~4"
set "CONFIG_VALUE=%~5"

if "%CONFIG_KEY%"=="" (
    echo [错误] 请提供配置项和值
    echo 用法: startup.bat skywalking set ^<key^> ^<value^>
    echo 示例: startup.bat skywalking set agent.service_name my-service
    goto :eof
)

if not exist "%SKYWALKING_CONFIG%" (
    echo [错误] 配置文件不存在，请先安装 Agent
    goto :eof
)

echo 设置配置: %CONFIG_KEY% = %CONFIG_VALUE%
powershell -Command ^
    "$content = Get-Content '%SKYWALKING_CONFIG%' -Raw; " ^
    "$pattern = '%CONFIG_KEY%=.*'; " ^
    "$replacement = '%CONFIG_KEY%=%CONFIG_VALUE%'; " ^
    "$newContent = $content -replace $pattern, $replacement; " ^
    "Set-Content '%SKYWALKING_CONFIG%' $newContent;"

echo [成功] 配置已更新
goto :eof

endlocal
