# Release v1.0.29 - 2026-02-25

## What's New / 更新內容

### English

- [Feature] XP/Level gamification system — entities earn XP by completing TODOs, display level progress bar on entity cards (Android + Web Portal)
- [Feature] Unified Info Hub — embedded User Guide, FAQ, Release Notes & Comparison pages in a single tabbed interface
- [Feature] E-Claw vs Telegram comparison — 12-category side-by-side feature comparison with 3-column card layout and QR codes
- [Feature] Collapsible notification preferences section in Android settings
- [Fix] GitHub Actions CI — repaired all workflow failures
- [Fix] Debug/test devices can now use all 8 entity slots
- [Fix] Dark theme for notification/language settings cards and API field name mismatch
- [Fix] Auth redirect loop caused by /api/notifications/count returning 401
- [Improve] Settings page UX polish and footer description update
- [Improve] Domain migration from eclaw.up.railway.app to eclawbot.com

### 繁體中文

- [新功能] 經驗等級系統 — 實體完成 TODO 任務可獲得經驗值，等級進度條顯示於實體卡片（Android + Web Portal）
- [新功能] 統一資訊中心 — 整合使用指南、常見問題、更新日誌與比較頁面於單一分頁介面
- [新功能] E-Claw vs Telegram 比較 — 12 類功能並排比較，三欄卡片式排版，附 QR Code
- [新功能] 通知偏好設定可收合區塊（Android 設定頁面）
- [修復] 修復所有 GitHub Actions CI 流程失敗
- [修復] 除錯/測試裝置現可使用全部 8 個實體欄位
- [修復] 通知/語言設定卡片深色主題及 API 欄位名稱不匹配
- [修復] 通知計數 API 回傳 401 導致的認證重導迴圈
- [改進] 設定頁面 UX 優化及頁尾描述更新
- [改進] 網域遷移至 eclawbot.com

## Technical Changes

- Backend: Added `xp` and `level` columns to `entities` table, `awardEntityXP()` helper, mission.js TODO→DONE XP hook
- Backend: Domain migration to eclawbot.com, auth redirect loop fix
- Android: Entity XP bar in `EntityCardAdapter`, collapsible notification preferences, dark theme fixes
- Web Portal: XP progress bar on dashboard entity cards, unified Info Hub, comparison page redesign
- CI/CD: Repaired GitHub Actions workflows
- Bot Skill Doc: Added XP/Level system documentation for bot awareness
