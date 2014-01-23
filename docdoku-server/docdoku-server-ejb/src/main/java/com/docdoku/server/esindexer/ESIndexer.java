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

import com.docdoku.core.common.BinaryResource;
import com.docdoku.core.common.Workspace;
import com.docdoku.core.document.*;
import com.docdoku.core.exceptions.DocumentRevisionNotFoundException;
import com.docdoku.core.exceptions.IndexerServerException;
import com.docdoku.core.exceptions.PartRevisionNotFoundException;
import com.docdoku.core.exceptions.StorageException;
import com.docdoku.core.meta.InstanceAttribute;
import com.docdoku.core.product.PartIteration;
import com.docdoku.core.product.PartMaster;
import com.docdoku.core.product.PartRevision;
import com.docdoku.core.product.PartRevisionKey;
import com.docdoku.core.query.DocumentSearchQuery;
import com.docdoku.core.query.PartSearchQuery;
import com.docdoku.core.query.SearchQuery;
import com.docdoku.core.services.IDataManagerLocal;
import com.docdoku.server.dao.*;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import org.apache.poi.hslf.extractor.PowerPointExtractor;
import org.apache.poi.hssf.extractor.ExcelExtractor;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.search.*;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.NoNodeAvailableException;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Search Method using ElasticSearch API.
 * @author Taylor LABEJOF
 */
@Stateless(name="ESIndexer")
public class ESIndexer{
    private final static String CONF_PROPERTIES="/com/docdoku/server/esindexer/conf.properties";
    private final static Properties CONF = new Properties();

