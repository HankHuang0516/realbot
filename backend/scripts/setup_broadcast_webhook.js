/**
 * Bind entity 2 of BROADCAST_TEST_DEVICE to Railway bot.
 * This gives test-broadcast.js a webhook entity for delivered_to testing.
 * Run from backend/ directory.
 */

const WebSocket = require('ws');

const WSS_URL = 'wss://clawdbot-railway-template-production-e663.up.railway.app';
const HTTPS_URL = 'https://clawdbot-railway-template-production-e663.up.railway.app/tools/invoke';
const GATEWAY_TOKEN = '1a712b828ffc1b3d3a94978b7e9805be1591b175f3ee11637840e4437e49d232';
const SETUP_USERNAME = 'admin';
const SETUP_PASSWORD = 'asasas123';

const BROADCAST_DEVICE_ID = '2a0ad04d-9107-4250-b8be-ecd565983fb2';
const BROADCAST_DEVICE_SECRET = '77c91d51-7677-4c1f-aece-fe26fd651d6d-cfff4f91-6883-4450-b17d-1ae1cf4085b4';
const ENTITY_ID = 2; // bind entity 2 as the webhook entity
const ECLAW_API = 'https://eclawbot.com';

async function wsConnect(url, username, password, gatewayToken, setupPassword) {
  return new Promise((resolve, reject) => {
    const ws = new WebSocket(url, {
      headers: {
        'Authorization': 'Basic ' + Buffer.from(username + ':' + password).toString('base64'),
        'Origin': 'https://' + new URL(url.replace('wss://', 'https://')).host
      }
    });
    let reqCounter = 0;
    const pending = new Map();

    function sendReq(method, params) {
      const id = 'probe-' + (++reqCounter);
      ws.send(JSON.stringify({ type: 'req', id, method, params }));
      return new Promise((res, rej) => {
        const t = setTimeout(() => rej(new Error('Timeout: ' + method)), 10000);
        pending.set(id, { resolve: res, reject: rej, timer: t });
      });
    }

    ws.on('open', async () => {
      try {
        await sendReq('connect', {
          minProtocol: 3, maxProtocol: 3,
          client: { id: 'openclaw-probe', version: 'dev', platform: 'node', mode: 'probe' },
          role: 'operator', scopes: ['operator.admin'],
          auth: { token: gatewayToken, password: setupPassword },
          caps: [], userAgent: 'eclaw-backend/1.0'
        });
        console.log('WS Auth OK');

        const sessRes = await sendReq('sessions.list', {});
        const sessions = sessRes.sessions || [];
        console.log('Found sessions:', sessions.map(s => s.key).join(', '));
        ws.close();
        if (sessions.length > 0) resolve(sessions[0].key);
        else reject(new Error('No sessions found on gateway'));
      } catch (e) {
        ws.close();
        reject(e);
      }
    });

    ws.on('message', (data) => {
      try {
        const msg = JSON.parse(data.toString());
        if (msg.type === 'event') return;
        if (msg.type === 'res') {
          const p = pending.get(msg.id);
          if (p) {
            clearTimeout(p.timer);
            pending.delete(msg.id);
            if (msg.ok) p.resolve(msg.payload || msg.result || {});
            else p.reject(new Error(JSON.stringify(msg.error)));
          }
        }
      } catch (e) { /* ignore */ }
    });

    ws.on('error', (e) => reject(new Error('WS Error: ' + e.message)));
    setTimeout(() => { ws.close(); reject(new Error('Overall WS timeout')); }, 20000);
  });
}

async function main() {
  console.log('=== Step 1: Register BROADCAST_TEST_DEVICE entity 2 ===');
  const regRes = await fetch(ECLAW_API + '/api/device/register', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      deviceId: BROADCAST_DEVICE_ID,
      deviceSecret: BROADCAST_DEVICE_SECRET,
      entityId: ENTITY_ID
    })
  });
  const regData = await regRes.json();
  console.log('Result:', JSON.stringify(regData));

  const bindingCode = regData.bindingCode || regData.code;
  if (!bindingCode) {
    console.error('ERROR: No binding code in response');
    process.exit(1);
  }
  console.log('Binding code:', bindingCode);

  console.log('\n=== Step 2: Connect to Railway bot via WebSocket ===');
  const sessionKey = await wsConnect(WSS_URL, SETUP_USERNAME, SETUP_PASSWORD, GATEWAY_TOKEN, SETUP_PASSWORD);
  console.log('Session key:', sessionKey);

  console.log('\n=== Step 3: Bind Railway bot to BROADCAST_TEST_DEVICE entity 2 ===');
  const bindRes = await fetch(ECLAW_API + '/api/bind', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      code: bindingCode,
      name: 'Webhook-Bot',
      webhook_url: HTTPS_URL,
      token: GATEWAY_TOKEN,
      session_key: sessionKey,
      setup_username: SETUP_USERNAME,
      setup_password: SETUP_PASSWORD
    })
  });
  const bindData = await bindRes.json();
  console.log('Bind result:', JSON.stringify(bindData).substring(0, 400));

  if (!bindData.success) {
    console.error('ERROR: Binding failed');
    process.exit(1);
  }

  console.log('\n=== SUCCESS ===');
  console.log('BROADCAST_TEST_DEVICE entity 2 is now bound to Railway bot with webhook');
  console.log('Entity 2 botSecret:', bindData.botSecret);
}

main().catch(e => {
  console.error('Fatal error:', e.message);
  process.exit(1);
});
