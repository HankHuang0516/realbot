// ============================================
// PostgreSQL Database Module
// Handles data persistence for Claw Backend
// ============================================

const { Pool } = require('pg');

// Database connection pool
let pool = null;

// Initialize database connection
async function initDatabase() {
    try {
        // Railway automatically provides DATABASE_URL environment variable
        // when you add a PostgreSQL service
        const connectionString = process.env.DATABASE_URL;

        if (!connectionString) {
            console.error('[DB] DATABASE_URL not found. PostgreSQL persistence disabled.');
            console.error('[DB] To enable: Add PostgreSQL service in Railway dashboard');
            return false;
        }

        // Create connection pool
        pool = new Pool({
            connectionString: connectionString,
            ssl: process.env.NODE_ENV === 'production' ? {
                rejectUnauthorized: false // Railway uses self-signed certificates
            } : false
        });

        // Test connection
        const client = await pool.connect();
        console.log('[DB] PostgreSQL connection established');
        client.release();

        // Create tables if they don't exist
        await createTables();

        return true;
    } catch (err) {
        console.error('[DB] Failed to initialize database:', err.message);
        return false;
    }
}

// Create database tables
async function createTables() {
    try {
        const client = await pool.connect();

        // Create devices table
        await client.query(`
            CREATE TABLE IF NOT EXISTS devices (
                device_id TEXT PRIMARY KEY,
                device_secret TEXT NOT NULL,
                created_at BIGINT NOT NULL,
                updated_at BIGINT DEFAULT EXTRACT(EPOCH FROM NOW()) * 1000
            )
        `);

        // Create entities table
        await client.query(`
            CREATE TABLE IF NOT EXISTS entities (
                device_id TEXT NOT NULL,
                entity_id INTEGER NOT NULL,
                bot_secret TEXT,
                is_bound BOOLEAN DEFAULT FALSE,
                name TEXT,
                character TEXT NOT NULL,
                state TEXT NOT NULL,
                message TEXT NOT NULL,
                parts JSONB DEFAULT '{}',
                battery_level INTEGER DEFAULT 100,
                last_updated BIGINT NOT NULL,
                message_queue JSONB DEFAULT '[]',
                webhook JSONB,
                app_version TEXT,
                PRIMARY KEY (device_id, entity_id),
                FOREIGN KEY (device_id) REFERENCES devices(device_id) ON DELETE CASCADE
            )
        `);

        // Create index for faster queries
        await client.query(`
            CREATE INDEX IF NOT EXISTS idx_entities_bound
            ON entities(is_bound)
            WHERE is_bound = TRUE
        `);

        console.log('[DB] Database tables ready');
        client.release();
    } catch (err) {
        console.error('[DB] Failed to create tables:', err.message);
        throw err;
    }
}

// Save device and all entities to database
async function saveDeviceData(deviceId, deviceData) {
    if (!pool) {
        console.warn('[DB] Database not initialized, skipping save');
        return false;
    }

    try {
        const client = await pool.connect();

        try {
            await client.query('BEGIN');

            // Upsert device
            await client.query(
                `INSERT INTO devices (device_id, device_secret, created_at, updated_at)
                 VALUES ($1, $2, $3, $4)
                 ON CONFLICT (device_id)
                 DO UPDATE SET updated_at = $4`,
                [deviceId, deviceData.deviceSecret, deviceData.createdAt, Date.now()]
            );

            // Save all entities
            for (let i = 0; i < 4; i++) {
                const entity = deviceData.entities[i];
                if (!entity) continue;

                await client.query(
                    `INSERT INTO entities (
                        device_id, entity_id, bot_secret, is_bound, name,
                        character, state, message, parts, battery_level,
                        last_updated, message_queue, webhook, app_version
                    ) VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12, $13, $14)
                    ON CONFLICT (device_id, entity_id)
                    DO UPDATE SET
                        bot_secret = $3,
                        is_bound = $4,
                        name = $5,
                        character = $6,
                        state = $7,
                        message = $8,
                        parts = $9,
                        battery_level = $10,
                        last_updated = $11,
                        message_queue = $12,
                        webhook = $13,
                        app_version = $14`,
                    [
                        deviceId,
                        i,
                        entity.botSecret,
                        entity.isBound,
                        entity.name,
                        entity.character,
                        entity.state,
                        entity.message,
                        JSON.stringify(entity.parts),
                        entity.batteryLevel,
                        entity.lastUpdated,
                        JSON.stringify(entity.messageQueue || []),
                        entity.webhook ? JSON.stringify(entity.webhook) : null,
                        entity.appVersion
                    ]
                );
            }

            await client.query('COMMIT');
            return true;
        } catch (err) {
            await client.query('ROLLBACK');
            throw err;
        } finally {
            client.release();
        }
    } catch (err) {
        console.error(`[DB] Failed to save device ${deviceId}:`, err.message);
        return false;
    }
}

