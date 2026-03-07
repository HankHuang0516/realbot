# Soul & Rule 模板貢獻系統 Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 讓 Soul（靈魂）和 Rule（規則）與 Skill 一樣，支援社群透過 Git PR 貢獻官方模板，並在 Web Portal 和 Android App 中提供 Gallery 選用入口。

**Architecture:** 新增 `soul-templates.json` / `rule-templates.json` 資料檔，後端在 `backend/index.js` 新增 2 個 public GET endpoints（對稱現有 `/api/skill-templates`），Web Portal（mission.html）和 Android App 各自新增獨立 Gallery Dialog，使用者點選後預填新增表單。

**Tech Stack:** Node.js / Express（後端）、純 JS + HTML/CSS（Web Portal）、Kotlin / Material 3（Android）

---

## 現有程式碼參考

- 現有 skill-templates API：`backend/index.js` 第 651–800 行
- Web Portal skill gallery：`backend/public/portal/mission.html`
  - `loadSkillTemplates()`、`buildGalleryHtml()`、`filterGallery()`、`fillSkillTemplate()`
  - CSS classes：`.tpl-gallery-card`, `.tpl-gallery-icon`, `.tpl-gallery-info`
- Android Skill Adapter：`app/.../ui/mission/MissionSkillAdapter.kt`
- Android ViewModel skill 方法：`app/.../ui/MissionViewModel.kt` 第 375–403 行
- strings.xml：`app/src/main/res/values/strings.xml`（目前有 `add_soul`、`add_rule` 等）

---

## Task 1：建立 soul-templates.json（後端資料層）

**Files:**
- Create: `backend/data/soul-templates.json`

**Step 1: 建立初始 soul-templates.json（含 2 個範例模板）**

```json
[
  {
    "id": "friendly-assistant",
    "label": "Friendly Assistant",
    "icon": "😊",
    "name": "Friendly Assistant",
    "description": "Warm, patient, and encouraging. Always responds with a positive tone and tries to understand the user's needs before offering solutions. Avoids technical jargon unless the user demonstrates familiarity.",
    "author": "E-Claw",
    "updatedAt": "2026-03-07"
  },
  {
    "id": "professional-advisor",
    "label": "Professional Advisor",
    "icon": "💼",
    "name": "Professional Advisor",
    "description": "Formal, precise, and results-oriented. Communicates in a concise business tone. Provides structured answers with clear action items. Avoids casual language.",
    "author": "E-Claw",
    "updatedAt": "2026-03-07"
  }
]
```

**Step 2: Commit**

```bash
git add backend/data/soul-templates.json
git commit -m "feat(data): add soul-templates.json with 2 starter templates"
```

---

## Task 2：建立 rule-templates.json（後端資料層）

**Files:**
- Create: `backend/data/rule-templates.json`

**Step 1: 建立初始 rule-templates.json（含 2 個範例模板）**

```json
[
  {
    "id": "reply-in-english",
    "label": "Reply in English",
    "icon": "🇬🇧",
    "ruleType": "COMMUNICATION",
    "name": "Reply in English",
    "description": "Always respond in English regardless of the language used in the incoming message. If the user writes in another language, acknowledge it briefly then continue in English.",
    "author": "E-Claw",
    "updatedAt": "2026-03-07"
  },
  {
    "id": "daily-summary",
    "label": "Daily Summary",
    "icon": "📋",
    "ruleType": "WORKFLOW",
    "name": "Daily Summary",
    "description": "At the end of each day (or when requested), provide a brief summary of completed tasks, pending items, and any blockers encountered. Format as a bulleted list.",
    "author": "E-Claw",
    "updatedAt": "2026-03-07"
  }
]
```

**Step 2: Commit**

```bash
git add backend/data/rule-templates.json
git commit -m "feat(data): add rule-templates.json with 2 starter templates"
```

---

## Task 3：後端 API endpoints（`backend/index.js`）

**Files:**
- Modify: `backend/index.js`（在 skill-templates 的 `readFileSync` 附近新增）

