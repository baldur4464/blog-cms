package de.patrick.schmidt.blogcms.post.dto;

import de.patrick.schmidt.blogcms.post.Post;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PostDetailResponse(
        UUID id,
        String title,
        String slug,
        String excerpt,
        String content,
        String status,
        String authorUsername,
        UUID categoryId,
        String categoryName,
        List<UUID> tagIds,
        List<String> tags,
        Instant publishedAt,
        Instant createdAt,
        Instant updatedAt
) {
    public static PostDetailResponse from(Post post) {
        return new PostDetailResponse(
                post.getId(),
                post.getTitle(),
                post.getSlug(),
                post.getExcerpt(),
                post.getContent(),
                post.getStatus().name(),
                post.getAuthor().getUsername(),
                post.getCategory() != null ? post.getCategory().getId() : null,
                post.getCategory() != null ? post.getCategory().getName() : null,
                post.getTags().stream().map(t -> t.getId()).toList(),
                post.getTags().stream().map(t -> t.getName()).toList(),
                post.getPublishedAt(),
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }
}
