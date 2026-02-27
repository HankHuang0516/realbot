# Release v1.0.31 - 2026-02-27

## What's New / 更新內容

### English
- [Feature] Detect bot gateway disconnection (pairing required) and notify device users in real-time
- [Feature] Record all handshake failures to PostgreSQL for analysis (`handshake_failures` table)
- [Feature] Add push health status tracking for bots
- [Feature] Add `skills_documentation_url` to bind response for large skill doc fetch
- [Fix] Replace inline 44KB skill doc with short hint in bind response (performance improvement)
- [Fix] Improve local/private IP webhook rejection with OpenClaw Overview guidance
- [Fix] Update sessions_send error messages to reference official docs
- [Fix] Let entity cards scroll independently via RecyclerView
- [Fix] Restore item_entity_card.xml layout (removed in RecyclerView refactor)

### 繁體中文
- [新功能] 偵測機器人 Gateway 斷線（需要重新配對）並即時通知裝置用戶
- [新功能] 將所有 Handshake 失敗記錄存入 PostgreSQL 供分析（`handshake_failures` 表）
- [新功能] 新增機器人推送健康狀態追蹤
- [新功能] 綁定回應新增 `skills_documentation_url`，支援大型技能文件獨立載入
- [修復] 綁定回應中以簡短提示取代內嵌 44KB 技能文件（效能改善）
- [修復] 改善本機/私有 IP Webhook 拒絕提示，導引至 OpenClaw 文件
- [修復] 更新 sessions_send 錯誤訊息以引用官方文件
- [修復] 實體卡片改用 RecyclerView 獨立捲動
- [修復] 還原 item_entity_card.xml 佈局檔（RecyclerView 重構時誤刪）

## Technical Changes
- Backend: `handshake_failures` PostgreSQL table + `logHandshakeFailure()` helper + `GET /api/handshake-failures` endpoint
- Backend: `pushToBot()` detects "pairing required" / "gateway closed" in HTTP 200 response body, returns `pushed: false`
- Backend: `skills_documentation_url` replaces inline skill doc in bind response
- Backend: Push health status tracking for bot monitoring
- Android: Entity card layout restored, RecyclerView independent scrolling
- Android: versionCode 33, versionName 1.0.31
