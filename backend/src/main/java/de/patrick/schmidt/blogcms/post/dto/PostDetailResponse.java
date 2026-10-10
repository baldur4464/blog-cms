package de.patrick.schmidt.blogcms.post.dto;

import de.patrick.schmidt.blogcms.post.Post;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PostDetailResponse(
        UUID id,
        String locale,
        UUID translationGroupId,
        String title,
        String slug,
        String excerpt,
        String content,
        String status,
        List<TranslationRef> availableTranslations,
        String authorUsername,
        UUID categoryId,
        String categoryName,
        List<UUID> tagIds,
        List<String> tags,
        UUID coverMediaId,
        String coverImageUrl,
        Instant publishedAt,
        Instant createdAt,
        Instant updatedAt
) {
    /** Verweis auf eine Sprachversion derselben Übersetzungsgruppe (ADR-018 Kapitel 9). */
    public record TranslationRef(String locale, String slug) {}

    public static PostDetailResponse from(Post post, List<Post> groupSiblings, String categoryName, List<String> tagNames) {
        List<TranslationRef> translations = groupSiblings.stream()
                .sorted((a, b) -> a.getLocale().compareTo(b.getLocale()))
                .map(p -> new TranslationRef(p.getLocale(), p.getSlug()))
                .toList();
        return new PostDetailResponse(
                post.getId(),
                post.getLocale(),
                post.getTranslationGroupId(),
                post.getTitle(),
                post.getSlug(),
                post.getExcerpt(),
                post.getContent(),
                post.getStatus().name(),
                translations,
                post.getAuthor().getUsername(),
                post.getCategory() != null ? post.getCategory().getId() : null,
                categoryName,
                post.getTags().stream().map(t -> t.getId()).toList(),
                tagNames,
                post.getCoverMedia() != null ? post.getCoverMedia().getId() : null,
                post.getCoverMedia() != null ? post.getCoverMedia().getUrl() : null,
                post.getPublishedAt(),
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }
}
