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

import com.docdoku.core.meta.InstanceAttribute;
import com.docdoku.core.meta.InstanceTextAttribute;
import com.docdoku.core.workflow.Activity;
import com.docdoku.core.workflow.Task;
import com.docdoku.core.workflow.Workflow;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import rst.pdfbox.layout.elements.Document;
import rst.pdfbox.layout.elements.Frame;
import rst.pdfbox.layout.elements.Paragraph;
import rst.pdfbox.layout.elements.render.ColumnLayout;
import rst.pdfbox.layout.elements.render.VerticalLayout;
import rst.pdfbox.layout.elements.render.VerticalLayoutHint;
import rst.pdfbox.layout.shape.Rect;
import rst.pdfbox.layout.shape.Stroke;
import rst.pdfbox.layout.text.Alignment;
import rst.pdfbox.layout.text.BaseFont;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Creates a title block to append to other documents
 *
 * @author Morgan Guimard
 */
public class TitleBlockWriter {

    private static final Integer DOCUMENT_TITLE_SIZE = 18;
    private static final Integer TITLE_MAP_SIZE = 14;
    private static final Integer TEXT_SIZE = 10;

    private static final long PAGE_MARGIN_LEFT = 40;
    private static final long PAGE_MARGIN_RIGHT = 40;
    private static final long PAGE_MARGIN_TOP = 60;
    private static final long PAGE_MARGIN_BOTTOM = 60;

    private static final long TITLE_MARGIN_BOTTOM = 18;

    private static final long MAP_TITLE_MARGIN_BOTTOM = 5;
    private static final long SEPARATOR_VERTICAL_MARGIN = 18;
    private static final long LIGHT_SEPARATOR_VERTICAL_MARGIN = 2;
    private static final long CELLS_SPACING = 0;
    private static final Float SEPARATOR_SIZE = 1f;

    private static final java.awt.Color SEPARATOR_COLOR = java.awt.Color.black;
    private static final java.awt.Color LIGHT_SEPARATOR_COLOR = java.awt.Color.lightGray;

    private static final PDFont TEXT_REGULAR_FONT = BaseFont.Helvetica.getPlainFont();
    private static final PDFont TEXT_BOLD_FONT = BaseFont.Helvetica.getBoldFont();
    private static final PDFont TEXT_ITALIC_FONT = BaseFont.Helvetica.getItalicFont();

    private PDFont ICON_FONT;

    private static final Logger LOGGER = Logger.getLogger(TitleBlockWriter.class.getName());
    private static final String ICON_FONT_FILE = "com/docdoku/server/extras/fonts/fontawesome-webfont.ttf";

    private Document document;
    private TitleBlockData data;

    public TitleBlockWriter(TitleBlockData pData) {
        data = pData;
        document = new Document(PAGE_MARGIN_LEFT, PAGE_MARGIN_RIGHT, PAGE_MARGIN_TOP, PAGE_MARGIN_BOTTOM);
        loadIconFont();
    }

    public byte[] createTitleBlock() throws IOException {

        // Title + main information
        writeEntityInfo();

        // Attribute list
        writeAttributes();

        // Workflow
        writeLifeCycleState();

        return documentToByteArray(document);
    }

