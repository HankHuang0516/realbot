/**
 * Rich Message Templates validation tests (Jest + Supertest)
 *
 * Tests richContent validation, limits, and endpoint acceptance
 * for POST /api/transform and POST /api/client/speak (#258).
 */

// ── Mock all modules with side-effects before requiring index.js ──
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
    getChannelAccountByKey: jest.fn().mockResolvedValue(null),
    getChannelAccountById: jest.fn().mockResolvedValue(null),
    getChannelAccountByDevice: jest.fn().mockResolvedValue(null),
    getChannelAccountsByDevice: jest.fn().mockResolvedValue([]),
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
// POST /api/transform — richContent acceptance
// ════════════════════════════════════════════════════════════════
describe('POST /api/transform — richContent field', () => {
    it('returns 400 when deviceId is missing (baseline)', async () => {
        const res = await request(app)
            .post('/api/transform')
            .send({ entityId: 0, message: 'hello', richContent: { embeds: [{ title: 'Test' }] } });
        expect(res.status).toBe(400);
    });

    it('does not reject richContent field (nonexistent device → 404)', async () => {
        const res = await request(app)
            .post('/api/transform')
            .send({
                deviceId: 'rc-test-device-nonexistent',
                entityId: 0,
                botSecret: 'secret',
                message: 'hello',
                richContent: {
                    quickReplies: [
                        { label: 'Option A', value: 'a' },
                        { label: 'Option B', value: 'b' },
                    ],
                    buttons: [
                        { label: 'Open', action: 'url', value: 'https://example.com' },
                        { label: 'Done', action: 'callback', value: 'done_123' },
                    ],
                    embeds: [{
                        title: 'Status Report',
                        description: 'All tasks completed',
                        color: '#4CAF50',
                        fields: [
                            { name: 'Done', value: '5', inline: true },
                            { name: 'Pending', value: '0', inline: true },
                        ],
                    }],
                },
            });
        // richContent doesn't cause a 400 validation error
        expect(res.status).not.toBe(400);
    });

    it('accepts richContent with only embeds (no 400)', async () => {
        const res = await request(app)
            .post('/api/transform')
            .send({
                deviceId: 'rc-test-device-nonexistent',
                entityId: 0,
                botSecret: 'secret',
                message: '',
                richContent: { embeds: [{ title: 'Test', description: 'Hello' }] },
            });
        expect(res.status).not.toBe(400);
    });

    it('accepts richContent with only buttons (no 400)', async () => {
        const res = await request(app)
            .post('/api/transform')
            .send({
                deviceId: 'rc-test-device-nonexistent',
                entityId: 0,
                botSecret: 'secret',
                message: 'Choose:',
                richContent: {
                    buttons: [
                        { label: 'Yes', action: 'callback', value: 'yes' },
                        { label: 'No', action: 'callback', value: 'no' },
                    ],
                },
            });
        expect(res.status).not.toBe(400);
    });

    it('accepts richContent with only quickReplies (no 400)', async () => {
        const res = await request(app)
            .post('/api/transform')
            .send({
                deviceId: 'rc-test-device-nonexistent',
                entityId: 0,
                botSecret: 'secret',
                message: 'Pick one:',
                richContent: {
                    quickReplies: [
                        { label: 'A', value: 'a' },
                        { label: 'B', value: 'b' },
                        { label: 'C', value: 'c' },
                    ],
                },
            });
        expect(res.status).not.toBe(400);
    });
});

// ════════════════════════════════════════════════════════════════
// POST /api/client/speak — richContent acceptance
// ════════════════════════════════════════════════════════════════
describe('POST /api/client/speak — richContent field', () => {
    it('returns 400 when deviceId is missing', async () => {
        const res = await request(app)
            .post('/api/client/speak')
            .send({ text: 'hello', richContent: { buttons: [{ label: 'X' }] } });
        expect(res.status).toBe(400);
    });

    it('accepts richContent alongside text (not rejected as 400)', async () => {
        const res = await request(app)
            .post('/api/client/speak')
            .send({
                deviceId: 'rc-test-speak-nonexistent',
                deviceSecret: 'secret',
                entityId: 0,
                text: 'Choose a service:',
                source: 'test',
                richContent: {
                    quickReplies: [
                        { label: 'Balance', value: 'balance' },
                        { label: 'Support', value: 'support' },
                    ],
                },
            });
        // richContent field should not cause a validation error
        expect(res.status).not.toBe(400);
    });
});

// ════════════════════════════════════════════════════════════════
// POST /api/channel/button-callback — validation
// ════════════════════════════════════════════════════════════════
describe('POST /api/channel/button-callback — validation', () => {
    it('returns 400 when callbackId is missing', async () => {
        const res = await request(app)
            .post('/api/channel/button-callback')
            .send({ channel_api_key: 'key', deviceId: 'dev', entityId: 0 });
        expect(res.status).toBe(400);
        expect(res.body.success).toBe(false);
    });

    it('returns 400 when channel_api_key is missing', async () => {
        const res = await request(app)
            .post('/api/channel/button-callback')
            .send({ deviceId: 'dev', entityId: 0, callbackId: 'cb_123' });
        expect(res.status).toBe(400);
    });

    it('returns 403 for invalid channel API key', async () => {
        const res = await request(app)
            .post('/api/channel/button-callback')
            .send({ channel_api_key: 'invalid', deviceId: 'dev', entityId: 0, callbackId: 'cb_123' });
        expect(res.status).toBe(403);
    });
});

// ════════════════════════════════════════════════════════════════
// POST /api/channel/message — richContent acceptance
// ════════════════════════════════════════════════════════════════
describe('POST /api/channel/message — richContent field', () => {
    it('returns 400 when required fields are missing', async () => {
        const res = await request(app)
            .post('/api/channel/message')
            .send({ message: 'hello', richContent: { embeds: [{ title: 'X' }] } });
        expect(res.status).toBe(400);
    });

    it('returns 403 for invalid channel API key (richContent passthrough)', async () => {
        const res = await request(app)
            .post('/api/channel/message')
            .send({
                channel_api_key: 'invalid',
                deviceId: 'dev',
                entityId: 0,
                botSecret: 'secret',
                message: 'hello',
                richContent: {
                    embeds: [{ title: 'Report', description: 'Done', color: '#00FF00' }],
                    buttons: [{ label: 'View', action: 'url', value: 'https://example.com' }],
                },
            });
        expect(res.status).toBe(403);
    });
});
