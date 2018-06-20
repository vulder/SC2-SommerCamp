package de.uni_passau.fim.sommercamp.sc2;

/**
 * Exception for signaling that a unit was not found.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class UnitNotFoundException extends RuntimeException {

    public UnitNotFoundException(String message) {
        super(message);
    }

    public UnitNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnitNotFoundException(Throwable cause) {
        super(cause);
    }

    public UnitNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
