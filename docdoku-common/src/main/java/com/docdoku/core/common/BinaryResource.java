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

package com.docdoku.core.common;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * BinaryResource is the representation
 * of a file contained in either a document, part, template or any other file holder object.
 *
 * The fullName has the following pattern:
 * {workspace_id}/{holder_type}/{holder_id}/{holder_version}/{file_type}/{file_name}
 *
 * For example we could have:
 * project_X/documents/OUR ANSWER/A/1/AEAG_W08.doc
 *
 * {holder_version} and {file_type} are optional
 *
 * @author Florent Garin
 * @version 1.0, 02/06/08
 * @since   V1.0
 */
@Table(name="BINARYRESOURCE")
@Inheritance()
@NamedQueries ({
        @NamedQuery(name="BinaryResource.diskUsageInPath", query = "SELECT sum(br.contentLength) FROM BinaryResource br WHERE br.fullName like :path")
})
@Entity
public class BinaryResource implements Serializable, Comparable<BinaryResource>{

    @Id
    protected String fullName="";

    protected long contentLength;

    @javax.persistence.Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date lastModified;
    
    public BinaryResource() {
    }
    
    public BinaryResource(String pFullName, long pContentLength, Date pLastModified) {
        fullName = pFullName;
        contentLength = pContentLength;
        lastModified = pLastModified;
    }
    
    public String getWorkspaceId(){
        return BinaryResource.parseWorkspaceId(fullName);
    }
    
    public static String parseWorkspaceId(String pFullName){
        int index = pFullName.indexOf('/');
        return pFullName.substring(0, index);
    }

    public String getHolderType(){
        return BinaryResource.parseHolderType(fullName);
    }
    
    public static String parseHolderType(String pFullName){
        String[] parts = pFullName.split("/");
        return parts[1];
    }

    public String getHolderId(){
        return BinaryResource.parseHolderId(fullName);
    }

    public static String parseHolderId(String pFullName){
        String[] parts = pFullName.split("/");
        return parts[2];
    }

    public String getHolderRevision(){
        return BinaryResource.parseHolderRevision(fullName);
    }

    public static String parseHolderRevision(String pFullName){
        String[] parts = pFullName.split("/");
        if(parts.length==6 || parts.length==7)
            return parts[3];
        else
            return null;
    }

    public Integer getHolderIteration(){
        return BinaryResource.parseHolderIteration(fullName);
    }

    public static Integer parseHolderIteration(String pFullName){
        String[] parts = pFullName.split("/");
        if(parts.length==6 || parts.length==7)
            return new Integer(parts[4]);
        else
            return null;
    }

    public String getFileType(){
        return BinaryResource.parseFileType(fullName);
    }

    public static String parseFileType(String pFullName){
        String[] parts = pFullName.split("/");
        if(parts.length==7)
            return parts[5];
        else if(parts.length==5)
            return parts[3];
        else
            return null;
    }

    public String getNewFullName(String newName){
        int index = fullName.lastIndexOf('/');
        return fullName.substring(0,index) + '/' + newName;
    }

    public void setFullName(String pFullName) {
        fullName = pFullName;
    }
    
    public void setContentLength(long pContentLength) {
        contentLength = pContentLength;
    }


    public BinaryResource getPrevious(){
        String holderRevision = getHolderRevision();
        if(holderRevision==null) {
            return null;
        }

        String name = getName();
        String fileType = getFileType();
        int iteration=getHolderIteration();

        String[] parts = fullName.split("/");

        iteration--;
        if(iteration>0){
            String previousFullName=parts[0] + "/" + parts[1] + "/" + parts[2] + "/" + holderRevision  + "/" + iteration + (fileType==null?"":"/" + fileType) + "/" + name;
            return new BinaryResource(previousFullName, contentLength, lastModified);
        }else {
            return null;
        }
    }
    
    public String getFullName() {
        return fullName;
    }
    
    public String getName(){
        int index= fullName.lastIndexOf('/');
        return fullName.substring(index+1);
    }

    public long getContentLength() {
        return contentLength;
    }


    @Override
    public boolean equals(Object pObj) {
        if (this == pObj) {
            return true;
        }
        if (!(pObj instanceof BinaryResource)) {
            return false;
        }
        BinaryResource bin = (BinaryResource) pObj;
        return bin.fullName.equals(fullName);
    }
    
    @Override
    public int hashCode() {
        return fullName.hashCode();
    }

    @Override
    public int compareTo(BinaryResource pBinaryResource) {
        return fullName.compareTo(pBinaryResource.fullName);
    }
    
    @Override
    public String toString() {
        return getName();
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

}
