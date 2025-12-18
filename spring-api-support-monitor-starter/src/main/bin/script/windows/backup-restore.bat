@echo off
REM ============================================================
REM 备份还原脚本 (Windows)
REM 作者: CH
REM 版本: 1.0.0
REM ============================================================
setlocal enabledelayedexpansion

set SCRIPT_DIR=%~dp0
cd /d %SCRIPT_DIR%..\..
set APP_HOME=%cd%
set BACKUP_DIR=%APP_HOME%\backup

set ACTION=%~1
set TARGET=%~2

REM 初始化备份目录
if not exist "%BACKUP_DIR%\config" mkdir "%BACKUP_DIR%\config"
if not exist "%BACKUP_DIR%\jar" mkdir "%BACKUP_DIR%\jar"

if "%ACTION%"=="" goto help
if /i "%ACTION%"=="backup" goto backup
if /i "%ACTION%"=="restore" goto restore
if /i "%ACTION%"=="list" goto list
if /i "%ACTION%"=="clean" goto clean
if /i "%ACTION%"=="help" goto help
echo [错误] 未知命令: %ACTION%
goto help

:help
echo ============================================================
echo   备份还原脚本
echo ============================================================
echo.
echo 用法: %~nx0 [命令] [目标]
echo.
echo 命令:
echo   backup    备份 (config/jar/all)
echo   restore   还原 (config/jar/all)
echo   list      列出备份
echo   clean     清理旧备份 (默认保留7天)
echo.
echo 示例:
echo   %~nx0 backup config    # 备份配置
echo   %~nx0 backup jar       # 备份JAR
echo   %~nx0 backup           # 备份全部
echo   %~nx0 restore config   # 还原配置
echo   %~nx0 list             # 列出备份
echo.
goto end

:backup
set TIMESTAMP=%date:~0,4%%date:~5,2%%date:~8,2%_%time:~0,2%%time:~3,2%%time:~6,2%
set TIMESTAMP=%TIMESTAMP: =0%

if /i "%TARGET%"=="config" goto backup_config
if /i "%TARGET%"=="jar" goto backup_jar
goto backup_all

:backup_config
if exist "%APP_HOME%\config" (
    echo [备份] 配置文件...
    tar -czf "%BACKUP_DIR%\config\config_%TIMESTAMP%.tar.gz" -C "%APP_HOME%" config 2>nul
    if %errorlevel% equ 0 (
        echo [成功] 配置已备份
    ) else (
        xcopy "%APP_HOME%\config" "%BACKUP_DIR%\config\config_%TIMESTAMP%" /E /I /Q >nul 2>&1
        if %errorlevel% equ 0 (
            echo [成功] 配置已备份 (复制方式)
        ) else (
            echo [失败] 配置备份失败
        )
    )
) else (
    echo [警告] 配置目录不存在
)
goto end

:backup_jar
echo [备份] JAR文件...
for %%f in ("%APP_HOME%\*.jar") do (
    copy "%%f" "%BACKUP_DIR%\jar\%%~nf_%TIMESTAMP%.jar" >nul 2>&1
    if %errorlevel% equ 0 (
        echo [成功] %%~nxf 已备份
    ) else (
        echo [失败] JAR备份失败
    )
)
goto end

:backup_all
call :backup_config
call :backup_jar
goto end

:restore
if /i "%TARGET%"=="config" goto restore_config
if /i "%TARGET%"=="jar" goto restore_jar
goto restore_all

:restore_config
echo [还原] 配置文件...
for /f "delims=" %%f in ('dir /b /od "%BACKUP_DIR%\config\*.tar.gz" 2^>nul') do set LATEST_CONFIG=%%f
if defined LATEST_CONFIG (
    if exist "%APP_HOME%\config" (
        ren "%APP_HOME%\config" "config.old.%TIMESTAMP%" 2>nul
    )
    tar -xzf "%BACKUP_DIR%\config\%LATEST_CONFIG%" -C "%APP_HOME%" 2>nul
    if %errorlevel% equ 0 (
        echo [成功] 配置已还原: %LATEST_CONFIG%
    ) else (
        echo [失败] 配置还原失败
    )
) else (
    echo [警告] 未找到配置备份
)
goto end

:restore_jar
echo [还原] JAR文件...
for /f "delims=" %%f in ('dir /b /od "%BACKUP_DIR%\jar\*.jar" 2^>nul') do set LATEST_JAR=%%f
if defined LATEST_JAR (
    REM 获取原始文件名
    set JAR_NAME=%LATEST_JAR%
    set JAR_NAME=!JAR_NAME:_20=!
    for /f "tokens=1 delims=_" %%n in ("!LATEST_JAR!") do set JAR_BASE=%%n
    
    if exist "%APP_HOME%\!JAR_BASE!.jar" (
        ren "%APP_HOME%\!JAR_BASE!.jar" "!JAR_BASE!.jar.old" 2>nul
    )
    copy "%BACKUP_DIR%\jar\%LATEST_JAR%" "%APP_HOME%\" >nul 2>&1
    echo [成功] JAR已还原: %LATEST_JAR%
) else (
    echo [警告] 未找到JAR备份
)
goto end

:restore_all
call :restore_config
call :restore_jar
goto end

:list
echo ============================================================
echo   备份列表
echo ============================================================
echo.
echo [配置备份]
dir /b /od "%BACKUP_DIR%\config\*" 2>nul || echo   (无)
echo.
echo [JAR备份]
dir /b /od "%BACKUP_DIR%\jar\*.jar" 2>nul || echo   (无)
echo.
goto end

:clean
set DAYS=%TARGET%
if "%DAYS%"=="" set DAYS=7
echo [清理] 删除 %DAYS% 天前的备份...
forfiles /p "%BACKUP_DIR%" /s /m *.* /d -%DAYS% /c "cmd /c del @path" 2>nul
echo [完成] 清理完成
goto end

:end
endlocal
