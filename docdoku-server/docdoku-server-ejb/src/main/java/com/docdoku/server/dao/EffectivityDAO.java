package com.docdoku.server.dao;

import com.docdoku.core.exceptions.CreationException;
import com.docdoku.core.exceptions.EffectivityAlreadyExistsException;
import com.docdoku.core.exceptions.EffectivityNotFoundException;
import com.docdoku.core.product.Effectivity;
import com.docdoku.core.product.PartRevision;

import javax.persistence.*;
import java.util.Locale;

public class EffectivityDAO {
    private EntityManager em;
    private Locale mLocale;

    public EffectivityDAO(EntityManager pEM) {
        em = pEM;
        mLocale = Locale.getDefault();
    }

    public EffectivityDAO(Locale locale, EntityManager pEM) {
        em = pEM;
        mLocale = locale;
    }

    public Effectivity loadEffectivity(int pId) throws EffectivityNotFoundException {
        Effectivity effectivity = em.find(Effectivity.class, pId);
        if (effectivity == null) {
            throw new EffectivityNotFoundException(mLocale, String.valueOf(pId));
        } else {
            return effectivity;
        }
    }

    public void updateEffectivity(Effectivity effectivity) {
        em.merge(effectivity);
        em.flush();
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

    public PartRevision getPartRevisionHolder(int pId) {

        TypedQuery<PartRevision> query =
                em.createNamedQuery("Effectivity.findPartRevisionHolder", PartRevision.class);
        query.setParameter("effectivityId", pId);

        try {
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
}
