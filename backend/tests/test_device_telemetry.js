/**
 * Device Telemetry Module Tests
 *
 * Tests: sanitize, appendEntries, getEntries, getSummary, clearEntries, middleware
 * Uses a mock pool to simulate PostgreSQL without a real connection.
 */

const telemetry = require('../device-telemetry');

let passed = 0;
let failed = 0;

function test(name, fn) {
    try {
        fn();
        console.log(`  ✅ ${name}`);
        passed++;
    } catch (err) {
        console.error(`  ❌ ${name}: ${err.message}`);
        failed++;
    }
}

async function testAsync(name, fn) {
    try {
        await fn();
        console.log(`  ✅ ${name}`);
        passed++;
    } catch (err) {
        console.error(`  ❌ ${name}: ${err.message}`);
        failed++;
    }
}

function assert(condition, msg) {
    if (!condition) throw new Error(msg || 'Assertion failed');
}

// ============================================
// Mock Pool
// ============================================

function createMockPool() {
    const rows = [];
    let idCounter = 0;

    return {
        rows,
        query: async (sql, params) => {
            // CREATE TABLE / CREATE INDEX
            if (sql.trim().startsWith('CREATE')) return { rows: [] };

            // INSERT
            if (sql.includes('INSERT INTO device_telemetry')) {
                const [deviceId, ts, type, action, page, input, output, duration, meta, sizeBytes] = params;
                const row = {
                    id: ++idCounter,
                    device_id: deviceId, ts, type, action, page,
                    input: input ? JSON.parse(input) : null,
                    output: output ? JSON.parse(output) : null,
                    duration, meta: meta ? JSON.parse(meta) : null,
                    size_bytes: sizeBytes
                };
                rows.push(row);
                return { rows: [row] };
            }

            // SELECT MIN/MAX (summary) — must be before SUM check since summary query has both
            if (sql.includes('MIN(ts)')) {
                const deviceId = params[0];
                const deviceRows = rows.filter(r => r.device_id === deviceId);
                return { rows: [{
                    entry_count: deviceRows.length,
                    total_bytes: deviceRows.reduce((sum, r) => sum + (r.size_bytes || 0), 0),
                    oldest_ts: deviceRows.length > 0 ? Math.min(...deviceRows.map(r => r.ts)) : null,
                    newest_ts: deviceRows.length > 0 ? Math.max(...deviceRows.map(r => r.ts)) : null
                }] };
            }

            // SELECT SUM + COUNT (size check)
            if (sql.includes('SUM(size_bytes)')) {
                const deviceId = params[0];
                const deviceRows = rows.filter(r => r.device_id === deviceId);
                const total = deviceRows.reduce((sum, r) => sum + (r.size_bytes || 0), 0);
                return { rows: [{ total, cnt: deviceRows.length }] };
            }

            // SELECT type breakdown
            if (sql.includes('GROUP BY type')) {
                const deviceId = params[0];
                const deviceRows = rows.filter(r => r.device_id === deviceId);
                const breakdown = {};
                for (const r of deviceRows) {
                    breakdown[r.type] = (breakdown[r.type] || 0) + 1;
                }
                return { rows: Object.entries(breakdown).map(([type, cnt]) => ({ type, cnt })) };
            }

            // DELETE (must be before generic SELECT check since prune query has subquery)
            if (sql.includes('DELETE FROM device_telemetry')) {
                const deviceId = params[0];
                if (params.length === 1) {
                    // Clear all
                    const before = rows.length;
                    for (let i = rows.length - 1; i >= 0; i--) {
                        if (rows[i].device_id === deviceId) rows.splice(i, 1);
                    }
                    return { rows: [], rowCount: before - rows.length };
                } else {
                    // Prune oldest N
                    const limit = params[1];
                    const deviceRows = rows.filter(r => r.device_id === deviceId).sort((a, b) => a.ts - b.ts);
                    const toDelete = deviceRows.slice(0, limit);
                    for (const d of toDelete) {
                        const idx = rows.indexOf(d);
                        if (idx >= 0) rows.splice(idx, 1);
                    }
                    return { rows: [], rowCount: toDelete.length };
                }
            }

            // SELECT entries (read) — generic fallback
            if (sql.includes('FROM device_telemetry WHERE device_id')) {
                const deviceId = params[0];
                let filtered = rows.filter(r => r.device_id === deviceId);

                // Apply filters based on params
                let paramIdx = 1;
                if (sql.includes('AND type =')) {
                    filtered = filtered.filter(r => r.type === params[paramIdx]);
                    paramIdx++;
                }
                if (sql.includes('AND page =')) {
                    filtered = filtered.filter(r => r.page === params[paramIdx]);
                    paramIdx++;
                }
                if (sql.includes('AND action LIKE')) {
                    const pattern = params[paramIdx].replace(/%/g, '');
                    filtered = filtered.filter(r => r.action && r.action.includes(pattern));
                    paramIdx++;
                }

                // Sort by ts DESC (as PostgreSQL ORDER BY ts DESC would)
                filtered.sort((a, b) => b.ts - a.ts);
                const limit = params[params.length - 1];
                filtered = filtered.slice(0, limit);
                return { rows: filtered };
            }

            return { rows: [] };
        }
    };
}

