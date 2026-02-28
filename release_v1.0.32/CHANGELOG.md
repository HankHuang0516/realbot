# Release v1.0.32 - 2026-02-28

## What's New / 更新內容

### English

- [Feature] AI-powered binding troubleshooter — 3-stage diagnosis (rule engine → log correlation → Claude CLI proxy) for automatic bot connection issue resolution
- [Feature] AI Support Chat widget on admin dashboard for real-time admin assistance
- [Feature] WebSocket transport for OpenClaw gateways with SETUP_PASSWORD — separates Basic Auth and Bearer Token into different layers
- [Feature] XP system expanded with 8 new channels + message like/dislike UI reactions
- [Feature] Dashboard entity cards now support edit mode on Web portal (parity with Android)
- [Feature] Email notifications when feedback status changes
- [Feature] Webhook troubleshooting FAQ page with unified error messages
- [Feature] Chat integrity validation module (Android + Web) for detecting display/data mismatches
- [Improve] Claude CLI warmup to reduce cold start latency for AI support
- [Improve] Setup Password field in admin create bot dialog
- [Improve] Admin role awareness in AI support proxy
- [Fix] Usage limit now applies to all bound entities (not just free bots)
- [Fix] Telemetry middleware captures sub-router paths correctly (mission endpoints)
- [Fix] Telemetry duration=0 handling (minimum 1ms)
- [Fix] Web-sent messages now sync to Android chat + broadcast bubbles merged
- [Fix] Smart 401 handling — guides bot to retry with setup_password credentials
- [Fix] Entity card removal, copy button UX, loading UI improvements
- [Fix] message_reactions.message_id changed from INTEGER to UUID

### 繁體中文

- [新功能] AI 智能綁定診斷 — 三階段診斷（規則引擎 → 日誌關聯 → Claude CLI 代理）自動解決機器人連線問題
- [新功能] 管理面板新增 AI 支援聊天小工具，即時管理員協助
- [新功能] WebSocket 傳輸支援 SETUP_PASSWORD 的 OpenClaw 閘道器
- [新功能] XP 系統擴展至 8 個新頻道 + 訊息按讚/倒讚 UI
- [新功能] Web 入口網站的實體卡片新增編輯模式（與 Android 功能對齊）
- [新功能] 回饋狀態變更時發送電子郵件通知
- [新功能] Webhook 疑難排解 FAQ 頁面，統一錯誤訊息
- [新功能] 聊天完整性驗證模組（Android + Web）偵測顯示/資料不一致
- [改進] Claude CLI 預熱降低 AI 支援冷啟動延遲
- [改進] 管理員建立機器人對話框新增 Setup Password 欄位
- [改進] AI 支援代理增加管理員角色感知
- [修復] 使用量限制現在套用到所有綁定實體（不僅限免費機器人）
- [修復] 遙測中介軟體正確擷取子路由路徑（任務端點）
- [修復] 遙測 duration=0 處理（最低 1ms）
- [修復] Web 傳送的訊息現在同步到 Android 聊天 + 廣播氣泡合併
- [修復] 智慧 401 處理 — 引導機器人以 setup_password 憑證重試
- [修復] 實體卡片移除、複製按鈕 UX、載入 UI 改善
- [修復] message_reactions.message_id 從 INTEGER 改為 UUID

## Technical Changes

- Backend: AI support module (`ai-support.js`) with 3-stage diagnosis, Claude CLI proxy integration
- Backend: WebSocket connection pool (`wsConnectionPool`) for SETUP_PASSWORD gateways
- Backend: Chat integrity validation module (`chat-integrity.js`)
- Backend: Telemetry middleware rewrite for sub-router path capture and duration tracking
- Backend: Usage limit scope broadened to all bound entities (except personal bots)
- Backend: Health endpoint now includes `build` version for deployment tracking
- Android: `ChatIntegrityValidator.kt` for detecting display/data mismatches
- Android: Version bump 1.0.31 → 1.0.32 (versionCode 33 → 34)
- Web: Dashboard edit mode, AI support chat widget, webhook FAQ page
- Tests: 14 regression tests all passing, WebSocket auth test added
