const express = require('express');
const cors = require('cors');
const jwt = require('jsonwebtoken');
const crypto = require('crypto');
require('dotenv').config();

const app = express();
const PORT = process.env.PORT || 3000;
const JWT_SECRET = process.env.JWT_SECRET || crypto.randomBytes(32).toString('hex');
const WEBHOOK_SECRET = process.env.WEBHOOK_SECRET || crypto.randomBytes(32).toString('hex');

// ==================== In-Memory Storage ====================
// In production, use Redis or PostgreSQL

// Registered devices: { deviceId: { secret, bindingCode, codeExpiry, token, status, webhookUrl } }
const devices = new Map();

// Pending binding codes: { code: { deviceId, expiry } }
const pendingBindings = new Map();

// ==================== Middleware ====================

app.use(cors());
app.use(express.json());

// JWT Authentication middleware
const authenticateToken = (req, res, next) => {
  const authHeader = req.headers['authorization'];
  const token = authHeader && authHeader.split(' ')[1]; // Bearer TOKEN

  if (!token) {
    return res.status(401).json({ error: 'Access token required' });
  }

  jwt.verify(token, JWT_SECRET, (err, decoded) => {
    if (err) {
      return res.status(403).json({ error: 'Invalid or expired token' });
    }
    req.deviceId = decoded.deviceId;
    req.device = devices.get(decoded.deviceId);
    if (!req.device) {
      return res.status(403).json({ error: 'Device not found' });
    }
    next();
  });
};

// Webhook signature verification
const verifyWebhookSignature = (req, res, next) => {
  const signature = req.headers['x-webhook-signature'];
  const timestamp = req.headers['x-webhook-timestamp'];

  if (!signature || !timestamp) {
    return res.status(401).json({ error: 'Missing webhook signature' });
  }

  // Check timestamp is within 5 minutes
  const timestampAge = Date.now() - parseInt(timestamp);
  if (timestampAge > 5 * 60 * 1000) {
    return res.status(401).json({ error: 'Webhook timestamp too old' });
  }

  const payload = timestamp + '.' + JSON.stringify(req.body);
  const expectedSignature = crypto
    .createHmac('sha256', WEBHOOK_SECRET)
    .update(payload)
    .digest('hex');

  if (signature !== expectedSignature) {
    return res.status(401).json({ error: 'Invalid webhook signature' });
  }

  next();
};

// ==================== Helper Functions ====================

function generateBindingCode() {
  return Math.floor(100000 + Math.random() * 900000).toString();
}

function cleanupExpiredBindings() {
  const now = Date.now();
  for (const [code, binding] of pendingBindings.entries()) {
    if (binding.expiry < now) {
      pendingBindings.delete(code);
    }
  }
}

// Run cleanup every minute
setInterval(cleanupExpiredBindings, 60 * 1000);

function log(message, data = null) {
  const timestamp = new Date().toISOString();
  if (data) {
    console.log(`[${timestamp}] ${message}`, data);
  } else {
    console.log(`[${timestamp}] ${message}`);
  }
}

// ==================== Public Routes ====================

// Health check
app.get('/', (req, res) => {
  res.json({
    status: 'ok',
    message: 'Realbot Backend is running',
    version: '2.0.0',
    timestamp: new Date().toISOString()
  });
});

app.get('/api/health', (req, res) => {
  res.json({ status: 'healthy', uptime: process.uptime() });
});

// ==================== Device Registration ====================

// Step 1: Device registers and gets a binding code
// Called by Android app
app.post('/api/device/register', (req, res) => {
  const { deviceId, deviceSecret } = req.body;

  if (!deviceId || !deviceSecret) {
    return res.status(400).json({ error: 'deviceId and deviceSecret required' });
  }

  // Generate 6-digit binding code
  let bindingCode;
  do {
    bindingCode = generateBindingCode();
  } while (pendingBindings.has(bindingCode));

  const codeExpiry = Date.now() + 5 * 60 * 1000; // 5 minutes

  // Store or update device
  const existingDevice = devices.get(deviceId);
  const device = {
    secret: deviceSecret,
    bindingCode: bindingCode,
    codeExpiry: codeExpiry,
    token: existingDevice?.token || null,
    webhookUrl: existingDevice?.webhookUrl || null,
    webhookSecret: existingDevice?.webhookSecret || null,
    status: {
      character: 'LOBSTER',
      state: 'IDLE',
      message: 'Waiting for binding...',
      batteryLevel: 100,
      lastUpdated: Date.now()
    }
  };

  devices.set(deviceId, device);
  pendingBindings.set(bindingCode, { deviceId, expiry: codeExpiry });

  log(`Device registered: ${deviceId}, binding code: ${bindingCode}`);

  res.json({
    success: true,
    bindingCode: bindingCode,
    expiresIn: 300 // seconds
  });
});

