@echo off
REM 平台系统启动脚本 (Windows)
setlocal enabledelayedexpansion

set SCRIPT_DIR=%~dp0
cd /d %SCRIPT_DIR%..
set APP_HOME=%cd%
set APP_NAME=spring-api-support-system-starter
set JAR_NAME=%APP_NAME%-4.0.0.jar

set CONFIG_DIR=%APP_HOME%\config
set LIB_DIR=%APP_HOME%\lib
set LOG_DIR=%APP_HOME%\logs

if "%JAVA_OPTS%"=="" set JAVA_OPTS=-Xms512m -Xmx1024m -XX:+UseG1GC
set JAVA_OPTS=%JAVA_OPTS% -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=%LOG_DIR%
set JAVA_OPTS=%JAVA_OPTS% -Dlogging.file.path=%LOG_DIR%

set SPRING_OPTS=--spring.config.location=classpath:/,file:%CONFIG_DIR%/
if "%SPRING_PROFILE%"=="" set SPRING_PROFILE=prod
set SPRING_OPTS=%SPRING_OPTS% --spring.profiles.active=%SPRING_PROFILE%

if not exist "%LOG_DIR%" mkdir "%LOG_DIR%"

if "%JAVA_HOME%"=="" (set JAVA_CMD=java) else (set JAVA_CMD=%JAVA_HOME%\bin\java)

set CLASSPATH=%CONFIG_DIR%;%APP_HOME%\%JAR_NAME%
for %%i in ("%LIB_DIR%\*.jar") do set CLASSPATH=!CLASSPATH!;%%i

echo ==========================================
echo   %APP_NAME% Starting...
echo ==========================================

"%JAVA_CMD%" %JAVA_OPTS% -cp "%CLASSPATH%" com.chua.starter.system.SystemApplication %SPRING_OPTS% %*
endlocal
