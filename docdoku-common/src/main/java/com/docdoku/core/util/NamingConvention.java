/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2017 DocDoku SARL
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

package com.docdoku.core.util;

/**
 * Document IDs, part numbers and other object identifiers
 * and must conform to naming conventions.
 *
 * This class checks that these conventions are respected.
 *
 * @author Florent Garin
 */
public class NamingConvention {

    private static final char[] DOCUMENTS_FORBIDDEN_CHARS = {
            '%'
    };

    private static final char[] PARTS_FORBIDDEN_CHARS = {
            '%'
    };

    private static final char[] FORBIDDEN_CHARS = {
            '$','&','+',',','/',':',';','=','?','@','"', '<', '>', '#','%','{','}','|','\\','^','~','[',']',' ', '*','`'
    };

    private static final char[] FORBIDDEN_CHARS_MASK = {
            '$','&','+',',','/',':',';','=','?','@','"', '<', '>','%','{','}','|','\\','^','~','[',']',' ','`'
    };

    private static final char[] FORBIDDEN_CHARS_FILE = {
            '/', '\\', ':', '*', '?','"', '<', '>', '|', '~', '#',
            '^', '%', '{', '}','&','$','+',',', ';', '@', '\'', '`','=', '[', ']'
    };
    
    private static final String[] FORBIDDEN_NAMES = {"",".."};

    private NamingConvention() {
    }
    
    private static boolean forbidden(char pChar, char[] forbiddenChars) {
        for (char forbiddenChar : forbiddenChars) {
            if (pChar == forbiddenChar) {
                return true;
            }
        }
        return false;
    }

    private static boolean correct(String pShortName, char[] forbiddenChars) {
        if (pShortName == null) {
            return false;
        }

        for (String forbiddenName : FORBIDDEN_NAMES) {
            if (pShortName.equals(forbiddenName)) {
                return false;
            }
        }

        for (int i = 0; i < pShortName.length(); i++) {
            if (forbidden(pShortName.charAt(i), forbiddenChars)) {
                return false;
            }
        }
        return true;
    }
    
    public static boolean correct(String pShortName) {
        return correct(pShortName, FORBIDDEN_CHARS);
    }


    public static boolean correctNameFile(String pShortName) {
        return correct(pShortName, FORBIDDEN_CHARS_FILE);
    }
    public static boolean correctNameMask(String mask) {
        return correct(mask, FORBIDDEN_CHARS_MASK);
    }

    public static boolean correctDocumentId(String documentId) {
        return correct(documentId, DOCUMENTS_FORBIDDEN_CHARS);
    }

    public static boolean correctPartNumber(String documentId) {
        return correct(documentId, PARTS_FORBIDDEN_CHARS);
    }
}