package com.docdoku.core.exceptions;


/**
 * @author Morgan
 * @version 1.0.0
 */
public class ImportPreviewException extends Exception {
    public ImportPreviewException() {
    }

    public ImportPreviewException(String s) {
        super(s);
    }

    public ImportPreviewException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public ImportPreviewException(Throwable throwable) {
        super(throwable);
    }
}
