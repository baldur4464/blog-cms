package de.patrick.schmidt.blogcms.config;

import de.patrick.schmidt.blogcms.i18n.AppLocaleResolver;
import de.patrick.schmidt.blogcms.i18n.ContentLanguageInterceptor;
import de.patrick.schmidt.blogcms.i18n.LocaleService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Verdrahtet die i18n-Bausteine in Spring MVC (ADR-018 Kapitel 8).
 */
@Configuration
@RequiredArgsConstructor
public class WebI18nConfig implements WebMvcConfigurer {

    private final LocaleService localeService;

    @Bean
    public LocaleResolver localeResolver() {
        return new AppLocaleResolver(localeService);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new ContentLanguageInterceptor());
    }
}
