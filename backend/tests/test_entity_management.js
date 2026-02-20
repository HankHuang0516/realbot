/**
 * Entity Management — Local Unit Tests
 *
 * Tests reorder permutation logic, cooldown mechanics, and refresh
 * validation rules WITHOUT requiring server credentials.
 *
 * Usage: node test_entity_management.js
 */

const results = [];
let passed = 0;
let failed = 0;

function check(name, condition) {
    if (condition) {
        console.log(`  ✅ ${name}`);
        passed++;
    } else {
        console.log(`  ❌ ${name}`);
        failed++;
    }
    results.push({ name, passed: condition });
}

// ── Reorder permutation logic ───────────────────────────────
console.log('=== Reorder Permutation Validation ===\n');

function isValidPermutation(order, maxEntities) {
    if (!Array.isArray(order) || order.length !== maxEntities) return false;
    const sorted = [...order].sort();
    for (let i = 0; i < maxEntities; i++) {
        if (sorted[i] !== i) return false;
    }
    return true;
}

function isIdentity(order) {
    return order.every((v, i) => v === i);
}

check('[0,1,2,3] is valid permutation', isValidPermutation([0, 1, 2, 3], 4));
check('[1,0,2,3] is valid permutation', isValidPermutation([1, 0, 2, 3], 4));
check('[3,2,1,0] is valid permutation', isValidPermutation([3, 2, 1, 0], 4));
check('[2,0,3,1] is valid permutation', isValidPermutation([2, 0, 3, 1], 4));
check('[0,0,2,3] is NOT valid (duplicate)', !isValidPermutation([0, 0, 2, 3], 4));
check('[0,1,2,4] is NOT valid (out of range)', !isValidPermutation([0, 1, 2, 4], 4));
check('[0,1] is NOT valid (wrong length)', !isValidPermutation([0, 1], 4));
check('[] is NOT valid (empty)', !isValidPermutation([], 4));
check('[0,1,2,3] is identity', isIdentity([0, 1, 2, 3]));
check('[1,0,2,3] is NOT identity', !isIdentity([1, 0, 2, 3]));

console.log('');

// ── Reorder entity swap simulation ──────────────────────────
console.log('=== Reorder Entity Swap Simulation ===\n');

function simulateReorder(entities, order) {
    const maxSlots = 4;
    const oldEntities = [];
    for (let i = 0; i < maxSlots; i++) {
        oldEntities[i] = entities[i] ? { ...entities[i] } : { entityId: i, isBound: false, message: '' };
    }

    const newEntities = {};
    for (let newSlot = 0; newSlot < maxSlots; newSlot++) {
        const oldSlot = order[newSlot];
        const entity = { ...oldEntities[oldSlot] };
        entity.entityId = newSlot;
        newEntities[newSlot] = entity;
    }
    return newEntities;
}

// Test 1: Simple swap of 0 and 1
{
    const entities = {
        0: { entityId: 0, isBound: true, message: 'A', name: 'Bot-A', botSecret: 'secret-a' },
        1: { entityId: 1, isBound: true, message: 'B', name: 'Bot-B', botSecret: 'secret-b' },
        2: { entityId: 2, isBound: false, message: '', name: null },
        3: { entityId: 3, isBound: false, message: '', name: null },
    };

    const result = simulateReorder(entities, [1, 0, 2, 3]);

    check('Swap 0↔1: slot 0 has old slot 1 data', result[0].message === 'B');
    check('Swap 0↔1: slot 1 has old slot 0 data', result[1].message === 'A');
    check('Swap 0↔1: slot 0 entityId updated to 0', result[0].entityId === 0);
    check('Swap 0↔1: slot 1 entityId updated to 1', result[1].entityId === 1);
    check('Swap 0↔1: slot 0 name is Bot-B', result[0].name === 'Bot-B');
    check('Swap 0↔1: slot 1 name is Bot-A', result[1].name === 'Bot-A');
    check('Swap 0↔1: slot 0 botSecret is secret-b', result[0].botSecret === 'secret-b');
    check('Swap 0↔1: slot 2 unchanged', result[2].isBound === false);
}

// Test 2: Full reverse
{
    const entities = {
        0: { entityId: 0, isBound: true, message: 'M0' },
        1: { entityId: 1, isBound: true, message: 'M1' },
        2: { entityId: 2, isBound: true, message: 'M2' },
        3: { entityId: 3, isBound: true, message: 'M3' },
    };

    const result = simulateReorder(entities, [3, 2, 1, 0]);

    check('Reverse: slot 0 has M3', result[0].message === 'M3');
    check('Reverse: slot 1 has M2', result[1].message === 'M2');
    check('Reverse: slot 2 has M1', result[2].message === 'M1');
    check('Reverse: slot 3 has M0', result[3].message === 'M0');
    check('Reverse: all entityIds correct',
        result[0].entityId === 0 && result[1].entityId === 1 &&
        result[2].entityId === 2 && result[3].entityId === 3);
}

// Test 3: Rotation [1,2,3,0]
{
    const entities = {
        0: { entityId: 0, message: 'A' },
        1: { entityId: 1, message: 'B' },
        2: { entityId: 2, message: 'C' },
        3: { entityId: 3, message: 'D' },
    };

    const result = simulateReorder(entities, [1, 2, 3, 0]);

    check('Rotation: slot 0 has B (from 1)', result[0].message === 'B');
    check('Rotation: slot 1 has C (from 2)', result[1].message === 'C');
    check('Rotation: slot 2 has D (from 3)', result[2].message === 'D');
    check('Rotation: slot 3 has A (from 0)', result[3].message === 'A');
}

