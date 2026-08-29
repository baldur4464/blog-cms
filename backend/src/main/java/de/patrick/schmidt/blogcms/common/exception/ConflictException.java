package de.patrick.schmidt.blogcms.common.exception;

public class ConflictException extends LocalizedException {

    public ConflictException(String message) {
        super(message);
    }

    private ConflictException(String messageKey, Object[] args) {
        super(messageKey, args);
    }

    /** Erzeugt eine Exception mit lokalisierbarem Message-Key (ADR-018 Kapitel 11). */
    public static ConflictException of(String messageKey, Object... args) {
        return new ConflictException(messageKey, args);
    }
}
