@echo off
REM ============================================================
REM 升级和回滚脚本 (Windows)
REM 作者: CH
REM 版本: 1.0.0
REM ============================================================
setlocal enabledelayedexpansion

set SCRIPT_DIR=%~dp0
cd /d %SCRIPT_DIR%..\..
set APP_HOME=%cd%
set BACKUP_DIR=%APP_HOME%\backup
set ROLLBACK_DIR=%APP_HOME%\rollback

if "%~1"=="" goto help
if /i "%~1"=="upgrade" goto upgrade
if /i "%~1"=="rollback" goto rollback
if /i "%~1"=="status" goto status
if /i "%~1"=="help" goto help
echo [错误] 未知命令: %~1
goto help

:help
echo ============================================================
echo   升级和回滚脚本
echo ============================================================
echo.
echo 用法: %~nx0 [命令] [选项]
echo.
echo 命令:
echo   upgrade          从压缩包升级
echo   rollback         回滚到上一个还原点
echo   status           查看还原点状态
echo   help             显示帮助信息
echo.
echo 选项:
echo   --package ^<path^>   升级包路径 (zip/tar.gz)
echo   --no-restart      升级后不自动重启
echo.
echo 示例:
echo   %~nx0 upgrade --package app-4.1.0.zip
echo   %~nx0 rollback
echo   %~nx0 status
echo.
goto end

:upgrade
set PACKAGE=
set NO_RESTART=false
shift
:parse_upgrade_args
if "%~1"=="" goto do_upgrade
if /i "%~1"=="--package" (
    set PACKAGE=%~2
    shift
    shift
    goto parse_upgrade_args
)
if /i "%~1"=="--no-restart" (
    set NO_RESTART=true
    shift
    goto parse_upgrade_args
)
shift
goto parse_upgrade_args

:do_upgrade
if "%PACKAGE%"=="" (
    echo [错误] 请使用 --package 指定升级包
    goto end
)

if not exist "%PACKAGE%" (
    echo [错误] 升级包不存在: %PACKAGE%
    goto end
)

echo ============================================================
echo   开始升级
echo ============================================================

REM 创建还原点
set TIMESTAMP=%date:~0,4%%date:~5,2%%date:~8,2%_%time:~0,2%%time:~3,2%%time:~6,2%
set TIMESTAMP=%TIMESTAMP: =0%
if not exist "%ROLLBACK_DIR%" mkdir "%ROLLBACK_DIR%"
set ROLLBACK_POINT=%ROLLBACK_DIR%\rollback_%TIMESTAMP%
mkdir "%ROLLBACK_POINT%"

echo [创建还原点] %ROLLBACK_POINT%

REM 备份当前JAR
for %%f in ("%APP_HOME%\*.jar") do (
    copy "%%f" "%ROLLBACK_POINT%\" >nul 2>&1
    echo   已备份: %%~nxf
)

REM 备份配置
if exist "%APP_HOME%\config" (
    xcopy "%APP_HOME%\config" "%ROLLBACK_POINT%\config\" /E /I /Q >nul 2>&1
    echo   已备份: config/
)

REM 解压升级包
echo [解压升级包]
set EXT=%PACKAGE:~-4%
if /i "%EXT%"==".zip" (
    powershell -Command "Expand-Archive -Path '%PACKAGE%' -DestinationPath '%APP_HOME%\upgrade_temp' -Force" 2>nul
) else (
    tar -xzf "%PACKAGE%" -C "%APP_HOME%\upgrade_temp" 2>nul
)

if exist "%APP_HOME%\upgrade_temp" (
    REM 复制新文件
    for /r "%APP_HOME%\upgrade_temp" %%f in (*.jar) do (
        copy "%%f" "%APP_HOME%\" >nul 2>&1
        echo   已更新: %%~nxf
    )
    if exist "%APP_HOME%\upgrade_temp\config" (
        xcopy "%APP_HOME%\upgrade_temp\config" "%APP_HOME%\config\" /E /I /Y /Q >nul 2>&1
        echo   已更新: config/
    )
    rd /s /q "%APP_HOME%\upgrade_temp" 2>nul
    echo [成功] 升级完成
) else (
    echo [失败] 解压失败
)

if /i not "%NO_RESTART%"=="true" (
    echo [提示] 请手动重启应用
)
goto end

:rollback
echo ============================================================
echo   回滚操作
echo ============================================================

REM 查找最新还原点
set LATEST_POINT=
for /f "delims=" %%d in ('dir /b /od "%ROLLBACK_DIR%\rollback_*" 2^>nul') do set LATEST_POINT=%%d

if "%LATEST_POINT%"=="" (
    echo [错误] 未找到还原点
    goto end
)

echo [还原点] %LATEST_POINT%
set /p CONFIRM="确认回滚? [y/N]: "
if /i not "%CONFIRM%"=="y" (
    echo [取消] 操作已取消
    goto end
)

REM 恢复JAR
for %%f in ("%ROLLBACK_DIR%\%LATEST_POINT%\*.jar") do (
    copy "%%f" "%APP_HOME%\" >nul 2>&1
    echo   已恢复: %%~nxf
)

REM 恢复配置
if exist "%ROLLBACK_DIR%\%LATEST_POINT%\config" (
    xcopy "%ROLLBACK_DIR%\%LATEST_POINT%\config" "%APP_HOME%\config\" /E /I /Y /Q >nul 2>&1
    echo   已恢复: config/
)

echo [成功] 回滚完成
echo [提示] 请手动重启应用
goto end

:status
echo ============================================================
echo   还原点状态
echo ============================================================
echo.
if exist "%ROLLBACK_DIR%" (
    echo [还原点列表]
    for /f "delims=" %%d in ('dir /b /od "%ROLLBACK_DIR%\rollback_*" 2^>nul') do (
        echo   %%d
    )
) else (
    echo   (无还原点)
)
echo.
goto end

:end
endlocal
