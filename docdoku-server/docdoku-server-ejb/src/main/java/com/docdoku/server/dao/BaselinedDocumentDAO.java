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

import com.docdoku.core.configuration.BaselinedDocument;
import com.docdoku.core.configuration.BaselinedDocumentKey;
import com.docdoku.core.configuration.BaselinedFolderKey;
import com.docdoku.core.document.DocumentIteration;
import com.docdoku.core.document.DocumentRevisionKey;
import com.docdoku.core.exceptions.DocumentRevisionNotFoundException;

import javax.persistence.EntityManager;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class BaselinedDocumentDAO {

    private EntityManager em;
    private Locale mLocale;

    public BaselinedDocumentDAO(Locale pLocale, EntityManager pEM) {
        em = pEM;
        mLocale=pLocale;
    }

    public BaselinedDocumentDAO(EntityManager pEM) {
        em = pEM;
        mLocale=Locale.getDefault();
    }
    
    public BaselinedDocument loadBaselineDocument(BaselinedDocumentKey baselinedDocumentKey) throws DocumentRevisionNotFoundException {
        BaselinedDocument baselinedDocument = em.find(BaselinedDocument.class,baselinedDocumentKey);
        if (baselinedDocument == null) {
            DocumentRevisionKey key = new DocumentRevisionKey(baselinedDocumentKey.getTargetDocumentWorkspaceId(),baselinedDocumentKey.getTargetDocumentId(),"");
            throw new DocumentRevisionNotFoundException(mLocale, key);
        } else {
            return baselinedDocument;
        }
    }

    public List<DocumentIteration> findDocRsByFolder(BaselinedFolderKey baselinedFolderKey){
        List<DocumentIteration> list = em.createQuery("" +
                "SELECT d.documentIterations " +
                "FROM BaselinedFolder d " +
                "WHERE d.baselinedFolderKey = :pk ", DocumentIteration.class)
                .setParameter("pk", baselinedFolderKey)
                .getResultList();
        // TODO : find out why the list can contains a null value and remove this workaround :
        list.removeAll(Collections.singleton(null));
        return list;
    }

    public List<DocumentIteration> findDocumentRevision(DocumentRevisionKey pDocumentRevisionKey) {
        return em.createQuery("" +
                "SELECT d " +
                "FROM BaselinedFolder d, DocumentIteration i "  +
                "WHERE i member of d.documentIterations " +
                "AND i.documentRevision.documentMaster.workspace.id = :workspaceId " +
                "AND i.documentRevision.documentMasterId = :documentMasterId " +
                "AND i.documentRevision.version = :version",DocumentIteration.class)
                .setParameter("documentMasterId", pDocumentRevisionKey.getDocumentMaster().getId())
                .setParameter("version", pDocumentRevisionKey.getVersion())
                .setParameter("workspaceId", pDocumentRevisionKey.getDocumentMaster().getWorkspace())
                .getResultList();
    }

    public boolean hasDocumentRevision(DocumentRevisionKey pDocumentRevisionKey) {
        return !findDocumentRevision(pDocumentRevisionKey).isEmpty();
    }
}