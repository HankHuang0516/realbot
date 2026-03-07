# Design: E-Claw 品牌定位調整 — AI2AI 溝通 & AI Agent 協作

**日期**: 2026-03-07
**狀態**: 已確認
**範圍**: realbot + openclaw-channel-eclaw 兩個 repo

---

## 背景

E-Claw 原有品牌定位強調「復古電子寵物 × AI 動態桌布」（電子雞、90 年代情懷），
與實際核心功能（AI Agent 間通訊、多模型協作、A2A push）有落差。
本次調整將定位統一改為：**AI Agent 協作 × 即時視覺化**。

---

## 新定位核心文案

### 標語

| 語境 | 舊文案 | 新文案 |
|------|--------|--------|
| 主標語（中） | 復古電子寵物 × AI 動態桌布 | **AI Agent 協作 × 即時視覺化** |
| 副標（中） | 把 90 年代電子雞的靈魂注入你的 Android 桌布 | 讓多個 AI Agent 在 Android 桌布上即時互動——支援 Agent 間通訊（A2A）、廣播推送、多模型協作 |
| 功能名（中） | AI 電子寵物桌布 | **AI Agent 協作平台** |
| 功能描述（中） | 最多 8 個 AI 驅動的實體（免費 4 個、付費 8 個），在你的動態桌布上自由活動 | 最多 8 個 AI Agent 即時視覺化，支援 Agent 間通訊（A2A）與廣播協作 |
| FAQ 定義（英） | live wallpaper companion platform | **AI Agent collaboration and A2A communication platform** |
| FAQ 定義（中） | 動態桌布夥伴平台 | **AI Agent 協作與 A2A 通訊平台** |
| 比較頁副標（英） | full AI companion experience | **full AI Agent collaboration experience** |
| 比較頁副標（中） | AI 夥伴體驗 | **AI Agent 協作體驗** |
| Channel 標籤（英） | E-Claw (AI Live Wallpaper Chat) | **E-Claw (AI Agent Collaboration)** |
| Plugin blurb（英） | an AI chat platform for live wallpaper entities | **the AI Agent collaboration and A2A communication platform** |

---

## 修改範圍

### Repo 1：realbot

#### backend/public/portal/info.html
| 行號 | 舊 | 新 |
|------|----|----|
| 705 | `E-Claw 電子蝦 — 復古電子寵物 × AI 動態桌布` | `E-Claw 電子蝦 — AI Agent 協作 × 即時視覺化` |
| 714 | `把 90 年代電子雞的靈魂注入你的 Android 桌布——由 AI Bot 驅動，24/7 陪伴你。` | `讓多個 AI Agent 在 Android 桌布上即時互動——支援 Agent 間通訊（A2A）、廣播推送、多模型協作。` |
| 720 td[1] | `AI 電子寵物桌布` | `AI Agent 協作平台` |
| 720 td[2] | `最多 8 個 AI 驅動的實體（免費 4 個、付費 8 個），在你的動態桌布上自由活動` | `最多 8 個 AI Agent 即時視覺化，支援 Agent 間通訊（A2A）與廣播協作` |

#### backend/public/shared/i18n.js
| Key | 語系 | 舊 | 新 |
|-----|------|----|-----|
| `faq_a_what_is` | EN | "live wallpaper companion platform..." | "AI Agent collaboration and A2A communication platform..." |
| `faq_a_what_is` | ZH-TW | "動態桌布夥伴平台..." | "AI Agent 協作與 A2A 通訊平台..." |
| `faq_a_what_is` | ZH-CN | 同理更新 | |
| `faq_a_what_is` | JA/KO/TH/VI/ID | 同理更新 | |
| `cmp_subtitle` | EN | "full AI companion experience" | "full AI Agent collaboration experience" |
| `cmp_subtitle` | ZH-TW | "AI 夥伴體驗" | "AI Agent 協作體驗" |
| `cmp_subtitle` | ZH-CN/JA/KO/TH/VI/ID | 同理更新 | |

#### PRIVACY_POLICY.md
| 行號 | 舊 | 新 |
|------|----|----|
| 36 | `桌面寵物 (E-claw)` | `E-Claw 實體（AI 代理）` |
| 90 | `desktop pet (E-claw)` | `E-Claw entity (your AI agent)` |

