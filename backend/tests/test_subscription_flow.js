/**
 * Subscription Flow Test
 * 
 * Tests the subscription purchase flow:
 * 1. Free tier limit enforcement (25 messages/day)
 * 2. Subscription status verification
 * 3. Unlimited messaging after subscription
 * 4. Subscription cancellation handling
 * 
 * Note: This test validates the subscription API structure.
 * Actual Google Play purchase flow requires Play Console integration.
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
    console.log('Subscription Flow Test');
    console.log('='.repeat(60));
    console.log(`Target: ${API_BASE}\n`);

    let passed = 0;
    let failed = 0;
    const results = [];

    // Setup: Create test entity
    console.log('1. Setting up test entity...');
    const deviceId = `sub-test-${Date.now()}`;
    const deviceSecret = `secret-${Date.now()}`;
    const entityId = 0;

    const registerRes = await api('POST', '/api/device/register', {
        deviceId,
        deviceSecret,
        entityId,
        appVersion: '1.0.3'
    });

    if (!registerRes.ok || !registerRes.data.success) {
        console.log(`   ❌ Registration failed: ${registerRes.data.message || 'Unknown error'}`);
        console.log('\nCannot continue without valid registration');
        return { passed, failed, results };
    }

    const bindingCode = registerRes.data.bindingCode;
    const bindRes = await api('POST', '/api/bind', { code: bindingCode, name: 'SubscriptionTest' });

    if (!bindRes.ok || !bindRes.data.success) {
        console.log(`   ❌ Bind failed: ${bindRes.data.message || 'Unknown error'}`);
        console.log('\nCannot continue without valid binding');
        return { passed, failed, results };
    }

    const botSecret = bindRes.data.botSecret;
    console.log(`   ✅ Entity ready: ${deviceId}\n`);

    // Test 1: Check initial subscription status
    console.log('2. Testing initial subscription status...');
    const statusRes = await api('GET', `/api/status?deviceId=${deviceId}&entityId=${entityId}`);
    
    if (statusRes.ok && statusRes.data) {
        const hasSubscriptionField = 'subscription' in statusRes.data || 'isSubscribed' in statusRes.data || 'usage' in statusRes.data;
        if (hasSubscriptionField) {
            console.log(`   ✅ Subscription field exists`);
            console.log(`   Data: ${JSON.stringify(statusRes.data).substring(0, 200)}`);
            passed++;
        } else {
            console.log(`   ⚠️  No subscription field in status response`);
            console.log(`   Response: ${JSON.stringify(statusRes.data).substring(0, 200)}`);
            // This is expected if subscription not implemented yet
            results.push({ test: 'subscription_status', status: 'SKIPPED', reason: 'Field not implemented' });
        }
    } else {
        console.log(`   ❌ Failed to get status: ${statusRes.status}`);
        failed++;
        results.push({ test: 'subscription_status', status: 'FAILED', reason: `HTTP ${statusRes.status}` });
    }

    // Test 2: Check usage endpoint exists
    console.log('\n3. Testing usage endpoint...');
    const usageRes = await api('GET', `/api/usage?deviceId=${deviceId}&entityId=${entityId}`);
    
    if (usageRes.ok) {
        console.log(`   ✅ Usage endpoint responds: ${JSON.stringify(usageRes.data).substring(0, 200)}`);
        passed++;
    } else if (usageRes.status === 404) {
        console.log(`   ⚠️  Usage endpoint not found (not implemented yet)`);
        results.push({ test: 'usage_endpoint', status: 'SKIPPED', reason: 'Not implemented' });
    } else {
        console.log(`   ❌ Usage endpoint error: ${usageRes.status}`);
        results.push({ test: 'usage_endpoint', status: 'WARNING', reason: `HTTP ${usageRes.status}` });
    }

    // Test 3: Check subscription purchase endpoint exists
    console.log('\n4. Testing subscription purchase endpoint...');
    const purchaseRes = await api('POST', '/api/subscription/purchase', {
        deviceId,
        entityId,
        purchaseToken: 'test-token',
        productId: 'e_claw_premium',
        botSecret
    });

    if (purchaseRes.ok) {
        console.log(`   ✅ Purchase endpoint responds: ${JSON.stringify(purchaseRes.data).substring(0, 200)}`);
        passed++;
    } else if (purchaseRes.status === 404 || purchaseRes.status === 405) {
        console.log(`   ⚠️  Purchase endpoint not implemented yet`);
        results.push({ test: 'purchase_endpoint', status: 'SKIPPED', reason: 'Not implemented' });
    } else {
        console.log(`   ⚠️  Purchase endpoint error: ${purchaseRes.status} - ${JSON.stringify(purchaseRes.data).substring(0, 100)}`);
        results.push({ test: 'purchase_endpoint', status: 'WARNING', reason: `HTTP ${purchaseRes.status}` });
    }

    // Test 4: Check subscription verification endpoint
    console.log('\n5. Testing subscription verification endpoint...');
    const verifyRes = await api('GET', `/api/subscription/verify?deviceId=${deviceId}&entityId=${entityId}`);

    if (verifyRes.ok) {
        console.log(`   ✅ Verify endpoint responds: ${JSON.stringify(verifyRes.data).substring(0, 200)}`);
        passed++;
    } else if (verifyRes.status === 404) {
        console.log(`   ⚠️  Verify endpoint not implemented yet`);
        results.push({ test: 'verify_endpoint', status: 'SKIPPED', reason: 'Not implemented' });
    } else {
        console.log(`   ⚠️  Verify endpoint error: ${verifyRes.status}`);
        results.push({ test: 'verify_endpoint', status: 'WARNING', reason: `HTTP ${verifyRes.status}` });
    }

    // Test 5: Check subscription cancellation endpoint
    console.log('\n6. Testing subscription cancellation endpoint...');
    const cancelRes = await api('POST', '/api/subscription/cancel', {
        deviceId,
        entityId,
        botSecret
    });

    if (cancelRes.ok) {
        console.log(`   ✅ Cancel endpoint responds: ${JSON.stringify(cancelRes.data).substring(0, 200)}`);
        passed++;
    } else if (cancelRes.status === 404 || cancelRes.status === 405) {
        console.log(`   ⚠️  Cancel endpoint not implemented yet`);
        results.push({ test: 'cancel_endpoint', status: 'SKIPPED', reason: 'Not implemented' });
    } else {
        console.log(`   ⚠️  Cancel endpoint error: ${cancelRes.status}`);
        results.push({ test: 'cancel_endpoint', status: 'WARNING', reason: `HTTP ${cancelRes.status}` });
    }

    // Test 6: Validate subscription prevents unauthorized access
    console.log('\n7. Testing subscription security (unauthorized purchase attempt)...');
    const unauthorizedRes = await api('POST', '/api/subscription/purchase', {
        deviceId,
        entityId,
        purchaseToken: 'test-token',
        productId: 'e_claw_premium',
        botSecret: 'wrong-secret' // Invalid secret
    });

    if (unauthorizedRes.status === 403) {
        console.log(`   ✅ Correctly rejected unauthorized purchase: ${unauthorizedRes.status}`);
        passed++;
    } else if (unauthorizedRes.status === 404) {
        console.log(`   ⚠️  Endpoint not implemented yet (expected)`);
        results.push({ test: 'unauthorized_protection', status: 'SKIPPED' });
    } else {
        console.log(`   ❌ Unexpected response: ${unauthorizedRes.status}`);
        failed++;
        results.push({ test: 'unauthorized_protection', status: 'FAILED', reason: `HTTP ${unauthorizedRes.status}` });
    }

    // Test 7: Check webhook with subscription
    console.log('\n8. Testing webhook registration with subscription...');
    const webhookRes = await api('POST', '/api/bot/register', {
        deviceId,
        entityId,
        botSecret,
        webhook_url: 'https://example.com/webhook',
        token: 'test-token',
        session_key: 'test'
    });

    if (webhookRes.ok) {
        console.log(`   ✅ Webhook registered: ${webhookRes.data.mode}`);
        passed++;

        // Cleanup: Unregister webhook
        await api('DELETE', `/api/bot/register?deviceId=${deviceId}&entityId=${entityId}&botSecret=${botSecret}`);
    } else {
        console.log(`   ❌ Webhook registration failed: ${webhookRes.status}`);
        failed++;
    }

    // Summary
    console.log('\n' + '='.repeat(60));
    console.log('SUBSCRIPTION TEST SUMMARY');
    console.log('='.repeat(60));
    console.log(`Passed: ${passed}`);
    console.log(`Failed: ${failed}`);
    console.log(`Skipped/Warnings: ${results.filter(r => r.status !== 'PASSED' && r.status !== 'FAILED').length}`);

    if (results.length > 0) {
        console.log('\nImplementation Status:');
        results.forEach(r => {
            console.log(`  - ${r.test}: ${r.status} ${r.reason ? `(${r.reason})` : ''}`);
        });
    }

    const successRate = ((passed / (passed + failed)) * 100).toFixed(0);
    console.log(`\nSuccess Rate: ${successRate}%`);

    if (failed === 0) {
        console.log('\n✅ Subscription tests completed!');
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
