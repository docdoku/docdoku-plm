package com.docdoku.server.rest.dto;

public class FileDTO {
    private boolean created;
    private String fullName;
    private String shortName;

    public FileDTO(boolean created, String fullName, String shortName) {
        this.created = created;
        this.fullName = fullName;
        this.shortName = shortName;
    }

    public FileDTO() {
    }

    public boolean isCreated() {
        return created;
    }

    public void setCreated(boolean created) {
        this.created = created;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }
}
