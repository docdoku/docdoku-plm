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

package com.docdoku.core.query;

import java.io.Serializable;
import java.util.Date;

/**
 * Wraps data needed to perform a query on part revisions.
 * 
 * @author Morgan Guimard
 * @version 2.0, 03/01/2014
 * @since   V2.0
 */
public class PartSearchQuery extends SearchQuery implements Serializable{
    protected String partNumber;
    protected String name;
    protected Boolean standardPart;

    public PartSearchQuery(){

    }
    public PartSearchQuery(String workspaceId, String fullText, String partNumber, String name, String version,
                           String author, String type, Date creationDateFrom, Date creationDateTo, Date modificationDateFrom,
                           Date modificationDateTo, SearchQuery.AbstractAttributeQuery[] attributes,String[] tags, Boolean standardPart, String content){
        super(workspaceId,fullText,version,author,type,creationDateFrom,creationDateTo,modificationDateFrom,
                modificationDateTo,attributes,tags,content);
        this.partNumber=partNumber;
        this.name=name;
        this.standardPart=standardPart;
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
