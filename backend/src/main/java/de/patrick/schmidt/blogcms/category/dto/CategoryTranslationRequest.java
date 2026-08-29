package de.patrick.schmidt.blogcms.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryTranslationRequest(
        @NotBlank @Size(max = 100) String name,
        @Size(max = 500) String description
) {}
