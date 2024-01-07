@echo off

REM Find the process ID using port 9002
FOR /F "tokens=5" %%i IN ('netstat -ano ^| findstr :9002') DO SET pid=%%i

REM Check if a PID was found
IF NOT DEFINED pid (
    echo No process found using port 9002.
    GOTO EndScript
)

REM Kill the process using the found PID
taskkill /F /PID %pid%
echo Process with PID %pid% using port 9002 has been killed.

:EndScript
pause
