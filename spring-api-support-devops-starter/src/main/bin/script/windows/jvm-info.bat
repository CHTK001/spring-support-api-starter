@echo off
chcp 65001 >nul 2>&1
setlocal enabledelayedexpansion

REM ============================================================
REM JVM 信息查看脚本 (Windows)
REM ============================================================

set "PID=%~1"
set "APP_NAME=%~2"

if "%PID%"=="" (
    echo [错误] 请提供进程 PID
    exit /b 1
)

REM 检查 JAVA_HOME
set "JCMD=jcmd"
set "JSTAT=jstat"
set "JINFO=jinfo"
if defined JAVA_HOME (
    set "JCMD=%JAVA_HOME%\bin\jcmd.exe"
    set "JSTAT=%JAVA_HOME%\bin\jstat.exe"
    set "JINFO=%JAVA_HOME%\bin\jinfo.exe"
)

echo.
echo ╔══════════════════════════════════════════════════════════════╗
echo ║                    JVM 运行信息                              ║
echo ╚══════════════════════════════════════════════════════════════╝
echo.

echo 【进程信息】
echo   PID:        %PID%
echo   应用名称:   %APP_NAME%
echo.

REM JVM 版本信息
echo 【JVM 版本】
"%JCMD%" %PID% VM.version 2>nul
echo.

REM JVM 参数
echo 【JVM 参数】
"%JCMD%" %PID% VM.flags 2>nul
echo.

REM GC 统计
echo 【GC 统计】
"%JSTAT%" -gcutil %PID% 2>nul
echo.
echo   说明: S0/S1=Survivor区, E=Eden区, O=Old区, M=Metaspace
echo         YGC=Young GC次数, YGCT=Young GC时间, FGC=Full GC次数, FGCT=Full GC时间
echo.

REM 堆内存使用
echo 【堆内存使用】
"%JCMD%" %PID% GC.heap_info 2>nul
echo.

REM 线程数
echo 【线程信息】
for /f "tokens=*" %%a in ('"%JCMD%" %PID% Thread.print 2^>nul ^| find /c "tid="') do (
    echo   线程总数: %%a
)
echo.

endlocal
