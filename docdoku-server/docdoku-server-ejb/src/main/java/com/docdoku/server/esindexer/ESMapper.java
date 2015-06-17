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
import com.docdoku.core.meta.Tag;
import com.docdoku.core.product.PartIteration;
import com.docdoku.core.product.PartRevisionKey;
import com.docdoku.core.workflow.Workflow;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class is use to convert DocdokuPLM common item to JSON Builder for ElasticSearch databases.
 *
 * @author Taylor LABEJOF
 */
public class ESMapper {
    private static final Logger LOGGER = Logger.getLogger(ESTools.class.getName());
    public static final String WORKSPACEID_KEY = "workspaceId";
    public static final String ITERATIONS_KEY = "iterations";
    public static final String VERSION_KEY = "version";
    public static final String AUTHOR_KEY = "author";
    public static final String AUTHOR_LOGIN_KEY = "login";
    public static final String AUTHOR_NAME_KEY = "name";
    public static final String AUTHOR_SEARCH_KEY = ITERATIONS_KEY + "." + AUTHOR_KEY + "." + AUTHOR_LOGIN_KEY;
    public static final String CREATION_DATE_KEY = "creationDate";
    public static final String MODIFICATION_DATE_KEY = "modificationDate";
    public static final String TYPE_KEY = "type";


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
        return new DocumentRevisionKey(extractValue(source, WORKSPACEID_KEY), extractValue(source, "docMId"), extractValue(source, VERSION_KEY));
    }

    /**
     * Get the part revision key matching a hit line.
     *
     * @param source The source of a SearchHit
     * @return The part revision key
     */
    protected static PartRevisionKey getPartRevisionKey(Map<String, Object> source) {
        return new PartRevisionKey(extractValue(source, WORKSPACEID_KEY), extractValue(source, "partNumber"), extractValue(source, VERSION_KEY));
    }

    /**
     * Convert a Document Revision to a JSON Builder.
     *
     * @param doc           Document to pass to JSON
     * @param contentInputs Map of binary resources content
     * @return A JSON Builder to index
     */
    protected static XContentBuilder documentRevisionToJSON(DocumentIteration doc, Map<String, String> contentInputs) {
        try {

            XContentBuilder tmp = XContentFactory.jsonBuilder()
                    .startObject();
            setField(tmp, WORKSPACEID_KEY, doc.getWorkspaceId(), 0.6f);
            setField(tmp, "docMId", doc.getDocumentRevision().getDocumentMasterId(), 4.75f);
            setField(tmp, "title", doc.getDocumentRevision().getTitle(), 5f);
            setField(tmp, VERSION_KEY, doc.getDocumentVersion(), 0.10f);
            tmp.startArray(ITERATIONS_KEY);
            for (DocumentIteration iteration : doc.getDocumentRevision().getDocumentIterations()) {
                tmp.startObject();
                setField(tmp, "iteration", "" + iteration.getIteration(), 0.10f);
                if (doc.getAuthor() != null) {
                    tmp.startObject(AUTHOR_KEY);
                    setField(tmp, AUTHOR_LOGIN_KEY, iteration.getAuthor().getLogin(), 0.6f);
                    setField(tmp, AUTHOR_NAME_KEY, iteration.getAuthor().getName(), 0.6f);
                    tmp.endObject();
                }
                setField(tmp, TYPE_KEY, iteration.getDocumentRevision().getDocumentMaster().getType(), 2f);
                setField(tmp, CREATION_DATE_KEY, iteration.getDocumentRevision().getCreationDate(), 0.4f);
                setField(tmp, MODIFICATION_DATE_KEY, iteration.getModificationDate(), 0.4f);
                setField(tmp, "description", iteration.getDocumentRevision().getDescription(), 2f);
                setField(tmp, "revisionNote", iteration.getRevisionNote(), 0.5f);
                setField(tmp, "workflow", iteration.getDocumentRevision().getWorkflow(), 0.5f);
                setField(tmp, "folder", iteration.getDocumentRevision().getLocation().getShortName(), 0.5f);
                if (!iteration.getDocumentRevision().getTags().isEmpty()) {
                    tmp.startArray("tags");
                    for (Tag tag : doc.getDocumentRevision().getTags()) {
                        tmp.value(tag.getLabel());
                    }
                    tmp.endArray();
                }
                if (!iteration.getInstanceAttributes().isEmpty()) {
                    tmp.startObject("attributes");
                    Collection<InstanceAttribute> listAttr = iteration.getInstanceAttributes();
                    for (InstanceAttribute attr : listAttr) {
                        setField(tmp, attr.getNameWithoutWhiteSpace(), "" + attr.getValue(), 0.6f);
                    }
                    tmp.endObject();
                }

                if (!iteration.getAttachedFiles().isEmpty()) {
                    tmp.startObject("files");
                    for (Map.Entry<String, String> contentInput : contentInputs.entrySet()) {
                        tmp.startObject(contentInput.getKey());
                        setField(tmp, AUTHOR_NAME_KEY, contentInput.getKey(), 0.8f);
                        setField(tmp, "content", contentInput.getValue(), 0.6f);
                        tmp.endObject();
                    }
                    tmp.endObject();
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
        setParam(params, WORKSPACEID_KEY, doc.getWorkspaceId(), 0.6f);
        setParam(params, "docMId", doc.getDocumentRevision().getDocumentMasterId(), 4.75f);
        setParam(params, "title", doc.getDocumentRevision().getTitle(), 5f);
        setParam(params, VERSION_KEY, doc.getDocumentVersion(), 0.10f);
        setParam(params, "iteration", "" + doc.getIteration(), 0.10f);
        if (doc.getAuthor() != null) {
            Map<String, Object> authorParams = new HashMap<>();
            params.put(AUTHOR_KEY, authorParams);
            setParam(authorParams, AUTHOR_LOGIN_KEY, doc.getAuthor().getLogin(), 0.6f);
            setParam(authorParams, AUTHOR_NAME_KEY, doc.getAuthor().getName(), 0.6f);
        }
        setParam(params, TYPE_KEY, doc.getDocumentRevision().getDocumentMaster().getType(), 2f);
        setParam(params, CREATION_DATE_KEY, doc.getDocumentRevision().getCreationDate(), 0.4f);
        setParam(params, MODIFICATION_DATE_KEY, doc.getModificationDate(), 0.4f);
        setParam(params, "description", doc.getDocumentRevision().getDescription(), 2f);
        setParam(params, "revisionNote", doc.getRevisionNote(), 0.5f);
        setParam(params, "workflow", doc.getDocumentRevision().getWorkflow(), 0.5f);
        setParam(params, "folder", doc.getDocumentRevision().getLocation().getShortName(), 0.5f);
        if (!doc.getDocumentRevision().getTags().isEmpty()) {
            params.put("tags", doc.getDocumentRevision().getTags().toArray());
        }
        if (!doc.getInstanceAttributes().isEmpty()) {
            Map<String, Object> attrParams = new HashMap<>();
            params.put("attributes", attrParams);
            Collection<InstanceAttribute> listAttr = doc.getInstanceAttributes();
            for (InstanceAttribute attr : listAttr) {
                setParam(attrParams, attr.getNameWithoutWhiteSpace(), "" + attr.getValue(), 0.6f);
            }
        }
        if (!doc.getAttachedFiles().isEmpty()) {
            Map<String, Object> filesParams = new HashMap<>();
            params.put("files", filesParams);
            for (Map.Entry<String, String> contentInput : contentInputs.entrySet()) {
                Map<String, Object> map = new HashMap<>();
                filesParams.put(contentInput.getKey(), map);
                setParam(map, AUTHOR_NAME_KEY, contentInput.getKey(), 0.8f);
                setParam(map, "content", contentInput.getValue(), 0.6f);
            }
        }

        return params;

    }

    /**
     * Create the Json for a new Part.
     * This will be used if and only if the PartRevision has not been indexed in elastic search.
     *
     * @param part the PartIteration which was checkin.
     * @return The Json produced contains the PartRevision information and the information of all the iteration.
     */
    protected static XContentBuilder partRevisionToJson(PartIteration part) {
        try {
            XContentBuilder tmp = XContentFactory.jsonBuilder()
                    .startObject();
            setField(tmp, WORKSPACEID_KEY, part.getWorkspaceId(), 0.6f);
            setField(tmp, "partNumber", part.getPartNumber(), 4.75f);
            setField(tmp, "name", part.getPartRevision().getPartMaster().getName(), 5f);
            setField(tmp, TYPE_KEY, part.getPartRevision().getPartMaster().getType(), 2f);
            setField(tmp, VERSION_KEY, part.getPartVersion(), 0.10f);
            tmp.startArray(ITERATIONS_KEY);
            for (PartIteration iteration : part.getPartRevision().getPartIterations()) {
                tmp.startObject();

                setField(tmp, "iteration", iteration.getIteration(), 0.10f);
                setField(tmp, "standardPart", iteration.getPartRevision().getPartMaster().isStandardPart(), 0.05f);
                if (iteration.getAuthor() != null) {
                    tmp.startObject(AUTHOR_KEY);
                    setField(tmp, AUTHOR_LOGIN_KEY, iteration.getAuthor().getLogin(), 0.6f);
                    setField(tmp, AUTHOR_NAME_KEY, iteration.getAuthor().getName(), 0.6f);
                    tmp.endObject();
                }
                setField(tmp, CREATION_DATE_KEY, iteration.getCreationDate(), 0.4f);
                setField(tmp, MODIFICATION_DATE_KEY, iteration.getModificationDate(), 0.4f);
                setField(tmp, "description", iteration.getPartRevision().getDescription(), 2f);
                setField(tmp, "revisionNote", iteration.getIterationNote(), 0.5f);
                setField(tmp, "workflow", iteration.getPartRevision().getWorkflow(), 0.5f);
                if (!iteration.getInstanceAttributes().isEmpty()) {
                    tmp.startObject("attributes");
                    Collection<InstanceAttribute> listAttr = iteration.getInstanceAttributes();
                    for (InstanceAttribute attr : listAttr) {
                        setField(tmp, attr.getNameWithoutWhiteSpace(), "" + attr.getValue(), 0.6f);
                    }
                    tmp.endObject();
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
     * @return The Map is well formatted to be used by an elasticsearch Script
     */
    protected static Map<String, Object> partIterationMap(PartIteration part) {
        Map<String, Object> params = new HashMap<>();

        setParam(params, WORKSPACEID_KEY, part.getWorkspaceId(), 0.6f);
        setParam(params, VERSION_KEY, part.getPartVersion(), 0.10f);
        setParam(params, "iteration", part.getIteration(), 0.10f);
        setParam(params, "standardPart", part.getPartRevision().getPartMaster().isStandardPart(), 0.05f);

        if (part.getAuthor() != null) {
            // new Map equivalent of startObject of XContentBuilder.
            Map<String, Object> authorParams = new HashMap<>();
            params.put(AUTHOR_KEY, authorParams);
            setParam(authorParams, AUTHOR_LOGIN_KEY, part.getAuthor().getLogin(), 0.6f);
            setParam(authorParams, AUTHOR_NAME_KEY, part.getAuthor().getName(), 0.6f);
        }

        setParam(params, CREATION_DATE_KEY, part.getCreationDate(), 0.4f);
        setParam(params, MODIFICATION_DATE_KEY, part.getModificationDate(), 0.4f);
        setParam(params, "description", part.getPartRevision().getDescription(), 2f);
        setParam(params, "revisionNote", part.getIterationNote(), 0.5f);
        setParam(params, "workflow", part.getPartRevision().getWorkflow(), 0.5f);

        if (!part.getInstanceAttributes().isEmpty()) {
            Collection<InstanceAttribute> listAttr = part.getInstanceAttributes();
            Map<String, Object> attributesParams = new HashMap<>();
            params.put("attributes", attributesParams);
            for (InstanceAttribute attr : listAttr) {
                setParam(attributesParams, attr.getNameWithoutWhiteSpace(), "" + attr.getValue(), 0.6f);
            }
        }

        return params;
    }

    private static void setParam(Map<String, Object> params, String name, Object value, float coef) {
        if (value != null) {
            Object array[] = new Object[2];
            array[0] = value;
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
            finalLifeCycleState = (finalLifeCycleState != null && !"".equals(finalLifeCycleState)) ? finalLifeCycleState : " ";
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