package de.patrick.schmidt.blogcms.media.dto;

import de.patrick.schmidt.blogcms.media.Media;

import java.time.Instant;
import java.util.UUID;

public record MediaResponse(
        UUID id,
        String filename,
        String originalName,
        String mimeType,
        long size,
        String url,
        Instant createdAt
) {
    public static MediaResponse from(Media m) {
        return new MediaResponse(
                m.getId(), m.getFilename(), m.getOriginalName(),
                m.getMimeType(), m.getSize(), m.getUrl(), m.getCreatedAt()
        );
    }
}
