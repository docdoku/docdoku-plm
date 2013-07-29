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

package com.docdoku.android.plm.client;

import org.json.JSONObject;

import java.io.File;
import java.io.Serializable;

/**
 *
 * @author: Martin Devillers
 */
public class Document implements Serializable{

    private String reference;
    private String path;
    private String author;
    private String creationDate;
    private String type;
    private String title;
    private String checkOutUserName;
    private String checkOutUserLogin;
    private String checkOutDate;
    private String lifeCycleState;
    private String description;
    private String[] files;
    private String[] linkedDocuments;
    private int revisionNumber;
    private String revisionNote;
    private String revisionDate;
    private String revisionAuthor;
    private String[] attributeNames;
    private String[] attributeValues;

    private boolean iterationNotification;
    private boolean stateChangeNotification;

    public Document(String reference){
        this.reference = reference;
        iterationNotification = false;
        stateChangeNotification = false;
    }

    public void setCheckOutUserName(String checkOutUserName){
        this.checkOutUserName = checkOutUserName;
    }

    public void setCheckOutUserLogin(String checkOutUserLogin){
        this.checkOutUserLogin = checkOutUserLogin;
    }

    public void setDocumentDetails(String path, String author, String creationDate, String type, String title, String lifeCycleState, String description){
        this.path = path;
        this.author = author;
        this.creationDate = creationDate;
        this.type = type;
        this.title = title;
        this.lifeCycleState = lifeCycleState;
        this.description = description;
    }

    public String[] getDocumentDetails(){
        String[] values = new String[10];
        values[0] = path.substring(path.lastIndexOf("/")+1,path.length());
        values[1] = reference;
        values[2] = author;
        values[3] = creationDate;
        values[4] = type;
        values[5] = title;
        values[6] = checkOutUserName;
        values[7] = checkOutDate;
        if (lifeCycleState.equals(JSONObject.NULL.toString())){
            values[8] = "";
        }
        else{
            values[8] = lifeCycleState;
        }
        values[9] = description;
        return values;
    }

    public void setFiles(String[] files){
        this.files = files;
    }

    public void setLastRevision(int revisionNumber, String revisionNote, String revisionAuthor, String revisionDate){
        this.revisionNumber = revisionNumber;
        this.revisionAuthor = revisionAuthor;
        this.revisionDate = revisionDate;
        this.revisionNote = revisionNote;
    }

    public String[] getLastRevision(){
        String[] result = new String[4];
        result[0] = reference + revisionNumber;
        result[1] = revisionNote;
        result[2] = revisionDate;
        result[3] = revisionAuthor;
        return result;
    }

    public void setAttributes(String[] attributeNames, String[] attributeValues){
        this.attributeNames = attributeNames;
        this.attributeValues = attributeValues;
    }

    public String[] getAttributeNames(){
        if (attributeNames == null){
            return new String[0];
        }
        return attributeNames;
    }

    public String[] getAttributeValues(){
        if (attributeValues == null){
            return new String[0];
        }
        return attributeValues;
    }

    public String getReference(){
        return reference;
    }

    public String getAuthor(){
        return author;
    }

    public String getCheckOutUserName(){
        return checkOutUserName;
    }

    public String getCheckOutUserLogin(){
        return checkOutUserLogin;
    }

    public void setIterationNotification(boolean set){
        iterationNotification = set;
    }

    public void setStateChangeNotification(boolean set){
        stateChangeNotification = set;
    }

    public void setLinkedDocuments(String[] linkedDocuments){
        this.linkedDocuments = linkedDocuments;
    }

    public String[] getLinkedDocuments(){
        if (linkedDocuments == null){
            return new String[0];
        }
        return linkedDocuments;
    }

    public boolean getIterationNotification(){
        return iterationNotification;
    }

    public boolean getStateChangeNotification(){
        return stateChangeNotification;
    }

    public String[] getFiles(){
        if (files == null){
            return new String[0];
        }
        return files;
    }
}
