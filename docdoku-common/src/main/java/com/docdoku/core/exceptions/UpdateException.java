package com.docdoku.core.exceptions;

import java.util.Locale;

/**
 * Created by fredericmaury on 23/11/2016.
 */
public class UpdateException extends ApplicationException {

    public UpdateException(String pMessage) {
        super(pMessage);
    }

    public UpdateException(Locale pLocale) {
        this(pLocale, null);
    }

    public UpdateException(Locale pLocale, Throwable pCause) {
        super(pLocale, pCause);
    }

    @Override
    public String getLocalizedMessage() {
        return getBundleDefaultMessage();
    }
}
