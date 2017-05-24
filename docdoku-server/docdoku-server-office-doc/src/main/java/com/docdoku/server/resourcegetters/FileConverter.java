/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2017 DocDoku SARL
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

import org.artofsolving.jodconverter.OfficeDocumentConverter;
import org.artofsolving.jodconverter.office.DefaultOfficeManagerConfiguration;
import org.artofsolving.jodconverter.office.OfficeManager;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.inject.Singleton;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Properties;

@Singleton
public class FileConverter {

    @Resource(name = "office.config")
    private Properties properties;

    private OfficeManager officeManager;

    @PostConstruct
    private void init() {
        String ooHome = properties.getProperty("office_home");
        int ooPort = Integer.parseInt(properties.getProperty("office_port"));
        officeManager = new DefaultOfficeManagerConfiguration()
                .setOfficeHome(new File(ooHome))
                .setPortNumber(ooPort)
                .buildOfficeManager();
        officeManager.start();
    }

    @PreDestroy
    private void close() {
        officeManager.stop();
    }

    public synchronized InputStream convertToPDF(String sourceName, final InputStream streamToConvert) throws IOException {
        File tmpDir = Files.createTempDirectory("docdoku-").toFile();
        File fileToConvert = new File(tmpDir, sourceName);

        Files.copy(streamToConvert, fileToConvert.toPath());

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
