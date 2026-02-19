# Release v1.0.15 - 2026-02-19

## What's New / 更新內容

### English
- [Fix] Chat echo prevention: add cross-source dedup in syncFromBackend() to prevent duplicate messages
- [Fix] Update LATEST_APP_VERSION constant from 1.0.3 to match actual release version
- [Feature] Add broadcast curl template to push notification format + skill doc update
- [Improve] Broadcast test uses polling for delivered_to instead of fixed 15s wait
- [Improve] Add all 14 tests to regression runner including credential-based tests
- [Fix] Entity name preservation test: fix stale botSecret after bind-free override (Scenario 13)
- [Fix] Test runner pattern matching: standardize pass output for entity name test

### 繁體中文
- [修復] 聊天回音防護：在 syncFromBackend() 新增跨來源去重複，防止訊息重複
- [修復] 更新 LATEST_APP_VERSION 常數，從 1.0.3 更正為實際發布版本
- [新功能] 推播通知格式加入廣播 curl 模板 + 技能文件更新
- [改進] 廣播測試改用輪詢 delivered_to，取代固定 15 秒等待
- [改進] 回歸測試執行器加入全部 14 個測試，含需要憑證的測試
- [修復] 實體名稱保存測試：修復 bind-free 覆蓋後過期的 botSecret（場景 13）
- [修復] 測試執行器模式比對：統一實體名稱測試的通過輸出格式

## Commits (since v1.0.14)
- e76938f fix: add cross-source dedup in syncFromBackend() to prevent chat echo
- 0871dd2 fix: update LATEST_APP_VERSION from 1.0.3 to 1.0.14
- 9eedb7c fix: broadcast test uses polling for delivered_to instead of fixed 15s wait
- b504886 feat: add broadcast curl template to push notification + skill doc
- f7c017b feat: add all 14 tests to regression runner incl. credential-based tests
- d268f67 Release v1.0.14

## Technical Changes
- Android: `StateRepository.kt` - cross-source dedup in syncFromBackend() prevents chat echo
- Backend: `index.js` - LATEST_APP_VERSION updated 1.0.3 → 1.0.15, broadcast curl template in push format
- Backend: `E-claw_mcp_skill.md` - updated version info examples
- Backend: `run_all_tests.js` - expanded to 14 tests including credential-based tests
- Backend: `tests/test-broadcast.js` - polling-based delivered_to verification
- Backend: `tests/test_entity_name_preservation.js` - fix Scenario 13 stale botSecret, standardize output
- Android: version bump 1.0.14 → 1.0.15 (versionCode 16 → 17)
