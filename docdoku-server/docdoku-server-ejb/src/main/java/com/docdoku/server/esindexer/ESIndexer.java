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

import com.docdoku.core.common.Account;
import com.docdoku.core.common.BinaryResource;
import com.docdoku.core.common.Workspace;
import com.docdoku.core.document.DocumentIteration;
import com.docdoku.core.document.DocumentMaster;
import com.docdoku.core.document.DocumentRevision;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.product.PartIteration;
import com.docdoku.core.product.PartMaster;
import com.docdoku.core.product.PartRevision;
import com.docdoku.core.services.IAccountManagerLocal;
import com.docdoku.core.services.IDataManagerLocal;
import com.docdoku.core.services.IMailerLocal;
import com.docdoku.server.dao.DocumentMasterDAO;
import com.docdoku.server.dao.PartMasterDAO;
import com.docdoku.server.dao.WorkspaceDAO;
import org.elasticsearch.ElasticsearchIllegalArgumentException;
import org.elasticsearch.action.ActionRequestValidationException;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequestBuilder;
import org.elasticsearch.action.update.UpdateRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.NoNodeAvailableException;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.indices.IndexAlreadyExistsException;
import org.elasticsearch.indices.InvalidIndexNameException;

import javax.annotation.Resource;
import javax.ejb.Asynchronous;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Index Method using ElasticSearch API.
 *
 * @author Taylor LABEJOF
 */
@Stateless(name = "ESIndexer")
public class ESIndexer {

    @PersistenceContext
    private EntityManager em;

    @Resource
    private SessionContext ctx;

    @Inject
    private IDataManagerLocal dataManager;

    @Inject
    private IAccountManagerLocal accountManager;

    @Inject
    private IMailerLocal mailer;

    @Inject
    private Client client;

    @Inject
    private ESConfigManager esConfigManager;

    private static final String I18N_CONF = "com.docdoku.core.i18n.LocalStrings";
    private static final Logger LOGGER = Logger.getLogger(ESIndexer.class.getName());

    public ESIndexer() {
        super();
    }

    /**
     * Create a new index in ElasticSearch
     *
     * @param workspaceId The name of the new index. It should be a workspace name.
     * @throws ESServerException             If a problem occur with the ElasticSearch server.
     * @throws ESIndexAlreadyExistsException If the index already exist.
     * @throws ESIndexNamingException        If the name doesn't suit for indexing.
     */
    public void createIndex(String workspaceId) throws ESServerException, ESIndexAlreadyExistsException, ESIndexNamingException {
        try {
            generateIndex(ESTools.formatIndexName(workspaceId));
        } catch (NoNodeAvailableException e) {
            LOGGER.log(Level.WARNING, "Cannot create index for : " + workspaceId + " : : The ElasticSearch server doesn't seem to respond");
        }
    }

    /**
     * Delete an index in ElasticSearch
     *
     * @param workspaceId The name of the index to delete.
     */
    public void deleteIndex(String workspaceId) {
        try {
            client.admin().indices().prepareDelete(ESTools.formatIndexName(workspaceId))
                    .execute().actionGet();
        } catch (NoNodeAvailableException e) {
            LOGGER.log(Level.WARNING, "Cannot delete index : The ElasticSearch server doesn't seem to respond");
        }
    }

    /**
     * Index all content in all workspace
     */
    @Asynchronous
    public void indexAll() {
        try {
            BulkRequestBuilder bulkRequest = client.prepareBulk();
            WorkspaceDAO wDAO = new WorkspaceDAO(em);

            for (Workspace w : wDAO.getAll()) {
                bulkRequest = bulkWorkspaceRequestBuilder(bulkRequest, w.getId(), true);
            }

            BulkResponse bulkResponse = bulkRequest.execute().actionGet();

            if (bulkResponse.hasFailures()) {
                LOGGER.log(Level.SEVERE, "Failures while bulk indexing: " + " \n " + bulkResponse.buildFailureMessage());
            }

        } catch (NoNodeAvailableException e) {
            LOGGER.log(Level.WARNING, "Cannot index all: The ElasticSearch server doesn't seem to respond");
        } catch (ESIndexNamingException e) {
            LOGGER.log(Level.SEVERE, "Cannot index all: NamingException", e);
        }
    }

