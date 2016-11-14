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

    SerialNumberBasedEffectivity createSerialNumberBasedEffectivity(String pName, String pDescription, ConfigurationItem pConfigurationItem, String pStartNumber, String pEndNumber) throws EffectivityAlreadyExistsException, CreationException;
    DateBasedEffectivity createDateBasedEffectivity(String pName, String pDescription, ConfigurationItem pConfigurationItem, Date pStartDate, Date pEndDate) throws EffectivityAlreadyExistsException, CreationException;
    LotBasedEffectivity createLotBasedEffectivity(String pName, String pDescription, ConfigurationItem pConfigurationItem, String pStartLotId, String pEndLotId) throws EffectivityAlreadyExistsException, CreationException;
    Effectivity getEffectivity(int pId) throws EffectivityNotFoundException;
    List<Effectivity> getEffectivities();
    List<Effectivity> getEffectivityOfConfigurationItem(String pConfigurationItemId);
    Effectivity updateEffectivity(int pId, String pName, String pDescription) throws EffectivityNotFoundException;
    SerialNumberBasedEffectivity updateSerialNumberBasedEffectivity(int pId, String pName, String pDescription, String pStartNumber, String pEndNumber) throws EffectivityNotFoundException;
    DateBasedEffectivity updateDateBasedEffectivity(int pId, String pName, String pDescription, Date pStartDate, Date pEndDate) throws EffectivityNotFoundException;
    LotBasedEffectivity updateLotBasedEffectivity(int pId, String pName, String pDescription, String pStartLotId, String pEndLotId) throws EffectivityNotFoundException;
    void deleteEffectivity(int pId) throws EffectivityNotFoundException;

}
