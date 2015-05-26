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

import com.docdoku.core.configuration.DocumentCollection;

import javax.persistence.EntityManager;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DocumentCollectionDAO {

    private EntityManager em;

    private static Logger LOGGER = Logger.getLogger(DocumentCollectionDAO.class.getName());

    public DocumentCollectionDAO(EntityManager pEM) {
        em = pEM;
    }

    public void createDocumentCollection(DocumentCollection documentCollection){
        try {
            em.persist(documentCollection);
            em.flush();
        }catch (Exception e){
            LOGGER.log(Level.SEVERE,"Fail to create a collection of documents",e);
        }
    }
}