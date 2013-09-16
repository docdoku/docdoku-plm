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

package com.docdoku.android.plm.client.parts;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import com.docdoku.android.plm.client.Element;
import com.docdoku.android.plm.client.R;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Model containing the data for a part
 *
 * @author: Martin Devillers
 * @version 1.0
 */
public class Part extends Element implements Serializable{
    private static final String LOG_TAG = "com.docdoku.android.plm.client.parts.Part";

    private static final String JSON_KEY_PART_NAME = "name";
    private static final String JSON_KEY_PART_ITERATIONS = "partIterations";
    private static final String JSON_KEY_PART_ITERATION_NOTE = "iterationNote";
    private static final String JSON_KEY_PART_CAD_FILE = "nativeCADFile";
    private static final String JSON_KEY_COMPONENT_ARRAY = "components";
    private static final String JSON_KEY_COMPONENT_INFORMATION = "component";
    private static final String JSON_KEY_COMPONENT_NUMBER = "number";
    private static final String JSON_KEY_COMPONENT_AMOUNT = "amount";
    private static final String JSON_KEY_PART_NUMBER = "number";
    private static final String JSON_KEY_PART_VERSION = "version";
    private static final String JSON_KEY_PART_LIFECYCLE_STATE = "lifeCycleState";
    private static final String JSON_KEY_PART_STANDARD_PART = "standardPart";

    private final String key;
    private String number;
    private String version;
    private String nativeCADFile;
    private String lifecycleState;
    private boolean standardPart;
    private Component[] components;

    public Part(String key){
        this.key = key;
    }

    public String[] getGeneralInformationValues(Context context){
        String[] generalInformationValues = new String[10];
        generalInformationValues[0] = number;
        generalInformationValues[1] = name;
        if (standardPart){
            generalInformationValues[2] = context.getResources().getString(R.string.yes);
        }else{
            generalInformationValues[2] = context.getResources().getString(R.string.no);
        }
        generalInformationValues[3] = version;
        generalInformationValues[4] = authorName;
        generalInformationValues[5] = creationDate;
        if (JSONObject.NULL.toString().equals(lifecycleState)){
            generalInformationValues[6] = "";
        } else {
            generalInformationValues[6] = lifecycleState;
        }
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

    public String getCADFileName(){
        try{
            return nativeCADFile.substring(nativeCADFile.lastIndexOf("/") + 1);
        } catch (IndexOutOfBoundsException e){
            return nativeCADFile;
        }
    }

    public String getCADFileUrl(){
        return nativeCADFile;
    }

    public int getNumComponents(){
        if (components == null){
            return 0;
        }else{
            Log.i(LOG_TAG, "Number of components found: " + components.length);
            return components.length;
        }
    }

    public Component getComponent(int i){
        return components[i];
    }

    @Override
    public String[] getLastIteration(){
        String[] result = new String[4];
        result[0] = key + "-" + iterationNumber;
        result[1] = iterationNote;
        result[2] = iterationDate;
        result[3] = iterationAuthor;
        return result;
    }

    @Override
    protected void updateLastIterationFromJSON(JSONObject lastIteration) throws JSONException {
        Object CADFile = lastIteration.get(JSON_KEY_PART_CAD_FILE);
        if (JSONObject.NULL.equals(CADFile)){
            nativeCADFile = null;
        }else{
            nativeCADFile = (String) CADFile;
            Log.i(LOG_TAG, "CAD file downloaded: " + nativeCADFile);
        }
        JSONArray componentArray = lastIteration.getJSONArray(JSON_KEY_COMPONENT_ARRAY);
        components = new Component[componentArray.length()];
        for (int i = 0; i<components.length; i++){
            JSONObject component = componentArray.getJSONObject(i);
            JSONObject componentInformation = component.getJSONObject(JSON_KEY_COMPONENT_INFORMATION);
            components[i] = new Component(componentInformation.getString(JSON_KEY_COMPONENT_NUMBER), component.getInt(JSON_KEY_COMPONENT_AMOUNT));
        }
    }

    public String getKey(){
        return key;
    }

    @Override
    public Part updateFromJSON(JSONObject partJSON, Resources resources) throws JSONException {
        updateElementFromJSON(partJSON, resources);
        setPartDetails(
                partJSON.getString(JSON_KEY_PART_NUMBER),
                partJSON.getString(JSON_KEY_PART_VERSION),
                partJSON.getString(JSON_KEY_PART_LIFECYCLE_STATE),
                partJSON.getBoolean(JSON_KEY_PART_STANDARD_PART)
        );
        return this;
    }

    private void setPartDetails(String number, String version, String lifecycleState, boolean standardPart){
        this.number = number;
        this.version = version;
        this.lifecycleState = lifecycleState;
        this.standardPart = standardPart;
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
    protected String getUrlPath() {
        return "/parts/" + getKey();
    }

    @Override
    protected String getNameJSONKey() {
        return JSON_KEY_PART_NAME;
    }

    public class Component implements Serializable{
        private final String number;
        private final int amount;

        public Component(String number, int amount){
            this.number = number;
            this.amount = amount;
        }

        public String getNumber(){
            return number;
        }

        public int getAmount() {
            return amount;
        }
    }
}
