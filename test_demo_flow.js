const BASE_URL = 'https://eclawbot.com';
const BINDING_CODE = "774471"; // NEW CODE provided by user
const ENTITY_ID = 0;

function sleep(ms) {
    return new Promise(resolve => setTimeout(resolve, ms));
}

async function runDemo() {
    console.log(`\n?? Starting Realbot Full Demo...`);
    console.log(`Target: ${BASE_URL}`);
    console.log(`Code: ${BINDING_CODE}`);

    let botSecret = null;

    // 1. Bind
    console.log("\n--- Step 1: Binding ---");
    try {
        const res = await fetch(`${BASE_URL}/api/bind`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ code: BINDING_CODE })
        });
        const data = await res.json();
        if (data.success) {
            botSecret = data.botSecret;
            console.log(`??Bound Successfully!`);
            console.log(`Secret: ${botSecret.substring(0, 6)}...`);
        } else {
            console.error(`??Binding Failed: ${data.message}`);
            process.exit(1);
        }
    } catch (e) {
        console.error("Binding Error:", e.message);
        process.exit(1);
    }

    // 2. Update Status (Dynamic Color Test)
    console.log("\n--- Step 2: Update Status (Dynamic Color) ---");
    try {
        // Bright Purple (0xFF00FF) = 16711935
        const parts = { "COLOR": 16711935, "METALLIC": 1.0, "GLOSS": 0.8 };
        const res = await fetch(`${BASE_URL}/api/transform`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                entityId: ENTITY_ID,
                botSecret: botSecret,
                character: "LOBSTER",
                state: "exCITED", // Intentionally mixed case to test robustness
                message: "Look! I'm Purple & Shiny! ????,
                parts: parts
            })
        });
        const data = await res.json();
        if (data.success) {
            console.log(`??Transform Sent: Purple Metallic Lobster`);
        } else {
            console.error(`??Transform Failed: ${data.message}`);
        }
    } catch (e) {
        console.error("Transform Error:", e.message);
    }

    await sleep(2000);

    // 3. Wake Up
    console.log("\n--- Step 3: Wake Up ---");
    try {
        const res = await fetch(`${BASE_URL}/api/wakeup`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                entityId: ENTITY_ID,
                botSecret: botSecret
            })
        });
        const data = await res.json();
        if (data.success) {
            console.log(`??Wake Up Command Sent!`);
        } else {
            console.error(`??Wake Up Failed: ${data.message}`);
        }
    } catch (e) {
        console.error("Wake Up Error:", e.message);
    }

    await sleep(2000);

    // 4. Get Status
    console.log("\n--- Step 4: Get Status ---");
    try {
        // Note: GET request usually doesn't need secret for read-only status in this API design, 
        // but let's check what the public status says.
        const res = await fetch(`${BASE_URL}/api/status?entityId=${ENTITY_ID}`);
        const data = await res.json();
        console.log(`??Current Status:`);
        console.log(`   Character: ${data.character}`);
        console.log(`   State: ${data.state}`);
        console.log(`   Message: "${data.message}"`);
        console.log(`   Parts: ${JSON.stringify(data.parts)}`);
    } catch (e) {
        console.error("Get Status Error:", e.message);
    }

    console.log("\n?? Demo Complete!");
}

runDemo();
