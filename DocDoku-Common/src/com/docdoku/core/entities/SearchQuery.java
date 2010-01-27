/*
 * DocDoku, Professional Open Source
 * Copyright 2006, 2007, 2008, 2009, 2010 DocDoku SARL
 *
 * This file is part of DocDoku.
 *
 * DocDoku is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DocDoku is distributed in the hope that it will be useful,  
 * but WITHOUT ANY WARRANTY; without even the implied warranty of  
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the  
 * GNU General Public License for more details.  
 *  
 * You should have received a copy of the GNU General Public License  
 * along with DocDoku.  If not, see <http://www.gnu.org/licenses/>.  
 */

package com.docdoku.core.entities;

import java.util.Date;

/**
 * Wraps data needed to perform a query on documents.
 * 
 * @author Florent GARIN
 * @version 2.0, 27/01/10
 * @since   V2.0
 */
public class SearchQuery {
    
    private String m_workspaceId;
    private String m_mdocId;
    private String m_title;
    private String m_version;
    private String m_author;
    private String m_type;
    private Date m_creationDateFrom;
    private Date m_creationDateTo;
    private AbstractAttributeQuery[] m_attributes;
    private String[] m_tags;
    private String m_content;


    public SearchQuery(){
        
    }

    public void setAttributes(AbstractAttributeQuery[] attributes) {
        this.m_attributes = attributes;
    }

    public void setAuthor(String author) {
        this.m_author = author;
    }

    public void setContent(String content) {
        this.m_content = content;
    }

    public void setCreationDateFrom(Date creationDateFrom) {
        this.m_creationDateFrom = creationDateFrom;
    }

    public void setCreationDateTo(Date creationDateTo) {
        this.m_creationDateTo = creationDateTo;
    }

    public void setMDocId(String mdocId) {
        this.m_mdocId = mdocId;
    }

    public void setTags(String[] tags) {
        this.m_tags = tags;
    }

    public void setTitle(String title) {
        this.m_title = title;
    }

    public void setType(String type) {
        this.m_type = type;
    }

    public void setVersion(String version) {
        this.m_version = version;
    }

    public void setWorkspaceId(String workspaceId) {
        this.m_workspaceId = workspaceId;
    }


    public AbstractAttributeQuery[] getAttributes() {
        return m_attributes;
    }

    public String getAuthor() {
        return m_author;
    }

    public String getContent() {
        return m_content;
    }

    public Date getCreationDateFrom() {
        return m_creationDateFrom;
    }

    public Date getCreationDateTo() {
        return m_creationDateTo;
    }

    public String getMDocId() {
        return m_mdocId;
    }

    public String[] getTags() {
        return m_tags;
    }

    public String getTitle() {
        return m_title;
    }

    public String getType() {
        return m_type;
    }

    public String getVersion() {
        return m_version;
    }

    public String getWorkspaceId() {
        return m_workspaceId;
    }


    public static abstract class AbstractAttributeQuery{
        protected String m_name;
        
        public String getName() {
            return m_name;
        }
        public AbstractAttributeQuery(String name){
            m_name=name;
        }
        public abstract boolean attributeMatches(InstanceAttribute attr);
    }

    public static class TextAttributeQuery extends AbstractAttributeQuery{
        private String m_value;
        public TextAttributeQuery(String name, String value){
            super(name);
            m_value=value;
        }
        @Override
        public boolean attributeMatches(InstanceAttribute attr){
            return attr.isValueEquals(m_value);
        }
    }
    public static class NumberAttributeQuery extends AbstractAttributeQuery{
        private float m_value;
        public NumberAttributeQuery(String name, float value){
            super(name);
            m_value=value;
        }
        @Override
        public boolean attributeMatches(InstanceAttribute attr){
            return attr.isValueEquals(m_value);
        }
    }
    public static class BooleanAttributeQuery extends AbstractAttributeQuery{
        private boolean m_value;
        public BooleanAttributeQuery(String name, boolean value){
            super(name);
            m_value=value;
        }
        @Override
        public boolean attributeMatches(InstanceAttribute attr){
            return attr.isValueEquals(m_value);
        }
    }
    public static class URLAttributeQuery extends AbstractAttributeQuery{
        private String m_value;
        public URLAttributeQuery(String name, String value){
            super(name);
            m_value=value;
        }
        @Override
        public boolean attributeMatches(InstanceAttribute attr){
            return attr.isValueEquals(m_value);
        }
    }
    public static class DateAttributeQuery extends AbstractAttributeQuery{
        private Date m_fromDate;
        private Date m_toDate;
        public DateAttributeQuery(String name, Date fromDate, Date toDate){
            super(name);
            m_fromDate=fromDate;
            m_toDate=toDate;
        }

        @Override
        public boolean attributeMatches(InstanceAttribute attr) {
            if (attr instanceof InstanceDateAttribute) {
                InstanceDateAttribute dateAttr = (InstanceDateAttribute) attr;
                Date dateValue = dateAttr.getDateValue();
                return !(dateValue.after(m_toDate) || dateValue.before(m_fromDate));
            }else
                return false;
        }
    }
}
