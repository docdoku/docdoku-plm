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

/**
 *
 * @author Morgan Guimard
 */
public class EntityConstraintException extends ApplicationException{
    private final String mKey;

    public EntityConstraintException(String pMessage) {
        super(pMessage);
        mKey=null;
    }

    public EntityConstraintException(Locale pLocale, String pKey) {
        super(pLocale);
        mKey=pKey;
    }

    @Override
    public String getLocalizedMessage() {
        return mKey==null?null:getBundleMessage(mKey);
    }

}

