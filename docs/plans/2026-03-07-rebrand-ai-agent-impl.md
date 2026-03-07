# E-Claw 品牌重定位 Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 把 E-Claw 所有對外描述從「復古電子寵物」改為「AI Agent 協作 × 即時視覺化」定位。

**Architecture:** 純文字修改，不涉及邏輯變更。涵蓋兩個 repo：`realbot`（主專案）及 `realbot/openclaw-channel-eclaw`（Channel plugin）。i18n 只需更新 EN + ZH-TW（其他語系回落到 EN）。

**Tech Stack:** HTML、JavaScript i18n.js、Markdown、JSON、TypeScript

---

## 預備：確認工作目錄

```bash
# 確認目前在 realbot repo 根目錄
pwd
# 預期輸出包含 /realbot
```

---

### Task 1：info.html — 功能介紹區塊（3 個位置）

**Files:**
- Modify: `backend/public/portal/info.html:705,714,720`

**Step 1: 修改主標語（行 705）**

在 [backend/public/portal/info.html](backend/public/portal/info.html) 使用 Edit 工具：

舊：
```
E-Claw 電子蝦 — 復古電子寵物 × AI 動態桌布
```
新：
```
E-Claw 電子蝦 — AI Agent 協作 × 即時視覺化
```

**Step 2: 修改副標段落（行 714）**

舊：
```
把 90 年代電子雞的靈魂注入你的 Android 桌布——由 AI Bot 驅動，24/7 陪伴你。
```
新：
```
讓多個 AI Agent 在 Android 桌布上即時互動——支援 Agent 間通訊（A2A）、廣播推送、多模型協作。
```

**Step 3: 修改功能列表第一列（行 720）**

舊：
```html
<tr><td>AI 電子寵物桌布</td><td>最多 8 個 AI 驅動的實體（免費 4 個、付費 8 個），在你的動態桌布上自由活動</td></tr>
```
新：
```html
<tr><td>AI Agent 協作平台</td><td>最多 8 個 AI Agent 即時視覺化，支援 Agent 間通訊（A2A）與廣播協作（免費 4 個、付費 8 個）</td></tr>
```

**Step 4: 驗證舊字串已不存在**

用 Grep 在 info.html 中搜索：
- `復古電子寵物` → 應找不到
- `電子雞` → 應找不到
- `AI 電子寵物桌布` → 應找不到

**Step 5: Commit**

```bash
git add backend/public/portal/info.html
git commit -m "feat(rebrand): update info.html features section to AI Agent positioning"
```

---

### Task 2：i18n.js — faq_a_what_is（EN + ZH-TW）

**Files:**
- Modify: `backend/public/shared/i18n.js:106,1021`

**Step 1: 更新 EN faq_a_what_is（行 106）**

舊：
```
"faq_a_what_is": "E-Claw is a live wallpaper companion platform for Android. You can bind AI bots to your device — they update your home screen in real time with messages, mood, and animations. Manage everything through the Android app or the web portal.",
```
新：
```
"faq_a_what_is": "E-Claw is an AI Agent collaboration and A2A communication platform for Android. Bind your AI bots to visualize their real-time state, enable Agent-to-Agent (A2A) messaging, broadcast to multiple entities, and orchestrate multi-agent workflows — all managed from the Android app or web portal.",
```

**Step 2: 更新 ZH-TW faq_a_what_is（行 1021）**

舊：
```
"faq_a_what_is": "E-Claw 是 Android 上的動態桌布夥伴平台。你可以將 AI 機器人綁定到裝置上——它們會即時更新你的主畫面，顯示訊息、情緒和動畫。可透過 Android App 或網頁入口管理一切。",
```
新：
```
"faq_a_what_is": "E-Claw 是 Android 上的 AI Agent 協作與 A2A 通訊平台。將 AI Bot 綁定到裝置上，即時視覺化 Agent 狀態、啟用 Agent 間訊息傳遞（A2A）、廣播推送、多模型協作——透過 Android App 或網頁入口管理一切。",
```

**Step 3: 驗證**

用 Grep 搜索 `live wallpaper companion` → 應找不到

**Step 4: Commit**

