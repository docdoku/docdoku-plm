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

package com.docdoku.server.dao;

import com.docdoku.core.configuration.ProductBaseline;
import com.docdoku.core.configuration.ProductInstanceIteration;
import com.docdoku.core.exceptions.CreationException;
import com.docdoku.core.exceptions.PathToPathLinkAlreadyExistsException;
import com.docdoku.core.exceptions.PathToPathLinkNotFoundException;
import com.docdoku.core.product.ConfigurationItem;
import com.docdoku.core.product.PartLink;
import com.docdoku.core.product.PartUsageLink;
import com.docdoku.core.product.PathToPathLink;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by morgan on 29/04/15.
 */
public class PathToPathLinkDAO {

    private static final Logger LOGGER = Logger.getLogger(PathToPathLinkDAO.class.getName());

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
            LOGGER.log(Level.FINEST,null,pEEEx);
            throw new PathToPathLinkAlreadyExistsException(mLocale, pathToPathLink);
        } catch (PersistenceException pPEx) {
            LOGGER.log(Level.FINEST,null,pPEx);
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
        return em.createNamedQuery("PathToPathLink.findPathToPathLinkByProductInstanceIteration",PathToPathLink.class)
                .setParameter("productInstanceIteration",productInstanceIteration)
                .getResultList();
    }

    public List<String> getDistinctPathToPathLinkTypes(ProductBaseline productBaseline) {
        return em.createNamedQuery("PathToPathLink.findPathToPathLinkTypesByProductBaseline",String.class)
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

    public List<PathToPathLink> getNextPathToPathLinkInProduct(ConfigurationItem configurationItem, PathToPathLink pathToPathLink){
        return em.createNamedQuery("PathToPathLink.findNextPathToPathLinkInProduct", PathToPathLink.class)
                .setParameter("configurationItem", configurationItem)
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

    public List<PathToPathLink> findRootPathToPathLinks(ProductBaseline productBaseline, String type) {
        return em.createNamedQuery("PathToPathLink.findRootPathToPathLinkForGivenProductBaselineAndType", PathToPathLink.class)
                .setParameter("productBaseline", productBaseline)
                .setParameter("type", type)
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
                .setParameter("type", type)
                .setParameter("source", source)
                .getResultList();
    }

    public List<PathToPathLink> getSourcesPathToPathLinksInBaseline(ProductBaseline productBaseline, String type, String source) {
        return em.createNamedQuery("PathToPathLink.findSourcesPathToPathLinkInProductBaseline", PathToPathLink.class)
                .setParameter("productBaseline", productBaseline)
                .setParameter("type", type)
                .setParameter("source", source)
                .getResultList();
    }

    public List<PathToPathLink> getPathToPathLinksFromPartialPath(String usageLinkId){
        return em.createNamedQuery("PathToPathLink.findLinksWherePartialPathIsPresent", PathToPathLink.class)
                .setParameter("inChain", "%" + usageLinkId + "-%")
                .setParameter("endOfChain", "%" + usageLinkId)
                .getResultList();
    }

    public void removePathToPathLinks(String usageLinkId) {

        List<PathToPathLink> pathToPathLinks = getPathToPathLinksFromPartialPath(usageLinkId);

        for(PathToPathLink pathToPathLink:pathToPathLinks){
            try{
                ConfigurationItem configurationItem = em.createNamedQuery("ConfigurationItem.findByPathToPathLink", ConfigurationItem.class)
                        .setParameter("pathToPathLink", pathToPathLink)
                        .getSingleResult();

                configurationItem.removePathToPathLink(pathToPathLink);
            } catch (NoResultException e){
                // Nothing to remove, continue loop
            }

        }

    }

    private void upgradePathToPathLink(PartLink oldLink, PartLink newLink){

        List<PathToPathLink> oldP2PLinks = getPathToPathLinksFromPartialPath(oldLink.getFullId());
        String oldFullId = oldLink.getFullId();
        String newFullId = newLink.getFullId();

        for(PathToPathLink pathToPathLink:oldP2PLinks){
            pathToPathLink.setSourcePath(upgradePath(pathToPathLink.getSourcePath(), oldFullId, newFullId));
            pathToPathLink.setTargetPath(upgradePath(pathToPathLink.getTargetPath(), oldFullId, newFullId));
        }

        if(oldLink.getSubstitutes() != null){
            int size = oldLink.getSubstitutes().size();
            for(int i = 0; i < size; i++){
                PartLink oldSubstituteLink = oldLink.getSubstitutes().get(i);
                PartLink newSubstituteLink = newLink.getSubstitutes().get(i);
                upgradePathToPathLink(oldSubstituteLink,newSubstituteLink);
            }
        }

    }

    public void cloneAndUpgradePathToPathLinks(List<PartUsageLink> oldComponents, List<PartUsageLink> newComponents) {
        int size = oldComponents.size();
        //keep a track of p2p to create and add it to the configuration item only once
        //at the end of the process in order to not create them twice
        //Map<PathToPathLink,Map<PathToPathLink, ConfigurationItem>> is oldP2P => newP2P => the configuration item into
        //which it has be added
        Map<PathToPathLink,Map<PathToPathLink, ConfigurationItem>> modifiedP2P=new HashMap<>();
        for(int i = 0; i < size; i++){
            PartLink oldLink = oldComponents.get(i);
            PartLink newLink = newComponents.get(i);
            cloneAndUpgradePathToPathLink(oldLink, newLink, modifiedP2P);
        }
        //to the new p2p to their configuration item
        for(Map<PathToPathLink, ConfigurationItem> p2pAndConfigItem:modifiedP2P.values()){
            Map.Entry<PathToPathLink, ConfigurationItem> pair = p2pAndConfigItem.entrySet().iterator().next();
            ConfigurationItem ci = pair.getValue();
            ci.addPathToPathLink(pair.getKey());
        }
    }

    private void cloneAndUpgradePathToPathLink(PartLink oldLink, PartLink newLink, Map<PathToPathLink,Map<PathToPathLink, ConfigurationItem>> modifiedP2P){

        List<PathToPathLink> oldP2PLinks = getPathToPathLinksFromPartialPath(oldLink.getFullId());

        String oldFullId = oldLink.getFullId();
        String newFullId = newLink.getFullId();
        PathToPathLink clone;
        for(PathToPathLink pathToPathLink:oldP2PLinks){
            if(modifiedP2P.get(pathToPathLink) !=null){
                clone = modifiedP2P.get(pathToPathLink).keySet().iterator().next();
            }else{
                clone = new PathToPathLink(pathToPathLink.getType(),pathToPathLink.getSourcePath(),pathToPathLink.getTargetPath(),pathToPathLink.getDescription());
            }
            clone.setSourcePath(upgradePath(clone.getSourcePath(), oldFullId, newFullId));
            clone.setTargetPath(upgradePath(clone.getTargetPath(), oldFullId, newFullId));

            // Add in configuration item list
            try {
                ConfigurationItem configurationItem = em.createNamedQuery("ConfigurationItem.findByPathToPathLink", ConfigurationItem.class)
                        .setParameter("pathToPathLink", pathToPathLink)
                        .getSingleResult();
                Map<PathToPathLink, ConfigurationItem> p2pToAdd = new HashMap<>();
                p2pToAdd.put(clone,configurationItem);
                modifiedP2P.put(pathToPathLink,p2pToAdd);
            }catch(NoResultException e){
                LOGGER.log(Level.FINEST,null,e);
            }
        }

        if(oldLink.getSubstitutes() != null){
            int size = oldLink.getSubstitutes().size();
            for(int i = 0; i < size; i++){
                PartLink oldSubstituteLink = oldLink.getSubstitutes().get(i);
                PartLink newSubstituteLink = newLink.getSubstitutes().get(i);
                cloneAndUpgradePathToPathLink(oldSubstituteLink, newSubstituteLink, modifiedP2P);
            }
        }

    }

    public String upgradePath(String path, String oldFullId, String newFullId) {
        return path.replaceAll("("+oldFullId+")(-|$)", newFullId + "$2");
    }

    public List<PathToPathLink> getPathToPathLinkSourceInContext(ConfigurationItem configurationItem, ProductInstanceIteration productInstanceIteration, String path){
        if(productInstanceIteration != null){
            return em.createNamedQuery("PathToPathLink.isSourceInProductInstanceContext", PathToPathLink.class)
                    .setParameter("productInstanceIteration", productInstanceIteration)
                    .setParameter("path", path)
                    .getResultList();
        }else{
            return em.createNamedQuery("PathToPathLink.isSourceInConfigurationItemContext", PathToPathLink.class)
                    .setParameter("configurationItem", configurationItem)
                    .setParameter("path", path)
                    .getResultList();
        }
    }

    public List<PathToPathLink> getPathToPathLinkTargetInContext(ConfigurationItem configurationItem, ProductInstanceIteration productInstanceIteration, String path){
        if(productInstanceIteration != null){
            return em.createNamedQuery("PathToPathLink.isTargetInProductInstanceContext", PathToPathLink.class)
                    .setParameter("productInstanceIteration", productInstanceIteration)
                    .setParameter("path", path)
                    .getResultList();
        }else{
            return em.createNamedQuery("PathToPathLink.isTargetInConfigurationItemContext", PathToPathLink.class)
                    .setParameter("configurationItem", configurationItem)
                    .setParameter("path", path)
                    .getResultList();
        }
    }

}
