/**
 * Upload AAB to Google Play with proxy support
 *
 * In environments where androidpublisher.googleapis.com is blocked but
 * www.googleapis.com is allowed, this script overrides the API root URL
 * and configures the proxy for gaxios.
 */

const { google } = require('googleapis');
const fs = require('fs');
const path = require('path');

// Configure proxy
const proxyUrl = process.env.GLOBAL_AGENT_HTTP_PROXY;
if (proxyUrl) {
    process.env.HTTPS_PROXY = proxyUrl;
    process.env.HTTP_PROXY = proxyUrl;
    process.env.NO_PROXY = 'localhost,127.0.0.1';
    process.env.no_proxy = 'localhost,127.0.0.1';
    console.log('[proxy] HTTPS_PROXY configured');
}

const PACKAGE_NAME = 'com.hank.clawlive';
const SERVICE_ACCOUNT_PATH = path.join(__dirname, '..', 'play-service-account.json');
const DEFAULT_AAB_PATH = path.join(__dirname, '..', 'app', 'build', 'outputs', 'bundle', 'release', 'app-release.aab');

const TRACK_CONFIG = {
    internal: { status: 'completed', label: 'Internal Testing (內部測試)' },
    production: { status: 'completed', label: 'Production (正式版 - 提交審查)' },
};

function parseArgs() {
    let track = 'internal';
    let aabPath = DEFAULT_AAB_PATH;

    for (const arg of process.argv.slice(2)) {
        if (arg.startsWith('--track=')) {
            track = arg.split('=')[1];
        } else if (!arg.startsWith('--')) {
            aabPath = arg;
        }
    }

    if (!TRACK_CONFIG[track]) {
        console.error(`ERROR: Unknown track "${track}". Available: ${Object.keys(TRACK_CONFIG).join(', ')}`);
        process.exit(1);
    }

    return { track, aabPath };
}

async function main() {
    const { track, aabPath } = parseArgs();
    const config = TRACK_CONFIG[track];

    if (!fs.existsSync(SERVICE_ACCOUNT_PATH)) {
        console.error('ERROR: play-service-account.json not found');
        process.exit(1);
    }

    if (!fs.existsSync(aabPath)) {
        console.error(`ERROR: AAB file not found at ${aabPath}`);
        process.exit(1);
    }

    console.log(`\n=== Google Play Upload (proxy mode) ===`);
    console.log(`Package:  ${PACKAGE_NAME}`);
    console.log(`Track:    ${config.label}`);
    console.log(`AAB:      ${aabPath}`);
    console.log();

    console.log('[1/4] Authenticating with service account...');
    const auth = new google.auth.GoogleAuth({
        keyFile: SERVICE_ACCOUNT_PATH,
        scopes: ['https://www.googleapis.com/auth/androidpublisher'],
    });

    // Use www.googleapis.com instead of androidpublisher.googleapis.com
    const play = google.androidpublisher({
        version: 'v3',
        auth,
        rootUrl: 'https://www.googleapis.com/',
    });

    console.log('[2/4] Creating edit...');
    const { data: edit } = await play.edits.insert({
        packageName: PACKAGE_NAME,
        requestBody: {},
    });
    const editId = edit.id;
    console.log(`       Edit ID: ${editId}`);

    const aabSize = (fs.statSync(aabPath).size / 1024 / 1024).toFixed(1);
    console.log(`[3/4] Uploading AAB (${aabSize} MB)...`);
    const { data: bundle } = await play.edits.bundles.upload({
        packageName: PACKAGE_NAME,
        editId,
        media: {
            mimeType: 'application/octet-stream',
            body: fs.createReadStream(aabPath),
        },
    });
    const versionCode = bundle.versionCode;
    console.log(`       Version code: ${versionCode}`);

    console.log(`[4/4] Assigning to ${track} track...`);
    await play.edits.tracks.update({
        packageName: PACKAGE_NAME,
        editId,
        track,
        requestBody: {
            track,
            releases: [{
                versionCodes: [versionCode],
                status: config.status,
            }],
        },
    });

    await play.edits.commit({
        packageName: PACKAGE_NAME,
        editId,
    });

    console.log(`\n=== SUCCESS ===`);
    console.log(`Version code ${versionCode} → ${config.label}`);
    console.log(`Check: https://play.google.com/console → E-Claw\n`);
}

main().catch(err => {
    console.error('\nUpload failed:', err.message);
    if (err.errors) {
        err.errors.forEach(e => console.error(' -', e.message));
    }
    process.exit(1);
});
