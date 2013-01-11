/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2013 DocDoku SARL
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

import com.docdoku.core.services.CreationException;
import com.docdoku.core.services.DocumentMasterTemplateNotFoundException;
import com.docdoku.core.document.DocumentMasterTemplate;
import com.docdoku.core.document.DocumentMasterTemplateKey;
import com.docdoku.core.services.DocumentMasterTemplateAlreadyExistsException;

import java.util.List;
import java.util.Locale;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import javax.persistence.Query;

public class DocumentMasterTemplateDAO {

    private EntityManager em;
    private Locale mLocale;

    public DocumentMasterTemplateDAO(Locale pLocale, EntityManager pEM) {
        em = pEM;
        mLocale = pLocale;
    }

    public DocumentMasterTemplateDAO(EntityManager pEM) {
        em = pEM;
        mLocale = Locale.getDefault();
    }

    public void updateDocMTemplate(DocumentMasterTemplate pTemplate) {
        em.merge(pTemplate);
    }

    public DocumentMasterTemplate removeDocMTemplate(DocumentMasterTemplateKey pKey) throws DocumentMasterTemplateNotFoundException {
        DocumentMasterTemplate template = loadDocMTemplate(pKey);
        em.remove(template);
        return template;
    }

    public DocumentMasterTemplate[] findAllDocMTemplates(String pWorkspaceId) {
        DocumentMasterTemplate[] templates;
        Query query = em.createQuery("SELECT DISTINCT t FROM DocumentMasterTemplate t WHERE t.workspaceId = :workspaceId");
        List listTemplates = query.setParameter("workspaceId", pWorkspaceId).getResultList();
        templates = new DocumentMasterTemplate[listTemplates.size()];
        for (int i = 0; i < listTemplates.size(); i++) {
            templates[i] = (DocumentMasterTemplate) listTemplates.get(i);
        }

        return templates;
    }

    public DocumentMasterTemplate loadDocMTemplate(DocumentMasterTemplateKey pKey)
            throws DocumentMasterTemplateNotFoundException {
        DocumentMasterTemplate template = em.find(DocumentMasterTemplate.class, pKey);
        if (template == null) {
            throw new DocumentMasterTemplateNotFoundException(mLocale, pKey.getId());
        } else {
            return template;
        }
    }

    public void createDocMTemplate(DocumentMasterTemplate pTemplate) throws DocumentMasterTemplateAlreadyExistsException, CreationException {
        try {
            //the EntityExistsException is thrown only when flush occurs
            em.persist(pTemplate);
            em.flush();
        } catch (EntityExistsException pEEEx) {
            throw new DocumentMasterTemplateAlreadyExistsException(mLocale, pTemplate);
        } catch (PersistenceException pPEx) {
            //EntityExistsException is case sensitive
            //whereas MySQL is not thus PersistenceException could be
            //thrown instead of EntityExistsException
            throw new CreationException(mLocale);
        }
    }
}
