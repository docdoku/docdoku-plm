package com.docdoku.server.dao;

import com.docdoku.core.configuration.ProductBaseline;
import com.docdoku.core.configuration.ProductInstanceIteration;
import com.docdoku.core.exceptions.CreationException;
import com.docdoku.core.exceptions.PathToPathLinkAlreadyExistsException;
import com.docdoku.core.exceptions.PathToPathLinkNotFoundException;
import com.docdoku.core.product.ConfigurationItem;
import com.docdoku.core.product.PathToPathLink;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;
import java.util.List;
import java.util.Locale;

/**
 * Created by morgan on 29/04/15.
 */
public class PathToPathLinkDAO {

    private EntityManager em;
    private Locale mLocale;

    public PathToPathLinkDAO(Locale pLocale, EntityManager pEM) {
        em = pEM;
        mLocale = pLocale;
    }

    public PathToPathLinkDAO(EntityManager pEM) {
        em = pEM;
        mLocale = Locale.getDefault();
    }

    public void createPathToPathLink(PathToPathLink pathToPathLink) throws CreationException, PathToPathLinkAlreadyExistsException {

        try {
            //the EntityExistsException is thrown only when flush occurs
            em.persist(pathToPathLink);
            em.flush();
        } catch (EntityExistsException pEEEx) {
            throw new PathToPathLinkAlreadyExistsException(mLocale, pathToPathLink);
        } catch (PersistenceException pPEx) {
            //EntityExistsException is case sensitive
            //whereas MySQL is not thus PersistenceException could be
            //thrown instead of EntityExistsException
            throw new CreationException(mLocale);
        }
    }

    public PathToPathLink loadPathToPathLink(int pathToPathLinkId) throws PathToPathLinkNotFoundException {
        PathToPathLink pathToPathLink = em.find(PathToPathLink.class,pathToPathLinkId);
        if(pathToPathLink != null){
            return pathToPathLink;
        }
        throw new PathToPathLinkNotFoundException(mLocale,pathToPathLinkId);
    }

    public void removePathToPathLink(PathToPathLink pathToPathLink) {
        em.remove(pathToPathLink);
        em.flush();
    }

    public List<String> getDistinctPathToPathLinkTypes(ProductInstanceIteration productInstanceIteration) {
        return em.createNamedQuery("PathToPathLink.findPathToPathLinkTypesByProductInstanceIteration",String.class)
                .setParameter("productInstanceIteration",productInstanceIteration)
                .getResultList();
    }
    public List<PathToPathLink> getDistinctPathToPathLink(ProductInstanceIteration productInstanceIteration) {
        return em.createNamedQuery("PathToPathLink.findPathToPathTypedLinkByProductInstanceIteration",PathToPathLink.class)
                .setParameter("productInstanceIteration",productInstanceIteration)
                .getResultList();
    }

    public List<String> getDistinctPathToPathLinkTypes(ProductBaseline productBaseline) {
        return em.createNamedQuery("PathToPathLink.findPathToPathLinkTypesByProductBaseline",String.class)
                .setParameter("productBaseline",productBaseline)
                .getResultList();
    }

    public List<PathToPathLink> getDistinctPathToPathLinks(ProductBaseline productBaseline) {
        return em.createNamedQuery("PathToPathLink.findPathToPathTypedLinkByProductBaseline",PathToPathLink.class)
                .setParameter("productBaseline",productBaseline)
                .getResultList();
    }

    public List<String> getDistinctPathToPathLinkTypes(ConfigurationItem configurationItem) {
        return em.createNamedQuery("PathToPathLink.findPathToPathLinkTypesByProduct",String.class)
                .setParameter("configurationItem",configurationItem)
                .getResultList();
    }

    public PathToPathLink getSamePathToPathLink(ConfigurationItem configurationItem, PathToPathLink pathToPathLink){
        try {
            return em.createNamedQuery("PathToPathLink.findSamePathToPathLinkInProduct", PathToPathLink.class)
                    .setParameter("configurationItem", configurationItem)
                    .setParameter("targetPath", pathToPathLink.getTargetPath())
                    .setParameter("sourcePath", pathToPathLink.getSourcePath())
                    .setParameter("type", pathToPathLink.getType())
                    .getSingleResult();
        }catch(NoResultException e){
            return null;
        }
    }

