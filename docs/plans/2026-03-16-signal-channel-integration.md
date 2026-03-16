# Signal Channel Integration Plan (Issue #211)

## Overview

將 Signal Messenger 整合到 EClaw，讓 entity 可以透過 Signal 收發訊息。
使用者已在 Railway 部署 `signal-cli-rest-api`，本計畫在 EClaw backend 新增原生 Signal 模組，直接橋接 signal-cli-rest-api，不依賴 OpenClaw 平台。

---

## Architecture Decision

### 選擇：Native Backend Module（非 OpenClaw Plugin）

**原因：**
1. signal-cli-rest-api 沒有原生 webhook callback，需要 polling/WebSocket — 放在 backend 內更容易管理生命週期
2. 避免多一個獨立服務部署
3. 使用者的 Signal 帳號直接綁定 EClaw entity，不需要 OpenClaw 中介
4. 複用現有 Channel API 架構（`channel_accounts` table、`pushToChannelCallback`、`bindingType: 'channel'`）

### 訊息流

```
Signal User sends DM
       ↓
signal-cli-rest-api (Railway)
       ↓ (EClaw backend polls via GET /v1/receive or WebSocket)
backend/signal.js (bridge module)
       ↓ maps Signal msg → POST /api/channel/message (internal)
EClaw entity processes message
       ↓ pushToChannelCallback() fires
backend/signal.js receives callback
       ↓ POST /v2/send to signal-cli-rest-api
Signal User receives reply
```

---

## Implementation Steps

### Step 1: Backend Module — `backend/signal.js`

新建 `backend/signal.js`，負責：

**1a. 連線管理**
- 環境變數：`SIGNAL_API_URL`（signal-cli-rest-api 的 URL）、`SIGNAL_NUMBER`（已連結的 Signal 號碼）
- 啟動時驗證連線：`GET /v1/about` 確認 signal-cli-rest-api 可達
- 匯出 `router` + `init(deps)` pattern（與其他模組一致）

**1b. 訊息輪詢（Message Polling）**
- 啟動 polling loop：每 5 秒 `GET /v1/receive/{number}`
- 解析收到的訊息，過濾 `type: "message"`（忽略 receipt、typing 等）
- 映射 Signal 使用者 → EClaw entity（透過 `signal_bindings` 查詢）
- 將訊息轉發到對應 entity（內部呼叫 transform/channel message flow）

**1c. 訊息發送**
- 接收 `pushToChannelCallback` 的 payload
- 呼叫 `POST /v2/send` 發送回覆給 Signal 使用者
- 支援媒體附件轉換（EClaw photo/voice/file → Signal base64 attachment）

**1d. QR Code Linking**
- `GET /api/signal/qr-link` — 代理 signal-cli-rest-api 的 `GET /v1/qrcodelink`
- 回傳 QR code 圖片供前端顯示
- 用於初次設定 Signal 帳號連結

### Step 2: Database — Signal Binding Table

在 `backend/db.js` 新增 table：

```sql
CREATE TABLE IF NOT EXISTS signal_bindings (
    id SERIAL PRIMARY KEY,
    device_id TEXT NOT NULL REFERENCES devices(device_id) ON DELETE CASCADE,
    entity_id INTEGER NOT NULL,
    signal_number TEXT NOT NULL,           -- Signal user's phone number
    signal_api_url TEXT NOT NULL,          -- signal-cli-rest-api URL
    linked_number TEXT NOT NULL,           -- EClaw's linked Signal number
    channel_account_id INTEGER REFERENCES channel_accounts(id),
    dm_pairing TEXT DEFAULT 'open',        -- 'open' | 'locked' | 'admin_only'
    status TEXT DEFAULT 'active',
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL,
    UNIQUE(device_id, entity_id),
    UNIQUE(signal_number, linked_number)
);
```

**設計重點：**
- 每個 entity 最多綁定一個 Signal 使用者（1:1 DM pairing）
- `signal_number` 是對話對象的號碼，`linked_number` 是 EClaw 的 Signal 號碼
- 透過 `channel_account_id` 連結到 Channel API 體系
- DM pairing policy 控制誰可以觸發 entity 回應

### Step 3: API Endpoints — `backend/signal.js` router

| Endpoint | Auth | Purpose |
|----------|------|---------|
| `GET /api/signal/status` | deviceId+Secret | Signal 連線狀態、已連結號碼 |
| `POST /api/signal/link` | deviceId+Secret | 啟動 QR code linking 流程 |
| `GET /api/signal/qr` | deviceId+Secret | 取得 QR code 圖片 |
| `POST /api/signal/bind` | deviceId+Secret | 綁定 Signal 使用者到 entity |
| `DELETE /api/signal/bind/:entityId` | deviceId+Secret | 解除綁定 |
| `GET /api/signal/bindings` | deviceId+Secret | 列出所有 Signal 綁定 |
| `PATCH /api/signal/bind/:entityId` | deviceId+Secret | 更新 DM pairing policy |

