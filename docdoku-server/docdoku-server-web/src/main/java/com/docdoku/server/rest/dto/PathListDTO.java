package com.docdoku.server.rest.dto;


import java.io.Serializable;

public class PathListDTO implements Serializable {

    private String[] paths;

    public PathListDTO() {
    }

    public PathListDTO(String[] paths) {
        this.paths = paths;
    }

    public String[] getPaths() {
        return paths;
    }

    public void setPaths(String[] paths) {
        this.paths = paths;
    }
}
