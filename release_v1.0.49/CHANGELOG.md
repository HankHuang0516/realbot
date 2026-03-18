# Release v1.0.49 - 2026-03-18

## What's New / 更新內容

### English
- [Feature] Card Holder Redesign — 3 sections (My Cards, Recent, Collected), block/unblock contacts, chat history modal, unified search across all platforms (Web, Android, iOS)
- [Feature] SEO Rebrand to EClawbot — new landing page, llms.txt, OG image for better AI search visibility
- [Feature] AI Chat ViewModel refactor — AiChatBottomSheet now uses AiChatViewModel for cleaner architecture
- [Feature] UX validation workflow — 3-layer automated UX testing (static audit, parity check, live validation)
- [Feature] 115 new Jest tests covering auth, subscription, official-borrow, device-preferences, publisher
- [Fix] Kotlin compile errors resolved for v1.0.48 release
- [Fix] Scheduled messages not showing in Android app chat
- [Fix] Race condition, NPE, and file deletion issues (7 open issues resolved)
- [Fix] Card Holder i18n translations for all 8 languages
- [Fix] delete-account.html and screen-control.html telemetry paths
- [Fix] Publisher Jest test env var isolation
- [Fix] Portal screen-control auth regression (deviceSecret vs botSecret)
- [Fix] Chat input text contrast (white-on-light-gray)
- [Fix] Missing nav_card_holder i18n key
- [Improve] Bot audit closed-loop with github-issue and audit-log endpoints
- [Improve] Publisher health check, rate limiter, better error messages
- [Improve] i18n for OpenClaw usecase examples
- [Improve] Cache-busting for i18n.js in info.html

### 繁體中文
- [新功能] 名片夾改版 — 三區塊設計（我的名片、最近互動、收藏），封鎖/解封聯絡人，聊天記錄彈窗，統一搜尋（Web、Android、iOS 三平台同步）
- [新功能] SEO 品牌重塑為 EClawbot — 新 Landing Page、llms.txt、OG 圖片，提升 AI 搜尋能見度
- [新功能] AI 聊天 ViewModel 重構 — AiChatBottomSheet 改用 AiChatViewModel 架構
- [新功能] UX 驗證工作流 — 三層自動化 UX 測試（靜態審計、平台一致性、線上驗證）
- [新功能] 新增 115 個 Jest 測試（auth、subscription、official-borrow、device-preferences、publisher）
- [修復] 解決 Kotlin 編譯錯誤
- [修復] 排程訊息無法在 Android 聊天中顯示
- [修復] 競態條件、NPE、檔案刪除等 7 個問題
- [修復] 名片夾 8 語言 i18n 翻譯
- [修復] delete-account.html 和 screen-control.html 遙測路徑
- [修復] Publisher Jest 測試環境變數隔離
- [修復] Portal screen-control 認證回歸（deviceSecret vs botSecret）
- [修復] 聊天輸入文字對比度（白字配淺灰背景）
- [修復] 缺少 nav_card_holder i18n key
- [改進] Bot 審計閉環（github-issue + audit-log 端點）
- [改進] Publisher 健康檢查、速率限制、錯誤訊息優化
- [改進] OpenClaw 使用案例 i18n 國際化
- [改進] info.html 的 i18n.js 快取清除

## Technical Changes
- Backend: Card Holder redesign APIs (my-cards, recent, history-by-code, block/unblock), publisher enhancements, 115 new Jest tests, bot audit endpoints
- Android: CardHolderActivity complete rewrite, AiChatViewModel refactor, Kotlin compile fixes, bottom nav Cards tab
- iOS: New Cards tab, cards.tsx screen, API service updates, i18n for 8 languages
- Web Portal: card-holder.html rewrite, nav.js update, release-notes.html, i18n additions
- Tests: 19 new Jest card-holder tests, integration test for redesign APIs, UX validation workflow
