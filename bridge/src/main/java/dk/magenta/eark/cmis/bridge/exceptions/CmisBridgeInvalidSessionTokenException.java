package dk.magenta.eark.cmis.bridge.exceptions;

/**
 * @author DarkStar1.
 */
public class CmisBridgeInvalidSessionTokenException extends RuntimeException {
    /**
     * Constructs a new runtime exception with {@code null} as its
     * detail message.  The cause is not initialized, and may subsequently be
     * initialized by a call to {@link #initCause}.
     */
    public CmisBridgeInvalidSessionTokenException() {
        super();
    }

    /**
     * Constructs a new runtime exception with the specified detail message.
     * The cause is not initialized, and may subsequently be initialized by a
     * call to {@link #initCause}.
     *
     * @param message the detail message. The detail message is saved for
     *                later retrieval by the {@link #getMessage()} method.
     */
    public CmisBridgeInvalidSessionTokenException(String message) {
        super(message);
    }
}
