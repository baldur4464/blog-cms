package de.patrick.schmidt.blogcms.category.dto;

import java.util.List;
import java.util.UUID;

/**
 * Lokalisierte Kategorie-Darstellung (ADR-018 Kapitel 9).
 * {@code translated=false} signalisiert, dass auf die Default-Locale zurückgefallen wurde.
 */
public record CategoryResponse(
        UUID id,
        String locale,
        String name,
        String slug,
        String description,
        boolean translated,
        List<String> availableLocales
) {}
