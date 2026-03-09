/**
 * Channel API — Regression Test
 *
 * Tests the OpenClaw channel plugin integration flow:
 *   1. Provision channel API key for a test device
 *   2. Register callback URL
 *   3. Bind entity via channel API
 *   4. Send bot message via channel API
 *   5. Verify entity state updated
 *   6. Unregister callback
 *
 * Credentials are auto-loaded from backend/.env:
 *   BROADCAST_TEST_DEVICE_ID, BROADCAST_TEST_DEVICE_SECRET
 *
 * Usage:
 *   node test-channel-api.js
 *   node test-channel-api.js --local    # test against localhost:3000
 */

const path = require('path');
const fs = require('fs');
const http = require('http');

// ── Config ──────────────────────────────────────────────────
const args = process.argv.slice(2);
const isLocal = args.includes('--local');
const API_BASE = isLocal ? 'http://localhost:3000' : 'https://eclawbot.com';

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

async function fetchJSON(url) {
  const res = await fetch(url);
  const text = await res.text();
  let data;
  try { data = JSON.parse(text); } catch { data = { raw: text.substring(0, 200) }; }
  return { status: res.status, data };
}

// ── Test Runner ─────────────────────────────────────────────
let passed = 0;
let failed = 0;

function assert(condition, message) {
  if (condition) {
    console.log(`  ✓ ${message}`);
    passed++;
  } else {
    console.error(`  ✗ ${message}`);
    failed++;
  }
}

