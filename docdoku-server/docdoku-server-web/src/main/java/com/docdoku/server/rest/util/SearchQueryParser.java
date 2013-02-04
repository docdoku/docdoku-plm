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

package com.docdoku.server.rest.util;

import com.docdoku.core.document.SearchQuery;

import java.util.Date;

public class SearchQueryParser {

    public static SearchQuery parseQuery(String workspaceId , String pQuery){

        String pDocMId = null;
        String pTitle = null;
        String pVersion = null;
        String pAuthor = null;
        String pType = null;
        Date pCreationDateFrom = null;
        Date pCreationDateTo = null;
        SearchQuery.AbstractAttributeQuery[] pAttributes = null;
        String[] pTags = null;
        String pContent = null;

        String[] query = pQuery.split("&");

        for(String filters : query){

            String[] filter = filters.split("=");

            if(filter.length == 2){
                switch (filter[0]){
                    case "id" : pDocMId = filter[1]; break;
                    case "title" : pTitle = filter[1]; break;
                    case "version" : pVersion = filter[1]; break;
                    case "author" : pAuthor = filter[1]; break;
                    case "type" : pType = filter[1]; break;
                    case "from" : pCreationDateFrom = new Date(Long.valueOf(filter[1])); break;
                    case "to" : pCreationDateTo = new Date(Long.valueOf(filter[1])); break;
                    // TODO handle attributes parsing
                    // case "attributes" : pTags = filter[1].split(","); break;
                    case "tags" : pTags = filter[1].split(","); break;
                    case "content" : pContent = filter[1]; break;
                }
            }

        }

        return  new SearchQuery(workspaceId, pDocMId, pTitle, pVersion, pAuthor,
                pType, pCreationDateFrom, pCreationDateTo, pAttributes, pTags, pContent);

    }

}
