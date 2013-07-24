/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2013 DocDoku SARL
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

package com.docdoku.server.converters.utils;

import javax.vecmath.Vector3d;

public class PartThreeJs {

    public double[] vertices;

    public float getRadius() {
        double radius = 0, maxRadius = 0;

        for(int i = 3; i < vertices.length; i=i+3) {
            Vector3d vector = new Vector3d(vertices[i], vertices[i+1], vertices[i+2]);
            radius = vector.length();
            if ( radius > maxRadius ) maxRadius = radius;
        }

        return (float)maxRadius;
    }

}
