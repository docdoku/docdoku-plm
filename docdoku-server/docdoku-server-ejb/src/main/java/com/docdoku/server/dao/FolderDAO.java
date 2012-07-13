/*
 * DocDoku, Professional Open Source
 * Copyright 2006, 2007, 2008, 2009, 2010, 2011, 2012 DocDoku SARL
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
import com.docdoku.core.services.FolderAlreadyExistsException;
import com.docdoku.core.services.FolderNotFoundException;
import com.docdoku.core.document.Folder;
import com.docdoku.core.document.DocumentMaster;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import javax.persistence.Query;

public class FolderDAO {
    
    private EntityManager em;
    private Locale mLocale;
    
    public FolderDAO(Locale pLocale, EntityManager pEM) {
        em = pEM;
        mLocale=pLocale;
    }
    
    public FolderDAO(EntityManager pEM) {
        em = pEM;
        mLocale=Locale.getDefault();
    }
    
    public Folder loadFolder(String pCompletePath) throws FolderNotFoundException {
        Folder folder = em.find(Folder.class,pCompletePath);
        if (folder == null)
            throw new FolderNotFoundException(mLocale, pCompletePath);
        else
            return folder;
    }
    
    public void createFolder(Folder pFolder) throws FolderAlreadyExistsException, CreationException{
        try{
            //the EntityExistsException is thrown only when flush occurs          
            em.persist(pFolder);
            em.flush();
        }catch(EntityExistsException pEEEx){
            throw new FolderAlreadyExistsException(mLocale, pFolder);
        }catch(PersistenceException pPEx){
            //EntityExistsException is case sensitive
            //whereas MySQL is not thus PersistenceException could be
            //thrown instead of EntityExistsException
            throw new CreationException(mLocale);
        }
    }
    
    public Folder[] getSubFolders(String pCompletePath){
        Folder[] folders;
        Query query = em.createQuery("SELECT DISTINCT f FROM Folder f WHERE f.parentFolder.completePath = :completePath");
        query.setParameter("completePath",pCompletePath);
        List listFolders = query.getResultList();
        folders = new Folder[listFolders.size()];
        for(int i=0;i<listFolders.size();i++)
            folders[i]=(Folder) listFolders.get(i);
        
        return folders;
    }
    
    public Folder[] getSubFolders(Folder pFolder){
        return getSubFolders(pFolder.getCompletePath());
    }
    
    //TODO performance should be improved
    private Set<Folder> getAllSubFolders(Folder pFolder){
        Set<Folder> allSubFolders = new HashSet<Folder>();
        Folder[] subFolders = getSubFolders(pFolder);
        allSubFolders.addAll(Arrays.asList(subFolders));
        
        for(Folder subFolder:subFolders)
            allSubFolders.addAll(getAllSubFolders(subFolder));
        return allSubFolders;
    }
    
    public List<DocumentMaster> removeFolder(String pCompletePath) throws FolderNotFoundException{
        Folder folder = em.find(Folder.class,pCompletePath);
        if(folder==null)
            throw new FolderNotFoundException(mLocale, pCompletePath);
        
        return removeFolder(folder);
    }
    
    public List<DocumentMaster> removeFolder(Folder pFolder){
        DocumentMasterDAO docMDAO=new DocumentMasterDAO(mLocale,em);
        List<DocumentMaster> allDocM = new LinkedList<DocumentMaster>();
        List<DocumentMaster> docMs = docMDAO.findDocMsByFolder(pFolder.getCompletePath());
        allDocM.addAll(docMs);
        
        for(DocumentMaster docM:allDocM)
            docMDAO.removeDocM(docM);
        
        Folder[] subFolders = getSubFolders(pFolder);
        for(Folder subFolder:subFolders)
            allDocM.addAll(removeFolder(subFolder));
        
        em.remove(pFolder);
        //flush to insure the right delete order to avoid integrity constraint
        //violation on folder.
        em.flush();
        
        return allDocM;
    }

    public List<DocumentMaster> moveFolder(Folder pFolder, Folder pNewFolder) throws FolderAlreadyExistsException, CreationException{
        DocumentMasterDAO docMDAO=new DocumentMasterDAO(mLocale,em);
        List<DocumentMaster> allDocMs = new LinkedList<DocumentMaster>();
        List<DocumentMaster> docMs = docMDAO.findDocMsByFolder(pFolder.getCompletePath());
        allDocMs.addAll(docMs);

        for(DocumentMaster docM:allDocMs){
            docM.setLocation(pNewFolder);
        }

        Folder[] subFolders = getSubFolders(pFolder);
        for(Folder subFolder:subFolders){
            Folder newSubFolder = new Folder(pNewFolder.getCompletePath(),subFolder.getShortName());
            createFolder(newSubFolder);
            allDocMs.addAll(moveFolder(subFolder,newSubFolder));
        }
        em.remove(pFolder);
        //flush to insure the right delete order to avoid integrity constraint
        //violation on folder.
        em.flush();

        return allDocMs;
    }
}