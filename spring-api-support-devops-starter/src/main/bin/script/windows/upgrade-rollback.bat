@echo off
chcp 65001 >nul 2>&1
setlocal enabledelayedexpansion

REM ============================================================
REM 升级回滚脚本 (Windows)
REM 支持 zip/tar.gz 格式升级包
REM ============================================================

set "ACTION=%~1"
set "APP_NAME=%~2"
set "APP_HOME=%~3"
set "JAR_NAME=%~4"
set "PACKAGE_PATH=%~5"
set "NO_RESTART=%~6"

if "%ACTION%"=="" (
    echo [错误] 请提供操作: upgrade, rollback, status
    exit /b 1
)

set "BACKUP_DIR=%APP_HOME%\backup"
set "ROLLBACK_DIR=%BACKUP_DIR%\rollback"
set "CONFIG_DIR=%APP_HOME%\config"

if not exist "%BACKUP_DIR%" mkdir "%BACKUP_DIR%"
if not exist "%ROLLBACK_DIR%" mkdir "%ROLLBACK_DIR%"

if /i "%ACTION%"=="upgrade" goto :do_upgrade
if /i "%ACTION%"=="rollback" goto :do_rollback
if /i "%ACTION%"=="status" goto :show_status
echo [错误] 未知操作: %ACTION%
exit /b 1

REM ============================================================
REM 升级
REM ============================================================
:do_upgrade
echo.
echo ╔══════════════════════════════════════════════════════════════╗
echo ║                      应用升级                                ║
echo ╚══════════════════════════════════════════════════════════════╝
echo.

if "%PACKAGE_PATH%"=="" (
    echo [错误] 请使用 --package 指定升级包路径
    exit /b 1
)

if not exist "%PACKAGE_PATH%" (
    echo [错误] 升级包不存在: %PACKAGE_PATH%
    exit /b 1
)

echo   升级包:   %PACKAGE_PATH%
echo   应用目录: %APP_HOME%
echo.

REM 创建还原点
echo [步骤 1] 创建还原点...
for /f "tokens=1-3 delims=/" %%a in ("%date%") do set "DATE_STR=%%a%%b%%c"
for /f "tokens=1-2 delims=:." %%a in ("%time: =0%") do set "TIME_STR=%%a%%b"
set "TIMESTAMP=%DATE_STR%_%TIME_STR%"
set "RESTORE_POINT=%ROLLBACK_DIR%\%TIMESTAMP%"
mkdir "%RESTORE_POINT%"

REM 备份当前 JAR
if exist "%APP_HOME%\%JAR_NAME%" (
    copy "%APP_HOME%\%JAR_NAME%" "%RESTORE_POINT%\" >nul 2>&1
    echo   √ JAR 已备份
)

REM 备份当前配置
if exist "%CONFIG_DIR%" (
    xcopy "%CONFIG_DIR%" "%RESTORE_POINT%\config\" /E /I /Q /Y >nul 2>&1
    echo   √ 配置已备份
)

REM 记录还原点信息
echo %TIMESTAMP%> "%RESTORE_POINT%\restore_point.info"
echo %JAR_NAME%>> "%RESTORE_POINT%\restore_point.info"
echo   √ 还原点已创建: %RESTORE_POINT%

REM 解压升级包
echo.
echo [步骤 2] 解压升级包...
set "TEMP_EXTRACT=%TEMP%\upgrade_%TIMESTAMP%"
mkdir "%TEMP_EXTRACT%"

REM 判断文件类型并解压
echo %PACKAGE_PATH% | findstr /i ".zip" >nul
if not errorlevel 1 (
    echo   使用 PowerShell 解压 ZIP...
    powershell -Command "Expand-Archive -Path '%PACKAGE_PATH%' -DestinationPath '%TEMP_EXTRACT%' -Force"
    goto :extract_done
)

