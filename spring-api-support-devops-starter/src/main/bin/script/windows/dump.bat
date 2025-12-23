@echo off
chcp 65001 >nul 2>&1
setlocal enabledelayedexpansion

REM ============================================================
REM Dump 转储脚本 (Windows)
REM 支持堆转储和线程转储
REM ============================================================

set "SCRIPT_DIR=%~dp0"
for %%i in ("%SCRIPT_DIR%\..\..") do set "BIN_DIR=%%~fi"
call "%BIN_DIR%\common.bat"

set "OUTPUT_DIR=%LOG_DIR%\dump"

if "%~1"=="-h" goto :show_help
if "%~1"=="--help" goto :show_help
if "%~1"=="" goto :dump_all
if "%~1"=="-a" goto :dump_all
if "%~1"=="-H" goto :dump_heap
if "%~1"=="-t" goto :dump_thread
if "%~1"=="-l" goto :list_dumps
if "%~1"=="-c" goto :cleanup_dumps
goto :show_help

:show_help
echo.
echo Dump 转储管理
echo.
echo 用法: %~nx0 [命令]
echo.
echo 命令:
echo   (无参数)          生成堆+线程转储
echo   -a               生成所有转储 (堆+线程)
echo   -H               只生成堆转储
echo   -t               只生成线程转储
echo   -l               列出已有转储文件
echo   -c               清理旧转储文件
echo.
echo 选项:
echo   -h, --help       显示帮助信息
echo.
echo 输出目录: %OUTPUT_DIR%
echo.
echo 示例:
echo   %~nx0                      生成所有转储
echo   %~nx0 -H                   只生成堆转储
echo   %~nx0 -t                   只生成线程转储
echo   %~nx0 -l                   查看已有文件
echo.
goto :eof

:dump_all
echo.
echo ╔══════════════════════════════════════════════════════════════╗
echo ║                  生成 Dump 转储                              ║
echo ╚══════════════════════════════════════════════════════════════╝
echo.

call :get_pid
if not defined APP_PID (
    echo [警告] 应用未运行
    goto :eof
)

call :check_java
if errorlevel 1 goto :eof

call :prepare_dump
echo 应用 PID: %APP_PID%
echo 输出目录: %OUTPUT_DIR%
echo.

call :do_dump_heap
call :do_dump_thread

echo.
echo [完成] Dump 文件已保存到: %OUTPUT_DIR%
echo.
goto :eof

:dump_heap
echo.
echo ╔══════════════════════════════════════════════════════════════╗
echo ║                  生成堆转储                                  ║
echo ╚══════════════════════════════════════════════════════════════╝
echo.

call :get_pid
if not defined APP_PID (
    echo [警告] 应用未运行
    goto :eof
)

call :check_java
if errorlevel 1 goto :eof

call :prepare_dump
echo 应用 PID: %APP_PID%
echo.

call :do_dump_heap
echo.
goto :eof

:dump_thread
echo.
echo ╔══════════════════════════════════════════════════════════════╗
echo ║                  生成线程转储                                ║
echo ╚══════════════════════════════════════════════════════════════╝
echo.

call :get_pid
if not defined APP_PID (
    echo [警告] 应用未运行
    goto :eof
)

call :check_java
if errorlevel 1 goto :eof

call :prepare_dump
echo 应用 PID: %APP_PID%
echo.

call :do_dump_thread
echo.
goto :eof

:list_dumps
echo.
echo ╔══════════════════════════════════════════════════════════════╗
echo ║                  Dump 文件列表                               ║
echo ╚══════════════════════════════════════════════════════════════╝
echo.
echo 输出目录: %OUTPUT_DIR%
echo.

if not exist "%OUTPUT_DIR%" (
    echo   (目录不存在)
    goto :eof
)