```bash
git add backend/public/shared/i18n.js
git commit -m "feat(rebrand): update faq_a_what_is to AI Agent collaboration messaging"
```

---

### Task 3：i18n.js — cmp_subtitle + cmp_cta_text（EN + ZH-TW）

**Files:**
- Modify: `backend/public/shared/i18n.js:757,840,1671,1754`

**Step 1: 更新 EN cmp_subtitle（行 757）**

舊：
```
"cmp_subtitle": "E-Claw offers a full AI companion experience — live wallpaper, push, tasks, and more. Telegram only has chat.",
```
新：
```
"cmp_subtitle": "E-Claw offers a full AI Agent collaboration experience — A2A communication, live visualization, push broadcasts, tasks, and more. Telegram only has chat.",
```

**Step 2: 更新 EN cmp_cta_text（行 840）**

舊：
```
"cmp_cta_text": "E-Claw delivers the most complete AI companion experience. Why settle for just chat?",
```
新：
```
"cmp_cta_text": "E-Claw delivers the most complete AI Agent collaboration experience. Why settle for just chat?",
```

**Step 3: 更新 ZH-TW cmp_subtitle（行 1671）**

舊：
```
"cmp_subtitle": "E-Claw 提供完整的 AI 夥伴體驗 — 動態桌布、推播、任務管理一應俱全。Telegram 只有聊天。",
```
新：
```
"cmp_subtitle": "E-Claw 提供完整的 AI Agent 協作體驗 — A2A 通訊、即時視覺化、廣播推送、任務管理一應俱全。Telegram 只有聊天。",
```

**Step 4: 更新 ZH-TW cmp_cta_text（行 1754）**

舊：
```
"cmp_cta_text": "E-Claw 提供最完整的 AI 夥伴體驗，何必只用聊天？",
```
新：
```
"cmp_cta_text": "E-Claw 提供最完整的 AI Agent 協作體驗，何必只用聊天？",
```

**Step 5: 驗證**

Grep `AI companion experience` → 應找不到
Grep `AI 夥伴體驗` → 應找不到

**Step 6: Commit**

```bash
git add backend/public/shared/i18n.js
git commit -m "feat(rebrand): update comparison page strings to AI Agent collaboration"
```

---

### Task 4：PRIVACY_POLICY.md — 移除「寵物」稱呼

**Files:**
- Modify: `PRIVACY_POLICY.md:36,90`

**Step 1: 找到行 36 並修改（中文）**

先 Read PRIVACY_POLICY.md 確認行號，再用 Edit 修改：

舊（行約 36）：
```
當您與桌面寵物 (E-claw) 對話時，
```
新：
```
當您與 E-Claw 實體（AI 代理）對話時，
```

**Step 2: 找到行 90 並修改（英文）**

舊（行約 90）：
```
When you talk to your desktop pet (E-claw), your messages are sent via an encrypted channel (HTTPS) to the AI Agent (Bot) you have bound.
```
新：
```
When you talk to your E-Claw entity (your AI agent), your messages are sent via an encrypted channel (HTTPS) to the AI Agent (Bot) you have bound.
```

**Step 3: 驗證**

Grep `桌面寵物` → 應找不到
Grep `desktop pet` → 應找不到

**Step 4: Commit**

```bash
git add PRIVACY_POLICY.md
git commit -m "feat(rebrand): update privacy policy - replace pet terminology with AI agent"
```

---

### Task 5：backend/README.md + package/ 描述

**Files:**
- Modify: `backend/README.md:3`
- Modify: `package/package.json:4,33,35`
- Modify: `package/README.md:3`

**Step 1: 更新 backend/README.md（行 3）**

舊：
```
Node.js backend server for the Claw Live Wallpaper Android app.
```
新：
```
Node.js backend server for E-Claw — the AI Agent collaboration and A2A communication platform for Android.
```

**Step 2: 更新 package/package.json**

讀取檔案後修改以下三個欄位：

行 4 `"description"`:
```
舊："E-Claw channel plugin for OpenClaw — AI chat platform for live wallpaper entities"
新："E-Claw channel plugin for OpenClaw — AI Agent collaboration and A2A communication platform"
```

