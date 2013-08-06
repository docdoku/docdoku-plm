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

import android.content.res.Resources;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author: Martin Devillers
 */
public class Document extends Element implements Serializable{

    private static final String JSON_KEY_PART_ITERATIONS = "documentIterations";

    private String identification, reference;
    private String path, author, creationDate, type, title, lifeCycleState, description;
    private int revisionNumber;
    private String revisionNote, revisionDate, revisionAuthor;

    private boolean iterationNotification, stateChangeNotification;

    public Document(String identification){
        this.identification = identification;
        reference = identification.substring(0, identification.lastIndexOf("-"));
        iterationNotification = false;
        stateChangeNotification = false;
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
        if (JSONObject.NULL.toString().equals(lifeCycleState)){
            values[8] = "";
        } else {
            values[8] = lifeCycleState;
        }
        values[9] = description;
        return values;
    }

    public String[] getLastRevision(){
        String[] result = new String[4];
        result[0] = identification + "-" + revisionNumber;
        result[1] = revisionNote;
        result[2] = revisionDate;
        result[3] = revisionAuthor;
        return result;
    }

    public String getIdentification(){
        return identification;
    }

    public String getAuthor(){
        return author;
    }

    public void setIterationNotification(boolean set){
        iterationNotification = set;
    }

    public void setStateChangeNotification(boolean set){
        stateChangeNotification = set;
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

    public int getRevisionNumber() {
        return revisionNumber;
    }

    public Document updateFromJSON(JSONObject documentJSON, Resources resources) throws JSONException {
        updateElementFromJSON(documentJSON, resources);
        SimpleDateFormat dateFormat = new SimpleDateFormat(resources.getString(R.string.simpleDateFormat));
        setDocumentDetails(
                documentJSON.getString("path"),
                documentJSON.getJSONObject("author").getString("name"),
                dateFormat.format(new Date(Long.valueOf(documentJSON.getString("creationDate")))),
                documentJSON.getString("type"),
                documentJSON.getString("title"),
                documentJSON.getString("lifeCycleState"),
                documentJSON.getString("description")
        );
        return this;
    }

    private void setDocumentDetails(String path, String author, String creationDate, String type, String title, String lifeCycleState, String description){
        this.path = path;
        this.author = author;
        this.creationDate = creationDate;
        this.type = type;
        this.title = title;
        this.lifeCycleState = lifeCycleState;
        this.description = description;
    }

    private void setLastRevision(int revisionNumber, String revisionNote, String revisionAuthor, String revisionDate){
        this.revisionNumber = revisionNumber;
        this.revisionAuthor = revisionAuthor;
        this.revisionDate = revisionDate;
        if (JSONObject.NULL.toString().equals(revisionNote)){
            this.revisionNote = "";
        }
        else{
            this.revisionNote = revisionNote;
        }
    }

    /**
     * The following methods provide the keys to read the part attributes in the JSONObject received from the server
     */

    @Override
    protected String getIterationsJSONKey() {
        return JSON_KEY_PART_ITERATIONS;
    }
}
