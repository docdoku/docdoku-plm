/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2015 DocDoku SARL
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
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/**
 *
 * @author Florent Garin
 */
public class FileIO {

    private static final int CHUNK_SIZE = 1024 * 8;
    private static final int BUFFER_CAPACITY = 1024 * 16;


    private static final List<String> DOC_EXTENSIONS = Arrays.asList("odt", "html", "sxw", "swf", "sxc", "doc", "docx", "xls", "xlsx", "rtf", "txt", "ppt", "pptx", "odp", "wpd", "tsv", "sxi", "csv", "pdf");
    private static final List<String> AV_EXTENSIONS = Arrays.asList("mp3", "mpg", "flv", "mp4", "aac", "mov");
    private static final List<String> IMAGE_EXTENSIONS = Arrays.asList("jpg", "png", "gif", "psd", "jpeg", "psp", "tif");
    private static final List<String> ARCHIVE_EXTENSIONS = Arrays.asList("zip");

    private static final Logger LOGGER = Logger.getLogger(FileIO.class.getName());

    private FileIO() {
    }

    public static void rmDir(File pDir) {
        if (pDir.isDirectory()) {
            File[] files = pDir.listFiles();
            if(files!=null){
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
        try(InputStream in = new BufferedInputStream(new FileInputStream(pIn), BUFFER_CAPACITY);
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

    public static String getExtension(File file) {
        return getExtension(file.getName());
    }

    public static String getFileNameWithoutExtension(File file) {
        return getFileNameWithoutExtension(file.getName());
    }

    public static String getFileNameWithoutExtension(String fileName) {
        int index = fileName.lastIndexOf(".");
        if(index!=-1) {
            return fileName.substring(0, index);
        } else {
            return fileName;
        }
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
        StringBuilder encodedURLBuf = new StringBuilder();

        String[] parts = url.split("/");
        encodedURLBuf.append(URLEncoder.encode(parts[0], "UTF-8"));

        for (int i = 1; i < parts.length; i++) {
            encodedURLBuf.append("/").append(URLEncoder.encode(parts[i], "UTF-8"));
        }
        return encodedURLBuf.toString();
    }

    public static String getLinkEncoded(String link, String enc) throws UnsupportedEncodingException {
        StringBuilder codeUriBuild = new StringBuilder();

        String[] tabFolder = link.split("/");
        codeUriBuild.append(URLEncoder.encode(tabFolder[0], enc));

        for (int i = 1; i < tabFolder.length; i++) {
            codeUriBuild.append("/").append(URLEncoder.encode(tabFolder[i], enc));
        }
        return codeUriBuild.toString();
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

    public static boolean isArchiveFile(String fileName){
        String ext=getExtension(fileName);
        return ARCHIVE_EXTENSIONS.contains(ext);
    }

    public static void unzipArchive(File archive, File outputDir) {
        try {
            ZipFile zipfile = new ZipFile(archive);
            for (Enumeration e = zipfile.entries(); e.hasMoreElements(); ) {
                ZipEntry entry = (ZipEntry) e.nextElement();
                unzipEntry(zipfile, entry, outputDir);
            }
        } catch (Exception e) {
            LOGGER.log(Level.FINEST,null,e);
        }
    }

    private static void unzipEntry(ZipFile zipfile, ZipEntry entry, File outputDir) throws IOException {
        if (entry.isDirectory()) {
            new File(outputDir, entry.getName()).mkdirs();
            return;
        }

        File outputFile = new File(outputDir, entry.getName());
        if (!outputFile.getParentFile().exists()){
            outputFile.getParentFile().mkdirs();
        }
        try(BufferedInputStream in = new BufferedInputStream(zipfile.getInputStream(entry), BUFFER_CAPACITY);
        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(outputFile), BUFFER_CAPACITY)){
            copyBufferedStream(in, out);
        }
    }

    public static boolean existsInArchive(File archiveFile, String fileName) throws IOException {
        boolean exists = false;
        ZipFile zipfile = null;
        try{
            zipfile = new ZipFile(archiveFile);
            exists = zipfile.getEntry(fileName) != null;
        }finally {
            try{
                if(zipfile != null){
                    zipfile.close();
                }
            }catch (IOException e){
                LOGGER.log(Level.FINEST,null,e);
            }
        }
        return exists;
    }

    public static boolean existsInArchive(InputStream archiveInputStream, String fileName) {
        ZipEntry zipEntry;
        try(ZipInputStream zipInputStream = new ZipInputStream(archiveInputStream)) {
            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                if (zipEntry.getName().equals(fileName)) {
                    zipInputStream.close();
                    return true;
                }
            }
        } catch (IOException e) {
            LOGGER.log(Level.INFO, null, e);
        }
        return false;
    }

}
