/*
 * DocDoku, Professional Open Source
 * Copyright 2006, 2007, 2008, 2009 DocDoku SARL
 *
 * This file is part of DocDoku.
 *
 * DocDoku is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DocDoku is distributed in the hope that it will be useful,  
 * but WITHOUT ANY WARRANTY; without even the implied warranty of  
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the  
 * GNU General Public License for more details.  
 *  
 * You should have received a copy of the GNU General Public License  
 * along with DocDoku.  If not, see <http://www.gnu.org/licenses/>.  
 */

package com.docdoku.core;

import com.docdoku.core.entities.keys.Version;
import java.text.MessageFormat;
import java.util.Locale;

/**
 *
 * @author Florent GARIN
 */
public class WorkflowNotFoundException extends ApplicationException {
    
    private String mKey;
    private Object[] mArgs;
    
    
    
    public WorkflowNotFoundException(String pMessage) {
        super(pMessage);
    }
    
    
    public WorkflowNotFoundException(Locale pLocale, int pID) {
        this(pLocale, pID, null);
    }

    public WorkflowNotFoundException(Locale pLocale, int pID, Throwable pCause) {
        super(pLocale, pCause);
        mArgs=new Object[]{pID};
        mKey="WorkflowNotFoundException1";
    }

    public WorkflowNotFoundException(Locale pLocale, String pMDocID, Version pMDocVersion) {
        this(pLocale, pMDocID, pMDocVersion, null);
    }

    public WorkflowNotFoundException(Locale pLocale, String pMDocID, Version pMDocVersion, Throwable pCause) {
        this(pLocale, pMDocID, pMDocVersion.toString(), pCause);
    }

    public WorkflowNotFoundException(Locale pLocale, String pMDocID, String pMDocStringVersion) {
        this(pLocale, pMDocID, pMDocStringVersion, null);
    }

    public WorkflowNotFoundException(Locale pLocale, String pMDocID, String pMDocStringVersion, Throwable pCause) {
        super(pLocale, pCause);
        mArgs=new Object[]{pMDocID,pMDocStringVersion};
        mKey="WorkflowNotFoundException2";
    }
    
    public String getLocalizedMessage() {
        String message = getBundleMessage(mKey);
        return MessageFormat.format(message,mArgs);     
    }
}
