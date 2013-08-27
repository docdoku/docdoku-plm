/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2013 DocDoku SARL
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
import com.docdoku.core.product.PartIteration;
import com.docdoku.core.product.PartIterationKey;
import com.docdoku.core.services.*;
import com.docdoku.core.util.FileIO;
import com.docdoku.server.converters.CADConverter;
import com.google.common.io.Files;
import com.google.common.io.InputSupplier;

import javax.ejb.EJB;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Properties;


@CatiaFileConverter
public class CatiaFileConverterImpl implements CADConverter{

    private final static String CONF_PROPERTIES="/com/docdoku/server/converters/catia/conf.properties";

    private final static Properties CONF = new Properties();

    @EJB
    private IProductManagerLocal productService;

    @EJB
    private IDataManagerLocal dataManager;

    static{
        try {
            CONF.load(CatiaFileConverterImpl.class.getResourceAsStream(CONF_PROPERTIES));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public File convert(PartIteration partToConvert, final BinaryResource cadFile) throws IOException, InterruptedException, UserNotActiveException, PartRevisionNotFoundException, WorkspaceNotFoundException, CreationException, UserNotFoundException, NotAllowedException, FileAlreadyExistsException, StorageException {
        String woExName = FileIO.getFileNameWithoutExtension(cadFile.getName());
        File tmpDir = Files.createTempDir();
        File tmpCadFile;
        File tmpDAEFile = new File(tmpDir, woExName+".dae");
        File daeFile = null;

        try {
            String catPartConverter = CONF.getProperty("catPartConverter");

            tmpCadFile = new File(tmpDir, cadFile.getName());

            Files.copy(new InputSupplier<InputStream>() {
                @Override
                public InputStream getInput() throws IOException {
                    try {
                        return dataManager.getBinaryResourceInputStream(cadFile);
                    } catch (StorageException e) {
                        e.printStackTrace();
                        throw new IOException(e);
                    }
                }
            }, tmpCadFile);

            String[] args = {"sh", catPartConverter, tmpCadFile.getAbsolutePath() , tmpDAEFile.getAbsolutePath()};

            ProcessBuilder pb = new ProcessBuilder(args);
            Process process = pb.start();

            process.waitFor();

            int exitCode = process.exitValue();

            if (exitCode==0) {
                if(tmpDAEFile.exists() && tmpDAEFile.length() > 0 ){
                    PartIterationKey partIPK = partToConvert.getKey();
                    BinaryResource jsBinaryResource = productService.saveGeometryInPartIteration(partIPK, woExName+".dae", 0, tmpDAEFile.length());
                    OutputStream daeOutputStream = null;
                    try {
                        daeOutputStream = dataManager.getBinaryResourceOutputStream(jsBinaryResource);
                        Files.copy(tmpDAEFile, daeOutputStream);
                    } finally {
                        daeOutputStream.flush();
                        daeOutputStream.close();
                    }
                }
            }

            return daeFile;

        } catch(Exception e){

            e.printStackTrace();
            return null;
        }
        finally {
            FileIO.rmDir(tmpDir);
        }
    }

    @Override
    public boolean canConvertToJSON(String cadFileExtension) {
        return Arrays.asList("catpart").contains(cadFileExtension);
    }
}
