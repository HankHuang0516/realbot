/**
 * Mission Control Dashboard API
 * PostgreSQL + Optimistic Locking + Trigger Version Control
 * 
 * Endpoints:
 * GET  /api/mission/dashboard          - 取得 Dashboard
 * POST /api/mission/dashboard          - 上傳 Dashboard (含版本檢查)
 * GET  /api/mission/items              - 取得任務
 * POST /api/mission/items              - 新增任務
 * PUT  /api/mission/items/:id         - 更新任務
 * DELETE /api/mission/items/:id       - 刪除任務
 * GET  /api/mission/notes             - 取得筆記
 * GET  /api/mission/rules             - 取得規則
 */

const { Pool } = require('pg');
const pool = new Pool({
    connectionString: process.env.DATABASE_URL || 'postgresql://user:pass@localhost:5432/realbot'
});

// Initialize database tables
async function initMissionDatabase() {
    const fs = require('fs');
    const path = require('path');
    
    try {
        const schemaPath = path.join(__dirname, 'mission_schema.sql');
        const schema = fs.readFileSync(schemaPath, 'utf8');
        
        // Split by semicolons and execute each statement
        const statements = schema.split(';').filter(s => s.trim());
        
        for (const statement of statements) {
            if (statement.trim()) {
                try {
                    await pool.query(statement);
                } catch (err) {
                    // Ignore "already exists" errors
                    if (!err.message.includes('already exists') && 
                        !err.message.includes('duplicate key')) {
                        console.warn('[Mission] Schema warning:', err.message);
                    }
                }
            }
        }
        
        console.log('[Mission] Database initialized');
    } catch (error) {
        console.error('[Mission] Failed to init database:', error);
    }
}

// Initialize on module load
initMissionDatabase();

// ============================================
// Dashboard API
// ============================================

/**
 * GET /api/mission/dashboard
 * 取得完整 Dashboard
 */
app.get('/api/mission/dashboard', async (req, res) => {
    const { deviceId, entityId, botSecret } = req.query;
    
    if (!deviceId || !botSecret) {
        return res.status(400).json({ 
            success: false, 
            error: 'Missing deviceId or botSecret' 
        });
    }
    
    try {
        // Verify credentials
        const entity = await findEntityByCredentials(deviceId, parseInt(entityId || 0), botSecret);
        if (!entity) {
            return res.status(401).json({ 
                success: false, 
                error: 'Invalid credentials' 
            });
        }
        
        // Get dashboard
        const result = await pool.query(
            `SELECT * FROM mission_dashboard WHERE device_id = $1`,
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
                    lastSyncedAt: Date.now()
                }
            });
        }
        
        const row = result.rows[0];
        
        // Parse JSONB fields
        const dashboard = {
            deviceId: row.device_id,
            version: row.version,
            lastSyncedAt: new Date(row.last_synced_at).getTime(),
            todoList: row.todo_list,
            missionList: row.mission_list,
            doneList: row.done_list,
            notes: row.notes,
            rules: row.rules,
            lastUpdated: new Date(row.updated_at).getTime()
        };
        
        res.json({ success: true, dashboard });
    } catch (error) {
        console.error('[Mission] Error fetching dashboard:', error);
        res.status(500).json({ success: false, error: error.message });
    }
});

/**
 * POST /api/mission/dashboard
 * 上傳 Dashboard (Optimistic Locking)
 */
