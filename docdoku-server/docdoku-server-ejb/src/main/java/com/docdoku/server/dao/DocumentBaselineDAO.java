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

import com.docdoku.core.configuration.DocumentBaseline;
import com.docdoku.core.exceptions.BaselineNotFoundException;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Locale;

/**
 * Data access object for DocumentBaseline
 *
 * @author Taylor LABEJOF
 * @version 2.0, 28/08/14
 * @since   V2.0
 */
public class DocumentBaselineDAO {
    private EntityManager em;
    private Locale mLocale;

    public DocumentBaselineDAO(EntityManager em) {
        this.em = em;
        this.mLocale = Locale.getDefault();
    }

    public DocumentBaselineDAO(EntityManager em, Locale mLocale) {
        this.em = em;
        this.mLocale = mLocale;
    }

    public void createBaseline(DocumentBaseline documentBaseline) {
        em.persist(documentBaseline);
        em.flush();
    }

    public List<DocumentBaseline> findBaselines(String workspaceId) {
        return em.createQuery("SELECT b FROM DocumentBaseline b WHERE b.author.workspace.id = :workspaceId", DocumentBaseline.class)
                .setParameter("workspaceId",workspaceId)
                .getResultList();
    }

    public DocumentBaseline loadBaseline(int baselineId) throws BaselineNotFoundException {
        DocumentBaseline documentBaseline = em.find(DocumentBaseline.class,baselineId);
        if(documentBaseline == null){
            throw new BaselineNotFoundException(mLocale,baselineId);
        }else{
            return documentBaseline;
        }
    }

    public void deleteBaseline(DocumentBaseline documentBaseline) {
        em.remove(documentBaseline);
        em.flush();
    }

    public boolean existBaselinedDocument(String workspaceId, String documentId, String documentVersion) {
        return em.createNamedQuery("BaselinedDocument.existBaselinedDocument", Long.class)
                .setParameter("documentId", documentId)
                .setParameter("documentVersion", documentVersion)
                .setParameter("workspaceId", workspaceId)
                .getSingleResult() > 0;
    }
}
