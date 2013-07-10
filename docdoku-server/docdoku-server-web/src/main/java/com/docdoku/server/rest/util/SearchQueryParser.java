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

import com.docdoku.core.document.DocumentSearchQuery;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;

public class SearchQueryParser {

    public static DocumentSearchQuery parseStringQuery(String workspaceId , String pQuery){

        String pDocMId = null;
        String pTitle = null;
        String pVersion = null;
        String pAuthor = null;
        String pType = null;
        Date pCreationDateFrom = null;
        Date pCreationDateTo = null;
        ArrayList<DocumentSearchQuery.AbstractAttributeQuery> pAttributes = new ArrayList<DocumentSearchQuery.AbstractAttributeQuery>();
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
                    case "tags" : pTags = filter[1].split(","); break;
                    case "content" : pContent = filter[1]; break;

                    case "attributes" :

                        String[] attributesString = filter[1].split(";");

                        for(String attributeString : attributesString){

                            String[] attribute = attributeString.split(":");

                            if(attribute.length == 3){

                                switch(attribute[0]){
                                    case "BOOLEAN" :
                                        DocumentSearchQuery.BooleanAttributeQuery baq = new DocumentSearchQuery.BooleanAttributeQuery(attribute[1],Boolean.valueOf(attribute[2]));
                                        pAttributes.add(baq);
                                        break;
                                    case "DATE" :
                                        DocumentSearchQuery.DateAttributeQuery daq = new DocumentSearchQuery.DateAttributeQuery();
                                        daq.setName(attribute[1]);
                                        daq.setFromDate(new Date(Long.valueOf(attribute[2])));
                                        pAttributes.add(daq);
                                        break;
                                    case "TEXT" :
                                        DocumentSearchQuery.TextAttributeQuery taq = new DocumentSearchQuery.TextAttributeQuery(attribute[1],attribute[2]);
                                        pAttributes.add(taq);
                                        break;
                                    case "NUMBER" :
                                        try {
                                            DocumentSearchQuery.NumberAttributeQuery naq = new DocumentSearchQuery.NumberAttributeQuery(attribute[1], NumberFormat.getInstance().parse(attribute[2]).floatValue());
                                            pAttributes.add(naq);
                                        } catch (ParseException e) {
                                            e.printStackTrace();
                                        }
                                        break;
                                    case "URL" :
                                        DocumentSearchQuery.URLAttributeQuery uaq = new DocumentSearchQuery.URLAttributeQuery(attribute[0],attribute[1]);
                                        pAttributes.add(uaq);
                                        break;

                                    default : break;
                                }

                            }

                        }

                        break;

                }
            }

        }

        DocumentSearchQuery.AbstractAttributeQuery[] pAttributesArray = pAttributes.toArray(new DocumentSearchQuery.AbstractAttributeQuery[pAttributes.size()]);

        return  new DocumentSearchQuery(workspaceId, pDocMId, pTitle, pVersion, pAuthor,
                pType, pCreationDateFrom, pCreationDateTo, pAttributesArray, pTags, pContent);

    }

}
