// ============================================
// Stress Test for Claw Live Wallpaper Backend
// Tests Bug #1 (malformed requests) and Bug #2 (data persistence)
// ============================================

const API_BASE = process.env.API_BASE || 'http://localhost:3000';
const VERBOSE = process.env.VERBOSE === 'true';

// Color output for terminal
const colors = {
    reset: '\x1b[0m',
    red: '\x1b[31m',
    green: '\x1b[32m',
    yellow: '\x1b[33m',
    blue: '\x1b[34m',
    magenta: '\x1b[35m',
    cyan: '\x1b[36m'
};

function log(message, color = 'reset') {
    console.log(`${colors[color]}${message}${colors.reset}`);
}

// Test results tracking
const results = {
    total: 0,
    passed: 0,
    failed: 0,
    errors: []
};

// Helper: Make HTTP request
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

// Helper: Generate random string
function randomString(length = 10) {
    const chars = 'abcdefghijklmnopqrstuvwxyz0123456789';
    let result = '';
    for (let i = 0; i < length; i++) {
        result += chars.charAt(Math.floor(Math.random() * chars.length));
    }
    return result;
}

// Helper: Random number
function randomInt(min, max) {
    return Math.floor(Math.random() * (max - min + 1)) + min;
}

// Test: Health check (baseline)
async function testHealthCheck() {
    log('\n[Test] Health Check', 'cyan');
    const res = await request('/api/health');
    results.total++;

    if (res.ok && res.data.status === 'ok') {
        log('✓ Health check passed', 'green');
        results.passed++;
        return true;
    } else {
        log('✗ Health check failed', 'red');
        results.failed++;
        results.errors.push('Health check failed');
        return false;
    }
}

// Test: Register device and bind entity (setup)
async function setupTestEntity() {
    log('\n[Setup] Creating test entity', 'cyan');

    const deviceId = `test-device-${randomString(8)}`;
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

    // Bind entity
    const bindRes = await request('/api/bind', {
        method: 'POST',
        body: { code: bindingCode, name: 'StressTest' }
    });

    if (!bindRes.ok) {
        log('✗ Failed to bind entity', 'red');
        return null;
    }

    const botSecret = bindRes.data.botSecret;
    log(`✓ Test entity created: ${deviceId} / Entity ${entityId}`, 'green');

    return { deviceId, deviceSecret, entityId, botSecret };
}

// Test: Bug #1 - Malformed entity-to-entity speak requests
async function testMalformedEntitySpeak(entity) {
    log('\n[Bug #1 Test] Malformed entity-to-entity speak requests', 'cyan');

    const malformedRequests = [
        // Missing fromEntityId
        { deviceId: entity.deviceId, toEntityId: 1, botSecret: entity.botSecret, text: 'test' },
        // Missing toEntityId
        { deviceId: entity.deviceId, fromEntityId: entity.entityId, botSecret: entity.botSecret, text: 'test' },
        // Wrong field names (the original bug)
        { deviceId: entity.deviceId, from: entity.entityId, to: 1, botSecret: entity.botSecret, text: 'test' },
        // Invalid fromEntityId (string instead of number)
        { deviceId: entity.deviceId, fromEntityId: 'invalid', toEntityId: 1, botSecret: entity.botSecret, text: 'test' },
        // Out of range entityId
        { deviceId: entity.deviceId, fromEntityId: 999, toEntityId: 1, botSecret: entity.botSecret, text: 'test' },
        // Negative entityId
        { deviceId: entity.deviceId, fromEntityId: -1, toEntityId: 1, botSecret: entity.botSecret, text: 'test' },
        // NaN entityId
        { deviceId: entity.deviceId, fromEntityId: NaN, toEntityId: 1, botSecret: entity.botSecret, text: 'test' },
        // Null entityId
        { deviceId: entity.deviceId, fromEntityId: null, toEntityId: 1, botSecret: entity.botSecret, text: 'test' },
        // Undefined entityId (omitted)
        { deviceId: entity.deviceId, botSecret: entity.botSecret, text: 'test' }
    ];

    let passed = 0;
    let failed = 0;

    for (let i = 0; i < malformedRequests.length; i++) {
        const req = malformedRequests[i];
        results.total++;

        if (VERBOSE) {
            log(`  Request ${i + 1}/${malformedRequests.length}: ${JSON.stringify(req)}`, 'yellow');
        }

        const res = await request('/api/entity/speak-to', {
            method: 'POST',
            body: req
        });

        // Server should return 400/403/404, NOT crash (status 0 or 500)
        if (res.status === 0 || res.status === 500) {
            log(`  ✗ Request ${i + 1} caused server crash or 500 error`, 'red');
            results.failed++;
            failed++;
            results.errors.push(`Malformed entity speak request ${i + 1} caused crash`);
        } else if (res.status >= 400 && res.status < 500) {
            if (VERBOSE) {
                log(`  ✓ Request ${i + 1} handled correctly (${res.status})`, 'green');
            }
            results.passed++;
            passed++;
        } else {
            log(`  ⚠ Request ${i + 1} unexpected status: ${res.status}`, 'yellow');
            results.passed++;
            passed++;
        }
    }

    log(`\nMalformed requests: ${passed} handled correctly, ${failed} caused issues`, passed === malformedRequests.length ? 'green' : 'red');
}

