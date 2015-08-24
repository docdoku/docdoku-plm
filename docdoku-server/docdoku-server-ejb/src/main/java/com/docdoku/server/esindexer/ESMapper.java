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

package com.docdoku.server.esindexer;

import com.docdoku.core.document.DocumentIteration;
import com.docdoku.core.document.DocumentRevisionKey;
import com.docdoku.core.meta.InstanceAttribute;
import com.docdoku.core.meta.InstanceListOfValuesAttribute;
import com.docdoku.core.meta.Tag;
import com.docdoku.core.product.PartIteration;
import com.docdoku.core.product.PartRevisionKey;
import com.docdoku.core.workflow.Workflow;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class is use to convert DocdokuPLM common item to JSON Builder for ElasticSearch databases.
 *
 * @author Taylor LABEJOF
 */
public class ESMapper {
    private static final Logger LOGGER = Logger.getLogger(ESMapper.class.getName());
    public static final String WORKSPACE_ID_KEY = "workspaceId";
    public static final String ITERATIONS_KEY = "iterations";
    public static final String ITERATION_KEY = "iteration";
    public static final String VERSION_KEY = "version";
    public static final String AUTHOR_KEY = "author";
    public static final String AUTHOR_LOGIN_KEY = "login";
    public static final String AUTHOR_NAME_KEY = "name";
    public static final String AUTHOR_SEARCH_KEY = ITERATIONS_KEY + "." + AUTHOR_KEY + "." + AUTHOR_LOGIN_KEY;
    public static final String CREATION_DATE_KEY = "creationDate";
    public static final String MODIFICATION_DATE_KEY = "modificationDate";
    public static final String TYPE_KEY = "type";
    public static final String DOCUMENT_ID_KEY = "docMId";
    public static final String PART_NUMBER_KEY = "partNumber";
    public static final String PART_NAME_KEY = "name";
    public static final String TITLE_KEY = "title";
    public static final String DESCRIPTION_KEY = "description";
    public static final String REVISION_NOTE_KEY = "revisionNote";
    public static final String WORKFLOW_KEY = "workflow";
    public static final String FOLDER_KEY = "folder";
    public static final String TAGS_KEY = "tags";
    public static final String ATTRIBUTES_KEY = "attributes";
    public static final String ATTRIBUTE_NAME = "attr_name";
    public static final String ATTRIBUTE_VALUE = "attr_value";
    public static final String FILES_KEY = "files";
    public static final String CONTENT_KEY = "content";
    public static final String STANDARD_PART_KEY = "standardPart";
    public static final String PART_TYPE = "part";
    public static final String DOCUMENT_TYPE = "document" ;
    public static final String ATTR_NESTED_PATH = ITERATIONS_KEY +"."+ ATTRIBUTES_KEY;


    private ESMapper() {
        super();
    }

    /**
     * Get the document revision key matching a hit line.
     *
     * @param source The source of a SearchHit
     * @return The document revision key
     */
    protected static DocumentRevisionKey getDocumentRevisionKey(Map<String, Object> source) {
        return new DocumentRevisionKey(extractValue(source, WORKSPACE_ID_KEY), extractValue(source, DOCUMENT_ID_KEY), extractValue(source, VERSION_KEY));
    }

    /**
     * Get the part revision key matching a hit line.
     *
     * @param source The source of a SearchHit
     * @return The part revision key
     */
    protected static PartRevisionKey getPartRevisionKey(Map<String, Object> source) {
        return new PartRevisionKey(extractValue(source, WORKSPACE_ID_KEY), extractValue(source, PART_NUMBER_KEY), extractValue(source, VERSION_KEY));
    }

