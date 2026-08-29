package de.patrick.schmidt.blogcms.category.dto;

import de.patrick.schmidt.blogcms.category.CategoryTranslation;

import java.util.UUID;

public record CategoryTranslationResponse(
        UUID categoryId,
        String locale,
        String name,
        String slug,
        String description
) {
    public static CategoryTranslationResponse from(CategoryTranslation t) {
        return new CategoryTranslationResponse(
                t.getCategory().getId(), t.getLocale(), t.getName(), t.getSlug(), t.getDescription());
    }
}
