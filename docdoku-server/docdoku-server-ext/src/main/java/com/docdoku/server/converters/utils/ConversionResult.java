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

package com.docdoku.server.converters.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * This ConversionResult class represents the conversion status done by a CADConverter plugin.
 * <p>
 * It holds the converted file and its materials.
 */
// TODO replace java.io.File with java.io.InputStream (usage in EJBs)
public class ConversionResult {

    /**
     * The converted file for succeed conversions
     */
    private File convertedFile;
    /**
     * The list of materials files if any
     */
    private List<File> materials = new ArrayList<>();
    /**
     * The output of conversion program
     */
    private String stdOutput;
    /**
     * The error output of conversion program
     */
    private String errorOutput;

    /**
     * Default constructor
     */
    public ConversionResult() {
    }

    /**
     * Constructor with converted file
     *
     * @param convertedFile the converted file
     */
    public ConversionResult(File convertedFile) {
        this.convertedFile = convertedFile;
    }

    /**
     * Constructor with converted file and materials
     *
     * @param convertedFile the converted file
     */
    public ConversionResult(File convertedFile, List<File> materials) {
        this.convertedFile = convertedFile;
        this.materials = materials;
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