// Step 2: OpenClaw binds using the code
// Called by OpenClaw
app.post('/api/bind', (req, res) => {
  const { code, webhookUrl } = req.body;

  if (!code) {
    return res.status(400).json({ error: 'Binding code required' });
  }

  cleanupExpiredBindings();

  const binding = pendingBindings.get(code);
  if (!binding) {
    return res.status(400).json({ error: 'Invalid or expired binding code' });
  }

  const device = devices.get(binding.deviceId);
  if (!device) {
    return res.status(400).json({ error: 'Device not found' });
  }

  // Generate JWT token for OpenClaw
  const token = jwt.sign(
    { deviceId: binding.deviceId, type: 'openclaw' },
    JWT_SECRET,
    { expiresIn: '30d' }
  );

  // Generate webhook secret for this binding
  const deviceWebhookSecret = crypto.randomBytes(16).toString('hex');

  // Update device
  device.token = token;
  device.webhookUrl = webhookUrl || null;
  device.webhookSecret = deviceWebhookSecret;
  device.status.state = 'IDLE';
  device.status.message = 'Connected to OpenClaw!';
  device.status.lastUpdated = Date.now();

  // Remove used binding code
  pendingBindings.delete(code);

  log(`Device bound: ${binding.deviceId}`);

  res.json({
    success: true,
    token: token,
    webhookSecret: deviceWebhookSecret,
    deviceId: binding.deviceId,
    skills: getMCPSkills()
  });
});

// ==================== Authenticated Routes (for OpenClaw) ====================

// Get device status
app.get('/api/status', authenticateToken, (req, res) => {
  const status = req.device.status;
  log(`GET /api/status for device ${req.deviceId}`);
  res.json(status);
});

// Update device status
app.put('/api/status', authenticateToken, (req, res) => {
  const { character, state, message, batteryLevel } = req.body;

  // Validate character
  if (character && !['LOBSTER', 'PIG'].includes(character)) {
    return res.status(400).json({ error: 'Invalid character. Must be LOBSTER or PIG' });
  }

  // Validate state
  const validStates = ['IDLE', 'BUSY', 'EATING', 'SLEEPING', 'EXCITED'];
  if (state && !validStates.includes(state)) {
    return res.status(400).json({ error: `Invalid state. Must be one of: ${validStates.join(', ')}` });
  }

  // Validate batteryLevel
  if (batteryLevel !== undefined && (batteryLevel < 0 || batteryLevel > 100)) {
    return res.status(400).json({ error: 'Battery level must be between 0 and 100' });
  }

  // Update status
  const status = req.device.status;
  if (character) status.character = character;
  if (state) status.state = state;
  if (message) status.message = message;
  if (batteryLevel !== undefined) status.batteryLevel = batteryLevel;
  status.lastUpdated = Date.now();

  log(`PUT /api/status for device ${req.deviceId}:`, status);
  res.json({ success: true, status });
});

// Wake up (triggered by OpenClaw)
app.post('/api/wakeup', authenticateToken, (req, res) => {
  const status = req.device.status;
  status.state = 'EXCITED';
  status.message = req.body.message || 'Hey! OpenClaw woke me up!';
  status.lastUpdated = Date.now();

  // Reset to IDLE after 10 seconds
  setTimeout(() => {
    if (status.state === 'EXCITED') {
      status.state = 'IDLE';
      status.message = 'Back to normal...';
      status.lastUpdated = Date.now();
    }
  }, 10000);

  log(`POST /api/wakeup for device ${req.deviceId}`);
  res.json({ success: true, status });
});

// ==================== Device Routes (for Android App) ====================

// Get status for device (uses deviceId + secret)
app.post('/api/device/status', (req, res) => {
  const { deviceId, deviceSecret } = req.body;

  if (!deviceId || !deviceSecret) {
    return res.status(400).json({ error: 'deviceId and deviceSecret required' });
  }

  const device = devices.get(deviceId);
  if (!device || device.secret !== deviceSecret) {
    return res.status(403).json({ error: 'Invalid device credentials' });
  }

  res.json(device.status);
});

// Send message from device to OpenClaw
app.post('/api/device/message', (req, res) => {
  const { deviceId, deviceSecret, message } = req.body;

  if (!deviceId || !deviceSecret || !message) {
    return res.status(400).json({ error: 'deviceId, deviceSecret, and message required' });
  }

  const device = devices.get(deviceId);
  if (!device || device.secret !== deviceSecret) {
    return res.status(403).json({ error: 'Invalid device credentials' });
  }

  if (!device.webhookUrl) {
    return res.status(400).json({ error: 'No OpenClaw webhook configured' });
  }

  // Send webhook to OpenClaw
  const timestamp = Date.now().toString();
  const payload = { deviceId, message, timestamp };
  const signature = crypto
    .createHmac('sha256', device.webhookSecret)
    .update(timestamp + '.' + JSON.stringify(payload))
    .digest('hex');

  // In production, use fetch or axios to send the webhook
  log(`Webhook to OpenClaw: ${device.webhookUrl}`, payload);

  // Update status
  device.status.state = 'BUSY';
  device.status.message = `Processing: ${message.substring(0, 30)}...`;
  device.status.lastUpdated = Date.now();

  res.json({
    success: true,
    message: 'Message sent to OpenClaw',
    webhookPayload: payload,
    signature: signature
  });
});

