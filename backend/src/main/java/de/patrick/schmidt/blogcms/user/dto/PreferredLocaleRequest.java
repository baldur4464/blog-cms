package de.patrick.schmidt.blogcms.user.dto;

import jakarta.validation.constraints.NotBlank;

public record PreferredLocaleRequest(@NotBlank String preferredLocale) {}
