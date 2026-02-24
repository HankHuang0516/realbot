const BASE_URL = 'https://eclawbot.com';
const ENTITY_ID = 0; // Target Entity 0 (Main)

function sleep(ms) {
    return new Promise(resolve => setTimeout(resolve, ms));
}

const BINDING_CODE = "700039"; // Provided by user
let botSecret = null;

async function bindBot() {
    try {
        console.log(`[Binding] Using code: ${BINDING_CODE}`);
        const res = await fetch(`${BASE_URL}/api/bind`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ code: BINDING_CODE })
        });
        const data = await res.json();
        if (data.success) {
            botSecret = data.botSecret;
            console.log(`[Bound] Success! Secret: ${botSecret.substring(0, 6)}...`);
        } else {
            console.error(`[Binding Failed] ${data.message}`);
            process.exit(1);
        }
    } catch (e) {
        console.error("[Binding Error]", e.message);
        process.exit(1);
    }
}

async function sendTransform(state, message, parts) {
    if (!botSecret) {
        console.error("No botSecret available!");
        return;
    }
    try {
        console.log(`[Sent] State: ${state}, Parts: ${JSON.stringify(parts)}`);
        const res = await fetch(`${BASE_URL}/api/transform`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                entityId: ENTITY_ID,
                botSecret: botSecret, // Include authentication
                character: "LOBSTER",
                state: state,
                message: message,
                parts: parts
            })
        });
        if (!res.ok) console.error(`Error: ${res.status} ${res.statusText}`);
    } catch (e) {
        console.error("Network Error:", e.message);
    }
}

async function runAnimationTest() {
    console.log(`\n?? Starting Limb Animation Test on Entity ${ENTITY_ID}...`);
    console.log(`URL: ${BASE_URL}`);

    // authenticate first
    await bindBot();

    // Sequence 1: Wave Left Claw
    console.log("\n--- Sequence 1: Waving Left Claw ---");
    for (let i = 0; i < 3; i++) {
        await sendTransform("EXCITED", "Hi! (Left Wave)", { CLAW_LEFT: 45, CLAW_RIGHT: 0 });
        await sleep(500);
        await sendTransform("EXCITED", "Hi! (Left Wave)", { CLAW_LEFT: 0, CLAW_RIGHT: 0 });
        await sleep(500);
    }

    // Sequence 2: Wave Right Claw
    console.log("\n--- Sequence 2: Waving Right Claw ---");
    for (let i = 0; i < 3; i++) {
        await sendTransform("EXCITED", "Right Wave!", { CLAW_LEFT: 0, CLAW_RIGHT: -45 });
        await sleep(500);
        await sendTransform("EXCITED", "Right Wave!", { CLAW_LEFT: 0, CLAW_RIGHT: 0 });
        await sleep(500);
    }

    // Sequence 3: Both Claws Up (Cheer)
    console.log("\n--- Sequence 3: Cheer (Both Up) ---");
    await sendTransform("EXCITED", "YAY!!!", { CLAW_LEFT: 60, CLAW_RIGHT: -60 });
    await sleep(2000);

    // Reset
    console.log("\n--- Resetting to Idle ---");
    await sendTransform("IDLE", "Ready.", { CLAW_LEFT: 0, CLAW_RIGHT: 0 });

    console.log("\n??Animation Test Complete!");
}

runAnimationTest();
