// Claw Live Wallpaper Backend - Multi-Device Multi-Entity Support (v5)
// Each device has its own 4 entity slots (matrix architecture)
const express = require('express');
const cors = require('cors');
const fs = require('fs');
const path = require('path');
const app = express();
const port = process.env.PORT || 3000;

// Middleware
app.use(cors());
app.use(express.json());

// ============================================
// MATRIX ARCHITECTURE: devices[deviceId].entities[0-3]
// Each device has independent entity slots
// ============================================

const MAX_ENTITIES_PER_DEVICE = 4;

// Latest app version - update this with each release
// Bot will warn users if their app version is older than this
const LATEST_APP_VERSION = "1.0.3";

// Device registry - each device has its own entities
const devices = {};

// Pending binding codes (code -> { deviceId, entityId, expires })
const pendingBindings = {};

// Helper: Generate 6-digit binding code
function generateBindingCode() {
    let code;
    do {
        code = Math.floor(100000 + Math.random() * 900000).toString();
    } while (pendingBindings[code]); // Ensure unique
    return code;
}

// Helper: Generate secure bot secret (32 character hex string)
function generateBotSecret() {
    const chars = 'abcdef0123456789';
    let secret = '';
    for (let i = 0; i < 32; i++) {
        secret += chars.charAt(Math.floor(Math.random() * chars.length));
    }
    return secret;
}

// Helper: Create default entity
function createDefaultEntity(entityId) {
    return {
        entityId: entityId,
        botSecret: null,
        isBound: false,
        name: null, // Optional name set by bot (max 20 chars)
        character: "LOBSTER",
        state: "IDLE",
        message: `Entity #${entityId} waiting...`,
        parts: {},
        batteryLevel: 100,
        lastUpdated: Date.now(),
        messageQueue: [],
        webhook: null,
        appVersion: null // Device app version (e.g., "1.0.3")
    };
}

// Helper: Get version info for responses
function getVersionInfo(deviceAppVersion) {
    const isOutdated = deviceAppVersion && deviceAppVersion !== LATEST_APP_VERSION &&
        compareVersions(deviceAppVersion, LATEST_APP_VERSION) < 0;
    return {
        latestVersion: LATEST_APP_VERSION,
        deviceVersion: deviceAppVersion || null,
        isOutdated: isOutdated,
        versionWarning: isOutdated
            ? `App version ${deviceAppVersion} is outdated. Please update to v${LATEST_APP_VERSION} for best experience.`
            : null
    };
}

// Helper: Compare semantic versions (returns -1, 0, or 1)
function compareVersions(v1, v2) {
    const parts1 = v1.split('.').map(Number);
    const parts2 = v2.split('.').map(Number);
    for (let i = 0; i < Math.max(parts1.length, parts2.length); i++) {
        const p1 = parts1[i] || 0;
        const p2 = parts2[i] || 0;
        if (p1 < p2) return -1;
        if (p1 > p2) return 1;
    }
    return 0;
}

// Helper: Get or create device
function getOrCreateDevice(deviceId, deviceSecret = null) {
    if (!devices[deviceId]) {
        devices[deviceId] = {
            deviceId: deviceId,
            deviceSecret: deviceSecret,
            createdAt: Date.now(),
            entities: {}
        };
        // Initialize 4 entity slots
        for (let i = 0; i < MAX_ENTITIES_PER_DEVICE; i++) {
            devices[deviceId].entities[i] = createDefaultEntity(i);
        }
        console.log(`[Device] New device registered: ${deviceId}`);
    }
    return devices[deviceId];
}

// Helper: Get entity by deviceId and entityId
function getEntity(deviceId, entityId) {
    const device = devices[deviceId];
    if (!device) return null;
    return device.entities[entityId] || null;
}

// Helper: Load MCP skill documentation
function loadSkillDoc() {
    try {
        const docPath = path.join(__dirname, 'realbot_mcp_skill.md');
        return fs.readFileSync(docPath, 'utf8');
    } catch (err) {
        return "MCP Skill documentation not found.";
    }
}

