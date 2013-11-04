/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2013 DocDoku SARL
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

import com.docdoku.core.document.DocumentMasterKey;
import com.docdoku.core.common.Version;
import java.text.MessageFormat;
import java.util.Locale;

/**
 *
 * @author Florent Garin
 */
public class DocumentMasterNotFoundException extends ApplicationException {


    private String mDocMId;
    private String mDocMStringVersion;
     
    
    
    public DocumentMasterNotFoundException(String pMessage) {
        super(pMessage);
    }
    
    
    public DocumentMasterNotFoundException(Locale pLocale, DocumentMasterKey pDocMPK) {
        this(pLocale, pDocMPK, null);
    }

    public DocumentMasterNotFoundException(Locale pLocale, DocumentMasterKey pDocMPK, Throwable pCause) {
        this(pLocale, pDocMPK.getId(), pDocMPK.getVersion(), pCause);
    }

    public DocumentMasterNotFoundException(Locale pLocale, String pDocMID, Version pDocMVersion) {
        this(pLocale, pDocMID, pDocMVersion.toString(), null);
    }

    public DocumentMasterNotFoundException(Locale pLocale, String pDocMId, String pDocMStringVersion) {
        this(pLocale, pDocMId, pDocMStringVersion, null);
    }

    public DocumentMasterNotFoundException(Locale pLocale, String pDocMId, String pDocMStringVersion, Throwable pCause) {
        super(pLocale, pCause);
        mDocMId=pDocMId;
        mDocMStringVersion=pDocMStringVersion;
    }
    
    public String getLocalizedMessage() {
        String message = getBundleDefaultMessage();
        return MessageFormat.format(message,mDocMId,mDocMStringVersion);     
    }
}
