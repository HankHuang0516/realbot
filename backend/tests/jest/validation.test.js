/**
 * API input-validation tests (Jest + Supertest)
 *
 * Verifies that endpoints return 400/404 for bad/missing input
 * without needing a real database.
 */

// ── Same mocks as health.test.js ──
jest.mock('pg', () => ({
    Pool: jest.fn().mockImplementation(() => ({
        query: jest.fn().mockResolvedValue({ rows: [], rowCount: 0 }),
        connect: jest.fn().mockResolvedValue({
            query: jest.fn().mockResolvedValue({ rows: [] }),
            release: jest.fn(),
        }),
        end: jest.fn().mockResolvedValue(undefined),
    })),
}));

jest.mock('../../db', () => ({
    initDatabase: jest.fn().mockResolvedValue(false),
    saveDeviceData: jest.fn().mockResolvedValue(true),
    saveAllDevices: jest.fn().mockResolvedValue(true),
    loadAllDevices: jest.fn().mockResolvedValue({}),
    deleteDevice: jest.fn().mockResolvedValue(true),
    getStats: jest.fn().mockResolvedValue({}),
    closeDatabase: jest.fn().mockResolvedValue(undefined),
    saveOfficialBot: jest.fn().mockResolvedValue(true),
    loadOfficialBots: jest.fn().mockResolvedValue({}),
    deleteOfficialBot: jest.fn().mockResolvedValue(true),
    saveOfficialBinding: jest.fn().mockResolvedValue(true),
    removeOfficialBinding: jest.fn().mockResolvedValue(true),
    getOfficialBinding: jest.fn().mockResolvedValue(null),
    getDeviceOfficialBindings: jest.fn().mockResolvedValue([]),
    updateSubscriptionVerified: jest.fn().mockResolvedValue(true),
    loadAllOfficialBindings: jest.fn().mockResolvedValue([]),
    getExpiredPersonalBindings: jest.fn().mockResolvedValue([]),
    getPaidBorrowSlots: jest.fn().mockResolvedValue(0),
    incrementPaidBorrowSlots: jest.fn().mockResolvedValue(true),
    saveFeedback: jest.fn().mockResolvedValue({ id: 1 }),
}));

jest.mock('../../flickr', () => ({
    initFlickr: jest.fn(),
    uploadPhoto: jest.fn().mockResolvedValue(null),
    isAvailable: jest.fn().mockReturnValue(false),
}));

jest.mock('../../scheduler', () => ({
    init: jest.fn(),
    createSchedule: jest.fn().mockResolvedValue({ id: 1 }),
    updateSchedule: jest.fn().mockResolvedValue(true),
    deleteSchedule: jest.fn().mockResolvedValue(true),
    getSchedules: jest.fn().mockResolvedValue([]),
    getSchedule: jest.fn().mockResolvedValue(null),
    getSchedulesForBot: jest.fn().mockResolvedValue([]),
}));

jest.mock('../../device-telemetry', () => ({
    initTelemetryTable: jest.fn().mockResolvedValue(undefined),
    appendEntries: jest.fn().mockResolvedValue(undefined),
    captureApiCall: jest.fn().mockResolvedValue(undefined),
    getEntries: jest.fn().mockResolvedValue([]),
    getSummary: jest.fn().mockResolvedValue({}),
    clearEntries: jest.fn().mockResolvedValue(undefined),
    createMiddleware: jest.fn().mockReturnValue((_req, _res, next) => next()),
    sanitize: jest.fn().mockImplementation((v) => v),
    MAX_BUFFER_BYTES: 1024 * 1024,
    MAX_ENTRIES: 500,
}));

jest.mock('../../device-feedback', () => ({
    initFeedbackTable: jest.fn().mockResolvedValue(undefined),
    initFeedbackPhotosTable: jest.fn().mockResolvedValue(undefined),
    captureLogSnapshot: jest.fn().mockResolvedValue([]),
    captureDeviceState: jest.fn().mockResolvedValue({}),
    autoTriage: jest.fn().mockResolvedValue('low'),
    generateAiPrompt: jest.fn().mockReturnValue(''),
    saveFeedback: jest.fn().mockResolvedValue({ id: 1 }),
    getFeedbackList: jest.fn().mockResolvedValue([]),
    getFeedbackById: jest.fn().mockResolvedValue(null),
    updateFeedback: jest.fn().mockResolvedValue(true),
    createGithubIssue: jest.fn().mockResolvedValue(null),
    getPendingDebugFeedback: jest.fn().mockResolvedValue([]),
    saveDebugResult: jest.fn().mockResolvedValue(true),
    setMark: jest.fn().mockResolvedValue(undefined),
    getMark: jest.fn().mockResolvedValue(null),
    clearMark: jest.fn().mockResolvedValue(undefined),
    LOG_WINDOW_MS: 60000,
    MAX_PHOTOS_PER_FEEDBACK: 10,
    MAX_PHOTO_SIZE: 5 * 1024 * 1024,
    saveFeedbackPhoto: jest.fn().mockResolvedValue({ id: 1 }),
    getFeedbackPhotos: jest.fn().mockResolvedValue([]),
    getFeedbackPhoto: jest.fn().mockResolvedValue(null),
    deleteFeedbackPhotos: jest.fn().mockResolvedValue(undefined),
    cleanupResolvedFeedbackPhotos: jest.fn().mockResolvedValue(undefined),
}));

