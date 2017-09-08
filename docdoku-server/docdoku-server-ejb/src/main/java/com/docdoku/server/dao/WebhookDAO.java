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

import com.docdoku.core.exceptions.WebhookNotFoundException;
import com.docdoku.core.hooks.Webhook;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Locale;

public class WebhookDAO {

    private EntityManager em;
    private Locale mLocale;

    public WebhookDAO(Locale pLocale, EntityManager pEM) {
        mLocale = pLocale;
        em = pEM;
    }

    public Webhook loadWebhook(int id) throws WebhookNotFoundException {
        Webhook webhook = em.find(Webhook.class, id);
        if (webhook == null) {
            throw new WebhookNotFoundException(mLocale, id);
        }
        return webhook;
    }

    public void removeWebook(Webhook w) {
        // remove options before

        // then
        em.remove(w);
        em.flush();
    }

    public List<Webhook> loadWebhooks(String workspaceId) {
        return em.createNamedQuery("Webhook.findByWorkspace", Webhook.class)
                .setParameter("workspaceId", workspaceId).getResultList();
    }

    public void createWebhook(Webhook webhook) {
        em.persist(webhook);
        em.flush();
    }
}
