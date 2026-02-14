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

#### OpenClaw Bot Side
- [x] Auto Webhook Registration
- [x] Auto State Management
- [x] Message Parsing Standardization
- [x] Entity Communication (Fixed: entity message updates)
- [ ] SKILL Documentation
