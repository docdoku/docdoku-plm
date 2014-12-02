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
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class is use to convert DocdokuPLM common item to JSON Builder for ElasticSearch databases
 * @author Taylor LABEJOF
 */
public class ESMapper {
    private static final Logger LOGGER = Logger.getLogger(ESTools.class.getName());
    public static final String WORKSPACEID_KEY ="workspaceId";
    public static final String VERSION_KEY ="version";
    public static final String AUTHOR_KEY ="author";
    public static final String CREATION_DATE_KEY ="creationDate";
    public static final String TYPE_KEY ="type";


    private ESMapper(){
        super();
    }

    /**
     * Get the document revision key matching a hit line
     * @param source The source of a SearchHit
     * @return The document revision key
     */
    protected static DocumentRevisionKey getDocumentRevisionKey(Map<String, Object> source){
        return new DocumentRevisionKey(extractValue(source,WORKSPACEID_KEY), extractValue(source,"docMId"), extractValue(source,VERSION_KEY));
    }

    /**
     * Get the part revision key matching a hit line
     * @param source The source of a SearchHit
     * @return The part revision key
     */
    protected static PartRevisionKey getPartRevisionKey(Map<String, Object> source){
        return new PartRevisionKey(extractValue(source,WORKSPACEID_KEY), extractValue(source,"partNumber"), extractValue(source,VERSION_KEY));
    }

