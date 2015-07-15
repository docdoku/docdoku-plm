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
            if (docQuery.getContent() != null) {
                ((BoolQueryBuilder) qr).should(QueryBuilders.fuzzyLikeThisFieldQuery(ESMapper.CONTENT_KEY).likeText(docQuery.getContent()));
            }
            qr = addCommonQuery(docQuery, qr);
        }
        return qr;
    }

    /**
     * Add queries common to both part and doc.
     *
     * @param searchQuery The search query wanted
     * @param qr The QueryBuilder initialized with the specific (doc/part) values
     * @return A query builder with the common query for part and doc
     */
    private QueryBuilder addCommonQuery(SearchQuery searchQuery, QueryBuilder qr) {
        if (searchQuery.getVersion() != null) {
            ((BoolQueryBuilder) qr).should(QueryBuilders.fuzzyLikeThisFieldQuery(ESMapper.VERSION_KEY).likeText(searchQuery.getVersion()));
        }
        if (searchQuery.getAuthor() != null) {
            ((BoolQueryBuilder) qr).should(QueryBuilders.fuzzyLikeThisFieldQuery(ESMapper.AUTHOR_SEARCH_KEY).likeText(searchQuery.getAuthor()));
        }
        if (searchQuery.getType() != null) {
            ((BoolQueryBuilder) qr).should(QueryBuilders.fuzzyLikeThisFieldQuery(ESMapper.TYPE_KEY).likeText(searchQuery.getType()));
        }

        if (searchQuery.getCreationDateFrom() != null) {
            ((BoolQueryBuilder) qr).should(QueryBuilders.rangeQuery(ESMapper.CREATION_DATE_KEY).from(searchQuery.getCreationDateFrom()));
        }
        if (searchQuery.getCreationDateTo() != null) {
            ((BoolQueryBuilder) qr).should(QueryBuilders.rangeQuery(ESMapper.CREATION_DATE_KEY).to(searchQuery.getCreationDateTo()));
        }
        if (searchQuery.getModificationDateFrom() != null) {
            ((BoolQueryBuilder) qr).should(QueryBuilders.rangeQuery(ESMapper.MODIFICATION_DATE_KEY).from(searchQuery.getModificationDateFrom()));
        }
        if (searchQuery.getModificationDateTo() != null) {
            ((BoolQueryBuilder) qr).should(QueryBuilders.rangeQuery(ESMapper.MODIFICATION_DATE_KEY).to(searchQuery.getModificationDateTo()));
        }
        if (searchQuery.getAttributes() != null) {
            for (SearchQuery.AbstractAttributeQuery attr : searchQuery.getAttributes()) {
                if (attr instanceof SearchQuery.DateAttributeQuery) {
                    QueryBuilder b = QueryBuilders.boolQuery().must(QueryBuilders.termQuery(ESMapper.ATTRIBUTE_NAME,attr.getNameWithoutWhiteSpace()))
                            .must(QueryBuilders.rangeQuery(ESMapper.ATTRIBUTE_VALUE).from(((SearchQuery.DateAttributeQuery) attr).getFromDate()).to(((SearchQuery.DateAttributeQuery) attr).getToDate()));
                    ((BoolQueryBuilder) qr).should(b);
                } else if (attr instanceof SearchQuery.TextAttributeQuery) {
                    QueryBuilder b = QueryBuilders.boolQuery().must(QueryBuilders.termQuery(ESMapper.ATTRIBUTE_NAME, attr.getNameWithoutWhiteSpace()))
                        .must(QueryBuilders.termQuery(ESMapper.ATTRIBUTE_VALUE, ((SearchQuery.TextAttributeQuery) attr).getTextValue()));
                    ((BoolQueryBuilder) qr).should(b);
                } else if (attr instanceof SearchQuery.NumberAttributeQuery) {
                    QueryBuilder b = QueryBuilders.boolQuery().must(QueryBuilders.termQuery(ESMapper.ATTRIBUTE_NAME,attr.getNameWithoutWhiteSpace()))
                            .must(QueryBuilders.termQuery(ESMapper.ATTRIBUTE_VALUE, "" + ((SearchQuery.NumberAttributeQuery) attr).getNumberValue()));
                    ((BoolQueryBuilder) qr).should(b);
                } else if (attr instanceof SearchQuery.BooleanAttributeQuery) {
                    QueryBuilder b = QueryBuilders.boolQuery().must(QueryBuilders.termQuery(ESMapper.ATTRIBUTE_NAME,attr.getNameWithoutWhiteSpace()))
                            .must(QueryBuilders.termQuery(ESMapper.ATTRIBUTE_VALUE,  "" + ((SearchQuery.BooleanAttributeQuery) attr).isBooleanValue()));
                    ((BoolQueryBuilder) qr).should(b);
                } else if (attr instanceof SearchQuery.URLAttributeQuery) {
                    QueryBuilder b = QueryBuilders.boolQuery().must(QueryBuilders.termQuery(ESMapper.ATTRIBUTE_NAME,attr.getNameWithoutWhiteSpace()))
                            .must(QueryBuilders.termQuery(ESMapper.ATTRIBUTE_VALUE,((SearchQuery.URLAttributeQuery) attr).getUrlValue()));
                    ((BoolQueryBuilder) qr).should(b);
                }
            }
        }
        if (searchQuery.getTags() != null) {
            ((BoolQueryBuilder) qr).should(QueryBuilders.inQuery(ESMapper.TAGS_KEY, searchQuery.getTags()));
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
            if (partQuery.isStandardPart() != null) {
                String text = partQuery.isStandardPart() ? "TRUE" : "FALSE";
                ((BoolQueryBuilder) qr).should(QueryBuilders.fuzzyLikeThisFieldQuery(ESMapper.STANDARD_PART_KEY).likeText(text));
            }
            qr = addCommonQuery(partQuery, qr);
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
                ((BoolQueryBuilder) qr).should(QueryBuilders.rangeQuery(ESMapper.CREATION_DATE_KEY).from(query.getCreationDateFrom().getTime()));
            }
            if (query.getCreationDateTo() != null) {
                ((BoolQueryBuilder) qr).should(QueryBuilders.rangeQuery(ESMapper.CREATION_DATE_KEY).to(query.getCreationDateTo().getTime()));
            }
            if (query.getModificationDateFrom() != null) {
                ((BoolQueryBuilder) qr).should(QueryBuilders.rangeQuery(ESMapper.MODIFICATION_DATE_KEY).from(query.getModificationDateFrom().getTime()));
            }
            if (query.getModificationDateTo() != null) {
                ((BoolQueryBuilder) qr).should(QueryBuilders.rangeQuery(ESMapper.MODIFICATION_DATE_KEY).to(query.getModificationDateTo().getTime()));
            }
            if (query.getAttributes() != null) {
                for (SearchQuery.AbstractAttributeQuery attr : query.getAttributes()) {
                    if (attr instanceof SearchQuery.DateAttributeQuery) {
                        ((BoolQueryBuilder) qr).should(QueryBuilders.termQuery(attr.getNameWithoutWhiteSpace(), ((SearchQuery.DateAttributeQuery) attr).getFromDate().getTime()));
                    } else if (attr instanceof SearchQuery.TextAttributeQuery) {
                        ((BoolQueryBuilder) qr).should(QueryBuilders.termQuery(attr.getNameWithoutWhiteSpace(), ((SearchQuery.TextAttributeQuery) attr).getTextValue()));
                    } else if (attr instanceof SearchQuery.NumberAttributeQuery) {
                        ((BoolQueryBuilder) qr).should(QueryBuilders.termQuery(attr.getNameWithoutWhiteSpace(), "" + ((SearchQuery.NumberAttributeQuery) attr).getNumberValue()));
                    } else if (attr instanceof SearchQuery.BooleanAttributeQuery) {
                        ((BoolQueryBuilder) qr).should(QueryBuilders.termQuery(attr.getNameWithoutWhiteSpace(), "" + ((SearchQuery.BooleanAttributeQuery) attr).isBooleanValue()));
                    } else if (attr instanceof SearchQuery.URLAttributeQuery) {
                        ((BoolQueryBuilder) qr).should(QueryBuilders.termQuery(attr.getNameWithoutWhiteSpace(), ((SearchQuery.URLAttributeQuery) attr).getUrlValue()));
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
