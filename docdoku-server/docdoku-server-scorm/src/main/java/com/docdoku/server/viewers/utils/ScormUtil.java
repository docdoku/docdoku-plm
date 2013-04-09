package com.docdoku.server.viewers.utils;

import com.docdoku.core.util.FileIO;

import java.io.File;
import java.io.IOException;

public class ScormUtil {

    private ScormUtil(){}

    public static final String IMS_MANIFEST = "imsmanifest.xml";
    public static final String SCORM_FOLDER = "scorm";

    public static File getManifest(String vaultPath, String resourceFullName) {
        File scormArchive = new File(vaultPath, resourceFullName);
        File unzippedScormFolder = getUnzippedScormFolder(scormArchive);
        return new File(unzippedScormFolder, IMS_MANIFEST);
    }

    public static boolean isScormArchive(File file) {
        try {
            return FileIO.isArchiveFile(file.getName()) && FileIO.existsInArchive(file, IMS_MANIFEST);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void extractScormArchive(File scormArchive) {
        FileIO.unzipArchive(scormArchive, getUnzippedScormFolder(scormArchive));
    }

    public static File getScormSubResource(String resourceFullName, String subResourceName, String vaultPath) {
        File scormArchive = new File(vaultPath, resourceFullName);
        return new File(getUnzippedScormFolder(scormArchive), subResourceName);
    }

    public static File getUnzippedScormFolder(File scormArchive) {
        String fileNameWithoutExtension = FileIO.getFileNameWithoutExtension(scormArchive);
        return new File(scormArchive.getAbsolutePath().replace(scormArchive.getName(), SCORM_FOLDER), fileNameWithoutExtension);
    }

    public static boolean isScormPath(File file) {
        return file.getAbsolutePath().contains(File.separator + SCORM_FOLDER + File.separator);
    }

}
