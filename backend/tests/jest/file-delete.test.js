/**
 * Jest test: DELETE /api/device/files/:fileId
 * Validates file delete endpoint input validation and auth
 */
const request = require('supertest');
const app = require('../../index');

describe('DELETE /api/device/files/:fileId', () => {
    const FAKE_FILE_ID = '00000000-0000-0000-0000-000000000000';

    // Warm up the app (wait for init to settle)
    beforeAll(async () => {
        await request(app).get('/api/health');
    });

    it('returns 400 when credentials are missing', async () => {
        const res = await request(app).delete(`/api/device/files/${FAKE_FILE_ID}`);
        expect(res.status).toBe(400);
        expect(res.body.success).toBe(false);
        expect(res.body.error).toMatch(/Missing credentials/i);
    });

    it('returns 401 when deviceSecret is wrong', async () => {
        const res = await request(app)
            .delete(`/api/device/files/${FAKE_FILE_ID}?deviceId=nonexistent&deviceSecret=wrong`);
        expect(res.status).toBe(401);
        expect(res.body.success).toBe(false);
    });
});
