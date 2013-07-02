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

package com.docdoku.server.filters;

import com.docdoku.core.common.Account;
import com.docdoku.core.common.BinaryResource;
import com.docdoku.core.common.User;
import com.docdoku.core.document.DocumentIteration;
import com.docdoku.core.document.DocumentMasterKey;
import com.docdoku.core.product.PartRevisionKey;
import com.docdoku.core.services.IDocumentManagerLocal;
import com.docdoku.core.services.IDocumentResourceGetterManagerLocal;
import com.docdoku.core.services.IProductManagerLocal;
import org.apache.commons.lang.StringUtils;

import javax.ejb.EJB;
import javax.security.auth.login.LoginException;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.regex.Pattern;

public class FilesFilter implements Filter {

    @EJB
    private IProductManagerLocal productService;

    @EJB
    private IDocumentManagerLocal documentService;

    @EJB
    private IDocumentResourceGetterManagerLocal documentResourceGetterService;

    @EJB
    private GuestProxy guestProxy;

    @Override
    public void doFilter(ServletRequest pRequest, ServletResponse pResponse,
                         FilterChain chain) throws IOException, ServletException {


        HttpServletRequest httpRequest = (HttpServletRequest) pRequest;

        // don't filter post requests, security will be handled by doPost in uploadDownloadServlet
        if(httpRequest.getMethod().equalsIgnoreCase("POST")){
            chain.doFilter(pRequest,pResponse);
            return;
        }

        HttpSession sessionHTTP = httpRequest.getSession();
        Account account = (Account) sessionHTTP.getAttribute("account");

        String qs = httpRequest.getQueryString();
        String originURL = httpRequest.getRequestURI() + (qs == null ? "" : "?" + qs);

        int offset = httpRequest.getContextPath().equals("") ? 2 : 3;

        String requestURI = httpRequest.getRequestURI();
        String[] pathInfo = Pattern.compile("/").split(requestURI);

        String fullName = "";

        BinaryResource binaryResource = null;
        DocumentIteration docI = null;

        User user = null;

        boolean isSubResource = false;
        boolean needsCacheHeaders = false;

        String outputFormat = pRequest.getParameter("output");

        boolean isDocumentAndOutputSpecified = outputFormat != null && !outputFormat.isEmpty();

        String subResourceVirtualPath = "";

        // Private share file url type = localhost/shared-files/uuid/{iteration}/filename
        // Non obfuscated file url type = localhost/files/{workspace}/{entityType}/{entityId}/{version}/{iteration}/filename

        boolean isPrivateSharedFile = URLDecoder.decode(pathInfo[offset - 1], "UTF-8").equals("shared-files");

        if (!isPrivateSharedFile) {

            try {

                String workspaceId = URLDecoder.decode(pathInfo[offset], "UTF-8");
                String elementType = URLDecoder.decode(pathInfo[offset + 1], "UTF-8");

                if (elementType.equals("documents")) {

                    needsCacheHeaders = true;

                    String docMId = URLDecoder.decode(pathInfo[offset + 2], "UTF-8");
                    String docMVersion = pathInfo[offset + 3];
                    int iteration = Integer.parseInt(pathInfo[offset + 4]);
                    String fileName = URLDecoder.decode(pathInfo[offset + 5], "UTF-8");

                    fullName = workspaceId + "/" + elementType + "/" + docMId + "/" + docMVersion + "/" + iteration + "/" + fileName;

                    if(account != null){
                        binaryResource = documentService.getBinaryResource(fullName);
                        docI = documentService.findDocumentIterationByBinaryResource(binaryResource);
                        user = documentService.whoAmI(workspaceId);
                    }else{
                        DocumentMasterKey docMK = new DocumentMasterKey(workspaceId, docMId, docMVersion);
                        binaryResource = guestProxy.getPublicBinaryResourceForDocument(docMK,fullName);
                        docI = guestProxy.findDocumentIterationByBinaryResource(binaryResource);
                        user = guestProxy.whoAmI();
                    }

                    if (pathInfo.length > offset + 6) {
                        String[] pathInfosExtra = Arrays.copyOfRange(pathInfo, offset + 6, pathInfo.length);
                        isSubResource = true;
                        subResourceVirtualPath = documentResourceGetterService.getSubResourceVirtualPath(binaryResource, StringUtils.join(pathInfosExtra, '/'));
                    }


                } else if (elementType.equals("parts")) {

                    needsCacheHeaders = true;

                    String partNumber = URLDecoder.decode(pathInfo[offset + 2], "UTF-8");
                    String version = pathInfo[offset + 3];
                    int iteration = Integer.parseInt(pathInfo[offset + 4]);

                    String fileName;
                    if (pathInfo.length == offset + 7) {
                        fileName = URLDecoder.decode(pathInfo[offset + 6], "UTF-8");
                        String subType = URLDecoder.decode(pathInfo[offset + 5], "UTF-8"); //subType may be nativecad
                        fullName = workspaceId + "/" + elementType + "/" + partNumber + "/" + version + "/" + iteration + "/" + subType + "/" + fileName;
                    } else {
                        fileName = URLDecoder.decode(pathInfo[offset + 5], "UTF-8");
                        fullName = workspaceId + "/" + elementType + "/" + partNumber + "/" + version + "/" + iteration + "/" + fileName;
                    }
                    if(account != null){
                        binaryResource = productService.getBinaryResource(fullName);
                    }else{
                        PartRevisionKey partK = new PartRevisionKey(workspaceId, partNumber, version);
                        binaryResource = guestProxy.getPublicBinaryResourceForPart(partK,fullName);
                    }
                }

                // We don't serve these files to guests, no changes.
                else if (elementType.equals("document-templates")) {
                    String templateID = URLDecoder.decode(pathInfo[offset + 2], "UTF-8");
                    String fileName = URLDecoder.decode(pathInfo[offset + 3], "UTF-8");
                    fullName = workspaceId + "/" + elementType + "/" + templateID + "/" + fileName;
                    binaryResource = documentService.getBinaryResource(fullName);
                } else if (elementType.equals("part-templates")) {
                    String templateID = URLDecoder.decode(pathInfo[offset + 2], "UTF-8");
                    String fileName = URLDecoder.decode(pathInfo[offset + 3], "UTF-8");
                    fullName = workspaceId + "/" + elementType + "/" + templateID + "/" + fileName;
                    binaryResource = documentService.getBinaryResource(fullName);
                }


                // Store necessary vars in request for uploadDownloadServlet doGet method
                pRequest.setAttribute("binaryResource",binaryResource);
                pRequest.setAttribute("isSubResource",isSubResource);
                pRequest.setAttribute("isDocumentAndOutputSpecified",isDocumentAndOutputSpecified);
                pRequest.setAttribute("fullName",fullName);
                pRequest.setAttribute("outputFormat",outputFormat);
                pRequest.setAttribute("subResourceVirtualPath",subResourceVirtualPath);
                pRequest.setAttribute("needsCacheHeaders",needsCacheHeaders);
                pRequest.setAttribute("user",user);
                pRequest.setAttribute("docI",docI);

                chain.doFilter(pRequest,pResponse);

            } catch (LoginException pEx) {
                httpRequest.getRequestDispatcher("/faces/login.xhtml?originURL=" + URLEncoder.encode(originURL, "UTF-8")).forward(pRequest, pResponse);
                return;
            } catch (Exception pEx){
                throw new ServletException("Error while downloading the file.", pEx);
            }

        } else { // private shared file



        }


    }

    @Override
    public void destroy() {
    }

    @Override
    public void init(FilterConfig filterConfig) {
    }
}
