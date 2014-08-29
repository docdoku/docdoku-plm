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
import com.docdoku.core.exceptions.AccessRightException;
import com.docdoku.core.exceptions.NotAllowedException;
import com.docdoku.core.product.PartIterationKey;
import com.docdoku.core.product.PartMasterTemplateKey;
import com.docdoku.core.services.*;
import com.google.common.io.ByteStreams;
import com.google.common.io.InputSupplier;

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
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;


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

    private static final int DEFAULT_BUFFER_SIZE = 4096;
    private static final String MULTIPART_BOUNDARY = "MULTIPART_BYTERANGES";

    @Override
    protected void doHead(HttpServletRequest pRequest, HttpServletResponse pResponse) throws ServletException, IOException{
        // Process request without content.
        processRequest(pRequest, pResponse, false);
    }

    @Override
    protected void doGet(HttpServletRequest pRequest, HttpServletResponse pResponse) throws ServletException, IOException {
        // Process request with content.
        processRequest(pRequest, pResponse, true);
    }

    private void processRequest(HttpServletRequest pRequest, HttpServletResponse pResponse, boolean content) throws ServletException, IOException {

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

            // 404 if no binary resource
            if(binaryResource == null){
                pResponse.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }


            // Prepare some variables to process the response
            long lastModified = binaryResource.getLastModified().getTime();
            long length = binaryResource.getContentLength();
            String fileName = binaryResource.getName();
            String eTag = fileName + "_" + length + "_" + lastModified;

            // Insure we serve the requested output format
            if(outputFormat != null){
                fileName+="."+outputFormat;
            }

            fileName = URLEncoder.encode(fileName, "UTF-8");
            
            // Set content type
            String contentType;
            if (isSubResource) {
                contentType = FileTypeMap.getDefaultFileTypeMap().getContentType(subResourceVirtualPath);
            } else {
                if (isDocumentAndOutputSpecified) {
                    contentType = FileTypeMap.getDefaultFileTypeMap().getContentType(fullName + "." + outputFormat);
                } else {
                    contentType = FileTypeMap.getDefaultFileTypeMap().getContentType(fullName);
                }
            }

            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            // If-None-Match header should contain "*" or ETag. If so, then return 304.
            String ifNoneMatch = pRequest.getHeader("If-None-Match");
            if (ifNoneMatch != null && matches(ifNoneMatch, eTag)) {
                pResponse.setHeader("ETag", eTag);
                pResponse.sendError(HttpServletResponse.SC_NOT_MODIFIED);
                return;
            }


            // If-Modified-Since header should be greater than LastModified. If so, then return 304.
            // This header is ignored if any If-None-Match header is specified.
            long ifModifiedSince = pRequest.getDateHeader("If-Modified-Since");
            if (ifNoneMatch == null && ifModifiedSince != -1 && ifModifiedSince + 1000 > lastModified) {
                pResponse.setHeader("ETag", eTag); // Required in 304.
                pResponse.sendError(HttpServletResponse.SC_NOT_MODIFIED);
                return;
            }


            // Validate request headers for resume ----------------------------------------------------

            // If-Match header should contain "*" or ETag. If not, then return 412.
            String ifMatch = pRequest.getHeader("If-Match");
            if (ifMatch != null && !matches(ifMatch, eTag)) {
                pResponse.sendError(HttpServletResponse.SC_PRECONDITION_FAILED);
                return;
            }

            // If-Unmodified-Since header should be greater than LastModified. If not, then return 412.
            long ifUnmodifiedSince = pRequest.getDateHeader("If-Unmodified-Since");
            if (ifUnmodifiedSince != -1 && ifUnmodifiedSince + 1000 <= lastModified) {
                pResponse.sendError(HttpServletResponse.SC_PRECONDITION_FAILED);
                return;
            }

            // Validate and process range -------------------------------------------------------------

            Range full = new Range(0, length - 1, length);
            List<Range> ranges = new ArrayList<>();

            // Validate and process Range and If-Range headers.
            String range = pRequest.getHeader("Range");
            if (range != null) {

                // Range header should match format "bytes=n-n,n-n,n-n...". If not, then return 416.
                if (!range.matches("^bytes=\\d*-\\d*(,\\d*-\\d*)*$")) {
                    pResponse.setHeader("Content-Range", "bytes */" + length); // Required in 416.
                    pResponse.sendError(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
                    return;
                }

                // If-Range header should either match ETag or be greater then LastModified. If not,
                // then return full file.
                String ifRange = pRequest.getHeader("If-Range");
                if (ifRange != null && !ifRange.equals(eTag)) {
                    try {
                        long ifRangeTime = pRequest.getDateHeader("If-Range"); // Throws IAE if invalid.
                        if (ifRangeTime != -1 && ifRangeTime + 1000 < lastModified) {
                            ranges.add(full);
                        }
                    } catch (IllegalArgumentException ignore) {
                        ranges.add(full);
                    }
                }

                // If any valid If-Range header, then process each part of byte range.
                if (ranges.isEmpty()) {
                    for (String part : range.substring(6).split(",")) {
                        // Assuming a file with length of 100, the following examples returns bytes at:
                        // 50-80 (50 to 80), 40- (40 to length=100), -20 (length-20=80 to length=100).
                        long start = sublong(part, 0, part.indexOf("-"));
                        long end = sublong(part, part.indexOf("-") + 1, part.length());

                        if (start == -1) {
                            start = length - end;
                            end = length - 1;
                        } else if (end == -1 || end > length - 1) {
                            end = length - 1;
                        }

                        // Check if Range is syntactically valid. If not, then return 416.
                        if (start > end) {
                            pResponse.setHeader("Content-Range", "bytes */" + length); // Required in 416.
                            pResponse.sendError(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
                            return;
                        }

                        // Add range.
                        ranges.add(new Range(start, end, length));
                    }
                }
            }

            // Prepare and initialize response --------------------------------------------------------

            boolean acceptsGzip;
            String disposition = "inline";

            // If client accepts gzip for the resource :
            // We don't gzip geometry files or nativecad, cause we need the content length in the response headers
            // Should be better if client tell us he doesn't want gzip.
            String acceptEncoding = pRequest.getHeader("Accept-Encoding");
            acceptsGzip = acceptEncoding != null && accepts(acceptEncoding, "gzip") && !binaryResource.getOwnerType().equals("parts");

            if (contentType.startsWith("text")) {
                contentType += ";charset=UTF-8";
            }

            // Expect for images, determine content disposition. If content type is supported by
            // the browser, then set to inline, else attachment which will pop a 'save as' dialogue.
            if (!contentType.startsWith("image")) {
                String accept = pRequest.getHeader("Accept");
                disposition = accept != null && accepts(accept, contentType) ? "inline" : "attachment";
            }

            if(!isDocumentAndOutputSpecified && !isSubResource){
                disposition = "attachment";
            }

            pResponse.reset();
            pResponse.setBufferSize(DEFAULT_BUFFER_SIZE);
            pResponse.setHeader("Content-Disposition", disposition + ";filename=\"" + fileName  + "\"");
            pResponse.setHeader("Accept-Ranges", "bytes");
            pResponse.setHeader("ETag", eTag);
            pResponse.setContentType(contentType);
            setLastModifiedHeaders(lastModified, pResponse);
            if(needsCacheHeaders){
                setCacheHeaders(86400,pResponse);
            }


            // Prepare the input stream  --------------------------------------------------------
            InputStream binaryContentInputStream;

            if (isSubResource) {
                binaryContentInputStream = dataManager.getBinarySubResourceInputStream(binaryResource, subResourceVirtualPath);
            } else {
                if (isDocumentAndOutputSpecified) {
                    binaryContentInputStream = documentResourceGetterService.getConvertedResource(outputFormat, binaryResource, docI, user);
                } else {
                    binaryContentInputStream = dataManager.getBinaryResourceInputStream(binaryResource);
                }
            }

            // Send file (or parts) to client  --------------------------------------------------------
            OutputStream httpOut = pResponse.getOutputStream();

            try{
                Range r;
                if (ranges.isEmpty() || ranges.get(0) == full) {
                    r = full;
                    pResponse.setContentType(contentType);
                    pResponse.setHeader("Content-Range", "bytes " + r.start + "-" + r.end + "/" + r.total);
                    if (content) {
                        if (acceptsGzip) {
                            pResponse.setHeader("Content-Encoding", "gzip");
                            httpOut = new GZIPOutputStream(httpOut, DEFAULT_BUFFER_SIZE);
                        } else {
                            pResponse.setContentLength((int) binaryResource.getContentLength());
                        }
                        copy(binaryContentInputStream, httpOut, r.start, r.length,length);
                    }

                } else if (ranges.size() == 1) {
                    r = ranges.get(0);
                    pResponse.setContentType(contentType);
                    pResponse.setHeader("Content-Range", "bytes " + r.start + "-" + r.end + "/" + r.total);
                    pResponse.setContentLength((int) r.length);
                    pResponse.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT); // 206.

                    if (content) {
                        copy(binaryContentInputStream, httpOut, r.start, r.length,length);
                    }

                } else {

                    // Return multiple parts of file.
                    pResponse.setContentType("multipart/byteranges; boundary=" + MULTIPART_BOUNDARY);
                    pResponse.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT); // 206.

                    if (content) {
                        // Cast back to ServletOutputStream to get the easy println methods.
                        ServletOutputStream sos = (ServletOutputStream) httpOut;

                        // Copy multi part range.
                        for (Range range1 : ranges) {
                            // Add multipart boundary and header fields for every range.
                            sos.println();
                            sos.println("--" + MULTIPART_BOUNDARY);
                            sos.println("Content-Type: " + contentType);
                            sos.println("Content-Range: bytes " + range1.start + "-" + range1.end + "/" + range1.total);

                            // Copy single part range of multi part range.
                            copy(binaryContentInputStream, httpOut, range1.start, range1.length,length);
                        }

                        // End with multipart boundary.
                        sos.println();
                        sos.println("--" + MULTIPART_BOUNDARY + "--");
                    }
                }

            }
            finally {
                close(binaryContentInputStream);
                if(httpOut  != null){
                    httpOut.flush();
                }
                close(httpOut);
            }

        } catch (Exception pEx) {
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

        ServletException rollbackFailException = null;
        try {
            utx.begin();
            switch (elementType) {
                case "documents": {
                    String docMId = URLDecoder.decode(pathInfos[offset + 2], "UTF-8");
                    String docMVersion = pathInfos[offset + 3];
                    int iteration = Integer.parseInt(pathInfos[offset + 4]);
                    fileName = URLDecoder.decode(pathInfos[offset + 5], "UTF-8");
                    docPK = new DocumentIterationKey(workspaceId, docMId, docMVersion, iteration);
                    binaryResource = documentService.saveFileInDocument(docPK, fileName, 0);
                    break;
                }
                case "document-templates": {
                    String templateID = URLDecoder.decode(pathInfos[offset + 2], "UTF-8");
                    fileName = URLDecoder.decode(pathInfos[offset + 3], "UTF-8");
                    templatePK = new DocumentMasterTemplateKey(workspaceId, templateID);
                    binaryResource = documentService.saveFileInTemplate(templatePK, fileName, 0);
                    break;
                }
                case "part-templates": {
                    String templateID = URLDecoder.decode(pathInfos[offset + 2], "UTF-8");
                    fileName = URLDecoder.decode(pathInfos[offset + 3], "UTF-8");
                    partTemplatePK = new PartMasterTemplateKey(workspaceId, templateID);
                    binaryResource = productService.saveFileInTemplate(partTemplatePK, fileName, 0);
                    break;
                }
                case "parts": {
                    String partMNumber = URLDecoder.decode(pathInfos[offset + 2], "UTF-8");
                    String partMVersion = pathInfos[offset + 3];
                    int iteration = Integer.parseInt(pathInfos[offset + 4]);
                    partPK = new PartIterationKey(workspaceId, partMNumber, partMVersion, iteration);
                    if (pathInfos.length == offset + 7) {
                        fileName = URLDecoder.decode(pathInfos[offset + 6], "UTF-8");
                        binaryResource = productService.saveNativeCADInPartIteration(partPK, fileName, 0);
                    } else {
                        fileName = URLDecoder.decode(pathInfos[offset + 5], "UTF-8");
                        binaryResource = productService.saveFileInPartIteration(partPK, fileName, 0);
                    }
                    break;
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
                // TODO Check "Why use a loop if you break systematically?"
                break;
            }

            /*
            * TODO: this next bloc update the content length on the BinaryResource,
            * It would be more readable to use a dedicated method
            */
            switch (elementType) {
                case "documents":
                    documentService.saveFileInDocument(docPK, fileName, length);
                    documentPostUploaderService.process(binaryResource);
                    break;
                case "document-templates":
                    documentService.saveFileInTemplate(templatePK, fileName, length);
                    break;
                case "part-templates":
                    productService.saveFileInTemplate(partTemplatePK, fileName, length);
                    break;
                case "parts":
                    if (pathInfos.length == offset + 7) {
                        productService.saveNativeCADInPartIteration(partPK, fileName, length);
                        //TODO: Should be put in a DocumentPostUploader plugin
                        converterService.convertCADFileToJSON(partPK, binaryResource);
                    } else {
                        productService.saveFileInPartIteration(partPK, fileName, length);
                    }
                    break;
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
                rollbackFailException = new ServletException("Rollback failed.", pRBEx);
            }
        }
        if(rollbackFailException != null){
            throw rollbackFailException;
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

    private static boolean matches(String matchHeader, String toMatch) {
        String[] matchValues = matchHeader.split("\\s*,\\s*");
        Arrays.sort(matchValues);
        return Arrays.binarySearch(matchValues, toMatch) > -1 || Arrays.binarySearch(matchValues, "*") > -1;
    }

    private static long sublong(String value, int beginIndex, int endIndex) {
        String substring = value.substring(beginIndex, endIndex);
        return (substring.length() > 0) ? Long.parseLong(substring) : -1;
    }

    private static boolean accepts(String acceptHeader, String toAccept) {
        String[] acceptValues = acceptHeader.split("\\s*(,|;)\\s*");
        Arrays.sort(acceptValues);
        return Arrays.binarySearch(acceptValues, toAccept) > -1
                || Arrays.binarySearch(acceptValues, toAccept.replaceAll("/.*$", "/*")) > -1
                || Arrays.binarySearch(acceptValues, "*/*") > -1;
    }


    private static void close(Closeable resource) {
        if (resource != null) {
            try {
                resource.close();
            } catch (IOException ignore) {
                // Ignore IOException. If you want to handle this anyway, it might be useful to know
                // that this will generally only be thrown when the client aborted the request.
            }
        }
    }

    private static void copy(final InputStream input, OutputStream output, long start, long length, long binaryLength) throws IOException {

        if(start == 0 && binaryLength == length){
            ByteStreams.copy(input, output);
        }else{
            // Slice the input stream considering offset and length
            InputStream slicedInputStream = ByteStreams.slice(new InputSupplier<InputStream>() {
                public InputStream getInput() throws IOException {
                    return input;
                }
            }, start, length).getInput();

            ByteStreams.copy(slicedInputStream, output);
        }
    }

    protected class Range {

        long start;
        long end;
        long length;
        long total;

        public Range(long start, long end, long total) {
            this.start = start;
            this.end = end;
            this.length = end - start + 1;
            this.total = total;
        }

    }

}
