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

package com.docdoku.api.models.utils;

import com.docdoku.api.models.DocumentIterationDTO;
import com.docdoku.api.models.DocumentRevisionDTO;
import com.docdoku.api.models.PartIterationDTO;
import com.docdoku.api.models.PartRevisionDTO;
/**
 * This class helps to get last iteration from documents or parts
 *
 * @Author Morgan Guimard
 */
public class LastIterationHelper {

    /**
     * Get the last iteration of a {@link com.docdoku.core.document.DocumentIteration}
     *
     * @param documentRevision: the document revision to search in
     * @return the last iteration of document revision
     */
    public static DocumentIterationDTO getLastIteration(DocumentRevisionDTO documentRevision){
        int iterations = documentRevision.getDocumentIterations().size();
        return documentRevision.getDocumentIterations().get(iterations - 1);
    }

    /**
     * Get the last iteration of a {@link com.docdoku.core.product.PartIteration}
     *
     *
     * @param partRevision: the part revision to search in
     * @return the last iteration of part revision
     */
    public static PartIterationDTO getLastIteration(PartRevisionDTO partRevision){
        int iterations = partRevision.getPartIterations().size();
        return partRevision.getPartIterations().get(iterations - 1);
    }

}
