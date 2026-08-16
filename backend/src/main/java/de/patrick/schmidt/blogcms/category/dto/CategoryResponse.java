package de.patrick.schmidt.blogcms.category.dto;

import de.patrick.schmidt.blogcms.category.Category;

import java.util.UUID;

public record CategoryResponse(UUID id, String name, String slug, String description) {
    public static CategoryResponse from(Category c) {
        return new CategoryResponse(c.getId(), c.getName(), c.getSlug(), c.getDescription());
    }
}
