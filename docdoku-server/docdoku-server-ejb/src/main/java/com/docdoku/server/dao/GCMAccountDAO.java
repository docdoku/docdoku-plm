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
import com.docdoku.core.exceptions.CreationException;
import com.docdoku.core.exceptions.GCMAccountAlreadyExistsException;
import com.docdoku.core.exceptions.GCMAccountNotFoundException;
import com.docdoku.core.gcm.GCMAccount;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import java.util.Locale;

public class GCMAccountDAO {

    private EntityManager em;
    private Locale mLocale;

    public GCMAccountDAO(Locale pLocale, EntityManager pEM) {
        mLocale=pLocale;
        em=pEM;
    }

    public GCMAccountDAO(EntityManager pEM) {
        mLocale=Locale.getDefault();
        em=pEM;
    }

    public GCMAccount loadGCMAccount(Account account) throws GCMAccountNotFoundException {
        GCMAccount gcmAccount = em.find(GCMAccount.class, account.getLogin());
        if(gcmAccount == null){
            throw new GCMAccountNotFoundException(mLocale,account.getLogin());
        }
        return gcmAccount;
    }

    public void createGCMAccount(GCMAccount gcmAccount) throws GCMAccountAlreadyExistsException, CreationException {
        try{
            //the EntityExistsException is thrown only when flush occurs
            em.persist(gcmAccount);
            em.flush();
        }catch(EntityExistsException pEEEx){
            throw new GCMAccountAlreadyExistsException(mLocale, gcmAccount.getAccount().getLogin());
        }catch(PersistenceException pPEx){
            //EntityExistsException is case sensitive
            //whereas MySQL is not thus PersistenceException could be
            //thrown instead of EntityExistsException
            throw new CreationException(mLocale);
        }
    }

    public void deleteGCMAccount(GCMAccount gcmAccount){
        em.remove(gcmAccount);
        em.flush();
    }

}