// Auto-decay loop for ALL devices' entities
setInterval(() => {
    const now = Date.now();
    for (const deviceId in devices) {
        const device = devices[deviceId];
        for (let i = 0; i < MAX_ENTITIES_PER_DEVICE; i++) {
            const entity = device.entities[i];
            if (!entity || !entity.isBound) continue;

            // 1. Battery Decay
            if (entity.batteryLevel > 0) entity.batteryLevel -= 1;

            // 2. Random State Change (Idle vs Sleep) if no updates for 20s
            if (now - entity.lastUpdated > 20000) {
                if (Math.random() > 0.7) {
                    entity.state = "SLEEPING";
                    entity.message = "Zzz...";
                } else {
                    entity.state = "IDLE";
                    entity.message = "Waiting...";
                }
                entity.lastUpdated = now;
            }
        }
    }

    // Clean up expired binding codes
    for (const code in pendingBindings) {
        if (pendingBindings[code].expires < now) {
            delete pendingBindings[code];
        }
    }
}, 5000);

// ============================================
// ROUTES
// ============================================

app.get('/', (req, res) => {
    const deviceCount = Object.keys(devices).length;
    let boundCount = 0;
    for (const deviceId in devices) {
        for (let i = 0; i < MAX_ENTITIES_PER_DEVICE; i++) {
            if (devices[deviceId].entities[i]?.isBound) boundCount++;
        }
    }
    res.send(`Claw Backend Running! Devices: ${deviceCount}, Bound Entities: ${boundCount}`);
});

// Health check endpoint for Railway
app.get('/api/health', (req, res) => {
    res.status(200).json({ status: 'ok', timestamp: Date.now() });
});

// ============================================
// DEVICE REGISTRATION (Android App)
// ============================================

/**
 * POST /api/device/register
 * Android app registers a specific entity slot and gets binding code.
 * Body: { entityId: 0-3, deviceId: "...", deviceSecret: "..." }
 *
 * NEW: Each device now has its own 4 entity slots!
 */
app.post('/api/device/register', (req, res) => {
    const { entityId, deviceId, deviceSecret, appVersion } = req.body;

    // Validate entityId
    const eId = parseInt(entityId);
    if (isNaN(eId) || eId < 0 || eId >= MAX_ENTITIES_PER_DEVICE) {
        return res.status(400).json({
            success: false,
            message: `Invalid entityId. Must be 0-${MAX_ENTITIES_PER_DEVICE - 1}`
        });
    }

    if (!deviceId || !deviceSecret) {
        return res.status(400).json({
            success: false,
            message: "deviceId and deviceSecret required"
        });
    }

    // Get or create device
    const device = getOrCreateDevice(deviceId, deviceSecret);

    // Verify device secret if device already exists
    if (device.deviceSecret && device.deviceSecret !== deviceSecret) {
        return res.status(403).json({
            success: false,
            message: "Invalid deviceSecret for this device"
        });
    }

    // Generate new binding code
    const code = generateBindingCode();
    const expires = Date.now() + (5 * 60 * 1000); // 5 minutes

    // Store pending binding (include appVersion for tracking)
    pendingBindings[code] = {
        deviceId: deviceId,
        entityId: eId,
        expires: expires,
        appVersion: appVersion || null
    };

    // Store appVersion on entity (update even if already bound)
    if (appVersion) {
        device.entities[eId].appVersion = appVersion;
    }

    console.log(`[Register] Device ${deviceId} Entity ${eId}: Code ${code} (app v${appVersion || 'unknown'})`);

    res.json({
        success: true,
        deviceId: deviceId,
        entityId: eId,
        bindingCode: code,
        expiresIn: 300, // 5 minutes in seconds
        versionInfo: getVersionInfo(appVersion)
    });
});

/**
 * POST /api/device/status
 * Android app polls for entity status using deviceId + secret.
 * Body: { entityId: 0-3, deviceId: "...", deviceSecret: "..." }
 */
app.post('/api/device/status', (req, res) => {
    const { entityId, deviceId, deviceSecret, appVersion } = req.body;

    const eId = parseInt(entityId);
    if (isNaN(eId) || eId < 0 || eId >= MAX_ENTITIES_PER_DEVICE) {
        return res.status(400).json({ success: false, message: "Invalid entityId" });
    }

    const device = devices[deviceId];
    if (!device) {
        return res.status(404).json({ success: false, message: "Device not found" });
    }

    // Verify device secret
    if (device.deviceSecret !== deviceSecret) {
        return res.status(403).json({ success: false, message: "Unauthorized" });
    }

    const entity = device.entities[eId];

    // Update app version on entity (track latest version seen)
    if (appVersion) {
        entity.appVersion = appVersion;
    }

    res.json({
        deviceId: deviceId,
        entityId: entity.entityId,
        name: entity.name,
        character: entity.character,
        state: entity.state,
        message: entity.message,
        parts: entity.parts,
        batteryLevel: entity.batteryLevel,
        lastUpdated: entity.lastUpdated,
        isBound: entity.isBound,
        versionInfo: getVersionInfo(appVersion || entity.appVersion)
    });
});

