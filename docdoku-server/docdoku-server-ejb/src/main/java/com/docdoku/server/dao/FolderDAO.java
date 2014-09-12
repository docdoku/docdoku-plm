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
import com.docdoku.core.document.Folder;
import com.docdoku.core.exceptions.CreationException;
import com.docdoku.core.exceptions.FolderAlreadyExistsException;
import com.docdoku.core.exceptions.FolderNotFoundException;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import javax.persistence.TypedQuery;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FolderDAO {
    
    private EntityManager em;
    private Locale mLocale;
    private static final Logger LOGGER = Logger.getLogger(FolderDAO.class.getName());
    
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
        if (folder == null) {
            throw new FolderNotFoundException(mLocale, pCompletePath);
        } else {
            return folder;
        }
    }
    
    public void createFolder(Folder pFolder) throws FolderAlreadyExistsException, CreationException{
        try{
            //the EntityExistsException is thrown only when flush occurs          
            em.persist(pFolder);
            em.flush();
        }catch(EntityExistsException pEEEx){
            LOGGER.log(Level.FINEST,null,pEEEx);
            throw new FolderAlreadyExistsException(mLocale, pFolder);
        }catch(PersistenceException pPEx){
            //EntityExistsException is case sensitive
            //whereas MySQL is not thus PersistenceException could be
            //thrown instead of EntityExistsException
            LOGGER.log(Level.FINEST,null,pPEx);
            throw new CreationException(mLocale);
        }
    }
    
    public Folder[] getSubFolders(String pCompletePath){
        Folder[] folders;
        TypedQuery<Folder> query = em.createQuery("SELECT DISTINCT f FROM Folder f WHERE f.parentFolder.completePath = :completePath", Folder.class);
        query.setParameter("completePath",pCompletePath);
        List listFolders = query.getResultList();
        folders = new Folder[listFolders.size()];
        for(int i=0;i<listFolders.size();i++) {
            folders[i] = (Folder) listFolders.get(i);
        }
        
        return folders;
    }
    
    public Folder[] getSubFolders(Folder pFolder){
        return getSubFolders(pFolder.getCompletePath());
    }

    public List<DocumentRevision> removeFolder(String pCompletePath) throws FolderNotFoundException{
        Folder folder = em.find(Folder.class,pCompletePath);
        if(folder==null) {
            throw new FolderNotFoundException(mLocale, pCompletePath);
        }
        
        return removeFolder(folder);
    }
    
    public List<DocumentRevision> removeFolder(Folder pFolder){
        DocumentRevisionDAO docRDAO=new DocumentRevisionDAO(mLocale,em);
        List<DocumentRevision> allDocR = new LinkedList<>();
        List<DocumentRevision> docRs = docRDAO.findDocRsByFolder(pFolder.getCompletePath());
        allDocR.addAll(docRs);
        
        for(DocumentRevision docR:allDocR) {
            docRDAO.removeRevision(docR);
        }
        
        Folder[] subFolders = getSubFolders(pFolder);
        for(Folder subFolder:subFolders) {
            allDocR.addAll(removeFolder(subFolder));
        }
        
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