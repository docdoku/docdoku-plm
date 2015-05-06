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

package com.docdoku.server.rest.file.util;

import com.docdoku.core.common.BinaryResource;

import javax.activation.FileTypeMap;
import javax.activation.MimetypesFileTypeMap;
import javax.ws.rs.core.EntityTag;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class BinaryResourceDownloadMeta {
    private static final Logger LOGGER = Logger.getLogger(BinaryResourceDownloadMeta.class.getName());
    private static MimetypesFileTypeMap fileTypeMap = null;
    private static final String CHARSET = "UTF-8";

    private String fullName;
    private String outputFormat;
    private String downloadType;
    private long length;
    private Date lastModified;

    private String subResourceVirtualPath;

    public BinaryResourceDownloadMeta(BinaryResource binaryResource, String outputFormat, String downloadType) {
        this.fullName = binaryResource.getName();
        this.outputFormat = outputFormat;
        this.downloadType = downloadType;
        this.length = binaryResource.getContentLength();
        this.lastModified = binaryResource.getLastModified();
        if(fileTypeMap==null){
            BinaryResourceDownloadMeta.initFileTypeMap();
        }
    }
    public BinaryResourceDownloadMeta(BinaryResource binaryResource) {
        this(binaryResource,null,null);
    }

    /**
     * Get the full name of the file
     * @return Full name of the file
     */
    public String getFullName(){
        return fullName;
    }

    /**
     * Get the output name of file
     * @return Output name of file
     */
    public String getFileName(){
        String fileName = fullName;
        try {
            fileName = URLEncoder.encode(fileName, CHARSET).replace("+", " ");
        } catch (UnsupportedEncodingException e) {
            LOGGER.log(Level.WARNING,null,e);
        }

        if(outputFormat !=null){
            fileName += "."+ outputFormat;
        }

        return fileName;
    }

    public boolean isConverted(){
        return outputFormat!=null && !outputFormat.isEmpty();
    }

    /**
     * Get the file size
     * @return File size
     */
    public long getLength(){
        return length;
    }

    /**
     * Get the last modification date of the file
     * @return Last modification date
     */
    public Date getLastModified(){
        return (lastModified!=null) ? (Date) lastModified.clone() : null;
    }

    /**
     * Get the last modification date of the file
     * @return Last modification date
     */
    public long getLastModifiedTime(){
        return (lastModified!=null) ? lastModified.getTime() : 0;
    }

    /**
     * Get file entity tag
     * @return Unique Entity Tag for the file
     */
    public EntityTag getETag(){
        //Todo add iteration and version
        //Todo remove special char from full Name
        return new EntityTag(fullName + "_" + length + "_" + lastModified.getTime());
    }

    public void setSubResourceVirtualPath(String subResourceVirtualPath){
        if(subResourceVirtualPath!=null && !subResourceVirtualPath.isEmpty()){
            this.subResourceVirtualPath = subResourceVirtualPath;
        }
    }

    /**
     * Get the Content type for this file
     * @return Http Response content type
     */
    public String getContentType(){
        String contentType;
        if (subResourceVirtualPath!=null) {
            contentType = FileTypeMap.getDefaultFileTypeMap().getContentType(subResourceVirtualPath);
        } else {
            if (outputFormat!=null) {
                contentType = FileTypeMap.getDefaultFileTypeMap().getContentType(fullName + "." + outputFormat);
            } else {
                contentType = FileTypeMap.getDefaultFileTypeMap().getContentType(fullName);
            }
        }

        if (contentType!=null && contentType.startsWith("text")) {
            contentType += ";charset="+ CHARSET;
        }

        return (contentType != null) ? contentType : "application/octet-stream";
    }

    /**
     * Get the Content disposition for this file
     * @return Http Response content disposition
     */
    // Todo check if we can have unencoding contentDisposition
    // Todo check accept request
    public String getContentDisposition(){
        String dispositionType = ("viewer".equals(downloadType)) ? "inline" : "attachement";
        return dispositionType+";filename=\""+ getFileName() +"\"";
    }

    private static void initFileTypeMap(){
        fileTypeMap = new MimetypesFileTypeMap();

        // Additional MIME types
        fileTypeMap.addMimeTypes("application/atom+xml atom");
        fileTypeMap.addMimeTypes("application/msword doc dot");
        fileTypeMap.addMimeTypes("application/mspowerpoint ppt pot");
        fileTypeMap.addMimeTypes("application/msexcel xls");
        fileTypeMap.addMimeTypes("application/pdf pdf");
        fileTypeMap.addMimeTypes("application/rdf+xml rdf rss");
        fileTypeMap.addMimeTypes("application/x-vnd.openxmlformat docx docm dotx dotm");
        fileTypeMap.addMimeTypes("application/x-vnd.openxmlformat xlsx xlsm");
        fileTypeMap.addMimeTypes("application/x-vnd.openxmlformat pptx pptm potx");
        fileTypeMap.addMimeTypes("application/x-javascript js");
        fileTypeMap.addMimeTypes("application/x-rar-compressed rar");
        fileTypeMap.addMimeTypes("application/x-textedit bat cmd");
        fileTypeMap.addMimeTypes("application/zip zip");
        fileTypeMap.addMimeTypes("audio/mpeg mp3");
        fileTypeMap.addMimeTypes("image/bmp bmp");
        fileTypeMap.addMimeTypes("image/gif gif");
        fileTypeMap.addMimeTypes("image/jpeg jpg jpeg jpe");
        fileTypeMap.addMimeTypes("image/png png");
        fileTypeMap.addMimeTypes("text/css css");
        fileTypeMap.addMimeTypes("text/csv csv");
        fileTypeMap.addMimeTypes("text/html htm html");
        fileTypeMap.addMimeTypes("text/xml xml");
        fileTypeMap.addMimeTypes("video/quicktime qt mov moov");
        fileTypeMap.addMimeTypes("video/mpeg mpeg mpg mpe mpv vbs mpegv");
        fileTypeMap.addMimeTypes("video/msvideo avi");
        fileTypeMap.addMimeTypes("video/mp4 mp4");
        fileTypeMap.addMimeTypes("video/ogg ogg");

        FileTypeMap.setDefaultFileTypeMap(fileTypeMap);
    }
}
