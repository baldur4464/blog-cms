package de.patrick.schmidt.blogcms.i18n;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpHeaders;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.support.RequestContextUtils;

import java.util.Locale;

/**
 * Spiegelt die aufgelöste Locale in den {@link LocaleContextHolder} (für Services
 * und Exception-Handling) und setzt den {@code Content-Language}-Header
 * (ADR-018 Kapitel 8).
 */
public class ContentLanguageInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        Locale locale = RequestContextUtils.getLocale(request);
        LocaleContextHolder.setLocale(locale);
        response.setHeader(HttpHeaders.CONTENT_LANGUAGE, locale.toLanguageTag());
        return true;
    }
}
