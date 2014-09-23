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
import com.docdoku.core.document.DocumentRevision;
import com.docdoku.core.document.DocumentRevisionKey;
import com.docdoku.core.exceptions.AccountNotFoundException;
import com.docdoku.core.product.PartRevision;
import com.docdoku.core.product.PartRevisionKey;
import com.docdoku.core.services.*;
import com.docdoku.core.sharing.SharedDocument;
import com.docdoku.core.sharing.SharedEntity;
import com.docdoku.core.sharing.SharedPart;
import org.apache.commons.lang.StringUtils;

import javax.ejb.EJB;
import javax.security.auth.login.LoginException;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FilesFilter implements Filter {
    private static final Logger LOGGER = Logger.getLogger(FilesFilter.class.getName());

    @EJB
    private IProductManagerLocal productService;

    @EJB
    private IDocumentManagerLocal documentService;

    @EJB
    private IUserManagerLocal userManager;

    @EJB
    private IDocumentResourceGetterManagerLocal documentResourceGetterService;

    @EJB
    private IShareManagerLocal shareService;

    @EJB
    private GuestProxy guestProxy;

    @Override
    public void doFilter(ServletRequest pRequest, ServletResponse pResponse,
                         FilterChain chain) throws IOException, ServletException {


        HttpServletRequest httpRequest = (HttpServletRequest) pRequest;
        String remoteUser = httpRequest.getRemoteUser();

        String qs = httpRequest.getQueryString();
        String originURL = httpRequest.getRequestURI() + (qs == null ? "" : "?" + qs);

        HttpSession sessionHTTP = httpRequest.getSession();


        // Check headers for Authorization
        if(remoteUser == null){

            String authorization = httpRequest.getHeader("Authorization");

            if(authorization != null && !authorization.isEmpty()){
                String[] splitAuthorization = authorization.split(" ");
                if(splitAuthorization.length == 2){
                    byte[] decoded = DatatypeConverter.parseBase64Binary(splitAuthorization[1]);
                    String credentials = new String(decoded, "US-ASCII");
                    String[] splitCredentials = credentials.split(":");
                    String userLogin = splitCredentials[0];
                    String userPassword = splitCredentials[1];

                    //Logout in case of user is already logged in,
                    //that could happen when using multiple tabs
                    httpRequest.logout();
                    httpRequest.login(userLogin, userPassword);

                    try {
                        Account account = userManager.getAccount(userLogin);
                        if(account!=null) {
                            remoteUser = account.getLogin();
                        }
                    }catch(AccountNotFoundException e){
                        LOGGER.log(Level.FINEST,null,e);
                    }
                    //case insensitive fix
                    if(!userLogin.equals(remoteUser)){
                        httpRequest.logout();
                        httpRequest.getRequestDispatcher(httpRequest.getContextPath()+"/faces/login.xhtml?originURL=" + URLEncoder.encode(originURL, "UTF-8")).forward(pRequest, pResponse);
                        return;
                    }
                    sessionHTTP.setAttribute("remoteUser",userLogin);
                }
            }

        }

        // don't filter post requests, security will be handled by doPost in uploadDownloadServlet
        if(httpRequest.getMethod().equalsIgnoreCase("POST")){
            chain.doFilter(pRequest,pResponse);
            return;
        }

        int offset = "".equals(httpRequest.getContextPath()) ? 1 : 2;

        String requestURI = httpRequest.getRequestURI();
        //remove empty entries because of Three.js that generates url with double /
        String[] pathInfo = FilesFilter.removeEmptyEntries(requestURI.split("/"));

        String fullName = "";
        String workspaceId;
        String elementType;
        String fileName;
        int iteration;
        BinaryResource binaryResource = null;
        DocumentIteration docI = null;
        User user = null;
        boolean isSubResource = false;
        boolean needsCacheHeaders = false;
        String outputFormat = pRequest.getParameter("output");
        boolean isDocumentAndOutputSpecified = outputFormat != null && !outputFormat.isEmpty();
        String subResourceVirtualPath = "";

        boolean isPrivateSharedFile = URLDecoder.decode(pathInfo[offset - 1], "UTF-8").equals("shared-files");

        if (!isPrivateSharedFile) {

            try {

                workspaceId = URLDecoder.decode(pathInfo[offset], "UTF-8");
                elementType = URLDecoder.decode(pathInfo[offset + 1], "UTF-8");

                switch (elementType) {
                    case "documents":

                        needsCacheHeaders = true;

                        String docMId = URLDecoder.decode(pathInfo[offset + 2], "UTF-8");
                        String docMVersion = pathInfo[offset + 3];
                        iteration = Integer.parseInt(pathInfo[offset + 4]);
                        fileName = URLDecoder.decode(pathInfo[offset + 5], "UTF-8");

                        fullName = workspaceId + "/" + elementType + "/" + docMId + "/" + docMVersion + "/" + iteration + "/" + fileName;

                        if (remoteUser != null) {
                            binaryResource = documentService.getBinaryResource(fullName);
                            docI = documentService.findDocumentIterationByBinaryResource(binaryResource);
                            user = documentService.whoAmI(workspaceId);
                        } else {
                            DocumentRevisionKey docRK = new DocumentRevisionKey(workspaceId, docMId, docMVersion);
                            binaryResource = guestProxy.getPublicBinaryResourceForDocument(docRK, fullName);
                            docI = guestProxy.findDocumentIterationByBinaryResource(binaryResource);
                            user = guestProxy.whoAmI();
                        }

                        if (pathInfo.length > offset + 6) {
                            String[] pathInfosExtra = Arrays.copyOfRange(pathInfo, offset + 6, pathInfo.length);
                            isSubResource = true;
                            subResourceVirtualPath = documentResourceGetterService.getSubResourceVirtualPath(binaryResource, StringUtils.join(pathInfosExtra, '/'));
                        }


                        break;
                    case "parts":

                        needsCacheHeaders = true;

                        String partNumber = URLDecoder.decode(pathInfo[offset + 2], "UTF-8");
                        String version = pathInfo[offset + 3];
                        iteration = Integer.parseInt(pathInfo[offset + 4]);

                        if (pathInfo.length == offset + 7) {
                            fileName = URLDecoder.decode(pathInfo[offset + 6], "UTF-8");
                            String subType = URLDecoder.decode(pathInfo[offset + 5], "UTF-8"); //subType may be nativecad
                            fullName = workspaceId + "/" + elementType + "/" + partNumber + "/" + version + "/" + iteration + "/" + subType + "/" + fileName;
                        } else {
                            fileName = URLDecoder.decode(pathInfo[offset + 5], "UTF-8");
                            fullName = workspaceId + "/" + elementType + "/" + partNumber + "/" + version + "/" + iteration + "/" + fileName;
                        }
                        if (remoteUser != null) {
                            binaryResource = productService.getBinaryResource(fullName);
                        } else {
                            PartRevisionKey partK = new PartRevisionKey(workspaceId, partNumber, version);
                            binaryResource = guestProxy.getPublicBinaryResourceForPart(partK, fullName);
                        }
                        break;

                    // We don't serve these files to guests, no changes.
                    case "document-templates": {
                        String templateID = URLDecoder.decode(pathInfo[offset + 2], "UTF-8");
                        fileName = URLDecoder.decode(pathInfo[offset + 3], "UTF-8");
                        fullName = workspaceId + "/" + elementType + "/" + templateID + "/" + fileName;
                        binaryResource = documentService.getBinaryResource(fullName);
                        break;
                    }
                    case "part-templates": {
                        String templateID = URLDecoder.decode(pathInfo[offset + 2], "UTF-8");
                        fileName = URLDecoder.decode(pathInfo[offset + 3], "UTF-8");
                        fullName = workspaceId + "/" + elementType + "/" + templateID + "/" + fileName;
                        binaryResource = documentService.getBinaryResource(fullName);
                        break;
                    }
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
                httpRequest.getRequestDispatcher(httpRequest.getContextPath()+"/faces/login.xhtml?originURL=" + URLEncoder.encode(originURL, "UTF-8")).forward(pRequest, pResponse);
            } catch (Exception pEx){
                LOGGER.log(Level.FINEST,null,pEx);
                throw new ServletException("Error while downloading the file.", pEx);
            }

        } else {  // private shared file

            try {

                String uuid = URLDecoder.decode(pathInfo[offset], "UTF-8");
                iteration = Integer.valueOf(URLDecoder.decode(pathInfo[offset + 1], "UTF-8"));

                SharedEntity sharedEntity = shareService.findSharedEntityForGivenUUID(uuid);
                workspaceId = sharedEntity.getWorkspace().getId();

                if(sharedEntity.getExpireDate() != null && sharedEntity.getExpireDate().getTime() < new Date().getTime()){
                    shareService.deleteSharedEntityIfExpired(sharedEntity);
                    pRequest.getRequestDispatcher(httpRequest.getContextPath()+"/faces/sharedEntityExpired.xhtml").forward(pRequest, pResponse);
                    return;
                }

                if(sharedEntity instanceof SharedDocument){
                    DocumentRevision documentRevision = ((SharedDocument) sharedEntity).getDocumentRevision();
                    String id = documentRevision.getId();
                    String version = documentRevision.getVersion();

                    fileName = URLDecoder.decode(pathInfo[offset + 2], "UTF-8");
                    fullName = workspaceId + "/" + "documents" + "/" + id + "/" + version + "/" + iteration + "/" + fileName;

                    if(remoteUser != null){
                        binaryResource = documentService.getBinaryResource(fullName);
                        docI = documentService.findDocumentIterationByBinaryResource(binaryResource);
                        user = documentService.whoAmI(workspaceId);
                    }else{
                        binaryResource = guestProxy.getBinaryResourceForSharedDocument(fullName);
                        docI = guestProxy.findDocumentIterationByBinaryResource(binaryResource);
                        user = guestProxy.whoAmI();
                    }

                    if (pathInfo.length > offset + 3) {
                        String[] pathInfosExtra = Arrays.copyOfRange(pathInfo, offset + 3, pathInfo.length);
                        isSubResource = true;
                        subResourceVirtualPath = documentResourceGetterService.getSubResourceVirtualPath(binaryResource, StringUtils.join(pathInfosExtra, '/'));
                    }

                }else if(sharedEntity instanceof SharedPart){
                    PartRevision partRevision = ((SharedPart) sharedEntity).getPartRevision();

                    String partNumber = partRevision.getPartNumber();
                    String version =partRevision.getVersion();

                    if (pathInfo.length == offset + 4) {
                        fileName = URLDecoder.decode(pathInfo[offset + 3], "UTF-8");
                        String subType = URLDecoder.decode(pathInfo[offset + 2], "UTF-8"); //subType may be nativecad
                        fullName = workspaceId + "/" + "parts" + "/" + partNumber + "/" + version + "/" + iteration + "/" + subType + "/" + fileName;
                    } else {
                        fileName = URLDecoder.decode(pathInfo[offset + 2], "UTF-8");
                        fullName = workspaceId + "/" + "parts" + "/" + partNumber + "/" + version + "/" + iteration + "/" + fileName;
                    }
                    if(remoteUser != null){
                        binaryResource = productService.getBinaryResource(fullName);
                    }else{
                        binaryResource = guestProxy.getBinaryResourceForSharedPart(fullName);
                    }
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

            } catch (Exception e) {
                httpRequest.getRequestDispatcher(httpRequest.getContextPath()+"/faces/login.xhtml?originURL=" + URLEncoder.encode(originURL, "UTF-8")).forward(pRequest, pResponse);
            }

        }

    }

    private static String[] removeEmptyEntries(String[] entries) {
        List<String> elements = new LinkedList<>(Arrays.asList(entries));

        for (Iterator<String> it = elements.iterator(); it.hasNext(); ) {
            if (it.next().isEmpty()) {
                it.remove();
            }
        }
        return elements.toArray(new String[elements.size()]);
    }

    @Override
    public void destroy() {
    }

    @Override
    public void init(FilterConfig filterConfig) {
    }
}
