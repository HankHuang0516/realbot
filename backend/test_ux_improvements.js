/**
 * UX Improvements Verification Tests
 * Tests for: Agent Cards, Layout Editor, API responses
 */

const API_BASE = 'https://realbot-production.up.railway.app';

async function api(method, path, body = null) {
    const url = `${API_BASE}${path}`;
    const options = {
        method,
        headers: { 'Content-Type': 'application/json' }
    };
    if (body) options.body = JSON.stringify(body);

    const response = await fetch(url, options);
    return response.json();
}

async function runTests() {
    console.log('='.repeat(60));
    console.log('UX IMPROVEMENTS VERIFICATION TESTS');
    console.log('='.repeat(60));

    let passed = 0;
    let failed = 0;

    // Test 1: /api/entities returns isBound field
    console.log('\n--- Test 1: /api/entities response structure ---');
    try {
        const response = await api('GET', '/api/entities');
        console.log('Response:', JSON.stringify(response, null, 2));

        // Check response structure
        if (!response.entities) {
            console.log('FAIL: Missing entities array');
            failed++;
        } else if (typeof response.activeCount !== 'number') {
            console.log('FAIL: Missing activeCount');
            failed++;
        } else {
            console.log('PASS: Response has correct structure');
            passed++;

            // If there are entities, check they have isBound
            if (response.entities.length > 0) {
                const firstEntity = response.entities[0];
                if (firstEntity.isBound === true) {
                    console.log('PASS: Entity has isBound: true');
                    passed++;
                } else {
                    console.log('FAIL: Entity missing isBound field or not true');
                    console.log('Entity:', firstEntity);
                    failed++;
                }

                // Check required fields
                const requiredFields = ['entityId', 'character', 'state', 'message', 'lastUpdated'];
                const missingFields = requiredFields.filter(f => !(f in firstEntity));
                if (missingFields.length === 0) {
                    console.log('PASS: Entity has all required fields');
                    passed++;
                } else {
                    console.log('FAIL: Entity missing fields:', missingFields);
                    failed++;
                }
            } else {
                console.log('INFO: No bound entities to test (bind one first)');
            }
        }
    } catch (e) {
        console.log('FAIL: API error -', e.message);
        failed++;
    }

    // Test 2: /api/entities with deviceId filter
    console.log('\n--- Test 2: /api/entities with deviceId filter ---');
    try {
        const testDeviceId = 'test-device-' + Date.now();
        const response = await api('GET', `/api/entities?deviceId=${testDeviceId}`);

        if (response.entities && Array.isArray(response.entities)) {
            console.log('PASS: Filter by deviceId works');
            console.log(`Entities for ${testDeviceId}: ${response.entities.length}`);
            passed++;
        } else {
            console.log('FAIL: Invalid response for filtered query');
            failed++;
        }
    } catch (e) {
        console.log('FAIL: API error -', e.message);
        failed++;
    }

    // Test 3: /api/device/register creates device
    console.log('\n--- Test 3: /api/device/register ---');
    try {
        const testDeviceId = 'test-ux-' + Date.now();
        const response = await api('POST', '/api/device/register', {
            deviceId: testDeviceId,
            entityId: 0
        });

        if (response.success && response.code) {
            console.log('PASS: Device registration successful');
            console.log(`Binding code: ${response.code}`);
            passed++;

            // Test 4: Verify device appears in /api/entities (should be empty, not bound)
            console.log('\n--- Test 4: Unbound entity not in /api/entities ---');
            const entitiesResp = await api('GET', `/api/entities?deviceId=${testDeviceId}`);
            if (entitiesResp.entities.length === 0) {
                console.log('PASS: Unbound entity correctly excluded from /api/entities');
                passed++;
            } else {
                console.log('FAIL: Unbound entity should not appear in /api/entities');
                failed++;
            }
        } else {
            console.log('FAIL: Device registration failed');
            console.log('Response:', response);
            failed++;
        }
    } catch (e) {
        console.log('FAIL: API error -', e.message);
        failed++;
    }

    // Test 5: /api/status returns isBound
    console.log('\n--- Test 5: /api/status includes isBound ---');
    try {
        const testDeviceId = 'test-status-' + Date.now();
        // First register
        await api('POST', '/api/device/register', { deviceId: testDeviceId, entityId: 0 });

        // Then check status
        const status = await api('GET', `/api/status?deviceId=${testDeviceId}&entityId=0`);

        if ('isBound' in status) {
            console.log('PASS: /api/status includes isBound field');
            console.log(`isBound: ${status.isBound}`);
            passed++;
        } else {
            console.log('FAIL: /api/status missing isBound field');
            console.log('Response:', status);
            failed++;
        }
    } catch (e) {
        console.log('FAIL: API error -', e.message);
        failed++;
    }

    // Test 6: /api/device/status returns isBound
    console.log('\n--- Test 6: /api/device/status includes isBound ---');
    try {
        const testDeviceId = 'test-devstatus-' + Date.now();
        await api('POST', '/api/device/register', { deviceId: testDeviceId, entityId: 0 });

        const status = await api('POST', '/api/device/status', { deviceId: testDeviceId });

        if (status.entities && status.entities.length > 0) {
            const entity = status.entities[0];
            if ('isBound' in entity) {
                console.log('PASS: /api/device/status includes isBound field');
                passed++;
            } else {
                console.log('FAIL: Entity in /api/device/status missing isBound');
                failed++;
            }
        } else {
            console.log('FAIL: /api/device/status returned no entities');
            failed++;
        }
    } catch (e) {
        console.log('FAIL: API error -', e.message);
        failed++;
    }

    // Summary
    console.log('\n' + '='.repeat(60));
    console.log(`RESULTS: ${passed} passed, ${failed} failed`);
    console.log('='.repeat(60));

    if (failed > 0) {
        console.log('\n⚠️  Some tests failed. Please check the backend.');
    } else {
        console.log('\n✅ All tests passed!');
    }
}

runTests().catch(console.error);
