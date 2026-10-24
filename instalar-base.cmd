@echo off
rem crea la base local de KOLAB: doble clic y responder lo que pregunta
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0scripts\crear-base-local.ps1"
echo.
pause
