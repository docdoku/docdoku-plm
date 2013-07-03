/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2013 DocDoku SARL
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
package com.docdoku.server.http;

import com.docdoku.core.common.BinaryResource;
import com.docdoku.core.common.User;
import com.docdoku.core.document.DocumentIteration;
import com.docdoku.core.document.DocumentIterationKey;
import com.docdoku.core.document.DocumentMasterTemplateKey;
import com.docdoku.core.product.PartIterationKey;
import com.docdoku.core.product.PartMasterTemplateKey;
import com.docdoku.core.services.*;
import com.google.common.io.ByteStreams;

import javax.activation.FileTypeMap;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import javax.transaction.Status;
import javax.transaction.UserTransaction;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;


@MultipartConfig(fileSizeThreshold = 1024 * 1024)
public class UploadDownloadServlet extends HttpServlet {

    @EJB
    private IDocumentManagerLocal documentService;

    @EJB
    private IProductManagerLocal productService;

    @EJB
    private IConverterManagerLocal converterService;

    @EJB
    private IDocumentResourceGetterManagerLocal documentResourceGetterService;

    @EJB
    private IDocumentPostUploaderManagerLocal documentPostUploaderService;

    @EJB
    private IDocumentViewerManagerLocal documentViewerService;

    @EJB
    private IDataManagerLocal dataManager;

    @Resource
    private UserTransaction utx;

    @Override
    protected void doGet(HttpServletRequest pRequest, HttpServletResponse pResponse) throws ServletException, IOException {

        try {

            // Get stored vars in request from FilesFilter
            BinaryResource binaryResource = (BinaryResource) pRequest.getAttribute("binaryResource");
            boolean isSubResource = (boolean) pRequest.getAttribute("isSubResource");
            boolean isDocumentAndOutputSpecified = (boolean) pRequest.getAttribute("isDocumentAndOutputSpecified");
            boolean needsCacheHeaders = (boolean) pRequest.getAttribute("needsCacheHeaders");
            String fullName = (String) pRequest.getAttribute("fullName");
            String outputFormat = (String) pRequest.getAttribute("outputFormat");
            String subResourceVirtualPath = (String) pRequest.getAttribute("subResourceVirtualPath");
            User user = (User) pRequest.getAttribute("user");
            DocumentIteration docI = (DocumentIteration) pRequest.getAttribute("docI");

            if(needsCacheHeaders){
                setCacheHeaders(86400,pResponse);
            }

            //set content type
            String contentType = "";
            if (isSubResource) {
                contentType = FileTypeMap.getDefaultFileTypeMap().getContentType(subResourceVirtualPath);
            } else {
                if (isDocumentAndOutputSpecified) {
                    contentType = FileTypeMap.getDefaultFileTypeMap().getContentType(fullName + "." + outputFormat);
                } else {
                    contentType = FileTypeMap.getDefaultFileTypeMap().getContentType(fullName);
                }
            }

            pResponse.setContentType(contentType);

            long lastModified = binaryResource.getLastModified().getTime();
            long ifModified = pRequest.getDateHeader("If-Modified-Since");

            if (lastModified > ifModified) {

                setLastModifiedHeaders(lastModified, pResponse);

                InputStream binaryContentInputStream = null;
                ServletOutputStream httpOut = null;

                try {
                    if (isSubResource) {

                        binaryContentInputStream = dataManager.getBinarySubResourceInputStream(binaryResource, subResourceVirtualPath);

                    } else {
                        if (isDocumentAndOutputSpecified) {
                            binaryContentInputStream = documentResourceGetterService.getConvertedResource(outputFormat, binaryResource, docI, user);
                        } else {
                            pResponse.setContentLength((int) binaryResource.getContentLength());
                            binaryContentInputStream = dataManager.getBinaryResourceInputStream(binaryResource);
                        }

                    }
                    httpOut = pResponse.getOutputStream();
                    ByteStreams.copy(binaryContentInputStream, httpOut);
                } finally {
                    if(binaryContentInputStream != null){
                        binaryContentInputStream.close();
                    }
                    if(httpOut != null){
                        httpOut.flush();
                        httpOut.close();
                    }
                }

            } else {
                pResponse.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
            }

        } catch (Exception pEx) {
            pEx.printStackTrace();
            pResponse.setHeader("Reason-Phrase", pEx.getMessage());
            throw new ServletException("Error while downloading the file.", pEx);
        }
    }

