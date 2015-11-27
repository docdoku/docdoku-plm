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

package com.docdoku.server.converters.step;

import com.docdoku.core.common.BinaryResource;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.product.PartIteration;
import com.docdoku.core.services.IDataManagerLocal;
import com.docdoku.core.util.FileIO;
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


@StepFileConverter
public class StepFileConverterImpl implements CADConverter {

    private static final String PYTHON_SCRIPT_TO_OBJ = "/com/docdoku/server/converters/step/convert_step_obj.py";
    private static final String CONF_PROPERTIES = "/com/docdoku/server/converters/step/conf.properties";
    private static final Properties CONF = new Properties();
    private static final Logger LOGGER = Logger.getLogger(StepFileConverterImpl.class.getName());

    @InternalService
    @Inject
    private IDataManagerLocal dataManager;

    static {
        try (InputStream inputStream = StepFileConverterImpl.class.getResourceAsStream(CONF_PROPERTIES)){
            CONF.load(inputStream);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, null, e);
        }
    }

    @Override
    public ConversionResult convert(PartIteration partToConvert, final BinaryResource cadFile, File tempDir) throws IOException, InterruptedException, UserNotActiveException, PartRevisionNotFoundException, WorkspaceNotFoundException, CreationException, UserNotFoundException, NotAllowedException, FileAlreadyExistsException, StorageException {

        String extension = FileIO.getExtension(cadFile.getName());
        File tmpCadFile = new File(tempDir, partToConvert.getKey() + "." + extension);
        File tmpOBJFile = new File(tempDir.getAbsolutePath() + "/" + UUID.randomUUID() + ".obj");

        String pythonInterpreter = CONF.getProperty("pythonInterpreter");
        String freeCadLibPath = CONF.getProperty("freeCadLibPath");

        File scriptToOBJ;

        try(InputStream scriptStream = StepFileConverterImpl.class.getResourceAsStream(PYTHON_SCRIPT_TO_OBJ)){
            scriptToOBJ = new File(tempDir,"python_script" + UUID.randomUUID() + ".py");
            Files.copy(scriptStream, scriptToOBJ.toPath());
        }

        try (InputStream in = dataManager.getBinaryResourceInputStream(cadFile)) {
            Files.copy(in, tmpCadFile.toPath());
        } catch (StorageException e) {
            LOGGER.log(Level.WARNING, null, e);
            throw new IOException(e);
        }

        String[] args = {pythonInterpreter, scriptToOBJ.getAbsolutePath(), "-l", freeCadLibPath, "-i", tmpCadFile.getAbsolutePath(), "-o", tmpOBJFile.getAbsolutePath()};
        ProcessBuilder pb = new ProcessBuilder(args);

        Process process = pb.start();

        // Read buffers
        String stdOutput = ConverterUtils.getOutput(process.getInputStream());
        String errorOutput = ConverterUtils.getOutput(process.getErrorStream());

        LOGGER.info(stdOutput);

        process.waitFor();

        if (process.exitValue() == 0) {
            return new ConversionResult(tmpOBJFile);
        }

        LOGGER.log(Level.SEVERE, "Cannot convert to obj : " + tmpCadFile.getAbsolutePath(), errorOutput);
        return null;
    }

    @Override
    public boolean canConvertToOBJ(String cadFileExtension) {
        return Arrays.asList("stp", "step", "igs", "iges").contains(cadFileExtension);
    }

}
