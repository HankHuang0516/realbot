# Realbot MCP Skills 🦞

將這些工具定義提供給您的 OpenClaw / Claude Bot，讓它能夠操作您的 `realbot` 後端。

## 1. 核心工具 (Core Tools)

這些工具直接對應到您 Railway 後端的 API。

### `update_claw_status`
更新桌布上龍蝦的狀態與訊息。

*   **Endpoint**: `POST /api/transform`
*   **Description**: Change the avatar's state, emotion, and display message.
*   **Schema**:
    ```json
    {
      "name": "update_claw_status",
      "description": "Updates the status of the Claw Live Wallpaper agent.",
      "parameters": {
        "type": "object",
        "properties": {
          "message": {
            "type": "string",
            "description": "The text to display on the wallpaper (e.g., 'Working hard', 'Hello!')"
          },
          "state": {
            "type": "string",
            "enum": ["IDLE", "BUSY", "EATING", "SLEEPING", "EXCITED"],
            "description": "The behavior state of the character."
          },
          "character": {
            "type": "string",
            "enum": ["LOBSTER", "PIG"],
            "description": "The form of the avatar. Default is LOBSTER."
          },
          "parts": {
            "type": "object",
            "description": "Rotation angles for body parts. Keys: CLAW_LEFT, CLAW_RIGHT. Values: Degrees (e.g. {'CLAW_LEFT': 30, 'CLAW_RIGHT': -30})",
            "additionalProperties": { "type": "number" }
          }
        },
        "required": ["message"]
      }
    }
    ```

### `listen_for_user_messages`
檢查是否有來自手機用戶的新訊息。

*   **Endpoint**: `GET /api/client/pending`
*   **Description**: Poll for any new messages sent by the user from the Android device.
*   **Schema**:
    ```json
    {
      "name": "listen_for_user_messages",
      "description": "Checks for new messages sent by the user from the mobile device.",
      "parameters": {
        "type": "object",
        "properties": {}
      }
    }
    ```
*   **Returns**:
    ```json
    {
      "count": 1,
      "messages": [
        { "text": "Hello bot!", "timestamp": 123456789, "read": false }
      ]
    }
    ```

### `wake_up_claw`
喚醒手機 (通常用於測試 Webhook 連線)。

*   **Endpoint**: `POST /api/wakeup`
*   **Schema**:
    ```json
    {
      "name": "wake_up_claw",
      "description": "Triggers a 'wake up' event on the device, causing visual feedback.",
      "parameters": {
        "type": "object",
        "properties": {}
      }
    }
    ```

---

## 2. 進階動畫 (Advanced Animation)

現在後端已支援 `parts` 參數，您可以控制龍蝦的肢體！

### 範例：揮手 (Wave)
讓左螯舉起來 (旋轉 45 度)。

```json
{
  "name": "update_claw_status",
  "arguments": {
    "message": "Hi there!",
    "state": "EXCITED",
    "parts": {
      "CLAW_LEFT": 45,
      "CLAW_RIGHT": 0
    }
  }
}
```

### 範例：舉雙手歡呼 (Cheer)
```json
{
  "name": "update_claw_status",
  "arguments": {
    "message": "Yay!",
    "state": "EXCITED",
    "parts": {
      "CLAW_LEFT": 60,
      "CLAW_RIGHT": -60
    }
  }
}
```

> **注意**: 這些角度會持續保持，直到下一次 `update_claw_status` 更新。若要做連續動畫，需連續發送指令 (但請注意 API 頻率)。
