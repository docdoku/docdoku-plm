/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2017 DocDoku SARL
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
package com.docdoku.server.indexer;

import com.docdoku.core.common.Account;
import com.docdoku.core.common.BinaryResource;
import com.docdoku.core.common.Workspace;
import com.docdoku.core.document.DocumentIteration;
import com.docdoku.core.document.DocumentMaster;
import com.docdoku.core.document.DocumentRevision;
import com.docdoku.core.exceptions.AccountNotFoundException;
import com.docdoku.core.exceptions.StorageException;
import com.docdoku.core.product.PartIteration;
import com.docdoku.core.product.PartMaster;
import com.docdoku.core.product.PartRevision;
import com.docdoku.core.query.DocumentSearchQuery;
import com.docdoku.core.query.PartSearchQuery;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.IAccountManagerLocal;
import com.docdoku.core.services.IBinaryStorageManagerLocal;
import com.docdoku.core.services.IIndexerManagerLocal;
import com.docdoku.core.services.IMailerLocal;
import com.docdoku.server.dao.DocumentMasterDAO;
import com.docdoku.server.dao.PartMasterDAO;
import com.docdoku.server.dao.WorkspaceDAO;
import org.elasticsearch.ResourceAlreadyExistsException;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequestBuilder;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.NoNodeAvailableException;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;

import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Asynchronous;
import javax.ejb.Local;
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
 * @author Morgan Guimard
 */
@Stateless(name = "IndexerManagerBean")
@Local(IIndexerManagerLocal.class)
@DeclareRoles({UserGroupMapping.REGULAR_USER_ROLE_ID, UserGroupMapping.ADMIN_ROLE_ID})
public class IndexerManagerBean implements IIndexerManagerLocal {

    @PersistenceContext
    private EntityManager em;

    @Inject
    private Client client;

    @Inject
    private IAccountManagerLocal accountManager;

    @Inject
    private IMailerLocal mailer;

    @Inject
    private IndexerConfigManager indexerConfigManager;

    @Inject
    private IBinaryStorageManagerLocal storageManager;

    private static final String I18N_CONF = "com.docdoku.core.i18n.LocalStrings";

    private static final Logger LOGGER = Logger.getLogger(IndexerManagerBean.class.getName());

    @Override
    @RolesAllowed({UserGroupMapping.ADMIN_ROLE_ID, UserGroupMapping.REGULAR_USER_ROLE_ID})
    public void createWorkspaceIndex(String workspaceId) {
        try {
            createIndex(IndexerUtils.formatIndexName(workspaceId));
        } catch (NoNodeAvailableException e) {
            LOGGER.log(Level.WARNING, "Cannot create index for workspace [" + workspaceId + "] " +
                    "The ElasticSearch server doesn't seem to respond");
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Cannot create index for workspace [" + workspaceId + "]", e);
        }
    }

    @Override
    @RolesAllowed({UserGroupMapping.ADMIN_ROLE_ID, UserGroupMapping.REGULAR_USER_ROLE_ID})
    public void deleteWorkspaceIndex(String workspaceId) {
        try {
            client.admin().indices().prepareDelete(IndexerUtils.formatIndexName(workspaceId)).execute().actionGet();
        } catch (NoNodeAvailableException e) {
            LOGGER.log(Level.WARNING, "Cannot delete index : The ElasticSearch server doesn't seem to respond");
        }
    }

