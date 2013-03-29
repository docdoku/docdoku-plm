package com.docdoku.server.visualizers.utils;

import com.docdoku.core.util.FileIO;

import java.io.File;

public class ScormUtil {

    public static File getManifest(String resourceFullName, String vaultPath) {
        File archive = new File(vaultPath + "/" + resourceFullName);
        File folder = new File(archive.getAbsolutePath().replace(archive.getName(),"scorm/" + FileIO.getFileNameWithoutExtension(archive)));
        return new File(folder, "imsmanifest.xml");
    }

}
