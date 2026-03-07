# Skill Contribution Auto-Approve + Admin Dashboard Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Replace manual skill contribution review with auto-approve pipeline (GitHub URL validation), add admin contribution history UI, document bot schedule API, and fix entity 4's schedule.

**Architecture:** New `skill_contributions` PostgreSQL table holds all contribution history (persistent across Railway redeploys). On startup, approved DB entries are merged into the in-memory `skillTemplatesData` list. Async GitHub API validation happens after the bot receives an immediate response. Old pending JSON system is fully removed.

**Tech Stack:** Node.js/Express, PostgreSQL (via existing `pool.query`), admin.html (vanilla JS + `i18n.t()`), Markdown

---

## Task 1: DB Migration — Add `skill_contributions` Table

**Files:**
- Modify: `backend/db.js` (in `initDb()`, after existing `CREATE TABLE IF NOT EXISTS` blocks)

**Step 1: Find the right insertion point in db.js**

Open `backend/db.js`. Search for the last `CREATE TABLE IF NOT EXISTS` block before the `ALTER TABLE` migration lines. Add the new table creation **inside the same `client.query` transaction block**.

**Step 2: Add table + index**

```javascript
await client.query(`
  CREATE TABLE IF NOT EXISTS skill_contributions (
    id SERIAL PRIMARY KEY,
    pending_id TEXT NOT NULL UNIQUE,
    skill_id TEXT NOT NULL,
    label TEXT,
    icon TEXT,
    title TEXT NOT NULL,
    url TEXT NOT NULL,
    author TEXT,
    required_vars JSONB DEFAULT '[]',
    steps TEXT,
    submitted_by JSONB NOT NULL,
    submitted_at TIMESTAMPTZ DEFAULT NOW(),
    status TEXT NOT NULL DEFAULT 'verifying',
    verified_at TIMESTAMPTZ,
    verification_result JSONB,
    rejected_reason TEXT
  )
`);
await client.query(`
  CREATE INDEX IF NOT EXISTS idx_skill_contrib_status ON skill_contributions(status)
`);
```

**Step 3: Add DB helper functions to db.js**

At the bottom of `db.js`, before `module.exports`, add:

```javascript
// --- Skill Contributions ---
async function insertSkillContribution(entry) {
    await pool.query(
        `INSERT INTO skill_contributions
         (pending_id, skill_id, label, icon, title, url, author, required_vars, steps, submitted_by, status)
         VALUES ($1,$2,$3,$4,$5,$6,$7,$8,$9,$10,'verifying')`,
        [entry.pendingId, entry.id, entry.label, entry.icon, entry.title,
         entry.url, entry.author, JSON.stringify(entry.requiredVars || []),
         entry.steps, JSON.stringify(entry.submittedBy)]
    );
}

async function updateSkillContribution(pendingId, updates) {
    const sets = [];
    const vals = [];
    let i = 1;
    if (updates.status)              { sets.push(`status=$${i++}`);              vals.push(updates.status); }
    if (updates.verifiedAt)          { sets.push(`verified_at=$${i++}`);         vals.push(updates.verifiedAt); }
    if (updates.verificationResult)  { sets.push(`verification_result=$${i++}`); vals.push(JSON.stringify(updates.verificationResult)); }
    if (updates.rejectedReason)      { sets.push(`rejected_reason=$${i++}`);     vals.push(updates.rejectedReason); }
    vals.push(pendingId);
    await pool.query(`UPDATE skill_contributions SET ${sets.join(',')} WHERE pending_id=$${i}`, vals);
}

async function getSkillContributions() {
    const result = await pool.query(
        `SELECT * FROM skill_contributions ORDER BY submitted_at DESC`
    );
    return result.rows;
}

async function getApprovedSkillContributions() {
    const result = await pool.query(
        `SELECT * FROM skill_contributions WHERE status='approved' ORDER BY verified_at ASC`
    );
    return result.rows;
}
```

Add to `module.exports`:
```javascript
insertSkillContribution,
updateSkillContribution,
getSkillContributions,
getApprovedSkillContributions,
```