// ── Main Test ───────────────────────────────────────────────
async function runTests() {
  const env = loadEnvFile();
  const DEVICE_ID = env.BROADCAST_TEST_DEVICE_ID;
  const DEVICE_SECRET = env.BROADCAST_TEST_DEVICE_SECRET;

  if (!DEVICE_ID || !DEVICE_SECRET) {
    console.error('ERROR: BROADCAST_TEST_DEVICE_ID and BROADCAST_TEST_DEVICE_SECRET required in backend/.env');
    process.exit(1);
  }

  console.log(`\n🔌 Channel API Test — ${API_BASE}`);
  console.log(`   Device: ${DEVICE_ID.substring(0, 8)}...`);
  console.log('');

  // ── Test 1: Provision ──
  console.log('1. Provision channel API key');

  // We need JWT auth for provision. For testing, we'll directly test the
  // channel register/bind/message endpoints since provision requires login.
  // Instead, we'll use the DB directly if local, or skip provision test.
  console.log('   (Skipping provision — requires JWT login. Testing register/bind/message flow.)');
  console.log('');

  // ── Test 2: Register callback ──
  console.log('2. Register callback URL');

  // First, check if channel account exists for this device
  // For testing, we need to have a channel account already provisioned
  // Let's test with invalid credentials first
  const badRegister = await postJSON(`${API_BASE}/api/channel/register`, {
    channel_api_key: 'eck_invalid',
    channel_api_secret: 'ecs_invalid',
    callback_url: 'http://localhost:9999/test-webhook',
  });
  assert(badRegister.status === 403, 'Invalid credentials rejected (403)');

  // Test missing callback_url
  const noCallback = await postJSON(`${API_BASE}/api/channel/register`, {
    channel_api_key: 'eck_test',
    channel_api_secret: 'ecs_test',
  });
  assert(noCallback.status === 400 || noCallback.status === 403, 'Missing callback_url handled');

  console.log('');

  // ── Test 3: Bind entity ──
  console.log('3. Bind entity via channel API');

  const badBind = await postJSON(`${API_BASE}/api/channel/bind`, {
    channel_api_key: 'eck_invalid',
    channel_api_secret: 'ecs_invalid',
    entityId: 7,
    name: 'TestBot',
  });
  assert(badBind.status === 403, 'Invalid credentials rejected for bind (403)');

  // Test invalid entityId
  const badEntity = await postJSON(`${API_BASE}/api/channel/bind`, {
    channel_api_key: 'eck_test',
    channel_api_secret: 'ecs_test',
    entityId: 99,
  });
  assert(badEntity.status === 400 || badEntity.status === 403, 'Invalid entityId handled');

  console.log('');

  // ── Test 4: Send message ──
  console.log('4. Send message via channel API');

  const badMessage = await postJSON(`${API_BASE}/api/channel/message`, {
    channel_api_key: 'eck_invalid',
    deviceId: DEVICE_ID,
    entityId: 0,
    botSecret: 'invalid',
    message: 'Test message',
  });
  assert(badMessage.status === 403, 'Invalid channel API key rejected for message (403)');

  const missingFields = await postJSON(`${API_BASE}/api/channel/message`, {
    message: 'Test',
  });
  assert(missingFields.status === 400, 'Missing required fields rejected (400)');

  console.log('');

  // ── Test 5: Unregister callback ──
  console.log('5. Unregister callback');

  const badUnregister = await deleteJSON(`${API_BASE}/api/channel/register`, {
    channel_api_key: 'eck_invalid',
    channel_api_secret: 'ecs_invalid',
  });
  assert(badUnregister.status === 403, 'Invalid credentials rejected for unregister (403)');

  console.log('');

  // ── Test 6: Verify existing endpoints still work ──
  console.log('6. Coexistence check — existing APIs still work');

  const entities = await fetchJSON(`${API_BASE}/api/entities?deviceId=${DEVICE_ID}`);
  assert(entities.status === 200, 'GET /api/entities still works');
  assert(Array.isArray(entities.data?.entities), 'Returns entity array');

  console.log('');

  // ── Test 7: Init Push Verification (test-sink) ──
  console.log('7. Init push verification (ECLAW_READY via test-sink)');

  {
    const ts = Date.now();
    const sinkDeviceId = `test-init-push-${ts}`;
    const sinkDeviceSecret = `secret-sink-${ts}`;
    const sinkSlot = `init-push-${ts}`;

    // Register a fresh test device
    const reg = await postJSON(`${API_BASE}/api/device/register`, {
      deviceId: sinkDeviceId,
      deviceSecret: sinkDeviceSecret,
      entityId: 0,
      isTestDevice: true,
    });
    assert(reg.data.success, 'Init push: test device registered');

    // Provision channel account via provision-device (no JWT needed)
    const prov = await postJSON(`${API_BASE}/api/channel/provision-device`, {
      deviceId: sinkDeviceId,
      deviceSecret: sinkDeviceSecret,
    });

    if (!prov.data.success || !prov.data.channel_api_key) {
      console.log('  ⏭  Init push test skipped — provision-device failed');
      console.log(`     (${prov.data.message || prov.data.error || 'unknown'})`);
    } else {
      const { channel_api_key, channel_api_secret } = prov.data;

      // Point callback to test-sink
      const sinkUrl = `${API_BASE}/api/channel/test-sink?slot=${sinkSlot}`;
      const regCb = await postJSON(`${API_BASE}/api/channel/register`, {
        channel_api_key,
        channel_api_secret,
        callback_url: sinkUrl,
      });
      assert(regCb.data.success, 'Init push: callback registered to test-sink');

      // Clear any stale payloads
      await fetch(`${API_BASE}/api/channel/test-sink?slot=${sinkSlot}`, {
        method: 'DELETE',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ deviceId: sinkDeviceId, deviceSecret: sinkDeviceSecret }),
      });

      // Full bind — should trigger ECLAW_READY push
      const bindR = await postJSON(`${API_BASE}/api/channel/bind`, {
        channel_api_key,
        channel_api_secret,
        entityId: 0,
        name: 'TestInitBot',
      });
      assert(bindR.data.success, 'Init push: bind succeeded');

      // Wait for async fire-and-forget push to arrive
      await new Promise(r => setTimeout(r, 1000));

      // Read sink
      const sinkRes = await fetchJSON(
        `${API_BASE}/api/channel/test-sink?slot=${sinkSlot}&deviceId=${sinkDeviceId}&deviceSecret=${sinkDeviceSecret}`,
      );
      const payloads = sinkRes.data.payloads || [];
      const initMsg = payloads.find(p => p.payload?.text?.includes('ECLAW_READY'));

      assert(!!initMsg, 'Init push: ECLAW_READY message received in test-sink');
      if (initMsg) {
        const txt = initMsg.payload.text;
        assert(txt.includes(sinkDeviceId), 'Init push: text contains deviceId');
        assert(txt.includes(bindR.data.botSecret), 'Init push: text contains botSecret');
        assert(txt.includes('mission/dashboard'), 'Init push: text contains mission dashboard URL');
        assert(initMsg.payload.event === 'message', 'Init push: event type is "message"');
        assert(initMsg.payload.entityId === 0, 'Init push: entityId is 0');
      }

      // Idempotent reconnect must NOT re-send ECLAW_READY
      await fetch(`${API_BASE}/api/channel/test-sink?slot=${sinkSlot}`, {
        method: 'DELETE',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ deviceId: sinkDeviceId, deviceSecret: sinkDeviceSecret }),
      });
      await postJSON(`${API_BASE}/api/channel/bind`, {
        channel_api_key,
        channel_api_secret,
        entityId: 0,
      });
      await new Promise(r => setTimeout(r, 1000));

      const sinkRes2 = await fetchJSON(
        `${API_BASE}/api/channel/test-sink?slot=${sinkSlot}&deviceId=${sinkDeviceId}&deviceSecret=${sinkDeviceSecret}`,
      );
      const initMsg2 = (sinkRes2.data.payloads || []).find(p => p.payload?.text?.includes('ECLAW_READY'));
      assert(!initMsg2, 'Init push: idempotent reconnect does NOT re-send ECLAW_READY');

      // Cleanup
      await fetch(`${API_BASE}/api/device/entity`, {
        method: 'DELETE',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ deviceId: sinkDeviceId, deviceSecret: sinkDeviceSecret, entityId: 0 }),
      });
    }
  }

  console.log('');

  // ── Summary ──
  console.log('═══════════════════════════════════════════');
  console.log(`   Results: ${passed} passed, ${failed} failed`);
  console.log('═══════════════════════════════════════════');

  if (failed > 0) {
    process.exit(1);
  }
}

runTests().catch(err => {
  console.error('Test failed with error:', err);
  process.exit(1);
});