// ============================================
// BOT BINDING (OpenClaw)
// ============================================

/**
 * POST /api/bind
 * Bot binds to a specific entity using binding code.
 * Body: { code: "123456", name: "optional name (max 20 chars)" }
 *
 * The binding code maps to a specific device + entity combination.
 */
app.post('/api/bind', (req, res) => {
    const { code, name } = req.body;

    if (!code) {
        return res.status(400).json({ success: false, message: "Binding code required" });
    }

    // Validate name length if provided
    if (name && name.length > 20) {
        return res.status(400).json({ success: false, message: "Name must be 20 characters or less" });
    }

    const binding = pendingBindings[code];
    if (!binding || binding.expires < Date.now()) {
        delete pendingBindings[code];
        return res.status(400).json({
            success: false,
            message: "Invalid or expired binding code"
        });
    }

    const { deviceId, entityId } = binding;
    const device = devices[deviceId];
    if (!device) {
        return res.status(400).json({ success: false, message: "Device not found" });
    }

    const entity = device.entities[entityId];

    // Generate bot secret for this binding
    const botSecret = generateBotSecret();

    // Mark as bound
    entity.isBound = true;
    entity.botSecret = botSecret;
    entity.name = name || null; // Set name if provided
    entity.state = "IDLE";
    entity.message = "Connected!";
    entity.lastUpdated = Date.now();

    // Get app version from pending binding (stored when device registered)
    const deviceAppVersion = binding.appVersion || entity.appVersion;

    // Clear used binding code
    delete pendingBindings[code];

    console.log(`[Bind] Device ${deviceId} Entity ${entityId} bound with botSecret${name ? ` (name: ${name})` : ''} (app v${deviceAppVersion || 'unknown'})`);

    res.json({
        success: true,
        message: `Device ${deviceId} Entity ${entityId} bound successfully`,
        deviceId: deviceId,
        entityId: entityId,
        botSecret: botSecret, // Bot must save this!
        name: entity.name,
        deviceInfo: {
            deviceId: deviceId,
            entityId: entityId,
            status: "ONLINE"
        },
        versionInfo: getVersionInfo(deviceAppVersion),
        skills_documentation: loadSkillDoc()
    });
});

// ============================================
// ENTITY STATUS & CONTROL
// ============================================

/**
 * GET /api/entities
 * Get all bound entities across all devices.
 * Query: ?deviceId=xxx (optional, filter by device)
 */
app.get('/api/entities', (req, res) => {
    const filterDeviceId = req.query.deviceId;
    const entities = [];

    for (const deviceId in devices) {
        if (filterDeviceId && deviceId !== filterDeviceId) continue;

        const device = devices[deviceId];
        for (let i = 0; i < MAX_ENTITIES_PER_DEVICE; i++) {
            const entity = device.entities[i];
            if (entity.isBound) {
                entities.push({
                    deviceId: deviceId,
                    entityId: entity.entityId,
                    name: entity.name,
                    character: entity.character,
                    state: entity.state,
                    message: entity.message,
                    parts: entity.parts,
                    batteryLevel: entity.batteryLevel,
                    lastUpdated: entity.lastUpdated,
                    isBound: true  // Always true since we only return bound entities
                });
            }
        }
    }

    res.json({
        entities: entities,
        activeCount: entities.length,
        deviceCount: Object.keys(devices).length,
        maxEntitiesPerDevice: MAX_ENTITIES_PER_DEVICE
    });
});

/**
 * GET /api/status
 * Get status for specific device + entity.
 * Query: ?deviceId=xxx&entityId=0&appVersion=1.0.3
 */
