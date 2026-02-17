# Task: Implement Subscription Mechanism (實作訂閱機制)

## Overview (總覽)
Add a free tier + subscription mechanism to the Claw Live app.

## Tasks (任務清單)

### Phase 1: Planning (階段 1: 規劃)
- [x] Research project structure
- [x] Understand message flow
- [x] Create implementation plan
- [x] Get user approval

### Phase 2: Google Play Billing Integration
- [x] Add Google Play Billing Library dependency
- [x] Create BillingManager.kt
- [X] Create subscription product on Play Console

### Phase 3: Usage Tracking
- [x] Create UsageManager.kt
- [x] Add usage tracking in message send flow
- [x] Add usage tracking in message receive flow
- [x] Implement daily reset logic

### Phase 4: UI Implementation
- [x] Create SettingsActivity.kt
- [x] Move entity layout options to Settings
- [x] Add Settings button
- [x] Create subscribe button

### Phase 6: Testing & Release (階段 6: 測試與發布)
- [x] Test free tier limit enforcement (15 sends/day) (測試免費層級限制 (15 則/日))
- [x] Generate signed AAB for Google Play Console (生成 Google Play Console 用簽署 AAB)
- [x] Create Release Workflow (associated with `/release` command) (建立發布工作流程 (關聯 `/release` 指令))
- [ ] Upload AAB to Google Play Console (Internal Testing) (上傳 AAB 至 Google Play Console (內部測試))
- [X] Create subscription product `e_claw_premium` (建立訂閱產品 `e_claw_premium`)
- [ ] Add license testers (新增授權測試人員)
- [ ] Test subscription purchase flow (測試訂閱購買流程)

### Phase 7: Roadmap & Optimization
- [x] Data Security Check
    - [x] Backend Math.random() replaced
    - [x] Enable Minification (ProGuard)
    - [x] Add Network Security Config

- [x] Documentation: Translate to English

- [ ] UI: Optimize entity appearance
- [ ] AI Behavior: Allow bot to decide entity movement

### Phase 8: Multi-Entity Architecture Optimization

#### Device & Backend
- [ ] Remove Battery Info
- [ ] Dynamic Entity Slots

#### OpenClaw 端 (Bot Side)
- [ ] **Auto Webhook Registration**: Automatically register webhook after binding (Webhook 自動註冊: 綁定後自動完成)
- [ ] **Auto State Management**: Implement standard flow: receive message → BUSY → process → IDLE (狀態自動管理: 收到訊息→BUSY→處理→IDLE 標準流程)
- [ ] **Message Parsing Standardization**: Add regex patterns to help bots parse messages (訊息解析標準化: 加入 regex pattern 方便 bot 解析)
- [ ] **Entity Communication Optimization** (High Priority): `/api/entity/speak-to` response too slow (20s+ timeout) (Entity 通訊優化 (高優先): 響應太慢，常 20s+ timeout)
- [ ] **SKILL Documentation**: Add more examples and best practices (SKILL 文件完善: 加入更多範例和最佳實踐)

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
