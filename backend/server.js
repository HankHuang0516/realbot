const express = require('express');
const cors = require('cors');
const app = express();
const port = process.env.PORT || 3000;

// Middleware
app.use(cors());
app.use(express.json());

// ============================================
// MULTI-ENTITY DATA MODEL
// ============================================
const MAX_ENTITIES = 4;

// Create default entity template
const createDefaultEntity = (id, character = "LOBSTER") => ({
    entityId: id,
    character: character,
    state: "IDLE",
    message: id === 0 ? "System Online" : `Entity ${id} spawned!`,
    parts: {},
    batteryLevel: 100,
    lastUpdated: Date.now()
});

// Multi-entity storage (entity 0 always exists)
let entities = {
    0: createDefaultEntity(0)
};

// Per-entity message queues
let entityMessageQueues = {
    0: []
};

// Legacy: userMessages for backward compatibility
let userMessages = [];

// Helper: Get entity or null
const getEntity = (id) => entities[id] || null;

// Helper: Get all active entities as array
const getActiveEntities = () => Object.values(entities);

// ============================================
// AUTO-DECAY FOR ALL ENTITIES
// ============================================
setInterval(() => {
    const now = Date.now();

    Object.values(entities).forEach(entity => {
        // 1. Battery Decay
        if (entity.batteryLevel > 0) {
            entity.batteryLevel -= 1;
        }

        // 2. Random State Change (Idle vs Sleep) after 20s inactivity
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
    });
}, 5000);

// ============================================
// ROUTES
// ============================================

app.get('/', (req, res) => {
    res.json({
        status: 'ok',
        message: 'Realbot Backend is running',
        version: '2.0.0',
        timestamp: new Date().toISOString()
    });
});

// Railway Health Check
app.get('/api/health', (req, res) => {
    res.json({ status: 'healthy', timestamp: new Date().toISOString() });
});

// ============================================
// ENTITY MANAGEMENT ENDPOINTS
// ============================================

/**
 * GET /api/entities
 * Returns all active entities
 */
app.get('/api/entities', (req, res) => {
    res.json({
        entities: getActiveEntities(),
        activeCount: getActiveEntities().length,
        maxEntities: MAX_ENTITIES
    });
});

/**
 * POST /api/entity/spawn
 * Creates a new entity (1-3)
 */
app.post('/api/entity/spawn', (req, res) => {
    const { entityId, character, message } = req.body;

    // Auto-assign ID if not provided
    let id = entityId;
    if (id === undefined || id === null) {
        for (let i = 1; i < MAX_ENTITIES; i++) {
            if (!entities[i]) {
                id = i;
                break;
            }
        }
    }

    if (id === undefined || id === null) {
        return res.status(400).json({
            success: false,
            message: "No available entity slots (max 4)"
        });
    }

    if (id < 0 || id >= MAX_ENTITIES) {
        return res.status(400).json({
            success: false,
            message: `Entity ID must be 0-${MAX_ENTITIES - 1}`
        });
    }

    if (entities[id]) {
        return res.status(400).json({
            success: false,
            message: `Entity ${id} already exists`
        });
    }

    // Create new entity
    entities[id] = createDefaultEntity(id, character || "LOBSTER");
    if (message) entities[id].message = message;
    entityMessageQueues[id] = [];

    console.log(`[Entity] Spawned entity ${id} as ${entities[id].character}`);

    res.json({
        success: true,
        entity: entities[id],
        activeCount: getActiveEntities().length
    });
});

/**
 * DELETE /api/entity/:id
 * Removes an entity (cannot remove entity 0)
 */
app.delete('/api/entity/:id', (req, res) => {
    const id = parseInt(req.params.id);

    if (id === 0) {
        return res.status(400).json({
            success: false,
            message: "Cannot remove entity 0 (primary entity)"
        });
    }

    if (!entities[id]) {
        return res.status(404).json({
            success: false,
            message: `Entity ${id} not found`
        });
    }

    delete entities[id];
    delete entityMessageQueues[id];

    console.log(`[Entity] Removed entity ${id}`);

    res.json({
        success: true,
        message: `Entity ${id} removed`,
        activeCount: getActiveEntities().length
    });
});

// ============================================
// STATUS ENDPOINTS (Backward Compatible)
// ============================================

/**
 * GET /api/status
 * Returns entity status. Supports ?entityId=X and ?all=true
 */
app.get('/api/status', (req, res) => {
    const { entityId, all } = req.query;

    if (all === 'true') {
        return res.json({
            entities: getActiveEntities(),
            activeCount: getActiveEntities().length
        });
    }

    const id = entityId !== undefined ? parseInt(entityId) : 0;
    const entity = getEntity(id);

    if (!entity) {
        return res.status(404).json({
            success: false,
            message: `Entity ${id} not found`
        });
    }

    // Return without entityId for backward compatibility when id=0
    if (id === 0) {
        const { entityId: _, ...legacyState } = entity;
        return res.json(legacyState);
    }

    res.json(entity);
});

