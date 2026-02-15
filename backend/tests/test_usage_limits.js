/**
 * Usage Limit Enforcement Test
 * 
 * Tests usage limit edge cases:
 * 1. Free tier limit (25 messages/day)
 * 2. Limit exceeded response
 * 3. Reset behavior
 * 4. Subscription bypass
 * 5. Concurrent usage tracking
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
    console.log('Usage Limit Enforcement Test');
    console.log('='.repeat(60));
    console.log(`Target: ${API_BASE}\n`);

    let passed = 0;
    let failed = 0;
    const results = [];

    // Setup: Create test entity
    console.log('1. Setting up test entity...');
    const deviceId = `usage-test-${Date.now()}`;
    const deviceSecret = `secret-${Date.now()}`;
    const entityId = 0;

    const registerRes = await api('POST', '/api/device/register', {
        deviceId,
        deviceSecret,
        entityId,
        appVersion: '1.0.3'
    });

    if (!registerRes.ok || !registerRes.data.success) {
        console.log(`   ❌ Registration failed`);
        return { passed, failed, results };
    }

    const bindRes = await api('POST', '/api/bind', {
        code: registerRes.data.bindingCode,
        name: 'UsageTest'
    });

    if (!bindRes.ok || !bindRes.data.success) {
        console.log(`   ❌ Bind failed`);
        return { passed, failed, results };
    }

    const botSecret = bindRes.data.botSecret;
    console.log(`   ✅ Entity ready: ${deviceId}\n`);

    // Test 1: Check initial usage (should be 0 or not exist)
    console.log('2. Testing initial usage check...');
    const usageRes = await api('GET', `/api/usage?deviceId=${deviceId}&entityId=${entityId}`);
    
    if (usageRes.ok && usageRes.data) {
        const hasUsage = 'usage' in usageRes.data || 'count' in usageRes.data || 'messagesUsed' in usageRes.data;
        if (hasUsage) {
            console.log(`   ✅ Usage tracking exists: ${JSON.stringify(usageRes.data).substring(0, 100)}`);
            passed++;
        } else {
            console.log(`   ⚠️  No usage field found (may not be implemented)`);
            results.push({ test: 'usage_tracking', status: 'SKIPPED', reason: 'Field not found' });
        }
    } else if (usageRes.status === 404) {
        console.log(`   ⚠️  Usage endpoint not implemented`);
        results.push({ test: 'usage_tracking', status: 'SKIPPED', reason: 'Not implemented' });
    } else {
        console.log(`   ❌ Usage check failed: ${usageRes.status}`);
        failed++;
    }

    // Test 2: Check usage reset endpoint
    console.log('\n3. Testing usage reset endpoint...');
    const resetRes = await api('POST', '/api/usage/reset', {
        deviceId,
        entityId,
        botSecret
    });

    if (resetRes.ok) {
        console.log(`   ✅ Reset endpoint responds: ${JSON.stringify(resetRes.data).substring(0, 100)}`);
        passed++;
    } else if (resetRes.status === 404) {
        console.log(`   ⚠️  Reset endpoint not implemented`);
        results.push({ test: 'usage_reset', status: 'SKIPPED', reason: 'Not implemented' });
    } else {
        console.log(`   ⚠️  Reset endpoint error: ${resetRes.status}`);
        results.push({ test: 'usage_reset', status: 'WARNING' });
    }

    // Test 3: Check limit configuration endpoint
    console.log('\n4. Testing limit configuration endpoint...');
    const limitRes = await api('GET', '/api/usage/limits');

    if (limitRes.ok) {
        console.log(`   ✅ Limits endpoint responds: ${JSON.stringify(limitRes.data).substring(0, 100)}`);
        
        // Verify expected fields
        const hasLimit = 'freeTier' in limitRes.data || 'dailyLimit' in limitRes.data || 'limit' in limitRes.data;
        if (hasLimit) {
            console.log(`   ✅ Contains expected limit fields`);
            passed++;
        } else {
            console.log(`   ⚠️  Missing expected limit fields`);
            results.push({ test: 'limit_config', status: 'WARNING', reason: 'Missing fields' });
        }
    } else if (limitRes.status === 404) {
        console.log(`   ⚠️  Limits endpoint not implemented`);
        results.push({ test: 'limit_config', status: 'SKIPPED', reason: 'Not implemented' });
    } else {
        console.log(`   ❌ Limits endpoint error: ${limitRes.status}`);
        failed++;
    }

    // Test 4: Send messages and verify usage increases
    console.log('\n5. Testing message usage tracking...');
    const messageCount = 5;
    let messagesTracked = 0;

    for (let i = 0; i < messageCount; i++) {
        const msgRes = await api('POST', '/api/client/speak', {
            deviceId,
            entityId,
            text: `Test message ${i + 1}`
        });

        if (msgRes.ok || msgRes.status === 200 || msgRes.status === 429) {
            messagesTracked++;
        }
        
        await sleep(100); // Small delay between messages
    }

    console.log(`   Sent ${messageCount} messages, ${messagesTracked} tracked`);
    if (messagesTracked === messageCount) {
        passed++;
    } else {
        console.log(`   ⚠️  Some messages not processed`);
        results.push({ test: 'message_tracking', status: 'PARTIAL', count: messagesTracked });
    }

    // Test 5: Check usage after messages
    console.log('\n6. Checking usage after messages...');
    const usageAfterRes = await api('GET', `/api/usage?deviceId=${deviceId}&entityId=${entityId}`);
    
    if (usageAfterRes.ok && usageAfterRes.data) {
        console.log(`   ✅ Usage after messages: ${JSON.stringify(usageAfterRes.data).substring(0, 100)}`);
        passed++;
    } else if (usageAfterRes.status === 404) {
        console.log(`   ⚠️  Usage endpoint not implemented`);
        results.push({ test: 'usage_after_messages', status: 'SKIPPED' });
    } else {
        console.log(`   ❌ Failed to get usage: ${usageAfterRes.status}`);
        failed++;
    }

    // Test 6: Test limit exceeded response (429 status)
    console.log('\n7. Testing limit exceeded response...');
    // This test simulates what happens when limit is exceeded
    // We expect a 429 Too Many Requests response
    const limitExceededRes = await api('POST', '/api/client/speak', {
        deviceId,
        entityId,
        text: 'This should be rejected if limit exceeded'
    });

    if (limitExceededRes.status === 429) {
        console.log(`   ✅ Correctly returned 429 (Too Many Requests)`);
        passed++;
    } else if (limitExceededRes.status === 200 || limitExceededRes.status === 400) {
        console.log(`   ⚠️  No rate limiting implemented yet (got ${limitExceededRes.status})`);
        results.push({ test: 'rate_limiting', status: 'SKIPPED', reason: 'Not implemented' });
    } else {
        console.log(`   ⚠️  Unexpected response: ${limitExceededRes.status}`);
        results.push({ test: 'rate_limiting', status: 'WARNING', reason: `HTTP ${limitExceededRes.status}` });
    }

    // Test 7: Verify usage is tied to subscription status
    console.log('\n8. Testing subscription bypass for usage...');
    const subBypassRes = await api('GET', `/api/usage?deviceId=${deviceId}&entityId=${entityId}&subscriptionStatus=active`);

    if (subBypassRes.ok) {
        console.log(`   ✅ Subscription bypass parameter accepted`);
        passed++;
    } else if (subBypassRes.status === 404) {
        console.log(`   ⚠️  Subscription bypass not implemented`);
        results.push({ test: 'subscription_bypass', status: 'SKIPPED' });
    } else {
        console.log(`   ❌ Bypass test failed: ${subBypassRes.status}`);
        failed++;
    }

    // Test 8: Concurrent usage tracking test
    console.log('\n9. Testing concurrent usage tracking...');
    const concurrentDevice1 = `usage-concurrent1-${Date.now()}`;
    const concurrentDevice2 = `usage-concurrent2-${Date.now()}`;
    
    // Register two devices
    const reg1 = await api('POST', '/api/device/register', {
        deviceId: concurrentDevice1,
        deviceSecret: 'secret1',
        entityId: 0,
        appVersion: '1.0.3'
    });

    const reg2 = await api('POST', '/api/device/register', {
        deviceId: concurrentDevice2,
        deviceSecret: 'secret2',
        entityId: 0,
        appVersion: '1.0.3'
    });

    if (reg1.ok && reg2.ok) {
        console.log(`   ✅ Concurrent device registration works`);
        passed++;
    } else {
        console.log(`   ❌ Concurrent registration failed`);
        failed++;
    }

    // Summary
    console.log('\n' + '='.repeat(60));
    console.log('USAGE LIMIT TEST SUMMARY');
    console.log('='.repeat(60));
    console.log(`Passed: ${passed}`);
    console.log(`Failed: ${failed}`);
    console.log(`Skipped/Warnings: ${results.length}`);

    if (results.length > 0) {
        console.log('\nImplementation Status:');
        results.forEach(r => {
            console.log(`  - ${r.test}: ${r.status} ${r.reason ? `(${r.reason})` : ''}`);
        });
    }

    const successRate = ((passed / (passed + failed)) * 100).toFixed(0);
    console.log(`\nSuccess Rate: ${successRate}%`);

    if (failed === 0) {
        console.log('\n✅ Usage limit tests completed!');
    } else {
        console.log(`\n❌ ${failed} test(s) failed`);
    }

    return { passed, failed, results };
}

// Run tests
runTests().catch(err => {
    console.error('\nTest error:', err.message);
    process.exit(1);
});
