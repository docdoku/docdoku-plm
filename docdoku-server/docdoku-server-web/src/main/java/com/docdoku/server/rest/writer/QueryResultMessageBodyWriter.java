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
import com.docdoku.core.configuration.*;
import com.docdoku.core.document.DocumentIteration;
import com.docdoku.core.document.DocumentLink;
import com.docdoku.core.document.DocumentRevision;
import com.docdoku.core.exceptions.ProductInstanceMasterNotFoundException;
import com.docdoku.core.exceptions.UserNotActiveException;
import com.docdoku.core.exceptions.UserNotFoundException;
import com.docdoku.core.exceptions.WorkspaceNotFoundException;
import com.docdoku.core.meta.InstanceAttribute;
import com.docdoku.core.meta.InstanceAttributeDescriptor;
import com.docdoku.core.meta.InstanceDateAttribute;
import com.docdoku.core.meta.InstanceListOfValuesAttribute;
import com.docdoku.core.product.PartIteration;
import com.docdoku.core.product.PartLinkList;
import com.docdoku.core.product.PartRevision;
import com.docdoku.core.query.QueryContext;
import com.docdoku.core.query.QueryField;
import com.docdoku.core.query.QueryResultRow;
import com.docdoku.core.services.IProductInstanceManagerLocal;
import com.docdoku.core.util.Tools;
import com.docdoku.server.export.ExcelGenerator;
import com.docdoku.server.rest.collections.QueryResult;

import javax.ejb.Stateless;
import javax.json.Json;
import javax.json.JsonValue;
import javax.json.stream.JsonGenerator;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
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
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;


@Stateless
@Provider
public class QueryResultMessageBodyWriter implements MessageBodyWriter<QueryResult> {

    private static SimpleDateFormat FORMAT = QueryResultMessageBodyWriter.getFormat();
    private ExcelGenerator excelGenerator = new ExcelGenerator();

    private static final Logger LOGGER = Logger.getLogger(QueryResultMessageBodyWriter.class.getName());

    private Context context;

