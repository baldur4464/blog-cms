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
        String coverImageUrl,
        Instant publishedAt,
        Instant createdAt,
        Instant updatedAt
) {
    public static PostSummaryResponse from(Post post, String categoryName) {
        return new PostSummaryResponse(
                post.getId(),
                post.getLocale(),
                post.getTitle(),
                post.getSlug(),
                post.getExcerpt(),
                post.getStatus().name(),
                post.getAuthor().getUsername(),
                categoryName,
                post.getCoverMedia() != null ? post.getCoverMedia().getUrl() : null,
                post.getPublishedAt(),
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }
}
