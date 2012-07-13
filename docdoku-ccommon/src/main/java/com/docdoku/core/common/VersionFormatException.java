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

package com.docdoku.core.common;

/**
 *
 * @author Florent Garin
 */
public class VersionFormatException extends IllegalArgumentException {

    private int mErrorOffset;
    private String mInputString;


    public VersionFormatException(String pInputString, int pErrorOffset) {
        super("Error trying to parse the string \"" + pInputString + "\" at the character position " + pErrorOffset);
        mErrorOffset = pErrorOffset;
        mInputString = pInputString;
    }

    public int getErrorOffset() {
        return mErrorOffset;
    }

    public String getInputString() {
        return mInputString;
    }

}
