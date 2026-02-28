// ============================================
// E-Claw AI Chat Widget
// Floating chat button + expandable panel
// Loaded on all authenticated portal pages
// ============================================
(function () {
    'use strict';

    const STORAGE_KEY = 'eclaw_ai_chat_history';
    const MAX_HISTORY = 20;

    let chatHistory = [];
    let isOpen = false;
    let isLoading = false;

    // ── localStorage ──────────────────────────
    function loadHistory() {
        try {
            chatHistory = JSON.parse(localStorage.getItem(STORAGE_KEY)) || [];
        } catch (_) { chatHistory = []; }
    }

    function saveHistory() {
        try { localStorage.setItem(STORAGE_KEY, JSON.stringify(chatHistory.slice(-MAX_HISTORY))); }
        catch (_) {}
    }

    function getCurrentPage() {
        const m = window.location.pathname.match(/\/([^/]+)\.html/);
        return m ? m[1] : 'unknown';
    }

    // ── Build DOM ─────────────────────────────
    function createWidget() {
        // FAB
        const fab = document.createElement('button');
        fab.id = 'aiChatFab';
        fab.className = 'ai-chat-fab';
        fab.innerHTML = '<span class="ai-chat-fab-icon">\u{1F916}</span>';
        fab.title = 'AI Assistant';
        fab.addEventListener('click', toggleChat);

        // Panel
        const panel = document.createElement('div');
        panel.id = 'aiChatPanel';
        panel.className = 'ai-chat-panel';
        panel.style.display = 'none';
        panel.innerHTML = `
            <div class="ai-chat-header">
                <div class="ai-chat-header-left">
                    <span class="ai-chat-header-icon">\u{1F916}</span>
                    <span class="ai-chat-header-title">E-Claw AI</span>
                </div>
                <div class="ai-chat-header-right">
                    <button class="ai-chat-clear-btn" title="Clear history">\u{1F5D1}\uFE0F</button>
                    <button class="ai-chat-close-btn" title="Close">&times;</button>
                </div>
            </div>
            <div class="ai-chat-messages" id="aiChatMessages"></div>
            <div class="ai-chat-action-bar" id="aiChatActionBar" style="display:none"></div>
            <div class="ai-chat-input-area">
                <textarea id="aiChatInput" class="ai-chat-input" rows="1"
                          placeholder="Ask a question..." maxlength="2000"></textarea>
                <button id="aiChatSend" class="ai-chat-send-btn" disabled>
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <line x1="22" y1="2" x2="11" y2="13"></line>
                        <polygon points="22 2 15 22 11 13 2 9 22 2"></polygon>
                    </svg>
                </button>
            </div>`;

        document.body.appendChild(fab);
        document.body.appendChild(panel);

        // Events
        panel.querySelector('.ai-chat-close-btn').addEventListener('click', toggleChat);
        panel.querySelector('.ai-chat-clear-btn').addEventListener('click', () => {
            chatHistory = [];
            localStorage.removeItem(STORAGE_KEY);
            renderMessages();
        });

        const input = document.getElementById('aiChatInput');
        const sendBtn = document.getElementById('aiChatSend');

        input.addEventListener('input', () => {
            sendBtn.disabled = !input.value.trim() || isLoading;
            input.style.height = 'auto';
            input.style.height = Math.min(input.scrollHeight, 100) + 'px';
        });

        input.addEventListener('keydown', (e) => {
            if (e.key === 'Enter' && !e.shiftKey) {
                e.preventDefault();
                if (!isLoading && input.value.trim()) sendMessage();
            }
        });

        sendBtn.addEventListener('click', sendMessage);

        renderMessages();
    }

    // ── Toggle ────────────────────────────────
    function toggleChat() {
        isOpen = !isOpen;
        const panel = document.getElementById('aiChatPanel');
        const fab = document.getElementById('aiChatFab');
        if (isOpen) {
            panel.style.display = 'flex';
            fab.classList.add('ai-chat-fab-hidden');
            document.getElementById('aiChatInput').focus();
            scrollToBottom();
        } else {
            panel.style.display = 'none';
            fab.classList.remove('ai-chat-fab-hidden');
        }
    }

    // ── Render ─────────────────────────────────
    function renderMessages() {
        const container = document.getElementById('aiChatMessages');
        if (!container) return;
        container.innerHTML = '';

        if (chatHistory.length === 0) {
            container.innerHTML = `
                <div class="ai-chat-welcome">
                    <div class="ai-chat-welcome-icon">\u{1F99E}</div>
                    <div class="ai-chat-welcome-text">Hi! I'm E-Claw AI. Ask me anything about the platform.</div>
                </div>`;
            return;
        }

        for (const msg of chatHistory) {
            const div = document.createElement('div');
            div.className = 'ai-chat-msg ai-chat-msg-' + msg.role;
            div.innerHTML = msg.role === 'user' ? escapeHtml(msg.content) : renderMarkdown(msg.content);
            container.appendChild(div);
        }
    }

    function escapeHtml(str) {
        const d = document.createElement('div');
        d.textContent = str;
        return d.innerHTML;
    }

    function renderMarkdown(text) {
        if (!text) return '';
        let h = escapeHtml(text);
        h = h.replace(/```(\w*)\n([\s\S]*?)```/g, '<pre><code>$2</code></pre>');
        h = h.replace(/`([^`]+)`/g, '<code class="ai-chat-inline-code">$1</code>');
        h = h.replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>');
        h = h.replace(/\[([^\]]+)\]\(([^)]+)\)/g, '<a href="$2" target="_blank" rel="noopener">$1</a>');
        h = h.replace(/\n/g, '<br>');
        return h;
    }

    function scrollToBottom() {
        const c = document.getElementById('aiChatMessages');
        if (c) setTimeout(() => { c.scrollTop = c.scrollHeight; }, 50);
    }

    function showTyping() {
        const c = document.getElementById('aiChatMessages');
        const d = document.createElement('div');
        d.className = 'ai-chat-msg ai-chat-msg-assistant ai-chat-typing';
        d.id = 'aiChatTyping';
        d.innerHTML = '<span class="ai-chat-dot"></span><span class="ai-chat-dot"></span><span class="ai-chat-dot"></span>';
        c.appendChild(d);
        scrollToBottom();
    }

    function hideTyping() {
        const el = document.getElementById('aiChatTyping');
        if (el) el.remove();
    }

    // ── Send ──────────────────────────────────
    async function sendMessage() {
        const input = document.getElementById('aiChatInput');
        const text = input.value.trim();
        if (!text || isLoading) return;

        chatHistory.push({ role: 'user', content: text });
        saveHistory();
        renderMessages();
        scrollToBottom();

        input.value = '';
        input.style.height = 'auto';
        document.getElementById('aiChatSend').disabled = true;
        isLoading = true;
        showTyping();

        try {
            const data = await apiCall('POST', '/api/ai-support/chat', {
                message: text,
                history: chatHistory.slice(-MAX_HISTORY),
                page: getCurrentPage()
            });

            hideTyping();

            if (data.response) {
                chatHistory.push({ role: 'assistant', content: data.response });
                saveHistory();
                renderMessages();
                scrollToBottom();
            }

            if (data.actions && data.actions.length > 0) {
                showActionBar(data.actions);
            }
        } catch (err) {
            hideTyping();
            chatHistory.push({ role: 'assistant', content: 'Sorry, something went wrong. Please try again.' });
            saveHistory();
            renderMessages();
            scrollToBottom();
        } finally {
            isLoading = false;
            document.getElementById('aiChatInput').focus();
        }
    }

    // ── Admin Actions ─────────────────────────
    function showActionBar(actions) {
        const bar = document.getElementById('aiChatActionBar');
        bar.style.display = 'block';
        bar.innerHTML = '';
        for (const action of actions) {
            if (action.type === 'create_issue') {
                const btn = document.createElement('button');
                btn.className = 'ai-chat-action-btn';
                btn.textContent = '\u{1F4CB} Create Issue: "' + (action.title || '').substring(0, 50) + '"';
                btn.addEventListener('click', () => confirmCreateIssue(action));
                bar.appendChild(btn);
            }
        }
    }

    function confirmCreateIssue(action) {
        if (!confirm('Create GitHub issue?\n\nTitle: ' + action.title + '\n\nBody:\n' + (action.body || '').substring(0, 300) + '...')) return;

        apiCall('POST', '/api/ai-support/create-issue', {
            title: action.title,
            body: action.body,
            labels: action.labels
        }).then(data => {
            if (data.success) {
                chatHistory.push({ role: 'assistant', content: 'GitHub issue #' + data.number + ' created: ' + data.url });
                saveHistory();
                renderMessages();
                scrollToBottom();
                if (typeof showToast === 'function') showToast('Issue #' + data.number + ' created!', 'success');
            }
        }).catch(err => {
            if (typeof showToast === 'function') showToast('Failed to create issue', 'error');
        });

        document.getElementById('aiChatActionBar').style.display = 'none';
    }

    // ── Init ──────────────────────────────────
    function init() {
        const check = setInterval(() => {
            if (window.currentUser) {
                clearInterval(check);
                loadHistory();
                createWidget();
            }
        }, 200);
        setTimeout(() => clearInterval(check), 10000);
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }
})();
