package com.docdoku.core.query;

import com.docdoku.core.meta.InstanceAttribute;
import com.docdoku.core.meta.InstanceDateAttribute;

import javax.xml.bind.annotation.XmlSeeAlso;
import java.io.Serializable;
import java.util.Date;

/**
 * Wraps data needed to perform a query on document or part revisions.
 *
 * @author Taylor LABEJOF
 * @version 0.1, 03/01/2014
 * @since   V0.1
 */
public class SearchQuery  implements Serializable {

    protected String workspaceId;
    protected String fullText;
    protected String version;
    protected String author;
    protected String type;
    protected Date creationDateFrom;
    protected Date creationDateTo;
    protected AbstractAttributeQuery[] attributes;

    public SearchQuery(){}
    public SearchQuery(String workspaceId, String fullText, String version, String author, String type, Date creationDateFrom, Date creationDateTo, AbstractAttributeQuery[] attributes) {
        this.workspaceId = workspaceId;
        this.fullText =fullText;
        this.version = version;
        this.author = author;
        this.type = type;
        this.creationDateFrom = creationDateFrom;
        this.creationDateTo = creationDateTo;
        this.attributes = attributes;
    }

    // Getter
    public String getWorkspaceId() {return workspaceId;}
    public String getFullText() {return fullText;}
    public String getVersion() {return version;}
    public String getAuthor() {return author;}
    public String getType() {return type;}
    public Date getCreationDateFrom() {return creationDateFrom;}
    public Date getCreationDateTo() {return creationDateTo;}
    public AbstractAttributeQuery[] getAttributes() {return attributes;}
    //Setter
    public void setWorkspaceId(String workspaceId) {this.workspaceId = workspaceId;}
    public void setFullText(String fullText){this.fullText = fullText;}
    public void setVersion(String version) {this.version = version;}
    public void setAuthor(String author) {this.author = author;}
    public void setType(String type) {this.type = type;}
    public void setCreationDateFrom(Date creationDateFrom) {this.creationDateFrom = creationDateFrom;}
    public void setCreationDateTo(Date creationDateTo) {this.creationDateTo = creationDateTo;}
    public void setAttributes(AbstractAttributeQuery[] attributes) {this.attributes = attributes;}

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