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

import com.docdoku.core.document.DocumentRevision;
import com.docdoku.core.exceptions.CreationException;
import com.docdoku.core.exceptions.FolderAlreadyExistsException;
import com.docdoku.core.exceptions.FolderNotFoundException;
import com.docdoku.core.document.Folder;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import javax.persistence.*;

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
        TypedQuery<Folder> query = em.createQuery("SELECT DISTINCT f FROM Folder f WHERE f.parentFolder.completePath = :completePath", Folder.class);
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
    
    public List<DocumentRevision> removeFolder(String pCompletePath) throws FolderNotFoundException{
        Folder folder = em.find(Folder.class,pCompletePath);
        if(folder==null)
            throw new FolderNotFoundException(mLocale, pCompletePath);
        
        return removeFolder(folder);
    }
    
    public List<DocumentRevision> removeFolder(Folder pFolder){
        DocumentRevisionDAO docRDAO=new DocumentRevisionDAO(mLocale,em);
        List<DocumentRevision> allDocR = new LinkedList<>();
        List<DocumentRevision> docRs = docRDAO.findDocRsByFolder(pFolder.getCompletePath());
        allDocR.addAll(docRs);
        
        for(DocumentRevision docR:allDocR)
            docRDAO.removeRevision(docR);
        
        Folder[] subFolders = getSubFolders(pFolder);
        for(Folder subFolder:subFolders)
            allDocR.addAll(removeFolder(subFolder));
        
        em.remove(pFolder);
        //flush to insure the right delete order to avoid integrity constraint
        //violation on folder.
        em.flush();
        
        return allDocR;
    }

    public List<DocumentRevision> moveFolder(Folder pFolder, Folder pNewFolder) throws FolderAlreadyExistsException, CreationException{
        DocumentRevisionDAO docRDAO=new DocumentRevisionDAO(mLocale,em);
        List<DocumentRevision> allDocRs = new LinkedList<>();
        List<DocumentRevision> docRs = docRDAO.findDocRsByFolder(pFolder.getCompletePath());
        allDocRs.addAll(docRs);

        for(DocumentRevision docR:allDocRs){
            docR.setLocation(pNewFolder);
        }

        Folder[] subFolders = getSubFolders(pFolder);
        for(Folder subFolder:subFolders){
            Folder newSubFolder = new Folder(pNewFolder.getCompletePath(),subFolder.getShortName());
            createFolder(newSubFolder);
            allDocRs.addAll(moveFolder(subFolder,newSubFolder));
        }
        em.remove(pFolder);
        //flush to insure the right delete order to avoid integrity constraint
        //violation on folder.
        em.flush();

        return allDocRs;
    }
}