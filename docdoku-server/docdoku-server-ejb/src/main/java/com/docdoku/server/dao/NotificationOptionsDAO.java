/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2017 DocDoku SARL
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

import com.docdoku.core.common.Workspace;
import com.docdoku.core.notification.NotificationOptions;

import javax.persistence.EntityManager;
import java.util.Locale;

public class NotificationOptionsDAO {

    private EntityManager em;
    private Locale mLocale;

    public NotificationOptionsDAO(Locale pLocale, EntityManager pEM) {
        em = pEM;
        mLocale = pLocale;
    }

    public NotificationOptionsDAO(EntityManager pEM) {
        em = pEM;
        mLocale = Locale.getDefault();
    }

    public void updateNotificationOptions(String workspaceId, boolean sendEmails) {
        NotificationOptions notificationOptions = getNotificationOptionsOrNew(workspaceId);
        notificationOptions.setSendEmails(sendEmails);
    }

    public NotificationOptions getNotificationOptionsOrNew(String workspaceId) {
        NotificationOptions notificationOptions = em.find(NotificationOptions.class, workspaceId);
        Workspace workspace = em.find(Workspace.class, workspaceId);
        if (notificationOptions == null) {
            notificationOptions = new NotificationOptions(workspace);
            em.persist(notificationOptions);
            em.flush();
        }
        return notificationOptions;
    }

}
