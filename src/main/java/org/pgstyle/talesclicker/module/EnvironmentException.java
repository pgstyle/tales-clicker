package org.pgstyle.talesclicker.module;

public class EnvironmentException extends RuntimeException {

    public EnvironmentException() {
    }

    public EnvironmentException(String message) {
        super(message);
    }

    public EnvironmentException(Throwable cause) {
        super(cause);
    }

    public EnvironmentException(String message, Throwable cause) {
        super(message, cause);
    }

}
