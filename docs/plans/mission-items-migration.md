# Mission Items Migration Plan

## 目標

將所有 Mission Control 資料從 `mission_dashboard` 的 JSONB 陣列遷移到 `mission_items` 表，消除並發衝突。

## 現狀

| 儲存層 | 狀態 |
|--------|------|
| `mission_dashboard` 7 個 JSONB 欄位 | **主要**：todo_list, mission_list, done_list, notes, rules, skills, souls |
| `mission_items` 表 | **孤立**：只有 4 個 `/items/*` 端點使用，未與 JSONB 同步 |
| `mission_notes` 表 | Notes 有獨立表，但 JSONB 仍是主要讀取來源 |
| `mission_rules` 表 | Rules 雙寫（JSONB + 表），但讀取仍走 JSONB |

### 問題
- 所有 `/todo/*`, `/skill/*`, `/soul/*`, `/note/*`, `/rule/*` 端點都操作 JSONB 陣列
- 每次寫入替換整個陣列 → 並發衝突
- Category 讓每個 item 多一個可修改欄位，衝突風險更高

## 方案：統一遷移到 `mission_items` 表

### Phase 1：擴展 Schema

```sql
-- 擴展 mission_items 表支援所有類型
ALTER TABLE mission_items ADD COLUMN IF NOT EXISTS category VARCHAR(64);
ALTER TABLE mission_items ADD COLUMN IF NOT EXISTS item_type VARCHAR(32) NOT NULL DEFAULT 'todo';
-- item_type: 'todo', 'mission', 'done', 'note', 'rule', 'skill', 'soul'

-- Note 專屬欄位
ALTER TABLE mission_items ADD COLUMN IF NOT EXISTS content TEXT DEFAULT '';

-- Rule 專屬欄位
ALTER TABLE mission_items ADD COLUMN IF NOT EXISTS rule_type VARCHAR(32);
ALTER TABLE mission_items ADD COLUMN IF NOT EXISTS assigned_entities JSONB DEFAULT '[]';
ALTER TABLE mission_items ADD COLUMN IF NOT EXISTS is_enabled BOOLEAN DEFAULT true;
ALTER TABLE mission_items ADD COLUMN IF NOT EXISTS config JSONB DEFAULT '{}';

-- Skill 專屬欄位
ALTER TABLE mission_items ADD COLUMN IF NOT EXISTS url TEXT DEFAULT '';

-- Soul 專屬欄位
ALTER TABLE mission_items ADD COLUMN IF NOT EXISTS template_id VARCHAR(64);
ALTER TABLE mission_items ADD COLUMN IF NOT EXISTS is_active BOOLEAN DEFAULT true;

-- 共用
ALTER TABLE mission_items ADD COLUMN IF NOT EXISTS is_system BOOLEAN DEFAULT false;
ALTER TABLE mission_items ADD COLUMN IF NOT EXISTS sort_order INTEGER DEFAULT 0;

-- 索引
CREATE INDEX IF NOT EXISTS idx_mission_items_type ON mission_items(device_id, item_type);
CREATE INDEX IF NOT EXISTS idx_mission_items_category ON mission_items(category);
```

### Phase 2：遷移 API 端點（逐類型改寫）

每個類型的改法相同：**讀寫改走 `mission_items` 表，不再碰 JSONB 陣列**。

#### 2a. TODO 端點（5 個）

| 端點 | 改法 |
|------|------|
| `POST /todo/add` | `INSERT INTO mission_items (id, device_id, item_type, title, ..., category)` |
| `POST /todo/update` | `UPDATE mission_items SET title=$1, category=$2 WHERE id=$3 AND device_id=$4` |
| `POST /todo/delete` | `DELETE FROM mission_items WHERE id=$1 AND device_id=$2` |
| `POST /todo/start` | `UPDATE mission_items SET status='STARTED' WHERE ...` |
| `POST /todo/done` | `UPDATE mission_items SET item_type='done', status='COMPLETED', completed_at=NOW() WHERE ...` |

**Title-based lookup 改為 ID-based**：
- 新增：回傳 `id` (UUID)
- 更新/刪除：優先用 `id`，fallback `title` (向後相容)

#### 2b. Note 端點（3 個）

| 端點 | 改法 |
|------|------|
| `POST /note/add` | `INSERT INTO mission_items (item_type='note', title, content, category)` |
| `POST /note/update` | `UPDATE mission_items SET ... WHERE id=$1` |
| `POST /note/delete` | `DELETE FROM mission_items WHERE id=$1` |
| `GET /notes` | `SELECT * FROM mission_items WHERE device_id=$1 AND item_type='note'` + category filter |

#### 2c. Rule 端點（4 個）

同上模式，`item_type='rule'`，額外欄位 `rule_type`, `assigned_entities`, `is_enabled`, `config`。

#### 2d. Skill 端點（3 個）

同上模式，`item_type='skill'`，額外欄位 `url`, `assigned_entities`。

#### 2e. Soul 端點（4 個）

同上模式，`item_type='soul'`，額外欄位 `template_id`, `assigned_entities`, `is_active`。

### Phase 3：改寫 Dashboard GET/POST

