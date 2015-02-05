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

package com.docdoku.core.common;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Florent Garin
 */
public class Version implements Serializable, Comparable<Version>, Cloneable {

    private List<VersionUnit> mVersionUnits;

    public enum VersionUnit {
        A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U,V,W,X,Y,Z
    }

    public Version() {
        mVersionUnits = new LinkedList<>();
        mVersionUnits.add(VersionUnit.A);
    }

    public Version(String pStringVersion) {
        mVersionUnits = new LinkedList<>();

        for (int i = 0; i < pStringVersion.length(); i++) {
            try {
                mVersionUnits.add(VersionUnit.valueOf(pStringVersion.charAt(i) + ""));
            } catch (IllegalArgumentException pIAEx) {
                throw new VersionFormatException(pStringVersion, i);
            }
        }

    }

    public void increase() {

        for (int i = mVersionUnits.size() - 1; i > -1; i--) {
            VersionUnit unit = mVersionUnits.get(i);
            if (unit == VersionUnit.Z) {
                if (i == 0) {
                    mVersionUnits.add(0, VersionUnit.A);
                }
            } else {
                int ordinal = unit.ordinal();
                mVersionUnits.set(i, VersionUnit.values()[++ordinal]);
                break;
            }
        }

    }

    @Override
    public String toString() {
        StringBuilder stringVersion = new StringBuilder();
        for (VersionUnit unit : mVersionUnits) {
            stringVersion.append(unit.toString());
        }

        return stringVersion.toString();
    }

    @Override
    public boolean equals(Object pObj) {
        if (this == pObj) {
            return true;
        }
        if (!(pObj instanceof Version)) {
            return false;
        }
        Version version = (Version) pObj;
        //because of bug #6277781 Serialization of Enums over IIOP is broken.
        //we compare the string representation.
        //without this bug regular implementation would be: 
        //mVersionUnits.equals(version.mVersionUnits);
        return toString().equals(version.toString());
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    public int compareTo(Version pVersion) {
        return toString().compareTo(pVersion.toString());
    }

    /**
     * perform a deep clone operation
     */
    @Override
    public Version clone() {
        Version clone;
        try {
            clone = (Version) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
        //perform a deep copy
        clone.mVersionUnits = new LinkedList<>(mVersionUnits);
        return clone;
    }
}
