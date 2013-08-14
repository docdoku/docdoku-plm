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

package com.docdoku.android.plm.client.documents;

import android.content.res.Resources;
import android.util.Log;
import com.docdoku.android.plm.client.Element;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 *
 * @author: Martin Devillers
 */
public class Document extends Element implements Serializable{

    private static final String JSON_KEY_DOCUMENT_NAME = "title";
    private static final String JSON_KEY_DOCUMENT_ITERATIONS = "documentIterations";
    private static final String JSON_KEY_DOCUMENT_ITERATION_NOTE = "revisionNote";
    private static final String JSON_KEY_DOCUMENT_ATTACHED_FILES = "attachedFiles";

    private String identification, reference;
    private String path, type, lifeCycleState;
    private String[] files;

    private boolean iterationNotification, stateChangeNotification;


    public int getNumberOfFiles(){
        if (files == null){
            return 0;
        }
        return files.length;
    }

    public String getFileName(int i){
        try{
            return files[i].substring(files[i].lastIndexOf("/")+1);
        } catch (IndexOutOfBoundsException e){
            return files[i];
        }
    }

    public String getFile(int i){
        return files[i];
    }

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
        values[2] = authorName;
        values[3] = creationDate;
        values[4] = type;
        values[5] = name;
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

    @Override
    public String[] getLastIteration(){
        String[] result = new String[4];
        result[0] = identification + "-" + iterationNumber;
        result[1] = iterationNote;
        result[2] = iterationDate;
        result[3] = iterationAuthor;
        return result;
    }

    public String getIdentification(){
        return identification;
    }

    public String getAuthor(){
        return authorName;
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

    public Document updateFromJSON(JSONObject documentJSON, Resources resources) throws JSONException {
        updateElementFromJSON(documentJSON, resources);
        setDocumentDetails(
                documentJSON.getString("path"),
                documentJSON.getString("type"),
                documentJSON.getString("lifeCycleState")
        );
        return this;
    }

    @Override
    protected void updateLastIterationFromJSON(JSONObject lastIteration) throws JSONException{
        JSONArray attachedFiles = lastIteration.getJSONArray(JSON_KEY_DOCUMENT_ATTACHED_FILES);
        String[] files = new String[attachedFiles.length()];
        for (int i = 0; i<files.length; i++){
            files[i] = attachedFiles.getString(i);
            Log.i("com.docdoku.android.plm.client", "File found: " + files[i]);
        }
        setFiles(files);
    }

    private void setFiles(String[] files){
        this.files = files;
    }

    private void setDocumentDetails(String path, String type, String lifeCycleState){
        this.path = path;
        this.type = type;
        this.lifeCycleState = lifeCycleState;
    }

    /**
     * The following methods provide the keys to read the document attributes in the JSONObject received from the server
     */

    @Override
    protected String getIterationsJSONKey() {
        return JSON_KEY_DOCUMENT_ITERATIONS;
    }

    @Override
    protected String getIterationNoteJSONKey() {
        return JSON_KEY_DOCUMENT_ITERATION_NOTE;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected String getUrlPath() {
        return "/documents/" + getIdentification();
    }

    @Override
    protected String getNameJSONKey() {
        return JSON_KEY_DOCUMENT_NAME;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
