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

package com.docdoku.core.query;

import java.util.Date;

/**
 * Wraps data needed to perform a basic query on part revisions.
 * Contrary to what the query builder through the {@link Query} class
 * is capable of, the structure of a basic query is fix.
 *
 * This class is not persisted and should be considered as value object.
 *
 * @author Morgan Guimard
 * @version 2.0, 03/01/2014
 * @since V2.0
 */
public class PartSearchQuery extends SearchQuery {
    private String partNumber;
    private String name;
    private Boolean standardPart;

    public PartSearchQuery() {

    }

    public PartSearchQuery(String workspaceId, String fullText, String partNumber, String name, String version,
                           String author, String type, Date creationDateFrom, Date creationDateTo, Date modificationDateFrom,
                           Date modificationDateTo, SearchQuery.AbstractAttributeQuery[] attributes, String[] tags, Boolean standardPart, String content, boolean fetchHeadOnly) {
        super(workspaceId, fullText, version, author, type, creationDateFrom, creationDateTo, modificationDateFrom,
                modificationDateTo, attributes, tags, content, fetchHeadOnly);
        this.partNumber = partNumber;
        this.name = name;
        this.standardPart = standardPart;
    }

    //Getter
    public String getPartNumber() {
        return partNumber;
    }

    public String getName() {
        return name;
    }

    public Boolean isStandardPart() {
        return standardPart;
    }

    //Setter
    public void setPartNumber(String partNumber) {
        this.partNumber = partNumber;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setStandardPart(Boolean standardPart) {
        this.standardPart = standardPart;
    }
}
