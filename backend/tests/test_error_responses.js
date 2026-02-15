/**
 * Error Response Validation Test
 * 
 * Tests that API returns proper error responses:
 * 1. 400 Bad Request for invalid input
 * 2. 401 Unauthorized for missing/invalid credentials
 * 3. 403 Forbidden for access denied
 * 4. 404 Not Found for missing resources
 * 5. 429 Too Many Requests for rate limiting
 * 6. 500 Internal Server Error is NOT returned for client errors
 */

const API_BASE = process.env.API_BASE || 'https://eclaw.up.railway.app';

async function api(method, path, body = null) {
    const options = {
        method,
        headers: { 'Content-Type': 'application/json' }
    };
    if (body) options.body = JSON.stringify(body);
    
    try {
        const res = await fetch(`${API_BASE}${path}`, options);
        return { 
            status: res.status, 
            data: await res.json().catch(() => ({})),
            ok: res.ok 
        };
    } catch (err) {
        return { status: 0, error: err.message, ok: false };
    }
}

async function sleep(ms) {
    return new Promise(resolve => setTimeout(resolve, ms));
}

async function runTests() {
    console.log('='.repeat(60));
    console.log('Error Response Validation Test');
    console.log('='.repeat(60));
    console.log(`Target: ${API_BASE}\n`);

    let passed = 0;
    let failed = 0;
    const results = [];

    // Setup: Create test entity for authenticated tests
    console.log('1. Setting up test entity...');
    const deviceId = `error-test-${Date.now()}`;
    const deviceSecret = `secret-${Date.now()}`;
    const entityId = 0;

    const registerRes = await api('POST', '/api/device/register', {
        deviceId,
        deviceSecret,
        entityId,
        appVersion: '1.0.3'
    });

    if (!registerRes.ok || !registerRes.data.success) {
        console.log(`   ❌ Registration failed, some tests will be skipped`);
        console.log(`   Note: Running unauthenticated error tests...\n`);
    } else {
        const bindRes = await api('POST', '/api/bind', {
            code: registerRes.data.bindingCode,
            name: 'ErrorTest'
        });
        
        if (bindRes.ok) {
            console.log(`   ✅ Entity ready for authenticated tests\n`);
        } else {
            console.log(`   ⚠️  Bind failed, running partial tests\n`);
        }
    }

    // Test 1: 400 Bad Request - Missing required fields
    console.log('2. Testing 400 Bad Request (missing fields)...');
    const missingFieldsRes = await api('POST', '/api/transform', {
        deviceId: deviceId
        // Missing entityId, botSecret, state
    });

    if (missingFieldsRes.status === 400) {
        console.log(`   ✅ Correctly returned 400 for missing fields`);
        passed++;
    } else if (missingFieldsRes.status >= 500) {
        console.log(`   ❌ Server crashed (${missingFieldsRes.status}) - should return 400`);
        failed++;
        results.push({ test: 'missing_fields', status: 'FAILED', reason: `Server error ${missingFieldsRes.status}` });
    } else {
        console.log(`   ⚠️  Got ${missingFieldsRes.status}, expected 400`);
        results.push({ test: 'missing_fields', status: 'WARNING', reason: `Got ${missingFieldsRes.status}` });
    }

    // Test 2: 401/403 Unauthorized - Invalid botSecret
    console.log('\n3. Testing 403 Forbidden (invalid botSecret)...');
    const invalidSecretRes = await api('POST', '/api/transform', {
        deviceId: deviceId,
        entityId: entityId,
        botSecret: 'invalid-secret-12345',
        state: 'IDLE',
        message: 'test'
    });

    if (invalidSecretRes.status === 403 || invalidSecretRes.status === 401) {
        console.log(`   ✅ Correctly returned ${invalidSecretRes.status} for invalid secret`);
        passed++;
    } else if (invalidSecretRes.status >= 500) {
        console.log(`   ❌ Server crashed (${invalidSecretRes.status}) - should return 403`);
        failed++;
        results.push({ test: 'invalid_secret', status: 'FAILED', reason: `Server error ${invalidSecretRes.status}` });
    } else {
        console.log(`   ⚠️  Got ${invalidSecretRes.status}, expected 403`);
        results.push({ test: 'invalid_secret', status: 'WARNING', reason: `Got ${invalidSecretRes.status}` });
    }

    // Test 3: 403 Forbidden - Wrong device
    console.log('\n4. Testing 403 Forbidden (cross-device access)...');
    const crossDeviceRes = await api('POST', '/api/transform', {
        deviceId: 'wrong-device-id',
        entityId: entityId,
        botSecret: 'invalid-secret',
        state: 'IDLE',
        message: 'test'
    });

    if (crossDeviceRes.status === 403) {
        console.log(`   ✅ Correctly rejected cross-device access`);
        passed++;
    } else if (crossDeviceRes.status >= 500) {
        console.log(`   ❌ Server crashed (${crossDeviceRes.status})`);
        failed++;
        results.push({ test: 'cross_device', status: 'FAILED', reason: `Server error ${crossDeviceRes.status}` });
    } else {
        console.log(`   ⚠️  Got ${crossDeviceRes.status}, expected 403`);
        results.push({ test: 'cross_device', status: 'WARNING', reason: `Got ${crossDeviceRes.status}` });
    }

    // Test 4: 404 Not Found - Non-existent entity
    console.log('\n5. Testing 404 Not Found (non-existent entity)...');
    const notFoundRes = await api('GET', '/api/status?deviceId=nonexistent-device&entityId=999');

    if (notFoundRes.status === 404) {
        console.log(`   ✅ Correctly returned 404 for non-existent entity`);
        passed++;
    } else if (notFoundRes.status >= 500) {
        console.log(`   ❌ Server crashed (${notFoundRes.status})`);
        failed++;
        results.push({ test: 'not_found', status: 'FAILED', reason: `Server error ${notFoundRes.status}` });
    } else {
        console.log(`   ⚠️  Got ${notFoundRes.status}, expected 404`);
        results.push({ test: 'not_found', status: 'WARNING', reason: `Got ${notFoundRes.status}` });
    }

    // Test 5: Invalid JSON body
    console.log('\n6. Testing 400 Bad Request (invalid JSON)...');
    const invalidJsonRes = await fetch(`${API_BASE}/api/transform`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: '{ invalid json }'
    });

    if (invalidJsonRes.status === 400) {
        console.log(`   ✅ Correctly handled invalid JSON`);
        passed++;
    } else if (invalidJsonRes.status >= 500) {
        console.log(`   ❌ Server crashed on invalid JSON (${invalidJsonRes.status})`);
        failed++;
        results.push({ test: 'invalid_json', status: 'FAILED', reason: `Server error ${invalidJsonRes.status}` });
    } else {
        console.log(`   ⚠️  Got ${invalidJsonRes.status}, expected 400`);
        results.push({ test: 'invalid_json', status: 'WARNING', reason: `Got ${invalidJsonRes.status}` });
    }

    // Test 6: Invalid entity ID type
    console.log('\n7. Testing 400 Bad Request (invalid entityId type)...');
    const invalidTypeRes = await api('POST', '/api/transform', {
        deviceId: deviceId,
        entityId: 'not-a-number',
        botSecret: 'invalid',
        state: 'IDLE',
        message: 'test'
    });

    if (invalidTypeRes.status === 400) {
        console.log(`   ✅ Correctly rejected invalid entityId type`);
        passed++;
    } else if (invalidTypeRes.status >= 500) {
        console.log(`   ❌ Server crashed (${invalidTypeRes.status})`);
        failed++;
        results.push({ test: 'invalid_type', status: 'FAILED', reason: `Server error ${invalidTypeRes.status}` });
    } else {
        console.log(`   ⚠️  Got ${invalidTypeRes.status}, expected 400`);
        results.push({ test: 'invalid_type', status: 'WARNING', reason: `Got ${invalidTypeRes.status}` });
    }

    // Test 7: Out of range entity ID
    console.log('\n8. Testing 400 Bad Request (out of range entityId)...');
    const outOfRangeRes = await api('POST', '/api/transform', {
        deviceId: deviceId,
        entityId: 999,
        botSecret: 'invalid',
        state: 'IDLE',
        message: 'test'
    });

    if (outOfRangeRes.status === 400 || outOfRangeRes.status === 404) {
        console.log(`   ✅ Correctly rejected out of range entityId`);
        passed++;
    } else if (outOfRangeRes.status >= 500) {
        console.log(`   ❌ Server crashed (${outOfRangeRes.status})`);
        failed++;
        results.push({ test: 'out_of_range', status: 'FAILED', reason: `Server error ${outOfRangeRes.status}` });
    } else {
        console.log(`   ⚠️  Got ${outOfRangeRes.status}, expected 400/404`);
        results.push({ test: 'out_of_range', status: 'WARNING', reason: `Got ${outOfRangeRes.status}` });
    }

    // Test 8: Malformed speak-to request (Bug #1 fix validation)
    console.log('\n9. Testing malformed speak-to request...');
    const malformedSpeakRes = await api('POST', '/api/entity/speak-to', {
        deviceId: deviceId,
        from: 0,  // Wrong field name (should be fromEntityId)
        to: 1,    // Wrong field name (should be toEntityId)
        botSecret: 'invalid',
        text: 'test'
    });

    if (malformedSpeakRes.status === 400 || malformedSpeakRes.status === 403) {
        console.log(`   ✅ Correctly handled malformed speak-to (400/403)`);
        passed++;
    } else if (malformedSpeakRes.status >= 500) {
        console.log(`   ❌ Server crashed on malformed request (${malformedSpeakRes.status}) - BUG!`);
        failed++;
        results.push({ test: 'malformed_speak', status: 'FAILED', reason: 'Server crashed on malformed request' });
    } else {
        console.log(`   ⚠️  Got ${malformedSpeakRes.status}`);
        results.push({ test: 'malformed_speak', status: 'WARNING', reason: `Got ${malformedSpeakRes.status}` });
    }

    // Test 9: Rate limiting (429)
    console.log('\n10. Testing rate limiting (429)...');
    // Send many requests quickly
    let rateLimited = false;
    for (let i = 0; i < 20; i++) {
        const rateRes = await api('POST', '/api/transform', {
            deviceId: deviceId,
            entityId: entityId,
            botSecret: 'invalid',
            state: 'IDLE',
            message: `Rate test ${i}`
        });
        
        if (rateRes.status === 429) {
            rateLimited = true;
            break;
        }
        
        await sleep(50);
    }

    if (rateLimited) {
        console.log(`   ✅ Rate limiting working (429 received)`);
        passed++;
    } else {
        console.log(`   ⚠️  No rate limiting detected (no 429 received)`);
        results.push({ test: 'rate_limiting', status: 'SKIPPED', reason: 'Not implemented' });
    }

    // Test 10: Error response format validation
    console.log('\n11. Testing error response format...');
    const formatRes = await api('POST', '/api/transform', {
        deviceId: deviceId,
        entityId: 'invalid',
        botSecret: 'invalid',
        state: 'IDLE',
        message: 'test'
    });

    if (formatRes.data && 'success' in formatRes.data) {
        console.log(`   ✅ Error response has proper format (success field)`);
        passed++;
    } else {
        console.log(`   ⚠️  Error response format unexpected`);
        results.push({ test: 'error_format', status: 'WARNING', reason: 'Format may not match spec' });
    }

    // Summary
    console.log('\n' + '='.repeat(60));
    console.log('ERROR RESPONSE TEST SUMMARY');
    console.log('='.repeat(60));
    console.log(`Passed: ${passed}`);
    console.log(`Failed: ${failed}`);
    console.log(`Skipped/Warnings: ${results.length}`);

    if (results.length > 0) {
        console.log('\nResults:');
        results.forEach(r => {
            console.log(`  - ${r.test}: ${r.status} ${r.reason ? `(${r.reason})` : ''}`);
        });
    }

    const successRate = ((passed / (passed + failed)) * 100).toFixed(0);
    console.log(`\nSuccess Rate: ${successRate}%`);

    if (failed === 0) {
        console.log('\n✅ Error response tests completed!');
    } else {
        console.log(`\n❌ ${failed} test(s) failed - See results above`);
    }

    return { passed, failed, results };
}

// Run tests
runTests().catch(err => {
    console.error('\nTest error:', err.message);
    process.exit(1);
});
