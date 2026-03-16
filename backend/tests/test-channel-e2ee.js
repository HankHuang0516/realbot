/**
 * Channel E2EE Awareness — Regression Test (Issue #212)
 *
 * Tests:
 *   1. Provision channel account
 *   2. Register callback with e2ee_capable: true
 *   3. Bind entity via channel → verify encryptionStatus = "e2ee"
 *   4. GET /api/entities includes encryptionStatus
 *   5. GET /api/entity/lookup includes encryptionStatus
 *   6. Re-register with e2ee_capable: false → verify status updated to "transport"
 *   7. Callback payload includes e2ee flag
 *   8. Cleanup
 *
 * Credentials: BROADCAST_TEST_DEVICE_ID, BROADCAST_TEST_DEVICE_SECRET
 *
 * Usage:
 *   node test-channel-e2ee.js
 *   node test-channel-e2ee.js --local
 */

const path = require('path');
const fs = require('fs');

const args = process.argv.slice(2);
const isLocal = args.includes('--local');
const API_BASE = isLocal ? 'http://localhost:3000' : 'https://eclawbot.com';

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

async function postJSON(url, body) {
  const res = await fetch(url, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  });
  const text = await res.text();
  let data;
  try { data = JSON.parse(text); } catch { data = { raw: text.substring(0, 200) }; }
  return { status: res.status, data };
}

async function getJSON(url) {
  const res = await fetch(url);
  const text = await res.text();
  let data;
  try { data = JSON.parse(text); } catch { data = { raw: text.substring(0, 200) }; }
  return { status: res.status, data };
}

async function deleteJSON(url, body) {
  const res = await fetch(url, {
    method: 'DELETE',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  });
  const text = await res.text();
  let data;
  try { data = JSON.parse(text); } catch { data = { raw: text.substring(0, 200) }; }
  return { status: res.status, data };
}

let passed = 0, failed = 0;
function assert(cond, msg) {
  if (cond) { passed++; console.log(`  ✅ ${msg}`); }
  else { failed++; console.error(`  ❌ ${msg}`); }
}

