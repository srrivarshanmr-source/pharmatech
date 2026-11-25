@echo off
title PharmaTech Platform Runner
echo ===================================================================
echo   Starting PharmaTech - Medicine Availability Platform (Spring Boot)
echo ===================================================================
set "PROJECT_DIR=%~dp0"

rem Use bundled JDK/Maven if present, otherwise use system environment
if exist "%PROJECT_DIR%.tools\jdk17.0.21_0\bin\java.exe" (
    set "JAVA_HOME=%PROJECT_DIR%.tools\jdk17.0.21_0"
    set "PATH=%PROJECT_DIR%.tools\jdk17.0.21_0\bin;%PROJECT_DIR%.tools\apache-maven-3.9.6\bin;%PATH%"
)

echo Checking Java version...
java -version
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Java could not be started. Please ensure Java 17+ is installed.
    pause
    exit /b %ERRORLEVEL%
)

echo Starting PharmaTech application on http://localhost:8080 ...
cd /d "%PROJECT_DIR%"
if exist "%PROJECT_DIR%target\pharmatech-platform-1.0.0-SNAPSHOT.jar" (
    echo Launching pre-built application JAR...
    java -jar "%PROJECT_DIR%target\pharmatech-platform-1.0.0-SNAPSHOT.jar"
) else (
    echo Building and launching via Maven...
    mvn spring-boot:run -DskipTests
)
pause
