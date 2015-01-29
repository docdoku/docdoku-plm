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

import com.docdoku.core.configuration.BaselinedFolder;
import com.docdoku.core.configuration.BaselinedFolderKey;
import com.docdoku.core.exceptions.FolderNotFoundException;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Locale;

public class BaselinedFolderDAO {

    private EntityManager em;
    private Locale mLocale;

    public BaselinedFolderDAO(Locale pLocale, EntityManager pEM) {
        em = pEM;
        mLocale=pLocale;
    }

    public BaselinedFolderDAO(EntityManager pEM) {
        em = pEM;
        mLocale=Locale.getDefault();
    }

    public BaselinedFolder loadBaselineFolder(BaselinedFolderKey baselinedFolderKey) throws FolderNotFoundException {
        BaselinedFolder baselinedFolder = em.find(BaselinedFolder.class,baselinedFolderKey);
        if (baselinedFolder == null) {
            throw new FolderNotFoundException(mLocale, baselinedFolderKey.getCompletePath());
        } else {
            return baselinedFolder;
        }
    }

    public List<BaselinedFolder> getSubFolders(BaselinedFolderKey baselinedFolderKey){
        return em.createQuery("" +
                "SELECT DISTINCT f " +
                "FROM BaselinedFolder f " +
                "WHERE f.parentFolder.completePath = :completePath " +
                "AND f.folderCollection.id = :collectionId", BaselinedFolder.class)
             .setParameter("completePath",baselinedFolderKey.getCompletePath())
             .setParameter("collectionId", baselinedFolderKey.getFolderCollection())
             .getResultList();
    }
}