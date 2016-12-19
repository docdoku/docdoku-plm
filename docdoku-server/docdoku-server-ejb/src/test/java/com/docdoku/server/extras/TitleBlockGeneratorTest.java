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

package com.docdoku.server.extras;

import com.docdoku.core.common.User;
import com.docdoku.core.document.DocumentIteration;
import com.docdoku.core.document.DocumentRevision;
import com.docdoku.core.meta.InstanceAttribute;
import com.docdoku.core.product.PartIteration;
import com.docdoku.core.product.PartRevision;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.parser.PdfReaderContentParser;
import com.itextpdf.text.pdf.parser.SimpleTextExtractionStrategy;
import com.itextpdf.text.pdf.parser.TextExtractionStrategy;
import org.apache.commons.io.FileUtils;
import java.nio.file.Files;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;

import static org.mockito.MockitoAnnotations.initMocks;

/**
 * Created by Charles Fallourd on 04/01/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class TitleBlockGeneratorTest {

    private File tmpDir;
    private Date date;

    private DocumentIteration documentIteration;
    private PartIteration partIteration;

    @Before
    public void setup() throws Exception {
        initMocks(this);
        tmpDir = Files.createTempDirectory("docdoku-").toFile();
        User user = Mockito.mock(User.class);
        Mockito.when(user.getLogin()).thenReturn("DocdokuTest");
        Mockito.when(user.getName()).thenReturn("DocdokuTest");

        DocumentRevision documentRevision = new DocumentRevision();
        documentRevision.setCreationDate(new Date());
        documentRevision.setAuthor(user);
        documentRevision.setTitle("TestTitleOrName");
        documentRevision.setTags(new HashSet<>());
        documentRevision.setDescription("TestDescription");

        documentIteration = Mockito.spy(new DocumentIteration());
        documentIteration.setDocumentRevision(documentRevision);
        date = new Date();
        documentIteration.setCreationDate(date);
        Mockito.doReturn("TestIdOrNumber").when(documentIteration).getId();
        Mockito.doReturn("TestIdOrNumber-A-154").when(documentIteration).toString();
        Mockito.doReturn("A").when(documentIteration).getVersion();
        Mockito.when(documentIteration.getInstanceAttributes()).thenReturn(new ArrayList<InstanceAttribute>());
        documentIteration.setAuthor(user);
        documentIteration.setRevisionNote("RevisionNote");
        documentIteration.setIteration(154);

        PartRevision partRevision = Mockito.spy(new PartRevision());
        partRevision.setCreationDate(new Date());
        partRevision.setAuthor(user);
        Mockito.doReturn("TestTitleOrName").when(partRevision).getPartName();
        partRevision.setTags(new HashSet<>());
        partRevision.setDescription("TestDescription");

        partIteration = Mockito.spy(new PartIteration());
        partIteration.setPartRevision(partRevision);
        date = new Date();
        partIteration.setCreationDate(date);
        Mockito.doReturn("TestIdOrNumber").when(partIteration).getNumber();
        Mockito.doReturn("A").when(partIteration).getVersion();
        Mockito.when(partIteration.getInstanceAttributes()).thenReturn(new ArrayList<InstanceAttribute>());
        partIteration.setAuthor(user);
        partIteration.setIterationNote("RevisionNote");
        partIteration.setIteration(154);

    }

    @After
    public void cleanup() throws IOException {
        FileUtils.deleteDirectory(tmpDir);
    }

    private InputStream createSimplePdf(String text) throws IOException, DocumentException {
        File pdf = new File(tmpDir, ""+System.currentTimeMillis());
        pdf.createNewFile();
        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(pdf));
        document.open();
        document.add(new Chunk(text));
        document.add(Chunk.NEWLINE);
        document.close();

        return new FileInputStream(pdf);
    }

    @Test
    public void testAddBlockTitleToPDF() throws Exception {
        InputStream fullPdf = TitleBlockGenerator.addBlockTitleToPDF(createSimplePdf(""), documentIteration, Locale.ENGLISH);
        PdfReader pdfReader = new PdfReader(fullPdf);
        //The number page is always
        Assert.assertTrue(pdfReader.getNumberOfPages() >= 2);
        String content = parsePdf(pdfReader,1);
        assertInContent(content);

        fullPdf= TitleBlockGenerator.addBlockTitleToPDF(createSimplePdf("Some text from a binary ressource. This text should be on the second page of the pdf. DocdokuKeyword.")
                ,documentIteration,Locale.ENGLISH);
        pdfReader = new PdfReader(fullPdf);
        Assert.assertTrue(pdfReader.getNumberOfPages() >= 2);
        content = parsePdf(pdfReader,1);
        assertInContent(content);
        //Assert that the second page still exist.
        content = parsePdf(pdfReader,2);
        Assert.assertTrue(content.contains("DocdokuKeyword."));

        fullPdf = TitleBlockGenerator.addBlockTitleToPDF(createSimplePdf(""), partIteration, Locale.ENGLISH);
        pdfReader = new PdfReader(fullPdf);
        //The number page is always
        Assert.assertTrue(pdfReader.getNumberOfPages() >= 2);
        content = parsePdf(pdfReader,1);
        assertInContent(content);

        fullPdf= TitleBlockGenerator.addBlockTitleToPDF(createSimplePdf("Some text from a binary ressource. This text should be on the second page of the pdf. DocdokuKeyword.")
                ,documentIteration,Locale.ENGLISH);
        pdfReader = new PdfReader(fullPdf);
        Assert.assertTrue(pdfReader.getNumberOfPages() >= 2);
        content = parsePdf(pdfReader,1);
        assertInContent(content);
        //Assert that the second page still exist.
        content = parsePdf(pdfReader,2);
        Assert.assertTrue(content.contains("DocdokuKeyword."));

    }

    private void assertInContent(String content) throws  Exception{
        Assert.assertTrue(content.contains("DocdokuTest"));
        Assert.assertTrue(content.contains("TestIdOrNumber"));
        Assert.assertTrue(content.contains("TestIdOrNumber-A"));
        Assert.assertTrue(content.contains(""+154));
        SimpleDateFormat simpleFormat = new SimpleDateFormat("yyyy/MM/dd");
        Assert.assertTrue(content.contains(simpleFormat.format(date)));
        Assert.assertTrue(content.contains("RevisionNote"));
        //Currently, we do not display the title of the document. Could be a good idea to do so.
        //Assert.assertTrue(content.contains("DocumentTitle"));
    }

    @Test
    public void testMergePdfDocuments() throws Exception {
        InputStream inputStream = TitleBlockGenerator.mergePdfDocuments(
                createSimplePdf("Some text from a binary ressource. This text should be on the second page of the pdf. DocdokuKeyword."),
                createSimplePdf("Some text from a binary ressource. This text should be on the second page of the pdf. SecondDocdokuKeyword."));
        PdfReader pdfReader = new PdfReader(inputStream);
        String firstPage = new String(pdfReader.getPageContent(1));
        Assert.assertTrue(firstPage.contains("DocdokuKeyword."));
        String secondPage = new String(pdfReader.getPageContent(2));
        Assert.assertTrue(secondPage.contains("SecondDocdokuKeyword."));
    }

    private String parsePdf(PdfReader reader, int page) throws IOException {
//        PdfReader reader = new PdfReader(pdf);
        PdfReaderContentParser parser = new PdfReaderContentParser(reader);
//        PrintWriter out = new PrintWriter(new FileOutputStream(txt));
        TextExtractionStrategy strategy;
        strategy = parser.processContent(page, new SimpleTextExtractionStrategy());
        return strategy.getResultantText();

//        out.flush();
//        out.close();
    }
}