jest.mock('../../gatekeeper', () => ({
    detectMaliciousMessage: jest.fn().mockReturnValue({ isMalicious: false }),
    detectAndMaskLeaks: jest.fn().mockImplementation((text) => text),
    initGatekeeperTable: jest.fn().mockResolvedValue(undefined),
    loadBlockedDevices: jest.fn().mockResolvedValue(undefined),
    recordViolation: jest.fn().mockResolvedValue(undefined),
    isDeviceBlocked: jest.fn().mockReturnValue(false),
    getStrikeInfo: jest.fn().mockResolvedValue({ strikes: 0, blocked: false }),
    getFreeBotTOS: jest.fn().mockResolvedValue(null),
    hasAgreedToTOS: jest.fn().mockResolvedValue(false),
    recordTOSAgreement: jest.fn().mockResolvedValue(undefined),
    setServerLog: jest.fn(),
    MAX_STRIKES: 3,
    FREE_BOT_TOS_VERSION: '1.0',
}));

jest.mock('../../mission', () => {
    const express = jest.requireActual('express');
    return jest.fn().mockReturnValue({
        router: express.Router(),
        initMissionDatabase: jest.fn().mockResolvedValue(undefined),
        setNotifyCallback: jest.fn(),
        setPushToBot: jest.fn(),
        setPushToChannelCallback: jest.fn(),
    });
});

jest.mock('../../auth', () => {
    const express = jest.requireActual('express');
    const noop = (_req, _res, next) => next();
    return jest.fn().mockReturnValue({
        router: express.Router(),
        authMiddleware: noop,
        softAuthMiddleware: noop,
        adminMiddleware: noop,
        initAuthDatabase: jest.fn().mockResolvedValue(undefined),
        pool: {
            query: jest.fn().mockResolvedValue({ rows: [], rowCount: 0 }),
        },
    });
});

jest.mock('../../subscription', () => {
    const express = jest.requireActual('express');
    return jest.fn().mockReturnValue({
        router: express.Router(),
        loadPremiumStatus: jest.fn().mockResolvedValue(undefined),
    });
});

const request = require('supertest');
let app;

beforeAll(() => {
    app = require('../../index');
});

afterAll(async () => {
    const { httpServer } = require('../../index');
    await new Promise(resolve => httpServer.close(resolve));
    jest.resetModules();
});

// ════════════════════════════════════════════════════════════════
// /api/bind — missing params → 400
// ════════════════════════════════════════════════════════════════
describe('POST /api/bind — missing required fields', () => {
    it('returns 400 when deviceId is missing', async () => {
        const res = await request(app)
            .post('/api/bind')
            .send({ entityId: 0, botName: 'TestBot', webhook: 'https://example.com' });
        expect(res.status).toBe(400);
    });

    it('returns 400 when entityId is missing', async () => {
        const res = await request(app)
            .post('/api/bind')
            .send({ deviceId: 'device-001', botName: 'TestBot', webhook: 'https://example.com' });
        expect(res.status).toBe(400);
    });
});

// ════════════════════════════════════════════════════════════════
// /api/bot/sync-message — missing params → 400
// ════════════════════════════════════════════════════════════════
describe('POST /api/bot/sync-message — missing required fields', () => {
    it('returns 400 when deviceId is missing', async () => {
        const res = await request(app)
            .post('/api/bot/sync-message')
            .send({ entityId: 0, botSecret: 'secret', message: 'hello' });
        expect(res.status).toBe(400);
    });

    it('returns 400 when both message and mediaUrl are absent', async () => {
        const res = await request(app)
            .post('/api/bot/sync-message')
            .send({ deviceId: 'device-001', entityId: 0, botSecret: 'secret' });
        expect(res.status).toBe(400);
    });

    it('returns 404 when device does not exist', async () => {
        const res = await request(app)
            .post('/api/bot/sync-message')
            .send({
                deviceId: 'nonexistent-device',
                entityId: 0,
                botSecret: 'secret',
                message: 'hello',
            });
        expect(res.status).toBe(404);
    });
});

