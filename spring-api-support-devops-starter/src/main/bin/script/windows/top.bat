@echo off
chcp 65001 >nul 2>&1
setlocal enabledelayedexpansion

REM ============================================================
REM CPU/内存分析脚本 (Windows)
REM ============================================================

set "SCRIPT_DIR=%~dp0"
for %%i in ("%SCRIPT_DIR%\..\..") do set "BIN_DIR=%%~fi"
call "%BIN_DIR%\common.bat"

if "%~1"=="" goto :show_all
if "%~1"=="-h" goto :show_help
if "%~1"=="--help" goto :show_help
if "%~1"=="-c" goto :show_cpu
if "%~1"=="-m" goto :show_memory
if "%~1"=="-t" goto :show_threads
if "%~1"=="-w" goto :watch_mode
goto :show_help

:show_help
echo.
echo CPU/内存分析
echo.
echo 用法: %~nx0 [命令]
echo.
echo 命令:
echo   (无参数)          显示 CPU 和内存使用情况
echo   -c               仅显示 CPU 使用
echo   -m               仅显示内存使用
echo   -t               显示线程信息
echo   -w ^<seconds^>     监控模式 (持续刷新)
echo.
echo 选项:
echo   -h, --help       显示帮助信息
echo.
echo 示例:
echo   %~nx0                      显示概览
echo   %~nx0 -c                   CPU 详情
echo   %~nx0 -m                   内存详情
echo   %~nx0 -t                   线程信息
echo   %~nx0 -w 5                 每5秒刷新
echo.
goto :eof

:show_all
echo.
echo ╔══════════════════════════════════════════════════════════════╗
echo ║                  应用资源使用情况                            ║
echo ╚══════════════════════════════════════════════════════════════╝
echo.

call :get_pid
if not defined APP_PID (
    echo [警告] 应用未运行
    echo.
    goto :eof
)

echo 应用 PID: %APP_PID%
echo.

call :show_cpu_info
call :show_memory_info
goto :eof

:show_cpu
echo.
echo ╔══════════════════════════════════════════════════════════════╗
echo ║                  CPU 使用详情                                ║
echo ╚══════════════════════════════════════════════════════════════╝
echo.

call :get_pid
if not defined APP_PID (
    echo [警告] 应用未运行
    goto :eof
)

call :show_cpu_info

REM 使用 JDK 工具获取线程 CPU
call :check_java
if errorlevel 1 goto :eof

echo 【高 CPU 线程】
echo ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

REM 获取 jstack 输出分析
set "JSTACK_CMD=%JAVA_CMD%"
set "JSTACK_CMD=!JSTACK_CMD:java.exe=jstack.exe!"

if exist "!JSTACK_CMD!" (
    echo 正在分析线程...
    "!JSTACK_CMD!" %APP_PID% > "%TEMP%\jstack_output.txt" 2>&1
    
    REM 统计线程状态
    set "RUNNABLE=0"
    set "WAITING=0"
    set "BLOCKED=0"
    
    for /f "tokens=*" %%l in ('findstr /i "java.lang.Thread.State" "%TEMP%\jstack_output.txt"') do (
        echo %%l | findstr /i "RUNNABLE" >nul && set /a "RUNNABLE+=1"
        echo %%l | findstr /i "WAITING" >nul && set /a "WAITING+=1"
        echo %%l | findstr /i "BLOCKED" >nul && set /a "BLOCKED+=1"
    )
    
    echo   RUNNABLE: !RUNNABLE!
    echo   WAITING:  !WAITING!
    echo   BLOCKED:  !BLOCKED!
    
    del "%TEMP%\jstack_output.txt" 2>nul
) else (
    echo   jstack 不可用
)
echo.
goto :eof

:show_memory
echo.
echo ╔══════════════════════════════════════════════════════════════╗
echo ║                  内存使用详情                                ║
echo ╚══════════════════════════════════════════════════════════════╝
echo.

call :get_pid
if not defined APP_PID (
    echo [警告] 应用未运行
    goto :eof
)

call :show_memory_info

REM 使用 jcmd 获取堆信息
call :check_java
if errorlevel 1 goto :eof

echo 【JVM 堆内存】
echo ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

set "JCMD_CMD=%JAVA_CMD%"
set "JCMD_CMD=!JCMD_CMD:java.exe=jcmd.exe!"

if exist "!JCMD_CMD!" (
    "!JCMD_CMD!" %APP_PID% GC.heap_info 2>nul | findstr /v "^$"
) else (
    echo   jcmd 不可用
)
echo.
goto :eof

:show_threads
echo.
echo ╔══════════════════════════════════════════════════════════════╗
echo ║                  线程信息                                    ║
echo ╚══════════════════════════════════════════════════════════════╝
echo.

call :get_pid
if not defined APP_PID (
    echo [警告] 应用未运行
    goto :eof
)

echo 应用 PID: %APP_PID%
echo.

call :check_java
if errorlevel 1 goto :eof

set "JSTACK_CMD=%JAVA_CMD%"
set "JSTACK_CMD=!JSTACK_CMD:java.exe=jstack.exe!"

