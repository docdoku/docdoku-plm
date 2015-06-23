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

import com.docdoku.core.common.Workspace;
import com.docdoku.core.document.DocumentRevision;
import com.docdoku.core.document.DocumentRevisionKey;
import com.docdoku.core.exceptions.DocumentRevisionNotFoundException;
import com.docdoku.core.exceptions.ESServerException;
import com.docdoku.core.exceptions.PartRevisionNotFoundException;
import com.docdoku.core.product.PartRevision;
import com.docdoku.core.product.PartRevisionKey;
import com.docdoku.core.query.DocumentSearchQuery;
import com.docdoku.core.query.PartSearchQuery;
import com.docdoku.core.query.SearchQuery;
import com.docdoku.server.dao.DocumentRevisionDAO;
import com.docdoku.server.dao.PartRevisionDAO;
import com.docdoku.server.dao.WorkspaceDAO;
import org.elasticsearch.action.search.*;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.NoNodeAvailableException;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.text.MessageFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Search Method using ElasticSearch API.
 *
 * @author Taylor LABEJOF
 */
@Stateless(name = "ESSearcher")
public class ESSearcher {
    private static final String I18N_CONF = "com.docdoku.core.i18n.LocalStrings";
    private static final Logger LOGGER = Logger.getLogger(ESSearcher.class.getName());
    private static final String ES_TYPE_DOCUMENT = "document";
    private static final String ES_TYPE_PART = "part";
    private static final String ES_SEARCH_ERROR_1 = "ES_SearchError1";
    private static final String ES_SERVER_ERROR_1 = "IndexerServerException";

    @PersistenceContext
    private EntityManager em;

    /**
     * Constructor
     */
    public ESSearcher() {
        super();
    }

    /**
     * Search a document
     *
     * @param docQuery DocumentSearchQuery
     * @return List of document master
     */
    public List<DocumentRevision> search(DocumentSearchQuery docQuery) throws ESServerException {
        try {
            Client client = ESTools.createClient();
            QueryBuilder qr = getQueryBuilder(docQuery);
            SearchRequestBuilder srb = getSearchRequest(client, ESTools.formatIndexName(docQuery.getWorkspaceId()), ES_TYPE_DOCUMENT, qr);
            SearchResponse sr = srb.execute().actionGet();


            List<DocumentRevision> listOfDocuments = new ArrayList<>();
            for (int i = 0; i < sr.getHits().getHits().length; i++) {
                SearchHit hit = sr.getHits().getAt(i);
                DocumentRevision docR = getDocumentRevision(hit);
                if (docR != null && !listOfDocuments.contains(docR)) {
                    listOfDocuments.add(docR);
                }
            }

            //Todo FilterConfigSpec

            client.close();
            return listOfDocuments;
        } catch (NoNodeAvailableException e) {
            String logMessage = ResourceBundle.getBundle(I18N_CONF, Locale.getDefault()).getString(ES_SEARCH_ERROR_1);
            LOGGER.log(Level.WARNING, logMessage, e);
            throw new ESServerException(Locale.getDefault(), ES_SERVER_ERROR_1);
        }
    }

    /**
     * Search a part
     *
     * @param partQuery PartSearchQuery
     * @return List of part revision
     */
    public List<PartRevision> search(PartSearchQuery partQuery) throws ESServerException {
        try {
            Client client = ESTools.createClient();
            QueryBuilder qr = getQueryBuilder(partQuery);
            SearchRequestBuilder srb = getSearchRequest(client, ESTools.formatIndexName(partQuery.getWorkspaceId()), ES_TYPE_PART, qr);
            SearchResponse sr = srb.execute().actionGet();

            HashSet<PartRevision> setOfParts = new HashSet<>();
            for (int i = 0; i < sr.getHits().getHits().length; i++) {
                SearchHit hit = sr.getHits().getAt(i);
                PartRevision partRevision = getPartRevision(hit);
                if (partRevision != null) {
                    setOfParts.add(partRevision);
                }
            }

            //Todo FilterConfigSpec

            client.close();
            return new ArrayList<>(setOfParts);
        } catch (NoNodeAvailableException e) {
            String logMessage = ResourceBundle.getBundle(I18N_CONF, Locale.getDefault()).getString(ES_SEARCH_ERROR_1);
            LOGGER.log(Level.WARNING, logMessage, e);
            throw new ESServerException(Locale.getDefault(), ES_SERVER_ERROR_1);
        }

    }

