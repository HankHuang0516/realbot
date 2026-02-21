#!/usr/bin/env node
/**
 * HTTP Chat Client for OpenClaw Gateway
 *
 * Communicates with a Claude bot via HTTP POST to the OpenClaw
 * /tools/invoke endpoint using the sessions_send protocol.
 *
 * This is the same protocol the Eclaw backend uses to push
 * messages to bots (see backend/index.js pushToBot function).
 *
 * Usage:
 *   node tools/http-chat.js
 *   node tools/http-chat.js --url https://eclaw0.zeabur.app --token YOUR_TOKEN
 */

const readline = require('readline');

// Default config
const CONFIG = {
    baseUrl: 'https://eclaw0.zeabur.app',
    token: 'iw0QkY7pc5dmnKxE4D3FA2B68OsR9j1M',
    sessionKey: 'agent:main:main',
};

// Parse CLI arguments
const args = process.argv.slice(2);
for (let i = 0; i < args.length; i += 2) {
    if (args[i] === '--url') CONFIG.baseUrl = args[i + 1];
    if (args[i] === '--token') CONFIG.token = args[i + 1];
    if (args[i] === '--session') CONFIG.sessionKey = args[i + 1];
}

const INVOKE_URL = CONFIG.baseUrl.replace(/\/$/, '') + '/tools/invoke';

console.log('=== OpenClaw HTTP Chat Client ===');
console.log(`Endpoint: ${INVOKE_URL}`);
console.log(`Session: ${CONFIG.sessionKey}`);
console.log('');

async function invokeGateway(tool, toolArgs) {
    const payload = { tool, args: toolArgs };

    try {
        const response = await fetch(INVOKE_URL, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${CONFIG.token}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(payload),
            signal: AbortSignal.timeout(30000)
        });

        const text = await response.text();

        if (!response.ok) {
            return { ok: false, status: response.status, error: text };
        }

        try {
            return { ok: true, data: JSON.parse(text) };
        } catch {
            return { ok: true, data: text };
        }
    } catch (err) {
        if (err.name === 'TimeoutError' || err.name === 'AbortError') {
            // Timeout is expected for sessions_send - gateway accepted and AI is processing
            return { ok: true, data: '(gateway accepted - AI processing)' };
        }
        return { ok: false, error: err.message };
    }
}

async function listSessions() {
    console.log('Discovering sessions...');
    const result = await invokeGateway('sessions_list', {});

    if (!result.ok) {
        console.error(`Failed (${result.status}): ${result.error}`);
        return;
    }

    const data = result.data;
    if (typeof data === 'object' && data.result?.content) {
        const text = data.result.content[0]?.text || JSON.stringify(data.result.content);
        try {
            const sessions = JSON.parse(text);
            console.log('Active sessions:');
            if (Array.isArray(sessions)) {
                sessions.forEach((s, i) => {
                    const key = typeof s === 'string' ? s : (s.sessionKey || s.key || s.id);
                    console.log(`  ${i + 1}. ${key}`);
                });
            } else if (sessions.sessions) {
                sessions.sessions.forEach((s, i) => {
                    const key = typeof s === 'string' ? s : (s.sessionKey || s.key || s.id);
                    console.log(`  ${i + 1}. ${key}`);
                });
            } else {
                console.log(`  ${JSON.stringify(sessions, null, 2)}`);
            }
        } catch {
            console.log(`  ${text}`);
        }
    } else {
        console.log(`Response: ${JSON.stringify(data)}`);
    }
}

async function sendMessage(text) {
    console.log('Sending...');
    const result = await invokeGateway('sessions_send', {
        sessionKey: CONFIG.sessionKey,
        message: text
    });

    if (!result.ok) {
        console.error(`Send failed (${result.status}): ${result.error}`);

        if (result.status === 401) {
            console.error('Hint: Token may be invalid. Check your gateway token.');
        } else if (result.status === 404) {
            console.error('Hint: sessions_send tool may be blocked. Add it to .openclaw/openclaw.json allow list.');
        }
        return;
    }

    const data = result.data;
    if (typeof data === 'string') {
        console.log(`[Result]: ${data}`);
    } else if (data.result?.content) {
        const responseText = data.result.content[0]?.text || JSON.stringify(data.result.content);
        console.log(`[Bot]: ${responseText}`);
    } else if (data.error) {
        console.error(`[Error]: ${JSON.stringify(data.error)}`);
    } else {
        console.log(`[Response]: ${JSON.stringify(data).substring(0, 500)}`);
    }
}

// Set up interactive input
const rl = readline.createInterface({
    input: process.stdin,
    output: process.stdout,
    prompt: 'You> '
});

// Test connectivity first
async function init() {
    console.log('Testing gateway connectivity...');
    await listSessions();
    console.log('\nType a message to chat (or /help for commands):');
    rl.prompt();
}

rl.on('line', async (line) => {
    const input = line.trim();
    if (!input) {
        rl.prompt();
        return;
    }

    if (input === '/quit' || input === '/exit') {
        console.log('Goodbye!');
        process.exit(0);
    }

    if (input === '/sessions') {
        await listSessions();
        rl.prompt();
        return;
    }

    if (input.startsWith('/session ')) {
        CONFIG.sessionKey = input.substring(9).trim();
        console.log(`Session key changed to: ${CONFIG.sessionKey}`);
        rl.prompt();
        return;
    }

    if (input === '/help') {
        console.log('Commands:');
        console.log('  /sessions    - List active sessions');
        console.log('  /session KEY - Change session key');
        console.log('  /quit        - Exit');
        console.log('  /help        - Show this help');
        console.log('  (any text)   - Send message to bot');
        rl.prompt();
        return;
    }

    await sendMessage(input);
    rl.prompt();
});

rl.on('close', () => {
    process.exit(0);
});

init();
