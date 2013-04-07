package com.docdoku.server.viewers.utils;

import com.docdoku.core.util.FileIO;

import java.io.File;
import java.io.IOException;

public class ScormUtil {

    public static final String IMS_MANIFEST = "imsmanifest.xml";

    public static File getManifest(String resourceFullName, String vaultPath) {
        File archive = new File(vaultPath + "/" + resourceFullName);
        File folder = new File(archive.getAbsolutePath().replace(archive.getName(), "scorm" + File.separator + FileIO.getFileNameWithoutExtension(archive)));
        return new File(folder, IMS_MANIFEST);
    }

    public static boolean isScormArchive(File file) {
        try {
            return FileIO.isArchiveFile(file.getName()) && FileIO.existsInArchive(file, IMS_MANIFEST);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

}
