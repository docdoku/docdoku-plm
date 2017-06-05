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

import java.util.ArrayList;
import java.util.List;

/**
 * A class to stock dry run result, a preview of the import
 *
 * @author Laurent Le Van
 * @since 29/06/2016
 */
public class ImportPreview {

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
