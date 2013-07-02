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
import com.docdoku.core.document.DocumentMaster;
import com.docdoku.core.document.DocumentMasterKey;
import com.docdoku.core.product.PartRevision;
import com.docdoku.core.product.PartRevisionKey;
import com.docdoku.core.services.IDocumentManagerLocal;
import com.docdoku.core.services.IProductManagerLocal;

import javax.ejb.EJB;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.regex.Pattern;

public class PermalinkFilter implements Filter {

    @EJB
    private IProductManagerLocal productService;

    @EJB
    private IDocumentManagerLocal documentService;

    @EJB
    private GuestProxy guestProxy;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String requestURI = httpRequest.getRequestURI();
        String[] pathInfos = Pattern.compile("/").split(requestURI);
        int offset = httpRequest.getContextPath().equals("") ? 2 : 3;

        HttpSession sessionHTTP = httpRequest.getSession();
        Account account = (Account) sessionHTTP.getAttribute("account");

        if(account == null){

            String entityType = URLDecoder.decode(pathInfos[offset-1], "UTF-8");
            boolean redirect = false;

            if(entityType != null){
                switch (entityType){
                    case "documents" :
                        try {
                            String workspaceId = URLDecoder.decode(pathInfos[offset], "UTF-8");
                            String documentMasterId = URLDecoder.decode(pathInfos[offset+1],"UTF-8");
                            String documentMasterVersion = pathInfos[offset+2];

                            DocumentMasterKey docMK  =  new DocumentMasterKey(workspaceId,documentMasterId,documentMasterVersion);
                            DocumentMaster publicDocumentMaster = guestProxy.getPublicDocumentMaster(docMK);
                            request.setAttribute("publicDocumentMaster",publicDocumentMaster);
                        } catch (Exception e) {
                            redirect = true;
                        }

                        break;
                    case "parts" :
                        try {
                            String workspaceId = URLDecoder.decode(pathInfos[offset], "UTF-8");
                            String partNumber = URLDecoder.decode(pathInfos[offset+1],"UTF-8");
                            String partVersion = pathInfos[offset+2];

                            PartRevisionKey partK  = new PartRevisionKey(workspaceId,partNumber,partVersion);
                            PartRevision publicPartRevision = guestProxy.getPublicPartRevision(partK);
                            request.setAttribute("publicPartRevision",publicPartRevision);
                        } catch (Exception e) {
                            redirect = true;
                        }
                        break;
                    default : redirect = true; break ;
                }
            }

            if(redirect){
                String qs=httpRequest.getQueryString();
                String originURL = httpRequest.getRequestURI() + (qs==null?"": "?" + qs);
                httpRequest.getRequestDispatcher("/faces/login.xhtml?originURL=" + URLEncoder.encode(originURL, "UTF-8")).forward(request, response);
            }else{
                chain.doFilter(request, response);
            }
        }else{
            chain.doFilter(request, response);
        }

    }

    @Override
    public void destroy() {
    }

    @Override
    public void init(FilterConfig filterConfig) {
    }
}