    /**
     * Search a DocdokuPLM item
     *
     * @param query The search query
     * @return List of object matching the search query
     */
    public List<Object> search(SearchQuery query) throws ESServerException {
        try {
            Client client = ESTools.createClient();
            QueryBuilder qr = getQueryBuilder(query);
            MultiSearchRequestBuilder srbm = client.prepareMultiSearch();
            srbm.add(getSearchRequest(client, ESTools.formatIndexName(query.getWorkspaceId()), ES_TYPE_DOCUMENT, qr));
            srbm.add(getSearchRequest(client, ESTools.formatIndexName(query.getWorkspaceId()), ES_TYPE_PART, qr));
            MultiSearchResponse srm = srbm.execute().actionGet();

            List<Object> ret = new ArrayList<>();
            MultiSearchResponse.Item sri = srm.getResponses()[0];
            SearchResponse sr = sri.getResponse();
            for (int i = 0; i < sr.getHits().getHits().length; i++) {
                SearchHit hit = sr.getHits().getAt(i);
                DocumentRevision docR = getDocumentRevision(hit);
                if (docR != null && !ret.contains(docR)) {
                    ret.add(docR);
                }
            }

            sri = srm.getResponses()[1];
            sr = sri.getResponse();
            for (int i = 0; i < sr.getHits().getHits().length; i++) {
                SearchHit hit = sr.getHits().getAt(i);
                PartRevision partRevision = getPartRevision(hit);
                if (partRevision != null && !ret.contains(partRevision)) {
                    ret.add(partRevision);
                }
            }
            client.close();
            return ret;
        } catch (NoNodeAvailableException e) {
            String logMessage = ResourceBundle.getBundle(I18N_CONF, Locale.getDefault()).getString(ES_SEARCH_ERROR_1);
            LOGGER.log(Level.WARNING, logMessage, e);
            throw new ESServerException(Locale.getDefault(), ES_SERVER_ERROR_1);
        }
    }

    /**
     * Search a document in all Workspace
     *
     * @param docQuery DocumentSearchQuery
     * @return List of document master
     */
    public List<DocumentRevision> searchInAllWorkspace(DocumentSearchQuery docQuery) throws ESServerException {
        try {
            Client client = ESTools.createClient();
            QueryBuilder qr = getQueryBuilder(docQuery);
            MultiSearchRequestBuilder srbm = client.prepareMultiSearch();
            WorkspaceDAO wDAO = new WorkspaceDAO(em);
            for (Workspace w : wDAO.getAll()) {
                srbm.add(getSearchRequest(client, ESTools.formatIndexName(w.getId()), ES_TYPE_DOCUMENT, qr));
            }
            MultiSearchResponse srm = srbm.execute().actionGet();


            List<DocumentRevision> listOfDocuments = new ArrayList<>();
            for (MultiSearchResponse.Item sri : srm.getResponses()) {
                if (!sri.isFailure()) {
                    SearchResponse sr = sri.getResponse();
                    for (int i = 0; i < sr.getHits().getHits().length; i++) {
                        SearchHit hit = sr.getHits().getAt(i);
                        DocumentRevision docR = getDocumentRevision(hit);
                        if (docR != null && !listOfDocuments.contains(docR)) {
                            listOfDocuments.add(docR);
                        }
                    }
                }
            }
            client.close();
            return listOfDocuments;
        } catch (NoNodeAvailableException e) {
            String logMessage = ResourceBundle.getBundle(I18N_CONF, Locale.getDefault()).getString(ES_SEARCH_ERROR_1);
            LOGGER.log(Level.WARNING, logMessage, e);
            throw new ESServerException(Locale.getDefault(), ES_SERVER_ERROR_1);
        }
    }

