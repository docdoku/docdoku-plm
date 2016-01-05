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

package com.docdoku.server.extras;

import com.docdoku.core.document.DocumentIteration;
import com.docdoku.core.meta.InstanceAttribute;
import com.docdoku.core.product.PartIteration;
import com.docdoku.core.workflow.Activity;
import com.docdoku.core.workflow.Task;
import com.docdoku.core.workflow.Workflow;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.LineSeparator;

import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * @author Morgan Guimard
 *
 * This class define the default pdf generation for both part and document.
 * This behaviour can be overridden:
 * @see com.docdoku.server.extras.PartTitleBlockGenerator
 * @see com.docdoku.server.extras.DocumentTitleBlockGenerator
 */
public abstract class TitleBlockGenerator {

    private static Font NORMAL_12;
    private static Font BOLD_12;
    private static Font BOLD_18;

    protected static final float TABLE_PERCENT_WIDTH = 100f;
    protected static final float SIGNATURE_SIZE_W = 80f;
    protected static final float SIGNATURE_SIZE_H = 60f;

    protected static final String TEMP_FILE_NAME = "output.pdf";

    protected static final String BASE_NAME = "com.docdoku.server.viewers.localization.TitleBlockGenerator";

    protected static final Logger LOGGER = Logger.getLogger(TitleBlockGenerator.class.getName());
    static{
        try {
            BaseFont normal = BaseFont.createFont("com/docdoku/server/viewers/fonts/DejaVuSans.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            BaseFont bold = BaseFont.createFont("com/docdoku/server/viewers/fonts/DejaVuSans-Bold.ttf",  BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            NORMAL_12 = new Font(normal, 12, Font.NORMAL);
            BOLD_12 = new Font(bold, 12, Font.BOLD);
            BOLD_18 = new Font(bold, 18, Font.BOLD);
        } catch (DocumentException | IOException e) {
            LOGGER.log(Level.SEVERE,null,e);
        }
    }

    protected String title ;
    protected String subject;
    protected String authorName;
    protected String version;

    protected String creationDate;
    protected String iterationDate;
    protected String keywords;
    protected String description;
    protected java.util.List<InstanceAttribute> instanceAttributes;
    protected String currentIteration;
    protected Workflow workflow;
    protected Locale pLocale;
    protected String revisionNote;
    protected String lifeCycleState;

    protected void generateHeader(Paragraph preface, ResourceBundle bundle) {
        // Title + description
        preface.add(new Paragraph(title, BOLD_18));
        addEmptyLine(preface, 1);

        if(!description.isEmpty()){
            preface.add(new Paragraph(description, NORMAL_12));
        }

        // Separator
        LineSeparator separator = new LineSeparator();
        preface.add(separator);
        addEmptyLine(preface, 1);

    }

    protected void generateTable(Paragraph preface, ResourceBundle bundle) {
        // Author + date
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(TABLE_PERCENT_WIDTH);

        PdfPCell cell;

        cell = new PdfPCell(new Phrase(authorName,NORMAL_12));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        table.addCell(cell);

        cell = new PdfPCell(new Phrase(creationDate,NORMAL_12));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(cell);

        preface.add(table);

        addEmptyLine(preface, 1);

        preface.add(new Paragraph(bundle.getString("iteration"), BOLD_12));
        addEmptyLine(preface,1);

        PdfPTable iterationTable = new PdfPTable(5);
        iterationTable.setWidthPercentage(TABLE_PERCENT_WIDTH);

        // Table header
        cell = new PdfPCell(new Phrase(bundle.getString("iteration.version"),BOLD_12));
        cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
        iterationTable.addCell(cell);
        cell = new PdfPCell(new Phrase(bundle.getString("iteration.iteration"),BOLD_12));
        cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
        iterationTable.addCell(cell);
        cell = new PdfPCell(new Phrase(bundle.getString("iteration.date"),BOLD_12));
        cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
        iterationTable.addCell(cell);
        cell = new PdfPCell(new Phrase(bundle.getString("iteration.author"),BOLD_12));
        cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
        iterationTable.addCell(cell);
        cell = new PdfPCell(new Phrase(bundle.getString("iteration.Notes"),BOLD_12));
        cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
        iterationTable.addCell(cell);

        // Table body
        cell = new PdfPCell(new Phrase(version,NORMAL_12));
        iterationTable.addCell(cell);
        cell = new PdfPCell(new Phrase(currentIteration,NORMAL_12));
        iterationTable.addCell(cell);
        cell = new PdfPCell(new Phrase(iterationDate,NORMAL_12));
        iterationTable.addCell(cell);
        cell = new PdfPCell(new Phrase(authorName,NORMAL_12));
        iterationTable.addCell(cell);
        cell = new PdfPCell(new Phrase(revisionNote,NORMAL_12));
        iterationTable.addCell(cell);

        preface.add(iterationTable);
    }

    public void generateAttribute(Paragraph preface, ResourceBundle bundle) {


            // Table title
            preface.add(new Paragraph(bundle.getString("attributes"), BOLD_12));
            addEmptyLine(preface,1);

            PdfPTable attributesTable = new PdfPTable(2);
            attributesTable.setWidthPercentage(100f);

            // Table head
            PdfPCell cell = new PdfPCell(new Phrase(bundle.getString("attributes.name"),BOLD_12));
            cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
            attributesTable.addCell(cell);
            cell = new PdfPCell(new Phrase(bundle.getString("attributes.value"),BOLD_12));
            cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
            attributesTable.addCell(cell);

            // Table body

            for (InstanceAttribute attr : instanceAttributes) {
                cell = new PdfPCell(new Phrase(attr.getName(), NORMAL_12));
                attributesTable.addCell(cell);
                Object value = attr.getValue();
                cell = new PdfPCell(new Phrase(value != null ? String.valueOf(value) : "", NORMAL_12));
                attributesTable.addCell(cell);
            }

            preface.add(attributesTable);

    }

    private void generateLyfeCycleState(Paragraph preface, ResourceBundle bundle) {
        // Table title
        preface.add(new Paragraph(bundle.getString("lifecycle") + " : " + lifeCycleState, BOLD_12));
        addEmptyLine(preface,1);

        PdfPTable lifeCycleTable = new PdfPTable(5);
        lifeCycleTable.setWidthPercentage(100f);
        PdfPCell cell;
        for(Activity activity : workflow.getActivities()){

            boolean headerRendered = false ;

            for(Task task : activity.getTasks()){

                if (task.isApproved() || task.isRejected()) {

                    if(!headerRendered){
                        // Table head
                        cell = new PdfPCell(new Phrase(activity.getLifeCycleState(),BOLD_12));
                        cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                        cell.setColspan(5);
                        lifeCycleTable.addCell(cell);

                        cell = new PdfPCell(new Phrase(bundle.getString("lifecycle.task"),BOLD_12));
                        cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                        lifeCycleTable.addCell(cell);

                        cell = new PdfPCell(new Phrase(bundle.getString("lifecycle.date"),BOLD_12));
                        cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                        lifeCycleTable.addCell(cell);

                        cell = new PdfPCell(new Phrase(bundle.getString("lifecycle.author"),BOLD_12));
                        cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                        lifeCycleTable.addCell(cell);

                        cell = new PdfPCell(new Phrase(bundle.getString("lifecycle.comments"),BOLD_12));
                        cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                        lifeCycleTable.addCell(cell);

                        cell = new PdfPCell(new Phrase(bundle.getString("lifecycle.signature"),BOLD_12));
                        cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                        lifeCycleTable.addCell(cell);

                        headerRendered = true;
                    }

                    // Table body
                    cell = new PdfPCell(new Phrase(task.getTitle(),NORMAL_12));
                    lifeCycleTable.addCell(cell);

                    SimpleDateFormat simpleFormat = new SimpleDateFormat(bundle.getString("date.format"));
                    cell = new PdfPCell(new Phrase(simpleFormat.format(task.getClosureDate()), NORMAL_12));
                    lifeCycleTable.addCell(cell);

                    cell = new PdfPCell(new Phrase(task.getWorker().getName(), NORMAL_12));
                    lifeCycleTable.addCell(cell);

                    cell = new PdfPCell(new Phrase(task.getClosureComment(), NORMAL_12));
                    lifeCycleTable.addCell(cell);

                    if (task.getSignature() != null) {
                        try {
                            byte[] imageByte;
                            int indexOfFirstComma = task.getSignature().indexOf(",");
                            String base64 = task.getSignature().substring(indexOfFirstComma+1,task.getSignature().length());
                            imageByte = DatatypeConverter.parseBase64Binary(base64);
                            Image image = Image.getInstance(imageByte);
                            image.setCompressionLevel(Image.ORIGINAL_NONE);
                            image.scaleToFit(SIGNATURE_SIZE_W,SIGNATURE_SIZE_H);
                            cell = new PdfPCell(image);
                            lifeCycleTable.addCell(cell);
                        } catch (Exception e) {
                            cell = new PdfPCell(new Phrase(bundle.getString("signature.error"), NORMAL_12));
                            lifeCycleTable.addCell(cell);
                        }

                    } else {
                        cell = new PdfPCell(new Phrase(""));
                        lifeCycleTable.addCell(cell);
                    }

                }
            }

        }

        preface.add(lifeCycleTable);
    }

    /*
* Generate a block title pdf page and add it to the pdf given in the input stream
* */
    public InputStream generateBlockTitleToPDF(InputStream inputStream) throws IOException, DocumentException {

        File tmpDir = com.google.common.io.Files.createTempDir();
        File blockTitleFile = new File(tmpDir, inputStream.toString());

        ResourceBundle bundle = ResourceBundle.getBundle(BASE_NAME, pLocale);

        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(blockTitleFile));
        document.open();

        // Main paragraph
        Paragraph preface = new Paragraph();
        generateHeader(preface,bundle);
        generateTable(preface,bundle);
        addEmptyLine(preface,1);
        if(!instanceAttributes.isEmpty()){
            generateAttribute(preface,bundle);
            addEmptyLine(preface,1);
        }
        if(workflow != null){
            generateLyfeCycleState(preface,bundle);
        }

        document.add(preface);

        addMetaData(document);

        document.close();

        tmpDir.deleteOnExit();

        // Merge the pdf generated with the pdf given in the input stream
        //TODO: use PdfStamper to insert into the existing pdf.
        return mergePdfDocuments(new FileInputStream(blockTitleFile),inputStream);

    }

