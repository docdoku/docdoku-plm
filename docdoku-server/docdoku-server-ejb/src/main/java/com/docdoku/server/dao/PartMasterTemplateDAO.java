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

import com.docdoku.core.exceptions.CreationException;
import com.docdoku.core.exceptions.PartMasterTemplateAlreadyExistsException;
import com.docdoku.core.exceptions.PartMasterTemplateNotFoundException;
import com.docdoku.core.meta.ListOfValuesKey;
import com.docdoku.core.product.PartMasterTemplate;
import com.docdoku.core.product.PartMasterTemplateKey;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import java.util.List;
import java.util.Locale;

public class PartMasterTemplateDAO {

    private EntityManager em;
    private Locale mLocale;

    public PartMasterTemplateDAO(Locale pLocale, EntityManager pEM) {
        em = pEM;
        mLocale = pLocale;
    }

    public PartMasterTemplateDAO(EntityManager pEM) {
        em = pEM;
        mLocale = Locale.getDefault();
    }

    public void updatePartMTemplate(PartMasterTemplate pTemplate) {
        em.merge(pTemplate);
    }

    public PartMasterTemplate removePartMTemplate(PartMasterTemplateKey pKey) throws PartMasterTemplateNotFoundException {
        PartMasterTemplate template = loadPartMTemplate(pKey);
        em.remove(template);
        return template;
    }

    public List<PartMasterTemplate> findAllPartMTemplates(String pWorkspaceId) {
        Query query = em.createQuery("SELECT DISTINCT t FROM PartMasterTemplate t WHERE t.workspaceId = :workspaceId");
        return query.setParameter("workspaceId", pWorkspaceId).getResultList();
    }

    public PartMasterTemplate loadPartMTemplate(PartMasterTemplateKey pKey)
            throws PartMasterTemplateNotFoundException {
        PartMasterTemplate template = em.find(PartMasterTemplate.class, pKey);
        if (template == null) {
            throw new PartMasterTemplateNotFoundException(mLocale, pKey.getId());
        } else {
            return template;
        }
    }

    public void createPartMTemplate(PartMasterTemplate pTemplate) throws PartMasterTemplateAlreadyExistsException, CreationException {
        try {
            //the EntityExistsException is thrown only when flush occurs
            em.persist(pTemplate);
            em.flush();
        } catch (EntityExistsException pEEEx) {
            throw new PartMasterTemplateAlreadyExistsException(mLocale, pTemplate);
        } catch (PersistenceException pPEx) {
            //EntityExistsException is case sensitive
            //whereas MySQL is not thus PersistenceException could be
            //thrown instead of EntityExistsException
            throw new CreationException(mLocale);
        }
    }

    public List<PartMasterTemplate> findAllPartMTemplatesFromLOV(ListOfValuesKey lovKey){
        return em.createNamedQuery("PartMasterTemplate.findWhereLOV", PartMasterTemplate.class)
                .setParameter("lovName", lovKey.getName())
                .setParameter("workspace_id", lovKey.getWorkspaceId())
                .getResultList();
    }
}
