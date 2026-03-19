# Debug Log Viewer — Implementation Plan

## Goal
Add an in-app debug log viewer page accessible only to admin/developer users from the Settings page.

## Current State
- `FileTimberTree.kt` already writes all Timber logs to `debug_log.txt` (512KB ring buffer)
- `CrashLogViewerActivity.kt` exists as a UI template for viewing logs
- Backend RBAC has admin/developer/operator/viewer roles in `user_roles` table
- `GET /api/auth/bind-email/status` does NOT return roles (only email/OAuth status)
- Android app has no role awareness

## Changes

### 1. Backend: Add `roles` to bind-email/status response
**File**: `backend/auth.js` — `GET /api/auth/bind-email/status` (~line 919-974)
- After finding the user account, query `user_roles JOIN roles` for that user
- Add `roles: ['admin', 'developer', ...]` array to the response JSON
- Zero-risk: additive field, no existing clients break

### 2. Android: Update API model
**File**: `ClawApiService.kt`
- Add `val roles: List<String>? = null` to `BindEmailStatusResponse`

### 3. Android: Store roles in DeviceManager
**File**: `DeviceManager.kt`
- Add `getRoles()` / `setRoles()` using SharedPreferences (comma-separated string)
- Add `isDeveloperOrAdmin(): Boolean` helper

### 4. Android: Create DebugLogViewerActivity
**New file**: `DebugLogViewerActivity.kt`
- Programmatic UI (matching CrashLogViewerActivity style — dark Material 3)
- Load `FileTimberTree.instance.getRecentLines(500)`
- Display in scrollable monospace TextView with color-coded levels
- Filter chips: ALL / DEBUG / INFO / WARN / ERROR
- Action buttons: Refresh, Share, Clear
- Auto-scroll to bottom
- Telemetry: trackPageView("debug_logs")

### 5. Android: Add button to SettingsActivity
**File**: `SettingsActivity.kt`
- Add "Debug Logs" button next to existing "Crash Logs" button
- Visibility: `GONE` by default, set to `VISIBLE` when roles include "admin" or "developer"
- Check roles on `onResume()` after bind-email/status API call completes

### 6. Android: Register in AndroidManifest.xml
- Add `DebugLogViewerActivity` with `parentActivityName=".SettingsActivity"`

### 7. Android: String resources (8 languages)
Add strings for: title, empty state, filter labels, share/clear/refresh, clear confirmation

### 8. Jest test for roles in bind-email/status
- Verify the response includes `roles` field
