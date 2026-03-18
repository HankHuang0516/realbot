# Rich Message Templates Design (#258)

## Overview
Add platform-agnostic rich message support to EClaw: **Quick Replies**, **Inline Buttons**, and **Rich Embeds**. These allow bots to send structured, interactive messages instead of plain text.

---

## Data Structure

### `richContent` JSON field (stored in chat history, passed in push payloads)

```jsonc
{
  // Quick Replies — disappear after selection, auto-send chosen text
  "quickReplies": [
    { "label": "查詢餘額", "value": "balance" },
    { "label": "聯繫客服", "value": "support" }
  ],

  // Buttons — persistent, can open URL or trigger callback
  "buttons": [
    { "label": "查看詳情", "action": "url", "value": "https://..." },
    { "label": "標記完成", "action": "callback", "value": "mission_done_123" }
  ],

  // Embeds — structured card with optional image/fields
  "embeds": [{
    "title": "任務報告",
    "description": "所有任務已完成",
    "color": "#4CAF50",
    "fields": [
      { "name": "已完成", "value": "5", "inline": true },
      { "name": "進行中", "value": "0", "inline": true }
    ],
    "thumbnail": "https://example.com/avatar.png",
    "url": "https://eclawbot.com/portal/mission.html"
  }]
}
```

All three sub-fields are optional. A message can have text + richContent, or richContent alone.

Limits:
- `quickReplies`: max 10 items, label max 40 chars
- `buttons`: max 5 items, label max 40 chars
- `embeds`: max 3 items, title max 256 chars, description max 2048 chars, fields max 10 per embed

---

## Implementation Plan

### Phase 1: Backend (channel-api.js + index.js)

#### 1a. Extend `POST /api/channel/message`
- Accept optional `richContent` field in request body
- Validate structure and limits (quickReplies/buttons/embeds)
- Pass through to `saveChatMessage()` and push

#### 1b. Extend `pushToChannelCallback()`
- Add `richContent` field to callback payload (alongside existing text/media)
- Channel plugins receive rich content and can render platform-appropriately

#### 1c. Extend `POST /api/transform`
- Bot responses via transform can include `richContent` in the response body
- Pass through to entity state + push

#### 1d. Extend `POST /api/client/speak`
- Client messages can include `richContent` (for testing/admin use)
- Pass through to push

#### 1e. Button Callback endpoint: `POST /api/channel/button-callback`
- When user clicks a `callback` button, the channel plugin POSTs back:
  ```json
  { "channel_api_key": "...", "deviceId": "...", "entityId": 0,
    "callbackId": "mission_done_123", "userId": "user123" }
  ```
- Server pushes this as a special event to the bot webhook

#### 1f. Discord format conversion
- When pushing to Discord webhooks, convert EClaw `richContent` to Discord format:
  - `embeds` → Discord embed objects
  - `buttons` → Discord components (action rows + buttons)
  - `quickReplies` → Not supported by Discord (fallback: append as text list)

#### 1g. Chat history storage
- `richContent` stored as JSONB in `saveChatMessage()` alongside existing fields
- Returned in `GET /api/chat/history` responses

### Phase 2: Web Portal (chat.html)

#### 2a. Rich message rendering
In `renderMessages()`, after building `textHtml`:
- Check `msg.richContent` field
- Render Quick Replies as chip buttons below the message bubble
- Render Buttons as styled action buttons
- Render Embeds as card-style divs with title/description/fields/thumbnail

#### 2b. Quick Reply interaction
- Click → auto-send the `value` as a new message via existing `sendMessage()` flow
- Hide quick reply chips after selection (CSS class toggle)

#### 2c. Button interaction
- `action: "url"` → `window.open(value, '_blank')`
- `action: "callback"` → POST to `/api/channel/button-callback` (or direct `sendMessage()` with callback metadata)

#### 2d. Embed rendering
- Card layout: colored left border, title (bold, optional link), description, fields grid, thumbnail
- CSS classes: `.rich-embed`, `.rich-embed-title`, `.rich-embed-field`, etc.

### Phase 3: Android (ChatAdapter.kt)

#### 3a. Update ChatMessage model
- Add `richContent: RichContent?` field to `ChatMessage` data class
- Parse from API response JSON

#### 3b. Extend ChatAdapter ViewHolders
- Add layout containers for quick replies (ChipGroup), buttons (LinearLayout), embeds (CardView)
- In `bind()`, check `message.richContent` and populate accordingly

#### 3c. Interaction handlers
- Quick Reply click → call existing send message API
- Button URL → open in browser
- Button callback → POST callback or send as message

### Phase 4: iOS/React Native

#### 4a. Update ChatMessage type
- Add `richContent?: RichContent` to `ChatMessage` interface

#### 4b. New components
- `QuickReplyChips` — horizontal ScrollView with pill buttons
- `ActionButtons` — vertical button list
- `EmbedCard` — styled View with title/description/fields

#### 4c. Integration in ChatBubble
- Conditional rendering based on `message.richContent`

### Phase 5: Tests

#### 5a. Jest unit test
- `tests/jest/rich-message.test.js` — validation, limits, malformed input

#### 5b. Integration test
- `tests/test-rich-message-templates.js` — end-to-end: send rich message via channel API, verify in chat history, verify push payload

---

## DB Schema Change

Add `rich_content` JSONB column to chat messages table (if chat uses a table), or include in the existing message JSON blob.

---

## Discord Conversion Map

| EClaw Format | Discord Format |
|-------------|----------------|
| `embeds[].title` | `embeds[].title` |
| `embeds[].description` | `embeds[].description` |
| `embeds[].color` (hex) | `embeds[].color` (int) |
| `embeds[].fields` | `embeds[].fields` |
| `embeds[].thumbnail` | `embeds[].thumbnail.url` |
| `embeds[].url` | `embeds[].url` |
| `buttons` (callback) | `components` action row + button type 2 |
| `buttons` (url) | `components` action row + link button type 5 |
| `quickReplies` | Fallback: text list (Discord has no equivalent) |

---

## API Examples

### Bot sends rich reply via channel
```bash
POST /api/channel/message
{
  "channel_api_key": "...",
  "deviceId": "...",
  "entityId": 0,
  "botSecret": "...",
  "message": "請選擇服務：",
  "richContent": {
    "quickReplies": [
      { "label": "查詢餘額", "value": "balance" },
      { "label": "聯繫客服", "value": "support" }
    ]
  }
}
```

### Bot sends embed via transform
```bash
POST /api/transform
{
  "deviceId": "...",
  "botSecret": "...",
  "message": "",
  "richContent": {
    "embeds": [{
      "title": "任務完成報告",
      "description": "Entity #3 已完成所有指派任務",
      "color": "#4CAF50",
      "fields": [
        { "name": "完成數", "value": "5", "inline": true },
        { "name": "失敗數", "value": "0", "inline": true }
      ]
    }]
  }
}
```
