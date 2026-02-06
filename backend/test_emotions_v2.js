const BASE_URL = 'https://realbot-production.up.railway.app';
const DELAY_EMOTION = 3000;
const DELAY_ANIM = 200;

function sleep(ms) {
    return new Promise(resolve => setTimeout(resolve, ms));
}

async function send(state, message, parts) {
    try {
        await fetch(`${BASE_URL}/api/transform`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                character: "LOBSTER",
                state: state,
                message: message,
                parts: parts
            })
        });
    } catch (e) { console.error(e.message); }
}

async function runEyeTest() {
    console.log(`👀 Starting Eye Expression Test on: ${BASE_URL}\n`);

    // 1. Blink Test
    console.log("😉 Blink Test");
    for (let i = 0; i < 3; i++) {
        await send("IDLE", "Blinking...", { "EYE_LID": 1.0 }); // Close
        await sleep(150);
        await send("IDLE", "Blinking...", { "EYE_LID": 0.0 }); // Open
        await sleep(500);
    }

    // 2. Angry (Combat)
    console.log("😡 Angry - Slanted & Half Lidded");
    await send("BUSY", "ANGRY!!!", {
        "CLAW_LEFT": -10, "CLAW_RIGHT": 10,
        "EYE_LID": 0.5,
        "EYE_ANGLE": 25.0 // Inward Slant / \
    });
    await sleep(DELAY_EMOTION);

    // 3. Sad (Crying)
    console.log("😢 Sad - Droopy Outward");
    await send("SLEEPING", "Sad...", {
        "CLAW_LEFT": 10, "CLAW_RIGHT": -10,
        "EYE_LID": 0.6,
        "EYE_ANGLE": -25.0 // Outward Slant \ /
    });
    await sleep(DELAY_EMOTION);

    // 4. Suspicious/Squint
    console.log("🤨 Suspicious - Narrow Eyes");
    await send("IDLE", "Hmm...", {
        "EYE_LID": 0.7,
        "EYE_ANGLE": 0.0
    });
    await sleep(DELAY_EMOTION);

    // 5. Surprise/Wide Awake
    console.log("😳 Surprise - Wide Open");
    await send("EXCITED", "O_O", {
        "EYE_LID": -0.2, // Extra open (if logic allows, or just 0) 
        "EYE_ANGLE": 0.0
    });
    await sleep(DELAY_EMOTION);

    // Reset
    console.log("Done.");
    await send("IDLE", "Normal", { "EYE_LID": 0.0, "EYE_ANGLE": 0.0 });
}

runEyeTest();
