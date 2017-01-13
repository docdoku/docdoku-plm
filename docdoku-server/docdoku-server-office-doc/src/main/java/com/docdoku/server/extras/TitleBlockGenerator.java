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

import com.docdoku.core.document.DocumentIteration;
import com.docdoku.core.product.PartIteration;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.logging.Logger;

/**
 * @author Morgan Guimard
 *         <p>
 *         This class define the default pdf generation for both part and document.
 *         This behaviour can be overridden:
 * @see PartTitleBlockData
 * @see DocumentTitleBlockData
 */
public abstract class TitleBlockGenerator {

    private static final Logger LOGGER = Logger.getLogger(TitleBlockGenerator.class.getName());

    /**
     * Generate a block title pdf page and add it to the pdf given in the input stream
     */
    public static InputStream addBlockTitleToPDF(InputStream pdfDocument, DocumentIteration docI, Locale pLocale) throws IOException {
        DocumentTitleBlockData data =
                new DocumentTitleBlockData(docI, pLocale);
        return merge(pdfDocument, new TitleBlockWriter(data).createTitleBlock());
    }

    /**
     * Generate a block title pdf page and add it to the pdf given in the input stream
     */
    public static InputStream addBlockTitleToPDF(InputStream pdfDocument, PartIteration partIteration, Locale pLocale) throws IOException {
        PartTitleBlockData data =
                new PartTitleBlockData(partIteration, pLocale);
        return merge(pdfDocument, new TitleBlockWriter(data).createTitleBlock());
    }

    public static InputStream merge(InputStream originalPDF, byte[] titleBlock) throws IOException {

        ByteArrayOutputStream tempOutStream = new ByteArrayOutputStream();
        PDFMergerUtility mergedDoc = new PDFMergerUtility();

        InputStream titleBlockStream = new ByteArrayInputStream(titleBlock);

        mergedDoc.addSource(titleBlockStream);
        mergedDoc.addSource(originalPDF);

        mergedDoc.setDestinationStream(tempOutStream);
        mergedDoc.mergeDocuments(MemoryUsageSetting.setupMainMemoryOnly());

        return new ByteArrayInputStream(tempOutStream.toByteArray());


    }
}
