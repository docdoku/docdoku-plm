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

package com.docdoku.core.document;

import com.docdoku.core.meta.InstanceAttribute;
import com.docdoku.core.meta.InstanceDateAttribute;

import javax.xml.bind.annotation.XmlSeeAlso;
import java.io.Serializable;
import java.util.Date;

/**
 * Wraps data needed to perform a query on documents.
 * 
 * @author Florent Garin
 * @version 2.0, 27/01/10
 * @since   V2.0
 */
public class DocumentSearchQuery implements Serializable{
    
    private String workspaceId;
    private String docMId;
    private String title;
    private String version;
    private String author;
    private String type;
    private Date creationDateFrom;
    private Date creationDateTo;
    private AbstractAttributeQuery[] attributes;
    private String[] tags;
    private String content;


    public DocumentSearchQuery(){
        
    }

    public DocumentSearchQuery(String workspaceId, String docMId, String title, String version, String author, String type, Date creationDateFrom, Date creationDateTo, DocumentSearchQuery.AbstractAttributeQuery[] attributes, String[] tags, String content){
        this.workspaceId=workspaceId;
        this.docMId=docMId;
        this.title=title;
        this.version=version;
        this.author=author;
        this.type=type;
        this.creationDateFrom=creationDateFrom;
        this.creationDateTo=creationDateTo;
        this.attributes=attributes;
        this.tags=tags;
        this.content=content;
    }
    
    public void setAttributes(AbstractAttributeQuery[] attributes) {
        this.attributes = attributes;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setCreationDateFrom(Date creationDateFrom) {
        this.creationDateFrom = creationDateFrom;
    }

    public void setCreationDateTo(Date creationDateTo) {
        this.creationDateTo = creationDateTo;
    }

    public void setDocMId(String docMId) {
        this.docMId = docMId;
    }

    public void setTags(String[] tags) {
        this.tags = tags;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setWorkspaceId(String workspaceId) {
        this.workspaceId = workspaceId;
    }


    public AbstractAttributeQuery[] getAttributes() {
        return attributes;
    }

    public String getAuthor() {
        return author;
    }

    public String getContent() {
        return content;
    }

    public Date getCreationDateFrom() {
        return creationDateFrom;
    }

    public Date getCreationDateTo() {
        return creationDateTo;
    }

    public String getDocMId() {
        return docMId;
    }

    public String[] getTags() {
        return tags;
    }

    public String getTitle() {
        return title;
    }

    public String getType() {
        return type;
    }

    public String getVersion() {
        return version;
    }

    public String getWorkspaceId() {
        return workspaceId;
    }

    @XmlSeeAlso({TextAttributeQuery.class, NumberAttributeQuery.class, DateAttributeQuery.class, BooleanAttributeQuery.class, URLAttributeQuery.class})
    public static abstract class AbstractAttributeQuery implements Serializable{
        protected String name;
        
        public String getName() {
            return name;
        }
        public String getNameWithoutWhiteSpace(){ return this.name.replaceAll(" ","_"); }
        public void setName(String name) {
            this.name = name;
        }
        public AbstractAttributeQuery(){}
        public AbstractAttributeQuery(String name){
            this.name=name;
        }
        public abstract boolean attributeMatches(InstanceAttribute attr);
    }

    public static class TextAttributeQuery extends AbstractAttributeQuery{
        private String textValue;
        public TextAttributeQuery(){}
        public TextAttributeQuery(String name, String value){
            super(name);
            this.textValue=value;
        }
        public String getTextValue() {
            return textValue;
        }
        public void setTextValue(String textValue) {
            this.textValue = textValue;
        }
        @Override
        public boolean attributeMatches(InstanceAttribute attr){
            return attr.isValueEquals(textValue);
        }
    }
    public static class NumberAttributeQuery extends AbstractAttributeQuery{
        private float numberValue;
        public NumberAttributeQuery(){}
        public NumberAttributeQuery(String name, float value){
            super(name);
            this.numberValue=value;
        }
        public float getNumberValue() {
            return numberValue;
        }
        public void setNumberValue(float numberValue) {
            this.numberValue = numberValue;
        }
        @Override
        public boolean attributeMatches(InstanceAttribute attr){
            return attr.isValueEquals(numberValue);
        }
    }
    public static class BooleanAttributeQuery extends AbstractAttributeQuery{
        private boolean booleanValue;
        public BooleanAttributeQuery(){}
        public BooleanAttributeQuery(String name, boolean value){
            super(name);
            this.booleanValue=value;
        }
        public boolean isBooleanValue() {
            return booleanValue;
        }
        public void setBooleanValue(boolean booleanValue) {
            this.booleanValue = booleanValue;
        }
        @Override
        public boolean attributeMatches(InstanceAttribute attr){
            return attr.isValueEquals(booleanValue);
        }
    }
    public static class URLAttributeQuery extends AbstractAttributeQuery{
        private String urlValue;
        public URLAttributeQuery(){}
        public URLAttributeQuery(String name, String value){
            super(name);
            this.urlValue=value;
        }

        public void setUrlValue(String urlValue) {
            this.urlValue = urlValue;
        }
        public String getUrlValue() {
            return urlValue;
        }
        @Override
        public boolean attributeMatches(InstanceAttribute attr){
            return attr.isValueEquals(urlValue);
        }
    }
    public static class DateAttributeQuery extends AbstractAttributeQuery{
        private Date fromDate;
        private Date toDate;
        public DateAttributeQuery(){}
        public DateAttributeQuery(String name, Date fromDate, Date toDate){
            super(name);
            this.fromDate=fromDate;
            this.toDate=toDate;
        }
        public Date getFromDate() {
            return fromDate;
        }
        public void setFromDate(Date fromDate) {
            this.fromDate = fromDate;
        }
        public Date getToDate() {
            return toDate;
        }
        public void setToDate(Date toDate) {
            this.toDate = toDate;
        }
        @Override
        public boolean attributeMatches(InstanceAttribute attr) {
            if (attr instanceof InstanceDateAttribute) {
                InstanceDateAttribute dateAttr = (InstanceDateAttribute) attr;
                Date dateValue = dateAttr.getDateValue();
                if(toDate !=null && fromDate !=null)
                    return !(dateValue.after(toDate) || dateValue.before(fromDate));
            }
            return false;
        }
    }
}
