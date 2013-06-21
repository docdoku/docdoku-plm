package com.docdoku.cli.exceptions;


public class StatusException extends Exception{

    public StatusException() {}

    public StatusException(String message) {
        super(message);
    }

    public StatusException(Throwable cause) {
        super(cause);
    }

    public StatusException(String message, Throwable cause) {
        super(message, cause);
    }
}