    private void writeEntityInfo() throws IOException {

        Paragraph titleParagraph = new Paragraph();
        titleParagraph.addText(data.getTitle(), DOCUMENT_TITLE_SIZE, TEXT_BOLD_FONT);
        document.add(titleParagraph, new VerticalLayoutHint(Alignment.Left, 0, 0, 0, TITLE_MARGIN_BOTTOM));

        Paragraph paragraph = new Paragraph();
        paragraph.addText(data.getBundleString("original.author"), TEXT_SIZE, TEXT_BOLD_FONT);
        space(paragraph);
        paragraph.addText(data.getAuthorName(), TEXT_SIZE, TEXT_REGULAR_FONT);
        breakLine(paragraph);
        paragraph.addText(data.getBundleString("iteration.date"), TEXT_SIZE, TEXT_BOLD_FONT);
        space(paragraph);
        paragraph.addText(data.getCreationDate(), TEXT_SIZE, TEXT_REGULAR_FONT);
        breakLine(paragraph);
        paragraph.addText(data.getBundleString("iteration"), TEXT_SIZE, TEXT_BOLD_FONT);
        space(paragraph);
        paragraph.addText(data.getCurrentIteration(), TEXT_SIZE, TEXT_REGULAR_FONT);
        breakLine(paragraph);
        paragraph.addText(data.getBundleString("iteration.date"), TEXT_SIZE, TEXT_BOLD_FONT);
        space(paragraph);
        paragraph.addText(data.getIterationDate(), TEXT_SIZE, TEXT_REGULAR_FONT);
        breakLine(paragraph);

        String revisionNote = data.getRevisionNote();

        if (revisionNote != null) {
            paragraph.addText(data.getBundleString("iteration.note"), TEXT_SIZE, TEXT_BOLD_FONT);
            space(paragraph);
            paragraph.addText(revisionNote, TEXT_SIZE, TEXT_REGULAR_FONT);
        }

        document.add(paragraph, new VerticalLayoutHint(Alignment.Left, 0, 0, 0, 5));

        String description = data.getDescription();

        if(description != null && !description.isEmpty()){
            drawLightHorizontalSeparator();
            Paragraph descriptionParagraph = new Paragraph();
            descriptionParagraph.addText(description, TEXT_SIZE, TEXT_ITALIC_FONT);
            document.add(descriptionParagraph);
        }

    }


    private void writeAttributes() throws IOException {

        List<InstanceAttribute> instanceAttributes = data.getInstanceAttributes();

        if (!instanceAttributes.isEmpty()) {

            resetLayout();
            drawHorizontalSeparator();

            Paragraph mapTitle = new Paragraph();
            mapTitle.addText(data.getBundleString("attributes"), TITLE_MAP_SIZE, TEXT_BOLD_FONT);
            breakLine(mapTitle);

            document.add(mapTitle, new VerticalLayoutHint(Alignment.Left, 0, 0, 0, MAP_TITLE_MARGIN_BOTTOM));

            InstanceAttribute headerAttribute = new InstanceTextAttribute(
                    data.getBundleString("attributes.name"),
                    data.getBundleString("attributes.value"),
                    false
            );

            writeAttribute(headerAttribute, true);

            for (InstanceAttribute attr : instanceAttributes) {
                writeAttribute(attr, false);
            }
        }

    }

    private void writeAttribute(InstanceAttribute attr, boolean isHeaderRow) throws IOException {

        resetLayout();
        drawLightHorizontalSeparator();

        document.add(new ColumnLayout(4, CELLS_SPACING));

        Paragraph keyParagraph = new Paragraph();
        keyParagraph.addText(attr.getName(), TEXT_SIZE, TEXT_BOLD_FONT);
        document.add(keyParagraph);

        document.add(ColumnLayout.NEWCOLUMN);

        Paragraph valueParagraph = new Paragraph();
        valueParagraph.setMaxWidth(360.0f);
        valueParagraph.addText(String.valueOf(attr.getValue()), TEXT_SIZE, isHeaderRow ? TEXT_BOLD_FONT : TEXT_REGULAR_FONT);
        document.add(valueParagraph);

    }

    private void writeLifeCycleState() throws IOException {
        drawWorkflow(data.getWorkflow());
    }

    private void drawWorkflow(Workflow workflow) throws IOException {
        if (workflow != null) {
            resetLayout();
            drawHorizontalSeparator();
            Paragraph paragraph = new Paragraph();
            paragraph.addText(data.getBundleString("lifecycle"), TITLE_MAP_SIZE, TEXT_BOLD_FONT);
            document.add(paragraph, new VerticalLayoutHint(Alignment.Left, 0, 0, 0, 10));

            for (Activity activity : workflow.getActivities()) {
                drawActivity(activity);
            }
        }
    }

    private void drawActivity(Activity activity) throws IOException {

        Paragraph activityTitle = new Paragraph();
        activityTitle.addText(activity.getLifeCycleState(), TEXT_SIZE, TEXT_BOLD_FONT);
        breakLine(activityTitle);
        document.add(activityTitle);

        drawLightHorizontalSeparator();

        for (Task task : activity.getTasks()) {
            drawTask(task);
        }
    }

