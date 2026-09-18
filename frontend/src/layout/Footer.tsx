import LanguageSwitcher from '../components/LanguageSwitcher';

/**
 * Globaler Footer mit Sprachumschalter – auf jedem Bildschirm verfügbar
 * (ADR-018 Kapitel 3 & 12).
 */
export default function Footer() {
  return (
    <footer className="border-t border-gray-200 bg-white px-6 py-3 flex items-center justify-between text-xs text-gray-400">
      <span>Blog CMS</span>
      <LanguageSwitcher />
    </footer>
  );
}
