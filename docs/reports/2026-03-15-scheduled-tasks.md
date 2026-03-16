# EClaw Scheduled Tasks

> Exported: 2026-03-15

---

## 1. `comprehensive-claudemd-file`

| Field | Value |
|-------|-------|
| Schedule | Every day at 08:07 AM |
| Cron | `0 8 * * *` |
| Enabled | Yes |
| Last Run (UTC) | 2026-03-15 15:07 |
| Next Run (UTC) | 2026-03-16 15:07 |

### Instruction

1. Analyze this repository and create a comprehensive CLAUDE.md file that explains the codebase structure, development workflows, and key conventions for AI assistants to follow. If one already exists, update it with the most recent state

2. push change

---

## 2. `improve-our-tests`

| Field | Value |
|-------|-------|
| Schedule | Every day at 07:00 AM |
| Cron | `0 7 * * *` |
| Enabled | Yes |
| Last Run (UTC) | 2026-03-15 14:02 |
| Next Run (UTC) | 2026-03-16 14:00 |

### Instruction

1. Analyze the test coverage of the codebase and propose some areas in which we should improve our tests.

2. 開始處理P0~P1

3. 新增的case 全數移到regression list中

4. run regression

5. 全數修復完畢之後才能push到main

---

## 3. `uiux`

| Field | Value |
|-------|-------|
| Schedule | Every day at 06:01 AM |
| Cron | `0 6 * * *` |
| Enabled | Yes |
| Last Run (UTC) | 2026-03-15 13:19 |
| Next Run (UTC) | 2026-03-16 13:01 |

### Instruction

[啟動UIUX審查]

A. 審查目標
1. Web Portal
2. Android App

B. 審查紀錄文件與更新: `docs\reports\*platform-pages-features-inventory.md`

C. 直接讀 code 審查並參考以下技能:
1. 使用者訪談技能: user-interview
2. 介面設計技能:
   (1) ui-visual-design
   (2) information-architecture
   (3) wireframing
3. 互動流程驗證: prototype-testing
4. 可用性測試: usability-testing
5. 無障礙設計: accessibility-design
6. 設計系統建立: design-system

D. 依照審查項目 開始執行UIUX改善

E. 依照workflow push 到main

---

## 4. `check-entity`

| Field | Value |
|-------|-------|
| Schedule | Every day at 08:24 AM |
| Cron | `15 8 * * *` |
| Enabled | Yes |
| Last Run (UTC) | 2026-03-15 15:23 |
| Next Run (UTC) | 2026-03-16 15:23 |

### Instruction

1. 檢查EClaw全部的實體排程內容與結果(前一日的每一個任務內容)是否如實完成
2. 如果是bot問題請用speak to直接詢問/改善/訓練實體
3. 檢查後台 log 有沒有error級別 或是有沒有可疑/具有風險的 debug message
4. 如發現是Eclaw後台出現淺在問題，要具體描述問題/建議實施方向 然後發git issue

---

## 5. `finding-cybersecurity-risks`

| Field | Value |
|-------|-------|
| Schedule | Every day at 03:00 AM |
| Cron | `0 3 * * *` |
| Enabled | Yes |
| Last Run (UTC) | 2026-03-16 01:31 |
| Next Run (UTC) | 2026-03-16 10:00 |

### Instruction

1. 去網路尋找資安相關的技能來搜索目前網站/APP/後台 有沒有任何資安漏洞的風險
2. 任何P0~P1的風險要具體列出問題並描述具體改善措施然後發布到專案的git issue之中

---

## 6. `seo`

| Field | Value |
|-------|-------|
| Schedule | Every day at 03:34 AM |
| Cron | `0 3 * * *` |
| Enabled | Yes |
| Last Run (UTC) | 2026-03-16 01:06 |
| Next Run (UTC) | 2026-03-16 10:34 |

### Instruction

以下是Web優化逐步審查:

1. **基礎設施層（技術 SEO）：讓 Google 進得來**
   如果你的房子門鎖著，客人就進不去。這一層是在確保 Google 能順利讀取你的內容：
   - 讀取權限 (Indexability)： 確保沒有誤設 noindex 標籤，讓 Google 可以收錄網頁。
   - 載入速度 (Site Speed)： 網站開太慢，Google 和使用者都會直接離開。
   - 行動裝置友善 (Mobile Friendly)： 現在 Google 優先抓取手機版的內容。
   - 安全性 (HTTPS)： 加密協定是現在網站的標配。

2. **導航層（網站架構）：讓 Google 不迷路**
   Google 喜歡結構分明、條理清晰的網站。就像百貨公司的樓層介紹一樣：
   - 樹狀結構： 首頁 > 大分類 > 小分類 > 文章。不要讓網頁藏得太深（點超過 3 次才看到）。
   - 內部連結 (Internal Linking)： 相關的文章互相連結，這能告訴 Google 哪些頁面最重要，並建立主題權威。
   - 麵包屑導覽 (Breadcrumbs)： 告訴使用者（和 Google）目前所在的具體位置。

3. **語意層（內容架構）：讓 Google 看得懂**
   這就是如何把程式碼寫得有意義：
   - 標題標籤 (H1-H6)：
     - `<h1>` 只能有一個（書名）。
     - `<h2>` 是大章節。
     - `<h3>` 是小節。
     - 邏輯錯誤： 如果 AI 亂跳 H 標籤（例如直接從 H2 跳到 H4），Google 就會覺得你的邏輯混亂。
   - 語意化標籤 (Semantic HTML)： 使用 `<header>`、`<main>`、`<article>`、`<footer>` 等，這比一堆看不出意義的 `<div>` 更能讓 Google 理解網頁各區塊的功能。
   - 結構化資料 (Schema Markup)： 這是一段給 Google 看的「小抄」，直接告訴它這是一個「產品價格」、「食譜」還是「常見問答」，讓搜尋結果出現星星評分或圖片。

---

## Summary

- **Total tasks**: 6
- **All enabled**: Yes
- **All recurring**: Daily
