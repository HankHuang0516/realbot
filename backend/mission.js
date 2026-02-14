/**
 * Mission Control Dashboard API
 * 
 * Endpoints:
 * GET  /api/mission/dashboard          - 取得完整 Dashboard
 * POST /api/mission/dashboard          - 更新完整 Dashboard
 * GET  /api/mission/items              - 取得所有任務
 * POST /api/mission/items              - 新增任務
 * PUT  /api/mission/items/:id         - 更新任務
 * DELETE /api/mission/items/:id       - 刪除任務
 * GET  /api/mission/notes             - 取得筆記
 * PUT  /api/mission/notes/:id         - 更新筆記
 * GET  /api/mission/rules              - 取得規則
 * PUT  /api/mission/rules/:id          - 更新規則
 */

const MISSION_COLLECTION = 'mission_dashboard';

/**
 * GET /api/mission/dashboard
 * 取得完整 Dashboard 快照
 */
app.get('/api/mission/dashboard', async (req, res) => {
    const { deviceId, botSecret } = req.query;
    
    if (!deviceId || !botSecret) {
        return res.status(400).json({ 
            success: false, 
            error: 'Missing deviceId or botSecret' 
        });
    }
    
    try {
        const entity = await findEntityByCredentials(deviceId, parseInt(req.query.entityId || 0), botSecret);
        if (!entity) {
            return res.status(401).json({ 
                success: false, 
                error: 'Invalid credentials' 
            });
        }
        
        // 從資料庫取得 Dashboard
        const snapshot = await db.collection(MISSION_COLLECTION).findOne({ deviceId });
        
        if (!snapshot) {
            // 回傳空 Dashboard
            return res.json({
                success: true,
                dashboard: {
                    todoList: [],
                    missionList: [],
                    doneList: [],
                    notes: [],
                    rules: [],
                    version: 1,
                    lastSyncedAt: Date.now()
                }
            });
        }
        
        res.json({ success: true, dashboard: snapshot });
    } catch (error) {
        console.error('[Mission] Error fetching dashboard:', error);
        res.status(500).json({ success: false, error: error.message });
    }
});

/**
 * POST /api/mission/dashboard
 * 更新完整 Dashboard (用戶手動上傳)
 */
app.post('/api/mission/dashboard', async (req, res) => {
    const { deviceId, botSecret, dashboard } = req.body;
    
    if (!deviceId || !botSecret || !dashboard) {
        return res.status(400).json({ 
            success: false, 
            error: 'Missing required fields' 
        });
    }
    
    try {
        const entity = await findEntityByCredentials(deviceId, parseInt(req.body.entityId || 0), botSecret);
        if (!entity) {
            return res.status(401).json({ 
                success: false, 
                error: 'Invalid credentials' 
            });
        }
        
        // 驗證 Dashboard 結構
        const { error } = validateDashboard(dashboard);
        if (error) {
            return res.status(400).json({ 
                success: false, 
                error: `Invalid dashboard: ${error}` 
            });
        }
        
        // 增加版本控制和時間戳
        dashboard.lastSyncedAt = Date.now();
        dashboard.version = (dashboard.version || 0) + 1;
        
        // 儲存到資料庫 (upsert)
        await db.collection(MISSION_COLLECTION).updateOne(
            { deviceId },
            { 
                $set: { 
                    ...dashboard,
                    updatedAt: Date.now()
                },
                $setOnInsert: {
                    createdAt: Date.now()
                }
            },
            { upsert: true }
        );
        
        console.log(`[Mission] Dashboard updated for device ${deviceId}, version: ${dashboard.version}`);
        
        res.json({ 
            success: true, 
            version: dashboard.version,
            message: 'Dashboard uploaded successfully' 
        });
    } catch (error) {
        console.error('[Mission] Error updating dashboard:', error);
        res.status(500).json({ success: false, error: error.message });
    }
});

// ============ Mission Items ============

/**
 * GET /api/mission/items
 * 取得所有任務 (TODO/Mission/Done)
 */
