# Release v1.0.11 - 2026-02-18

## What's New / 更新內容

### English
- [Feature] 3 new regression tests: multi-entity chat monitoring, usage limit validation, Mission Control incremental publish
- [Feature] `isTestDevice` flag on device registration for accurate test device cleanup
- [Improve] Regression suite now covers 10 tests (up from 7)

### 繁體中文
- [新功能] 新增 3 個回歸測試：多實體聊天監控、使用量限制驗證、任務中心增量發布
- [新功能] 裝置註冊時新增 `isTestDevice` 標記，精準清理測試裝置
- [改進] 回歸測試套件現涵蓋 10 個測試（原為 7 個）

## Technical Changes
- Backend: `test_chat_monitoring.js` - speak-to, broadcast, dedup, rate limit, error handling across 2-4 entities
- Backend: `test_usage_limit.js` - 15-message free tier limit, 429 response, premium bypass check
- Backend: `test_mission_publish.js` - TODO/RULE CRUD, delta notification, version increment, bot auth
- Backend: `run_all_tests.js` - added 3 new tests to regression runner (10 total)
- Backend: `index.js` - `isTestDevice` flag stored on register, admin cleanup filters test devices
- Android: version bump 1.0.10 → 1.0.11 (versionCode 12 → 13)
