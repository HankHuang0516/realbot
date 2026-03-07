# Soul & Rule 模板貢獻系統 — 設計文件

**日期**: 2026-03-07
**狀態**: 已確認，待實作

---

## 背景

目前只有「技能（Skill）」支援社群透過 Git PR 貢獻模板（`backend/data/skill-templates.json`）。靈魂（Soul）和規則（Rule）只能在 runtime 透過 API 操作，沒有對應的官方模板庫。

本功能將靈魂和規則擴展為與技能完全對稱的貢獻系統。

---

## 設計目標

- 社群可透過 PR 貢獻靈魂模板和規則模板
- 批准後自動進入官方模板庫
- App / Portal 使用者可從 Gallery 瀏覽並套用官方模板
- 實作對稱現有 skill-templates 系統，減少新增抽象

---

## 方案選擇

**選定方案 B：完整對稱**

| 考量 | 說明 |
|------|------|
| 可預測性 | 對稱 skill-templates，開發者熟悉路徑 |
| 完整體驗 | App / Portal 同步支援，Feature Parity |
| 可維護性 | 不引入新的統一 API，減少耦合 |

---

## 後端設計

### 新增檔案

#### `backend/data/soul-templates.json`

貢獻者必填欄位：

| 欄位 | 類型 | 說明 |
|------|------|------|
| `id` | string | URL-safe 唯一識別碼（kebab-case） |
| `label` | string | UI 顯示名稱 |
| `icon` | string | 單一 emoji |
| `name` | string | 新增 Soul 時預填的名稱 |
| `description` | string | 人設描述（純文字，預填至描述欄） |
| `author` | string | 貢獻者 GitHub username |
| `updatedAt` | string | 格式 `YYYY-MM-DD` |

貢獻者可自由新增其他欄位（如 `personality`、`examplePhrases`）。

範例：
```json
[
  {
    "id": "friendly-assistant",
    "label": "友善助手",
    "icon": "😊",
    "name": "Friendly Assistant",
    "description": "Warm, patient, and encouraging. Always responds with a positive tone and tries to understand the user's needs before offering solutions.",
    "author": "contributor-username",
    "updatedAt": "2026-03-07",
    "personality": ["friendly", "patient", "encouraging"]
  }
]
```

#### `backend/data/rule-templates.json`

貢獻者必填欄位（比 soul 多一個 `ruleType`）：

| 欄位 | 類型 | 說明 |
|------|------|------|
| `id` | string | URL-safe 唯一識別碼 |
| `label` | string | UI 顯示名稱 |
| `icon` | string | 單一 emoji |
| `ruleType` | string | `WORKFLOW` / `COMMUNICATION` / `CODE_REVIEW` / `DEPLOYMENT` / `SYNC` / `HEARTBEAT` |
| `name` | string | 預填的規則名稱 |
| `description` | string | 規則內容，支援 Markdown |
| `author` | string | 貢獻者 GitHub username |
| `updatedAt` | string | 格式 `YYYY-MM-DD` |

範例：
```json
[
  {
    "id": "reply-in-english",
    "label": "英文回覆",
    "icon": "🇬🇧",
    "ruleType": "COMMUNICATION",
    "name": "Reply in English",
    "description": "Always respond in English regardless of the language used in the message.",
    "author": "contributor-username",
    "updatedAt": "2026-03-07"
  }
]
```

### 新增 API Endpoints（`backend/mission.js`）

| 端點 | 方法 | Auth | 說明 |
|------|------|------|------|
| `/api/mission/soul-templates` | GET | 無（public） | 回傳 soul-templates.json 陣列 |
| `/api/mission/rule-templates` | GET | 無（public） | 回傳 rule-templates.json 陣列 |

與現有 skill-templates endpoint 一致，回傳格式：
```json
{ "success": true, "templates": [...] }
```

---

## Android UI 設計

### 新增檔案

- `SoulGalleryDialog.kt` — 複用 SkillGalleryDialog 結構
- `RuleGalleryDialog.kt` — 同上

### 新增 ViewModel 方法

