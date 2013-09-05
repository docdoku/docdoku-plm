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
 * Models either a {@link com.docdoku.android.plm.client.parts.Part Part} or a {@link com.docdoku.android.plm.client.documents.Document Document}.
 *
 * @author: martindevillers
 */
public abstract class Element implements Serializable{
    private static final String LOG_TAG = "com.docdoku.android.plm.client.Element";

    private static final String JSON_KEY_AUTHOR = "author";
    private static final String JSON_KEY_CREATION_DATE = "creationDate";
    private static final String JSON_KEY_DESCRIPTION = "description";
    private static final String JSON_KEY_CHECKOUT_USER = "checkOutUser";
    private static final String JSON_KEY_USER_NAME = "name";
    private static final String JSON_KEY_USER_LOGIN = "login";
    private static final String JSON_KEY_CHECKOUT_DATE = "checkOutDate";
    private static final String JSON_KEY_LINKED_DOCUMENTS = "linkedDocuments";
    private static final String JSON_KEY_LINKED_DOCUMENT_ID = "documentMasterId";
    private static final String JSON_KEY_LINKED_DOCUMENT_VERSION = "documentMasterVersion";
    private static final String JSON_KEY_ATTRIBUTES = "instanceAttributes";
    private static final String JSON_KEY_ATTRIBUTE_TYPE = "type";
    private static final String JSON_KEY_ATTRIBUTE_NAME = "name";
    private static final String JSON_KEY_ATTRIBUTE_VALUE = "value";
    private static final String JSON_KEY_ITERATION_NUMBER = "iteration";
    private static final String JSON_KEY_ITERATION_AUTHOR = "author";
    private static final String JSON_KEY_ITERATION_DATE = "creationDate";

    private static final String ATTRIBUTE_DATE = "DATE";
    private static final String ATTRIBUTE_BOOLEAN = "BOOLEAN";
    private static final String ATTRIBUTE_BOOLEAN_TRUE = "true";

    protected String name;
    protected String authorName;
    private String authorLogin;
    protected String creationDate;
    protected String description;
    private String[] linkedDocuments;
    private Attribute[] attributes;
    protected String checkOutUserName, checkOutUserLogin, checkOutDate;
    protected int iterationNumber;
    protected String iterationNote, iterationAuthor, iterationDate;
    private String lastIterationJSONString;

    /**
     * Uses a <code>JSONObject</code> representing this <code>Element</code> to fill in its attributes
     *
     * @param elementJSON this <code>Element</code>'s <code>JSONObject</code>
     * @param resources the <code>Resources</code> to convert some of attributes in the <code>JSONObject</code> to more readable <code>String</code>s
     * @return this <code>Element</code>
     * @throws JSONException if an error is encountered when reading the <code>JSONObject</code>
     */
    public abstract Element updateFromJSON(JSONObject elementJSON, Resources resources) throws JSONException;


    public String getAuthor(){
        return authorName;
    }

    public String getCheckOutUserName(){
        return checkOutUserName;
    }

    public String getCheckOutUserLogin(){
        return checkOutUserLogin;
    }

    public String getCheckOutDate(){
        return checkOutDate;
    }

    public int getIterationNumber() {
        return iterationNumber;
    }

    public int getNumberOfAttributes(){
        if (attributes != null){
            return attributes.length;
        } else {
            return 0;
        }
    }

    public Attribute getAttribute(int i){
        return attributes[i];
    }

    public String[] getAttributeNames(){
        String[] attributeNames = new String[0];
        if (attributes != null){
            attributeNames = new String[attributes.length];
            for (int i = 0; i < attributeNames.length; i++){
                attributeNames[i] = attributes[i].getName();
            }
        }
        return attributeNames;
    }

    public String[] getAttributeValues(){
        String[] attributeValues = new String[0];
        if (attributes != null){
            attributeValues = new String[attributes.length];
            for (int i = 0; i < attributeValues.length; i++){
                attributeValues[i] = attributes[i].getValue();
            }
        }
        return attributeValues;
    }

    public int getNumberOfLinkedDocuments(){
        if (linkedDocuments == null) return 0;
        return linkedDocuments.length;
    }

    public String getLinkedDocument(int i){
        return linkedDocuments[i];
    }

    /**
     * Update the <code>JSONObject</code> representing this <code>Element</code>'s last iteration to set the iteration
     * note provided by the user.
     *
     * @param note the iteration note
     * @return the <code>JSONObject</code>, updated
     */
    public JSONObject getLastIterationJSONWithUpdateNote(String note){
        try{
            JSONObject lastIteration = new JSONObject(lastIterationJSONString);
            lastIteration.put(getIterationNoteJSONKey(), note);
            return lastIteration;
        } catch (JSONException e){
            Log.e(LOG_TAG, "Failed to update iteration JSON object");
        }
        return null;
    }

    public String getLastIterationAuthorName(){
        return iterationAuthor;
    }

    public String getLastIterationDate(){
        return iterationDate;
    }

