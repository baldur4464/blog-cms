package de.patrick.schmidt.blogcms.user.dto;

import de.patrick.schmidt.blogcms.user.User;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String username,
        String email,
        String role,
        String status,
        Instant createdAt,
        Instant updatedAt
) {
    public static UserResponse from(User u) {
        return new UserResponse(
                u.getId(), u.getUsername(), u.getEmail(),
                u.getRole().name(), u.getStatus().name(),
                u.getCreatedAt(), u.getUpdatedAt()
        );
    }
}