// ==================== MCP Endpoint for OpenClaw ====================

function getMCPSkills() {
  return {
    name: 'realbot',
    version: '1.0.0',
    description: 'Control Claw Live Wallpaper on Android device',
    tools: [
      {
        name: 'update_claw_status',
        description: 'Update the Claw character status on the Android device',
        inputSchema: {
          type: 'object',
          properties: {
            character: {
              type: 'string',
              enum: ['LOBSTER', 'PIG'],
              description: 'The character to display'
            },
            state: {
              type: 'string',
              enum: ['IDLE', 'BUSY', 'EATING', 'SLEEPING', 'EXCITED'],
              description: 'The state of the character'
            },
            message: {
              type: 'string',
              description: 'Message to display below the character'
            },
            batteryLevel: {
              type: 'integer',
              minimum: 0,
              maximum: 100,
              description: 'Battery level indicator'
            }
          }
        }
      },
      {
        name: 'wake_up_claw',
        description: 'Wake up the Claw character (shows excited animation)',
        inputSchema: {
          type: 'object',
          properties: {
            message: {
              type: 'string',
              description: 'Optional wake up message'
            }
          }
        }
      },
      {
        name: 'get_claw_status',
        description: 'Get the current status of the Claw character',
        inputSchema: {
          type: 'object',
          properties: {}
        }
      }
    ]
  };
}

// MCP tools list endpoint
app.get('/mcp/tools', authenticateToken, (req, res) => {
  res.json(getMCPSkills());
});

// MCP tool execution endpoint
app.post('/mcp/execute', authenticateToken, (req, res) => {
  const { tool, arguments: args } = req.body;

  if (!tool) {
    return res.status(400).json({ error: 'Tool name required' });
  }

  const status = req.device.status;

  switch (tool) {
    case 'update_claw_status':
      if (args.character) status.character = args.character;
      if (args.state) status.state = args.state;
      if (args.message) status.message = args.message;
      if (args.batteryLevel !== undefined) status.batteryLevel = args.batteryLevel;
      status.lastUpdated = Date.now();
      log(`MCP execute update_claw_status:`, status);
      return res.json({ success: true, result: status });

    case 'wake_up_claw':
      status.state = 'EXCITED';
      status.message = args.message || 'Wake up!';
      status.lastUpdated = Date.now();
      setTimeout(() => {
        if (status.state === 'EXCITED') {
          status.state = 'IDLE';
          status.message = 'Back to normal...';
          status.lastUpdated = Date.now();
        }
      }, 10000);
      log(`MCP execute wake_up_claw`);
      return res.json({ success: true, result: status });

    case 'get_claw_status':
      log(`MCP execute get_claw_status`);
      return res.json({ success: true, result: status });

    default:
      return res.status(400).json({ error: `Unknown tool: ${tool}` });
  }
});

// ==================== Start Server ====================

app.listen(PORT, '0.0.0.0', () => {
  console.log('');
  console.log('╔═══════════════════════════════════════════════════╗');
  console.log('║       Realbot Backend Server v2.0 Started         ║');
  console.log('╠═══════════════════════════════════════════════════╣');
  console.log(`║  Port: ${PORT.toString().padEnd(42)}║`);
  console.log(`║  Environment: ${(process.env.NODE_ENV || 'development').padEnd(35)}║`);
  console.log('╚═══════════════════════════════════════════════════╝');
  console.log('');
  console.log('Public endpoints:');
  console.log('  GET  /                    - Health check');
  console.log('  GET  /api/health          - Railway health check');
  console.log('  POST /api/device/register - Register device & get binding code');
  console.log('  POST /api/bind            - Bind OpenClaw with code');
  console.log('');
  console.log('Authenticated endpoints (Bearer token):');
  console.log('  GET  /api/status          - Get agent status');
  console.log('  PUT  /api/status          - Update agent status');
  console.log('  POST /api/wakeup          - Wake up agent');
  console.log('  GET  /mcp/tools           - List MCP tools');
  console.log('  POST /mcp/execute         - Execute MCP tool');
  console.log('');
  console.log('Device endpoints (deviceId + secret):');
  console.log('  POST /api/device/status   - Get status for device');
  console.log('  POST /api/device/message  - Send message to OpenClaw');
  console.log('');
});