// Save all devices to database
async function saveAllDevices(devicesObject) {
    if (!pool) {
        console.warn('[DB] Database not initialized, skipping save');
        return false;
    }

    try {
        let savedCount = 0;
        for (const deviceId in devicesObject) {
            const success = await saveDeviceData(deviceId, devicesObject[deviceId]);
            if (success) savedCount++;
        }

        if (savedCount > 0) {
            console.log(`[DB] Saved ${savedCount} devices to PostgreSQL`);
        }
        return true;
    } catch (err) {
        console.error('[DB] Failed to save devices:', err.message);
        return false;
    }
}

// Load all devices from database
async function loadAllDevices() {
    if (!pool) {
        console.warn('[DB] Database not initialized, skipping load');
        return {};
    }

    try {
        const client = await pool.connect();

        // Load all devices
        const devicesResult = await client.query(
            'SELECT * FROM devices ORDER BY created_at ASC'
        );

        // Load all entities
        const entitiesResult = await client.query(
            'SELECT * FROM entities ORDER BY device_id, entity_id ASC'
        );

        client.release();

        // Reconstruct devices object
        const devices = {};

        for (const row of devicesResult.rows) {
            devices[row.device_id] = {
                deviceId: row.device_id,
                deviceSecret: row.device_secret,
                createdAt: parseInt(row.created_at),
                entities: {}
            };
        }

        // Add entities to devices
        for (const row of entitiesResult.rows) {
            const deviceId = row.device_id;
            const entityId = parseInt(row.entity_id);

            if (!devices[deviceId]) continue;

            devices[deviceId].entities[entityId] = {
                entityId: entityId,
                botSecret: row.bot_secret,
                isBound: row.is_bound,
                name: row.name,
                character: row.character,
                state: row.state,
                message: row.message,
                parts: typeof row.parts === 'string' ? JSON.parse(row.parts) : row.parts,
                batteryLevel: row.battery_level,
                lastUpdated: parseInt(row.last_updated),
                messageQueue: typeof row.message_queue === 'string'
                    ? JSON.parse(row.message_queue)
                    : (row.message_queue || []),
                webhook: row.webhook
                    ? (typeof row.webhook === 'string' ? JSON.parse(row.webhook) : row.webhook)
                    : null,
                appVersion: row.app_version
            };
        }

        const deviceCount = Object.keys(devices).length;
        let boundCount = 0;
        for (const deviceId in devices) {
            for (let i = 0; i < 4; i++) {
                if (devices[deviceId].entities[i]?.isBound) boundCount++;
            }
        }

        if (deviceCount > 0) {
            console.log(`[DB] Loaded ${deviceCount} devices, ${boundCount} bound entities from PostgreSQL`);
        }

        return devices;
    } catch (err) {
        console.error('[DB] Failed to load devices:', err.message);
        return {};
    }
}

// Delete device from database
async function deleteDevice(deviceId) {
    if (!pool) return false;

    try {
        const client = await pool.connect();
        // CASCADE will automatically delete associated entities
        await client.query('DELETE FROM devices WHERE device_id = $1', [deviceId]);
        client.release();
        console.log(`[DB] Deleted device ${deviceId} from PostgreSQL`);
        return true;
    } catch (err) {
        console.error(`[DB] Failed to delete device ${deviceId}:`, err.message);
        return false;
    }
}

// Get database statistics
async function getStats() {
    if (!pool) return null;

    try {
        const client = await pool.connect();

        const devicesResult = await client.query('SELECT COUNT(*) FROM devices');
        const entitiesResult = await client.query('SELECT COUNT(*) FROM entities WHERE is_bound = TRUE');

        client.release();

        return {
            devices: parseInt(devicesResult.rows[0].count),
            boundEntities: parseInt(entitiesResult.rows[0].count)
        };
    } catch (err) {
        console.error('[DB] Failed to get stats:', err.message);
        return null;
    }
}

// Close database connection
async function closeDatabase() {
    if (pool) {
        await pool.end();
        console.log('[DB] PostgreSQL connection closed');
    }
}

module.exports = {
    initDatabase,
    saveDeviceData,
    saveAllDevices,
    loadAllDevices,
    deleteDevice,
    getStats,
    closeDatabase
};
