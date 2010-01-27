/*
 * DocDoku, Professional Open Source
 * Copyright 2006, 2007, 2008, 2009, 2010 DocDoku SARL
 *
 * This file is part of DocDoku.
 *
 * DocDoku is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DocDoku is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with DocDoku.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.docdoku.server;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.concurrent.locks.ReentrantLock;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJBException;
import javax.ejb.MessageDriven;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.poi.hslf.extractor.PowerPointExtractor;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Entity class IndexerBean
 *
 * @author Florent.Garin
 */
@MessageDriven(name = "IndexerBean", activationConfig =  {
    @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge"),
    @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class IndexerBean implements MessageListener {
    
    @Resource(name="indexPath")
    private String indexPath;
    
    /*
     * Insure only one indexation task is running at a time.
     * So we can avoid LockObtainFailedException
     * and also minimize memory consumption.
     */
    private final static ReentrantLock INDEX_LOCK = new ReentrantLock();
    
    
    public void onMessage(Message message) {       
        try {
            String fullName = message.getStringProperty("fullName");
            String pathName = message.getStringProperty("pathName");
            String action = message.getStringProperty("action");
            if(action.equals("add"))
                addToIndex(fullName, pathName);
            else if(action.equals("remove"))
                removeFromIndex(fullName);
            
        }catch (JMSException ex) {
            throw new EJBException(ex);
        }
    }
    private void removeFromIndex(String fullName){
        INDEX_LOCK.lock();
        IndexWriter indexWriter = null;
        Directory indexDir = null;
        try {
            indexDir = FSDirectory.getDirectory(indexPath);
            indexWriter = new IndexWriter(indexDir, new StandardAnalyzer());
            indexWriter.deleteDocuments(new Term("fullName",fullName));
        }catch (LockObtainFailedException ex) {
            try{
                if (IndexReader.isLocked(indexDir))
                    IndexReader.unlock(indexDir);
            }catch (IOException pIOEx) {
                throw new EJBException(pIOEx);
            }
            throw new EJBException(ex);
        }catch (CorruptIndexException ex) {
            throw new EJBException(ex);
        }catch (IOException ex) {
            throw new EJBException(ex);
        }finally{
            INDEX_LOCK.unlock();
            try {
                if(indexWriter!=null)
                    indexWriter.close();
            } catch (IOException ex) {
                throw new EJBException(ex);
            }
        }
    }
    
    private void addToIndex(String fullName, String pathName){
        INDEX_LOCK.lock();
        IndexWriter indexWriter = null;
        Directory indexDir = null;
        try {
            indexDir = FSDirectory.getDirectory(indexPath);
            indexWriter = new IndexWriter(indexDir, new StandardAnalyzer());
            int ext = pathName.lastIndexOf('.');
            String extension = "";
            if(ext !=-1)
                extension = pathName.substring(ext);
            
            
            
            if(extension.equals(".odt")
            || extension.equals(".ods")
            || extension.equals(".odp")
            || extension.equals(".odg")
            || extension.equals(".odc")
            || extension.equals(".odf")
            || extension.equals(".odb")
            || extension.equals(".odi")
            || extension.equals(".odm")){
                final StringBuilder text = new StringBuilder();
                ZipInputStream zipOpenDoc = new ZipInputStream(new BufferedInputStream(new FileInputStream(pathName)));
                ZipEntry zipEntry;
                while((zipEntry = zipOpenDoc.getNextEntry()) != null) {
                    if (zipEntry.getName().equals("content.xml")) {
                        SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
                        SAXParser parser = saxParserFactory.newSAXParser();
                        parser.parse(zipOpenDoc,new DefaultHandler(){
                            public void characters(char[] ch,
                                    int start,
                                    int length)
                                    throws SAXException{
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
                Reader contentReader=new StringReader(text.toString());
                addDoc(indexWriter,contentReader,fullName);
                contentReader.close();
            }else if(extension.equals(".doc")){
                //MSWord Document
                InputStream wordStream=new BufferedInputStream(new FileInputStream(pathName));
                WordExtractor wordExtractor= new WordExtractor(wordStream);
                Reader contentReader=new StringReader(wordExtractor.getText());
                wordStream.close();
                addDoc(indexWriter,contentReader,fullName);
                contentReader.close();
            }else if(extension.equals(".ppt") || extension.equals(".pps")){
                //MSPowerPoint Document
                InputStream pptStream=new BufferedInputStream(new FileInputStream(pathName));
                PowerPointExtractor pptExtractor= new PowerPointExtractor(pptStream);
                Reader contentReader=new StringReader(pptExtractor.getText(true,true));
                pptStream.close();
                addDoc(indexWriter,contentReader,fullName);
                pptExtractor.close();
                contentReader.close();
            }else if(extension.equals(".txt")){
                //Text Document
                Reader contentReader=new BufferedReader(new FileReader(pathName));
                addDoc(indexWriter,contentReader,fullName);
                contentReader.close();
            }else if(extension.equals(".xls")){
                //MSExcelExtractor Document
                //InputStream excelStream=new BufferedInputStream(new FileInputStream(pathName));
                //ExcelExtractor excelExtractor= new ExcelExtractor(excelStream);
                //Reader contentReader=new StringReader(excelExtractor.getText());
                //excelStream.close();
                //addDoc(indexWriter,contentReader,fullName);
                //excelExtractor.close();
                //contentReader.close();
            }else if(extension.equals(".html") || extension.equals(".htm")){
                
            }else if(extension.equals(".csv")){
                
            }else if(extension.equals(".xml")){
                
            }else if(extension.equals(".rtf")){
                
            }else if(extension.equals(".pdf")){
                
            }else if(extension.equals(".msg")){
                
            }
        }catch (CorruptIndexException ex) {
            throw new EJBException(ex);
        }catch (LockObtainFailedException ex) {
            try{
                if (IndexReader.isLocked(indexDir))
                    IndexReader.unlock(indexDir);
            }catch (IOException pIOEx) {
                throw new EJBException(pIOEx);
            }
            throw new EJBException(ex);
        }catch (ParserConfigurationException ex) {
            throw new EJBException(ex);
        }catch (SAXException ex) {
            throw new EJBException(ex);
        }catch (IOException ex) {
            throw new EJBException(ex);
        }finally{
            INDEX_LOCK.unlock();
            try {
                if(indexWriter!=null)
                    indexWriter.close();
            } catch (IOException ex) {
                throw new EJBException(ex);
            }
        }
    }
    
    private void addDoc(IndexWriter pIndexWriter, Reader pContentReader, String pFullName) throws FileNotFoundException, CorruptIndexException, IOException{
        Document doc = new Document();
        doc.add(new Field("fullName",pFullName, Field.Store.YES,Field.Index.UN_TOKENIZED));
        doc.add(new Field("content", pContentReader));
        pIndexWriter.addDocument(doc);
    }
}
