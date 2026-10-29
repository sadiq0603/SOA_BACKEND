@echo off
REM BiblioTech Health & Eureka Registry Check
echo Checking BiblioTech Backend Microservices Health...
powershell -ExecutionPolicy Bypass -File "%~dp0check-backend.ps1"
pause