**Step 4: Verify syntax**

```bash
node -e "require('./backend/db.js'); console.log('db.js OK')"
```
Expected: `db.js OK` (no errors)

**Step 5: Commit**

```bash
git add backend/db.js
git commit -m "feat(db): add skill_contributions table + helper functions"
```

---

## Task 2: Backend — Replace Pending System with Auto-Approve Pipeline

**Files:**
- Modify: `backend/index.js` (Skill Templates API block, lines ~652–830)

### Step 1: Remove old pending infrastructure

Delete these lines from `index.js`:

```javascript
// DELETE these lines:
const pendingTemplatesPath = path.join(__dirname, 'data/skill-templates-pending.json');
let pendingTemplatesData = (() => { ... })();
function savePendingTemplates() { ... }
```

### Step 2: Add contributions log bootstrap (replace the deleted lines)

After `const skillTemplatesData = ...`, add:

```javascript
// Load approved contributions from DB on startup and merge into in-memory list
// (runs async, safe — DB entries supplement the git-tracked JSON)
async function loadApprovedContributions() {
    try {
        const rows = await db.getApprovedSkillContributions();
        for (const row of rows) {
            const alreadyInList = skillTemplatesData.some(t => t.id === row.skill_id);
            if (!alreadyInList) {
                skillTemplatesData.push({
                    id: row.skill_id,
                    label: row.label,
                    icon: row.icon,
                    title: row.title,
                    url: row.url,
                    author: row.author,
                    updatedAt: row.verified_at ? row.verified_at.toISOString().slice(0, 10) : '',
                    requiredVars: row.required_vars || [],
                    steps: row.steps
                });
            }
        }
        if (process.env.DEBUG === 'true') console.log(`[SKILL] Loaded ${rows.length} approved contributions from DB`);
    } catch (err) {
        console.error('[SKILL] Failed to load approved contributions from DB:', err.message);
    }
}
// Fire after DB is ready (call this after initDb resolves in the startup sequence)
```

**Find where DB init resolves in index.js** (search for `initDb` or `db.init`). After that call, add:
```javascript
loadApprovedContributions();
```

### Step 3: Replace `POST /api/skill-templates/contribute`

Replace the entire existing route with:

