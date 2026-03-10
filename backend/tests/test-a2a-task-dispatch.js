/**
 * A2A Task Dispatch Test — Phase One
 *
 * Tests the Agent-to-Agent task dispatch flow:
 *   1. EClaw Official Agent sends a structured task to Entity #3
 *   2. Verifies the task was pushed successfully
 *   3. Polls chat history for Entity #3's response
 *   4. Validates response format and content
 *
 * Also tests the new Bot Tools (web-search, web-fetch) endpoints.
 *
 * Credentials from backend/.env:
 *   BROADCAST_TEST_DEVICE_ID, BROADCAST_TEST_DEVICE_SECRET
 *
 * Usage:
 *   node test-a2a-task-dispatch.js
 *   node test-a2a-task-dispatch.js --device <deviceId> --secret <deviceSecret>
 *   node test-a2a-task-dispatch.js --task-only        # skip bot-tools tests
 *   node test-a2a-task-dispatch.js --tools-only       # only test bot-tools endpoints
 */

const path = require('path');
const fs = require('fs');

// ── Config ──────────────────────────────────────────────────
const API_BASE = process.env.API_BASE || 'https://eclawbot.com';
const POLL_INTERVAL_MS = 5000;
const MAX_WAIT_MS = 120000;  // 2 minutes for bot response
const TARGET_ENTITY_ID = 3;

// ── CLI args ─────────────────────────────────────────────────
const args = process.argv.slice(2);
function getArg(name) {
    const idx = args.indexOf(`--${name}`);
    return idx >= 0 && args[idx + 1] ? args[idx + 1] : null;
}
const hasFlag = (name) => args.includes(`--${name}`);

// ── .env loader ─────────────────────────────────────────────
function loadEnvFile() {
    const envPath = path.resolve(__dirname, '..', '.env');
    if (!fs.existsSync(envPath)) return {};
    const vars = {};
    fs.readFileSync(envPath, 'utf8').split('\n').forEach(line => {
        line = line.trim();
        if (!line || line.startsWith('#')) return;
        const idx = line.indexOf('=');
        if (idx > 0) vars[line.slice(0, idx).trim()] = line.slice(idx + 1).trim();
    });
    return vars;
}

// ── Helpers ─────────────────────────────────────────────────
async function fetchJSON(url) {
    const res = await fetch(url);
    if (!res.ok) throw new Error(`GET ${url} → ${res.status}`);
    return res.json();
}

async function postJSON(url, body) {
    const res = await fetch(url, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body),
    });
    const data = await res.json();
    return { status: res.status, ok: res.ok, data };
}

const sleep = ms => new Promise(r => setTimeout(r, ms));

// ── Test Result Tracking ────────────────────────────────────
const results = [];
function check(name, passed, detail = '') {
    results.push({ name, passed, detail });
    const icon = passed ? 'PASS' : 'FAIL';
    const suffix = detail ? ` — ${detail}` : '';
    console.log(`  [${icon}] ${name}${suffix}`);
}

// ── Bot Tools Tests ─────────────────────────────────────────
async function testBotTools(deviceId, botSecret) {
    console.log('\n═══ Bot Tools API Tests ═══\n');

    // Test 1: web-search endpoint
    console.log('Testing /api/bot/web-search...');
    try {
        const searchUrl = `${API_BASE}/api/bot/web-search?q=EClaw%20live%20wallpaper&deviceId=${deviceId}&botSecret=${botSecret}&entityId=0`;
        const searchRes = await fetchJSON(searchUrl);
        check('web-search returns results', searchRes.results && searchRes.results.length > 0,
            `${searchRes.resultCount || 0} results for "EClaw live wallpaper"`);
        check('web-search has query field', searchRes.query === 'EClaw live wallpaper');
        check('web-search results have title+url', searchRes.results.length > 0 &&
            searchRes.results[0].title && searchRes.results[0].url,
            searchRes.results.length > 0 ? `First: "${searchRes.results[0].title}"` : 'No results');
    } catch (err) {
        check('web-search endpoint', false, err.message);
    }

    // Test 2: web-search missing query
    console.log('Testing /api/bot/web-search missing query...');
    try {
        const res = await fetch(`${API_BASE}/api/bot/web-search?deviceId=${deviceId}&botSecret=${botSecret}`);
        check('web-search rejects missing query', res.status === 400);
    } catch (err) {
        check('web-search missing query', false, err.message);
    }

    // Test 3: web-fetch endpoint
    console.log('Testing /api/bot/web-fetch...');
    try {
        const fetchUrl = `${API_BASE}/api/bot/web-fetch?url=${encodeURIComponent('https://eclawbot.com/api/health')}&deviceId=${deviceId}&botSecret=${botSecret}&entityId=0`;
        const fetchRes = await fetchJSON(fetchUrl);
        check('web-fetch returns content', !!fetchRes.content, `${fetchRes.length || 0} chars`);
        check('web-fetch has url field', fetchRes.url === 'https://eclawbot.com/api/health');
    } catch (err) {
        check('web-fetch endpoint', false, err.message);
    }

    // Test 4: web-fetch blocks localhost
    console.log('Testing /api/bot/web-fetch blocks internal URLs...');
    try {
        const res = await fetch(`${API_BASE}/api/bot/web-fetch?url=http://localhost:3000&deviceId=${deviceId}&botSecret=${botSecret}`);
        check('web-fetch blocks localhost', res.status === 403);
    } catch (err) {
        check('web-fetch blocks localhost', false, err.message);
    }

    // Test 5: web-search rate limit headers
    console.log('Testing /api/bot/web-search missing auth...');
    try {
        const res = await fetch(`${API_BASE}/api/bot/web-search?q=test`);
        check('web-search requires auth', res.status === 400);
    } catch (err) {
        check('web-search auth check', false, err.message);
    }
}

