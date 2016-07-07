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

import java.util.ArrayList;
import java.util.List;

/**
 * A class to stock dry run result, a preview of the import
 * @author Laurent Le Van
 * @since 29/06/2016
 */
public class ImportPreview {

    /**
     * Part revisions which will be checked out
     */
    private List<PartRevision> partRevsToCheckout;

    public ImportPreview(){
                this.partRevsToCheckout = new ArrayList<>();
    }

    public ImportPreview(List<PartRevision> partRevisions){
        this.partRevsToCheckout = partRevisions;
    }

    public List<PartRevision> getPartRevsToCheckout(){
        return partRevsToCheckout;
    }

    public void setPartRevisions(List<PartRevision> partRevsToCheckout){
        this.partRevsToCheckout = partRevsToCheckout;
    }


}
