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

package com.docdoku.server.http;

import com.docdoku.core.common.BinaryResource;
import com.docdoku.core.document.DocumentIteration;
import com.docdoku.core.document.DocumentRevision;
import com.docdoku.core.exceptions.NotAllowedException;
import com.docdoku.core.exceptions.SharedEntityNotFoundException;
import com.docdoku.core.product.Geometry;
import com.docdoku.core.product.PartIteration;
import com.docdoku.core.product.PartRevision;
import com.docdoku.core.services.IShareManagerLocal;
import com.docdoku.core.sharing.SharedDocument;
import com.docdoku.core.sharing.SharedEntity;
import com.docdoku.core.sharing.SharedPart;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLDecoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * @author Morgan Guimard
 */

public class PrivateShareServlet extends HttpServlet {

    @EJB
    private IShareManagerLocal shareService;

    private static final Logger LOGGER = Logger.getLogger(PrivateShareServlet.class.getName());

    @Override
    protected void doPost(HttpServletRequest pRequest, HttpServletResponse pResponse) throws ServletException, IOException {

        String[] pathInfos = Pattern.compile("/").split(pRequest.getRequestURI());
        int offset = pRequest.getContextPath().isEmpty() ? 2 : 3;
        String uuid = URLDecoder.decode(pathInfos[offset], "UTF-8");

        try{

            SharedEntity sharedEntity = shareService.findSharedEntityForGivenUUID(uuid);

            // check if expire
            if(sharedEntity.getExpireDate() != null && sharedEntity.getExpireDate().getTime() < new Date().getTime()){
                shareService.deleteSharedEntityIfExpired(sharedEntity);
                pRequest.getRequestDispatcher(pRequest.getContextPath()+"/faces/sharedEntityExpired.xhtml").forward(pRequest, pResponse);
                return;
            }

            // check shared entity password and provided password

            if(sharedEntity.getPassword() != null){
                String providedPassword = pRequest.getParameter("password");
                if(providedPassword != null && md5sum(providedPassword).equals(sharedEntity.getPassword())){
                    handleOnCheckSuccess(pRequest,pResponse,sharedEntity);
                }else{
                    pRequest.getRequestDispatcher(pRequest.getContextPath()+"/faces/sharedEntityPassword.xhtml").forward(pRequest, pResponse);
                }
            }else{
                handleOnCheckSuccess(pRequest, pResponse, sharedEntity);
            }

        } catch (Exception pEx) {
            LOGGER.log(Level.SEVERE, null, pEx);
            throw new ServletException("error while fetching your data.", pEx);
        }

    }

    @Override
    protected void doGet(HttpServletRequest pRequest, HttpServletResponse pResponse) throws ServletException, IOException {

        try {

            String[] pathInfos = Pattern.compile("/").split(pRequest.getRequestURI());
            int offset = pRequest.getContextPath().isEmpty() ? 2 : 3;
            String uuid = URLDecoder.decode(pathInfos[offset], "UTF-8");

            try{

                SharedEntity sharedEntity = shareService.findSharedEntityForGivenUUID(uuid);

                // check if expire
                if(sharedEntity.getExpireDate() != null && sharedEntity.getExpireDate().getTime() < new Date().getTime()){
                    shareService.deleteSharedEntityIfExpired(sharedEntity);
                    pRequest.getRequestDispatcher(pRequest.getContextPath()+"/faces/sharedEntityExpired.xhtml").forward(pRequest, pResponse);
                    return;
                }

                // check if password protected -> should come from the doPost
                if(sharedEntity.getPassword() != null){
                    pRequest.getRequestDispatcher(pRequest.getContextPath()+"/faces/sharedEntityPassword.xhtml").forward(pRequest, pResponse);
                }else{
                    // Tests for doGet are ok
                    handleOnCheckSuccess(pRequest,pResponse,sharedEntity);
                }


            }catch(SharedEntityNotFoundException ex){
                pRequest.removeAttribute("sharedEntity");
            }

        } catch (Exception pEx) {
            LOGGER.log(Level.SEVERE, null, pEx);
            throw new ServletException("error while processing the request.", pEx);
        }
    }

    private void handleOnCheckSuccess(HttpServletRequest pRequest, HttpServletResponse pResponse, SharedEntity sharedEntity) throws ServletException, IOException, NotAllowedException {

        pRequest.setAttribute("sharedEntity",sharedEntity);

        if(sharedEntity instanceof SharedDocument){

            DocumentRevision documentRevision = ((SharedDocument) sharedEntity).getDocumentRevision();
            DocumentIteration documentIteration =  documentRevision.getLastIteration();

            if(documentIteration == null){
                throw new NotAllowedException(Locale.getDefault(), "NotAllowedException27");
            }

            pRequest.setAttribute("documentRevision", documentRevision);
            pRequest.setAttribute("attr", new ArrayList<>(documentIteration.getInstanceAttributes()));
            pRequest.getRequestDispatcher(pRequest.getContextPath()+"/faces/documentPermalink.xhtml").forward(pRequest, pResponse);

        }else if(sharedEntity instanceof SharedPart){

            PartRevision partRevision = ((SharedPart) sharedEntity).getPartRevision();
            PartIteration partIteration =  partRevision.getLastIteration();

            if(partIteration == null){
                throw new NotAllowedException(Locale.getDefault(), "NotAllowedException41");
            }

            String nativeCadFileURI ="";
            String uuid = sharedEntity.getUuid();

            if(partIteration.getNativeCADFile() != null){
                BinaryResource binaryResource = partIteration.getNativeCADFile();
                nativeCadFileURI = "/api/files/" + binaryResource.getFullName() + "/uuid/" + uuid;
            }

            String geometryFileURI = "";
            if(!partRevision.getLastIteration().getGeometries().isEmpty()){
                Geometry geometry = partRevision.getLastIteration().getSortedGeometries().get(0);
                geometryFileURI ="/api/files/" + geometry.getFullName() + "/uuid/" + uuid;
            }

            pRequest.setAttribute("partRevision", partRevision);
            pRequest.setAttribute("attr", new ArrayList<>(partIteration.getInstanceAttributes()));
            pRequest.setAttribute("nativeCadFileURI",nativeCadFileURI);
            pRequest.setAttribute("geometryFileURI",geometryFileURI);
            pRequest.getRequestDispatcher(pRequest.getContextPath()+"/faces/partPermalink.xhtml").forward(pRequest, pResponse);
        }

    }


    private String md5sum(String pText) throws NoSuchAlgorithmException {

        byte[] digest = MessageDigest.getInstance("MD5").digest(pText.getBytes());
        StringBuffer hexString = new StringBuffer();
        for (byte aDigest : digest) {
            String hex = Integer.toHexString(0xFF & aDigest);
            if (hex.length() == 1) {
                hexString.append("0" + hex);
            } else {
                hexString.append(hex);
            }
        }
        return hexString.toString();

    }


}
