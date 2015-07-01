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

import com.docdoku.core.document.DocumentMaster;
import com.docdoku.core.document.DocumentRevision;
import com.docdoku.core.exceptions.CreationException;
import com.docdoku.core.exceptions.DocumentMasterAlreadyExistsException;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DocumentMasterDAO {
    private static final Logger LOGGER = Logger.getLogger(DocumentMasterDAO.class.getName());

    private EntityManager em;
    private Locale mLocale;

    public DocumentMasterDAO(Locale pLocale, EntityManager pEM) {
        em = pEM;
        mLocale = pLocale;
    }

    public DocumentMasterDAO(EntityManager pEM) {
        em = pEM;
        mLocale = Locale.getDefault();
    }

    public void createDocM(DocumentMaster pDocumentMaster) throws DocumentMasterAlreadyExistsException, CreationException {
        try {
            //the EntityExistsException is thrown only when flush occurs
            em.persist(pDocumentMaster);
            em.flush();
        } catch (EntityExistsException pEEEx) {
            LOGGER.log(Level.FINER,null,pEEEx);
            throw new DocumentMasterAlreadyExistsException(mLocale, pDocumentMaster);
        } catch (PersistenceException pPEx) {
            //EntityExistsException is case sensitive
            //whereas MySQL is not thus PersistenceException could be
            //thrown instead of EntityExistsException
            LOGGER.log(Level.FINER,null,pPEx);
            throw new CreationException(mLocale);
        }
    }

    public void removeDocM(DocumentMaster pDocM) {
        DocumentRevisionDAO documentRevisionDAO = new DocumentRevisionDAO(mLocale, em);
        List<DocumentRevision> docRs = new ArrayList<>(pDocM.getDocumentRevisions());
        for(DocumentRevision documentRevision:docRs){
            documentRevisionDAO.removeRevision(documentRevision);
        }
        em.remove(pDocM);
    }


    public List<DocumentMaster> getAllByWorkspace(String workspaceId) {
        return em.createNamedQuery("DocumentMaster.findByWorkspace",DocumentMaster.class)
                                                 .setParameter("workspaceId",workspaceId)
                                                 .getResultList();
    }
}
