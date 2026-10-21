@echo off
title Fintech AI Risk Frontend GUI
echo ===================================================
echo Building and Running Java Frontend...
echo ===================================================
cd /d "%~dp0frontend"
if not exist "bin" mkdir "bin"

set "JAVAC_CMD=javac"
set "JAVA_CMD=java"

where javac >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    if exist "C:\Program Files\Eclipse Adoptium\jdk-21.0.6.7-hotspot\bin\javac.exe" (
        set "JAVAC_CMD=C:\Program Files\Eclipse Adoptium\jdk-21.0.6.7-hotspot\bin\javac.exe"
        set "JAVA_CMD=C:\Program Files\Eclipse Adoptium\jdk-21.0.6.7-hotspot\bin\java.exe"
    )
)

echo Compiling Java source files...
"%JAVAC_CMD%" -d bin src\ApiClient.java src\FintechApp.java
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Java compilation failed.
    pause
    exit /b %ERRORLEVEL%
)

echo Launching Fintech Desktop UI...
"%JAVA_CMD%" -cp bin FintechApp
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Failed to run Java application.
    pause
)
