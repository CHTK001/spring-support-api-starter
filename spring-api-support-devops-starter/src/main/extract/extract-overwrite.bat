@echo off
chcp 65001 >nul
REM 覆盖解压（不清空目录）
REM Usage: extract-overwrite.bat

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

echo Extracting (overwrite mode): %ARCHIVE%
powershell -Command "Expand-Archive -Path '%ARCHIVE%' -DestinationPath '%SCRIPT_DIR%' -Force"

echo Done. Extracted to: %TARGET_DIR%
endlocal
