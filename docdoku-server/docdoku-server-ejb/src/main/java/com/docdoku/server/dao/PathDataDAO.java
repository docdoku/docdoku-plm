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

import com.docdoku.core.configuration.PathData;
import com.docdoku.core.configuration.ProductInstanceMaster;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PathDataDAO {

    private EntityManager em;
    private Locale mLocale;

    private static Logger LOGGER = Logger.getLogger(PathDataDAO.class.getName());

    public PathDataDAO(EntityManager pEM) {
        em = pEM;
    }

    public PathDataDAO(Locale pLocale, EntityManager pEM) {
        em = pEM;
        mLocale = pLocale;
    }

    public void createPathData(PathData pathData){
        try {
            em.persist(pathData);
            em.flush();
        }catch (Exception e){
            LOGGER.log(Level.SEVERE,"Fail to create path data",e);
        }
    }


    public PathData findByPathAndProductInstance(String path, ProductInstanceMaster productInstanceMaster){
        try {
            return em.createNamedQuery("PathData.findByPathAndProductInstanceMaster", PathData.class)
                    .setParameter("path", path)
                    .setParameter("productInstanceMaster", productInstanceMaster)
                    .getSingleResult();
        }catch(NoResultException e){
            return null;
        }
    }
    public ProductInstanceMaster findByPathData(PathData pathData){
        try {
            return em.createNamedQuery("ProductInstanceMaster.findByPathData", ProductInstanceMaster.class)
                    .setParameter("pathData", pathData)
                    .getSingleResult();
        }catch(NoResultException e){
            return null;
        }
    }

    public void removePathData(PathData pathData) {
        em.remove(pathData);
        em.flush();
    }
}