app.get('/api/mission/items', async (req, res) => {
    const { deviceId, botSecret, status, priority } = req.query;
    
    if (!deviceId || !botSecret) {
        return res.status(400).json({ error: 'Missing deviceId or botSecret' });
    }
    
    try {
        const entity = await findEntityByCredentials(deviceId, parseInt(req.query.entityId || 0), botSecret);
        if (!entity) {
            return res.status(401).json({ error: 'Invalid credentials' });
        }
        
        const snapshot = await db.collection(MISSION_COLLECTION).findOne({ deviceId });
        if (!snapshot) {
            return res.json({ success: true, items: [] });
        }
        
        // 合併所有列表
        let allItems = [
            ...(snapshot.todoList || []),
            ...(snapshot.missionList || []),
            ...(snapshot.doneList || [])
        ];
        
        // 篩選
        if (status) {
            const statuses = status.split(',');
            allItems = allItems.filter(item => statuses.includes(item.status));
        }
        if (priority) {
            const priorities = priority.split(',');
            allItems = allItems.filter(item => priorities.includes(item.priority));
        }
        
        // 排序：優先權 > 建立時間
        allItems.sort((a, b) => {
            if (b.priority.value !== a.priority.value) {
                return b.priority.value - a.priority.value;
            }
            return b.createdAt - a.createdAt;
        });
        
        res.json({ success: true, items: allItems });
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});

/**
 * POST /api/mission/items
 * 新增任務
 */
app.post('/api/mission/items', async (req, res) => {
    const { deviceId, botSecret, item, list } = req.body; // list: 'todo', 'mission', 'done'
    
    if (!deviceId || !botSecret || !item) {
        return res.status(400).json({ error: 'Missing required fields' });
    }
    
    try {
        const entity = await findEntityByCredentials(deviceId, parseInt(req.body.entityId || 0), botSecret);
        if (!entity) {
            return res.status(401).json({ error: 'Invalid credentials' });
        }
        
        const targetList = list || 'todoList';
        const updateField = targetList === 'missionList' ? 'missionList' : 
                           targetList === 'doneList' ? 'doneList' : 'todoList';
        
        // 添加時間戳
        item.createdAt = Date.now();
        item.updatedAt = Date.now();
        
        await db.collection(MISSION_COLLECTION).updateOne(
            { deviceId },
            { 
                $push: { [updateField]: item },
                $set: { updatedAt: Date.now() }
            }
        );
        
        res.json({ success: true, item });
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});

// ============ Notes ============

/**
 * GET /api/mission/notes
 * 取得筆記 (Bots 可讀)
 */
app.get('/api/mission/notes', async (req, res) => {
    const { deviceId, botSecret, category } = req.query;
    
    if (!deviceId || !botSecret) {
        return res.status(400).json({ error: 'Missing deviceId or botSecret' });
    }
    
    try {
        const entity = await findEntityByCredentials(deviceId, parseInt(req.query.entityId || 0), botSecret);
        if (!entity) {
            return res.status(401).json({ error: 'Invalid credentials' });
        }
        
        const snapshot = await db.collection(MISSION_COLLECTION).findOne({ deviceId });
        if (!snapshot) {
            return res.json({ success: true, notes: [] });
        }
        
        let notes = snapshot.notes || [];
        if (category) {
            notes = notes.filter(n => n.category === category);
        }
        
        res.json({ success: true, notes });
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});

// ============ Rules ============

/**
 * GET /api/mission/rules
 * 取得規則列表
 */
app.get('/api/mission/rules', async (req, res) => {
    const { deviceId, botSecret, type } = req.query;
    
    if (!deviceId || !botSecret) {
        return res.status(400).json({ error: 'Missing deviceId or botSecret' });
    }
    
    try {
        const entity = await findEntityByCredentials(deviceId, parseInt(req.query.entityId || 0), botSecret);
        if (!entity) {
            return res.status(401).json({ error: 'Invalid credentials' });
        }
        
        const snapshot = await db.collection(MISSION_COLLECTION).findOne({ deviceId });
        if (!snapshot) {
            return res.json({ success: true, rules: [] });
        }
        
        let rules = snapshot.rules || [];
        if (type) {
            rules = rules.filter(r => r.ruleType === type);
        }
        
        res.json({ success: true, rules });
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});

// ============ Helpers ============

function validateDashboard(dashboard) {
    // 驗證必要欄位
    if (!dashboard || typeof dashboard !== 'object') {
        return { error: 'Dashboard must be an object' };
    }
    
    // 可選：驗證各列表結構
    const validLists = ['todoList', 'missionList', 'doneList', 'notes', 'rules'];
    
    return { error: null };
}

async function findEntityByCredentials(deviceId, entityId, botSecret) {
    // 從資料庫查找 entity
    const device = await db.collection('devices').findOne({ deviceId });
    if (!device || !device.entities) return null;
    
    const entity = device.entities.find(e => e.entityId === entityId);
    if (!entity || entity.botSecret !== botSecret) return null;
    
    return entity;
}