app.get('/api/status', (req, res) => {
    const deviceId = req.query.deviceId;
    const eId = parseInt(req.query.entityId) || 0;
    const appVersion = req.query.appVersion;

    if (!deviceId) {
        return res.status(400).json({ success: false, message: "deviceId required" });
    }

    if (eId < 0 || eId >= MAX_ENTITIES_PER_DEVICE) {
        return res.status(400).json({ success: false, message: "Invalid entityId" });
    }

    const device = devices[deviceId];
    if (!device) {
        return res.status(404).json({ success: false, message: "Device not found" });
    }

    const entity = device.entities[eId];

    // Update app version on entity (track latest version seen from this device)
    if (appVersion) {
        entity.appVersion = appVersion;
    }

    res.json({
        deviceId: deviceId,
        entityId: entity.entityId,
        name: entity.name,
        character: entity.character,
        state: entity.state,
        message: entity.message,
        parts: entity.parts,
        batteryLevel: entity.batteryLevel,
        lastUpdated: entity.lastUpdated,
        isBound: entity.isBound,
        versionInfo: getVersionInfo(appVersion || entity.appVersion)
    });
});

/**
 * POST /api/transform
 * Update entity status (Bot uses this).
 * Body: { deviceId, entityId: 0-3, botSecret, name, character, state, message, parts }
 * REQUIRES botSecret for authentication!
 */
app.post('/api/transform', (req, res) => {
    const { deviceId, entityId, botSecret, name, character, state, message, parts } = req.body;

    if (!deviceId) {
        return res.status(400).json({ success: false, message: "deviceId required" });
    }

    const eId = parseInt(entityId) || 0;
    if (eId < 0 || eId >= MAX_ENTITIES_PER_DEVICE) {
        return res.status(400).json({ success: false, message: "Invalid entityId" });
    }

    const device = devices[deviceId];
    if (!device) {
        return res.status(404).json({ success: false, message: "Device not found" });
    }

    const entity = device.entities[eId];

    if (!entity.isBound) {
        return res.status(400).json({
            success: false,
            message: `Device ${deviceId} Entity ${eId} is not bound yet`
        });
    }

    // Verify botSecret
    if (!botSecret || botSecret !== entity.botSecret) {
        return res.status(403).json({
            success: false,
            message: "Invalid or missing botSecret"
        });
    }

    // Validate and update name if provided
    if (name !== undefined) {
        if (name && name.length > 20) {
            return res.status(400).json({ success: false, message: "Name must be 20 characters or less" });
        }
        entity.name = name || null;
    }

    if (character) entity.character = character;
    if (state) entity.state = state;
    if (message !== undefined) entity.message = message;
    if (parts) entity.parts = { ...entity.parts, ...parts };
    if (entity.batteryLevel < 10) entity.batteryLevel = 100;

    entity.lastUpdated = Date.now();

    console.log(`[Transform] Device ${deviceId} Entity ${eId}: ${state || entity.state} - "${message || entity.message}"`);

    res.json({
        success: true,
        deviceId: deviceId,
        entityId: eId,
        currentState: {
            name: entity.name,
            character: entity.character,
            state: entity.state,
            message: entity.message,
            parts: entity.parts
        },
        versionInfo: getVersionInfo(entity.appVersion)
    });
});

// POST /api/wakeup - REMOVED (client-side wakeup retained without push)

/**
 * DELETE /api/entity
 * Remove/unbind an entity.
 * Body/Query: { deviceId, entityId, botSecret }
 */
app.delete('/api/entity', (req, res) => {
    const deviceId = req.body.deviceId || req.query.deviceId;
    const entityId = req.body.entityId ?? req.query.entityId;
    const botSecret = req.body.botSecret || req.query.botSecret;

    if (!deviceId) {
        return res.status(400).json({ success: false, message: "deviceId required" });
    }

    const eId = parseInt(entityId);
    if (isNaN(eId) || eId < 0 || eId >= MAX_ENTITIES_PER_DEVICE) {
        return res.status(400).json({ success: false, message: "Invalid entityId" });
    }

    const device = devices[deviceId];
    if (!device) {
        return res.status(404).json({ success: false, message: "Device not found" });
    }

    const entity = device.entities[eId];

    if (!entity.isBound) {
        return res.status(400).json({ success: false, message: `Entity ${eId} is not bound` });
    }

    // Verify botSecret
    if (!botSecret || botSecret !== entity.botSecret) {
        return res.status(403).json({ success: false, message: "Invalid botSecret" });
    }

    // Reset entity to unbound state
    device.entities[eId] = createDefaultEntity(eId);

    console.log(`[Remove] Device ${deviceId} Entity ${eId} unbound`);
    res.json({ success: true, message: `Entity ${eId} removed` });
});

// ============================================
// CLIENT MESSAGING
// ============================================

