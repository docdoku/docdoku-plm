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
package com.docdoku.server.rest.writer;

import com.docdoku.core.common.User;
import com.docdoku.core.meta.InstanceAttribute;
import com.docdoku.core.meta.InstanceAttributeDescriptor;
import com.docdoku.core.meta.InstanceListOfValuesAttribute;
import com.docdoku.core.product.PartIteration;
import com.docdoku.core.product.PartRevision;
import com.docdoku.core.query.QueryContext;
import com.docdoku.core.query.QueryField;
import com.docdoku.core.query.QueryResultRow;
import com.docdoku.server.export.ExcelGenerator;
import com.docdoku.server.rest.collections.QueryResult;
import org.apache.commons.lang.StringUtils;

import javax.json.Json;
import javax.json.JsonValue;
import javax.json.stream.JsonGenerator;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@Provider
public class QueryWriter implements MessageBodyWriter<QueryResult> {

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    private ExcelGenerator excelGenerator = new ExcelGenerator();
    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return type.equals(QueryResult.class);
    }

    @Override
    public long getSize(QueryResult queryResult, Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType) {
        return -1;
    }

    @Override
    public void writeTo(QueryResult queryResult, Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> multivaluedMap, OutputStream outputStream) throws IOException, WebApplicationException {

        if(queryResult.getExportType().equals(QueryResult.ExportType.JSON)){
            generateJSONResponse(outputStream,queryResult);

        } else if(queryResult.getExportType().equals(QueryResult.ExportType.CSV)){
            generateCSVResponse(outputStream,queryResult);

        } else if(queryResult.getExportType().equals(QueryResult.ExportType.XLS)){
            excelGenerator.generateXLSResponse(queryResult);
        }else{
            throw new IllegalArgumentException();
        }

    }

    private void generateCSVResponse(OutputStream o, QueryResult queryResult) throws IOException {
        String header = StringUtils.join(queryResult.getQuery().getSelects(), "; ");
        header += "\n";
        o.write(header.getBytes());

        for(QueryResultRow row : queryResult.getRows()){
            writeCSVRow(row,o,queryResult);
        }
        o.flush();
    }

    private void writeCSVRow(QueryResultRow row, OutputStream o, QueryResult queryResult) throws IOException {

        List<String> selects = queryResult.getQuery().getSelects();
        List<String> data = new ArrayList<>();

        PartRevision part  = row.getPartRevision();

        QueryContext context = row.getContext();

        for(String select : selects){

            switch (select){
                case QueryField.CTX_PRODUCT_ID:
                    String productId = context != null ? context.getConfigurationItemId() : "";
                    data.add(productId);
                    break;
                case QueryField.CTX_SERIAL_NUMBER:
                    String serialNumber = context != null ? context.getSerialNumber() : "";
                    data.add(serialNumber!=null?serialNumber:"");
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
                    data.add(row.getDepth()+"");
                    break;
                case QueryField.CTX_AMOUNT:
                    data.add(row.getAmount()+"");
                    break;
                default:
                    if (select.contains(QueryField.PART_REVISION_ATTRIBUTES_PREFIX)){
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
        rowData += "\n";
        o.write(rowData.getBytes());
    }

    private void generateJSONResponse(OutputStream outputStream, QueryResult queryResult) throws UnsupportedEncodingException {

        String charSet = "UTF-8";
        JsonGenerator jg = Json.createGenerator(new OutputStreamWriter(outputStream, charSet));
        jg.writeStartArray();

        List<String> selects = queryResult.getQuery().getSelects();
        List<String> attributesSelect = getSelectedAttributes(selects);

        for (QueryResultRow row : queryResult.getRows()) {

            QueryContext context = row.getContext();

            PartRevision part = row.getPartRevision();

            jg.writeStartObject();

            jg.write(QueryField.PART_REVISION_PART_KEY, part.getPartNumber() + '-' + part.getVersion());

            // PartMaster data

            if (selects.contains(QueryField.PART_MASTER_NUMBER)) {
                jg.write(QueryField.PART_MASTER_NUMBER, part.getPartNumber());
            }

            if (selects.contains(QueryField.PART_MASTER_NAME)) {
                String sName = part.getPartName();
                jg.write(QueryField.PART_MASTER_NAME, sName != null ? sName : "");
            }

            if (selects.contains(QueryField.PART_MASTER_TYPE)) {
                String sType = part.getType();
                jg.write(QueryField.PART_MASTER_TYPE, sType != null ? sType : "");
            }

            // PartRevision data

            if (selects.contains(QueryField.PART_REVISION_MODIFICATION_DATE)) {
                PartIteration pi = part.getLastIteration();
                if (pi != null) {
                    writeDate(jg, QueryField.PART_REVISION_MODIFICATION_DATE, pi.getModificationDate());
                }
            }

            if (selects.contains(QueryField.PART_REVISION_CREATION_DATE)) {
                writeDate(jg, QueryField.PART_REVISION_CREATION_DATE, part.getCreationDate());
            }

            if (selects.contains(QueryField.PART_REVISION_CHECKOUT_DATE)) {
                writeDate(jg, QueryField.PART_REVISION_CHECKOUT_DATE, part.getCheckOutDate());
            }

            if (selects.contains(QueryField.PART_REVISION_CHECKIN_DATE)) {
                PartIteration lastCheckedInIteration = part.getLastCheckedInIteration();
                writeDate(jg, QueryField.PART_REVISION_CHECKIN_DATE, lastCheckedInIteration != null ? lastCheckedInIteration.getCheckInDate() : null);
            }

            if (selects.contains(QueryField.PART_REVISION_VERSION)) {
                String version = part.getVersion();
                jg.write(QueryField.PART_REVISION_VERSION, version);
            }

            if (selects.contains(QueryField.PART_REVISION_LIFECYCLE_STATE)) {
                String lifeCycleState = part.getLifeCycleState();
                jg.write(QueryField.PART_REVISION_LIFECYCLE_STATE, lifeCycleState != null ? lifeCycleState : "");
            }

            if (selects.contains(QueryField.PART_REVISION_STATUS)) {
                PartRevision.RevisionStatus status = part.getStatus();
                jg.write(QueryField.PART_REVISION_STATUS, status.toString());
            }

            if (selects.contains(QueryField.AUTHOR_LOGIN)) {
                User user = part.getAuthor();
                jg.write(QueryField.AUTHOR_LOGIN, user.getLogin());
            }

            if (selects.contains(QueryField.AUTHOR_NAME)) {
                User user = part.getAuthor();
                jg.write(QueryField.AUTHOR_NAME, user.getName());
            }

            if (selects.contains(QueryField.CTX_DEPTH)) {
                jg.write(QueryField.CTX_DEPTH, row.getDepth());
            }

            for (String attributeSelect : attributesSelect) {

                String attributeSelectType = attributeSelect.substring(0, attributeSelect.indexOf(".")).substring(QueryField.PART_REVISION_ATTRIBUTES_PREFIX.length());
                ;
                String attributeSelectName = attributeSelect.substring(attributeSelect.indexOf(".") + 1);

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
                    jg.write(attributeSelect, attributeValue);
                }
            }

            if (selects.contains(QueryField.CTX_PRODUCT_ID)) {
                String configurationItemId = context != null ? context.getConfigurationItemId() : "";
                jg.write(QueryField.CTX_PRODUCT_ID, configurationItemId);
            }
            if (selects.contains(QueryField.CTX_SERIAL_NUMBER)) {
                String serialNumber = context != null ? context.getSerialNumber() : "";
                jg.write(QueryField.CTX_SERIAL_NUMBER, serialNumber != null ? serialNumber : "");
            }
            if (selects.contains(QueryField.CTX_AMOUNT)) {
                String amount = row.getAmount()+"";
                jg.write(QueryField.CTX_AMOUNT, amount);
            }

            jg.writeEnd();
        }

        jg.writeEnd();
        jg.flush();
    }

    private void writeDate(JsonGenerator jg, String key, Date date) {
        if (date != null){
            String formattedDate = simpleDateFormat.format(date);
            jg.write(key, formattedDate);
        }else{
            jg.write(key, JsonValue.NULL);
        }
    }

    private String getFormattedDate(Date date) {
        return date != null ? simpleDateFormat.format(date) : "";
    }

    public List<String> getSelectedAttributes(List<String> selects) {
        List<String> attributesSelect = new ArrayList<>();
        for(String select : selects){
            if (select.contains(QueryField.PART_REVISION_ATTRIBUTES_PREFIX)){
                attributesSelect.add(select);
            }
        }
        return attributesSelect;
    }
}