    /**
     * Index all resources in this workspace
     *
     * @param workspaceId Workspace to index
     */
    @Asynchronous
    public void indexWorkspace(String workspaceId) {
        String failureMessage = "";
        boolean hasSuccess = true;

        try {

            BulkRequestBuilder bulkRequest = client.prepareBulk();
            bulkRequest = bulkWorkspaceRequestBuilder(bulkRequest, workspaceId, false);
            BulkResponse bulkResponse = bulkRequest.execute().actionGet();

            if (bulkResponse.hasFailures()) {
                hasSuccess = false;
                failureMessage = bulkResponse.buildFailureMessage();
            }

        } catch (ActionRequestValidationException e) {
            hasSuccess = false;
            failureMessage = getString("ES_IndexError4");
        } catch (ESIndexNamingException e) {
            hasSuccess = false;
            failureMessage = getString("ES_IndexError2") + " " + workspaceId;
        } catch (NoNodeAvailableException e) {
            hasSuccess = false;
            failureMessage = getString("ES_IndexError2");
        }

        if (!hasSuccess) {
            LOGGER.log(Level.WARNING, "Multiple indexing meet some problem:" + " \n " + failureMessage);
        } else {
            LOGGER.log(Level.INFO, "The workspace " + workspaceId + " has been indexed");
        }

        sendNotification(workspaceId, hasSuccess, failureMessage);
    }

    /**
     * Index multiple DocumentIteration entities in ElasticSearch Cluster
     *
     * @param docs The List of document iteration to index
     */
    @Asynchronous
    public void indexMultiple(List<DocumentIteration> docs) {
        for (DocumentIteration documentIteration : docs) {
            indexDocument(documentIteration);
        }
    }

    /**
     * Index a documentIteration in ElasticSearch Cluster
     *
     * @param doc The document iteration to index
     */
    @Asynchronous
    public void index(DocumentIteration doc) {
        indexDocument(doc);
    }

    private void indexDocument(DocumentIteration documentIteration) {
        try {
            tryCreateIndex(ESTools.formatIndexName(documentIteration.getWorkspaceId()));
            indexRequest(documentIteration).execute()
                    .actionGet();
        } catch (NoNodeAvailableException e) {
            LOGGER.log(Level.WARNING, "Cannot index document " + documentIteration + ": The ElasticSearch server doesn't seem to respond");
        } catch (ESIndexNamingException e) {
            LOGGER.log(Level.SEVERE, "Cannot index document " + documentIteration + ": NamingException", e);
        } catch (ElasticsearchIllegalArgumentException e) {
            LOGGER.log(Level.SEVERE, "Cannot index document " + documentIteration + ": Illegal arguments", e);
        }
    }

    /**
     * Index a partIteration in ElasticSearch Cluster
     *
     * @param part The part iteration to index
     */
    @Asynchronous
    public void index(PartIteration part) {
        String workspaceId = part.getWorkspaceId();
        try {
            tryCreateIndex(ESTools.formatIndexName(workspaceId));
            indexRequest(part).execute()
                    .actionGet();
        } catch (NoNodeAvailableException e) {
            LOGGER.log(Level.WARNING, "Cannot index part " + part + ": The ElasticSearch server doesn't seem to respond");
        } catch (ESIndexNamingException e) {
            LOGGER.log(Level.SEVERE, "Cannot index part " + part + ": NamingException", e);
        } catch (ElasticsearchIllegalArgumentException e) {
            LOGGER.log(Level.SEVERE, "Cannot index part " + part + ": Illegal arguments", e);
        }
    }

    /**
     * Remove this docIteration from ElasticSearch Cluster
     *
     * @param doc The document iteration to remove from index
     */
    @Asynchronous
    public void delete(DocumentIteration doc) {
        try {
            deleteRequest(doc).execute()
                    .actionGet();
        } catch (NoNodeAvailableException e) {
            LOGGER.log(Level.WARNING, "Cannot delete document " + doc + ": The ElasticSearch server doesn't seem to respond");
        }
    }

    /**
     * Remove this partIteration from ElasticSearch Cluster
     *
     * @param part The part iteration to remove from index
     */
    @Asynchronous
    public void delete(PartIteration part) {
        try {
            deleteRequest(part).execute()
                    .actionGet();
        } catch (NoNodeAvailableException e) {
            LOGGER.log(Level.WARNING, "Cannot delete part " + part + ": The ElasticSearch server doesn't seem to respond");
        }
    }