/**
 * POST /api/client/speak
 * Client sends message to entity (stored in entity's queue).
 * Body: { deviceId, entityId, text, source }
 *
 * entityId can be:
 *   - number: single entity (e.g., 0)
 *   - array: broadcast to multiple entities (e.g., [0, 1, 2])
 *   - "all": broadcast to ALL bound entities
 *
 * If bot has registered webhook, push notification is sent.
 */
app.post('/api/client/speak', async (req, res) => {
    const { deviceId, entityId, text, source = "client" } = req.body;

    if (!deviceId) {
        return res.status(400).json({ success: false, message: "deviceId required" });
    }

    const device = devices[deviceId];
    if (!device) {
        return res.status(404).json({ success: false, message: "Device not found" });
    }

    // Determine target entity IDs
    let targetIds = [];
    if (entityId === "all") {
        // Broadcast to all bound entities
        for (let i = 0; i < MAX_ENTITIES_PER_DEVICE; i++) {
            if (device.entities[i] && device.entities[i].isBound) {
                targetIds.push(i);
            }
        }
    } else if (Array.isArray(entityId)) {
        // Array of entity IDs
        targetIds = entityId.map(id => parseInt(id)).filter(id => id >= 0 && id < MAX_ENTITIES_PER_DEVICE);
    } else {
        // Single entity ID
        const eId = parseInt(entityId) || 0;
        if (eId >= 0 && eId < MAX_ENTITIES_PER_DEVICE) {
            targetIds.push(eId);
        }
    }

    if (targetIds.length === 0) {
        return res.status(400).json({ success: false, message: "No valid target entities" });
    }

    // Parallel processing for broadcast - all entities receive message simultaneously
    const pushPromises = targetIds.map(async (eId) => {
        const entity = device.entities[eId];
        if (!entity) return null;

        entity.message = `Received: "${text}"`;
        entity.lastUpdated = Date.now();

        const messageObj = {
            text: text,
            from: source,
            timestamp: Date.now(),
            read: false
        };
        entity.messageQueue.push(messageObj);

        console.log(`[Client] Device ${deviceId} -> Entity ${eId}: "${text}" (source: ${source})`);

        // Push to bot if webhook is registered
        let pushResult = { pushed: false, reason: "no_webhook" };
        if (entity.webhook) {
            pushResult = await pushToBot(entity, deviceId, "new_message", {
                message: `[Device ${deviceId} Entity ${eId} 收到新訊息]\n來源: ${source}\n內容: ${text}`
            });

            if (pushResult.pushed) {
                messageObj.delivered = true;
            }
        }

        return {
            entityId: eId,
            pushed: pushResult.pushed,
            mode: entity.webhook ? "push" : "polling"
        };
    });

    // Wait for all push operations to complete in parallel
    const results = (await Promise.all(pushPromises)).filter(r => r !== null);

    res.json({
        success: true,
        message: `Sent to ${results.length} entity(s)`,
        targets: results,
        broadcast: targetIds.length > 1
    });
});

/**
 * POST /api/entity/speak-to
 * Entity-to-entity messaging within the same device.
 * Body: { deviceId, fromEntityId, toEntityId, botSecret, text }
 *
 * Requires botSecret of the SENDING entity for authentication.
 * The receiving entity gets the message with source marked as the sending entity.
 */
