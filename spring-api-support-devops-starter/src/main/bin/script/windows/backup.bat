@echo off
chcp 65001 >nul 2>&1
setlocal enabledelayedexpansion

REM ============================================================
REM 备份管理脚本 (Windows)
REM ============================================================

set "SCRIPT_DIR=%~dp0"
for %%i in ("%SCRIPT_DIR%\..\..") do set "BIN_DIR=%%~fi"
call "%BIN_DIR%\common.bat"

if "%~1"=="" goto :show_backups
if "%~1"=="-h" goto :show_help
if "%~1"=="--help" goto :show_help
if "%~1"=="-c" goto :create_backup
if "%~1"=="-d" goto :delete_backup
if "%~1"=="-l" goto :show_backups
if "%~1"=="-r" goto :restore_backup
if "%~1"=="-k" goto :set_keep
goto :show_help

:show_help
echo.
echo 备份管理
echo.
echo 用法: %~nx0 [命令]
echo.
echo 命令:
echo   (无参数)          列出所有备份
echo   -c               创建新备份
echo   -d ^<id^>          删除指定备份
echo   -l               列出所有备份
echo   -r ^<id^>          恢复指定备份
echo   -k ^<count^>       设置保留备份数量
echo.
echo 选项:
echo   -h, --help       显示帮助信息
echo.
echo 示例:
echo   %~nx0                      显示备份列表
echo   %~nx0 -c                   创建备份
echo   %~nx0 -r 1                 恢复第1个备份
echo   %~nx0 -d 2                 删除第2个备份
echo   %~nx0 -k 5                 只保留最近5个备份
echo.
goto :eof

:show_backups
echo.
echo ╔══════════════════════════════════════════════════════════════╗
echo ║                  备份列表                                    ║
echo ╚══════════════════════════════════════════════════════════════╝
echo.

if not exist "%BACKUP_DIR%" (
    echo   备份目录: %BACKUP_DIR%
    echo   状态: 目录不存在
    echo.
    goto :eof
)

echo 备份目录: %BACKUP_DIR%
echo.

set "INDEX=0"
for /f "delims=" %%f in ('dir /b /o-d "%BACKUP_DIR%\*.jar" 2^>nul') do (
    set /a "INDEX+=1"
    set "BACKUP_FILE=%%f"
    
    REM 获取文件大小
    for %%s in ("%BACKUP_DIR%\%%f") do set "FILE_SIZE=%%~zs"
    set /a "SIZE_MB=!FILE_SIZE!/1024/1024"
    
    REM 获取修改时间
    for %%t in ("%BACKUP_DIR%\%%f") do set "FILE_TIME=%%~tt"
    
    echo   !INDEX!. !BACKUP_FILE!
    echo      大小: !SIZE_MB! MB
    echo      时间: !FILE_TIME!
    echo.
)

if %INDEX%==0 (
    echo   (无备份)
    echo.
)
goto :eof

:create_backup
echo.
echo ╔══════════════════════════════════════════════════════════════╗
echo ║                  创建备份                                    ║
echo ╚══════════════════════════════════════════════════════════════╝
echo.

if not exist "%JAR_PATH%" (
    echo [错误] JAR 文件不存在: %JAR_PATH%
    goto :eof
)

if not exist "%BACKUP_DIR%" mkdir "%BACKUP_DIR%"

REM 生成带时间戳的备份文件名
for /f "tokens=2 delims==" %%i in ('wmic os get localdatetime /value') do set "DT=%%i"
set "TIMESTAMP=%DT:~0,4%%DT:~4,2%%DT:~6,2%_%DT:~8,2%%DT:~10,2%%DT:~12,2%"
set "BACKUP_NAME=%APP_NAME%_%TIMESTAMP%.jar"

echo [步骤] 正在备份...
echo   源文件: %JAR_PATH%
echo   备份到: %BACKUP_DIR%\%BACKUP_NAME%

copy "%JAR_PATH%" "%BACKUP_DIR%\%BACKUP_NAME%" >nul

