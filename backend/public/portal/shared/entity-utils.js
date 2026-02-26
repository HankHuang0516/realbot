// Shared entity avatar/name/label resolution
// Replaces duplicated ENTITY_CHARS across all portal pages
// Fixes #77 and #76: entity IDs 4+ showed '?' emoji

const ENTITY_CHARS_DEFAULT = {
    0: { name: 'Lobster', emoji: '\u{1F99E}' },
    1: { name: 'Pig', emoji: '\u{1F437}' },
    2: { name: 'Lobster', emoji: '\u{1F99E}' },
    3: { name: 'Pig', emoji: '\u{1F437}' }
};

// Shared state - populated by each page's entity load
let _entityAvatarMap = {};
let _entityNameMap = {};

/**
 * Call after fetching entities from API to populate shared maps.
 * @param {Array} entities - array of entity objects from /api/entities
 */
function updateEntityMaps(entities) {
    _entityAvatarMap = {};
    _entityNameMap = {};
    (entities || []).forEach(e => {
        if (e.avatar) _entityAvatarMap[e.entityId] = e.avatar;
        if (e.name) _entityNameMap[e.entityId] = e.name;
    });
}

/**
 * Get the avatar emoji for an entity.
 * Priority: server avatar > localStorage > ENTITY_CHARS_DEFAULT > fallback by modulo
 */
function getAvatarForEntity(entityId) {
    if (_entityAvatarMap[entityId]) return _entityAvatarMap[entityId];
    const saved = localStorage.getItem('eclaw_avatar_' + entityId);
    if (saved) return saved;
    const char = ENTITY_CHARS_DEFAULT[entityId];
    if (char) return char.emoji;
    // For entity IDs 4+, cycle through defaults by modulo
    return ENTITY_CHARS_DEFAULT[entityId % 4]?.emoji || '\u{1F99E}';
}

/**
 * Get the display name for an entity.
 * Priority: server name > ENTITY_CHARS_DEFAULT > fallback by parity
 */
function getEntityDisplayName(entityId) {
    if (_entityNameMap[entityId]) return _entityNameMap[entityId];
    const char = ENTITY_CHARS_DEFAULT[entityId];
    if (char) return char.name;
    return (entityId % 2 === 0 ? 'Lobster' : 'Pig');
}

/**
 * Get a full label like "🦞 Lobster (#4)" for an entity.
 * Uses the page's local escapeHtml/esc function if available.
 */
function getEntityLabel(entityId) {
    const avatar = getAvatarForEntity(entityId);
    const name = getEntityDisplayName(entityId);
    // Use whichever escape function is available on the page
    const escapeFn = typeof escapeHtml === 'function' ? escapeHtml
        : typeof esc === 'function' ? esc
        : (s) => s;
    return `${avatar} ${escapeFn(name)} (#${entityId})`;
}