    @Override
    protected void doPost(HttpServletRequest pRequest, HttpServletResponse pResponse) throws ServletException, IOException {

        String[] pathInfos = Pattern.compile("/").split(pRequest.getRequestURI());
        int offset = pRequest.getContextPath().isEmpty() ? 2 : 3;

        String workspaceId = URLDecoder.decode(pathInfos[offset], "UTF-8");
        String elementType = pathInfos[offset + 1];

        String fileName = null;
        PartIterationKey partPK = null;
        DocumentIterationKey docPK = null;
        DocumentMasterTemplateKey templatePK = null;
        PartMasterTemplateKey partTemplatePK = null;
        BinaryResource binaryResource = null;

        try {
            utx.begin();
            if (elementType.equals("documents")) {
                String docMId = URLDecoder.decode(pathInfos[offset + 2], "UTF-8");
                String docMVersion = pathInfos[offset + 3];
                int iteration = Integer.parseInt(pathInfos[offset + 4]);
                fileName = URLDecoder.decode(pathInfos[offset + 5], "UTF-8");
                docPK = new DocumentIterationKey(workspaceId, docMId, docMVersion, iteration);
                binaryResource = documentService.saveFileInDocument(docPK, fileName, 0);
            } else if (elementType.equals("document-templates")) {
                String templateID = URLDecoder.decode(pathInfos[offset + 2], "UTF-8");
                fileName = URLDecoder.decode(pathInfos[offset + 3], "UTF-8");
                templatePK = new DocumentMasterTemplateKey(workspaceId, templateID);
                binaryResource = documentService.saveFileInTemplate(templatePK, fileName, 0);
            } else if (elementType.equals("part-templates")) {
                String templateID = URLDecoder.decode(pathInfos[offset + 2], "UTF-8");
                fileName = URLDecoder.decode(pathInfos[offset + 3], "UTF-8");
                partTemplatePK = new PartMasterTemplateKey(workspaceId, templateID);
                binaryResource = productService.saveFileInTemplate(partTemplatePK, fileName, 0);
            } else if (elementType.equals("parts")) {
                String partMNumber = URLDecoder.decode(pathInfos[offset + 2], "UTF-8");
                String partMVersion = pathInfos[offset + 3];
                int iteration = Integer.parseInt(pathInfos[offset + 4]);
                partPK = new PartIterationKey(workspaceId, partMNumber, partMVersion, iteration);
                if (pathInfos.length==offset + 7) {
                    fileName = URLDecoder.decode(pathInfos[offset + 6], "UTF-8");
                    binaryResource = productService.saveNativeCADInPartIteration(partPK, fileName, 0);
                } else {
                    fileName = URLDecoder.decode(pathInfos[offset + 5], "UTF-8");
                    binaryResource = productService.saveFileInPartIteration(partPK, fileName, 0);
                }
            }

            Collection<Part> uploadedParts = pRequest.getParts();
            long length = 0;

            for (Part item : uploadedParts) {
                InputStream inputStream = null;
                OutputStream outputStream = null;
                try {
                    outputStream = dataManager.getBinaryResourceOutputStream(binaryResource);
                    inputStream = item.getInputStream();
                    length = ByteStreams.copy(inputStream, outputStream);
                } finally {
                    inputStream.close();
                    outputStream.flush();
                    outputStream.close();
                }
                break;
            }

            /*
            * TODO: this next bloc update the content length on the BinaryResource,
            * It would be more readable to use a dedicated method
            */
            if (elementType.equals("documents")) {
                documentService.saveFileInDocument(docPK, fileName, length);
                documentPostUploaderService.process(binaryResource);
            } else if (elementType.equals("document-templates")) {
                documentService.saveFileInTemplate(templatePK, fileName, length);
            } else if (elementType.equals("part-templates")) {
                productService.saveFileInTemplate(partTemplatePK, fileName, length);
            } else if (elementType.equals("parts")) {
                if (pathInfos.length==offset + 7) {
                    productService.saveNativeCADInPartIteration(partPK, fileName, length);
                    //TODO: Should be put in a DocumentPostUploader plugin
                    converterService.convertCADFileToJSON(partPK, binaryResource);
                } else {
                    productService.saveFileInPartIteration(partPK, fileName, length);
                }
            }
            utx.commit();
        } catch (Exception pEx) {
            pResponse.setHeader("Reason-Phrase", pEx.getMessage());
            if(pEx instanceof NotAllowedException || pEx instanceof AccessRightException){
                pResponse.sendError(HttpServletResponse.SC_FORBIDDEN,pEx.getMessage());
            }else{
                throw new ServletException("Error while uploading the file.", pEx);
            }
        } finally {
            try {
                if (utx.getStatus() == Status.STATUS_ACTIVE || utx.getStatus() == Status.STATUS_MARKED_ROLLBACK) {
                    utx.rollback();
                }
            } catch (Exception pRBEx) {
                pResponse.setHeader("Reason-Phrase", pRBEx.getMessage());
                throw new ServletException("Rollback failed.", pRBEx);
            }
        }
    }

    private void setLastModifiedHeaders(long lastModified, HttpServletResponse pResponse) {
        DateFormat httpDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        httpDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        Calendar cal = new GregorianCalendar();
        cal.setTimeInMillis(lastModified);
        pResponse.setHeader("Last-Modified", httpDateFormat.format(cal.getTime()));
        pResponse.setHeader("Pragma", "");
    }

    private void setCacheHeaders(int cacheSeconds, HttpServletResponse pResponse) {
        pResponse.setHeader("Cache-Control", "max-age=" + cacheSeconds);
        DateFormat httpDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        httpDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        Calendar cal = new GregorianCalendar();
        cal.add(Calendar.SECOND, cacheSeconds);
        pResponse.setHeader("Expires", httpDateFormat.format(cal.getTime()));
        pResponse.setHeader("Pragma", "");
    }

    @Override
    protected void doHead(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    }

}
