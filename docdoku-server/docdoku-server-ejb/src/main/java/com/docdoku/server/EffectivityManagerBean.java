package com.docdoku.server;

import com.docdoku.core.exceptions.CreationException;
import com.docdoku.core.exceptions.EffectivityAlreadyExistsException;
import com.docdoku.core.exceptions.EffectivityNotFoundException;
import com.docdoku.core.product.*;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.IAccountManagerLocal;
import com.docdoku.core.services.IEffectivityManagerLocal;
import com.docdoku.server.dao.EffectivityDAO;

import javax.annotation.security.DeclareRoles;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Date;
import java.util.List;

@DeclareRoles({UserGroupMapping.GUEST_PROXY_ROLE_ID, UserGroupMapping.REGULAR_USER_ROLE_ID, UserGroupMapping.ADMIN_ROLE_ID})
@Local(IEffectivityManagerLocal.class)
@Stateless(name = "EffectivityManagerBean")
public class EffectivityManagerBean implements IEffectivityManagerLocal {

    @PersistenceContext
    private EntityManager em;

    @Inject
    private IAccountManagerLocal accountManager;

    @Override
    public SerialNumberBasedEffectivity createSerialNumberBasedEffectivity(String pName, String pDescription, ConfigurationItem pConfigurationItem, String pStartNumber, String pEndNumber) throws EffectivityAlreadyExistsException, CreationException {
        EffectivityDAO effectivityDAO = new EffectivityDAO(em);
        SerialNumberBasedEffectivity serialNumberBasedEffectivity = new SerialNumberBasedEffectivity();
        serialNumberBasedEffectivity.setName(pName);
        serialNumberBasedEffectivity.setDescription(pDescription);
        serialNumberBasedEffectivity.setConfigurationItem(pConfigurationItem);
        serialNumberBasedEffectivity.setStartNumber(pStartNumber);
        serialNumberBasedEffectivity.setEndNumber(pEndNumber);
        effectivityDAO.createEffectivity(serialNumberBasedEffectivity);
        return serialNumberBasedEffectivity;
    }

    @Override
    public DateBasedEffectivity createDateBasedEffectivity(String pName, String pDescription, ConfigurationItem pConfigurationItem, Date pStartDate, Date pEndDate) throws EffectivityAlreadyExistsException, CreationException {
        EffectivityDAO effectivityDAO = new EffectivityDAO(em);
        DateBasedEffectivity dateBasedEffectivity = new DateBasedEffectivity();
        dateBasedEffectivity.setName(pName);
        dateBasedEffectivity.setDescription(pDescription);
        dateBasedEffectivity.setConfigurationItem(pConfigurationItem);
        dateBasedEffectivity.setStartDate(pStartDate);
        dateBasedEffectivity.setEndDate(pEndDate);
        effectivityDAO.createEffectivity(dateBasedEffectivity);
        return dateBasedEffectivity;
    }

    @Override
    public LotBasedEffectivity createLotBasedEffectivity(String pName, String pDescription, ConfigurationItem pConfigurationItem, String pStartLotId, String pEndLotId) throws EffectivityAlreadyExistsException, CreationException {
        EffectivityDAO effectivityDAO = new EffectivityDAO(em);
        LotBasedEffectivity lotBasedEffectivity = new LotBasedEffectivity();
        lotBasedEffectivity.setName(pName);
        lotBasedEffectivity.setDescription(pDescription);
        lotBasedEffectivity.setConfigurationItem(pConfigurationItem);
        lotBasedEffectivity.setStartLotId(pStartLotId);
        lotBasedEffectivity.setEndLotId(pEndLotId);
        effectivityDAO.createEffectivity(lotBasedEffectivity);
        return lotBasedEffectivity;
    }

    @Override
    public Effectivity getEffectivity(String pId) throws EffectivityNotFoundException {
        return new EffectivityDAO(em).loadEffectivity(pId);
    }

    @Override
    public List<Effectivity> getEffectivities() {
        return new EffectivityDAO(em).loadEffectivities();
    }

    @Override
    public List<Effectivity> getEffectivityOfConfigurationItem(String pConfigurationItemId) {
        EffectivityDAO effectivityDAO = new EffectivityDAO(em);
        return effectivityDAO.findEffectivitiesOfConfigurationItem(pConfigurationItemId);
    }

    @Override
    public Effectivity updateEffectivity(String pId, String pName, String pDescription) throws EffectivityNotFoundException {
        EffectivityDAO effectivityDAO = new EffectivityDAO(em);
        Effectivity effectivity = effectivityDAO.loadEffectivity(pId);
        effectivity.setName(pName);
        effectivity.setDescription(pDescription);
        effectivityDAO.updateEffectivity(effectivity);
        return effectivity;
    }

    @Override
    public void deleteEffectivity(String pId) throws EffectivityNotFoundException {
        EffectivityDAO effectivityDAO = new EffectivityDAO(em);
        Effectivity effectivity = effectivityDAO.loadEffectivity(pId);
        effectivityDAO.removeEffectivity(effectivity);
    }
}
