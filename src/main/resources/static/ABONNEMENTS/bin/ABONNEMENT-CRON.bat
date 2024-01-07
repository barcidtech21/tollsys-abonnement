@echo off
setlocal enabledelayedexpansion

REM -- Extract hours from the arguments
set "hours=%~1"
set "validHours="
set "cronExpression="

REM -- Replace commas with space and validate hours
for %%a in (%hours%) do (
    if %%a geq 0 if %%a leq 23 (
        set "validHours=!validHours!%%a,"
    ) else (
        echo Invalid hour: %%a
        exit /b 1
    )
)

REM -- Remove the trailing comma and construct the cron expression
set "validHours=!validHours:~0,-1!"
set "cronExpression=0 0 !validHours! * * ?"

REM -- Echo the cron expression (for debugging purposes)
echo Cron expression: !cronExpression!

REM -- Execute CURL command
curl -X GET  "http://localhost:9002/api/v1/abonnements/update-cron?hours=%hours%" -H "accept: application/json"

endlocal


