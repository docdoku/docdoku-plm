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
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author: Martin Devillers
 */
public class Part extends Element implements Serializable{

    private static final String JSON_KEY_PART_NAME = "name";
    private static final String JSON_KEY_PART_ITERATIONS = "partIterations";
    private static final String JSON_KEY_PART_ITERATION_NOTE = "iterationNote";

    private String key;
    private String number;
    private String version;
    private String workflow;
    private String lifecycleState;
    private boolean standardPart;
    private boolean publicShared;

    public Part(String key){
        this.key = key;
    }

    public String[] getGeneralInformationValues(){
        String[] generalInformationValues = new String[10];
        generalInformationValues[0] = number;
        generalInformationValues[1] = name;
        generalInformationValues[2] = Boolean.toString(standardPart);
        generalInformationValues[3] = version;
        generalInformationValues[4] = authorName;
        generalInformationValues[5] = creationDate;
        generalInformationValues[6] = lifecycleState;
        generalInformationValues[7] = checkOutUserName;
        generalInformationValues[8] = checkOutDate;
        generalInformationValues[9] = description;
        return generalInformationValues;
    }

    public String getCheckOutUserName(){
        return checkOutUserName;
    }

    public String getCheckOutUserLogin(){
        return checkOutUserLogin;
    }

    @Override
    protected void updateLastIterationFromJSON(JSONObject lastIteration) throws JSONException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getKey(){
        return key;
    }

    public String getAuthorName(){
        return authorName;
    }

    @Override
    public Part updateFromJSON(JSONObject partJSON, Resources resources) throws JSONException {
        updateElementFromJSON(partJSON, resources);
        setPartDetails(
                partJSON.getString("number"),
                partJSON.getString("version"),
                partJSON.getString("workflow"),
                partJSON.getString("lifeCycleState"),
                partJSON.getBoolean("standardPart"),
                partJSON.getBoolean("publicShared")
        );
        return this;
    }

    private void setPartDetails(String number, String version, String workflow, String lifecycleState, boolean standardPart, boolean publicShared){
        this.number = number;
        this.version = version;
        this.workflow = workflow;
        this.lifecycleState = lifecycleState;
        this.standardPart = standardPart;
        this.publicShared = publicShared;
    }

    /**
     * The following methods provide the keys to read the part attributes in the JSONObject received from the server
     */
    @Override
    protected String getIterationsJSONKey() {
        return JSON_KEY_PART_ITERATIONS;
    }

    @Override
    protected String getIterationNoteJSONKey() {
        return JSON_KEY_PART_ITERATION_NOTE;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected String getNameJSONKey() {
        return JSON_KEY_PART_NAME;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
