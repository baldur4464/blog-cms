package de.patrick.schmidt.blogcms.common.exception;

/**
 * Basis für fachliche Exceptions, deren Meldung entweder ein fertiger Text ist
 * oder ein Message-Bundle-Schlüssel, den der GlobalExceptionHandler in der
 * aufgelösten Locale übersetzt (ADR-018 Kapitel 11).
 */
public abstract class LocalizedException extends RuntimeException {

    private final String messageKey;
    private final transient Object[] args;

    protected LocalizedException(String message) {
        super(message);
        this.messageKey = null;
        this.args = null;
    }

    protected LocalizedException(String messageKey, Object[] args) {
        super(messageKey);
        this.messageKey = messageKey;
        this.args = args;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public Object[] getArgs() {
        return args;
    }
}
