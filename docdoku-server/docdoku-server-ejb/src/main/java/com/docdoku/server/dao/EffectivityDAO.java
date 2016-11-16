package com.docdoku.server.dao;

import com.docdoku.core.exceptions.CreationException;
import com.docdoku.core.exceptions.EffectivityAlreadyExistsException;
import com.docdoku.core.exceptions.EffectivityNotFoundException;
import com.docdoku.core.product.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class EffectivityDAO {
    private EntityManager em;
    private Locale mLocale;

    public EffectivityDAO(Locale pLocale, EntityManager pEM) {
        em=pEM;
        mLocale=pLocale;
    }

    public EffectivityDAO(EntityManager pEM) {
        em=pEM;
        mLocale=Locale.getDefault();
    }

    public List<Effectivity> getListEffectivities(Set<Effectivity> pEffectivitySet) {
        Object[] objects = pEffectivitySet.toArray();
        List<Effectivity> effectivities = new ArrayList<>();
        for(int i=0; i<objects.length; i++) {
            effectivities.add((Effectivity)objects[i]);
        }
        return effectivities;
    }

    public Effectivity loadEffectivity(int pId) throws EffectivityNotFoundException {
        Effectivity effectivity = em.find(Effectivity.class,pId);
        if (effectivity == null) {
            throw new EffectivityNotFoundException(mLocale, String.valueOf(pId));
        } else {
            return effectivity;
        }
    }

    public List<SerialNumberBasedEffectivity> loadSerialNumberBasedEffectivities(PartRevision pPartRevision) {
        List<Effectivity> effectivities = this.getListEffectivities(pPartRevision.getEffectivities());
        List<SerialNumberBasedEffectivity> serialNumberBasedEffectivities = new ArrayList<>();
        for(Effectivity effectivity : effectivities) {
            if(effectivity.getClass().equals(SerialNumberBasedEffectivity.class)) {
                serialNumberBasedEffectivities.add((SerialNumberBasedEffectivity) effectivity);
            }
        }
        return serialNumberBasedEffectivities;
    }

    public List<DateBasedEffectivity> loadDateBasedEffectivities(PartRevision pPartRevision) {
        List<Effectivity> effectivities = this.getListEffectivities(pPartRevision.getEffectivities());
        List<DateBasedEffectivity> dateBasedEffectivities = new ArrayList<>();
        for(Effectivity effectivity : effectivities) {
            if(effectivity.getClass().equals(DateBasedEffectivity.class)) {
                dateBasedEffectivities.add((DateBasedEffectivity) effectivity);
            }
        }
        return dateBasedEffectivities;
    }

    public List<LotBasedEffectivity> loadLotBasedEffectivities(PartRevision pPartRevision) {
        List<Effectivity> effectivities = this.getListEffectivities(pPartRevision.getEffectivities());
        List<LotBasedEffectivity> lotBasedEffectivities = new ArrayList<>();
        for(Effectivity effectivity : effectivities) {
            if(effectivity.getClass().equals(LotBasedEffectivity.class)) {
                lotBasedEffectivities.add((LotBasedEffectivity) effectivity);
            }
        }
        return lotBasedEffectivities;
    }

    public List<Effectivity> findEffectivitiesOfConfigurationItem(String pConfigurationItemId) {
        List<Effectivity> effectivities = null;
        try {
            effectivities = em.createNamedQuery("Effectivity.ofConfigurationItem", Effectivity.class)
                    .setParameter("configurationItemId", pConfigurationItemId).getResultList();
        } catch (NoResultException ex) {
            // null will be returned
        }
        return effectivities;
    }

    public void updateEffectivity(Effectivity effectivity) {
        em.merge(effectivity);
    }

    public void removeEffectivity(Effectivity pEffectivity) {
        em.remove(pEffectivity);
        em.flush();
    }

    public void createEffectivity(Effectivity pEffectivity) throws EffectivityAlreadyExistsException, CreationException {
        try {
            //the EntityExistsException is thrown only when flush occurs
            em.persist(pEffectivity);
            em.flush();
        } catch (EntityExistsException pEEEx) {
            throw new EffectivityAlreadyExistsException(mLocale, pEffectivity);
        } catch (PersistenceException pPEx) {
            //EntityExistsException is case sensitive
            //whereas MySQL is not thus PersistenceException could be
            //thrown instead of EntityExistsException
            throw new CreationException(mLocale);
        }
    }
}
