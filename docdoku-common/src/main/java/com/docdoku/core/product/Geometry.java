/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2014 DocDoku SARL
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

import com.docdoku.core.common.BinaryResource;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;

/**
 * Wraps a CAD file providing quality information.
 * Higher quality is more faces are defined.
 * This CAD file is not the native one used by the authoring tool but the generated one
 * used for the in-browser visualizer.
 * 
 * @author Florent Garin
 * @version 1.1, 20/07/12
 * @since   V1.1
 */
@Table(name="GEOMETRY")
@Entity
public class Geometry extends BinaryResource{

    /**
     * Starts at 0, smaller is greater.
     */
    private int quality;

    /*
    * Box
    * */

    private double xMin = 0;
    private double yMin = 0;
    private double zMin = 0;
    private double xMax = 0;
    private double yMax = 0;
    private double zMax = 0;

    public Geometry() {
    }
    
    public Geometry(int pQuality, String pFullName, long pContentLength, Date pLastModified) {
        super(pFullName, pContentLength, pLastModified);
        this.quality=pQuality;
    }

    public int getQuality() {
        return quality;
    }

    public void setQuality(int quality) {
        this.quality = quality;
    }

    public void setBox(double x1, double y1, double z1, double x2, double y2, double z2){
        xMin = Math.min(x1,x2);
        xMax = Math.max(x1,x2);
        yMin = Math.min(y1,y2);
        yMax = Math.max(y1,y2);
        zMin = Math.min(z1,z2);
        zMax = Math.max(z1,z2);
    }

}