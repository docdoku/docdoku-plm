/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2017 DocDoku SARL
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * A class to store dry run result of a data import.
 *
 * A dry run informs of the actions that will be performed, parts to checkout,
 * parts to create when the import will be executed for real.
 *
 * Instances of this class are not persisted.
 *
 * @author Laurent Le Van
 *
 * @version 2.5, 29/06/2016
 * @since   V2.5
 */
public class ImportPreview implements Serializable {

    /**
     * Part revisions which will be checked out
     */
    private List<PartRevision> partRevsToCheckout = new ArrayList<>();

    /**
     * Part revisions which will be created
     */
    private List<PartMaster> partsToCreate = new ArrayList<>();


    public ImportPreview() {
    }

    public ImportPreview(List<PartRevision> partRevsToCheckout, List<PartMaster> partsToCreate) {
        this.partRevsToCheckout.addAll(partRevsToCheckout);
        this.partsToCreate.addAll(partsToCreate);
    }

    public List<PartRevision> getPartRevsToCheckout() {
        return partRevsToCheckout;
    }

    public void setPartRevsToCheckout(List<PartRevision> partRevsToCheckout) {
        this.partRevsToCheckout = partRevsToCheckout;
    }

    public List<PartMaster> getPartsToCreate() {
        return partsToCreate;
    }

    public void setPartsToCreate(List<PartMaster> partsToCreate) {
        this.partsToCreate = partsToCreate;
    }
}