app.post('/api/entity/speak-to', async (req, res) => {
    const { deviceId, fromEntityId, toEntityId, botSecret, text } = req.body;

    if (!deviceId) {
        return res.status(400).json({ success: false, message: "deviceId required" });
    }

    const device = devices[deviceId];
    if (!device) {
        return res.status(404).json({ success: false, message: "Device not found" });
    }

    const fromId = parseInt(fromEntityId);
    const toId = parseInt(toEntityId);

    if (fromId < 0 || fromId >= MAX_ENTITIES_PER_DEVICE) {
        return res.status(400).json({ success: false, message: "Invalid fromEntityId" });
    }
    if (toId < 0 || toId >= MAX_ENTITIES_PER_DEVICE) {
        return res.status(400).json({ success: false, message: "Invalid toEntityId" });
    }
    if (fromId === toId) {
        return res.status(400).json({ success: false, message: "Cannot send message to self" });
    }

    const fromEntity = device.entities[fromId];
    const toEntity = device.entities[toId];

    // Verify botSecret of sending entity
    if (!fromEntity.isBound || fromEntity.botSecret !== botSecret) {
        return res.status(403).json({ success: false, message: "Invalid botSecret for sending entity" });
    }

    // Target entity must be bound
    if (!toEntity.isBound) {
        return res.status(400).json({ success: false, message: `Entity ${toId} is not bound` });
    }

    // Create message source identifier
    const sourceLabel = `entity:${fromId}:${fromEntity.character}`;

    toEntity.message = `From Entity ${fromId}: "${text}"`;
    toEntity.lastUpdated = Date.now();

    const messageObj = {
        text: text,
        from: sourceLabel,
        fromEntityId: fromId,
        fromCharacter: fromEntity.character,
        timestamp: Date.now(),
        read: false
    };
    toEntity.messageQueue.push(messageObj);

    console.log(`[Entity] Device ${deviceId} Entity ${fromId} -> Entity ${toId}: "${text}"`);

    // Push to target bot if webhook is registered
    let pushResult = { pushed: false, reason: "no_webhook" };
    if (toEntity.webhook) {
        pushResult = await pushToBot(toEntity, deviceId, "entity_message", {
            message: `[Device ${deviceId} Entity ${toId} 收到新訊息]\n來源: ${sourceLabel}\n內容: ${text}`
        });

        if (pushResult.pushed) {
            messageObj.delivered = true;
        }
    }

    res.json({
        success: true,
        message: `Message sent from Entity ${fromId} to Entity ${toId}`,
        from: { entityId: fromId, character: fromEntity.character },
        to: { entityId: toId, character: toEntity.character },
        pushed: pushResult.pushed,
        mode: toEntity.webhook ? "push" : "polling"
    });
});

/**
 * POST /api/entity/broadcast
 * Broadcast message from one entity to all other bound entities on the same device.
 * Body: { deviceId, fromEntityId, botSecret, text }
 *
 * Requires botSecret of the SENDING entity for authentication.
 * All other bound entities on the same device will receive the message.
 */
app.post('/api/entity/broadcast', async (req, res) => {
    const { deviceId, fromEntityId, botSecret, text } = req.body;

    if (!deviceId) {
        return res.status(400).json({ success: false, message: "deviceId required" });
    }
    if (!text) {
        return res.status(400).json({ success: false, message: "text required" });
    }

    const device = devices[deviceId];
    if (!device) {
        return res.status(404).json({ success: false, message: "Device not found" });
    }

    const fromId = parseInt(fromEntityId);
    if (isNaN(fromId) || fromId < 0 || fromId >= MAX_ENTITIES_PER_DEVICE) {
        return res.status(400).json({ success: false, message: "Invalid fromEntityId" });
    }

    const fromEntity = device.entities[fromId];

    // Verify botSecret of sending entity
    if (!fromEntity.isBound || fromEntity.botSecret !== botSecret) {
        return res.status(403).json({ success: false, message: "Invalid botSecret for sending entity" });
    }

    // Create message source identifier
    const sourceLabel = `entity:${fromId}:${fromEntity.character}`;

    // Find all other bound entities and send in parallel
    const targetIds = [];
    for (let i = 0; i < MAX_ENTITIES_PER_DEVICE; i++) {
        if (i !== fromId && device.entities[i].isBound) {
            targetIds.push(i);
        }
    }

    if (targetIds.length === 0) {
        return res.json({
            success: true,
            message: "No other bound entities to broadcast to",
            from: { entityId: fromId, character: fromEntity.character },
            sentCount: 0,
            results: []
        });
    }

    console.log(`[Broadcast] Device ${deviceId} Entity ${fromId} -> Entities [${targetIds.join(',')}]: "${text}"`);

    // Parallel processing - send to all entities simultaneously
    const pushPromises = targetIds.map(async (toId) => {
        const toEntity = device.entities[toId];

        toEntity.message = `From Entity ${fromId}: "${text}"`;
        toEntity.lastUpdated = Date.now();

        const messageObj = {
            text: text,
            from: sourceLabel,
            fromEntityId: fromId,
            fromCharacter: fromEntity.character,
            timestamp: Date.now(),
            read: false
        };
        toEntity.messageQueue.push(messageObj);

        // Push to target bot if webhook is registered
        let pushResult = { pushed: false, reason: "no_webhook" };
        if (toEntity.webhook) {
            pushResult = await pushToBot(toEntity, deviceId, "entity_broadcast", {
                message: `[Device ${deviceId} Entity ${toId} 收到廣播]\n來源: ${sourceLabel}\n內容: ${text}`
            });

            if (pushResult.pushed) {
                messageObj.delivered = true;
            }
        }

        return {
            entityId: toId,
            character: toEntity.character,
            pushed: pushResult.pushed,
            mode: toEntity.webhook ? "push" : "polling"
        };
    });

    // Wait for all push operations to complete in parallel
    const results = await Promise.all(pushPromises);
    const pushedCount = results.filter(r => r.pushed).length;

    res.json({
        success: true,
        message: `Broadcast sent from Entity ${fromId} to ${results.length} entities`,
        from: { entityId: fromId, character: fromEntity.character },
        sentCount: results.length,
        pushedCount: pushedCount,
        results: results
    });
});

