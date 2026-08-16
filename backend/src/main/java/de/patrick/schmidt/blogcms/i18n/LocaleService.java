package de.patrick.schmidt.blogcms.i18n;

import de.patrick.schmidt.blogcms.config.I18nProperties;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Zentrale Locale-Logik (ADR-018 Kapitel 8/13): Validierung und Abgleich
 * angeforderter Sprachen gegen die konfigurierte Allow-List.
 */
@Service
public class LocaleService {

    private final Locale defaultLocale;
    private final List<Locale> supportedLocales;

    public LocaleService(I18nProperties properties) {
        this.defaultLocale = Locale.forLanguageTag(properties.getDefaultLocale());
        this.supportedLocales = properties.getSupportedLocales().stream()
                .map(Locale::forLanguageTag)
                .toList();
    }

    public Locale getDefaultLocale() {
        return defaultLocale;
    }

    public List<Locale> getSupportedLocales() {
        return supportedLocales;
    }

    public boolean isSupported(String languageTag) {
        return match(languageTag).isPresent();
    }

    /** Exakter bzw. sprach-basierter Abgleich eines einzelnen Tags gegen die Supported-Locales. */
    public Optional<Locale> match(String languageTag) {
        if (languageTag == null || languageTag.isBlank()) {
            return Optional.empty();
        }
        Locale requested = Locale.forLanguageTag(languageTag.trim());
        if (requested.getLanguage().isEmpty()) {
            return Optional.empty();
        }
        return supportedLocales.stream()
                .filter(l -> l.toLanguageTag().equalsIgnoreCase(requested.toLanguageTag()))
                .findFirst()
                .or(() -> supportedLocales.stream()
                        .filter(l -> l.getLanguage().equals(requested.getLanguage()))
                        .findFirst());
    }

    /** Beste Übereinstimmung aus einem Accept-Language-Header. */
    public Optional<Locale> matchAcceptLanguage(String headerValue) {
        if (headerValue == null || headerValue.isBlank()) {
            return Optional.empty();
        }
        try {
            List<Locale.LanguageRange> ranges = Locale.LanguageRange.parse(headerValue);
            return Optional.ofNullable(Locale.lookup(ranges, supportedLocales));
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }

    /** Löst einen Tag zu einer Supported-Locale auf, sonst Default-Locale. */
    public Locale resolveOrDefault(String languageTag) {
        return match(languageTag).orElse(defaultLocale);
    }
}
