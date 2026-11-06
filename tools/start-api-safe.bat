@echo off
rem ============================================================
rem  system-api hardened launcher (spring-api-support-system-starter)
rem  port 18170, context-path /system/api
rem
rem  Why this exists (differences from start-api.bat):
rem    1) explicit -cp instead of java -jar.
rem       The jar references lib/ through its MANIFEST Class-Path. If any
rem       dependency jar is truncated (an interrupted copy-dependencies leaves
rem       a file whose zip has no central directory), java -jar dies with an
rem       opaque NoClassDefFoundError that never names the bad jar.
rem       Explicit -cp is deterministic.
rem    2) lib integrity precheck: refuses to start when lib looks incomplete.
rem    3) port precheck: refuses to double-start.
rem
rem  Usage (on the server):
rem    D:\app\system\bin\start-api-safe.bat
rem ============================================================
setlocal
set "APP_DIR=D:\app\system\api"
set "JAVA_HOME=D:\app\system\runtime\jdk25"
set "LOG_DIR=D:\app\system\logs"
set "JAR=spring-api-support-system-starter-4.0.0.42.jar"
set "MAIN=com.chua.starter.system.SystemApplication"
set "MIN_LIBS=300"

if not exist "%LOG_DIR%" mkdir "%LOG_DIR%"
set "LOG=%LOG_DIR%\api.log"

if not exist "%JAVA_HOME%\bin\java.exe" (
  echo [ERROR] java not found: %JAVA_HOME%\bin\java.exe
  exit /b 1
)
if not exist "%APP_DIR%\%JAR%" (
  echo [ERROR] jar not found: %APP_DIR%\%JAR%
  exit /b 1
)
if not exist "%APP_DIR%\lib" (
  echo [ERROR] lib dir not found: %APP_DIR%\lib
  exit /b 1
)

set /a LIBCOUNT=0
for %%f in ("%APP_DIR%\lib\*.jar") do set /a LIBCOUNT+=1
if %LIBCOUNT% LSS %MIN_LIBS% (
  echo [ERROR] lib has only %LIBCOUNT% jars, expected at least %MIN_LIBS%.
  echo         Re-copy lib/ from the build output, then retry.
  exit /b 1
)
echo [%date% %time%] start-api-safe: lib count = %LIBCOUNT% >> "%LOG%"

netstat -ano | findstr ":18170" | findstr "LISTENING" >nul 2>&1
if not errorlevel 1 (
  echo [ERROR] port 18170 is already listening. Run stop-all.bat first.
  exit /b 1
)

cd /d "%APP_DIR%"
echo [%date% %time%] start-api-safe: launching >> "%LOG%"
start "system-api" /min cmd /c ""%JAVA_HOME%\bin\java.exe" -Dfile.encoding=UTF-8 -cp "%JAR%;lib/*" %MAIN% >> "%LOG%" 2>&1"
echo system-api started (safe launcher), log: %LOG%
endlocal
