/*
 * DocDoku, Professional Open Source
 * Copyright 2006, 2007, 2008, 2009, 2010, 2011 DocDoku SARL
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
package com.docdoku.server.dao;

import com.docdoku.core.services.FileNotFoundException;
import com.docdoku.core.services.FileAlreadyExistsException;
import com.docdoku.core.services.CreationException;
import com.docdoku.core.*;
import com.docdoku.core.common.BinaryResource;
import com.docdoku.core.document.Document;
import com.docdoku.core.document.MasterDocumentTemplate;
import java.util.*;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;
import javax.persistence.Query;

public class BinaryResourceDAO {

    private EntityManager em;
    private Locale mLocale;

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
            throw new FileAlreadyExistsException(mLocale, pBinaryResource);
        } catch (PersistenceException pPEx) {
            //EntityExistsException is case sensitive
            //whereas MySQL is not thus PersistenceException could be
            //thrown instead of EntityExistsException
            throw new CreationException(mLocale);
        }
    }

    public void removeBinaryResource(String pFullName) throws FileNotFoundException {
        BinaryResource file = loadBinaryResource(pFullName);
        em.remove(file);
    }

    public void removeBinaryResource(BinaryResource pBinaryResource) {
        em.remove(pBinaryResource);
    }

    public BinaryResource loadBinaryResource(String pFullName) throws FileNotFoundException {
        BinaryResource file = em.find(BinaryResource.class, pFullName);
        if (file == null) {
            throw new FileNotFoundException(mLocale, pFullName);
        } else {
            return file;
        }
    }

    public Document getDocumentOwner(BinaryResource pBinaryResource) {
        Query query = em.createQuery("SELECT d FROM Document d WHERE :binaryResource MEMBER OF d.attachedFiles");
        try {
            return (Document) query.setParameter("binaryResource", pBinaryResource).getSingleResult();
        } catch (NoResultException pNREx) {
            return null;
        }
    }

    public MasterDocumentTemplate getTemplateOwner(BinaryResource pBinaryResource) {
        Query query = em.createQuery("SELECT t FROM MasterDocumentTemplate t WHERE :binaryResource MEMBER OF t.attachedFiles");
        try {
            return (MasterDocumentTemplate) query.setParameter("binaryResource", pBinaryResource).getSingleResult();
        } catch (NoResultException pNREx) {
            return null;
        }
    }
}
