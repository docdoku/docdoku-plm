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

import com.docdoku.core.exceptions.PasswordRecoveryRequestNotFoundException;
import com.docdoku.core.security.PasswordRecoveryRequest;

import javax.persistence.EntityManager;
import java.util.Locale;

public class PasswordRecoveryRequestDAO {
    
    private EntityManager em;
    private Locale mLocale;
    
    public PasswordRecoveryRequestDAO(Locale pLocale, EntityManager pEM) {
        em = pEM;
        mLocale =pLocale;
    }
    
    public PasswordRecoveryRequestDAO(EntityManager pEM) {
        em=pEM;
        mLocale=Locale.getDefault();
    }
    
    public PasswordRecoveryRequest loadPasswordRecoveryRequest(String pPasswordRRUuid) throws PasswordRecoveryRequestNotFoundException {
        PasswordRecoveryRequest passwdRR = em.find(PasswordRecoveryRequest.class,pPasswordRRUuid);
        if (passwdRR == null) {
            throw new PasswordRecoveryRequestNotFoundException(mLocale, pPasswordRRUuid);
        } else {
            return passwdRR;
        }
    }
    

    public void removePasswordRecoveryRequest(PasswordRecoveryRequest pPasswdRRUuid){
        em.remove(pPasswdRRUuid);
    }
    
}