/*
 * DocDoku, Professional Open Source
 * Copyright 2006, 2007, 2008, 2009, 2010, 2011, 2012 DocDoku SARL
 *
 * This file is part of DocDoku.
 *
 * DocDoku is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DocDoku is distributed in the hope that it will be useful,  
 * but WITHOUT ANY WARRANTY; without even the implied warranty of  
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the  
 * GNU General Public License for more details.  
 *  
 * You should have received a copy of the GNU General Public License  
 * along with DocDoku.  If not, see <http://www.gnu.org/licenses/>.  
 */

package com.docdoku.server.http;

import com.docdoku.core.services.IDocumentManagerLocal;
import com.docdoku.core.document.DocumentIterationKey;
import com.docdoku.core.document.DocumentMasterTemplateKey;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.util.regex.Pattern;
import javax.activation.FileTypeMap;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServlet;

import javax.annotation.Resource;
import javax.transaction.Status;
import javax.transaction.UserTransaction;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

public class UploadDownloadServlet extends HttpServlet {

    @EJB
    private IDocumentManagerLocal documentService;
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
            String[] pathInfos = Pattern.compile("/").split(pRequest.getRequestURI());
            int offset;
            if (pRequest.getContextPath().equals("")) {
                offset = 2;
            } else {
                offset = 3;
            }


            String workspaceId = URLDecoder.decode(pathInfos[offset], "UTF-8");
            String elementType = pathInfos[offset + 1];
            String fullName = null;

            if (elementType.equals("documents")) {
                String docMId = URLDecoder.decode(pathInfos[offset + 2], "UTF-8");
                String docMVersion = pathInfos[offset + 3];
                int iteration = Integer.parseInt(pathInfos[offset + 4]);
                String fileName = URLDecoder.decode(pathInfos[offset + 5], "UTF-8");
                fullName = workspaceId + "/" + elementType + "/" + docMId + "/" + docMVersion + "/" + iteration + "/" + fileName;
            } else if (elementType.equals("templates")) {
                String templateID = URLDecoder.decode(pathInfos[offset + 2], "UTF-8");
                String fileName = URLDecoder.decode(pathInfos[offset + 3], "UTF-8");
                fullName = workspaceId + "/" + elementType + "/" + templateID + "/" + fileName;
            }

            File dataFile = documentService.getDataFile(fullName);
            File fileToOutput;
            if ("pdf".equals(pRequest.getParameter("type"))) {
                pResponse.setContentType("application/pdf");
                String ooHome = getServletContext().getInitParameter("OO_HOME");
                int ooPort = Integer.parseInt(getServletContext().getInitParameter("OO_PORT"));
                fileToOutput = new FileConverter(ooHome, ooPort).convertToPDF(dataFile);
            } else if ("swf".equals(pRequest.getParameter("type"))) {
                pResponse.setContentType("application/x-shockwave-flash");
                String pdf2SWFHome = getServletContext().getInitParameter("PDF2SWF_HOME");
                String ooHome = getServletContext().getInitParameter("OO_HOME");
                int ooPort = Integer.parseInt(getServletContext().getInitParameter("OO_PORT"));
                FileConverter fileConverter = new FileConverter(pdf2SWFHome, ooHome, ooPort);
                fileToOutput = fileConverter.convertToSWF(dataFile);
            } else {
                //pResponse.setHeader("Content-disposition", "attachment; filename=\"" + dataFile.getName() + "\"");             
                String contentType = FileTypeMap.getDefaultFileTypeMap().getContentType(dataFile);
                pResponse.setContentType(contentType);
                fileToOutput = dataFile;
            }
            pResponse.setContentLength((int) fileToOutput.length());
            ServletOutputStream httpOut = pResponse.getOutputStream();
            InputStream input = new BufferedInputStream(new FileInputStream(fileToOutput), BUFFER_CAPACITY);

            byte[] data = new byte[CHUNK_SIZE];
            int length;
            while ((length = input.read(data)) != -1) {
                httpOut.write(data, 0, length);
            }
            input.close();
            httpOut.flush();
            httpOut.close();

        } catch (Exception pEx) {
            throw new ServletException("Error while downloading the file.", pEx);
        }
    }

    @Override
    protected void doPost(HttpServletRequest pRequest, HttpServletResponse pResponse) throws ServletException, IOException {

        boolean isMultipart = ServletFileUpload.isMultipartContent(pRequest);
        if (!isMultipart) {
            throw new ServletException("Error, the request doesn't contain a multipart content.");
        }

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
        DocumentIterationKey docPK = null;
        DocumentMasterTemplateKey templatePK = null;
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

            } else if (elementType.equals("templates")) {
                String templateID = URLDecoder.decode(pathInfos[offset + 2], "UTF-8");
                fileName = URLDecoder.decode(pathInfos[offset + 3], "UTF-8");
                templatePK = new DocumentMasterTemplateKey(workspaceId, templateID);
                vaultFile = documentService.saveFileInTemplate(templatePK, fileName, 0);
            }
            vaultFile.getParentFile().mkdirs();
            vaultFile.createNewFile();
            ServletFileUpload upload = new ServletFileUpload();
            FileItemIterator iter = upload.getItemIterator(pRequest);
            while (iter.hasNext()) {
                FileItemStream item = iter.next();
                if (!item.isFormField()) {
                    String name = item.getFieldName();
                    if (name.equals("upload")) {
                        InputStream in = new BufferedInputStream(item.openStream(), BUFFER_CAPACITY);
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
                    }
                }
            }
            if (elementType.equals("documents")) {
                documentService.saveFileInDocument(docPK, fileName, vaultFile.length());
            } else if (elementType.equals("templates")) {
                documentService.saveFileInTemplate(templatePK, fileName, vaultFile.length());
            }
            utx.commit();
        } catch (Exception pEx) {
            throw new ServletException("Error while uploading the file.", pEx);
        } finally {
            try {
                if (utx.getStatus() == Status.STATUS_ACTIVE || utx.getStatus()==Status.STATUS_MARKED_ROLLBACK) {
                    utx.rollback();
                }
            } catch (Exception pRBEx) {
                throw new ServletException("Rollback failed.", pRBEx);
            }
        }
    }

    @Override
    protected void doHead(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    }
}
