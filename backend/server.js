const express = require('express');
const cors = require('cors');
require('dotenv').config();

const app = express();
const PORT = process.env.PORT || 3000;

// Middleware
app.use(cors());
app.use(express.json());

// In-memory state (in production, use a database like Redis or PostgreSQL)
let agentStatus = {
  character: 'LOBSTER',
  state: 'IDLE',
  message: 'Hello! I am ready.',
  batteryLevel: 100,
  lastUpdated: Date.now()
};

// ==================== API Routes ====================

// Health check endpoint
app.get('/', (req, res) => {
  res.json({
    status: 'ok',
    message: 'Realbot Backend is running',
    timestamp: new Date().toISOString()
  });
});

// GET /api/status - Get current agent status
app.get('/api/status', (req, res) => {
  agentStatus.lastUpdated = Date.now();

  console.log('[' + new Date().toISOString() + '] GET /api/status - Returning:', agentStatus);
  res.json(agentStatus);
});

// POST /api/wakeup - Handle user tap/wake up event
app.post('/api/wakeup', (req, res) => {
  console.log('[' + new Date().toISOString() + '] POST /api/wakeup - Agent woken up!');

  // Change state to EXCITED when user taps
  agentStatus.state = 'EXCITED';
  agentStatus.message = 'Hey! You woke me up!';
  agentStatus.lastUpdated = Date.now();

  // Reset to IDLE after 10 seconds
  setTimeout(() => {
    if (agentStatus.state === 'EXCITED') {
      agentStatus.state = 'IDLE';
      agentStatus.message = 'Back to normal...';
      agentStatus.lastUpdated = Date.now();
    }
  }, 10000);

  res.json({
    success: true,
    message: 'Agent woken up!',
    status: agentStatus
  });
});

// PUT /api/status - Update agent status (for admin/control purposes)
app.put('/api/status', (req, res) => {
  const { character, state, message, batteryLevel } = req.body;

  // Validate character
  if (character && !['LOBSTER', 'PIG'].includes(character)) {
    return res.status(400).json({ error: 'Invalid character. Must be LOBSTER or PIG' });
  }

  // Validate state
  const validStates = ['IDLE', 'BUSY', 'EATING', 'SLEEPING', 'EXCITED'];
  if (state && !validStates.includes(state)) {
    return res.status(400).json({ error: 'Invalid state. Must be one of: ' + validStates.join(', ') });
  }

  // Validate batteryLevel
  if (batteryLevel !== undefined && (batteryLevel < 0 || batteryLevel > 100)) {
    return res.status(400).json({ error: 'Battery level must be between 0 and 100' });
  }

  // Update status
  if (character) agentStatus.character = character;
  if (state) agentStatus.state = state;
  if (message) agentStatus.message = message;
  if (batteryLevel !== undefined) agentStatus.batteryLevel = batteryLevel;
  agentStatus.lastUpdated = Date.now();

  console.log('[' + new Date().toISOString() + '] PUT /api/status - Updated to:', agentStatus);
  res.json({ success: true, status: agentStatus });
});

// GET /api/health - Health check for Railway
app.get('/api/health', (req, res) => {
  res.json({ status: 'healthy', uptime: process.uptime() });
});

// ==================== Start Server ====================

app.listen(PORT, '0.0.0.0', () => {
  console.log('');
  console.log('========================================');
  console.log('   Realbot Backend Server Started');
  console.log('========================================');
  console.log('  Port: ' + PORT);
  console.log('  Environment: ' + (process.env.NODE_ENV || 'development'));
  console.log('  Time: ' + new Date().toISOString());
  console.log('========================================');
  console.log('');
  console.log('Available endpoints:');
  console.log('  GET  /              - Health check');
  console.log('  GET  /api/status    - Get agent status');
  console.log('  POST /api/wakeup    - Wake up agent (user tap)');
  console.log('  PUT  /api/status    - Update agent status');
  console.log('  GET  /api/health    - Health check for Railway');
  console.log('');
});