if errorlevel 1 (
    echo [错误] 备份失败
) else (
    echo [成功] 备份完成
    echo.
    
    REM 清理旧备份
    call :cleanup_old_backups
)
goto :eof

:cleanup_old_backups
REM 默认保留5个备份
if not defined BACKUP_KEEP set "BACKUP_KEEP=5"

set "COUNT=0"
for /f "delims=" %%f in ('dir /b /o-d "%BACKUP_DIR%\*.jar" 2^>nul') do (
    set /a "COUNT+=1"
    if !COUNT! gtr %BACKUP_KEEP% (
        echo [清理] 删除旧备份: %%f
        del "%BACKUP_DIR%\%%f"
    )
)
goto :eof

:delete_backup
set "TARGET_ID=%~2"
if "%TARGET_ID%"=="" (
    echo [错误] 请指定备份编号
    echo 用法: %~nx0 -d ^<id^>
    goto :eof
)

echo.

set "INDEX=0"
for /f "delims=" %%f in ('dir /b /o-d "%BACKUP_DIR%\*.jar" 2^>nul') do (
    set /a "INDEX+=1"
    if "!INDEX!"=="%TARGET_ID%" (
        echo 正在删除: %%f
        del "%BACKUP_DIR%\%%f"
        echo [成功] 已删除
        goto :eof
    )
)

echo [错误] 未找到编号 %TARGET_ID% 的备份
goto :eof

:restore_backup
set "TARGET_ID=%~2"
if "%TARGET_ID%"=="" (
    echo [错误] 请指定备份编号
    echo 用法: %~nx0 -r ^<id^>
    goto :eof
)

echo.
echo ╔══════════════════════════════════════════════════════════════╗
echo ║                  恢复备份                                    ║
echo ╚══════════════════════════════════════════════════════════════╝
echo.

set "INDEX=0"
for /f "delims=" %%f in ('dir /b /o-d "%BACKUP_DIR%\*.jar" 2^>nul') do (
    set /a "INDEX+=1"
    if "!INDEX!"=="%TARGET_ID%" (
        set "RESTORE_FILE=%%f"
        goto :do_restore
    )
)

echo [错误] 未找到编号 %TARGET_ID% 的备份
goto :eof

:do_restore
echo 即将恢复: %RESTORE_FILE%
echo.
set /p "CONFIRM=确认恢复? 当前文件将被覆盖 [y/N] "
if /i not "%CONFIRM%"=="y" (
    echo 已取消
    goto :eof
)

REM 先备份当前文件
if exist "%JAR_PATH%" (
    echo [步骤] 备份当前文件...
    call :create_backup_silent
)

echo [步骤] 恢复文件...
copy "%BACKUP_DIR%\%RESTORE_FILE%" "%JAR_PATH%" >nul

if errorlevel 1 (
    echo [错误] 恢复失败
) else (
    echo [成功] 恢复完成
)
goto :eof

:create_backup_silent
if not exist "%BACKUP_DIR%" mkdir "%BACKUP_DIR%"
for /f "tokens=2 delims==" %%i in ('wmic os get localdatetime /value') do set "DT=%%i"
set "TIMESTAMP=%DT:~0,4%%DT:~4,2%%DT:~6,2%_%DT:~8,2%%DT:~10,2%%DT:~12,2%"
set "BACKUP_NAME=%APP_NAME%_pre-restore_%TIMESTAMP%.jar"
copy "%JAR_PATH%" "%BACKUP_DIR%\%BACKUP_NAME%" >nul
goto :eof

:set_keep
set "KEEP_COUNT=%~2"
if "%KEEP_COUNT%"=="" (
    echo [错误] 请指定保留数量
    goto :eof
)

echo.
echo 备份保留策略: 保留最近 %KEEP_COUNT% 个备份
echo.

set "BACKUP_KEEP=%KEEP_COUNT%"
call :cleanup_old_backups
goto :eof

endlocal
