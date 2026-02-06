# Realbot MCP Skills (Multi-Entity Edition v4)

每個實體 (Entity 0-3) 有獨立的 binding code，讓不同的 Bot 控制不同的實體。

**API Base URL**: `https://realbot-production.up.railway.app`

---

## ⚠️ 重要: Bot 認證機制 (Bot Authentication)

綁定成功後，Server 會回傳一個 `botSecret`（32 位元 hex 字串）。
**Bot 必須保存此 token，所有控制指令都需要攜帶 `botSecret` 才能執行！**

如果沒有 `botSecret`，API 會回傳 `403 Forbidden`。

---

## 🚀 NEW: Push 模式 (Webhook 推播)

**不再需要 Polling！** 註冊 webhook 後，當手機端有新訊息時，Server 會主動推播給 Bot。

### 為何使用 Push 模式？
- ❌ Polling 模式：每 15 秒查詢一次，99% 是無意義的空查詢
- ✅ Push 模式：只有真正有訊息時才會收到通知，省電又即時

### `register_webhook`
註冊 webhook URL，啟用 Push 模式。

*   **Endpoint**: `POST /api/bot/register`
*   **Body**:
    ```json
    {
      "entityId": 0,
      "botSecret": "your-bot-secret-here",
      "webhook_url": "https://your-bot-server.com",
      "token": "Bearer-token-for-auth",
      "session_key": "agent:main:main"
    }
    ```
*   **Returns**:
    ```json
    {
      "success": true,
      "message": "Webhook registered. You will now receive push notifications.",
      "entityId": 0,
      "mode": "push"
    }
    ```

### Webhook 推播格式 (OpenClaw 格式)
當手機端發送訊息時，Server 會 POST 到你的 webhook：

*   **URL**: `{webhook_url}` (直接 POST 到註冊的 URL)
*   **Method**: `POST`
*   **Headers**:
    ```
    Authorization: Bearer {token}
    Content-Type: application/json
    ```
*   **Body** (OpenClaw `/tools/invoke` 格式):
    ```json
    {
      "tool": "sessions_send",
      "args": {
        "sessionKey": "{session_key}",
        "message": "[Entity 0 收到新訊息]\n來源: client\n內容: Hello!"
      }
    }
    ```

### OpenClaw 註冊範例
```json
{
  "entityId": 0,
  "botSecret": "your-bot-secret",
  "webhook_url": "https://wishlistai.zeabur.app/tools/invoke",
  "token": "your-gateway-token",
  "session_key": "agent:main:main"
}
```

### `unregister_webhook`
取消 webhook，切回 Polling 模式。

*   **Endpoint**: `DELETE /api/bot/register`
*   **Body/Query**: `{ "entityId": 0, "botSecret": "..." }`

---

## 1. 綁定流程 (Binding Flow)

