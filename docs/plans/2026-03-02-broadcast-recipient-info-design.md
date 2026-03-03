# Broadcast Recipient Info Injection — Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Inject a `[BROADCAST RECIPIENTS]` context block into broadcast push messages so bots know who else received the same broadcast, enabling social interaction.

**Architecture:** Add a `device_preferences` DB table (reusing notification prefs pattern). Add a `buildBroadcastRecipientBlock()` helper in index.js. Inject the block into both `/api/client/speak` (user broadcast) and `/api/entity/broadcast` (bot broadcast) push message assembly. Add toggle UI to Web Portal settings.html and Android SettingsActivity.

**Tech Stack:** Node.js/Express backend, PostgreSQL, HTML/JS portal, Kotlin Android

**Issue:** [#105](https://github.com/HankHuang0516/realbot/issues/105)

---

## Task 1: Device Preferences DB Table + Module

**Files:**
- Create: `backend/device-preferences.js`
- Modify: `backend/index.js` (require + init, lines ~17 and ~6397)

**Step 1: Create `backend/device-preferences.js`**

Follow the pattern from `backend/notifications.js` (lines 1-50, 173-219).

```javascript
// ============================================
// Device Preferences Module
// Per-device settings stored in PostgreSQL
// ============================================

const DEFAULTS = {
    broadcast_recipient_info: true
};

let pool = null;

async function initTable(chatPool) {
    pool = chatPool;
    await pool.query(`
        CREATE TABLE IF NOT EXISTS device_preferences (
            device_id TEXT PRIMARY KEY,
            prefs JSONB DEFAULT '{}',
            updated_at BIGINT DEFAULT EXTRACT(EPOCH FROM NOW()) * 1000
        )
    `);
}

async function getPrefs(deviceId) {
    if (!pool) return { ...DEFAULTS };
    try {
        const result = await pool.query(
            'SELECT prefs FROM device_preferences WHERE device_id = $1',
            [deviceId]
        );
        if (result.rows.length === 0) return { ...DEFAULTS };
        let stored = result.rows[0].prefs;
        if (typeof stored === 'string') {
            try { stored = JSON.parse(stored); } catch { stored = {}; }
        }
        return { ...DEFAULTS, ...stored };
    } catch (err) {
        console.error('[DevicePrefs] getPrefs error:', err.message);
        return { ...DEFAULTS };
    }
}

async function updatePrefs(deviceId, prefs) {
    if (!pool) return;
    // Only allow known keys
    const filtered = {};
    for (const key of Object.keys(DEFAULTS)) {
        if (key in prefs) filtered[key] = !!prefs[key];
    }
    await pool.query(`
        INSERT INTO device_preferences (device_id, prefs, updated_at)
        VALUES ($1, $2, $3)
        ON CONFLICT (device_id) DO UPDATE
        SET prefs = device_preferences.prefs || $2::jsonb,
            updated_at = $3
    `, [deviceId, JSON.stringify(filtered), Date.now()]);
}

module.exports = { DEFAULTS, initTable, getPrefs, updatePrefs };
```

**Step 2: Wire up in index.js**

Add require at line ~17 (after `const notifModule = require('./notifications');`):
```javascript
const devicePrefs = require('./device-preferences');
```

Add init at line ~6397 (after `notifModule.initNotificationTables(chatPool);`):
```javascript
devicePrefs.initTable(chatPool);
```

**Step 3: Commit**

```bash
git add backend/device-preferences.js backend/index.js
git commit -m "feat: add device preferences module with DB table (#105)"
```

---

## Task 2: Device Preferences API Endpoints

**Files:**
- Modify: `backend/index.js` (add 2 endpoints near notification-preferences, lines ~6560-6580)

**Step 1: Add GET and PUT endpoints**

Insert after the `/api/notification-preferences` PUT endpoint (line ~6580):

```javascript
// ============================================
// DEVICE PREFERENCES (broadcast settings, etc.)
// ============================================
app.get('/api/device-preferences', async (req, res) => {
    const deviceId = authDevice(req);
    if (!deviceId) return res.status(401).json({ success: false, error: 'Unauthorized' });

    const prefs = await devicePrefs.getPrefs(deviceId);
    res.json({ success: true, prefs, defaults: devicePrefs.DEFAULTS });
});

app.put('/api/device-preferences', async (req, res) => {
    const deviceId = authDevice(req);
    if (!deviceId) return res.status(401).json({ success: false, error: 'Unauthorized' });

    const { prefs } = req.body;
    if (!prefs || typeof prefs !== 'object') {
        return res.status(400).json({ success: false, error: 'prefs object required' });
    }

    await devicePrefs.updatePrefs(deviceId, prefs);
    const updated = await devicePrefs.getPrefs(deviceId);
    res.json({ success: true, prefs: updated });
});
```

**Step 2: Commit**

```bash
git add backend/index.js
git commit -m "feat: add device-preferences API endpoints (GET/PUT) (#105)"
```

---

## Task 3: `buildBroadcastRecipientBlock()` Helper

**Files:**
- Modify: `backend/index.js` (add helper function near broadcast utilities, before `/api/entity/broadcast` at line ~3475)

**Step 1: Add the helper function**

Insert before the broadcast endpoint (around line ~3470):

```javascript
/**
 * Build [BROADCAST RECIPIENTS] context block for push messages.
 * @param {object} device - The device object
 * @param {number[]} recipientIds - All recipient entity IDs (excluding sender)
 * @param {number} currentEntityId - The entity receiving this particular push
 * @returns {string} The formatted block, or empty string
 */
function buildBroadcastRecipientBlock(device, recipientIds, currentEntityId) {
    if (recipientIds.length === 0) return '';
    let block = `[BROADCAST RECIPIENTS] This message was sent to ${recipientIds.length} entities:\n`;
    for (const id of recipientIds) {
        const entity = device.entities[id];
        if (!entity) continue;
        const nameStr = entity.name ? ` "${entity.name}"` : '';
        const youMarker = id === currentEntityId ? ' ← YOU' : '';
        block += `- entity_${id}${nameStr} (${entity.character || 'LOBSTER'})${youMarker}\n`;
    }
    return block + '\n';
}
```

**Step 2: Commit**

```bash
git add backend/index.js
git commit -m "feat: add buildBroadcastRecipientBlock() helper (#105)"
```

---

## Task 4: Inject Recipient Info into Bot Broadcast (`/api/entity/broadcast`)

**Files:**
- Modify: `backend/index.js` (lines ~3620-3650, inside the broadcast push message assembly)

**Step 1: Add preference check + injection**

At the top of the `results = targetIds.map(...)` callback (line ~3594), before the push message assembly, read the device preference. Then inject the block right before `[BROADCAST]` tag at line ~3640.

Before line 3594 (before the `const results = targetIds.map(...)` block), add:
```javascript
// Check device preference for broadcast recipient info
const broadcastPrefs = await devicePrefs.getPrefs(deviceId);
const showRecipientInfo = broadcastPrefs.broadcast_recipient_info !== false;
```

Note: This means the outer function must be async-aware. The `targetIds.map(...)` on line 3594 is synchronous. The preference read should happen once BEFORE the map loop.

Then at line ~3640 (where `pushMsg += '\n\n[BROADCAST]...`), change to:
```javascript
// Inject recipient info if enabled
if (showRecipientInfo) {
    pushMsg += '\n\n' + buildBroadcastRecipientBlock(device, targetIds, toId);
}
pushMsg += `[BROADCAST] From: ${sourceLabel}\n`;
```

(Remove the leading `\n\n` from the existing `[BROADCAST]` line since we now add it conditionally above — or keep it as-is if recipient block is empty.)

**Step 2: Commit**

```bash
git add backend/index.js
git commit -m "feat: inject recipient info into bot broadcast push (#105)"
```

---

## Task 5: Inject Recipient Info into User Broadcast (`/api/client/speak`)

**Files:**
- Modify: `backend/index.js` (lines ~2680-2730, inside the user broadcast push assembly)

**Step 1: Add preference check + injection**

This is trickier because `/api/client/speak` handles both single-entity and broadcast cases. The recipient block should ONLY be injected when `targetIds.length > 1` (actual broadcast).

Before the `pushPromises = targetIds.map(...)` at line ~2680, add:
```javascript
// Check device preference for broadcast recipient info (only for multi-target broadcasts)
let showRecipientInfo = false;
if (targetIds.length > 1) {
    const broadcastPrefs = await devicePrefs.getPrefs(deviceId);
    showRecipientInfo = broadcastPrefs.broadcast_recipient_info !== false;
}
```

Then at line ~2715 (where `pushMsg += '[MESSAGE]...`), inject before it:
```javascript
// Inject broadcast recipient info if this is a multi-target broadcast
if (showRecipientInfo) {
    pushMsg += buildBroadcastRecipientBlock(device, targetIds, eId);
}
```

**Step 2: Commit**

```bash
git add backend/index.js
git commit -m "feat: inject recipient info into user broadcast push (#105)"
```

---

## Task 6: Web Portal Toggle (`settings.html`)

**Files:**
- Modify: `backend/public/portal/settings.html` (add broadcast settings section after notifications card)
- Modify: `backend/public/shared/i18n.js` (add i18n keys for all locales)

**Step 1: Add Broadcast Settings card in settings.html**

Insert after the Notifications card closing `</div>` (line ~659) and before the Feedback card (line ~661):

```html
<!-- Broadcast Settings Card -->
<div class="card">
    <div class="card-title">
        <span>📡 <span data-i18n="broadcast_settings_title">Broadcast Settings</span></span>
    </div>
    <p class="section-desc" data-i18n="broadcast_settings_desc">Configure how broadcast messages work</p>
    <div class="setting-row">
        <span class="setting-row-label" data-i18n="broadcast_pref_recipient_info">Show recipient list in broadcasts</span>
        <label class="toggle-switch">
            <input type="checkbox" id="toggleBroadcastRecipientInfo" onchange="toggleDevicePref('broadcast_recipient_info', this.checked)">
            <span class="toggle-slider"></span>
        </label>
    </div>
</div>
```

**Step 2: Add JS functions**

In the `<script>` section, add after the notification preferences code (after line ~1129):

```javascript
// ============================================
// DEVICE PREFERENCES (broadcast settings)
// ============================================
async function loadDevicePreferences() {
    try {
        const data = await apiCall('GET', '/api/device-preferences');
        const prefs = data.prefs || {};
        const toggle = document.getElementById('toggleBroadcastRecipientInfo');
        if (toggle) toggle.checked = prefs.broadcast_recipient_info !== false;
    } catch (e) {
        console.error('Failed to load device preferences:', e);
    }
}

async function toggleDevicePref(key, enabled) {
    try {
        const prefs = {};
        prefs[key] = enabled;
        await apiCall('PUT', '/api/device-preferences', { prefs });
    } catch (e) {
        showToast('Failed to update preference', 'error');
    }
}
```

**Step 3: Call `loadDevicePreferences()` on page load**

Find the init section (around line ~807 where `loadLanguagePreference()` and `loadNotifPreferences()` are called) and add:
```javascript
loadDevicePreferences();
```

**Step 4: Add i18n keys to `backend/public/shared/i18n.js`**

For each locale block, add:
- **en:** `"broadcast_settings_title": "Broadcast Settings"`, `"broadcast_settings_desc": "Configure how broadcast messages work"`, `"broadcast_pref_recipient_info": "Show recipient list in broadcasts"`
- **zh-TW:** `"broadcast_settings_title": "廣播設定"`, `"broadcast_settings_desc": "設定廣播訊息的運作方式"`, `"broadcast_pref_recipient_info": "在廣播中顯示接收者列表"`
- **zh-CN:** `"broadcast_settings_title": "广播设置"`, `"broadcast_settings_desc": "配置广播消息的工作方式"`, `"broadcast_pref_recipient_info": "在广播中显示接收者列表"`
- **ja:** `"broadcast_settings_title": "ブロードキャスト設定"`, `"broadcast_settings_desc": "ブロードキャストメッセージの動作を設定"`, `"broadcast_pref_recipient_info": "ブロードキャストに受信者リストを表示"`
- **ko:** `"broadcast_settings_title": "방송 설정"`, `"broadcast_settings_desc": "방송 메시지 작동 방식 설정"`, `"broadcast_pref_recipient_info": "방송에 수신자 목록 표시"`
- **th:** `"broadcast_settings_title": "การตั้งค่าการแพร่ภาพ"`, `"broadcast_settings_desc": "กำหนดค่าการทำงานของข้อความแพร่ภาพ"`, `"broadcast_pref_recipient_info": "แสดงรายชื่อผู้รับในการแพร่ภาพ"`
- **vi:** `"broadcast_settings_title": "Cài đặt phát sóng"`, `"broadcast_settings_desc": "Cấu hình cách hoạt động của tin nhắn phát sóng"`, `"broadcast_pref_recipient_info": "Hiển thị danh sách người nhận trong phát sóng"`
- **id:** `"broadcast_settings_title": "Pengaturan Siaran"`, `"broadcast_settings_desc": "Konfigurasi cara kerja pesan siaran"`, `"broadcast_pref_recipient_info": "Tampilkan daftar penerima di siaran"`

**Step 5: Commit**

```bash
git add backend/public/portal/settings.html backend/public/shared/i18n.js
git commit -m "feat: add broadcast recipient info toggle to Web Portal settings (#105)"
```

---

## Task 7: Android Settings Toggle

**Files:**
- Modify: `app/src/main/java/com/hank/clawlive/data/remote/ClawApiService.kt` (add API methods + response class)
- Modify: `app/src/main/java/com/hank/clawlive/SettingsActivity.kt` (add broadcast settings section)
- Modify: `app/src/main/res/values/strings.xml` (add English strings)
- Modify: `app/src/main/res/values-zh-rTW/strings.xml` (add zh-TW strings)
- Modify all other `values-*/strings.xml` for other locales

**Step 1: Add API methods to `ClawApiService.kt`**

After line ~306 (after `updateNotificationPreferences`), add:

```kotlin
@GET("api/device-preferences")
suspend fun getDevicePreferences(
    @Query("deviceId") deviceId: String,
    @Query("deviceSecret") deviceSecret: String
): DevicePreferencesResponse

@PUT("api/device-preferences")
suspend fun updateDevicePreferences(@Body body: Map<String, @JvmSuppressWildcards Any>): ApiResponse
```

After `NotificationPreferencesResponse` (line ~598), add:

```kotlin
data class DevicePreferencesResponse(
    val success: Boolean,
    val prefs: Map<String, Boolean> = emptyMap(),
    val error: String? = null
)
```

**Step 2: Add broadcast settings to `SettingsActivity.kt`**

Follow the exact same pattern as notification preferences (lines 820-944). Add a "Broadcast Settings" expandable section with a single toggle for `broadcast_recipient_info`.

Add after the notification preferences section (before the closing `}` of the class, line ~945):

1. Add member variables for the broadcast settings UI container
2. Create `setupBroadcastPrefsSection()` — follows same pattern as notification prefs section
3. Add `loadBroadcastPreferences()` — calls `getDevicePreferences()`
4. Add `buildBroadcastPrefToggles()` — creates a single toggle for `broadcast_recipient_info`
5. Add `updateBroadcastPref()` — calls `updateDevicePreferences()`

**Step 3: Add string resources**

`values/strings.xml`:
```xml
<string name="broadcast_settings_title">Broadcast Settings</string>
<string name="broadcast_settings_desc">Configure how broadcast messages work</string>
<string name="broadcast_pref_recipient_info">Show recipient list in broadcasts</string>
```

`values-zh-rTW/strings.xml`:
```xml
<string name="broadcast_settings_title">廣播設定</string>
<string name="broadcast_settings_desc">設定廣播訊息的運作方式</string>
<string name="broadcast_pref_recipient_info">在廣播中顯示接收者列表</string>
```

Add equivalent strings for all other locales (ja, ko, zh-CN, th, vi, id).

**Step 4: Verify compilation**

```bash
JAVA_HOME="/c/Program Files/Android/Android Studio/jbr" ./gradlew.bat compileDebugKotlin
```
Expected: BUILD SUCCESSFUL

**Step 5: Commit**

```bash
git add app/
git commit -m "feat: add broadcast recipient info toggle to Android Settings (#105)"
```

---

## Task 8: Integration Test

**Files:**
- Modify: `backend/tests/test-broadcast.js` (add test case for recipient info)

**Step 1: Add test for recipient block format**

Add a test that:
1. Creates a device with 3 bound entities
2. Sends a broadcast from entity 0
3. Verifies the push message contains `[BROADCAST RECIPIENTS]` block
4. Verifies the block lists entity 1 and entity 2
5. Verifies `← YOU` is on the correct line for each recipient

Since `pushToBot` is fire-and-forget, the test should verify the helper function directly:

```javascript
// Test buildBroadcastRecipientBlock output format
const mockDevice = {
    entities: {
        0: { name: "Alpha", character: "LOBSTER", isBound: true },
        1: { name: "Beta", character: "PIG", isBound: true },
        2: { name: null, character: "LOBSTER", isBound: true }
    }
};
// Expected output for entity_1 receiving:
// [BROADCAST RECIPIENTS] This message was sent to 2 entities:
// - entity_1 "Beta" (PIG) ← YOU
// - entity_2 (LOBSTER)
```

**Step 2: Run test**

```bash
node backend/tests/test-broadcast.js
```
Expected: All tests pass

**Step 3: Commit**

```bash
git add backend/tests/test-broadcast.js
git commit -m "test: add broadcast recipient info block test (#105)"
```

---

## Task 9: Final Merge + Deploy

**Step 1: Merge to main and push**

```bash
git checkout main
git merge feature/broadcast-recipient-info
git push origin main
```

**Step 2: Verify deployment**

Railway auto-deploys from `backend/` on push to main. Verify the app starts successfully.

**Step 3: Close issue**

```bash
gh issue close 105 --comment "Implemented in main. Broadcast messages now include recipient info block."
```

---

## Summary of Files Changed

| File | Action | Description |
|------|--------|-------------|
| `backend/device-preferences.js` | Create | DB module for device-level preferences |
| `backend/index.js` | Modify | Wire module, add API endpoints, add helper, inject into both broadcast paths |
| `backend/public/portal/settings.html` | Modify | Add broadcast settings toggle card |
| `backend/public/shared/i18n.js` | Modify | Add i18n keys for 8 locales |
| `app/.../ClawApiService.kt` | Modify | Add API methods + response class |
| `app/.../SettingsActivity.kt` | Modify | Add broadcast settings section |
| `app/src/main/res/values*/strings.xml` | Modify | Add string resources (8 locales) |
| `backend/tests/test-broadcast.js` | Modify | Add recipient block test |