**Step 1: 找到現有 skill-templates 的 readFileSync（第 ~654 行），在其下方新增兩行載入**

```javascript
// 在這一行之後：
// const skillTemplatesData = JSON.parse(fs.readFileSync(path.join(__dirname, 'data/skill-templates.json'), 'utf8'));

const soulTemplatesData = JSON.parse(fs.readFileSync(path.join(__dirname, 'data/soul-templates.json'), 'utf8'));
const ruleTemplatesData = JSON.parse(fs.readFileSync(path.join(__dirname, 'data/rule-templates.json'), 'utf8'));
```

**Step 2: 在 `GET /api/skill-templates` endpoint（第 ~665 行）之後新增兩個 endpoints**

```javascript
app.get('/api/soul-templates', (req, res) => {
    res.json({ success: true, templates: soulTemplatesData });
});

app.get('/api/rule-templates', (req, res) => {
    res.json({ success: true, templates: ruleTemplatesData });
});
```

**Step 3: 手動測試**

```bash
node backend/index.js &
curl http://localhost:3000/api/soul-templates
# 預期: { "success": true, "templates": [...] }  (2 個模板)

curl http://localhost:3000/api/rule-templates
# 預期: { "success": true, "templates": [...] }  (2 個模板)
kill %1
```

**Step 4: Commit**

```bash
git add backend/index.js
git commit -m "feat(api): add GET /api/soul-templates and /api/rule-templates endpoints"
```

---

## Task 4：Web Portal — Soul Gallery（`mission.html`）

**Files:**
- Modify: `backend/public/portal/mission.html`

**Step 1: 在 `let skillTemplates = []` 附近（第 ~1273 行）新增宣告**

```javascript
let soulTemplates = [];
let ruleTemplates = [];
```

**Step 2: 在 `loadSkillTemplates()` 之後新增兩個 load 函式**

```javascript
async function loadSoulTemplates() {
    try {
        const data = await apiCall('GET', '/api/soul-templates');
        if (data.success) soulTemplates = data.templates;
    } catch (e) { console.warn('Failed to load soul templates:', e); }
}

async function loadRuleTemplates() {
    try {
        const data = await apiCall('GET', '/api/rule-templates');
        if (data.success) ruleTemplates = data.templates;
    } catch (e) { console.warn('Failed to load rule templates:', e); }
}
```

**Step 3: 在 DOMContentLoaded 事件（呼叫 `loadSkillTemplates()` 的地方）同步呼叫新函式**

```javascript
loadSoulTemplates();
loadRuleTemplates();
```

**Step 4: 新增 Soul Gallery 的 show/apply/fill 函式**

在 `fillSkillTemplate()` / `applySkillTemplate()` 附近新增：

```javascript
function fillSoulTemplate(tpl) {
    const nameEl = document.getElementById('dlg_soul_name');
    const descEl = document.getElementById('dlg_soul_desc');
    if (nameEl) nameEl.value = tpl.name || '';
    if (descEl) descEl.value = tpl.description || '';
}

function showSoulGallery() {
    const cards = soulTemplates.map(t =>
        `<div class="tpl-gallery-card" onclick="selectSoulTemplate('${esc(t.id)}')">
            <div class="tpl-gallery-icon">${t.icon || '🧠'}</div>
            <div class="tpl-gallery-info">
                <div class="tpl-gallery-title">${esc(t.label)}</div>
                <div class="tpl-gallery-meta">by ${esc(t.author || '—')} · ${esc(t.updatedAt || '')}</div>
                <div class="tpl-gallery-status" style="color:var(--text-muted);">${esc((t.description || '').substring(0, 60))}…</div>
            </div>
            <button class="btn btn-outline" style="font-size:12px;padding:5px 12px;flex-shrink:0;" onclick="event.stopPropagation();selectSoulTemplate('${esc(t.id)}')">Select</button>
        </div>`
    ).join('') || '<p style="color:var(--text-muted);text-align:center;">No templates available</p>';

    document.body.insertAdjacentHTML('beforeend',
        `<div id="soul_gallery_overlay" class="dialog-overlay" onclick="if(event.target===this)document.getElementById('soul_gallery_overlay').remove()">
            <div class="dialog" style="max-width:480px;">
                <div class="dialog-title">靈魂模板</div>
                <input type="text" placeholder="搜尋…" oninput="filterSoulGallery(this.value)" style="margin-bottom:12px;" />
                <div id="soul_gallery_list" style="max-height:380px;overflow-y:auto;">${cards}</div>
                <div class="dialog-actions">
                    <button class="btn btn-outline" onclick="document.getElementById('soul_gallery_overlay').remove()">取消</button>
                </div>
            </div>
        </div>`
    );
}

function filterSoulGallery(query) {
    const q = query.toLowerCase();
    document.querySelectorAll('#soul_gallery_list .tpl-gallery-card').forEach(card => {
        const title = card.querySelector('.tpl-gallery-title');
        card.style.display = (title && title.textContent.toLowerCase().includes(q)) ? '' : 'none';
    });
}

function selectSoulTemplate(id) {
    const tpl = soulTemplates.find(t => t.id === id);
    if (!tpl) return;
    fillSoulTemplate(tpl);
    document.getElementById('soul_gallery_overlay')?.remove();
}
```

