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

package com.docdoku.server;

import com.docdoku.core.exceptions.CreationException;
import com.docdoku.core.exceptions.EffectivityAlreadyExistsException;
import com.docdoku.core.exceptions.EffectivityNotFoundException;
import com.docdoku.core.exceptions.UpdateException;
import com.docdoku.core.product.*;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.IAccountManagerLocal;
import com.docdoku.core.services.IEffectivityManagerLocal;
import com.docdoku.server.dao.EffectivityDAO;
import com.docdoku.server.dao.PartRevisionDAO;

import javax.annotation.security.DeclareRoles;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Date;
import java.util.Locale;
import java.util.Set;

@DeclareRoles({UserGroupMapping.GUEST_ROLE_ID, UserGroupMapping.REGULAR_USER_ROLE_ID, UserGroupMapping.ADMIN_ROLE_ID})
@Local(IEffectivityManagerLocal.class)
@Stateless(name = "EffectivityManagerBean")
public class EffectivityManagerBean implements IEffectivityManagerLocal {

    @PersistenceContext
    private EntityManager em;

    @Inject
    private IAccountManagerLocal accountManager;

    @Override
    public SerialNumberBasedEffectivity createSerialNumberBasedEffectivity(
            PartRevision pPartRevision, String pName, String pDescription, ConfigurationItem pConfigurationItem, String pStartNumber, String pEndNumber)
            throws EffectivityAlreadyExistsException, CreationException {
        EffectivityDAO effectivityDAO = new EffectivityDAO(em);
        SerialNumberBasedEffectivity serialNumberBasedEffectivity = new SerialNumberBasedEffectivity();
        serialNumberBasedEffectivity.setName(pName);
        serialNumberBasedEffectivity.setDescription(pDescription);
        serialNumberBasedEffectivity.setConfigurationItem(pConfigurationItem);
        serialNumberBasedEffectivity.setStartNumber(pStartNumber);
        serialNumberBasedEffectivity.setEndNumber(pEndNumber);
        try {
            if (pStartNumber.isEmpty() || pEndNumber.isEmpty() || pConfigurationItem.getId() == null) {
                throw new CreationException(Locale.getDefault());
            }
        } catch (NullPointerException npe) {
            throw new CreationException(Locale.getDefault());
        }

        effectivityDAO.createEffectivity(serialNumberBasedEffectivity);

        Set<Effectivity> effectivities = pPartRevision.getEffectivities();
        effectivities.add(serialNumberBasedEffectivity);
        pPartRevision.setEffectivities(effectivities);
        PartRevisionDAO partRevisionDAO = new PartRevisionDAO(em);
        partRevisionDAO.updateRevision(pPartRevision);
        return serialNumberBasedEffectivity;
    }

    @Override
    public DateBasedEffectivity createDateBasedEffectivity(
            PartRevision pPartRevision, String pName, String pDescription, Date pStartDate, Date pEndDate)
            throws EffectivityAlreadyExistsException, CreationException {
        EffectivityDAO effectivityDAO = new EffectivityDAO(em);
        DateBasedEffectivity dateBasedEffectivity = new DateBasedEffectivity();
        dateBasedEffectivity.setName(pName);
        dateBasedEffectivity.setDescription(pDescription);
        dateBasedEffectivity.setStartDate(pStartDate);
        dateBasedEffectivity.setEndDate(pEndDate);
        effectivityDAO.createEffectivity(dateBasedEffectivity);
        try {
            if (pStartDate == null || pEndDate == null) {
                throw new CreationException(Locale.getDefault());
            }
        } catch (NullPointerException npe) {
            throw new CreationException(Locale.getDefault());
        }

        Set<Effectivity> effectivities = pPartRevision.getEffectivities();
        effectivities.add(dateBasedEffectivity);
        pPartRevision.setEffectivities(effectivities);
        PartRevisionDAO partRevisionDAO = new PartRevisionDAO(em);
        partRevisionDAO.updateRevision(pPartRevision);
        return dateBasedEffectivity;
    }

    @Override
    public LotBasedEffectivity createLotBasedEffectivity(
            PartRevision pPartRevision, String pName, String pDescription, ConfigurationItem pConfigurationItem, String pStartLotId, String pEndLotId)
            throws EffectivityAlreadyExistsException, CreationException {
        EffectivityDAO effectivityDAO = new EffectivityDAO(em);
        LotBasedEffectivity lotBasedEffectivity = new LotBasedEffectivity();
        lotBasedEffectivity.setName(pName);
        lotBasedEffectivity.setDescription(pDescription);
        lotBasedEffectivity.setConfigurationItem(pConfigurationItem);
        lotBasedEffectivity.setStartLotId(pStartLotId);
        lotBasedEffectivity.setEndLotId(pEndLotId);
        effectivityDAO.createEffectivity(lotBasedEffectivity);
        try {
            if (pStartLotId.isEmpty() || pEndLotId.isEmpty() || pConfigurationItem.getId() == null) {
                throw new CreationException(Locale.getDefault());
            }
        } catch (NullPointerException npe) {
            throw new CreationException(Locale.getDefault());
        }

        Set<Effectivity> effectivities = pPartRevision.getEffectivities();
        effectivities.add(lotBasedEffectivity);
        pPartRevision.setEffectivities(effectivities);
        PartRevisionDAO partRevisionDAO = new PartRevisionDAO(em);
        partRevisionDAO.updateRevision(pPartRevision);
        return lotBasedEffectivity;
    }

