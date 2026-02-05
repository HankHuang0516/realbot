const express = require('express');
const cors = require('cors');
const app = express();
const port = process.env.PORT || 3000;

// Middleware
app.use(cors());
app.use(express.json());

// In-memory Database (Simulate persistent storage)
let agentState = {
    character: "LOBSTER", // LOBSTER, PIG
    state: "IDLE",       // IDLE, BUSY, EATING, SLEEPING, EXCITED
    message: "System Online",
    batteryLevel: 100,
    lastUpdated: Date.now()
};

// Auto-decay and random changes simulation (The "Ghost" in the machine)
setInterval(() => {
    // 1. Battery Decay
    if (agentState.batteryLevel > 0) {
        agentState.batteryLevel -= 1;
    }

    // 2. Random State Change (Idle vs Sleep)
    const now = Date.now();
    if (now - agentState.lastUpdated > 20000) { // If no interaction for 20s
        if (Math.random() > 0.7) {
            agentState.state = "SLEEPING";
            agentState.message = "Zzz...";
        } else {
            agentState.state = "IDLE";
            agentState.message = "Waiting for command...";
        }
        
        // Randomly switch character form occasionally
        if (Math.random() > 0.8) {
             agentState.character = agentState.character === "LOBSTER" ? "PIG" : "LOBSTER";
             console.log(`[Auto] Scaled transformed into ${agentState.character}`);
        }
        
        agentState.lastUpdated = now;
    }
}, 5000); // Check every 5s

// --- Routes ---

app.get('/', (req, res) => {
    res.send('Claw Backend is Running! 🦞');
});

/**
 * GET /api/status
 * Returns the current agent status for the Android App.
 */
app.get('/api/status', (req, res) => {
    res.json(agentState);
});

/**
 * POST /api/wakeup
 * Webhook to wake up the bot (e.g. from Touch or External Trigger).
 */
app.post('/api/wakeup', (req, res) => {
    console.log("[Webhook] Wake Up Signal Received!");
    
    agentState.state = "EXCITED";
    agentState.message = "I'm Awake!";
    agentState.lastUpdated = Date.now();
    
    // Auto-revert to IDLE after 5 seconds
    setTimeout(() => {
        if (agentState.state === "EXCITED") {
            agentState.state = "IDLE";
            agentState.message = "Ready.";
        }
    }, 5000);

    res.json({ success: true, message: "Agent Woken Up" });
});

/**
 * POST /api/transform
 * MCP Tool endpoint: Force a transformation.
 */
app.post('/api/transform', (req, res) => {
    const { character, state, message } = req.body;
    
    if (character) agentState.character = character;
    if (state) agentState.state = state;
    if (message) agentState.message = message;
    
    agentState.lastUpdated = Date.now();
    
    console.log(`[MCP] Transform command: ${JSON.stringify(req.body)}`);
    res.json({ success: true, currentState: agentState });
});

// Start Server
app.listen(port, () => {
    console.log(`Server running on port ${port}`);
});
