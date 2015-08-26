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
package com.docdoku.server.dao;

import com.docdoku.core.common.BinaryResource;
import com.docdoku.core.configuration.PathDataIteration;
import com.docdoku.core.configuration.ProductInstanceIteration;
import com.docdoku.core.document.DocumentIteration;
import com.docdoku.core.document.DocumentMasterTemplate;
import com.docdoku.core.exceptions.CreationException;
import com.docdoku.core.exceptions.FileAlreadyExistsException;
import com.docdoku.core.exceptions.FileNotFoundException;
import com.docdoku.core.product.Geometry;
import com.docdoku.core.product.PartIteration;
import com.docdoku.core.product.PartMasterTemplate;

import javax.persistence.*;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BinaryResourceDAO {
    private static final Logger LOGGER = Logger.getLogger(BinaryResourceDAO.class.getName());

    private final EntityManager em;
    private final Locale mLocale;

    public BinaryResourceDAO(Locale pLocale, EntityManager pEM) {
        em = pEM;
        mLocale = pLocale;
    }

    public BinaryResourceDAO(EntityManager pEM) {
        em = pEM;
        mLocale = Locale.getDefault();
    }

    public void createBinaryResource(BinaryResource pBinaryResource) throws FileAlreadyExistsException, CreationException {
        try {
            //the EntityExistsException is thrown only when flush occurs    
            em.persist(pBinaryResource);
            em.flush();
        } catch (EntityExistsException pEEEx) {
            LOGGER.log(Level.FINER,null,pEEEx);
            throw new FileAlreadyExistsException(mLocale, pBinaryResource);
        } catch (PersistenceException pPEx) {
            //EntityExistsException is case sensitive
            //whereas MySQL is not thus PersistenceException could be
            //thrown instead of EntityExistsException
            LOGGER.log(Level.FINER,null,pPEx);
            throw new CreationException(mLocale);
        }
    }

    public void removeBinaryResource(String pFullName) throws FileNotFoundException {
        BinaryResource file = loadBinaryResource(pFullName);
        em.remove(file);
    }

    public void removeBinaryResource(BinaryResource pBinaryResource) {
        em.remove(pBinaryResource);
        em.flush();
    }

    public BinaryResource loadBinaryResource(String pFullName) throws FileNotFoundException {
        BinaryResource file = em.find(BinaryResource.class, pFullName);
        if(null == file){
            throw new FileNotFoundException(mLocale,pFullName);
        }
        return file;

    }

    public PartIteration getPartOwner(BinaryResource pBinaryResource) {
        TypedQuery<PartIteration> query;
        if(pBinaryResource instanceof Geometry){
            query = em.createQuery("SELECT p FROM PartIteration p WHERE :binaryResource MEMBER OF p.geometries", PartIteration.class);
        }else if(pBinaryResource.isNativeCADFile()){
            query = em.createQuery("SELECT p FROM PartIteration p WHERE p.nativeCADFile = :binaryResource", PartIteration.class);
        }else{
            query = em.createQuery("SELECT p FROM PartIteration p WHERE :binaryResource MEMBER OF p.attachedFiles", PartIteration.class);
        }
        try {
            return query.setParameter("binaryResource", pBinaryResource).getSingleResult();
        } catch (NoResultException pNREx) {
            LOGGER.log(Level.FINER,null,pNREx);
            return null;
        }
    }
    
    public DocumentIteration getDocumentOwner(BinaryResource pBinaryResource) {
        TypedQuery<DocumentIteration> query = em.createQuery("SELECT d FROM DocumentIteration d WHERE :binaryResource MEMBER OF d.attachedFiles", DocumentIteration.class);
        try {
            return query.setParameter("binaryResource", pBinaryResource).getSingleResult();
        } catch (NoResultException pNREx) {
            LOGGER.log(Level.FINER,null,pNREx);
            return null;
        }
    }
    public ProductInstanceIteration getProductInstanceIterationOwner(BinaryResource pBinaryResource) {
        TypedQuery<ProductInstanceIteration> query = em.createQuery("SELECT d FROM ProductInstanceIteration d WHERE :binaryResource MEMBER OF d.attachedFiles", ProductInstanceIteration.class);
        try {
            return query.setParameter("binaryResource", pBinaryResource).getSingleResult();
        } catch (NoResultException pNREx) {
            LOGGER.log(Level.FINER,null,pNREx);
            return null;
        }
    }

    public PathDataIteration getPathDataOwner(BinaryResource pBinaryResource) {
        TypedQuery<PathDataIteration> query = em.createQuery("SELECT p FROM PathDataIteration p WHERE :binaryResource MEMBER OF p.attachedFiles", PathDataIteration.class);
        try {
            return query.setParameter("binaryResource", pBinaryResource).getSingleResult();
        } catch (NoResultException pNREx) {
            LOGGER.log(Level.FINER,null,pNREx);
            return null;
        }
    }


    public DocumentMasterTemplate getDocumentTemplateOwner(BinaryResource pBinaryResource) {
        TypedQuery<DocumentMasterTemplate> query = em.createQuery("SELECT t FROM DocumentMasterTemplate t WHERE :binaryResource MEMBER OF t.attachedFiles", DocumentMasterTemplate.class);
        try {
            return query.setParameter("binaryResource", pBinaryResource).getSingleResult();
        } catch (NoResultException pNREx) {
            LOGGER.log(Level.FINER,null,pNREx);
            return null;
        }
    }

    public PartMasterTemplate getPartTemplateOwner(BinaryResource pBinaryResource) {
        TypedQuery<PartMasterTemplate> query = em.createQuery("SELECT t FROM PartMasterTemplate t WHERE t.attachedFile = :binaryResource", PartMasterTemplate.class);
        try {
            return query.setParameter("binaryResource", pBinaryResource).getSingleResult();
        } catch (NoResultException pNREx) {
            LOGGER.log(Level.FINER,null,pNREx);
            return null;
        }
    }

    public BinaryResource findNativeCadBinaryResourceInWorkspace(String workspaceId, String cadFileName) {
        TypedQuery<BinaryResource> query = em.createQuery("SELECT br FROM BinaryResource br WHERE br.fullName like :name", BinaryResource.class);
        try {
            return query.setParameter("name", workspaceId + "/parts/%/nativecad/" + cadFileName).getSingleResult();
        } catch (NoResultException pNREx) {
            LOGGER.log(Level.FINER,null,pNREx);
            return null;
        }
    }


}
