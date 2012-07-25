/*
 * DocDoku, Professional Open Source
 * Copyright 2006, 2007, 2008, 2009, 2010, 2011, 2012 DocDoku SARL
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

package com.docdoku.core.services;


import com.docdoku.core.common.Version;
import com.docdoku.core.product.PartRevisionKey;
import java.text.MessageFormat;
import java.util.Locale;

/**
 *
 * @author Florent Garin
 */
public class PartRevisionNotFoundException extends ApplicationException {


    private String mPartMNumber;
    private String mPartRStringVersion;
     
    
    
    public PartRevisionNotFoundException(String pMessage) {
        super(pMessage);
    }
    
    
    public PartRevisionNotFoundException(Locale pLocale, PartRevisionKey pPartRPK) {
        this(pLocale, pPartRPK, null);
    }

    public PartRevisionNotFoundException(Locale pLocale, PartRevisionKey pPartRPK, Throwable pCause) {
        this(pLocale, pPartRPK.getPartMaster().getNumber(), pPartRPK.getVersion(), pCause);
    }

    public PartRevisionNotFoundException(Locale pLocale, String pPartMNumber, Version pPartRVersion) {
        this(pLocale, pPartMNumber, pPartRVersion.toString(), null);
    }

    public PartRevisionNotFoundException(Locale pLocale, String pPartMNumber, String pPartRStringVersion) {
        this(pLocale, pPartMNumber, pPartRStringVersion, null);
    }

    public PartRevisionNotFoundException(Locale pLocale, String pPartMNumber, String pPartRStringVersion, Throwable pCause) {
        super(pLocale, pCause);
        mPartMNumber=pPartMNumber;
        mPartRStringVersion=pPartRStringVersion;
    }
    
    public String getLocalizedMessage() {
        String message = getBundleDefaultMessage();
        return MessageFormat.format(message,mPartMNumber,mPartRStringVersion);     
    }
}
