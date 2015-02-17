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


package com.docdoku.core.product;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Represents a CAD instance of a specific part defined in a
 * {@link PartUsageLink} {@link PartSubstituteLink}.
 * Using its attributes: translation and orientation on the three axis we'll be
 * able to create the mesh to render it.
 * 
 * @author Florent Garin
 * @version 1.1, 20/07/12
 * @since   V1.1
 */
@Table(name="CADINSTANCE")
@Entity
public class CADInstance implements Serializable, Cloneable {


    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Id
    private int id;
    
    
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

    
    public CADInstance() {
    }

    public CADInstance(double tx, double ty, double tz, double rx, double ry, double rz) {
        this.tx = tx;
        this.ty = ty;
        this.tz = tz;
        this.rx = rx;
        this.ry = ry;
        this.rz = rz;
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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public CADInstance clone() {
        CADInstance clone;
        try {
            clone = (CADInstance) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
        return clone;
    }

}
