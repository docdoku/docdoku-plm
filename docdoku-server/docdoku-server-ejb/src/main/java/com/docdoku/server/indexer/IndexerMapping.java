package com.docdoku.server.indexer;

import com.docdoku.core.common.User;
import com.docdoku.core.document.DocumentIteration;
import com.docdoku.core.document.DocumentIterationKey;
import com.docdoku.core.document.DocumentMaster;
import com.docdoku.core.document.DocumentRevision;
import com.docdoku.core.meta.InstanceAttribute;
import com.docdoku.core.meta.InstanceListOfValuesAttribute;
import com.docdoku.core.meta.Tag;
import com.docdoku.core.product.PartIteration;
import com.docdoku.core.product.PartIterationKey;
import com.docdoku.core.product.PartMaster;
import com.docdoku.core.product.PartRevision;
import com.docdoku.core.workflow.Workflow;
import org.apache.commons.io.IOUtils;
import org.elasticsearch.common.xcontent.XContentBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 * @author Morgan Guimard
 */
public class IndexerMapping {

    protected static final String WORKSPACE_ID_KEY = "workspaceId";
    protected static final String ITERATION_KEY = "iteration";
    protected static final String VERSION_KEY = "version";
    protected static final String AUTHOR_LOGIN_KEY = "authorLogin";
    protected static final String AUTHOR_NAME_KEY = "authorName";
    protected static final String FILE_NAME_KEY = "fileName";
    protected static final String CREATION_DATE_KEY = "creationDate";
    protected static final String MODIFICATION_DATE_KEY = "modificationDate";
    protected static final String TYPE_KEY = "type";
    protected static final String DOCUMENT_ID_KEY = "docMId";
    protected static final String PART_NUMBER_KEY = "partNumber";
    protected static final String PART_NAME_KEY = "partName";
    protected static final String TITLE_KEY = "title";
    protected static final String DESCRIPTION_KEY = "description";
    protected static final String REVISION_NOTE_KEY = "revisionNote";
    protected static final String WORKFLOW_KEY = "workflow";
    protected static final String FOLDER_KEY = "folder";
    protected static final String TAGS_KEY = "tags";
    protected static final String ATTRIBUTES_KEY = "attributes";
    protected static final String ATTRIBUTE_NAME = "attr_name";
    protected static final String ATTRIBUTE_VALUE = "attr_value";
    protected static final String FILES_KEY = "files";
    protected static final String CONTENT_KEY = "content";
    protected static final String STANDARD_PART_KEY = "standardPart";
    protected static final String PART_TYPE = "part";
    protected static final String DOCUMENT_TYPE = "document";
    protected static final String DEFAULT_TYPE = "_default_";
    protected static final String ALL_FIELDS = "_all";

    private static final Logger LOGGER = Logger.getLogger(IndexerMapping.class.getName());
    private static final String DOCUMENT_MAPPING_RESOURCE = "/com/docdoku/server/indexer/document-mapping.json";
    private static final String PART_MAPPING_RESOURCE = "/com/docdoku/server/indexer/part-mapping.json";
    private static final String SOURCE_MAPPING_RESOURCE = "/com/docdoku/server/indexer/source-mapping.json";

    private IndexerMapping() {
    }

    public static String createDocumentIterationMapping() throws IOException {
        return loadContentBuilderFromResource(DOCUMENT_MAPPING_RESOURCE);
    }

    public static String createPartIterationMapping() throws IOException {
        return loadContentBuilderFromResource(PART_MAPPING_RESOURCE);
    }

    public static String createSourceMapping() throws IOException {
        return loadContentBuilderFromResource(SOURCE_MAPPING_RESOURCE);
    }

    public static void documentIterationToJSON(XContentBuilder xcb, DocumentIteration documentIteration, Map<String, String> contentInputs) throws IOException {

        DocumentRevision documentRevision = documentIteration.getDocumentRevision();
        DocumentMaster documentMaster = documentRevision.getDocumentMaster();
        User author = documentMaster.getAuthor();
        Set<Tag> tags = documentRevision.getTags();
        List<InstanceAttribute> instanceAttributes = documentIteration.getInstanceAttributes();
        setField(xcb, WORKSPACE_ID_KEY, documentIteration.getWorkspaceId());
        setField(xcb, DOCUMENT_ID_KEY, documentRevision.getDocumentMasterId());
        setField(xcb, TITLE_KEY, documentIteration.getTitle());
        setField(xcb, VERSION_KEY, documentIteration.getVersion());
        setField(xcb, TYPE_KEY, documentMaster.getType());
        setField(xcb, DESCRIPTION_KEY, documentRevision.getDescription());
        setField(xcb, ITERATION_KEY, documentIteration.getIteration());
        setField(xcb, CREATION_DATE_KEY, documentRevision.getCreationDate());
        setField(xcb, MODIFICATION_DATE_KEY, documentIteration.getModificationDate());
        setField(xcb, REVISION_NOTE_KEY, documentIteration.getRevisionNote());
        setField(xcb, WORKFLOW_KEY, documentRevision.getWorkflow());
        setField(xcb, FOLDER_KEY, documentRevision.getLocation().getShortName());

        addCommonJSONFields(xcb, author, tags, instanceAttributes, contentInputs);

    }

