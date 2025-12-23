@echo off
chcp 65001 >nul 2>&1
setlocal enabledelayedexpansion

REM ============================================================
REM JVM 诊断脚本 (Windows)
REM ============================================================

set "SCRIPT_DIR=%~dp0"
for %%i in ("%SCRIPT_DIR%\..\..") do set "BIN_DIR=%%~fi"
call "%BIN_DIR%\common.bat"

if "%~1"=="" goto :show_info
if "%~1"=="-h" goto :show_help
if "%~1"=="--help" goto :show_help
if "%~1"=="-f" goto :show_flags
if "%~1"=="-s" goto :show_sysprops
if "%~1"=="-c" goto :show_classloading
if "%~1"=="-g" goto :show_gc
if "%~1"=="-v" goto :show_vm_info
goto :show_help

:show_help
echo.
echo JVM 诊断
echo.
echo 用法: %~nx0 [命令]
echo.
echo 命令:
echo   (无参数)          显示 JVM 概要信息
echo   -f               显示 JVM 参数 (flags)
echo   -s               显示系统属性
echo   -c               显示类加载信息
echo   -g               显示 GC 信息
echo   -v               显示 VM 信息
echo.
echo 选项:
echo   -h, --help       显示帮助信息
echo.
echo 示例:
echo   %~nx0                      JVM 概要
echo   %~nx0 -f                   查看 JVM 参数
echo   %~nx0 -g                   查看 GC 状态
echo   %~nx0 -s                   查看系统属性
echo.
goto :eof

:show_info
echo.
echo ╔══════════════════════════════════════════════════════════════╗
echo ║                  JVM 概要信息                                ║
echo ╚══════════════════════════════════════════════════════════════╝
echo.

call :get_pid
if not defined APP_PID (
    echo [警告] 应用未运行
    echo.
    goto :eof
)

call :check_java
if errorlevel 1 goto :eof

set "JCMD=%JAVA_CMD%"
set "JCMD=!JCMD:java.exe=jcmd.exe!"

if not exist "!JCMD!" (
    echo [错误] jcmd 不可用
    goto :eof
)

echo 应用 PID: %APP_PID%
echo.

echo 【VM 信息】
echo ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
"!JCMD!" %APP_PID% VM.version 2>nul
echo.

echo 【启动时间】
echo ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
"!JCMD!" %APP_PID% VM.uptime 2>nul
echo.

echo 【堆内存概要】
echo ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
"!JCMD!" %APP_PID% GC.heap_info 2>nul | findstr /v "^$"
echo.
goto :eof

:show_flags
echo.
echo ╔══════════════════════════════════════════════════════════════╗
echo ║                  JVM 参数                                    ║
echo ╚══════════════════════════════════════════════════════════════╝
echo.

call :get_pid
if not defined APP_PID (
    echo [警告] 应用未运行
    goto :eof
)

call :check_java
if errorlevel 1 goto :eof

set "JCMD=%JAVA_CMD%"
set "JCMD=!JCMD:java.exe=jcmd.exe!"

if not exist "!JCMD!" (
    echo [错误] jcmd 不可用
    goto :eof
)

echo 【所有 JVM 参数】
echo ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
"!JCMD!" %APP_PID% VM.flags 2>nul
echo.

echo 【命令行参数】
echo ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
"!JCMD!" %APP_PID% VM.command_line 2>nul
echo.
goto :eof

:show_sysprops
echo.
echo ╔══════════════════════════════════════════════════════════════╗
echo ║                  系统属性                                    ║
echo ╚══════════════════════════════════════════════════════════════╝
echo.

call :get_pid
if not defined APP_PID (
    echo [警告] 应用未运行
    goto :eof
)

call :check_java
if errorlevel 1 goto :eof

set "JCMD=%JAVA_CMD%"
set "JCMD=!JCMD:java.exe=jcmd.exe!"

if not exist "!JCMD!" (
    echo [错误] jcmd 不可用
    goto :eof
)

"!JCMD!" %APP_PID% VM.system_properties 2>nul
goto :eof

:show_classloading
echo.
echo ╔══════════════════════════════════════════════════════════════╗
echo ║                  类加载信息                                  ║
echo ╚══════════════════════════════════════════════════════════════╝
echo.

call :get_pid
if not defined APP_PID (
    echo [警告] 应用未运行
    goto :eof
)

call :check_java
if errorlevel 1 goto :eof

set "JCMD=%JAVA_CMD%"
set "JCMD=!JCMD:java.exe=jcmd.exe!"

if not exist "!JCMD!" (
    echo [错误] jcmd 不可用
    goto :eof
)

echo 【类加载统计】
echo ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
"!JCMD!" %APP_PID% VM.classloader_stats 2>nul
echo.

echo 【类加载器层次】
echo ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
"!JCMD!" %APP_PID% VM.classloaders 2>nul | findstr /n "^" | findstr "^[1-9]:" | findstr "^[1-2][0-9]:"
echo   ...
echo.
goto :eof

:show_gc
echo.
echo ╔══════════════════════════════════════════════════════════════╗
echo ║                  GC 信息                                     ║
echo ╚══════════════════════════════════════════════════════════════╝
echo.

call :get_pid
if not defined APP_PID (
    echo [警告] 应用未运行
    goto :eof
)

call :check_java
if errorlevel 1 goto :eof

set "JCMD=%JAVA_CMD%"
set "JCMD=!JCMD:java.exe=jcmd.exe!"
set "JSTAT=%JAVA_CMD%"
set "JSTAT=!JSTAT:java.exe=jstat.exe!"

if not exist "!JCMD!" (
    echo [错误] jcmd 不可用
    goto :eof
)

echo 【GC 堆信息】
echo ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
"!JCMD!" %APP_PID% GC.heap_info 2>nul
echo.

if exist "!JSTAT!" (
    echo 【GC 统计 (jstat)】
    echo ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    "!JSTAT!" -gc %APP_PID% 2>nul
    echo.
    
    echo 【GC 次数和时间】
    echo ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    "!JSTAT!" -gcutil %APP_PID% 2>nul
    echo.
)

echo 【触发 GC】
echo ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
echo   使用: %~nx0 -g run   触发 Full GC
echo.

if "%~2"=="run" (
    echo [执行] 触发 GC...
    "!JCMD!" %APP_PID% GC.run 2>nul
    echo [完成]
    echo.
)
goto :eof

:show_vm_info
echo.
echo ╔══════════════════════════════════════════════════════════════╗
echo ║                  VM 详细信息                                 ║
echo ╚══════════════════════════════════════════════════════════════╝
echo.

call :get_pid
if not defined APP_PID (
    echo [警告] 应用未运行
    goto :eof
)

call :check_java
if errorlevel 1 goto :eof

set "JCMD=%JAVA_CMD%"
set "JCMD=!JCMD:java.exe=jcmd.exe!"
set "JINFO=%JAVA_CMD%"
set "JINFO=!JINFO:java.exe=jinfo.exe!"

if not exist "!JCMD!" (
    echo [错误] jcmd 不可用
    goto :eof
)

echo 【VM 版本】
echo ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
"!JCMD!" %APP_PID% VM.version 2>nul
echo.

echo 【VM 信息】
echo ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
"!JCMD!" %APP_PID% VM.info 2>nul | findstr /n "^" | findstr "^[1-9]:" | findstr "^[1-3][0-9]:"
echo   ...
echo.

echo 【可用诊断命令】
echo ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
"!JCMD!" %APP_PID% help 2>nul
echo.
goto :eof

endlocal