    static{
        try {
            CONF.load(ESIndexer.class.getResourceAsStream(CONF_PROPERTIES));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @PersistenceContext
    private EntityManager em;

    @EJB
    private IDataManagerLocal dataManager;

    public ESIndexer() {
        super();
    }

    /**
     * Create a ElasticSearch Client to make QueryRequest
     */
    private Client createClient() throws IndexerServerException {
        try{
            return new TransportClient().addTransportAddress(new InetSocketTransportAddress(CONF.getProperty("host"), Integer.parseInt(CONF.getProperty("port"))));
        }catch (NoNodeAvailableException e){
            Logger.getLogger(ESIndexer.class.getName()).log(Level.SEVERE, null, e);
            throw new IndexerServerException(Locale.getDefault(), "IndexerServerException1");
        }
    }

    /**
     * Index all content in all workspace
     */
    @Asynchronous
    public void indexAll() throws IndexerServerException {
        try{
            Client client = createClient();
            BulkRequestBuilder bulkRequest = client.prepareBulk();
            WorkspaceDAO wDAO = new WorkspaceDAO(em);
            DocumentMasterDAO docMasterDAO = new DocumentMasterDAO(em);
            PartMasterDAO partMasterDAO = new PartMasterDAO(em);
            for(Workspace w : wDAO.getAll()){
                for(DocumentMaster docM : docMasterDAO.getAllByWorkspace(w.getId())){
                    for(DocumentRevision docR : docM.getDocumentRevisions()){
                        for(DocumentIteration docI : docR.getDocumentIterations()){
                            bulkRequest.add(indexRequest(client, docI));
                        }
                    }
                }
                for(PartMaster partMaster : partMasterDAO.getAllByWorkspace(w.getId())){
                    for(PartRevision partRev : partMaster.getPartRevisions()){
                        for(PartIteration partIte : partRev.getPartIterations()){
                            bulkRequest.add(indexRequest(client, partIte));
                        }
                    }
                }
            }

            BulkResponse bulkResponse = bulkRequest.execute().actionGet();
            client.close();
            if (bulkResponse.hasFailures()) {                                                                               // TODO Failure case
                System.out.println(bulkResponse.buildFailureMessage());
            }
        }catch (NoNodeAvailableException e){
            Logger.getLogger(ESIndexer.class.getName()).log(Level.SEVERE, null, e);
            throw new IndexerServerException(Locale.getDefault(), "IndexerServerException1");
        }
    }

    /**
     * Index a documentIteration in ElasticSearch Cluster
     * @param doc The document iteration to index
     */
    @Asynchronous
    public void index(DocumentIteration doc) throws IndexerServerException {
        try{
            Client client = createClient();
            indexRequest(client, doc).execute()
                                     .actionGet();
            client.close();
        }catch (NoNodeAvailableException e){
            Logger.getLogger(ESIndexer.class.getName()).log(Level.SEVERE, null, e);
            throw new IndexerServerException(Locale.getDefault(), "IndexerServerException1");
        }
    }

    /**
     * Index a partIteration in ElasticSearch Cluster
     * @param part The part iteration to index
     */
    @Asynchronous
    public void index(PartIteration part) throws IndexerServerException {
        try{
            Client client = createClient();
            indexRequest(client, part).execute()
                              .actionGet();
            client.close();
        }catch (NoNodeAvailableException e){
            Logger.getLogger(ESIndexer.class.getName()).log(Level.SEVERE, null, e);
            throw new IndexerServerException(Locale.getDefault(), "IndexerServerException1");
        }
    }

    /**
     * Remove this docIteration from ElasticSearch Cluster
     * @param doc The document iteration to remove from index
     */
    @Asynchronous
    public void delete(DocumentIteration doc) throws IndexerServerException {
        try{
            Client client = createClient();
            client.prepareDelete(doc.getWorkspaceId().toLowerCase(), "document", doc.getKey().toString())
                    .execute()
                    .actionGet();
            client.close();
        }catch (NoNodeAvailableException e){
            Logger.getLogger(ESIndexer.class.getName()).log(Level.SEVERE, null, e);
            throw new IndexerServerException(Locale.getDefault(), "IndexerServerException1");
        }
    }

    /**
     * Remove this partIteration from ElasticSearch Cluster
     * @param part The part iteration to remove from index
     */
    @Asynchronous
    public void delete(PartIteration part) throws IndexerServerException {
        try{
            Client client = createClient();
            client.prepareDelete(part.getWorkspaceId().toLowerCase(), "part", part.getKey().toString())
                    .execute()
                    .actionGet();
            client.close();
        }catch (NoNodeAvailableException e){
            Logger.getLogger(ESIndexer.class.getName()).log(Level.SEVERE, null, e);
            throw new IndexerServerException(Locale.getDefault(), "IndexerServerException1");
        }
    }

    /**
     * Search a document
     * @param docQuery DocumentSearchQuery
     * @return List of document master
     */
    public List<DocumentRevision> search(DocumentSearchQuery docQuery) throws IndexerServerException {
        try{
            Client client = createClient();
            QueryBuilder qr = getQueryBuilder(docQuery);
            SearchRequestBuilder srb = getSearchRequest(client, docQuery.getWorkspaceId().toLowerCase(), "document", qr);
            SearchResponse sr = srb.execute().actionGet();


            List<DocumentRevision> listOfDocuments = new ArrayList<>();
            for(int i=0; i<sr.getHits().getHits().length;i++){
                SearchHit hit = sr.getHits().getAt(i);
                Map<String,Object> source = hit.getSource();
                DocumentRevisionKey docRevisionKey = new DocumentRevisionKey(extractValue(source, "workspaceId"),extractValue(source, "docMId"),extractValue(source, "version"));
                try {
                    DocumentRevision docR = new DocumentRevisionDAO(em).loadDocR(docRevisionKey);
                    if(!listOfDocuments.contains(docR)){
                        listOfDocuments.add(docR);
                    }
                } catch (DocumentRevisionNotFoundException e) {
                    e.printStackTrace();                                                                                // TODO DocNotfound error
                }
            }

            client.close();
            return listOfDocuments;
        }catch (NoNodeAvailableException e){
            Logger.getLogger(ESIndexer.class.getName()).log(Level.SEVERE, null, e);
            throw new IndexerServerException(Locale.getDefault(), "IndexerServerException1");
        }
    }

    /**
     * Search a part
     * @param partQuery PartSearchQuery
     * @return List of part revision
     */
    public List<PartRevision> search(PartSearchQuery partQuery) throws IndexerServerException {
        try{
            Client client = createClient();
            QueryBuilder qr = getQueryBuilder(partQuery);
            SearchRequestBuilder srb = getSearchRequest(client, partQuery.getWorkspaceId().toLowerCase(), "part", qr);
            SearchResponse sr = srb.execute().actionGet();

            List<PartRevision> listOfParts = new ArrayList<>();
            for(int i=0; i<sr.getHits().getHits().length;i++){
                SearchHit hit = sr.getHits().getAt(i);
                Map<String,Object> source = hit.getSource();
                PartRevisionKey partRevisionKey = new PartRevisionKey(extractValue(source, "workspaceId"), extractValue(source, "partNumber"), extractValue(source, "version"));
                try {
                    if(!listOfParts.contains(new PartRevisionDAO(em).loadPartR(partRevisionKey))){
                        listOfParts.add(new PartRevisionDAO(em).loadPartR(partRevisionKey));
                    }
                } catch (PartRevisionNotFoundException e) {
                    e.printStackTrace();                                                                                    // TODO PartNotFound error
                }
            }
            client.close();
            return listOfParts;
        }catch (NoNodeAvailableException e){
            Logger.getLogger(ESIndexer.class.getName()).log(Level.SEVERE, null, e);
            throw new IndexerServerException(Locale.getDefault(), "IndexerServerException1");
        }

    }

    /**
     * Search a part
     * @param query PartSearchQuery
     * @return List of part revision
     */
    public List<Object> search(SearchQuery query) throws IndexerServerException {
        try{
            Client client = createClient();
            QueryBuilder qr = getQueryBuilder(query);
            MultiSearchRequestBuilder srbm = client.prepareMultiSearch();
            srbm.add(getSearchRequest(client, query.getWorkspaceId().toLowerCase(), "document", qr));
            srbm.add(getSearchRequest(client, query.getWorkspaceId().toLowerCase(), "part", qr));
            MultiSearchResponse srm = srbm.execute().actionGet();

            List<Object> ret= new ArrayList<>();
            MultiSearchResponse.Item sri = srm.getResponses()[0];
            SearchResponse sr = sri.getResponse();
            for(int i=0; i<sr.getHits().getHits().length;i++){
                SearchHit hit = sr.getHits().getAt(i);
                Map<String,Object> source = hit.getSource();
                DocumentRevisionKey documentRevisionKey = new DocumentRevisionKey(source.get("workspaceId").toString(),source.get("docMId").toString(),source.get("version").toString());
                try {
                    DocumentRevision docR = new DocumentRevisionDAO(em).loadDocR(documentRevisionKey);
                    if(!ret.contains(docR)){
                        ret.add(docR);
                    }
                } catch (DocumentRevisionNotFoundException e) {
                    e.printStackTrace();                                                                                    // TODO DocNotFound error
                }
            }

            sri = srm.getResponses()[1];
            sr = sri.getResponse();
            for(int i=0; i<sr.getHits().getHits().length;i++){
                SearchHit hit = sr.getHits().getAt(i);
                Map<String,Object> source = hit.getSource();
                PartRevisionKey partRevisionKey = new PartRevisionKey(source.get("workspaceId").toString(), source.get("partNumber").toString(), source.get("version").toString());
                try {
                    if(!ret.contains(new PartRevisionDAO(em).loadPartR(partRevisionKey))){
                        ret.add(new PartRevisionDAO(em).loadPartR(partRevisionKey));
                    }
                } catch (PartRevisionNotFoundException e) {
                    e.printStackTrace();                                                                                // TODO PartNotFound error
                }
            }
            client.close();
            return ret;
        }catch (NoNodeAvailableException e){
            Logger.getLogger(ESIndexer.class.getName()).log(Level.SEVERE, null, e);
            throw new IndexerServerException(Locale.getDefault(), "IndexerServerException1");
        }
    }

    /**
     * Search a document in all Workspace
     * @param docQuery DocumentSearchQuery
     * @return List of document master
     */
    public List<DocumentRevision> searchInAllWorkspace(DocumentSearchQuery docQuery) throws IndexerServerException {
        try{
            Client client = createClient();
            QueryBuilder qr = getQueryBuilder(docQuery);
            MultiSearchRequestBuilder srbm = client.prepareMultiSearch();
            WorkspaceDAO wDAO = new WorkspaceDAO(em);
            for(Workspace w : wDAO.getAll()){
                srbm.add(getSearchRequest(client, w.getId().toLowerCase(), "document", qr));
            }
            MultiSearchResponse srm = srbm.execute().actionGet();


            List<DocumentRevision> listOfDocuments = new ArrayList<>();
            for (MultiSearchResponse.Item sri : srm.getResponses()){
                if(!sri.isFailure()){
                    SearchResponse sr = sri.getResponse();
                    for(int i=0; i<sr.getHits().getHits().length;i++){
                        SearchHit hit = sr.getHits().getAt(i);
                        Map<String,Object> source = hit.getSource();
                        DocumentRevisionKey documentRevisionKey = new DocumentRevisionKey(source.get("workspaceId").toString(),source.get("docMId").toString(),source.get("version").toString());
                        try {
                            DocumentRevision docR = new DocumentRevisionDAO(em).loadDocR(documentRevisionKey);
                            if(!listOfDocuments.contains(docR)){
                                listOfDocuments.add(docR);
                            }
                        } catch (DocumentRevisionNotFoundException e) {
                            e.printStackTrace();                                                                        //TODO DocumentNotFound Error
                        }
                    }
                }
            }
            client.close();
            return listOfDocuments;
        }catch (NoNodeAvailableException e){
            Logger.getLogger(ESIndexer.class.getName()).log(Level.SEVERE, null, e);
            throw new IndexerServerException(Locale.getDefault(), "IndexerServerException1");
        }
    }

    /**
     * Search a part in all Workspace
     * @param partQuery PartSearchQuery
     * @return List of part revision
     */
    public List<PartRevision> searchInAllWorkspace(PartSearchQuery partQuery) throws IndexerServerException {
        try{
            Client client = createClient();
            QueryBuilder qr = getQueryBuilder(partQuery);
            MultiSearchRequestBuilder srbm = client.prepareMultiSearch();
            WorkspaceDAO wDAO = new WorkspaceDAO(em);
            for(Workspace w : wDAO.getAll()){
                srbm.add(getSearchRequest(client, w.getId().toLowerCase(), "part", qr));
            }
            MultiSearchResponse srm = srbm.execute().actionGet();


            List<PartRevision> listOfParts = new ArrayList<>();
            for (MultiSearchResponse.Item sri : srm.getResponses()){
                if(!sri.isFailure()){
                    SearchResponse sr = sri.getResponse();
                    for(int i=0; i<sr.getHits().getHits().length;i++){
                        SearchHit hit = sr.getHits().getAt(i);
                        Map<String,Object> source = hit.getSource();
                        PartRevisionKey partRevisionKey = new PartRevisionKey(source.get("workspaceId").toString(), source.get("partNumber").toString(), source.get("version").toString());
                        try {
                            if(!listOfParts.contains(new PartRevisionDAO(em).loadPartR(partRevisionKey))){
                                listOfParts.add(new PartRevisionDAO(em).loadPartR(partRevisionKey));
                            }
                        } catch (PartRevisionNotFoundException e) {
                            e.printStackTrace();                                                                            // TODO PartNotFound Error
                        }
                    }
                }
            }

            client.close();
            return listOfParts;
        }catch (NoNodeAvailableException e){
            Logger.getLogger(ESIndexer.class.getName()).log(Level.SEVERE, null, e);
            throw new IndexerServerException(Locale.getDefault(), "IndexerServerException1");
        }
    }

    /**
     * Search a part
     * @param query PartSearchQuery
     * @return List of part revision
     */
    public List<Object> searchInAllWorkspace(SearchQuery query) throws IndexerServerException {                                                       // TODO Optimize it
       /* List<DocumentRevision> listOfDocuments = searchInAllWorkspace((DocumentSearchQuery) query);
        List<PartRevision> listOfParts = searchInAllWorkspace((PartSearchQuery) query);

        List<Object> ret= new ArrayList<>();
        ret.addAll(listOfDocuments);
        ret.addAll(listOfParts);
        return ret;*/
        return null; //TODO
    }

    /**
     * Get the Index request for a documentIteration in ElasticSearch Cluster
     * @param doc The document iteration to index
     */
    private IndexRequestBuilder indexRequest(Client client, DocumentIteration doc){
        try{
            XContentBuilder jsonDoc = documentIterationToJSON(doc);
            return client.prepareIndex(doc.getWorkspaceId().toLowerCase(), "document", doc.getKey().toString())
                         .setSource(jsonDoc);
        }catch (NoNodeAvailableException e){
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get the Index request for a partIteration in ElasticSearch Cluster
     * @param part The part iteration to index
     */
    private IndexRequestBuilder indexRequest(Client client, PartIteration part){
        try{
            XContentBuilder jsonDoc = partIterationToJSON(part);
            return client.prepareIndex(part.getWorkspaceId().toLowerCase(), "part", part.getKey().toString())
                         .setSource(jsonDoc);
        }catch (NoNodeAvailableException e){
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Return a ElasticSearch Query for DocumentSearch
     * @param docQuery DocumentSearchQuery wanted
     * @return a ElasticSearch.QueryBuilder
     */
    private QueryBuilder getQueryBuilder(DocumentSearchQuery docQuery){
        QueryBuilder qr;
        if(docQuery.getDocMId() != null){
            qr = QueryBuilders.fuzzyLikeThisQuery().likeText(docQuery.getDocMId());
        }else{
            qr = QueryBuilders.boolQuery();
            if(docQuery.getTitle() != null){
                ((BoolQueryBuilder) qr).should(
                        QueryBuilders.fuzzyLikeThisFieldQuery("title").likeText(docQuery.getTitle()));
            }
            if(docQuery.getVersion() != null){
                ((BoolQueryBuilder) qr).should(
                        QueryBuilders.fuzzyLikeThisFieldQuery("version").likeText(docQuery.getVersion()));
            }
            if(docQuery.getAuthor() != null){
                ((BoolQueryBuilder) qr).should(
                        QueryBuilders.fuzzyLikeThisFieldQuery("author").likeText(docQuery.getAuthor()));
            }
            if(docQuery.getType() != null){
                ((BoolQueryBuilder) qr).should(
                        QueryBuilders.fuzzyLikeThisFieldQuery("type").likeText(docQuery.getType()));
            }
            if(docQuery.getCreationDateFrom() != null && docQuery.getCreationDateTo() != null){
                ((BoolQueryBuilder) qr).should(
                        QueryBuilders.rangeQuery("creationDate").from(docQuery.getCreationDateFrom()).to(docQuery.getCreationDateTo()));
            }
            if(docQuery.getAttributes() != null){
                for(DocumentSearchQuery.AbstractAttributeQuery attr:docQuery.getAttributes()){
                    if (attr instanceof DocumentSearchQuery.DateAttributeQuery) {
                        ((BoolQueryBuilder) qr).should(QueryBuilders.rangeQuery(attr.getNameWithoutWhiteSpace()).from(((DocumentSearchQuery.DateAttributeQuery) attr).getFromDate()).to(((DocumentSearchQuery.DateAttributeQuery) attr).getToDate()));
                    } else if (attr instanceof DocumentSearchQuery.TextAttributeQuery) {
                        ((BoolQueryBuilder) qr).should(QueryBuilders.fuzzyLikeThisFieldQuery(attr.getNameWithoutWhiteSpace()).likeText(((DocumentSearchQuery.TextAttributeQuery) attr).getTextValue()));
                    } else if (attr instanceof DocumentSearchQuery.NumberAttributeQuery) {
                        ((BoolQueryBuilder) qr).should(QueryBuilders.fuzzyLikeThisFieldQuery(attr.getNameWithoutWhiteSpace()).likeText("" + ((DocumentSearchQuery.NumberAttributeQuery) attr).getNumberValue()));
                    } else if (attr instanceof DocumentSearchQuery.BooleanAttributeQuery) {
                        ((BoolQueryBuilder) qr).should(QueryBuilders.fuzzyLikeThisFieldQuery(attr.getNameWithoutWhiteSpace()).likeText(""+((DocumentSearchQuery.BooleanAttributeQuery) attr).isBooleanValue()));
                    } else if (attr instanceof DocumentSearchQuery.URLAttributeQuery) {
                        ((BoolQueryBuilder) qr).should(QueryBuilders.fuzzyLikeThisFieldQuery(attr.getNameWithoutWhiteSpace()).likeText(((DocumentSearchQuery.URLAttributeQuery) attr).getUrlValue()));
                    }
                }
            }
            if(docQuery.getContent() != null) ((BoolQueryBuilder) qr).should(QueryBuilders.fuzzyLikeThisFieldQuery("files").likeText(docQuery.getContent()));
        }
        return qr;
    }

    /**
     * Return a ElasticSearch Query for PartSearch
     * @param partQuery PartSearchQuery wanted
     * @return a ElasticSearch.QueryBuilder
     */
    private QueryBuilder getQueryBuilder (PartSearchQuery partQuery){
        QueryBuilder qr;
        if(partQuery.getPartNumber() != null){
            qr = QueryBuilders.fuzzyLikeThisQuery().likeText(partQuery.getPartNumber());
        }else{
            qr = QueryBuilders.boolQuery();
            if(partQuery.getName() != null) ((BoolQueryBuilder) qr).should(QueryBuilders.fuzzyLikeThisFieldQuery("name").likeText(partQuery.getName()));
            if(partQuery.getVersion() != null) ((BoolQueryBuilder) qr).should(QueryBuilders.fuzzyLikeThisFieldQuery("version").likeText(partQuery.getVersion()));
            if(partQuery.getAuthor() != null) ((BoolQueryBuilder) qr).should(QueryBuilders.fuzzyLikeThisFieldQuery("author").likeText(partQuery.getAuthor()));
            if(partQuery.getType() != null) ((BoolQueryBuilder) qr).should(QueryBuilders.fuzzyLikeThisFieldQuery("type").likeText(partQuery.getType()));
            if(partQuery.isStandardPart() != null){
                if(partQuery.isStandardPart()){
                    ((BoolQueryBuilder) qr).should(QueryBuilders.fuzzyLikeThisFieldQuery("standardPart").likeText("TRUE"));
                }else{
                    ((BoolQueryBuilder) qr).should(QueryBuilders.fuzzyLikeThisFieldQuery("standardPart").likeText("FALSE"));
                }
            }
            if(partQuery.getCreationDateFrom() != null && partQuery.getCreationDateTo() != null){
                ((BoolQueryBuilder) qr).should(QueryBuilders.rangeQuery("creationDate").from(partQuery.getCreationDateFrom()).to(partQuery.getCreationDateTo()));}
            if(partQuery.getAttributes() != null){
                for(PartSearchQuery.AbstractAttributeQuery attr:partQuery.getAttributes()){
                    if (attr instanceof PartSearchQuery.DateAttributeQuery) {
                        ((BoolQueryBuilder) qr).should(QueryBuilders.rangeQuery(attr.getNameWithoutWhiteSpace()).from(((PartSearchQuery.DateAttributeQuery) attr).getFromDate()).to(((PartSearchQuery.DateAttributeQuery) attr).getToDate()));
                    } else if (attr instanceof PartSearchQuery.TextAttributeQuery) {
                        ((BoolQueryBuilder) qr).should(QueryBuilders.fuzzyLikeThisFieldQuery(attr.getNameWithoutWhiteSpace()).likeText(((PartSearchQuery.TextAttributeQuery) attr).getTextValue()));
                    } else if (attr instanceof PartSearchQuery.NumberAttributeQuery) {
                        ((BoolQueryBuilder) qr).should(QueryBuilders.fuzzyLikeThisFieldQuery(attr.getNameWithoutWhiteSpace()).likeText(""+((PartSearchQuery.NumberAttributeQuery) attr).getNumberValue()));
                    } else if (attr instanceof PartSearchQuery.BooleanAttributeQuery) {
                        ((BoolQueryBuilder) qr).should(QueryBuilders.fuzzyLikeThisFieldQuery(attr.getNameWithoutWhiteSpace()).likeText(""+((PartSearchQuery.BooleanAttributeQuery) attr).isBooleanValue()));
                    } else if (attr instanceof PartSearchQuery.URLAttributeQuery) {
                        ((BoolQueryBuilder) qr).should(QueryBuilders.fuzzyLikeThisFieldQuery(attr.getNameWithoutWhiteSpace()).likeText(((PartSearchQuery.URLAttributeQuery) attr).getUrlValue()));
                    }
                }
            }
        }
        return qr;
    }

    /**
     * Return a ElasticSearch Query for a document or a part research
     * @param query SearchQuery wanted
     * @return a ElasticSearch.QueryBuilder
     */
    private QueryBuilder getQueryBuilder(SearchQuery query) {
        QueryBuilder qr;
        if(query.getFullText() != null){
            qr = QueryBuilders.fuzzyLikeThisQuery().likeText(query.getFullText());
        }else{
            qr = QueryBuilders.boolQuery();
            if(query.getVersion() != null) ((BoolQueryBuilder) qr).should(QueryBuilders.fuzzyLikeThisFieldQuery("version").likeText(query.getVersion()));
            if(query.getAuthor() != null) ((BoolQueryBuilder) qr).should(QueryBuilders.fuzzyLikeThisFieldQuery("author").likeText(query.getAuthor()));
            if(query.getType() != null) ((BoolQueryBuilder) qr).should(QueryBuilders.fuzzyLikeThisFieldQuery("type").likeText(query.getType()));
            if(query.getCreationDateFrom() != null && query.getCreationDateTo() != null){
                ((BoolQueryBuilder) qr).should(QueryBuilders.rangeQuery("creationDate").from(query.getCreationDateFrom()).to(query.getCreationDateTo()));}
            if(query.getAttributes() != null){
                for(PartSearchQuery.AbstractAttributeQuery attr:query.getAttributes()){
                    if (attr instanceof PartSearchQuery.DateAttributeQuery) {
                        ((BoolQueryBuilder) qr).should(QueryBuilders.rangeQuery(attr.getNameWithoutWhiteSpace()).from(((PartSearchQuery.DateAttributeQuery) attr).getFromDate()).to(((PartSearchQuery.DateAttributeQuery) attr).getToDate()));
                    } else if (attr instanceof PartSearchQuery.TextAttributeQuery) {
                        ((BoolQueryBuilder) qr).should(QueryBuilders.fuzzyLikeThisFieldQuery(attr.getNameWithoutWhiteSpace()).likeText(((PartSearchQuery.TextAttributeQuery) attr).getTextValue()));
                    } else if (attr instanceof PartSearchQuery.NumberAttributeQuery) {
                        ((BoolQueryBuilder) qr).should(QueryBuilders.fuzzyLikeThisFieldQuery(attr.getNameWithoutWhiteSpace()).likeText(""+((PartSearchQuery.NumberAttributeQuery) attr).getNumberValue()));
                    } else if (attr instanceof PartSearchQuery.BooleanAttributeQuery) {
                        ((BoolQueryBuilder) qr).should(QueryBuilders.fuzzyLikeThisFieldQuery(attr.getNameWithoutWhiteSpace()).likeText(""+((PartSearchQuery.BooleanAttributeQuery) attr).isBooleanValue()));
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
     * @param workspaceId Workspace of research
     * @param type Type of resource searched
     * @param pQuery Search criterion
     * @return the uniWorkspace Search Request
     */
    private SearchRequestBuilder getSearchRequest(Client client, String workspaceId, String type, QueryBuilder pQuery){
        return client.prepareSearch(workspaceId)
                     .setTypes(type)
                     .setSearchType(SearchType.QUERY_THEN_FETCH)
                     .setQuery(pQuery);
    }

    /**
     * Convert a Document Iteration to a JSON Builder
     * @param doc Document to pass to JSON
     * @return A JSON Builder to index
     */
    private XContentBuilder documentIterationToJSON(DocumentIteration doc){
        try {
            float nbIteration = doc.getDocumentRevision().getLastIteration().getIteration();                              // Calcul of the number of iteration
            float seniority = nbIteration - doc.getIteration();                                                         // Calcul of iteration seniority
            float coef = 1 - (seniority/nbIteration);                                                                   // Calcul of decrease factor
            if(coef < 0.40) coef = 0.40f;
            XContentBuilder tmp = XContentFactory.jsonBuilder()                                                         
                .startObject();
                    if(doc.getWorkspaceId() != null){
                        tmp.field("workspaceId", doc.getWorkspaceId(), coef);
                    }
                    if(doc.getDocumentRevision().getDocumentMasterId() != null){
                        tmp.field("docMId", doc.getDocumentRevision().getDocumentMasterId(), (1.75 * coef));
                    }
                    if(doc.getDocumentRevision().getTitle() != null && ! doc.getDocumentRevision().getTitle().equals("")){
                        tmp.field("title", doc.getDocumentRevision().getTitle(), (3.0 * coef));
                    }
                    if(doc.getDocumentVersion() != null){
                        tmp.field("version", doc.getDocumentVersion(), coef);
                    }
                    if(doc.getIteration() > 0){
                        tmp.field("iteration", "" + doc.getIteration(), coef);
                    }
                    if(doc.getAuthor() != null){
                        tmp.field("author", doc.getAuthor(), coef);
                    }
                    if(doc.getDocumentRevision().getDocumentMaster().getType() != null){
                        tmp.field("type", doc.getDocumentRevision().getDocumentMaster().getType(), coef);
                    }
                    if(doc.getDocumentRevision().getCreationDate() != null){
                        tmp.field("creationDate", doc.getDocumentRevision().getCreationDate(), coef);
                    }
                    if(doc.getDocumentRevision().getDescription() != null && ! doc.getDocumentRevision().getDescription().equals("")){
                        tmp.field("description", doc.getDocumentRevision().getDescription(), coef);
                    }
                    if(doc.getRevisionNote() != null){
                        tmp.field("revisionNote", doc.getRevisionNote(), (0.50f * coef));
                    }
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
                                tmp.field(attr.getNameWithoutWhiteSpace(),attr.getValue(), coef);
                            }
                        tmp.endObject();
                    }
                    if(!doc.getAttachedFiles().isEmpty()){
                        tmp.startObject("files");
                        for (BinaryResource bin : doc.getAttachedFiles()) {
                            try {
                                tmp.startObject(bin.getName());
                                    tmp.field("name",bin.getName(), coef);
                                    String str = streamToString(bin.getFullName(),dataManager.getBinaryResourceInputStream(bin));
                                    tmp.field("content",str, coef);
                                tmp.endObject();
                            } catch (StorageException e) {
                                e.printStackTrace();
                            }
                        }
                        tmp.endObject();
                    }
                tmp.endObject();
            return tmp;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

    }

    /**
     * Convert a Part Iteration to a JSON Builder
     * @param part Part to pass to JSON
     * @return A JSON Builder to index
     */
    private XContentBuilder partIterationToJSON(PartIteration part) {
        try {
            float nbIteration = part.getPartRevision().getLastIteration().getIteration();                               // Calcul of the number of iteration
            float seniority = nbIteration - part.getIteration();                                                        // Calcul of iteration seniority
            float coef = 1 - (seniority/nbIteration);                                                                   // Calcul of decrease factor
            if(coef < 0.40) coef = 0.40f;
            XContentBuilder tmp = XContentFactory.jsonBuilder()
                .startObject();
                    if(part.getWorkspaceId() != null){
                        tmp.field("workspaceId", part.getWorkspaceId(), coef);
                    }
                    if(part.getPartNumber() != null){
                        tmp.field("partNumber", part.getPartNumber(), (1.75 * coef));
                    }
                    if(part.getPartRevision().getPartMaster().getName() != null && ! part.getPartRevision().getPartMaster().getName().equals("")){
                        tmp.field("name", part.getPartRevision().getPartMaster().getName(), (3.0 * coef));
                    }
                    if(part.getPartVersion() != null){
                        tmp.field("version", part.getPartVersion(), coef);
                    }
                    if(part.getIteration() > 0){
                        tmp.field("iteration", "" + part.getIteration(), coef);
                    }
                    tmp.field("standardPart", part.getPartRevision().getPartMaster().isStandardPart(), coef);
                    if(part.getAuthor() != null){
                        tmp.field("author", part.getAuthor(), coef);
                    }
                    if(part.getPartRevision().getPartMaster().getType() != null){
                        tmp.field("type", part.getPartRevision().getPartMaster().getType(), coef);
                    }
                    if(part.getCreationDate() != null){
                        tmp.field("creationDate", part.getCreationDate(), coef);
                    }
                    if(part.getPartRevision().getDescription() != null && !part.getPartRevision().getDescription().equals("")){
                        tmp.field("description", part.getPartRevision().getDescription(), coef);
                    }
                    if(part.getIterationNote() != null){
                        tmp.field("revisionNote", part.getIterationNote(), (0.5 * coef));
                    }
                    if(! part.getInstanceAttributes().isEmpty()){
                        tmp.startObject("attributes");
                            Collection<InstanceAttribute> listAttr = part.getInstanceAttributes().values();
                            for(InstanceAttribute attr:listAttr){
                                tmp.field(attr.getNameWithoutWhiteSpace(),attr.getValue(), coef);
                            }
                        tmp.endObject();
                    }
                    if(! part.getAttachedFiles().isEmpty()){
                        tmp.startObject("files");
                        for (BinaryResource bin : part.getAttachedFiles()) {
                            try {
                                tmp.startObject(bin.getName());
                                tmp.field("name",bin.getName(), coef);
                                String str = streamToString(bin.getFullName(),dataManager.getBinaryResourceInputStream(bin));
                                tmp.field("content",str, coef);
                                tmp.endObject();
                            } catch (StorageException e) {
                                e.printStackTrace();
                            }
                        }
                        tmp.endObject();
                    }
                tmp.endObject();
            return tmp;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Extract a value from a ES result
     *
     * @param source Source of a ES hit
     * @param key Key of the field to extract
     * @return The value of the field "key"
     */
    private String extractValue(Map<String, Object> source, String key){
        Object ret = source.get(key);
        if(ret instanceof List){
            return ((List) ret).get(0).toString();
        }else{
            return ret.toString();
        }
    }


    /**
     * Get Stream for a Bin Resource
     * @param fullName The full name of the resource
     * @param inputStream Stream of the resource
     * @return String to index
     */
    private String streamToString(String fullName, InputStream inputStream){
        String strRet = "";

        try {
            int lastDotIndex = fullName.lastIndexOf('.');
            String extension = "";
            if (lastDotIndex != -1) {
                extension = fullName.substring(lastDotIndex);
            }

            switch (extension){
                case ".odt":
                case ".ods":
                case ".odp":
                case ".odg":
                case ".odc":
                case ".odf":
                case ".odb":
                case ".odi":
                case ".odm":                                                                                            // OpenOffice Documents
                    final StringBuilder text = new StringBuilder();
                    ZipInputStream zipOpenDoc = new ZipInputStream(new BufferedInputStream(inputStream));
                    ZipEntry zipEntry;
                    while ((zipEntry = zipOpenDoc.getNextEntry()) != null) {
                        if (zipEntry.getName().equals("content.xml")) {
                            SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
                            SAXParser parser = saxParserFactory.newSAXParser();
                            parser.parse(zipOpenDoc, new DefaultHandler() {

                                @Override
                                public void characters(char[] ch,
                                                       int start,
                                                       int length)
                                        throws SAXException {
                                    for (int i = start; i < start + length; i++) {
                                        text.append(ch[i]);
                                    }
                                    text.append("\r\n");
                                }
                            });
                            break;
                        }
                    }
                    zipOpenDoc.close();
                    strRet = text.toString();
                    break;
                case ".doc":                                                                                            //MSWord Documents
                    InputStream wordStream = new BufferedInputStream(inputStream);
                    WordExtractor wordExtractor = new WordExtractor(wordStream);
                    strRet = wordExtractor.getText();
                    wordStream.close();
                    break;
                case ".docx":                                                                                           //XMLMSWord Documents
                    InputStream wordXStream = new BufferedInputStream(inputStream);
                    XWPFWordExtractor wordXExtractor = new XWPFWordExtractor(new XWPFDocument(wordXStream));
                    strRet = wordXExtractor.getText();
                    wordXStream.close();
                    break;
                case ".ppt":
                case ".pps":                                                                                            //MSPowerPoint Document
                    InputStream pptStream = new BufferedInputStream(inputStream);
                    PowerPointExtractor pptExtractor = new PowerPointExtractor(pptStream);
                    strRet = pptExtractor.getText(true, true);
                    pptStream.close();
                    break;
                case ".txt":                                                                                            //Text Document
                case ".csv":                                                                                            //CSV Document
                    strRet = new Scanner(inputStream,"UTF-8").useDelimiter("\\A").next();
                    break;
                case ".xls":                                                                                            //MSExcelExtractor Document
                    POIFSFileSystem excelStream = new POIFSFileSystem(inputStream);
                    ExcelExtractor excelExtractor= new ExcelExtractor(excelStream);
                    strRet = excelExtractor.getText();
                    break;
                case ".pdf":                                                                                            // PDF Document
                    PdfReader reader = new PdfReader(inputStream);
                    strRet = "";
                    for(int i=1; i<=reader.getNumberOfPages(); i++){
                        strRet += PdfTextExtractor.getTextFromPage(reader,i);
                    }
                    reader.close();
                    break;
                case ".html":
                case ".htm":
                case ".xml":
                case ".rtf":
                case ".msg":
                default: break;
            }
        } catch (ParserConfigurationException|SAXException|IOException ex) {
            throw new EJBException(ex);
        }
        return strRet;
    }
}