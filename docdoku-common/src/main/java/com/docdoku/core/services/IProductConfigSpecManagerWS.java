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
package com.docdoku.core.services;

import com.docdoku.core.configuration.ConfigSpec;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.product.ConfigurationItemKey;
import com.docdoku.core.product.PartUsageLink;

import javax.jws.WebService;

/**
 * The product confiSpec service which is the entry point for the API related to products configSpec
 * definition and manipulation. The client of these functions must
 * be authenticated and have read or write access rights on the workspace
 * where the operations occur.
 *
 * @author Taylor LABEJOF
 * @version 2.0, 26/09/14
 * @since   V2.0
 */
@WebService
public interface IProductConfigSpecManagerWS {
    ConfigSpec getConfigSpecForBaseline(int baselineId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, BaselineNotFoundException;

    /**
     * Resolves the product structure identified by the supplied
     * <a href="ConfigurationItemKey.html">ConfigurationItemKey</a>.
     * The resolution is made according to the given
     * <a href="ConfigSpec.html">ConfigSpec</a> and starts at the specified
     * part usage link if any.
     *
     * @param ciKey
     * The product structure to resolve
     *
     * @param configSpec
     * The rules for the resolution algorithm
     *
     * @param partUsageLink
     * The part usage link id, if null starts from the root part
     *
     * @param depth
     * The fetch depth
     *
     * @return
     * The resolved product
     *
     * @throws com.docdoku.core.exceptions.ConfigurationItemNotFoundException
     * @throws WorkspaceNotFoundException
     * @throws NotAllowedException
     * @throws UserNotFoundException
     * @throws UserNotActiveException
     */
    PartUsageLink filterProductStructure(ConfigurationItemKey ciKey, ConfigSpec configSpec, Integer partUsageLink, Integer depth) throws ConfigurationItemNotFoundException, WorkspaceNotFoundException, NotAllowedException, UserNotFoundException, UserNotActiveException, PartUsageLinkNotFoundException, AccessRightException;
}
