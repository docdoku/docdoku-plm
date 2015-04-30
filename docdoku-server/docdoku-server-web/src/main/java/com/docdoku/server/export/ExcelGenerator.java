package com.docdoku.server.export;/*
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

import com.docdoku.core.common.User;
import com.docdoku.core.meta.InstanceAttribute;
import com.docdoku.core.meta.InstanceAttributeDescriptor;
import com.docdoku.core.meta.InstanceListOfValuesAttribute;
import com.docdoku.core.product.PartIteration;
import com.docdoku.core.product.PartRevision;
import com.docdoku.core.query.QueryContext;
import com.docdoku.core.query.QueryField;
import com.docdoku.core.query.QueryResultRow;
import com.docdoku.server.rest.collections.QueryResult;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author: Chadid Asmae
 */
public class ExcelGenerator {

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    public File generateXLSResponse(QueryResult queryResult) {

        File excelFile = new File("export_parts.xls");
        //Blank workbook
        XSSFWorkbook workbook = new XSSFWorkbook();

        //Create a blank sheet
        XSSFSheet sheet = workbook.createSheet("Parts Data");

        //This data needs to be written (Object[])
        String header = StringUtils.join(queryResult.getQuery().getSelects(), "; ");
        Map<Integer, String[]> data = new HashMap<>();
        data.put(1, header.split(";"));
        int i = 1;
        for (QueryResultRow row : queryResult.getRows()) {
            i++;
            data.put(i, createXLSRow(queryResult, row));
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
            e.printStackTrace();
        }
        return excelFile;

    }

    private String[] createXLSRow(QueryResult queryResult, QueryResultRow row) {

        List<String> selects = queryResult.getQuery().getSelects();
        List<String> data = new ArrayList<>();

        PartRevision part = row.getPartRevision();

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
                    PartIteration piLastIteration = part.getLastIteration();
                    data.add((piLastIteration != null && piLastIteration.getModificationDate() != null) ? simpleDateFormat.format(piLastIteration.getModificationDate()) : "");
                    break;
                case QueryField.PART_REVISION_CREATION_DATE:
                    data.add((part.getCreationDate() != null) ? simpleDateFormat.format(part.getCreationDate()) : "");
                    break;
                case QueryField.PART_REVISION_CHECKOUT_DATE:
                    data.add((part.getCheckOutDate() != null) ? simpleDateFormat.format(part.getCheckOutDate()) : "");
                    break;
                case QueryField.PART_REVISION_CHECKIN_DATE:
                    PartIteration lastCheckedPI = part.getLastCheckedInIteration();
                    data.add((lastCheckedPI != null && lastCheckedPI.getCheckInDate() != null) ? simpleDateFormat.format(lastCheckedPI.getCheckInDate()) : "");
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
                default:
                    if (select.contains(QueryField.PART_REVISION_ATTRIBUTES_PREFIX)) {
                        String attributeSelectType = select.substring(0, select.indexOf(".")).substring(QueryField.PART_REVISION_ATTRIBUTES_PREFIX.length());
                        String attributeSelectName = select.substring(select.indexOf(".") + 1);
                        String attributeValue = "";
                        PartIteration pi = part.getLastIteration();
                        if (pi != null) {
                            List<InstanceAttribute> attributes = pi.getInstanceAttributes();
                            if (attributes != null) {
                                for (InstanceAttribute attribute : attributes) {
                                    InstanceAttributeDescriptor attributeDescriptor = new InstanceAttributeDescriptor(attribute);
                                    if (attributeDescriptor.getName().equals(attributeSelectName)
                                            && attributeDescriptor.getStringType().equals(attributeSelectType)) {

                                        attributeValue = attribute.getValue() + "";
                                        if (attribute instanceof InstanceListOfValuesAttribute) {
                                            attributeValue = ((InstanceListOfValuesAttribute) attribute).getSelectedName();
                                        }
                                    }
                                }
                            }
                        }

                        data.add(attributeValue);
                    } else {
                        data.add("");
                    }
            }

        }

        String rowData = StringUtils.join(data, "; ");
        return rowData.split(";");
    }
}