**Step 5: 找到 Soul 的新增對話框（搜尋 `showAddSoul` 或 `dlg_soul_name` 或 `showSoulDialog`），在對話框的名稱欄位前新增「從模板選擇」按鈕**

在 soul 對話框的 HTML 開頭加入按鈕（用 `insertAdjacentHTML` 或直接在 dialog HTML 字串中加）：

```html
<button class="btn btn-outline btn-sm" onclick="showSoulGallery()" style="margin-bottom:12px;width:100%;">
    🎭 從模板選擇
</button>
```

**Step 6: 新增 Rule Gallery（同 Soul 模式，欄位改為 name、description、ruleType）**

```javascript
function fillRuleTemplate(tpl) {
    const nameEl = document.getElementById('dlg_rule_name');
    const descEl = document.getElementById('dlg_rule_desc');
    const typeEl = document.getElementById('dlg_rule_type');
    if (nameEl) nameEl.value = tpl.name || '';
    if (descEl) descEl.value = tpl.description || '';
    if (typeEl && tpl.ruleType) typeEl.value = tpl.ruleType;
}

function showRuleGallery() {
    const cards = ruleTemplates.map(t =>
        `<div class="tpl-gallery-card" onclick="selectRuleTemplate('${esc(t.id)}')">
            <div class="tpl-gallery-icon">${t.icon || '📋'}</div>
            <div class="tpl-gallery-info">
                <div class="tpl-gallery-title">${esc(t.label)}</div>
                <div class="tpl-gallery-meta">by ${esc(t.author || '—')} · ${esc(t.ruleType || '')} · ${esc(t.updatedAt || '')}</div>
                <div class="tpl-gallery-status" style="color:var(--text-muted);">${esc((t.description || '').substring(0, 60))}…</div>
            </div>
            <button class="btn btn-outline" style="font-size:12px;padding:5px 12px;flex-shrink:0;" onclick="event.stopPropagation();selectRuleTemplate('${esc(t.id)}')">Select</button>
        </div>`
    ).join('') || '<p style="color:var(--text-muted);text-align:center;">No templates available</p>';

    document.body.insertAdjacentHTML('beforeend',
        `<div id="rule_gallery_overlay" class="dialog-overlay" onclick="if(event.target===this)document.getElementById('rule_gallery_overlay').remove()">
            <div class="dialog" style="max-width:480px;">
                <div class="dialog-title">規則模板</div>
                <input type="text" placeholder="搜尋…" oninput="filterRuleGallery(this.value)" style="margin-bottom:12px;" />
                <div id="rule_gallery_list" style="max-height:380px;overflow-y:auto;">${cards}</div>
                <div class="dialog-actions">
                    <button class="btn btn-outline" onclick="document.getElementById('rule_gallery_overlay').remove()">取消</button>
                </div>
            </div>
        </div>`
    );
}

function filterRuleGallery(query) {
    const q = query.toLowerCase();
    document.querySelectorAll('#rule_gallery_list .tpl-gallery-card').forEach(card => {
        const title = card.querySelector('.tpl-gallery-title');
        card.style.display = (title && title.textContent.toLowerCase().includes(q)) ? '' : 'none';
    });
}

function selectRuleTemplate(id) {
    const tpl = ruleTemplates.find(t => t.id === id);
    if (!tpl) return;
    fillRuleTemplate(tpl);
    document.getElementById('rule_gallery_overlay')?.remove();
}
```