echo %PACKAGE_PATH% | findstr /i ".tar.gz .tgz" >nul
if not errorlevel 1 (
    REM 尝试使用 tar (Windows 10+)
    where tar >nul 2>&1
    if not errorlevel 1 (
        echo   使用 tar 解压...
        tar -xzf "%PACKAGE_PATH%" -C "%TEMP_EXTRACT%"
        goto :extract_done
    )
    REM 尝试使用 7z
    where 7z >nul 2>&1
    if not errorlevel 1 (
        echo   使用 7-Zip 解压...
        7z x "%PACKAGE_PATH%" -so | 7z x -aoa -si -ttar -o"%TEMP_EXTRACT%"
        goto :extract_done
    )
    echo [错误] 无法解压 tar.gz 文件，请安装 7-Zip
    echo        winget install 7zip.7zip
    rd /s /q "%TEMP_EXTRACT%" 2>nul
    exit /b 1
)

echo [错误] 不支持的文件格式，仅支持 .zip 和 .tar.gz
rd /s /q "%TEMP_EXTRACT%" 2>nul
exit /b 1

:extract_done
echo   √ 解压完成

REM 查找并复制新文件
echo.
echo [步骤 3] 部署新版本...

REM 查找 JAR 文件
for /r "%TEMP_EXTRACT%" %%f in (*.jar) do (
    echo   发现 JAR: %%~nxf
    copy "%%f" "%APP_HOME%\%JAR_NAME%" >nul 2>&1
    echo   √ JAR 已更新
)

REM 查找配置文件
if exist "%TEMP_EXTRACT%\config" (
    xcopy "%TEMP_EXTRACT%\config" "%CONFIG_DIR%\" /E /I /Q /Y >nul 2>&1
    echo   √ 配置已更新
)

REM 清理临时文件
rd /s /q "%TEMP_EXTRACT%" 2>nul

echo.
echo [完成] 升级完成

if /i not "%NO_RESTART%"=="true" (
    echo.
    echo [提示] 请重启应用使更改生效
    echo        startup.bat restart
)

goto :eof

REM ============================================================
REM 回滚
REM ============================================================
:do_rollback
echo.
echo ╔══════════════════════════════════════════════════════════════╗
echo ║                      应用回滚                                ║
echo ╚══════════════════════════════════════════════════════════════╝
echo.

REM 查找最新还原点
set "LATEST="
for /d %%d in ("%ROLLBACK_DIR%\*") do (
    set "LATEST=%%d"
)

if not defined LATEST (
    echo [错误] 没有可用的还原点
    exit /b 1
)

echo   还原点: %LATEST%
echo.
set /p "CONFIRM=确认回滚到此还原点? (y/N): "
if /i not "%CONFIRM%"=="y" (
    echo 已取消
    goto :eof
)

echo.
echo [步骤 1] 还原 JAR 文件...
if exist "%LATEST%\*.jar" (
    for %%f in ("%LATEST%\*.jar") do (
        copy "%%f" "%APP_HOME%\%JAR_NAME%" >nul 2>&1
    )
    echo   √ JAR 已还原
) else (
    echo   - 无 JAR 备份
)

echo [步骤 2] 还原配置文件...
if exist "%LATEST%\config" (
    xcopy "%LATEST%\config" "%CONFIG_DIR%\" /E /I /Q /Y >nul 2>&1
    echo   √ 配置已还原
) else (
    echo   - 无配置备份
)

echo.
echo [完成] 回滚完成
echo [提示] 请重启应用使更改生效

goto :eof

REM ============================================================
REM 查看还原点状态
REM ============================================================
:show_status
echo.
echo ════════════════════════════════════════════════════════════════
echo                     还原点状态
echo ════════════════════════════════════════════════════════════════
echo.
echo   还原点目录: %ROLLBACK_DIR%
echo.

set "COUNT=0"
for /d %%d in ("%ROLLBACK_DIR%\*") do (
    set /a "COUNT+=1"
    echo   !COUNT!. %%~nxd
    if exist "%%d\*.jar" (
        for %%f in ("%%d\*.jar") do echo       JAR: %%~nxf
    )
    if exist "%%d\config" (
        echo       配置: 已备份
    )
)

if %COUNT%==0 (
    echo   无还原点
) else (
    echo.
    echo   共 %COUNT% 个还原点
)

endlocal
