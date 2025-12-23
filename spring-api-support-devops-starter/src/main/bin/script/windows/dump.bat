@echo off
chcp 65001 >nul 2>&1
setlocal enabledelayedexpansion

REM ============================================================
REM Dump 转储脚本 (Windows)
REM 支持堆转储和线程转储
REM ============================================================

set "PID=%~1"
set "DUMP_MODE=%~2"
set "OUTPUT_DIR=%~3"
set "APP_NAME=%~4"

if "%PID%"=="" (
    echo [错误] 请提供进程 PID
    exit /b 1
)
if "%DUMP_MODE%"=="" set "DUMP_MODE=all"
if "%OUTPUT_DIR%"=="" set "OUTPUT_DIR=%~dp0..\..\logs\dump"

REM 创建输出目录
if not exist "%OUTPUT_DIR%" mkdir "%OUTPUT_DIR%"

REM 检查 JAVA_HOME
set "JMAP=jmap"
set "JSTACK=jstack"
if defined JAVA_HOME (
    set "JMAP=%JAVA_HOME%\bin\jmap.exe"
    set "JSTACK=%JAVA_HOME%\bin\jstack.exe"
)

REM 生成时间戳
for /f "tokens=1-6 delims=/:. " %%a in ("%date% %time%") do (
    set "TIMESTAMP=%%a%%b%%c_%%d%%e%%f"
)

echo.
echo ╔══════════════════════════════════════════════════════════════╗
echo ║                    生成 Dump 转储                            ║
echo ╚══════════════════════════════════════════════════════════════╝
echo.
echo   PID:        %PID%
echo   模式:       %DUMP_MODE%
echo   输出目录:   %OUTPUT_DIR%
echo.

if /i "%DUMP_MODE%"=="heap" goto :dump_heap
if /i "%DUMP_MODE%"=="thread" goto :dump_thread
if /i "%DUMP_MODE%"=="all" (
    call :dump_heap
    call :dump_thread
    goto :done
)

echo [错误] 未知的 dump 模式: %DUMP_MODE%
echo 支持的模式: heap, thread, all
exit /b 1

:dump_heap
echo [堆转储] 生成中...
set "HEAP_FILE=%OUTPUT_DIR%\heapdump_%TIMESTAMP%.hprof"
"%JMAP%" -dump:format=b,file="%HEAP_FILE%" %PID% 2>nul
if exist "%HEAP_FILE%" (
    for %%f in ("%HEAP_FILE%") do set "HEAP_SIZE=%%~zf"
    call :format_size !HEAP_SIZE! HEAP_SIZE_HUMAN
    echo   √ 堆转储: %HEAP_FILE% (!HEAP_SIZE_HUMAN!)
) else (
    echo   × 堆转储失败
)
goto :eof

:dump_thread
echo [线程转储] 生成中...
set "THREAD_FILE=%OUTPUT_DIR%\threaddump_%TIMESTAMP%.txt"
"%JSTACK%" -l %PID% > "%THREAD_FILE%" 2>nul
if exist "%THREAD_FILE%" (
    for /f %%a in ('find /c /v "" ^< "%THREAD_FILE%"') do set "LINE_COUNT=%%a"
    echo   √ 线程转储: %THREAD_FILE% (!LINE_COUNT! 行)
    echo.
    echo 【线程状态统计】
    for /f %%a in ('findstr /c:"java.lang.Thread.State: RUNNABLE" "%THREAD_FILE%" ^| find /c /v ""') do echo   RUNNABLE:      %%a
    for /f %%a in ('findstr /c:"java.lang.Thread.State: WAITING" "%THREAD_FILE%" ^| find /c /v ""') do echo   WAITING:       %%a
    for /f %%a in ('findstr /c:"java.lang.Thread.State: TIMED_WAITING" "%THREAD_FILE%" ^| find /c /v ""') do echo   TIMED_WAITING: %%a
    for /f %%a in ('findstr /c:"java.lang.Thread.State: BLOCKED" "%THREAD_FILE%" ^| find /c /v ""') do echo   BLOCKED:       %%a
    
    findstr /c:"Found.*deadlock" "%THREAD_FILE%" >nul 2>&1
    if not errorlevel 1 (
        echo.
        echo [警告] 检测到死锁!
    )
) else (
    echo   × 线程转储失败
)
goto :eof

:format_size
set "SIZE=%~1"
set "RESULT_VAR=%~2"
if %SIZE% GEQ 1073741824 (
    set /a "GB=%SIZE% / 1073741824"
    set "%RESULT_VAR%=!GB! GB"
) else if %SIZE% GEQ 1048576 (
    set /a "MB=%SIZE% / 1048576"
    set "%RESULT_VAR%=!MB! MB"
) else if %SIZE% GEQ 1024 (
    set /a "KB=%SIZE% / 1024"
    set "%RESULT_VAR%=!KB! KB"
) else (
    set "%RESULT_VAR%=%SIZE% B"
)
goto :eof

:done
echo.
echo [完成] Dump 文件已保存到: %OUTPUT_DIR%

endlocal
