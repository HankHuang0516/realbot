#!/usr/bin/env node
/**
 * eclaw_context Injection Verification Test
 *
 * Verifies that speak-to and broadcast API calls correctly inject
 * eclaw_context (b2bRemaining, b2bMax, expectsReply, missionHints, silentToken)
 * into the channel push payload (v1.0.17 Channel Bot Context Parity).
 *
 * Strategy:
 *  1. Provision two channel accounts (Sender + Receiver)
 *  2. Bind entity 5 (Sender) and entity 6 (Receiver)
 *  3. Register Receiver's callback → test-sink (server captures payload)
 *  4. Sender (entity 5) speaks-to Receiver (entity 6)
 *  5. Inspect test-sink payload → assert eclaw_context fields present
 *  6. Sender broadcasts to [6] → assert eclaw_context in broadcast payload
 *  7. Cleanup
 *
 * Usage:
 *   node test-eclaw-context-injection.js
 *   node test-eclaw-context-injection.js --local
 */

const path = require('path');
const fs   = require('fs');

const args    = process.argv.slice(2);
const API_BASE = args.includes('--local') ? 'http://localhost:3000' : 'https://eclawbot.com';

const ENTITY_SENDER   = 5;
const ENTITY_RECEIVER = 6;
const POLL_MS  = 1500;
const WAIT_MS  = 15000;

// ── .env loader ─────────────────────────────────────────────────────────────
function loadEnv() {
    const p = path.resolve(__dirname, '..', '.env');
    if (!fs.existsSync(p)) return {};
    const vars = {};
    fs.readFileSync(p, 'utf8').split('\n').forEach(line => {
        line = line.trim();
        if (!line || line.startsWith('#')) return;
        const idx = line.indexOf('=');
        if (idx > 0) vars[line.slice(0, idx).trim()] = line.slice(idx + 1).trim();
    });
    return vars;
}

// ── HTTP helpers ─────────────────────────────────────────────────────────────
async function req(method, url, body) {
    const opts = { method, headers: { 'Content-Type': 'application/json' } };
    if (body) opts.body = JSON.stringify(body);
    const res  = await fetch(url, opts);
    const text = await res.text();
    let data;
    try { data = JSON.parse(text); } catch { data = { raw: text.slice(0, 300) }; }
    return { status: res.status, data };
}
const post = (url, body) => req('POST',   url, body);
const get  = (url)       => req('GET',    url);
const del  = (url, body) => req('DELETE', url, body);

// ── Test runner ───────────────────────────────────────────────────────────────
let passed = 0, failed = 0;
const failures = [];

function assert(cond, msg, extra = '') {
    if (cond) { console.log(`  ✅ ${msg}`); passed++; }
    else       { console.error(`  ❌ ${msg}${extra ? ` → ${extra}` : ''}`); failed++; failures.push(msg); }
}

// ── Poll helper ───────────────────────────────────────────────────────────────
async function poll(fn, desc) {
    const deadline = Date.now() + WAIT_MS;
    while (Date.now() < deadline) {
        const r = await fn();
        if (r) return r;
        await new Promise(x => setTimeout(x, POLL_MS));
    }
    throw new Error(`Timeout: ${desc}`);
}

