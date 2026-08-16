package de.patrick.schmidt.blogcms.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Sprach-Konfiguration (ADR-018 Kapitel 13).
 * Neue Sprachen werden ausschließlich hier ergänzt – ohne Schema-Änderung.
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.i18n")
public class I18nProperties {

    private String defaultLocale = "de";
    private List<String> supportedLocales = List.of("de", "en");

    /** Startup-Check (ADR-018 Kapitel 13): Default-Locale muss Teil der Supported-Locales sein. */
    @PostConstruct
    void validate() {
        if (supportedLocales == null || supportedLocales.isEmpty()) {
            throw new IllegalStateException("app.i18n.supported-locales must not be empty");
        }
        if (defaultLocale == null || defaultLocale.isBlank()) {
            throw new IllegalStateException("app.i18n.default-locale must be set");
        }
        if (!supportedLocales.contains(defaultLocale)) {
            throw new IllegalStateException(
                    "app.i18n.default-locale '" + defaultLocale + "' is not part of supported-locales " + supportedLocales);
        }
    }
}
