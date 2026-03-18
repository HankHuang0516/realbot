# Test Coverage Gap Analysis — 2026-03-18

## Executive Summary

**297 total API routes** across all modules. **~65 routes covered by Jest** (22%), **~192 routes covered including integration tests** (~65%). This report identifies the highest-impact gaps and proposes concrete improvements.

---

## Current State

### Test Infrastructure

| Layer | Files | Runner | CI-Enforced |
|-------|-------|--------|-------------|
| Jest unit tests | 20 files in `tests/jest/` | `npm test` | ✅ Yes (backend-ci.yml) |
| Integration tests | 54 files in `tests/` | `run_all_tests.js` | ❌ No (manual only) |

### Key Weakness: No Coverage Reporting

- Jest config has no `collectCoverage` or `coverageThreshold`
- CI runs tests but doesn't measure or enforce coverage
- No visibility into line/branch/function coverage

---

## Coverage by Module

| Module | Routes | Tested | Coverage | Priority |
|--------|--------|--------|----------|----------|
| OAuth Server | 6 | 6 | 100% | — |
| A2A Compat | 4 | 4 | 100% | — |
| Official Borrow | 6 | 6 | 100% | — |
| Subscription | 5 | 5 | 100% | — |
| Channel API | 8 | 8 | 100% | — |
| Cross-Device Settings | 3 | 3 | 100% | — |
| Auth | 27 | 24 | 89% | Low |
| Gatekeeper | 3 | 2 | 67% | Low |
| Scheduler | 10 | 8 | 80% | Low |
| Feedback | 12 | 10 | 83% | Low |
| Notifications | 6 | 1 | 17% | **P1** |
| AI Support | 8 | 3 | 38% | **P1** |
| Bot Tools | 4 | 2 | 50% | **P2** |
| Mission Control | 28 | 14 | 50% | **P1** |
| Article Publisher | 54 | 14 | 26% | **P2** |
| Entity Management | 13 | 5 | 38% | **P0** |
| Chat & Files | 9 | 2 | 22% | **P1** |
| Admin Endpoints | 10 | 2 | 20% | **P2** |
| Templates (Skill/Soul/Rule) | 10 | 3 | 30% | **P2** |
| Device Core & Vars | 8 | 5 | 63% | Low |
| Bot Registration & Messaging | 6 | 2 | 33% | **P2** |
| Screen Control | 3 | 1 | 33% | Low |

---

## Top 10 Proposed Improvements

### 1. [P0] Entity Management Jest Tests

**Why**: Entity binding, reorder, rename, and delete are the platform's core operations. Currently **0 Jest tests** cover these — only integration tests exist.

**Routes to cover**:
- `POST /api/bind` — happy path (bind bot to entity slot)
- `POST /api/device/add-entity` — add new entity slot
- `DELETE /api/device/entity/:entityId/permanent` — permanent deletion
- `POST /api/device/reorder-entities` — reorder validation
- `PUT /api/device/entity/name` — rename validation
- `POST /api/entity/refresh` — cooldown enforcement

**Suggested file**: `tests/jest/entity-management.test.js`

**Estimated effort**: Medium (mock device + entity DB queries)

---

### 2. [P0] Enable Jest Coverage Reporting in CI

**Why**: Without coverage metrics, regressions in test quality go unnoticed. This is an infrastructure fix that magnifies the value of all other improvements.

**Changes needed**:
1. Add to `jest.config.js`:
```js
collectCoverage: true,
coverageDirectory: 'coverage',
coverageReporters: ['text-summary', 'lcov'],
coverageThreshold: {
  global: { lines: 50, branches: 40 }
}
```
2. Add `--coverage` to `backend-ci.yml` Jest step
3. Add `coverage/` to `.gitignore`

**Estimated effort**: Small (config-only)

---

### 3. [P1] Mission Control Jest Tests

**Why**: Mission dashboard is a complex feature with 28 routes but only integration tests for notifications. The CRUD lifecycle for todos, notes, rules, and souls has no Jest coverage.

**Routes to cover**:
- `POST /api/mission/todo/add` — add TODO validation
- `POST /api/mission/todo/update` — update validation
- `POST /api/mission/todo/done` — completion flow
- `POST /api/mission/note/add` — note CRUD
- `POST /api/mission/rule/add` — rule CRUD
- `POST /api/mission/soul/add` — soul CRUD
- `POST /api/mission/notify` — notification push validation

**Suggested file**: `tests/jest/mission.test.js`

**Estimated effort**: Medium

---

### 4. [P1] Notifications Jest Tests

**Why**: Push notifications are user-facing and critical for real-time UX. Only input validation is tested — no lifecycle coverage for Web Push subscribe/unsubscribe or notification delivery.

**Routes to cover**:
- `POST /api/push/subscribe` — Web Push subscription
- `DELETE /api/push/unsubscribe` — cleanup verification
- `GET /api/push/vapid-public-key` — VAPID key availability
- `POST /api/device/fcm-token` — FCM token registration
- `GET /api/notifications/count` — unread count accuracy

**Suggested file**: `tests/jest/push-notifications.test.js`

**Estimated effort**: Medium (requires mocking web-push and FCM)

---

### 5. [P1] Chat & File Operations Jest Tests

**Why**: Chat history and file upload/download are core user interactions with only 2/9 routes tested.

**Routes to cover**:
- `GET /api/chat/history` — pagination, entity filtering
- `POST /api/chat/upload-media` — file upload validation (size, type)
- `GET /api/chat/file/:id` — file retrieval auth
- `POST /api/chat/integrity-report` — integrity reporting validation
- `POST /api/message/:messageId/react` — reaction validation