/**
 * GET /api/client/pending
 * Get pending messages for entity.
 * Query: ?deviceId=xxx&entityId=0&botSecret=xxx
 *
 * Without botSecret: returns count only (peek mode)
 * With valid botSecret: returns and consumes messages
 */
app.get('/api/client/pending', (req, res) => {
    const deviceId = req.query.deviceId;
    const eId = parseInt(req.query.entityId) || 0;
    const botSecret = req.query.botSecret || req.headers['x-bot-secret'];

    if (!deviceId) {
        return res.status(400).json({ success: false, message: "deviceId required" });
    }

    if (eId < 0 || eId >= MAX_ENTITIES_PER_DEVICE) {
        return res.status(400).json({ success: false, message: "Invalid entityId" });
    }

    const device = devices[deviceId];
    if (!device) {
        return res.status(404).json({ success: false, message: "Device not found" });
    }

    const entity = device.entities[eId];
    const pending = entity.messageQueue.filter(m => !m.read);

    // Without botSecret: peek mode (count only, don't consume)
    if (!botSecret) {
        return res.json({
            deviceId: deviceId,
            entityId: eId,
            count: pending.length,
            messages: [],
            note: "Provide botSecret to retrieve and consume messages"
        });
    }

    // Verify botSecret matches
    if (entity.botSecret !== botSecret) {
        return res.status(403).json({
            success: false,
            message: "Invalid botSecret for this entity"
        });
    }

    // Log when messages are consumed (authenticated)
    if (pending.length > 0) {
        console.log(`[Pending] Device ${deviceId} Entity ${eId}: ${pending.length} messages consumed`);
    }

    // Mark as read (consume)
    pending.forEach(m => m.read = true);

    res.json({
        deviceId: deviceId,
        entityId: eId,
        count: pending.length,
        messages: pending,
        versionInfo: getVersionInfo(entity.appVersion)
    });
});

// ============================================
// DEBUG ENDPOINTS
// ============================================

/**
 * GET /api/debug/devices
 * Show all devices and their entities (for debugging).
 */
app.get('/api/debug/devices', (req, res) => {
    const result = [];
    for (const deviceId in devices) {
        const device = devices[deviceId];
        const entities = [];
        for (let i = 0; i < MAX_ENTITIES_PER_DEVICE; i++) {
            const e = device.entities[i];
            entities.push({
                entityId: i,
                isBound: e.isBound,
                name: e.name,
                character: e.character,
                state: e.state,
                message: e.message,
                messageQueueLength: e.messageQueue?.length || 0,
                unreadMessages: e.messageQueue?.filter(m => !m.read).length || 0,
                webhookRegistered: e.webhook !== null,
                mode: e.webhook ? "push" : "polling"
            });
        }
        result.push({
            deviceId: deviceId,
            createdAt: device.createdAt,
            entities: entities
        });
    }
    res.json({
        deviceCount: result.length,
        pendingBindings: Object.keys(pendingBindings).length,
        devices: result
    });
});

/**
 * POST /api/debug/reset
 * Reset all devices (for testing).
 */
app.post('/api/debug/reset', (req, res) => {
    for (const deviceId in devices) {
        delete devices[deviceId];
    }
    for (const code in pendingBindings) {
        delete pendingBindings[code];
    }
    console.log("[Debug] All devices reset");
    res.json({ success: true, message: "All devices reset" });
});

// ============================================
// BOT WEBHOOK REGISTRATION (Push Mode)
// ============================================

/**
 * POST /api/bot/register
 * Bot registers its webhook URL to receive push notifications.
 * Body: { deviceId, entityId, botSecret, webhook_url, token, session_key }
 */
