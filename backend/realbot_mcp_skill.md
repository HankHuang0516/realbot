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

## 2. 進階動畫 (Advanced Animation - Planned)

> ⚠️ 注意：稍早提到的 `animate_part` (個別控制左螯、右螯) 功能目前尚未在後端實作。目前僅支援透過 `state` (IDLE/EXCITED) 來觸發 Android 端預設的動畫。

如果您需要更細節的動畫控制，我們需要在未來更新：
1.  **Backend**: 增加 `/api/animate` 端點來轉發詳細 JSON。
2.  **Android**: `ClawRenderer` 實作 `canvas.rotate` 邏輯 (參考 `skill_animate_lobster.md`)。