```javascript
/**
 * POST /api/skill-templates/contribute
 * Bot submits a new skill. Auto-verifies GitHub URL asynchronously.
 * Auth: deviceId + botSecret + entityId
 */
app.post('/api/skill-templates/contribute', async (req, res) => {
    const { deviceId, botSecret, entityId, skill } = req.body;

    if (!deviceId || !botSecret || !skill) {
        return res.status(400).json({ success: false, error: 'deviceId, botSecret, and skill required' });
    }
    const device = devices[deviceId];
    if (!device) return res.status(404).json({ success: false, error: 'Device not found' });

    const eId = parseInt(entityId) || 0;
    const entity = device.entities[eId];
    if (!entity || !entity.isBound || entity.botSecret !== botSecret) {
        return res.status(403).json({ success: false, error: 'Invalid botSecret or entity not bound' });
    }

    const { id, label, icon, title, url, author, requiredVars, steps } = skill;
    if (!id || !title || !url || !steps) {
        return res.status(400).json({ success: false, error: 'skill must include: id, title, url, steps' });
    }

    // Duplicate ID check (approved list)
    if (skillTemplatesData.some(t => t.id === id)) {
        serverLog('warn', 'skill_contribute', `Duplicate skill id rejected: ${id}`, { deviceId, entityId: eId });
        return res.status(409).json({ success: false, error: `Skill id "${id}" already exists. Choose a different id.` });
    }

    const pendingId = crypto.randomUUID();
    const entry = {
        pendingId, id,
        label: label || id, icon: icon || '🔧',
        title, url, author: author || entity.name || `entity_${eId}`,
        requiredVars: requiredVars || [], steps,
        submittedBy: { deviceId, entityId: eId, entityName: entity.name || null }
    };

    // Persist to DB immediately (status: verifying)
    await db.insertSkillContribution(entry);
    serverLog('info', 'skill_contribute', `Skill contribution received: ${id} (verifying)`, { deviceId, entityId: eId, metadata: { skillId: id, pendingId } });

    // Respond immediately — don't wait for GitHub check
    res.json({ success: true, pendingId, message: `Skill "${title}" submitted. Auto-verifying GitHub URL...` });

    // Async GitHub URL verification
    setImmediate(async () => {
        try {
            const match = url.match(/github\.com\/([^\/]+)\/([^\/\s]+)/);
            if (!match) {
                await db.updateSkillContribution(pendingId, { status: 'rejected', rejectedReason: 'not_github_url' });
                serverLog('warn', 'skill_contribute', `Skill ${id} rejected: not a GitHub URL`, { deviceId, entityId: eId });
                return;
            }
            const [, owner, repo] = match;
            const cleanRepo = repo.replace(/\/$/, '').replace(/\.git$/, '');
            const ghResp = await fetch(`https://api.github.com/repos/${owner}/${cleanRepo}`, {
                headers: { 'User-Agent': 'EclawBot/1.0' }
            });

            if (ghResp.status === 200) {
                const ghData = await ghResp.json();
                const approved = {
                    id, label: entry.label, icon: entry.icon, title, url,
                    author: entry.author,
                    updatedAt: new Date().toISOString().slice(0, 10),
                    requiredVars: entry.requiredVars, steps
                };
                skillTemplatesData.push(approved);
                fs.writeFileSync(path.join(__dirname, 'data/skill-templates.json'), JSON.stringify(skillTemplatesData, null, 2));
                await db.updateSkillContribution(pendingId, {
                    status: 'approved',
                    verifiedAt: new Date().toISOString(),
                    verificationResult: { githubStatus: 200, stars: ghData.stargazers_count, description: ghData.description }
                });
                serverLog('info', 'skill_approve', `Skill auto-approved: ${id} (stars: ${ghData.stargazers_count})`, { deviceId, entityId: eId, metadata: { skillId: id } });
                if (process.env.DEBUG === 'true') console.log(`[SKILL] Auto-approved: ${id}`);
            } else {
                await db.updateSkillContribution(pendingId, {
                    status: 'rejected',
                    rejectedReason: `github_${ghResp.status}`,
                    verificationResult: { githubStatus: ghResp.status }
                });
                serverLog('warn', 'skill_contribute', `Skill ${id} rejected: GitHub returned ${ghResp.status}`, { deviceId, entityId: eId });
            }
        } catch (err) {
            await db.updateSkillContribution(pendingId, { status: 'rejected', rejectedReason: `error: ${err.message}` });
            console.error('[SKILL] Auto-verify error:', err.message);
        }
    });
});
```

### Step 4: Remove old pending endpoints

Delete these entire route blocks:
- `GET /api/skill-templates/pending`
- `POST /api/skill-templates/pending/:pendingId/approve`
- `DELETE /api/skill-templates/pending/:pendingId`

### Step 5: Add new admin API endpoints (after the existing GET /api/skill-templates route)

```javascript
/**
 * GET /api/skill-templates/contributions
 * Admin: view full contribution history (all statuses).
 */
app.get('/api/skill-templates/contributions', async (req, res) => {
    if (!verifyAdmin(req)) return res.status(403).json({ success: false, error: 'Admin token required' });
    try {
        const rows = await db.getSkillContributions();
        const contributions = rows.map(r => ({
            pendingId: r.pending_id,
            id: r.skill_id,
            title: r.title,
            url: r.url,
            author: r.author,
            icon: r.icon,
            submittedBy: r.submitted_by,
            submittedAt: r.submitted_at,
            status: r.status,
            verifiedAt: r.verified_at,
            verificationResult: r.verification_result,
            rejectedReason: r.rejected_reason
        }));
        res.json({ success: true, count: contributions.length, contributions });
    } catch (err) {
        res.status(500).json({ success: false, error: err.message });
    }
});

/**
 * DELETE /api/skill-templates/:skillId
 * Admin: revoke an approved skill from the live registry.
 */
