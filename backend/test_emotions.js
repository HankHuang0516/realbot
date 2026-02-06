const BASE_URL = 'https://realbot-production.up.railway.app';
const DELAY_EMOTION = 3000; // Time to hold the emotion
const DELAY_ANIM = 300;     // Fast animation tick

function sleep(ms) {
    return new Promise(resolve => setTimeout(resolve, ms));
}

async function send(state, message, left, right) {
    try {
        await fetch(`${BASE_URL}/api/transform`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                character: "LOBSTER",
                state: state,
                message: message,
                parts: { "CLAW_LEFT": left, "CLAW_RIGHT": right }
            })
        });
    } catch (e) {
        console.error("Error:", e.message);
    }
}

async function runEmotions() {
    console.log(`🎭 Starting Emotion Showcase on: ${BASE_URL}\n`);

    // 1. 喜 (Joy) - Rapid Excited Waving
    console.log("😊 喜 (Joy) - Yatta!");
    const joyMsg = "So Happy! ^o^";
    await send("EXCITED", joyMsg, -60, 60); await sleep(DELAY_ANIM);
    await send("EXCITED", joyMsg, -45, 45); await sleep(DELAY_ANIM);
    await send("EXCITED", joyMsg, -70, 70); await sleep(DELAY_ANIM);
    await send("EXCITED", joyMsg, -60, 60);
    await sleep(DELAY_EMOTION);

    // 2. 怒 (Anger) - Combat Mode, Stiff
    console.log("😡 怒 (Anger) - Grrr...");
    const angerMsg = "DO NOT TOUCH ME!";
    await send("BUSY", angerMsg, -10, 10); // Horizontal stiffness
    await sleep(DELAY_EMOTION);

    // 3. 哀 (Sorrow) - Droopy
    console.log("😢 哀 (Sorrow) - *Sob*");
    await send("IDLE", "Sad Lobster... T_T", 10, -10); // Drooping down low
    await sleep(DELAY_EMOTION);

    // 4. 樂 (Fun) - Dancing / Waving
    console.log("🥳 樂 (Fun) - Party Time!");
    const funMsg = "Dancing~ ♪";
    for (let i = 0; i < 3; i++) {
        await send("BUSY", funMsg, -45, 10); await sleep(400); // Leaning Left
        await send("BUSY", funMsg, -10, 45); await sleep(400); // Leaning Right
    }
    await sleep(DELAY_EMOTION);

    // 5. 驚 (Surprise/Shock) - Wide Open
    console.log("😱 驚 (Shock) - WHAT?!");
    await send("EXCITED", "OMFG!!", -90, 90); // Maximum wide spread
    await sleep(DELAY_EMOTION);

    // 6. 恐 (Fear) - Hiding/Protecting Face
    console.log("😨 恐 (Fear) - Don't eat me!");
    await send("SLEEPING", "Hiding...", 90, -90); // Crossed inwards/downwards
    await sleep(DELAY_EMOTION);

    // Reset
    console.log("🍵 Back to Normal");
    await send("IDLE", "I am calm now.", 0, 0);
}

runEmotions();
