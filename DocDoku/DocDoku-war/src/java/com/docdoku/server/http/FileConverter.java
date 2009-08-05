/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
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
 * @author admin2
 */
public class FileConverter {

    private File fileClass;
    private String pdf2swfPath;

    private String ooHome;
    private int ooPort;

    public FileConverter(FileAVT fileAVT) {
        this.fileClass = new File(fileAVT.getAVTPath());
        this.pdf2swfPath = null;

    }

    public FileConverter(String pdf2swfCmd, FileAVT fileAVT, String ooHome, int ooPort) {
        this.fileClass = new File(fileAVT.getAVTPath());
        this.pdf2swfPath = pdf2swfCmd;
        this.ooHome=ooHome;
        this.ooPort=ooPort;
    }

    public String getFileName() {
        return fileClass.getAbsolutePath();
    }

    private File convertToSwfService() throws Exception {
        File fileWithDiapo = null;
        File swfFile = null;
        String extension = FileIO.getExtension(getFileName());
        if (extension.equals("pdf") == false) {
            //Si le fichier lui même est de format swf, plus besoin de le converit
            //ni de comparer les dates
            if (extension.equals("swf") == true) {
                File file = new File(getFileName());
                return file;
            } else {
                fileWithDiapo = new File(convertTopdf());
            }
        } else {
            fileWithDiapo = new File(getFileName());
        }
        int index = fileWithDiapo.getName().lastIndexOf(".");
        swfFile = new File(fileWithDiapo.getParentFile(), fileWithDiapo.getName().substring(0, index) + ".swf");
        if (swfFile.exists()) {
            if (fileWithDiapo.lastModified() <= swfFile.lastModified()) {
                return swfFile;
            }
        }

        String sep = File.separator;
        String sourceFile = fileWithDiapo.getCanonicalPath();
        String destinationRepository = swfFile.getAbsolutePath();
        String pdf2swfFolder = pdf2swfPath + sep + "pdf2swf";
        String[] cmdArray2 = {pdf2swfFolder, sourceFile, "-o", destinationRepository};

        Shell sh = new Shell();
        sh.setDirectory(new File(pdf2swfPath));
        ProcessConsumer pc = sh.exec(cmdArray2);
        pc.consume();

        addViewer(new File(destinationRepository));

        if (swfFile.exists()) {
            return swfFile;
        } else {
            return null;
        }
    }

    private void addViewer(File file) throws IOException, URISyntaxException {
        String destinationRepository = file.getAbsolutePath();
        String sep = File.separator;

        String rfxViewer = FileIO.urlToFile(FileConverter.class.getResource("/com/docdoku/server/http/resources/swf/rfxview.swf")).getCanonicalPath();
        String[] commandelecteur = {pdf2swfPath + sep + "swfcombine", rfxViewer, "viewport=" + destinationRepository, "-o", destinationRepository};

        Shell sh = new Shell();
        ProcessConsumer pc2 = sh.exec(commandelecteur);
        pc2.consume();

    }

    public File convertToSwf() throws Exception {

        File fileWithDiapo = null;
        File fileToReturn = null;
        String extension = FileIO.getExtension(getFileName());
        if (extension.equals("pdf") == false) {
            //Si le fichier lui même est de format swf, plus besoin de le converit
            //ni de comparer les dates
            if (extension.equals("swf") == true) {
                File file = new File(getFileName());
                return file;
            } else {
                fileWithDiapo = new File(convertTopdf());
            }
        } else {

            fileWithDiapo = new File(getFileName());
        }
        try {
            File fswf = verifyExistingFile("swf");
            if ((fswf != null)) {
                if (fswf.exists()) {
                    if (fswf.lastModified() <= fileWithDiapo.lastModified()) {
                        fileToReturn = fswf;
                    } else {
                        fileToReturn = convertToSwfService();
                    }
                } else {
                    fileToReturn = convertToSwfService();
                }
            } else {
                fileToReturn = convertToSwfService();
            }
        } catch (Exception ex) {
            throw new Exception(ex.getMessage());
        }
        return fileToReturn;

    }

    public String convertTopdf() throws Exception {
        File pdfFile = null;
        int index = fileClass.getName().lastIndexOf(".");
        pdfFile = new File(fileClass.getParentFile(), fileClass.getName().substring(0, index) + ".pdf");
        if (pdfFile.exists()) {
            if (fileClass.lastModified() <= pdfFile.lastModified()) {
                return pdfFile.getAbsolutePath();
            }
        }
        ManagedProcessOfficeManagerConfiguration cfg=new ManagedProcessOfficeManagerConfiguration(OfficeConnectionMode.socket(ooPort));
        cfg.setOfficeHome(new File(ooHome));
        OfficeManager officeManager = new ManagedProcessOfficeManager(cfg);
        officeManager.start();
        OfficeDocumentConverter converter = new OfficeDocumentConverter(officeManager);
        converter.convert(fileClass, pdfFile);

        return pdfFile.getAbsolutePath();

    }

    public File verifyExistingFile(String extension) {
        File swf = null;
        File file = new File(fileClass.getParentFile(), FileIO.getFileNameWithoutExtension(fileClass) + "." + extension);
        if (file.exists()) {
            swf = file;
        }
        return swf;
    }
}
