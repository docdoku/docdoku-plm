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
package com.docdoku.core.util;

import java.io.*;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Helper class for file manipulation and information.
 *
 * @author Florent Garin
 */
public class FileIO {

    private static final int CHUNK_SIZE = 1024 * 8;
    private static final int BUFFER_CAPACITY = 1024 * 16;

    private static final List<String> DOC_EXTENSIONS = Arrays.asList("odt", "html", "sxw", "swf", "sxc", "doc", "docx", "xls", "xlsx", "rtf", "txt", "ppt", "pptx", "odp", "wpd", "tsv", "sxi", "csv", "pdf");
    private static final String ENCODING_CHARSET = "UTF-8";

    private static final Logger LOGGER = Logger.getLogger(FileIO.class.getName());

    private FileIO() {
    }

    public static void rmDir(File pDir) {
        if (pDir.isDirectory()) {
            File[] files = pDir.listFiles();
            if (files != null) {
                for (File subFile : files) {
                    if (subFile.isDirectory()) {
                        rmDir(subFile);
                    } else {
                        subFile.delete();
                    }
                }
                pDir.delete();
            }
        }
    }

    public static void copyFile(File pIn, File pOut) throws IOException {
        pOut.getParentFile().mkdirs();
        pOut.createNewFile();
        try (InputStream in = new BufferedInputStream(new FileInputStream(pIn), BUFFER_CAPACITY);
             OutputStream out = new BufferedOutputStream(new FileOutputStream(pOut), BUFFER_CAPACITY)) {
            FileIO.copyBufferedStream(in, out);
        }
    }

    public static void copyBufferedStream(InputStream in, OutputStream out) throws IOException {
        byte[] data = new byte[CHUNK_SIZE];
        int length;
        while ((length = in.read(data)) != -1) {
            out.write(data, 0, length);
        }
    }

    public static String getExtension(String fileName) {
        String ext = null;
        int i = fileName.lastIndexOf('.');
        if (i > 0 && i < fileName.length() - 1) {
            ext = fileName.substring(i + 1).toLowerCase();
        }
        return ext;
    }

    public static String getFileNameWithoutExtension(String fileName) {
        int index = fileName.lastIndexOf(".");
        if (index != -1) {
            return fileName.substring(0, index);
        } else {
            return fileName;
        }
    }


    public static boolean isDocFile(String fileName) {
        String ext = getExtension(fileName);
        return DOC_EXTENSIONS.contains(ext);
    }

    public static String encode(String toEncode) {
        try {
            return URLEncoder.encode(toEncode, ENCODING_CHARSET);
        } catch (UnsupportedEncodingException e) {
            LOGGER.log(Level.SEVERE, "Cannot encode string " + toEncode, e);
            return toEncode;
        }
    }

}
