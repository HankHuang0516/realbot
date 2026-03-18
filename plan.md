# Plan: 模板系統分類標籤 (Category Tags)

## 目標
為 skill / soul / rule 三種模板加上 `category` 欄位，並在三平台的模板 gallery 中加入分類 filter chips。

## 範圍

### 1. 資料層 — 模板 JSON 加 `category` 欄位
- `backend/data/skill-templates.json`: 每個模板加 `"category": "..."`
  - claude-proxy → "AI"
  - x-tweet-fetcher → "Social"
  - model-hierarchy → "AI"
  - eclaw-a2a-toolkit → "Integration"
- `backend/data/soul-templates.json`:
  - friendly-assistant → "General"
  - professional-advisor → "General"
- `backend/data/rule-templates.json`:
  - reply-in-english → "Communication"（已有 ruleType=COMMUNICATION）
  - daily-summary → "Workflow"（已有 ruleType=WORKFLOW）

### 2. 後端 API — 無 breaking change
- `GET /api/skill-templates` — category 欄位自然跟著 JSON 回傳，無需改動
- `GET /api/soul-templates` — 同上
- `GET /api/rule-templates` — 同上
- `POST /api/skill-templates/contribute` — 接受可選 `category` 字串 (maxlength 30)
- `POST /api/soul-templates/contribute` — 接受可選 `category` 字串
- `POST /api/rule-templates/contribute` — 接受可選 `category` 字串

### 3. Web Portal — `mission.html` gallery 加 filter chips
- **Skill Gallery** (`buildGalleryHtml`): 在搜尋框下方加一排 category filter chips，從 templates 自動提取 unique categories
- **Soul Gallery** (`showSoulGallery`): 同上
- **Rule Gallery** (`showRuleGallery`): 同上
- 新增 CSS: `.tpl-filter-bar`, `.tpl-filter-chip` 樣式（參考 card-holder 的 filter-chip）
- 每個 gallery card 加 `data-category` 屬性，filter 邏輯在點擊 chip 時篩選

### 4. Android — gallery dialogs 加 filter chips
- `SoulGalleryDialog.kt`: ChipGroup 加動態 category chips
- `RuleGalleryDialog.kt`: ChipGroup 加動態 category chips
- `MissionControlActivity.kt` `showTemplateGalleryDialogInternal()`: 加動態 category chips
- `SkillTemplate` data model (`ClawApiService.kt`): 加 `val category: String? = null`
- `item_template_gallery.xml`: 可選顯示 category badge

### 5. iOS — `mission.tsx` gallery 加 filter chips
- 加 category chip row 在搜尋框下方
- 三個 filtered lists 加 category 過濾邏輯

### 6. Jest 測試
- 更新現有 template Jest tests 驗證 `category` 欄位存在

## 不做
- 不改 DB schema（category 在 JSON 裡，不在 contributions table 加 column）
- 不改 admin.html（管理頁面不需要 filter）
