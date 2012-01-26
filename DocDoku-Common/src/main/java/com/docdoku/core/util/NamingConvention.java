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

package com.docdoku.core.util;

/**
 *
 * @author Florent Garin
 */
public class NamingConvention {
    
    
    private final static char[] FORBIDDEN_CHARS = {'/', '\\', ':', '*', '?',
    '"', '<', '>', '|', '~'};
    
    private final static String[] FORBIDDEN_NAMES = {"",".."};
    
    private NamingConvention() {
    }
    
    private static boolean forbidden(char pChar) {
        for (int i = 0; i < FORBIDDEN_CHARS.length; i++)
            if (pChar == FORBIDDEN_CHARS[i])
                return true;
        return false;
    }
    
    public static boolean correct(String pShortName) {
        if (pShortName == null)
            return false;
        
        for (int i = 0; i < FORBIDDEN_NAMES.length; i++)
            if (pShortName.equals(FORBIDDEN_NAMES[i]))
                return false;
        
        for (int i = 0; i < pShortName.length(); i++)
            if (forbidden(pShortName.charAt(i)))
                return false;
        return true;
    }
}
