/**
 * Unit test for buildBroadcastRecipientBlock() output format.
 *
 * Since the function lives inside index.js (not exported), this test
 * replicates the exact logic and validates the output format spec.
 *
 * Usage: node backend/tests/test-broadcast-recipient-block.js
 */

// ── Replicate the helper function (must match index.js) ──
function buildBroadcastRecipientBlock(device, recipientIds, currentEntityId) {
    if (recipientIds.length === 0) return '';
    let block = `[BROADCAST RECIPIENTS] This message was sent to ${recipientIds.length} entities:\n`;
    for (const id of recipientIds) {
        const entity = device.entities[id];
        if (!entity) continue;
        const nameStr = entity.name ? ` "${entity.name}"` : '';
        const youMarker = id === currentEntityId ? ' \u2190 YOU' : '';
        block += `- entity_${id}${nameStr} (${entity.character || 'LOBSTER'})${youMarker}\n`;
    }
    return block + '\n';
}

// ── Test Helpers ──
let passed = 0;
let failed = 0;

function assert(condition, message) {
    if (condition) {
        passed++;
        console.log(`  ✓ ${message}`);
    } else {
        failed++;
        console.error(`  ✗ ${message}`);
    }
}

function assertEqual(actual, expected, message) {
    if (actual === expected) {
        passed++;
        console.log(`  ✓ ${message}`);
    } else {
        failed++;
        console.error(`  ✗ ${message}`);
        console.error(`    Expected: ${JSON.stringify(expected)}`);
        console.error(`    Actual:   ${JSON.stringify(actual)}`);
    }
}

// ── Tests ──
console.log('\n=== buildBroadcastRecipientBlock() Tests ===\n');

// Test 1: Empty recipients
console.log('Test 1: Empty recipients returns empty string');
{
    const device = { entities: {} };
    const result = buildBroadcastRecipientBlock(device, [], 0);
    assertEqual(result, '', 'Empty array returns empty string');
}

// Test 2: Two recipients with names, current entity marked
console.log('\nTest 2: Two recipients with names');
{
    const device = {
        entities: {
            1: { name: '小豬', character: 'PIG', isBound: true },
            2: { name: '大蝦', character: 'LOBSTER', isBound: true }
        }
    };
    const result = buildBroadcastRecipientBlock(device, [1, 2], 1);
    assert(result.includes('[BROADCAST RECIPIENTS] This message was sent to 2 entities:'), 'Has header with count');
    assert(result.includes('- entity_1 "小豬" (PIG) ← YOU'), 'Current entity marked with ← YOU');
    assert(result.includes('- entity_2 "大蝦" (LOBSTER)'), 'Other entity listed without marker');
    const youCount = (result.match(/← YOU/g) || []).length;
    assertEqual(youCount, 1, 'Only one entity has YOU marker');
}

// Test 3: Entity without name
console.log('\nTest 3: Entity without name');
{
    const device = {
        entities: {
            0: { name: null, character: 'LOBSTER', isBound: true },
            1: { name: '小豬', character: 'PIG', isBound: true }
        }
    };
    const result = buildBroadcastRecipientBlock(device, [0, 1], 0);
    assert(result.includes('- entity_0 (LOBSTER) ← YOU'), 'No-name entity: no quotes, just (CHARACTER)');
    assert(result.includes('- entity_1 "小豬" (PIG)'), 'Named entity has quotes');
}

// Test 4: Three recipients (max scenario with 4 entities minus sender)
console.log('\nTest 4: Three recipients (max broadcast scenario)');
{
    const device = {
        entities: {
            0: { name: 'Alpha', character: 'LOBSTER', isBound: true },
            1: { name: 'Beta', character: 'PIG', isBound: true },
            2: { name: null, character: 'LOBSTER', isBound: true }
        }
    };
    const result = buildBroadcastRecipientBlock(device, [0, 1, 2], 2);
    assert(result.includes('sent to 3 entities'), 'Header shows 3');
    assert(result.includes('entity_2 (LOBSTER) ← YOU'), 'Entity 2 (no name) marked as YOU');
    assert(result.includes('entity_0 "Alpha" (LOBSTER)'), 'Entity 0 listed');
    assert(result.includes('entity_1 "Beta" (PIG)'), 'Entity 1 listed');
}

// Test 5: Default character when missing
console.log('\nTest 5: Missing character defaults to LOBSTER');
{
    const device = {
        entities: {
            0: { name: 'Test', isBound: true }
        }
    };
    const result = buildBroadcastRecipientBlock(device, [0], 0);
    assert(result.includes('(LOBSTER)'), 'Falls back to LOBSTER');
}

// Test 6: Block ends with newline (for clean concatenation)
console.log('\nTest 6: Block ends with double newline');
{
    const device = {
        entities: {
            0: { name: 'X', character: 'PIG', isBound: true }
        }
    };
    const result = buildBroadcastRecipientBlock(device, [0], 0);
    assert(result.endsWith('\n\n'), 'Ends with double newline for clean concat');
}

// Test 7: Missing entity in device.entities is skipped
console.log('\nTest 7: Missing entity ID gracefully skipped');
{
    const device = {
        entities: {
            0: { name: 'Exists', character: 'LOBSTER', isBound: true }
            // entity 1 does not exist
        }
    };
    const result = buildBroadcastRecipientBlock(device, [0, 1], 0);
    assert(result.includes('sent to 2 entities'), 'Header counts all IDs');
    assert(result.includes('entity_0'), 'Existing entity listed');
    assert(!result.includes('entity_1'), 'Missing entity skipped');
}

// ── Summary ──
console.log(`\n${'='.repeat(40)}`);
console.log(`Results: ${passed} passed, ${failed} failed`);
console.log(`${'='.repeat(40)}\n`);

process.exit(failed > 0 ? 1 : 0);
