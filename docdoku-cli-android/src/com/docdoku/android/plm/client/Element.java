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
 * @author: martindevillers
 */
public abstract class Element implements Serializable{

    private static final String JSON_KEY_CHECKOUT_USER = "checkOutUser";
    private static final String JSON_KEY_CHECKOUT_USER_NAME = "name";
    private static final String JSON_KEY_CHECKOUT_USER_LOGIN = "login";
    private static final String JSON_KEY_CHECKOUT_DATE = "checkOutDate";
    private static final String JSON_KEY_ATTACHED_FILES = "attachedFiles";
    private static final String JSON_KEY_LINKED_DOCUMENTS = "linkedDocuments";
    private static final String JSON_KEY_LINKED_DOCUMENT_ID = "documentMasterId";
    private static final String JSON_KEY_LINKED_DOCUMENT_VERSION = "documentMasterVersion";
    private static final String JSON_KEY_ATTRIBUTES = "instanceAttributes";
    private static final String JSON_KEY_ATTRIBUTE_TYPE = "type";
    private static final String JSON_KEY_ATTRIBUTE_NAME = "name";
    private static final String JSON_KEY_ATTRIBUTE_VALUE = "value";

    private static final String ATTRIBUTE_DATE = "DATE";
    private static final String ATTRIBUTE_BOOLEAN = "BOOLEAN";
    private static final String ATTRIBUTE_BOOLEAN_TRUE = "true";

    protected String[] files, linkedDocuments;
    protected Attribute[] attributes;
    protected String checkOutUserName, checkOutUserLogin, checkOutDate;

    public abstract Element updateFromJSON(JSONObject elementJSON, Resources resources) throws JSONException;

    public String getCheckOutUserName(){
        return checkOutUserName;
    }

    public String getCheckOutUserLogin(){
        return checkOutUserLogin;
    }

    public String[] getAttributeNames(){
        String[] attributeNames = new String[0];
        if (attributes != null){
            attributeNames = new String[attributes.length];
            for (int i = 0; i < attributeNames.length; i++){
                attributeNames[i] = attributes[i].getAttributeName();
            }

        }
        return attributeNames;
    }

    public String[] getAttributeValues(){
        String[] attributeValues = new String[0];
        if (attributes != null){
            attributeValues = new String[attributes.length];
            for (int i = 0; i < attributeValues.length; i++){
                attributeValues[i] = attributes[i].getAttributeValue();
            }
        }
        return attributeValues;
    }

    public int getNumberOfFiles(){
        if (files == null){
            return 0;
        }
        return files.length;
    }

    public String getFile(int i){
        return files[i];
    }

    protected Element updateElementFromJSON(JSONObject elementJSON, Resources resources) throws JSONException{
        Object checkOutUser = elementJSON.get(JSON_KEY_CHECKOUT_USER);
        SimpleDateFormat dateFormat = new SimpleDateFormat(resources.getString(R.string.fullDateFormat));
        if (!checkOutUser.equals(JSONObject.NULL)){
            setCheckOutInformation(
                    ((JSONObject) checkOutUser).getString(JSON_KEY_CHECKOUT_USER_NAME),
                    ((JSONObject) checkOutUser).getString(JSON_KEY_CHECKOUT_USER_LOGIN),
                    dateFormat.format(new Date(Long.valueOf(elementJSON.getString(JSON_KEY_CHECKOUT_DATE))))
            );
        }
        String JSON_KEY_ITERATIONS = getIterationsJSONKey();
        JSONArray iterationsArray = elementJSON.getJSONArray(JSON_KEY_ITERATIONS);
        int numIterations = iterationsArray.length();
        if (numIterations>0){
            JSONObject lastIteration = iterationsArray.getJSONObject(numIterations - 1);
            /*JSONArray attachedFiles = lastIteration.getJSONArray(JSON_KEY_ATTACHED_FILES);
            String[] files = new String[attachedFiles.length()];
            for (int i = 0; i<files.length; i++){
                files[i] = attachedFiles.getString(i);
                Log.i("com.docdoku.android.plm.client", "File found: " + files[i]);
            }
            setFiles(files);*/
            JSONArray linkedDocuments = lastIteration.getJSONArray(JSON_KEY_LINKED_DOCUMENTS);
            String[] documents = new String[linkedDocuments.length()];
            for (int i = 0; i<documents.length; i++){
                documents[i] = linkedDocuments.getJSONObject(i).getString(JSON_KEY_LINKED_DOCUMENT_ID) + "-" + linkedDocuments.getJSONObject(i).getString(JSON_KEY_LINKED_DOCUMENT_VERSION);
            }
            setLinkedDocuments(documents);
            JSONArray attributesArray = lastIteration.getJSONArray(JSON_KEY_ATTRIBUTES);
            Attribute[] attributes = new Attribute[attributesArray.length()];
            for (int i = 0; i<attributes.length; i++){
                JSONObject attribute = attributesArray.getJSONObject(i);
                String attributeType = attribute.getString(JSON_KEY_ATTRIBUTE_TYPE);
                String attributeName = attribute.getString(JSON_KEY_ATTRIBUTE_NAME);
                String attributeValue = attribute.getString(JSON_KEY_ATTRIBUTE_VALUE);
                if (attributeType.equals(ATTRIBUTE_DATE) && !attributeValue.equals("")){
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(resources.getString(R.string.simpleDateFormat));
                    attributes[i] = new Attribute(attributeName, simpleDateFormat.format(new Date(Long.valueOf(attributeValue))));
                }else if(attributeType.equals(ATTRIBUTE_BOOLEAN)){
                    if (attributeValue.equals(ATTRIBUTE_BOOLEAN_TRUE)){
                        attributes[i] = new Attribute(attributeName, resources.getString(R.string.True));
                    } else {
                        attributes[i] = new Attribute(attributeName, resources.getString(R.string.False));
                    }
                }else{
                    attributes[i] = new Attribute(attributeName, attributeValue);
                }
                attributes[i] = new Attribute(attribute.getString(JSON_KEY_ATTRIBUTE_NAME), attribute.getString(JSON_KEY_ATTRIBUTE_VALUE));
            }
            setAttributes(attributes);
        }
        return this;
    }

    private void setFiles(String[] files){
        this.files = files;
    }

    private void setAttributes(Attribute[] attributes){
        this.attributes = attributes;
    }

    private void setLinkedDocuments(String[] linkedDocuments){
        this.linkedDocuments = linkedDocuments;
    }

    public void setCheckOutInformation(String checkOutUserName, String checkOutUserLogin, String checkOutDate){
        this.checkOutUserName = checkOutUserName;
        this.checkOutUserLogin = checkOutUserLogin;
        this.checkOutDate = checkOutDate;
    }

    protected abstract String getIterationsJSONKey();

    public class Attribute implements Serializable {

        private String attributeName;
        private String attributeValue;

        public Attribute(String name, String value){
            attributeName = name;
            attributeValue = value;
        }

        public String getAttributeName(){
            return attributeName;
        }

        public String getAttributeValue(){
            return attributeValue;
        }
    }
}
