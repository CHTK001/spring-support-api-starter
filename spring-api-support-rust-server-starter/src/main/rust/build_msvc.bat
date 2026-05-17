@echo off
cd /d "h:\workspace\2\spring-support-api-parent\spring-api-support-rust-server-starter\src\main\rust"
call "C:\Program Files (x86)\Microsoft Visual Studio\2019\BuildTools\VC\Auxiliary\Build\vcvars64.bat" > nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo vcvars64.bat failed
    exit /b 1
)
:: Add missing Windows SDK um\x64 path where kernel32.Lib resides
set "LIB=%LIB%;C:\Program Files (x86)\Windows Kits\10\lib\10.0.19041.0\um\x64"
:: Remove Git Bash's /usr/bin from PATH to avoid link.exe shadowing
set "PATH=C:\Program Files (x86)\Microsoft Visual Studio\2019\BuildTools\VC\Tools\MSVC\14.29.30133\bin\Hostx64\x64;%PATH%"
echo LIB=%LIB%
echo Linker:
where link.exe
echo Building...
C:\Users\yemen\.cargo\bin\cargo.exe build --release %*
if %ERRORLEVEL% NEQ 0 (
    echo cargo build failed with error code %ERRORLEVEL%
    exit /b %ERRORLEVEL%
)
echo Build successful!
