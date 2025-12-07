@echo off
REM OAuth 单点登录系统启动脚本 (Windows)
REM 作者: CH
REM 创建时间: 2024

setlocal enabledelayedexpansion

REM 获取脚本所在目录
set SCRIPT_DIR=%~dp0
cd /d %SCRIPT_DIR%..
set APP_HOME=%cd%
set APP_NAME=spring-api-support-oauth-starter
set JAR_NAME=%APP_NAME%-4.0.0.jar

REM 配置目录
set CONFIG_DIR=%APP_HOME%\config
set LIB_DIR=%APP_HOME%\lib
set LOG_DIR=%APP_HOME%\logs

REM JVM参数
if "%JAVA_OPTS%"=="" set JAVA_OPTS=-Xms512m -Xmx1024m -XX:+UseG1GC
set JAVA_OPTS=%JAVA_OPTS% -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=%LOG_DIR%

REM 日志配置
set JAVA_OPTS=%JAVA_OPTS% -Dlogging.file.path=%LOG_DIR%

REM Spring配置
set SPRING_OPTS=--spring.config.location=classpath:/,file:%CONFIG_DIR%/
if "%SPRING_PROFILE%"=="" set SPRING_PROFILE=prod
set SPRING_OPTS=%SPRING_OPTS% --spring.profiles.active=%SPRING_PROFILE%

REM 创建日志目录
if not exist "%LOG_DIR%" mkdir "%LOG_DIR%"

REM 检查Java环境
if "%JAVA_HOME%"=="" (
    set JAVA_CMD=java
) else (
    set JAVA_CMD=%JAVA_HOME%\bin\java
)

REM 构建classpath
set CLASSPATH=%CONFIG_DIR%;%APP_HOME%\%JAR_NAME%
for %%i in ("%LIB_DIR%\*.jar") do (
    set CLASSPATH=!CLASSPATH!;%%i
)

echo ==========================================
echo   %APP_NAME% 启动中...
echo ==========================================
echo   APP_HOME: %APP_HOME%
echo   JAVA_CMD: %JAVA_CMD%
echo   CONFIG_DIR: %CONFIG_DIR%
echo ==========================================

REM 启动应用
"%JAVA_CMD%" %JAVA_OPTS% ^
    -cp "%CLASSPATH%" ^
    com.chua.starter.oauth.sso.AuthApplication ^
    %SPRING_OPTS% ^
    %*

endlocal
