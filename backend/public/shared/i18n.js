const TRANSLATIONS = {
    en: {
        // Mission Control (mission.html)
        "mc_title": "E-Claw Mission Control",
        "mc_auth_title": "Mission Control",
        "mc_auth_subtitle": "Enter your device credentials to sync Dashboard",
        "mc_input_device_id": "Device ID",
        "mc_input_device_secret": "Device Secret",
        "mc_btn_connect": "Connect",
        "mc_auth_error_missing": "Please enter Device ID and Secret",
        "mc_refresh": "Refresh",
        "mc_save": "Save to Cloud",
        "mc_saving": "Uploading...",
        "mc_save_unsaved": "Save to Cloud *",
        "mc_todo_title": "📋 TODO List",
        "mc_btn_add": "+ Add",
        "mc_mission_title": "🚀 Mission List",
        "mc_done_title": "✅ Done List",
        "mc_notes_title": "📝 Notes (Bot Read-only)",
        "mc_rules_title": "📜 Rules (Workflow)",
        "mc_sync_unsaved": "* Unsaved changes",
        "mc_sync_synced": "Synced",
        "mc_empty_todo": "No TODO items",
        "mc_empty_mission": "No active missions",
        "mc_empty_done": "No completed items",
        "mc_empty_notes": "No notes",
        "mc_empty_rules": "No rules",
        "mc_status_pending": "Pending",
        "mc_status_inprogress": "In Progress",
        "mc_status_blocked": "Blocked",
        "mc_status_done": "Done",
        "mc_status_cancelled": "Cancelled",
        "mc_priority_low": "🟢 Low",
        "mc_priority_medium": "🟡 Med",
        "mc_priority_high": "🟠 High",
        "mc_priority_urgent": "🔴 Urgent",
        "mc_confirm_delete": "Are you sure you want to delete?",
        "mc_confirm_version": "Version conflict (You: v{you}, Server: v{server}). Download latest version?",
        "mc_dlg_add_todo": "Add TODO",
        "mc_dlg_edit": "Edit",
        "mc_dlg_title": "Title",
        "mc_dlg_desc": "Description",
        "mc_dlg_priority": "Priority",
        "mc_dlg_save": "Save",
        "mc_dlg_cancel": "Cancel",
        "mc_dlg_add_note": "Add Note",
        "mc_dlg_edit_note": "Edit Note",
        "mc_dlg_content": "Content",
        "mc_dlg_category": "Category",
        "mc_dlg_add_rule": "Add Rule",
        "mc_dlg_edit_rule": "Edit Rule",
        "mc_dlg_rule_name": "Rule Name",
        "mc_dlg_rule_type": "Type",
        "mc_menu_move_mission": "➡️ Move to Mission",
        "mc_menu_mark_done": "✅ Mark Done",
        "mc_menu_delete": "🗑️ Delete",

        // Portal Shared
        "portal_login_title": "E-Claw - Login",
        "portal_app_title": "E-Claw",
        "portal_app_subtitle": "Live Wallpaper Companion",
        "nav_dashboard": "Dashboard",
        "nav_chat": "Chat",
        "nav_mission": "Mission",
        "nav_settings": "Settings",
        "nav_logout": "Logout",

        // Login (index.html)
        "login_tab_login": "Login",
        "login_tab_register": "Register",
        "login_tab_device": "Device",
        "login_label_email": "Email",
        "login_label_password": "Password",
        "login_placeholder_email": "your@email.com",
        "login_placeholder_password": "Enter password",
        "login_btn_login": "Login",
        "login_btn_logging_in": "Logging in...",
        "login_link_forgot": "Forgot password?",
        "login_label_confirm": "Confirm Password",
        "login_hint_password": "Must contain both letters and numbers",
        "login_btn_create": "Create Account",
        "login_btn_creating": "Creating account...",
        "login_device_desc": "Already have the Android app? Enter your device credentials from Settings > Web Portal.",
        "login_btn_device": "Login with Device",
        "login_forgot_desc": "Enter your email and we'll send you a reset link.",
        "login_btn_reset_link": "Send Reset Link",
        "login_link_back": "Back to login",
        "login_reset_desc": "Enter your new password.",
        "login_btn_reset": "Reset Password",
        "login_msg_fill_all": "Please fill in all fields",
        "login_msg_pass_match": "Passwords do not match",

        // Dashboard (dashboard.html)
        "dash_title": "Dashboard",
        "dash_loading": "Loading entities...",
        "dash_empty_title": "No entities bound yet",
        "dash_empty_desc": "Add your first entity below to get started!",
        "dash_add_entity": "Add Entity",
        "dash_label_select_slot": "Select Entity Slot",
        "dash_btn_generate": "Generate Code",
        "dash_btn_generating": "Generating...",
        "dash_btn_regenerate": "Regenerate",
        "dash_btn_copy": "Copy Command",
        "dash_code_expired": "Code expired - generate a new one",
        "dash_code_expires_in": "Expires in {s}s",
        "dash_btn_remove": "Remove",
        "dash_remove_title": "Remove Entity",
        "dash_remove_desc": "Are you sure you want to remove <strong>{name}</strong> (#{id})? This will unbind the entity from your device.",
        "dash_slot_occupied": "Slot {id} is already occupied",

        // Settings (settings.html)
        "settings_title": "Settings",
        "settings_account": "Account",
        "settings_sub": "Subscription",
        "settings_sub_premium": "PREMIUM",
        "settings_sub_free": "FREE",
        "settings_usage_today": "Messages today",
        "settings_usage_unlimited": "sent today (Unlimited)",
        "settings_usage_limit": "{used}/{limit} sends today",
        "settings_renews_on": "Renews on {date}",
        "settings_price": "NT$99",
        "settings_period": "per month - Unlimited messages",
        "settings_btn_subscribe": "Subscribe",
        "settings_btn_hide_card": "Hide Card Form",
        "settings_label_card": "Card Number",
        "settings_label_expiry": "Expiry Date",
        "settings_label_ccv": "CCV",
        "settings_btn_pay": "Pay NT$99",
        "settings_processing": "Processing payment...",
        "settings_btn_cancel": "Cancel Subscription",
        "settings_btn_cancelling": "Cancelling...",
        "settings_cancel_title": "Cancel Subscription",
        "settings_cancel_desc": "Your premium access will remain active until the current billing period ends. After that, you'll revert to the free plan with 15 messages per day.",
        "settings_btn_keep": "Keep Subscription",
        "settings_lang": "Language",

        // Chat (chat.html)
        "chat_title": "Chat",
        "chat_filter_all": "All",
        "chat_filter_my": "My Messages",
        "chat_send_to": "Send to:",
        "chat_input_placeholder": "Type a message...",
        "chat_btn_send": "Send",
        "chat_btn_sending": "Sending...",
        "chat_empty": "No messages yet",
        "chat_empty_sub": "Send a message to your entities below",
        "chat_limit_reached": "Daily message limit reached. Upgrade to Premium for unlimited messages!"
    },
    zh: {
        // Mission Control (mission.html)
        "mc_title": "E-Claw Mission Control",
        "mc_auth_title": "Mission Control",
        "mc_auth_subtitle": "輸入你的裝置憑證來同步 Dashboard",
        "mc_input_device_id": "Device ID",
        "mc_input_device_secret": "Device Secret",
        "mc_btn_connect": "連線",
        "mc_auth_error_missing": "請輸入 Device ID 和 Secret",
        "mc_refresh": "重新整理",
        "mc_save": "儲存至雲端",
        "mc_saving": "上傳中...",
        "mc_save_unsaved": "儲存至雲端 *",
        "mc_todo_title": "📋 待辦事項",
        "mc_btn_add": "+ 新增",
        "mc_mission_title": "🚀 任務列表",
        "mc_done_title": "✅ 已完成",
        "mc_notes_title": "📝 筆記 (Bot 唯讀)",
        "mc_rules_title": "📜 規則 (Workflow)",
        "mc_sync_unsaved": "* 未儲存的變更",
        "mc_sync_synced": "已同步",
        "mc_empty_todo": "尚無待辦事項",
        "mc_empty_mission": "尚無進行中的任務",
        "mc_empty_done": "尚無已完成項目",
        "mc_empty_notes": "尚無筆記",
        "mc_empty_rules": "尚無規則",
        "mc_status_pending": "待處理",
        "mc_status_inprogress": "執行中",
        "mc_status_blocked": "阻塞中",
        "mc_status_done": "完成",
        "mc_status_cancelled": "已取消",
        "mc_priority_low": "🟢 低",
        "mc_priority_medium": "🟡 中",
        "mc_priority_high": "🟠 高",
        "mc_priority_urgent": "🔴 緊急",
        "mc_confirm_delete": "確定刪除？",
        "mc_confirm_version": "版本衝突 (你: v{you}, 伺服器: v{server})。要下載最新版本嗎？",
        "mc_dlg_add_todo": "新增待辦事項",
        "mc_dlg_edit": "編輯",
        "mc_dlg_title": "標題",
        "mc_dlg_desc": "描述",
        "mc_dlg_priority": "優先權",
        "mc_dlg_save": "儲存",
        "mc_dlg_cancel": "取消",
        "mc_dlg_add_note": "新增筆記",
        "mc_dlg_edit_note": "編輯筆記",
        "mc_dlg_content": "內容",
        "mc_dlg_category": "分類",
        "mc_dlg_add_rule": "新增規則",
        "mc_dlg_edit_rule": "編輯規則",
        "mc_dlg_rule_name": "規則名稱",
        "mc_dlg_rule_type": "類型",
        "mc_menu_move_mission": "➡️ 移至 Mission",
        "mc_menu_mark_done": "✅ 標記完成",
        "mc_menu_delete": "🗑️ 刪除",

        // Portal Shared
        "portal_login_title": "E-Claw - 登入",
        "portal_app_title": "E-Claw",
        "portal_app_subtitle": "動態桌布小幫手",
        "nav_dashboard": "儀表板",
        "nav_chat": "聊天",
        "nav_mission": "任務",
        "nav_settings": "設定",
        "nav_logout": "登出",

        // Login (index.html)
        "login_tab_login": "登入",
        "login_tab_register": "註冊",
        "login_tab_device": "裝置",
        "login_label_email": "Email",
        "login_label_password": "密碼",
        "login_placeholder_email": "your@email.com",
        "login_placeholder_password": "輸入密碼",
        "login_btn_login": "登入",
        "login_btn_logging_in": "登入中...",
        "login_link_forgot": "忘記密碼？",
        "login_label_confirm": "確認密碼",
        "login_hint_password": "必須包含字母和數字",
        "login_btn_create": "建立帳號",
        "login_btn_creating": "建立中...",
        "login_device_desc": "已經有 Android App 了嗎？從 設定 > 網頁版入口 輸入裝置憑證。",
        "login_btn_device": "使用裝置登入",
        "login_forgot_desc": "輸入 Email，我們將寄送重設連結給您。",
        "login_btn_reset_link": "發送重設連結",
        "login_link_back": "返回登入",
        "login_reset_desc": "輸入您的新密碼。",
        "login_btn_reset": "重設密碼",
        "login_msg_fill_all": "請填寫所有欄位",
        "login_msg_pass_match": "密碼不符",

        // Dashboard (dashboard.html)
        "dash_title": "儀表板",
        "dash_loading": "載入實體中...",
        "dash_empty_title": "尚未綁定實體",
        "dash_empty_desc": "在下方新增您的第一個實體以開始使用！",
        "dash_add_entity": "新增實體",
        "dash_label_select_slot": "選擇實體插槽",
        "dash_btn_generate": "產生代碼",
        "dash_btn_generating": "產生中...",
        "dash_btn_regenerate": "重新產生",
        "dash_btn_copy": "複製指令",
        "dash_code_expired": "代碼已過期 - 請重新產生",
        "dash_code_expires_in": "{s} 秒後過期",
        "dash_btn_remove": "移除",
        "dash_remove_title": "移除實體",
        "dash_remove_desc": "確定要移除 <strong>{name}</strong> (#{id}) 嗎？這將解除該實體與您裝置的綁定。",
        "dash_slot_occupied": "插槽 {id} 已被佔用",

        // Settings (settings.html)
        "settings_title": "設定",
        "settings_account": "帳號",
        "settings_sub": "訂閱",
        "settings_sub_premium": "高級會員",
        "settings_sub_free": "免費版",
        "settings_usage_today": "今日訊息",
        "settings_usage_unlimited": "今日已送 (無限制)",
        "settings_usage_limit": "今日 {used}/{limit} 則",
        "settings_renews_on": "續約日期：{date}",
        "settings_price": "NT$99",
        "settings_period": "每月 - 無限訊息",
        "settings_btn_subscribe": "訂閱",
        "settings_btn_hide_card": "隱藏卡片表格",
        "settings_label_card": "卡號",
        "settings_label_expiry": "到期日",
        "settings_label_ccv": "CCV",
        "settings_btn_pay": "支付 NT$99",
        "settings_processing": "處理付款中...",
        "settings_btn_cancel": "取消訂閱",
        "settings_btn_cancelling": "取消中...",
        "settings_cancel_title": "取消訂閱",
        "settings_cancel_desc": "您的高級會員資格將保留至本期結束。之後將恢復為免費版（每日 15 則訊息）。",
        "settings_btn_keep": "保留訂閱",
        "settings_lang": "語言",

        // Chat (chat.html)
        "chat_title": "聊天",
        "chat_filter_all": "全部",
        "chat_filter_my": "我的訊息",
        "chat_send_to": "傳送給：",
        "chat_input_placeholder": "輸入訊息...",
        "chat_btn_send": "傳送",
        "chat_btn_sending": "傳送中...",
        "chat_empty": "尚無訊息",
        "chat_empty_sub": "在下方傳送訊息給您的實體",
        "chat_limit_reached": "已達每日訊息上限。升級至高級會員以享受無限訊息！"
    }
};