    /**
     * Convert a Document Iteration to a JSON Builder
     * @param doc Document to pass to JSON
     * @param contentInputs Map of binary resources content
     * @return A JSON Builder to index
     */
    protected static XContentBuilder documentIterationToJSON(DocumentIteration doc, Map<String,String> contentInputs){
        try {
            float nbIteration = doc.getDocumentRevision().getLastIteration().getIteration();                            // Calcul of the number of iteration
            float seniority = nbIteration - doc.getIteration();                                                         // Calcul of iteration seniority
            float coef = (seniority/nbIteration) * 10;                                                                  // Calcul of decrease factor
            XContentBuilder tmp = XContentFactory.jsonBuilder()
                    .startObject();
            setField(tmp,WORKSPACEID_KEY,doc.getWorkspaceId(), 1f);
            setField(tmp,"docMId",doc.getDocumentRevision().getDocumentMasterId(), 2.75f);
            setField(tmp,"title",doc.getDocumentRevision().getTitle(),3f);
            setField(tmp,VERSION_KEY,doc.getDocumentVersion(), 0.5f);
            setField(tmp,"iteration",""+doc.getIteration(),0.5f);
            if(doc.getAuthor() != null){
                tmp.startObject(AUTHOR_KEY);
                setField(tmp,"login",doc.getAuthor().getLogin(),1f);
                setField(tmp,"name",doc.getAuthor().getName(),1f);
                tmp.endObject();
            }
            setField(tmp,TYPE_KEY, doc.getDocumentRevision().getDocumentMaster().getType(), 1f);
            setField(tmp,CREATION_DATE_KEY,doc.getDocumentRevision().getCreationDate(),1f);
            setField(tmp,"description",doc.getDocumentRevision().getDescription(),1f);
            setField(tmp,"revisionNote",doc.getRevisionNote(),0.5f);
            setField(tmp,"workflow",doc.getDocumentRevision().getWorkflow(),0.25f);
            setField(tmp,"folder",doc.getDocumentRevision().getLocation().getShortName(),0.25f);
            if(!doc.getDocumentRevision().getTags().isEmpty()){
                tmp.startArray("tags");
                for(Tag tag:doc.getDocumentRevision().getTags()){
                    tmp.value(tag.getLabel());
                }
                tmp.endArray();
            }
            if(!doc.getInstanceAttributes().isEmpty()){
                tmp.startObject("attributes");
                Collection<InstanceAttribute> listAttr = doc.getInstanceAttributes().values();
                for(InstanceAttribute attr:listAttr){
                    setField(tmp,attr.getNameWithoutWhiteSpace(),""+attr.getValue(),1f);
                }
                tmp.endObject();
            }
            if(!doc.getAttachedFiles().isEmpty()){
                tmp.startObject("files");
                for(Map.Entry<String, String> contentInput : contentInputs.entrySet()) {
                    tmp.startObject(contentInput.getKey());
                    setField(tmp,"name",contentInput.getKey(),1f);
                    setField(tmp, "content", contentInput.getValue(), 1f);
                    tmp.endObject();
                }
                tmp.endObject();
            }
            setField(tmp, "negative_boost_value", "", coef);                                                               // Set the decrease factor
            tmp.endObject();
            return tmp;
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "The document " + doc + " can't be indexed.",e);
            return null;
        }
    }

    /**
     * Convert a Part Iteration to a JSON Builder
     * @param part Part to pass to JSON
     * @return A JSON Builder to index
     */
    protected static XContentBuilder partIterationToJSON(PartIteration part) {
        try {
            float nbIteration = part.getPartRevision().getLastIteration().getIteration();                               // Calcul of the number of iteration
            float seniority = nbIteration - part.getIteration();                                                        // Calcul of iteration seniority
            float coef = (seniority/nbIteration) * 10;                                                                  // Calcul of decrease factor
            XContentBuilder tmp = XContentFactory.jsonBuilder()
                    .startObject();
            setField(tmp,WORKSPACEID_KEY,part.getWorkspaceId(), 1f);
            setField(tmp,"partNumber",part.getPartNumber(), 2.75f);
            setField(tmp,"name",part.getPartRevision().getPartMaster().getName(), 3f);
            setField(tmp,VERSION_KEY,part.getPartVersion(), 0.5f);
            setField(tmp,"iteration",part.getIteration(), 0.5f);
            setField(tmp,"standardPart",part.getPartRevision().getPartMaster().isStandardPart(), 0.25f);
            if(part.getAuthor() != null){
                tmp.startObject(AUTHOR_KEY);
                setField(tmp,"login",part.getAuthor().getLogin(),1f);
                setField(tmp,"name",part.getAuthor().getName(),1f);
                tmp.endObject();
            }
            setField(tmp,TYPE_KEY,part.getPartRevision().getPartMaster().getType(),1f);
            setField(tmp,CREATION_DATE_KEY,part.getCreationDate(),1f);
            setField(tmp,"description",part.getPartRevision().getDescription(),1f);
            setField(tmp,"revisionNote",part.getIterationNote(),0.5f);
            setField(tmp,"workflow",part.getPartRevision().getWorkflow(),0.25f);
            if(! part.getInstanceAttributes().isEmpty()){
                tmp.startObject("attributes");
                Collection<InstanceAttribute> listAttr = part.getInstanceAttributes().values();
                for(InstanceAttribute attr:listAttr){
                    setField(tmp,attr.getNameWithoutWhiteSpace(),""+attr.getValue(),1f);
                }
                tmp.endObject();
            }
            setField(tmp, "negative_boost_value", "", coef);                                                            // Set the decrease factor
            tmp.endObject();
            return tmp;
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "The part " + part + " can't be indexed.",e);
            return null;
        }
    }

    private static XContentBuilder setField(XContentBuilder object, String field, String pValue, float coef ) throws IOException {
        String value = (pValue != null && !"".equals(pValue)) ? pValue : " ";
        object.field(field, value, coef);
        return object;
    }

    private static XContentBuilder setField(XContentBuilder object, String field, int value, float coef ) throws IOException {
        object.field(field, ""+value, coef);
        return object;
    }

    private static XContentBuilder setField(XContentBuilder object, String field, Workflow value, float coef ) throws IOException {
        if(value != null){
            String finalLifeCycleState = value.getFinalLifeCycleState();
            finalLifeCycleState = (finalLifeCycleState != null && !"".equals(finalLifeCycleState)) ? finalLifeCycleState : " ";
            return object.field(field, ""+finalLifeCycleState, coef);
        }
        return null;
    }

    private static XContentBuilder setField(XContentBuilder object, String field, Object value, float coef ) throws IOException {
        if(value != null){
            return object.field(field, value, coef);
        }
        return null;
    }

    /**
     * Extract a value from a ES result
     *
     * @param source Source of a ES hit
     * @param key Key of the field to extract
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