    /**
     * Convert a Document Revision to a JSON Builder.
     *"" + attr.getValue()
     * @param doc           Document to pass to JSON
     * @param contentInputs Map of binary resources content
     * @return A JSON Builder to index
     */
    protected static XContentBuilder documentRevisionToJSON(DocumentIteration doc, Map<String, String> contentInputs) {
        try {

            XContentBuilder tmp = XContentFactory.jsonBuilder()
                    .startObject();
            setField(tmp, WORKSPACE_ID_KEY, doc.getWorkspaceId(), 0.6f);
            setField(tmp, DOCUMENT_ID_KEY, doc.getDocumentRevision().getDocumentMasterId(), 4.75f);
            setField(tmp, TITLE_KEY, doc.getTitle(), 5f);
            setField(tmp, VERSION_KEY, doc.getVersion(), 0.10f);
            setField(tmp, TYPE_KEY, doc.getDocumentRevision().getDocumentMaster().getType(), 2f);
            setField(tmp, DESCRIPTION_KEY, doc.getDocumentRevision().getDescription(), 2f);
            tmp.startArray(ITERATIONS_KEY);
            for (DocumentIteration iteration : doc.getDocumentRevision().getDocumentIterations()) {
                tmp.startObject();
                setField(tmp, ITERATION_KEY, "" + iteration.getIteration(), 0.10f);
                if (doc.getAuthor() != null) {
                    tmp.startObject(AUTHOR_KEY);
                    setField(tmp, AUTHOR_LOGIN_KEY, iteration.getAuthor().getLogin(), 0.6f);
                    setField(tmp, AUTHOR_NAME_KEY, iteration.getAuthor().getName(), 0.6f);
                    tmp.endObject();
                }
                setField(tmp, CREATION_DATE_KEY, iteration.getDocumentRevision().getCreationDate(), 0.4f);
                setField(tmp, MODIFICATION_DATE_KEY, iteration.getModificationDate(), 0.4f);
                setField(tmp, REVISION_NOTE_KEY, iteration.getRevisionNote(), 0.5f);
                setField(tmp, WORKFLOW_KEY, iteration.getDocumentRevision().getWorkflow(), 0.5f);
                setField(tmp, FOLDER_KEY, iteration.getDocumentRevision().getLocation().getShortName(), 0.5f);
                if (!iteration.getDocumentRevision().getTags().isEmpty()) {
                    tmp.startArray(TAGS_KEY);
                    for (Tag tag : doc.getDocumentRevision().getTags()) {
                        tmp.value(tag.getLabel());
                    }
                    tmp.endArray();
                }
                if (!iteration.getInstanceAttributes().isEmpty()) {
                    Collection<InstanceAttribute> listAttr = iteration.getInstanceAttributes();
                    tmp.startArray(ATTRIBUTES_KEY);
                    for (InstanceAttribute attr : listAttr) {
                        tmp.startObject();
                        setAttrField(tmp, attr, 0.6f);
                        tmp.endObject();
                    }
                    tmp.endArray();
                }

                if (!iteration.getAttachedFiles().isEmpty()) {
                    tmp.startArray(FILES_KEY);
                    for (Map.Entry<String, String> contentInput : contentInputs.entrySet()) {
                        tmp.startObject();
                        setField(tmp, AUTHOR_NAME_KEY, contentInput.getKey(), 0.8f);
                        setField(tmp, CONTENT_KEY, contentInput.getValue(), 0.6f);
                        tmp.endObject();
                    }
                    tmp.endArray();
                }
                tmp.endObject();
            }
            tmp.endArray();
            tmp.endObject();
            return tmp;
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "The document " + doc + " can't be indexed.", e);
            return null;
        }
    }


    /**
     * Convert a Document Revision to a JSON Builder.
     * This will be used if and only if the DocumentRevision has not been indexed in elastic search.
     *
     * @param doc           Document to pass to JSON
     * @param contentInputs Map of binary resources content
     * @return A JSON Builder to index
     */
    protected static Map<String, Object> docIterationMap(DocumentIteration doc, Map<String, String> contentInputs) {
        Map<String, Object> params = new HashMap<>();
        setParam(params, WORKSPACE_ID_KEY, doc.getWorkspaceId(), 0.6f);
        setParam(params, DOCUMENT_ID_KEY, doc.getDocumentRevision().getDocumentMasterId(), 4.75f);
        setParam(params, TITLE_KEY, doc.getTitle(), 5f);
        setParam(params, VERSION_KEY, doc.getVersion(), 0.10f);
        setParam(params, ITERATION_KEY, "" + doc.getIteration(), 0.10f);
        if (doc.getAuthor() != null) {
            Map<String, Object> authorParams = new HashMap<>();
            params.put(AUTHOR_KEY, authorParams);
            setParam(authorParams, AUTHOR_LOGIN_KEY, doc.getAuthor().getLogin(), 0.6f);
            setParam(authorParams, AUTHOR_NAME_KEY, doc.getAuthor().getName(), 0.6f);
        }
        setParam(params, CREATION_DATE_KEY, doc.getDocumentRevision().getCreationDate(), 0.4f);
        setParam(params, MODIFICATION_DATE_KEY, doc.getModificationDate(), 0.4f);
        setParam(params, REVISION_NOTE_KEY, doc.getRevisionNote(), 0.5f);
        setParam(params, WORKFLOW_KEY, doc.getDocumentRevision().getWorkflow(), 0.5f);
        setParam(params, FOLDER_KEY, doc.getDocumentRevision().getLocation().getShortName(), 0.5f);
        if (!doc.getDocumentRevision().getTags().isEmpty()) {
            List<String> labels = new ArrayList<>();
            for (Tag tag : doc.getDocumentRevision().getTags()) {
                labels.add(tag.getLabel());
            }
            params.put(TAGS_KEY, labels);
        }
        if (!doc.getInstanceAttributes().isEmpty()) {
            Collection<InstanceAttribute> listAttr = doc.getInstanceAttributes();
            List<Map<String,Object>> listAttributes = new ArrayList<>();
            params.put(ATTRIBUTES_KEY, listAttributes);
            for (InstanceAttribute attr : listAttr) {
                Map<String,Object> attributesParams = new HashMap<>();
                listAttributes.add(attributesParams);
                setAttrParam(attributesParams, attr, 0.6f);
            }
        }
        if (!doc.getAttachedFiles().isEmpty()) {
            List<Map<String,Object>> filesParams = new ArrayList<>();
            params.put(FILES_KEY, filesParams);
            for (Map.Entry<String, String> contentInput : contentInputs.entrySet()) {
                Map<String, Object> map = new HashMap<>();
                filesParams.add(map);
                setParam(map, AUTHOR_NAME_KEY, contentInput.getKey(), 0.8f);
                setParam(map, CONTENT_KEY, contentInput.getValue(), 0.6f);
            }
        }

        return params;

    }

    /**
     * Create the Json for a new Part.
     * This will be used if and only if the PartRevision has not been indexed in elastic search.
     *
     * @param part the PartIteration which was checkin.
     * @param binaryList
     * @return The Json produced contains the PartRevision information and the information of all the iteration.
     */
    protected static XContentBuilder partRevisionToJson(PartIteration part, Map<String, String> binaryList) {
        try {
            XContentBuilder tmp = XContentFactory.jsonBuilder()
                    .startObject();
            setField(tmp, WORKSPACE_ID_KEY, part.getWorkspaceId(), 0.6f);
            setField(tmp, PART_NUMBER_KEY, part.getPartNumber(), 4.75f);
            setField(tmp, PART_NAME_KEY, part.getPartRevision().getPartMaster().getName(), 5f);
            setField(tmp, TYPE_KEY, part.getPartRevision().getPartMaster().getType(), 2f);
            setField(tmp, VERSION_KEY, part.getPartVersion(), 0.10f);
            setField(tmp, DESCRIPTION_KEY, part.getPartRevision().getDescription(), 2f);
            tmp.startArray(ITERATIONS_KEY);
            for (PartIteration iteration : part.getPartRevision().getPartIterations()) {
                tmp.startObject();

                setField(tmp, ITERATION_KEY, iteration.getIteration(), 0.10f);
                setField(tmp, STANDARD_PART_KEY, iteration.getPartRevision().getPartMaster().isStandardPart(), 0.05f);
                if (iteration.getAuthor() != null) {
                    tmp.startObject(AUTHOR_KEY);
                    setField(tmp, AUTHOR_LOGIN_KEY, iteration.getAuthor().getLogin(), 0.6f);
                    setField(tmp, AUTHOR_NAME_KEY, iteration.getAuthor().getName(), 0.6f);
                    tmp.endObject();
                }
                setField(tmp, CREATION_DATE_KEY, iteration.getCreationDate(), 0.4f);
                setField(tmp, MODIFICATION_DATE_KEY, iteration.getModificationDate(), 0.4f);
                setField(tmp, REVISION_NOTE_KEY, iteration.getIterationNote(), 0.5f);
                setField(tmp, WORKFLOW_KEY, iteration.getPartRevision().getWorkflow(), 0.5f);
                if (!iteration.getPartRevision().getTags().isEmpty()) {
                    tmp.startArray(TAGS_KEY);
                    for (Tag tag : iteration.getPartRevision().getTags()) {
                        tmp.value(tag.getLabel());
                    }
                    tmp.endArray();
                }
                if (!iteration.getInstanceAttributes().isEmpty()) {
                    Collection<InstanceAttribute> listAttr = iteration.getInstanceAttributes();
                    tmp.startArray(ATTRIBUTES_KEY);
                    for (InstanceAttribute attr : listAttr) {
                        tmp.startObject();
                        setAttrField(tmp,attr,0.6f);
                        tmp.endObject();
                    }
                    tmp.endArray();
                }

                if (!iteration.getAttachedFiles().isEmpty()) {
                    tmp.startArray(FILES_KEY);
                    for (Map.Entry<String, String> contentInput : binaryList.entrySet()) {
                        tmp.startObject();
                        setField(tmp, AUTHOR_NAME_KEY, contentInput.getKey(), 0.8f);
                        setField(tmp, CONTENT_KEY, contentInput.getValue(), 0.6f);
                        tmp.endObject();
                    }
                    tmp.endArray();
                }

                tmp.endObject();

            }
            tmp.endArray();
            tmp.endObject();
            return tmp;
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "The part " + part + " can't be indexed.", e);
            return null;
        }
    }

    /**
     * Create a Map of all the data of an iteration.
     *
     * @param part PartIteration to be Mapped.
     * @param binaryList
     * @return The Map is well formatted to be used by an elasticsearch Script
     */
    protected static Map<String, Object> partIterationMap(PartIteration part, Map<String, String> binaryList) {
        Map<String, Object> params = new HashMap<>();

        setParam(params, WORKSPACE_ID_KEY, part.getWorkspaceId(), 0.6f);
        setParam(params, VERSION_KEY, part.getPartVersion(), 0.10f);
        setParam(params, ITERATION_KEY, part.getIteration(), 0.10f);
        setParam(params, STANDARD_PART_KEY, part.getPartRevision().getPartMaster().isStandardPart(), 0.05f);

        if (part.getAuthor() != null) {
            // new Map equivalent of startObject of XContentBuilder.
            Map<String, Object> authorParams = new HashMap<>();
            params.put(AUTHOR_KEY, authorParams);
            setParam(authorParams, AUTHOR_LOGIN_KEY, part.getAuthor().getLogin(), 0.6f);
            setParam(authorParams, AUTHOR_NAME_KEY, part.getAuthor().getName(), 0.6f);
        }

        setParam(params, CREATION_DATE_KEY, part.getCreationDate(), 0.4f);
        setParam(params, MODIFICATION_DATE_KEY, part.getModificationDate(), 0.4f);
        setParam(params, REVISION_NOTE_KEY, part.getIterationNote(), 0.5f);
        setParam(params, WORKFLOW_KEY, part.getPartRevision().getWorkflow(), 0.5f);

        if (!part.getPartRevision().getTags().isEmpty()) {
            List<String> labels = new ArrayList<>();
            for (Tag tag : part.getPartRevision().getTags()) {
                labels.add(tag.getLabel());
            }
            params.put(TAGS_KEY, labels);
        }

        if (!part.getInstanceAttributes().isEmpty()) {
            Collection<InstanceAttribute> listAttr = part.getInstanceAttributes();
            List<Map<String,Object>> listAttributes = new ArrayList<>();
            params.put(ATTRIBUTES_KEY, listAttributes);
            for (InstanceAttribute attr : listAttr) {
                Map<String,Object> attributesParams = new HashMap<>();
                listAttributes.add(attributesParams);
                setAttrParam(attributesParams, attr, 0.6f);
            }
        }

        if (!part.getAttachedFiles().isEmpty()) {
            List<Map<String,Object>> filesParams = new ArrayList<>();
            params.put(FILES_KEY, filesParams);
            for (Map.Entry<String, String> contentInput : binaryList.entrySet()) {
                Map<String, Object> map = new HashMap<>();
                filesParams.add(map);
                setParam(map, AUTHOR_NAME_KEY, contentInput.getKey(), 0.8f);
                setParam(map, CONTENT_KEY, contentInput.getValue(), 0.6f);
            }
        }

        return params;
    }

    private static void setAttrField(XContentBuilder object,InstanceAttribute attr, float coef ) throws IOException {
        setField(object,ATTRIBUTE_NAME,attr.getNameWithoutWhiteSpace(),coef);
        if(attr instanceof InstanceListOfValuesAttribute) {
            InstanceListOfValuesAttribute lov = (InstanceListOfValuesAttribute) attr;
            String lovItemName = !lov.getItems().isEmpty() ? lov.getItems().get(lov.getIndexValue()).getName() : "";
            setField(object,ATTRIBUTE_VALUE,lovItemName,coef);
        } else {
            setField(object,ATTRIBUTE_VALUE,""+attr.getValue(),coef);
        }

    }

    private static void setAttrParam(Map<String,Object> params, InstanceAttribute attr,float coef) {
        setParam(params, ATTRIBUTE_NAME, attr.getNameWithoutWhiteSpace(), coef);
        if(attr instanceof InstanceListOfValuesAttribute) {
            InstanceListOfValuesAttribute lov = (InstanceListOfValuesAttribute) attr;
            String lovItemName = !lov.getItems().isEmpty() ? lov.getItems().get(lov.getIndexValue()).getName() : "";
            setParam(params, ATTRIBUTE_VALUE, lovItemName, coef);
        } else {
            setParam(params,ATTRIBUTE_VALUE,""+attr.getValue(),coef);
        }
    }

    private static void setParam(Map<String, Object> params, String name, Object value, float coef) {
        if (value != null) {
            Object array[] = new Object[2];
            array[0] = value;
            array[1] = coef;
            params.put(name, array);
        }
    }

    private static void setParam(Map<String, Object> params, String name, Workflow value, float coef) {
        if (value != null) {
            Object array[] = new Object[2];
            String finalLifeCycleState = value.getFinalLifeCycleState();
            finalLifeCycleState = (finalLifeCycleState != null && !finalLifeCycleState.isEmpty()) ? finalLifeCycleState : " ";
            array[0] = finalLifeCycleState;
            array[1] = coef;
            params.put(name, array);
        }
    }

    private static XContentBuilder setField(XContentBuilder object, String field, String pValue, float coef) throws IOException {
        String value = (pValue != null && !"".equals(pValue)) ? pValue : " ";
        object.field(field, value, coef);
        return object;
    }

    private static XContentBuilder setField(XContentBuilder object, String field, int value, float coef) throws IOException {
        object.field(field, "" + value, coef);
        return object;
    }

    private static XContentBuilder setField(XContentBuilder object, String field, Workflow value, float coef) throws IOException {
        if (value != null) {
            String finalLifeCycleState = value.getFinalLifeCycleState();
            finalLifeCycleState = (finalLifeCycleState != null && !finalLifeCycleState.isEmpty()) ? finalLifeCycleState : " ";
            return object.field(field, "" + finalLifeCycleState, coef);
        }
        return null;
    }

    private static XContentBuilder setField(XContentBuilder object, String field, Object value, float coef) throws IOException {
        if (value != null) {
            return object.field(field, value, coef);
        }
        return null;
    }

    /**
     * Extract a value from a ES result
     *
     * @param source Source of a ES hit
     * @param key    Key of the field to extract
     * @return The value of the field "key"
     */
    private static String extractValue(Map<String, Object> source, String key) {
        Object ret = source.get(key);
        if (ret instanceof List) {
            return ((List) ret).get(0).toString();
        } else {
            return ret.toString();
        }
    }
}