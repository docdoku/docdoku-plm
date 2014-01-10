package com.docdoku.server.esindexer;

import com.docdoku.core.common.BinaryResource;
import com.docdoku.core.common.Workspace;
import com.docdoku.core.document.*;
import com.docdoku.core.exceptions.PartRevisionNotFoundException;
import com.docdoku.core.exceptions.StorageException;
import com.docdoku.core.meta.InstanceAttribute;
import com.docdoku.core.product.*;
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
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
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
 * @version 2.0, 03/01/2014
 * @since   V0.1
 */
@Singleton(name="ESIndexer")
@ConcurrencyManagement(ConcurrencyManagementType.CONTAINER)
public class ESIndexer {
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
     * @return True if all indexage sucess
     */
    public boolean indexAll(){
        return (indexAllDocument() && indexAllPart());
    }

    /**
     * Index all document in all workspace
     * @return True if documents indexage sucess
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
     * @return True if parts indexage sucess
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
     * Remove this docIteration from ElasticSearch Cluster
     * @param doc The document iteration to remove from index
     */
    @Asynchronous
    @Lock(LockType.WRITE)
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
    @Asynchronous
    @Lock(LockType.WRITE)
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
                DocumentMasterKey docMasterKey = new DocumentMasterKey(source.get("workspaceId").toString(),source.get("docMId").toString(),source.get("version").toString());
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
                PartRevisionKey partRevisionKey = new PartRevisionKey(source.get("workspaceId").toString(), source.get("partNumber").toString(), source.get("version").toString());
                if(!listOfParts.contains(new PartRevisionDAO(em).loadPartR(partRevisionKey))){
                    listOfParts.add(new PartRevisionDAO(em).loadPartR(partRevisionKey));
                }
            }
            closeClient();
            return listOfParts;

        }catch (NoNodeAvailableException e){
            return null;
        } catch (PartRevisionNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
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
            if(docQuery.getTitle() != null) ((BoolQueryBuilder) qr).should(QueryBuilders.fuzzyLikeThisFieldQuery("title").likeText(docQuery.getTitle()));
            if(docQuery.getVersion() != null) ((BoolQueryBuilder) qr).should(QueryBuilders.fuzzyLikeThisFieldQuery("version").likeText(docQuery.getVersion()));
            if(docQuery.getAuthor() != null) ((BoolQueryBuilder) qr).should(QueryBuilders.fuzzyLikeThisFieldQuery("author").likeText(docQuery.getAuthor()));
            if(docQuery.getType() != null) ((BoolQueryBuilder) qr).should(QueryBuilders.fuzzyLikeThisFieldQuery("type").likeText(docQuery.getType()));
            if(docQuery.getCreationDateFrom() != null && docQuery.getCreationDateTo() != null){
                ((BoolQueryBuilder) qr).should(QueryBuilders.rangeQuery("creationDate").from(docQuery.getCreationDateFrom()).to(docQuery.getCreationDateTo()));}
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
            if(docQuery.getContent() != null) ((BoolQueryBuilder) qr).should(QueryBuilders.fuzzyLikeThisFieldQuery("content").likeText(docQuery.getContent()));
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
            XContentBuilder tmp = XContentFactory.jsonBuilder()                                                                         // Creation de DocRoueA
                .startObject()
                    .field("workspaceId", doc.getWorkspaceId())
                    .field("docMId", doc.getDocumentMasterId())
                    .field("title", doc.getDocumentMaster().getTitle())
                    .field("version", doc.getDocumentMasterVersion())
                    .field("iteration", doc.getIteration())
                    .field("docMAuthor", doc.getDocumentMaster().getAuthor())
                    .field("author", doc.getAuthor())
                    .field("type", doc.getDocumentMaster().getType())
                    .field("creationDate", doc.getDocumentMaster().getCreationDate())
                    .field("description", doc.getDocumentMaster().getDescription())
                    .field("checkOutUser", doc.getDocumentMaster().getCheckOutUser())
                    .field("checkOutDate", doc.getDocumentMaster().getCheckOutDate());
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
                            tmp.field(attr.getNameWithoutWhiteSpace(),attr.getValue());
                        }
                        tmp.endObject();
                    }
                    if(!doc.getAttachedFiles().isEmpty()){
                        tmp.startObject("content");
                        for (BinaryResource bin : doc.getAttachedFiles()) {
                            try {
                                String str = streamToString(bin.getFullName(),dataManager.getBinaryResourceInputStream(bin));
                                tmp.field(bin.getFullName(),str);
                            } catch (StorageException e) {
                                e.printStackTrace();
                            }
                        }
                        tmp.endObject();
                    }
                tmp.endObject();
            return tmp;
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
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
            XContentBuilder tmp = XContentFactory.jsonBuilder()                                                                         // Creation de DocRoueA
                    .startObject()
                    .field("workspaceId", part.getWorkspaceId())
                    .field("partNumber", part.getPartNumber())
                    .field("name", part.getPartRevision().getPartMaster().getName())
                    .field("version", part.getPartVersion())
                    .field("iteration", part.getIteration())
                    .field("standardPart", part.getPartRevision().getPartMaster().isStandardPart())
                    .field("partMAuthor", part.getPartRevision().getPartMaster().getAuthor())
                    .field("author", part.getAuthor())
                    .field("type", part.getPartRevision().getPartMaster().getType())
                    .field("creationDate", part.getCreationDate())
                    .field("description", part.getPartRevision().getDescription())
                    .field("checkOutUser", part.getPartRevision().getCheckOutUser())
                    .field("checkOutDate", part.getPartRevision().getCheckOutDate())
                    .startObject("attributes");
                        Collection<InstanceAttribute> listAttr = part.getInstanceAttributes().values();
                        for(InstanceAttribute attr:listAttr){
                            tmp.field(attr.getNameWithoutWhiteSpace(),attr.getValue());
                        }
                    tmp.endObject()
                    .startObject("content");
                        for (BinaryResource bin : part.getAttachedFiles()) {
                            try {
                                String str = streamToString(bin.getFullName(),dataManager.getBinaryResourceInputStream(bin));
                                tmp.field(bin.getFullName(),str);
                            } catch (StorageException e) {
                                e.printStackTrace();
                            }
                        }
                    tmp.endObject()
                .endObject();
            return tmp;
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            return null;
        }
    }

    /**
     * Get Stream for a Bin Resource
     * @param fullName Fullname of the resource
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
            } else if (extension.equals(".doc")) {
                //MSWord Document
                InputStream wordStream = new BufferedInputStream(inputStream);
                WordExtractor wordExtractor = new WordExtractor(wordStream);
                strRet = wordExtractor.getText();
                wordStream.close();
            } else if (extension.equals(".docx")){
                InputStream wordStream = new BufferedInputStream(inputStream);
                XWPFWordExtractor wordExtractor = new XWPFWordExtractor(new XWPFDocument(wordStream));
                strRet = wordExtractor.getText();
                wordStream.close();
            } else if (extension.equals(".ppt") || extension.equals(".pps")) {
                //MSPowerPoint Document
                InputStream pptStream = new BufferedInputStream(inputStream);
                PowerPointExtractor pptExtractor = new PowerPointExtractor(pptStream);
                strRet = pptExtractor.getText(true, true);
                pptStream.close();
            } else if (extension.equals(".txt") || extension.equals(".csv")) {
                //Text Document
                strRet = new Scanner(inputStream,"UTF-8").useDelimiter("\\A").next();
            } else if (extension.equals(".xls")) {
                //MSExcelExtractor Document
                POIFSFileSystem excelStream = new POIFSFileSystem(inputStream);
                ExcelExtractor excelExtractor= new ExcelExtractor(excelStream);
                strRet = excelExtractor.getText();
            } else if (extension.equals(".html") || extension.equals(".htm")) {
            } else if (extension.equals(".xml")) {
            } else if (extension.equals(".rtf")) {
            } else if (extension.equals(".pdf")) {
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