    /**
     * Search a part in all Workspace
     *
     * @param partQuery PartSearchQuery
     * @return List of part revision
     */
    public List<PartRevision> searchInAllWorkspace(PartSearchQuery partQuery) throws ESServerException {
        try {
            Client client = ESTools.createClient();
            QueryBuilder qr = getQueryBuilder(partQuery);
            MultiSearchRequestBuilder srbm = client.prepareMultiSearch();
            WorkspaceDAO wDAO = new WorkspaceDAO(em);
            for (Workspace w : wDAO.getAll()) {
                srbm.add(getSearchRequest(client, ESTools.formatIndexName(w.getId()), ES_TYPE_PART, qr));
            }
            MultiSearchResponse srm = srbm.execute().actionGet();


            List<PartRevision> listOfParts = new ArrayList<>();
            for (MultiSearchResponse.Item sri : srm.getResponses()) {
                if (!sri.isFailure()) {
                    SearchResponse sr = sri.getResponse();
                    for (int i = 0; i < sr.getHits().getHits().length; i++) {
                        SearchHit hit = sr.getHits().getAt(i);
                        PartRevision partRevision = getPartRevision(hit);
                        if (partRevision != null && !listOfParts.contains(partRevision)) {
                            listOfParts.add(partRevision);
                        }
                    }
                }
            }

            client.close();
            return listOfParts;
        } catch (NoNodeAvailableException e) {
            String logMessage = ResourceBundle.getBundle(I18N_CONF, Locale.getDefault()).getString(ES_SEARCH_ERROR_1);
            LOGGER.log(Level.WARNING, logMessage, e);
            throw new ESServerException(Locale.getDefault(), ES_SERVER_ERROR_1);
        }
    }

