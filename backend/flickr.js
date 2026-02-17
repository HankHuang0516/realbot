// Flickr Upload Service for Eclaw Chat Photos
// Uses flickr-sdk v6 (CommonJS) API
const Flickr = require('flickr-sdk');
const fs = require('fs');
const path = require('path');
const os = require('os');

const ALBUM_NAME = 'Eclaw';

let authPlugin = null;   // OAuth plugin for Upload
let flickrClient = null;  // REST API client
let cachedAlbumId = null;

/**
 * Initialize Flickr client with OAuth credentials
 * Returns true if initialized successfully
 */
function initFlickr() {
    const { FLICKR_API_KEY, FLICKR_API_SECRET, FLICKR_OAUTH_TOKEN, FLICKR_OAUTH_TOKEN_SECRET } = process.env;

    if (!FLICKR_API_KEY || !FLICKR_API_SECRET || !FLICKR_OAUTH_TOKEN || !FLICKR_OAUTH_TOKEN_SECRET) {
        console.warn('[Flickr] Missing credentials - photo upload disabled');
        console.warn('[Flickr] Required: FLICKR_API_KEY, FLICKR_API_SECRET, FLICKR_OAUTH_TOKEN, FLICKR_OAUTH_TOKEN_SECRET');
        return false;
    }

    try {
        authPlugin = Flickr.OAuth.createPlugin(
            FLICKR_API_KEY,
            FLICKR_API_SECRET,
            FLICKR_OAUTH_TOKEN,
            FLICKR_OAUTH_TOKEN_SECRET
        );
        flickrClient = new Flickr(authPlugin);
        console.log('[Flickr] Client initialized successfully');
        return true;
    } catch (err) {
        console.error('[Flickr] Failed to initialize:', err.message);
        return false;
    }
}

/**
 * Retry wrapper for Flickr API calls that may hit rate limits (429)
 * Retries up to maxRetries times with exponential backoff
 */
async function withRetry(fn, label, maxRetries = 3) {
    for (let attempt = 0; attempt <= maxRetries; attempt++) {
        try {
            return await fn();
        } catch (err) {
            const status = err.statusCode || err.status || (err.response && err.response.status);
            if (status === 429 && attempt < maxRetries) {
                const delay = Math.pow(2, attempt) * 1000; // 1s, 2s, 4s
                console.warn(`[Flickr] ${label}: Rate limited (429), retrying in ${delay}ms (attempt ${attempt + 1}/${maxRetries})`);
                await new Promise(r => setTimeout(r, delay));
            } else {
                throw err;
            }
        }
    }
}

/**
 * Find or create the "Eclaw" photoset/album
 * @param {string} primaryPhotoId - Required when creating a new album
 * @returns {string|null} photoset ID
 */
async function getOrCreateAlbum(primaryPhotoId = null) {
    if (cachedAlbumId) return cachedAlbumId;
    if (!flickrClient) return null;

    try {
        const res = await withRetry(
            () => flickrClient.photosets.getList(),
            'getList'
        );
        const photosets = res.body.photosets.photoset || [];
        const existing = photosets.find(p => p.title._content === ALBUM_NAME);

        if (existing) {
            cachedAlbumId = existing.id;
            console.log(`[Flickr] Found album "${ALBUM_NAME}" (ID: ${cachedAlbumId})`);
            return cachedAlbumId;
        }

        // Create album if we have a photo to use as primary
        if (primaryPhotoId) {
            const createRes = await withRetry(
                () => flickrClient.photosets.create({
                    title: ALBUM_NAME,
                    description: 'Eclaw Chat Photos',
                    primary_photo_id: primaryPhotoId
                }),
                'createAlbum'
            );
            cachedAlbumId = createRes.body.photoset.id;
            console.log(`[Flickr] Created album "${ALBUM_NAME}" (ID: ${cachedAlbumId})`);
            return cachedAlbumId;
        }

        console.log(`[Flickr] Album "${ALBUM_NAME}" not found, will create on first upload`);
        return null;
    } catch (err) {
        console.error('[Flickr] Failed to get/create album:', err.message);
        return null;
    }
}

/**
 * Upload a photo to Flickr and add to Eclaw album
 * @param {Buffer} buffer - Image file buffer
 * @param {string} filename - Original filename
 * @returns {{ success: boolean, url?: string, photoId?: string, error?: string }}
 */
async function uploadPhoto(buffer, filename) {
    if (!authPlugin || !flickrClient) {
        return { success: false, error: 'Flickr client not initialized' };
    }

    try {
        // Write buffer to temp file (flickr-sdk requires file path)
        const tempPath = path.join(os.tmpdir(), `eclaw_${Date.now()}_${filename}`);
        fs.writeFileSync(tempPath, buffer);

        // Upload to Flickr using Flickr.Upload constructor (returns thenable)
        const uploadRes = await new Flickr.Upload(authPlugin, tempPath, {
            title: `eclaw_${Date.now()}`,
            tags: 'eclaw chat',
            is_public: 1,
            hidden: 2  // Hidden from public searches
        });

        // Clean up temp file
        try { fs.unlinkSync(tempPath); } catch (_) {}

        // Parse photo ID from upload response
        const photoId = uploadRes.body.photoid._content || uploadRes.body.photoid;
        console.log(`[Flickr] Uploaded photo ID: ${photoId}`);

        // Get photo info to construct URL (lighter than getSizes, avoids rate limits)
        const infoRes = await withRetry(
            () => flickrClient.photos.getInfo({ photo_id: photoId }),
            'getInfo'
        );
        const photo = infoRes.body.photo;
        // Construct static URL: _b = Large 1024px
        const photoUrl = `https://live.staticflickr.com/${photo.server}/${photo.id}_${photo.secret}_b.jpg`;
        console.log(`[Flickr] Photo URL: ${photoUrl}`);

        // Add to album (fire-and-forget, don't block on this)
        addToAlbumAsync(photoId);

        return { success: true, url: photoUrl, photoId };

    } catch (err) {
        console.error('[Flickr] Upload error:', err.message);
        return { success: false, error: err.message };
    }
}

/**
 * Add photo to album asynchronously (non-blocking)
 */
async function addToAlbumAsync(photoId) {
    try {
        const albumId = await getOrCreateAlbum(photoId);
        if (albumId) {
            await withRetry(
                () => flickrClient.photosets.addPhoto({
                    photoset_id: albumId,
                    photo_id: photoId
                }),
                'addPhoto'
            );
            console.log(`[Flickr] Added photo ${photoId} to album ${albumId}`);
        }
    } catch (err) {
        // Photo might already be in album (if it was used to create it)
        if (!err.message.includes('already in')) {
            console.warn('[Flickr] Failed to add to album:', err.message);
        }
    }
}

/**
 * Check if Flickr is available
 */
function isAvailable() {
    return authPlugin !== null && flickrClient !== null;
}

module.exports = { initFlickr, uploadPhoto, isAvailable };
