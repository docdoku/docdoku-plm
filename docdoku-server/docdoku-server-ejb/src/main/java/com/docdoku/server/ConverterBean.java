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
import com.docdoku.core.product.Conversion;
import com.docdoku.core.product.Geometry;
import com.docdoku.core.product.PartIteration;
import com.docdoku.core.product.PartIterationKey;
import com.docdoku.core.services.IConverterManagerLocal;
import com.docdoku.core.services.IDataManagerLocal;
import com.docdoku.core.services.IProductManagerLocal;
import com.docdoku.core.util.FileIO;
import com.docdoku.server.converters.CADConverter;
import com.docdoku.server.converters.utils.GeometryParser;
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
import java.io.*;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * CAD File converter
 *
 * @author Florent.Garin
 */
@Stateless(name = "ConverterBean")
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

    private static final String CONF_PROPERTIES = "/com/docdoku/server/converters/utils/conf.properties";
    private static final Properties CONF = new Properties();
    private static final Logger LOGGER = Logger.getLogger(ConverterBean.class.getName());

    static {

        InputStream inputStream = null;
        try {
            inputStream = ConverterBean.class.getResourceAsStream(CONF_PROPERTIES);
            CONF.load(inputStream);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, null, e);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                LOGGER.log(Level.FINEST, null, e);
            }
        }
    }

    @Override
    @Asynchronous
    public void convertCADFileToOBJ(PartIterationKey pPartIPK, BinaryResource cadBinaryResource) throws Exception {

        // Are there any existing conversions
        Conversion existingConversion = productService.getConversion(pPartIPK);

        // Don't try to convert if any conversions pending
        if(existingConversion != null && existingConversion.isPending()){
            LOGGER.log(Level.SEVERE, "Conversion already running for part iteration " + pPartIPK);
            return;
        }

        // Clean old non pending conversions
        if(existingConversion != null){
            productService.removeConversion(existingConversion);
        }

        Conversion conversion = productService.createConversion(pPartIPK);

        File tempDir = Files.createTempDir();

        String ext = FileIO.getExtension(cadBinaryResource.getName());

        CADConverter selectedConverter = null;

        for (CADConverter converter : converters) {
            if (converter.canConvertToOBJ(ext)) {
                selectedConverter = converter;
                break;
            }
        }

        if (selectedConverter != null) {

            PartIterationDAO partIDAO = new PartIterationDAO(em);
            PartIteration partI = partIDAO.loadPartI(pPartIPK);

            File convertedFile = selectedConverter.convert(partI, cadBinaryResource, tempDir);

            double[] box = GeometryParser.calculateBox(convertedFile);

            if (convertedFile != null) {
                boolean succeed = decimate(pPartIPK, convertedFile, tempDir, box);
                conversion.setSucceed(succeed);
            }

        } else {
            LOGGER.log(Level.WARNING, "No CAD converter able to handle " + cadBinaryResource.getName());
        }

        FileIO.rmDir(tempDir);
        conversion.setPending(false);

    }

    private boolean decimate(PartIterationKey pPartIPK, File file, File tempDir, double[] box) {

        String decimater = CONF.getProperty("decimater");

        try {
            String[] args = {decimater, "-i", file.getAbsolutePath(), "-o", tempDir.getAbsolutePath(), "1", "0.6", "0.2"};
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

            if (proc.exitValue() == 0) {
                String baseName = tempDir.getAbsolutePath() + "/" + FileIO.getFileNameWithoutExtension(file.getName());
                saveFile(pPartIPK, 0, new File(baseName + "100.obj"), box);
                saveFile(pPartIPK, 1, new File(baseName + "60.obj"), box);
                saveFile(pPartIPK, 2, new File(baseName + "20.obj"), box);
                return true;
            } else {
                LOGGER.log(Level.SEVERE, "Decimation failed with code = " + proc.exitValue(), output.toString());
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Decimation failed for " + file.getAbsolutePath(), e);
        } finally {
            FileIO.rmDir(tempDir);
        }

        return false;
    }

    private void saveFile(PartIterationKey partIPK, int quality, File file, double[] box) {
        OutputStream os = null;

        try {
            Geometry lod = (Geometry) productService.saveGeometryInPartIteration(partIPK, file.getName(), quality, file.length(), box);
            os = dataManager.getBinaryResourceOutputStream(lod);
            Files.copy(file, os);
            LOGGER.log(Level.INFO, "Decimation and save done");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Cannot save geometry to part iteration", e);
        } finally {
            try {
                if(os != null){
                    os.flush();
                    os.close();
                }
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE,null, e);
            }
        }
    }


}