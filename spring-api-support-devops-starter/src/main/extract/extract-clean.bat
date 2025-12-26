@echo off
chcp 65001 >nul
REM 清空同名目录后解压
REM Usage: extract-clean.bat [--strip]
REM --strip: 跳过压缩包内的父目录，直接解压内容到当前目录

setlocal enabledelayedexpansion
set "SCRIPT_DIR=%~dp0"
set "ARCHIVE_NAME=${project.artifactId}-${project.version}"
set "STRIP_MODE=false"

REM 解析参数
:parse_args
if "%~1"=="" goto :end_parse
if "%~1"=="--strip" set "STRIP_MODE=true"
if "%~1"=="-s" set "STRIP_MODE=true"
shift
goto :parse_args
:end_parse

REM 查找压缩包
if exist "%SCRIPT_DIR%%ARCHIVE_NAME%.zip" (
    set "ARCHIVE=%SCRIPT_DIR%%ARCHIVE_NAME%.zip"
) else (
    echo Error: Archive not found: %ARCHIVE_NAME%.zip
    exit /b 1
)

if "%STRIP_MODE%"=="true" (
    set "TARGET_DIR=%SCRIPT_DIR%"
    echo Extracting ^(strip mode^): !ARCHIVE!
    set "TMP_DIR=%TEMP%\extract_tmp_%RANDOM%"
    mkdir "!TMP_DIR!"
    powershell -Command "Expand-Archive -Path '!ARCHIVE!' -DestinationPath '!TMP_DIR!' -Force"
    REM 移动父目录内的内容到目标目录
    for /d %%D in ("!TMP_DIR!\*") do (
        xcopy /e /y /q "%%D\*" "%SCRIPT_DIR%" >nul 2>&1
    )
    rmdir /s /q "!TMP_DIR!"
) else (
    set "TARGET_DIR=%SCRIPT_DIR%%ARCHIVE_NAME%"
    echo Cleaning directory: !TARGET_DIR!
    if exist "!TARGET_DIR!" (
        rmdir /s /q "!TARGET_DIR!"
    )
    echo Extracting: !ARCHIVE!
    powershell -Command "Expand-Archive -Path '!ARCHIVE!' -DestinationPath '%SCRIPT_DIR%' -Force"
)

echo Done. Extracted to: %TARGET_DIR%
endlocal
