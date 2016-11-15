package com.docdoku.core.exceptions;

import java.text.MessageFormat;
import java.util.Locale;

/**
 * @author Frédéric Maury
 */
public class EffectivityNotFoundException extends EntityNotFoundException {
    private String mId;

    public EffectivityNotFoundException(String pMessage) {
        super(pMessage);
        mId = null;
    }

    public EffectivityNotFoundException(Locale pLocale, String pId) {
        this(pLocale, pId, null);
    }

    public EffectivityNotFoundException(Locale pLocale, String pId, Throwable pCause) {
        super(pLocale, pCause);
        mId = pId;
    }

    @Override
    public String getLocalizedMessage() {
        String message = getBundleDefaultMessage();
        return MessageFormat.format(message, mId);
    }
}
