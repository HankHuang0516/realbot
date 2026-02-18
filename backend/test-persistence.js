// ============================================
// Data Persistence Test - Bug #2 Validation
// Tests if data survives server restart (simulating Railway redeployment)
// ============================================

const API_BASE = process.env.API_BASE || 'http://localhost:3000';

const colors = {
    reset: '\x1b[0m',
    red: '\x1b[31m',
    green: '\x1b[32m',
    yellow: '\x1b[33m',
    cyan: '\x1b[36m'
};

function log(message, color = 'reset') {
    console.log(`${colors[color]}${message}${colors.reset}`);
}

async function request(endpoint, options = {}) {
    const url = `${API_BASE}${endpoint}`;
    try {
        const response = await fetch(url, {
            method: options.method || 'GET',
            headers: {
                'Content-Type': 'application/json',
                ...options.headers
            },
            body: options.body ? JSON.stringify(options.body) : undefined
        });

        const data = await response.json().catch(() => ({}));
        return { status: response.status, data, ok: response.ok };
    } catch (err) {
        return { status: 0, error: err.message, ok: false };
    }
}

function randomString(length = 10) {
    const chars = 'abcdefghijklmnopqrstuvwxyz0123456789';
    let result = '';
    for (let i = 0; i < length; i++) {
        result += chars.charAt(Math.floor(Math.random() * chars.length));
    }
    return result;
}

async function setupTestEntity() {
    log('\n[Setup] Creating test entity for persistence test', 'cyan');

    const deviceId = `persist-test-${randomString(8)}`;
    const deviceSecret = randomString(32);
    const entityId = 0;

    // Register device
    const regRes = await request('/api/device/register', {
        method: 'POST',
        body: { deviceId, deviceSecret, entityId, appVersion: '1.0.3', isTestDevice: true }
    });

    if (!regRes.ok) {
        log('✗ Failed to register device', 'red');
        return null;
    }

    const bindingCode = regRes.data.bindingCode;
    log(`Binding code: ${bindingCode}`, 'yellow');

    // Bind entity
    const bindRes = await request('/api/bind', {
        method: 'POST',
        body: { code: bindingCode, name: 'PersistenceTest' }
    });

    if (!bindRes.ok) {
        log('✗ Failed to bind entity', 'red');
        return null;
    }

    const botSecret = bindRes.data.botSecret;
    log(`✓ Test entity created: ${deviceId} / Entity ${entityId}`, 'green');
    log(`Bot Secret: ${botSecret.substring(0, 8)}...`, 'yellow');

    return { deviceId, deviceSecret, entityId, botSecret };
}

async function checkEntityExists(entity) {
    log('\n[Check] Verifying entity exists', 'cyan');

    const res = await request(`/api/status?deviceId=${entity.deviceId}&entityId=${entity.entityId}`);

    if (res.ok && res.data.isBound) {
        log(`✓ Entity found and bound`, 'green');
        log(`  Name: ${res.data.name}`, 'yellow');
        log(`  Character: ${res.data.character}`, 'yellow');
        log(`  State: ${res.data.state}`, 'yellow');
        return true;
    } else {
        log('✗ Entity not found or not bound', 'red');
        return false;
    }
}

async function saveEntityInfo(entity) {
    const fs = require('fs');
    const filePath = './test-entity-info.json';
    fs.writeFileSync(filePath, JSON.stringify(entity, null, 2), 'utf8');
    log(`\n✓ Entity info saved to ${filePath}`, 'green');
    log('\nIMPORTANT: Now restart the server and run this script with --check flag', 'yellow');
    log('Example: node test-persistence.js --check', 'yellow');
}

async function loadEntityInfo() {
    const fs = require('fs');
    const filePath = './test-entity-info.json';
    if (!fs.existsSync(filePath)) {
        log('✗ No saved entity info found. Run without --check flag first.', 'red');
        return null;
    }
    const data = JSON.parse(fs.readFileSync(filePath, 'utf8'));
    log('✓ Entity info loaded', 'green');
    return data;
}

async function runSetupMode() {
    log('\n╔════════════════════════════════════════════════════════════╗', 'cyan');
    log('║        Data Persistence Test - SETUP MODE                ║', 'cyan');
    log('╚════════════════════════════════════════════════════════════╝', 'cyan');

    const entity = await setupTestEntity();
    if (!entity) {
        log('\n✗ Setup failed', 'red');
        process.exit(1);
    }

    const exists = await checkEntityExists(entity);
    if (!exists) {
        log('\n✗ Entity not found after creation', 'red');
        process.exit(1);
    }

    await saveEntityInfo(entity);
}

async function runCheckMode() {
    log('\n╔════════════════════════════════════════════════════════════╗', 'cyan');
    log('║        Data Persistence Test - CHECK MODE                ║', 'cyan');
    log('╚════════════════════════════════════════════════════════════╝', 'cyan');

    const entity = await loadEntityInfo();
    if (!entity) {
        process.exit(1);
    }

    log(`\nChecking for entity: ${entity.deviceId} / Entity ${entity.entityId}`, 'yellow');

    const exists = await checkEntityExists(entity);

    if (exists) {
        log('\n✓ SUCCESS! Entity survived server restart', 'green');
        log('Bug #2 (Data Persistence) is FIXED! 🎉', 'green');
        process.exit(0);
    } else {
        log('\n✗ FAILED! Entity lost after server restart', 'red');
        log('Bug #2 (Data Persistence) still exists', 'red');
        process.exit(1);
    }
}

// Main
async function main() {
    const args = process.argv.slice(2);
    const checkMode = args.includes('--check') || args.includes('-c');

    if (checkMode) {
        await runCheckMode();
    } else {
        await runSetupMode();
    }
}

main().catch(err => {
    log(`\n✗ Test crashed: ${err.message}`, 'red');
    console.error(err);
    process.exit(1);
});