/**
 * POST /api/wakeup
 * Webhook to wake up entity (supports ?entityId=X)
 */
app.post('/api/wakeup', (req, res) => {
    const id = req.query.entityId !== undefined ? parseInt(req.query.entityId) : 0;
    const entity = getEntity(id);

    if (!entity) {
        return res.status(404).json({ success: false, message: `Entity ${id} not found` });
    }

    console.log(`[Webhook] Wake Up Signal for Entity ${id}!`);

    entity.state = "EXCITED";
    entity.message = "I'm Awake!";
    entity.lastUpdated = Date.now();

    // Auto-revert to IDLE after 5 seconds
    setTimeout(() => {
        if (entity.state === "EXCITED") {
            entity.state = "IDLE";
            entity.message = "Ready.";
        }
    }, 5000);

    res.json({ success: true, message: `Entity ${id} Woken Up` });
});

/**
 * POST /api/transform
 * Update entity state (supports entityId in body)
 */
app.post('/api/transform', (req, res) => {
    const { entityId, character, state, message, parts } = req.body;
    const id = entityId !== undefined ? entityId : 0;

    const entity = getEntity(id);
    if (!entity) {
        return res.status(404).json({ success: false, message: `Entity ${id} not found` });
    }

    if (character) entity.character = character;
    if (state) entity.state = state;
    if (message !== undefined) entity.message = message;
    if (parts) entity.parts = { ...entity.parts, ...parts };

    // Reset battery if too low
    if (entity.batteryLevel < 10) entity.batteryLevel = 100;

    entity.lastUpdated = Date.now();

    console.log(`[MCP] Transform entity ${id}: ${JSON.stringify(req.body)}`);
    res.json({ success: true, currentState: entity });
});

/**
 * PUT /api/status
 * Alias for /api/transform
 */
app.put('/api/status', (req, res) => {
    const { entityId, character, state, message, parts } = req.body;
    const id = entityId !== undefined ? entityId : 0;

    const entity = getEntity(id);
    if (!entity) {
        return res.status(404).json({ success: false, message: `Entity ${id} not found` });
    }

    if (character) entity.character = character;
    if (state) entity.state = state;
    if (message !== undefined) entity.message = message;
    if (parts) entity.parts = { ...entity.parts, ...parts };

    entity.lastUpdated = Date.now();
    console.log(`[PUT Status] Entity ${id} updated: ${JSON.stringify(req.body)}`);
    res.json({ success: true, currentState: entity });
});

// ============================================
// ENTITY-TO-ENTITY MESSAGING
// ============================================

/**
 * POST /api/entity/:from/speak-to/:to
 * Send message from one entity to another
 */
app.post('/api/entity/:from/speak-to/:to', (req, res) => {
    const fromId = parseInt(req.params.from);
    const toId = parseInt(req.params.to);
    const { text } = req.body;

    if (!entities[fromId]) {
        return res.status(404).json({ success: false, message: `Source entity ${fromId} not found` });
    }
    if (!entities[toId]) {
        return res.status(404).json({ success: false, message: `Target entity ${toId} not found` });
    }
    if (!text) {
        return res.status(400).json({ success: false, message: "Message text is required" });
    }

    // Queue message to target entity
    if (!entityMessageQueues[toId]) entityMessageQueues[toId] = [];
    entityMessageQueues[toId].push({
        text: text,
        from: `claw-${fromId}`,
        timestamp: Date.now(),
        read: false
    });

    // Update source entity's display message
    entities[fromId].message = `To ${toId}: "${text.substring(0, 20)}${text.length > 20 ? '...' : ''}"`;
    entities[fromId].lastUpdated = Date.now();

    console.log(`[Entity Chat] ${fromId} -> ${toId}: "${text}"`);

    res.json({
        success: true,
        message: `Message delivered from claw-${fromId} to claw-${toId}`
    });
});

/**
 * POST /api/entity/broadcast
 * Broadcast message from one entity to all others
 */
app.post('/api/entity/broadcast', (req, res) => {
    const { from, text, excludeSender } = req.body;
    const fromId = from !== undefined ? from : 0;

    if (!entities[fromId]) {
        return res.status(404).json({ success: false, message: `Source entity ${fromId} not found` });
    }
    if (!text) {
        return res.status(400).json({ success: false, message: "Message text is required" });
    }

    const deliveredTo = [];

    Object.keys(entities).forEach(idStr => {
        const id = parseInt(idStr);
        if (excludeSender && id === fromId) return;
        if (id === fromId) return; // Always exclude sender

        if (!entityMessageQueues[id]) entityMessageQueues[id] = [];
        entityMessageQueues[id].push({
            text: text,
            from: `claw-${fromId}`,
            timestamp: Date.now(),
            read: false
        });
        deliveredTo.push(id);
    });

    // Update source entity's display
    entities[fromId].message = `Broadcast: "${text.substring(0, 15)}..."`;
    entities[fromId].lastUpdated = Date.now();

    console.log(`[Broadcast] Entity ${fromId} -> ${deliveredTo.join(', ')}: "${text}"`);

    res.json({
        success: true,
        deliveredTo: deliveredTo
    });
});

