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
package com.docdoku.core.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Florent Garin
 */
public class FileIO {

    private final static int CHUNK_SIZE = 1024 * 8;
    private final static int BUFFER_CAPACITY = 1024 * 16;


    private final static List<String> DOC_EXTENSIONS = Arrays.asList("odt", "html", "sxw", "swf", "sxc", "doc", "docx", "xls", "xlsx", "rtf", "txt", "ppt", "pptx", "odp", "wpd", "tsv", "sxi", "csv", "pdf");
    private final static List<String> AV_EXTENSIONS = Arrays.asList("mp3", "mpg", "flv", "mp4", "aac", "mov");
    private final static List<String> IMAGE_EXTENSIONS = Arrays.asList("jpg", "png", "gif", "psd", "jpeg", "psp", "tif");

    private FileIO() {
    }

    public static void rmDir(File pDir) {
        if (pDir.isDirectory()) {
            for (File subFile : pDir.listFiles()) {
                if (subFile.isDirectory()) {
                    rmDir(subFile);
                } else {
                    subFile.delete();
                }
            }
            pDir.delete();
        }
    }

    public static void copyFile(File pIn, File pOut) throws FileNotFoundException, IOException {
        pOut.getParentFile().mkdirs();
        pOut.createNewFile();
        InputStream in = new BufferedInputStream(new FileInputStream(pIn), BUFFER_CAPACITY);
        OutputStream out = new BufferedOutputStream(new FileOutputStream(pOut), BUFFER_CAPACITY);

        byte[] data = new byte[CHUNK_SIZE];
        int length;
        while ((length = in.read(data)) != -1) {
            out.write(data, 0, length);
        }

        in.close();
        out.close();
    }

    public static String getExtension(String fileName) {
        String ext = null;
        int i = fileName.lastIndexOf('.');
        if (i > 0 && i < fileName.length() - 1) {
            ext = fileName.substring(i + 1).toLowerCase();
        }
        return ext;
    }

    public static String getExtension(File file) {
        return getExtension(file.getName());
    }

    public static String getFileNameWithoutExtension(File file) {
        int index = file.getName().lastIndexOf(".");
        if(index!=-1)
            return file.getName().substring(0, index);
        else
            return file.getName();

    }

    public static File urlToFile(URL url) {
        File f;
        try {
            f = new File(url.toURI());
        } catch (Exception ex) {
            f = new File(url.getPath());
        }
        return f;
    }

    public static String encodeURL(String url) throws UnsupportedEncodingException {
        String encodedURL;

        String[] parts = url.split("/");
        encodedURL = URLEncoder.encode(parts[0], "UTF-8");

        for (int i = 1; i < parts.length; i++) {
            encodedURL += "/" + URLEncoder.encode(parts[i], "UTF-8");
        }
        return encodedURL;
    }

    public static String getLinkEncoded(String link, String enc) throws UnsupportedEncodingException {
        String codeUri;

        String[] tabFolder = link.split("/");
        codeUri = URLEncoder.encode(tabFolder[0], enc);

        for (int i = 1; i < tabFolder.length; i++) {
            codeUri += "/" + URLEncoder.encode(tabFolder[i], enc);
        }
        return codeUri;
    }

    public static boolean isAVFile(String fileName){
        String ext=getExtension(fileName);
        return AV_EXTENSIONS.contains(ext);
    }

    public static boolean isDocFile(String fileName){
        String ext=getExtension(fileName);
        return DOC_EXTENSIONS.contains(ext);
    }

    public static boolean isImageFile(String fileName){
        String ext=getExtension(fileName);
        return IMAGE_EXTENSIONS.contains(ext);
    }
}
