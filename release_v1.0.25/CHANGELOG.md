# Release v1.0.25 - 2026-02-22

## What's New / 更新內容

### English
- [Improve] Schedule dialog: replace cramped quick-time buttons with Material 3 Chips for better UX
- [Feature] Schedule: add Custom Cron expression support for repeat schedules (web portal parity)
- [Feature] Add Soul category to Mission Control for entity personality management
- [Feature] Add bottom navigation bar for quick access to Mission/Chat/Files/Wallpaper
- [Improve] Feedback: add recording indicator, auto-sync status with GitHub issue state
- [Fix] Entity visibility diagnosis logging + auto-sync fix (#48)
- [Fix] CI/CD improvements: Railway preview environments, PR write permissions

### 繁體中文
- [改進] 排程對話框：將擁擠的快捷時間按鈕替換為 Material 3 Chips，提升操作體驗
- [新功能] 排程：新增自訂 Cron 表達式支援，重複排程與網頁版同步
- [新功能] 任務控制中心新增「靈魂」分類，管理實體個性設定
- [新功能] 新增底部導航列，快速切換任務/聊天/檔案/桌布
- [改進] 意見回饋：新增錄製指示器，自動同步 GitHub Issue 狀態
- [修復] 實體可見性診斷日誌 + 自動同步修復 (#48)
- [修復] CI/CD 改善：Railway 預覽環境、PR 寫入權限

## Technical Changes
- Android: Schedule dialog restructured with ChipGroup + Cron input section
- Android: Added RecordingIndicatorHelper, FeedbackPreferences, bottom nav icons
- Android: Soul management (add_soul, soul_template strings + UI)
- Backend: LATEST_APP_VERSION synced to 1.0.25
- CI/CD: Railway preview deploy, JUnit/Gson test dependencies, ESLint fixes