    /**
     * Delete index for all resources in this workspace
     *
     * @param workspaceId Workspace to delete
     */
    @Asynchronous
    public void deleteWorkspace(String workspaceId) {
        String failureMessage = "";
        boolean hasSuccess = true;

        try {
            deleteIndex(workspaceId);
        } catch (NoNodeAvailableException e) {
            hasSuccess = false;
            failureMessage = getString("ES_DeleteError2");
            LOGGER.log(Level.WARNING, "Cannot delete index " + workspaceId + ": The ElasticSearch server doesn't seem to respond");
        }

        sendNotification(workspaceId, hasSuccess, failureMessage);
    }

    private void generateIndex(String pIndex) throws ESIndexAlreadyExistsException, ESIndexNamingException {
        try {
            client.admin().indices().prepareCreate(pIndex)
                    .setSettings(ImmutableSettings.settingsBuilder()
                            .put("number_of_shards", esConfigManager.getNumberOfShards())
                            .put("number_of_replicas", esConfigManager.getNumberOfReplicas())
                            .put("auto_expand_replicas", esConfigManager.getAutoExpandReplicas()))
                    .addMapping(ESMapper.PART_TYPE, partMapping())
                    .addMapping(ESMapper.DOCUMENT_TYPE, docMapping())
                    .setSource(defaultMapping())
                    .execute().actionGet();
        } catch (IndexAlreadyExistsException e) {
            LOGGER.log(Level.SEVERE, "Cannot create index: " + pIndex + " already exists");
            throw new ESIndexAlreadyExistsException(Locale.getDefault());
        } catch (InvalidIndexNameException e) {
            LOGGER.log(Level.SEVERE, "Cannot create index: " + pIndex + " invalid index name");
            throw new ESIndexNamingException(Locale.getDefault());
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error on mapping creation " + pIndex, e);
        }
    }

    private XContentBuilder partMapping() throws IOException {
        XContentBuilder tmp = XContentFactory.jsonBuilder().startObject();
        tmp.startObject(ESMapper.PART_TYPE);
        tmp = commonMapping(tmp);
        tmp.endObject();
        tmp.endObject();
        return tmp;
    }

