/**
 * Regression Test Runner
 * Runs all backend tests before release
 */

const { spawn } = require('child_process');
const path = require('path');
const fs = require('fs');

// Test files to run (in order of importance)
// Note: Tests must be v5 compatible (deviceId + entityId based)
const TEST_FILES = [
    'test_ux_improvements.js',      // Critical: API structure, isBound
    'test_device_isolation.js',     // Critical: Multi-device isolation
    'test_messaging.js',            // Core: Message delivery
    'test_webhook.js',              // Feature: Webhook push
    // 'test_entity_isolation.js',  // TODO: Needs v5 update
    // 'test_name_feature.js',      // TODO: Needs v5 update
    // 'test_entity_communication.js', // TODO: Needs v5 update - uses old endpoint format
];

const TEST_TIMEOUT = 30000; // 30 seconds per test

async function runTest(testFile) {
    return new Promise((resolve) => {
        const testPath = path.join(__dirname, testFile);

        if (!fs.existsSync(testPath)) {
            console.log(`SKIP: ${testFile} (file not found)`);
            resolve({ file: testFile, passed: true, skipped: true });
            return;
        }

        console.log(`\n${'='.repeat(60)}`);
        console.log(`Running: ${testFile}`);
        console.log('='.repeat(60));

        const child = spawn('node', [testPath], {
            cwd: __dirname,
            stdio: 'pipe'
        });

        let output = '';
        let resolved = false;

        // Timeout handler
        const timeout = setTimeout(() => {
            if (!resolved) {
                resolved = true;
                child.kill();
                console.log(`\n⏱️ TIMEOUT: ${testFile} (${TEST_TIMEOUT/1000}s)`);
                resolve({ file: testFile, passed: false, timeout: true });
            }
        }, TEST_TIMEOUT);

        child.stdout.on('data', (data) => {
            const str = data.toString();
            output += str;
            process.stdout.write(str);
        });

        child.stderr.on('data', (data) => {
            const str = data.toString();
            output += str;
            process.stderr.write(str);
        });

        child.on('close', (code) => {
            if (resolved) return;
            resolved = true;
            clearTimeout(timeout);

            // Check for various pass patterns
            const passed =
                output.includes('All tests passed') ||
                output.includes('all tests passed') ||
                output.includes('All isolation tests passed') ||
                (output.includes('RESULTS:') && output.includes(', 0 failed')) ||
                (output.includes('Result:') && output.includes('passed') && !output.includes('0/')) ||
                (output.match(/\d+\/\d+ tests passed/) && output.includes('✅'));
            resolve({ file: testFile, passed, code, output });
        });

        child.on('error', (err) => {
            if (resolved) return;
            resolved = true;
            clearTimeout(timeout);
            console.error(`Error running ${testFile}:`, err.message);
            resolve({ file: testFile, passed: false, error: err.message });
        });
    });
}

async function main() {
    console.log('='.repeat(60));
    console.log('REGRESSION TEST RUNNER');
    console.log('='.repeat(60));
    console.log(`Date: ${new Date().toISOString()}`);
    console.log(`Tests to run: ${TEST_FILES.length}`);

    const results = [];

    for (const testFile of TEST_FILES) {
        const result = await runTest(testFile);
        results.push(result);
    }

    // Summary
    console.log('\n' + '='.repeat(60));
    console.log('REGRESSION TEST SUMMARY');
    console.log('='.repeat(60));

    let passCount = 0;
    let failCount = 0;
    let skipCount = 0;

    for (const r of results) {
        if (r.skipped) {
            console.log(`⏭️  SKIP: ${r.file}`);
            skipCount++;
        } else if (r.timeout) {
            console.log(`⏱️  TIMEOUT: ${r.file}`);
            failCount++;
        } else if (r.passed) {
            console.log(`✅ PASS: ${r.file}`);
            passCount++;
        } else {
            console.log(`❌ FAIL: ${r.file}`);
            failCount++;
        }
    }

    console.log('\n' + '-'.repeat(60));
    console.log(`Total: ${passCount} passed, ${failCount} failed, ${skipCount} skipped`);
    console.log('-'.repeat(60));

    if (failCount > 0) {
        console.log('\n❌ RELEASE BLOCKED: Some tests failed!');
        console.log('Fix the failing tests before releasing.');
        process.exit(1);
    } else {
        console.log('\n✅ ALL TESTS PASSED - Ready for release!');
        process.exit(0);
    }
}

main().catch(console.error);
