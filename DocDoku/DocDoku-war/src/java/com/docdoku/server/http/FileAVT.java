package com.docdoku.server.http;

import com.docdoku.core.util.FileIO;
import java.io.File;

public class FileAVT {

    private final static String[] LIST_OF_TEXT_FILE = {"odt", "html", "sxw", "swf", "sxc", "doc", "xls", "rtf", "txt", "ppt", "odp", "wpd", "tsv", "sxi", "csv", "pdf"};
    private final static String[] LIST_OF_AV_EXTENSIONS = {"mp3", "mpg", "flv", "mp4", "aac", "mov"};
    private final static String[] LIST_OF_IMAGES_EXTENSIONS = {"jpg", "png", "gif", "psd", "jpeg", "psp", "tif"};
    private String typeFile;
    private String filePath;

    public FileAVT() {
    }

    public FileAVT(String fileIn) {
        File file = new File(fileIn);
        this.filePath = file.getPath();

        if (verifyInListOfVideoAudio(file.getAbsolutePath()) != null) {
            this.typeFile = "audioVideo";
        } else if (verifyInListOfImagesExtension(file.getAbsolutePath()) != null) {
            this.typeFile = "image";
        } else if (verifyInListOfTextFile(file.getAbsolutePath()) != null) {
            this.typeFile = "textFile";
        } else {
            this.typeFile = "other";
        }
    }

    public String getTypeFile() {
        return typeFile;
    }

    public String getAVTPath() {
        return filePath;
    }

    private String verifyInListOfTextFile(String fileName) {
        String mime = null;
        File file = new File(fileName);
        String ext = FileIO.getExtension(file);
        for (int i = 0; i < LIST_OF_TEXT_FILE.length; i++) {
            if (ext != null && ext.equalsIgnoreCase(LIST_OF_TEXT_FILE[i])) {
                mime = LIST_OF_TEXT_FILE[i];
            }
        }
        return mime;
    }

    private String verifyInListOfVideoAudio(String fileName) {
        String mime = null;
        File file = new File(fileName);
        String ext = FileIO.getExtension(file);
        for (int i = 0; i < LIST_OF_AV_EXTENSIONS.length; i++) {

            if (ext != null && ext.equalsIgnoreCase(LIST_OF_AV_EXTENSIONS[i])) {
                mime = LIST_OF_AV_EXTENSIONS[i];
            }
        }
        return mime;
    }

    public String verifyInListOfImagesExtension(String fileName) {
        String mime = null;
        File file = new File(fileName);
        String ext=FileIO.getExtension(file);
        for (int i = 0; i < LIST_OF_IMAGES_EXTENSIONS.length; i++) {
            if (ext!=null && ext.equalsIgnoreCase(LIST_OF_IMAGES_EXTENSIONS[i])) {
                mime = LIST_OF_IMAGES_EXTENSIONS[i];
            }
        }
        return mime;
    }
}