    private void drawTask(Task task) throws IOException {

        String iconMessage;
        String taskMessage;

        switch (task.getStatus()) {
            case APPROVED:
                iconMessage = "\uf00c";
                taskMessage = data.getBundleString("lifecycle.approved");
                break;
            case REJECTED:
                iconMessage = "\uf00d";
                taskMessage = data.getBundleString("lifecycle.rejected");
                break;
            default:
                // Dont draw other tasks status
                return;
        }

        Paragraph left = new Paragraph();
        document.add(new ColumnLayout(2, CELLS_SPACING));
        left.addText(iconMessage, TEXT_SIZE, ICON_FONT);
        space(left);
        left.addText(task.getTitle(), TEXT_SIZE, TEXT_BOLD_FONT);

        document.add(left, new VerticalLayoutHint(Alignment.Left, 0, 0, 0, 0));

        document.add(ColumnLayout.NEWCOLUMN);

        Date closureDate = task.getClosureDate();
        Paragraph right = new Paragraph();
        if (closureDate != null) {
            right.addText(data.format(closureDate), TEXT_SIZE, TEXT_ITALIC_FONT);
        }
        document.add(right, new VerticalLayoutHint(Alignment.Right, 0, 0, 0, 0));

        resetLayout();

        document.add(new ColumnLayout(4, CELLS_SPACING));

        Paragraph taskDetailsLeft = new Paragraph();
        taskDetailsLeft.addText(taskMessage, TEXT_SIZE, TEXT_ITALIC_FONT);
        space(taskDetailsLeft);
        taskDetailsLeft.addText(task.getWorker().getName(), TEXT_SIZE, TEXT_BOLD_FONT);
        document.add(taskDetailsLeft, new VerticalLayoutHint(Alignment.Left, 0, 0, 10, 10));

        document.add(ColumnLayout.NEWCOLUMN);

        Paragraph taskDetailsRight = new Paragraph();
        taskDetailsRight.addText(task.getClosureComment(), TEXT_SIZE - 1, TEXT_ITALIC_FONT);
        document.add(taskDetailsRight, new VerticalLayoutHint(Alignment.Left, 0, 0, 10, 10));
        taskDetailsRight.setMaxWidth(360.0f);

        resetLayout();
    }

    // Util methods


    private byte[] documentToByteArray(Document document) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        document.save(out);
        return out.toByteArray();
    }

    private void drawHorizontalSeparator() {
        Paragraph emptyParagraph = new Paragraph();
        Frame frame = new Frame(emptyParagraph, document.getPageWidth(), SEPARATOR_SIZE);
        frame.setShape(new Rect());
        frame.setBorder(SEPARATOR_COLOR, Stroke.builder().build());
        frame.setMargin(0, 0, SEPARATOR_VERTICAL_MARGIN, SEPARATOR_VERTICAL_MARGIN);
        document.add(frame);
    }

    private void breakLine(Paragraph p) throws IOException {
        p.addText("\n", TEXT_SIZE, TEXT_REGULAR_FONT);
    }

    private void space(Paragraph p) throws IOException {
        p.addText(" ", TEXT_SIZE, TEXT_REGULAR_FONT);
    }

    private void resetLayout() {
        document.add(new VerticalLayout());
    }

    private void drawLightHorizontalSeparator() {
        Paragraph emptyParagraph = new Paragraph();
        Frame frame = new Frame(emptyParagraph, document.getPageWidth(), SEPARATOR_SIZE);
        frame.setShape(new Rect());
        frame.setBorder(LIGHT_SEPARATOR_COLOR, Stroke.builder().build());
        frame.setMargin(0, 0, LIGHT_SEPARATOR_VERTICAL_MARGIN, LIGHT_SEPARATOR_VERTICAL_MARGIN);
        document.add(frame);
    }

    private void loadIconFont() {
        PDDocument pdDocument = document.getPDDocument();

        try (InputStream inputStream = TitleBlockWriter.class.getClassLoader()
                .getResourceAsStream(ICON_FONT_FILE)) {
            ICON_FONT = PDType0Font.load(pdDocument, inputStream);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, null, e);
        }
    }
}
