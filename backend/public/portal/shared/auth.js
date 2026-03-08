// E-Claw Portal - Auth Guard
// Include on every page except index.html

let currentUser = null;

async function checkAuth() {
    try {
        const data = await apiCall('GET', '/api/auth/me');
        currentUser = data.user;
        window.currentUser = currentUser;

        // Restore language preference from server if not set locally
        if (currentUser.language && typeof i18n !== 'undefined') {
            const local = localStorage.getItem('eclaw-language');
            if (!local && currentUser.language !== 'en') {
                localStorage.setItem('eclaw-language', currentUser.language);
                i18n.lang = currentUser.language;
                i18n.apply();
            }
        }

        // Update nav email
        const emailEl = document.getElementById('navEmail');
        if (emailEl) emailEl.textContent = currentUser.email;

        return currentUser;
    } catch (e) {
        window.location.href = 'index.html';
        return null;
    }
}
