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

package com.docdoku.server.converters.dae;

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
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@DaeFileConverter
public class DaeFileConverterImpl implements CADConverter {

    private static final String CONF_PROPERTIES = "/com/docdoku/server/converters/dae/conf.properties";
    private static final Properties CONF = new Properties();
    private static final Logger LOGGER = Logger.getLogger(DaeFileConverterImpl.class.getName());

    @InternalService
    @Inject
    private IDataManagerLocal dataManager;

    static {
        try (InputStream inputStream = DaeFileConverterImpl.class.getResourceAsStream(CONF_PROPERTIES)){
            CONF.load(inputStream);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, null, e);
        }
    }

    @Override
    public ConversionResult convert(PartIteration partToConvert, final BinaryResource cadFile, File tempDir) throws IOException, InterruptedException, UserNotActiveException, PartRevisionNotFoundException, WorkspaceNotFoundException, CreationException, UserNotFoundException, NotAllowedException, FileAlreadyExistsException, StorageException {

        String assimp = CONF.getProperty("assimp");

        File executable = new File(assimp);

        if (!executable.exists()) {
            LOGGER.log(Level.SEVERE, "Cannot convert file \"" + cadFile.getName() + "\", \"" + assimp + "\" is not available");
            return null;
        }

        if (!executable.canExecute()) {
            LOGGER.log(Level.SEVERE, "Cannot convert file \"" + cadFile.getName() + "\", \"" + assimp + "\" has no execution rights");
            return null;
        }


        File tmpCadFile = new File(tempDir, cadFile.getName().trim());
        try (InputStream in = dataManager.getBinaryResourceInputStream(cadFile)) {
            Files.copy(in, tmpCadFile.toPath());
        } catch (StorageException e) {
            LOGGER.log(Level.WARNING, null, e);
            throw new IOException(e);
        }

        UUID uuid = UUID.randomUUID();
        String convertedFileName = tempDir.getAbsolutePath() + "/" + uuid + ".obj";
        String convertedMtlFileName = tempDir.getAbsolutePath() + "/" + uuid + ".obj.mtl";

        String[] args = {assimp, "export", tmpCadFile.getAbsolutePath(), convertedFileName};
        ProcessBuilder pb = new ProcessBuilder(args);
        Process process = pb.start();

        // Read buffers
        String stdOutput = ConverterUtils.getOutput(process.getInputStream());
        String errorOutput = ConverterUtils.getOutput(process.getErrorStream());

        LOGGER.info(stdOutput);

        process.waitFor();

        if (process.exitValue() == 0) {
            List<File> materials = new ArrayList<>();
            materials.add(new File(convertedMtlFileName));
            return new ConversionResult(new File(convertedFileName), materials);
        }

        LOGGER.log(Level.SEVERE, "Cannot convert to obj : " + tmpCadFile.getAbsolutePath(), errorOutput);

        return null;

    }

    @Override
    public boolean canConvertToOBJ(String cadFileExtension) {
        return Arrays.asList("dxf", "dae", "lwo", "x", "ac", "cob", "scn", "ms3d").contains(cadFileExtension);
    }

}
