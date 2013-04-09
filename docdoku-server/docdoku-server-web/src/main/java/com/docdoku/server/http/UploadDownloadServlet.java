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

import com.docdoku.core.document.DocumentIterationKey;
import com.docdoku.core.document.DocumentMasterTemplateKey;
import com.docdoku.core.product.PartIterationKey;
import com.docdoku.core.product.PartMasterTemplateKey;
import com.docdoku.core.services.*;
import org.apache.commons.lang.StringUtils;

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
import java.io.*;
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

    private final static int CHUNK_SIZE = 1024 * 8;
    private final static int BUFFER_CAPACITY = 1024 * 16;
    @Resource
    private UserTransaction utx;

    @Override
    protected void doGet(HttpServletRequest pRequest,
                         HttpServletResponse pResponse)
            throws ServletException, IOException {

        try {
            String login = pRequest.getRemoteUser();

            //String[] pathInfos = Pattern.compile("/").split(pRequest.getRequestURI());
            //remove empty entries because of Three.js that generates url with double /
            String[] pathInfos = UploadDownloadServlet.removeEmptyEntries(pRequest.getRequestURI().split("/"));
            int offset;
            if (pRequest.getContextPath().equals("")) {
                offset = 1;
            } else {
                offset = 2;
            }


            String workspaceId = URLDecoder.decode(pathInfos[offset], "UTF-8");
            String elementType = pathInfos[offset + 1];
            String fullName = null;
            File dataFile = null;

            if (elementType.equals("documents")) {
                //documents are versioned objects so we can rely on client cache without any risk 
                setCacheHeaders(86400, pResponse);
                String docMId = URLDecoder.decode(pathInfos[offset + 2], "UTF-8");
                String docMVersion = pathInfos[offset + 3];
                int iteration = Integer.parseInt(pathInfos[offset + 4]);
                String fileName = URLDecoder.decode(pathInfos[offset + 5], "UTF-8");
                String[] pathInfosExtra = new String[]{};
                if (pathInfos.length > offset + 6) {
                    pathInfosExtra = Arrays.copyOfRange(pathInfos, offset + 6, pathInfos.length);
                }
                String subResourceName = StringUtils.join(pathInfosExtra, '/');
                fullName = workspaceId + "/" + elementType + "/" + docMId + "/" + docMVersion + "/" + iteration + "/" + fileName;
                dataFile = documentResourceGetterService.getDataFile(fullName, subResourceName);
            } else if (elementType.equals("document-templates")) {
                String templateID = URLDecoder.decode(pathInfos[offset + 2], "UTF-8");
                String fileName = URLDecoder.decode(pathInfos[offset + 3], "UTF-8");
                fullName = workspaceId + "/" + elementType + "/" + templateID + "/" + fileName;
                dataFile = documentService.getDataFile(fullName);
            }else if (elementType.equals("part-templates")) {
                String templateID = URLDecoder.decode(pathInfos[offset + 2], "UTF-8");
                String fileName = URLDecoder.decode(pathInfos[offset + 3], "UTF-8");
                fullName = workspaceId + "/" + elementType + "/" + templateID + "/" + fileName;
                dataFile = documentService.getDataFile(fullName);
            }
            else if (elementType.equals("parts")) {
                //parts are versioned objects so we can rely on client cache without any risk 
                setCacheHeaders(86400, pResponse);
                String partNumber = URLDecoder.decode(pathInfos[offset + 2], "UTF-8");
                String version = pathInfos[offset + 3];
                int iteration = Integer.parseInt(pathInfos[offset + 4]);
                String fileName;
                if(pathInfos.length==offset + 7){
                    fileName = URLDecoder.decode(pathInfos[offset + 6], "UTF-8");
                    String subType = URLDecoder.decode(pathInfos[offset + 5], "UTF-8"); //subType may be nativecad
                    fullName = workspaceId + "/" + elementType + "/" + partNumber + "/" + version + "/" + iteration + "/" + subType + "/" + fileName;
                }else{
                    fileName = URLDecoder.decode(pathInfos[offset + 5], "UTF-8");
                    fullName = workspaceId + "/" + elementType + "/" + partNumber + "/" + version + "/" + iteration + "/" + fileName;
                }
                dataFile = productService.getDataFile(fullName);
            }

            //set content type
            String contentType = FileTypeMap.getDefaultFileTypeMap().getContentType(dataFile);
            pResponse.setContentType(contentType);

            if (elementType.equals("documents") && "viewer".equals(pRequest.getParameter("type"))) {
                dataFile = documentViewerService.prepareFileForViewer(pRequest, pResponse, getServletContext(), dataFile);
            }

            long lastModified = dataFile.lastModified();
            long ifModified = pRequest.getDateHeader("If-Modified-Since");

            if (lastModified > ifModified) {
                setLastModifiedHeaders(lastModified, pResponse);
                pResponse.setContentLength((int) dataFile.length());
                ServletOutputStream httpOut = pResponse.getOutputStream();
                InputStream input = new BufferedInputStream(new FileInputStream(dataFile), BUFFER_CAPACITY);

                byte[] data = new byte[CHUNK_SIZE];
                int length;
                while ((length = input.read(data)) != -1) {
                    httpOut.write(data, 0, length);
                }
                input.close();
                httpOut.flush();
                httpOut.close();
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

        String login = pRequest.getRemoteUser();
        String[] pathInfos = Pattern.compile("/").split(pRequest.getRequestURI());
        int offset;
        if (pRequest.getContextPath().equals("")) {
            offset = 2;
        } else {
            offset = 3;
        }

        String workspaceId = URLDecoder.decode(pathInfos[offset], "UTF-8");
        String elementType = pathInfos[offset + 1];

        String fileName = null;
        PartIterationKey partPK = null;
        DocumentIterationKey docPK = null;
        DocumentMasterTemplateKey templatePK = null;
        PartMasterTemplateKey partTemplatePK = null;
        File vaultFile = null;

        try {
            utx.begin();
            if (elementType.equals("documents")) {
                String docMId = URLDecoder.decode(pathInfos[offset + 2], "UTF-8");
                String docMVersion = pathInfos[offset + 3];
                int iteration = Integer.parseInt(pathInfos[offset + 4]);
                fileName = URLDecoder.decode(pathInfos[offset + 5], "UTF-8");
                docPK = new DocumentIterationKey(workspaceId, docMId, docMVersion, iteration);
                vaultFile = documentService.saveFileInDocument(docPK, fileName, 0);
            } else if (elementType.equals("document-templates")) {
                String templateID = URLDecoder.decode(pathInfos[offset + 2], "UTF-8");
                fileName = URLDecoder.decode(pathInfos[offset + 3], "UTF-8");
                templatePK = new DocumentMasterTemplateKey(workspaceId, templateID);
                vaultFile = documentService.saveFileInTemplate(templatePK, fileName, 0);
            }else if (elementType.equals("part-templates")) {
                String templateID = URLDecoder.decode(pathInfos[offset + 2], "UTF-8");
                fileName = URLDecoder.decode(pathInfos[offset + 3], "UTF-8");
                partTemplatePK = new PartMasterTemplateKey(workspaceId, templateID);
                vaultFile = productService.saveFileInTemplate(partTemplatePK, fileName, 0);
            }
            else if (elementType.equals("parts")) {
                String partMNumber = URLDecoder.decode(pathInfos[offset + 2], "UTF-8");
                String partMVersion = pathInfos[offset + 3];
                int iteration = Integer.parseInt(pathInfos[offset + 4]);
                partPK = new PartIterationKey(workspaceId, partMNumber, partMVersion, iteration);
                if(pathInfos.length==offset + 7){
                    fileName = URLDecoder.decode(pathInfos[offset + 6], "UTF-8");
                    vaultFile = productService.saveNativeCADInPartIteration(partPK, fileName, 0);
                }else{
                    fileName = URLDecoder.decode(pathInfos[offset + 5], "UTF-8");
                    vaultFile = productService.saveFileInPartIteration(partPK, fileName, 0);
                }
            }
            vaultFile.getParentFile().mkdirs();
            Collection<Part> uploadedParts = pRequest.getParts();
            for (Part item : uploadedParts) {
                InputStream in = new BufferedInputStream(item.getInputStream(), BUFFER_CAPACITY);
                OutputStream out = new BufferedOutputStream(new FileOutputStream(vaultFile), BUFFER_CAPACITY);

                byte[] data = new byte[CHUNK_SIZE];
                int length;
                try {
                    while ((length = in.read(data)) != -1) {
                        out.write(data, 0, length);
                    }
                } finally {
                    in.close();
                    out.close();
                }
                break;
            }
            if (elementType.equals("documents")) {
                File fileSaved = documentService.saveFileInDocument(docPK, fileName, vaultFile.length());
                documentPostUploaderService.process(fileSaved);
            } else if (elementType.equals("document-templates")) {
                documentService.saveFileInTemplate(templatePK, fileName, vaultFile.length());
            } else if (elementType.equals("part-templates")) {
                productService.saveFileInTemplate(partTemplatePK, fileName, vaultFile.length());
            }
            else if (elementType.equals("parts")) {
                if(pathInfos.length==offset + 7){
                    productService.saveNativeCADInPartIteration(partPK, fileName, vaultFile.length());
                    converterService.convertCADFileToJSON(partPK, vaultFile);
                }else{
                    productService.saveFileInPartIteration(partPK, fileName, vaultFile.length());
                }
            }
            utx.commit();
        } catch (Exception pEx) {
            pResponse.setHeader("Reason-Phrase", pEx.getMessage());
            throw new ServletException("Error while uploading the file.", pEx);
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

    private static String[] removeEmptyEntries(String[] entries) {
        List<String> elements = new LinkedList<String>(Arrays.asList(entries));

        for (Iterator<String> it = elements.iterator(); it.hasNext(); ) {
            if (it.next().isEmpty()) {
                it.remove();
            }
        }
        return elements.toArray(new String[elements.size()]);
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
