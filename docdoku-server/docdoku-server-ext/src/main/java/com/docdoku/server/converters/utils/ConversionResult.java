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