    @Override
    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID})
    public void indexDocumentIteration(DocumentIteration documentIteration) {

        try {
            createIndex(IndexerUtils.formatIndexName(documentIteration.getWorkspaceId()));
        } catch (ResourceAlreadyExistsException e) {
            LOGGER.log(Level.INFO, "Index already exists");
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, null, e);
        }

        try {
            indexRequest(documentIteration).get();
        } catch (NoNodeAvailableException e) {
            LOGGER.log(Level.WARNING, "Cannot create index for requested document iteration indexation, The ElasticSearch server doesn't seem to respond", e);
        }

    }

    @Override
    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID})
    public void indexDocumentIterations(List<DocumentIteration> documentIterations) {
        documentIterations.forEach(this::indexDocumentIteration);
    }

    @Override
    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID})
    public void indexPartIteration(PartIteration partIteration) {
        try {
            createIndex(IndexerUtils.formatIndexName(partIteration.getWorkspaceId()));
        } catch (ResourceAlreadyExistsException e) {
            LOGGER.log(Level.INFO, "Index already exists");
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, null, e);
        }

        try {
            indexRequest(partIteration).get();
        } catch (NoNodeAvailableException e) {
            LOGGER.log(Level.WARNING, "Cannot create index for requested part iteration indexation, The ElasticSearch server doesn't seem to respond", e);
        }
    }

    @Override
    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID})
    public void indexPartIterations(List<PartIteration> partIterations) {
        partIterations.forEach(this::indexPartIteration);
    }

    @Override
    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID})
    public void removeDocumentIterationFromIndex(DocumentIteration documentIteration) {
        try {
            deleteRequest(documentIteration).execute().actionGet();
        } catch (NoNodeAvailableException e) {
            LOGGER.log(Level.WARNING, "Cannot delete document " + documentIteration + ": The ElasticSearch server doesn't seem to respond");
        }
    }

    @Override
    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID})
    public void removePartIterationFromIndex(PartIteration partIteration) {
        try {
            deleteRequest(partIteration).execute().actionGet();
        } catch (NoNodeAvailableException e) {
            LOGGER.log(Level.WARNING, "Cannot delete part iteration " + partIteration + ": The ElasticSearch server doesn't seem to respond");
        }
    }

    @Override
    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID})
    public List<DocumentRevision> searchDocumentRevisions(DocumentSearchQuery documentSearchQuery) {
        return null;
    }

    @Override
    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID})
    public List<PartRevision> searchPartRevisions(PartSearchQuery partSearchQuery) {
        return null;
    }

    @Override
    @Asynchronous
    @RolesAllowed({UserGroupMapping.ADMIN_ROLE_ID})
    public void indexAllWorkspaces() {
        try {
            BulkRequestBuilder bulkRequest = client.prepareBulk();
            WorkspaceDAO wDAO = new WorkspaceDAO(em);

            for (Workspace workspace : wDAO.getAll()) {
                bulkRequest = bulkWorkspaceRequestBuilder(bulkRequest, workspace.getId());
            }

            BulkResponse bulkResponse = bulkRequest.execute().actionGet();

            if (bulkResponse.hasFailures()) {
                LOGGER.log(Level.SEVERE, "Failures while bulk indexing: \n" + bulkResponse.buildFailureMessage());
            }

        } catch (NoNodeAvailableException e) {
            LOGGER.log(Level.WARNING, "Cannot index all workspaces: The ElasticSearch server doesn't seem to respond");
        }
    }

    @Override
    @RolesAllowed({UserGroupMapping.ADMIN_ROLE_ID, UserGroupMapping.REGULAR_USER_ROLE_ID})
    public void indexWorkspace(String workspaceId) {

        BulkRequestBuilder bulkRequest = client.prepareBulk();
        bulkWorkspaceRequestBuilder(bulkRequest, workspaceId);
        BulkResponse bulkResponse = bulkRequest.execute().actionGet();

        if (bulkResponse.hasFailures()) {
            LOGGER.log(Level.SEVERE, "Failures while bulk indexing: \n" + bulkResponse.buildFailureMessage());
        }
    }

    @Override
    @RolesAllowed({UserGroupMapping.ADMIN_ROLE_ID, UserGroupMapping.REGULAR_USER_ROLE_ID})
    public void removeWorkspaceFromIndex(String workspaceId) {

        String failureMessage = "";
        boolean hasSuccess = false;

        try {
            client.admin().indices().prepareDelete(IndexerUtils.formatIndexName(workspaceId)).execute().actionGet();
            hasSuccess = true;
        } catch (NoNodeAvailableException e) {
            LOGGER.log(Level.WARNING, "Cannot delete index for workspace [" + workspaceId + "] : The ElasticSearch server doesn't seem to respond");
            failureMessage = getString("ES_DeleteError2");
        }

        sendNotification(workspaceId, hasSuccess, failureMessage);
    }

    private void createIndex(String pIndex) throws IOException {

        Settings settings = Settings.builder()
                .put("number_of_shards", indexerConfigManager.getNumberOfShards())
                .put("number_of_replicas", indexerConfigManager.getNumberOfReplicas())
                .put("auto_expand_replicas", indexerConfigManager.getAutoExpandReplicas())
                .build();

        client.admin().indices().prepareCreate(pIndex)
                .setSettings(settings)
                .addMapping(IndexerMapping.PART_TYPE, IndexerMapping.createPartIterationMapping())
                .addMapping(IndexerMapping.DOCUMENT_TYPE, IndexerMapping.createDocumentIterationMapping())
                .setSource(IndexerMapping.createSourceMapping())
                .execute().actionGet();

    }

    private BulkRequestBuilder bulkWorkspaceRequestBuilder(BulkRequestBuilder pBulkRequest, String workspaceId) {
        BulkRequestBuilder bulkRequest = pBulkRequest;
        String index = IndexerUtils.formatIndexName(workspaceId);

        try {
            createIndex(index);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Cannot create index for workspace [" + workspaceId + "]", e);
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


    private IndexRequestBuilder indexRequest(DocumentIteration documentIteration) throws NoNodeAvailableException {
        Map<String, String> binaryList = getBinaryList(documentIteration.getAttachedFiles());
        XContentBuilder jsonDoc = IndexerMapping.documentIterationToJSON(documentIteration, binaryList);
        return client.prepareIndex(IndexerUtils.formatIndexName(documentIteration.getWorkspaceId()),
                IndexerMapping.DOCUMENT_TYPE, documentIteration.getKey().toString())
                .setSource(jsonDoc);
    }

    private IndexRequestBuilder indexRequest(PartIteration partIteration) {
        Map<String, String> binaryList = getBinaryList(partIteration.getAttachedFiles());
        XContentBuilder jsonDoc = IndexerMapping.partIterationToJSON(partIteration, binaryList);
        return client.prepareIndex(IndexerUtils.formatIndexName(partIteration.getWorkspaceId()),
                IndexerMapping.PART_TYPE, partIteration.getKey().toString())
                .setSource(jsonDoc);
    }

    private Map<String, String> getBinaryList(Set<BinaryResource> attachedFiles) {
        Map<String, String> binaryList = new HashMap<>();
        for (BinaryResource bin : attachedFiles) {
            try (InputStream in = storageManager.getBinaryResourceInputStream(bin)) {
                binaryList.put(bin.getName(), IndexerUtils.streamToString(bin.getFullName(), in));
            } catch (StorageException | IOException e) {
                LOGGER.log(Level.SEVERE, "Cannot read file " + bin.getFullName(), e);
            }
        }
        return binaryList;
    }

    private DeleteRequestBuilder deleteRequest(DocumentIteration documentIteration) throws NoNodeAvailableException {
        return client.prepareDelete(IndexerUtils.formatIndexName(documentIteration.getWorkspaceId()),
                IndexerMapping.DOCUMENT_TYPE, documentIteration.getKey().toString());
    }

    private DeleteRequestBuilder deleteRequest(PartIteration partIteration) throws NoNodeAvailableException {
        return client.prepareDelete(IndexerUtils.formatIndexName(partIteration.getWorkspaceId()),
                IndexerMapping.PART_TYPE, partIteration.getKey().toString());
    }

    // TODO : review indexer notification system
    private void sendNotification(String workspaceId, boolean hasSuccess, String message) {
        try {
            Account account = accountManager.getMyAccount();
            mailer.sendIndexerResult(account, workspaceId, hasSuccess, message);
        } catch (AccountNotFoundException e) {
            LOGGER.log(Level.SEVERE, null, e);
        }
    }

    private String getString(String key) {
        return ResourceBundle.getBundle(I18N_CONF, Locale.getDefault()).getString(key);
    }
}