// ── Main ─────────────────────────────────────────────────────────────────────
async function run() {
    const env = loadEnv();
    const DEVICE_ID     = env.BROADCAST_TEST_DEVICE_ID;
    const DEVICE_SECRET = env.BROADCAST_TEST_DEVICE_SECRET;
    if (!DEVICE_ID) { console.error('Missing BROADCAST_TEST_DEVICE_ID in .env'); process.exit(1); }

    const SINK    = `${API_BASE}/api/channel/test-sink`;
    const SLOT_RX = `eclaw-ctx-${Date.now()}`;
    const TOKEN_RX = 'ctx-token-' + Math.random().toString(36).slice(2);
    const CALLBACK_RX = `${SINK}?slot=${SLOT_RX}`;

    console.log(`\n🧪  eclaw_context Injection Test — ${API_BASE}`);
    console.log(`    Device: ${DEVICE_ID.slice(0, 8)}...`);
    console.log(`    Entities: ${ENTITY_SENDER} (Sender) → ${ENTITY_RECEIVER} (Receiver/channel)\n`);

    let senderBotSecret, receiverApiKey, receiverApiSecret, receiverAccountId;

    // ── 1. Provision Sender as channel entity ─────────────────────────────────
    console.log('── 1. Provision + bind Sender (entity 5) as channel ──');
    const provSender = await post(`${API_BASE}/api/channel/provision-device`, { deviceId: DEVICE_ID, deviceSecret: DEVICE_SECRET });
    assert(provSender.status === 200, 'Sender: provision OK');
    const sApiKey = provSender.data?.channel_api_key;
    const sApiSec = provSender.data?.channel_api_secret;

    // Register sender (dummy callback, we don't care about its webhook)
    await post(`${API_BASE}/api/channel/register`, {
        channel_api_key: sApiKey, channel_api_secret: sApiSec,
        callback_url: `${SINK}?slot=sender-dummy-${Date.now()}`,
        callback_token: 'dummy'
    });

    // Clean up entity slot first
    await del(`${API_BASE}/api/device/entity`, { deviceId: DEVICE_ID, deviceSecret: DEVICE_SECRET, entityId: ENTITY_SENDER });
    await new Promise(x => setTimeout(x, 300));

    const bindSender = await post(`${API_BASE}/api/channel/bind`, {
        channel_api_key: sApiKey, channel_api_secret: sApiSec,
        entityId: ENTITY_SENDER, name: 'TestSender'
    });
    assert(bindSender.status === 200, `Sender: bind entity ${ENTITY_SENDER} OK`, JSON.stringify(bindSender.data));
    senderBotSecret = bindSender.data?.botSecret;
    assert(typeof senderBotSecret === 'string', 'Sender: got botSecret');

    // ── 2. Provision Receiver as channel entity with test-sink callback ────────
    console.log('\n── 2. Provision + bind Receiver (entity 6) with test-sink callback ──');
    const provRx = await post(`${API_BASE}/api/channel/provision-device`, { deviceId: DEVICE_ID, deviceSecret: DEVICE_SECRET });
    assert(provRx.status === 200, 'Receiver: provision OK');
    receiverApiKey    = provRx.data?.channel_api_key;
    receiverApiSecret = provRx.data?.channel_api_secret;
    receiverAccountId = provRx.data?.id;

    const regRx = await post(`${API_BASE}/api/channel/register`, {
        channel_api_key: receiverApiKey, channel_api_secret: receiverApiSecret,
        callback_url: CALLBACK_RX, callback_token: TOKEN_RX
    });
    assert(regRx.status === 200, 'Receiver: register callback to test-sink OK');

    await del(`${API_BASE}/api/device/entity`, { deviceId: DEVICE_ID, deviceSecret: DEVICE_SECRET, entityId: ENTITY_RECEIVER });
    await new Promise(x => setTimeout(x, 300));

    const bindRx = await post(`${API_BASE}/api/channel/bind`, {
        channel_api_key: receiverApiKey, channel_api_secret: receiverApiSecret,
        entityId: ENTITY_RECEIVER, name: 'TestReceiver'
    });
    assert(bindRx.status === 200, `Receiver: bind entity ${ENTITY_RECEIVER} OK`);
    assert(bindRx.data?.botSecret !== senderBotSecret, 'Sender/Receiver have distinct botSecrets');

    // Clear sink
    await del(`${SINK}?slot=${SLOT_RX}&deviceId=${DEVICE_ID}&deviceSecret=${DEVICE_SECRET}`);

    // ── 3. Speak-to: Sender (5) → Receiver (6) ───────────────────────────────
    console.log('\n── 3. speak-to: Entity 5 → Entity 6 ──');
    const speakRes = await post(`${API_BASE}/api/entity/speak-to`, {
        deviceId:      DEVICE_ID,
        fromEntityId:  ENTITY_SENDER,
        toEntityId:    ENTITY_RECEIVER,
        botSecret:     senderBotSecret,
        text:          'Hello from Sender! Testing eclaw_context.',
        expects_reply: true
    });
    assert(speakRes.status === 200, `speak-to returns 200`, JSON.stringify(speakRes.data));

    // ── 4. Inspect test-sink payload for eclaw_context ────────────────────────
    console.log('\n── 4. Verify eclaw_context in speak-to payload ──');
    let speakPayloads;
    try {
        speakPayloads = await poll(async () => {
            const r = await get(`${SINK}?slot=${SLOT_RX}&deviceId=${DEVICE_ID}&deviceSecret=${DEVICE_SECRET}`);
            const ps = r.data?.payloads || [];
            return ps.length > 0 ? ps : null;
        }, 'test-sink receives speak-to callback');
    } catch (err) {
        assert(false, `Callback received within ${WAIT_MS}ms (${err.message})`);
        speakPayloads = [];
    }

    if (speakPayloads.length > 0) {
        const p  = speakPayloads[0];
        const pl = p.payload || {};
        const ctx = pl.eclaw_context;

        assert(pl.event === 'entity_message',     'speak-to: event=entity_message');
        assert(pl.fromEntityId === ENTITY_SENDER, 'speak-to: fromEntityId correct');
        assert(pl.text && pl.text.length > 0,     'speak-to: text present');

        // eclaw_context checks
        assert(ctx !== undefined && ctx !== null, 'speak-to: eclaw_context present in payload');
        if (ctx) {
            assert(typeof ctx.b2bRemaining === 'number',  `speak-to: b2bRemaining is number (got ${ctx.b2bRemaining})`);
            assert(typeof ctx.b2bMax       === 'number',  `speak-to: b2bMax is number (got ${ctx.b2bMax})`);
            assert(ctx.expectsReply        === true,      `speak-to: expectsReply=true`);
            assert(ctx.silentToken         === '[SILENT]',`speak-to: silentToken='[SILENT]' (got ${ctx.silentToken})`);
            assert(ctx.missionHints !== undefined,        'speak-to: missionHints field present (may be empty string)');

            console.log(`\n  📦 eclaw_context received:`);
            console.log(`     b2bRemaining: ${ctx.b2bRemaining}`);
            console.log(`     b2bMax:       ${ctx.b2bMax}`);
            console.log(`     expectsReply: ${ctx.expectsReply}`);
            console.log(`     silentToken:  ${ctx.silentToken}`);
            console.log(`     missionHints: ${JSON.stringify(ctx.missionHints).slice(0, 80)}`);
        }
    }

    // ── 5. Broadcast: Sender (5) → [Receiver (6)] ────────────────────────────
    console.log('\n── 5. broadcast: Entity 5 → [Entity 6] ──');

    // Clear sink for broadcast test
    await del(`${SINK}?slot=${SLOT_RX}&deviceId=${DEVICE_ID}&deviceSecret=${DEVICE_SECRET}`);

    const bcastRes = await post(`${API_BASE}/api/entity/broadcast`, {
        deviceId:      DEVICE_ID,
        fromEntityId:  ENTITY_SENDER,
        botSecret:     senderBotSecret,
        targetIds:     [ENTITY_RECEIVER],
        text:          'Broadcast test for eclaw_context.',
        expects_reply: false
    });
    assert(bcastRes.status === 200, `broadcast returns 200`, JSON.stringify(bcastRes.data));

    console.log('\n── 6. Verify eclaw_context in broadcast payload ──');
    let bcastPayloads;
    try {
        bcastPayloads = await poll(async () => {
            const r = await get(`${SINK}?slot=${SLOT_RX}&deviceId=${DEVICE_ID}&deviceSecret=${DEVICE_SECRET}`);
            const ps = r.data?.payloads || [];
            return ps.length > 0 ? ps : null;
        }, 'test-sink receives broadcast callback');
    } catch (err) {
        assert(false, `Broadcast callback received within ${WAIT_MS}ms (${err.message})`);
        bcastPayloads = [];
    }

    if (bcastPayloads.length > 0) {
        const p  = bcastPayloads[0];
        const pl = p.payload || {};
        const ctx = pl.eclaw_context;

        assert(pl.event === 'broadcast',              'broadcast: event=broadcast');
        assert(pl.isBroadcast === true,               'broadcast: isBroadcast=true');
        assert(ctx !== undefined && ctx !== null,     'broadcast: eclaw_context present');
        if (ctx) {
            assert(typeof ctx.b2bRemaining === 'number', `broadcast: b2bRemaining (got ${ctx.b2bRemaining})`);
            assert(ctx.expectsReply        === false,    `broadcast: expectsReply=false (got ${ctx.expectsReply})`);
            assert(ctx.silentToken         === '[SILENT]','broadcast: silentToken=[SILENT]');

            console.log(`\n  📦 eclaw_context in broadcast:`);
            console.log(`     b2bRemaining: ${ctx.b2bRemaining}`);
            console.log(`     expectsReply: ${ctx.expectsReply}`);
            console.log(`     silentToken:  ${ctx.silentToken}`);
        }
    }

    // ── Cleanup ───────────────────────────────────────────────────────────────
    console.log('\n── Cleanup ──');
    await del(`${API_BASE}/api/device/entity`, { deviceId: DEVICE_ID, deviceSecret: DEVICE_SECRET, entityId: ENTITY_SENDER });
    await del(`${API_BASE}/api/device/entity`, { deviceId: DEVICE_ID, deviceSecret: DEVICE_SECRET, entityId: ENTITY_RECEIVER });
    await del(`${API_BASE}/api/channel/register`, { channel_api_key: sApiKey,      channel_api_secret: sApiSec });
    await del(`${API_BASE}/api/channel/register`, { channel_api_key: receiverApiKey, channel_api_secret: receiverApiSecret });
    console.log('  Entities 5 & 6 unbound, channel accounts unregistered');

    // ── Summary ───────────────────────────────────────────────────────────────
    console.log('\n' + '═'.repeat(60));
    console.log(`  eclaw_context injection: ${passed} passed, ${failed} failed`);
    if (failures.length) {
        console.log('\n  Failed:');
        failures.forEach(f => console.log(`    • ${f}`));
    }
    console.log('═'.repeat(60) + '\n');
    if (failed > 0) process.exit(1);
}

run().catch(err => {
    console.error('\nTest crashed:', err.message);
    process.exit(1);
});
