package com.docdoku.server.rest.dto;

import java.io.Serializable;

public class OrganizationDTO implements Serializable {

    private String name;
    private String description;

    public OrganizationDTO() {
    }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }

    public void setDescription(String description) { this.description = description; }

}