class I18n {
    constructor() {
        const saved = localStorage.getItem('eclaw-language');
        if (saved) {
            this.lang = saved;
        } else {
            // Auto-detect from browser language
            const browserLang = (navigator.language || navigator.userLanguage || 'en').toLowerCase();
            this.lang = browserLang.startsWith('zh') ? 'zh' : 'en';
        }
        this.observers = [];
    }

    setLanguage(lang) {
        if (!TRANSLATIONS[lang]) return;
        this.lang = lang;
        localStorage.setItem('eclaw-language', lang);
        this.apply();
        this.observers.forEach(cb => cb(lang));
    }

    t(key, params = {}) {
        const dict = TRANSLATIONS[this.lang] || TRANSLATIONS['en'];
        let str = dict[key] || key;

        // Simple parameter replacement {name}
        Object.keys(params).forEach(k => {
            str = str.replace(new RegExp(`{${k}}`, 'g'), params[k]);
        });
        return str;
    }

    apply() {
        document.querySelectorAll('[data-i18n]').forEach(el => {
            const key = el.getAttribute('data-i18n');
            if (key) {
                // Check if it's an input/textarea placeholder or text content
                if (el.tagName === 'INPUT' || el.tagName === 'TEXTAREA') {
                    if (el.hasAttribute('placeholder')) {
                        el.placeholder = this.t(key);
                    }
                } else {
                    el.innerHTML = this.t(key);
                }
            }
        });

        // Update html lang attribute
        document.documentElement.lang = this.lang === 'zh' ? 'zh-TW' : 'en';
    }

    onLanguageChange(cb) {
        this.observers.push(cb);
    }
}

// Global instance
const i18n = new I18n();

// Auto-apply on load
document.addEventListener('DOMContentLoaded', () => {
    i18n.apply();
});
