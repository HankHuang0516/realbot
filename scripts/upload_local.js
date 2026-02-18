#!/usr/bin/env node
/**
 * Upload AAB to Google Play - Local version (no proxy)
 *
 * Usage:
 *   node scripts/upload_local.js [--track=internal|production] [path/to/app-release.aab]
 *
 * Examples:
 *   node scripts/upload_local.js --track=internal release_v1.0.11/app-release.aab
 *   node scripts/upload_local.js --track=production release_v1.0.11/app-release.aab
 *
 * Prerequisites:
 *   npm install googleapis
 *   play-service-account.json in project root
 */

const { google } = require('googleapis');
const fs = require('fs');
const path = require('path');

const PACKAGE_NAME = 'com.hank.clawlive';
const SERVICE_ACCOUNT_PATH = path.join(__dirname, '..', 'play-service-account.json');
const DEFAULT_AAB_PATH = path.join(__dirname, '..', 'release_v1.0.11', 'app-release.aab');

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
            aabPath = path.resolve(arg);
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
        console.error('ERROR: play-service-account.json not found in project root');
        console.error('       Expected at:', SERVICE_ACCOUNT_PATH);
        process.exit(1);
    }

    if (!fs.existsSync(aabPath)) {
        console.error(`ERROR: AAB file not found at ${aabPath}`);
        process.exit(1);
    }

    console.log(`\n=== Google Play Upload (local) ===`);
    console.log(`Package:  ${PACKAGE_NAME}`);
    console.log(`Track:    ${config.label}`);
    console.log(`AAB:      ${aabPath}`);
    console.log();

    console.log('[1/4] Authenticating with service account...');
    const auth = new google.auth.GoogleAuth({
        keyFile: SERVICE_ACCOUNT_PATH,
        scopes: ['https://www.googleapis.com/auth/androidpublisher'],
    });

    const play = google.androidpublisher({
        version: 'v3',
        auth,
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
