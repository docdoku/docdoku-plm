/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2014 DocDoku SARL
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

import com.docdoku.core.common.BinaryResource;
import com.docdoku.core.product.PartIteration;
import com.docdoku.core.product.PartIterationKey;
import com.docdoku.core.services.IConverterManagerLocal;
import com.docdoku.core.util.FileIO;
import com.docdoku.server.converters.CADConverter;
import com.docdoku.server.dao.PartIterationDAO;

import javax.ejb.AsyncResult;
import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.File;
import java.util.concurrent.Future;


/**
 * CAD File converter
 *
 * @author Florent.Garin
 */
@Stateless(name="ConverterBean")
public class ConverterBean implements IConverterManagerLocal {

    @PersistenceContext
    private EntityManager em;

    @Inject
    @Any
    private Instance<CADConverter> converters;

    @Override
    @Asynchronous
    public Future<File> convertCADFileToJSON(PartIterationKey pPartIPK, BinaryResource cadBinaryResource) throws Exception {
        String ext = FileIO.getExtension(cadBinaryResource.getName());
        File convertedFile = null;
        CADConverter selectedConverter=null;
        for(CADConverter converter:converters){
            if(converter.canConvertToJSON(ext)){
                selectedConverter=converter;
                break;
            }
        }
        if(selectedConverter!=null){
            PartIterationDAO partIDAO = new PartIterationDAO(em);
            PartIteration partI = partIDAO.loadPartI(pPartIPK);

            convertedFile = selectedConverter.convert(partI, cadBinaryResource);
        }
        return new AsyncResult<>(convertedFile);
    }


}
