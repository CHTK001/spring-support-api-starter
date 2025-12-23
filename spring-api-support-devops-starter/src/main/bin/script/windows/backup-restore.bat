@echo off
chcp 65001 >nul 2>&1
setlocal enabledelayedexpansion

REM ============================================================
REM 备份还原脚本 (Windows)
REM ============================================================

set "ACTION=%~1"
set "MODE=%~2"
set "APP_NAME=%~3"
set "JAR_NAME=%~4"
set "APP_HOME=%~5"

if "%ACTION%"=="" (
    echo [错误] 请提供操作: backup, restore
    exit /b 1
)
if "%MODE%"=="" set "MODE=all"

set "BACKUP_DIR=%APP_HOME%\backup"
set "CONFIG_DIR=%APP_HOME%\config"

if not exist "%BACKUP_DIR%" mkdir "%BACKUP_DIR%"

REM 生成时间戳
for /f "tokens=1-3 delims=/" %%a in ("%date%") do set "DATE_STR=%%a%%b%%c"
for /f "tokens=1-2 delims=:." %%a in ("%time: =0%") do set "TIME_STR=%%a%%b"
set "TIMESTAMP=%DATE_STR%_%TIME_STR%"

if /i "%ACTION%"=="backup" goto :do_backup
if /i "%ACTION%"=="restore" goto :do_restore
echo [错误] 未知操作: %ACTION%
exit /b 1

REM ============================================================
REM 备份
REM ============================================================
:do_backup
echo.
echo ════════════════════════════════════════════════════════════════
echo                     备份 - %MODE%
echo ════════════════════════════════════════════════════════════════
echo.
echo   应用名称: %APP_NAME%
echo   备份目录: %BACKUP_DIR%
echo   备份模式: %MODE%
echo.

if /i "%MODE%"=="config" goto :backup_config
if /i "%MODE%"=="jar" goto :backup_jar
if /i "%MODE%"=="all" (
    call :backup_config
    call :backup_jar
    goto :backup_done
)
echo [错误] 未知备份模式: %MODE%
exit /b 1

:backup_config
set "CONFIG_BACKUP=%BACKUP_DIR%\config_%TIMESTAMP%"
echo [备份] 配置文件...
if exist "%CONFIG_DIR%" (
    xcopy "%CONFIG_DIR%" "%CONFIG_BACKUP%\" /E /I /Q /Y >nul 2>&1
    if not errorlevel 1 (
        echo   √ 配置已备份到: %CONFIG_BACKUP%
    ) else (
        echo   × 配置备份失败
    )
) else (
    echo   - 配置目录不存在，跳过
)
goto :eof

:backup_jar
set "JAR_BACKUP=%BACKUP_DIR%\%JAR_NAME%.%TIMESTAMP%"
echo [备份] JAR 文件...
if exist "%APP_HOME%\%JAR_NAME%" (
    copy "%APP_HOME%\%JAR_NAME%" "%JAR_BACKUP%" >nul 2>&1
    if not errorlevel 1 (
        for %%f in ("%JAR_BACKUP%") do set "JAR_SIZE=%%~zf"
        set /a "JAR_SIZE_MB=!JAR_SIZE! / 1048576"
        echo   √ JAR 已备份到: %JAR_BACKUP% (!JAR_SIZE_MB! MB)
    ) else (
        echo   × JAR 备份失败
    )
) else (
    echo   - JAR 文件不存在，跳过
)
goto :eof

:backup_done
echo.
echo [完成] 备份完成
echo.
echo 备份文件列表:
dir /b "%BACKUP_DIR%\*%TIMESTAMP%*" 2>nul
goto :eof

REM ============================================================
REM 还原
REM ============================================================
:do_restore
echo.
echo ════════════════════════════════════════════════════════════════
echo                     还原 - %MODE%
echo ════════════════════════════════════════════════════════════════
echo.

REM 列出可用备份
echo 可用备份:
echo.

set "IDX=0"
if /i "%MODE%"=="config" (
    for /d %%d in ("%BACKUP_DIR%\config_*") do (
        set /a "IDX+=1"
        set "BACKUP_!IDX!=%%d"
        echo   !IDX!. %%~nxd
    )
) else if /i "%MODE%"=="jar" (
    for %%f in ("%BACKUP_DIR%\%APP_NAME%*.jar.*") do (
        set /a "IDX+=1"
        set "BACKUP_!IDX!=%%f"
        echo   !IDX!. %%~nxf
    )
) else (
    echo [配置备份]
    for /d %%d in ("%BACKUP_DIR%\config_*") do (
        set /a "IDX+=1"
        set "BACKUP_!IDX!=%%d"
        set "TYPE_!IDX!=config"
        echo   !IDX!. [配置] %%~nxd
    )
    echo.
    echo [JAR 备份]
    for %%f in ("%BACKUP_DIR%\*.jar.*") do (
        set /a "IDX+=1"
        set "BACKUP_!IDX!=%%f"
        set "TYPE_!IDX!=jar"
        echo   !IDX!. [JAR] %%~nxf
    )
)

if %IDX%==0 (
    echo   无可用备份
    goto :eof
)

echo.
set /p "CHOICE=请选择要还原的备份 (1-%IDX%, q 退出): "
if /i "%CHOICE%"=="q" goto :eof

set "SELECTED=!BACKUP_%CHOICE%!"
if not defined SELECTED (
    echo [错误] 无效选择
    goto :eof
)

echo.
echo 将还原: %SELECTED%
set /p "CONFIRM=确认还原? (y/N): "
if /i not "%CONFIRM%"=="y" (
    echo 已取消
    goto :eof
)

REM 执行还原
set "SELECTED_TYPE=!TYPE_%CHOICE%!"
if /i "%SELECTED_TYPE%"=="config" (
    echo [还原] 配置文件...
    xcopy "%SELECTED%" "%CONFIG_DIR%\" /E /I /Q /Y >nul 2>&1
    echo   √ 配置已还原
) else if /i "%SELECTED_TYPE%"=="jar" (
    echo [还原] JAR 文件...
    copy "%SELECTED%" "%APP_HOME%\%JAR_NAME%" >nul 2>&1
    echo   √ JAR 已还原
) else (
    REM 根据文件类型判断
    if exist "%SELECTED%\*" (
        echo [还原] 配置文件...
        xcopy "%SELECTED%" "%CONFIG_DIR%\" /E /I /Q /Y >nul 2>&1
        echo   √ 配置已还原
    ) else (
        echo [还原] JAR 文件...
        copy "%SELECTED%" "%APP_HOME%\%JAR_NAME%" >nul 2>&1
        echo   √ JAR 已还原
    )
)

echo.
echo [完成] 还原完成
echo [提示] 请重启应用使更改生效

endlocal