// Test: Bug #1 - Malformed transform requests
async function testMalformedTransform(entity) {
    log('\n[Bug #1 Test] Malformed transform requests', 'cyan');

    const malformedRequests = [
        // Missing entityId
        { deviceId: entity.deviceId, botSecret: entity.botSecret, state: 'IDLE' },
        // Invalid entityId
        { deviceId: entity.deviceId, entityId: 'invalid', botSecret: entity.botSecret, state: 'IDLE' },
        // Out of range entityId
        { deviceId: entity.deviceId, entityId: 999, botSecret: entity.botSecret, state: 'IDLE' },
        // Null entityId
        { deviceId: entity.deviceId, entityId: null, botSecret: entity.botSecret, state: 'IDLE' },
        // Missing deviceId
        { entityId: entity.entityId, botSecret: entity.botSecret, state: 'IDLE' },
        // Wrong deviceId
        { deviceId: 'wrong-device', entityId: entity.entityId, botSecret: entity.botSecret, state: 'IDLE' },
        // Missing botSecret
        { deviceId: entity.deviceId, entityId: entity.entityId, state: 'IDLE' },
        // Wrong botSecret
        { deviceId: entity.deviceId, entityId: entity.entityId, botSecret: 'wrong-secret', state: 'IDLE' }
    ];

    let passed = 0;
    let failed = 0;

    for (let i = 0; i < malformedRequests.length; i++) {
        const req = malformedRequests[i];
        results.total++;

        if (VERBOSE) {
            log(`  Request ${i + 1}/${malformedRequests.length}: ${JSON.stringify(req)}`, 'yellow');
        }

        const res = await request('/api/transform', {
            method: 'POST',
            body: req
        });

        // Server should return 400/403/404, NOT crash
        if (res.status === 0 || res.status === 500) {
            log(`  ✗ Request ${i + 1} caused server crash or 500 error`, 'red');
            results.failed++;
            failed++;
            results.errors.push(`Malformed transform request ${i + 1} caused crash`);
        } else if (res.status >= 400 && res.status < 500) {
            if (VERBOSE) {
                log(`  ✓ Request ${i + 1} handled correctly (${res.status})`, 'green');
            }
            results.passed++;
            passed++;
        } else {
            log(`  ⚠ Request ${i + 1} unexpected status: ${res.status}`, 'yellow');
            results.passed++;
            passed++;
        }
    }

    log(`\nMalformed requests: ${passed} handled correctly, ${failed} caused issues`, passed === malformedRequests.length ? 'green' : 'red');
}

// Test: Bug #1 - Random garbage requests (stress test)
async function testRandomGarbageRequests() {
    log('\n[Bug #1 Test] Random garbage requests (stress test)', 'cyan');

    const endpoints = [
        { path: '/api/transform', method: 'POST' },
        { path: '/api/entity/speak-to', method: 'POST' },
        { path: '/api/entity/broadcast', method: 'POST' },
        { path: '/api/client/speak', method: 'POST' }
    ];

    const requestCount = 50;
    let passed = 0;
    let failed = 0;

    for (let i = 0; i < requestCount; i++) {
        results.total++;

        // Generate random garbage data
        const garbageData = {};
        const fieldCount = randomInt(0, 10);
        for (let j = 0; j < fieldCount; j++) {
            const key = randomString(randomInt(3, 15));
            const valueType = randomInt(0, 5);
            let value;
            switch (valueType) {
                case 0: value = randomString(randomInt(0, 50)); break;
                case 1: value = randomInt(-1000, 1000); break;
                case 2: value = Math.random() > 0.5; break;
                case 3: value = null; break;
                case 4: value = undefined; break;
                case 5: value = { nested: randomString(5) }; break;
            }
            garbageData[key] = value;
        }

        const endpoint = endpoints[randomInt(0, endpoints.length - 1)];
        const res = await request(endpoint.path, {
            method: endpoint.method,
            body: garbageData
        });

        // Server should NOT crash (status 0 or 500)
        if (res.status === 0 || res.status === 500) {
            if (VERBOSE) {
                log(`  ✗ Random request ${i + 1} caused crash (${endpoint.path})`, 'red');
            }
            results.failed++;
            failed++;
            results.errors.push(`Random garbage request ${i + 1} caused crash`);
        } else {
            if (VERBOSE && i % 10 === 0) {
                log(`  ✓ ${i + 1}/${requestCount} random requests handled`, 'green');
            }
            results.passed++;
            passed++;
        }
    }

    log(`\nRandom garbage requests: ${passed}/${requestCount} handled correctly`, passed === requestCount ? 'green' : 'red');
}

