package com.docdoku.core.exceptions;

import com.docdoku.core.product.Effectivity;

import java.text.MessageFormat;
import java.util.Locale;

public class EffectivityAlreadyExistsException extends EntityAlreadyExistsException {
    private Effectivity mEffectivity;

    public EffectivityAlreadyExistsException(String pMessage) {
        super(pMessage);
        mEffectivity=null;
    }

    public EffectivityAlreadyExistsException(Locale pLocale, Effectivity pEffectivity) {
        this(pLocale, pEffectivity, null);
    }

    public EffectivityAlreadyExistsException(Locale pLocale, Effectivity pEffectivity, Throwable pCause) {
        super(pLocale, pCause);
        mEffectivity=pEffectivity;
    }

    @Override
    public String getLocalizedMessage() {
        String message = getBundleDefaultMessage();
        return MessageFormat.format(message, mEffectivity);
    }
}