// ── A2A Task Dispatch Test ──────────────────────────────────
async function testA2ATaskDispatch(deviceId, deviceSecret) {
    console.log('\n═══ A2A Task Dispatch Test ═══\n');

    // Step 1: Check Entity #3 status
    console.log('Step 1: Checking Entity #3 status...');
    let entity3Status;
    try {
        const status = await fetchJSON(`${API_BASE}/api/device/status?deviceId=${deviceId}&deviceSecret=${deviceSecret}`);
        const entities = status.entities || [];
        const entity3 = entities.find(e => e.entityId === TARGET_ENTITY_ID);
        check('Entity #3 exists', !!entity3);
        check('Entity #3 is bound', entity3 && entity3.isBound, entity3 ? `character: ${entity3.character}` : '');
        entity3Status = entity3;
        if (!entity3 || !entity3.isBound) {
            console.log('  ⚠ Entity #3 is not bound. Skipping task dispatch.');
            return;
        }
    } catch (err) {
        check('Get device status', false, err.message);
        return;
    }

    // Step 2: Record chat history baseline
    console.log('Step 2: Recording chat history baseline...');
    let baselineCount = 0;
    try {
        const history = await fetchJSON(`${API_BASE}/api/chat/history?deviceId=${deviceId}&deviceSecret=${deviceSecret}&entityId=${TARGET_ENTITY_ID}&limit=5`);
        baselineCount = (history.messages || []).length;
        check('Chat history accessible', true, `${baselineCount} existing messages`);
    } catch (err) {
        check('Chat history baseline', false, err.message);
    }

    // Step 3: Send A2A task to Entity #3
    console.log('Step 3: Sending A2A task to Entity #3...');
    const taskMessage = [
        '【EClaw Official Agent — 任務指派】',
        '',
        '任務類型: MISSION_DASHBOARD_UPDATE',
        '優先級: HIGH',
        '截止時間: 立即執行',
        '',
        '任務內容:',
        '請在 Mission Dashboard 上新增一個 TODO 項目：',
        '- 標題: "A2A 通訊測試成功"',
        '- 內容: "EClaw Official Agent 成功透過 A2A 協議派送任務給 Entity #3。測試時間: ' + new Date().toISOString() + '"',
        '',
        '完成後請回報執行結果。',
        '',
        '同時，你現在擁有新的搜尋工具！',
        '嘗試使用 web-search 搜尋 "EClaw live wallpaper" 並回報搜尋結果數量。',
        '工具用法已在 [AVAILABLE TOOLS — Web Search & Fetch] 區塊中。'
    ].join('\n');

    try {
        const sendResult = await postJSON(`${API_BASE}/api/client/speak`, {
            deviceId,
            entityId: TARGET_ENTITY_ID,
            text: taskMessage,
            source: 'EClaw Official Agent'
        });

        check('Task sent successfully', sendResult.ok && sendResult.data.success,
            sendResult.data.pushed ? 'pushed to bot' : 'queued for polling');

        if (!sendResult.ok) {
            console.log('  Response:', JSON.stringify(sendResult.data));
            return;
        }
    } catch (err) {
        check('Send A2A task', false, err.message);
        return;
    }

    // Step 4: Poll for bot response
    console.log(`Step 4: Polling for Entity #3 response (max ${MAX_WAIT_MS / 1000}s)...`);
    const startTime = Date.now();
    let botResponded = false;
    let responseMessage = '';

    while (Date.now() - startTime < MAX_WAIT_MS) {
        await sleep(POLL_INTERVAL_MS);
        const elapsed = Math.round((Date.now() - startTime) / 1000);
        process.stdout.write(`  Waiting... ${elapsed}s\r`);

        try {
            const history = await fetchJSON(`${API_BASE}/api/chat/history?deviceId=${deviceId}&deviceSecret=${deviceSecret}&entityId=${TARGET_ENTITY_ID}&limit=5`);
            const messages = history.messages || [];

            // Look for a new bot response after our task
            const newMessages = messages.filter(m =>
                m.source === 'bot' &&
                new Date(m.timestamp).getTime() > startTime - 5000
            );

            if (newMessages.length > 0) {
                responseMessage = newMessages[0].text || newMessages[0].message || '';
                botResponded = true;
                console.log(`\n  Bot responded after ${elapsed}s`);
                break;
            }
        } catch {
            // Polling error — continue
        }
    }

    check('Bot responded within timeout', botResponded,
        botResponded ? `Response: "${responseMessage.substring(0, 100)}..."` : `No response after ${MAX_WAIT_MS / 1000}s`);

    // Step 5: Check if bot created the TODO
    if (botResponded) {
        console.log('Step 5: Checking Mission Dashboard for new TODO...');
        try {
            const dashboard = await fetchJSON(`${API_BASE}/api/mission/dashboard?deviceId=${deviceId}&deviceSecret=${deviceSecret}`);
            const todos = dashboard.items || [];
            const a2aTodo = todos.find(t => t.title && t.title.includes('A2A'));
            check('Bot created A2A TODO item', !!a2aTodo,
                a2aTodo ? `Found: "${a2aTodo.title}"` : 'Not found in dashboard');
        } catch (err) {
            check('Mission Dashboard check', false, err.message);
        }
    }
}