#### backend/README.md
| 行號 | 舊 | 新 |
|------|----|----|
| 3 | `Node.js backend server for the Claw Live Wallpaper Android app.` | `Node.js backend server for E-Claw — the AI Agent collaboration and A2A communication platform for Android.` |

#### package/package.json
| 欄位 | 舊 | 新 |
|------|----|----|
| description | `E-Claw channel plugin for OpenClaw — AI chat platform for live wallpaper entities` | `E-Claw channel plugin for OpenClaw — AI Agent collaboration and A2A communication platform` |
| selectionLabel | `E-Claw (AI Live Wallpaper Chat)` | `E-Claw (AI Agent Collaboration)` |
| description (plugin) | `Connect OpenClaw to E-Claw — an AI chat platform for live wallpaper entities on Android.` | `Connect OpenClaw to E-Claw — the AI Agent collaboration and A2A communication platform for Android.` |

#### package/README.md
| 行號 | 舊 | 新 |
|------|----|----|
| 3 | `OpenClaw channel plugin for E-Claw — an AI chat platform for live wallpaper entities on Android.` | `OpenClaw channel plugin for E-Claw — the AI Agent collaboration and A2A communication platform for Android.` |

---

### Repo 2：openclaw-channel-eclaw

#### package.json
| 欄位 | 舊 | 新 |
|------|----|----|
| description | `E-Claw channel plugin for OpenClaw — AI chat platform for live wallpaper entities` | `E-Claw channel plugin for OpenClaw — AI Agent collaboration and A2A communication platform` |
| selectionLabel | `E-Claw (AI Live Wallpaper Chat)` | `E-Claw (AI Agent Collaboration)` |
| plugin.description | `Connect OpenClaw to E-Claw — an AI chat platform for live wallpaper entities on Android.` | `Connect OpenClaw to E-Claw — the AI Agent collaboration and A2A communication platform for Android.` |

#### README.md
| 行號 | 舊 | 新 |
|------|----|----|
| 3 | `OpenClaw channel plugin for E-Claw — an AI chat platform for live wallpaper entities on Android.` | `OpenClaw channel plugin for E-Claw — the AI Agent collaboration and A2A communication platform for Android.` |

#### openclaw.plugin.json
| 欄位 | 舊 | 新 |
|------|----|----|
| description | `E-Claw AI chat platform channel for OpenClaw` | `E-Claw AI Agent collaboration channel for OpenClaw` |

#### index.ts (JSDoc)
舊：`E-Claw AI chat platform channel plugin`
新：`E-Claw AI Agent collaboration channel plugin`

#### channel.ts
| 欄位 | 舊 | 新 |
|------|----|----|
| selectionLabel | `E-Claw (AI Live Wallpaper Chat)` | `E-Claw (AI Agent Collaboration)` |
| blurb | `Connect OpenClaw to E-Claw — an AI chat platform for live wallpaper entities on Android.` | `Connect OpenClaw to E-Claw — the AI Agent collaboration and A2A communication platform for Android.` |

#### scripts/openclaw-discussions-post.md
更新前兩段的 intro，保留安裝/配置說明不動。

---

## 不變的部分

- Android `strings.xml` 的 `wallpaper_description`（已是 "Live status of your AI agent"，符合新定位）
- `footer_desc`（已是 "AI collaboration platform for Android live wallpaper"，可接受）
- `cmp_eclaw_tagline` "Native live wallpaper channel"（描述事實功能，可保留）
- `borrow_desc`（描述租用機器人功能，與定位無關）
- 所有技術性 API 文件、程式碼注釋（不涉及品牌定位）

---

## 目標受眾

同時服務：
1. **AI Bot 開發者**：強調 A2A 通訊、多模型協作、技術架構
2. **OpenClaw 一般用戶**：強調即時視覺化、多 Agent 管理的易用性

---

## 實作順序

1. `backend/public/portal/info.html`（影響最大，主要入口頁）
2. `backend/public/shared/i18n.js`（多語系 FAQ + 比較頁）
3. `PRIVACY_POLICY.md`（重要法律文件）
4. `backend/README.md` + `package/package.json` + `package/README.md`
5. `openclaw-channel-eclaw` 所有檔案
