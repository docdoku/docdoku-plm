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

package com.docdoku.server.export;

import com.docdoku.core.common.User;
import com.docdoku.core.configuration.PathDataIteration;
import com.docdoku.core.document.DocumentLink;
import com.docdoku.core.document.DocumentRevision;
import com.docdoku.core.meta.InstanceAttribute;
import com.docdoku.core.meta.InstanceAttributeDescriptor;
import com.docdoku.core.meta.InstanceListOfValuesAttribute;
import com.docdoku.core.product.PartIteration;
import com.docdoku.core.product.PartLinkList;
import com.docdoku.core.product.PartRevision;
import com.docdoku.core.query.QueryContext;
import com.docdoku.core.query.QueryField;
import com.docdoku.core.query.QueryResultRow;
import com.docdoku.core.util.Tools;
import com.docdoku.server.helpers.LangHelper;
import com.docdoku.server.rest.collections.QueryResult;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Chadid Asmae
 */
public class ExcelGenerator {

    private static final Logger LOGGER = Logger.getLogger(ExcelGenerator.class.getName());

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    public File generateXLSResponse(QueryResult queryResult, Locale locale, String baseURL) {
        LangHelper langHelper = new LangHelper(locale);
        File excelFile = new File("export_parts.xls");
        //Blank workbook
        XSSFWorkbook workbook = new XSSFWorkbook();

        //Create a blank sheet
        XSSFSheet sheet = workbook.createSheet("Parts Data");

        //This data needs to be written (Object[])
        String header = StringUtils.join(queryResult.getQuery().getSelects(), "; ");
        Map<Integer, String[]> data = new HashMap<>();
        String[] headerFormatted = new String[header.split(";").length];
        int headerIndex = 0;
        String[] columns = header.split(";");
        for (String column : columns) {
            String columnTranslated;
            if (!column.isEmpty()) {
                if (column.trim().startsWith(QueryField.PART_REVISION_ATTRIBUTES_PREFIX)) {
                    columnTranslated = column.split("-")[1];
                    columnTranslated = columnTranslated.substring(columnTranslated.indexOf(".") + 1);
                }
                else if (column.trim().startsWith(QueryField.PATH_DATA_ATTRIBUTES_PREFIX)) {
                    columnTranslated = column.split("-")[2];
                    columnTranslated = columnTranslated.substring(columnTranslated.indexOf(".") + 1);
                }
                else {
                    columnTranslated = langHelper.getLocalizedMessage(column.trim(), locale);
                }
                headerFormatted[headerIndex++] = columnTranslated != null ? columnTranslated : column;
            }
        }
        data.put(1, headerFormatted);
        int i = 1;
        for (QueryResultRow row : queryResult.getRows()) {
            i++;
            data.put(i, createXLSRow(queryResult, row, baseURL));
        }

        //Iterate over data and write to sheet
        Set<Integer> keyset = data.keySet();
        int rownum = 0;
        for (Integer key : keyset) {
            Row row = sheet.createRow(rownum++);
            String[] objArr = data.get(key);
            int cellnum = 0;
            for (String obj : objArr) {
                Cell cell = row.createCell(cellnum++);
                cell.setCellValue(obj);
            }
        }
        Font headerFont = workbook.createFont();
        headerFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
        headerFont.setFontHeightInPoints((short) 10);
        headerFont.setFontName("Courier New");
        headerFont.setItalic(true);
        headerFont.setColor(IndexedColors.WHITE.getIndex());
        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.BLUE.getIndex());
        headerStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);

        sheet.getRow(0).setRowStyle(headerStyle);
        try {
            //Write the workbook in file system
            FileOutputStream out = new FileOutputStream(excelFile);
            workbook.write(out);
            out.close();
        } catch (Exception e) {
            LOGGER.log(Level.FINEST, null, e);
        }
        return excelFile;

    }

    private String[] createXLSRow(QueryResult queryResult, QueryResultRow row, String baseURL) {

        List<String> selects = queryResult.getQuery().getSelects();
        List<String> data = new ArrayList<>();

        PartRevision part = row.getPartRevision();
        PartIteration lastCheckedInIteration = part.getLastCheckedInIteration();
        PartIteration lastIteration = part.getLastIteration();

        QueryContext context = row.getContext();

        for (String select : selects) {

            switch (select) {
                case QueryField.CTX_PRODUCT_ID:
                    String productId = context != null ? context.getConfigurationItemId() : "";
                    data.add(productId);
                    break;
                case QueryField.CTX_SERIAL_NUMBER:
                    String serialNumber = context != null ? context.getSerialNumber() : "";
                    data.add(serialNumber != null ? serialNumber : "");
                    break;
                case QueryField.PART_MASTER_NUMBER:
                    data.add(part.getPartNumber());
                    break;
                case QueryField.PART_MASTER_NAME:
                    String sName = part.getPartName();
                    data.add(sName != null ? sName : "");
                    break;
                case QueryField.PART_MASTER_TYPE:
                    String sType = part.getType();
                    data.add(sType != null ? sType : "");
                    break;
                case QueryField.PART_REVISION_MODIFICATION_DATE:
                    data.add((lastIteration != null && lastIteration.getModificationDate() != null) ? simpleDateFormat.format(lastIteration.getModificationDate()) : "");
                    break;
                case QueryField.PART_REVISION_CREATION_DATE:
                    data.add((part.getCreationDate() != null) ? simpleDateFormat.format(part.getCreationDate()) : "");
                    break;
                case QueryField.PART_REVISION_CHECKOUT_DATE:
                    data.add((part.getCheckOutDate() != null) ? simpleDateFormat.format(part.getCheckOutDate()) : "");
                    break;
                case QueryField.PART_REVISION_CHECKIN_DATE:
                    data.add((lastCheckedInIteration != null && lastCheckedInIteration.getCheckInDate() != null) ? simpleDateFormat.format(lastCheckedInIteration.getCheckInDate()) : "");
                    break;
                case QueryField.PART_REVISION_VERSION:
                    data.add(part.getVersion() != null ? part.getVersion() : "");
                    break;
                case QueryField.PART_REVISION_LIFECYCLE_STATE:
                    data.add(part.getLifeCycleState() != null ? part.getLifeCycleState() : "");
                    break;
                case QueryField.PART_REVISION_STATUS:
                    data.add(part.getStatus().toString());
                    break;
                case QueryField.AUTHOR_LOGIN:
                    User user = part.getAuthor();
                    data.add(user.getLogin());
                    break;
                case QueryField.AUTHOR_NAME:
                    User userAuthor = part.getAuthor();
                    data.add(userAuthor.getName());
                    break;
                case QueryField.CTX_DEPTH:
                    data.add(row.getDepth() + "");
                    break;
                case QueryField.CTX_AMOUNT:
                    data.add(row.getAmount() + "");
                    break;
                case QueryField.PART_ITERATION_LINKED_DOCUMENTS:
                    StringBuilder sb = new StringBuilder();
                    if (lastCheckedInIteration != null) {
                        Set<DocumentLink> linkedDocuments = lastCheckedInIteration.getLinkedDocuments();
                        for (DocumentLink documentLink : linkedDocuments) {
                            DocumentRevision targetDocument = documentLink.getTargetDocument();
                            sb.append(baseURL + "/documents/" + targetDocument.getWorkspaceId() + "/" + targetDocument.getId() + "/" + targetDocument.getVersion() + " ");
                        }
                    }
                    data.add(sb.toString());
                    break;

                case QueryField.CTX_P2P_SOURCE:
                    Map<String, List<PartLinkList>> sources = row.getSources();
                    String sourcePartLinksAsString = Tools.getPartLinksAsExcelString(sources);
                    data.add(sourcePartLinksAsString);
                    break;

                case QueryField.CTX_P2P_TARGET:
                    Map<String, List<PartLinkList>> targets = row.getTargets();
                    String targetPartLinksAsString = Tools.getPartLinksAsExcelString(targets);
                    data.add(targetPartLinksAsString);
                    break;

                default:
                    if (select.startsWith(QueryField.PART_REVISION_ATTRIBUTES_PREFIX)) {
                        String attributeSelectType = select.substring(0, select.indexOf(".")).substring(QueryField.PART_REVISION_ATTRIBUTES_PREFIX.length());
                        String attributeSelectName = select.substring(select.indexOf(".") + 1);
                        String attributeValue = "";
                        StringBuilder sbattr = new StringBuilder();
                        if (lastIteration != null) {
                            List<InstanceAttribute> attributes = lastIteration.getInstanceAttributes();
                            if (attributes != null) {
                                for (InstanceAttribute attribute : attributes) {
                                    InstanceAttributeDescriptor attributeDescriptor = new InstanceAttributeDescriptor(attribute);
                                    if (attributeDescriptor.getName().equals(attributeSelectName)
                                            && attributeDescriptor.getStringType().equals(attributeSelectType)) {

                                        attributeValue = attribute.getValue() + "";
                                        if (attribute instanceof InstanceListOfValuesAttribute) {
                                            attributeValue = ((InstanceListOfValuesAttribute) attribute).getSelectedName();
                                        }
                                        sbattr.append(attributeValue + " ");
                                    }
                                }
                            }
                        }
                        data.add(sbattr.toString().trim());
                    }
                    if (select.startsWith(QueryField.PATH_DATA_ATTRIBUTES_PREFIX)) {
                        String attributeSelectType = select.substring(0, select.indexOf(".")).substring(QueryField.PATH_DATA_ATTRIBUTES_PREFIX.length());
                        String attributeSelectName = select.substring(select.indexOf(".") + 1);
                        String attributeValue = "";
                        PathDataIteration pdi = row.getPathDataIteration();
                        StringBuilder sbpdattr = new StringBuilder();
                        if (pdi != null) {
                            List<InstanceAttribute> attributes = pdi.getInstanceAttributes();
                            if (attributes != null) {
                                for (InstanceAttribute attribute : attributes) {
                                    InstanceAttributeDescriptor attributeDescriptor = new InstanceAttributeDescriptor(attribute);
                                    if (attributeDescriptor.getName().equals(attributeSelectName)
                                            && attributeDescriptor.getStringType().equals(attributeSelectType)) {

                                        attributeValue = attribute.getValue() + "";
                                        if (attribute instanceof InstanceListOfValuesAttribute) {
                                            attributeValue = ((InstanceListOfValuesAttribute) attribute).getSelectedName();
                                        }
                                        sbpdattr.append(attributeValue + " ");
                                    }
                                }
                            }
                        }
                        data.add(sbpdattr.toString().trim());
                    }
            }

        }

        String rowData = StringUtils.join(data, "; ");
        return rowData.split(";");
    }
}
