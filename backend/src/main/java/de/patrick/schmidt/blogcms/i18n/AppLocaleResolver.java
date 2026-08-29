package de.patrick.schmidt.blogcms.i18n;

import de.patrick.schmidt.blogcms.user.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.LocaleResolver;

import java.util.Locale;
import java.util.Optional;

/**
 * Locale-Negotiation gemäß ADR-018 Kapitel 8:
 * <pre>
 *   1. ?locale= Query-Parameter
 *   2. User.preferredLocale (angemeldeter Benutzer)
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
                .or(this::preferredLocaleOfAuthenticatedUser)
                .or(() -> localeService.matchAcceptLanguage(request.getHeader("Accept-Language")))
                .orElseGet(localeService::getDefaultLocale);
    }

    private Optional<Locale> preferredLocaleOfAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof User user) {
            return localeService.match(user.getPreferredLocale());
        }
        return Optional.empty();
    }

    @Override
    public void setLocale(HttpServletRequest request, HttpServletResponse response, Locale locale) {
        // stateless — keine serverseitige Persistenz der Locale
    }
}