    /**
     * Return a ElasticSearch Query for DocumentSearch
     *
     * @param docQuery DocumentSearchQuery wanted
     * @return a ElasticSearch.QueryBuilder
     */
    private QueryBuilder getQueryBuilder(DocumentSearchQuery docQuery) {
        QueryBuilder qr;
        if (docQuery.getFullText() != null) {
            qr = QueryBuilders.disMaxQuery()                                                                            // TODO Cut the query and make a boolQuery() with all the words
                    .add(QueryBuilders.fuzzyLikeThisQuery()
                            .likeText(docQuery.getFullText()))
                    .add(QueryBuilders.queryString("*" + docQuery.getFullText() + "*")
                            .boost(2.5f))
                    .tieBreaker(1.2f);
        } else {
            qr = QueryBuilders.boolQuery();
            if (docQuery.getDocMId() != null) {
                ((BoolQueryBuilder) qr).should(
                        QueryBuilders.fuzzyLikeThisFieldQuery(ESMapper.DOCUMENT_ID_KEY).likeText(docQuery.getDocMId()));
            }
            if (docQuery.getTitle() != null) {
                ((BoolQueryBuilder) qr).should(
                        QueryBuilders.fuzzyLikeThisFieldQuery(ESMapper.TITLE_KEY).likeText(docQuery.getTitle()));
            }
            if (docQuery.getVersion() != null) {
                ((BoolQueryBuilder) qr).should(
                        QueryBuilders.fuzzyLikeThisFieldQuery(ESMapper.VERSION_KEY).likeText(docQuery.getVersion()));
            }
            if (docQuery.getAuthor() != null) {
                ((BoolQueryBuilder) qr).should(
                        QueryBuilders.fuzzyLikeThisFieldQuery(ESMapper.AUTHOR_SEARCH_KEY).likeText(docQuery.getAuthor()));
            }
            if (docQuery.getType() != null) {
                ((BoolQueryBuilder) qr).should(
                        QueryBuilders.fuzzyLikeThisFieldQuery(ESMapper.TYPE_KEY).likeText(docQuery.getType()));
            }
            if (docQuery.getCreationDateFrom() != null) {
                ((BoolQueryBuilder) qr).should(
                        QueryBuilders.rangeQuery(ESMapper.CREATION_DATE_KEY).from(docQuery.getCreationDateFrom()));
            }
            if (docQuery.getCreationDateTo() != null) {
                ((BoolQueryBuilder) qr).should(
                        QueryBuilders.rangeQuery(ESMapper.CREATION_DATE_KEY).to(docQuery.getCreationDateTo()));
            }
            if (docQuery.getModificationDateFrom() != null) {
                ((BoolQueryBuilder) qr).should(
                        QueryBuilders.rangeQuery(ESMapper.MODIFICATION_DATE_KEY).from(docQuery.getModificationDateFrom()));
            }
            if (docQuery.getModificationDateTo() != null) {
                ((BoolQueryBuilder) qr).should(
                        QueryBuilders.rangeQuery(ESMapper.MODIFICATION_DATE_KEY).to(docQuery.getModificationDateTo()));
            }
            if (docQuery.getAttributes() != null) {
                for (DocumentSearchQuery.AbstractAttributeQuery attr : docQuery.getAttributes()) {
                    if (attr instanceof DocumentSearchQuery.DateAttributeQuery) {
                        ((BoolQueryBuilder) qr).should(QueryBuilders.rangeQuery(attr.getNameWithoutWhiteSpace()).from(((DocumentSearchQuery.DateAttributeQuery) attr).getFromDate()).to(((DocumentSearchQuery.DateAttributeQuery) attr).getToDate()));
                    } else if (attr instanceof DocumentSearchQuery.TextAttributeQuery) {
                        ((BoolQueryBuilder) qr).should(QueryBuilders.fuzzyLikeThisFieldQuery(attr.getNameWithoutWhiteSpace()).likeText(((DocumentSearchQuery.TextAttributeQuery) attr).getTextValue()));
                    } else if (attr instanceof DocumentSearchQuery.NumberAttributeQuery) {
                        ((BoolQueryBuilder) qr).should(QueryBuilders.fuzzyLikeThisFieldQuery(attr.getNameWithoutWhiteSpace()).likeText("" + ((DocumentSearchQuery.NumberAttributeQuery) attr).getNumberValue()));
                    } else if (attr instanceof DocumentSearchQuery.BooleanAttributeQuery) {
                        ((BoolQueryBuilder) qr).should(QueryBuilders.fuzzyLikeThisFieldQuery(attr.getNameWithoutWhiteSpace()).likeText("" + ((DocumentSearchQuery.BooleanAttributeQuery) attr).isBooleanValue()));
                    } else if (attr instanceof DocumentSearchQuery.URLAttributeQuery) {
                        ((BoolQueryBuilder) qr).should(QueryBuilders.fuzzyLikeThisFieldQuery(attr.getNameWithoutWhiteSpace()).likeText(((DocumentSearchQuery.URLAttributeQuery) attr).getUrlValue()));
                    }
                }
            }
            if (docQuery.getContent() != null) {
                ((BoolQueryBuilder) qr).should(QueryBuilders.fuzzyLikeThisFieldQuery(ESMapper.CONTENT_KEY).likeText(docQuery.getContent()));
            }

            if (docQuery.getTags() != null) {
                ((BoolQueryBuilder) qr).should(QueryBuilders.inQuery(ESMapper.TAGS_KEY,docQuery.getTags()));
            }
        }
        return qr;
    }

