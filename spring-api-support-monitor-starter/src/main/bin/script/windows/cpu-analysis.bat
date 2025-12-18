@echo off
REM ============================================================
REM CPU/内存分析脚本 (Windows)
REM 作者: CH
REM 版本: 1.0.0
REM ============================================================
setlocal enabledelayedexpansion

set PID=%~1
set MODE=%~2
set APP_NAME=%~3
if "%MODE%"=="" set MODE=cpu
if "%APP_NAME%"=="" set APP_NAME=Java应用

if "%PID%"=="" (
    echo [错误] 未提供进程ID
    echo 用法: %~nx0 ^<PID^> [cpu^|mem] [应用名]
    exit /b 1
)

tasklist /fi "PID eq %PID%" 2>nul | find "%PID%" >nul
if %errorlevel% neq 0 (
    echo [错误] 进程 %PID% 不存在
    exit /b 1
)

echo ============================================================
echo   %APP_NAME% 性能分析
echo ============================================================
echo   进程ID: %PID%
echo   分析模式: %MODE%
echo   时间: %date% %time%
echo ============================================================

if /i "%MODE%"=="cpu" goto cpu_analysis
if /i "%MODE%"=="mem" goto mem_analysis
echo [错误] 未知模式: %MODE%
goto end

:cpu_analysis
echo.
echo [CPU 分析]
echo.
echo --- 进程CPU使用率 ---
wmic process where "ProcessId=%PID%" get ProcessId,Name,PercentProcessorTime,ThreadCount /format:list 2>nul | find "="
echo.
echo --- 系统CPU信息 ---
wmic cpu get LoadPercentage,Name /format:list 2>nul | find "="
echo.
echo --- 高CPU线程提示 ---
echo 使用 jstack %PID% 可查看线程堆栈
echo 使用 Arthas 的 thread 命令可实时分析热点线程
goto end

:mem_analysis
echo.
echo [内存分析]
echo.
echo --- 进程内存使用 ---
wmic process where "ProcessId=%PID%" get ProcessId,WorkingSetSize,VirtualSize,PageFileUsage /format:list 2>nul | find "="
echo.
echo --- 系统内存信息 ---
wmic OS get FreePhysicalMemory,TotalVisibleMemorySize /format:list 2>nul | find "="
echo.
echo --- JVM堆内存 ---
set JAVA_BIN=
if defined JAVA_HOME set JAVA_BIN=%JAVA_HOME%\bin\
if defined JAVA_BIN (
    if exist "%JAVA_BIN%jcmd.exe" (
        "%JAVA_BIN%jcmd" %PID% GC.heap_info 2>nul
    )
)
echo.
echo --- 内存优化提示 ---
echo 使用 jmap -histo %PID% 可查看对象分布
echo 使用 jmap -dump:format=b,file=heap.hprof %PID% 可导出堆转储
goto end

:end
echo.
echo ============================================================
endlocal
