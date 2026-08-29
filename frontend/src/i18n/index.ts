import i18n from 'i18next';
import { initReactI18next } from 'react-i18next';
import LanguageDetector from 'i18next-browser-languagedetector';

// ADR-018 Kapitel 12: Admin-Panel-Oberflächensprache (react-i18next).
export const SUPPORTED_LOCALES = ['de', 'en'] as const;
export const DEFAULT_LOCALE = 'de';

export type SupportedLocale = (typeof SUPPORTED_LOCALES)[number];

const resources = {
  de: {
    translation: {
      nav: {
        posts: 'Beiträge',
        categories: 'Kategorien',
        tags: 'Tags',
        media: 'Medien',
        users: 'Benutzer',
      },
      common: {
        loading: 'Laden…',
        logout: 'Abmelden',
        language: 'Sprache',
      },
    },
  },
  en: {
    translation: {
      nav: {
        posts: 'Posts',
        categories: 'Categories',
        tags: 'Tags',
        media: 'Media',
        users: 'Users',
      },
      common: {
        loading: 'Loading…',
        logout: 'Sign out',
        language: 'Language',
      },
    },
  },
};

// Auflösungsreihenfolge für anonyme/lokale Wahl (ADR-018 Kapitel 3):
// localStorage (explizite Wahl) → Browser → default-locale.
i18n
  .use(LanguageDetector)
  .use(initReactI18next)
  .init({
    resources,
    supportedLngs: [...SUPPORTED_LOCALES],
    fallbackLng: DEFAULT_LOCALE,
    load: 'languageOnly',
    detection: {
      order: ['localStorage', 'navigator'],
      lookupLocalStorage: 'locale',
      caches: ['localStorage'],
    },
    interpolation: { escapeValue: false },
  });

export default i18n;
