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
import com.docdoku.core.product.PartIteration;
import com.docdoku.core.product.PartRevision;
import org.apache.commons.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.logging.Logger;

@RunWith(MockitoJUnitRunner.class)
public class TitleBlockWriterTest {

    private static final Logger LOGGER = Logger.getLogger(TitleBlockWriterTest.class.getName());
    private Date date;

    private DocumentIteration documentIteration;
    private PartIteration partIteration;
    private User user;

    @Before
    public void setup() throws Exception {

        user = Mockito.mock(User.class);
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
        Mockito.when(documentIteration.getInstanceAttributes()).thenReturn(new ArrayList<>());
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
        Mockito.when(partIteration.getInstanceAttributes()).thenReturn(new ArrayList<>());
        partIteration.setAuthor(user);
        partIteration.setIterationNote("RevisionNote");
        partIteration.setIteration(154);
    }


    @Test
    public void createTitleBlockForDocumentIterationTest() throws Exception {
        DocumentTitleBlockData documentTitleData = new DocumentTitleBlockData(documentIteration, new Locale("en"));
        byte[] titleBlock = new TitleBlockWriter(documentTitleData).createTitleBlock();
        PDDocument loadedDocument = PDDocument.load(titleBlock);
        Assert.assertNotNull(loadedDocument);
        String text = new PDFTextStripper().getText(loadedDocument);
        Assert.assertFalse(text.isEmpty());
        Assert.assertTrue(text.contains(user.getLogin()));
        Assert.assertTrue(text.contains(documentIteration.getId()));
        Assert.assertTrue(text.contains(documentIteration.getDocumentRevision().getDescription()));
    }

    @Test
    public void createTitleBlockForPartIterationTest() throws Exception {
        PartTitleBlockData partTitleBlockData = new PartTitleBlockData(partIteration, new Locale("en"));
        byte[] titleBlock = new TitleBlockWriter(partTitleBlockData).createTitleBlock();
        PDDocument loadedDocument = PDDocument.load(titleBlock);

        Assert.assertNotNull(loadedDocument);
        String text = new PDFTextStripper().getText(loadedDocument);

        Assert.assertFalse(text.isEmpty());
        Assert.assertTrue(text.contains(user.getLogin()));
        Assert.assertTrue(text.contains(partIteration.getNumber()));
        Assert.assertTrue(text.contains(partIteration.getPartRevision().getDescription()));

    }

    @Test
    public void mergeTitleBlock() throws IOException {

        PartTitleBlockData partTitleBlockData = new PartTitleBlockData(partIteration, new Locale("en"));
        byte[] titleBlock = new TitleBlockWriter(partTitleBlockData).createTitleBlock();

        URL resource = TitleBlockWriterTest.class.getClassLoader()
                .getResource("com/docdoku/server/extras/sample.pdf");

        Assert.assertNotNull(resource);

        byte[] pdfBytes = IOUtils.toByteArray(new FileInputStream(resource.getPath()));

        ByteArrayInputStream pdfStream = new ByteArrayInputStream(pdfBytes);

        InputStream merge = TitleBlockGenerator.merge(pdfStream, titleBlock);

        PDDocument mergedDocument = PDDocument.load(merge);
        PDDocument pdfAsDocument = PDDocument.load(pdfBytes);
        PDDocument titleBlockAsDocument = PDDocument.load(titleBlock);

        Assert.assertEquals(mergedDocument.getNumberOfPages(), titleBlockAsDocument.getNumberOfPages() + pdfAsDocument.getNumberOfPages());

        String titleBlockText = new PDFTextStripper().getText(titleBlockAsDocument);
        String pdfText = new PDFTextStripper().getText(pdfAsDocument);
        String mergedText = new PDFTextStripper().getText(mergedDocument);

        Assert.assertEquals(mergedText, titleBlockText + pdfText);

    }

}
