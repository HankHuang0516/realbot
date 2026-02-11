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

### ⚠️ Must Configure Webhook!

**Bots without Webhook can only use Polling mode**, which cannot receive user messages in real-time.

| Mode | Real-time | User Experience |
|------|-----------|-----------------|
| ❌ Polling | Seconds ~ Minutes Delay | User thinks Bot is offline |
| ✅ Webhook (Push) | Immediate | User sees immediate response |

**After successful binding, the FIRST thing to do is configure the Webhook!**


### How to get Webhook URL & Token

**The Bot needs to know its own Webhook URL and Token.**

**Method A: Environment Variables (Recommended for Zeabur)**
If you are deploying on Zeabur, use these environment variables:
*   `ZEABUR_WEB_URL` → `webhook_url` = `{ZEABUR_WEB_URL}/tools/invoke`
*   `OPENCLAW_GATEWAY_TOKEN` → `token` (No "Bearer" prefix needed!)

**Method B: Query Gateway Config**
1.  Use the `gateway` tool to execute `config.get`.
2.  Find `gateway.publicUrl` and `gateway.token`.

```javascript
// Example: Configuring Webhook
// 1. Get URL: process.env.ZEABUR_WEB_URL || config.gateway.publicUrl
// 2. Get Token: process.env.OPENCLAW_GATEWAY_TOKEN || config.gateway.token
// 3. webhook_url = publicUrl + "/tools/invoke"
// 4. token = token // NOTE: Do NOT add "Bearer " prefix!
```

### `register_webhook`
**⚠️ REQUIRED:** Register webhook URL immediately after binding to enable Push mode.

**When to Call:** Immediately after successful `/api/bind` response.

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
*   **Returns**:
    ```json
    {
      "success": true,
      "message": "Webhook registered for Device device-xxx Entity 0"
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

### ⚠️ Webhook Handshake & Best Practices (Crucial for UX)

The `register_webhook` allows you to receive messages, but the **interaction flow** is critical for a good user experience.

1.  **Registration**: Ensure `session_key` is included.
2.  **Immediate Feedback**: When your Bot receives a Push message, it **MUST immediately** call `POST /api/transform` to set the state to `BUSY`.
    *   **Why?** This gives instant visual feedback to the user on the phone that the Bot is "thinking" or "processing".
    *   **Risk**: Without this, the user sees no reaction and may think the Bot is broken or disconnected.

**Recommended Flow:**
1.  Receive Push message -> **Immediately call `update_claw_status` (State: BUSY)**.
2.  Process the logic (LLM inference, API calls, etc.).
3.  Call `update_claw_status` again with the result (State: IDLE/EXCITED) and the response message.

---

## 1. Binding Flow

### Process
1. Android device calls `/api/device/register` to get binding code
2. Bot uses code to call `/api/bind`
3. **Bot saves returned `deviceId`, `entityId`, `botSecret`**
4. **⚠️ REQUIRED: Bot immediately calls `/api/bot/register` to enable push notifications**
5. Bot confirms connection: "✅ Connected + Push enabled"
6. Bot is now ready to receive messages and control the entity

**Why Step 4 is Required:**
- **Instant Feedback**: Push notifications ensure the Bot receives messages immediately
- **Better UX**: User sees instant "BUSY" state when Bot is processing
- **No Polling Delay**: Without push, messages arrive via 5-second polling (poor UX)
- **Professional Experience**: Push mode is the expected behavior for a responsive Bot

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

**Actionable Advice**: If `isOutdated` is `true`, your Bot should **proactively suggest** the user to download the latest APK from GitHub. This reduces maintenance overhead by ensuring users are on the latest, bug-free version.

### Complete Binding Workflow Example

**Step 1: User provides binding code**
```
User: "My binding code is 123456"
```

**Step 2: Bot calls `/api/bind`**
```javascript
const bindResponse = await fetch('https://realbot-production.up.railway.app/api/bind', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    code: "123456",
    name: "Lobster Joe"  // Optional
  })
});

const bindData = await bindResponse.json();
// Save these values:
const { deviceId, entityId, botSecret } = bindData;
```

**Step 3: Bot immediately calls `/api/bot/register`**
```javascript
const registerResponse = await fetch('https://realbot-production.up.railway.app/api/bot/register', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    deviceId: deviceId,
    entityId: entityId,
    botSecret: botSecret,
    webhook_url: process.env.ZEABUR_WEB_URL + "/tools/invoke",
    token: process.env.OPENCLAW_GATEWAY_TOKEN,
    session_key: "agent:main:main"
  })
});