// ── Main ────────────────────────────────────────────────────
async function main() {
    console.log('╔══════════════════════════════════════════╗');
    console.log('║  A2A Task Dispatch & Bot Tools Test      ║');
    console.log('║  Phase One Testing                       ║');
    console.log('╚══════════════════════════════════════════╝');

    const env = loadEnvFile();
    const deviceId = getArg('device') || env.BROADCAST_TEST_DEVICE_ID;
    const deviceSecret = getArg('secret') || env.BROADCAST_TEST_DEVICE_SECRET;

    if (!deviceId || !deviceSecret) {
        console.error('\n  ✗ Missing credentials. Provide via:');
        console.error('    --device <id> --secret <secret>');
        console.error('    or set BROADCAST_TEST_DEVICE_ID / BROADCAST_TEST_DEVICE_SECRET in backend/.env');
        process.exit(1);
    }

    console.log(`\n  Device: ${deviceId}`);
    console.log(`  Target: Entity #${TARGET_ENTITY_ID}`);
    console.log(`  API: ${API_BASE}\n`);

    // Get a botSecret for bot-tools auth (from any bound entity)
    let botSecret = null;
    try {
        const status = await fetchJSON(`${API_BASE}/api/device/status?deviceId=${deviceId}&deviceSecret=${deviceSecret}`);
        const bound = (status.entities || []).find(e => e.isBound && e.botSecret);
        if (bound) botSecret = bound.botSecret;
    } catch {
        // Will skip bot-tools tests if no botSecret
    }

    if (!hasFlag('task-only')) {
        if (botSecret) {
            await testBotTools(deviceId, botSecret);
        } else {
            console.log('\n  ⚠ No bound entity with botSecret found — skipping Bot Tools tests');
        }
    }

    if (!hasFlag('tools-only')) {
        await testA2ATaskDispatch(deviceId, deviceSecret);
    }

    // ── Summary ─────────────────────────────────────────────
    console.log('\n═══ Summary ═══');
    const passed = results.filter(r => r.passed).length;
    const failed = results.filter(r => !r.passed).length;
    console.log(`  ${passed} passed, ${failed} failed, ${results.length} total`);

    if (failed > 0) {
        console.log('\n  Failed tests:');
        results.filter(r => !r.passed).forEach(r => {
            console.log(`    ✗ ${r.name}${r.detail ? ` — ${r.detail}` : ''}`);
        });
    }

    process.exit(failed > 0 ? 1 : 0);
}

main().catch(err => {
    console.error('Unexpected error:', err);
    process.exit(1);
});