#### `GET /dashboard`
```js
// 從 mission_items 組裝各類型陣列
const items = await client.query(
  'SELECT * FROM mission_items WHERE device_id = $1 ORDER BY sort_order, created_at DESC',
  [deviceId]
);
const grouped = groupBy(items.rows, 'item_type');
res.json({
  success: true,
  dashboard: {
    todoList: grouped.todo || [],
    missionList: grouped.mission || [],
    doneList: grouped.done || [],
    notes: grouped.note || [],
    rules: grouped.rule || [],
    skills: [...systemSkills, ...(grouped.skill || [])],
    souls: grouped.soul || [],
    version: dashboardRow.version
  }
});
```

#### `POST /dashboard`（整批上傳）
- 接收完整 dashboard → 用 transaction 做 diff-based sync
- 比較 incoming items vs DB items（by id）
- INSERT 新的、UPDATE 有變的、DELETE 消失的
- 這樣即使整批上傳，也是 row-level 操作

### Phase 4：資料遷移腳本

```js
async function migrateJsonbToItems(pool) {
  const dashboards = await pool.query('SELECT * FROM mission_dashboard');
  for (const row of dashboards.rows) {
    const deviceId = row.device_id;
    const types = [
      ['todo', row.todo_list],
      ['mission', row.mission_list],
      ['done', row.done_list],
      ['note', row.notes],
      ['rule', row.rules],
      ['skill', row.skills],
      ['soul', row.souls],
    ];
    for (const [itemType, items] of types) {
      for (const item of (items || [])) {
        if (item.isSystem) continue; // skip system skills
        await pool.query(`
          INSERT INTO mission_items (id, device_id, item_type, title, description,
            priority, status, assigned_bot, category, content, rule_type,
            assigned_entities, is_enabled, config, url, template_id, is_active,
            created_by, created_at, updated_at)
          VALUES ($1,$2,$3,$4,$5,$6,$7,$8,$9,$10,$11,$12,$13,$14,$15,$16,$17,$18,
            to_timestamp($19/1000.0), to_timestamp($20/1000.0))
          ON CONFLICT (id) DO NOTHING
        `, [
          item.id || uuidv4(), deviceId, itemType,
          item.title || item.name || '', item.description || '',
          item.priority || 2, item.status || 'PENDING',
          item.assignedBot || null, item.category || null,
          item.content || '', item.ruleType || null,
          JSON.stringify(item.assignedEntities || []),
          item.isEnabled !== false, JSON.stringify(item.config || {}),
          item.url || '', item.templateId || null,
          item.isActive !== false, item.createdBy || 'migrated',
          item.createdAt || Date.now(), item.updatedAt || Date.now()
        ]);
      }
    }
  }
}
```

### Phase 5：清理

- ~~移除 JSONB 欄位~~ → 保留但不再寫入（backward compat for old clients）
- 移除 `mission_notes` 表（合併進 `mission_items`）
- 移除 `mission_rules` 表的雙寫邏輯
- 更新 `POST /dashboard` 同步邏輯

## 執行步驟（開發順序）

| 步驟 | 改動範圍 | 風險 |
|------|----------|------|
| 1. Schema migration | `mission_schema.sql`, `db.js` | 低（只加欄位） |
| 2. 資料遷移腳本 | 新增 `scripts/migrate-mission-items.js` | 中（需跑一次） |
| 3. 改寫 TODO 端點 | `mission.js` 5 個端點 | 中 |
| 4. 改寫 Note 端點 | `mission.js` 4 個端點 | 中 |
| 5. 改寫 Rule 端點 | `mission.js` 4 個端點 | 中 |
| 6. 改寫 Skill 端點 | `mission.js` 3 個端點 | 中 |
| 7. 改寫 Soul 端點 | `mission.js` 4 個端點 | 中 |
| 8. 改寫 Dashboard GET/POST | `mission.js` 2 個端點 | 高（核心路徑） |
| 9. 改寫 Notify | `mission.js` 1 個端點 | 低 |
| 10. 更新 Jest tests | `tests/jest/mission.test.js` | 低 |
| 11. 清理舊表/雙寫 | `mission.js`, schema | 低 |

## API 向後相容

| 面向 | 策略 |
|------|------|
| Bot API 介面 | **不變** — 仍用 title/name 定位，category 可選 |
| 回傳格式 | **不變** — 回傳相同 JSON 結構 |
| Dashboard GET | **不變** — 回傳相同 7 個陣列 |
| Dashboard POST | **改進** — 改為 diff-based sync，不再整批覆蓋 |
| `/items/*` 端點 | **統一** — 成為所有類型的底層 CRUD |

## 並發改善效果

| 場景 | 遷移前 | 遷移後 |
|------|--------|--------|
| Bot A 改 TODO #1，Bot B 改 TODO #2 | 後者覆蓋前者 | 各改各的 row，零衝突 |
| Bot A 加 TODO，Bot B 改 category | 後者可能丟掉新 TODO | INSERT vs UPDATE，零衝突 |
| Category rename（批量） | 替換整個 JSONB | `UPDATE SET category=$1 WHERE category=$2`，一條 SQL |
| Dashboard 整批上傳 | 後者覆蓋全部 | Diff-based sync，只改變化的 row |
