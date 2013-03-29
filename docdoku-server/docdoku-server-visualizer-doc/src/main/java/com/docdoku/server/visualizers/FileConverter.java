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

package com.docdoku.server.visualizers;

import com.developpez.adiguba.shell.ProcessConsumer;
import com.developpez.adiguba.shell.Shell;
import com.docdoku.core.util.FileIO;
import org.artofsolving.jodconverter.OfficeDocumentConverter;
import org.artofsolving.jodconverter.office.DefaultOfficeManagerConfiguration;
import org.artofsolving.jodconverter.office.OfficeManager;

import java.io.File;
import java.io.IOException;


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

    public File convertToSWF(File fileToConvert) throws IOException {
        if ("swf".equals(FileIO.getExtension(fileToConvert))) {
            return fileToConvert;
        }
        String swfFileNameWOExt = FileIO.getFileNameWithoutExtension(fileToConvert);
        File swfFile = new File(fileToConvert.getParentFile(), swfFileNameWOExt + ".swf");

        if (swfFile.exists() && fileToConvert.lastModified() < swfFile.lastModified()) {
            return swfFile;
        }

        File pdfFile=convertToPDF(fileToConvert);
        String sep = File.separator;

        String sourceFile = pdfFile.getAbsolutePath();
        String outputFile = swfFile.getAbsolutePath();
        String pdf2SWFCmd = pdf2SWFHome + sep + "pdf2swf";
        String[] cmdArray = {pdf2SWFCmd, sourceFile, "-o", outputFile, "-T 9", "-f"};

        Shell sh = new Shell();
        sh.setDirectory(new File(pdf2SWFHome));
        ProcessConsumer pc = sh.exec(cmdArray);
        pc.consume();

        /*
        String swfViewer = FileIO.urlToFile(FileConverter.class.getResource(RFX_VIEWER)).getAbsolutePath();
        cmdArray = new String[]{pdf2SWFHome + sep + "swfcombine", swfViewer, "viewport=" + outputFile, "-o", outputFile};
        pc = sh.exec(cmdArray);
        pc.consume();
        */
        return swfFile;
    }

    public File convertToPDF(File fileToConvert) {
        if ("pdf".equals(FileIO.getExtension(fileToConvert))) {
            return fileToConvert;
        }
        String pdfFileNameWOExt = FileIO.getFileNameWithoutExtension(fileToConvert);
        File pdfFile = new File(fileToConvert.getParentFile(), pdfFileNameWOExt + ".pdf");

        if (pdfFile.exists() && fileToConvert.lastModified() < pdfFile.lastModified()) {
            return pdfFile;
        }

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
