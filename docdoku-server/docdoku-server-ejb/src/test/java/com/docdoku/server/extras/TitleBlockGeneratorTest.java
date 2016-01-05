package com.docdoku.server.extras;

import com.docdoku.core.common.User;
import com.docdoku.core.document.DocumentIteration;
import com.docdoku.core.document.DocumentRevision;
import com.docdoku.core.meta.InstanceAttribute;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfWriter;
import org.apache.commons.io.FileUtils;
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
 * Created by kelto on 04/01/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class TitleBlockGeneratorTest {

    private File tmpDir;
    private Date documentDate;

    private DocumentIteration documentIteration;

    @Before
    public void setup() throws Exception {
        initMocks(this);
        tmpDir = com.google.common.io.Files.createTempDir();
        User user = Mockito.mock(User.class);
        Mockito.when(user.getLogin()).thenReturn("DocdokuTest");
        Mockito.when(user.getName()).thenReturn("DocdokuTest");

        DocumentRevision documentRevision = new DocumentRevision();
        documentRevision.setCreationDate(new Date());
        documentRevision.setAuthor(user);
        documentRevision.setTitle("DocumentTitle");
        documentRevision.setTags(new HashSet<>());
        documentRevision.setDescription("DocumentDescription");

        documentIteration = Mockito.spy(new DocumentIteration());
        documentIteration.setDocumentRevision(documentRevision);
        documentDate = new Date();
        documentIteration.setCreationDate(documentDate);
        Mockito.doReturn("DocId").when(documentIteration).getId();
        Mockito.doReturn("DocId-A").when(documentIteration).getVersion();
        Mockito.when(documentIteration.getInstanceAttributes()).thenReturn(new ArrayList<InstanceAttribute>());
        documentIteration.setAuthor(user);
        documentIteration.setRevisionNote("RevisionNote");
        documentIteration.setIteration(154);

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
        String content = new String(pdfReader.getPageContent(1));
        assertInContent(content);

        fullPdf= TitleBlockGenerator.addBlockTitleToPDF(createSimplePdf("Some text from a binary ressource. This text should be on the second page of the pdf. DocdokuKeyword.")
                ,documentIteration,Locale.ENGLISH);
        pdfReader = new PdfReader(fullPdf);
        Assert.assertTrue(pdfReader.getNumberOfPages() >= 2);
        content = new String(pdfReader.getPageContent(1));
        assertInContent(content);
        //Assert that the second page still exist.
        content = new String(pdfReader.getPageContent(2));
        Assert.assertTrue(content.contains("DocdokuKeyword."));

    }

    private void assertInContent(String content) throws  Exception{
        Assert.assertTrue(content.contains("DocdokuTest"));
        Assert.assertTrue(content.contains("DocId"));
        Assert.assertTrue(content.contains("DocId-A"));
        Assert.assertTrue(content.contains(""+154));
        SimpleDateFormat simpleFormat = new SimpleDateFormat("yyyy/MM/dd");
        Assert.assertTrue(content.contains(simpleFormat.format(documentDate)));
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
}
