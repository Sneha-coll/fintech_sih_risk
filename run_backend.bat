@echo off
title Fintech AI Risk Backend Server
echo ===================================================
echo Starting Fintech AI Risk Engine Backend (Python)...
echo ===================================================
cd /d "%~dp0backend"

set "PY_CMD=python"
where python >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    if exist "%LOCALAPPDATA%\Programs\Python\Python312\python.exe" (
        set "PY_CMD=%LOCALAPPDATA%\Programs\Python\Python312\python.exe"
    ) else if exist "%LOCALAPPDATA%\Programs\Python\Launcher\py.exe" (
        set "PY_CMD=%LOCALAPPDATA%\Programs\Python\Launcher\py.exe"
    )
)

"%PY_CMD%" server.py
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Failed to run Python backend.
    pause
)
