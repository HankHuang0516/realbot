const BASE_URL = 'https://realbot-production.up.railway.app';
const DELAY_EMOTION = 1000;

function sleep(ms) {
    return new Promise(resolve => setTimeout(resolve, ms));
}

// Helper to interact with a specific user ID
async function sendAction(id, message, state) {
    try {
        const url = `${BASE_URL}/api/transform?id=${id}`;
        console.log(`Testing User [${id}] -> ${state}`);

        await fetch(url, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                character: "LOBSTER",
                state: state,
                message: message
            })
        });
    } catch (e) { console.error(e.message); }
}

async function getStatus(id) {
    const res = await fetch(`${BASE_URL}/api/status?id=${id}`);
    const data = await res.json();
    console.log(`User [${id}] Status: ${data.state} | Msg: ${data.message.substring(0, 15)}...`);
    return data;
}

async function runMultiTenantTest() {
    console.log(`👥 Starting Multi-Tenancy Test on: ${BASE_URL}\n`);

    const userA = "User_Alice_001";
    const userB = "User_Bob_002";

    // 1. Set Alice to ANGRY
    await sendAction(userA, "Alice is Angry", "BUSY");

    // 2. Set Bob to HAPPY
    await sendAction(userB, "Bob is Happy", "EXCITED");

    await sleep(2000); // Wait for processing

    // 3. Check STATUS - They should be different
    console.log("\n--- Verification ---");
    const statusA = await getStatus(userA);
    const statusB = await getStatus(userB);

    if (statusA.state !== statusB.state) {
        console.log("\n✅ SUCCESS: Agents are independent!");
    } else {
        console.log("\n❌ FAIL: Agents are synced (Shared State).");
    }
}

runMultiTenantTest();
