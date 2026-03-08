import i18next from 'i18next';
import { initReactI18next } from 'react-i18next';
import * as Localization from 'expo-localization';

import en from './en.json';
import zhTW from './zh-TW.json';
import zhCN from './zh-CN.json';
import ja from './ja.json';
import ko from './ko.json';
import th from './th.json';
import vi from './vi.json';
import id from './id.json';

export const SUPPORTED_LANGUAGES = [
  { code: 'en', label: 'English', nativeLabel: 'English' },
  { code: 'zh-TW', label: '繁體中文', nativeLabel: '繁體中文' },
  { code: 'zh-CN', label: '简体中文', nativeLabel: '简体中文' },
  { code: 'ja', label: '日本語', nativeLabel: '日本語' },
  { code: 'ko', label: '한국어', nativeLabel: '한국어' },
  { code: 'th', label: 'ภาษาไทย', nativeLabel: 'ภาษาไทย' },
  { code: 'vi', label: 'Tiếng Việt', nativeLabel: 'Tiếng Việt' },
  { code: 'id', label: 'Bahasa Indonesia', nativeLabel: 'Bahasa Indonesia' },
] as const;

export type LanguageCode = typeof SUPPORTED_LANGUAGES[number]['code'];

/** Detect best matching language from device locale */
function detectLanguage(): string {
  const deviceLocale = Localization.getLocales()[0]?.languageTag ?? 'en';
  const lang = deviceLocale.toLowerCase();

  if (lang.startsWith('zh-hant') || lang === 'zh-tw') return 'zh-TW';
  if (lang.startsWith('zh')) return 'zh-CN';
  if (lang.startsWith('ja')) return 'ja';
  if (lang.startsWith('ko')) return 'ko';
  if (lang.startsWith('th')) return 'th';
  if (lang.startsWith('vi')) return 'vi';
  if (lang.startsWith('id')) return 'id';
  return 'en';
}

i18next.use(initReactI18next).init({
  resources: {
    en: { translation: en },
    'zh-TW': { translation: zhTW },
    'zh-CN': { translation: zhCN },
    ja: { translation: ja },
    ko: { translation: ko },
    th: { translation: th },
    vi: { translation: vi },
    id: { translation: id },
  },
  lng: detectLanguage(),
  fallbackLng: 'en',
  interpolation: { escapeValue: false },
});

export default i18next;
