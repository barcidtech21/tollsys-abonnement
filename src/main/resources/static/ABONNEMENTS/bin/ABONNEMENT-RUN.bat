@echo off

REM Check if port 9002 is in use
netstat -ano | findstr "0.0.0.0:9002" > nul

IF %ERRORLEVEL% EQU 0 (
    echo service Abonnement déjà démarré
) ELSE (
    REM Execute the JAR file
    java -jar ../lib/abonnement.jar --spring.config.location=%ABONNEMENT_DIR%/conf/application.properties
)

pause
