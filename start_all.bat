@echo off
title Fintech AI Risk System Launcher
echo ========================================================
echo Launching Fintech AI Risk Guardian (Backend + Frontend)
echo ========================================================
cd /d "%~dp0"

start "Fintech Backend (Python)" cmd /c "run_backend.bat"
timeout /t 2 /nobreak >nul
start "Fintech Frontend (Java)" cmd /c "run_frontend.bat"

