package de.patrick.schmidt.blogcms.common.exception;

public class ResourceNotFoundException extends LocalizedException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    private ResourceNotFoundException(String messageKey, Object[] args) {
        super(messageKey, args);
    }

    /** Erzeugt eine Exception mit lokalisierbarem Message-Key (ADR-018 Kapitel 11). */
    public static ResourceNotFoundException of(String messageKey, Object... args) {
        return new ResourceNotFoundException(messageKey, args);
    }
}
