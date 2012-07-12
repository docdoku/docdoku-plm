/*
 * DocDoku, Professional Open Source
 * Copyright 2006, 2007, 2008, 2009, 2010, 2011, 2012 DocDoku SARL
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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.annotation.Resource;
import javax.ejb.Asynchronous;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.EJBException;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
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
import org.apache.lucene.util.Version;
import org.apache.poi.hslf.extractor.PowerPointExtractor;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Singleton class IndexerBean
 *
 * @author Florent.Garin
 */
@Singleton(name="IndexerBean")
@ConcurrencyManagement(ConcurrencyManagementType.CONTAINER)
public class IndexerBean {

    @Resource(name = "indexPath")
    private String indexPath;
    
    @Asynchronous
    @Lock(LockType.WRITE)
    public void removeFromIndex(String fullName) {
        IndexWriter indexWriter = null;
        Directory indexDir = null;
        try {
            indexDir = FSDirectory.open(new File(indexPath));
            indexWriter = new IndexWriter(indexDir, new StandardAnalyzer(Version.LUCENE_30),IndexWriter.MaxFieldLength.LIMITED);
            indexWriter.deleteDocuments(new Term("fullName", fullName));
        } catch (LockObtainFailedException ex) {
            try {
                if (IndexWriter.isLocked(indexDir)) {
                    IndexWriter.unlock(indexDir);
                }
            } catch (IOException pIOEx) {
                throw new EJBException(pIOEx);
            }
            throw new EJBException(ex);
        } catch (CorruptIndexException ex) {
            throw new EJBException(ex);
        } catch (IOException ex) {
            throw new EJBException(ex);
        } finally {
            try {
                if (indexWriter != null) {
                    indexWriter.close();
                }
            } catch (IOException ex) {
                throw new EJBException(ex);
            }
        }
    }

    @Asynchronous
    @Lock(LockType.WRITE)
    public void addToIndex(String fullName, String pathName) {
        IndexWriter indexWriter = null;
        Directory indexDir = null;
        try {
            indexDir = FSDirectory.open(new File(indexPath));
            indexWriter = new IndexWriter(indexDir, new StandardAnalyzer(Version.LUCENE_30), IndexWriter.MaxFieldLength.LIMITED);
            int ext = pathName.lastIndexOf('.');
            String extension = "";
            if (ext != -1) {
                extension = pathName.substring(ext);
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
                ZipInputStream zipOpenDoc = new ZipInputStream(new BufferedInputStream(new FileInputStream(pathName)));
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
                Reader contentReader = new StringReader(text.toString());
                addDoc(indexWriter, contentReader, fullName);
                contentReader.close();
            } else if (extension.equals(".doc")) {
                //MSWord Document
                InputStream wordStream = new BufferedInputStream(new FileInputStream(pathName));
                WordExtractor wordExtractor = new WordExtractor(wordStream);
                Reader contentReader = new StringReader(wordExtractor.getText());
                wordStream.close();
                addDoc(indexWriter, contentReader, fullName);
                contentReader.close();
            } else if (extension.equals(".ppt") || extension.equals(".pps")) {
                //MSPowerPoint Document
                InputStream pptStream = new BufferedInputStream(new FileInputStream(pathName));
                PowerPointExtractor pptExtractor = new PowerPointExtractor(pptStream);
                Reader contentReader = new StringReader(pptExtractor.getText(true, true));
                pptStream.close();
                addDoc(indexWriter, contentReader, fullName);
                pptExtractor.close();
                contentReader.close();
            } else if (extension.equals(".txt")) {
                //Text Document
                Reader contentReader = new BufferedReader(new FileReader(pathName));
                addDoc(indexWriter, contentReader, fullName);
                contentReader.close();
            } else if (extension.equals(".xls")) {
                //MSExcelExtractor Document
                //InputStream excelStream=new BufferedInputStream(new FileInputStream(pathName));
                //ExcelExtractor excelExtractor= new ExcelExtractor(excelStream);
                //Reader contentReader=new StringReader(excelExtractor.getText());
                //excelStream.close();
                //addDoc(indexWriter,contentReader,fullName);
                //excelExtractor.close();
                //contentReader.close();
            } else if (extension.equals(".html") || extension.equals(".htm")) {
            } else if (extension.equals(".csv")) {
            } else if (extension.equals(".xml")) {
            } else if (extension.equals(".rtf")) {
            } else if (extension.equals(".pdf")) {
            } else if (extension.equals(".msg")) {
            }
        } catch (CorruptIndexException ex) {
            throw new EJBException(ex);
        } catch (LockObtainFailedException ex) {
            try {
                if (IndexWriter.isLocked(indexDir)) {
                    IndexWriter.unlock(indexDir);
                }
            } catch (IOException pIOEx) {
                throw new EJBException(pIOEx);
            }
            throw new EJBException(ex);
        } catch (ParserConfigurationException ex) {
            throw new EJBException(ex);
        } catch (SAXException ex) {
            throw new EJBException(ex);
        } catch (IOException ex) {
            throw new EJBException(ex);
        } finally {
            try {
                if (indexWriter != null) {
                    indexWriter.close();
                }
            } catch (IOException ex) {
                throw new EJBException(ex);
            }
        }
    }

    private void addDoc(IndexWriter pIndexWriter, Reader pContentReader, String pFullName) throws FileNotFoundException, CorruptIndexException, IOException {
        Document doc = new Document();
        doc.add(new Field("fullName", pFullName, Field.Store.YES, Field.Index.NOT_ANALYZED));
        doc.add(new Field("content", pContentReader));
        pIndexWriter.addDocument(doc);
    }
}
