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

package com.docdoku.server.converters.obj;

import com.docdoku.core.common.BinaryResource;
import com.docdoku.core.exceptions.StorageException;
import com.docdoku.core.product.PartIteration;
import com.docdoku.core.services.IDataManagerLocal;
import com.docdoku.server.InternalService;
import com.docdoku.server.ServiceLocator;
import com.docdoku.server.converters.CADConverter;
import com.docdoku.server.converters.utils.ConversionResult;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;


@ObjFileConverter
public class ObjFileConverterImpl implements CADConverter {


    @InternalService
    @Inject
    private IDataManagerLocal dataManager;

    private static final Logger LOGGER = Logger.getLogger(ObjFileConverterImpl.class.getName());


    @Override
    public ConversionResult convert(PartIteration partToConvert, final BinaryResource cadFile, File tempDir) throws Exception {

        File tmpCadFile = new File(tempDir, partToConvert.getKey() + ".obj");

        try (InputStream in = dataManager.getBinaryResourceInputStream(cadFile)) {
            Files.copy(in, tmpCadFile.toPath());
        } catch (StorageException e) {
            LOGGER.log(Level.WARNING, null, e);
            throw new IOException(e);
        }

        return new ConversionResult(tmpCadFile);
    }

    @Override
    public boolean canConvertToOBJ(String cadFileExtension) {
        return Arrays.asList("obj").contains(cadFileExtension);
    }

}