package com.docdoku.core.services;

import com.docdoku.core.exceptions.CreationException;
import com.docdoku.core.exceptions.EffectivityAlreadyExistsException;
import com.docdoku.core.exceptions.EffectivityNotFoundException;
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

    SerialNumberBasedEffectivity getSerialNumberBasedEffectivity(int pId) throws EffectivityNotFoundException;
    DateBasedEffectivity getDateBasedEffectivity(int pId) throws EffectivityNotFoundException;
    LotBasedEffectivity getLotBasedEffectivity(int pId) throws EffectivityNotFoundException;

    List<SerialNumberBasedEffectivity> getSerialNumberBasedEffectivities(PartRevision pPartRevision);
    List<DateBasedEffectivity> getDateBasedEffectivities(PartRevision pPartRevision);
    List<LotBasedEffectivity> getLotBasedEffectivities(PartRevision pPartRevision);

    List<Effectivity> getEffectivityOfConfigurationItem(String pConfigurationItemId);
    Effectivity updateEffectivity(int pId, String pName, String pDescription) throws EffectivityNotFoundException;
    SerialNumberBasedEffectivity updateSerialNumberBasedEffectivity(int pId, String pName, String pDescription, String pStartNumber, String pEndNumber) throws EffectivityNotFoundException;
    DateBasedEffectivity updateDateBasedEffectivity(int pId, String pName, String pDescription, Date pStartDate, Date pEndDate) throws EffectivityNotFoundException;
    LotBasedEffectivity updateLotBasedEffectivity(int pId, String pName, String pDescription, String pStartLotId, String pEndLotId) throws EffectivityNotFoundException;

    void deleteEffectivity(PartRevision pPartRevision, int pId) throws EffectivityNotFoundException;
}
