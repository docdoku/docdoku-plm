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
package com.docdoku.server;

import com.docdoku.core.common.BinaryResource;
import com.docdoku.core.product.PartIteration;
import com.docdoku.core.product.PartIterationKey;
import com.docdoku.core.services.IConverterManagerLocal;
import com.docdoku.core.services.IDataManagerLocal;
import com.docdoku.core.services.IProductManagerLocal;
import com.docdoku.core.util.FileIO;
import com.docdoku.server.converters.CADConverter;
import com.docdoku.server.dao.PartIterationDAO;
import com.google.common.io.Files;

import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * CAD File converter
 *
 * @author Florent.Garin
 */
@Stateless(name="ConverterBean")
public class ConverterBean implements IConverterManagerLocal {

    @PersistenceContext
    private EntityManager em;

    @Inject
    @Any
    private Instance<CADConverter> converters;

    @EJB
    private IProductManagerLocal productService;

    @EJB
    private IDataManagerLocal dataManager;

    private static final String CONF_PROPERTIES="/com/docdoku/server/converters/utils/conf.properties";
    private static final Properties CONF = new Properties();
    private static final Logger logger = Logger.getLogger(ConverterBean.class.getName());

    static{

        InputStream inputStream = null;
        try {
            inputStream = ConverterBean.class.getResourceAsStream(CONF_PROPERTIES);
            CONF.load(inputStream);
        } catch (IOException e) {
            logger.log(Level.WARNING, null, e);
        } finally {
            try{
                if(inputStream!=null){
                    inputStream.close();
                }
            }catch (IOException e){
                logger.log(Level.FINEST, null, e);
            }
        }
    }

    @Override
    @Asynchronous
    public void convertCADFileToOBJ(PartIterationKey pPartIPK, BinaryResource cadBinaryResource) throws Exception {
        String ext = FileIO.getExtension(cadBinaryResource.getName());
        File convertedFile = null;
        CADConverter selectedConverter=null;
        for(CADConverter converter:converters){
            if(converter.canConvertToOBJ(ext)){
                selectedConverter=converter;
                break;
            }
        }
        if(selectedConverter!=null){

            PartIterationDAO partIDAO = new PartIterationDAO(em);
            PartIteration partI = partIDAO.loadPartI(pPartIPK);

            // Convert the file to obj format
            convertedFile = selectedConverter.convert(partI, cadBinaryResource);

            if(convertedFile != null){
               decimate(pPartIPK,convertedFile);
            }


        }

    }
    private void decimate(PartIterationKey pPartIPK, File file){

        String decimater = CONF.getProperty("decimaterPath");

        File tempDir = Files.createTempDir();

        try{

            String[] args = {decimater, "-i", file.getAbsolutePath(), "-o", tempDir.getAbsolutePath() , "1","0.6","0.2"};
            ProcessBuilder pb = new ProcessBuilder(args);
            Process proc = pb.start();
            proc.waitFor();

            if(proc.exitValue() == 0){
                String baseName = tempDir.getAbsolutePath() + "/" + FileIO.getFileNameWithoutExtension(file.getName());
                saveFile(pPartIPK,0,new File(baseName + "100.obj"));
                saveFile(pPartIPK,1,new File(baseName + "60.obj"));
                saveFile(pPartIPK,2,new File(baseName + "20.obj"));
            }

            logger.log(Level.INFO, "Decimation done, exit code = " + proc.exitValue());

        }catch(Exception e){
            logger.log(Level.SEVERE, null, e);
        } finally {
            FileIO.rmDir(tempDir);
        }

    }

    private void saveFile(PartIterationKey partIPK, int quality, File file) {

        OutputStream os = null;

        try {
            BinaryResource jsBinaryResource = productService.saveGeometryInPartIteration(partIPK,file.getName(), quality, file.length());
            os = dataManager.getBinaryResourceOutputStream(jsBinaryResource);
            Files.copy(file, os);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                os.flush();
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}