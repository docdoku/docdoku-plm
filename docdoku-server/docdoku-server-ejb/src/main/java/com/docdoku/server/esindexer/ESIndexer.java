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
import javax.ejb.EJB;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
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
    private static final String CONF_PROPERTIES = "/com/docdoku/server/esindexer/conf.properties";
    private static final Properties CONF = new Properties();
    private static final String I18N_CONF = "com.docdoku.core.i18n.LocalStrings";
    private static final Logger LOGGER = Logger.getLogger(ESIndexer.class.getName());

    private static final String ES_INDEX_FAIL = " indexing failed.\n Cause by : ";
    private static final String ES_INDEX_ERROR_1 = "ES_IndexError1";
    private static final String ES_INDEX_ERROR_2 = "ES_IndexError2";
    private static final String ES_INDEX_ERROR_3 = "ES_IndexError3";
    private static final String ES_INDEX_CREATION_ERROR_2 = "ES_IndexCreationError2";
    private static final String ES_DELETE_ERROR_1 = "ES_DeleteError1";

    static {
        try (InputStream inputStream  = ESIndexer.class.getResourceAsStream(CONF_PROPERTIES)) {
            CONF.load(inputStream);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, null, e);
        }
    }

    @PersistenceContext
    private EntityManager em;

    @EJB
    private IDataManagerLocal dataManager;

    @Resource
    private SessionContext ctx;

    @EJB
    private IAccountManagerLocal accountManager;

    @EJB
    private IMailerLocal mailer;

    /**
     * Constructor
     */
    public ESIndexer() {
        super();
    }

    private void createIndex(String pIndex, Client pClient) throws ESIndexAlreadyExistsException, ESIndexNamingException {
        try {
            pClient.admin().indices().prepareCreate(pIndex)
                    .setSettings(ImmutableSettings.settingsBuilder()
                            .put("number_of_shards", CONF.getProperty("number_of_shards"))
                            .put("number_of_replicas", CONF.getProperty("number_of_replicas"))
                            .put("auto_expand_replicas", CONF.getProperty("auto_expand_replicas")))
                    .addMapping(ESMapper.PART_TYPE,this.partMapping())
                    .addMapping(ESMapper.DOCUMENT_TYPE, this.docMapping())
                    .setSource(defaultMapping())
                    .execute().actionGet();
        } catch (IndexAlreadyExistsException e) {
            String logMessage = ResourceBundle.getBundle(I18N_CONF, Locale.getDefault()).getString("ES_IndexCreationError1");
            LOGGER.log(Level.FINEST, logMessage + " " + pIndex, e);
            throw new ESIndexAlreadyExistsException(Locale.getDefault());
        } catch (InvalidIndexNameException e) {
            String logMessage = ResourceBundle.getBundle(I18N_CONF, Locale.getDefault()).getString(ES_INDEX_CREATION_ERROR_2);
            LOGGER.log(Level.INFO, logMessage + " " + pIndex, e);
            throw new ESIndexNamingException(Locale.getDefault());
        } catch(IOException e) {
            LOGGER.log(Level.ALL,"Error on mapping creation" + pIndex,e);
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
                tmp.field("enabled","true");
            tmp.endObject();
            tmp.startArray("dynamic_templates");
                tmp.startObject();
                    //All field with the name content should be analyzed for full text search
                    tmp.startObject("content_string");
                        tmp.field("match",ESMapper.CONTENT_KEY);
                        tmp.field("match_mapping_type","string");
                        tmp.startObject("mapping");
                            tmp.field("type","string");
                            tmp.field("index","analyzed");
                        tmp.endObject();
                    tmp.endObject();
                tmp.endObject();
                tmp.startObject();
                    //set by default all the field as not_analyzed.
                    // data won't be flatten, term filter/query will be possible.
                    tmp.startObject("default_string");
                        tmp.field("match","*");
                        tmp.field("match_mapping_type","string");
                        tmp.startObject("mapping");
                            tmp.field("type","string");
                            tmp.field("index","not_analyzed");
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
                .field("type","nested")
                .startObject("properties");
        //map the attributes values as non analyzed, string will not be decomposed
        tmp.startObject(ESMapper.ATTRIBUTE_VALUE);
        tmp.field("type","string");
        tmp.field("index", "not_analyzed");
        tmp.endObject();
        tmp.endObject();
        tmp.endObject();
        tmp.endObject();
        tmp.endObject();
        tmp.endObject();

        return tmp;
    }

    private void tryCreateIndex(String pIndex, Client pClient) throws ESIndexNamingException {
        try {
            createIndex(pIndex, pClient);
        } catch (ESIndexAlreadyExistsException e) {
            LOGGER.log(Level.FINEST, null, e);
        }
    }

    private void deleteIndex(String pIndex, Client pClient) {
        pClient.admin().indices().prepareDelete(pIndex)
                .execute().actionGet();
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
        Client client = null;
        try {
            client = ESTools.createClient();
            createIndex(ESTools.formatIndexName(workspaceId), client);
        } catch (NoNodeAvailableException e) {
            LOGGER.log(Level.WARNING, "Error on creating: " + workspaceId + " index");
            LOGGER.log(Level.FINER, null, e);
        } finally {
            ESTools.closeClient(client);
        }
    }

    /**
     * Delete a index in ElasticSearch
     *
     * @param workspaceId The name of the index to delete.
     */
    public void deleteIndex(String workspaceId) {
        Client client = null;
        try {
            client = ESTools.createClient();
            deleteIndex(ESTools.formatIndexName(workspaceId), client);
        } catch (NoNodeAvailableException | ESServerException e) {
            LOGGER.log(Level.WARNING, "Error on deleting: " + workspaceId + " index", e);
        } finally {
            ESTools.closeClient(client);
        }
    }

    /**
     * Index all content in all workspace
     */
    @Asynchronous
    public void indexAll() {
        Client client = null;
        try {
            client = ESTools.createClient();
            BulkRequestBuilder bulkRequest = client.prepareBulk();
            WorkspaceDAO wDAO = new WorkspaceDAO(em);

            for (Workspace w : wDAO.getAll()) {
                bulkRequest = bulkWorkspaceRequestBuilder(client, bulkRequest, w.getId(), true);
            }

            BulkResponse bulkResponse = bulkRequest.execute().actionGet();

            if (bulkResponse.hasFailures()) {
                String logMessage = ResourceBundle.getBundle(I18N_CONF, Locale.getDefault()).getString(ES_INDEX_ERROR_3);
                LOGGER.log(Level.WARNING, logMessage + " \n " + bulkResponse.buildFailureMessage());
            }
        } catch (ESServerException | NoNodeAvailableException | ESIndexNamingException e) {
            String logMessage = ResourceBundle.getBundle(I18N_CONF, Locale.getDefault()).getString(ES_INDEX_ERROR_2);
            LOGGER.log(Level.WARNING, logMessage, e);
        } finally {
            ESTools.closeClient(client);
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
        Client client = null;

        try {
            client = ESTools.createClient();
            BulkRequestBuilder bulkRequest = client.prepareBulk();
            bulkRequest = bulkWorkspaceRequestBuilder(client, bulkRequest, workspaceId, false);
            BulkResponse bulkResponse = bulkRequest.execute().actionGet();

            if (bulkResponse.hasFailures()) {
                hasSuccess = false;
                failureMessage = bulkResponse.buildFailureMessage();
            }
        } catch (ActionRequestValidationException e) {
            hasSuccess = false;
            failureMessage = ResourceBundle.getBundle(I18N_CONF, Locale.getDefault()).getString("ES_IndexError4");
        } catch (ESIndexNamingException e) {
            hasSuccess = false;
            failureMessage = ResourceBundle.getBundle(I18N_CONF, Locale.getDefault()).getString(ES_INDEX_CREATION_ERROR_2) + " " + workspaceId;
        } catch (ESServerException | NoNodeAvailableException e) {
            hasSuccess = false;
            failureMessage = ResourceBundle.getBundle(I18N_CONF, Locale.getDefault()).getString(ES_INDEX_ERROR_2);
        } finally {
            ESTools.closeClient(client);
        }

        if (!hasSuccess) {
            String logMessage = ResourceBundle.getBundle(I18N_CONF, Locale.getDefault()).getString(ES_INDEX_ERROR_3);
            LOGGER.log(Level.WARNING, logMessage + " \n " + failureMessage);
        } else {
            LOGGER.log(Level.INFO, "The workspace " + workspaceId + " has been indexed");
        }

        try {
            String login = ctx.getCallerPrincipal().getName();
            Account account = accountManager.getAccount(login);
            mailer.sendIndexerResult(account, workspaceId, hasSuccess, failureMessage);
        } catch (AccountNotFoundException e) {
            String logMessage = ResourceBundle.getBundle(I18N_CONF, Locale.getDefault()).getString("ES_MailError1");
            LOGGER.log(Level.WARNING, logMessage, e);
        }
    }

    /**
     * Index a documentIteration in ElasticSearch Cluster
     *
     * @param doc The document iteration to index
     */
    @Asynchronous
    public void index(DocumentIteration doc) {
        String workspaceId = doc.getWorkspaceId();
        Client client = null;
        try {
            client = ESTools.createClient();
            tryCreateIndex(ESTools.formatIndexName(workspaceId), client);
            indexRequest(client, doc).execute()
                    .actionGet();
        } catch (NoNodeAvailableException | ESServerException e) {
            String logMessage = ResourceBundle.getBundle(I18N_CONF, Locale.getDefault()).getString(ES_INDEX_ERROR_1);
            LOGGER.log(Level.WARNING, doc + ES_INDEX_FAIL + logMessage, e);
        } catch (ESIndexNamingException e) {
            String logMessage = ResourceBundle.getBundle(I18N_CONF, Locale.getDefault()).getString(ES_INDEX_CREATION_ERROR_2);
            LOGGER.log(Level.WARNING, doc + ES_INDEX_FAIL + logMessage + " " + workspaceId, e);
        } catch (ElasticsearchIllegalArgumentException e){
            LOGGER.log(Level.SEVERE, null, e);
        } finally {
            ESTools.closeClient(client);
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
        Client client = null;
        try {
            client = ESTools.createClient();
            tryCreateIndex(ESTools.formatIndexName(workspaceId), client);
            indexRequest(client, part).execute()
                    .actionGet();
        } catch (NoNodeAvailableException | ESServerException e) {
            String logMessage = ResourceBundle.getBundle(I18N_CONF, Locale.getDefault()).getString(ES_INDEX_ERROR_1);
            LOGGER.log(Level.WARNING, part + ES_INDEX_FAIL + logMessage, e);
        } catch (ESIndexNamingException e) {
            String logMessage = ResourceBundle.getBundle(I18N_CONF, Locale.getDefault()).getString(ES_INDEX_CREATION_ERROR_2);
            LOGGER.log(Level.WARNING, part + ES_INDEX_FAIL + logMessage + " " + workspaceId, e);
        } catch (ElasticsearchIllegalArgumentException e){
            LOGGER.log(Level.SEVERE, null, e);
        } finally {
            ESTools.closeClient(client);
        }
    }

    /**
     * Remove this docIteration from ElasticSearch Cluster
     *
     * @param doc The document iteration to remove from index
     */
    @Asynchronous
    public void delete(DocumentIteration doc) {
        Client client = null;
        try {
            client = ESTools.createClient();
            deleteRequest(client, doc).execute()
                    .actionGet();
        } catch (NoNodeAvailableException | ESServerException e) {
            String logMessage = ResourceBundle.getBundle(I18N_CONF, Locale.getDefault()).getString(ES_DELETE_ERROR_1);
            LOGGER.log(Level.WARNING, logMessage, e);
        } finally {
            ESTools.closeClient(client);
        }
    }

    /**
     * Remove this partIteration from ElasticSearch Cluster
     *
     * @param part The part iteration to remove from index
     */
    @Asynchronous
    public void delete(PartIteration part) {
        Client client = null;
        try {
            client = ESTools.createClient();
            deleteRequest(client, part).execute()
                    .actionGet();
        } catch (NoNodeAvailableException | ESServerException e) {
            String logMessage = ResourceBundle.getBundle(I18N_CONF, Locale.getDefault()).getString(ES_DELETE_ERROR_1);
            LOGGER.log(Level.WARNING, logMessage, e);
        } finally {
            ESTools.closeClient(client);
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
        Client client = null;
        try {
            client = ESTools.createClient();
            deleteIndex(ESTools.formatIndexName(workspaceId), client);
        } catch (ESServerException | NoNodeAvailableException e) {
            hasSuccess = false;
            failureMessage = ResourceBundle.getBundle(I18N_CONF, Locale.getDefault()).getString("ES_DeleteError2");
            String logMessage = ResourceBundle.getBundle(I18N_CONF, Locale.getDefault()).getString("ES_DeleteError3");
            LOGGER.log(Level.WARNING, logMessage + " \n " + failureMessage, e);
        } finally {
            ESTools.closeClient(client);
        }

        try {
            String login = ctx.getCallerPrincipal().getName();
            Account account = accountManager.getAccount(login);
            mailer.sendIndexerResult(account, workspaceId, hasSuccess, failureMessage);
        } catch (AccountNotFoundException e) {
            String logMessage = ResourceBundle.getBundle(I18N_CONF, Locale.getDefault()).getString("ES_MailError1");
            LOGGER.log(Level.WARNING, logMessage, e);
        }
    }

    private BulkRequestBuilder bulkWorkspaceRequestBuilder(Client client, BulkRequestBuilder pBulkRequest, String workspaceId, boolean silent) throws ESIndexNamingException {
        BulkRequestBuilder bulkRequest = pBulkRequest;
        try {
            createIndex(ESTools.formatIndexName(workspaceId), client);
        } catch (ESIndexAlreadyExistsException e) {
            LOGGER.log(Level.FINEST, null, e);
        } catch (ESIndexNamingException e) {
            LOGGER.log(Level.FINEST, null, e);
            if (!silent) {
                throw e;
            }
        }

        bulkRequest = bulkDocumentsIndexRequestBuilder(client, bulkRequest, workspaceId);
        bulkRequest = bulkPartsIndexRequestBuilder(client, bulkRequest, workspaceId);

        return bulkRequest;
    }

    private BulkRequestBuilder bulkDocumentsIndexRequestBuilder(Client client, BulkRequestBuilder pBulkRequest, String workspaceId) {
        DocumentMasterDAO docMasterDAO = new DocumentMasterDAO(em);
        for (DocumentMaster docM : docMasterDAO.getAllByWorkspace(workspaceId)) {
            for (DocumentRevision docR : docM.getDocumentRevisions()) {
                pBulkRequest.add(indexRequest(client, docR.getLastIteration()));
            }
        }
        return pBulkRequest;
    }

    private BulkRequestBuilder bulkPartsIndexRequestBuilder(Client client, BulkRequestBuilder pBulkRequest, String workspaceId) {
        PartMasterDAO partMasterDAO = new PartMasterDAO(em);
        for (PartMaster partMaster : partMasterDAO.getAllByWorkspace(workspaceId)) {
            for (PartRevision partRev : partMaster.getPartRevisions()) {
                pBulkRequest.add(indexRequest(client, partRev.getLastIteration()));
            }
        }
        return pBulkRequest;
    }


    /**
     * Get the Index request for a documentIteration in ElasticSearch Cluster
     *
     * @param doc The document iteration to index
     */
    private UpdateRequestBuilder indexRequest(Client client, DocumentIteration doc) throws NoNodeAvailableException {
        Map<String, String> binaryList = new HashMap<>();
        for (BinaryResource bin : doc.getAttachedFiles()) {
            try (InputStream in = dataManager.getBinaryResourceInputStream(bin)){
                binaryList.put(bin.getName(), ESTools.streamToString(bin.getFullName(), in));
            } catch (StorageException | IOException e) {
                LOGGER.log(Level.FINEST, null, e);
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
    private UpdateRequestBuilder indexRequest(Client client, PartIteration part) {
        Map<String, String> binaryList = new HashMap<>();
        for(BinaryResource bin : part.getAttachedFiles()) {
            try (InputStream in = dataManager.getBinaryResourceInputStream(bin)){
                binaryList.put(bin.getName(),ESTools.streamToString(bin.getFullName(), in));
            } catch (StorageException | IOException e) {
                LOGGER.log(Level.FINEST, null, e);
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
    private DeleteRequestBuilder deleteRequest(Client client, DocumentIteration doc) throws NoNodeAvailableException {
        return client.prepareDelete(ESTools.formatIndexName(doc.getWorkspaceId()), ESMapper.DOCUMENT_TYPE, doc.getKey().toString());
    }

    /**
     * Get the Delete request for a partIteration in ElasticSearch Cluster
     *
     * @param part The part iteration to delete
     */
    private DeleteRequestBuilder deleteRequest(Client client, PartIteration part) throws NoNodeAvailableException {
        return client.prepareDelete(ESTools.formatIndexName(part.getWorkspaceId()), ESMapper.PART_TYPE, part.getKey().toString());
    }


}