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

import com.docdoku.core.meta.InstanceAttribute;
import com.docdoku.core.meta.InstanceDateAttribute;

import javax.xml.bind.annotation.XmlSeeAlso;
import java.io.Serializable;
import java.util.Date;

/**
 * Wraps data needed to perform a query on document or part revisions.
 * @author Taylor LABEJOF
 */
public class SearchQuery  implements Serializable {

    protected String workspaceId;
    protected String fullText;
    protected String version;
    protected String author;
    protected String type;
    protected Date creationDateFrom;
    protected Date creationDateTo;
    protected Date modificationDateFrom;
    protected Date modificationDateTo;
    protected AbstractAttributeQuery[] attributes;
    protected String[] tags;
    protected String content;

    public SearchQuery(){

    }
    public SearchQuery(String workspaceId, String fullText, String version, String author, String type,
                       Date creationDateFrom, Date creationDateTo, Date modificationDateFrom, Date modificationDateTo,
                       AbstractAttributeQuery[] attributes, String[] tags, String content) {
        this.workspaceId = workspaceId;
        this.fullText =fullText;
        this.version = version;
        this.author = author;
        this.type = type;
        this.creationDateFrom = (creationDateFrom!=null) ? (Date) creationDateFrom.clone() : null;
        this.creationDateTo = (creationDateTo!=null) ? (Date) creationDateTo.clone() : null;
        this.modificationDateFrom = (modificationDateFrom!=null) ? (Date) modificationDateFrom.clone() : null;
        this.modificationDateTo = (modificationDateTo!=null) ? (Date) modificationDateTo.clone() : null;
        this.attributes = attributes;
        this.tags = tags;
        this.content = content;
    }

    // Getter
    public String getWorkspaceId() {
        return workspaceId;
    }
    public String getFullText() {
        return fullText;
    }
    public String getVersion() {
        return version;
    }
    public String getAuthor() {
        return author;
    }
    public String getType() {
        return type;
    }
    public String[] getTags() {
        return tags;
    }
    public Date getCreationDateFrom() {
        return (creationDateFrom!=null) ? (Date) creationDateFrom.clone() : null;
    }
    public Date getCreationDateTo() {
        return (creationDateTo!=null) ? (Date) creationDateTo.clone() : null;
    }
    public Date getModificationDateFrom() {
        return (modificationDateFrom!=null) ? (Date) modificationDateFrom.clone() : null;
    }
    public Date getModificationDateTo() {
        return (modificationDateTo!=null) ? (Date) modificationDateTo.clone() : null;
    }
    public AbstractAttributeQuery[] getAttributes() {
        return attributes;
    }
    public String getContent() {
        return content;
    }

    //Setter
    public void setWorkspaceId(String workspaceId) {
        this.workspaceId = workspaceId;
    }
    public void setFullText(String fullText){
        this.fullText = fullText;
    }
    public void setVersion(String version) {
        this.version = version;
    }
    public void setAuthor(String author) {
        this.author = author;
    }
    public void setType(String type) {
        this.type = type;
    }
    public void setTags(String[] tags) {
        this.tags = tags;
    }
    public void setCreationDateFrom(Date creationDateFrom) {
        this.creationDateFrom = (creationDateFrom!=null) ? (Date) creationDateFrom.clone() : null;
    }
    public void setCreationDateTo(Date creationDateTo) {
        this.creationDateTo = (creationDateTo!=null) ? (Date) creationDateTo.clone() : null;
    }
    public void setModificationDateFrom(Date modificationDateFrom) {
        this.modificationDateFrom = (modificationDateFrom!=null) ? (Date) modificationDateFrom.clone() : null;
    }
    public void setModificationDateTo(Date modificationDateTo) {
        this.modificationDateTo = (modificationDateTo!=null) ? (Date) modificationDateTo.clone() : null;
    }

    public void setAttributes(AbstractAttributeQuery[] attributes) {
        this.attributes = attributes;
    }
    public void setContent(String content) {
        this.content = content;
    }

    @XmlSeeAlso({TextAttributeQuery.class, NumberAttributeQuery.class, DateAttributeQuery.class, BooleanAttributeQuery.class, URLAttributeQuery.class})
    public abstract static class AbstractAttributeQuery implements Serializable{
        protected String name;

        public String getName() {
            return name;
        }
        public String getNameWithoutWhiteSpace(){
            return this.name.replaceAll(" ","_");
        }
        public void setName(String name) {
            this.name = name;
        }
        public AbstractAttributeQuery(){

        }
        public AbstractAttributeQuery(String name){
            this.name=name;
        }
        public abstract boolean attributeMatches(InstanceAttribute attr);
        public abstract boolean hasValue();
        @Override
        public abstract String toString();
    }

    public static class TextAttributeQuery extends AbstractAttributeQuery{
        private String textValue;
        public TextAttributeQuery(){

        }
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

        @Override
        public boolean hasValue() {
            return !textValue.isEmpty();
        }

        @Override
        public String toString() {
            return textValue;
        }
    }
    public static class NumberAttributeQuery extends AbstractAttributeQuery{
        private Float numberValue;
        public NumberAttributeQuery(){

        }
        public NumberAttributeQuery(String name, Float value){
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

        @Override
        public boolean hasValue() {
            return numberValue != null;
        }

        @Override
        public String toString() {
            return numberValue.toString();
        }
    }
    public static class BooleanAttributeQuery extends AbstractAttributeQuery{
        private boolean booleanValue;
        public BooleanAttributeQuery(){

        }
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

        @Override
        public boolean hasValue() {
            //by default boolean attribute must have a value
            return true;
        }

        @Override
        public String toString() {
            return ""+booleanValue;
        }
    }
    public static class URLAttributeQuery extends AbstractAttributeQuery{
        private String urlValue;
        public URLAttributeQuery(){

        }
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

        @Override
        public boolean hasValue() {
            return !urlValue.isEmpty();
        }

        @Override
        public String toString() {
            return urlValue;
        }
    }
    public static class DateAttributeQuery extends AbstractAttributeQuery{
        private Date date;
        public DateAttributeQuery(){

        }
        public DateAttributeQuery(String name, Date date){
            super(name);
            this.date = date;
        }
        public Date getDate() {
            return (date !=null) ? (Date) date.clone() : null;
        }
        public void setDate(Date date) {
            this.date = (date !=null) ? (Date) date.clone() : null;
        }

        @Override
        public boolean attributeMatches(InstanceAttribute attr) {
            if (attr instanceof InstanceDateAttribute) {
                InstanceDateAttribute dateAttr = (InstanceDateAttribute) attr;
                Date dateValue = dateAttr.getDateValue();
                if( date !=null) {
                    return  dateValue.equals(date);
                }
            }
            return false;
        }

        @Override
        public boolean hasValue() {
            return date != null;
        }

        @Override
        public String toString() {
            return date.toString();
        }
    }
    public static class LovAttributeQuery extends AbstractAttributeQuery{
        private String lovValue;
        public LovAttributeQuery(){

        }
        public LovAttributeQuery(String name, String value){
            super(name);
            this.lovValue=value;
        }

        public void setLovValue(String lovValue) {
            this.lovValue = lovValue;
        }
        public String getLovValue() {
            return lovValue;
        }
        @Override
        public boolean attributeMatches(InstanceAttribute attr){
            return attr.isValueEquals(lovValue);
        }

        @Override
        public boolean hasValue() {
            return !lovValue.isEmpty();
        }

        @Override
        public String toString() {
            return lovValue;
        }
    }
}