/**
 * Mission Control Dashboard API
 * PostgreSQL + Optimistic Locking + Trigger Version Control
 *
 * Mounted at: /api/mission
 *
 * Endpoints:
 * GET  /dashboard          - 取得 Dashboard
 * POST /dashboard          - 上傳 Dashboard (含版本檢查)
 * GET  /items              - 取得任務
 * POST /items              - 新增任務
 * PUT  /items/:id          - 更新任務
 * DELETE /items/:id        - 刪除任務
 * GET  /notes              - 取得筆記
 * GET  /rules              - 取得規則
 *
 * Auth: All endpoints accept either deviceSecret (user/APP) or botSecret (bot)
 */

const express = require('express');
const { Pool } = require('pg');
const crypto = require('crypto');
const fs = require('fs');
const path = require('path');

const pool = new Pool({
    connectionString: process.env.DATABASE_URL || 'postgresql://user:pass@localhost:5432/realbot'
});

// Initialize database tables from schema file
async function initMissionDatabase() {
    try {
        const schemaPath = path.join(__dirname, 'mission_schema.sql');
        const schema = fs.readFileSync(schemaPath, 'utf8');

        // Split SQL respecting $$ function bodies (don't split inside $$ blocks)
        const statements = [];
        let current = '';
        let inDollarBlock = false;
        const lines = schema.split('\n');
        for (const line of lines) {
            const trimmed = line.trim();
            if (trimmed.startsWith('--')) {
                continue; // skip comments
            }
            current += line + '\n';
            // Track $$ blocks
            const dollarCount = (line.match(/\$\$/g) || []).length;
            if (dollarCount % 2 === 1) {
                inDollarBlock = !inDollarBlock;
            }
            // Only split on ; when outside $$ blocks
            if (!inDollarBlock && trimmed.endsWith(';')) {
                const stmt = current.trim();
                if (stmt && stmt !== ';') {
                    statements.push(stmt);
                }
                current = '';
            }
        }
        if (current.trim()) {
            statements.push(current.trim());
        }

        for (const statement of statements) {
            try {
                await pool.query(statement);
            } catch (err) {
                if (!err.message.includes('already exists') &&
                    !err.message.includes('duplicate key')) {
                    console.warn('[Mission] Schema warning:', err.message);
                }
            }
        }

        console.log('[Mission] Database initialized');
    } catch (error) {
        console.error('[Mission] Failed to init database:', error);
    }
}

/**
 * Factory function - receives the in-memory devices object from index.js
 * Returns { router, initMissionDatabase }
 */
