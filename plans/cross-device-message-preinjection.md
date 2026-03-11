# Cross-Device Message Pre-Injection Interface

## 概要
設計一個介面讓裝置擁有者可以為「收到的跨裝置訊息」增加預注入指令（pre-injection instructions），類似 AI 官方 bot 的 system prompt。當其他裝置透過 public code 發送 cross-speak 訊息給本裝置的 entity 時，這段預注入指令會被加入 push 訊息中，讓 bot 按照擁有者設定的規則行為。

## 使用情境
- 裝置擁有者想讓 bot 收到跨裝置訊息時，自動翻譯成中文
- 限制 bot 回覆的語氣或內容（如：「用繁體中文回覆，語氣友善」）
- 對不同來源設定不同的處理規則
- 類似官方 bot push 中的 `[ACTION REQUIRED]` 指令區段

## 實作計畫

### Step 1: Backend — 擴展 device-preferences
**檔案**: `backend/device-preferences.js`

在 `DEFAULTS` 加入新欄位：
```js
cross_device_pre_inject: ''  // 空字串 = 不注入
```

修改 `updatePrefs()` — 目前只支持 boolean，需改為也支援 string 類型的 pref（`cross_device_pre_inject`）。

### Step 2: Backend — 在 cross-speak push 中注入
**檔案**: `backend/index.js` (約 line 4117)

在 `POST /api/entity/cross-speak` 的 push 訊息組裝中，讀取目標裝置的 `cross_device_pre_inject` pref，若非空則插入到 push 訊息中（在 `[CROSS-DEVICE MESSAGE]` 之前）：

```
[DEVICE OWNER INSTRUCTION]
{cross_device_pre_inject 的內容}
```

### Step 3: Web Portal — settings.html 新增 UI
**檔案**: `backend/public/portal/settings.html`

在 Broadcast Settings card 後面新增「Cross-Device Message Settings」card：
- 標題：📨 Cross-Device Message Settings
- 說明文字：Configure pre-injected instructions for incoming cross-device messages
- textarea 輸入框（最大 500 字元），placeholder 範例
- Save 按鈕
- 用現有的 `apiCall('PUT', '/api/device-preferences', ...)` pattern

### Step 4: Android App — 對應設定頁面
**檔案**: 需要在 Android settings 中新增對應 UI

在 Android 的設定頁面加入相同的 cross-device pre-injection 設定項，使用相同的 API endpoint。

### Step 5: i18n 支援
在 `translations.json` 加入相關翻譯字串（中文/英文）。

## 不做的事
- 不做 per-entity 或 per-contact 的預注入（MVP 先做 device-level）
- 不做 broadcast 的預注入（只針對 cross-speak）
- 不修改 gatekeeper 邏輯
