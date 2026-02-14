# Task: Implement Subscription Mechanism (實作訂閱機制)

## Overview (總覽)
Add a free tier + subscription mechanism to the Claw Live app. (為 Claw Live 應用程式新增免費用戶層級與訂閱機制。)
- **Free tier (免費層級)**: 25 messages per day (receive + send combined) (每日 25 則訊息，發送與接收合計)
- **Subscription tier (訂閱層級)**: Unlimited messages (無限訊息)

## Tasks (任務清單)

### Phase 1: Planning (階段 1: 規劃)
- [x] Research project structure (研究專案結構)
- [x] Understand message flow (API endpoints) (了解訊息流與 API 端點)
- [x] Create implementation plan (建立實作計畫)
- [x] Get user approval (取得用戶批准)

### Phase 2: Google Play Billing Integration (階段 2: Google Play 結帳整合)
- [x] Add Google Play Billing Library dependency (新增 Google Play Billing Library 依賴)
- [x] Create `BillingManager.kt` for subscription handling (建立 `BillingManager.kt` 處理訂閱)
- [X] Create subscription product ID on Play Console (manual) (在 Play Console 上建立訂閱產品 ID (手動))

### Phase 3: Usage Tracking (階段 3: 用量追蹤)
- [x] Create `UsageManager.kt` for tracking daily message count (建立 `UsageManager.kt` 追蹤每日訊息數)
- [x] Add usage tracking in message send flow (`MessageActivity.kt`) (在訊息發送流程新增用量追蹤)
- [x] Add usage tracking in message receive flow (`StateRepository.kt`) (在訊息接收流程新增用量追蹤)
- [x] Implement daily reset logic (實作每日重置邏輯)

### Phase 4: UI Implementation (階段 4: UI 實作)
- [x] Create `SettingsActivity.kt` with subscription/usage UI (建立包含訂閱/用量 UI 的 `SettingsActivity.kt`)
- [x] Move entity layout options to Settings (將實體佈局選項移至設定)
- [x] Add Settings button in MainActivity (replaced Debug/EntityManager buttons) (在 MainActivity 新增設定按鈕 (取代 Debug/EntityManager 按鈕))
- [x] Create subscribe button with Material Design (使用 Material Design 建立訂閱按鈕)

### Phase 6: Testing & Release (階段 6: 測試與發布)
- [x] Test free tier limit enforcement (15 sends/day) (測試免費層級限制 (15 則/日))
- [x] Generate signed AAB for Google Play Console (生成 Google Play Console 用簽署 AAB)
- [x] Create Release Workflow (associated with `/release` command) (建立發布工作流程 (關聯 `/release` 指令))
- [ ] Upload AAB to Google Play Console (Internal Testing) (上傳 AAB 至 Google Play Console (內部測試))
- [X] Create subscription product `e_claw_premium` (建立訂閱產品 `e_claw_premium`)
- [ ] Add license testers (新增授權測試人員)
- [ ] Test subscription purchase flow (測試訂閱購買流程)

### Phase 7: Roadmap & Optimization (Backlog) (階段 7: 路線圖與優化 (待辦))
- [x] **Data Security Check** (High Priority) (資料安全性檢查 (高優先)) - Report Generated [security_audit.md](security_audit.md)
    - [ ] **Fix**: Replace `Math.random()` in Backend (High Priority) (修復: 替換後端 `Math.random()` (高優先))
    - [ ] **Fix**: Enable Minification (ProGuard) in App (High Priority) (修復: 啟用 App 代碼混淆 (ProGuard) (高優先))
    - [ ] **Fix**: Add Network Security Config (High Priority) (修復: 新增網路安全配置 (高優先))

- [/] **Documentation**: Translate `backend/realbot_mcp_skill.md` to English (High Priority) (文件: 翻譯 `backend/realbot_mcp_skill.md` 為英文 (高優先))
    - *Rule*: Ensure `skills_documentation` in bot binding JSON is always English. (規則: 確保機器人綁定 JSON 中的 `skills_documentation` 始終為英文)

- [ ] **UI**: Optimize entity appearance (Medium Priority) (介面: 優化實體外觀 (中優先))
- [ ] **AI Behavior**: Allow bot to decide entity movement position (Medium Priority) (AI 行為: 允許機器人決定實體移動位置 (中優先))

### Phase 8: Multi-Entity Architecture Optimization (階段 8: 多實體架構優化)

#### 裝置端 & 後台 (Device & Backend)
- [ ] **Remove Battery Info**: Remove battery information feature (no practical use) (移除電量資訊功能 (沒實際用途))
- [ ] **Dynamic Entity Slots**: 4 entity slots may not be enough, support dynamic creation (動態 Entity 槽位: 4 個可能不夠，需支援動態建立)

#### OpenClaw 端 (Bot Side)
- [ ] **Auto Webhook Registration**: Automatically register webhook after binding (Webhook 自動註冊: 綁定後自動完成)
- [ ] **Auto State Management**: Implement standard flow: receive message → BUSY → process → IDLE (狀態自動管理: 收到訊息→BUSY→處理→IDLE 標準流程)
- [ ] **Message Parsing Standardization**: Add regex patterns to help bots parse messages (訊息解析標準化: 加入 regex pattern 方便 bot 解析)
- [ ] **Entity Communication Optimization** (High Priority): `/api/entity/speak-to` response too slow (20s+ timeout) (Entity 通訊優化 (高優先): 響應太慢，常 20s+ timeout)
- [ ] **SKILL Documentation**: Add more examples and best practices (SKILL 文件完善: 加入更多範例和最佳實踐)

!!!![Bug]!!!! 極高優先權

### Phase 9: AI Resource Usage (階段 9: AI 資源用量)
- [ ] **Status Bar Integration**: Integrate AI remaining usage into the status bar (整合 AI 剩餘用量至狀態列)
    - **JSON Structure**:
      ```json
      "usage": {
        "percentage": 85,           // Standardized remaining percentage (0-100)
        "label": "85% 剩餘",        // Text to display in status bar
        "status": "NORMAL",         // Status: NORMAL (Green), WARNING (Yellow), CRITICAL (Red)
        "resetTime": "2026-02-11T14:00:00Z", // (Optional) Next reset time
        "description": "本小時剩餘 1.2k tokens" // (Optional) Detailed description
      }
      ```
    - **Logic**: Backend standardizes usage (OpenAI/Subscription/Local). App only reads `percentage` and `label`.

---

## 🚨 High Priority: Push Response Not Visible

### Issue
When Entity-to-Entity communication occurs (via `/api/entity/speak-to` or broadcast), the receiving Entity's device does not display the push notification/message on the Android app.

### Symptom
- User can send messages from phone to bot ✓
- Bot can reply via transform ✓
- Entity 1/2 can broadcast, but the phone doesn't show the message ❌

### Expected Behavior
- When Entity 0 broadcasts, all devices should show the broadcast message
- When Entity 0 speaks to Entity 1, Entity 1's device should display the message

### Root Cause Analysis Needed
- [ ] Check if webhook push is being sent correctly for entity-to-entity messages
- [ ] Verify the Android app handles entity-to-entity push notifications
- [ ] Check message format compatibility between webhook and app

### Priority: HIGH
