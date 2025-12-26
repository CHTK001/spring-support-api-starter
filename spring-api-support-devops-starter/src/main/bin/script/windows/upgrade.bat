@echo off
chcp 65001 >nul 2>&1
setlocal enabledelayedexpansion

REM ============================================================
REM 升级管理脚本 (Windows)
REM ============================================================

set "SCRIPT_DIR=%~dp0"
for %%i in ("%SCRIPT_DIR%\..\..") do set "BIN_DIR=%%~fi"
call "%BIN_DIR%\common.bat"

set "UPLOAD_DIR=%BASE_DIR%\upload"

if "%~1"=="" goto :show_status
if "%~1"=="-h" goto :show_help
if "%~1"=="--help" goto :show_help
if "%~1"=="-u" goto :do_upgrade
if "%~1"=="-r" goto :rollback
if "%~1"=="-s" goto :show_status
if "%~1"=="-c" goto :check_upload
goto :show_help

:show_help
echo.
echo 升级管理
echo.
echo 用法: %~nx0 [命令]
echo.
echo 命令:
echo   (无参数)          显示当前状态
echo   -u               执行升级 (从 upload 目录)
echo   -r               回滚到上一版本
echo   -s               显示状态
echo   -c               检查 upload 目录
echo.
echo 选项:
echo   -h, --help       显示帮助信息
echo.
echo 说明:
echo   将新版 JAR 放入 upload 目录后执行 -u 升级
echo   升级前会自动备份当前版本
echo.
echo 示例:
echo   %~nx0                      显示状态
echo   %~nx0 -u                   执行升级
echo   %~nx0 -r                   回滚上一版本
echo   %~nx0 -c                   检查待升级文件
echo.
goto :eof

:show_status
echo.
echo ╔══════════════════════════════════════════════════════════════╗
echo ║                  升级状态                                    ║
echo ╚══════════════════════════════════════════════════════════════╝
echo.

echo 【当前版本】
echo ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
if exist "%JAR_PATH%" (
    for %%f in ("%JAR_PATH%") do (
        set "FILE_SIZE=%%~zf"
        set "FILE_TIME=%%~tf"
    )
    set /a "SIZE_MB=!FILE_SIZE!/1024/1024"
    echo   文件: %JAR_NAME%
    echo   大小: !SIZE_MB! MB
    echo   时间: !FILE_TIME!
) else (
    echo   (未安装)
)
echo.

echo 【上传目录】
echo ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
echo   路径: %UPLOAD_DIR%
if not exist "%UPLOAD_DIR%" (
    echo   状态: 目录不存在
) else (
    set "UPLOAD_COUNT=0"
    for %%f in ("%UPLOAD_DIR%\*.jar") do set /a "UPLOAD_COUNT+=1"
    if !UPLOAD_COUNT!==0 (
        echo   状态: 无待升级文件
    ) else (
        echo   状态: 发现 !UPLOAD_COUNT! 个文件
        for %%f in ("%UPLOAD_DIR%\*.jar") do (
            echo          %%~nxf
        )
    )
)
echo.

echo 【备份状态】
echo ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
if not exist "%BACKUP_DIR%" (
    echo   状态: 无备份
) else (
    set "BACKUP_COUNT=0"
    for %%f in ("%BACKUP_DIR%\*.jar") do set /a "BACKUP_COUNT+=1"
    echo   备份数量: !BACKUP_COUNT!
    if !BACKUP_COUNT! gtr 0 (
        for /f "delims=" %%f in ('dir /b /o-d "%BACKUP_DIR%\*.jar" 2^>nul') do (
            echo   最新备份: %%f
            goto :status_done
        )
    )
)
:status_done
echo.
goto :eof

:check_upload
echo.
echo ╔══════════════════════════════════════════════════════════════╗
echo ║                  检查上传目录                                ║
echo ╚══════════════════════════════════════════════════════════════╝
echo.

if not exist "%UPLOAD_DIR%" (
    echo [警告] 上传目录不存在: %UPLOAD_DIR%
    echo.
    set /p "CREATE=是否创建目录? [Y/n] "
    if /i "!CREATE!"=="Y" (
        mkdir "%UPLOAD_DIR%"
        echo [成功] 目录已创建
    )
    if "!CREATE!"=="" (
        mkdir "%UPLOAD_DIR%"
        echo [成功] 目录已创建
    )
    goto :eof
)

echo 上传目录: %UPLOAD_DIR%
echo.

set "INDEX=0"
for %%f in ("%UPLOAD_DIR%\*.jar") do (
    set /a "INDEX+=1"
    for %%s in ("%%f") do (
        set "FILE_SIZE=%%~zs"
        set "FILE_TIME=%%~ts"
    )
    set /a "SIZE_MB=!FILE_SIZE!/1024/1024"
    echo   !INDEX!. %%~nxf
    echo      大小: !SIZE_MB! MB
    echo      时间: !FILE_TIME!
    echo.
)

if %INDEX%==0 (
    echo   (无文件)
    echo.
    echo 请将新版 JAR 文件放入此目录后执行升级
)
echo.
goto :eof

