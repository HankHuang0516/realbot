#!/usr/bin/env node
/**
 * WebSocket Chat Client for OpenClaw Gateway
 *
 * Connects to a Claude bot via the OpenClaw WebSocket gateway
 * and enables interactive conversation.
 *
 * Usage:
 *   node tools/ws-chat.js
 *   node tools/ws-chat.js --url wss://eclaw0.zeabur.app --token YOUR_TOKEN
 */

const WebSocket = require('ws');
const readline = require('readline');

// Default config - can be overridden via CLI args
const CONFIG = {
    wsUrl: 'wss://eclaw0.zeabur.app',
    token: 'iw0QkY7pc5dmnKxE4D3FA2B68OsR9j1M',
    sessionKey: 'agent:main:main',
};

// Parse CLI arguments
const args = process.argv.slice(2);
for (let i = 0; i < args.length; i += 2) {
    if (args[i] === '--url') CONFIG.wsUrl = args[i + 1];
    if (args[i] === '--token') CONFIG.token = args[i + 1];
    if (args[i] === '--session') CONFIG.sessionKey = args[i + 1];
}

console.log('=== OpenClaw WebSocket Chat Client ===');
console.log(`URL: ${CONFIG.wsUrl}`);
console.log(`Session: ${CONFIG.sessionKey}`);
console.log('');

let ws;
let reconnectAttempts = 0;
const MAX_RECONNECT = 3;

function connect() {
    console.log('Connecting...');

    ws = new WebSocket(CONFIG.wsUrl, {
        headers: {
            'Authorization': `Bearer ${CONFIG.token}`
        }
    });

    ws.on('open', () => {
        console.log('Connected to gateway!');
        reconnectAttempts = 0;

        // Discover active sessions first
        console.log('Discovering active sessions...');
        ws.send(JSON.stringify({
            tool: 'sessions_list',
            args: {}
        }));
    });

    ws.on('message', (data) => {
        const msg = data.toString();
        try {
            const parsed = JSON.parse(msg);
            if (parsed.result?.content) {
                // sessions_list response or sessions_send response
                const text = parsed.result.content[0]?.text || JSON.stringify(parsed.result.content);
                console.log(`\n[Bot]: ${text}`);
            } else if (parsed.tool === 'sessions_send') {
                // Incoming push message from bot
                console.log(`\n[Bot Push]: ${parsed.args?.message || JSON.stringify(parsed)}`);
            } else if (parsed.error) {
                console.error(`\n[Error]: ${parsed.error}`);
            } else {
                console.log(`\n[Response]: ${msg.substring(0, 500)}`);
            }
        } catch {
            console.log(`\n[Raw]: ${msg.substring(0, 500)}`);
        }
        rl.prompt();
    });

    ws.on('error', (err) => {
        console.error(`Connection error: ${err.message}`);
    });

    ws.on('close', (code, reason) => {
        console.log(`Disconnected (${code}): ${reason.toString()}`);
        if (reconnectAttempts < MAX_RECONNECT) {
            reconnectAttempts++;
            const delay = Math.pow(2, reconnectAttempts) * 1000;
            console.log(`Reconnecting in ${delay / 1000}s... (attempt ${reconnectAttempts}/${MAX_RECONNECT})`);
            setTimeout(connect, delay);
        } else {
            console.log('Max reconnection attempts reached. Exiting.');
            process.exit(1);
        }
    });
}

// Set up interactive input
const rl = readline.createInterface({
    input: process.stdin,
    output: process.stdout,
    prompt: 'You> '
});

function sendMessage(text) {
    if (!ws || ws.readyState !== WebSocket.OPEN) {
        console.log('Not connected. Waiting for connection...');
        return;
    }

    const payload = {
        tool: 'sessions_send',
        args: {
            sessionKey: CONFIG.sessionKey,
            message: text
        }
    };

    ws.send(JSON.stringify(payload));
    console.log('(message sent)');
}

rl.on('line', (line) => {
    const input = line.trim();
    if (!input) {
        rl.prompt();
        return;
    }

    if (input === '/quit' || input === '/exit') {
        console.log('Goodbye!');
        if (ws) ws.close();
        process.exit(0);
    }

    if (input === '/sessions') {
        ws.send(JSON.stringify({ tool: 'sessions_list', args: {} }));
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

    sendMessage(input);
    rl.prompt();
});

rl.on('close', () => {
    if (ws) ws.close();
    process.exit(0);
});

// Start connection
connect();

// Show prompt after a short delay to let connection establish
setTimeout(() => {
    console.log('\nType a message to chat (or /help for commands):');
    rl.prompt();
}, 2000);
