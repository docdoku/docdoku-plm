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


package com.docdoku.core.configuration;

import com.docdoku.core.product.PartIteration;
import com.docdoku.core.product.PartLink;
import com.docdoku.core.product.PartMaster;

import java.util.List;

/**
 * A product structure filter is used to select for a given {@link PartMaster}s
 * one or more candidate {@link PartIteration}s.
 *
 * It does the equivalent operation for a given {@link PartLink}.
 *
 * Contrary to {@link ProductConfigSpec} the filtering is not strict in the sens that
 * more than one {@link PartIteration} and {@link PartLink} can be returned.
 *
 * @author Morgan Guimard
 */

public interface ProductStructureFilter {

    /**
     * Selects the retained iteration(s) of the specified {@link PartMaster}.
     *
     * @param partMaster the part to filter
     *
     * @return the list of eligible part iterations
     */
    List<PartIteration> filter(PartMaster partMaster);

    /**
     * From a given {@link PartLink} selects one or many
     * effective links to consider. It should be noticed that the link is
     * supplied into the form of a complete path whereas the selected
     * links are returned individually.
     *
     * A frequent implementation is to return the {@link PartLink} itself,
     * hence the latest item of the list.
     *
     * @param path the path to the part link to filter into the form of an ordered
     *             list of {@link PartLink}s from the root of the structure
     *             to the {@link PartLink} itself.
     *
     * @return the list of eligible part links (unitary form).
     */
    List<PartLink> filter(List<PartLink> path);
}