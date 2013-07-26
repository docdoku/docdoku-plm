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

import java.io.Serializable;

/**
 *
 * @author: Martin Devillers
 */
public class Document implements Serializable{

    private String reference;
    private String folder;
    private String author;
    private String creationDate;
    private String type;
    private String title;
    private String checkOutUserName;
    private String checkOutUserLogin;
    private String dateReservation;
    private String lifeCycleState;
    private String description;
    private String[] files;

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

    public void setDocumentDetails(String folder, String author, String creationDate, String type, String title, String lifeCycleState, String description){
        this.folder = folder;
        this.author = author;
        this.creationDate = creationDate;
        this.type = type;
        this.title = title;
        this.lifeCycleState = lifeCycleState;
        this.description = description;
    }

    public void setFiles(String[] files){
        this.files = files;
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

    public String getReservationDate(){
        return dateReservation;
    }

    public String getTitle(){
        return title;
    }

    public boolean iterationNotificationEnabled(){
        return iterationNotification;
    }

    public void setIterationNotification(boolean set){
        iterationNotification = set;
    }

    public boolean stateChangeNotificationEnabled(){
        return stateChangeNotification;
    }

    public void setStateChangeNotification(boolean set){
        stateChangeNotification = set;
    }

    public String getFolder(){
        return folder;
    }

    public String getCreationDate(){
        return creationDate;
    }

    public String getType(){
        return type;
    }

    public String getLifeCycleState(){
        return lifeCycleState;
    }

    public String getDescription(){
        return description;
    }

    public boolean getIterationNotification(){
        return iterationNotification;
    }

    public boolean getStateChangeNotification(){
        return stateChangeNotification;
    }

    public String[] getFiles(){
        return files;
    }
}
