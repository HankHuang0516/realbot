# Release v1.0.50 - 2026-03-18

## What's New / 更新內容

### English
- [Fix] AI chat message loss and freeze on reopen (#248) — messages no longer disappear or cause UI freeze when reopening AI chat
- [Fix] Card Holder "My Cards" tab broken by duplicate variable declaration (#273)
- [Fix] Card Holder `onAuth()` replaced with correct `checkAuth()` call (#283)
- [Fix] Health Check auth and removed WordPress from scheduled tasks (#272)
- [Fix] Android CI: added FILES to NavItem enum (#281), resolved MissionViewModel pre-existing failures (#282)
- [Fix] Removed duplicate `filter_all` string resource in Android strings.xml (#280)

### 繁體中文
- [修復] AI 聊天訊息遺失與重開凍結問題 (#248) — 重新開啟 AI 聊天時訊息不再消失或畫面凍結
- [修復] 名片夾「我的名片」分頁因重複變數宣告而無法使用 (#273)
- [修復] 名片夾 `onAuth()` 替換為正確的 `checkAuth()` 呼叫 (#283)
- [修復] Health Check 認證修復，並從排程任務中移除 WordPress (#272)
- [修復] Android CI：NavItem 新增 FILES 列舉值 (#281)，修復 MissionViewModel 既有錯誤 (#282)
- [修復] 移除 Android strings.xml 中重複的 `filter_all` 字串資源 (#280)

## Technical Changes
- Backend: LATEST_APP_VERSION synced to 1.0.50, Health Check endpoint auth fix, WordPress schedule removal
- Android: versionCode 55, NavItem.FILES enum, MissionViewModel fix, duplicate string resource cleanup
- Web Portal: card-holder.html duplicate variable and auth function fixes
