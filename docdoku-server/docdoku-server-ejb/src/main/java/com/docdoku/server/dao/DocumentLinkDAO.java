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

import com.docdoku.core.document.DocumentIteration;
import com.docdoku.core.document.DocumentLink;
import com.docdoku.core.product.PartIteration;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import java.util.List;
import java.util.Locale;


public class DocumentLinkDAO {

    private EntityManager em;
    private Locale mLocale;

    public DocumentLinkDAO(Locale pLocale, EntityManager pEM) {
        em = pEM;
        mLocale = pLocale;
    }

    public DocumentLinkDAO(EntityManager pEM) {
        em = pEM;
        mLocale = Locale.getDefault();
    }

    public void removeLink(DocumentLink pLink){
        em.remove(pLink);
    }

    public void createLink(DocumentLink pLink){
        try{
            //the EntityExistsException is thrown only when flush occurs
            em.persist(pLink);
            em.flush();
        }catch(EntityExistsException pEEEx){
            //already created
        }
    }

    public List<DocumentIteration> getInverseDocumentsLinks(DocumentIteration documentIteration){
        return em.createNamedQuery("DocumentLink.findInverseDocumentLinks",DocumentIteration.class)
                .setParameter("documentIteration",documentIteration)
                .getResultList();
    }

    public List<PartIteration> getInversePartsLinks(DocumentIteration documentIteration){
        return em.createNamedQuery("DocumentLink.findInversePartLinks",PartIteration.class)
                .setParameter("documentIteration",documentIteration)
                .getResultList();
    }
}