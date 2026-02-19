# Release v1.0.14 - 2026-02-19

## What's New / 更新內容

### English
- [Feature] Server logs: new `server_logs` table + `/api/logs` endpoint for remote debugging with category/level filters
- [Fix] Broadcast `delivered_to` overwrite bug — previously only the last recipient was recorded
- [Fix] Prevent entity message echo by deduplicating bot chat messages
- [Feature] Add CLAUDE.md for Claude Code cloud sessions
- [Test] New regression test for broadcast delivery tracking (in broadcast test suite)
- [Test] New regression test for entity message echo (7 scenarios, 19 assertions)

### 繁體中文
- [新功能] 伺服器日誌：新增 `server_logs` 資料表 + `/api/logs` 端點，支援分類/等級篩選的遠端除錯
- [修復] 廣播 `delivered_to` 覆寫錯誤 — 先前僅記錄最後一位接收者
- [修復] 防止實體訊息回音，透過去重複機制過濾機器人聊天訊息
- [新功能] 新增 CLAUDE.md 供 Claude Code 雲端環境使用
- [測試] 新增廣播送達追蹤回歸測試
- [測試] 新增實體訊息回音回歸測試（7 場景、19 斷言）

## Commits (since v1.0.13)
- 345f9a4 docs: add CLAUDE.md for Claude Code cloud sessions
- 1b8e969 feat: add server_logs table + /api/logs endpoint for remote debugging
- ba76c2b fix: broadcast delivered_to overwrite bug + add broadcast regression test
- b368b7f Merge pull request #6
- 7ee269f test: add regression test for entity message echo (7 scenarios, 19 assertions)
- 625a54b Merge pull request #5
- 6931d72 fix: prevent entity message echo by deduplicating bot chat messages
- d924243 Release v1.0.13

## Technical Changes
- Backend: `db.js` - new `server_logs` table with PostgreSQL migration
- Backend: `index.js` - `/api/logs` endpoint with auth, category/level/device filters, 500-entry limit
- Backend: `index.js` - fix broadcast `delivered_to` array accumulation (was being overwritten per recipient)
- Backend: `index.js` - deduplicate bot chat messages to prevent entity message echo
- Backend: `tests/test_broadcast.js` - added delivered_to verification assertions
- Backend: `tests/test_entity_echo.js` - new test file (7 scenarios, 19 assertions)
- Android: version bump 1.0.13 → 1.0.14 (versionCode 15 → 16)
