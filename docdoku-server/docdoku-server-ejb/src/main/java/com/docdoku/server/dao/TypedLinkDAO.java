package com.docdoku.server.dao;

import com.docdoku.core.configuration.ProductInstanceIteration;
import com.docdoku.core.exceptions.CreationException;
import com.docdoku.core.exceptions.TypedLinkAlreadyExistsException;
import com.docdoku.core.exceptions.TypedLinkNotFoundException;
import com.docdoku.core.product.TypedLink;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import java.util.List;
import java.util.Locale;

/**
 * Created by morgan on 29/04/15.
 */
public class TypedLinkDAO {

    private EntityManager em;
    private Locale mLocale;

    public TypedLinkDAO(Locale pLocale, EntityManager pEM) {
        em = pEM;
        mLocale = pLocale;
    }

    public TypedLinkDAO(EntityManager pEM) {
        em = pEM;
        mLocale = Locale.getDefault();
    }

    public void createTypedLink(TypedLink typedLink) throws CreationException, TypedLinkAlreadyExistsException {

        try {
            //the EntityExistsException is thrown only when flush occurs
            em.persist(typedLink);
            em.flush();
        } catch (EntityExistsException pEEEx) {
            throw new TypedLinkAlreadyExistsException(mLocale, typedLink);
        } catch (PersistenceException pPEx) {
            //EntityExistsException is case sensitive
            //whereas MySQL is not thus PersistenceException could be
            //thrown instead of EntityExistsException
            throw new CreationException(mLocale);
        }
    }

    public TypedLink loadTypedLink(int typedLinkedId) throws TypedLinkNotFoundException {
        TypedLink typedLink = em.find(TypedLink.class,typedLinkedId);
        if(typedLink != null){
            return typedLink;
        }
        throw new TypedLinkNotFoundException(mLocale,typedLinkedId);
    }

    public void removeTypedLink(TypedLink typedLink) {
        em.remove(typedLink);
        em.flush();
    }

    public List<String> getDistinctTypedLinksType(ProductInstanceIteration productInstanceIteration) {
        return em.createNamedQuery("TypedLink.findTypedLinksTypeByProductInstanceIteration",String.class)
                .setParameter("productInstanceIteration",productInstanceIteration)
                .getResultList();
    }
}
