package de.patrick.schmidt.blogcms.tag.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TagTranslationRequest(@NotBlank @Size(max = 80) String name) {}
