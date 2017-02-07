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

import com.docdoku.core.query.DocumentSearchQuery;
import com.docdoku.core.query.PartSearchQuery;
import com.docdoku.core.query.SearchQuery;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Morgan Guimard
 */
public class IndexerQueryBuilder {

    public static QueryBuilder getSearchQueryBuilder(DocumentSearchQuery documentSearchQuery) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        List<QueryBuilder> documentQueries = getDocumentQueries(documentSearchQuery);
        documentQueries.forEach(boolQueryBuilder::must);
        return boolQueryBuilder;
    }

    public static QueryBuilder getSearchQueryBuilder(PartSearchQuery partSearchQuery) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        List<QueryBuilder> partQueries = getPartQueries(partSearchQuery);
        partQueries.forEach(boolQueryBuilder::must);
        return boolQueryBuilder;
    }


    private static List<QueryBuilder> getDocumentQueries(DocumentSearchQuery documentSearchQuery) {
        List<QueryBuilder> queries = new ArrayList<>();

        String docMId = documentSearchQuery.getDocMId();
        String title = documentSearchQuery.getTitle();
        String folder = documentSearchQuery.getFolder();

        if (docMId != null && !docMId.isEmpty()) {
            queries.add(QueryBuilders.fuzzyQuery(IndexerMapping.DOCUMENT_ID_KEY, docMId));
        }

        if (title != null && !title.isEmpty()) {
            queries.add(QueryBuilders.fuzzyQuery(IndexerMapping.TITLE_KEY, title));
        }

        if (folder != null && !folder.isEmpty()) {
            queries.add(QueryBuilders.fuzzyQuery(IndexerMapping.FOLDER_KEY, folder));
        }

        queries.addAll(getCommonQueries(documentSearchQuery));

        return queries;
    }

    private static List<QueryBuilder> getPartQueries(PartSearchQuery partSearchQuery) {
        List<QueryBuilder> queries = new ArrayList<>();

        String partNumber = partSearchQuery.getPartNumber();
        String partName = partSearchQuery.getName();

        if (partNumber != null && !partNumber.isEmpty()) {
            queries.add(QueryBuilders.fuzzyQuery(IndexerMapping.PART_NUMBER_KEY, partNumber));
        }

        if (partName != null && !partName.isEmpty()) {
            queries.add(QueryBuilders.fuzzyQuery(IndexerMapping.PART_NAME_KEY, partName));
        }

        queries.addAll(getCommonQueries(partSearchQuery));

        return queries;
    }

    private static List<QueryBuilder> getCommonQueries(SearchQuery searchQuery) {

        String[] tags = searchQuery.getTags();
        SearchQuery.AbstractAttributeQuery[] attributes = searchQuery.getAttributes();

        List<QueryBuilder> queries = new ArrayList<>();

        if (searchQuery.getVersion() != null) {
            queries.add(QueryBuilders.termQuery(IndexerMapping.VERSION_KEY, searchQuery.getVersion()));
        }
        if (searchQuery.getAuthor() != null) {
            BoolQueryBuilder authorQuery = QueryBuilders.boolQuery();
            authorQuery.should(QueryBuilders.fuzzyQuery(IndexerMapping.AUTHOR_NAME_KEY, searchQuery.getAuthor()));
            authorQuery.should(QueryBuilders.fuzzyQuery(IndexerMapping.AUTHOR_LOGIN_KEY, searchQuery.getAuthor()));
            queries.add(authorQuery);
        }
        if (searchQuery.getType() != null) {
            queries.add(QueryBuilders.fuzzyQuery(IndexerMapping.TYPE_KEY, searchQuery.getType()));
        }

        if (searchQuery.getCreationDateFrom() != null) {
            queries.add(QueryBuilders.rangeQuery(IndexerMapping.CREATION_DATE_KEY).from(searchQuery.getCreationDateFrom()));
        }

        if (searchQuery.getCreationDateTo() != null) {
            queries.add(QueryBuilders.rangeQuery(IndexerMapping.CREATION_DATE_KEY).to(searchQuery.getCreationDateTo()));
        }

        if (searchQuery.getModificationDateFrom() != null) {
            queries.add(QueryBuilders.rangeQuery(IndexerMapping.MODIFICATION_DATE_KEY).from(searchQuery.getModificationDateFrom()));
        }

        if (searchQuery.getModificationDateTo() != null) {
            queries.add(QueryBuilders.rangeQuery(IndexerMapping.MODIFICATION_DATE_KEY).to(searchQuery.getModificationDateTo()));
        }

        if (searchQuery.getContent() != null) {
            queries.add(QueryBuilders.matchQuery(IndexerMapping.CONTENT_KEY, searchQuery.getContent()));
        }

        if (tags != null && tags.length > 0) {
            queries.add(QueryBuilders.termsQuery(IndexerMapping.TAGS_KEY, tags));
        }

        if (attributes != null) {
            Stream.of(attributes)
                    .collect(Collectors.groupingBy(SearchQuery.AbstractAttributeQuery::getNameWithoutWhiteSpace))
                    .forEach((attributeName, attributeList) -> addAttributeToQueries(queries, attributeName, attributeList));
        }

        return queries;
    }

    private static void addAttributeToQueries(List<QueryBuilder> queries, String attributeName, List<SearchQuery.AbstractAttributeQuery> attributeList) {

        BoolQueryBuilder attributeQueryBuilder = QueryBuilders.boolQuery();

        attributeQueryBuilder.must(QueryBuilders.nestedQuery(IndexerMapping.ATTRIBUTES_KEY,
                QueryBuilders.termQuery(IndexerMapping.ATTRIBUTES_KEY + "." + IndexerMapping.ATTRIBUTE_NAME, attributeName), ScoreMode.None));

        BoolQueryBuilder valuesQuery = QueryBuilders.boolQuery();

        for (SearchQuery.AbstractAttributeQuery attr : attributeList) {
            String attributeValue = attr.toString();
            if (attributeValue != null && !attributeValue.isEmpty()) {
                valuesQuery.should(QueryBuilders.nestedQuery(IndexerMapping.ATTRIBUTES_KEY,
                        QueryBuilders.termQuery(IndexerMapping.ATTRIBUTES_KEY + "." + IndexerMapping.ATTRIBUTE_VALUE, attributeValue), ScoreMode.None));
            }
        }

        if (valuesQuery.hasClauses()) {
            attributeQueryBuilder.must(valuesQuery);
        }

        queries.add(attributeQueryBuilder);

    }

}
