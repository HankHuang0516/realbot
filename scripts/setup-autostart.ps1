# ──────────────────────────────────────────────
# EClaw — One-Click Autostart Installer
# ──────────────────────────────────────────────
# Run this script once to register eclaw-claude-remote.bat
# in Windows Startup folder. It will auto-launch on every login.
#
# Usage: Right-click → Run with PowerShell
#        OR: powershell -ExecutionPolicy Bypass -File setup-autostart.ps1
# ──────────────────────────────────────────────

$ErrorActionPreference = "Stop"

$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Definition
$BatFile = Join-Path $ScriptDir "eclaw-claude-remote.bat"
$StartupFolder = [Environment]::GetFolderPath("Startup")
$ShortcutPath = Join-Path $StartupFolder "EClaw Claude Remote.lnk"

# Verify bat file exists
if (-not (Test-Path $BatFile)) {
    Write-Host "ERROR: eclaw-claude-remote.bat not found at: $BatFile" -ForegroundColor Red
    Read-Host "Press Enter to exit"
    exit 1
}

# Create shortcut in Startup folder
$WshShell = New-Object -ComObject WScript.Shell
$Shortcut = $WshShell.CreateShortcut($ShortcutPath)
$Shortcut.TargetPath = $BatFile
$Shortcut.WorkingDirectory = $ScriptDir
$Shortcut.Description = "Auto-start EClaw Claude Code Remote Session"
$Shortcut.Save()

Write-Host ""
Write-Host "Autostart registered!" -ForegroundColor Green
Write-Host ""
Write-Host "  Shortcut: $ShortcutPath" -ForegroundColor Cyan
Write-Host "  Target:   $BatFile" -ForegroundColor Cyan
Write-Host ""
Write-Host "Claude Code will auto-launch on next Windows login." -ForegroundColor Green
Write-Host ""
Write-Host "To remove: delete the shortcut from:" -ForegroundColor Yellow
Write-Host "  $StartupFolder" -ForegroundColor Yellow
Write-Host ""
Read-Host "Press Enter to exit"