    private static SimpleDateFormat getFormat() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        return format;
    }

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
            try {
                generateJSONResponse(outputStream, queryResult);
            } catch (NamingException e) {
                LOGGER.log(Level.SEVERE,null,e);
            }
        }
        else if(queryResult.getExportType().equals(QueryResult.ExportType.XLS)){
            excelGenerator.generateXLSResponse(queryResult, new Locale(queryResult.getQuery().getAuthor().getLanguage()), "");
        }
        else{
            throw new IllegalArgumentException();
        }

    }

    private void generateJSONResponse(OutputStream outputStream, QueryResult queryResult) throws UnsupportedEncodingException, NamingException {

        String charSet = "UTF-8";
        JsonGenerator jg = Json.createGenerator(new OutputStreamWriter(outputStream, charSet));
        jg.writeStartArray();

        List<String> selects = queryResult.getQuery().getSelects();
        List<String> partIterationSelectedAttributes = getPartIterationSelectedAttributes(selects);
        List<String> pathDataSelectedAttributes = getPathDataSelectedAttributes(selects);

        context = new InitialContext();
        IProductInstanceManagerLocal productInstanceService = (IProductInstanceManagerLocal) context.lookup("java:global/docdoku-server-ear/docdoku-server-ejb/ProductInstanceManagerBean");

        for (QueryResultRow row : queryResult.getRows()) {

            QueryContext queryContext = row.getContext();

            PartRevision part = row.getPartRevision();
            PartIteration lastCheckedInIteration = part.getLastCheckedInIteration();

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

            if (selects.contains(QueryField.PART_ITERATION_LINKED_DOCUMENTS)) {

                StringBuilder sb = new StringBuilder();

                if(null!= queryContext && null != queryContext.getSerialNumber()){
                    try {

                        ProductInstanceMaster productInstanceMaster = productInstanceService.getProductInstanceMaster(new ProductInstanceMasterKey(queryContext.getSerialNumber(), queryContext.getWorkspaceId(), queryContext.getConfigurationItemId()));
                        ProductInstanceIteration lastIteration = productInstanceMaster.getLastIteration();
                        ProductBaseline basedOn = lastIteration.getBasedOn();
                        PartCollection partCollection = basedOn.getPartCollection();
                        BaselinedPart baselinedPart = partCollection.getBaselinedPart(new BaselinedPartKey(partCollection.getId(), queryContext.getWorkspaceId(), part.getPartNumber()));
                        PartIteration targetPart = baselinedPart.getTargetPart();
                        Set<DocumentLink> linkedDocuments = targetPart.getLinkedDocuments();
                        DocumentCollection documentCollection = basedOn.getDocumentCollection();

                        for(DocumentLink documentLink:linkedDocuments){
                            DocumentRevision targetDocument = documentLink.getTargetDocument();
                            BaselinedDocument baselinedDocument = documentCollection.getBaselinedDocument(new BaselinedDocumentKey(documentCollection.getId(), queryContext.getWorkspaceId(), targetDocument.getDocumentMasterId(), targetDocument.getVersion()));
                            if(null != baselinedDocument) {
                                DocumentIteration targetDocumentIteration = baselinedDocument.getTargetDocument();
                                sb.append(targetDocumentIteration.toString() + ",");
                            }
                        }

                    } catch (UserNotFoundException | UserNotActiveException | WorkspaceNotFoundException | ProductInstanceMasterNotFoundException e) {
                        LOGGER.log(Level.FINEST,null,e);
                    }
                }else{
                    if(lastCheckedInIteration != null){
                        Set<DocumentLink> linkedDocuments = lastCheckedInIteration.getLinkedDocuments();

                        for(DocumentLink documentLink:linkedDocuments){
                            DocumentRevision targetDocument = documentLink.getTargetDocument();
                            DocumentIteration targetDocumentLastCheckedInIteration = targetDocument.getLastCheckedInIteration();
                            if(targetDocumentLastCheckedInIteration != null){
                                sb.append(targetDocumentLastCheckedInIteration.toString()+",");
                            }
                        }
                    }
                }

                jg.write(QueryField.PART_ITERATION_LINKED_DOCUMENTS, sb.toString());

            }

            for (String attributeSelect : partIterationSelectedAttributes) {

                String attributeSelectType = attributeSelect.substring(0, attributeSelect.indexOf(".")).substring(QueryField.PART_REVISION_ATTRIBUTES_PREFIX.length());

                String attributeSelectName = attributeSelect.substring(attributeSelect.indexOf(".") + 1);

                String attributeValue = "";

                PartIteration pi = part.getLastIteration();

                if (pi != null) {
                    List<InstanceAttribute> attributes = pi.getInstanceAttributes();

                    if (attributes != null) {
                        jg.writeStartArray(attributeSelect);

                        for (InstanceAttribute attribute : attributes) {
                            InstanceAttributeDescriptor attributeDescriptor = new InstanceAttributeDescriptor(attribute);

                            if (attributeDescriptor.getName().equals(attributeSelectName)
                                    && attributeDescriptor.getStringType().equals(attributeSelectType)) {

                                attributeValue = attribute.getValue() + "";

                                if (attribute instanceof InstanceDateAttribute) {
                                    attributeValue = getFormattedDate(((InstanceDateAttribute) attribute).getDateValue());
                                } else if (attribute instanceof InstanceListOfValuesAttribute) {
                                    attributeValue = ((InstanceListOfValuesAttribute) attribute).getSelectedName();
                                }

                                jg.write(attributeValue);
                            }
                        }

                        jg.writeEnd();

                    } else {
                        jg.write(attributeSelect, attributeValue);
                    }

                } else {
                    // TODO: maybe this line is useless and should be removed
                    jg.write(attributeSelect, attributeValue);
                }
            }

            for (String attributeSelect : pathDataSelectedAttributes) {

                String attributeSelectType = attributeSelect.substring(0, attributeSelect.indexOf(".")).substring(QueryField.PATH_DATA_ATTRIBUTES_PREFIX.length());

                String attributeSelectName = attributeSelect.substring(attributeSelect.indexOf(".") + 1);

                String attributeValue = "";

                PathDataIteration pdi = row.getPathDataIteration();

                if (pdi != null) {
                    List<InstanceAttribute> attributes = pdi.getInstanceAttributes();

                    if (attributes != null) {
                        jg.writeStartArray(attributeSelect);

                        for (InstanceAttribute attribute : attributes) {
                            InstanceAttributeDescriptor attributeDescriptor = new InstanceAttributeDescriptor(attribute);

                            if (attributeDescriptor.getName().equals(attributeSelectName)
                                    && attributeDescriptor.getStringType().equals(attributeSelectType)) {

                                attributeValue = attribute.getValue() + "";

                                if (attribute instanceof InstanceDateAttribute) {
                                    attributeValue = getFormattedDate(((InstanceDateAttribute) attribute).getDateValue());
                                } else if (attribute instanceof InstanceListOfValuesAttribute) {
                                    attributeValue = ((InstanceListOfValuesAttribute) attribute).getSelectedName();
                                }

                                jg.write(attributeValue);
                            }
                        }

                        jg.writeEnd();

                    } else {
                        jg.write(attributeSelect, attributeValue);
                    }
                }
            }

            if (selects.contains(QueryField.CTX_PRODUCT_ID)) {
                String configurationItemId = queryContext != null ? queryContext.getConfigurationItemId() : "";
                jg.write(QueryField.CTX_PRODUCT_ID, configurationItemId);
            }
            if (selects.contains(QueryField.CTX_SERIAL_NUMBER)) {
                String serialNumber = queryContext != null ? queryContext.getSerialNumber() : "";
                jg.write(QueryField.CTX_SERIAL_NUMBER, serialNumber != null ? serialNumber : "");
            }
            if (selects.contains(QueryField.CTX_AMOUNT)) {
                String amount = row.getAmount()+"";
                jg.write(QueryField.CTX_AMOUNT, amount);
            }

            if (selects.contains(QueryField.CTX_P2P_SOURCE)) {
                Map<String, List<PartLinkList>> sources = row.getSources();
                String partLinksAsString = Tools.getPartLinksAsHumanString(sources);
                jg.write(QueryField.CTX_P2P_SOURCE, partLinksAsString);
            }

            if (selects.contains(QueryField.CTX_P2P_TARGET)) {
                Map<String, List<PartLinkList>> targets = row.getTargets();
                String partLinksAsString = Tools.getPartLinksAsHumanString(targets);
                jg.write(QueryField.CTX_P2P_TARGET, partLinksAsString);
            }

            if(selects.contains(QueryField.PART_MASTER_IS_STANDARD)){
                boolean isStandard = row.getPartRevision().getPartMaster().isStandardPart();
                jg.write(QueryField.PART_MASTER_IS_STANDARD, isStandard);
            }

            jg.writeEnd();
        }

        jg.writeEnd();
        jg.flush();
    }


    private void writeDate(JsonGenerator jg, String key, Date date) {
        if (date != null){
            String formattedDate = getFormattedDate(date);
            jg.write(key, formattedDate);
        }else{
            jg.write(key, JsonValue.NULL);
        }
    }

    private String getFormattedDate(Date date) {
        return date != null ? FORMAT.format(date) : "";
    }

    public List<String> getPartIterationSelectedAttributes(List<String> selects) {
        List<String> attributesSelect = new ArrayList<>();
        for(String select : selects){
            if (select.contains(QueryField.PART_REVISION_ATTRIBUTES_PREFIX)){
                attributesSelect.add(select);
            }
        }
        return attributesSelect;
    }

    public List<String> getPathDataSelectedAttributes(List<String> selects) {
        List<String> attributesSelect = new ArrayList<>();
        for(String select : selects){
            if (select.contains(QueryField.PATH_DATA_ATTRIBUTES_PREFIX)){
                attributesSelect.add(select);
            }
        }
        return attributesSelect;
    }
}
