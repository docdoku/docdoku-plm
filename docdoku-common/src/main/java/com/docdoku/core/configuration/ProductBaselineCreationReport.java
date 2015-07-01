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

import com.docdoku.core.product.PartRevision;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class ProductBaselineCreationReport implements Serializable{
    ProductBaseline productBaseline;
    String message;
    Set<PartRevision> conflits;

    public ProductBaselineCreationReport(){

    }

    public ProductBaselineCreationReport(ProductBaseline productBaseline) {
        this.productBaseline = productBaseline;
        this.message = null;
        this.conflits = new HashSet<>();
    }

    public ProductBaseline getProductBaseline() {
        return productBaseline;
    }
    public void setProductBaseline(ProductBaseline productBaseline) {
        this.productBaseline = productBaseline;
    }

    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }

    public Set<PartRevision> getConflits() {
        return conflits;
    }

    public void setConflits(Set<PartRevision> conflits) {
        this.conflits = conflits;
    }

    public void addConflit(PartRevision partRevision) {
        this.conflits.add(partRevision);
    }
    public void addConflits(Collection<PartRevision> partRevisions) {
        this.conflits.addAll(partRevisions);
    }
}
