package de.patrick.schmidt.blogcms.common.exception;

public class BusinessException extends LocalizedException {

    public BusinessException(String message) {
        super(message);
    }

    private BusinessException(String messageKey, Object[] args) {
        super(messageKey, args);
    }

    /** Erzeugt eine Exception mit lokalisierbarem Message-Key (ADR-018 Kapitel 11). */
    public static BusinessException of(String messageKey, Object... args) {
        return new BusinessException(messageKey, args);
    }
}
