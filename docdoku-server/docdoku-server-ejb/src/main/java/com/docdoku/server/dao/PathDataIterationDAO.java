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

import com.docdoku.core.configuration.PathDataIteration;
import com.docdoku.core.configuration.ProductInstanceIteration;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PathDataIterationDAO {

    private EntityManager em;

    private static final Logger LOGGER = Logger.getLogger(PathDataIterationDAO.class.getName());

    public PathDataIterationDAO(EntityManager pEM) {
        em = pEM;
    }

    public void createPathDataIteration(PathDataIteration pathDataIteration){
        try {
            em.persist(pathDataIteration);
            em.flush();
        }catch (Exception e){
            LOGGER.log(Level.SEVERE,"Fail to create path data",e);
        }
    }

    public List<PathDataIteration> getPathDataIterations(String path, ProductInstanceIteration productInstanceIteration){
        return em.createNamedQuery("PathDataIteration.findFromPathAndProductInstanceIteration",PathDataIteration.class)
                .setParameter("path", path)
                .setParameter("productInstanceIteration", productInstanceIteration)
                .getResultList();
    }

    public PathDataIteration getLastPathDataIteration(String path, ProductInstanceIteration productInstanceIteration){
        List<PathDataIteration> pathDataIterations = getPathDataIterations(path, productInstanceIteration);
        if(pathDataIterations.isEmpty()){
            return null;
        }
        return pathDataIterations.get(pathDataIterations.size()-1);
    }

}