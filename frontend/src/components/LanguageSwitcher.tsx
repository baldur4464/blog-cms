import { useTranslation } from 'react-i18next';
import { useAuth } from '../auth/AuthContext';
import { usersApi } from '../api/users';
import { SUPPORTED_LOCALES } from '../i18n';

/**
 * Sprachumschalter (ADR-018 Kapitel 3 & 12).
 * Persistiert die Wahl lokal (localStorage via i18next) und – wenn angemeldet –
 * zusätzlich am Benutzerkonto (preferredLocale).
 */
export default function LanguageSwitcher() {
  const { t, i18n } = useTranslation();
  const { user } = useAuth();
  const current = i18n.resolvedLanguage ?? i18n.language;

  const change = async (locale: string) => {
    if (locale === current) return;
    await i18n.changeLanguage(locale);
    if (user) {
      try {
        await usersApi.setPreferredLocale(locale);
      } catch {
        // Best-effort: UI ist bereits umgestellt und lokal persistiert.
      }
    }
  };

  return (
    <div className="flex items-center gap-1" role="group" aria-label={t('common.language')}>
      {SUPPORTED_LOCALES.map((loc) => (
        <button
          key={loc}
          type="button"
          onClick={() => change(loc)}
          aria-pressed={current === loc}
          className={`text-xs px-1.5 py-0.5 rounded transition-colors ${
            current === loc ? 'text-white font-semibold' : 'text-gray-500 hover:text-white'
          }`}
        >
          {loc.toUpperCase()}
        </button>
      ))}
    </div>
  );
}
