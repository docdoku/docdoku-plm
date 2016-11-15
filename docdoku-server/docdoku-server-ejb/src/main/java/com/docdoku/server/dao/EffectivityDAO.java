package com.docdoku.server.dao;

import com.docdoku.core.exceptions.CreationException;
import com.docdoku.core.exceptions.EffectivityAlreadyExistsException;
import com.docdoku.core.exceptions.EffectivityNotFoundException;
import com.docdoku.core.product.DateBasedEffectivity;
import com.docdoku.core.product.Effectivity;
import com.docdoku.core.product.LotBasedEffectivity;
import com.docdoku.core.product.SerialNumberBasedEffectivity;

import javax.persistence.*;
import java.util.List;
import java.util.Locale;

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

    public Effectivity loadEffectivity(int pId) throws EffectivityNotFoundException {
        Effectivity effectivity = em.find(Effectivity.class,pId);
        if (effectivity == null) {
            throw new EffectivityNotFoundException(mLocale, String.valueOf(pId));
        } else {
            return effectivity;
        }
    }

    public List<SerialNumberBasedEffectivity> loadSerialNumberBasedEffectivities() {
        TypedQuery<SerialNumberBasedEffectivity> query = em.createQuery("SELECT DISTINCT e FROM SerialNumberBasedEffectivity e", SerialNumberBasedEffectivity.class);
        return query.getResultList();
    }

    public List<DateBasedEffectivity> loadDateBasedEffectivities() {
        TypedQuery<DateBasedEffectivity> query = em.createQuery("SELECT DISTINCT e FROM DateBasedEffectivity e", DateBasedEffectivity.class);
        return query.getResultList();
    }

    public List<LotBasedEffectivity> loadLotBasedEffectivities() {
        TypedQuery<LotBasedEffectivity> query = em.createQuery("SELECT DISTINCT e FROM LotBasedEffectivity e", LotBasedEffectivity.class);
        return query.getResultList();
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
