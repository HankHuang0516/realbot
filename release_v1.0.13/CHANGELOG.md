# Release v1.0.13 - 2026-02-19

## What's New / 更新內容

### English
- [Fix] Keyboard covers chat input on Pixel 9a / Android 15 edge-to-edge mode
- [Fix] Entity name preserved across unbind/rebind operations (no longer lost)
- [Fix] Usage counter inflation: prevent double-counting and sync client-server usage
- [Fix] Extend idle timeout from 20s to 5min so bot replies are not overwritten
- [Improve] Restructure push notifications for 90%+ API usage rate
- [Improve] Sync subscription status between Google Play and server to lift usage limits
- [Test] Entity name preservation verification tests (13 scenarios, 49 assertions)
- [Test] Automated bot API response rate regression test

### 繁體中文
- [修復] Pixel 9a / Android 15 全螢幕模式下鍵盤遮擋聊天輸入框
- [修復] 實體名稱在解綁/重新綁定後不再遺失
- [修復] 使用量計數器重複計算問題，同步客戶端與伺服器使用量
- [修復] 閒置超時從 20 秒延長至 5 分鐘，避免覆蓋機器人回覆
- [改進] 重構推送通知架構，API 使用率達 90% 以上
- [改進] Google Play 訂閱狀態同步到伺服器，解除使用限制
- [測試] 實體名稱保留驗證測試（13 場景、49 斷言）
- [測試] 自動化機器人 API 回應率回歸測試

## Technical Changes
- Android: `ChatActivity.kt` - fix edge-to-edge keyboard inset handling for Android 15
- Android: `BillingManager.kt` - sync subscription with server after Google Play purchase
- Android: `UsageManager.kt` - prevent usage counter inflation, client-server sync
- Android: `ClawApiService.kt` - add `syncSubscription()` endpoint
- Backend: `index.js` - extend idle timeout 20s→5min, restructure push notifications
- Backend: `subscription.js` - fix usage counter double-counting, add sync endpoint
- Backend: `package-lock.json` - dependency update
- Tests: `test_entity_name_preservation.js` - 13 scenarios, 49 assertions for name persistence
- Tests: automated bot API response rate regression test
