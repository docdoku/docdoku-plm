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

import com.docdoku.core.change.ModificationNotification;
import com.docdoku.core.product.PartIteration;
import com.docdoku.core.product.PartIterationKey;
import com.docdoku.core.product.PartRevision;
import com.docdoku.core.product.PartRevisionKey;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

public class ModificationNotificationDAO {
    private static final Logger LOGGER = Logger.getLogger(ModificationNotificationDAO.class.getName());

    private EntityManager em;
    private Locale mLocale;

    public ModificationNotificationDAO(Locale pLocale, EntityManager pEM) {
        em = pEM;
        mLocale = pLocale;
    }

    public ModificationNotificationDAO(EntityManager pEM) {
        em = pEM;
        mLocale = Locale.getDefault();
    }

    public void removeModificationNotifications(PartIterationKey pPartIPK){
        em.createNamedQuery("ModificationNotification.removeAllOnPartIteration")
                .setParameter("workspaceId", pPartIPK.getWorkspaceId())
                .setParameter("partNumber", pPartIPK.getPartMasterNumber())
                .setParameter("version", pPartIPK.getPartRevisionVersion())
                .setParameter("iteration", pPartIPK.getIteration()).executeUpdate();
    }

    public void removeModificationNotifications(PartRevisionKey pPartRPK){
        em.createNamedQuery("ModificationNotification.removeAllOnPartRevision")
                .setParameter("workspaceId", pPartRPK.getWorkspaceId())
                .setParameter("partNumber", pPartRPK.getPartMasterNumber())
                .setParameter("version", pPartRPK.getVersion()).executeUpdate();
    }

    public void createModificationNotification(ModificationNotification pNotification) {
        em.persist(pNotification);
    }

    public ModificationNotification getModificationNotification(int pId) {
        return em.find(ModificationNotification.class, pId);
    }

    public List<ModificationNotification> getModificationNotifications(PartIterationKey pPartIPK) {
        List<ModificationNotification> notifications =  em.createNamedQuery("ModificationNotification.findByImpactedPartIteration", ModificationNotification.class)
                .setParameter("workspaceId", pPartIPK.getWorkspaceId())
                .setParameter("partNumber", pPartIPK.getPartMasterNumber())
                .setParameter("version", pPartIPK.getPartRevisionVersion())
                .setParameter("iteration", pPartIPK.getIteration()).getResultList();
        return notifications;
    }





}