**Step 7: 在 Rule 的新增對話框加入「從模板選擇」按鈕（同 Soul，找 rule dialog HTML）**

```html
<button class="btn btn-outline btn-sm" onclick="showRuleGallery()" style="margin-bottom:12px;width:100%;">
    📋 從模板選擇
</button>
```

**Step 8: Commit**

```bash
git add backend/public/portal/mission.html
git commit -m "feat(portal): add soul & rule template gallery to mission page"
```

---

## Task 5：Android — i18n 字串

**Files:**
- Modify: `app/src/main/res/values/strings.xml`
- Modify: `app/src/main/res/values-en/strings.xml`（若存在）

**Step 1: 在 `strings.xml` 的 mission 字串段落新增**

```xml
<!-- Soul/Rule Template Gallery -->
<string name="mission_choose_from_template">從模板選擇</string>
<string name="mission_soul_gallery_title">靈魂模板</string>
<string name="mission_rule_gallery_title">規則模板</string>
<string name="mission_template_by">by %s</string>
<string name="mission_use_template">套用此模板</string>
<string name="mission_template_loading">載入模板中…</string>
<string name="mission_template_empty">目前沒有可用的模板</string>
```

**Step 2: 在英文語系檔案新增（確認路徑是否存在）**

```bash
# 確認英文語系資料夾
ls app/src/main/res/values-en/
```

若存在，在 `values-en/strings.xml` 新增：

```xml
<string name="mission_choose_from_template">Choose from template</string>
<string name="mission_soul_gallery_title">Soul Templates</string>
<string name="mission_rule_gallery_title">Rule Templates</string>
<string name="mission_template_by">by %s</string>
<string name="mission_use_template">Use this template</string>
<string name="mission_template_loading">Loading templates…</string>
<string name="mission_template_empty">No templates available</string>
```

**Step 3: Commit**

```bash
git add app/src/main/res/values/strings.xml
git add app/src/main/res/values-en/strings.xml  # 若存在
git commit -m "feat(android/i18n): add soul & rule template gallery strings"
```

---

## Task 6：Android — MissionViewModel（fetch 模板）

**Files:**
- Modify: `app/src/main/java/com/hank/clawlive/ui/MissionViewModel.kt`

**Step 1: 確認現有 ApiService 的定義路徑**

```bash
grep -r "skill-templates\|skillTemplates" app/src/main --include="*.kt" -l
```

**Step 2: 在 ApiService interface（或 Retrofit 定義處）新增兩個 GET 方法**

找到定義 skill-templates 的位置，在旁邊新增：

```kotlin
@GET("soul-templates")
suspend fun getSoulTemplates(): Response<TemplatesResponse>

@GET("rule-templates")
suspend fun getRuleTemplates(): Response<TemplatesResponse>
```

若沒有通用 `TemplatesResponse`，使用：

```kotlin
data class TemplatesResponse(
    val success: Boolean,
    val templates: List<JsonObject>
)
```

**Step 3: 在 `MissionViewModel` 新增 StateFlow 和 fetch 方法**