    /**
     * Return a ElasticSearch Query for PartSearch
     *
     * @param partQuery PartSearchQuery wanted
     * @return a ElasticSearch.QueryBuilder
     */
    private QueryBuilder getQueryBuilder(PartSearchQuery partQuery) {
        QueryBuilder qr;
        if (partQuery.getFullText() != null) {                                                                          // TODO Cut the query and make a boolQuery() with all the words
            qr = QueryBuilders.disMaxQuery()
                    .add(QueryBuilders.fuzzyLikeThisQuery()
                            .likeText(partQuery.getFullText()))
                    .add(QueryBuilders.queryString("*" + partQuery.getFullText() + "*")
                            .boost(2.5f))
                    .tieBreaker(1.2f);
        } else {
            qr = QueryBuilders.boolQuery();
            if (partQuery.getPartNumber() != null) {
                ((BoolQueryBuilder) qr).should(QueryBuilders.fuzzyLikeThisFieldQuery(ESMapper.PART_NUMBER_KEY).likeText(partQuery.getPartNumber()));
            }
            if (partQuery.getName() != null) {
                ((BoolQueryBuilder) qr).should(QueryBuilders.fuzzyLikeThisFieldQuery(ESMapper.PART_NAME_KEY).likeText(partQuery.getName()));
            }
            if (partQuery.getVersion() != null) {
                ((BoolQueryBuilder) qr).should(QueryBuilders.fuzzyLikeThisFieldQuery(ESMapper.VERSION_KEY).likeText(partQuery.getVersion()));
            }
            if (partQuery.getAuthor() != null) {
                ((BoolQueryBuilder) qr).should(QueryBuilders.fuzzyLikeThisFieldQuery(ESMapper.AUTHOR_SEARCH_KEY).likeText(partQuery.getAuthor()));
            }
            if (partQuery.getType() != null) {
                ((BoolQueryBuilder) qr).should(QueryBuilders.fuzzyLikeThisFieldQuery(ESMapper.TYPE_KEY).likeText(partQuery.getType()));
            }
            if (partQuery.isStandardPart() != null) {
                String text = partQuery.isStandardPart() ? "TRUE":"FALSE";
                ((BoolQueryBuilder) qr).should(QueryBuilders.fuzzyLikeThisFieldQuery(ESMapper.STANDARD_PART_KEY).likeText(text));
            }
            if (partQuery.getCreationDateFrom() != null) {
                ((BoolQueryBuilder) qr).should(QueryBuilders.rangeQuery(ESMapper.CREATION_DATE_KEY).from(partQuery.getCreationDateFrom()));
            }
            if (partQuery.getCreationDateTo() != null) {
                ((BoolQueryBuilder) qr).should(QueryBuilders.rangeQuery(ESMapper.CREATION_DATE_KEY).to(partQuery.getCreationDateTo()));
            }
            if (partQuery.getModificationDateFrom() != null) {
                ((BoolQueryBuilder) qr).should(QueryBuilders.rangeQuery(ESMapper.MODIFICATION_DATE_KEY).from(partQuery.getModificationDateFrom()));
            }
            if (partQuery.getModificationDateTo() != null) {
                ((BoolQueryBuilder) qr).should(QueryBuilders.rangeQuery(ESMapper.MODIFICATION_DATE_KEY).to(partQuery.getModificationDateTo()));
            }
            if (partQuery.getAttributes() != null) {
                for (PartSearchQuery.AbstractAttributeQuery attr : partQuery.getAttributes()) {
                    if (attr instanceof PartSearchQuery.DateAttributeQuery) {
                        ((BoolQueryBuilder) qr).should(QueryBuilders.rangeQuery(attr.getNameWithoutWhiteSpace()).from(((PartSearchQuery.DateAttributeQuery) attr).getFromDate()).to(((PartSearchQuery.DateAttributeQuery) attr).getToDate()));
                    } else if (attr instanceof PartSearchQuery.TextAttributeQuery) {
                        ((BoolQueryBuilder) qr).should(QueryBuilders.fuzzyLikeThisFieldQuery(attr.getNameWithoutWhiteSpace()).likeText(((PartSearchQuery.TextAttributeQuery) attr).getTextValue()));
                    } else if (attr instanceof PartSearchQuery.NumberAttributeQuery) {
                        ((BoolQueryBuilder) qr).should(QueryBuilders.fuzzyLikeThisFieldQuery(attr.getNameWithoutWhiteSpace()).likeText("" + ((PartSearchQuery.NumberAttributeQuery) attr).getNumberValue()));
                    } else if (attr instanceof PartSearchQuery.BooleanAttributeQuery) {
                        ((BoolQueryBuilder) qr).should(QueryBuilders.fuzzyLikeThisFieldQuery(attr.getNameWithoutWhiteSpace()).likeText("" + ((PartSearchQuery.BooleanAttributeQuery) attr).isBooleanValue()));
                    } else if (attr instanceof PartSearchQuery.URLAttributeQuery) {
                        ((BoolQueryBuilder) qr).should(QueryBuilders.fuzzyLikeThisFieldQuery(attr.getNameWithoutWhiteSpace()).likeText(((PartSearchQuery.URLAttributeQuery) attr).getUrlValue()));
                    }
                }
            }
            if (partQuery.getTags() != null) {
                ((BoolQueryBuilder) qr).should(QueryBuilders.inQuery(ESMapper.TAGS_KEY,partQuery.getTags()));
            }
        }
        return qr;
    }