app.delete('/api/skill-templates/:skillId', async (req, res) => {
    if (!verifyAdmin(req)) return res.status(403).json({ success: false, error: 'Admin token required' });
    const { skillId } = req.params;
    const idx = skillTemplatesData.findIndex(t => t.id === skillId);
    if (idx === -1) return res.status(404).json({ success: false, error: `Skill "${skillId}" not found in registry` });

    skillTemplatesData.splice(idx, 1);
    fs.writeFileSync(path.join(__dirname, 'data/skill-templates.json'), JSON.stringify(skillTemplatesData, null, 2));
    serverLog('info', 'skill_approve', `Skill revoked by admin: ${skillId}`, {});
    res.json({ success: true, message: `Skill "${skillId}" removed from registry` });
});
```

**Step 6: Verify syntax**

```bash
node -e "require('./backend/index.js')" 2>&1 | head -5
```
Expected: no `SyntaxError`

**Step 7: Commit**

```bash
git add backend/index.js
git commit -m "feat(skill-templates): auto-approve pipeline + contributions history API

- Remove manual pending system (JSON file + 3 manual endpoints)
- POST /api/skill-templates/contribute: immediate response + async GitHub verify
- Duplicate ID check against approved list before insert
- Auto-approve (HTTP 200) or auto-reject with reason stored in DB
- GET /api/skill-templates/contributions: admin history endpoint
- DELETE /api/skill-templates/:skillId: admin revoke endpoint
- On startup: merge DB-approved skills into in-memory list"
```

---

## Task 3: admin.html — Skill Contributions Tab

**Files:**
- Modify: `backend/public/admin.html`

### Step 1: Add i18n keys

Find the i18n keys object in admin.html (search for `admin_title` or `i18n.addKeys`). Add:

```javascript
// In the en locale section:
skill_contrib_tab: 'Skill Contributions',
skill_contrib_status_all: 'All',
skill_contrib_status_approved: 'Approved',
skill_contrib_status_rejected: 'Rejected',
skill_contrib_status_verifying: 'Verifying',
skill_contrib_revoke: 'Revoke',
skill_contrib_revoke_confirm: 'Remove this skill from the live registry?',
skill_contrib_revoked: 'Skill revoked',
skill_contrib_no_data: 'No contributions yet',
skill_contrib_github_404: 'GitHub 404 — repo not found',

// In the zh-TW locale section:
skill_contrib_tab: '技能貢獻紀錄',
skill_contrib_status_all: '全部',
skill_contrib_status_approved: '已批准',
skill_contrib_status_rejected: '已拒絕',
skill_contrib_status_verifying: '驗證中',
skill_contrib_revoke: '撤銷',
skill_contrib_revoke_confirm: '從正式列表移除此技能？',
skill_contrib_revoked: '技能已撤銷',
skill_contrib_no_data: '尚無貢獻記錄',
skill_contrib_github_404: 'GitHub 404 — repo 不存在',
```

### Step 2: Add `contribData` variable

Find where `let statsData, bindingsData, usersData, botsData;` is declared. Add:

```javascript
let contribData = null;
```

### Step 3: Add to `loadAll()`

Find the `Promise.all([...])` in `loadAll()`. Add contributions fetch:

```javascript
const [stats, bindings, users, bots, contribs] = await Promise.all([
    apiCall('GET', '/api/admin/stats'),
    apiCall('GET', '/api/admin/bindings'),
    apiCall('GET', '/api/admin/users'),
    apiCall('GET', '/api/admin/bots'),
    apiCall('GET', '/api/skill-templates/contributions')
]);
// ...existing assignments...
contribData = contribs;
```

### Step 4: Add contributions section to `render()`

At the end of the `render()` function, before the final `adminContent.innerHTML = html` line, append:

```javascript
// Skill Contributions section
html += '<div class="section-card">';
html += '<h2>' + i18n.t('skill_contrib_tab') + '</h2>';

