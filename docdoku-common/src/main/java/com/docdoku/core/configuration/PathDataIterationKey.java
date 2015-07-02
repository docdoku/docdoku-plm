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

package com.docdoku.core.configuration;

import java.io.Serializable;

/**
 * Identity class of {@link com.docdoku.core.configuration.PathDataIteration}.
 *
 * @author Florent Garin
 */
public class PathDataIterationKey implements Serializable{

    private int pathDataMaster;
    private int iteration;

    public PathDataIterationKey(){
    }


    public PathDataIterationKey(int pathDataMaster, int iteration) {
        this.pathDataMaster = pathDataMaster;
        this.iteration = iteration;
    }

    public int getPathDataMaster() {
        return pathDataMaster;
    }

    public void setPathDataMaster(int pathDataMaster) {
        this.pathDataMaster = pathDataMaster;
    }

    public int getIteration() {
        return iteration;
    }

    public void setIteration(int iteration) {
        this.iteration = iteration;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PathDataIterationKey that = (PathDataIterationKey) o;

        if (iteration != that.iteration) {
            return false;
        }
        if (pathDataMaster != that.pathDataMaster) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = pathDataMaster;
        result = 31 * result + iteration;
        return result;
    }
}