行 33 `"selectionLabel"`:
```
舊："E-Claw (AI Live Wallpaper Chat)"
新："E-Claw (AI Agent Collaboration)"
```

行 35 `"description"` (plugin object 內):
```
舊："Connect OpenClaw to E-Claw — an AI chat platform for live wallpaper entities on Android."
新："Connect OpenClaw to E-Claw — the AI Agent collaboration and A2A communication platform for Android."
```

**Step 3: 更新 package/README.md（行 3）**

舊：
```
OpenClaw channel plugin for [E-Claw](https://eclawbot.com) — an AI chat platform for live wallpaper entities on Android.
```
新：
```
OpenClaw channel plugin for [E-Claw](https://eclawbot.com) — the AI Agent collaboration and A2A communication platform for Android.
```

**Step 4: 驗證**

Grep `live wallpaper entities` in package/ → 應找不到
Grep `AI chat platform` in backend/README.md → 應找不到

**Step 5: Commit**

```bash
git add backend/README.md package/package.json package/README.md
git commit -m "feat(rebrand): update package descriptions to AI Agent collaboration"
```

---

### Task 6：openclaw-channel-eclaw — package.json + openclaw.plugin.json + index.ts

**Files:**
- Modify: `openclaw-channel-eclaw/package.json`
- Modify: `openclaw-channel-eclaw/openclaw.plugin.json`
- Modify: `openclaw-channel-eclaw/src/index.ts`

**Step 1: 讀取 openclaw-channel-eclaw/package.json 確認行號**

**Step 2: 更新 package.json**

`"description"` (頂層):
```
舊："E-Claw channel plugin for OpenClaw — AI chat platform for live wallpaper entities"
新："E-Claw channel plugin for OpenClaw — AI Agent collaboration and A2A communication platform"
```

`"selectionLabel"` (plugins 陣列內):
```
舊："E-Claw (AI Live Wallpaper Chat)"
新："E-Claw (AI Agent Collaboration)"
```

`"description"` (plugins 陣列內):
```
舊："Connect OpenClaw to E-Claw — an AI chat platform for live wallpaper entities on Android."
新："Connect OpenClaw to E-Claw — the AI Agent collaboration and A2A communication platform for Android."
```

**Step 3: 更新 openclaw.plugin.json**

讀取後找 `"description"` 欄位：

舊：
```
"description": "E-Claw AI chat platform channel for OpenClaw"
```
新：
```
"description": "E-Claw AI Agent collaboration channel for OpenClaw"
```

**Step 4: 更新 src/index.ts JSDoc**

讀取後找 `E-Claw AI chat platform channel plugin` 這行：

舊（JSDoc 內）：
```
 * E-Claw Channel Plugin for OpenClaw.
```
在其後的描述中把 `AI chat platform` 改為 `AI Agent collaboration`：

舊：
```
"description": 'E-Claw AI chat platform channel plugin',
```
新：
```
"description": 'E-Claw AI Agent collaboration channel plugin',
```

**Step 5: 驗證**

Grep `AI chat platform` in openclaw-channel-eclaw/ → 應找不到（除了可能的注釋）

**Step 6: Commit**

```bash
cd openclaw-channel-eclaw
git add package.json openclaw.plugin.json src/index.ts
git commit -m "feat(rebrand): update plugin metadata to AI Agent collaboration"
```

---

### Task 7：openclaw-channel-eclaw — src/channel.ts + README.md

**Files:**
- Modify: `openclaw-channel-eclaw/src/channel.ts`
- Modify: `openclaw-channel-eclaw/README.md:3`

**Step 1: 更新 src/channel.ts**

讀取檔案後：

`selectionLabel`:
```
舊："E-Claw (AI Live Wallpaper Chat)"
新："E-Claw (AI Agent Collaboration)"
```

`blurb`:
```
舊："Connect OpenClaw to E-Claw — an AI chat platform for live wallpaper entities on Android."
新："Connect OpenClaw to E-Claw — the AI Agent collaboration and A2A communication platform for Android."
```

**Step 2: 更新 README.md（行 3）**