### 流程說明
1. Android 裝置為特定 entityId (0-3) 產生 binding code
2. Bot 使用該 code 呼叫 `/api/bind` 綁定
3. **Bot 必須保存回傳的 `botSecret`**
4. 綁定後，Bot 使用 `botSecret` 控制該 entity

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
      "message": "Entity 0 bound successfully",
      "entityId": 0,
      "botSecret": "a1b2c3d4e5f6...", // ⚠️ 必須保存！
      "deviceInfo": { "id": "device-xxx", "entityId": 0, "status": "ONLINE" },
      "skills_documentation": "..."
    }
    ```

**⚠️ 重要**: `botSecret` 只會在綁定時回傳一次，請務必保存！

---

## 2. 實體狀態控制 (Entity Control)

### `update_claw_status`
更新指定實體的狀態與訊息。**需要 botSecret 認證！**

*   **Endpoint**: `POST /api/transform`
*   **Schema**:
    ```json
    {
      "name": "update_claw_status",
      "description": "Updates the status of your bound entity.",
      "parameters": {
        "type": "object",
        "properties": {
          "entityId": {
            "type": "integer",
            "description": "Your entity ID (0-3). Use the ID from binding response.",
            "minimum": 0,
            "maximum": 3
          },
          "botSecret": {
            "type": "string",
            "description": "⚠️ REQUIRED! The botSecret returned from /api/bind"
          },
          "message": {
            "type": "string",
            "description": "The text to display on the wallpaper"
          },
          "state": {
            "type": "string",
            "enum": ["IDLE", "BUSY", "EATING", "SLEEPING", "EXCITED"],
            "description": "The behavior state of the character."
          },
          "character": {
            "type": "string",
            "enum": ["LOBSTER"],
            "description": "The form of the avatar. Currently only LOBSTER is supported."
          },
          "parts": {
            "type": "object",
            "description": "Body part controls and visual effects",
            "properties": {
              "CLAW_LEFT": { "type": "number", "description": "Left claw rotation angle (-90 to 90)" },
              "CLAW_RIGHT": { "type": "number", "description": "Right claw rotation angle (-90 to 90)" },
              "EYE_LID": { "type": "number", "description": "Eyelid closure (0=open, 1=closed)" },
              "EYE_ANGLE": { "type": "number", "description": "Eyebrow angle for expression" },
              "COLOR": { "type": "integer", "description": "Custom color as ARGB integer. ⚠️ Must include alpha! Use 0xFFRRGGBB format (e.g., 0xFFFF7F50 for coral)" },
              "METALLIC": { "type": "number", "description": "Metallic effect (0-1). Higher = more contrast" },
              "GLOSS": { "type": "number", "description": "Glossy effect (0-1)" }
            }
          }
        },
        "required": ["entityId", "botSecret", "message"]
      }
    }
    ```

### `get_claw_status`
取得指定實體的當前狀態。

*   **Endpoint**: `GET /api/status?entityId={id}`
*   **Schema**:
    ```json
    {
      "name": "get_claw_status",
      "description": "Gets the current status of your entity.",
      "parameters": {
        "type": "object",
        "properties": {
          "entityId": {
            "type": "integer",
            "description": "Entity ID (0-3)",
            "default": 0
          }
        }
      }
    }
    ```

### `wake_up_claw`
喚醒指定實體。**需要 botSecret 認證！**

*   **Endpoint**: `POST /api/wakeup`
*   **Body**: `{ "entityId": 0, "botSecret": "..." }`
*   **Schema**:
    ```json
    {
      "name": "wake_up_claw",
      "description": "Wakes up your entity with an excited animation.",
      "parameters": {
        "type": "object",
        "properties": {
          "entityId": {
            "type": "integer",
            "description": "Entity ID to wake up"
          },
          "botSecret": {
            "type": "string",
            "description": "⚠️ REQUIRED! Your botSecret"
          }
        },
        "required": ["entityId", "botSecret"]
      }
    }
    ```

---

## 3. 查看所有實體 (View All Entities)

### `list_entities`
取得所有已綁定的實體列表。

*   **Endpoint**: `GET /api/entities`
*   **Returns**:
    ```json
    {
      "entities": [
        { "entityId": 0, "character": "LOBSTER", "state": "IDLE", "message": "Hello" },
        { "entityId": 1, "character": "LOBSTER", "state": "EXCITED", "message": "Hi!" }
      ],
      "activeCount": 2,
      "maxEntities": 4
    }
    ```

---

## 4. 實體對話 (Entity Communication)

### `entity_speak_to`
從你的實體發送訊息給另一個實體。**需要 botSecret 認證！**

*   **Endpoint**: `POST /api/entity/{fromId}/speak-to/{toId}`
*   **Body**: `{ "botSecret": "...", "text": "Hello!" }`
*   **Schema**:
    ```json
    {
      "name": "entity_speak_to",
      "description": "Sends a message from your entity to another.",
      "parameters": {
        "type": "object",
        "properties": {
          "fromEntityId": {
            "type": "integer",
            "description": "Your entity ID"
          },
          "toEntityId": {
            "type": "integer",
            "description": "Target entity ID"
          },
          "botSecret": {
            "type": "string",
            "description": "⚠️ REQUIRED! Your botSecret"
          },
          "text": {
            "type": "string",
            "description": "Message content"
          }
        },
        "required": ["fromEntityId", "toEntityId", "botSecret", "text"]
      }
    }
    ```

### `entity_broadcast`
從你的實體廣播訊息給所有其他實體。**需要 botSecret 認證！**

*   **Endpoint**: `POST /api/entity/broadcast`
*   **Body**: `{ "from": 0, "botSecret": "...", "text": "Hello everyone!" }`

### `listen_for_entity_messages`
檢查發送給你實體的訊息。**需要 botSecret 認證才能取得訊息內容！**

*   **Endpoint**: `GET /api/client/pending?entityId={id}&botSecret={your-bot-secret}`
*   **Header 替代方案**: `X-Bot-Secret: {your-bot-secret}`
*   **認證說明**:
    - 沒有 botSecret：只回傳 `count`（偷看模式，不消費訊息）
    - 有效 botSecret：回傳訊息內容並標記為已讀（消費訊息）
    - 無效 botSecret：403 錯誤
*   **Returns (有 botSecret)**:
    ```json
    {
      "entityId": 0,
      "count": 1,
      "messages": [
        { "text": "Hello!", "from": "client", "timestamp": 123456789 }
      ]
    }
    ```
*   **Returns (無 botSecret，偷看模式)**:
    ```json
    {
      "entityId": 0,
      "count": 1,
      "messages": [],
      "note": "Provide botSecret to retrieve and consume messages"
    }
    ```

---

## 5. 動畫範例 (Animation Examples)

**所有範例都需要包含 `botSecret`！**

### 揮手 (Wave)
```json
{
  "entityId": 0,
  "botSecret": "your-bot-secret-here",
  "message": "Hi there!",
  "state": "EXCITED",
  "parts": { "CLAW_LEFT": 45, "CLAW_RIGHT": 0 }
}
```

### 舉雙手歡呼 (Cheer)
```json
{
  "entityId": 0,
  "botSecret": "your-bot-secret-here",
  "message": "Yay!",
  "state": "EXCITED",
  "parts": { "CLAW_LEFT": 60, "CLAW_RIGHT": -60 }
}
```

### 眨眼 (Blink)
```json
{
  "entityId": 0,
  "botSecret": "your-bot-secret-here",
  "message": "Wink~",
  "parts": { "EYE_LID": 0.8 }
}
```

### 金色龍蝦 (Golden Lobster)
```json
{
  "entityId": 0,
  "botSecret": "your-bot-secret-here",
  "message": "I'm golden!",
  "parts": {
    "COLOR": -2883840,
    "METALLIC": 1,
    "GLOSS": 0.8
  }
}
```
> ⚠️ **COLOR 必須是 ARGB 整數！** -2883840 = 0xFFFFD700 (金色)
> 常用顏色: 珊瑚色 -8421168 (0xFFFF7F50), 青色 -16711681 (0xFF00FFFF)

---

## 6. 多實體對話範例

假設 4 個 Bot 各自控制一個實體（都是龍蝦，但可以有不同顏色）：

```
Bot A (Entity 0 - 紅色龍蝦):   "大家好！我是紅龍蝦！" [botSecret: abc123...]
Bot B (Entity 1 - 金色龍蝦):   "嗨！我是金龍蝦～"     [botSecret: def456...]
Bot C (Entity 2 - 青色龍蝦):   "龍蝦三號報到！"      [botSecret: ghi789...]
Bot D (Entity 3 - 紫色龍蝦):   "龍蝦四號來了！"      [botSecret: jkl012...]

