# Realbot MCP Skills (Multi-Device Edition v5)

## 🆕 v5 Major Change: Matrix Architecture

### Old Architecture (v4)
```
Server
└── entitySlots[0-3]  // Global shared, first come first served
```

### New Architecture (v5)
```
Server
└── devices[deviceId]
    └── entities[0-3]  // Each device has 4 independent entities

Device A (Phone 1) ← Bot A
├── Entity 0
├── Entity 1
├── Entity 2
├── Entity 3

Device B (Phone 2) ← Bot B
├── Entity 0  // No conflict with Entity 0 of Device A!
├── Entity 1
└── ...
```

### API Changes
**All APIs now require `deviceId` parameter!**

| Old API | New API |
|---------|---------|
| `GET /api/status?entityId=0` | `GET /api/status?deviceId=xxx&entityId=0` |
| `POST /api/transform { entityId, ... }` | `POST /api/transform { deviceId, entityId, ... }` |

---

## ⚠️ Important: Bot Authentication

After successful binding, Server returns:
- `deviceId` - Bound Device ID
- `entityId` - Bound Entity ID (0-3)
- `botSecret` - 32-bit authentication token

**Bot must save these three values. All control commands require them!**

---

## 🚀 Push Mode (Webhook)

### `register_webhook`
Register webhook URL to enable Push mode.

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

### Webhook Push Format (OpenClaw)
```json
{
  "tool": "sessions_send",
  "args": {
    "sessionKey": "{session_key}",
    "message": "[Device xxx Entity 0 received message]\nSource: client\nContent: Hello!"
  }
}
```

### ⚠️ Important Behavior: Update Status Immediately

When Bot receives message via Push, **it must immediately call `update_claw_status`** to update status, letting the device know the message is received and processing.

**Correct Flow:**
1. Receive Push message
2. **Immediately** call `POST /api/transform` to update status (e.g., `state: "BUSY"`, `message: "Processing..."`)
3. Execute tasks (query, calc, API calls)
4. After task completion, call `POST /api/transform` again with final result

**Incorrect Flow:** Process first then update status -> Device user wonders "Did Bot receive it?"

---

## 1. Binding Flow

### Process
1. Android device calls `/api/device/register` to get binding code
2. Bot uses code to call `/api/bind`
3. **Bot saves returned `deviceId`, `entityId`, `botSecret`**
4. After binding, Bot uses these values to control the entity

### `bind_to_entity`
Bind to specific entity using binding code.

*   **Endpoint**: `POST /api/bind`
*   **Body**:
    ```json
    {
      "code": "123456",
      "name": "Lobster Joe"
    }
    ```
    - `name` (Optional): Entity name, max 20 chars, displayed on wallpaper
*   **Returns**:
    ```json
    {
      "success": true,
      "message": "Device device-xxx Entity 0 bound successfully",
      "deviceId": "device-xxx",
      "entityId": 0,
      "botSecret": "a1b2c3d4e5f6...",
      "deviceInfo": { "deviceId": "device-xxx", "entityId": 0, "status": "ONLINE" },
      "versionInfo": {
        "latestVersion": "1.0.3",
        "deviceVersion": "1.0.2",
        "isOutdated": true,
        "versionWarning": "App version 1.0.2 is outdated. Please update to v1.0.3 for best experience."
      },
      "skills_documentation": "..."
    }
    ```

**⚠️ Important**: Must save `deviceId`, `entityId`, `botSecret`!

### App Version Check
The `versionInfo` field tells you about the Android app version:
- `latestVersion`: Latest release version
- `deviceVersion`: User's current app version
- `isOutdated`: `true` if user should update
- `versionWarning`: Warning message to show user (or `null` if up-to-date)

**If `isOutdated` is `true`**, consider notifying the user to update their app for the best experience.

---

## 2. Entity Control

### `update_claw_status`
Update status and message of specific entity.

*   **Endpoint**: `POST /api/transform`
*   **Body**:
    ```json
    {
      "deviceId": "device-xxx",
      "entityId": 0,
      "botSecret": "your-bot-secret",
      "name": "Joe",
      "message": "Hello!",
      "state": "EXCITED",
      "character": "LOBSTER",
      "parts": {
        "CLAW_LEFT": -45,
        "CLAW_RIGHT": 45
      }
    }
    ```
    - `name` (Optional): Entity name, max 20 chars. Empty string to clear.

### `get_claw_status`
Get current status of specific entity.

*   **Endpoint**: `GET /api/status?deviceId=xxx&entityId=0`

### `wake_up_claw`
Wake up specific entity.

*   **Endpoint**: `POST /api/wakeup`
*   **Body**: `{ "deviceId": "xxx", "entityId": 0, "botSecret": "..." }`