    protected Element updateElementFromJSON(JSONObject elementJSON, Resources resources) throws JSONException{
        SimpleDateFormat dateFormat = new SimpleDateFormat(resources.getString(R.string.fullDateFormat));
        name = elementJSON.getString(getNameJSONKey());
        creationDate = dateFormat.format(new Date(Long.valueOf(elementJSON.getString(JSON_KEY_CREATION_DATE))));
        description = elementJSON.getString(JSON_KEY_DESCRIPTION);
        JSONObject author = elementJSON.getJSONObject(JSON_KEY_AUTHOR);
        setAuthor(author.getString(JSON_KEY_USER_LOGIN),
                author.getString(JSON_KEY_USER_NAME));
        Object checkOutUser = elementJSON.get(JSON_KEY_CHECKOUT_USER);
        if (!checkOutUser.equals(JSONObject.NULL)){
            setCheckOutInformation(
                    ((JSONObject) checkOutUser).getString(JSON_KEY_USER_NAME),
                    ((JSONObject) checkOutUser).getString(JSON_KEY_USER_LOGIN),
                    dateFormat.format(new Date(Long.valueOf(elementJSON.getString(JSON_KEY_CHECKOUT_DATE))))
            );
        }
        JSONArray iterationsArray = elementJSON.getJSONArray(getIterationsJSONKey());
        int numIterations = iterationsArray.length();
        if (numIterations>0){
            JSONObject lastIteration = iterationsArray.getJSONObject(numIterations - 1);
            updateLastIterationFromJSON(lastIteration);
            setLastIteration(lastIteration.getInt(JSON_KEY_ITERATION_NUMBER),
                    lastIteration.getString(getIterationNoteJSONKey()),
                    lastIteration.getJSONObject(JSON_KEY_ITERATION_AUTHOR).getString(JSON_KEY_USER_NAME),
                    dateFormat.format(new Date(Long.valueOf(lastIteration.getString(JSON_KEY_ITERATION_DATE)))));
            JSONArray linkedDocuments = lastIteration.getJSONArray(JSON_KEY_LINKED_DOCUMENTS);
            String[] documents = new String[linkedDocuments.length()];
            Log.i(LOG_TAG, "Number of linked documents found :" + linkedDocuments.length());
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
                Log.i(LOG_TAG, "Attribute found. \nType: " + attributeType + "\nName: " + attributeName + "\nRaw value: " + attributeValue);
                if (attributeType.equals(ATTRIBUTE_DATE) && !attributeValue.equals("")){
                    Log.i(LOG_TAG, "Attribute identified as date type");
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
            }
            setAttributes(attributes);
            lastIterationJSONString = lastIteration.toString();
        }
        return this;
    }

    protected abstract void updateLastIterationFromJSON(JSONObject lastIteration) throws JSONException;

    private void setAuthor(String authorLogin, String authorName){
        this.authorLogin = authorLogin;
        this.authorName = authorName;
    }

    private void setAttributes(Attribute[] attributes){
        this.attributes = attributes;
    }

    private void setLinkedDocuments(String[] linkedDocuments){
        this.linkedDocuments = linkedDocuments;
    }

    /**
     * Sets the information for the last checkin/checkout of this <code>Element</code>
     *
     * @param checkOutUserName null if checked in, the checkout user's name if checked out
     * @param checkOutUserLogin null if checked in, the checkout user's login if checked out
     * @param checkOutDate null if checked in, the checkout date if checked out
     */
    public void setCheckOutInformation(String checkOutUserName, String checkOutUserLogin, String checkOutDate){
        this.checkOutUserName = checkOutUserName;
        this.checkOutUserLogin = checkOutUserLogin;
        this.checkOutDate = checkOutDate;
    }

    /**
     * Sets the information for this <code>Element</code>'s last iteration
     *
     * @param iterationNumber the last iteration's number
     * @param iterationNote the note left by the last iteration's author
     * @param iterationAuthor the last iteration author's name
     * @param iterationDate the date of the last iteration
     */
    public void setLastIteration(int iterationNumber, String iterationNote, String iterationAuthor, String iterationDate){
        this.iterationNumber = iterationNumber;
        this.iterationAuthor = iterationAuthor;
        this.iterationDate = iterationDate;
        if (JSONObject.NULL.toString().equals(iterationNote)){
            this.iterationNote = "";
        }
        else{
            this.iterationNote = iterationNote;
        }
    }

    protected abstract String getNameJSONKey();
    protected abstract String getIterationsJSONKey();
    protected abstract String getIterationNoteJSONKey();
    protected abstract String getUrlPath();
    protected abstract String[] getLastIteration();

    /**
     * Class representing an <code>Element</code>'s attribute, with its <code>name</code> and its <code>value</code>.
     */
    public class Attribute implements Serializable {

        private final String name;
        private final String value;

        public Attribute(String name, String value){
            this.name = name;
            this.value = value;
        }

        public String getName(){
            return name;
        }

        public String getValue(){
            return value;
        }
    }
}
