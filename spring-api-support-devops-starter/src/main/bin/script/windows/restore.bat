@echo off
chcp 65001 >nul 2>&1
setlocal enabledelayedexpansion

REM ============================================================
REM 恢复管理脚本 (Windows)
REM 快速访问备份恢复功能
REM ============================================================

set "SCRIPT_DIR=%~dp0"
for %%i in ("%SCRIPT_DIR%\..\..") do set "BIN_DIR=%%~fi"
call "%BIN_DIR%\common.bat"

if "%~1"=="-h" goto :show_help
if "%~1"=="--help" goto :show_help
if "%~1"=="" goto :interactive_restore

REM 如果有参数,直接调用 backup.bat -r
call "%SCRIPT_DIR%\backup.bat" -r %*
goto :eof

:show_help
echo.
echo 恢复管理 (快捷方式)
echo.
echo 用法: %~nx0 [id]
echo.
echo 参数:
echo   id               要恢复的备份编号 (可选)
echo.
echo 选项:
echo   -h, --help       显示帮助信息
echo.
echo 说明:
echo   无参数时进入交互式恢复模式
echo   提供编号时直接恢复指定备份
echo.
echo 示例:
echo   %~nx0                      交互式选择恢复
echo   %~nx0 1                    恢复第1个备份
echo.
echo 更多备份管理功能请使用 backup.bat
echo.
goto :eof

:interactive_restore
echo.
echo ╔══════════════════════════════════════════════════════════════╗
echo ║                  交互式恢复                                  ║
echo ╚══════════════════════════════════════════════════════════════╝
echo.

if not exist "%BACKUP_DIR%" (
    echo [错误] 备份目录不存在: %BACKUP_DIR%
    goto :eof
)

echo 可用备份:
echo.

set "INDEX=0"
for /f "delims=" %%f in ('dir /b /o-d "%BACKUP_DIR%\*.jar" 2^>nul') do (
    set /a "INDEX+=1"
    
    for %%s in ("%BACKUP_DIR%\%%f") do set "FILE_SIZE=%%~zs"
    set /a "SIZE_MB=!FILE_SIZE!/1024/1024"
    for %%t in ("%BACKUP_DIR%\%%f") do set "FILE_TIME=%%~tt"
    
    echo   !INDEX!. %%f
    echo      !SIZE_MB! MB - !FILE_TIME!
    echo.
)

if %INDEX%==0 (
    echo   (无可用备份)
    echo.
    goto :eof
)

echo.
set /p "CHOICE=请选择要恢复的备份编号 (1-%INDEX%, 或按 Enter 取消): "

if "%CHOICE%"=="" (
    echo 已取消
    goto :eof
)

REM 验证输入
set /a "CHOICE_NUM=%CHOICE%" 2>nul
if %CHOICE_NUM% lss 1 (
    echo [错误] 无效的编号
    goto :eof
)
if %CHOICE_NUM% gtr %INDEX% (
    echo [错误] 编号超出范围
    goto :eof
)

call "%SCRIPT_DIR%\backup.bat" -r %CHOICE%
goto :eof

endlocal
