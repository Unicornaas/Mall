@echo off
setlocal
cd /d "%~dp0"

powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0start-all.ps1"
set "MALL_EXIT_CODE=%ERRORLEVEL%"

echo.
if not "%MALL_EXIT_CODE%"=="0" (
    echo Startup was not fully successful. Check the log path shown above.
)
echo Press any key to close this window. The started services will keep running.
pause >nul
exit /b %MALL_EXIT_CODE%
