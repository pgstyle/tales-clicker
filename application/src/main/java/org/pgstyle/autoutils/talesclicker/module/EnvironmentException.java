package org.pgstyle.autoutils.talesclicker.module;

/**
 * The {@code EnvironmentException} indicate a problem related to the
 * Application {@link Environment}.
 *
 * @since 1.0
 * @author PGKan
 */
public class EnvironmentException extends RuntimeException {

    public EnvironmentException() {}

    /**
     * @param message the exception message
     */
    public EnvironmentException(String message) {super(message);}

    /**
     * @param cause the underlying cause
     */
    public EnvironmentException(Throwable cause) {super(cause);}

    /**
     * @param message the exception message
     * @param cause the underlying cause
     */
    public EnvironmentException(String message, Throwable cause) {super(message, cause);}

}