    @Override
    public Effectivity getEffectivity(int pId) throws EffectivityNotFoundException {
        return new EffectivityDAO(em).loadEffectivity(pId);
    }

    @Override
    public Effectivity updateEffectivity(int pId, String pName, String pDescription) throws EffectivityNotFoundException {
        EffectivityDAO effectivityDAO = new EffectivityDAO(em);
        Effectivity effectivity = effectivityDAO.loadEffectivity(pId);
        effectivity.setName(pName);
        effectivity.setDescription(pDescription);
        effectivityDAO.updateEffectivity(effectivity);
        return effectivity;
    }

    @Override
    public SerialNumberBasedEffectivity updateSerialNumberBasedEffectivity(int pId, String pName, String pDescription, String pStartNumber, String pEndNumber) throws EffectivityNotFoundException, UpdateException {
        EffectivityDAO effectivityDAO = new EffectivityDAO(em);
        SerialNumberBasedEffectivity serialNumberBasedEffectivity = (SerialNumberBasedEffectivity) effectivityDAO.loadEffectivity(pId);
        serialNumberBasedEffectivity.setName(pName);
        serialNumberBasedEffectivity.setDescription(pDescription);
        serialNumberBasedEffectivity.setStartNumber(pStartNumber);
        serialNumberBasedEffectivity.setEndNumber(pEndNumber);
        try {
            if (pStartNumber.isEmpty() || pEndNumber.isEmpty()) {
                throw new UpdateException(Locale.getDefault());
            }
        } catch (NullPointerException npe) {
            throw new UpdateException(Locale.getDefault());
        }
        effectivityDAO.updateEffectivity(serialNumberBasedEffectivity);
        return serialNumberBasedEffectivity;
    }

    @Override
    public DateBasedEffectivity updateDateBasedEffectivity(int pId, String pName, String pDescription, Date pStartDate, Date pEndDate) throws EffectivityNotFoundException, UpdateException {
        EffectivityDAO effectivityDAO = new EffectivityDAO(em);
        DateBasedEffectivity dateBasedEffectivity = (DateBasedEffectivity) effectivityDAO.loadEffectivity(pId);
        dateBasedEffectivity.setName(pName);
        dateBasedEffectivity.setDescription(pDescription);
        dateBasedEffectivity.setStartDate(pStartDate);
        dateBasedEffectivity.setEndDate(pEndDate);
        try {
            if (pStartDate == null || pEndDate == null) {
                throw new UpdateException(Locale.getDefault());
            }
        } catch (NullPointerException npe) {
            throw new UpdateException(Locale.getDefault());
        }
        effectivityDAO.updateEffectivity(dateBasedEffectivity);
        return dateBasedEffectivity;
    }

    @Override
    public LotBasedEffectivity updateLotBasedEffectivity(int pId, String pName, String pDescription, String pStartLotId, String pEndLotId) throws EffectivityNotFoundException, UpdateException {
        EffectivityDAO effectivityDAO = new EffectivityDAO(em);
        LotBasedEffectivity lotBasedEffectivity = (LotBasedEffectivity) effectivityDAO.loadEffectivity(pId);
        lotBasedEffectivity.setName(pName);
        lotBasedEffectivity.setDescription(pDescription);
        lotBasedEffectivity.setStartLotId(pStartLotId);
        lotBasedEffectivity.setEndLotId(pEndLotId);
        try {
            if (pStartLotId.isEmpty() || pEndLotId.isEmpty()) {
                throw new UpdateException(Locale.getDefault());
            }
        } catch (NullPointerException npe) {
            throw new UpdateException(Locale.getDefault());
        }
        effectivityDAO.updateEffectivity(lotBasedEffectivity);
        return lotBasedEffectivity;
    }

    @Override
    public void deleteEffectivity(PartRevision pPartRevision, int pId) throws EffectivityNotFoundException {
        EffectivityDAO effectivityDAO = new EffectivityDAO(em);
        Effectivity effectivity = effectivityDAO.loadEffectivity(pId);
        boolean effectivityFind = false;

        Object[] objects = pPartRevision.getEffectivities().toArray();
        for(int i=0; i<objects.length && !effectivityFind; i++) {
            if(((Effectivity)objects[i]).getId() == effectivity.getId()) {
                effectivityFind = true;
                PartRevisionDAO partRevisionDAO = new PartRevisionDAO(em);
                partRevisionDAO.removePartRevisionEffectivity(pPartRevision, ((Effectivity)objects[i]));
            }
        }

        effectivityDAO.removeEffectivity(effectivity);
    }
}
