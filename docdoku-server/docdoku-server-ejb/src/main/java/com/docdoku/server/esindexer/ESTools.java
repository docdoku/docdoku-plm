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

import com.docdoku.core.exceptions.ESServerException;
import com.docdoku.core.util.Tools;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import org.apache.poi.hslf.extractor.PowerPointExtractor;
import org.apache.poi.hssf.extractor.ExcelExtractor;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Tools for Search & Index Method using ElasticSearch API.
 * @author Taylor LABEJOF
 */
public class ESTools {
    private static final String CONF_PROPERTIES="/com/docdoku/server/esindexer/conf.properties";
    private static final Properties CONF = new Properties();
    private static final String I18N_CONF="com.docdoku.core.i18n.LocalStrings";
    private static final Logger LOGGER = Logger.getLogger(ESTools.class.getName());


    static{
        InputStream inputStream = null;
        try {
            inputStream = ESTools.class.getResourceAsStream(CONF_PROPERTIES);
            CONF.load(inputStream);
        } catch (IOException e) {
            String message = ResourceBundle.getBundle(I18N_CONF, Locale.getDefault()).getString("ES_ConfWarning1");
            Logger.getLogger(ESTools.class.getName()).log(Level.WARNING,message,e);
        } finally {
            try{
                if(inputStream!=null){
                    inputStream.close();
                }
            }catch (IOException e){
                Logger.getLogger(ESTools.class.getName()).log(Level.WARNING,null,e);
            }
        }
    }

    private ESTools() {
        super();
    }

    /**
     * Create a ElasticSearch Client to make QueryRequest
     */
    protected static Client createClient() throws ESServerException {
        try{
            Settings settings = ImmutableSettings.settingsBuilder()
                    .put("cluster.name", CONF.getProperty("cluster.name")).build();

            return new TransportClient(settings).addTransportAddress(new InetSocketTransportAddress(CONF.getProperty("host"), Integer.parseInt(CONF.getProperty("port"))));
        }catch (ElasticsearchException e){
            LOGGER.log(Level.WARNING, null, e);
            throw new ESServerException(Locale.getDefault(), "IndexerServerException");
        }
    }

    /**
     * Convert the workspaceId to a Elastic Search index name
     *
     * @param workspaceId Id to convert
     * @return The workspaceId without uppercase and space
     */
    protected static String formatIndexName(String workspaceId){
        try {
            return java.net.URLEncoder.encode(Tools.unAccent(workspaceId), "UTF-8").toLowerCase();
        } catch (UnsupportedEncodingException e) {
            LOGGER.log(Level.FINEST,null,e);
            return null;
        }
    }

    /**
     * Get Stream for a Bin Resource
     * @param fullName The full name of the resource
     * @param inputStream Stream of the resource
     * @return String to index
     */
    protected static String streamToString(String fullName, InputStream inputStream) {
        String strRet = " ";

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
                case ".odm":
                    strRet = openOfficeDocumentToString(inputStream);
                    break;
                case ".doc":
                    strRet = microsoftWordDocumentToString(inputStream);
                    break;
                case ".docx":
                    strRet = microsoftXMLWordDocumentToString(inputStream);
                    break;
                case ".ppt":
                case ".pps":
                    strRet = microsoftPowerPointDocumentToString(inputStream);
                    break;
                case ".txt":                                                                                            //Text Document
                case ".csv":                                                                                            //CSV Document
                    strRet = new Scanner(inputStream,"UTF-8").useDelimiter("\\A").next();
                    break;
                case ".xls":                                                                                            //MSExcelExtractor Document
                    strRet = microsoftExelDocumentToString(inputStream);
                    break;
                case ".pdf":                                                                                            // PDF Document
                    strRet = pdfDocumentToString(inputStream, fullName);
                    break;
                case ".html":
                case ".htm":
                case ".xml":
                case ".rtf":
                case ".msg":
                    break;
                default:
                    break;
            }
        } catch (ParserConfigurationException |SAXException|IOException ex) {
            LOGGER.log(Level.WARNING, "The file " + fullName + " can't be indexed.",ex);
        }
        return strRet;
    }

    private static String openOfficeDocumentToString(InputStream inputStream) throws IOException, SAXException, ParserConfigurationException {
        final StringBuilder text = new StringBuilder();
        ZipInputStream zipOpenDoc = new ZipInputStream(new BufferedInputStream(inputStream));
        ZipEntry zipEntry;
        while ((zipEntry = zipOpenDoc.getNextEntry()) != null) {
            if ("content.xml".equals(zipEntry.getName())) {
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

        return text.toString();
    }

    private static String microsoftWordDocumentToString(InputStream inputStream) throws IOException {
        String strRet;
        InputStream wordStream = new BufferedInputStream(inputStream);
        WordExtractor wordExtractor = new WordExtractor(wordStream);
        strRet = wordExtractor.getText();
        wordStream.close();
        return strRet;
    }

    private static String microsoftXMLWordDocumentToString(InputStream inputStream) throws IOException {
        String strRet;
        InputStream wordXStream = new BufferedInputStream(inputStream);
        XWPFWordExtractor wordXExtractor = new XWPFWordExtractor(new XWPFDocument(wordXStream));
        strRet = wordXExtractor.getText();
        wordXStream.close();
        return strRet;
    }

    private static String microsoftPowerPointDocumentToString(InputStream inputStream) throws IOException {
        String strRet;
        InputStream pptStream = new BufferedInputStream(inputStream);
        PowerPointExtractor pptExtractor = new PowerPointExtractor(pptStream);
        strRet = pptExtractor.getText(true, true);
        pptStream.close();
        return strRet;
    }

    private static String microsoftExelDocumentToString(InputStream inputStream) throws IOException {
        POIFSFileSystem excelStream = new POIFSFileSystem(inputStream);
        ExcelExtractor excelExtractor= new ExcelExtractor(excelStream);
        return excelExtractor.getText();
    }

    private static String pdfDocumentToString(InputStream inputStream, String fullName) throws IOException {
        StringBuilder buf = new StringBuilder();
        PdfReader reader = new PdfReader(inputStream);
        for(int i=1; i<=reader.getNumberOfPages(); i++){
            buf.append(pdfPageToString(reader,i,fullName));
        }
        reader.close();
        return buf.toString();
    }

    private static String pdfPageToString(PdfReader reader, int pageNumber, String fullName){
        try{
            return PdfTextExtractor.getTextFromPage(reader, pageNumber);
        }catch (Exception e){
            Logger.getLogger(ESIndexer.class.getName()).log(Level.INFO,"A problem occur in the file : "+fullName+", indexing at page :"+pageNumber);
            Logger.getLogger(ESIndexer.class.getName()).log(Level.FINER,null,e);
            return "";
        }
    }
}