舊：
```
OpenClaw channel plugin for [E-Claw](https://eclawbot.com) — an AI chat platform for live wallpaper entities on Android.
```
新：
```
OpenClaw channel plugin for [E-Claw](https://eclawbot.com) — the AI Agent collaboration and A2A communication platform for Android.
```

**Step 3: 驗證**

Grep `AI Live Wallpaper Chat` in openclaw-channel-eclaw/ → 應找不到
Grep `live wallpaper entities` in openclaw-channel-eclaw/ → 應找不到

**Step 4: Commit**

```bash
git add src/channel.ts README.md
git commit -m "feat(rebrand): update channel metadata and README to AI Agent collaboration"
```

---

### Task 8：openclaw-channel-eclaw — scripts/openclaw-discussions-post.md

**Files:**
- Modify: `openclaw-channel-eclaw/scripts/openclaw-discussions-post.md`

**Step 1: 讀取檔案**

讀取整個檔案確認 intro 段落位置（約行 1-11）

**Step 2: 更新標題（行 1）**

舊：
```
# [Show and Tell] @eclaw/openclaw-channel — E-Claw AI Live Wallpaper Chat
```
新：
```
# [Show and Tell] @eclaw/openclaw-channel — E-Claw AI Agent Collaboration Platform
```

**Step 3: 更新 "What is E-Claw?" 段落**

舊（行約 8-10）：
```
E-Claw renders AI-powered characters (lobsters, pigs 🦞🐷) as interactive **Android live wallpapers**. Users tap or speak to the wallpaper characters, and your OpenClaw bot responds in real-time through a native channel integration.
```
新：
```
E-Claw is an **AI Agent collaboration and A2A communication platform** for Android. Multiple AI Agents are visualized as live wallpaper entities (lobsters, pigs 🦞🐷) — users interact with them in real-time, and agents can communicate with each other (A2A) via broadcast and direct messaging.
```

**Step 4: 驗證**

Grep `AI Live Wallpaper Chat` in scripts/ → 應找不到

**Step 5: Commit**

```bash
git add scripts/openclaw-discussions-post.md
git commit -m "feat(rebrand): update community post to AI Agent collaboration positioning"
```

---

### Task 9：最終全面驗證

**Step 1: 在 realbot repo 搜索殘留舊文案**

用 Grep 搜索以下關鍵字（應全部找不到）：
- `復古電子寵物`
- `電子雞`
- `AI 電子寵物桌布`
- `live wallpaper companion`
- `AI companion experience`（注意：`AI Agent collaboration experience` 是對的）
- `AI 夥伴體驗`
- `桌面寵物`
- `desktop pet`
- `live wallpaper entities`（package/ 和 openclaw-channel-eclaw/ 內）
- `AI Live Wallpaper Chat`（openclaw-channel-eclaw/ 內）

**Step 2: 確認新文案正確出現**

Grep 以下字串應找到：
- `AI Agent 協作 × 即時視覺化` → info.html
- `AI Agent 協作平台` → info.html
- `AI Agent collaboration and A2A communication platform` → multiple files
- `E-Claw (AI Agent Collaboration)` → package.json, channel.ts

**Step 3: 最後 commit（若 Task 9 步驟 1-2 中有發現任何遺漏，先修補再 commit）**

```bash
# 在 realbot repo
git add -p  # 選擇性 stage（僅 backend/ 相關）
git status
```

---

## 注意事項

1. **openclaw-channel-eclaw 是獨立子目錄**，需要在該目錄下執行 git 操作：
   - 先確認 `openclaw-channel-eclaw/.git` 是否存在（獨立 repo）
   - 若存在：在子目錄 commit 並另行 push
   - 若不存在（submodule 或同 repo）：在根目錄 commit

2. **不需修改 Android strings.xml**：已是 "AI 代理的即時狀態" 等符合新定位的文字

3. **不需修改 footer_desc**："AI collaboration platform for Android live wallpaper" 已符合

4. **不需修改技術 API 文件**：`borrow_desc`、`cmp_eclaw_tagline` 等描述功能而非品牌定位

5. **i18n.js 其他語系**（ZH-CN、JA、KO、TH、VI、ID）：沒有 `faq_a_what_is` / `cmp_subtitle` 等 key，會自動 fallback 到 EN，無需修改
