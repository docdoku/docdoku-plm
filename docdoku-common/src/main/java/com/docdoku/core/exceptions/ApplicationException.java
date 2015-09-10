/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2015 DocDoku SARL
 *
 * This file is part of DocDokuPLM.
 *
 * DocDokuPLM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DocDokuPLM is distributed in the hope that it will be useful,  
 * but WITHOUT ANY WARRANTY; without even the implied warranty of  
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the  
 * GNU Affero General Public License for more details.  
 *  
 * You should have received a copy of the GNU Affero General Public License  
 * along with DocDokuPLM.  If not, see <http://www.gnu.org/licenses/>.  
 */

package com.docdoku.core.exceptions;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 *
 * @author Florent Garin
 */
public abstract class ApplicationException extends Exception{
    
    private static final String DEFAULT_BUNDLE_NAME = "com.docdoku.core.i18n.LocalStrings";
    private ResourceBundle mResourceBundle;

    public ApplicationException(String pMessage, Throwable pCause, String pBundleName) {
        super(pMessage, pCause);
        mResourceBundle=ResourceBundle.getBundle(pBundleName, Locale.getDefault());
    }
    
    public ApplicationException(String pMessage) {
        super(pMessage);
        mResourceBundle=ResourceBundle.getBundle(DEFAULT_BUNDLE_NAME, Locale.getDefault());
    }
    
    public ApplicationException(String pMessage, Throwable pCause) {
        super(pMessage, pCause);
        mResourceBundle=ResourceBundle.getBundle(DEFAULT_BUNDLE_NAME, Locale.getDefault());
    }
    
    public ApplicationException(Locale pLocale) {
        super();
        mResourceBundle=ResourceBundle.getBundle(DEFAULT_BUNDLE_NAME, pLocale);
    }
    
    public ApplicationException(Locale pLocale, Throwable pCause) {
        super(pCause);
        mResourceBundle=ResourceBundle.getBundle(DEFAULT_BUNDLE_NAME, pLocale);
    }
     public void setLocale(Locale pLocale){
        mResourceBundle=ResourceBundle.getBundle(DEFAULT_BUNDLE_NAME, pLocale);
    }
    
    protected String getBundleDefaultMessage(){
        return getBundleMessage(getClass().getSimpleName());
    }
    
    protected String getBundleMessage(String pKey){
        return mResourceBundle.getString(pKey);
    }
    
    @Override
    public String getMessage() {
        String detailMessage=super.getMessage();
        return detailMessage==null?getLocalizedMessage():detailMessage;
    }
    
    @Override
     public abstract String getLocalizedMessage();

    @Override
    public String toString() {
        return getMessage();
    }
}
