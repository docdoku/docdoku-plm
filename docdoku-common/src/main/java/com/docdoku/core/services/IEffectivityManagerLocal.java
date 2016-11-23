package com.docdoku.core.services;

import com.docdoku.core.exceptions.CreationException;
import com.docdoku.core.exceptions.EffectivityAlreadyExistsException;
import com.docdoku.core.exceptions.EffectivityNotFoundException;
import com.docdoku.core.exceptions.UpdateException;
import com.docdoku.core.product.*;

import java.util.Date;
import java.util.List;

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
