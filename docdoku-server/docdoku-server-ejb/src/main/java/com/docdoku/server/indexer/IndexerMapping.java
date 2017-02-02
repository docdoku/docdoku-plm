package com.docdoku.server.indexer;

import com.docdoku.core.common.User;
import com.docdoku.core.document.DocumentIteration;
import com.docdoku.core.document.DocumentMaster;
import com.docdoku.core.document.DocumentRevision;
import com.docdoku.core.meta.InstanceAttribute;
import com.docdoku.core.meta.InstanceListOfValuesAttribute;
import com.docdoku.core.meta.Tag;
import com.docdoku.core.product.PartIteration;
import com.docdoku.core.product.PartMaster;
import com.docdoku.core.product.PartRevision;
import com.docdoku.core.workflow.Workflow;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by morgan on 02/02/17.
 */
public class IndexerMapping {

    private static final String WORKSPACE_ID_KEY = "workspaceId";
    private static final String ITERATION_KEY = "iteration";
    private static final String VERSION_KEY = "version";
    private static final String AUTHOR_KEY = "author";
    private static final String AUTHOR_LOGIN_KEY = "login";
    private static final String AUTHOR_NAME_KEY = "name";
    private static final String CREATION_DATE_KEY = "creationDate";
    private static final String MODIFICATION_DATE_KEY = "modificationDate";
    private static final String TYPE_KEY = "type";
    private static final String DOCUMENT_ID_KEY = "docMId";
    private static final String PART_NUMBER_KEY = "partNumber";
    private static final String PART_NAME_KEY = "name";
    private static final String TITLE_KEY = "title";
    private static final String DESCRIPTION_KEY = "description";
    private static final String REVISION_NOTE_KEY = "revisionNote";
    private static final String WORKFLOW_KEY = "workflow";
    private static final String FOLDER_KEY = "folder";
    private static final String TAGS_KEY = "tags";
    private static final String ATTRIBUTES_KEY = "attributes";
    private static final String ATTRIBUTE_NAME = "attr_name";
    private static final String ATTRIBUTE_VALUE = "attr_value";
    private static final String FILES_KEY = "files";
    private static final String CONTENT_KEY = "content";
    private static final String STANDARD_PART_KEY = "standardPart";
    public static final String PART_TYPE = "part";
    public static final String DOCUMENT_TYPE = "document";

    private static final Logger LOGGER = Logger.getLogger(IndexerMapping.class.getName());

    private IndexerMapping() {
    }

    public static XContentBuilder createDocumentIterationMapping() throws IOException {
        return createMapping(DOCUMENT_TYPE);
    }

    public static XContentBuilder createPartIterationMapping() throws IOException {
        return createMapping(PART_TYPE);
    }

    public static XContentBuilder createSourceMapping() throws IOException {
        XContentBuilder tmp = XContentFactory.jsonBuilder().startObject();
        tmp.startObject("mappings");
        tmp.startObject("_default_");
        tmp.startObject("_all");
        tmp.field("enabled", "true");
        tmp.endObject();
        tmp.startArray("dynamic_templates");
        tmp.startObject();
        //All field with the name content should be analyzed for full text search
        tmp.startObject("content_string");
        tmp.field("match", CONTENT_KEY);
        tmp.field("match_mapping_type", "string");
        tmp.startObject("mapping");
        tmp.field("type", "string");
        tmp.field("index", "analyzed");
        tmp.endObject();
        tmp.endObject();
        tmp.endObject();
        tmp.startObject();
        //set by default all the field as not_analyzed.
        // data won't be flatten, term filter/query will be possible.
        tmp.startObject("default_string");
        tmp.field("match", "*");
        tmp.field("match_mapping_type", "string");
        tmp.startObject("mapping");
        tmp.field("type", "string");
        tmp.field("index", "not_analyzed");
        tmp.endObject();
        tmp.endObject();
        tmp.endObject();
        tmp.endArray();
        tmp.endObject();

        tmp.endObject();
        tmp.endObject();
        return tmp;
    }

