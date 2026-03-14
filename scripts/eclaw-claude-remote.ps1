# ──────────────────────────────────────────────
# EClaw — Auto-Start Claude Code Remote Session
# ──────────────────────────────────────────────
# Place this script's shortcut in shell:startup to auto-run on boot.
# Setup:
#   1. Open Run dialog (Win+R) → type: shell:startup → Enter
#   2. Right-click → New → Shortcut
#   3. Target: powershell.exe -ExecutionPolicy Bypass -File "C:\path\to\EClaw\scripts\eclaw-claude-remote.ps1"
#   4. Done! It will auto-launch on next login.
# ──────────────────────────────────────────────

$ErrorActionPreference = "Stop"

# ── Configuration ──
$ProjectPath = "C:\Users\$env:USERNAME\EClaw"   # Adjust to your EClaw repo path
$DelaySeconds = 5                                 # Wait for network/desktop to be ready
$WindowTitle = "EClaw Claude Remote"

# ── Wait for system to settle ──
Start-Sleep -Seconds $DelaySeconds

# ── Verify claude CLI exists ──
$claudePath = Get-Command claude -ErrorAction SilentlyContinue
if (-not $claudePath) {
    Write-Host "ERROR: 'claude' CLI not found in PATH." -ForegroundColor Red
    Write-Host "Install Claude Code: npm install -g @anthropic-ai/claude-code" -ForegroundColor Yellow
    Write-Host ""
    Read-Host "Press Enter to exit"
    exit 1
}

# ── Change to project directory ──
if (Test-Path $ProjectPath) {
    Set-Location $ProjectPath
    Write-Host "Working directory: $ProjectPath" -ForegroundColor Cyan
} else {
    Write-Host "WARNING: Project path not found: $ProjectPath" -ForegroundColor Yellow
    Write-Host "Starting claude in current directory instead." -ForegroundColor Yellow
}

# ── Set window title ──
$Host.UI.RawUI.WindowTitle = $WindowTitle

# ── Launch Claude Code ──
Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "  EClaw Claude Code Remote Session" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""

claude
