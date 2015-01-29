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

/**
 *
 * @author Florent Garin
 */
public class TransformationDTO implements Serializable, Cloneable{

    /**
     * Translation on x axis.
     */
    private double tx;

    /**
     * Translation on y axis.
     */
    private double ty;

    /**
     * Translation on z axis.
     */
    private double tz;

    /**
     * Radian rotation on x axis.
     */
    private double rx;

    /**
     * Radian rotation on y axis.
     */
    private double ry;

    /**
     * Radian rotation on z axis.
     */
    private double rz;


    public TransformationDTO() {

    }

    public TransformationDTO(double tx, double ty, double tz, double rx, double ry, double rz) {
        this.tx = tx;
        this.ty = ty;
        this.tz = tz;
        this.rx = rx;
        this.ry = ry;
        this.rz = rz;
    }

    public double getTx() {
        return tx;
    }

    public void setTx(double tx) {
        this.tx = tx;
    }

    public double getTy() {
        return ty;
    }

    public void setTy(double ty) {
        this.ty = ty;
    }

    public double getTz() {
        return tz;
    }

    public void setTz(double tz) {
        this.tz = tz;
    }

    public double getRx() {
        return rx;
    }

    public void setRx(double rx) {
        this.rx = rx;
    }

    public double getRy() {
        return ry;
    }

    public void setRy(double ry) {
        this.ry = ry;
    }

    public double getRz() {
        return rz;
    }

    public void setRz(double rz) {
        this.rz = rz;
    }

    @Override
    public TransformationDTO clone() {
        TransformationDTO clone = null;
        try {
            clone = (TransformationDTO) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
        return clone;
    }
}
