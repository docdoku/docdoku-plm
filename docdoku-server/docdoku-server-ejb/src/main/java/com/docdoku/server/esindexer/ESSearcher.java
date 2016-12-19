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
import org.elasticsearch.index.query.*;
import org.elasticsearch.indices.IndexMissingException;
import org.elasticsearch.search.SearchHit;

import javax.ejb.Stateless;
import javax.inject.Inject;
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

  /*  private static final String ES_SEARCH_ERROR_1 = "ES_SearchError1";
    private static final String ES_SEARCH_ERROR_2 = "ES_SearchError2";
    private static final String ES_SERVER_ERROR_1 = "IndexerServerException";
    private static final String ES_SERVER_ERROR_2 ="MissingIndexException";*/

    @PersistenceContext
    private EntityManager em;

    @Inject
    private Client client;

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
            QueryBuilder qr = getQueryBuilder(docQuery);
            SearchRequestBuilder srb = getSearchRequest(ESTools.formatIndexName(docQuery.getWorkspaceId()), ES_TYPE_DOCUMENT, qr);
            SearchResponse sr = srb.execute().actionGet();

            List<DocumentRevision> listOfDocuments = new ArrayList<>();

            for (int i = 0; i < sr.getHits().getHits().length; i++) {
                SearchHit hit = sr.getHits().getAt(i);
                DocumentRevision docR = getDocumentRevision(hit);
                if (docR != null && !listOfDocuments.contains(docR)) {
                    listOfDocuments.add(docR);
                }
            }

            return listOfDocuments;
        } catch (NoNodeAvailableException e) {
            LOGGER.log(Level.WARNING, "Cannot search: The ElasticSearch server doesn't seem to respond");
            throw new ESServerException(Locale.getDefault(), "ES_SearchError1");
        } catch (IndexMissingException e) {
            LOGGER.log(Level.WARNING, "Cannot search: index is missing", e);
            throw new ESServerException(Locale.getDefault(), "ES_SearchError2");
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
            QueryBuilder qr = getQueryBuilder(partQuery);
            SearchRequestBuilder srb = getSearchRequest(ESTools.formatIndexName(partQuery.getWorkspaceId()), ES_TYPE_PART, qr);
            SearchResponse sr = srb.execute().actionGet();

            Set<PartRevision> setOfParts = new HashSet<>();
            for (int i = 0; i < sr.getHits().getHits().length; i++) {
                SearchHit hit = sr.getHits().getAt(i);
                PartRevision partRevision = getPartRevision(hit);
                if (partRevision != null) {
                    setOfParts.add(partRevision);
                }
            }
            return new ArrayList<>(setOfParts);
        } catch (NoNodeAvailableException e) {
            LOGGER.log(Level.WARNING, "Cannot search: The ElasticSearch server doesn't seem to respond");
            throw new ESServerException(Locale.getDefault(), "ES_SearchError1");
        } catch (IndexMissingException e) {
            LOGGER.log(Level.WARNING, "Cannot search: index is missing", e);
            throw new ESServerException(Locale.getDefault(), "ES_SearchError2");
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
            QueryBuilder qr = getQueryBuilder(docQuery);
            MultiSearchRequestBuilder srbm = client.prepareMultiSearch();
            WorkspaceDAO wDAO = new WorkspaceDAO(em);
            for (Workspace w : wDAO.getAll()) {
                srbm.add(getSearchRequest(ESTools.formatIndexName(w.getId()), ES_TYPE_DOCUMENT, qr));
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
            return listOfDocuments;
        } catch (NoNodeAvailableException e) {
            LOGGER.log(Level.WARNING, "Cannot search in all workspace: The ElasticSearch server doesn't seem to respond");
            throw new ESServerException(Locale.getDefault(), "ES_SearchError1");
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
            QueryBuilder qr = getQueryBuilder(partQuery);
            MultiSearchRequestBuilder srbm = client.prepareMultiSearch();
            WorkspaceDAO wDAO = new WorkspaceDAO(em);
            for (Workspace w : wDAO.getAll()) {
                srbm.add(getSearchRequest(ESTools.formatIndexName(w.getId()), ES_TYPE_PART, qr));
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

            return listOfParts;
        }  catch (NoNodeAvailableException e) {
            LOGGER.log(Level.WARNING, "Cannot search in all workspace: The ElasticSearch server doesn't seem to respond");
            throw new ESServerException(Locale.getDefault(), "ES_SearchError1");
        }
    }

    private QueryBuilder getFullTextQuery(SearchQuery query) {
        // TODO Cut the query and make a boolQuery() with all the words
        QueryBuilder fullTextQuery = QueryBuilders.matchQuery("_all", query.getFullText())
                .operator(MatchQueryBuilder.Operator.OR)
                .fuzziness("AUTO");
        return fullTextQuery;
    }

    /**
     * Return a ElasticSearch Query for DocumentSearch
     *
     * @param docQuery DocumentSearchQuery wanted
     * @return a ElasticSearch.QueryBuilder
     */
    private QueryBuilder getQueryBuilder(DocumentSearchQuery docQuery) {
        ESQueryBuilder queryBuilder = new ESQueryBuilder();

        if (docQuery.getFullText() != null) {
            QueryBuilder query = getFullTextQuery(docQuery);
            queryBuilder.add(query);
        } else {
            if (docQuery.getDocMId() != null) {
                queryBuilder.add(QueryBuilders.fuzzyQuery(ESMapper.DOCUMENT_ID_KEY, docQuery.getDocMId()));
            }
            if (docQuery.getTitle() != null) {
                queryBuilder.add(QueryBuilders.fuzzyQuery(ESMapper.TITLE_KEY, docQuery.getTitle()));
            }
            if (docQuery.getFolder() != null) {
                queryBuilder.add(QueryBuilders.fuzzyQuery(ESMapper.FOLDER_KEY, docQuery.getFolder()));
            }
            addCommonQuery(queryBuilder, docQuery);
        }
        return queryBuilder.getFilteredQuery();
    }

    /**
     * Add queries common to both part and doc.
     *
     * @param searchQuery  The search query wanted
     * @param queryBuilder The QueryBuilder initialized with the specific (doc/part) values
     */
    private void addCommonQuery(ESQueryBuilder queryBuilder, SearchQuery searchQuery) {

        if (searchQuery.getVersion() != null) {
            queryBuilder.add(FilterBuilders.termFilter(ESMapper.VERSION_KEY, searchQuery.getVersion()));
        }
        if (searchQuery.getAuthor() != null) {
            queryBuilder.add(QueryBuilders.fuzzyQuery(ESMapper.AUTHOR_SEARCH_KEY, searchQuery.getAuthor()));
        }
        if (searchQuery.getType() != null) {
            queryBuilder.add(QueryBuilders.fuzzyQuery(ESMapper.TYPE_KEY, searchQuery.getType()));
        }

        if (searchQuery.getCreationDateFrom() != null) {
            queryBuilder.add(FilterBuilders.rangeFilter(ESMapper.CREATION_DATE_KEY).from(searchQuery.getCreationDateFrom()));
        }
        if (searchQuery.getCreationDateTo() != null) {
            queryBuilder.add(FilterBuilders.rangeFilter(ESMapper.CREATION_DATE_KEY).to(searchQuery.getCreationDateTo()));
        }
        if (searchQuery.getModificationDateFrom() != null) {
            queryBuilder.add(FilterBuilders.rangeFilter(ESMapper.MODIFICATION_DATE_KEY).from(searchQuery.getModificationDateFrom()));
        }
        if (searchQuery.getModificationDateTo() != null) {
            queryBuilder.add(FilterBuilders.rangeFilter(ESMapper.MODIFICATION_DATE_KEY).to(searchQuery.getModificationDateTo()));
        }
        if (searchQuery.getContent() != null) {
            queryBuilder.add(QueryBuilders.matchQuery(ESMapper.CONTENT_KEY, searchQuery.getContent()));
        }
        if (searchQuery.getAttributes() != null) {

            Map<String, List<NestedFilterBuilder>> filterBuilders = new HashMap<>();

            // Group attributes by
            for (SearchQuery.AbstractAttributeQuery attr : searchQuery.getAttributes()) {

                String attributeName = attr.getNameWithoutWhiteSpace();
                List<NestedFilterBuilder> attrFilters = filterBuilders.get(attributeName);

                if (null == attrFilters) {
                    attrFilters = new ArrayList<>();
                    filterBuilders.put(attributeName, attrFilters);
                }

                BoolFilterBuilder b = new BoolFilterBuilder();

                b.must(FilterBuilders.termFilter(ESMapper.ATTR_NESTED_PATH + "." + ESMapper.ATTRIBUTE_NAME, attributeName));

                if (attr.hasValue()) {
                    b.must(FilterBuilders.termFilter(ESMapper.ATTR_NESTED_PATH + "." + ESMapper.ATTRIBUTE_VALUE, attr.toString()));
                }

                NestedFilterBuilder nested = FilterBuilders.nestedFilter(ESMapper.ATTR_NESTED_PATH, b);
                attrFilters.add(nested);

            }

            for (Map.Entry<String, List<NestedFilterBuilder>> filterBuilderEntrySet : filterBuilders.entrySet()) {
                List<NestedFilterBuilder> builders = filterBuilderEntrySet.getValue();
                queryBuilder.add(FilterBuilders.orFilter(builders.toArray(new NestedFilterBuilder[builders.size()])));
            }
        }
        if (searchQuery.getTags() != null) {
            queryBuilder.add(FilterBuilders.inFilter(ESMapper.TAGS_KEY, searchQuery.getTags()));
        }
    }

    /**
     * Return a ElasticSearch Query for PartSearch
     *
     * @param partQuery PartSearchQuery wanted
     * @return a ElasticSearch.QueryBuilder
     */
    private QueryBuilder getQueryBuilder(PartSearchQuery partQuery) {
        ESQueryBuilder queryBuilder = new ESQueryBuilder();
        if (partQuery.getFullText() != null) {
            QueryBuilder query = getFullTextQuery(partQuery);
            queryBuilder.add(query);
        } else {
            if (partQuery.getPartNumber() != null) {
                queryBuilder.add(QueryBuilders.fuzzyQuery(ESMapper.PART_NUMBER_KEY, partQuery.getPartNumber()));
            }
            if (partQuery.getName() != null) {
                queryBuilder.add(QueryBuilders.fuzzyQuery(ESMapper.PART_NAME_KEY, partQuery.getName()));
            }
            if (partQuery.isStandardPart() != null) {
                queryBuilder.add(FilterBuilders.termFilter(ESMapper.STANDARD_PART_KEY, partQuery.isStandardPart()));
            }
            addCommonQuery(queryBuilder, partQuery);
        }

        return queryBuilder.getFilteredQuery();
    }

    /**
     * Return a uniWorkspace Search Request for a type of resource
     *
     * @param workspaceId Workspace of research
     * @param type        Type of resource searched
     * @param pQuery      Search criterion
     * @return the uniWorkspace Search Request
     */
    private SearchRequestBuilder getSearchRequest(String workspaceId, String type, QueryBuilder pQuery) {
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
