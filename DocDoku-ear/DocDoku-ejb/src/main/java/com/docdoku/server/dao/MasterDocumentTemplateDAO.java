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

import com.docdoku.core.services.CreationException;
import com.docdoku.core.services.MasterDocumentTemplateNotFoundException;
import com.docdoku.core.document.MasterDocumentTemplate;
import com.docdoku.core.document.MasterDocumentTemplateKey;
import com.docdoku.core.services.MasterDocumentTemplateAlreadyExistsException;

import java.util.List;
import java.util.Locale;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import javax.persistence.Query;

public class MasterDocumentTemplateDAO {

    private EntityManager em;
    private Locale mLocale;

    public MasterDocumentTemplateDAO(Locale pLocale, EntityManager pEM) {
        em = pEM;
        mLocale = pLocale;
    }

    public MasterDocumentTemplateDAO(EntityManager pEM) {
        em = pEM;
        mLocale = Locale.getDefault();
    }

    public void updateMDocTemplate(MasterDocumentTemplate pTemplate) {
        em.merge(pTemplate);
    }

    public MasterDocumentTemplate removeMDocTemplate(MasterDocumentTemplateKey pKey) throws MasterDocumentTemplateNotFoundException {
        MasterDocumentTemplate template = loadMDocTemplate(pKey);
        em.remove(template);
        return template;
    }

    public MasterDocumentTemplate[] findAllMDocTemplates(String pWorkspaceId) {
        MasterDocumentTemplate[] templates;
        Query query = em.createQuery("SELECT DISTINCT t FROM MasterDocumentTemplate t WHERE t.workspaceId = :workspaceId");
        List listTemplates = query.setParameter("workspaceId", pWorkspaceId).getResultList();
        templates = new MasterDocumentTemplate[listTemplates.size()];
        for (int i = 0; i < listTemplates.size(); i++) {
            templates[i] = (MasterDocumentTemplate) listTemplates.get(i);
        }

        return templates;
    }

    public MasterDocumentTemplate loadMDocTemplate(MasterDocumentTemplateKey pKey)
            throws MasterDocumentTemplateNotFoundException {
        MasterDocumentTemplate template = em.find(MasterDocumentTemplate.class, pKey);
        if (template == null) {
            throw new MasterDocumentTemplateNotFoundException(mLocale, pKey.getId());
        } else {
            return template;
        }
    }

    public void createMDocTemplate(MasterDocumentTemplate pTemplate) throws MasterDocumentTemplateAlreadyExistsException, CreationException {
        try {
            //the EntityExistsException is thrown only when flush occurs
            em.persist(pTemplate);
            em.flush();
        } catch (EntityExistsException pEEEx) {
            throw new MasterDocumentTemplateAlreadyExistsException(mLocale, pTemplate);
        } catch (PersistenceException pPEx) {
            //EntityExistsException is case sensitive
            //whereas MySQL is not thus PersistenceException could be
            //thrown instead of EntityExistsException
            throw new CreationException(mLocale);
        }
    }
}
