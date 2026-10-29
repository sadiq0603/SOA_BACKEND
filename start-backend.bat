@echo off
REM BiblioTech Microservices Startup Script
echo Starting BiblioTech Backend Microservices...
powershell -ExecutionPolicy Bypass -File "%~dp0start-backend.ps1"
pause