    public static XContentBuilder documentIterationToJSON(DocumentIteration documentIteration, Map<String, String> contentInputs) {

        DocumentRevision documentRevision = documentIteration.getDocumentRevision();
        DocumentMaster documentMaster = documentRevision.getDocumentMaster();
        User author = documentIteration.getAuthor();
        Set<Tag> tags = documentRevision.getTags();
        List<InstanceAttribute> instanceAttributes = documentIteration.getInstanceAttributes();

        try {

            XContentBuilder tmp = XContentFactory.jsonBuilder().startObject();

            setField(tmp, WORKSPACE_ID_KEY, documentIteration.getWorkspaceId());
            setField(tmp, DOCUMENT_ID_KEY, documentRevision.getDocumentMasterId());
            setField(tmp, TITLE_KEY, documentIteration.getTitle());
            setField(tmp, VERSION_KEY, documentIteration.getVersion());
            setField(tmp, TYPE_KEY, documentMaster.getType());
            setField(tmp, DESCRIPTION_KEY, documentRevision.getDescription());
            setField(tmp, ITERATION_KEY, "" + documentIteration.getIteration());
            setField(tmp, CREATION_DATE_KEY, documentRevision.getCreationDate());
            setField(tmp, MODIFICATION_DATE_KEY, documentIteration.getModificationDate());
            setField(tmp, REVISION_NOTE_KEY, documentIteration.getRevisionNote());
            setField(tmp, WORKFLOW_KEY, documentRevision.getWorkflow());
            setField(tmp, FOLDER_KEY, documentRevision.getLocation().getShortName());

            if (author != null) {
                tmp.startObject(AUTHOR_KEY);
                setField(tmp, AUTHOR_LOGIN_KEY, author.getLogin());
                setField(tmp, AUTHOR_NAME_KEY, author.getName());
                tmp.endObject();
            }

            if (!tags.isEmpty()) {
                tmp.startArray(TAGS_KEY);
                for (Tag tag : tags) {
                    tmp.value(tag.getLabel());
                }
                tmp.endArray();
            }

            if (!instanceAttributes.isEmpty()) {
                tmp.startArray(ATTRIBUTES_KEY);
                for (InstanceAttribute attr : instanceAttributes) {
                    tmp.startObject();
                    setAttrField(tmp, attr);
                    tmp.endObject();
                }
                tmp.endArray();
            }

            if (!contentInputs.isEmpty()) {
                tmp.startArray(FILES_KEY);
                for (Map.Entry<String, String> contentInput : contentInputs.entrySet()) {
                    tmp.startObject();
                    setField(tmp, AUTHOR_NAME_KEY, contentInput.getKey());
                    setField(tmp, CONTENT_KEY, contentInput.getValue());
                    tmp.endObject();
                }
                tmp.endArray();
            }

            return tmp.endObject();
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "The document " + documentIteration + " can't be indexed.", e);
            return null;
        }
    }

    public static XContentBuilder partIterationToJSON(PartIteration partIteration, Map<String, String> contentInputs) {

        PartRevision partRevision = partIteration.getPartRevision();
        PartMaster partMaster = partRevision.getPartMaster();
        User author = partIteration.getAuthor();
        Set<Tag> tags = partRevision.getTags();
        List<InstanceAttribute> instanceAttributes = partIteration.getInstanceAttributes();

        try {
            XContentBuilder tmp = XContentFactory.jsonBuilder()
                    .startObject();

            setField(tmp, WORKSPACE_ID_KEY, partIteration.getWorkspaceId());
            setField(tmp, PART_NUMBER_KEY, partIteration.getPartNumber());
            setField(tmp, PART_NAME_KEY, partMaster.getName());
            setField(tmp, TYPE_KEY, partMaster.getType());
            setField(tmp, VERSION_KEY, partIteration.getPartVersion());
            setField(tmp, DESCRIPTION_KEY, partRevision.getDescription());
            setField(tmp, ITERATION_KEY, partIteration.getIteration());
            setField(tmp, STANDARD_PART_KEY, partMaster.isStandardPart());
            setField(tmp, CREATION_DATE_KEY, partIteration.getCreationDate());
            setField(tmp, MODIFICATION_DATE_KEY, partIteration.getModificationDate());
            setField(tmp, REVISION_NOTE_KEY, partIteration.getIterationNote());
            setField(tmp, WORKFLOW_KEY, partRevision.getWorkflow());

            if (author != null) {
                tmp.startObject(AUTHOR_KEY);
                setField(tmp, AUTHOR_LOGIN_KEY, author.getLogin());
                setField(tmp, AUTHOR_NAME_KEY, author.getName());
                tmp.endObject();
            }

            if (!tags.isEmpty()) {
                tmp.startArray(TAGS_KEY);
                for (Tag tag : tags) {
                    tmp.value(tag.getLabel());
                }
                tmp.endArray();
            }

            if (!instanceAttributes.isEmpty()) {
                tmp.startArray(ATTRIBUTES_KEY);
                for (InstanceAttribute attr : instanceAttributes) {
                    tmp.startObject();
                    setAttrField(tmp, attr);
                    tmp.endObject();
                }
                tmp.endArray();
            }

            if (!contentInputs.isEmpty()) {
                tmp.startArray(FILES_KEY);
                for (Map.Entry<String, String> contentInput : contentInputs.entrySet()) {
                    tmp.startObject();
                    setField(tmp, AUTHOR_NAME_KEY, contentInput.getKey());
                    setField(tmp, CONTENT_KEY, contentInput.getValue());
                    tmp.endObject();
                }
                tmp.endArray();
            }


            tmp.endArray();
            return tmp.endObject();

        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "The part " + partIteration.getKey() + " can't be indexed.", e);
            return null;
        }
    }

    private static XContentBuilder createMapping(String type) throws IOException {
        XContentBuilder tmp = XContentFactory.jsonBuilder().startObject();
        tmp.startObject(type);
        tmp = commonMapping(tmp);
        tmp.endObject();
        tmp.endObject();
        return tmp;
    }

    private static XContentBuilder commonMapping(XContentBuilder tmp) throws IOException {
        // todo potential break since mapping review
        tmp.startObject("properties");
//                .startObject(ITERATIONS_KEY)
//                .startObject("properties")
//                .startObject(ATTRIBUTES_KEY)
//                .field("type", "nested")
//                .startObject("properties");
        //map the attributes values as non analyzed, string will not be decomposed
        tmp.startObject(ATTRIBUTE_VALUE);
        tmp.field("type", "string");
        tmp.field("index", "not_analyzed");
//        tmp.endObject();
//        tmp.endObject();
//        tmp.endObject();
//        tmp.endObject();
        tmp.endObject();
        return tmp.endObject();

    }

    // FIXME : find out and document why we insert white space if value is null or empty
    private static XContentBuilder setField(XContentBuilder object, String field, String pValue) throws IOException {
        String value = (pValue != null && !pValue.isEmpty()) ? pValue : " ";
        return object.field(field, value);
    }

    private static void setAttrField(XContentBuilder object, InstanceAttribute attr) throws IOException {
        setField(object, ATTRIBUTE_NAME, attr.getNameWithoutWhiteSpace());
        if (attr instanceof InstanceListOfValuesAttribute) {
            InstanceListOfValuesAttribute lov = (InstanceListOfValuesAttribute) attr;
            String lovItemName = !lov.getItems().isEmpty() ? lov.getItems().get(lov.getIndexValue()).getName() : "";
            setField(object, ATTRIBUTE_VALUE, lovItemName);
        } else {
            setField(object, ATTRIBUTE_VALUE, "" + attr.getValue());
        }

    }

    private static void setAttrParam(Map<String, Object> params, InstanceAttribute attr) {
        setParam(params, ATTRIBUTE_NAME, attr.getNameWithoutWhiteSpace());
        if (attr instanceof InstanceListOfValuesAttribute) {
            InstanceListOfValuesAttribute lov = (InstanceListOfValuesAttribute) attr;
            String lovItemName = !lov.getItems().isEmpty() ? lov.getItems().get(lov.getIndexValue()).getName() : "";
            setParam(params, ATTRIBUTE_VALUE, lovItemName);
        } else {
            setParam(params, ATTRIBUTE_VALUE, "" + attr.getValue());
        }
    }

    private static void setParam(Map<String, Object> params, String name, Object value) {
        if (value != null) {
            params.put(name, new Object[]{value});
        }
    }

    private static void setParam(Map<String, Object> params, String name, Workflow value) {
        if (value != null) {
            String finalLifeCycleState = value.getFinalLifeCycleState();
            finalLifeCycleState = (finalLifeCycleState != null && !finalLifeCycleState.isEmpty()) ? finalLifeCycleState : " ";
            params.put(name, new Object[]{finalLifeCycleState});
        }
    }

    private static XContentBuilder setField(XContentBuilder object, String field, int value) throws IOException {
        object.field(field, "" + value);
        return object;
    }

    private static XContentBuilder setField(XContentBuilder object, String field, Workflow value) throws IOException {
        if (value != null) {
            String finalLifeCycleState = value.getFinalLifeCycleState();
            finalLifeCycleState = (finalLifeCycleState != null && !finalLifeCycleState.isEmpty()) ? finalLifeCycleState : " ";
            return object.field(field, "" + finalLifeCycleState);
        }
        return null;
    }

    private static XContentBuilder setField(XContentBuilder object, String field, Object value) throws IOException {
        if (value != null) {
            return object.field(field, value);
        }
        return null;
    }
}
