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

package com.docdoku.server.converters.catia;

import com.docdoku.core.common.BinaryResource;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.product.PartIteration;
import com.docdoku.core.services.IDataManagerLocal;
import com.docdoku.server.InternalService;
import com.docdoku.server.converters.CADConverter;
import com.docdoku.server.converters.utils.ConversionResult;
import com.docdoku.server.converters.utils.ConverterUtils;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Properties;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;


@CatiaFileConverter
public class CatiaFileConverterImpl implements CADConverter{

    private static final String CONF_PROPERTIES="/com/docdoku/server/converters/catia/conf.properties";

    private static final Properties CONF = new Properties();

    @InternalService
    @Inject
    private IDataManagerLocal dataManager;

    private static final Logger LOGGER = Logger.getLogger(CatiaFileConverterImpl.class.getName());

    static{
        try (InputStream inputStream = CatiaFileConverterImpl.class.getResourceAsStream(CONF_PROPERTIES)){
            CONF.load(inputStream);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, null, e);
        }
    }

    @Override
    public ConversionResult convert(PartIteration partToConvert, final BinaryResource cadFile, File tempDir) throws IOException, InterruptedException, UserNotActiveException, PartRevisionNotFoundException, WorkspaceNotFoundException, CreationException, UserNotFoundException, NotAllowedException, FileAlreadyExistsException, StorageException {
        File tmpCadFile = new File(tempDir, cadFile.getName());
        File tmpDAEFile = new File(tempDir, UUID.randomUUID()+".dae");
        String catPartConverter = CONF.getProperty("catPartConverter");

        File executable = new File(catPartConverter);

        if(!executable.exists()){
            LOGGER.log(Level.SEVERE, "Cannot convert file \""+cadFile.getName()+"\", \""+catPartConverter+"\" is not available");
            return null;
        }

        if(!executable.canExecute()){
            LOGGER.log(Level.SEVERE, "Cannot convert file \""+cadFile.getName()+"\", \""+catPartConverter+"\" has no execution rights");
            return null;
        }

        try(InputStream in = dataManager.getBinaryResourceInputStream(cadFile)) {
            Files.copy(in, tmpCadFile.toPath());
        } catch (StorageException e) {
            LOGGER.log(Level.WARNING, null, e);
            throw new IOException(e);
        }


        String[] args = {"sh", catPartConverter, tmpCadFile.getAbsolutePath() , tmpDAEFile.getAbsolutePath()};

        ProcessBuilder pb = new ProcessBuilder(args);
        Process process1 = pb.start();

        // Read buffers
        String stdOutput1 = ConverterUtils.getOutput(process1.getInputStream());
        String errorOutput1 = ConverterUtils.getOutput(process1.getErrorStream());

        LOGGER.info(stdOutput1);

        process1.waitFor();

        // Convert to OBJ once converted to DAE
        if (process1.exitValue() == 0 && tmpDAEFile.exists() && tmpDAEFile.length() > 0 ){

            String assimp = CONF.getProperty("assimp");
            String convertedFileName = tempDir.getAbsolutePath() + "/" + UUID.randomUUID() + ".obj";
            String[] argsOBJ = {assimp, "export", tmpDAEFile.getAbsolutePath(), convertedFileName};
            pb = new ProcessBuilder(argsOBJ);
            Process process2 = pb.start();

            // Read buffers
            String stdOutput2 = ConverterUtils.getOutput(process2.getInputStream());
            String errorOutput2 = ConverterUtils.getOutput(process2.getErrorStream());

            LOGGER.info(stdOutput2);

            process2.waitFor();

            if (process2.exitValue() == 0) {
                return new ConversionResult( new File(convertedFileName));
            }else {
                LOGGER.log(Level.SEVERE, "Cannot convert to obj : " + tmpCadFile.getAbsolutePath(), errorOutput2);
            }
        } else {
            LOGGER.log(Level.SEVERE, "Cannot convert to dae : " + tmpCadFile.getAbsolutePath(), errorOutput1);
        }

        return null;
    }

    @Override
    public boolean canConvertToOBJ(String cadFileExtension) {
        return Arrays.asList("catpart").contains(cadFileExtension);
    }
}