在 `MissionViewModel.kt` 新增：
- `fetchSoulTemplates()` → 呼叫 `GET /api/mission/soul-templates`
- `fetchRuleTemplates()` → 呼叫 `GET /api/mission/rule-templates`

### UX 流程

**新增 Soul 流程：**
1. 使用者點「新增靈魂」→ 開啟 AddSoulDialog
2. Dialog 頂部有「從模板選擇」按鈕
3. 點擊後開啟 SoulGalleryDialog，顯示官方模板列表
4. 使用者選取模板 → `name` / `description` 預填回 AddSoulDialog
5. 使用者可修改後送出

**新增 Rule 流程：** 同上，對應欄位為 `name` / `description` / `ruleType`

### i18n 字串（`res/values/strings.xml`）

```xml
<string name="mission_choose_from_template">從模板選擇</string>
<string name="mission_soul_gallery_title">靈魂模板</string>
<string name="mission_rule_gallery_title">規則模板</string>
<string name="mission_template_by">by %s</string>
<string name="mission_use_template">套用此模板</string>
<string name="mission_template_loading">載入模板中...</string>
<string name="mission_template_empty">目前沒有可用的模板</string>
```

對應英文 (`res/values-en/strings.xml`)：
```xml
<string name="mission_choose_from_template">Choose from template</string>
<string name="mission_soul_gallery_title">Soul Templates</string>
<string name="mission_rule_gallery_title">Rule Templates</string>
<string name="mission_use_template">Use this template</string>
<string name="mission_template_loading">Loading templates...</string>
<string name="mission_template_empty">No templates available</string>
```

---

## Web Portal 設計

在 Web Portal 的 soul/rule 新增對話框中加入「從模板選擇」按鈕（Feature Parity）。

點擊後顯示模板 Gallery（可用現有 skill gallery 的 CSS/HTML 結構）。

i18n key 遵循現有 Portal 翻譯機制。

---

## README 更新

在現有技能貢獻說明（第 166-224 行）之後，新增：

### Contributing Soul Templates

1. Fork this repo
2. Edit `backend/data/soul-templates.json`, add one object to the array
3. Required fields: `id` (unique kebab-case), `label`, `icon`, `name`, `description`, `author`, `updatedAt`
4. `description` must be in English
5. Open a PR

### Contributing Rule Templates

1. Fork this repo
2. Edit `backend/data/rule-templates.json`, add one object to the array
3. Required fields: `id`, `label`, `icon`, `ruleType`, `name`, `description`, `author`, `updatedAt`
4. Valid `ruleType` values: `WORKFLOW`, `COMMUNICATION`, `CODE_REVIEW`, `DEPLOYMENT`, `SYNC`, `HEARTBEAT`
5. `description` supports Markdown and must be in English
6. Open a PR

---

## 影響檔案清單

| 檔案 | 變更類型 |
|------|---------|
| `backend/data/soul-templates.json` | 新建 |
| `backend/data/rule-templates.json` | 新建 |
| `backend/mission.js` | 新增 2 個 GET endpoints |
| `app/.../ui/mission/SoulGalleryDialog.kt` | 新建 |
| `app/.../ui/mission/RuleGalleryDialog.kt` | 新建 |
| `app/.../ui/mission/MissionViewModel.kt` | 新增 2 個 fetch 方法 |
| `app/.../ui/mission/AddSoulDialog.kt` (或同等) | 修改：加入「從模板選擇」按鈕 |
| `app/.../ui/mission/AddRuleDialog.kt` (或同等) | 修改：加入「從模板選擇」按鈕 |
| `app/src/main/res/values/strings.xml` | 新增 i18n 字串 |
| `app/src/main/res/values-en/strings.xml` | 新增 i18n 字串 |
| `backend/public/.../mission*.html` | 修改：Portal 對話框加入模板選擇 |
| `README.md` | 新增貢獻說明段落 |

---

## 不在本次範圍內

- 模板審核機制（沿用 PR review 流程，無自動化）
- 模板評分 / 使用次數統計
- 模板搜尋 / 篩選功能
- Soul / Rule Gallery 的 Tab 整合（已選方案 A）
