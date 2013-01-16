/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2013 DocDoku SARL
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

import com.docdoku.core.services.WorkspaceAlreadyExistsException;
import com.docdoku.core.services.WorkspaceNotFoundException;
import com.docdoku.core.services.FolderAlreadyExistsException;
import com.docdoku.core.services.CreationException;
import com.docdoku.core.*;
import com.docdoku.core.document.Folder;
import com.docdoku.core.common.Workspace;

import java.util.Locale;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;

public class WorkspaceDAO {
    
    private EntityManager em;    
    private Locale mLocale;
    
    public WorkspaceDAO(Locale pLocale, EntityManager pEM) {
        em=pEM;
        mLocale=pLocale;
    }
    
    public WorkspaceDAO(EntityManager pEM) {
        em=pEM;
        mLocale=Locale.getDefault();
    }
      
    public void updateWorkspace(Workspace pWorkspace){
        em.merge(pWorkspace);
    }
    
    public void createWorkspace(Workspace pWorkspace) throws WorkspaceAlreadyExistsException, CreationException, FolderAlreadyExistsException {
        try{
            //the EntityExistsException is thrown only when flush occurs
            em.persist(pWorkspace);
            em.flush();
            new FolderDAO(mLocale, em).createFolder(new Folder(pWorkspace.getId()));
        }catch(EntityExistsException pEEEx){
            throw new WorkspaceAlreadyExistsException(mLocale, pWorkspace);
        }catch(PersistenceException pPEx){
            //EntityExistsException is case sensitive
            //whereas MySQL is not thus PersistenceException could be
            //thrown instead of EntityExistsException
            throw new CreationException(mLocale);
        }
    }
    
    
    public Workspace loadWorkspace(String pID) throws WorkspaceNotFoundException {        
        Workspace workspace=em.find(Workspace.class,pID);      
        if (workspace == null) {
            throw new WorkspaceNotFoundException(mLocale, pID);
        } else {
            return workspace;
        }
    }
}