```kotlin
private val _soulTemplates = MutableStateFlow<List<JsonObject>>(emptyList())
val soulTemplates: StateFlow<List<JsonObject>> = _soulTemplates.asStateFlow()

private val _ruleTemplates = MutableStateFlow<List<JsonObject>>(emptyList())
val ruleTemplates: StateFlow<List<JsonObject>> = _ruleTemplates.asStateFlow()

fun fetchSoulTemplates() {
    viewModelScope.launch {
        try {
            val response = apiService.getSoulTemplates()
            if (response.isSuccessful) {
                _soulTemplates.value = response.body()?.templates ?: emptyList()
            }
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) Log.w("MISSION", "[MISSION] fetchSoulTemplates failed: ${e.message}")
        }
    }
}

fun fetchRuleTemplates() {
    viewModelScope.launch {
        try {
            val response = apiService.getRuleTemplates()
            if (response.isSuccessful) {
                _ruleTemplates.value = response.body()?.templates ?: emptyList()
            }
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) Log.w("MISSION", "[MISSION] fetchRuleTemplates failed: ${e.message}")
        }
    }
}
```

**Step 4: 驗證編譯**

```bash
JAVA_HOME="/c/Program Files/Android/Android Studio/jbr" ./gradlew.bat compileDebugKotlin
```

預期：BUILD SUCCESSFUL

**Step 5: Commit**

```bash
git add app/src/main/java/com/hank/clawlive/ui/MissionViewModel.kt
git commit -m "feat(android): add fetchSoulTemplates/fetchRuleTemplates to MissionViewModel"
```

---

## Task 7：Android — SoulGalleryDialog.kt

**Files:**
- Create: `app/src/main/java/com/hank/clawlive/ui/mission/SoulGalleryDialog.kt`

**Step 1: 找到現有 mission dialog 的套件路徑**

```bash
ls app/src/main/java/com/hank/clawlive/ui/mission/
```

確認套件名稱（通常是 `com.hank.clawlive.ui.mission`）

**Step 2: 建立 SoulGalleryDialog.kt**

```kotlin
package com.hank.clawlive.ui.mission

import android.content.Context
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.JsonObject
import com.hank.clawlive.R

class SoulGalleryDialog(
    private val context: Context,
    private val templates: List<JsonObject>,
    private val onTemplateSelected: (name: String, description: String) -> Unit
) {
    fun show() {
        val builder = MaterialAlertDialogBuilder(context)
            .setTitle(context.getString(R.string.mission_soul_gallery_title))

        if (templates.isEmpty()) {
            builder.setMessage(context.getString(R.string.mission_template_empty))
                .setPositiveButton(android.R.string.ok, null)
                .show()
            return
        }

        val labels = templates.map { t ->
            val icon = t.get("icon")?.asString ?: "🧠"
            val label = t.get("label")?.asString ?: ""
            val author = t.get("author")?.asString ?: ""
            "$icon $label  (by $author)"
        }.toTypedArray()

        builder.setItems(labels) { _, which ->
            val tpl = templates[which]
            val name = tpl.get("name")?.asString ?: ""
            val desc = tpl.get("description")?.asString ?: ""
            onTemplateSelected(name, desc)
        }
        .setNegativeButton(android.R.string.cancel, null)
        .show()
    }
}
```

**Step 3: 驗證編譯**

```bash
JAVA_HOME="/c/Program Files/Android/Android Studio/jbr" ./gradlew.bat compileDebugKotlin
```

**Step 4: Commit**

```bash
git add app/src/main/java/com/hank/clawlive/ui/mission/SoulGalleryDialog.kt
git commit -m "feat(android): add SoulGalleryDialog for soul template selection"
```

---

## Task 8：Android — RuleGalleryDialog.kt

**Files:**
- Create: `app/src/main/java/com/hank/clawlive/ui/mission/RuleGalleryDialog.kt`

**Step 1: 建立 RuleGalleryDialog.kt（同 Soul，多回傳 ruleType）**

