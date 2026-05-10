@echo off
echo Killing PID 10760...
taskkill /F /PID 10760
echo Result: %errorlevel%
echo Killing PID 19796...
taskkill /F /PID 19796
echo Result: %errorlevel%
pause
