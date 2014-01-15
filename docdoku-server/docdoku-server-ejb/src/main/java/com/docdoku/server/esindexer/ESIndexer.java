package com.docdoku.server.esindexer;

import com.docdoku.core.common.BinaryResource;
import com.docdoku.core.common.Workspace;
import com.docdoku.core.document.*;
import com.docdoku.core.exceptions.DocumentMasterNotFoundException;
import com.docdoku.core.exceptions.PartRevisionNotFoundException;
import com.docdoku.core.exceptions.StorageException;
import com.docdoku.core.meta.InstanceAttribute;
import com.docdoku.core.product.*;
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

import javax.ejb.*;
import javax.jws.WebService;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Search Method using ElasticSearch API.
 *
 * @author Taylor LABEJOF
 * @version 0.1, 03/01/2014
 * @since   V0.1
 */
@Singleton(name="ESIndexer")
@ConcurrencyManagement(ConcurrencyManagementType.CONTAINER)
public class ESIndexer{
    public static final String HOST = "localhost";
    public static final int PORT = 9300;
    private Client client;

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
    private void createClient(){
        if(client== null){
            client = new TransportClient()
                    .addTransportAddress(new InetSocketTransportAddress(HOST, PORT));
        }
    }

    /**
     * Close connexion with the ElasticSearch Server
     */
    private void closeClient(){
        if(client != null){
            client.close();
            client = null;
        }
    }

    /**
     * Index all content in all workspace
     * @return True if all index success
     */
    public boolean indexAll(){
        return (indexAllDocument() && indexAllPart());
    }

    /**
     * Index all content in all workspace, using bulkRequest
     * @return True if all index success
     */
    @Asynchronous
    @Lock(LockType.WRITE)
    public void indexAllBulk(){
        createClient();
        BulkRequestBuilder bulkRequest = client.prepareBulk();
        WorkspaceDAO wDAO = new WorkspaceDAO(em);
        DocumentMasterDAO docMasterDAO = new DocumentMasterDAO(em);
        PartMasterDAO partMasterDAO = new PartMasterDAO(em);
        for(Workspace w : wDAO.getAll()){
            for(DocumentMaster docMaster : docMasterDAO.getAllByWorkspace(w.getId())){
                for(DocumentIteration docIte : docMaster.getDocumentIterations()){
                    bulkRequest.add(indexRequest(docIte));
                }
            }
            for(PartMaster partMaster : partMasterDAO.getAllByWorkspace(w.getId())){
                for(PartRevision partRev : partMaster.getPartRevisions()){
                    for(PartIteration partIte : partRev.getPartIterations()){
                        bulkRequest.add(indexRequest(partIte));
                    }
                }
            }
        }

        BulkResponse bulkResponse = bulkRequest.execute().actionGet();
        closeClient();
        if (bulkResponse.hasFailures()) {                                                                               // TODO Failure case
            System.out.println(bulkResponse.buildFailureMessage());
        }
    }

    /**
     * Index all document in all workspace
     * @return True if the index of all documents success
     */
    public boolean indexAllDocument(){
        try{
            WorkspaceDAO wDAO = new WorkspaceDAO(em);
            DocumentMasterDAO docMasterDAO = new DocumentMasterDAO(em);
            for(Workspace w : wDAO.getAll()){
                for(DocumentMaster docMaster : docMasterDAO.getAllByWorkspace(w.getId())){
                    for(DocumentIteration docIte : docMaster.getDocumentIterations()){
                        index(docIte);
                    }
                }
            }
            return true;
        }catch (NoNodeAvailableException e){
            return false;
        }
    }

    /**
     * Index all document in all workspace
     * @return True if the index of all parts success
     */
    public boolean indexAllPart(){
        try{
            WorkspaceDAO wDAO = new WorkspaceDAO(em);
            PartMasterDAO partMasterDAO = new PartMasterDAO(em);
            for(Workspace w : wDAO.getAll()){
                for(PartMaster partMaster : partMasterDAO.getAllByWorkspace(w.getId())){
                    for(PartRevision partRev : partMaster.getPartRevisions()){
                        for(PartIteration partIte : partRev.getPartIterations()){
                            index(partIte);
                        }
                    }
                }
            }
            return true;
        }catch (NoNodeAvailableException e){
            return false;
        }
    }