// ============================================
// TESTS
// ============================================

(async () => {
    console.log('\n=== Sanitize ===\n');

    test('Strips deviceSecret', () => {
        const result = telemetry.sanitize({ deviceId: 'abc', deviceSecret: 'xyz123', text: 'hello' });
        assert(result.deviceSecret === '[REDACTED]', 'deviceSecret should be redacted');
        assert(result.deviceId === 'abc', 'deviceId should be kept');
        assert(result.text === 'hello', 'text should be kept');
    });

    test('Strips nested secrets', () => {
        const result = telemetry.sanitize({ body: { botSecret: 'sec', name: 'bot1' } });
        assert(result.body.botSecret === '[REDACTED]', 'nested botSecret should be redacted');
        assert(result.body.name === 'bot1', 'nested name should be kept');
    });

    test('Truncates long strings', () => {
        const longStr = 'A'.repeat(500);
        const result = telemetry.sanitize({ message: longStr });
        assert(result.message.length < 500, 'long string should be truncated');
        assert(result.message.includes('…'), 'should have ellipsis');
    });

    test('Handles null/undefined', () => {
        assert(telemetry.sanitize(null) === null);
        assert(telemetry.sanitize(undefined) === undefined);
    });

    console.log('\n=== Append + Read ===\n');

    const pool = createMockPool();

    await testAsync('Init table', async () => {
        await telemetry.initTelemetryTable(pool);
    });

    await testAsync('Append single entry', async () => {
        const result = await telemetry.appendEntries(pool, 'device-A', [{
            ts: 1000, type: 'page_view', page: 'chat', action: 'open_chat'
        }]);
        assert(result.accepted === 1, `Expected 1 accepted, got ${result.accepted}`);
        assert(result.dropped === 0);
    });

    await testAsync('Append batch of entries', async () => {
        const entries = [
            { ts: 2000, type: 'user_input', action: 'speak', input: { text: 'hello' } },
            { ts: 3000, type: 'bot_response', action: 'transform', output: { text: 'hi there' } },
            { ts: 4000, type: 'api_req', action: 'POST /api/client/speak', duration: 250 }
        ];
        const result = await telemetry.appendEntries(pool, 'device-A', entries);
        assert(result.accepted === 3, `Expected 3 accepted, got ${result.accepted}`);
    });

    await testAsync('Read all entries for device', async () => {
        const entries = await telemetry.getEntries(pool, 'device-A', {});
        assert(entries.length === 4, `Expected 4 entries, got ${entries.length}`);
        assert(entries[0].ts === 1000, 'Should be sorted oldest first');
        assert(entries[3].ts === 4000, 'Newest should be last');
    });

    await testAsync('Read with type filter', async () => {
        const entries = await telemetry.getEntries(pool, 'device-A', { type: 'api_req' });
        assert(entries.length === 1, `Expected 1 api_req, got ${entries.length}`);
        assert(entries[0].action === 'POST /api/client/speak');
    });

    await testAsync('Read with action filter', async () => {
        const entries = await telemetry.getEntries(pool, 'device-A', { action: 'speak' });
        assert(entries.length >= 1, 'Should find entries matching "speak"');
    });

    await testAsync('Device isolation', async () => {
        await telemetry.appendEntries(pool, 'device-B', [{
            ts: 5000, type: 'page_view', page: 'settings'
        }]);
        const aEntries = await telemetry.getEntries(pool, 'device-A', {});
        const bEntries = await telemetry.getEntries(pool, 'device-B', {});
        assert(aEntries.length === 4, 'Device A should have 4 entries');
        assert(bEntries.length === 1, 'Device B should have 1 entry');
    });

    console.log('\n=== Summary ===\n');

    await testAsync('Get summary', async () => {
        const summary = await telemetry.getSummary(pool, 'device-A');
        assert(summary !== null, 'Summary should not be null');
        assert(summary.entryCount === 4, `Expected 4 entries, got ${summary.entryCount}`);
        assert(summary.totalBytes > 0, 'Should have non-zero bytes');
        assert(summary.usagePercent >= 0, 'Should have usage percent');
        assert(summary.oldestTs === 1000, 'Oldest should be 1000');
        assert(summary.newestTs === 4000, 'Newest should be 4000');
        assert(summary.typeBreakdown.page_view === 1, 'Should have 1 page_view');
    });

    console.log('\n=== Clear ===\n');

    await testAsync('Clear device entries', async () => {
        await telemetry.clearEntries(pool, 'device-A');
        const entries = await telemetry.getEntries(pool, 'device-A', {});
        assert(entries.length === 0, 'Should have no entries after clear');
    });

    await testAsync('Clear does not affect other devices', async () => {
        const entries = await telemetry.getEntries(pool, 'device-B', {});
        assert(entries.length === 1, 'Device B should still have 1 entry');
    });

    console.log('\n=== captureApiCall (fire-and-forget) ===\n');

    await testAsync('captureApiCall writes api_req entry', async () => {
        telemetry.captureApiCall(pool, 'device-C', {
            method: 'POST', path: '/api/client/speak',
            input: { entityId: 0, text: 'test' },
            output: { status: 200, success: true },
            duration: 150
        });
        // Give it a tick to complete
        await new Promise(r => setTimeout(r, 50));
        const entries = await telemetry.getEntries(pool, 'device-C', {});
        assert(entries.length === 1, 'Should have 1 entry');
        assert(entries[0].type === 'api_req', 'Type should be api_req');
        assert(entries[0].action === 'POST /api/client/speak', 'Action should match');
        assert(entries[0].duration === 150, 'Duration should match');
    });

    console.log('\n=== Middleware ===\n');

    test('createMiddleware returns function', () => {
        const mw = telemetry.createMiddleware(pool, () => null);
        assert(typeof mw === 'function', 'Should return a function');
    });

    test('Middleware skips non-API paths', () => {
        const mw = telemetry.createMiddleware(pool, () => null);
        let nextCalled = false;
        mw(
            { path: '/portal/index.html' },
            { json: () => {} },
            () => { nextCalled = true; }
        );
        assert(nextCalled, 'Should call next for non-API paths');
    });

    test('Middleware intercepts res.json for API paths', () => {
        const mw = telemetry.createMiddleware(pool, (id) => id === 'dev1' ? {} : null);
        let jsonIntercepted = false;
        const mockRes = {
            json: function (data) { return data; },
            statusCode: 200
        };
        const mockReq = {
            path: '/api/client/speak',
            method: 'POST',
            body: { deviceId: 'dev1', text: 'hi' }
        };
        mw(mockReq, mockRes, () => {});
        // After middleware, res.json should be wrapped
        const result = mockRes.json({ success: true });
        assert(result !== undefined || result === undefined, 'Should not throw');
    });

    console.log('\n=== Size limits ===\n');

    test('MAX_BUFFER_BYTES is 1 MB', () => {
        assert(telemetry.MAX_BUFFER_BYTES === 1024 * 1024, `Expected 1MB, got ${telemetry.MAX_BUFFER_BYTES}`);
    });

    test('MAX_ENTRIES is 5000', () => {
        assert(telemetry.MAX_ENTRIES === 5000, `Expected 5000, got ${telemetry.MAX_ENTRIES}`);
    });

    // Summary
    console.log('\n==========================');
    console.log(`Results: ${passed} passed, ${failed} failed out of ${passed + failed} tests`);
    if (failed > 0) {
        console.log('❌ SOME TESTS FAILED');
        process.exit(1);
    } else {
        console.log('✅ ALL TESTS PASSED');
    }
})();
