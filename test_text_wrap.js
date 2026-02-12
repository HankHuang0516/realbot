const BASE_URL = 'https://eclaw.up.railway.app';
const DELAY = 5000;

function sleep(ms) {
    return new Promise(resolve => setTimeout(resolve, ms));
}

async function send(message) {
    try {
        console.log(`Sending: "${message.substring(0, 50)}..."`);
        await fetch(`${BASE_URL}/api/transform`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                character: "LOBSTER",
                state: "BUSY",
                message: message  // Long message here
            })
        });
    } catch (e) {
        console.error("Error:", e.message);
    }
}

async function runTest() {
    console.log(`?? Testing Text Wrapping on: ${BASE_URL}\n`);

    // 1. Ultra Long Paragraph (Testing max bubble width and vertical growth/overflow)
    const longParagraph = "Whatever you do, don't blink. Blink and you're dead. They are fast. Faster than you can believe. Don't turn your back. Don't look away. And don't blink. Good Luck. " +
        "The universe is big. It's vast and complicated and ridiculous. And sometimes, very rarely, impossible things just happen and we call them miracles. " +
        "I'm the Doctor, and I save people. And if anyone happens to be listening, and you've got any kind of problem with that, to hell with you! " +
        "Let's make this even longer to really test the scrolling and wrapping capabilities. " +
        "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. " +
        "Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. " +
        "Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. " +
        "Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum. " +
        "Repeating for effect: The quick brown fox jumps over the lazy dog. The quick brown fox jumps over the lazy dog. " +
        "Final check for massive overflow handling!";
    console.log("SENDING: Ultra Long Paragraph...");
    await send(longParagraph);
    console.log("WAITING... Check device for layout stability.");
    await sleep(DELAY * 2); // Wait longer for reading

    // 2. Continuous Data String + Newlines (Testing hard breaks)
    const logData = "FATAL_ERROR: [System.exit]\n" +
        "at_com.example.Process(Process.java:1024)\n" +
        "at_com.example.Loop(Loop.java:55)\n" +
        "Caused by: java.lang.OutOfMemoryError: Java heap space\n" +
        "--------------------------------------------------\n" +
        "Trace: 0x44552211AA [CRITICAL]";
    console.log("SENDING: Formatted Log Data...");
    await send(logData);
    console.log("WAITING... Check device for newline handling.");
}

runTest();