    /**
     * Index a documentIteration in ElasticSearch Cluster
     * @param doc The document iteration to index
     */
    @Asynchronous
    @Lock(LockType.WRITE)
    public void index(DocumentIteration doc){
        try{
            XContentBuilder jsonDoc = documentIterationToJSON(doc);
            createClient();
            client.prepareIndex(doc.getWorkspaceId().toLowerCase(), "document", doc.getKey().toString())
                    .setSource(jsonDoc)
                    .execute()
                    .actionGet();
            closeClient();
        }catch (NoNodeAvailableException e){
            e.printStackTrace();
        }
    }

    /**
     * Index a partIteration in ElasticSearch Cluster
     * @param part The part iteration to index
     */
    @Asynchronous
    @Lock(LockType.WRITE)
    public void index(PartIteration part){
        try{
            XContentBuilder jsonDoc = partIterationToJSON(part);
            createClient();
            client.prepareIndex(part.getWorkspaceId().toLowerCase(), "part", part.getKey().toString())
                    .setSource(jsonDoc)
                    .execute()
                    .actionGet();
            closeClient();
        }catch (NoNodeAvailableException e){
            e.printStackTrace();
        }
    }

    /**
     * Get the Index request for a documentIteration in ElasticSearch Cluster
     * @param doc The document iteration to index
     */
    private IndexRequestBuilder indexRequest(DocumentIteration doc){
        try{
            XContentBuilder jsonDoc = documentIterationToJSON(doc);
            IndexRequestBuilder irb = client.prepareIndex(doc.getWorkspaceId().toLowerCase(), "document", doc.getKey().toString())
                    .setSource(jsonDoc);
            return irb;
        }catch (NoNodeAvailableException e){
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get the Index request for a partIteration in ElasticSearch Cluster
     * @param part The part iteration to index
     */
    private IndexRequestBuilder indexRequest(PartIteration part){
        try{
            XContentBuilder jsonDoc = partIterationToJSON(part);
            IndexRequestBuilder irb = client.prepareIndex(part.getWorkspaceId().toLowerCase(), "part", part.getKey().toString())
                    .setSource(jsonDoc);
            return irb;
        }catch (NoNodeAvailableException e){
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Remove this docIteration from ElasticSearch Cluster
     * @param doc The document iteration to remove from index
     */
    public void rmIndex(DocumentIteration doc){
        createClient();
        client.prepareDelete(doc.getWorkspaceId().toLowerCase(), "document", doc.getKey().toString())
                .execute()
                .actionGet();
        closeClient();
    }

    /**
     * Remove this partIteration from ElasticSearch Cluster
     * @param part The part iteration to remove from index
     */
    public void rmIndex(PartIteration part){
        createClient();
        client.prepareDelete(part.getWorkspaceId().toLowerCase(), "part", part.getKey().toString())
                .execute()
                .actionGet();
        closeClient();
    }

    /**
     * Search a document
     * @param docQuery DocumentSearchQuery
     * @return List of document master
     */
    public List<DocumentMaster> search(DocumentSearchQuery docQuery){
        try{
            createClient();
            QueryBuilder qr = getDocumentQueryBuilder(docQuery);
            SearchRequestBuilder srb = getSearchRequest(docQuery.getWorkspaceId().toLowerCase(), "document", qr);
            SearchResponse sr = srb.execute().actionGet();


            List<DocumentMaster> listOfDocuments = new ArrayList<>();
            for(int i=0; i<sr.getHits().getHits().length;i++){
                SearchHit hit = sr.getHits().getAt(i);
                Map<String,Object> source = hit.getSource();
                DocumentMasterKey docMasterKey = new DocumentMasterKey(extractValue(source, "workspaceId"),extractValue(source, "docMId"),extractValue(source, "version"));
                if(!listOfDocuments.contains(new DocumentMasterDAO(em).loadDocM(docMasterKey))){
                    listOfDocuments.add(new DocumentMasterDAO(em).loadDocM(docMasterKey));
                }
            }

            closeClient();
            return listOfDocuments;
        }catch (NoNodeAvailableException e){
            return null;
        }catch (Exception e){
            return null;
        }
    }

    /**
     * Search a part
     * @param partQuery PartSearchQuery
     * @return List of part revision
     */
    public List<PartRevision> search(PartSearchQuery partQuery){
        try{
            createClient();
            QueryBuilder qr = getPartQueryBuilder(partQuery);
            SearchRequestBuilder srb = getSearchRequest(partQuery.getWorkspaceId().toLowerCase(), "part", qr);
            SearchResponse sr = srb.execute().actionGet();

            List<PartRevision> listOfParts = new ArrayList<>();
            for(int i=0; i<sr.getHits().getHits().length;i++){
                SearchHit hit = sr.getHits().getAt(i);
                Map<String,Object> source = hit.getSource();
                PartRevisionKey partRevisionKey = new PartRevisionKey(extractValue(source, "workspaceId"), extractValue(source, "partNumber"), extractValue(source, "version"));
                if(!listOfParts.contains(new PartRevisionDAO(em).loadPartR(partRevisionKey))){
                    listOfParts.add(new PartRevisionDAO(em).loadPartR(partRevisionKey));
                }
            }
            closeClient();
            return listOfParts;

        }catch (NoNodeAvailableException e){
            return null;
        } catch (PartRevisionNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Search a part
     * @param query PartSearchQuery
     * @return List of part revision
     */
    public List<Objects> search(SearchQuery query){
        try{
            createClient();
            QueryBuilder qr = getQueryBuilder(query);
            MultiSearchRequestBuilder srbm = client.prepareMultiSearch();
            srbm.add(getSearchRequest(query.getWorkspaceId().toLowerCase(), "document", qr));
            srbm.add(getSearchRequest(query.getWorkspaceId().toLowerCase(), "part", qr));
            MultiSearchResponse srm = srbm.execute().actionGet();

            List<DocumentMaster> listOfDocuments = new ArrayList<>();
            MultiSearchResponse.Item sri = srm.getResponses()[0];
            SearchResponse sr = sri.getResponse();
            for(int i=0; i<sr.getHits().getHits().length;i++){
                SearchHit hit = sr.getHits().getAt(i);
                Map<String,Object> source = hit.getSource();
                DocumentMasterKey docMasterKey = new DocumentMasterKey(source.get("workspaceId").toString(),source.get("docMId").toString(),source.get("version").toString());
                if(!listOfDocuments.contains(new DocumentMasterDAO(em).loadDocM(docMasterKey))){
                    listOfDocuments.add(new DocumentMasterDAO(em).loadDocM(docMasterKey));
                }
            }

            List<PartRevision> listOfParts = new ArrayList<>();
            sri = srm.getResponses()[1];
            sr = sri.getResponse();
            for(int i=0; i<sr.getHits().getHits().length;i++){
                SearchHit hit = sr.getHits().getAt(i);
                Map<String,Object> source = hit.getSource();
                PartRevisionKey partRevisionKey = new PartRevisionKey(source.get("workspaceId").toString(), source.get("partNumber").toString(), source.get("version").toString());
                if(!listOfParts.contains(new PartRevisionDAO(em).loadPartR(partRevisionKey))){
                    listOfParts.add(new PartRevisionDAO(em).loadPartR(partRevisionKey));
                }
            }
            closeClient();
            List ret= new ArrayList();
            ret.addAll(listOfDocuments);
            ret.addAll(listOfParts);
            return ret;
        }catch (NoNodeAvailableException e){
            return null;
        } catch (PartRevisionNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (DocumentMasterNotFoundException e) {
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
     * Search a document in all Workspace
     * @param docQuery DocumentSearchQuery
     * @return List of document master
     */
    public List<DocumentMaster> searchInAllWorkspace(DocumentSearchQuery docQuery){
        try{
            createClient();
            QueryBuilder qr = getDocumentQueryBuilder(docQuery);
            MultiSearchRequestBuilder srbm = client.prepareMultiSearch();
            WorkspaceDAO wDAO = new WorkspaceDAO(em);
            for(Workspace w : wDAO.getAll()){
                srbm.add(getSearchRequest(w.getId().toLowerCase(), "document", qr));
            }
            MultiSearchResponse srm = srbm.execute().actionGet();


            List<DocumentMaster> listOfDocuments = new ArrayList<>();
            for (MultiSearchResponse.Item sri : srm.getResponses()){
                if(!sri.isFailure()){
                    SearchResponse sr = sri.getResponse();
                    for(int i=0; i<sr.getHits().getHits().length;i++){
                        SearchHit hit = sr.getHits().getAt(i);
                        Map<String,Object> source = hit.getSource();
                        DocumentMasterKey docMasterKey = new DocumentMasterKey(source.get("workspaceId").toString(),source.get("docMId").toString(),source.get("version").toString());
                        if(!listOfDocuments.contains(new DocumentMasterDAO(em).loadDocM(docMasterKey))){
                            listOfDocuments.add(new DocumentMasterDAO(em).loadDocM(docMasterKey));
                        }
                    }
                }
            }
            closeClient();
            return listOfDocuments;
        }catch (NoNodeAvailableException e){
            return null;
        }catch (Exception e){
            return null;
        }
    }

    /**
     * Search a part in all Workspace
     * @param partQuery PartSearchQuery
     * @return List of part revision
     */
    public List<PartRevision> searchInAllWorkspace(PartSearchQuery partQuery){
        try{
            createClient();
            QueryBuilder qr = getPartQueryBuilder(partQuery);
            MultiSearchRequestBuilder srbm = client.prepareMultiSearch();
            WorkspaceDAO wDAO = new WorkspaceDAO(em);
            for(Workspace w : wDAO.getAll()){
                srbm.add(getSearchRequest(partQuery.getWorkspaceId().toLowerCase(), "part", qr));
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
                        if(!listOfParts.contains(new PartRevisionDAO(em).loadPartR(partRevisionKey))){
                            listOfParts.add(new PartRevisionDAO(em).loadPartR(partRevisionKey));
                        }
                    }
                }
            }

            createClient();
            return listOfParts;

        }catch (NoNodeAvailableException e){
            return null;
        } catch (PartRevisionNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Search a part
     * @param query PartSearchQuery
     * @return List of part revision
     */
    public List<Objects> searchInAllWorkspace(SearchQuery query){                                                       // TODO Optimize it
        try{
            List<DocumentMaster> listOfDocuments = searchInAllWorkspace((DocumentSearchQuery) query);
            List<PartRevision> listOfParts = searchInAllWorkspace((PartSearchQuery) query);

            List ret= new ArrayList();
            ret.addAll(listOfDocuments);
            ret.addAll(listOfParts);
            return ret;
        }catch (NoNodeAvailableException e){
            return null;
        }
    }

    /**
     * Return a ElasticSearch Query for DocumentSearch
     * @param docQuery DocumentSearchQuery wanted
     * @return a ElasticSearch.QueryBuilder
     */
    private QueryBuilder getDocumentQueryBuilder (DocumentSearchQuery docQuery){
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
    private QueryBuilder getPartQueryBuilder (PartSearchQuery partQuery){
        QueryBuilder qr;
        if(partQuery.getPartNumber() != null){
            qr = QueryBuilders.fuzzyLikeThisQuery().likeText(partQuery.getPartNumber());
        }else{
            qr = QueryBuilders.boolQuery();
            if(partQuery.getName() != null) ((BoolQueryBuilder) qr).should(QueryBuilders.fuzzyLikeThisFieldQuery("id").likeText(partQuery.getName()));
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
    private SearchRequestBuilder getSearchRequest(String workspaceId, String type, QueryBuilder pQuery){
        SearchRequestBuilder srb = client.prepareSearch(workspaceId)
                .setTypes(type)
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .setQuery(pQuery);
        return srb;
    }

    /**
     * Convert a Document Iteration to a JSON Builder
     * @param doc Document to pass to JSON
     * @return A JSON Builder to index
     */
    private XContentBuilder documentIterationToJSON(DocumentIteration doc){
        try {
            float nbIteration = doc.getDocumentMaster().getLastIteration().getIteration();                              // Calcul of the number of iteration
            float seniority = nbIteration - doc.getIteration();                                                         // Calcul of iteration seniority
            float coef = 1 - (seniority/nbIteration);                                                                   // Calcul of decrease factor
            if(coef < 0.40) coef = 0.40f;
            XContentBuilder tmp = XContentFactory.jsonBuilder()                                                         
                .startObject();
                    if(doc.getWorkspaceId() != null){
                        tmp.field("workspaceId", doc.getWorkspaceId(), coef);
                    }
                    if(doc.getDocumentMasterId() != null){
                        tmp.field("docMId", doc.getDocumentMasterId(), (1.5 * coef));
                    }
                    if(doc.getDocumentMaster().getTitle() != null && ! doc.getDocumentMaster().getTitle().equals("")){
                        tmp.field("title", doc.getDocumentMaster().getTitle(), (2.0 * coef));
                    }
                    if(doc.getDocumentMasterVersion() != null){
                        tmp.field("version", doc.getDocumentMasterVersion(), coef);
                    }
                    if(doc.getIteration() > 0){
                        tmp.field("iteration", "" + doc.getIteration(), coef);
                    }
                    if(doc.getAuthor() != null){
                        tmp.field("author", doc.getAuthor(), coef);
                    }
                    if(doc.getDocumentMaster().getType() != null){
                        tmp.field("type", doc.getDocumentMaster().getType(), coef);
                    }
                    if(doc.getDocumentMaster().getCreationDate() != null){
                        tmp.field("creationDate", doc.getDocumentMaster().getCreationDate(), coef);
                    }
                    if(doc.getDocumentMaster().getDescription() != null && ! doc.getDocumentMaster().getDescription().equals("")){
                        tmp.field("description", doc.getDocumentMaster().getDescription(), coef);
                    }
                    if(doc.getDocumentMaster().getCheckOutUser() != null){
                        tmp.field("checkOutUser", doc.getDocumentMaster().getCheckOutUser(), coef);
                    }
                    if(doc.getDocumentMaster().getCheckOutDate() != null){
                        tmp.field("checkOutDate", doc.getDocumentMaster().getCheckOutDate(), coef);
                    }
                    if(doc.getRevisionNote() != null){
                        tmp.field("revisionNote", doc.getRevisionNote(), (0.50f * coef));
                    }
                    if(!doc.getDocumentMaster().getTags().isEmpty()){
                        tmp.startArray("tags");
                            for(Tag tag:doc.getDocumentMaster().getTags()){
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
                        tmp.field("partNumber", part.getPartNumber(), (1.5 * coef));
                    }
                    if(part.getPartRevision().getPartMaster().getName() != null && ! part.getPartRevision().getPartMaster().getName().equals("")){
                        tmp.field("name", part.getPartRevision().getPartMaster().getName(), (2.0 * coef));
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
                    if(part.getPartRevision().getCheckOutUser() != null){
                        tmp.field("checkOutUser", part.getPartRevision().getCheckOutUser(), coef);
                    }
                    if(part.getPartRevision().getCreationDate() != null){
                        tmp.field("checkOutDate", part.getPartRevision().getCheckOutDate(), coef);
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

            if (extension.equals(".odt")
                    || extension.equals(".ods")
                    || extension.equals(".odp")
                    || extension.equals(".odg")
                    || extension.equals(".odc")
                    || extension.equals(".odf")
                    || extension.equals(".odb")
                    || extension.equals(".odi")
                    || extension.equals(".odm")) {
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
            } else if (extension.equals(".doc")) {                                                                      //MSWord Document
                InputStream wordStream = new BufferedInputStream(inputStream);
                WordExtractor wordExtractor = new WordExtractor(wordStream);
                strRet = wordExtractor.getText();
                wordStream.close();
            } else if (extension.equals(".docx")){
                InputStream wordStream = new BufferedInputStream(inputStream);                                          //XMLWord Document
                XWPFWordExtractor wordExtractor = new XWPFWordExtractor(new XWPFDocument(wordStream));
                strRet = wordExtractor.getText();
                wordStream.close();
            } else if (extension.equals(".ppt") || extension.equals(".pps")) {                                          //MSPowerPoint Document
                InputStream pptStream = new BufferedInputStream(inputStream);
                PowerPointExtractor pptExtractor = new PowerPointExtractor(pptStream);
                strRet = pptExtractor.getText(true, true);
                pptStream.close();
            } else if (extension.equals(".txt") || extension.equals(".csv")) {                                          //Text Document
                strRet = new Scanner(inputStream,"UTF-8").useDelimiter("\\A").next();
            } else if (extension.equals(".xls")) {                                                                      //MSExcelExtractor Document
                POIFSFileSystem excelStream = new POIFSFileSystem(inputStream);
                ExcelExtractor excelExtractor= new ExcelExtractor(excelStream);
                strRet = excelExtractor.getText();
            } else if (extension.equals(".html") || extension.equals(".htm")) {
            } else if (extension.equals(".xml")) {
            } else if (extension.equals(".rtf")) {
            } else if (extension.equals(".pdf")) {                                                                      //PDF Files
                PdfReader reader = new PdfReader(inputStream);
                strRet = "";
                for(int i=1; i<=reader.getNumberOfPages(); i++){
                    strRet += PdfTextExtractor.getTextFromPage(reader,i);
                }
                reader.close();
            } else if (extension.equals(".msg")) {
            }
        } catch (ParserConfigurationException ex) {
            throw new EJBException(ex);
        } catch (SAXException ex) {
            throw new EJBException(ex);
        } catch (IOException ex) {
            throw new EJBException(ex);
        }
        return strRet;
    }
}