@echo off
setlocal
cd /d "%~dp0"

echo [NearNest] Checking for code changes in the last 20 days...
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0update-apk.ps1" %*

if %ERRORLEVEL% equ 0 (
    echo.
    echo [NearNest] Finished successfully.
) else (
    echo.
    echo [NearNest] Build failed with exit code %ERRORLEVEL%.
)