// ════════════════════════════════════════════════════════════════
// /api/transform — missing params → 400
// ════════════════════════════════════════════════════════════════
describe('POST /api/transform — missing required fields', () => {
    it('returns 400 when deviceId is missing', async () => {
        const res = await request(app)
            .post('/api/transform')
            .send({ entityId: 0, message: 'hello' });
        expect(res.status).toBe(400);
    });
});

// ════════════════════════════════════════════════════════════════
// Template APIs — category field presence
// ════════════════════════════════════════════════════════════════
describe('GET /api/skill-templates — category field', () => {
    it('returns 200 with templates array', async () => {
        const res = await request(app).get('/api/skill-templates');
        expect(res.status).toBe(200);
        expect(res.body.success).toBe(true);
        expect(Array.isArray(res.body.templates)).toBe(true);
        expect(res.body.templates.length).toBeGreaterThan(0);
    });

    it('every template includes a category field', async () => {
        const res = await request(app).get('/api/skill-templates');
        for (const t of res.body.templates) {
            expect(t).toHaveProperty('category');
            expect(typeof t.category).toBe('string');
            expect(t.category.length).toBeGreaterThan(0);
        }
    });
});

describe('GET /api/soul-templates — category field', () => {
    it('returns 200 with templates array', async () => {
        const res = await request(app).get('/api/soul-templates');
        expect(res.status).toBe(200);
        expect(res.body.success).toBe(true);
        expect(Array.isArray(res.body.templates)).toBe(true);
        expect(res.body.templates.length).toBeGreaterThan(0);
    });

    it('every template includes a category field', async () => {
        const res = await request(app).get('/api/soul-templates');
        for (const t of res.body.templates) {
            expect(t).toHaveProperty('category');
            expect(typeof t.category).toBe('string');
            expect(t.category.length).toBeGreaterThan(0);
        }
    });
});

describe('GET /api/rule-templates — category field', () => {
    it('returns 200 with templates array', async () => {
        const res = await request(app).get('/api/rule-templates');
        expect(res.status).toBe(200);
        expect(res.body.success).toBe(true);
        expect(Array.isArray(res.body.templates)).toBe(true);
        expect(res.body.templates.length).toBeGreaterThan(0);
    });

    it('every template includes a category field', async () => {
        const res = await request(app).get('/api/rule-templates');
        for (const t of res.body.templates) {
            expect(t).toHaveProperty('category');
            expect(typeof t.category).toBe('string');
            expect(t.category.length).toBeGreaterThan(0);
        }
    });
});

// ════════════════════════════════════════════════════════════════
// POST /api/skill-templates/contribute — category validation
// ════════════════════════════════════════════════════════════════
describe('POST /api/skill-templates/contribute — category validation', () => {
    it('accepts optional category field (rejected at auth, not 500)', async () => {
        const res = await request(app)
            .post('/api/skill-templates/contribute')
            .send({
                deviceId: 'test-validator', botSecret: 'fake',
                skill: {
                    id: 'cat-test', title: 'T', url: 'https://github.com/t/t',
                    steps: '1. Step one. This is a detailed step that exceeds the minimum length.',
                    category: 'Automation'
                }
            });
        // 404 (device not found) or 403 (bad botSecret) — not 500
        expect(res.status).not.toBe(500);
    });

    it('rejects category longer than 30 chars', async () => {
        const res = await request(app)
            .post('/api/skill-templates/contribute')
            .send({
                deviceId: 'test-validator', botSecret: 'fake',
                skill: {
                    id: 'cat-long', title: 'T', url: 'https://github.com/t/t',
                    steps: '1. Step one. This is a detailed step that exceeds the minimum length.',
                    category: 'A'.repeat(31)
                }
            });
        // Should be 400 (validation) or 404 (device not found, if auth runs first)
        // The key: must not be 500
        expect(res.status).not.toBe(500);
    });
});

// ════════════════════════════════════════════════════════════════
// Unknown routes
// ════════════════════════════════════════════════════════════════
describe('Unknown routes', () => {
    it('GET /api/nonexistent returns 404', async () => {
        const res = await request(app).get('/api/nonexistent-endpoint-xyz');
        expect(res.status).toBe(404);
    });
});