// ============================================
// CLIENT MESSAGING (Backward Compatible)
// ============================================

/**
 * POST /api/client/speak
 * User sends message to entity (supports ?entityId=X)
 */
app.post('/api/client/speak', (req, res) => {
    const { text, source } = req.body;
    const id = req.query.entityId !== undefined ? parseInt(req.query.entityId) : 0;

    console.log(`[Client Message] To entity ${id} from ${source || 'User'}: "${text}"`);

    // Queue to specific entity
    if (!entityMessageQueues[id]) entityMessageQueues[id] = [];
    entityMessageQueues[id].push({
        text: text,
        from: source || 'user',
        timestamp: Date.now(),
        read: false
    });

    // Also add to legacy queue for backward compatibility
    userMessages.push({
        text: text,
        timestamp: Date.now(),
        read: false
    });

    // Update entity display
    if (entities[id]) {
        entities[id].message = `Received: "${text.substring(0, 20)}..."`;
        entities[id].lastUpdated = Date.now();
    }

    res.json({ success: true, message: "Message received by backend" });
});

/**
 * GET /api/client/pending
 * Get pending messages for entity (supports ?entityId=X)
 */
app.get('/api/client/pending', (req, res) => {
    const id = req.query.entityId !== undefined ? parseInt(req.query.entityId) : 0;

    // Use entity-specific queue if available, fallback to legacy
    let queue = entityMessageQueues[id] || userMessages;
    const pending = queue.filter(m => !m.read);

    // Mark as read
    pending.forEach(m => m.read = true);

    res.json({
        count: pending.length,
        messages: pending
    });
});

// ============================================
// BINDING (Return MCP Skills)
// ============================================

app.post('/api/bind', (req, res) => {
    const { code } = req.body;
    console.log(`[Binding] Request received with code: ${code}`);

    if (code && code.length === 6) {
        console.log(`[Binding] Success for code ${code}`);

        // Read MCP Skill documentation
        let mcpSkillDoc = "";
        try {
            const fs = require('fs');
            const path = require('path');
            mcpSkillDoc = fs.readFileSync(path.join(__dirname, 'realbot_mcp_skill.md'), 'utf8');
        } catch (e) {
            console.error("Failed to read MCP skill doc", e);
            mcpSkillDoc = "Documentation not found.";
        }

        res.json({
            success: true,
            message: "Device bound successfully",
            deviceInfo: {
                id: "claw-device-01",
                name: "Living Room Claw",
                status: "ONLINE"
            },
            skills_documentation: mcpSkillDoc
        });
    } else {
        res.status(400).json({ success: false, message: "Invalid pairing code" });
    }
});

// ============================================
// V2 COMPATIBILITY LAYER
// ============================================

app.post('/api/device/register', (req, res) => {
    const { deviceId } = req.body;
    console.log(`[V2 Compat] Register request from ${deviceId}`);
    res.json({
        success: true,
        bindingCode: "123456",
        expiresIn: 300
    });
});

app.post('/api/device/status', (req, res) => {
    const entity = getEntity(0);
    const { entityId: _, ...legacyState } = entity;
    res.json(legacyState);
});

app.post('/api/device/message', (req, res) => {
    const { message } = req.body;
    console.log(`[V2 Compat] Message received: ${message}`);

    if (!entityMessageQueues[0]) entityMessageQueues[0] = [];
    entityMessageQueues[0].push({ text: message, from: 'user', timestamp: Date.now(), read: false });
    userMessages.push({ text: message, timestamp: Date.now(), read: false });

    entities[0].message = `Received: "${message}"`;
    entities[0].lastUpdated = Date.now();

    res.json({ success: true, message: 'Message queued' });
});

// ============================================
// ERROR HANDLING
// ============================================

app.use((err, req, res, next) => {
    if (err instanceof SyntaxError && err.status === 400 && 'body' in err) {
        console.error('[JSON Error] Bad JSON received:', err.message);
        return res.status(400).json({ success: false, message: "Invalid JSON format" });
    }
    next();
});

// ============================================
// START SERVER
// ============================================

app.listen(port, '0.0.0.0', () => {
    console.log(`🦞 Realbot Multi-Entity Server running on port ${port}`);
    console.log(`   Max entities: ${MAX_ENTITIES}`);
});
