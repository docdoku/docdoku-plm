package com.docdoku.server.util;

import javax.servlet.http.Part;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

/**
 * Created by asmae on 12/01/15.
 */
public class PartImp implements Part {

    private File fileToUpload;

    public PartImp(File file){
        this.fileToUpload = file;
    }
    @Override
    public InputStream getInputStream() throws IOException {
        return new FileInputStream(fileToUpload);
    }

    @Override
    public String getContentType() {
        return ResourceUtil.FILE_TYPE;
    }

    @Override
    public String getName() {
        return this.fileToUpload.getName();
    }

    @Override
    public String getSubmittedFileName() {
        return fileToUpload.getPath();
    }

    @Override
    public long getSize() {
        return fileToUpload.getTotalSpace();
    }

    @Override
    public void write(String s) throws IOException {

    }

    @Override
    public void delete() throws IOException {

    }

    @Override
    public String getHeader(String s) {
        return null;
    }

    @Override
    public Collection<String> getHeaders(String s) {
        return null;
    }

    @Override
    public Collection<String> getHeaderNames() {
        return null;
    }
}
