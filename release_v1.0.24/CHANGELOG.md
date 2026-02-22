# v1.0.24 Changelog

## Changes since v1.0.23 (b3e7802)

### New Features
- **Account login**: Add account login to Android app for data recovery after reinstall
- **Email binding**: Add email binding from Android app for web portal login
- **Device transfer**: Add POST /api/admin/transfer-device endpoint with full DB migration
- **Bot mission notes**: Grant bots read-write access to mission_notes
- **Admin dashboard charts**: Add visualization charts for bot stats and platform breakdown
- **Debug KB integration**: Integrate yanhui debug KB into feedback system

### Bug Fixes
- **Feedback button style**: Unify feedback button style with other settings buttons
- **Entity auto-sync**: Auto-sync server-bound entities to local registry
- **JSONB null handling**: Handle null JSONB fields in device transfer migration
- **5 open issues resolved**: Fix issues #34, #35, #36, #37, #38
- **Test device exclusion**: Exclude test devices from admin dashboard

---

### 變更摘要 (中文)

#### 新功能
- **帳號登入**: Android app 新增帳號登入功能，支援重裝後資料恢復
- **Email 綁定**: 從 Android app 綁定 Email 以登入網頁管理後台
- **裝置轉移**: 新增裝置轉移 API，支援完整 DB 資料遷移
- **機器人任務筆記**: 授予機器人任務筆記的讀寫權限
- **管理後台圖表**: 新增機器人統計和平台分佈的視覺化圖表
- **Debug KB 整合**: 將 yanhui debug 知識庫整合至回饋系統

#### 錯誤修復
- **回饋按鈕樣式**: 統一回饋按鈕與其他設定按鈕的樣式
- **實體自動同步**: 自動同步伺服器綁定的實體到本地登錄
- **JSONB 空值處理**: 處理裝置轉移中 JSONB 欄位為空的情況
- **修復 5 個問題**: 修復 issues #34, #35, #36, #37, #38
- **排除測試裝置**: 管理後台排除測試裝置

---

**Files changed**: 18 files, +1649 / -129 lines
**Commits**: 12 (excluding merges and release commit)
