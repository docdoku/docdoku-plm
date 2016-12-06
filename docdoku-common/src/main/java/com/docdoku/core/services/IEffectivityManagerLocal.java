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

package com.docdoku.core.services;

import com.docdoku.core.exceptions.CreationException;
import com.docdoku.core.exceptions.EffectivityAlreadyExistsException;
import com.docdoku.core.exceptions.EffectivityNotFoundException;
import com.docdoku.core.exceptions.UpdateException;
import com.docdoku.core.product.*;

import java.util.Date;

/**
 * @author Frédéric Maury
 */
public interface IEffectivityManagerLocal {

    SerialNumberBasedEffectivity createSerialNumberBasedEffectivity(PartRevision pPartRevision, String pName, String pDescription, ConfigurationItem pConfigurationItem, String pStartNumber, String pEndNumber) throws EffectivityAlreadyExistsException, CreationException;
    DateBasedEffectivity createDateBasedEffectivity(PartRevision pPartRevision, String pName, String pDescription, Date pStartDate, Date pEndDate) throws EffectivityAlreadyExistsException, CreationException;
    LotBasedEffectivity createLotBasedEffectivity(PartRevision pPartRevision, String pName, String pDescription, ConfigurationItem pConfigurationItem, String pStartLotId, String pEndLotId) throws EffectivityAlreadyExistsException, CreationException;

    Effectivity getEffectivity(int pId) throws EffectivityNotFoundException;

    Effectivity updateEffectivity(int pId, String pName, String pDescription) throws EffectivityNotFoundException;
    SerialNumberBasedEffectivity updateSerialNumberBasedEffectivity(int pId, String pName, String pDescription, String pStartNumber, String pEndNumber) throws EffectivityNotFoundException, UpdateException;
    DateBasedEffectivity updateDateBasedEffectivity(int pId, String pName, String pDescription, Date pStartDate, Date pEndDate) throws EffectivityNotFoundException, UpdateException;
    LotBasedEffectivity updateLotBasedEffectivity(int pId, String pName, String pDescription, String pStartLotId, String pEndLotId) throws EffectivityNotFoundException, UpdateException;

    void deleteEffectivity(PartRevision pPartRevision, int pId) throws EffectivityNotFoundException;
}