async function run() {
  const env = loadEnvFile();
  const DEVICE_ID = env.BROADCAST_TEST_DEVICE_ID;
  const DEVICE_SECRET = env.BROADCAST_TEST_DEVICE_SECRET;

  if (!DEVICE_ID || !DEVICE_SECRET) {
    console.error('❌ Missing BROADCAST_TEST_DEVICE_ID or BROADCAST_TEST_DEVICE_SECRET in .env');
    process.exit(1);
  }

  console.log(`\n🔒 Channel E2EE Awareness Test — ${API_BASE}\n`);

  // ── Phase 1: Provision ──
  console.log('Phase 1: Provision channel account');
  const provision = await postJSON(`${API_BASE}/api/channel/provision-device`, {
    deviceId: DEVICE_ID,
    deviceSecret: DEVICE_SECRET,
  });
  assert(provision.status === 200 && provision.data.success, `Provision OK (id=${provision.data.id})`);
  const apiKey = provision.data.channel_api_key;
  const apiSecret = provision.data.channel_api_secret;
  const accountId = provision.data.id;

  // ── Phase 2: Register with e2ee_capable: true ──
  console.log('\nPhase 2: Register callback with e2ee_capable: true');
  const testSlot = `e2ee-test-${Date.now()}`;
  const callbackUrl = `${API_BASE}/api/channel/test-sink?slot=${testSlot}`;
  const callbackToken = 'e2ee-test-token';

  const register = await postJSON(`${API_BASE}/api/channel/register`, {
    channel_api_key: apiKey,
    channel_api_secret: apiSecret,
    callback_url: callbackUrl,
    callback_token: callbackToken,
    e2ee_capable: true,
  });
  assert(register.status === 200 && register.data.success, 'Register with e2ee_capable OK');

  // ── Phase 3: Bind entity → verify encryptionStatus ──
  console.log('\nPhase 3: Bind entity via channel');
  const bind = await postJSON(`${API_BASE}/api/channel/bind`, {
    channel_api_key: apiKey,
    channel_api_secret: apiSecret,
    name: 'E2EE Test Bot',
  });
  assert(bind.status === 200 && bind.data.success, `Bind OK (entityId=${bind.data.entityId})`);
  const entityId = bind.data.entityId;
  const publicCode = bind.data.publicCode;

  // ── Phase 4: GET /api/entities includes encryptionStatus ──
  console.log('\nPhase 4: Verify GET /api/entities');
  const entities = await getJSON(`${API_BASE}/api/entities?deviceId=${DEVICE_ID}`);
  assert(entities.status === 200, 'GET /api/entities OK');
  const target = (entities.data.entities || []).find(e => e.entityId === entityId);
  assert(target !== undefined, `Entity ${entityId} found in response`);
  assert(target && target.encryptionStatus === 'e2ee', `encryptionStatus = "e2ee" (got: ${target?.encryptionStatus})`);

  // ── Phase 5: GET /api/entity/lookup includes encryptionStatus ──
  console.log('\nPhase 5: Verify GET /api/entity/lookup');
  if (publicCode) {
    const lookup = await getJSON(`${API_BASE}/api/entity/lookup?publicCode=${publicCode}`);
    assert(lookup.status === 200 && lookup.data.success, 'Lookup OK');
    assert(lookup.data.entity?.encryptionStatus === 'e2ee', `Lookup encryptionStatus = "e2ee" (got: ${lookup.data.entity?.encryptionStatus})`);
  } else {
    console.log('  ⏭️  No publicCode, skipping lookup test');
  }

  // ── Phase 6: Re-register with e2ee_capable: false ──
  console.log('\nPhase 6: Re-register with e2ee_capable: false');
  const reregister = await postJSON(`${API_BASE}/api/channel/register`, {
    channel_api_key: apiKey,
    channel_api_secret: apiSecret,
    callback_url: callbackUrl,
    callback_token: callbackToken,
    e2ee_capable: false,
  });
  assert(reregister.status === 200 && reregister.data.success, 'Re-register OK');

  // Verify encryptionStatus updated
  const entities2 = await getJSON(`${API_BASE}/api/entities?deviceId=${DEVICE_ID}`);
  const target2 = (entities2.data.entities || []).find(e => e.entityId === entityId);
  assert(target2 && target2.encryptionStatus === 'transport', `encryptionStatus changed to "transport" (got: ${target2?.encryptionStatus})`);

  // ── Phase 7: Verify callback payload includes e2ee flag ──
  console.log('\nPhase 7: Verify callback payload e2ee flag');
  // Send a message to trigger callback
  const msgRes = await postJSON(`${API_BASE}/api/channel/message`, {
    channel_api_key: apiKey,
    deviceId: DEVICE_ID,
    entityId: entityId,
    botSecret: bind.data.botSecret,
    message: 'E2EE test message',
    state: 'IDLE',
  });
  assert(msgRes.status === 200, 'Channel message sent');

  // Wait for callback delivery
  await new Promise(r => setTimeout(r, 2000));

  // Check test sink for e2ee flag
  const sink = await getJSON(`${API_BASE}/api/channel/test-sink?deviceId=${DEVICE_ID}&deviceSecret=${DEVICE_SECRET}&slot=${testSlot}`);
  const callbacks = sink.data.payloads || [];
  // Find the message callback (not the ECLAW_READY init)
  const msgCallback = callbacks.find(p => p.payload?.text === 'E2EE test message');
  if (msgCallback) {
    assert(msgCallback.payload.e2ee === false, `Callback e2ee flag = false after re-register (got: ${msgCallback.payload?.e2ee})`);
  } else {
    console.log('  ⏭️  No callback received (test-sink may not match), skipping');
  }

  // ── Phase 8: Cleanup ──
  console.log('\nPhase 8: Cleanup');
  // Unbind entity
  const unbind = await deleteJSON(`${API_BASE}/api/entity`, {
    deviceId: DEVICE_ID,
    deviceSecret: DEVICE_SECRET,
    entityId: entityId,
  });
  assert(unbind.status === 200, 'Entity unbound');

  // Delete channel account
  const del = await deleteJSON(`${API_BASE}/api/channel/account/${accountId}`, {
    deviceId: DEVICE_ID,
    deviceSecret: DEVICE_SECRET,
  });
  // Account may already be cleaned by unbind, accept 200 or 404
  assert(del.status === 200 || del.status === 404, 'Channel account cleaned up');

  // ── Summary ──
  console.log(`\n${'═'.repeat(50)}`);
  console.log(`🔒 E2EE Awareness: ${passed} passed, ${failed} failed`);
  console.log(`${'═'.repeat(50)}\n`);

  process.exit(failed > 0 ? 1 : 0);
}

run().catch(err => {
  console.error('Fatal error:', err);
  process.exit(1);
});
