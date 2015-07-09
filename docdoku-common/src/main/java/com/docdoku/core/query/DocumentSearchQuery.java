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
 * Wraps data needed to perform a query on documents.
 * 
 * @author Florent Garin
 * @version 2.0, 03/01/2014
 * @since   V2.0
 */
public class DocumentSearchQuery extends SearchQuery implements Serializable{
    protected String docMId;
    protected String title;

    public DocumentSearchQuery(){

    }
    public DocumentSearchQuery(String workspaceId, String fullText, String docMId, String title, String version, String author, String type, Date creationDateFrom, Date creationDateTo, Date modificationDateFrom, Date modificationDateTo, SearchQuery.AbstractAttributeQuery[] attributes, String[] tags, String content){
        super(workspaceId,fullText,version,author,type,creationDateFrom,creationDateTo,modificationDateFrom,modificationDateTo,attributes,tags,content);
        this.docMId=docMId;
        this.title=title;
        this.content=content;
    }

    //Getter
    public String getDocMId() {
        return docMId;
    }
    public String getTitle() {
        return title;
    }

    //Setter
    public void setDocMId(String docMId) {
        this.docMId = docMId;
    }
    public void setTitle(String title) {
        this.title = title;
    }

}
