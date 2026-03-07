# 用 Claude 透過 Eclaw 指揮 OpenClaw — 從零開始

> 本文教你如何讓 Claude（AI 助理）成為你的「AI 指揮官」，
> 透過 Eclaw 系統遠端控制你的 OpenClaw bot。

---

## 架構概覽

```
你（User）
  │
  │ 用自然語言下指令
  ▼
Claude（AI 助理）
  │
  │ 呼叫 Eclaw HTTP API
  ▼
Eclaw 後端（eclawbot.com）
  │
  │ Push 通知 / speak-to
  ▼
OpenClaw Bot（你的 AI bot）
  │
  │ 執行任務後回報狀態
  ▼
Eclaw → Claude 查詢結果
```

**三個角色：**
| 角色 | 說明 |
|------|------|
| **Eclaw** | 中控台，管理你的 entity（bot 席位），提供 HTTP API |
| **OpenClaw** | AI bot 的運行平台，執行實際任務 |
| **Claude** | 你的 AI 指揮官，透過 Eclaw API 傳達指令給 OpenClaw |

---

## 前置準備

### 需要的東西

- [ ] Eclaw 帳號（[eclawbot.com](https://eclawbot.com)）+ 手機 App 或 Web Portal
- [ ] 一個已在 OpenClaw 平台上運行的 AI bot
- [ ] bot 已成功**綁定**到你的 Eclaw entity slot（手機 App 掃碼綁定）
- [ ] Claude Code CLI（[安裝說明](https://claude.ai/claude-code)）或 Claude.ai（含 MCP 工具）

---

## Step 1：取得你的憑證

綁定完成後，你需要兩個資訊：

### 1-A. 取得 Device ID

開啟 Eclaw Web Portal → 右上角 → Settings → 複製 **Device ID**

```
範例：xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx
```

### 1-B. 取得 Bot Secret

你的 OpenClaw bot 在綁定時已拿到 `botSecret`（顯示在 bot 的對話記錄或 binding 頁面）。

**或**從 Eclaw Web Portal 查：
Portal → Mission Control → 選擇你的 entity → 複製 Bot Secret

```
範例：xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
```

### 1-C. 確認 Entity ID

你最多有 8 個 entity slot（0–7）。
通常第一個 bot 是 entity **0**，第二個是 **1**，以此類推。

---

## Step 2：給 Claude 看「技能說明書」

Eclaw 有一份 AI 指令說明書，告訴 Claude 有哪些 API 可用。

### 方法 A：Claude Code CLI（推薦）

1. 下載說明書：
   ```
   https://eclawbot.com/api/skill-doc
   ```
   或在 Eclaw 綁定時，OpenClaw bot 會自動收到這份文件。

2. 在 Claude Code 對話開始時，把說明書內容貼入：
   ```
   請閱讀以下 Eclaw 技能說明書，之後我會用你來控制我的 OpenClaw bot：
   [貼入 E-claw_mcp_skill.md 的內容]
   ```

### 方法 B：Claude.ai + 上傳檔案

1. 從 Eclaw Portal 下載 `E-claw_mcp_skill.md`
2. 在 Claude.ai 對話中點擊 📎 附加檔案
3. 對 Claude 說：「請閱讀這份說明書，之後我要用你控制我的 OpenClaw bot」

---

## Step 3：告訴 Claude 你的憑證

在對話中把憑證提供給 Claude：

```
我的 Eclaw 設定：
- Device ID: xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx
- Entity ID: 0
- Bot Secret: xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx

請幫我控制這個 OpenClaw bot。
```

> ⚠️ **安全提醒**：Bot Secret 只用於 bot 操作，無法存取你的帳號設定或付款資訊。
> 但仍建議只在私人對話中使用，不要公開分享。

---

## Step 4：開始下指令

現在 Claude 已經知道如何使用 Eclaw API，你可以用自然語言下指令：

---

### 範例 1：查看 bot 目前狀態

**你說：**
```
幫我看看我的 bot 目前在做什麼
```

**Claude 會：**
1. 呼叫 `GET https://eclawbot.com/api/entities?deviceId=YOUR_ID`
2. 讀取 entity 0 的 `state` 和 `message`
3. 回報給你：「你的 bot 目前狀態是 IDLE，最後訊息是：...」

---

### 範例 2：給 bot 一個任務

**你說：**
```
叫我的 bot 去搜尋最近 GitHub 上最受歡迎的 Python 爬蟲框架，整理成清單回報給我
```

**Claude 會：**
1. 呼叫 `POST /api/entity/speak-to`，把任務說明推送給 bot
2. 等待 bot 執行並更新狀態
3. 查詢結果後回報給你

---

### 範例 3：讓 bot 變換情緒/狀態

**你說：**
```
讓我的 bot 進入工作模式，顯示「正在分析市場數據」
```

**Claude 會：**
```bash
# Claude 在背後執行
curl -s -X POST "https://eclawbot.com/api/transform" \
  -H "Content-Type: application/json" \
  -d '{
    "deviceId": "YOUR_DEVICE_ID",
    "entityId": 0,
    "botSecret": "YOUR_BOT_SECRET",
    "state": "WORKING",
    "message": "正在分析市場數據"
  }'
```

---

### 範例 4：設定每小時自動執行任務

**你說：**
```
幫我設定一個排程：每天早上 9 點，讓 bot 自動抓取今日科技新聞摘要
```

**Claude 會：**
```bash
curl -s -X POST "https://eclawbot.com/api/bot/schedules" \
  -H "Content-Type: application/json" \
  -d '{
    "deviceId": "YOUR_DEVICE_ID",
    "entityId": 0,
    "botSecret": "YOUR_BOT_SECRET",
    "message": "每日任務：搜尋今日科技新聞，整理 5 則摘要，用 POST /api/transform 更新狀態",
    "repeatType": "cron",
    "cronExpr": "0 9 * * *",
    "label": "Daily Tech News",
    "timezone": "Asia/Taipei"
  }'
```

---

### 範例 5：查看任務執行記錄

**你說：**
```
幫我看看 bot 最近的聊天記錄，有沒有完成我昨天交辦的任務
```

**Claude 會：**
1. 查詢 chat history API
2. 找出相關訊息
3. 摘要回報給你

---

## 進階：多 Bot 協作

如果你有多個 entity（多個 bot），Claude 可以協調它們一起工作：

**你說：**
```
讓 entity 0 負責收集資料，entity 1 負責分析，兩個 bot 一起幫我做市場調查
```

**Claude 會：**
1. 對 entity 0 發送資料收集任務
2. 對 entity 1 發送分析任務
3. 等兩個 bot 都回報後，整合結果給你

---

## 常用指令速查

| 你想做的事 | 說法範例 |
|-----------|---------|
| 查看 bot 狀態 | 「我的 bot 在幹嘛？」 |
| 交辦任務 | 「叫 bot 去做 ___」 |
| 更新 bot 顯示訊息 | 「讓 bot 顯示 '___'」 |
| 設定排程 | 「每天/每小時 讓 bot 自動做 ___」 |
| 查看排程 | 「列出我設定的所有排程」 |
| 取消排程 | 「取消 '___' 這個排程」 |
| 查看記錄 | 「bot 最近說了什麼？」 |
| 貢獻技能 | 「幫我把這個技能提交到 Eclaw 技能庫」 |

---

## 常見問題

**Q: Claude 說「我沒有辦法執行 HTTP 請求」怎麼辦？**
A: 你需要使用 **Claude Code CLI**（有 Bash 工具）或在 Claude.ai 啟用 MCP 工具。
Claude.ai 的標準對話介面無法直接發 HTTP 請求，Claude 會給你指令讓你自己貼到終端機執行。

**Q: Bot Secret 是什麼？在哪裡找？**
A: Bot Secret 是你的 OpenClaw bot 的認證金鑰。在 bot 綁定時顯示，也可在 Eclaw Portal → Mission Control 查看。

**Q: Claude 下的指令 bot 沒有反應？**
A: 確認以下幾點：
- Bot 是否已綁定（`isBound: true`）
- Bot Secret 是否正確
- Bot 的 OpenClaw 是否在線上

**Q: 可以讓 Claude 永遠控制 bot 嗎？**
A: 是的，透過設定**排程（Schedule）**，bot 會定時自動執行任務，不需要每次都手動叫 Claude 下指令。

---

## 延伸應用

| 應用場景 | 說明 |
|---------|------|
| **個人助理** | 定時抓取新聞、提醒事項、天氣 |
| **資料蒐集** | 自動搜尋特定主題的最新資訊 |
| **技能分享** | 把 bot 的能力打包成技能模板，貢獻給社群 |
| **多 bot 流水線** | 一個 bot 收集 → 另一個 bot 分析 → 另一個 bot 摘要 |
| **監控告警** | 定時檢查特定網站/數據，異常時發通知 |

---

*文件版本：2026-03-07 | 適用系統：Eclaw v1.1+*
