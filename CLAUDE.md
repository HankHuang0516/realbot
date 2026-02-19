# Claude Code Instructions

## Debugging Server Issues

When investigating backend bugs (broadcast failure, push not delivered, etc.):

1. **Query server logs FIRST** via `GET /api/logs`
   - Requires `deviceId` + `deviceSecret` (ask user if not available)
   - Filters: `category`, `level` (info/warn/error), `since` (timestamp ms), `filterDevice`, `limit`
   - Categories: `bind`, `unbind`, `transform`, `broadcast`, `broadcast_push`, `speakto_push`, `client_push`
   - Example: `curl -s "https://eclaw.up.railway.app/api/logs?deviceId=DEVICE_ID&deviceSecret=DEVICE_SECRET&category=broadcast_push&limit=50"`

2. **Check credentials**: Look in `backend/.env` (local only, gitignored). If not available, ask user for a valid deviceId+deviceSecret pair.

## Regression Tests

- **Bot API response rate**: `node backend/tests/test-bot-api-response.js`
  - Verifies bots call POST /api/transform via exec+curl (target: 90%+)
  - Requires `TEST_DEVICE_ID` in `backend/.env` or `--device` flag
- **Broadcast flow**: `node backend/tests/test-broadcast.js`
  - Tests broadcast delivery, delivered_to tracking, speak-to, chat history
  - Requires `BROADCAST_TEST_DEVICE_ID` + `BROADCAST_TEST_DEVICE_SECRET` in `backend/.env`

## Deployment

- Railway auto-deploys from `backend/` folder on push to main
- `railway.json` sets `startCommand: "node index.js"` (root dir = `backend/`)
- Changes to root files do NOT trigger deployment — must change files under `backend/`

## Key Architecture

- Backend: `backend/index.js` — Express server on Railway
- API Base: `https://eclaw.up.railway.app`
- 4 entity slots per device (0-3), each independently bindable
- Bots use OpenClaw platform (Zeabur), communicate via webhook push + exec+curl
- Push notifications use instruction-first format with pre-filled curl templates
