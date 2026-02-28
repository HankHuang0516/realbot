// Feedback Email Notification Module
// Sends transactional emails when feedback status changes (in_progress, resolved, closed)
const { Resend } = require('resend');

const resend = new Resend(process.env.RESEND_API_KEY || 'dummy');
const EMAIL_FROM = 'E-Claw <noreply@twopiggyhavefun.uk>';
const BASE_URL = process.env.BASE_URL || 'https://eclawbot.com';

const EMAIL_TRIGGER_STATUSES = ['in_progress', 'resolved', 'closed'];

const STATUS_CONFIG = {
    in_progress: { color: '#FFA726', label: 'In Progress', subject: id => `Your feedback #${id} is being worked on` },
    resolved:    { color: '#66BB6A', label: 'Resolved',    subject: id => `Your feedback #${id} has been resolved` },
    closed:      { color: '#888888', label: 'Closed',      subject: id => `Your feedback #${id} has been closed` }
};

const CATEGORY_EMOJI = { bug: '\u{1F41B}', feature: '\u{1F4A1}', question: '\u{2753}' };

/**
 * Look up verified user email by device_id.
 * Returns { email, language } or null.
 */
async function getUserEmailByDeviceId(pool, deviceId) {
    try {
        const result = await pool.query(
            'SELECT email, email_verified, language FROM user_accounts WHERE device_id = $1',
            [deviceId]
        );
        if (result.rows.length === 0) return null;
        const user = result.rows[0];
        if (!user.email_verified) return null;
        return { email: user.email, language: user.language || 'en' };
    } catch (err) {
        console.error('[FeedbackEmail] User lookup error:', err.message);
        return null;
    }
}

/**
 * Build HTML email for feedback status change.
 */
function buildEmailHtml({ feedbackId, category, message, newStatus, resolution, githubIssueUrl }) {
    const cfg = STATUS_CONFIG[newStatus] || STATUS_CONFIG.resolved;
    const emoji = CATEGORY_EMOJI[category] || '';
    const msgSummary = (message || '').length > 200 ? message.substring(0, 200) + '...' : (message || '');

    let resolutionBlock = '';
    if (resolution) {
        resolutionBlock = `
            <div style="background: #1A1A2E; border-left: 3px solid #6C63FF; padding: 12px 16px; border-radius: 0 8px 8px 0; margin: 16px 0;">
                <p style="color: #888; font-size: 12px; margin: 0 0 4px 0;">Resolution:</p>
                <p style="margin: 0; font-size: 14px; color: #E0E0E0; white-space: pre-wrap;">${escapeHtml(resolution)}</p>
            </div>`;
    }

    let githubBlock = '';
    if (githubIssueUrl) {
        githubBlock = `
            <p style="margin: 12px 0;">
                <a href="${escapeHtml(githubIssueUrl)}" style="color: #6C63FF; font-size: 13px; text-decoration: underline;">View GitHub Issue</a>
            </p>`;
    }

    return `
        <div style="font-family: Arial, sans-serif; max-width: 500px; margin: 0 auto; background: #0D0D1A; color: #fff; padding: 32px; border-radius: 12px;">
            <h2 style="color: #6C63FF; margin-top: 0;">Feedback Update</h2>

            <div style="background: #1A1A2E; border-radius: 8px; padding: 16px; margin: 16px 0;">
                <p style="margin: 4px 0; font-size: 14px;">
                    <span style="color: #888;">Feedback #</span><strong>${feedbackId}</strong>
                </p>
                <p style="margin: 4px 0; font-size: 14px;">
                    <span style="color: #888;">Category:</span> ${emoji} ${escapeHtml(category || 'bug')}
                </p>
                <p style="margin: 8px 0; font-size: 14px; color: #ccc;">
                    "${escapeHtml(msgSummary)}"
                </p>
            </div>

            <p style="font-size: 15px;">
                Status: <strong style="color: ${cfg.color}; background: ${cfg.color}22; padding: 4px 10px; border-radius: 4px;">${cfg.label}</strong>
            </p>

            ${resolutionBlock}
            ${githubBlock}

            <a href="${BASE_URL}/portal/feedback.html"
               style="display: inline-block; background: #6C63FF; color: #fff; padding: 12px 24px; border-radius: 8px; text-decoration: none; margin: 16px 0; font-weight: bold;">
                View Feedback
            </a>

            <p style="color: #888; font-size: 12px; margin-top: 24px;">
                You're receiving this because you submitted feedback on E-Claw.<br>
                You can manage notification preferences in your portal settings.
            </p>
        </div>`;
}

/**
 * Escape HTML special characters to prevent XSS in email content.
 */
function escapeHtml(str) {
    if (!str) return '';
    return String(str)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;');
}

/**
 * Send feedback status change email (fire-and-forget).
 * Checks: trigger status, verified email, notification preferences.
 *
 * @param {object} pool - PostgreSQL pool
 * @param {object} feedback - Feedback row from DB
 * @param {string} newStatus - The new status value
 * @param {string|null} resolution - Resolution text if provided
 * @param {object} notifModule - notifications module (for preference check)
 */
async function sendFeedbackStatusEmail(pool, feedback, newStatus, resolution, notifModule) {
    if (!EMAIL_TRIGGER_STATUSES.includes(newStatus)) return;
    if (!feedback || !feedback.device_id) return;

    // Check user notification preferences
    if (notifModule) {
        const prefs = await notifModule.getPrefs(feedback.device_id);
        if (!notifModule.isCategoryEnabled(prefs, 'feedback_resolved')) {
            console.log(`[FeedbackEmail] Skipped #${feedback.id}: user disabled feedback_resolved`);
            return;
        }
    }

    const user = await getUserEmailByDeviceId(pool, feedback.device_id);
    if (!user) {
        console.log(`[FeedbackEmail] Skipped #${feedback.id}: no verified email for device ${feedback.device_id}`);
        return;
    }

    const cfg = STATUS_CONFIG[newStatus];
    const subject = cfg.subject(feedback.id);
    const html = buildEmailHtml({
        feedbackId: feedback.id,
        category: feedback.category,
        message: feedback.message,
        newStatus,
        resolution: resolution || feedback.resolution || null,
        githubIssueUrl: feedback.github_issue_url || null
    });

    try {
        await resend.emails.send({
            from: EMAIL_FROM,
            to: user.email,
            subject,
            html
        });
        console.log(`[FeedbackEmail] Sent ${newStatus} email for #${feedback.id} to ${user.email}`);
    } catch (err) {
        console.error(`[FeedbackEmail] Failed to send for #${feedback.id}:`, err.message);
    }
}

module.exports = { sendFeedbackStatusEmail };
