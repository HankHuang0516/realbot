const BASE_URL = 'https://realbot-production.up.railway.app';
const DELAY_MS = 2000;

function sleep(ms) {
    return new Promise(resolve => setTimeout(resolve, ms));
}

async function runTest() {
    console.log(`Starting Animation Sequence on: ${BASE_URL}`);

    // 1. Initial Binding (Ensure we are connected/verified)
    try {
        await fetch(`${BASE_URL}/api/bind`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ code: "123456" })
        });
        console.log("Binding Verified.");
    } catch (e) { }

    // 2. Wave Left
    console.log("Action 1: Wave Left Claw 🦞 /");
    await sendTransform({
        character: "LOBSTER",
        state: "BUSY",
        message: "Hello! (Left)",
        parts: { "CLAW_LEFT": -45, "CLAW_RIGHT": 0 }
    });
    await sleep(DELAY_MS);

    // 3. Wave Right
    console.log("Action 2: Wave Right Claw 🦞 \\");
    await sendTransform({
        character: "LOBSTER",
        state: "BUSY",
        message: "Hi there! (Right)",
        parts: { "CLAW_LEFT": 0, "CLAW_RIGHT": 45 }
    });
    await sleep(DELAY_MS);

    // 4. BIG CLAWS (Both Open)
    console.log("Action 3: BIG CLAWS 🦞 \\_/");
    await sendTransform({
        character: "LOBSTER",
        state: "EXCITED",
        message: "I AM ALIVE!",
        parts: { "CLAW_LEFT": -60, "CLAW_RIGHT": 60 }
    });
    await sleep(DELAY_MS);

    // 5. Reset to Idle
    console.log("Action 4: Reset 🦞 | |");
    await sendTransform({
        character: "LOBSTER",
        state: "IDLE",
        message: "Waiting for command...",
        parts: { "CLAW_LEFT": 0, "CLAW_RIGHT": 0 }
    });

    console.log("Animation Sequence Complete!");
}

async function sendTransform(body) {
    try {
        const res = await fetch(`${BASE_URL}/api/transform`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(body)
        });
        // const data = await res.json();
        // console.log("Response:", data.success ? "OK" : "Error");
    } catch (e) {
        console.error("Failed:", e.message);
    }
}

runTest();
