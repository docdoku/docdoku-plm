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
import com.docdoku.core.query.SearchQuery;

import javax.ws.rs.core.MultivaluedMap;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SearchQueryParser {

    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    private static final Logger LOGGER = Logger.getLogger(SearchQueryParser.class.getName());
    private static final String ATTRIBUTES_DELIMITER = ";";
    private static final String ATTRIBUTES_SPLITTER = ":";
    private static final String FILTERS_DELIMITER = "=";
    private static final String QUERY_DELIMITER = "&";

    public static DocumentSearchQuery parseDocumentStringQuery(String workspaceId , MultivaluedMap<String,String> query){
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


        for(String filter : query.keySet()){
            List<String> values = query.get(filter);
            if(values.size() == 1){
                String value = null;
                try {
                    value = URLDecoder.decode(values.get(0), "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    LOGGER.log(Level.FINEST,null,e);
                }
                switch (filter){
                    case "q" :
                        fullText = value;
                        break;
                    case "id" :
                        pDocMId = value;
                        break;
                    case "title" :
                        pTitle = value;
                        break;
                    case "version" :
                        pVersion = value;
                        break;
                    case "author" :
                        pAuthor = value;
                        break;
                    case "type" :
                        pType = value;
                        break;
                    case "createdFrom" :
                        try {
                            pCreationDateFrom =  SIMPLE_DATE_FORMAT.parse(value);
                        } catch (ParseException e) {
                            LOGGER.log(Level.FINEST, null, e);
                        }
                        break;
                    case "createdTo" :
                        try {
                            pCreationDateTo =  SIMPLE_DATE_FORMAT.parse(value);
                        } catch (ParseException e) {
                            LOGGER.log(Level.FINEST, null, e);
                        }
                        break;
                    case "modifiedFrom" :
                        try {
                            pModificationDateFrom =  SIMPLE_DATE_FORMAT.parse(value);
                        } catch (ParseException e) {
                            LOGGER.log(Level.FINEST, null, e);
                        }
                        break;
                    case "modifiedTo" :
                        try {
                            pModificationDateTo =  SIMPLE_DATE_FORMAT.parse(value);
                        } catch (ParseException e) {
                            LOGGER.log(Level.FINEST, null, e);
                        }
                        break;
                    case "tags" :
                        pTags = value.split(",");
                        break;
                    case "content" :
                        pContent = value;
                        break;
                    case "attributes" :
                        pAttributes = parseAttributeStringQuery(value);
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

    public static PartSearchQuery parsePartStringQuery(String workspaceId , MultivaluedMap<String,String> query){
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
        String[] pTags = null;
        Boolean standardPart = null;
        String content = null;

        for(String filter : query.keySet()){
            List<String> values = query.get(filter);
            if(values.size() == 1) {
                String value = null;
                try {
                    value = URLDecoder.decode(values.get(0), "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    LOGGER.log(Level.FINEST,null,e);
                }
                switch (filter) {
                    case "q" :
                        fullText = value;
                        break;
                    case "number":
                        pNumber = value;
                        break;
                    case "name" :
                        pName = value;
                        break;
                    case "version" :
                        pVersion = value;
                        break;
                    case "author" :
                        pAuthor = value;
                        break;
                    case "type" :
                        pType = value;
                        break;
                    case "createdFrom" :
                        try {
                            pCreationDateFrom =  SIMPLE_DATE_FORMAT.parse(value);
                        } catch (ParseException e) {
                            LOGGER.log(Level.FINEST, null, e);
                        }
                        break;
                    case "createdTo" :
                        try {
                            pCreationDateTo =  SIMPLE_DATE_FORMAT.parse(value);
                        } catch (ParseException e) {
                            LOGGER.log(Level.FINEST, null, e);
                        }
                        break;
                    case "modifiedFrom" :
                        try {
                            pModificationDateFrom =  SIMPLE_DATE_FORMAT.parse(value);
                        } catch (ParseException e) {
                            LOGGER.log(Level.FINEST, null, e);
                        }
                        break;
                    case "modifiedTo" :
                        try {
                            pModificationDateTo =  SIMPLE_DATE_FORMAT.parse(value);
                        } catch (ParseException e) {
                            LOGGER.log(Level.FINEST, null, e);
                        }
                        break;
                    case "tags" :
                        pTags = value.split(",");
                        break;
                    case "standardPart" :
                        standardPart = Boolean.valueOf(value);
                        break;
                    case "content":
                        content = value;
                        break;
                    case "attributes":
                        pAttributes = parseAttributeStringQuery(value);
                        break;
                }
            }
        }

        PartSearchQuery.AbstractAttributeQuery[] pAttributesArray = pAttributes.toArray(new PartSearchQuery.AbstractAttributeQuery[pAttributes.size()]);

        return  new PartSearchQuery(workspaceId, fullText, pNumber, pName, pVersion, pAuthor, pType,
                pCreationDateFrom, pCreationDateTo, pModificationDateFrom, pModificationDateTo,
                pAttributesArray,pTags,standardPart,content);

    }

    private static List<SearchQuery.AbstractAttributeQuery> parseAttributeStringQuery(String attributeQuery){
        List<SearchQuery.AbstractAttributeQuery> pAttributes = new ArrayList<>();
        String[] attributesString = attributeQuery.split(ATTRIBUTES_DELIMITER);

        for(String attributeString : attributesString){

            int firstColon = attributeString.indexOf(ATTRIBUTES_SPLITTER);
            String attributeType = attributeString.substring(0,firstColon);
            attributeString = attributeString.substring(firstColon+1);

            int secondColon = attributeString.indexOf(ATTRIBUTES_SPLITTER);
            String attributeName = attributeString.substring(0,secondColon);
            String attributeValue = attributeString.substring(secondColon+1);

            switch(attributeType){
                case "BOOLEAN" :
                    SearchQuery.BooleanAttributeQuery baq = new SearchQuery.BooleanAttributeQuery(attributeName,Boolean.valueOf(attributeValue));
                    pAttributes.add(baq);
                    break;
                case "DATE" :
                    SearchQuery.DateAttributeQuery daq = new SearchQuery.DateAttributeQuery();
                    daq.setName(attributeName);
                    try {
                        //Date attributes are always UTC, should not use the default timezone
                        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                        df.setTimeZone(TimeZone.getTimeZone("UTC"));
                        daq.setDate(df.parse(attributeValue));
                        pAttributes.add(daq);
                    } catch (ParseException e) {
                        LOGGER.log(Level.FINEST,null,e);
                    }
                    break;
                case "TEXT" :
                    SearchQuery.TextAttributeQuery taq = new SearchQuery.TextAttributeQuery(attributeName,attributeValue);
                    pAttributes.add(taq);
                    break;
                case "NUMBER" :
                    try {
                        SearchQuery.NumberAttributeQuery naq = new SearchQuery.NumberAttributeQuery(attributeName, NumberFormat.getInstance().parse(attributeValue).floatValue());
                        pAttributes.add(naq);
                    } catch (ParseException e) {
                        LOGGER.log(Level.INFO, null, e);
                    }
                    break;
                case "URL" :
                    SearchQuery.URLAttributeQuery uaq = new SearchQuery.URLAttributeQuery(attributeName,attributeValue);
                    pAttributes.add(uaq);
                    break;

                case "LOV":
                    SearchQuery.LovAttributeQuery laq = new SearchQuery.LovAttributeQuery(attributeName,attributeValue);
                    pAttributes.add(laq);
                    break;

                default :
                    break;
            }

        }
        return  pAttributes;
    }

    private SearchQueryParser(){
        super();
    }

}