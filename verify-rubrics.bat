@echo off
REM BiblioTech Rubric Verification Script
echo Running BiblioTech Backend Rubrics Automated Verification...
powershell -ExecutionPolicy Bypass -File "%~dp0verify-rubrics.ps1"
pause
