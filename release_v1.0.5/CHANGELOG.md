# Release v1.0.5 - 2026-02-13

## What's New / 更新內容

### English
- [Feature] Added Privacy Policy page and settings button.
- [Feature] Display App Version in Settings.
- [Improvement] Enhanced Settings UI (Button styles, removed 'System Default' language option).
- [Fix] Fixed app crash due to corrupted `EncryptedSharedPreferences` (Clear data on error).
- [Fix] Fixed `UninitializedPropertyAccessException` crash in Settings.
- [Fix] Corrected Entity Count display logic to sync with API and Premium status.

### 繁體中文
- [新功能] 新增隱私權政策頁面與設定按鈕。
- [新功能] 設定頁面顯示 App 版本。
- [改進] 優化設定頁面 UI (按鈕樣式調整、移除「系統預設」語言選項)。
- [修復] 修復 `EncryptedSharedPreferences` 資料損毀導致的崩潰問題 (自動清除錯誤資料)。
- [修復] 修復設定頁面初始化錯誤導致的閃退 (`SettingsActivity`).
- [修復] 修正實體數量顯示邏輯，同步 API 與會員狀態。

## Technical Changes
- Android: Incremented version to 1.0.5 (VersionCode: 6).
- Backend: Updated MCP skill documentation.