echo 【堆转储 (.hprof)】
echo ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
set "HEAP_COUNT=0"
for %%f in ("%OUTPUT_DIR%\*.hprof") do (
    set /a "HEAP_COUNT+=1"
    for %%s in ("%%f") do set "FILE_SIZE=%%~zs"
    set /a "SIZE_MB=!FILE_SIZE!/1024/1024"
    echo   %%~nxf (!SIZE_MB! MB)
)
if !HEAP_COUNT!==0 echo   (无)
echo.

echo 【线程转储 (.txt)】
echo ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
set "THREAD_COUNT=0"
for %%f in ("%OUTPUT_DIR%\threaddump*.txt") do (
    set /a "THREAD_COUNT+=1"
    for %%s in ("%%f") do set "FILE_SIZE=%%~zs"
    set /a "SIZE_KB=!FILE_SIZE!/1024"
    echo   %%~nxf (!SIZE_KB! KB)
)
if !THREAD_COUNT!==0 echo   (无)
echo.
goto :eof

:cleanup_dumps
echo.
echo ╔══════════════════════════════════════════════════════════════╗
echo ║                  清理 Dump 文件                               ║
echo ╚══════════════════════════════════════════════════════════════╝
echo.

if not exist "%OUTPUT_DIR%" (
    echo [提示] 目录不存在
    goto :eof
)

set "TOTAL=0"
for %%f in ("%OUTPUT_DIR%\*.hprof" "%OUTPUT_DIR%\threaddump*.txt") do set /a "TOTAL+=1"

if %TOTAL%==0 (
    echo [提示] 无文件需要清理
    goto :eof
)

echo 将删除 %TOTAL% 个文件
echo.
set /p "CONFIRM=确认删除? [y/N] "
if /i not "%CONFIRM%"=="y" (
    echo 已取消
    goto :eof
)

del "%OUTPUT_DIR%\*.hprof" 2>nul
del "%OUTPUT_DIR%\threaddump*.txt" 2>nul
echo [完成] 已清理
echo.
goto :eof

:prepare_dump
if not exist "%OUTPUT_DIR%" mkdir "%OUTPUT_DIR%"

set "JMAP=%JAVA_CMD%"
set "JMAP=!JMAP:java.exe=jmap.exe!"
set "JSTACK=%JAVA_CMD%"
set "JSTACK=!JSTACK:java.exe=jstack.exe!"

for /f "tokens=2 delims==" %%i in ('wmic os get localdatetime /value') do set "DT=%%i"
set "TIMESTAMP=%DT:~0,4%%DT:~4,2%%DT:~6,2%_%DT:~8,2%%DT:~10,2%%DT:~12,2%"
goto :eof

:do_dump_heap
echo [堆转储] 生成中...
set "HEAP_FILE=%OUTPUT_DIR%\heapdump_%TIMESTAMP%.hprof"

if not exist "!JMAP!" (
    echo   × jmap 不可用
    goto :eof
)

"!JMAP!" -dump:format=b,file="%HEAP_FILE%" %APP_PID% 2>nul

if exist "%HEAP_FILE%" (
    for %%f in ("%HEAP_FILE%") do set "HEAP_SIZE=%%~zf"
    call :format_size !HEAP_SIZE! HEAP_SIZE_HUMAN
    echo   √ 堆转储: %HEAP_FILE%
    echo     大小: !HEAP_SIZE_HUMAN!
) else (
    echo   × 堆转储失败
)
goto :eof

:do_dump_thread
echo [线程转储] 生成中...
set "THREAD_FILE=%OUTPUT_DIR%\threaddump_%TIMESTAMP%.txt"

if not exist "!JSTACK!" (
    echo   × jstack 不可用
    goto :eof
)

"!JSTACK!" -l %APP_PID% > "%THREAD_FILE%" 2>nul

if exist "%THREAD_FILE%" (
    for /f %%a in ('find /c /v "" ^< "%THREAD_FILE%"') do set "LINE_COUNT=%%a"
    echo   √ 线程转储: %THREAD_FILE%
    echo     行数: !LINE_COUNT!
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

endlocal
