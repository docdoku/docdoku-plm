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
package com.docdoku.server;

import com.docdoku.core.exceptions.SharedEntityNotFoundException;
import com.docdoku.core.services.IShareManagerLocal;
import com.docdoku.core.sharing.SharedEntity;
import com.docdoku.server.dao.SharedEntityDAO;

import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Date;

/**
 * @author Morgan Guimard
 */

@Local(IShareManagerLocal.class)
@Stateless(name = "ShareManagerBean")
public class ShareManagerBean implements IShareManagerLocal {

    @PersistenceContext
    private EntityManager em;

    @Override
    public SharedEntity findSharedEntityForGivenUUID(String pUuid) throws SharedEntityNotFoundException {
        SharedEntityDAO sharedEntityDAO = new SharedEntityDAO(em);
        return sharedEntityDAO.loadSharedEntity(pUuid);
    }

    @Override
    public void deleteSharedEntityIfExpired(SharedEntity pSharedEntity) {

        // insure the entity is really expired
        if(pSharedEntity.getExpireDate() != null){
            Date now = new Date();
            if(pSharedEntity.getExpireDate().getTime() < now.getTime()){
                SharedEntityDAO sharedEntityDAO = new SharedEntityDAO(em);
                sharedEntityDAO.deleteSharedEntity(pSharedEntity);
            }

        }
    }
}
