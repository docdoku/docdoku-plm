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

package com.docdoku.server.converters.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ConversionResult {

    private File convertedFile;
    private List<File> materials = new ArrayList<>();
    private String stdOutput;
    private String errorOutput;


    public ConversionResult(File convertedFile) {
        this.convertedFile = convertedFile;
    }

    public ConversionResult(File convertedFile, List<File> materials) {
        this.convertedFile = convertedFile;
        this.materials = materials;
    }

    public ConversionResult(File convertedFile, List<File> materials, String stdOutput, String errorOutput) {
        this.convertedFile = convertedFile;
        this.materials = materials;
        this.stdOutput = stdOutput;
        this.errorOutput = errorOutput;
    }

    public File getConvertedFile() {
        return convertedFile;
    }

    public void setConvertedFile(File convertedFile) {
        this.convertedFile = convertedFile;
    }

    public List<File> getMaterials() {
        return materials;
    }

    public void setMaterials(List<File> materials) {
        this.materials = materials;
    }

    public String getStdOutput() {
        return stdOutput;
    }

    public void setStdOutput(String stdOutput) {
        this.stdOutput = stdOutput;
    }

    public String getErrorOutput() {
        return errorOutput;
    }

    public void setErrorOutput(String errorOutput) {
        this.errorOutput = errorOutput;
    }
}