---

## 3. View All Entities

### `list_entities`
Get list of all bound entities.

*   **Endpoint**: `GET /api/entities`
*   **Optional**: `?deviceId=xxx` Filter by device
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

## 4. Messaging

### `send_message_to_entity` (Client → Bot)
Phone sends message to Bot. Supports single entity or broadcast.

*   **Endpoint**: `POST /api/client/speak`
*   **Body**:
    ```json
    {
      "deviceId": "device-xxx",
      "entityId": 0,
      "text": "Hello Bot!",
      "source": "android_widget"
    }
    ```

#### Broadcast Mode
`entityId` can be:
- **Number**: Single entity (e.g., `0`)
- **Array**: Multiple entities (e.g., `[0, 1, 2]`)
- **"all"**: All bound entities

```json
{
  "deviceId": "device-xxx",
  "entityId": [0, 1, 2],
  "text": "Hello everyone!",
  "source": "broadcast"
}
```

**Response**:
```json
{
  "success": true,
  "message": "Sent to 3 entity(s)",
  "targets": [
    { "entityId": 0, "pushed": true, "mode": "push" },
    { "entityId": 1, "pushed": false, "mode": "polling" },
    { "entityId": 2, "pushed": true, "mode": "push" }
  ],
  "broadcast": true
}
```

### `entity_speak_to` (Entity → Entity)
Entity to entity messaging. Requires sender's botSecret.

*   **Endpoint**: `POST /api/entity/speak-to`
*   **Body**:
    ```json
    {
      "deviceId": "device-xxx",
      "fromEntityId": 0,
      "toEntityId": 1,
      "botSecret": "sending-entity-bot-secret",
      "text": "Hey Entity 1!"
    }
    ```
*   **Returns**:
    ```json
    {
      "success": true,
      "message": "Message sent from Entity 0 to Entity 1",
      "from": { "entityId": 0, "character": "LOBSTER" },
      "to": { "entityId": 1, "character": "LOBSTER" },
      "pushed": true,
      "mode": "push"
    }
    ```

**Receiver Message Format**:
```json
{
  "text": "Hey Entity 1!",
  "from": "entity:0:LOBSTER",
  "fromEntityId": 0,
  "fromCharacter": "LOBSTER",
  "timestamp": 1704067200000
}
```

**Push Notification Format**:
```
[Device device-xxx Entity 1 received message]
Source: entity:0:LOBSTER
Content: Hey Entity 1!
```

### `listen_for_messages` (Bot Polls Messages)
Bot checks for pending messages.

*   **Endpoint**: `GET /api/client/pending?deviceId=xxx&entityId=0&botSecret=xxx`
*   **No botSecret**: Peek count only
*   **With botSecret**: Retrieve and consume messages

---

## 5. Animation Examples

### Wave
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

### Cheer
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

## 6. Debug Endpoints

### `GET /api/debug/devices`
View all devices and entity states.

### `POST /api/debug/reset`
Reset all devices (Test only).

---

## 7. Endpoints requiring botSecret

| Endpoint | Purpose | Needs deviceId | Needs botSecret |
|----------|---------|----------------|-----------------|
| POST /api/bind | Bind | ❌ (in code) | ❌ (Generated) |
| POST /api/transform | Update Status | ✅ | ✅ |
| POST /api/wakeup | Wake Up | ✅ | ✅ |
| DELETE /api/entity | Remove Entity | ✅ | ✅ |
| POST /api/bot/register | Register Webhook | ✅ | ✅ |
| DELETE /api/bot/register | Unregister Webhook | ✅ | ✅ |
| GET /api/status | Query Status | ✅ | ❌ |
| GET /api/entities | List All | ❌ (Optional) | ❌ |
| GET /api/client/pending | Poll Messages | ✅ | ⚠️ (Peek count if missing) |
| POST /api/client/speak | Client Speak | ✅ | ❌ |
| POST /api/entity/speak-to | Entity Speak | ✅ | ✅ (Sender) |

---

## 8. Multi-Device Isolation Example

```
Device A (deviceId: "phone-alice")
├── Entity 0 ← Controlled by Bot Alice
└── Entity 1 ← Controlled by Bot Alice2

Device B (deviceId: "phone-bob")
├── Entity 0 ← Controlled by Bot Bob (No conflict with Alice Entity 0)
└── Entity 1 ← Controlled by Bot Bob2

Bot Alice receives on bind:
{
  "deviceId": "phone-alice",
  "entityId": 0,
  "botSecret": "abc123..."
}

Bot Bob receives on bind:
{
  "deviceId": "phone-bob",
  "entityId": 0,
  "botSecret": "def456..."  // Different secret!
}
```

Each Bot can only control its bound (deviceId, entityId) combination.
