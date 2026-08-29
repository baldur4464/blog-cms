package de.patrick.schmidt.blogcms.tag.dto;

import de.patrick.schmidt.blogcms.tag.TagTranslation;

import java.util.UUID;

public record TagTranslationResponse(
        UUID tagId,
        String locale,
        String name,
        String slug
) {
    public static TagTranslationResponse from(TagTranslation t) {
        return new TagTranslationResponse(t.getTag().getId(), t.getLocale(), t.getName(), t.getSlug());
    }
}
