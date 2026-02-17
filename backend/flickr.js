// Flickr Upload Service for Eclaw Chat Photos
// Pattern follows HankHuang0516/wishlist-app flickr integration
const { createFlickr } = require('flickr-sdk');
const fs = require('fs');
const path = require('path');
const os = require('os');

const ALBUM_NAME = 'Eclaw';

let flickr = null;    // REST API function: flickr('flickr.method', { params })
let upload = null;     // Upload function: upload(filePath, { params })
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
        const result = createFlickr({
            consumerKey: FLICKR_API_KEY,
            consumerSecret: FLICKR_API_SECRET,
            oauthToken: FLICKR_OAUTH_TOKEN,
            oauthTokenSecret: FLICKR_OAUTH_TOKEN_SECRET
        });
        flickr = result.flickr;
        upload = result.upload;
        console.log('[Flickr] Client initialized successfully');
        return true;
    } catch (err) {
        console.error('[Flickr] Failed to initialize:', err.message);
        return false;
    }
}

/**
 * Find or create the "Eclaw" photoset/album
 * @param {string} primaryPhotoId - Required when creating a new album
 * @returns {string|null} photoset ID
 */
async function getOrCreateAlbum(primaryPhotoId = null) {
    if (cachedAlbumId) return cachedAlbumId;
    if (!flickr) return null;

    try {
        const res = await flickr('flickr.photosets.getList', {});
        const photosets = res.photosets.photoset || [];
        const existing = photosets.find(p => p.title._content === ALBUM_NAME);

        if (existing) {
            cachedAlbumId = existing.id;
            console.log(`[Flickr] Found album "${ALBUM_NAME}" (ID: ${cachedAlbumId})`);
            return cachedAlbumId;
        }

        // Create album if we have a photo to use as primary
        if (primaryPhotoId) {
            const createRes = await flickr('flickr.photosets.create', {
                title: ALBUM_NAME,
                description: 'Eclaw Chat Photos',
                primary_photo_id: primaryPhotoId
            });
            cachedAlbumId = createRes.photoset.id;
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
    if (!upload || !flickr) {
        return { success: false, error: 'Flickr client not initialized' };
    }

    try {
        // Write buffer to temp file (flickr-sdk requires file path)
        const tempPath = path.join(os.tmpdir(), `eclaw_${Date.now()}_${filename}`);
        fs.writeFileSync(tempPath, buffer);

        // Upload to Flickr
        const uploadRes = await upload(tempPath, {
            title: `eclaw_${Date.now()}`,
            tags: 'eclaw chat',
            is_public: '1',
            hidden: '2'  // Hidden from public searches
        });

        // Clean up temp file
        try { fs.unlinkSync(tempPath); } catch (_) {}

        // Get photo ID from upload response
        const photoId = uploadRes.id || uploadRes;
        console.log(`[Flickr] Uploaded photo ID: ${photoId}`);

        // Add to album
        const albumId = await getOrCreateAlbum(photoId);
        if (albumId) {
            try {
                await flickr('flickr.photosets.addPhoto', {
                    photoset_id: albumId,
                    photo_id: photoId
                });
                console.log(`[Flickr] Added photo ${photoId} to album ${albumId}`);
            } catch (albumErr) {
                // Photo might already be in album (if it was used to create it)
                if (!albumErr.message.includes('already in')) {
                    console.warn('[Flickr] Failed to add to album:', albumErr.message);
                }
            }
        }

        // Get photo sizes to find a suitable URL
        const sizesRes = await flickr('flickr.photos.getSizes', { photo_id: photoId });
        const sizes = sizesRes.sizes.size || [];

        // Prefer Large (1024px), fallback to Medium 800, Original
        const preferredLabels = ['Large', 'Medium 800', 'Original', 'Medium 640', 'Medium'];
        let photoUrl = null;
        for (const label of preferredLabels) {
            const size = sizes.find(s => s.label === label);
            if (size) {
                photoUrl = size.source;
                break;
            }
        }

        // Last resort: use the largest available
        if (!photoUrl && sizes.length > 0) {
            photoUrl = sizes[sizes.length - 1].source;
        }

        if (!photoUrl) {
            return { success: false, error: 'Could not get photo URL after upload' };
        }

        console.log(`[Flickr] Photo URL: ${photoUrl}`);
        return { success: true, url: photoUrl, photoId };

    } catch (err) {
        console.error('[Flickr] Upload error:', err.message);
        return { success: false, error: err.message };
    }
}

/**
 * Check if Flickr is available
 */
function isAvailable() {
    return flickr !== null && upload !== null;
}

module.exports = { initFlickr, uploadPhoto, isAvailable };
