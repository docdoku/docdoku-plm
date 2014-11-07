package com.docdoku.server.rest.dto;


import java.io.Serializable;

public class PathListDTO implements Serializable {
    private String configSpec;
    private String[] paths;

    public PathListDTO() {
    }

    public PathListDTO(String configSpec, String[] paths) {
        this.configSpec = configSpec;
        this.paths = paths;
    }

    public String getConfigSpec() {
        return configSpec;
    }
    public void setConfigSpec(String configSpec) {
        this.configSpec = configSpec;
    }

    public String[] getPaths() {
        return paths;
    }
    public void setPaths(String[] paths) {
        this.paths = paths;
    }
}