    public static void partIterationToJSON(XContentBuilder xcb, PartIteration partIteration, Map<String, String> contentInputs) throws IOException {

        PartRevision partRevision = partIteration.getPartRevision();
        PartMaster partMaster = partRevision.getPartMaster();
        User author = partMaster.getAuthor();
        Set<Tag> tags = partRevision.getTags();
        List<InstanceAttribute> instanceAttributes = partIteration.getInstanceAttributes();
        setField(xcb, WORKSPACE_ID_KEY, partIteration.getWorkspaceId());
        setField(xcb, PART_NUMBER_KEY, partIteration.getPartNumber());
        setField(xcb, PART_NAME_KEY, partMaster.getName());
        setField(xcb, TYPE_KEY, partMaster.getType());
        setField(xcb, VERSION_KEY, partIteration.getPartVersion());
        setField(xcb, DESCRIPTION_KEY, partRevision.getDescription());
        setField(xcb, ITERATION_KEY, partIteration.getIteration());
        setField(xcb, STANDARD_PART_KEY, partMaster.isStandardPart());
        setField(xcb, CREATION_DATE_KEY, partIteration.getCreationDate());
        setField(xcb, MODIFICATION_DATE_KEY, partIteration.getModificationDate());
        setField(xcb, REVISION_NOTE_KEY, partIteration.getIterationNote());
        setField(xcb, WORKFLOW_KEY, partRevision.getWorkflow());

        addCommonJSONFields(xcb, author, tags, instanceAttributes, contentInputs);
    }

    private static void addCommonJSONFields(XContentBuilder tmp, User author, Set<Tag> tags, List<InstanceAttribute> instanceAttributes, Map<String, String> contentInputs) throws IOException {

        setField(tmp, AUTHOR_LOGIN_KEY, author.getLogin());
        setField(tmp, AUTHOR_NAME_KEY, author.getName());

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
                setField(tmp, FILE_NAME_KEY, contentInput.getKey());
                setField(tmp, CONTENT_KEY, contentInput.getValue());
                tmp.endObject();
            }
            tmp.endArray();
        }
    }

    // FIXME : find out and document why we insert white space if value is null or empty
    private static XContentBuilder setField(XContentBuilder object, String field, String pValue) throws IOException {
        String value = (pValue != null && !pValue.isEmpty()) ? pValue : " ";
        return object.field(field, value);
    }

    private static void setAttrField(XContentBuilder object, InstanceAttribute attr) throws IOException {

        setField(object, ATTRIBUTE_NAME, attr.getNameWithoutWhiteSpace());

        Object value;

        if (attr instanceof InstanceListOfValuesAttribute) {
            InstanceListOfValuesAttribute lov = (InstanceListOfValuesAttribute) attr;
            value = !lov.getItems().isEmpty() ? lov.getItems().get(lov.getIndexValue()).getName() : "";
        } else {
            value = attr.getValue();
        }

        setField(object, ATTRIBUTE_VALUE, "" + value);

    }

    private static XContentBuilder setField(XContentBuilder object, String field, int value) throws IOException {
        object.field(field, value);
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

    public static DocumentIterationKey getDocumentIterationKey(Map<?, ?> source) {
        //Jest returns the value as double...
        int iteration=Double.valueOf(extractValue(source, ITERATION_KEY)).intValue();

        return new DocumentIterationKey(extractValue(source, WORKSPACE_ID_KEY),
                extractValue(source, DOCUMENT_ID_KEY),
                extractValue(source, VERSION_KEY),
                iteration
        );
    }

    public static PartIterationKey getPartIterationKey(Map<?, ?> source) {
        //Jest returns the value as double...
        int iteration=Double.valueOf(extractValue(source, ITERATION_KEY)).intValue();

        return new PartIterationKey(extractValue(source, WORKSPACE_ID_KEY),
                extractValue(source, PART_NUMBER_KEY),
                extractValue(source, VERSION_KEY),
                iteration
        );
    }

    private static String extractValue(Map<?, ?> source, String key) {
        Object ret = source.get(key);
        if (ret instanceof List) {
            return ((List) ret).get(0).toString();
        } else {
            return ret.toString();
        }
    }

    private static String loadContentBuilderFromResource(String resourceLocation) throws IOException {
        try (InputStream is = IndexerMapping.class.getResourceAsStream(resourceLocation)) {
            return IOUtils.toString(is, "UTF-8");
        }
    }

}
