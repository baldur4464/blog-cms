package de.patrick.schmidt.blogcms.post.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record CreatePostRequest(
        @NotBlank @Size(max = 200) String title,
        @NotBlank String content,
        @Size(max = 500) String excerpt,
        @NotBlank String locale,
        UUID translationOf,
        UUID categoryId,
        List<UUID> tagIds
) {}
