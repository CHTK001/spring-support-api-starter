@echo off
chcp 65001 >nul
REM 清空同名目录后解压
REM Usage: extract-clean.bat

setlocal enabledelayedexpansion
set "SCRIPT_DIR=%~dp0"
set "ARCHIVE_NAME=${project.artifactId}-${project.version}"
set "TARGET_DIR=%SCRIPT_DIR%%ARCHIVE_NAME%"

REM 查找压缩包
if exist "%SCRIPT_DIR%%ARCHIVE_NAME%.zip" (
    set "ARCHIVE=%SCRIPT_DIR%%ARCHIVE_NAME%.zip"
) else (
    echo Error: Archive not found: %ARCHIVE_NAME%.zip
    exit /b 1
)

echo Cleaning directory: %TARGET_DIR%
if exist "%TARGET_DIR%" (
    rmdir /s /q "%TARGET_DIR%"
)

echo Extracting: %ARCHIVE%
powershell -Command "Expand-Archive -Path '%ARCHIVE%' -DestinationPath '%SCRIPT_DIR%' -Force"

echo Done. Extracted to: %TARGET_DIR%
endlocal
