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

package com.docdoku.server.util;

/*
 *
 * @author Asmae CHADID on 09/03/15.
 */

public class DocumentUtil {

    public static final String WORKSPACE_ID="TestWorkspace";
    public static final String DOCUMENT_ID ="TestDocument";
    public static final String DOCUMENT_TEMPLATE_ID="temp_1";
    public static final String FILE1_NAME ="uplodedFile";
    public static final String FILE2_NAME="file_à-tèsté.txt";
    public static final String FILE3_NAME="file_à-t*st?! .txt";
    public static final String FILE4_NAME ="uploadedFile";
    public static final long DOCUMENT_SIZE = 22;
    public static final String VERSION ="A" ;
    public static final int ITERATION = 1;
    public static final String FULL_NAME = WORKSPACE_ID+"/documents/"+DOCUMENT_ID+"/"+VERSION+"/"+ITERATION+"/"+ FILE1_NAME;
    public static final String FULL_NAME4 = WORKSPACE_ID+"/documents/"+DOCUMENT_ID+"/"+VERSION+"/"+ITERATION+"/"+ FILE4_NAME;
    public static final String FOLDER = "newFolder";
    public static final String USER_2_LOGIN = "user2";
    public static final String USER_2_NAME = "user2";
    public static final String USER2_MAIL = "user2@docdoku.com";
    public static final String LANGUAGE = "en";
    public static final String USER_1_LOGIN = "user1";
    public static final String USER_1_NAME = "user1";
    public static final String USER1_MAIL = "user1@docdoku.com";

    public static final String WORKSPACE_DESCRIPTION = "pDescription";

}