const contribs = (contribData && contribData.contributions) ? contribData.contributions : [];
if (contribs.length === 0) {
    html += '<p style="color:var(--muted)">' + i18n.t('skill_contrib_no_data') + '</p>';
} else {
    html += '<table class="admin-table"><thead><tr>' +
        '<th>ID</th><th>Title</th><th>By</th><th>Status</th><th>GitHub</th><th>Date</th><th></th>' +
        '</tr></thead><tbody>';

    contribs.forEach(c => {
        const statusIcon = c.status === 'approved' ? '✅' : c.status === 'rejected' ? '❌' : '⏳';
        const statusLabel = statusIcon + ' ' + i18n.t('skill_contrib_status_' + c.status, c.status);
        const ghInfo = c.verificationResult
            ? (c.verificationResult.githubStatus === 200
                ? `★${c.verificationResult.stars} ${escapeHtml(c.verificationResult.description || '')}`.slice(0, 60)
                : `GitHub ${c.verificationResult.githubStatus}`)
            : (c.rejectedReason || '—');
        const dateStr = c.submittedAt ? new Date(c.submittedAt).toLocaleDateString() : '—';
        const byStr = escapeHtml((c.submittedBy && c.submittedBy.entityName) || `entity_${c.submittedBy && c.submittedBy.entityId}`);
        const revokeBtn = c.status === 'approved'
            ? `<button class="btn-sm btn-danger" onclick="revokeSkill('${escapeHtml(c.id)}')">${i18n.t('skill_contrib_revoke')}</button>`
            : '';
        html += `<tr>
            <td><code>${escapeHtml(c.id)}</code></td>
            <td>${escapeHtml(c.title)}</td>
            <td>${byStr}</td>
            <td>${statusLabel}</td>
            <td style="font-size:0.85em;color:var(--muted)">${escapeHtml(ghInfo)}</td>
            <td>${dateStr}</td>
            <td>${revokeBtn}</td>
        </tr>`;
    });
    html += '</tbody></table>';
}
html += '</div>';
```

### Step 5: Add `revokeSkill()` function

After the existing admin action functions, add:

```javascript
async function revokeSkill(skillId) {
    if (!confirm(i18n.t('skill_contrib_revoke_confirm'))) return;
    try {
        await apiCall('DELETE', '/api/skill-templates/' + encodeURIComponent(skillId));
        alert(i18n.t('skill_contrib_revoked'));
        loadAll();
    } catch (e) {
        alert('Error: ' + e.message);
    }
}
```

**Step 6: Commit**

```bash
git add backend/public/admin.html
git commit -m "feat(admin): add Skill Contributions history tab with revoke action"
```

---

## Task 4: E-claw_mcp_skill.md — Fix Chapter 11 + Add Chapter 12

**Files:**
- Modify: `backend/E-claw_mcp_skill.md`

### Step 1: Fix the wrong scheduler reference in Chapter 11

Find and **delete** the "Scheduled Contribution" subsection at the end of Chapter 11 (lines ~1673–1689):

```markdown
### Scheduled Contribution (Recurring Search + Contribute)
...POST /api/mission/scheduler/create   ← WRONG, this endpoint does not exist
...
```

Replace with:

```markdown
### Scheduled Contribution (Recurring Search + Contribute)

To run the search + contribute cycle every hour automatically, create a cron schedule using the Bot Schedule API (see Chapter 12):

exec: curl -s -X POST "https://eclawbot.com/api/bot/schedules" -H "Content-Type: application/json" -d '{"deviceId":"YOUR_DEVICE_ID","botSecret":"YOUR_BOT_SECRET","entityId":YOUR_ENTITY_ID,"message":"HOURLY TASK: (1) GET https://eclawbot.com/api/skill-templates to see existing skill IDs, (2) web_search for a new popular OpenClaw skill on GitHub not already in the list, (3) verify the GitHub URL returns HTTP 200 via curl -s -o /dev/null -w \"%{http_code}\" https://api.github.com/repos/OWNER/REPO, (4) if valid, POST to https://eclawbot.com/api/skill-templates/contribute with real skill details","repeatType":"cron","cronExpr":"0 * * * *","label":"Hourly Skill Hunter"}'
```

### Step 2: Add Chapter 12 at end of file

Append to the very end of the file:

```markdown
---