```kotlin
package com.hank.clawlive.ui.mission

import android.content.Context
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.JsonObject
import com.hank.clawlive.R

class RuleGalleryDialog(
    private val context: Context,
    private val templates: List<JsonObject>,
    private val onTemplateSelected: (name: String, description: String, ruleType: String) -> Unit
) {
    fun show() {
        val builder = MaterialAlertDialogBuilder(context)
            .setTitle(context.getString(R.string.mission_rule_gallery_title))

        if (templates.isEmpty()) {
            builder.setMessage(context.getString(R.string.mission_template_empty))
                .setPositiveButton(android.R.string.ok, null)
                .show()
            return
        }

        val labels = templates.map { t ->
            val icon = t.get("icon")?.asString ?: "📋"
            val label = t.get("label")?.asString ?: ""
            val ruleType = t.get("ruleType")?.asString ?: ""
            "$icon $label  [$ruleType]"
        }.toTypedArray()

        builder.setItems(labels) { _, which ->
            val tpl = templates[which]
            val name = tpl.get("name")?.asString ?: ""
            val desc = tpl.get("description")?.asString ?: ""
            val ruleType = tpl.get("ruleType")?.asString ?: "WORKFLOW"
            onTemplateSelected(name, desc, ruleType)
        }
        .setNegativeButton(android.R.string.cancel, null)
        .show()
    }
}
```

**Step 2: 驗證編譯**

```bash
JAVA_HOME="/c/Program Files/Android/Android Studio/jbr" ./gradlew.bat compileDebugKotlin
```

**Step 3: Commit**

```bash
git add app/src/main/java/com/hank/clawlive/ui/mission/RuleGalleryDialog.kt
git commit -m "feat(android): add RuleGalleryDialog for rule template selection"
```

---

## Task 9：Android — 在 Soul/Rule 新增對話框加入「從模板選擇」按鈕

**Files:**
- Modify: Soul 新增對話框（先執行以下步驟找出確切檔案）
- Modify: Rule 新增對話框

**Step 1: 找出現有 Soul 和 Rule 的新增 UI 位置**

```bash
grep -r "add_soul\|addSoul\|showAddSoul\|AddSoulDialog\|dlg_soul" \
  app/src/main/java --include="*.kt" -l

grep -r "add_rule\|addRule\|showAddRule\|AddRuleDialog\|dlg_rule" \
  app/src/main/java --include="*.kt" -l
```

**Step 2: 在找到的 Soul 新增 UI（Activity 或 Fragment）找到「確定/新增」按鈕邏輯，在其上方加入「從模板選擇」按鈕**

通常在 `AlertDialog.Builder` 的 `setView()` 或 `setCustomView()` 裡，找到 soul name 輸入框，在旁邊加入按鈕邏輯：

```kotlin
// 在 soul 新增 dialog 建立後，加入「從模板選擇」功能
val btnChooseTemplate = view.findViewById<Button>(R.id.btn_soul_choose_template)
btnChooseTemplate?.setOnClickListener {
    viewModel.fetchSoulTemplates()
    viewLifecycleOwner.lifecycleScope.launch {
        viewModel.soulTemplates.collect { templates ->
            if (templates.isNotEmpty()) {
                SoulGalleryDialog(requireContext(), templates) { name, desc ->
                    etSoulName.setText(name)
                    etSoulDesc.setText(desc)
                }.show()
            }
        }
    }
}
```

**注意**：若 soul/rule 的新增邏輯是用 `MaterialAlertDialogBuilder` + custom XML layout，需先在對應 layout XML 加入按鈕：

在 soul 對話框的 layout XML（例如 `dialog_add_soul.xml`）加入：
```xml
<Button
    android:id="@+id/btn_soul_choose_template"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:text="@string/mission_choose_from_template"
    style="@style/Widget.Material3.Button.OutlinedButton"
    android:layout_marginBottom="8dp" />
```

若沒有獨立 layout XML（直接用 code 建），則改用 `LinearLayout` 程式碼建構加入按鈕。

**Step 3: 對 Rule 做同樣處理（多設定 ruleType spinner/dropdown）**

```kotlin
val btnChooseTemplate = view.findViewById<Button>(R.id.btn_rule_choose_template)
btnChooseTemplate?.setOnClickListener {
    viewModel.fetchRuleTemplates()
    viewLifecycleOwner.lifecycleScope.launch {
        viewModel.ruleTemplates.collect { templates ->
            if (templates.isNotEmpty()) {
                RuleGalleryDialog(requireContext(), templates) { name, desc, ruleType ->
                    etRuleName.setText(name)
                    etRuleDesc.setText(desc)
                    // 設定 ruleType spinner 選中對應項目
                    val idx = ruleTypeOptions.indexOf(ruleType)
                    if (idx >= 0) spinnerRuleType.setSelection(idx)
                }.show()
            }
        }
    }
}
```

