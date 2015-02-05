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

import com.docdoku.core.common.Account;
import com.docdoku.core.common.Workspace;
import com.docdoku.core.exceptions.AccountAlreadyExistsException;
import com.docdoku.core.exceptions.AccountNotFoundException;
import com.docdoku.core.exceptions.CreationException;
import com.docdoku.core.security.Credential;
import com.docdoku.core.security.UserGroupMapping;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import javax.persistence.TypedQuery;
import java.util.List;
import java.util.Locale;

public class AccountDAO {
    
    private EntityManager em;
    private Locale mLocale;
    
    public AccountDAO(Locale pLocale, EntityManager pEM) {
        mLocale=pLocale;
        em=pEM;
    }
    
    public AccountDAO(EntityManager pEM) {
        mLocale=Locale.getDefault();
        em=pEM;
    }

    public void createAccount(Account pAccount, String pPassword) throws AccountAlreadyExistsException, CreationException {
        try{
            //the EntityExistsException is thrown only when flush occurs 
            em.persist(pAccount);
            em.flush();
            Credential credential = Credential.createCredential(pAccount.getLogin(),pPassword);
            em.persist(credential);
            em.persist(new UserGroupMapping(pAccount.getLogin()));            
        }catch(EntityExistsException pEEEx){
            throw new AccountAlreadyExistsException(mLocale, pAccount.getLogin());
        }catch(PersistenceException pPEx){
            //EntityExistsException is case sensitive
            //whereas MySQL is not thus PersistenceException could be
            //thrown instead of EntityExistsException
            throw new CreationException(mLocale);
        }
    }
    
    public void updateAccount(Account pAccount, String pPassword){
        em.merge(pAccount);
        if(pPassword!=null){
            updateCredential(pAccount.getLogin(),pPassword);
        }
    }
    
    public void updateCredential(String pLogin, String pPassword){
        Credential credential = Credential.createCredential(pLogin,pPassword);
        em.merge(credential);
    }
    
    public Account loadAccount(String pLogin) throws AccountNotFoundException {
        Account account = em.find(Account.class,pLogin);
        if (account == null) {
            throw new AccountNotFoundException(mLocale, pLogin);
        } else {
            return account;
        }
    }
    
    public Workspace[] getAdministratedWorkspaces(Account pAdmin) {
        Workspace[] workspaces;
        TypedQuery<Workspace> query = em.createQuery("SELECT DISTINCT w FROM Workspace w WHERE w.admin = :admin", Workspace.class);
        List<Workspace> listWorkspaces = query.setParameter("admin",pAdmin).getResultList();
        workspaces = new Workspace[listWorkspaces.size()];
        for(int i=0;i<listWorkspaces.size();i++) {
            workspaces[i] = listWorkspaces.get(i);
        }
        
        return workspaces;    
    }

    public Workspace[] getAllWorkspaces() {
        Workspace[] workspaces;
        TypedQuery<Workspace> query = em.createQuery("SELECT DISTINCT w FROM Workspace w", Workspace.class);
        List<Workspace> listWorkspaces = query.getResultList();
        workspaces = new Workspace[listWorkspaces.size()];
        for(int i=0;i<listWorkspaces.size();i++) {
            workspaces[i] = listWorkspaces.get(i);
        }

        return workspaces;
    }

}
