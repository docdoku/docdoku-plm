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
        return new FileInputStream(this.fileToUpload);
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
