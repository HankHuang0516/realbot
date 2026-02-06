# Realbot MCP Skills (Multi-Device Edition v5)

**新架構！** 每個裝置 (Device) 有自己獨立的 4 個實體欄位 (Entity 0-3)。
不同裝置的 Entity 0 不會互相干擾。

**API Base URL**: `https://realbot-production.up.railway.app`

---

## 🆕 v5 重大改變：矩陣架構

### 舊架構 (v4)
```
Server
└── entitySlots[0-3]  // 全域共享，先搶先贏
```

### 新架構 (v5)
```
Server
└── devices[deviceId]
    └── entities[0-3]  // 每個裝置有獨立的 4 個 Entity

Device A (手機1) ← Bot A
├── Entity 0
├── Entity 1
├── Entity 2
└── Entity 3

Device B (手機2) ← Bot B
├── Entity 0  // 不會跟 Device A 的 Entity 0 衝突！
├── Entity 1
└── ...
```

### API 變化
**所有 API 現在都需要 `deviceId` 參數！**

| 舊 API | 新 API |
|--------|--------|
| `GET /api/status?entityId=0` | `GET /api/status?deviceId=xxx&entityId=0` |
| `POST /api/transform { entityId, ... }` | `POST /api/transform { deviceId, entityId, ... }` |

---

## ⚠️ 重要: Bot 認證機制

綁定成功後，Server 會回傳：
- `deviceId` - 綁定的裝置 ID
- `entityId` - 綁定的實體 ID (0-3)
- `botSecret` - 32 位元認證 token

**Bot 必須保存這三個值，所有控制指令都需要它們！**

---

## 🚀 Push 模式 (Webhook 推播)

### `register_webhook`
註冊 webhook URL，啟用 Push 模式。

*   **Endpoint**: `POST /api/bot/register`
*   **Body**:
    ```json
    {
      "deviceId": "device-xxx",
      "entityId": 0,
      "botSecret": "your-bot-secret-here",
      "webhook_url": "https://your-bot-server.com/tools/invoke",
      "token": "Bearer-token-for-auth",
      "session_key": "agent:main:main"
    }
    ```

### Webhook 推播格式 (OpenClaw 格式)
```json
{
  "tool": "sessions_send",
  "args": {
    "sessionKey": "{session_key}",
    "message": "[Device xxx Entity 0 收到新訊息]\n來源: client\n內容: Hello!"
  }
}
```

---

## 1. 綁定流程 (Binding Flow)

### 流程說明
1. Android 裝置呼叫 `/api/device/register` 取得 binding code
2. Bot 使用該 code 呼叫 `/api/bind` 綁定
3. **Bot 保存回傳的 `deviceId`, `entityId`, `botSecret`**
4. 綁定後，Bot 使用這三個值控制該 entity

### `bind_to_entity`
使用 binding code 綁定到特定實體。

*   **Endpoint**: `POST /api/bind`
*   **Body**:
    ```json
    {
      "code": "123456"
    }
    ```
*   **Returns**:
    ```json
    {
      "success": true,
      "message": "Device device-xxx Entity 0 bound successfully",
      "deviceId": "device-xxx",
      "entityId": 0,
      "botSecret": "a1b2c3d4e5f6...",
      "deviceInfo": { "deviceId": "device-xxx", "entityId": 0, "status": "ONLINE" },
      "skills_documentation": "..."
    }
    ```

**⚠️ 重要**: 必須保存 `deviceId`, `entityId`, `botSecret`！

---

## 2. 實體狀態控制 (Entity Control)

### `update_claw_status`
更新指定實體的狀態與訊息。

*   **Endpoint**: `POST /api/transform`
*   **Body**:
    ```json
    {
      "deviceId": "device-xxx",
      "entityId": 0,
      "botSecret": "your-bot-secret",
      "message": "Hello!",
      "state": "EXCITED",
      "character": "LOBSTER",
      "parts": {
        "CLAW_LEFT": -45,
        "CLAW_RIGHT": 45
      }
    }
    ```

### `get_claw_status`
取得指定實體的當前狀態。

*   **Endpoint**: `GET /api/status?deviceId=xxx&entityId=0`

