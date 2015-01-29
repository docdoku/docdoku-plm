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
import com.docdoku.core.common.Organization;
import com.docdoku.core.exceptions.CreationException;
import com.docdoku.core.exceptions.OrganizationAlreadyExistsException;
import com.docdoku.core.exceptions.OrganizationNotFoundException;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import java.util.Locale;

public class OrganizationDAO {


    private EntityManager em;
    private Locale mLocale;

    public OrganizationDAO(Locale pLocale, EntityManager pEM) {
        em=pEM;
        mLocale=pLocale;
    }

    public OrganizationDAO(EntityManager pEM) {
        em=pEM;
        mLocale=Locale.getDefault();
    }


    public void updateOrganization(Organization pOrganization){
        em.merge(pOrganization);
    }
    
    public void createOrganization(Organization pOrganization) throws OrganizationAlreadyExistsException, CreationException {
        try{
            //the EntityExistsException is thrown only when flush occurs
            em.persist(pOrganization);
            em.flush();
        }catch(EntityExistsException pEEEx){
            throw new OrganizationAlreadyExistsException(mLocale, pOrganization);
        }catch(PersistenceException pPEx){
            //EntityExistsException is case sensitive
            //whereas MySQL is not thus PersistenceException could be
            //thrown instead of EntityExistsException
            throw new CreationException(mLocale);
        }
    }

    public void deleteOrganization(Organization pOrganization) {
        for(Account member:pOrganization.getMembers())
            member.setOrganization(null);

        em.remove(pOrganization);
        em.flush();
    }
    
    public Organization loadOrganization(String pName) throws OrganizationNotFoundException {
        Organization organization=em.find(Organization.class,pName);
        if (organization == null) {
            throw new OrganizationNotFoundException(mLocale, pName);
        } else {
            return organization;
        }
    }


}