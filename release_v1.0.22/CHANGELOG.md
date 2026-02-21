# Release v1.0.22 - 2026-02-21

## What's New / 更新內容

### English
- [Feature] File management page for Web portal and Android app
- [Feature] Scheduling feature for timed message delivery (cron-based)
- [Fix] Prevent entity cards from disappearing + improve logging for #16
- [Fix] Compile fixes: drawable reference, DeviceManager property access, TelemetryHelper signatures

### 繁體中文
- [新功能] Web 入口與 Android App 的檔案管理頁面
- [新功能] 排程功能，支援定時訊息發送（cron 排程）
- [修復] 防止實體卡片消失 + 改善日誌記錄 (#16)
- [修復] 編譯修正：drawable 引用、DeviceManager 屬性存取、TelemetryHelper 簽名

## Commits (since v1.0.21)
- 3348f6c Merge pull request #25
- 7199403 merge: resolve i18n.js conflict
- 88330cd feat: add scheduling feature for timed message delivery
- 3cf778c Merge pull request #24
- 2435289 feat: add file management page to web portal and Android app
- 70cfd2e Merge pull request #23
- 970fcd6 fix: prevent entity cards from disappearing + improve logging for #16

## Technical Changes
- Backend: `scheduler.js` - New 353-line cron-based scheduler module
- Backend: `index.js` - Schedule CRUD endpoints, file management endpoints (+309 lines)
- Backend: `public/portal/schedule.html` - Schedule management page (853 lines)
- Backend: `public/portal/files.html` - File management page (676 lines)
- Backend: `public/shared/i18n.js` - Schedule + file i18n strings
- Backend: `package.json` - Added `node-cron` dependency
- Android: `ScheduleActivity.kt` - Schedule management (304 lines)
- Android: `FileManagerActivity.kt` - File manager (361 lines)
- Android: `ScheduleAdapter.kt` + `FileCardAdapter.kt` - List adapters
- Android: `ScheduleModels.kt` - Schedule data models
- Android: New layouts: `activity_schedule.xml`, `activity_file_manager.xml`, `dialog_create_schedule.xml`, `item_schedule.xml`, `item_file_card.xml`
- Android: version bump 1.0.21 → 1.0.22 (versionCode 23 → 24)
