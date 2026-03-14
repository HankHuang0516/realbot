@echo off
REM ──────────────────────────────────────────────
REM EClaw — Auto-Start Claude Code Remote Session
REM ──────────────────────────────────────────────
REM Quick setup:
REM   1. Win+R → shell:startup → Enter
REM   2. Copy this .bat file (or its shortcut) into that folder
REM   3. Edit PROJECT_PATH below to match your EClaw repo location
REM ──────────────────────────────────────────────

title EClaw Claude Remote

SET PROJECT_PATH=C:\Users\%USERNAME%\EClaw
SET DELAY_SECONDS=5

REM Wait for system to settle
timeout /t %DELAY_SECONDS% /nobreak >nul

REM Check if claude CLI exists
where claude >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] 'claude' CLI not found in PATH.
    echo Install: npm install -g @anthropic-ai/claude-code
    pause
    exit /b 1
)

REM Change to project directory
if exist "%PROJECT_PATH%" (
    cd /d "%PROJECT_PATH%"
    echo Working directory: %PROJECT_PATH%
) else (
    echo [WARNING] Project path not found: %PROJECT_PATH%
    echo Starting claude in current directory.
)

echo.
echo ========================================
echo   EClaw Claude Code Remote Session
echo ========================================
echo.

claude
