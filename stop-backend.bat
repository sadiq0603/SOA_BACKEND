@echo off
REM BiblioTech Microservices Shutdown Script
echo Stopping BiblioTech Backend Microservices...
powershell -ExecutionPolicy Bypass -File "%~dp0stop-backend.ps1"
pause
