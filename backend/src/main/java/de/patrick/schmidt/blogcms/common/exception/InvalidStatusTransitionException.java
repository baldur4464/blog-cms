package de.patrick.schmidt.blogcms.common.exception;

public class InvalidStatusTransitionException extends LocalizedException {

    public InvalidStatusTransitionException(String message) {
        super(message);
    }

    private InvalidStatusTransitionException(String messageKey, Object[] args) {
        super(messageKey, args);
    }

    /** Erzeugt eine Exception mit lokalisierbarem Message-Key (ADR-018 Kapitel 11). */
    public static InvalidStatusTransitionException of(String messageKey, Object... args) {
        return new InvalidStatusTransitionException(messageKey, args);
    }
}
