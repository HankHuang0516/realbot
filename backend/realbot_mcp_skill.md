# Realbot MCP Skills (Multi-Entity Edition)

將這些工具定義提供給您的 OpenClaw / Claude Bot，讓它能夠操作您的 `realbot` 後端。

**API Base URL**: `https://realbot-production.up.railway.app`

---

## 1. 實體管理 (Entity Management)

支援最多 4 個實體同時在桌布上顯示。

### `list_entities`
取得所有活躍的實體列表。

*   **Endpoint**: `GET /api/entities`
*   **Schema**:
    ```json
    {
      "name": "list_entities",
      "description": "Returns all active entities on the wallpaper.",
      "parameters": {
        "type": "object",
        "properties": {}
      }
    }
    ```
*   **Returns**:
    ```json
    {
      "entities": [
        { "entityId": 0, "character": "LOBSTER", "state": "IDLE", "message": "Hello" },
        { "entityId": 1, "character": "PIG", "state": "EXCITED", "message": "Hi!" }
      ],
      "activeCount": 2,
      "maxEntities": 4
    }
    ```

### `spawn_entity`
生成新的實體 (最多 4 個)。

*   **Endpoint**: `POST /api/entity/spawn`
*   **Schema**:
    ```json
    {
      "name": "spawn_entity",
      "description": "Spawns a new character entity on the wallpaper (max 4 total).",
      "parameters": {
        "type": "object",
        "properties": {
          "entityId": {
            "type": "integer",
            "description": "Entity ID (1-3). Omit for auto-assign. Entity 0 always exists.",
            "minimum": 1,
            "maximum": 3
          },
          "character": {
            "type": "string",
            "enum": ["LOBSTER", "PIG"],
            "description": "Character type for the new entity. Default: LOBSTER"
          },
          "message": {
            "type": "string",
            "description": "Initial message for the entity."
          }
        }
      }
    }
    ```

### `remove_entity`
移除實體 (無法移除 Entity 0)。

*   **Endpoint**: `DELETE /api/entity/{entityId}`
*   **Schema**:
    ```json
    {
      "name": "remove_entity",
      "description": "Removes an entity from the wallpaper. Cannot remove entity 0.",
      "parameters": {
        "type": "object",
        "properties": {
          "entityId": {
            "type": "integer",
            "description": "Entity ID to remove (1-3).",
            "minimum": 1,
            "maximum": 3
          }
        },
        "required": ["entityId"]
      }
    }
    ```

---

## 2. 實體狀態更新 (Entity Status)

### `update_claw_status`
更新指定實體的狀態與訊息 (支援 entityId 參數)。

*   **Endpoint**: `POST /api/transform`
*   **Schema**:
    ```json
    {
      "name": "update_claw_status",
      "description": "Updates the status of a specific entity (defaults to entity 0).",
      "parameters": {
        "type": "object",
        "properties": {
          "entityId": {
            "type": "integer",
            "description": "Target entity ID (0-3). Default: 0",
            "default": 0
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
            "enum": ["LOBSTER", "PIG"],
            "description": "The form of the avatar."
          },
          "parts": {
            "type": "object",
            "description": "Rotation angles for body parts. Keys: CLAW_LEFT, CLAW_RIGHT, EYE_LID, EYE_ANGLE",
            "additionalProperties": { "type": "number" }
          }
        },
        "required": ["message"]
      }
    }
    ```

---

## 3. 實體對話 (Entity Communication)

### `entity_speak_to`
從一個實體發送訊息給另一個實體。

*   **Endpoint**: `POST /api/entity/{fromId}/speak-to/{toId}`
*   **Schema**:
    ```json
    {
      "name": "entity_speak_to",
      "description": "Sends a message from one entity to another.",
      "parameters": {
        "type": "object",
        "properties": {
          "fromEntityId": {
            "type": "integer",
            "description": "Source entity ID (0-3)."
          },
          "toEntityId": {
            "type": "integer",
            "description": "Target entity ID (0-3)."
          },
          "text": {
            "type": "string",
            "description": "Message content."
          }
        },
        "required": ["fromEntityId", "toEntityId", "text"]
      }
    }
    ```

### `entity_broadcast`
從一個實體廣播訊息給所有其他實體。

*   **Endpoint**: `POST /api/entity/broadcast`
*   **Schema**:
    ```json
    {
      "name": "entity_broadcast",
      "description": "Broadcasts a message from one entity to all others.",
      "parameters": {
        "type": "object",
        "properties": {
          "from": {
            "type": "integer",
            "description": "Source entity ID."
          },
          "text": {
            "type": "string",
            "description": "Message to broadcast."
          }
        },
        "required": ["from", "text"]
      }
    }
    ```

### `listen_for_entity_messages`
檢查發送給特定實體的訊息。

*   **Endpoint**: `GET /api/client/pending?entityId={id}`
*   **Schema**:
    ```json
    {
      "name": "listen_for_entity_messages",
      "description": "Checks for messages sent to a specific entity.",
      "parameters": {
        "type": "object",
        "properties": {
          "entityId": {
            "type": "integer",
            "description": "Entity ID to check messages for. Default: 0"
          }
        }
      }
    }
    ```
*   **Returns**:
    ```json
    {
      "count": 1,
      "messages": [
        { "text": "Hello!", "from": "claw-0", "timestamp": 123456789, "read": false }
      ]
    }
    ```

---

## 4. 進階動畫 (Advanced Animation)

### 範例：揮手 (Wave)
```json
{
  "name": "update_claw_status",
  "arguments": {
    "entityId": 0,
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
    "entityId": 0,
    "message": "Yay!",
    "state": "EXCITED",
    "parts": {
      "CLAW_LEFT": 60,
      "CLAW_RIGHT": -60
    }
  }
}
```

---

## 5. 多實體對話範例 (Multi-Entity Conversation Example)

```javascript
// 1. 生成第二個實體 (小豬)
POST /api/entity/spawn
{ "entityId": 1, "character": "PIG", "message": "我是小豬!" }

// 2. 龍蝦打招呼
POST /api/transform
{ "entityId": 0, "message": "嗨小豬!", "state": "EXCITED" }

// 3. 龍蝦發送訊息給小豬
POST /api/entity/0/speak-to/1
{ "text": "你好嗎?" }

// 4. 檢查小豬收到的訊息
GET /api/client/pending?entityId=1
// Returns: { "messages": [{ "text": "你好嗎?", "from": "claw-0" }] }

// 5. 小豬回應
POST /api/transform
{ "entityId": 1, "message": "我很好!", "state": "EXCITED" }

// 6. 龍蝦廣播訊息
POST /api/entity/broadcast
{ "from": 0, "text": "大家好!" }
```

---

## 6. 舊版相容 (Backward Compatibility)

所有原有的 endpoint 仍然可用，預設操作 Entity 0：
- `GET /api/status` - 取得 Entity 0 狀態
- `POST /api/transform` - 更新 Entity 0 (不帶 entityId)
- `POST /api/wakeup` - 喚醒 Entity 0
- `GET /api/client/pending` - 取得 Entity 0 的訊息
