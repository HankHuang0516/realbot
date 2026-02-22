# E-claw 電子蝦

> **復古電子寵物 × AI 動態桌布** | Retro E-Pet meets AI Live Wallpaper

[![Release](https://img.shields.io/github/v/release/HankHuang0516/realbot)](https://github.com/HankHuang0516/realbot/releases/latest)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![Platform](https://img.shields.io/badge/platform-Android-green.svg)](https://www.android.com)
[![Backend](https://img.shields.io/badge/backend-Railway-purple.svg)](https://railway.app)

把 90 年代電子雞的靈魂注入你的 Android 桌布——由 AI Bot 驅動，24/7 陪伴你。

*Bring your Android wallpaper to life with a 90s tamagotchi soul — powered by AI Bots, 24/7.*

---

## ✨ Features / 功能特色

| 功能 | 說明 |
|------|------|
| 🦐 **AI 電子寵物桌布** | 最多 4 個 AI 驅動的實體，在你的動態桌布上自由活動 |
| 🤖 **OpenClaw Bot 整合** | 透過 Webhook + Exec-curl 與 AI Bot 雙向溝通 |
| 💬 **即時聊天** | 長按桌布即可與實體對話，支援完整訊息歷程 |
| 🔔 **Push 通知** | Bot 主動推送訊息，instruction-first 格式 |
| 📊 **Web 管理介面** | 跨裝置管理實體、查看狀態、遠端控制 |
| 📡 **裝置遙測** | 結構化 debug buffer，AI 輔助問題排查 |
| 🔐 **帳號登入** | 綁定 Google 帳號，重裝後自動恢復資料 |
| 📈 **免費 / 付費方案** | 內建 gatekeeper，15 則免費訊息 + 付費無限制 |

---

## 🏗️ Architecture / 架構

```
┌─────────────────────┐     ┌──────────────────────┐
│   Android App       │     │   Web Portal         │
│   (Kotlin)          │     │   (HTML/JS)          │
│                     │     │                      │
│  Live Wallpaper     │     │  Entity Management   │
│  Chat UI            │◄────►  Bot Config          │
│  Push Receiver      │     │  Telemetry Viewer    │
└─────────┬───────────┘     └────────┬─────────────┘
          │                          │
          │    HTTPS / REST API      │
          ▼                          ▼
┌─────────────────────────────────────────────────┐
│              Backend (Railway)                   │
│              Node.js + Express                   │
│                                                  │
│  /api/bind      /api/broadcast   /api/transform  │
│  /api/chat      /api/logs        /api/telemetry  │
│                                                  │
│         PostgreSQL (persistent store)            │
└────────────────────────┬────────────────────────┘
                         │  Webhook Push + exec+curl
                         ▼
              ┌──────────────────────┐
              │  OpenClaw Platform   │
              │  (Zeabur)            │
              │                      │
              │  AI Bot Instances    │
              │  (up to 4 per device)│
              └──────────────────────┘
```

- **4 entity slots** per device (0–3), independently bindable
- **Bots** communicate via Webhook push (incoming) + exec+curl (POST /api/transform)
- **Railway** auto-deploys on push to `main` (watches `backend/` folder)

---

## 🚀 Quick Start / 快速開始

### Prerequisites

- Android 8.0+ device
- Node.js 18+
- PostgreSQL (or use Railway's managed PostgreSQL)

### Local Backend Development

```bash
# Clone the repo
git clone https://github.com/HankHuang0516/realbot.git
cd realbot/backend

# Install dependencies
npm install

# Configure environment
cp .env.example .env   # then fill in DATABASE_URL, etc.

# Start dev server
npm run dev
# → Server running on http://localhost:3000
```

### Deploy to Railway

```bash
# Connect repo to Railway (auto-deploys from backend/ on push to main)
# Set environment variables in Railway dashboard:
#   DATABASE_URL  PORT  NODE_ENV
git push origin main
```

### Android App

1. Download the latest `.aab` / `.apk` from [GitHub Releases](https://github.com/HankHuang0516/realbot/releases/latest)
2. Set as Live Wallpaper → Long Settings → enter your `deviceId`
3. Open the Web Portal to bind AI entities

---

## 📁 Project Structure / 專案結構

```
realbot/
├── app/                    # Android app (Kotlin)
│   └── src/main/
│       ├── java/           # App source code
│       └── res/            # Resources, layouts, strings
├── backend/                # Node.js backend (Railway)
│   ├── index.js            # Express server entry point
│   ├── public/             # Web Portal (HTML/JS/CSS)
│   ├── device-telemetry.js # Telemetry buffer module
│   └── tests/              # Regression test suite
├── google_play/            # Store assets (icon, feature graphic)
├── RELEASE_HISTORY.md      # Version history with commit hashes
├── PRIVACY_POLICY.md       # Privacy policy
└── CLAUDE.md               # AI assistant instructions
```

---

## 🧪 Regression Tests / 回歸測試

```bash
# Bot API response rate (target: 90%+)
node backend/tests/test-bot-api-response.js

# Full broadcast flow (delivery, speak-to, chat history)
node backend/tests/test-broadcast.js
```

Requires `TEST_DEVICE_ID` / `BROADCAST_TEST_DEVICE_ID` + `BROADCAST_TEST_DEVICE_SECRET` in `backend/.env`.

---

## 📖 Documentation / 文件

- [Privacy Policy 隱私權政策](PRIVACY_POLICY.md)
- [Backend API Reference](backend/README.md)
- [MCP Skill Guide](backend/E-claw_mcp_skill.md)
- [Release History](RELEASE_HISTORY.md)

---

## 🤝 Contributing / 貢獻

This project is primarily a personal/experimental project. Issues and suggestions are welcome!

1. Fork the repo
2. Create a feature branch: `git checkout -b feat/your-feature`
3. Commit your changes
4. Open an issue to discuss before sending a PR

**Feature Parity Rule**: All user-facing features must be kept in sync between Web Portal and Android App.

---

## 📄 License

[MIT License](LICENSE) © 2026 HankHuang0516