app.post('/api/mission/dashboard', async (req, res) => {
    const { deviceId, entityId, botSecret, dashboard, version } = req.body;
    
    if (!deviceId || !botSecret || !dashboard) {
        return res.status(400).json({ 
            success: false, 
            error: 'Missing required fields' 
        });
    }
    
    const client = await pool.connect();
    
    try {
        // Verify credentials
        const entity = await findEntityByCredentials(deviceId, parseInt(entityId || 0), botSecret);
        if (!entity) {
            return res.status(401).json({ 
                success: false, 
                error: 'Invalid credentials' 
            });
        }
        
        await client.query('BEGIN');
        
        // Check version (Optimistic Locking)
        if (version !== undefined) {
            const versionCheck = await client.query(
                `SELECT version FROM mission_dashboard WHERE device_id = $1 FOR UPDATE`,
                [deviceId]
            );
            
            if (versionCheck.rows.length > 0 && versionCheck.rows[0].version !== version) {
                // Version conflict!
                await client.query('ROLLBACK');
                
                const currentVersion = versionCheck.rows[0].version;
                
                return res.status(409).json({
                    success: false,
                    error: 'VERSION_CONFLICT',
                    message: 'Dashboard has been modified by another client',
                    currentVersion,
                    yourVersion: version
                });
            }
        }
        
        // Update dashboard (Trigger will auto-increment version)
        const result = await client.query(
            `UPDATE mission_dashboard 
             SET todo_list = $2, 
                 mission_list = $3, 
                 done_list = $4, 
                 notes = $5, 
                 rules = $6,
                 last_synced_at = NOW()
             WHERE device_id = $1
             RETURNING version`,
            [
                deviceId,
                JSON.stringify(dashboard.todoList || []),
                JSON.stringify(dashboard.missionList || []),
                JSON.stringify(dashboard.doneList || []),
                JSON.stringify(dashboard.notes || []),
                JSON.stringify(dashboard.rules || [])
            ]
        );
        
        // Log sync action
        await client.query(
            `SELECT record_sync_action($1, 'SYNC', 'DASHBOARD', NULL, $2, $3, $4)`,
            [deviceId, version || 0, result.rows[0].version, `entity_${entityId}`]
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
 * GET /api/mission/items
 * 取得所有任務
 */
app.get('/api/mission/items', async (req, res) => {
    const { deviceId, entityId, botSecret, status, priority, listType } = req.query;
    
    if (!deviceId || !botSecret) {
        return res.status(400).json({ error: 'Missing deviceId or botSecret' });
    }
    
    try {
        const entity = await findEntityByCredentials(deviceId, parseInt(entityId || 0), botSecret);
        if (!entity) {
            return res.status(401).json({ error: 'Invalid credentials' });
        }
        
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
        res.status(500).json({ error: error.message });
    }
});

/**
 * POST /api/mission/items
 * 新增任務
 */
app.post('/api/mission/items', async (req, res) => {
    const { deviceId, entityId, botSecret, item, listType } = req.body;
    
    if (!deviceId || !botSecret || !item) {
        return res.status(400).json({ error: 'Missing required fields' });
    }
    
    try {
        const entity = await findEntityByCredentials(deviceId, parseInt(entityId || 0), botSecret);
        if (!entity) {
            return res.status(401).json({ error: 'Invalid credentials' });
        }
        
        const result = await pool.query(
            `INSERT INTO mission_items 
             (id, device_id, list_type, title, description, priority, status, assigned_bot, eta, created_by)
             VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10)
             RETURNING *`,
            [
                item.id || require('uuid').v4(),
                deviceId,
                listType || 'todo',
                item.title,
                item.description || '',
                item.priority || 2,
                item.status || 'PENDING',
                item.assignedBot || null,
                item.eta || null,
                item.createdBy || 'user'
            ]
        );
        
        // Record sync
        await pool.query(
            `SELECT record_sync_action($1, 'CREATE', 'ITEM', $2, 0, 1, $3)`,
            [deviceId, result.rows[0].id, `entity_${entityId}`]
        );
        
        res.json({ success: true, item: result.rows[0] });
    } catch (error) {
        console.error('[Mission] Error adding item:', error);
        res.status(500).json({ error: error.message });
    }
});

/**
 * PUT /api/mission/items/:id
 * 更新任務
 */
app.put('/api/mission/items/:id', async (req, res) => {
    const { deviceId, entityId, botSecret, item } = req.body;
    const { id } = req.params;
    
    if (!deviceId || !botSecret || !item) {
        return res.status(400).json({ error: 'Missing required fields' });
    }
    
    try {
        const entity = await findEntityByCredentials(deviceId, parseInt(entityId || 0), botSecret);
        if (!entity) {
            return res.status(401).json({ error: 'Invalid credentials' });
        }
        
        // Get old version for sync log
        const oldItem = await pool.query(
            'SELECT * FROM mission_items WHERE id = $1 AND device_id = $2',
            [id, deviceId]
        );
        
        if (oldItem.rows.length === 0) {
            return res.status(404).json({ error: 'Item not found' });
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
                item.eta || null,
                id,
                deviceId
            ]
        );
        
        // Record sync
        await pool.query(
            `SELECT record_sync_action($1, 'UPDATE', 'ITEM', $2, 1, 2, $3)`,
            [deviceId, id, `entity_${entityId}`]
        );
        
        res.json({ success: true, item: result.rows[0] });
    } catch (error) {
        console.error('[Mission] Error updating item:', error);
        res.status(500).json({ error: error.message });
    }
});

/**
 * DELETE /api/mission/items/:id
 * 刪除任務
 */
app.delete('/api/mission/items/:id', async (req, res) => {
    const { deviceId, entityId, botSecret } = req.body;
    const { id } = req.params;
    
    if (!deviceId || !botSecret) {
        return res.status(400).json({ error: 'Missing required fields' });
    }
    
    try {
        const entity = await findEntityByCredentials(deviceId, parseInt(entityId || 0), botSecret);
        if (!entity) {
            return res.status(401).json({ error: 'Invalid credentials' });
        }
        
        await pool.query(
            'DELETE FROM mission_items WHERE id = $1 AND device_id = $2',
            [id, deviceId]
        );
        
        // Record sync
        await pool.query(
            `SELECT record_sync_action($1, 'DELETE', 'ITEM', $2, 2, 3, $3)`,
            [deviceId, id, `entity_${entityId}`]
        );
        
        res.json({ success: true, message: 'Item deleted' });
    } catch (error) {
        console.error('[Mission] Error deleting item:', error);
        res.status(500).json({ error: error.message });
    }
});

// ============================================
// Notes API
// ============================================

/**
 * GET /api/mission/notes
 * 取得筆記 (Bots 可讀)
 */
app.get('/api/mission/notes', async (req, res) => {
    const { deviceId, entityId, botSecret, category } = req.query;
    
    if (!deviceId || !botSecret) {
        return res.status(400).json({ error: 'Missing deviceId or botSecret' });
    }
    
    try {
        const entity = await findEntityByCredentials(deviceId, parseInt(entityId || 0), botSecret);
        if (!entity) {
            return res.status(401).json({ error: 'Invalid credentials' });
        }
        
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
        res.status(500).json({ error: error.message });
    }
});

// ============================================
// Rules API
// ============================================

/**
 * GET /api/mission/rules
 * 取得規則列表
 */
app.get('/api/mission/rules', async (req, res) => {
    const { deviceId, entityId, botSecret, type } = req.query;
    
    if (!deviceId || !botSecret) {
        return res.status(400).json({ error: 'Missing deviceId or botSecret' });
    }
    
    try {
        const entity = await findEntityByCredentials(deviceId, parseInt(entityId || 0), botSecret);
        if (!entity) {
            return res.status(401).json({ error: 'Invalid credentials' });
        }
        
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
        res.status(500).json({ error: error.message });
    }
});

// ============================================
// Helper Functions
// ============================================

async function findEntityByCredentials(deviceId, entityId, botSecret) {
    try {
        // Use existing entity lookup from main app
        const device = await req.app.locals.db.collection('devices').findOne({ deviceId });
        if (!device || !device.entities) return null;
        
        const entity = device.entities.find(e => e.entityId === entityId);
        if (!entity || entity.botSecret !== botSecret) return null;
        
        return entity;
    } catch (error) {
        console.error('[Mission] Entity lookup error:', error);
        return null;
    }
}
