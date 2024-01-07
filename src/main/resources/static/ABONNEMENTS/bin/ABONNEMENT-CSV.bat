@echo off
SET abonnementDate=%1

IF "%abonnementDate%"=="" (
    echo Please provide a abonnementDate parameter in the format yyyy-MM-dd
    exit /b
)

curl -X GET "http://localhost:9002/api/v1/abonnements/csv?abonnementDate=%abonnementDate%" -H "accept: application/json"

pause