if not exist "!JSTACK_CMD!" (
    echo [错误] jstack 不可用
    goto :eof
)

echo 【线程统计】
echo ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

"!JSTACK_CMD!" %APP_PID% > "%TEMP%\jstack_threads.txt" 2>&1

REM 统计线程数
set "TOTAL=0"
for /f %%n in ('findstr /c:"\"" "%TEMP%\jstack_threads.txt" ^| find /c /v ""') do set "TOTAL=%%n"
echo   总线程数: %TOTAL%

REM 按状态统计
set "RUNNABLE=0"
set "WAITING=0"
set "TIMED_WAITING=0"
set "BLOCKED=0"

for /f "tokens=*" %%l in ('findstr /i "java.lang.Thread.State" "%TEMP%\jstack_threads.txt"') do (
    echo %%l | findstr /i "RUNNABLE" >nul && set /a "RUNNABLE+=1"
    echo %%l | findstr /i "TIMED_WAITING" >nul && set /a "TIMED_WAITING+=1"
    echo %%l | findstr /i "WAITING" >nul && set /a "WAITING+=1"
    echo %%l | findstr /i "BLOCKED" >nul && set /a "BLOCKED+=1"
)

echo.
echo 【状态分布】
echo   RUNNABLE:       !RUNNABLE!
echo   WAITING:        !WAITING!
echo   TIMED_WAITING:  !TIMED_WAITING!
echo   BLOCKED:        !BLOCKED!

del "%TEMP%\jstack_threads.txt" 2>nul
echo.
goto :eof

:watch_mode
set "INTERVAL=%~2"
if "%INTERVAL%"=="" set "INTERVAL=5"

echo.
echo ╔══════════════════════════════════════════════════════════════╗
echo ║                  监控模式 (Ctrl+C 退出)                      ║
echo ╚══════════════════════════════════════════════════════════════╝
echo.
echo 刷新间隔: %INTERVAL% 秒
echo.

:watch_loop
cls
echo ╔══════════════════════════════════════════════════════════════╗
echo ║                  应用监控                                    ║
echo ╚══════════════════════════════════════════════════════════════╝
echo 时间: %DATE% %TIME%
echo 刷新间隔: %INTERVAL% 秒 (Ctrl+C 退出)
echo.

call :get_pid
if not defined APP_PID (
    echo [警告] 应用未运行
    timeout /t %INTERVAL% /nobreak >nul
    goto :watch_loop
)

echo 应用 PID: %APP_PID%
echo.

call :show_cpu_info
call :show_memory_info

timeout /t %INTERVAL% /nobreak >nul
goto :watch_loop

:show_cpu_info
echo 【CPU 使用】
echo ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

REM 使用 wmic 获取 CPU 信息
for /f "tokens=2 delims==" %%c in ('wmic process where "processid=%APP_PID%" get PercentProcessorTime /value 2^>nul') do (
    set "CPU_USAGE=%%c"
)

REM wmic 的 PercentProcessorTime 可能不准，使用 powershell 获取
for /f %%c in ('powershell -Command "Get-Process -Id %APP_PID% -ErrorAction SilentlyContinue | Select-Object -ExpandProperty CPU"') do (
    set "CPU_TIME=%%c"
)

if defined CPU_TIME (
    echo   CPU 时间: !CPU_TIME! 秒
) else (
    echo   CPU 时间: N/A
)

REM 获取进程核心数
for /f "tokens=2 delims==" %%t in ('wmic process where "processid=%APP_PID%" get ThreadCount /value 2^>nul') do (
    set "THREAD_COUNT=%%t"
)

if defined THREAD_COUNT (
    echo   线程数:  !THREAD_COUNT!
)
echo.
goto :eof

:show_memory_info
echo 【内存使用】
echo ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

REM 使用 wmic 获取内存信息
for /f "tokens=2 delims==" %%m in ('wmic process where "processid=%APP_PID%" get WorkingSetSize /value 2^>nul') do (
    set "MEM_BYTES=%%m"
)

if defined MEM_BYTES (
    set /a "MEM_MB=!MEM_BYTES!/1024/1024"
    echo   物理内存:   !MEM_MB! MB
)

for /f "tokens=2 delims==" %%m in ('wmic process where "processid=%APP_PID%" get VirtualSize /value 2^>nul') do (
    set "VMEM_BYTES=%%m"
)

if defined VMEM_BYTES (
    set /a "VMEM_MB=!VMEM_BYTES!/1024/1024"
    echo   虚拟内存:   !VMEM_MB! MB
)

for /f "tokens=2 delims==" %%m in ('wmic process where "processid=%APP_PID%" get PrivatePageCount /value 2^>nul') do (
    set "PRIVATE_BYTES=%%m"
)

if defined PRIVATE_BYTES (
    set /a "PRIVATE_MB=!PRIVATE_BYTES!/1024/1024"
    echo   私有内存:   !PRIVATE_MB! MB
)
echo.
goto :eof

endlocal