## 12. Bot Schedule API

Bots can create, list, and delete schedules using **only their `botSecret`** — no `deviceSecret` required.

Schedules appear in the user's Schedule page on the Portal and Android App. When triggered, the bot receives a push notification (same format as user messages) with the schedule message as content. The bot has **5 minutes to respond** via `POST /api/transform` or it loses -10 XP.

### `POST /api/bot/schedules` — Create a schedule

```
exec: curl -s -X POST "https://eclawbot.com/api/bot/schedules" \
  -H "Content-Type: application/json" \
  -d '{
    "deviceId": "YOUR_DEVICE_ID",
    "entityId": YOUR_ENTITY_ID,
    "botSecret": "YOUR_BOT_SECRET",
    "message": "Task description the bot receives when triggered",
    "repeatType": "cron",
    "cronExpr": "0 * * * *",
    "label": "Hourly Task",
    "timezone": "Asia/Taipei"
  }'
```

| Field | Required | Notes |
|-------|----------|-------|
| `deviceId` | ✅ | |
| `entityId` | ✅ | |
| `botSecret` | ✅ | |
| `message` | ✅ | Content bot receives when schedule fires |
| `repeatType` | — | `"once"` (default) or `"cron"` |
| `cronExpr` | Required if `repeatType="cron"` | Standard 5-field cron (e.g. `"0 * * * *"` = hourly) |
| `scheduledAt` | Required if `repeatType="once"` | ISO 8601 datetime |
| `label` | — | Human-readable name shown in UI |
| `timezone` | — | IANA timezone (e.g. `"Asia/Taipei"`). Default: UTC |

**Response:**
```json
{ "success": true, "schedule": { "id": 42, "status": "active", "cronExpr": "0 * * * *", ... } }
```

### `GET /api/bot/schedules` — List your schedules

```
exec: curl -s "https://eclawbot.com/api/bot/schedules?deviceId=YOUR_DEVICE_ID&entityId=YOUR_ENTITY_ID&botSecret=YOUR_BOT_SECRET"
```

### `DELETE /api/bot/schedules/:id` — Delete a schedule

