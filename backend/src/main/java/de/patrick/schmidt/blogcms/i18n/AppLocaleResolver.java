package de.patrick.schmidt.blogcms.i18n;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.servlet.LocaleResolver;

import java.util.Locale;

/**
 * Locale-Negotiation gemäß ADR-018 Kapitel 8:
 * <pre>
 *   1. ?locale= Query-Parameter
 *   2. User.preferredLocale        (ergänzt im User-i18n-Schritt)
 *   3. Accept-Language Header
 *   4. konfigurierte default-locale
 * </pre>
 */
@RequiredArgsConstructor
public class AppLocaleResolver implements LocaleResolver {

    public static final String QUERY_PARAM = "locale";

    private final LocaleService localeService;

    @Override
    public Locale resolveLocale(HttpServletRequest request) {
        return localeService.match(request.getParameter(QUERY_PARAM))
                .or(() -> localeService.matchAcceptLanguage(request.getHeader("Accept-Language")))
                .orElseGet(localeService::getDefaultLocale);
    }

    @Override
    public void setLocale(HttpServletRequest request, HttpServletResponse response, Locale locale) {
        // stateless — keine serverseitige Persistenz der Locale
    }
}
