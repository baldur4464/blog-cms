package de.patrick.schmidt.blogcms.post.dto;

import de.patrick.schmidt.blogcms.post.Post;

import java.time.Instant;
import java.util.UUID;

public record PostSummaryResponse(
        UUID id,
        String locale,
        String title,
        String slug,
        String excerpt,
        String status,
        String authorUsername,
        String categoryName,
        Instant publishedAt,
        Instant createdAt,
        Instant updatedAt
) {
    public static PostSummaryResponse from(Post post) {
        return new PostSummaryResponse(
                post.getId(),
                post.getLocale(),
                post.getTitle(),
                post.getSlug(),
                post.getExcerpt(),
                post.getStatus().name(),
                post.getAuthor().getUsername(),
                post.getCategory() != null ? post.getCategory().getName() : null,
                post.getPublishedAt(),
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }
}
