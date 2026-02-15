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

### Phase 6: Testing & Release
- [x] Test free tier limit enforcement
- [x] Generate signed AAB
- [x] Create Release Workflow
- [ ] Upload AAB to Google Play Console
- [X] Create subscription product
- [ ] Add license testers
- [ ] Test subscription purchase flow

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

<<<<<<< HEAD
#### 🔴 **BUG - Push Response Visibility** (High Priority)
- [ ] **Push Response Not Visible**: User cannot see push response messages on phone (使用者看不到推播回應訊息)
  - **Symptom**: Bot sends response via webhook, but message doesn't appear on phone UI
  - **Impact**: Poor user experience, user thinks bot is not responding
  - **Investigation needed**: Check App message rendering logic

=======
<<<<<<< HEAD
>>>>>>> main
#### OpenClaw 端 (Bot Side)
- [ ] **Auto Webhook Registration**: Automatically register webhook after binding (Webhook 自動註冊: 綁定後自動完成)
- [ ] **Auto State Management**: Implement standard flow: receive message → BUSY → process → IDLE (狀態自動管理: 收到訊息→BUSY→處理→IDLE 標準流程)
- [ ] **Message Parsing Standardization**: Add regex patterns to help bots parse messages (訊息解析標準化: 加入 regex pattern 方便 bot 解析)
- [ ] **Entity Communication Optimization** (High Priority): `/api/entity/speak-to` response too slow (20s+ timeout) (Entity 通訊優化 (高優先): 響應太慢，常 20s+ timeout)
- [ ] **SKILL Documentation**: Add more examples and best practices (SKILL 文件完善: 加入更多範例和最佳實踐)

!!!![Bug]!!!! 極高優先權

### 🔴 Bug: Push Response Not Visible (高優先權)
- **Issue**: E-Claw App displays raw JSON webhook response instead of parsed message
- **Root Cause**: App shows `[Push] Response: {"ok":true,"result":{"content":[...]}}` raw JSON
- **Expected**: Extract and display `text` field from sessions_send response
- **Example of current behavior**:
  ```
  [Push] Response: {"ok":true,"result":{"content":[{"type":"text","text":"🌊 廣播回報..."}]}}
  ```
- **Expected behavior**: Display just the message text
- **Status**: Backend sends correct format, App needs to parse webhook response

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
=======
#### OpenClaw Bot Side
- [x] Auto Webhook Registration
- [x] Auto State Management
- [x] Message Parsing Standardization
- [x] Entity Communication (Fixed: entity message updates)
- [ ] SKILL Documentation
>>>>>>> origin/main