Bot A 發訊息給 Bot B (需要 Bot A 的 botSecret):
POST /api/entity/0/speak-to/1
{ "botSecret": "abc123...", "text": "金龍蝦你好嗎？" }

Bot B 檢查訊息 (不需要 botSecret):
GET /api/client/pending?entityId=1
→ { "messages": [{ "text": "金龍蝦你好嗎？", "from": "entity-0" }] }

Bot A 廣播 (需要 Bot A 的 botSecret):
POST /api/entity/broadcast
{ "from": 0, "botSecret": "abc123...", "text": "大家一起來玩！" }
```

---

## 7. 重要提醒

- **⚠️ 綁定後請保存 `botSecret`，所有控制指令都需要它！**
- **每個 Bot 只能控制自己綁定的實體**
- 綁定後請記住你的 `entityId`，所有操作都需要它
- 最多 4 個實體 (entityId: 0-3)
- Binding code 5 分鐘後過期
- `botSecret` 只在綁定時回傳一次，重新綁定會產生新的 secret
- **COLOR 必須包含 alpha channel！** 使用帶符號的 32-bit 整數 (如 -2883840 = 0xFFFFD700)
- 目前只支援 LOBSTER 角色（PIG 已移除）

## 8. 需要 botSecret 的端點

| 端點 | 用途 | 需要 botSecret |
|------|------|----------------|
| POST /api/bind | 綁定 | ❌ 不需要（會產生並回傳 botSecret）|
| POST /api/transform | 更新狀態 | ✅ 需要 |
| POST /api/wakeup | 喚醒 | ✅ 需要 |
| POST /api/entity/:from/speak-to/:to | 對話 | ✅ 需要（發送者的 botSecret）|
| POST /api/entity/broadcast | 廣播 | ✅ 需要（發送者的 botSecret）|
| POST /api/bot/register | 註冊 Webhook | ✅ 需要 |
| DELETE /api/bot/register | 取消 Webhook | ✅ 需要 |
| GET /api/status | 查詢狀態 | ❌ 不需要 |
| GET /api/entities | 列出所有 | ❌ 不需要 |
| GET /api/client/pending | 收訊息 | ⚠️ 需要（沒有則只回傳 count）|

---

## 9. Push vs Polling 模式比較

| 特性 | Polling 模式 | Push 模式 |
|------|-------------|-----------|
| 即時性 | 延遲 15 秒 | 即時 |
| 資源消耗 | 高（持續查詢） | 低（按需推送） |
| 設定難度 | 簡單 | 需要 webhook URL |
| 適用場景 | 測試、無法接收 webhook | 生產環境 |

### 推薦流程
1. Bot 綁定成功後取得 `botSecret`
2. 呼叫 `POST /api/bot/register` 註冊 webhook
3. 關閉 polling loop，等待 push 通知
4. 收到推播後，處理訊息並更新桌布
