@echo off
REM ============================================================
REM JVM 调优建议脚本 (Windows)
REM 作者: CH
REM 版本: 1.0.0
REM ============================================================
setlocal enabledelayedexpansion

set PID=%~1
set APP_NAME=%~2
if "%APP_NAME%"=="" set APP_NAME=Java应用

if "%PID%"=="" (
    echo [错误] 未提供进程ID
    echo 用法: %~nx0 ^<PID^> [应用名]
    exit /b 1
)

tasklist /fi "PID eq %PID%" 2>nul | find "%PID%" >nul
if %errorlevel% neq 0 (
    echo [错误] 进程 %PID% 不存在
    exit /b 1
)

set JAVA_BIN=
if defined JAVA_HOME (
    if exist "%JAVA_HOME%\bin\java.exe" set JAVA_BIN=%JAVA_HOME%\bin\
)

echo ============================================================
echo   %APP_NAME% JVM 调优分析
echo ============================================================

echo.
echo [收集系统信息...]
for /f "tokens=2 delims==" %%i in ('wmic ComputerSystem get TotalPhysicalMemory /value 2^>nul ^| find "="') do set TOTAL_MEM_BYTES=%%i
set /a TOTAL_MEM=%TOTAL_MEM_BYTES:~0,-6% 2>nul
for /f "tokens=2 delims==" %%i in ('wmic cpu get NumberOfCores /value 2^>nul ^| find "="') do set CPU_CORES=%%i
echo   总内存: %TOTAL_MEM%MB ^| CPU核心: %CPU_CORES%

echo.
echo [当前JVM参数]
if defined JAVA_BIN (
    if exist "%JAVA_BIN%jcmd.exe" (
        "%JAVA_BIN%jcmd" %PID% VM.flags 2>nul
    )
)

echo.
echo [GC统计]
if defined JAVA_BIN (
    if exist "%JAVA_BIN%jstat.exe" (
        "%JAVA_BIN%jstat" -gcutil %PID% 2>nul
    )
)

echo.
echo ============================================================
echo   调优建议
echo ============================================================
echo.
echo [本地规则分析...]

REM 计算推荐堆大小 (60%内存，最大8G)
set /a HEAP_MAX=%TOTAL_MEM% * 60 / 100
if %HEAP_MAX% gtr 8192 set HEAP_MAX=8192
set /a HEAP_MIN=%HEAP_MAX% / 2
if %HEAP_MIN% lss 512 set HEAP_MIN=512

echo 推荐 JVM 参数:
echo.
echo -Xms%HEAP_MIN%m -Xmx%HEAP_MAX%m
echo -XX:MetaspaceSize=256m -XX:MaxMetaspaceSize=256m
echo -XX:+UseG1GC -XX:MaxGCPauseMillis=200
echo -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=logs/

echo.
echo ============================================================
endlocal
