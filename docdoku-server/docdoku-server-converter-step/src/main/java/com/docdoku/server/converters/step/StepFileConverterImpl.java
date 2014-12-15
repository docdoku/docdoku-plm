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
import com.docdoku.core.product.PartIterationKey;
import com.docdoku.core.services.IDataManagerLocal;
import com.docdoku.core.services.IProductManagerLocal;
import com.docdoku.core.util.FileIO;
import com.docdoku.server.converters.CADConverter;
import com.docdoku.server.converters.utils.RadiusCalculator;
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
        File tmpJSFile = new File(tmpDir, woExName+".js");
        File tmpBINFile = new File(tmpDir, woExName + ".bin");
        File jsFile = null;
        InputStreamReader isr1 = null;
        BufferedReader br1 = null;
        InputStreamReader isr2 = null;
        BufferedReader br2 = null;

        try {
            // 1st step : convert cadFile to OBJ
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

            String[] args1 = {pythonInterpreter, scriptToOBJ.getAbsolutePath(), "-l" , freeCadLibPath, "-i", tmpCadFile.getAbsolutePath(), "-o", tmpOBJFile.getAbsolutePath()};
            ProcessBuilder pb1 = new ProcessBuilder(args1);

            Process p1 = pb1.start();
            isr1 = new InputStreamReader(p1.getInputStream());
            br1 = new BufferedReader(isr1);

            // read the output buffer (prevent waitFor to be never called)
            while (br1.readLine() != null);

            p1.waitFor();

            if(p1.exitValue() == 0){

                // 2nd step : convert temp obj file to bin/js
                File script =  FileIO.urlToFile(StepFileConverterImpl.class.getResource(PYTHON_SCRIPT_TO_JS));

                String[] args2 = {pythonInterpreter, script.getAbsolutePath(), "-t" ,"binary", "-i", tmpOBJFile.getAbsolutePath(), "-o", tmpJSFile.getAbsolutePath()};
                ProcessBuilder pb2 = new ProcessBuilder(args2);

                Process p2 = pb2.start();
                isr2 = new InputStreamReader(p2.getInputStream());
                br2 = new BufferedReader(isr2);
                while (br2.readLine() != null);

                p2.waitFor();

                if (p2.exitValue()==0) {

                    PartIterationKey partIPK = partToConvert.getKey();
                    BinaryResource binBinaryResource = productService.saveFileInPartIteration(partIPK, woExName + ".bin", tmpBINFile.length());
                    OutputStream binOutputStream = null;
                    try {
                        binOutputStream = dataManager.getBinaryResourceOutputStream(binBinaryResource);
                        Files.copy(tmpBINFile, binOutputStream);
                    } finally {
                        if(binOutputStream!=null){
                            binOutputStream.flush();
                            binOutputStream.close();
                        }
                    }

                    double radius = RadiusCalculator.calculateRadius(tmpJSFile);

                    BinaryResource jsBinaryResource = productService.saveGeometryInPartIteration(partIPK, woExName+".js", 0, tmpJSFile.length(),radius);
                    OutputStream jsOutputStream = null;
                    try {
                        jsOutputStream = dataManager.getBinaryResourceOutputStream(jsBinaryResource);
                        Files.copy(tmpJSFile, jsOutputStream);
                    } finally {
                        if(jsOutputStream!=null){
                            jsOutputStream.flush();
                            jsOutputStream.close();
                        }
                    }

                }
            }
            return jsFile;
        } finally {
            FileIO.rmDir(tmpDir);
            closeStream(isr1);
            closeStream(isr2);
            closeStream(br1);
            closeStream(br2);
        }
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
