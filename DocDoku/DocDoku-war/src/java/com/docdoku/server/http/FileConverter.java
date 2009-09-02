package com.docdoku.server.http;

import com.developpez.adiguba.shell.ProcessConsumer;
import com.developpez.adiguba.shell.Shell;
import com.docdoku.core.util.FileIO;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import net.sf.jodconverter.OfficeDocumentConverter;
import net.sf.jodconverter.office.ManagedProcessOfficeManager;
import net.sf.jodconverter.office.ManagedProcessOfficeManagerConfiguration;
import net.sf.jodconverter.office.OfficeConnectionMode;
import net.sf.jodconverter.office.OfficeManager;

/**
 *
 * @author Diom Yero Sow
 */
public class FileConverter {

    private String pdf2SWFHome;
    private String ooHome;
    private int ooPort;


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
        String[] cmdArray = {pdf2SWFCmd, sourceFile, "-o", outputFile};

        Shell sh = new Shell();
        sh.setDirectory(new File(pdf2SWFHome));
        ProcessConsumer pc = sh.exec(cmdArray);
        pc.consume();

        String rfxViewer = FileIO.urlToFile(FileConverter.class.getResource("/com/docdoku/server/http/resources/swf/rfxview.swf")).getAbsolutePath();
        cmdArray = new String[]{pdf2SWFHome + sep + "swfcombine", rfxViewer, "viewport=" + outputFile, "-o", outputFile};
        pc = sh.exec(cmdArray);
        pc.consume();

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

        ManagedProcessOfficeManagerConfiguration cfg = new ManagedProcessOfficeManagerConfiguration(OfficeConnectionMode.socket(ooPort));
        cfg.setOfficeHome(new File(ooHome));
        OfficeManager officeManager = new ManagedProcessOfficeManager(cfg);
        officeManager.start();
        OfficeDocumentConverter converter = new OfficeDocumentConverter(officeManager);
        converter.convert(fileToConvert, pdfFile);

        return pdfFile;
    }
}
