/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2015 DocDoku SARL
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

package com.docdoku.cli.helpers;

import javax.json.*;
import java.io.*;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Set;

public class MetaDirectoryManager {

    private File metaDirectory;
    private Properties indexProps;


    private static final String META_DIRECTORY_NAME = ".dplm";
    private static final String INDEX_FILE_NAME = "index.json";

    private static final String PART_NUMBER_PROP = "partNumber";
    private static final String REVISION_PROP = "revision";
    private static final String ITERATION_PROP = "iteration";
    private static final String WORKSPACE_PROP = "workspace";
    private static final String ID_PROP = "id";
    private static final String LAST_MODIFIED_DATE_PROP = "lastModifiedDate";
    private static final String DIGEST_PROP = "digest";

    public MetaDirectoryManager(File workingDirectory) throws IOException {
        this.metaDirectory=new File(workingDirectory,META_DIRECTORY_NAME);
        if(!metaDirectory.exists()) {
            metaDirectory.mkdir();
        }
        File indexFile = new File(metaDirectory,INDEX_FILE_NAME);
        indexProps = loadPropertiesFromIndexFile(indexFile);
    }

    private void saveIndex() throws IOException {
        File indexFile = new File(metaDirectory,INDEX_FILE_NAME);
        if(!indexFile.exists()) {
            indexFile.createNewFile();
        }
        JsonWriter writer = Json.createWriter(new FileOutputStream(indexFile));
        writer.write(getPropertiesAsJsonObject());
        writer.close();
    }

    public void setPartNumber(String filePath, String partNumber) throws IOException {
        indexProps.setProperty(filePath + "." + PART_NUMBER_PROP, partNumber);
        saveIndex();
    }

    public void setDocumentId(String filePath, String id) throws IOException {
        indexProps.setProperty(filePath + "." + ID_PROP, id);
        saveIndex();
    }

    public void setRevision(String filePath, String revision) throws IOException {
        indexProps.setProperty(filePath + "." + REVISION_PROP, revision);
        saveIndex();
    }

    public void setIteration(String filePath, int iteration) throws IOException {
        indexProps.setProperty(filePath + "." + ITERATION_PROP, iteration+"");
        saveIndex();
    }

    public void setLastModifiedDate(String filePath, long lastModifiedDate) throws IOException {
        indexProps.setProperty(filePath + "." + LAST_MODIFIED_DATE_PROP, lastModifiedDate+"");
        saveIndex();
    }

    public void setWorkspace(String filePath, String workspaceId) throws IOException {
        indexProps.setProperty(filePath + "." + WORKSPACE_PROP, workspaceId+"");
        saveIndex();
    }

    public void setDigest(String filePath, String digest) throws IOException {
        indexProps.setProperty(filePath + "." + DIGEST_PROP, digest);
        saveIndex();
    }

    public long getLastModifiedDate(String filePath){
        return Long.parseLong(indexProps.getProperty(filePath + "." + LAST_MODIFIED_DATE_PROP,"0"));
    }

    public String getPartNumber(String filePath){
        return indexProps.getProperty(filePath + "." + PART_NUMBER_PROP);
    }

    public String getWorkspace(String filePath){
        return indexProps.getProperty(filePath + "." + WORKSPACE_PROP);
    }

    public String getRevision(String filePath){
        return indexProps.getProperty(filePath + "." + REVISION_PROP);
    }

    public int getIteration(String filePath){
        return Integer.parseInt(indexProps.getProperty(filePath + "." + ITERATION_PROP,"0"));
    }

    public void deleteEntryInfo(String filePath) throws IOException {
        indexProps.remove(filePath + "." + ID_PROP);
        indexProps.remove(filePath + "." + PART_NUMBER_PROP);
        indexProps.remove(filePath + "." + REVISION_PROP);
        indexProps.remove(filePath + "." + ITERATION_PROP);
        indexProps.remove(filePath + "." + LAST_MODIFIED_DATE_PROP);
        indexProps.remove(filePath + "." + DIGEST_PROP);
        indexProps.remove(filePath + "." + WORKSPACE_PROP);
        saveIndex();
    }

    public String getDocumentId(String filePath) {
        return indexProps.getProperty(filePath + "." + ID_PROP);
    }

    public boolean isDocumentRelated(String filePath) {
        return getDocumentId(filePath) != null;
    }

    public boolean isPartRelated(String filePath) {
        return getPartNumber(filePath) != null;
    }

    private JsonObject getPropertiesAsJsonObject(){
        JsonObjectBuilder json = Json.createObjectBuilder();
        Enumeration keys = indexProps.keys();
        while (keys.hasMoreElements()) {
            String key = (String)keys.nextElement();
            String value = (String)indexProps.get(key);
            json.add(key,value);
        }
        return json.build();
    }

    private Properties loadPropertiesFromIndexFile(File file) {

        Properties props = new Properties();

        if(!file.exists()) {
            return props;
        }

        try{
            JsonReader reader = Json.createReader(new FileInputStream(file));
            JsonObject json = reader.readObject();
            Set<String> keys = json.keySet();
            for(String key : keys){
                props.setProperty(key,json.getString(key));
            }
            return props;

        } catch(IOException ex){
            file.delete();
            return props;
        }

    }
}
