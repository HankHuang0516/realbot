// E-Claw Portal - Navigation Bar

function renderNav(activePage) {
    const pages = [
        { id: 'dashboard', i18nKey: 'nav_dashboard', label: 'Dashboard', href: 'dashboard.html' },
        { id: 'chat', i18nKey: 'nav_chat', label: 'Chat', href: 'chat.html' },
        { id: 'mission', i18nKey: 'nav_mission', label: 'Mission', href: 'mission.html' },
        { id: 'settings', i18nKey: 'nav_settings', label: 'Settings', href: 'settings.html' }
    ];

    const nav = document.createElement('nav');
    nav.className = 'nav';
    nav.innerHTML = `
        <span class="nav-brand">E-Claw</span>
        <div class="nav-links">
            ${pages.map(p => `
                <a href="${p.href}" class="nav-link ${p.id === activePage ? 'active' : ''}"
                   data-i18n="${p.i18nKey}">${typeof i18n !== 'undefined' ? i18n.t(p.i18nKey) : p.label}</a>
            `).join('')}
        </div>
        <div class="nav-user">
            <span class="email" id="navEmail"></span>
            <button class="btn btn-outline btn-sm" onclick="logout()" data-i18n="nav_logout">${typeof i18n !== 'undefined' ? i18n.t('nav_logout') : 'Logout'}</button>
        </div>
    `;

    document.body.insertBefore(nav, document.body.firstChild);
}

async function logout() {
    try {
        await apiCall('POST', '/api/auth/logout');
    } catch (e) {}
    window.location.href = 'index.html';
}

// Alias for pages that call doLogout()
function doLogout() { logout(); }
