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

package com.docdoku.server.converters.ifc;

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
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;


@IFCFileConverter
public class IFCFileConverterImpl implements CADConverter{

    private static final String CONF_PROPERTIES="/com/docdoku/server/converters/ifc/conf.properties";
    private static final Properties CONF = new Properties();

    @InternalService
    @Inject
    private IDataManagerLocal dataManager;

    private static final Logger LOGGER = Logger.getLogger(IFCFileConverterImpl.class.getName());

    static{
        try (InputStream inputStream = IFCFileConverterImpl.class.getResourceAsStream(CONF_PROPERTIES)){
            CONF.load(inputStream);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, null, e);
        }
    }

    @Override
    public ConversionResult convert(PartIteration partToConvert, final BinaryResource cadFile, File tempDir) throws IOException, InterruptedException, UserNotActiveException, PartRevisionNotFoundException, WorkspaceNotFoundException, CreationException, UserNotFoundException, NotAllowedException, FileAlreadyExistsException, StorageException {


        UUID uuid = UUID.randomUUID();
        String extension = FileIO.getExtension(cadFile.getName());
        File tmpCadFile = new File(tempDir, partToConvert.getKey() + "." + extension);
        String convertedFileName = tempDir.getAbsolutePath() + "/" + uuid + ".obj" ;
        String convertedMtl = tempDir.getAbsolutePath() + "/" + uuid + ".mtl" ;
        String ifcConverter = CONF.getProperty("ifc_convert_path");

        File executable = new File(ifcConverter);

        if(!executable.exists()){
            LOGGER.log(Level.SEVERE, "Cannot convert file \""+cadFile.getName()+"\", \""+ifcConverter+"\" is not available");
            return null;
        }

        if(!executable.canExecute()){
            LOGGER.log(Level.SEVERE, "Cannot convert file \""+cadFile.getName()+"\", \""+ifcConverter+"\" has no execution rights");
            return null;
        }

        try(InputStream in = dataManager.getBinaryResourceInputStream(cadFile)) {
            Files.copy(in, tmpCadFile.toPath());
        } catch (StorageException e) {
            LOGGER.log(Level.WARNING, null, e);
            throw new IOException(e);
        }

        String[] args = {ifcConverter, "--sew-shells", tmpCadFile.getAbsolutePath(), convertedFileName};
        ProcessBuilder pb = new ProcessBuilder(args);
        Process process = pb.start();

        // Read buffers
        String stdOutput = ConverterUtils.getOutput(process.getInputStream());
        String errorOutput = ConverterUtils.getOutput(process.getErrorStream());

        LOGGER.info(stdOutput);

        process.waitFor();

        if(process.exitValue() == 0){
            List<File> materials = new ArrayList<>();
            materials.add(new File(convertedMtl));
            return new ConversionResult(new File(convertedFileName), materials);
        }

        LOGGER.log(Level.SEVERE, "Cannot convert to obj : " + tmpCadFile.getAbsolutePath(), errorOutput);
        return null;
    }

    @Override
    public boolean canConvertToOBJ(String cadFileExtension) {
        return Arrays.asList("ifc").contains(cadFileExtension);
    }

}