    public PathToPathLink getSamePathToPathLink(ProductInstanceIteration productInstanceIteration, PathToPathLink pathToPathLink){
        try {
            return em.createNamedQuery("PathToPathLink.findSamePathToPathLinkInProductInstanceIteration", PathToPathLink.class)
                    .setParameter("productInstanceIteration", productInstanceIteration)
                    .setParameter("targetPath", pathToPathLink.getTargetPath())
                    .setParameter("sourcePath", pathToPathLink.getSourcePath())
                    .setParameter("type", pathToPathLink.getType())
                    .getSingleResult();
        }catch(NoResultException e){
            return null;
        }
    }

    public List<PathToPathLink> getNextPathToPathLinkInProduct(ConfigurationItem configurationItem, PathToPathLink pathToPathLink){
        return em.createNamedQuery("PathToPathLink.findNextPathToPathLinkInProduct", PathToPathLink.class)
                .setParameter("configurationItem", configurationItem)
                .setParameter("targetPath", pathToPathLink.getTargetPath())
                .setParameter("type", pathToPathLink.getType())
                .getResultList();
    }

    public List<PathToPathLink> getNextPathToPathLinkInProductInstance(ProductInstanceIteration productInstanceIteration, PathToPathLink pathToPathLink){

        return em.createNamedQuery("PathToPathLink.findNextPathToPathLinkInProductInstanceIteration", PathToPathLink.class)
                .setParameter("productInstanceIteration", productInstanceIteration)
                .setParameter("targetPath", pathToPathLink.getTargetPath())
                .setParameter("type", pathToPathLink.getType())
                .getResultList();

    }

    public List<PathToPathLink> getPathToPathLinkFromSourceAndTarget(ProductBaseline baseline, String source, String target) {
        return em.createNamedQuery("PathToPathLink.findPathToPathLinkBySourceAndTargetInBaseline", PathToPathLink.class)
                .setParameter("baseline", baseline)
                .setParameter("source",source)
                .setParameter("target", target)
                .getResultList();
    }

    public List<PathToPathLink> getPathToPathLinkFromSourceAndTarget(ProductInstanceIteration productInstanceIteration, String source, String target) {
        return em.createNamedQuery("PathToPathLink.findPathToPathLinkBySourceAndTargetInProductInstance", PathToPathLink.class)
                .setParameter("productInstanceIteration", productInstanceIteration)
                .setParameter("source",source)
                .setParameter("target", target)
                .getResultList();
    }

    public List<PathToPathLink> getPathToPathLinkFromSourceAndTarget(ConfigurationItem configurationItem, String source, String target) {
        return em.createNamedQuery("PathToPathLink.findPathToPathLinkBySourceAndTargetInProduct", PathToPathLink.class)
                .setParameter("configurationItem", configurationItem)
                .setParameter("source",source)
                .setParameter("target", target)
                .getResultList();
    }

    public List<PathToPathLink> findRootPathToPathLinks(ConfigurationItem configurationItem, String type) {
        return em.createNamedQuery("PathToPathLink.findRootPathToPathLinkForGivenProductAndType", PathToPathLink.class)
                .setParameter("configurationItem", configurationItem)
                .setParameter("type", type)
                .getResultList();
    }

    public List<PathToPathLink> findPathToPathLinks(ConfigurationItem configurationItem) {
        return em.createNamedQuery("PathToPathLink.findRootPathToPathLinkForGivenProduct", PathToPathLink.class)
                .setParameter("configurationItem", configurationItem)
                .getResultList();
    }

    public List<PathToPathLink> findRootPathToPathLinks(ProductInstanceIteration productInstanceIteration, String type) {
        return em.createNamedQuery("PathToPathLink.findRootPathToPathLinkForGivenProductInstanceIterationAndType", PathToPathLink.class)
                .setParameter("productInstanceIteration", productInstanceIteration)
                .setParameter("type", type)
                .getResultList();
    }

    public List<PathToPathLink> getPathToPathLinkFromPathList(ConfigurationItem configurationItem, List<String> paths) {
        return em.createNamedQuery("PathToPathLink.findPathToPathLinkByPathListInProduct", PathToPathLink.class)
                .setParameter("configurationItem", configurationItem)
                .setParameter("paths",paths)
                .getResultList();
    }

    public List<PathToPathLink> getSourcesPathToPathLinksInProduct(ConfigurationItem configurationItem, String type, String source) {
        return em.createNamedQuery("PathToPathLink.findSourcesPathToPathLinkInProduct", PathToPathLink.class)
                .setParameter("configurationItem", configurationItem)
                .setParameter("type",type)
                .setParameter("source", source)
                .getResultList();
    }
}
