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


package com.docdoku.server.rest.dto;

import java.io.Serializable;


public class MarkerDTO implements Serializable{

    private int id;
      
    private String title;
    private String description;
    
    private double x;
    private double y;
    private double z;
    
    public MarkerDTO() {
    }

    public MarkerDTO(String pTitle, String pDescription, double pX, double pY, double pZ) {
        this.title=pTitle;
        this.description=pDescription;
        this.x=pX;
        this.y=pY;
        this.z=pZ;
    }

    public MarkerDTO(int pId, String pTitle, String pDescription, double pX, double pY, double pZ) {
        this.id = pId;
        this.title = pTitle;
        this.description = pDescription;
        this.x = pX;
        this.y = pY;
        this.z = pZ;
    }


    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }


    public void setX(double x) {
        this.x = x;
    }

    public double getX() {
        return x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getY() {
        return y;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public double getZ() {
        return z;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

}