:do_upgrade
echo.
echo ╔══════════════════════════════════════════════════════════════╗
echo ║                  执行升级                                    ║
echo ╚══════════════════════════════════════════════════════════════╝
echo.

if not exist "%UPLOAD_DIR%" (
    echo [错误] 上传目录不存在: %UPLOAD_DIR%
    goto :eof
)

REM 查找待升级文件
set "UPGRADE_FILE="
set "FILE_COUNT=0"
for %%f in ("%UPLOAD_DIR%\*.jar") do (
    set /a "FILE_COUNT+=1"
    set "UPGRADE_FILE=%%f"
)

if %FILE_COUNT%==0 (
    echo [错误] 上传目录中无 JAR 文件
    goto :eof
)

if %FILE_COUNT% gtr 1 (
    echo [警告] 发现多个 JAR 文件，请选择:
    echo.
    set "INDEX=0"
    for %%f in ("%UPLOAD_DIR%\*.jar") do (
        set /a "INDEX+=1"
        echo   !INDEX!. %%~nxf
    )
    echo.
    set /p "CHOICE=选择要升级的文件 (1-%FILE_COUNT%): "
    
    set "INDEX=0"
    for %%f in ("%UPLOAD_DIR%\*.jar") do (
        set /a "INDEX+=1"
        if "!INDEX!"=="!CHOICE!" set "UPGRADE_FILE=%%f"
    )
)

if not defined UPGRADE_FILE (
    echo [错误] 未选择文件
    goto :eof
)

for %%f in ("%UPGRADE_FILE%") do set "UPGRADE_NAME=%%~nxf"

echo 准备升级:
echo   当前文件: %JAR_NAME%
echo   新版文件: %UPGRADE_NAME%
echo.
set /p "CONFIRM=确认升级? [y/N] "
if /i not "%CONFIRM%"=="y" (
    echo 已取消
    goto :eof
)

REM 检查应用是否运行
call :get_pid
if defined APP_PID (
    echo [步骤] 停止应用...
    taskkill /PID %APP_PID% /F >nul 2>&1
    timeout /t 3 /nobreak >nul
    
    REM 验证进程是否真的停止了
    call :get_pid
    if defined APP_PID (
        echo [失败] 应用停止失败: 进程仍在运行 (PID: %APP_PID%)
        echo   提示: 请以管理员身份运行此脚本
        goto :eof
    )
)

REM 备份当前文件
if exist "%JAR_PATH%" (
    echo [步骤] 备份当前版本...
    call "%SCRIPT_DIR%\backup.bat" -c
)

REM 执行升级
echo [步骤] 复制新版本...
copy "%UPGRADE_FILE%" "%JAR_PATH%" >nul

if errorlevel 1 (
    echo [错误] 升级失败
    goto :eof
)

REM 清理上传文件
del "%UPGRADE_FILE%" 2>nul

echo.
echo [成功] 升级完成
echo.
set /p "START_APP=是否启动应用? [Y/n] "
if /i "%START_APP%"=="Y" (
    call "%BIN_DIR%\startup.bat" start
)
if "%START_APP%"=="" (
    call "%BIN_DIR%\startup.bat" start
)
goto :eof

:rollback
echo.
echo ╔══════════════════════════════════════════════════════════════╗
echo ║                  版本回滚                                    ║
echo ╚══════════════════════════════════════════════════════════════╝
echo.

if not exist "%BACKUP_DIR%" (
    echo [错误] 备份目录不存在
    goto :eof
)

REM 获取最新备份
set "LATEST_BACKUP="
for /f "delims=" %%f in ('dir /b /o-d "%BACKUP_DIR%\*.jar" 2^>nul') do (
    set "LATEST_BACKUP=%%f"
    goto :found_backup
)
:found_backup

if not defined LATEST_BACKUP (
    echo [错误] 无可用备份
    goto :eof
)

echo 最新备份: %LATEST_BACKUP%
echo.
set /p "CONFIRM=确认回滚? [y/N] "
if /i not "%CONFIRM%"=="y" (
    echo 已取消
    goto :eof
)

REM 检查应用是否运行
call :get_pid
if defined APP_PID (
    echo [步骤] 停止应用...
    taskkill /PID %APP_PID% /F >nul 2>&1
    timeout /t 3 /nobreak >nul
    
    REM 验证进程是否真的停止了
    call :get_pid
    if defined APP_PID (
        echo [失败] 应用停止失败: 进程仍在运行 (PID: %APP_PID%)
        echo   提示: 请以管理员身份运行此脚本
        goto :eof
    )
)

REM 执行回滚
echo [步骤] 恢复文件...
copy "%BACKUP_DIR%\%LATEST_BACKUP%" "%JAR_PATH%" >nul

if errorlevel 1 (
    echo [错误] 回滚失败
    goto :eof
)

echo.
echo [成功] 回滚完成
echo.
set /p "START_APP=是否启动应用? [Y/n] "
if /i "%START_APP%"=="Y" (
    call "%BIN_DIR%\startup.bat" start
)
if "%START_APP%"=="" (
    call "%BIN_DIR%\startup.bat" start
)
goto :eof

endlocal