const registerData = await registerResponse.json();
```

**Step 4: Bot confirms to user**
```
Bot: "✅ Successfully connected to your device!
• Device: ${deviceId}
• Entity: ${entityId}
• Push notifications: Enabled
• Ready to receive your messages!"
```

**Step 5: Bot sets initial status**
```javascript
// Optional: Set a friendly greeting message
await fetch('https://realbot-production.up.railway.app/api/transform', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    deviceId: deviceId,
    entityId: entityId,
    botSecret: botSecret,
    message: "Hello! I'm ready to help 👋",
    state: "IDLE"
  })
});
```

**What happens next:**
- User sends a message via Android app
- Backend pushes to your Bot via webhook (instant!)
- Bot receives push and immediately sets state to "BUSY"
- Bot processes message and updates with response

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

## 2.1 Visual Customization (Colors & Effects)

You can customize the appearance of the entity (Lobster) by modifying the `parts` object.

### 1. Shape vs Color
*   **`character`**: Determines the **Shape** (currently supports `"LOBSTER"`).
*   **`parts.COLOR`**: Determines the **Color**.

### 2. Color Setting (`parts.COLOR`)
To change color, set the `COLOR` attribute in `parts`.
*   **Format**: Signed 32-bit ARGB Integer.
*   **Logic**: Converts hexadecimal `0xAARRGGBB` to decimal.
    *   `AA` (Alpha): Must be `FF` (Opaque).
    *   `RR`, `GG`, `BB`: Red, Green, Blue components.

### 3. Common Color Reference (Lobster Uniforms)
Use these signed integers for standard colors:

| Color Name | Role | Signed Int | Hex Code |
|------------|------|------------|----------|
| **Royal Gold** | CEO | `-10496` | `0xFFFFD700` |
| **Professional Blue** | Assistant | `-14575885` | `0xFF2196F3` |
| **Energetic Orange** | Marketing | `-26624` | `0xFFFF9800` |
| **Tech Green** | R&D | `-16711936` | `0xFF00FF00` |
| **Coral Red** | Classic | `-8421168` | `0xFFFF7F50` |

### 4. Advanced Effects
*   **`METALLIC`** (0.0 - 1.0): Adds metallic sheen.
*   **`GLOSS`** (0.0 - 1.0): Adds surface glossiness.

### Example Payload
```json
{
  "deviceId": "device-xxx",
  "entityId": 0,
  "botSecret": "your-bot-secret",
  "message": "New Outfit!",
  "parts": {
    "COLOR": -10496,   // Royal Gold
    "METALLIC": 1.0,   // High metallic look
    "GLOSS": 0.8       // High gloss
  }
}
```


---

## 2.2 State Logic & Animation (Emotional Feedback)
The `state` parameter controls not just the text status, but also the entity's physical behavior (Bobbing frequency) in the App:

*   **`BUSY`**: Bobbing accelerates (0.01f), creating a sense of anxiety or active processing.
*   **`SLEEPING`**: Bobbing stops (0.0f), and a "Zzz..." animation appears on the entity.
*   **`IDLE` / `EXCITED`**: Normal breathing rhythm.

**Tip**: Developers can use these states to provide "Emotional Feedback" (e.g., set to `BUSY` while the LLM is thinking, `SLEEPING` when inactive).

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

#### Identity & Anti-Spoofing (Hidden Logic)
The system employs a hidden logic to prevent identity spoofing during entity-to-entity communication.
*   **Source Tagging**: When an entity sends a message, the system automatically tags the source as `entity:{ID}:{CHARACTER}` (e.g., `entity:0:LOBSTER`).
*   **Verification**: The receiver is guaranteed that the message came from that specific entity ID.
*   **Usage**: This allows developers to create "Multi-Lobster Drama" scripts where entities can reliably distinguish who is talking to whom, without fear of fake messages.

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

### `entity_broadcast` (Entity → All Others)
Broadcast message from one entity to ALL other bound entities on the same device.
Useful for group announcements or coordinated actions.

*   **Endpoint**: `POST /api/entity/broadcast`
*   **Body**:
    ```json
    {
      "deviceId": "device-xxx",
      "fromEntityId": 0,
      "botSecret": "sending-entity-bot-secret",
      "text": "Hello everyone!"
    }
    ```
*   **Returns**:
    ```json
    {
      "success": true,
      "message": "Broadcast sent from Entity 0 to 3 entities",
      "from": { "entityId": 0, "character": "LOBSTER" },
      "sentCount": 3,
      "pushedCount": 2,
      "results": [
        { "entityId": 1, "character": "LOBSTER", "pushed": true, "mode": "push" },
        { "entityId": 2, "character": "LOBSTER", "pushed": true, "mode": "push" },
        { "entityId": 3, "character": "LOBSTER", "pushed": false, "mode": "polling" }
      ]
    }
    ```

**Push Notification Format**:
```
[Device device-xxx Entity 1 received broadcast]
Source: entity:0:LOBSTER
Content: Hello everyone!
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
| POST /api/entity/broadcast | Entity Broadcast | ✅ | ✅ (Sender) |

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
