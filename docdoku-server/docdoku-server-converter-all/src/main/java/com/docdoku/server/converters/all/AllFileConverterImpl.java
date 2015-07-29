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

package com.docdoku.server.converters.all;

import com.docdoku.core.common.BinaryResource;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.product.PartIteration;
import com.docdoku.core.services.IDataManagerLocal;
import com.docdoku.core.util.FileIO;
import com.docdoku.server.converters.CADConverter;
import com.docdoku.server.converters.utils.ConversionResult;
import com.google.common.io.Files;
import com.google.common.io.InputSupplier;

import javax.ejb.EJB;
import java.io.*;
import java.util.Arrays;
import java.util.Properties;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;


@AllFileConverter
public class AllFileConverterImpl implements CADConverter{

    private static final String CONF_PROPERTIES="/com/docdoku/server/converters/all/conf.properties";
    private static final Properties CONF = new Properties();

    @EJB
    private IDataManagerLocal dataManager;

    private static final Logger LOGGER = Logger.getLogger(AllFileConverterImpl.class.getName());

    static{
        InputStream inputStream = null;
        try {
            inputStream = AllFileConverterImpl.class.getResourceAsStream(CONF_PROPERTIES);
            CONF.load(inputStream);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, null, e);
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
    public ConversionResult convert(PartIteration partToConvert, final BinaryResource cadFile, File tempDir) throws IOException, InterruptedException, UserNotActiveException, PartRevisionNotFoundException, WorkspaceNotFoundException, CreationException, UserNotFoundException, NotAllowedException, FileAlreadyExistsException, StorageException {

        String extension = FileIO.getExtension(cadFile.getName());
        File tmpCadFile = new File(tempDir, partToConvert.getKey() + "." + extension);
        String convertedFileName = tempDir.getAbsolutePath() + "/" + UUID.randomUUID();
        String meshconvBinary = CONF.getProperty("meshconv_path");

        File executable = new File(meshconvBinary);

        if(!executable.exists()){
            LOGGER.log(Level.SEVERE, "Cannot convert file \""+cadFile.getName()+"\", \""+meshconvBinary+"\" is not available");
            return null;
        }

        if(!executable.canExecute()){
            LOGGER.log(Level.SEVERE, "Cannot convert file \""+cadFile.getName()+"\", \""+meshconvBinary+"\" has no execution rights");
            return null;
        }

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

        String[] args = {meshconvBinary, tmpCadFile.getAbsolutePath(), "-c" , "obj", "-o", convertedFileName};
        ProcessBuilder pb = new ProcessBuilder(args);
        Process proc = pb.start();

        StringBuilder output = new StringBuilder();
        String line;
        // Read buffer
        InputStreamReader isr = new InputStreamReader(proc.getInputStream(),"UTF-8");
        BufferedReader br = new BufferedReader(isr);
        while ((line = br.readLine()) != null){
            output.append(line).append("\n");
        }
        br.close();

        proc.waitFor();

        if(proc.exitValue() == 0){
            return new ConversionResult(new File(convertedFileName + ".obj"));
        }

        LOGGER.log(Level.SEVERE, "Cannot convert to obj : " + tmpCadFile.getAbsolutePath(), output.toString());
        return null;
    }

    @Override
    public boolean canConvertToOBJ(String cadFileExtension) {
        return Arrays.asList("dxf","off","ply","stl","3ds","wrl").contains(cadFileExtension);
    }

}