### `wake_up_claw`
喚醒指定實體。

*   **Endpoint**: `POST /api/wakeup`
*   **Body**: `{ "deviceId": "xxx", "entityId": 0, "botSecret": "..." }`

---

## 3. 查看所有實體 (View All Entities)

### `list_entities`
取得所有已綁定的實體列表。

*   **Endpoint**: `GET /api/entities`
*   **Optional**: `?deviceId=xxx` 過濾特定裝置
*   **Returns**:
    ```json
    {
      "entities": [
        { "deviceId": "device-a", "entityId": 0, "character": "LOBSTER", "state": "IDLE" },
        { "deviceId": "device-b", "entityId": 0, "character": "LOBSTER", "state": "EXCITED" }
      ],
      "activeCount": 2,
      "deviceCount": 2,
      "maxEntitiesPerDevice": 4
    }
    ```

---

## 4. 訊息收發 (Messaging)

### `send_message_to_entity` (Client → Bot)
手機端發送訊息給 Bot。

*   **Endpoint**: `POST /api/client/speak`
*   **Body**:
    ```json
    {
      "deviceId": "device-xxx",
      "entityId": 0,
      "text": "Hello Bot!"
    }
    ```

### `listen_for_messages` (Bot 接收訊息)
Bot 檢查待處理訊息。

*   **Endpoint**: `GET /api/client/pending?deviceId=xxx&entityId=0&botSecret=xxx`
*   **無 botSecret**: 只回傳 count（偷看模式）
*   **有 botSecret**: 回傳並消費訊息

---

## 5. 動畫範例

### 揮手 (Wave)
```json
{
  "deviceId": "device-xxx",
  "entityId": 0,
  "botSecret": "your-bot-secret",
  "message": "Hi there!",
  "state": "EXCITED",
  "parts": { "CLAW_LEFT": 45, "CLAW_RIGHT": 0 }
}
```

### 舉雙手歡呼 (Cheer)
```json
{
  "deviceId": "device-xxx",
  "entityId": 0,
  "botSecret": "your-bot-secret",
  "message": "Yay!",
  "state": "EXCITED",
  "parts": { "CLAW_LEFT": 60, "CLAW_RIGHT": -60 }
}
```

---

## 6. Debug 端點

### `GET /api/debug/devices`
查看所有裝置與實體狀態。

### `POST /api/debug/reset`
重置所有裝置（測試用）。

---

## 7. 需要 botSecret 的端點

| 端點 | 用途 | 需要 deviceId | 需要 botSecret |
|------|------|---------------|----------------|
| POST /api/bind | 綁定 | ❌ (code 包含) | ❌ (會產生) |
| POST /api/transform | 更新狀態 | ✅ | ✅ |
| POST /api/wakeup | 喚醒 | ✅ | ✅ |
| DELETE /api/entity | 移除實體 | ✅ | ✅ |
| POST /api/bot/register | 註冊 Webhook | ✅ | ✅ |
| DELETE /api/bot/register | 取消 Webhook | ✅ | ✅ |
| GET /api/status | 查詢狀態 | ✅ | ❌ |
| GET /api/entities | 列出所有 | ❌ (可選) | ❌ |
| GET /api/client/pending | 收訊息 | ✅ | ⚠️ (無則只回傳 count) |
| POST /api/client/speak | 發訊息 | ✅ | ❌ |

---

## 8. 多裝置隔離範例

```
裝置 A (deviceId: "phone-alice")
├── Entity 0 ← Bot Alice 控制
└── Entity 1 ← Bot Alice2 控制

裝置 B (deviceId: "phone-bob")
├── Entity 0 ← Bot Bob 控制 (不會跟 Alice 的 Entity 0 衝突！)
└── Entity 1 ← Bot Bob2 控制

Bot Alice 綁定時收到:
{
  "deviceId": "phone-alice",
  "entityId": 0,
  "botSecret": "abc123..."
}

Bot Bob 綁定時收到:
{
  "deviceId": "phone-bob",
  "entityId": 0,
  "botSecret": "def456..."  // 不同的 secret！
}
```

每個 Bot 只能控制自己綁定的 (deviceId, entityId) 組合。
