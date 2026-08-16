package de.patrick.schmidt.blogcms.tag.dto;

import de.patrick.schmidt.blogcms.tag.Tag;

import java.util.UUID;

public record TagResponse(UUID id, String name, String slug) {
    public static TagResponse from(Tag t) {
        return new TagResponse(t.getId(), t.getName(), t.getSlug());
    }
}