// Test: Bug #2 - Data persistence check
async function testDataPersistence(entity) {
    log('\n[Bug #2 Test] Data persistence check', 'cyan');
    results.total++;

    // Check if entity still exists
    const res = await request('/api/entities');

    if (res.ok && res.data.entities) {
        const foundEntity = res.data.entities.find(e =>
            e.deviceId === entity.deviceId && e.entityId === entity.entityId
        );

        if (foundEntity) {
            log('✓ Entity persists after server operations', 'green');
            results.passed++;
            return true;
        } else {
            log('✗ Entity not found (data may have been lost)', 'red');
            results.failed++;
            results.errors.push('Entity data lost');
            return false;
        }
    } else {
        log('✗ Failed to check entities', 'red');
        results.failed++;
        results.errors.push('Failed to check entities');
        return false;
    }
}

// Test: Entity still bound after stress test
async function testEntityStillBound(entity) {
    log('\n[Test] Entity still bound after stress test', 'cyan');
    results.total++;

    const res = await request('/api/status', {
        method: 'GET',
        headers: {},
        query: `?deviceId=${entity.deviceId}&entityId=${entity.entityId}`
    });

    const statusRes = await request(`/api/status?deviceId=${entity.deviceId}&entityId=${entity.entityId}`);

    if (statusRes.ok && statusRes.data.isBound) {
        log('✓ Entity still bound after stress test', 'green');
        results.passed++;
        return true;
    } else {
        log('✗ Entity unbound or not found after stress test', 'red');
        results.failed++;
        results.errors.push('Entity became unbound after stress test');
        return false;
    }
}

// Main test runner
async function runTests() {
    log('\n╔════════════════════════════════════════════════════════════╗', 'magenta');
    log('║  Claw Backend Stress Test - Bug #1 & Bug #2 Validation  ║', 'magenta');
    log('╚════════════════════════════════════════════════════════════╝', 'magenta');

    log(`\nAPI Base: ${API_BASE}`, 'cyan');
    log(`Verbose Mode: ${VERBOSE ? 'ON' : 'OFF'}`, 'cyan');

    // 1. Health check
    const healthy = await testHealthCheck();
    if (!healthy) {
        log('\n✗ Server is not healthy, aborting tests', 'red');
        return;
    }

    // 2. Setup test entity
    const entity = await setupTestEntity();
    if (!entity) {
        log('\n✗ Failed to setup test entity, aborting tests', 'red');
        return;
    }

    // 3. Bug #1 tests - Malformed requests should NOT crash server
    await testMalformedEntitySpeak(entity);
    await testMalformedTransform(entity);
    await testRandomGarbageRequests();

    // 4. Check entity is still bound after all the malformed requests
    await testEntityStillBound(entity);

    // 5. Bug #2 test - Data persistence
    await testDataPersistence(entity);

    // 6. Final health check
    log('\n[Final] Health check after stress test', 'cyan');
    const stillHealthy = await testHealthCheck();

    // Print summary
    log('\n╔════════════════════════════════════════════════════════════╗', 'magenta');
    log('║                      TEST SUMMARY                         ║', 'magenta');
    log('╚════════════════════════════════════════════════════════════╝', 'magenta');

    log(`\nTotal Tests: ${results.total}`, 'cyan');
    log(`Passed: ${results.passed}`, 'green');
    log(`Failed: ${results.failed}`, results.failed > 0 ? 'red' : 'green');

    if (results.errors.length > 0) {
        log('\nErrors:', 'red');
        results.errors.forEach((err, i) => {
            log(`  ${i + 1}. ${err}`, 'red');
        });
    }

    const successRate = ((results.passed / results.total) * 100).toFixed(2);
    log(`\nSuccess Rate: ${successRate}%`, successRate === '100.00' ? 'green' : 'yellow');

    if (results.failed === 0 && stillHealthy) {
        log('\n✓ ALL TESTS PASSED - Backend is stable! 🎉', 'green');
        process.exit(0);
    } else {
        log('\n✗ SOME TESTS FAILED - Please review errors above', 'red');
        process.exit(1);
    }
}

// Run tests
runTests().catch(err => {
    log(`\n✗ Test runner crashed: ${err.message}`, 'red');
    console.error(err);
    process.exit(1);
});
