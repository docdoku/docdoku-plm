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
import java.util.ArrayList;
import java.util.Date;

/**
 *
 * @author: Martin Devillers
 */
public class Part extends Element implements Serializable{

    private static final String JSON_KEY_PART_ITERATIONS = "partIterations";

    private String key;
    private String number;
    private String version;
    private String name;
    private String authorName;
    private String creationDate;
    private String description;
    private String workflow;
    private String lifecycleState;
    private boolean standardPart;
    private String workspaceId;
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

    public String getKey(){
        return key;
    }

    public String getAuthorName(){
        return authorName;
    }

    @Override
    public Part updateFromJSON(JSONObject partJSON, Resources resources) throws JSONException {
        updateElementFromJSON(partJSON, resources);
        SimpleDateFormat dateFormat = new SimpleDateFormat(resources.getString(R.string.fullDateFormat));
        setPartDetails(
                partJSON.getString("number"),
                partJSON.getString("version"),
                partJSON.getString("name"),
                partJSON.getJSONObject("author").getString("name"),
                dateFormat.format(new Date(Long.valueOf(partJSON.getString("creationDate")))),
                partJSON.getString("description"),
                partJSON.getString("workflow"),
                partJSON.getString("lifeCycleState"),
                partJSON.getBoolean("standardPart"),
                partJSON.getString("workspaceId"),
                partJSON.getBoolean("publicShared")
        );
        return this;
    }

    private void setPartDetails(String number, String version, String name, String authorName, String creationDate, String description, String workflow, String lifecycleState, boolean standardPart, String workspaceId, boolean publicShared){
        this.number = number;
        this.version = version;
        this.name = name;
        this.authorName = authorName;
        this.creationDate = creationDate;
        this.description = description;
        this.workflow = workflow;
        this.lifecycleState = lifecycleState;
        this.standardPart = standardPart;
        this.workspaceId = workspaceId;
        this.publicShared = publicShared;
    }

    /**
     * The following methods provide the keys to read the part attributes in the JSONObject received from the server
     */

    @Override
    protected String getIterationsJSONKey() {
        return JSON_KEY_PART_ITERATIONS;
    }
}
