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

package com.docdoku.server.rest.util;

import com.docdoku.core.query.DocumentSearchQuery;
import com.docdoku.core.query.PartSearchQuery;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SearchQueryParser {
    private SearchQueryParser(){
        super();
    }
    private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    public static DocumentSearchQuery parseDocumentStringQuery(String workspaceId , String pQuery){
        String fullText = null;
        String pDocMId = null;
        String pTitle = null;
        String pVersion = null;
        String pAuthor = null;
        String pType = null;
        Date pCreationDateFrom = null;
        Date pCreationDateTo = null;
        Date pModificationDateFrom = null;
        Date pModificationDateTo = null;
        List<DocumentSearchQuery.AbstractAttributeQuery> pAttributes = new ArrayList<>();
        String[] pTags = null;
        String pContent = null;

        String[] query = pQuery.split("&");

        for(String filters : query){
            String[] filter = filters.split("=");
            if(filter.length == 2){
                switch (filter[0]){
                    case "q" :
                        fullText = filter[1];
                        break;
                    case "id" :
                        pDocMId = filter[1];
                        break;
                    case "title" :
                        pTitle = filter[1];
                        break;
                    case "version" :
                        pVersion = filter[1];
                        break;
                    case "author" :
                        pAuthor = filter[1];
                        break;
                    case "type" :
                        pType = filter[1];
                        break;
                    case "createdFrom" :
                        try {
                            pCreationDateFrom =  simpleDateFormat.parse(filter[1]);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        break;
                    case "createdTo" :
                        try {
                            pCreationDateTo =  simpleDateFormat.parse(filter[1]);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        break;
                    case "modifiedFrom" :
                        try {
                            pModificationDateFrom =  simpleDateFormat.parse(filter[1]);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        break;
                    case "modifiedTo" :
                        try {
                            pModificationDateTo =  simpleDateFormat.parse(filter[1]);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        break;
                    case "tags" :
                        pTags = filter[1].split(",");
                        break;
                    case "content" :
                        pContent = filter[1];
                        break;
                    case "attributes" :
                        pAttributes = parseDocumentAttributeStringQuery(filter[1]);
                        break;
                    default:
                        break;

                }
            }

        }

        DocumentSearchQuery.AbstractAttributeQuery[] pAttributesArray = pAttributes.toArray(new DocumentSearchQuery.AbstractAttributeQuery[pAttributes.size()]);

        return  new DocumentSearchQuery(workspaceId, fullText, pDocMId, pTitle, pVersion, pAuthor, pType,
                pCreationDateFrom, pCreationDateTo, pModificationDateFrom, pModificationDateTo,
                pAttributesArray, pTags, pContent);

    }
    public static PartSearchQuery parsePartStringQuery(String workspaceId , String pQuery){
        String fullText = null;
        String pNumber = null;
        String pName = null;
        String pVersion = null;
        String pAuthor = null;
        String pType = null;
        Date pCreationDateFrom = null;
        Date pCreationDateTo = null;
        Date pModificationDateFrom = null;
        Date pModificationDateTo = null;
        List<PartSearchQuery.AbstractAttributeQuery> pAttributes = new ArrayList<>();
        Boolean standardPart = null;

        String[] query = pQuery.split("&");

        for(String filters : query){
            String[] filter = filters.split("=");
            if(filter.length == 2){
                switch (filter[0]){
                    case "q" :
                        fullText = filter[1];
                        break;
                    case "number" :
                        pNumber = filter[1];
                        break;
                    case "name" :
                        pName = filter[1];
                        break;
                    case "version" :
                        pVersion = filter[1];
                        break;
                    case "author" :
                        pAuthor = filter[1];
                        break;
                    case "type" :
                        pType = filter[1];
                        break;
                    case "createdFrom" :
                        try {
                            pCreationDateFrom =  simpleDateFormat.parse(filter[1]);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        break;
                    case "createdTo" :
                        try {
                            pCreationDateTo =  simpleDateFormat.parse(filter[1]);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        break;
                    case "modifiedFrom" :
                        try {
                            pModificationDateFrom =  simpleDateFormat.parse(filter[1]);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        break;
                    case "modifiedTo" :
                        try {
                            pModificationDateTo =  simpleDateFormat.parse(filter[1]);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        break;
                    case "standardPart" :
                        standardPart = Boolean.valueOf(filter[1]);
                        break;
                    case "attributes" :
                        pAttributes = parsePartAttributeStringQuery(filter[1]);
                        break;
                }
            }
        }

        PartSearchQuery.AbstractAttributeQuery[] pAttributesArray = pAttributes.toArray(new PartSearchQuery.AbstractAttributeQuery[pAttributes.size()]);

        return  new PartSearchQuery(workspaceId, fullText, pNumber, pName, pVersion, pAuthor, pType,
                pCreationDateFrom, pCreationDateTo, pModificationDateFrom, pModificationDateTo,
                pAttributesArray,standardPart);

    }

    private static List<DocumentSearchQuery.AbstractAttributeQuery> parseDocumentAttributeStringQuery(String attributeQuery){
        List<DocumentSearchQuery.AbstractAttributeQuery> pAttributes = new ArrayList<>();
        String[] attributesString = attributeQuery.split(";");
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
                            Logger.getLogger(SearchQueryParser.class.getName()).log(Level.INFO, null, e);
                        }
                        break;
                    default:
                        break;

                }
            }
        }
        return  pAttributes;
    }

    private static List<PartSearchQuery.AbstractAttributeQuery> parsePartAttributeStringQuery(String attributeQuery){
        List<PartSearchQuery.AbstractAttributeQuery> pAttributes = new ArrayList<>();
        String[] attributesString = attributeQuery.split(";");
        for(String attributeString : attributesString){
            String[] attribute = attributeString.split(":");
            if(attribute.length == 3){
                switch(attribute[0]){
                    case "BOOLEAN" :
                        PartSearchQuery.BooleanAttributeQuery baq = new PartSearchQuery.BooleanAttributeQuery(attribute[1],Boolean.valueOf(attribute[2]));
                        pAttributes.add(baq);
                        break;
                    case "DATE" :
                        PartSearchQuery.DateAttributeQuery daq = new PartSearchQuery.DateAttributeQuery();
                        daq.setName(attribute[1]);
                        daq.setFromDate(new Date(Long.valueOf(attribute[2])));
                        pAttributes.add(daq);
                        break;
                    case "TEXT" :
                        PartSearchQuery.TextAttributeQuery taq = new PartSearchQuery.TextAttributeQuery(attribute[1],attribute[2]);
                        pAttributes.add(taq);
                        break;
                    case "NUMBER" :
                        try {
                            PartSearchQuery.NumberAttributeQuery naq = new PartSearchQuery.NumberAttributeQuery(attribute[1], NumberFormat.getInstance().parse(attribute[2]).floatValue());
                            pAttributes.add(naq);
                        } catch (ParseException e) {
                            Logger.getLogger(SearchQueryParser.class.getName()).log(Level.INFO, null, e);
                        }
                        break;
                    case "URL" :
                        PartSearchQuery.URLAttributeQuery uaq = new PartSearchQuery.URLAttributeQuery(attribute[0],attribute[1]);
                        pAttributes.add(uaq);
                        break;
                    default :
                        break;
                }
            }
        }
        return  pAttributes;
    }
}