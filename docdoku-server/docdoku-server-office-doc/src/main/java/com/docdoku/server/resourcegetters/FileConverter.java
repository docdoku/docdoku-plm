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

package com.docdoku.server.resourcegetters;

import com.google.common.io.Files;
import com.google.common.io.InputSupplier;
import org.artofsolving.jodconverter.OfficeDocumentConverter;
import org.artofsolving.jodconverter.office.DefaultOfficeManagerConfiguration;
import org.artofsolving.jodconverter.office.OfficeManager;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Singleton;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

@Singleton
public class FileConverter {

    private static final String PROPERTIES_FILE = "/com/docdoku/server/viewers/conf.properties";
    private static final String OO_HOME_KEY = "com.docdoku.server.viewers.ooHome";
    private static final String OO_PORT_KEY = "com.docdoku.server.viewers.ooPort";
    private static final Logger LOGGER = Logger.getLogger(FileConverter.class.getName());

    private OfficeManager officeManager;

    @PostConstruct
    private void init() {

        try (InputStream inputStream = FileConverter.class.getResourceAsStream(PROPERTIES_FILE)){

            Properties properties = new Properties();

            properties.load(inputStream);
            String ooHome = properties.getProperty(OO_HOME_KEY);
            int ooPort = Integer.parseInt(properties.getProperty(OO_PORT_KEY));
            officeManager = new DefaultOfficeManagerConfiguration()
                    .setOfficeHome(new File(ooHome))
                    .setPortNumber(ooPort)
                    .buildOfficeManager();
            officeManager.start();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, null, e);
            throw new RuntimeException(e);
        }
    }

    @PreDestroy
    private void close(){
        officeManager.stop();
    }

    public synchronized InputStream convertToPDF(String sourceName, final InputStream streamToConvert) throws IOException {
        File tmpDir = com.google.common.io.Files.createTempDir();
        File fileToConvert = new File(tmpDir, sourceName);

        Files.copy(new InputSupplier<InputStream>() {
            @Override
            public InputStream getInput() throws IOException {
                return streamToConvert;
            }
        }, fileToConvert);

        File pdfFile = convertToPDF(fileToConvert);

        //clean-up
        tmpDir.deleteOnExit();

        return new FileInputStream(pdfFile);
    }

    private File convertToPDF(File fileToConvert) {
        File pdfFile = new File(fileToConvert.getParentFile(), "converted.pdf");
        OfficeDocumentConverter converter = new OfficeDocumentConverter(officeManager);
        converter.convert(fileToConvert, pdfFile);
        return pdfFile;
    }

}
