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

import com.docdoku.core.common.Account;
import com.docdoku.core.common.User;
import java.text.MessageFormat;
import java.util.Locale;

/**
 *
 * @author Florent Garin
 */
public class AccessRightException extends ApplicationException {

    private String mName;
    
    
    public AccessRightException(String pMessage) {
        super(pMessage);
    }
    
    public AccessRightException(Locale pLocale, User pUser) {
        this(pLocale, pUser.toString());
    }
    
    public AccessRightException(Locale pLocale, Account pAccount) {
        this(pLocale, pAccount.toString());
    }

    public AccessRightException(Locale pLocale, String pName) {
        super(pLocale);
        mName=pName;
    }

    @Override
    public String getLocalizedMessage() {
        String message = getBundleDefaultMessage();
        return MessageFormat.format(message,mName);     
    }
}