    /*
    * Merge two pdf from input streams
    * */
    public static InputStream mergePdfDocuments(InputStream input1, InputStream input2){

        try {
            File tmpDir = com.google.common.io.Files.createTempDir();
            File tmpCopyFile = new File(tmpDir, TEMP_FILE_NAME);
            InputStream[] files = { input1, input2 };

            Document doc = new Document();
            PdfCopy copy = new PdfCopy(doc, new FileOutputStream(tmpCopyFile));
            doc.open();
            PdfReader pdfReader;

            int n;
            // TODO check for resources to be closed
            for (InputStream file : files) {
                pdfReader = new PdfReader(file);
                n = pdfReader.getNumberOfPages();
                for (int page = 0; page < n; ) {
                    copy.addPage(copy.getImportedPage(pdfReader, ++page));
                }
            }

            doc.close();

            tmpDir.deleteOnExit();

            return new FileInputStream(tmpCopyFile);
        }
        catch (Exception e){
            LOGGER.log(Level.INFO, null, e);
        }

        return null;
    }

    /*
    * Generate a block title pdf page and add it to the pdf given in the input stream
    * */
    public static InputStream addBlockTitleToPDF(InputStream inputStream, DocumentIteration docI, Locale pLocale) throws IOException, DocumentException {
        DocumentTitleBlockGenerator documentTitleBlockGenerator = new DocumentTitleBlockGenerator(inputStream,docI,pLocale);
        return documentTitleBlockGenerator.generateBlockTitleToPDF(inputStream);
    }

    public static InputStream addBlockTitleToPDF(InputStream inputStream, PartIteration partIteration, Locale pLocale) throws IOException, DocumentException {
        PartTitleBlockGenerator partTitleBlockGenerator = new PartTitleBlockGenerator(inputStream, partIteration, pLocale);
        return partTitleBlockGenerator.generateBlockTitleToPDF(inputStream);
    }


    protected static void addEmptyLine(Paragraph paragraph, int number) {
        for (int i = 0; i < number; i++) {
            paragraph.add(new Paragraph(" "));
        }
    }

    protected void addMetaData(Document document) {
        document.addTitle(title);
        document.addSubject(subject);
        document.addKeywords(keywords);
        document.addAuthor(authorName);
        document.addCreator(authorName);
    }


}
