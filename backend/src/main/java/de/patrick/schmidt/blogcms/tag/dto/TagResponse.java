package de.patrick.schmidt.blogcms.tag.dto;

import java.util.List;
import java.util.UUID;

/**
 * Lokalisierte Tag-Darstellung (ADR-018 Kapitel 9).
 * {@code translated=false} signalisiert, dass auf die Default-Locale zurückgefallen wurde.
 */
public record TagResponse(
        UUID id,
        String locale,
        String name,
        String slug,
        boolean translated,
        List<String> availableLocales
) {}
