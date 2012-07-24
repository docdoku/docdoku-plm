/*
 * DocDoku, Professional Open Source
 * Copyright 2006, 2007, 2008, 2009, 2010, 2011, 2012 DocDoku SARL
 *
 * This file is part of DocDoku.
 *
 * DocDoku is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DocDoku is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with DocDoku.  If not, see <http://www.gnu.org/licenses/>.
 */


package com.docdoku.core.product;

import java.io.Serializable;
import javax.persistence.Embeddable;

/**
 * Represents a CAD instance of a specific part defined in a
 * <code>PartUsageLink</code> or <code>PartSubstituteLink</code>. 
 * Using its attributes: translation and orientation on the three axis we'll be
 * able to create the mesh to render it.
 * 
 * @author Florent Garin
 * @version 1.1, 20/07/12
 * @since   V1.1
 */
@Embeddable
public class CADInstance implements Serializable{


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
     * Radian orientation on x axis.
     */
    private double rx;
    
    /**
     * Radian orientation on y axis.
     */
    private double ry;

    /**
     * Radian orientation on z axis.
     */
    private double rz;
    

    private Positioning positioning;   
    public enum Positioning {ABSOLUTE, PARENT_RELATIVE}
    
    public CADInstance() {
    }

    public CADInstance(double tx, double ty, double tz, double rx, double ry, double rz, Positioning positioning) {
        this.tx = tx;
        this.ty = ty;
        this.tz = tz;
        this.rx = rx;
        this.ry = ry;
        this.rz = rz;
        this.positioning = positioning;
    }
    
    

    public Positioning getPositioning() {
        return positioning;
    }

    public void setPositioning(Positioning positioning) {
        this.positioning = positioning;
    }


    public double getRx() {
        return rx;
    }

    public double getRy() {
        return ry;
    }

    public double getRz() {
        return rz;
    }

    public double getTx() {
        return tx;
    }

    public double getTy() {
        return ty;
    }

    public double getTz() {
        return tz;
    }

    public void setRx(double rx) {
        this.rx = rx;
    }

    public void setRy(double ry) {
        this.ry = ry;
    }

    public void setRz(double rz) {
        this.rz = rz;
    }

    public void setTx(double tx) {
        this.tx = tx;
    }

    public void setTy(double ty) {
        this.ty = ty;
    }

    public void setTz(double tz) {
        this.tz = tz;
    }

}
