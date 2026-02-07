# Release v1.0.4 - 2026-02-08

## What's New / 更新內容

### English
- [Feature] Real battery level reporting - Android now sends actual device battery percentage to backend
- [Feature] Device-side entity removal endpoint for cleanup
- [Feature] Bot-to-all broadcast endpoint (/api/entity/broadcast)
- [Feature] Real-time app version tracking in lastLoadCurrentLatestVersion

### 繁體中文
- [新功能] 真實電量回報 - Android 現在會將真實裝置電量傳送到後端
- [新功能] 裝置端實體移除端點，方便清理
- [新功能] Bot 對所有實體廣播端點 (/api/entity/broadcast)
- [新功能] 即時 App 版本追蹤功能

## Technical Changes
- **Backend**: Added `POST /api/device/battery` endpoint
- **Backend**: Removed simulated battery decay (was -1 every 5 seconds)
- **Backend**: Added `/api/entity` DELETE endpoint
- **Backend**: Added `/api/entity/broadcast` POST endpoint
- **Android**: Created `BatteryMonitor.kt` to listen for `ACTION_BATTERY_CHANGED`
- **Android**: Updated `ClawApiService.kt`, `StateRepository.kt`, `MainActivity.kt`
- **Android**: Version bump 1.0.3 → 1.0.4 (versionCode 4 → 5)
