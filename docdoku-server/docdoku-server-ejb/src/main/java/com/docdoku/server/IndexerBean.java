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
package com.docdoku.server;

import java.io.*;
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
    public void addToIndex(String fullName, InputStream inputStream) {
        IndexWriter indexWriter = null;
        Directory indexDir = null;
        try {
            indexDir = FSDirectory.open(new File(indexPath));
            indexWriter = new IndexWriter(indexDir, new StandardAnalyzer(Version.LUCENE_30), IndexWriter.MaxFieldLength.LIMITED);
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
                Reader contentReader = new StringReader(text.toString());
                addDoc(indexWriter, contentReader, fullName);
                contentReader.close();
            } else if (extension.equals(".doc")) {
                //MSWord Document
                InputStream wordStream = new BufferedInputStream(inputStream);
                WordExtractor wordExtractor = new WordExtractor(wordStream);
                Reader contentReader = new StringReader(wordExtractor.getText());
                wordStream.close();
                addDoc(indexWriter, contentReader, fullName);
                contentReader.close();
            } else if (extension.equals(".ppt") || extension.equals(".pps")) {
                //MSPowerPoint Document
                InputStream pptStream = new BufferedInputStream(inputStream);
                PowerPointExtractor pptExtractor = new PowerPointExtractor(pptStream);
                Reader contentReader = new StringReader(pptExtractor.getText(true, true));
                pptStream.close();
                addDoc(indexWriter, contentReader, fullName);
                pptExtractor.close();
                contentReader.close();
            } else if (extension.equals(".txt")) {
                //Text Document
                Reader contentReader = new BufferedReader(new InputStreamReader(inputStream));
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
