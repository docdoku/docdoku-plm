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

package com.docdoku.server.converters.step;

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
import java.util.Arrays;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;


@StepFileConverter
public class StepFileConverterImpl implements CADConverter{

    private static final String PYTHON_SCRIPT_TO_OBJ="/com/docdoku/server/converters/step/convert_step_obj.py";
    private static final String PYTHON_SCRIPT_TO_JS="/com/docdoku/server/converters/step/convert_obj_three.py";
    private static final String CONF_PROPERTIES="/com/docdoku/server/converters/step/conf.properties";
    private static final Properties CONF = new Properties();
    private static final Logger LOGGER = Logger.getLogger(StepFileConverterImpl.class.getName());

    @EJB
    private IProductManagerLocal productService;

    @EJB
    private IDataManagerLocal dataManager;

    static{
        InputStream inputStream = null;
        try {
            inputStream = StepFileConverterImpl.class.getResourceAsStream(CONF_PROPERTIES);
            CONF.load(inputStream);
        } catch (IOException e) {
            Logger.getLogger(StepFileConverterImpl.class.getName()).log(Level.INFO, null, e);
        } finally {
            try{
                if(inputStream!=null){
                    inputStream.close();
                }
            }catch (IOException e){
                Logger.getLogger(StepFileConverterImpl.class.getName()).log(Level.FINEST, null, e);
            }
        }
    }

    @Override
    public File convert(PartIteration partToConvert, final BinaryResource cadFile) throws IOException, InterruptedException, UserNotActiveException, PartRevisionNotFoundException, WorkspaceNotFoundException, CreationException, UserNotFoundException, NotAllowedException, FileAlreadyExistsException, StorageException {

        String woExName = FileIO.getFileNameWithoutExtension(cadFile.getName());
        File tmpDir = Files.createTempDir();
        File tmpCadFile;
        File tmpOBJFile = new File(tmpDir, woExName+".obj");
        InputStreamReader isr = null;
        BufferedReader br = null;

        try {

            String pythonInterpreter = CONF.getProperty("pythonInterpreter");
            String freeCadLibPath = CONF.getProperty("freeCadLibPath");

            File scriptToOBJ =  FileIO.urlToFile(StepFileConverterImpl.class.getResource(PYTHON_SCRIPT_TO_OBJ));
            tmpCadFile = new File(tmpDir, cadFile.getName());

            Files.copy(new InputSupplier<InputStream>() {
                @Override
                public InputStream getInput() throws IOException {
                    try {
                        return dataManager.getBinaryResourceInputStream(cadFile);
                    } catch (StorageException e) {
                        Logger.getLogger(StepFileConverterImpl.class.getName()).log(Level.WARNING, null, e);
                        throw new IOException(e);
                    }
                }
            }, tmpCadFile);

            String[] args = {pythonInterpreter, scriptToOBJ.getAbsolutePath(), "-l" , freeCadLibPath, "-i", tmpCadFile.getAbsolutePath(), "-o", tmpOBJFile.getAbsolutePath()};
            ProcessBuilder pb = new ProcessBuilder(args);

            Process p = pb.start();
            isr = new InputStreamReader(p.getInputStream());
            br = new BufferedReader(isr);

            // read the output buffer (prevent waitFor to be never called)
            while (br.readLine() != null);

            p.waitFor();

            if(p.exitValue() == 0){
                return tmpOBJFile;
            }

        } finally {
            FileIO.rmDir(tmpDir);
            closeStream(isr);
            closeStream(br);
        }

        return null;
    }

    @Override
    public boolean canConvertToOBJ(String cadFileExtension) {
        return Arrays.asList("stp", "step", "igs", "iges").contains(cadFileExtension);
    }


    private void closeStream(Closeable stream) {
        try{
            if(stream!=null){
                stream.close();
            }
        }catch (IOException e){
            LOGGER.log(Level.FINEST, null, e);
        }
    }
}