### Step 4: 整合到 Channel API 體系

**修改 `backend/index.js`：**
- 在 `pushToChannelCallback()` 增加 Signal 路由判斷
- 當 entity 的 `bindingType === 'signal'`，呼叫 signal module 的發送函數（而非 HTTP callback）
- 或者：Signal binding 同時建立 `channel_accounts` 記錄，callback 指向 backend 自身的 internal handler

**推薦做法：** 使用新的 `bindingType: 'signal'`（與 `'channel'` 區分），signal.js 直接處理推送，不走 HTTP callback loop。這避免了 backend 自己打自己的問題。

### Step 5: Web Portal UI

**修改 `backend/public/portal/settings.html`：**

在 Channel API 區塊下方新增「Signal Integration」卡片：

```
📱 Signal Integration
├── 連線狀態：🟢 Connected / 🔴 Not linked
├── 已連結號碼：+886-XXX-XXX-XXX
├── [Link Signal Account] 按鈕 → 顯示 QR code modal
├── Entity Bindings:
│   ├── Entity #0 ↔ +1234567890 (open) [Unbind]
│   └── Entity #2 ↔ +0987654321 (locked) [Unbind]
└── [+ Bind New] 按鈕 → 選 entity + 輸入 Signal 號碼
```

### Step 6: Android App UI

**修改 `SettingsActivity.kt`：**
- 新增 Signal 設定區塊（在帳號區下方）
- 連線狀態顯示
- QR code linking（使用 WebView 或 ImageView 顯示）
- Entity binding 列表 + 新增/刪除

**新增 API model：**
- `SignalStatus`、`SignalBinding` data classes
- `ClawApiService.kt` 新增 Signal endpoints

### Step 7: iOS App UI

**修改 `ios-app/app/(tabs)/settings.tsx`：**
- 新增 Signal 設定 section
- 與 Android 同等功能

### Step 8: E2EE Awareness

Signal 天生 E2EE，整合時：
- Entity 綁定 Signal 時自動設定 `encryptionStatus = 'e2ee'`
- 所有三平台 UI 會自動顯示 🔒 E2EE badge（已有支援）
- Push payload 包含 `e2ee: true`

### Step 9: 環境變數

新增到 `backend/.env.example`：

```
# Signal Integration (Issue #211)
SIGNAL_API_URL=          # signal-cli-rest-api URL (e.g., https://signal-cli-rest-api-production-xxxx.up.railway.app)
SIGNAL_NUMBER=           # Linked Signal phone number (e.g., +886912345678)
SIGNAL_POLL_INTERVAL=5000  # Polling interval in ms (default: 5000)
```

### Step 10: Tests

**新增 `backend/tests/test-signal-integration.js`：**
- Signal status endpoint 回應格式
- Binding CRUD lifecycle
- DM pairing policy validation
- 訊息格式轉換（Signal → EClaw → Signal）
- E2EE encryptionStatus 自動設定

**新增 `backend/tests/jest/signal.test.js`：**
- Input validation（missing fields、invalid number format）
- Auth protection（需要 deviceId+Secret）

---

## Impact Assessment

| 檔案 | 改動程度 | 說明 |
|------|---------|------|
| `backend/signal.js` | **新建** | Signal 橋接模組（~400 行） |
| `backend/db.js` | **小改** | 新增 `signal_bindings` table schema + CRUD |
| `backend/index.js` | **小改** | require signal module、`bindingType === 'signal'` routing（~30 行） |
| `backend/public/portal/settings.html` | **中改** | Signal 設定卡片 UI |
| `app/.../SettingsActivity.kt` | **中改** | Signal 設定區塊 |
| `app/.../ClawApiService.kt` | **小改** | Signal API endpoints |
| `ios-app/app/(tabs)/settings.tsx` | **中改** | Signal 設定 section |
| `backend/.env.example` | **微改** | 3 個新環境變數 |

**風險：**
- Polling 每 5 秒打一次 signal-cli-rest-api，需要確保不會 timeout 或造成訊息遺漏
- Signal 號碼格式驗證（國際格式 +xxx）
- signal-cli-rest-api 重啟時 polling 需要 graceful retry

**不影響的部分：**
- 現有 OpenClaw channel 整合完全不受影響
- 現有 bot webhook 流程不受影響
- Channel API 框架不需要改動

---

## Execution Order

1. ✅ `backend/db.js` — signal_bindings table
2. ✅ `backend/signal.js` — core module（連線、polling、發送、API routes）
3. ✅ `backend/index.js` — require + routing integration
4. ✅ `backend/.env.example` — 環境變數
5. ✅ Tests — unit + integration
6. ✅ Web Portal UI — settings.html Signal 卡片
7. ✅ Android UI — SettingsActivity Signal 區塊
8. ✅ iOS UI — settings.tsx Signal section
9. ✅ Deploy + verify

**預估：Steps 1-5 為核心，Steps 6-8 為 UI parity。**