    private XContentBuilder defaultMapping() throws IOException {
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
        tmp.field("match", ESMapper.CONTENT_KEY);
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

    private XContentBuilder docMapping() throws IOException {
        XContentBuilder tmp = XContentFactory.jsonBuilder().startObject();
        tmp.startObject(ESMapper.DOCUMENT_TYPE);
        tmp = commonMapping(tmp);
        tmp.endObject();
        tmp.endObject();
        return tmp;
    }

    private XContentBuilder commonMapping(XContentBuilder tmp) throws IOException {
        tmp.startObject("properties")
                .startObject(ESMapper.ITERATIONS_KEY)
                .startObject("properties")
                .startObject(ESMapper.ATTRIBUTES_KEY)
                .field("type", "nested")
                .startObject("properties");
        //map the attributes values as non analyzed, string will not be decomposed
        tmp.startObject(ESMapper.ATTRIBUTE_VALUE);
        tmp.field("type", "string");
        tmp.field("index", "not_analyzed");
        tmp.endObject();
        tmp.endObject();
        tmp.endObject();
        tmp.endObject();
        tmp.endObject();
        tmp.endObject();

        return tmp;
    }

    private void tryCreateIndex(String pIndex) throws ESIndexNamingException {
        try {
            generateIndex(pIndex);
        } catch (ESIndexAlreadyExistsException e) {
            LOGGER.log(Level.FINEST, null, e);
        }
    }

    private void sendNotification(String workspaceId, boolean hasSuccess, String failureMessage) {
        try {
            String login = ctx.getCallerPrincipal().getName();
            Account account = accountManager.getAccount(login);
            mailer.sendIndexerResult(account, workspaceId, hasSuccess, failureMessage);
        } catch (AccountNotFoundException e) {
            String logMessage = getString("ES_MailError1");
            LOGGER.log(Level.SEVERE, logMessage, e);
        }
    }

    private BulkRequestBuilder bulkWorkspaceRequestBuilder(BulkRequestBuilder pBulkRequest, String workspaceId, boolean silent) throws ESIndexNamingException {
        BulkRequestBuilder bulkRequest = pBulkRequest;
        try {
            generateIndex(ESTools.formatIndexName(workspaceId));
        } catch (ESIndexAlreadyExistsException e) {
            LOGGER.log(Level.WARNING, "Cannot generate index: " + workspaceId + " already exists");
        } catch (ESIndexNamingException e) {
            LOGGER.log(Level.SEVERE, null, e);
            if (!silent) {
                throw e;
            }
        }

        bulkRequest = bulkDocumentsIndexRequestBuilder(bulkRequest, workspaceId);
        bulkRequest = bulkPartsIndexRequestBuilder(bulkRequest, workspaceId);

        return bulkRequest;
    }

    private BulkRequestBuilder bulkDocumentsIndexRequestBuilder(BulkRequestBuilder pBulkRequest, String workspaceId) {
        DocumentMasterDAO docMasterDAO = new DocumentMasterDAO(em);
        for (DocumentMaster docM : docMasterDAO.getAllByWorkspace(workspaceId)) {
            for (DocumentRevision docR : docM.getDocumentRevisions()) {
                pBulkRequest.add(indexRequest(docR.getLastIteration()));
            }
        }
        return pBulkRequest;
    }

    private BulkRequestBuilder bulkPartsIndexRequestBuilder(BulkRequestBuilder pBulkRequest, String workspaceId) {
        PartMasterDAO partMasterDAO = new PartMasterDAO(em);
        for (PartMaster partMaster : partMasterDAO.getAllByWorkspace(workspaceId)) {
            for (PartRevision partRev : partMaster.getPartRevisions()) {
                pBulkRequest.add(indexRequest(partRev.getLastIteration()));
            }
        }
        return pBulkRequest;
    }


    /**
     * Get the Index request for a documentIteration in ElasticSearch Cluster
     *
     * @param doc The document iteration to index
     */
    private UpdateRequestBuilder indexRequest(DocumentIteration doc) throws NoNodeAvailableException {
        Map<String, String> binaryList = new HashMap<>();
        for (BinaryResource bin : doc.getAttachedFiles()) {
            try (InputStream in = dataManager.getBinaryResourceInputStream(bin)) {
                binaryList.put(bin.getName(), ESTools.streamToString(bin.getFullName(), in));
            } catch (StorageException | IOException e) {
                LOGGER.log(Level.SEVERE, "Cannot read file " + bin.getFullName(), e);
            }
        }
        XContentBuilder jsonDoc = ESMapper.documentRevisionToJSON(doc, binaryList);
        Map<String, Object> params = ESMapper.docIterationMap(doc, binaryList);
        return client.prepareUpdate(ESTools.formatIndexName(doc.getWorkspaceId()), ESMapper.DOCUMENT_TYPE, doc.getDocumentRevisionKey().toString())
                .setScript("ctx._source.iterations += iteration")
                .addScriptParam("iteration", params)
                .setUpsert(jsonDoc);
    }

    /**
     * Get the Index request for a partIteration in ElasticSearch Cluster
     *
     * @param part The part iteration to index
     */
    private UpdateRequestBuilder indexRequest(PartIteration part) {
        Map<String, String> binaryList = new HashMap<>();
        for (BinaryResource bin : part.getAttachedFiles()) {
            try (InputStream in = dataManager.getBinaryResourceInputStream(bin)) {
                binaryList.put(bin.getName(), ESTools.streamToString(bin.getFullName(), in));
            } catch (StorageException | IOException e) {
                LOGGER.log(Level.SEVERE, "Cannot read file " + bin.getFullName(), e);
            }
        }
        XContentBuilder json = ESMapper.partRevisionToJson(part, binaryList);
        Map<String, Object> params = ESMapper.partIterationMap(part, binaryList);
        return client
                .prepareUpdate(ESTools.formatIndexName(part.getWorkspaceId()),
                        ESMapper.PART_TYPE, part.getPartRevisionKey().toString())
                .setScript("ctx._source.iterations += iteration")
                .addScriptParam("iteration", params)
                .setUpsert(json);
    }

    /**
     * Get the Delete request for a documentIteration in ElasticSearch Cluster
     *
     * @param doc The document iteration to delete
     */
    private DeleteRequestBuilder deleteRequest(DocumentIteration doc) throws NoNodeAvailableException {
        return client.prepareDelete(ESTools.formatIndexName(doc.getWorkspaceId()), ESMapper.DOCUMENT_TYPE, doc.getKey().toString());
    }

    /**
     * Get the Delete request for a partIteration in ElasticSearch Cluster
     *
     * @param part The part iteration to delete
     */
    private DeleteRequestBuilder deleteRequest(PartIteration part) throws NoNodeAvailableException {
        return client.prepareDelete(ESTools.formatIndexName(part.getWorkspaceId()), ESMapper.PART_TYPE, part.getKey().toString());
    }

    private String getString(String key) {
        return ResourceBundle.getBundle(I18N_CONF, Locale.getDefault()).getString(key);
    }

}