module.exports = function(devices) {
    const router = express.Router();

    // ============================================
    // Auth Helpers
    // ============================================

    /**
     * Authenticate by botSecret (for bots/OpenClaw)
     */
    function findEntityByCredentials(deviceId, entityId, botSecret) {
        const device = devices[deviceId];
        if (!device) return null;
        const entity = (device.entities || {})[entityId];
        if (!entity || entity.botSecret !== botSecret) return null;
        return entity;
    }

    /**
     * Authenticate by deviceSecret (for Android APP / web page)
     */
    function findDeviceByCredentials(deviceId, deviceSecret) {
        const device = devices[deviceId];
        if (!device || device.deviceSecret !== deviceSecret) return null;
        return device;
    }

    /**
     * Dual auth middleware helper - accepts either deviceSecret or botSecret
     * Returns true if authenticated, sends 401 and returns false otherwise
     */
    function authenticate(req, res) {
        const params = { ...req.query, ...req.body };
        const { deviceId, deviceSecret, botSecret, entityId } = params;

        if (!deviceId) {
            res.status(400).json({ success: false, error: 'Missing deviceId' });
            return false;
        }

        // Try deviceSecret first (APP/web user)
        if (deviceSecret) {
            const device = findDeviceByCredentials(deviceId, deviceSecret);
            if (device) return true;
        }

        // Try botSecret (OpenClaw bot)
        if (botSecret) {
            const entity = findEntityByCredentials(deviceId, parseInt(entityId || 0), botSecret);
            if (entity) return true;
        }

        // Neither credential is valid
        if (!deviceSecret && !botSecret) {
            res.status(400).json({ success: false, error: 'Missing deviceSecret or botSecret' });
        } else {
            res.status(401).json({ success: false, error: 'Invalid credentials' });
        }
        return false;
    }

    // ============================================
    // Dashboard API
    // ============================================

    /**
     * GET /dashboard
     * 取得完整 Dashboard
     */
    router.get('/dashboard', async (req, res) => {
        if (!authenticate(req, res)) return;
        const { deviceId } = req.query;

        try {
            const result = await pool.query(
                'SELECT * FROM mission_dashboard WHERE device_id = $1',
                [deviceId]
            );

            if (result.rows.length === 0) {
                // Initialize dashboard if not exists
                await pool.query('SELECT init_mission_dashboard($1)', [deviceId]);

                return res.json({
                    success: true,
                    dashboard: {
                        deviceId,
                        version: 1,
                        todoList: [],
                        missionList: [],
                        doneList: [],
                        notes: [],
                        rules: [],
                        skills: [],
                        lastSyncedAt: Date.now()
                    }
                });
            }

            const row = result.rows[0];
            const dashboard = {
                deviceId: row.device_id,
                version: row.version,
                lastSyncedAt: new Date(row.last_synced_at).getTime(),
                todoList: row.todo_list,
                missionList: row.mission_list,
                doneList: row.done_list,
                notes: row.notes,
                rules: row.rules,
                skills: row.skills || [],
                lastUpdated: new Date(row.updated_at).getTime()
            };

            res.json({ success: true, dashboard });
        } catch (error) {
            console.error('[Mission] Error fetching dashboard:', error);
            res.status(500).json({ success: false, error: error.message });
        }
    });

    /**
     * POST /dashboard
     * 上傳 Dashboard (Optimistic Locking)
     */
    router.post('/dashboard', async (req, res) => {
        if (!authenticate(req, res)) return;
        const { deviceId, entityId, dashboard, version } = req.body;

        if (!dashboard) {
            return res.status(400).json({ success: false, error: 'Missing dashboard data' });
        }

        const client = await pool.connect();

        try {
            await client.query('BEGIN');

            // Ensure dashboard row exists (upsert)
            await client.query('SELECT init_mission_dashboard($1)', [deviceId]);

            // Check version (Optimistic Locking)
            if (version !== undefined) {
                const versionCheck = await client.query(
                    'SELECT version FROM mission_dashboard WHERE device_id = $1 FOR UPDATE',
                    [deviceId]
                );

                if (versionCheck.rows.length > 0 && versionCheck.rows[0].version !== version) {
                    await client.query('ROLLBACK');
                    return res.status(409).json({
                        success: false,
                        error: 'VERSION_CONFLICT',
                        message: 'Dashboard has been modified by another client',
                        currentVersion: versionCheck.rows[0].version,
                        yourVersion: version
                    });
                }
            }

            // Update dashboard (Trigger will auto-increment version)
            const result = await client.query(
                `UPDATE mission_dashboard
                 SET todo_list = $2, mission_list = $3, done_list = $4,
                     notes = $5, rules = $6, skills = $7, last_synced_at = NOW()
                 WHERE device_id = $1
                 RETURNING version`,
                [
                    deviceId,
                    JSON.stringify(dashboard.todoList || []),
                    JSON.stringify(dashboard.missionList || []),
                    JSON.stringify(dashboard.doneList || []),
                    JSON.stringify(dashboard.notes || []),
                    JSON.stringify(dashboard.rules || []),
                    JSON.stringify(dashboard.skills || [])
                ]
            );

            // Log sync action
            await client.query(
                'SELECT record_sync_action($1, $2, $3, $4, $5, $6, $7)',
                [deviceId, 'SYNC', 'DASHBOARD', null, version || 0, result.rows[0].version, `entity_${entityId || 'user'}`]
            );

            await client.query('COMMIT');

            console.log(`[Mission] Dashboard updated for ${deviceId}, version: ${result.rows[0].version}`);
            res.json({
                success: true,
                version: result.rows[0].version,
                message: 'Dashboard uploaded successfully'
            });
        } catch (error) {
            await client.query('ROLLBACK');
            console.error('[Mission] Error updating dashboard:', error);
            res.status(500).json({ success: false, error: error.message });
        } finally {
            client.release();
        }
    });

    // ============================================
    // Mission Items API
    // ============================================

    /**
     * GET /items
     * 取得所有任務
     */
    router.get('/items', async (req, res) => {
        if (!authenticate(req, res)) return;
        const { deviceId, status, priority, listType } = req.query;

        try {
            let query = 'SELECT * FROM mission_items WHERE device_id = $1';
            const params = [deviceId];
            let paramIndex = 2;

            if (status) {
                query += ` AND status = $${paramIndex++}`;
                params.push(status);
            }
            if (priority) {
                query += ` AND priority >= $${paramIndex++}`;
                params.push(parseInt(priority));
            }
            if (listType) {
                query += ` AND list_type = $${paramIndex++}`;
                params.push(listType);
            }

            query += ' ORDER BY priority DESC, created_at DESC';
            const result = await pool.query(query, params);

            res.json({ success: true, items: result.rows });
        } catch (error) {
            console.error('[Mission] Error fetching items:', error);
            res.status(500).json({ success: false, error: error.message });
        }
    });

    /**
     * POST /items
     * 新增任務
     */
    router.post('/items', async (req, res) => {
        if (!authenticate(req, res)) return;
        const { deviceId, entityId, item, listType } = req.body;

        if (!item || !item.title) {
            return res.status(400).json({ success: false, error: 'Missing item or title' });
        }

        try {
            // Ensure dashboard row exists (FK constraint)
            await pool.query('SELECT init_mission_dashboard($1)', [deviceId]);

            const result = await pool.query(
                `INSERT INTO mission_items
                 (id, device_id, list_type, title, description, priority, status, assigned_bot, eta, created_by)
                 VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10)
                 RETURNING *`,
                [
                    item.id || crypto.randomUUID(),
                    deviceId,
                    listType || 'todo',
                    item.title,
                    item.description || '',
                    item.priority || 2,
                    item.status || 'PENDING',
                    item.assignedBot || null,
                    item.eta ? new Date(item.eta) : null,
                    item.createdBy || 'user'
                ]
            );

            await pool.query(
                'SELECT record_sync_action($1, $2, $3, $4, $5, $6, $7)',
                [deviceId, 'CREATE', 'ITEM', result.rows[0].id, 0, 1, `entity_${entityId || 'user'}`]
            );

            res.json({ success: true, item: result.rows[0] });
        } catch (error) {
            console.error('[Mission] Error adding item:', error);
            res.status(500).json({ success: false, error: error.message });
        }
    });

    /**
     * PUT /items/:id
     * 更新任務
     */
    router.put('/items/:id', async (req, res) => {
        if (!authenticate(req, res)) return;
        const { deviceId, entityId, item } = req.body;
        const { id } = req.params;

        if (!item) {
            return res.status(400).json({ success: false, error: 'Missing item data' });
        }

        try {
            const oldItem = await pool.query(
                'SELECT * FROM mission_items WHERE id = $1 AND device_id = $2',
                [id, deviceId]
            );

            if (oldItem.rows.length === 0) {
                return res.status(404).json({ success: false, error: 'Item not found' });
            }

            const result = await pool.query(
                `UPDATE mission_items
                 SET title = $1, description = $2, priority = $3, status = $4,
                     assigned_bot = $5, eta = $6, updated_at = NOW()
                 WHERE id = $7 AND device_id = $8
                 RETURNING *`,
                [
                    item.title,
                    item.description || '',
                    item.priority || 2,
                    item.status || 'PENDING',
                    item.assignedBot || null,
                    item.eta ? new Date(item.eta) : null,
                    id,
                    deviceId
                ]
            );

            await pool.query(
                'SELECT record_sync_action($1, $2, $3, $4, $5, $6, $7)',
                [deviceId, 'UPDATE', 'ITEM', id, 1, 2, `entity_${entityId || 'user'}`]
            );

            res.json({ success: true, item: result.rows[0] });
        } catch (error) {
            console.error('[Mission] Error updating item:', error);
            res.status(500).json({ success: false, error: error.message });
        }
    });

    /**
     * DELETE /items/:id
     * 刪除任務
     */
    router.delete('/items/:id', async (req, res) => {
        if (!authenticate(req, res)) return;
        const { deviceId, entityId } = req.body;
        const { id } = req.params;

        try {
            await pool.query(
                'DELETE FROM mission_items WHERE id = $1 AND device_id = $2',
                [id, deviceId]
            );

            await pool.query(
                'SELECT record_sync_action($1, $2, $3, $4, $5, $6, $7)',
                [deviceId, 'DELETE', 'ITEM', id, 0, 0, `entity_${entityId || 'user'}`]
            );

            res.json({ success: true, message: 'Item deleted' });
        } catch (error) {
            console.error('[Mission] Error deleting item:', error);
            res.status(500).json({ success: false, error: error.message });
        }
    });

    // ============================================
    // Notes API
    // ============================================

    /**
     * GET /notes
     * 取得筆記 (Bots 可讀)
     */
    router.get('/notes', async (req, res) => {
        if (!authenticate(req, res)) return;
        const { deviceId, category } = req.query;

        try {
            let query = 'SELECT * FROM mission_notes WHERE device_id = $1';
            const params = [deviceId];

            if (category) {
                query += ' AND category = $2';
                params.push(category);
            }

            query += ' ORDER BY updated_at DESC';
            const result = await pool.query(query, params);

            res.json({ success: true, notes: result.rows });
        } catch (error) {
            console.error('[Mission] Error fetching notes:', error);
            res.status(500).json({ success: false, error: error.message });
        }
    });

    // ============================================
    // Rules API
    // ============================================

    /**
     * GET /rules
     * 取得規則列表
     */
    router.get('/rules', async (req, res) => {
        if (!authenticate(req, res)) return;
        const { deviceId, type } = req.query;

        try {
            let query = 'SELECT * FROM mission_rules WHERE device_id = $1';
            const params = [deviceId];

            if (type) {
                query += ' AND rule_type = $2';
                params.push(type);
            }

            query += ' ORDER BY priority DESC, created_at DESC';
            const result = await pool.query(query, params);

            res.json({ success: true, rules: result.rows });
        } catch (error) {
            console.error('[Mission] Error fetching rules:', error);
            res.status(500).json({ success: false, error: error.message });
        }
    });

    // ============================================
    // Mission Notify API
    // ============================================

    /**
     * POST /notify
     * Push mission updates to assigned entities via webhook
     * Body: { deviceId, deviceSecret, notifications: [{ type, title, priority, entityIds, url }] }
     *
     * type: 'TODO' | 'SKILL' | 'RULE'
     * TODO with HIGH/CRITICAL = 立刻執行
     * SKILL = 必須安裝
     * RULE = 必須遵守
     */
    router.post('/notify', async (req, res) => {
        if (!authenticate(req, res)) return;
        const { deviceId, notifications } = req.body;

        if (!notifications || !Array.isArray(notifications) || notifications.length === 0) {
            return res.status(400).json({ success: false, error: 'Missing notifications' });
        }

        const device = devices[deviceId];
        if (!device) {
            return res.status(404).json({ success: false, error: 'Device not found' });
        }

        // Build consolidated notification lines with entity labels
        const entityMessages = {};  // entityId -> [lines]
        const allLines = [];        // for the consolidated chat message
        for (const n of notifications) {
            const entityIds = n.entityIds || [];
            let typeTag = '';
            if (n.type === 'TODO') {
                const urgency = (n.priority >= 3) ? '⚠️ 立刻執行' : '📋 待處理';
                typeTag = `[TODO ${urgency}]`;
            } else if (n.type === 'SKILL') {
                typeTag = '[SKILL 必須安裝]';
            } else if (n.type === 'RULE') {
                typeTag = '[RULE 必須遵守]';
            } else {
                typeTag = `[${n.type}]`;
            }

            const entityLabels = entityIds.map(id => {
                const e = device.entities[parseInt(id)];
                return e ? `Entity ${id}` : `Entity ${id}`;
            }).join(', ');
            const line = `${typeTag} ${n.title}${n.url ? ' - ' + n.url : ''} → ${entityLabels}`;
            allLines.push(line);

            for (const eId of entityIds) {
                const id = parseInt(eId);
                if (!entityMessages[id]) entityMessages[id] = [];
                const perEntityLine = `${typeTag} ${n.title}${n.url ? ' - ' + n.url : ''}`;
                entityMessages[id].push(perEntityLine);
            }
        }

        // Save ONE consolidated chat message (user bubble)
        const chatText = `📢 任務通知\n${allLines.join('\n')}`;
        const allEntityIds = [...new Set(notifications.flatMap(n => (n.entityIds || []).map(Number)))];
        let chatMsgId = null;
        try {
            const insertResult = await pool.query(
                `INSERT INTO chat_messages (device_id, entity_id, text, source, is_from_user, is_from_bot)
                 VALUES ($1, $2, $3, $4, $5, $6) RETURNING id`,
                [deviceId, null, chatText, 'mission_notify', true, false]
            );
            chatMsgId = insertResult.rows[0]?.id;
        } catch (dbErr) {
            console.warn('[Mission] Failed to save consolidated chat message:', dbErr.message);
        }

        // Push to each entity via webhook
        const pushPromises = Object.entries(entityMessages).map(async ([eIdStr, lines]) => {
            const eId = parseInt(eIdStr);
            const entity = device.entities[eId];
            if (!entity || !entity.isBound) {
                return { entityId: eId, pushed: false, reason: 'not_bound' };
            }

            const dashboardApi = `GET https://eclaw.up.railway.app/api/mission/dashboard?deviceId=${deviceId}&botSecret=${entity.botSecret}&entityId=${eId}`;
            const pushMessage = `[Mission Control 任務更新]\n${lines.join('\n')}\n\n取得完整任務面板: ${dashboardApi}`;

            if (!entity.webhook) {
                return { entityId: eId, pushed: false, reason: 'no_webhook' };
            }

            const { url, token, sessionKey } = entity.webhook;
            const requestPayload = {
                tool: "sessions_send",
                args: {
                    sessionKey: sessionKey,
                    message: `[Device ${deviceId} Entity ${eId} - Mission Control 更新]\n${pushMessage}\n注意: 請使用 update_claw_status (POST /api/transform) 來回覆此訊息，將回覆內容放在 message 欄位`
                }
            };

            try {
                const response = await fetch(url, {
                    method: 'POST',
                    headers: { 'Authorization': `Bearer ${token}`, 'Content-Type': 'application/json' },
                    body: JSON.stringify(requestPayload)
                });

                if (response.ok) {
                    const responseText = await response.text().catch(() => '');
                    if (responseText && responseText.includes('No session found')) {
                        return { entityId: eId, pushed: false, reason: 'session_not_found' };
                    }
                    console.log(`[Mission] ✓ Notify pushed to Device ${deviceId} Entity ${eId}`);
                    return { entityId: eId, pushed: true };
                } else {
                    console.error(`[Mission] ✗ Notify push failed for Entity ${eId}: ${response.status}`);
                    return { entityId: eId, pushed: false, reason: `http_${response.status}` };
                }
            } catch (err) {
                console.error(`[Mission] ✗ Notify push error for Entity ${eId}:`, err.message);
                return { entityId: eId, pushed: false, reason: err.message };
            }
        });

        const pushResults = await Promise.all(pushPromises);
        const delivered = pushResults.filter(r => r.pushed).length;
        const deliveredIds = pushResults.filter(r => r.pushed).map(r => r.entityId);

        // Update consolidated chat message with delivery status
        if (chatMsgId && deliveredIds.length > 0) {
            try {
                await pool.query(
                    `UPDATE chat_messages SET is_delivered = true, delivered_to = $2 WHERE id = $1`,
                    [chatMsgId, deliveredIds.join(',')]
                );
            } catch (e) { /* ignore */ }
        }

        res.json({
            success: true,
            results: pushResults,
            delivered,
            total: pushResults.length,
            chatMessageId: chatMsgId
        });
    });

    return { router, initMissionDatabase };
};