console.log('');

// ── Cooldown logic ──────────────────────────────────────────
console.log('=== Cooldown Logic ===\n');

{
    const COOLDOWN_MS = 60000;
    const refreshCooldowns = {};

    function checkCooldown(key) {
        const lastRefresh = refreshCooldowns[key] || 0;
        const elapsed = Date.now() - lastRefresh;
        if (elapsed < COOLDOWN_MS) {
            return { allowed: false, remaining: Math.ceil((COOLDOWN_MS - elapsed) / 1000) };
        }
        return { allowed: true };
    }

    // First call should be allowed
    const first = checkCooldown('device1:0');
    check('First refresh allowed', first.allowed);

    // Set cooldown
    refreshCooldowns['device1:0'] = Date.now();

    // Second call should be blocked
    const second = checkCooldown('device1:0');
    check('Second refresh blocked (within cooldown)', !second.allowed);
    check('Remaining seconds > 0', second.remaining > 0);
    check('Remaining seconds <= 60', second.remaining <= 60);

    // Different entity should be allowed
    const other = checkCooldown('device1:1');
    check('Different entity not affected', other.allowed);

    // Expired cooldown (simulate)
    refreshCooldowns['device1:2'] = Date.now() - 61000;
    const expired = checkCooldown('device1:2');
    check('Expired cooldown allows refresh', expired.allowed);
}

console.log('');

// ── Refresh input validation ────────────────────────────────
console.log('=== Refresh Input Validation ===\n');

{
    function validateRefreshInput(body) {
        const { deviceId, deviceSecret, entityId } = body;
        if (!deviceId || !deviceSecret) return { valid: false, error: 'credentials required' };
        const eId = parseInt(entityId);
        if (isNaN(eId) || eId < 0 || eId >= 4) return { valid: false, error: 'invalid entityId' };
        return { valid: true, entityId: eId };
    }

    check('Valid input accepted', validateRefreshInput({ deviceId: 'a', deviceSecret: 'b', entityId: 0 }).valid);
    check('Missing deviceId rejected', !validateRefreshInput({ deviceSecret: 'b', entityId: 0 }).valid);
    check('Missing deviceSecret rejected', !validateRefreshInput({ deviceId: 'a', entityId: 0 }).valid);
    check('entityId -1 rejected', !validateRefreshInput({ deviceId: 'a', deviceSecret: 'b', entityId: -1 }).valid);
    check('entityId 4 rejected', !validateRefreshInput({ deviceId: 'a', deviceSecret: 'b', entityId: 4 }).valid);
    check('entityId "abc" rejected', !validateRefreshInput({ deviceId: 'a', deviceSecret: 'b', entityId: 'abc' }).valid);
    check('entityId 3 accepted (max valid)', validateRefreshInput({ deviceId: 'a', deviceSecret: 'b', entityId: 3 }).valid);
}

console.log('');

// ── Binding cache key logic ─────────────────────────────────
console.log('=== Binding Cache Reorder ===\n');

{
    function simulateBindingCacheReorder(cache, deviceId, order) {
        const maxSlots = 4;
        const oldBindings = {};
        for (let i = 0; i < maxSlots; i++) {
            const key = `${deviceId}:${i}`;
            if (cache[key]) oldBindings[i] = { ...cache[key] };
        }

        // Clear and rebuild
        for (let i = 0; i < maxSlots; i++) {
            delete cache[`${deviceId}:${i}`];
        }

        for (let newSlot = 0; newSlot < maxSlots; newSlot++) {
            const oldSlot = order[newSlot];
            if (oldBindings[oldSlot]) {
                const updated = { ...oldBindings[oldSlot], entity_id: newSlot };
                cache[`${deviceId}:${newSlot}`] = updated;
            }
        }
    }

    const cache = {
        'dev1:0': { bot_id: 'bot-A', entity_id: 0, session_key: 'sk-A' },
        'dev1:2': { bot_id: 'bot-B', entity_id: 2, session_key: 'sk-B' },
    };

    // Swap 0 and 2
    simulateBindingCacheReorder(cache, 'dev1', [2, 1, 0, 3]);

    check('Cache: bot-B now at slot 0', cache['dev1:0']?.bot_id === 'bot-B');
    check('Cache: bot-A now at slot 2', cache['dev1:2']?.bot_id === 'bot-A');
    check('Cache: entity_id updated to 0', cache['dev1:0']?.entity_id === 0);
    check('Cache: entity_id updated to 2', cache['dev1:2']?.entity_id === 2);
    check('Cache: session_key preserved for bot-B', cache['dev1:0']?.session_key === 'sk-B');
    check('Cache: session_key preserved for bot-A', cache['dev1:2']?.session_key === 'sk-A');
    check('Cache: slot 1 still empty', !cache['dev1:1']);
    check('Cache: slot 3 still empty', !cache['dev1:3']);
}

console.log('');

// ── Summary ─────────────────────────────────────────────────
console.log('==========================');
console.log(`Results: ${passed} passed, ${failed} failed out of ${passed + failed} tests`);
if (failed === 0) {
    console.log('✅ ALL TESTS PASSED');
} else {
    console.log(`❌ ${failed} TESTS FAILED`);
}

process.exit(failed > 0 ? 1 : 0);
