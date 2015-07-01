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
 * Identity class of {@link ProductInstanceIteration} objects.
 *
 * @author Florent Garin
 */
public class ProductInstanceIterationKey implements Serializable {

    private ProductInstanceMasterKey productInstanceMaster;
    private int iteration;

    public ProductInstanceIterationKey() {
    }

    public ProductInstanceIterationKey(String serialNumber, String pWorkspaceId, String pId, int pIteration) {
        this(new ProductInstanceMasterKey(serialNumber, pWorkspaceId, pId), pIteration);
    }

    public ProductInstanceIterationKey(ProductInstanceMasterKey pProductInstanceMaster, int pIteration) {
        productInstanceMaster = pProductInstanceMaster;
        iteration = pIteration;
    }

    public int getIteration() {
        return iteration;
    }

    public void setIteration(int iteration) {
        this.iteration = iteration;
    }

    public ProductInstanceMasterKey getProductInstanceMaster() {
        return productInstanceMaster;
    }

    public void setProductInstanceMaster(ProductInstanceMasterKey productInstanceMaster) {
        this.productInstanceMaster = productInstanceMaster;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ProductInstanceIterationKey that = (ProductInstanceIterationKey) o;

        return iteration == that.iteration && productInstanceMaster.equals(that.productInstanceMaster);

    }

    @Override
    public int hashCode() {
        int result = productInstanceMaster.hashCode();
        result = 31 * result + iteration;
        return result;
    }

    @Override
    public String toString() {
        return productInstanceMaster + "-" + iteration;
    }


}
