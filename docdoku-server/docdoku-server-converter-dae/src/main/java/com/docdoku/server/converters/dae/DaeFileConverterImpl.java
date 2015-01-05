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

package com.docdoku.server.converters.dae;

import com.docdoku.core.common.BinaryResource;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.product.PartIteration;
import com.docdoku.core.services.IDataManagerLocal;
import com.docdoku.core.services.IProductManagerLocal;
import com.docdoku.core.util.FileIO;
import com.docdoku.server.converters.CADConverter;
import com.google.common.io.Files;
import com.google.common.io.InputSupplier;

import javax.ejb.EJB;
import java.io.*;
import java.net.URL;
import java.util.Arrays;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

@DaeFileConverter
public class DaeFileConverterImpl implements CADConverter{

    private static final String CONF_PROPERTIES="/com/docdoku/server/converters/dae/conf.properties";
    private static final Properties CONF = new Properties();
    private static final Logger LOGGER = Logger.getLogger(DaeFileConverterImpl.class.getName());

    @EJB
    private IProductManagerLocal productService;

    @EJB
    private IDataManagerLocal dataManager;

    static{
        InputStream inputStream = null;
        try {
            inputStream = DaeFileConverterImpl.class.getResourceAsStream(CONF_PROPERTIES);
            CONF.load(inputStream);
        } catch (IOException e) {
            LOGGER.log(Level.INFO, null, e);
        } finally {
            try{
                if(inputStream!=null){
                    inputStream.close();
                }
            }catch (IOException e){
                LOGGER.log(Level.FINEST, null, e);
            }
        }
    }

    @Override
    public File convert(PartIteration partToConvert, final BinaryResource cadFile, File tempDir) throws IOException, InterruptedException, UserNotActiveException, PartRevisionNotFoundException, WorkspaceNotFoundException, CreationException, UserNotFoundException, NotAllowedException, FileAlreadyExistsException, StorageException {

        String assimp = CONF.getProperty("assimp");

        File tmpCadFile = new File(tempDir, cadFile.getName().trim());
        Files.copy(new InputSupplier<InputStream>() {
            @Override
            public InputStream getInput() throws IOException {
                try {
                    return dataManager.getBinaryResourceInputStream(cadFile);
                } catch (StorageException e) {
                    LOGGER.log(Level.WARNING, null, e);
                    throw new IOException(e);
                }
            }
        }, tmpCadFile);

        String convertedFileName = FileIO.getFileNameWithoutExtension(tmpCadFile.getAbsolutePath()) + ".obj";

        String[] args = {assimp, "export", tmpCadFile.getAbsolutePath(), convertedFileName};
        ProcessBuilder pb = new ProcessBuilder(args);
        Process proc = pb.start();

        proc.waitFor();

        if (proc.exitValue() == 0) {
            return new File(convertedFileName);
        }

        return null;

    }

    @Override
    public boolean canConvertToOBJ(String cadFileExtension) {
        return Arrays.asList("dae","ifc","lwo","x","ac","cob","scn","ms3d").contains(cadFileExtension);
    }

}
