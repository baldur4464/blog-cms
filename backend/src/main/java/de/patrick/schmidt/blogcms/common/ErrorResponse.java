package de.patrick.schmidt.blogcms.common;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String code,
        String message,
        String path,
        List<FieldError> errors
) {
    public record FieldError(String field, String code, String message) {}

    public static ErrorResponse of(int status, String error, ErrorCode code, String message, String path) {
        return new ErrorResponse(Instant.now(), status, error, code.name(), message, path, null);
    }

    public static ErrorResponse withFields(int status, String error, ErrorCode code,
                                           String message, String path, List<FieldError> errors) {
        return new ErrorResponse(Instant.now(), status, error, code.name(), message, path, errors);
    }
}