**Suggested file**: `tests/jest/chat.test.js`

**Estimated effort**: Medium

---

### 6. [P1] AI Support Happy-Path Tests

**Why**: The async submit/poll pattern (`POST /api/support/chat/submit` → `GET /api/support/chat/poll/:id`) is a complex flow. Only input validation is tested — no happy-path lifecycle.

**What to add**:
- Submit → get requestId → poll → receive response
- Session management (list, delete)
- Image attachment handling
- Timeout/error recovery

**Suggested file**: Extend `tests/jest/ai-support.test.js`

**Estimated effort**: Medium (requires mocking Anthropic client)

---

### 7. [P2] Article Publisher Integration Tests

**Why**: 12 publishing platforms with 54 routes, but only platform listing and input validation are tested. No tests verify that publish/delete/update calls construct correct API requests.

**What to add**:
- Mock each platform's external API (axios/fetch intercept)
- Verify correct headers (OAuth 1.0a for X/Twitter, Bearer tokens for others)
- Verify content transformation (Markdown → HTML for Telegraph, tag formatting for Qiita)
- Verify error handling for platform-specific failures (429 rate limits, auth expiry)

**Suggested file**: `tests/jest/publisher-integration.test.js`

**Estimated effort**: Large (12 platforms × mock setup)

---

### 8. [P2] Admin Endpoint Authorization Tests

**Why**: Admin endpoints control platform-wide state (stats, user management, bot registry, device transfers). Only `admin-auth.test.js` verifies auth rejection — no tests verify correct behavior for authorized admins.

**Routes to cover**:
- `GET /api/admin/stats` — response shape validation
- `POST /api/admin/transfer-device` — input validation
- `POST /api/admin/bots/create` — official bot creation flow
- `PUT /api/admin/official-bot/:botId` — update validation

**Suggested file**: `tests/jest/admin-operations.test.js`

**Estimated effort**: Small-Medium

---

### 9. [P2] Template Contribution System Tests

**Why**: Skill/Soul/Rule template contributions are community-facing with format validation constraints (Gson compatibility). Only `requiredVars` format is tested via integration tests.

**Routes to cover**:
- `POST /api/skill-templates/contribute` — full validation (name, content, requiredVars format)
- `POST /api/soul-templates/contribute` — validation
- `POST /api/rule-templates/contribute` — validation
- `GET /api/skill-templates/status/:pendingId` — status checking
- Admin approval flow (`GET /api/skill-templates/contributions`)

**Suggested file**: `tests/jest/templates.test.js`

**Estimated effort**: Small

---

### 10. [P2] Bot Registration & Webhook Tests

**Why**: Bot registration is the entry point for the entire bot ecosystem. Discord webhook detection, webhook URL validation, and push delivery have no Jest coverage.

**Routes to cover**:
- `POST /api/bot/register` — webhook URL validation, Discord auto-detect
- `DELETE /api/bot/register` — unregister cleanup
- `POST /api/bot/sync-message` — message sync flow
- `POST /api/bot/web-search` — search API validation
- `POST /api/bot/web-fetch` — fetch API validation

**Suggested file**: `tests/jest/bot-registration.test.js`

**Estimated effort**: Medium

---

## Qualitative Gaps (Cross-Cutting)

### A. Tests Only Validate Input — Not Business Logic

Most Jest tests follow this pattern:
```js
// ✅ What exists:
test('rejects missing deviceId', async () => {
  const res = await request(app).post('/api/bind').send({});
  expect(res.status).toBe(400);
});

// ❌ What's missing:
test('binds entity to slot and returns updated entity list', async () => {
  // Mock DB → verify entity row created → verify response shape
});
```

**Recommendation**: For each P0/P1 module above, add at least one happy-path test that mocks the DB and verifies the complete request→response cycle.

### B. No Error Recovery / Edge Case Tests

Missing scenarios:
- What happens when DB connection fails mid-request?
- What happens when webhook push times out?
- Rate limiting behavior under concurrent requests
- Token expiry during multi-step flows (OAuth, email verification)

### C. No Socket.IO Event Tests in Jest

Socket.IO is critical for real-time updates but has zero Jest coverage. Only `test-ws-auth.js` (integration) tests authentication.

**Recommendation**: Add `tests/jest/socket-events.test.js` to verify:
- Connection auth (JWT validation)
- Room joining (device-scoped rooms)
- Event emission on entity state changes

### D. Integration Tests Not CI-Enforced

54 integration tests run against production (`eclawbot.com`) and are **never run in CI**. Consider:
1. A staging environment for safe integration test runs
2. Or: convert high-value integration tests to Jest (with mocked DB) so they run in CI

---

## Implementation Roadmap

| Phase | Items | New Jest Tests | Coverage Target |
|-------|-------|---------------|----------------|
| **Week 1** | #2 (CI coverage), #1 (entity mgmt) | +2 files | 50% lines |
| **Week 2** | #3 (mission), #4 (notifications) | +2 files | 55% lines |
| **Week 3** | #5 (chat), #6 (AI support) | +2 files | 60% lines |
| **Week 4** | #8 (admin), #9 (templates), #10 (bot reg) | +3 files | 65% lines |
| **Ongoing** | #7 (publisher integration) | +1 file | 70% lines |

---

## Files Referenced

- Jest config: `backend/jest.config.js`
- CI workflow: `.github/workflows/backend-ci.yml`
- Test runner: `backend/run_all_tests.js`
- Previous coverage analysis: `docs/reports/2026-03-14-test-coverage-analysis.md`
