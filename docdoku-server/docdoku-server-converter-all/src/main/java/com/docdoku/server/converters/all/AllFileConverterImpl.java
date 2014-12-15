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

package com.docdoku.server.converters.all;

import com.docdoku.core.common.BinaryResource;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.product.PartIteration;
import com.docdoku.core.product.PartIterationKey;
import com.docdoku.core.services.IDataManagerLocal;
import com.docdoku.core.services.IProductManagerLocal;
import com.docdoku.core.util.FileIO;
import com.docdoku.server.converters.CADConverter;
import com.docdoku.server.converters.utils.RadiusCalculator;
import com.google.common.io.Files;
import com.google.common.io.InputSupplier;

import javax.ejb.EJB;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;


@AllFileConverter
public class AllFileConverterImpl implements CADConverter{

    @EJB
    private IProductManagerLocal productService;

    @EJB
    private IDataManagerLocal dataManager;

    @Override
    public File convert(PartIteration partToConvert, final BinaryResource cadFile) throws IOException, InterruptedException, UserNotActiveException, PartRevisionNotFoundException, WorkspaceNotFoundException, CreationException, UserNotFoundException, NotAllowedException, FileAlreadyExistsException, StorageException {
        File tmpDir = Files.createTempDir();
        File tmpCadFile = new File(tmpDir, cadFile.getName());
        Files.copy(new InputSupplier<InputStream>() {
            @Override
            public InputStream getInput() throws IOException {
                try {
                    return dataManager.getBinaryResourceInputStream(cadFile);
                } catch (StorageException e) {
                    Logger.getLogger(AllFileConverterImpl.class.getName()).log(Level.WARNING, null, e);
                    throw new IOException(e);
                }
            }
        }, tmpCadFile);

        try {
            PartIterationKey partIPK = partToConvert.getKey();

            // Calculate radius
            double radius = RadiusCalculator.calculateRadius(tmpCadFile);

            // Upload dae
            BinaryResource daeBinaryResource = productService.saveGeometryInPartIteration(partIPK, tmpCadFile.getName(), 0, tmpCadFile.length(), radius);
            OutputStream daeOutputStream = null;
            try {
                daeOutputStream = dataManager.getBinaryResourceOutputStream(daeBinaryResource);
                Files.copy(tmpCadFile, daeOutputStream);
            } finally {
                if(daeOutputStream!=null){
                    daeOutputStream.flush();
                    daeOutputStream.close();
                }
            }
            return tmpCadFile;
        } finally {
            FileIO.rmDir(tmpDir);
        }
    }

    @Override
    public boolean canConvertToOBJ(String cadFileExtension) {
        return Arrays.asList("stl", "dae").contains(cadFileExtension);
    }
}