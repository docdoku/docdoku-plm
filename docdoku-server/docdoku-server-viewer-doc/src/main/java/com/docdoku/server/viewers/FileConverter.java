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

package com.docdoku.server.viewers;

import com.developpez.adiguba.shell.ProcessConsumer;
import com.developpez.adiguba.shell.Shell;
import com.docdoku.core.util.FileIO;
import com.google.common.io.InputSupplier;
import org.artofsolving.jodconverter.OfficeDocumentConverter;
import org.artofsolving.jodconverter.office.DefaultOfficeManagerConfiguration;
import org.artofsolving.jodconverter.office.OfficeManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileConverter {

    private String pdf2SWFHome;
    private String ooHome;
    private int ooPort;

    private final static String SIMPLE_VIEWER="/com/docdoku/server/http/resources/swf/simple_viewer.swf";
    private final static String RFX_VIEWER="/com/docdoku/server/http/resources/swf/rfxview.swf";
  
    public FileConverter(String pdf2SWFHome, String ooHome, int ooPort) {
        this.pdf2SWFHome = pdf2SWFHome;
        this.ooHome = ooHome;
        this.ooPort = ooPort;
    }

    public FileConverter(String ooHome, int ooPort) {
        this.ooHome = ooHome;
        this.ooPort = ooPort;
    }

    public InputStream convertToPDF(final InputStream streamToConvert) throws IOException {
        File tmpDir = com.google.common.io.Files.createTempDir();
        File fileToConvert = new File(tmpDir, "source.tmp");

        com.google.common.io.Files.copy(new InputSupplier<InputStream>() {
            @Override
            public InputStream getInput() throws IOException {
                return streamToConvert;
            }
        }, fileToConvert);

        File pdfFile = directConvertToPDF(fileToConvert);

        //clean-up
        tmpDir.deleteOnExit();

        return new FileInputStream(pdfFile);
    }

    public InputStream convertToSWF(final InputStream streamToConvert) throws IOException {
        File tmpDir = com.google.common.io.Files.createTempDir();
        File fileToConvert = new File(tmpDir, "source.tmp");
        File swfFile = new File(tmpDir, "converted.swf");

        com.google.common.io.Files.copy(new InputSupplier<InputStream>() {
            @Override
            public InputStream getInput() throws IOException {
                return streamToConvert;
            }
        }, fileToConvert);

        File pdfFile = directConvertToPDF(fileToConvert);
        String sep = File.separator;

        String sourceFile = pdfFile.getAbsolutePath();
        String outputFile = swfFile.getAbsolutePath();

        String pdf2SWFCmd = pdf2SWFHome + sep + "pdf2swf";

        String[] cmdArray = {pdf2SWFCmd, sourceFile, "-o", outputFile, "-T 9", "-f"};

        Shell sh = new Shell();
        sh.setDirectory(new File(pdf2SWFHome));
        ProcessConsumer pc = sh.exec(cmdArray);
        pc.consume();

        tmpDir.deleteOnExit();
        return new FileInputStream(swfFile);
    }

    public File directConvertToPDF(File fileToConvert) {
        File pdfFile = new File(fileToConvert.getParentFile(), "converted.tmp");

        OfficeManager officeManager = new DefaultOfficeManagerConfiguration()
                .setOfficeHome(new File(ooHome))
                .setPortNumber(ooPort)
                .buildOfficeManager();
        officeManager.start();
        OfficeDocumentConverter converter = new OfficeDocumentConverter(officeManager);
        converter.convert(fileToConvert, pdfFile);
        officeManager.stop();

        return pdfFile;
    }

}
