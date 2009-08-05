/*
 * DocDoku, Professional Open Source
 * Copyright 2006, 2007, 2008, 2009 DocDoku SARL
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

import com.docdoku.core.entities.*;
import com.docdoku.core.entities.keys.*;
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

import com.docdoku.core.*;
import com.docdoku.core.util.FileIO;
import javax.annotation.Resource;
import javax.transaction.Status;
import javax.transaction.UserTransaction;
import net.sf.jodconverter.OfficeDocumentConverter;
import net.sf.jodconverter.office.ManagedProcessOfficeManager;
import net.sf.jodconverter.office.ManagedProcessOfficeManagerConfiguration;
import net.sf.jodconverter.office.OfficeConnectionMode;
import net.sf.jodconverter.office.OfficeManager;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

public class UploadDownloadServlet extends HttpServlet {

    @EJB
    private ICommandLocal commandService;
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
                String mdocID = URLDecoder.decode(pathInfos[offset + 2], "UTF-8");
                String mdocVersion = pathInfos[offset + 3];
                int iteration = Integer.parseInt(pathInfos[offset + 4]);
                String fileName = URLDecoder.decode(pathInfos[offset + 5], "UTF-8");
                fullName = workspaceId + "/" + elementType + "/" + mdocID + "/" + mdocVersion + "/" + iteration + "/" + fileName;
            } else if (elementType.equals("templates")) {
                String templateID = URLDecoder.decode(pathInfos[offset + 2], "UTF-8");
                String fileName = URLDecoder.decode(pathInfos[offset + 3], "UTF-8");
                fullName = workspaceId + "/" + elementType + "/" + templateID + "/" + fileName;
            }

            File dataFile = commandService.getDataFile(fullName);

            if ("pdf".equals(pRequest.getParameter("type"))) {
                pResponse.setContentType("application/pdf");
                
                File pdfFile=null;
                if("pdf".equals(FileIO.getExtension(dataFile))){
                    pdfFile=dataFile;
                }else{
                    String pdfFileNameWOExt = FileIO.getFileNameWithoutExtension(dataFile);
                    pdfFile = new File(dataFile.getParentFile(), pdfFileNameWOExt + ".pdf");
                    String ooHome = this.getInitParameter("OO_HOME");
                    int ooPort = Integer.parseInt(this.getInitParameter("OO_PORT"));
                    ManagedProcessOfficeManagerConfiguration cfg=new ManagedProcessOfficeManagerConfiguration(OfficeConnectionMode.socket(ooPort));
                    cfg.setOfficeHome(new File(ooHome));
                    OfficeManager officeManager = new ManagedProcessOfficeManager(cfg);
                    officeManager.start();
                    OfficeDocumentConverter converter = new OfficeDocumentConverter(officeManager);
                    converter.convert(dataFile, pdfFile);
                }
                pResponse.setHeader("Content-disposition", "attachment; filename=\"" + pdfFile.getName() + "\"");
                pResponse.setContentLength((int) pdfFile.length());

                ServletOutputStream httpOut = pResponse.getOutputStream();
                InputStream input = new BufferedInputStream(new FileInputStream(pdfFile), BUFFER_CAPACITY);

                byte[] data = new byte[CHUNK_SIZE];
                int length;
                while ((length = input.read(data)) != -1) {
                    httpOut.write(data, 0, length);
                }
                input.close();
                httpOut.flush();
                httpOut.close();
            } else if ("swf".equals(pRequest.getParameter("type"))) {
                pResponse.setContentType("application/x-shockwave-flash");
                String pdf2swfPath = this.getInitParameter("PDF2SWF_HOME");

                FileAVT fileAVT= new FileAVT(dataFile.getPath());
                String ooHome = this.getInitParameter("OO_HOME");
                int ooPort = Integer.parseInt(this.getInitParameter("OO_PORT"));

                FileConverter fileConverter = new FileConverter(pdf2swfPath, fileAVT, ooHome, ooPort);
                File swfFile = fileConverter.convertToSwf();
                //pResponse.setHeader("Content-disposition", "attachment; filename=\"" + swfFile.getName() + "\"");
                pResponse.setContentLength((int) swfFile.length());

                ServletOutputStream httpOut = pResponse.getOutputStream();
                InputStream input = new BufferedInputStream(new FileInputStream(swfFile), BUFFER_CAPACITY);
                byte[] data = new byte[CHUNK_SIZE];
                int length;
                while ((length = input.read(data)) != -1) {
                    httpOut.write(data, 0, length);
                }

                input.close();
                httpOut.flush();
                httpOut.close();
            } else {
                pResponse.setHeader("Content-disposition", "attachment; filename=\"" + dataFile.getName() + "\"");
                pResponse.setContentLength((int) dataFile.length());

                String contentType = FileTypeMap.getDefaultFileTypeMap().getContentType(dataFile);
                pResponse.setContentType(contentType);
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
            }
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
        DocumentKey docPK = null;
        BasicElementKey templatePK = null;
        File vaultFile = null;

        try {
            utx.begin();
            if (elementType.equals("documents")) {
                String mdocID = URLDecoder.decode(pathInfos[offset + 2], "UTF-8");
                String mdocVersion = pathInfos[offset + 3];
                int iteration = Integer.parseInt(pathInfos[offset + 4]);
                fileName = URLDecoder.decode(pathInfos[offset + 5], "UTF-8");
                docPK = new DocumentKey(workspaceId, mdocID, mdocVersion, iteration);
                vaultFile = commandService.saveFileInDocument(docPK, fileName, 0);

            } else if (elementType.equals("templates")) {
                String templateID = URLDecoder.decode(pathInfos[offset + 2], "UTF-8");
                fileName = URLDecoder.decode(pathInfos[offset + 3], "UTF-8");
                templatePK = new BasicElementKey(workspaceId, templateID);
                vaultFile = commandService.saveFileInTemplate(templatePK, fileName, 0);
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
                commandService.saveFileInDocument(docPK, fileName, vaultFile.length());
            } else if (elementType.equals("templates")) {
                commandService.saveFileInTemplate(templatePK, fileName, vaultFile.length());
            }
            utx.commit();
        } catch (Exception pEx) {
            throw new ServletException("Error while uploading the file.", pEx);
        } finally {
            try {
                if (utx.getStatus() == Status.STATUS_ACTIVE) {
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
