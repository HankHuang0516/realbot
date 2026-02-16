// E-Claw Portal - Navigation Bar

function renderNav(activePage) {
    const pages = [
        { id: 'dashboard', label: 'Dashboard', href: 'dashboard.html' },
        { id: 'chat', label: 'Chat', href: 'chat.html' },
        { id: 'mission', label: 'Mission', href: 'mission.html' },
        { id: 'settings', label: 'Settings', href: 'settings.html' }
    ];

    const nav = document.createElement('nav');
    nav.className = 'nav';
    nav.innerHTML = `
        <span class="nav-brand">E-Claw</span>
        <div class="nav-links">
            ${pages.map(p => `
                <a href="${p.href}" class="nav-link ${p.id === activePage ? 'active' : ''}">${p.label}</a>
            `).join('')}
        </div>
        <div class="nav-user">
            <span class="email" id="navEmail"></span>
            <button class="btn btn-outline btn-sm" onclick="logout()">Logout</button>
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
