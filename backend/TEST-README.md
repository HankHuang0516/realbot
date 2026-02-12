# Backend Stress Tests

This directory contains comprehensive stress tests for validating Bug #1 and Bug #2 fixes.

## Bug #1: Server Crash Prevention

**Problem**: Malformed API requests (e.g., wrong field names like `from`/`to` instead of `fromEntityId`/`toEntityId`) caused server crashes with `TypeError: Cannot read properties of undefined`.

**Fix**: Added validation checks in all endpoints to verify entities exist before accessing their properties.

**Tests**: `stress-test.js` sends hundreds of malformed requests to ensure the server handles them gracefully without crashing.

## Bug #2: Data Persistence Across Redeployment

**Problem**: Railway redeployment wiped all data stored in memory (`const devices = {}`), causing all entities to disconnect.

**Fix**: Implemented file-based persistence:
- Data saved to `backend/data/devices.json`
- Auto-saves every 30 seconds
- Saves immediately after critical operations (bind, unbind, webhook registration)
- Loads on startup
- Graceful shutdown handling (SIGINT, SIGTERM)

**Tests**: `test-persistence.js` validates data survives across server restarts.

## Running Tests

### 1. Stress Test (Bug #1 + Stability)

Tests that malformed requests don't crash the server and entities stay bound.

```bash
# Run against local server
cd backend
node stress-test.js

# Run against Railway production
API_BASE=https://eclaw.up.railway.app node stress-test.js

# Verbose mode (shows all requests)
VERBOSE=true node stress-test.js
```

**What it tests:**
- Malformed entity-to-entity speak requests (wrong field names, invalid types, out-of-range values)
- Malformed transform requests (missing fields, wrong types)
- Random garbage requests (50+ requests with random data)
- Entity remains bound after stress test
- Server health before and after
- Data persistence check

**Success criteria:**
- All malformed requests return 4xx errors (400/403/404), NOT 500 or crash
- Entity remains bound after stress test
- Server remains healthy

### 2. Persistence Test (Bug #2)

Tests that data survives server restarts (simulating Railway redeployment).

```bash
# Step 1: Setup - Create test entity
cd backend
node test-persistence.js

# Step 2: Restart the server (or redeploy to Railway)
# Ctrl+C and restart, or push to Railway

# Step 3: Check - Verify entity still exists
node test-persistence.js --check
```

**What it tests:**
- Entity data persists after server restart
- Binding state survives
- Bot credentials remain valid

**Success criteria:**
- Entity found after restart
- isBound = true
- All entity data intact

## Test Results

### Expected Output (Success)

```
?î‚??ê‚??ê‚??ê‚??ê‚??ê‚??ê‚??ê‚??ê‚??ê‚??ê‚??ê‚??ê‚??ê‚??ê‚??ê‚??ê‚??ê‚??ê‚??ê‚??ê‚??ê‚??ê‚??ê‚??ê‚??ê‚??ê‚??ê‚??ê‚??ê‚??ê‚?
?? Claw Backend Stress Test - Bug #1 & Bug #2 Validation  ???ö‚??ê‚??ê‚??ê‚??ê‚??ê‚??ê‚??ê‚??ê‚??ê‚??ê‚??ê‚??ê‚??ê‚??ê‚??ê‚??ê‚??ê‚??ê‚??ê‚??ê‚??ê‚??ê‚??ê‚??ê‚??ê‚??ê‚??ê‚??ê‚??ê‚??ê‚?

[Test] Health Check
??Health check passed

[Setup] Creating test entity
??Test entity created: test-device-abc12345 / Entity 0

[Bug #1 Test] Malformed entity-to-entity speak requests
Malformed requests: 9 handled correctly, 0 caused issues

[Bug #1 Test] Malformed transform requests
Malformed requests: 8 handled correctly, 0 caused issues

[Bug #1 Test] Random garbage requests (stress test)
Random garbage requests: 50/50 handled correctly

[Test] Entity still bound after stress test
??Entity still bound after stress test

[Bug #2 Test] Data persistence check
??Entity persists after server operations

[Final] Health check after stress test
??Health check passed

?î‚??ê‚??ê‚??ê‚??ê‚??ê‚??ê‚??ê‚??ê‚??ê‚??ê‚??ê‚??ê‚??ê‚??ê‚??ê‚??ê‚??ê‚??ê‚??ê‚??ê‚??ê‚??ê‚??ê‚??ê‚??ê‚??ê‚??ê‚??ê‚??ê‚??ê‚?
??                     TEST SUMMARY                         ???ö‚??ê‚??ê‚??ê‚??ê‚??ê‚??ê‚??ê‚??ê‚??ê‚??ê‚??ê‚??ê‚??ê‚??ê‚??ê‚??ê‚??ê‚??ê‚??ê‚??ê‚??ê‚??ê‚??ê‚??ê‚??ê‚??ê‚??ê‚??ê‚??ê‚??ê‚?

Total Tests: 72
Passed: 72
Failed: 0

Success Rate: 100.00%

??ALL TESTS PASSED - Backend is stable! ??
```

## Railway Volume Setup (for Bug #2 fix)

To enable data persistence on Railway:

1. Go to your Railway project
2. Click on your backend service
3. Go to "Settings" tab
4. Scroll to "Volumes" section
5. Click "New Volume"
6. Set mount path: `/app/backend/data`
7. Set size: 1GB (minimum)
8. Click "Add"

The `backend/data` directory will now persist across deployments.

## Continuous Testing

Add to your deployment workflow:

```bash
# After deployment
API_BASE=https://eclaw.up.railway.app node backend/stress-test.js

# If tests fail, rollback deployment
```

## Test Coverage

- ??Malformed entity-to-entity speak (9 test cases)
- ??Malformed transform requests (8 test cases)
- ??Random garbage requests (50 test cases)
- ??Entity binding stability
- ??Server health monitoring
- ??Data persistence across restarts
- ??Graceful error handling

## Troubleshooting

### Tests fail with "Connection refused"
- Server is not running
- Check API_BASE URL is correct

### Persistence test fails after restart
- Railway Volume not configured
- Check `backend/data/devices.json` exists
- Check file permissions

### Random failures
- Network issues
- Server overloaded
- Check server logs for details
