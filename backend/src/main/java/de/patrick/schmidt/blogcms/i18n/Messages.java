package de.patrick.schmidt.blogcms.i18n;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

/**
 * Bequemer Zugriff auf lokalisierte Meldungen in der aktuell aufgelösten Locale
 * (ADR-018 Kapitel 11).
 */
@Component
@RequiredArgsConstructor
public class Messages {

    private final MessageSource messageSource;

    public String get(String code, Object... args) {
        return messageSource.getMessage(code, args, LocaleContextHolder.getLocale());
    }
}
