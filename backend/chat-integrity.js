/**
 * Chat Integrity Validation Module
 *
 * Receives mismatch reports from Web/Android clients,
 * deduplicates them, logs via serverLog(), and auto-creates GitHub issues.
 *
 * Flow:
 *   1. Client detects display/data inconsistency after render
 *   2. POST /api/chat/integrity-report → report()
 *   3. serverLog() + DB persist
 *   4. If new fingerprint → createGithubIssue()
 */

// ── DB Setup ──────────────────────────────────────────────────────────────────

async function initIntegrityTable(pool) {
    if (!pool) return;
    try {
        await pool.query(`
            CREATE TABLE IF NOT EXISTS chat_integrity_reports (
                id SERIAL PRIMARY KEY,
                device_id TEXT NOT NULL,
                platform VARCHAR(16) NOT NULL,
                layer VARCHAR(16) NOT NULL,
                check_type VARCHAR(64) NOT NULL,
                fingerprint TEXT NOT NULL,
                details JSONB,
                github_issue_url TEXT,
                created_at BIGINT DEFAULT EXTRACT(EPOCH FROM NOW()) * 1000
            )
        `);
        await pool.query(`CREATE INDEX IF NOT EXISTS idx_cir_device ON chat_integrity_reports(device_id)`);
        await pool.query(`CREATE INDEX IF NOT EXISTS idx_cir_fp ON chat_integrity_reports(fingerprint)`);
        console.log('[ChatIntegrity] Table ready');
    } catch (err) {
        console.error('[ChatIntegrity] Table init error:', err.message);
    }
}

// ── In-memory dedup ───────────────────────────────────────────────────────────
// fingerprint → { count, firstSeenMs, issueUrl }
const _seen = new Map();
const DEDUP_TTL_MS = 60 * 60 * 1000; // 1 hour per unique fingerprint

function _pruneExpired() {
    const now = Date.now();
    for (const [fp, entry] of _seen) {
        if (now - entry.firstSeenMs > DEDUP_TTL_MS) _seen.delete(fp);
    }
}

// ── Core: report() ────────────────────────────────────────────────────────────

/**
 * Process an incoming mismatch report.
 * @param {Pool} pool - PostgreSQL connection pool
 * @param {Object} mismatch - { deviceId, platform, layer, checkType, description, expected, actual, affectedIds, appVersion }
 * @param {Object} deps - { serverLog }
 * @returns {{ logged: boolean, githubIssue: string|null, suppressed: boolean }}
 */
async function report(pool, mismatch, { serverLog }) {
    _pruneExpired();

    const {
        deviceId, platform, layer, checkType,
        description, expected, actual, affectedIds = [], appVersion
    } = mismatch;

    // Build fingerprint: device + check type + first affected ID
    const firstId = affectedIds[0] || 'none';
    const fingerprint = `${deviceId}:${layer}:${checkType}:${firstId}`;

    const now = Date.now();
    const existing = _seen.get(fingerprint);

    if (existing) {
        existing.count++;
        serverLog('warn', 'chat_integrity',
            `[${platform}] ${layer}:${checkType} repeat #${existing.count} — ${description || ''}`,
            { deviceId, metadata: { fingerprint, count: existing.count } }
        );
        return { logged: true, githubIssue: existing.issueUrl || null, suppressed: true };
    }

    // First time seeing this fingerprint
    _seen.set(fingerprint, { count: 1, firstSeenMs: now, issueUrl: null });

    // Always log
    serverLog('warn', 'chat_integrity',
        `[${platform}] ${layer}:${checkType} — ${description || JSON.stringify({ expected, actual })}`,
        { deviceId, metadata: { fingerprint, checkType, layer, platform, affectedIds, appVersion } }
    );

    // Persist to DB
    let savedId = null;
    if (pool) {
        try {
            const result = await pool.query(
                `INSERT INTO chat_integrity_reports
                 (device_id, platform, layer, check_type, fingerprint, details, created_at)
                 VALUES ($1, $2, $3, $4, $5, $6, $7) RETURNING id`,
                [deviceId, platform, layer, checkType, fingerprint,
                 JSON.stringify({ description, expected, actual, affectedIds, appVersion }), now]
            );
            savedId = result.rows[0]?.id;
        } catch (err) {
            console.error('[ChatIntegrity] DB insert error:', err.message);
        }
    }

    // Auto-create GitHub issue (direct API call)
    let githubIssue = null;
    const token = process.env.GITHUB_TOKEN;
    const repo = process.env.GITHUB_REPO;
    if (token && repo) {
        const title = `[ChatIntegrity] ${platform}/${layer}:${checkType} — ${(description || '').substring(0, 60)}`;
        const body = [
            `## Chat Integrity Mismatch`,
            '',
            `| Field | Value |`,
            `|---|---|`,
            `| **Device** | \`${deviceId}\` |`,
            `| **Platform** | ${platform} |`,
            `| **Layer** | ${layer} |`,
            `| **Check** | \`${checkType}\` |`,
            `| **App Version** | ${appVersion || 'N/A'} |`,
            `| **Time** | ${new Date(now).toISOString()} |`,
            `| **Report ID** | ${savedId || 'N/A'} |`,
            '',
            `### Description`,
            description || '_No description_',
            '',
            `### Expected`,
            '```json',
            JSON.stringify(expected, null, 2),
            '```',
            '',
            `### Actual`,
            '```json',
            JSON.stringify(actual, null, 2),
            '```',
            '',
            `### Affected Message IDs`,
            affectedIds.length > 0 ? affectedIds.map(id => `- \`${id}\``).join('\n') : '_None_',
            '',
            '---',
            '_Auto-detected by ChatIntegrityValidator_'
        ].join('\n');

        const labels = ['bug', 'chat-integrity', `platform:${platform}`, `layer:${layer}`];

        try {
            const res = await fetch(`https://api.github.com/repos/${repo}/issues`, {
                method: 'POST',
                headers: {
                    'Authorization': `token ${token}`,
                    'Accept': 'application/vnd.github.v3+json',
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ title, body, labels })
            });
            if (res.ok) {
                const data = await res.json();
                githubIssue = { url: data.html_url, number: data.number };
                _seen.get(fingerprint).issueUrl = githubIssue.url;
                if (pool && savedId) {
                    await pool.query(
                        `UPDATE chat_integrity_reports SET github_issue_url = $1 WHERE id = $2`,
                        [githubIssue.url, savedId]
                    ).catch(() => {});
                }
                console.log(`[ChatIntegrity] GitHub issue created: ${githubIssue.url}`);
            } else {
                const err = await res.text();
                console.error(`[ChatIntegrity] GitHub issue failed (${res.status}):`, err);
            }
        } catch (err) {
            console.error('[ChatIntegrity] GitHub API error:', err.message);
        }
    }

    return { logged: true, githubIssue: githubIssue?.url || null, suppressed: false };
}

module.exports = { initIntegrityTable, report };