```
exec: curl -s -X DELETE "https://eclawbot.com/api/bot/schedules/42" \
  -H "Content-Type: application/json" \
  -d '{"deviceId":"YOUR_DEVICE_ID","entityId":YOUR_ENTITY_ID,"botSecret":"YOUR_BOT_SECRET"}'
```
```

**Step 3: Verify no broken markdown**

```bash
node -e "
const fs = require('fs');
const content = fs.readFileSync('backend/E-claw_mcp_skill.md', 'utf8');
const lines = content.split('\n').length;
console.log('Lines:', lines, '— OK');
"
```

**Step 4: Commit**

```bash
git add backend/E-claw_mcp_skill.md
git commit -m "docs(skill-md): fix ch11 scheduler ref + add ch12 bot schedule API"
```

---

## Task 5: Deploy + Fix Entity 4 Schedule

**Step 1: Push all commits**

```bash
git push origin main
```

Expected: Railway auto-deploys within ~2 min (watches `backend/**`)

**Step 2: Wait for deploy and verify new API**

```bash
node -e "
setTimeout(async () => {
  const r = await fetch('https://eclawbot.com/api/skill-templates/contributions', {
    headers: {'x-admin-token': 'dev-only-localhost'}
  });
  const d = await r.json();
  console.log('Contributions endpoint:', d.success ? 'OK, count=' + d.count : 'FAIL', JSON.stringify(d).slice(0,200));
}, 90000);
console.log('Waiting 90s for Railway...');
" 2>&1
```

Expected: `Contributions endpoint: OK, count=2` (bat-cat rejected + arxiv-digest approved)

**Step 3: Notify entity 4 to delete wrong Rule and set real schedule**

Run `node backend/_fix_entity4_schedule.js` (create this file):

```javascript
// backend/_fix_entity4_schedule.js
const DEVICE_ID = '480def4c-2183-4d8e-afd0-b131ae89adcc';
const BOT_SECRET_0 = 'f5ad89b82675def1f0e3b222e793c2ac';

const msg = `[ADMIN UPDATE] The schedule system has been upgraded.

Please do TWO things now:

1. DELETE the wrong Mission Rule you created earlier:
exec: curl -s -X POST "https://eclawbot.com/api/mission/rule/delete" -H "Content-Type: application/json" -d '{"deviceId":"${DEVICE_ID}","entityId":4,"botSecret":"YOUR_BOT_SECRET","name":"Hourly Skill Hunter"}'

2. CREATE a real hourly cron schedule using the Bot Schedule API:
exec: curl -s -X POST "https://eclawbot.com/api/bot/schedules" -H "Content-Type: application/json" -d '{"deviceId":"${DEVICE_ID}","entityId":4,"botSecret":"YOUR_BOT_SECRET","message":"HOURLY TASK: (1) GET https://eclawbot.com/api/skill-templates to see existing IDs, (2) web_search for new popular OpenClaw skills on GitHub, (3) verify GitHub URL returns 200, (4) POST to https://eclawbot.com/api/skill-templates/contribute with real skill details","repeatType":"cron","cronExpr":"0 * * * *","label":"Hourly Skill Hunter","timezone":"Asia/Taipei"}'

After both commands, confirm with your status update.`;

fetch('https://eclawbot.com/api/entity/speak-to', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ deviceId: DEVICE_ID, fromEntityId: 0, toEntityId: 4, botSecret: BOT_SECRET_0, text: msg })
}).then(r => r.json()).then(d => console.log('Sent:', d.success)).catch(console.error);
```

```bash
node backend/_fix_entity4_schedule.js
```

**Step 4: Verify entity 4 created the real schedule**

```bash
node -e "
// Wait for entity 4 to respond, then verify schedule exists
setTimeout(async () => {
  const r = await fetch('https://eclawbot.com/api/entities?deviceId=480def4c-2183-4d8e-afd0-b131ae89adcc');
  const d = await r.json();
  const e4 = d.entities.find(e => e.entityId === 4);
  console.log('Entity 4:', e4 ? e4.state + ' | ' + (e4.message||'').slice(0,100) : 'not found');
}, 60000);
console.log('Waiting 60s...');
" 2>&1
```

**Step 5: Confirm Mission Dashboard Rule is gone**

```bash
node -e "
fetch('https://eclawbot.com/api/mission/dashboard?deviceId=480def4c-2183-4d8e-afd0-b131ae89adcc&entityId=0&botSecret=f5ad89b82675def1f0e3b222e793c2ac')
.then(r=>r.json()).then(d=>{
  const rule = (d.dashboard.rules||[]).find(r=>r.name==='Hourly Skill Hunter');
  console.log('Hourly Skill Hunter rule:', rule ? 'STILL EXISTS (not deleted)' : 'GONE (correct)');
}).catch(console.error);
" 2>&1
```

Expected: `Hourly Skill Hunter rule: GONE (correct)`

**Step 6: Final commit — clean up temp scripts**

```bash
git rm backend/_send_entity4.js backend/_send_retry.js backend/_send_schedule.js \
        backend/_send_to_entity4.py backend/_poll_pending.js backend/_wait_and_check.js \
        backend/_monitor_entity4.js backend/_final_check.js 2>/dev/null; \
git add -A && git commit -m "chore: remove temp debugging scripts"
git push origin main
```

---

## Definition of Done

- [ ] `POST /api/skill-templates/contribute` responds immediately, async-verifies GitHub URL, auto-approves or rejects
- [ ] Duplicate skill ID returns 409 before any DB insert
- [ ] `GET /api/skill-templates/contributions` returns full history (admin-only)
- [ ] `DELETE /api/skill-templates/:skillId` removes from live registry (admin-only)
- [ ] admin.html shows Skill Contributions table with revoke button
- [ ] `E-claw_mcp_skill.md` Chapter 11 has correct bot schedule curl, Chapter 12 exists
- [ ] Entity 4 has a real cron schedule (`GET /api/bot/schedules` confirms it)
- [ ] Mission Dashboard no longer has `Hourly Skill Hunter` rule
