/*
 *
 *  * DocDoku, Professional Open Source
 *  * Copyright 2006 - 2015 DocDoku SARL
 *  *
 *  * This file is part of DocDokuPLM.
 *  *
 *  * DocDokuPLM is free software: you can redistribute it and/or modify
 *  * it under the terms of the GNU Affero General Public License as published by
 *  * the Free Software Foundation, either version 3 of the License, or
 *  * (at your option) any later version.
 *  *
 *  * DocDokuPLM is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  * GNU Affero General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU Affero General Public License
 *  * along with DocDokuPLM.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.docdoku.core.product;

import javax.persistence.Embeddable;
import javax.persistence.Transient;

/**
 * @author Charles Fallourd on 01/03/16.
 */
@Embeddable
public class RotationMatrix {

    private double m00, m01, m02, m10, m11, m12, m20, m21, m22;

    public RotationMatrix() {

    }

    public RotationMatrix(double[] values) {
        m00 = values[0];
        m01 = values[1];
        m02 = values[2];
        m10 = values[3];
        m11 = values[4];
        m12 = values[5];
        m20 = values[6];
        m21 = values[7];
        m22 = values[8];
    }

    public double getM00() {
        return m00;
    }

    public void setM00(double m00) {
        this.m00 = m00;
    }

    public double getM01() {
        return m01;
    }

    public void setM01(double m01) {
        this.m01 = m01;
    }

    public double getM02() {
        return m02;
    }

    public void setM02(double m02) {
        this.m02 = m02;
    }

    public double getM10() {
        return m10;
    }

    public void setM10(double m10) {
        this.m10 = m10;
    }

    public double getM11() {
        return m11;
    }

    public void setM11(double m11) {
        this.m11 = m11;
    }

    public double getM12() {
        return m12;
    }

    public void setM12(double m12) {
        this.m12 = m12;
    }

    public double getM20() {
        return m20;
    }

    public void setM20(double m20) {
        this.m20 = m20;
    }

    public double getM21() {
        return m21;
    }

    public void setM21(double m21) {
        this.m21 = m21;
    }

    public double getM22() {
        return m22;
    }

    public void setM22(double m22) {
        this.m22 = m22;
    }

    @Transient
    public double[] getValues() {
        return new double[]{ m00, m01, m02, m10, m11, m12, m20, m21, m22 };
    }
}