app.post('/api/bot/register', (req, res) => {
    const { deviceId, entityId, botSecret, webhook_url, token, session_key } = req.body;

    if (!deviceId) {
        return res.status(400).json({ success: false, message: "deviceId required" });
    }

    const eId = parseInt(entityId) || 0;
    if (eId < 0 || eId >= MAX_ENTITIES_PER_DEVICE) {
        return res.status(400).json({ success: false, message: "Invalid entityId" });
    }

    const device = devices[deviceId];
    if (!device) {
        return res.status(404).json({ success: false, message: "Device not found" });
    }

    const entity = device.entities[eId];

    if (!entity.isBound) {
        return res.status(400).json({
            success: false,
            message: `Entity ${eId} is not bound yet. Bind first using /api/bind`
        });
    }

    // Verify botSecret
    if (!botSecret || botSecret !== entity.botSecret) {
        return res.status(403).json({ success: false, message: "Invalid botSecret" });
    }

    // Validate required fields
    if (!webhook_url || !token || !session_key) {
        return res.status(400).json({
            success: false,
            message: "Missing required fields: webhook_url, token, session_key"
        });
    }

    // Store webhook info
    entity.webhook = {
        url: webhook_url,
        token: token,
        sessionKey: session_key,
        registeredAt: Date.now()
    };

    console.log(`[Bot Register] Device ${deviceId} Entity ${eId} webhook registered: ${webhook_url}`);

    res.json({
        success: true,
        message: "Webhook registered. You will now receive push notifications.",
        deviceId: deviceId,
        entityId: eId,
        mode: "push"
    });
});

/**
 * DELETE /api/bot/register
 * Bot unregisters its webhook (switch back to polling mode).
 */
app.delete('/api/bot/register', (req, res) => {
    const deviceId = req.body.deviceId || req.query.deviceId;
    const entityId = req.body.entityId ?? req.query.entityId;
    const botSecret = req.body.botSecret || req.query.botSecret;

    if (!deviceId) {
        return res.status(400).json({ success: false, message: "deviceId required" });
    }

    const eId = parseInt(entityId) || 0;
    if (eId < 0 || eId >= MAX_ENTITIES_PER_DEVICE) {
        return res.status(400).json({ success: false, message: "Invalid entityId" });
    }

    const device = devices[deviceId];
    if (!device) {
        return res.status(404).json({ success: false, message: "Device not found" });
    }

    const entity = device.entities[eId];

    if (!botSecret || botSecret !== entity.botSecret) {
        return res.status(403).json({ success: false, message: "Invalid botSecret" });
    }

    entity.webhook = null;
    console.log(`[Bot Register] Device ${deviceId} Entity ${eId} webhook removed`);

    res.json({
        success: true,
        message: "Webhook removed. Switching back to polling mode.",
        deviceId: deviceId,
        entityId: eId,
        mode: "polling"
    });
});

/**
 * Helper: Push notification to bot webhook
 * Supports OpenClaw format: POST to /tools/invoke with tool invocation payload
 */
async function pushToBot(entity, deviceId, eventType, payload) {
    if (!entity.webhook) {
        return { pushed: false, reason: "no_webhook" };
    }

    const { url, token, sessionKey } = entity.webhook;

    try {
        // OpenClaw /tools/invoke format
        const response = await fetch(url, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                tool: "sessions_send",
                args: {
                    sessionKey: sessionKey,
                    message: payload.message || JSON.stringify(payload)
                }
            })
        });

        if (response.ok) {
            console.log(`[Push] Device ${deviceId} Entity ${entity.entityId}: ${eventType} pushed successfully`);
            return { pushed: true };
        } else {
            const errorText = await response.text().catch(() => '');
            console.log(`[Push] Device ${deviceId} Entity ${entity.entityId}: Push failed with status ${response.status} - ${errorText}`);
            return { pushed: false, reason: `http_${response.status}` };
        }
    } catch (err) {
        console.error(`[Push] Device ${deviceId} Entity ${entity.entityId}: Push error:`, err.message);
        return { pushed: false, reason: err.message };
    }
}

// Error Handling
app.use((err, req, res, next) => {
    if (err instanceof SyntaxError && err.status === 400 && 'body' in err) {
        return res.status(400).json({ success: false, message: "Invalid JSON format" });
    }
    next();
});

app.listen(port, () => {
    console.log(`Claw Backend v5 (Matrix Architecture) running on port ${port}`);
    console.log(`Max entities per device: ${MAX_ENTITIES_PER_DEVICE}`);
});
