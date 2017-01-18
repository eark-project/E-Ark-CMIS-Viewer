package dk.magenta.eark.cmis.bridge.exceptions;

/**
 * @author DarkStar1.
 */
public class CmisBridgeUserAuthenticationException extends RuntimeException {
    /**
     * Constructs a new runtime exception with {@code null} as its
     * detail message.  The cause is not initialized, and may subsequently be
     * initialized by a call to {@link #initCause}.
     */
    public CmisBridgeUserAuthenticationException() {
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
    public CmisBridgeUserAuthenticationException(String message) {
        super(message);
    }
}
