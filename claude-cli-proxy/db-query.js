#!/usr/bin/env node
// ============================================
// db-query.js — Read-only PostgreSQL query helper
// Used by Claude CLI AI to query the E-Claw database safely.
//
// Usage:  node db-query.js "SELECT * FROM server_logs WHERE device_id = '...' LIMIT 50"
// Output: JSON array of rows (or error message)
//
// Safety: Only SELECT statements allowed. No INSERT/UPDATE/DELETE/DROP/ALTER/TRUNCATE.
// ============================================

const { Client } = require('pg');
const fss = require('fs');
const pathh = require('path');

// Resolve DATABASE_URL: env var → config file fallback
const DATABASE_URL = process.env.DATABASE_URL || (() => {
    try {
        return fss.readFileSync(pathh.join(__dirname, '.db-config'), 'utf8').trim();
    } catch { return null; }
})();

if (!DATABASE_URL) {
    console.error('ERROR: No DATABASE_URL (env var not set and .db-config not found)');
    process.exit(1);
}

const sql = process.argv[2];

if (!sql) {
    console.error('Usage: node db-query.js "SELECT ..."');
    process.exit(1);
}

// Safety check: only allow SELECT
const normalized = sql.trim().toUpperCase();
const forbidden = ['INSERT', 'UPDATE', 'DELETE', 'DROP', 'ALTER', 'TRUNCATE', 'CREATE', 'GRANT', 'REVOKE'];
const firstWord = normalized.split(/\s+/)[0];
if (firstWord !== 'SELECT' && firstWord !== 'WITH') {
    console.error(`ERROR: Only SELECT queries allowed. Got: ${firstWord}`);
    process.exit(1);
}
for (const kw of forbidden) {
    // Check for forbidden keywords not inside quotes (simple heuristic)
    if (normalized.includes(kw + ' ') || normalized.includes(kw + '(') || normalized.endsWith(kw)) {
        console.error(`ERROR: Forbidden keyword "${kw}" detected. Read-only queries only.`);
        process.exit(1);
    }
}

(async () => {
    const client = new Client({ connectionString: DATABASE_URL });
    try {
        await client.connect();
        const result = await client.query(sql);
        console.log(JSON.stringify(result.rows, null, 2));
    } catch (err) {
        console.error(`DB Error: ${err.message}`);
        process.exit(1);
    } finally {
        await client.end();
    }
})();
