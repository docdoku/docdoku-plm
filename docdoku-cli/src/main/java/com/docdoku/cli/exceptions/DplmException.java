package com.docdoku.cli.exceptions;


public class DplmException extends Exception{

    public DplmException() {}

    public DplmException(String message) {
        super(message);
    }

    public DplmException(Throwable cause) {
        super(cause);
    }

    public DplmException(String message, Throwable cause) {
        super(message, cause);
    }
}