    /**
     * Return a ElasticSearch Query for a document or a part research
     *
     * @param query SearchQuery wanted
     * @return a ElasticSearch.QueryBuilder
     */
    private QueryBuilder getQueryBuilder(SearchQuery query) {
        QueryBuilder qr;
        if (query.getFullText() != null) {
            qr = QueryBuilders.fuzzyLikeThisQuery().likeText(query.getFullText());
        } else {
            qr = QueryBuilders.boolQuery();
            if (query.getVersion() != null) {
                ((BoolQueryBuilder) qr).should(QueryBuilders.fuzzyLikeThisFieldQuery(ESMapper.VERSION_KEY).likeText(query.getVersion()));
            }
            if (query.getAuthor() != null) {
                ((BoolQueryBuilder) qr).should(QueryBuilders.fuzzyLikeThisFieldQuery(ESMapper.AUTHOR_SEARCH_KEY).likeText(query.getAuthor()));
            }
            if (query.getType() != null) {
                ((BoolQueryBuilder) qr).should(QueryBuilders.fuzzyLikeThisFieldQuery(ESMapper.TYPE_KEY).likeText(query.getType()));
            }
            if (query.getCreationDateFrom() != null) {
                ((BoolQueryBuilder) qr).should(QueryBuilders.rangeQuery(ESMapper.CREATION_DATE_KEY).from(query.getCreationDateFrom()));
            }
            if (query.getCreationDateTo() != null) {
                ((BoolQueryBuilder) qr).should(QueryBuilders.rangeQuery(ESMapper.CREATION_DATE_KEY).to(query.getCreationDateTo()));
            }
            if (query.getModificationDateFrom() != null) {
                ((BoolQueryBuilder) qr).should(QueryBuilders.rangeQuery(ESMapper.MODIFICATION_DATE_KEY).from(query.getModificationDateFrom()));
            }
            if (query.getModificationDateTo() != null) {
                ((BoolQueryBuilder) qr).should(QueryBuilders.rangeQuery(ESMapper.MODIFICATION_DATE_KEY).to(query.getModificationDateTo()));
            }
            if (query.getAttributes() != null) {
                for (PartSearchQuery.AbstractAttributeQuery attr : query.getAttributes()) {
                    if (attr instanceof PartSearchQuery.DateAttributeQuery) {
                        ((BoolQueryBuilder) qr).should(QueryBuilders.rangeQuery(attr.getNameWithoutWhiteSpace()).from(((PartSearchQuery.DateAttributeQuery) attr).getFromDate()).to(((PartSearchQuery.DateAttributeQuery) attr).getToDate()));
                    } else if (attr instanceof PartSearchQuery.TextAttributeQuery) {
                        ((BoolQueryBuilder) qr).should(QueryBuilders.fuzzyLikeThisFieldQuery(attr.getNameWithoutWhiteSpace()).likeText(((PartSearchQuery.TextAttributeQuery) attr).getTextValue()));
                    } else if (attr instanceof PartSearchQuery.NumberAttributeQuery) {
                        ((BoolQueryBuilder) qr).should(QueryBuilders.fuzzyLikeThisFieldQuery(attr.getNameWithoutWhiteSpace()).likeText("" + ((PartSearchQuery.NumberAttributeQuery) attr).getNumberValue()));
                    } else if (attr instanceof PartSearchQuery.BooleanAttributeQuery) {
                        ((BoolQueryBuilder) qr).should(QueryBuilders.fuzzyLikeThisFieldQuery(attr.getNameWithoutWhiteSpace()).likeText("" + ((PartSearchQuery.BooleanAttributeQuery) attr).isBooleanValue()));
                    } else if (attr instanceof PartSearchQuery.URLAttributeQuery) {
                        ((BoolQueryBuilder) qr).should(QueryBuilders.fuzzyLikeThisFieldQuery(attr.getNameWithoutWhiteSpace()).likeText(((PartSearchQuery.URLAttributeQuery) attr).getUrlValue()));
                    }
                }
            }
        }
        return qr;
    }

