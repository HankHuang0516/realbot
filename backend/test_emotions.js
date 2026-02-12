/**
 * Emotion Showcase Test
 *
 * Tests transform animations with proper botSecret authentication.
 * Registers entity, binds to get secret, then runs emotion showcase.
 */

const BASE_URL = 'https://eclaw.up.railway.app';
const DELAY_EMOTION = 2000; // Time to hold the emotion
const DELAY_ANIM = 200;     // Fast animation tick

let botSecret = null;
let entityId = 0;

function sleep(ms) {
    return new Promise(resolve => setTimeout(resolve, ms));
}

async function api(method, path, body = null) {
    const options = {
        method,
        headers: { 'Content-Type': 'application/json' }
    };
    if (body) options.body = JSON.stringify(body);
    const res = await fetch(`${BASE_URL}${path}`, options);
    return { status: res.status, data: await res.json() };
}

async function send(state, message, left, right) {
    try {
        const res = await api('POST', '/api/transform', {
            entityId: entityId,
            botSecret: botSecret,
            character: "LOBSTER",
            state: state,
            message: message,
            parts: { "CLAW_LEFT": left, "CLAW_RIGHT": right }
        });
        if (!res.data.success) {
            console.log(`   ? ï?  Transform rejected: ${res.data.message}`);
        }
        return res.data.success;
    } catch (e) {
        console.error("Error:", e.message);
        return false;
    }
}

async function verify(expectedState, expectedMessage) {
    try {
        const res = await api('GET', `/api/status?entityId=${entityId}`);
        const data = res.data;
        const stateOk = data.state === expectedState;
        const msgOk = data.message === expectedMessage;
        if (stateOk && msgOk) {
            console.log(`   ??Verified: ${data.state} - "${data.message}"`);
        } else {
            console.log(`   ??Mismatch! Got: ${data.state} - "${data.message}"`);
        }
        return stateOk && msgOk;
    } catch (e) {
        console.log(`   ??Verify failed: ${e.message}`);
        return false;
    }
}

async function setupAuth() {
    console.log('?? Setting up authentication...\n');

    // Step 1: Register entity
    const deviceId = `emotion-test-${Date.now()}`;
    const deviceSecret = `secret-${Date.now()}`;

    const registerRes = await api('POST', '/api/device/register', {
        entityId: entityId,
        deviceId: deviceId,
        deviceSecret: deviceSecret
    });

    if (!registerRes.data.success) {
        console.log(`??Register failed: ${registerRes.data.message}`);
        return false;
    }

    console.log(`   Device registered, binding code: ${registerRes.data.bindingCode}`);

    // Step 2: Bind to get botSecret
    const bindRes = await api('POST', '/api/bind', {
        code: registerRes.data.bindingCode
    });

    if (!bindRes.data.success) {
        console.log(`??Bind failed: ${bindRes.data.message}`);
        return false;
    }

    botSecret = bindRes.data.botSecret;
    console.log(`   ??Authenticated! botSecret: ${botSecret.substring(0, 8)}...`);
    return true;
}

async function runEmotions() {
    console.log(`?Ž­ Starting Emotion Showcase on: ${BASE_URL}\n`);

    // Setup authentication first
    if (!await setupAuth()) {
        console.log('\n??Cannot run emotions without authentication');
        return;
    }

    console.log('\n?Ž¬ Running Emotion Showcase...\n');
    let passed = 0, failed = 0;

    // 1. ??(Joy) - Rapid Excited Waving
    console.log("?? ??(Joy) - Yatta!");
    const joyMsg = "So Happy! ^o^";
    await send("EXCITED", joyMsg, -60, 60); await sleep(DELAY_ANIM);
    await send("EXCITED", joyMsg, -45, 45); await sleep(DELAY_ANIM);
    await send("EXCITED", joyMsg, -70, 70); await sleep(DELAY_ANIM);
    await send("EXCITED", joyMsg, -60, 60);
    if (await verify("EXCITED", joyMsg)) passed++; else failed++;
    await sleep(DELAY_EMOTION);

    // 2. ??(Anger) - Combat Mode, Stiff
    console.log("?˜¡ ??(Anger) - Grrr...");
    const angerMsg = "DO NOT TOUCH ME!";
    await send("BUSY", angerMsg, -10, 10);
    if (await verify("BUSY", angerMsg)) passed++; else failed++;
    await sleep(DELAY_EMOTION);

    // 3. ?€ (Sorrow) - Droopy
    console.log("?˜¢ ?€ (Sorrow) - *Sob*");
    const sadMsg = "Sad Lobster... T_T";
    await send("IDLE", sadMsg, 10, -10);
    if (await verify("IDLE", sadMsg)) passed++; else failed++;
    await sleep(DELAY_EMOTION);

    // 4. æ¨?(Fun) - Dancing / Waving
    console.log("?¥³ æ¨?(Fun) - Party Time!");
    const funMsg = "Dancing~ ??;
    for (let i = 0; i < 3; i++) {
        await send("BUSY", funMsg, -45, 10); await sleep(300);
        await send("BUSY", funMsg, -10, 45); await sleep(300);
    }
    if (await verify("BUSY", funMsg)) passed++; else failed++;
    await sleep(DELAY_EMOTION);

    // 5. é©?(Surprise/Shock) - Wide Open
    console.log("?˜± é©?(Shock) - WHAT?!");
    const shockMsg = "OMFG!!";
    await send("EXCITED", shockMsg, -90, 90);
    if (await verify("EXCITED", shockMsg)) passed++; else failed++;
    await sleep(DELAY_EMOTION);

    // 6. ??(Fear) - Hiding/Protecting Face
    console.log("?˜¨ ??(Fear) - Don't eat me!");
    const fearMsg = "Hiding...";
    await send("SLEEPING", fearMsg, 90, -90);
    if (await verify("SLEEPING", fearMsg)) passed++; else failed++;
    await sleep(DELAY_EMOTION);

    // Reset
    console.log("?µ Back to Normal");
    const normalMsg = "I am calm now.";
    await send("IDLE", normalMsg, 0, 0);
    if (await verify("IDLE", normalMsg)) passed++; else failed++;

    // Summary
    console.log(`\n${'='.repeat(50)}`);
    console.log(`?? Result: ${passed}/${passed + failed} verified`);
    if (failed === 0) {
        console.log(`??All states correctly updated on server!`);
        console.log(`\n?’¡ Watch your Android wallpaper to see the animations!`);
    } else {
        console.log(`? ï?  ${failed} state(s) had mismatches`);
    }
}

runEmotions().catch(console.error);