**Step 4: 驗證編譯**

```bash
JAVA_HOME="/c/Program Files/Android/Android Studio/jbr" ./gradlew.bat compileDebugKotlin
```

預期：BUILD SUCCESSFUL（若有錯誤，根據錯誤訊息調整 view ID 或套件）

**Step 5: Commit**

```bash
git add app/src/main/java/com/hank/clawlive/ui/mission/
git add app/src/main/res/layout/
git commit -m "feat(android): wire SoulGalleryDialog and RuleGalleryDialog into add dialogs"
```

---

## Task 10：更新 README 貢獻說明

**Files:**
- Modify: `README.md`

**Step 1: 找到現有 skill-templates 貢獻說明的位置（第 ~166-224 行），在其後面新增**

```markdown
### Contributing Soul Templates

Community members can contribute soul (personality) templates for others to use.

1. Fork this repository
2. Edit `backend/data/soul-templates.json` — add one object to the array
3. Required fields:

   | Field | Type | Description |
   |-------|------|-------------|
   | `id` | string | Unique kebab-case identifier |
   | `label` | string | Display name shown in the gallery |
   | `icon` | string | Single emoji |
   | `name` | string | Pre-filled name for the soul |
   | `description` | string | Personality description (**must be in English**) |
   | `author` | string | Your GitHub username |
   | `updatedAt` | string | Format: `YYYY-MM-DD` |

4. Additional fields are allowed (e.g., `personality`, `examplePhrases`)
5. Open a Pull Request

### Contributing Rule Templates

Community members can contribute rule templates for common workflows and communication patterns.

1. Fork this repository
2. Edit `backend/data/rule-templates.json` — add one object to the array
3. Required fields:

   | Field | Type | Description |
   |-------|------|-------------|
   | `id` | string | Unique kebab-case identifier |
   | `label` | string | Display name shown in the gallery |
   | `icon` | string | Single emoji |
   | `ruleType` | string | One of: `WORKFLOW`, `COMMUNICATION`, `CODE_REVIEW`, `DEPLOYMENT`, `SYNC`, `HEARTBEAT` |
   | `name` | string | Pre-filled rule name |
   | `description` | string | Rule content, supports Markdown (**must be in English**) |
   | `author` | string | Your GitHub username |
   | `updatedAt` | string | Format: `YYYY-MM-DD` |

4. Open a Pull Request
```

**Step 2: Commit**

```bash
git add README.md
git commit -m "docs: add soul & rule template contribution guide to README"
```

---

## Task 11：最終部署推送

**Step 1: 確認所有 commits 完整**

```bash
git log --oneline -10
```

**Step 2: 推送到 main 觸發 Railway 重新部署**

```bash
git push origin main
```

**Step 3: 驗證 Railway 部署後 API 正常**

```bash
curl -s https://eclawbot.com/api/soul-templates | python -m json.tool | head -20
curl -s https://eclawbot.com/api/rule-templates | python -m json.tool | head -20
```

預期：各回傳 2 個模板

---

## 執行順序總覽

| Task | 內容 | 預估步驟數 |
|------|------|-----------|
| 1 | soul-templates.json | 2 |
| 2 | rule-templates.json | 2 |
| 3 | 後端 API endpoints | 4 |
| 4 | Web Portal soul + rule gallery | 8 |
| 5 | Android i18n 字串 | 3 |
| 6 | MissionViewModel fetch 方法 | 5 |
| 7 | SoulGalleryDialog.kt | 4 |
| 8 | RuleGalleryDialog.kt | 3 |
| 9 | 接入 soul/rule add dialog | 5 |
| 10 | README 更新 | 2 |
| 11 | 部署推送 | 3 |