    /**
     * Return a uniWorkspace Search Request for a type of resource
     *
     * @param workspaceId Workspace of research
     * @param type        Type of resource searched
     * @param pQuery      Search criterion
     * @return the uniWorkspace Search Request
     */
    private SearchRequestBuilder getSearchRequest(Client client, String workspaceId, String type, QueryBuilder pQuery) {
        return client.prepareSearch(workspaceId)
                .setTypes(type)
                .setSearchType(SearchType.QUERY_THEN_FETCH)
                .setQuery(pQuery);
    }

    /**
     * Get a document matching a search hit
     *
     * @param hit The search hit provide by ElasticSearch
     */
    private DocumentRevision getDocumentRevision(SearchHit hit) {
        DocumentRevisionKey docRevisionKey = ESMapper.getDocumentRevisionKey(hit.getSource());
        try {
            return new DocumentRevisionDAO(em).loadDocR(docRevisionKey);
        } catch (DocumentRevisionNotFoundException e) {
            String logMessage = ResourceBundle.getBundle(I18N_CONF, Locale.getDefault()).getString("DocumentRevisionNotFoundException");
            logMessage = MessageFormat.format(logMessage, docRevisionKey.getDocumentMaster().getWorkspace(), docRevisionKey.getVersion());
            LOGGER.log(Level.INFO, logMessage, e);
            return null;
        }
    }

    /**
     * Get a document matching a search hit
     *
     * @param hit The search hit provide by ElasticSearch
     */
    private PartRevision getPartRevision(SearchHit hit) {
        PartRevisionKey partRevisionKey = ESMapper.getPartRevisionKey(hit.getSource());
        try {
            return new PartRevisionDAO(em).loadPartR(partRevisionKey);
        } catch (PartRevisionNotFoundException e) {
            String logMessage = ResourceBundle.getBundle(I18N_CONF, Locale.getDefault()).getString("PartRevisionNotFoundException");
            logMessage = MessageFormat.format(logMessage, partRevisionKey.getPartMaster().toString(), partRevisionKey.getVersion());
            LOGGER.log(Level.INFO, logMessage, e);
            return null;
        }
    }
}