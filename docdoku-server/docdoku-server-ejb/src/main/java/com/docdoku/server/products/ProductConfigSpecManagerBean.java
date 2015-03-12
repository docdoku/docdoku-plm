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

package com.docdoku.server.products;

import com.docdoku.core.common.User;
import com.docdoku.core.configuration.*;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.product.*;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.IProductConfigSpecManagerLocal;
import com.docdoku.core.services.IProductConfigSpecManagerWS;
import com.docdoku.core.services.IProductManagerLocal;
import com.docdoku.core.services.IUserManagerLocal;
import com.docdoku.server.dao.ConfigurationItemDAO;
import com.docdoku.server.dao.PartUsageLinkDAO;
import com.docdoku.server.dao.ProductBaselineDAO;
import com.docdoku.server.dao.ProductInstanceMasterDAO;

import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.jws.WebService;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@DeclareRoles({UserGroupMapping.REGULAR_USER_ROLE_ID, UserGroupMapping.ADMIN_ROLE_ID, UserGroupMapping.GUEST_PROXY_ROLE_ID})
@Local(IProductConfigSpecManagerLocal.class)
@Stateless(name = "ProductConfigSpecManagerBean")
@WebService(endpointInterface = "com.docdoku.core.services.IProductConfigSpecManagerWS")
public class ProductConfigSpecManagerBean implements IProductConfigSpecManagerWS, IProductConfigSpecManagerLocal {
    @PersistenceContext
    private EntityManager em;

    @EJB
    private IUserManagerLocal userManager;
    @EJB
    private IProductManagerLocal productManager;

    private static final Logger LOGGER = Logger.getLogger(ProductConfigSpecManagerBean.class.getName());

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public ConfigSpec getLatestConfigSpec(String workspaceId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException{
        User user = userManager.checkWorkspaceReadAccess(workspaceId);
        return new LatestConfigSpec(user);
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public ConfigSpec getLatestReleasedConfigSpec(String workspaceId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException{
        User user = userManager.checkWorkspaceReadAccess(workspaceId);
        return new LatestReleasedConfigSpec(user);
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public ConfigSpec getConfigSpecForBaseline(int baselineId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, BaselineNotFoundException {
        ProductBaselineDAO productBaselineDAO = new ProductBaselineDAO(em);
        ProductBaseline productBaseline = productBaselineDAO.loadBaseline(baselineId);
        User user = userManager.checkWorkspaceReadAccess(productBaseline.getConfigurationItem().getWorkspaceId());
        return new BaselineConfigSpec(productBaseline, user);
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public ConfigSpec getConfigSpecForProductInstance(String workspaceId, String ciId, String serialNumber) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ProductInstanceMasterNotFoundException {
        User user = userManager.checkWorkspaceReadAccess(workspaceId);
        ProductInstanceMasterKey pimk = new ProductInstanceMasterKey(serialNumber,workspaceId,ciId);
        ProductInstanceMaster productIM = new ProductInstanceMasterDAO(em).loadProductInstanceMaster(pimk);
        ProductInstanceIteration productII = productIM.getLastIteration();
        return new ProductInstanceConfigSpec(productII,user);
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public PartUsageLink filterProductStructure(ConfigurationItemKey pKey, ConfigSpec configSpec, Integer partUsageLink, Integer pDepth) throws ConfigurationItemNotFoundException, WorkspaceNotFoundException, NotAllowedException, UserNotFoundException, UserNotActiveException, PartUsageLinkNotFoundException, AccessRightException {
        User user = userManager.checkWorkspaceReadAccess(pKey.getWorkspace());
        Locale userLocal = new Locale(user.getLanguage());
        int depth = (pDepth == null) ? -1 : pDepth;
        PartUsageLink rootUsageLink;

        if (partUsageLink == null || partUsageLink == -1) {                                                             // If no partUsageLink specified, we get the rootPartUsageLink
            rootUsageLink = getRootPartUsageLink(pKey);
        } else {
            rootUsageLink = new PartUsageLinkDAO(userLocal, em).loadPartUsageLink(partUsageLink);
        }

        PartMaster component = rootUsageLink.getComponent();

        if(component.getWorkspaceId().equals(pKey.getWorkspace())){
            if(configSpec != null){
                filterPartConfigSpec(configSpec, component, depth, user);
            }else{
                filterPartConfigSpec(new LatestConfigSpec(user), component, depth, user);
            }

            return rootUsageLink;
        }else{
            throw new AccessRightException(userLocal, user);
        }
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public List<PathChoice> filterPartUsageLinksOnReleased(ConfigurationItemKey ciKey) throws ConfigurationItemNotFoundException, UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException {
        User user = userManager.checkWorkspaceReadAccess(ciKey.getWorkspace());
        List<PathChoice> choices = new ArrayList<>();
        ConfigurationItemDAO configurationItemDAO = new ConfigurationItemDAO(new Locale(user.getLanguage()), em);
        ConfigurationItem configurationItem = configurationItemDAO.loadConfigurationItem(ciKey);

        PartUsageLink productVirtualLink = new PartUsageLink();
        productVirtualLink.setId(-1);
        productVirtualLink.setAmount(1d);
        List<PartLink> partLinks = new ArrayList<>();
        partLinks.add(productVirtualLink);

        filterPartUsageLinksOnReleased(choices, configurationItem.getDesignItem(), new ArrayList<>(), partLinks);

        removeVoidChoices(choices);

        return choices;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public List<PathChoice> filterPartUsageLinksOnLatest(ConfigurationItemKey ciKey) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ConfigurationItemNotFoundException {
        User user = userManager.checkWorkspaceReadAccess(ciKey.getWorkspace());
        List<PathChoice> choices = new ArrayList<>();
        ConfigurationItemDAO configurationItemDAO = new ConfigurationItemDAO(new Locale(user.getLanguage()), em);
        ConfigurationItem configurationItem = configurationItemDAO.loadConfigurationItem(ciKey);

        PartUsageLink rootPartUsageLink = getRootPartUsageLink(ciKey);
        List<PartLink> partLinks = new ArrayList<>();
        partLinks.add(rootPartUsageLink);

        filterPartUsageLinksOnLatest(choices, configurationItem.getDesignItem(), new ArrayList<>(), partLinks);

        removeVoidChoices(choices);

        return choices;
    }


    private void filterPartUsageLinksOnReleased(List<PathChoice> choices, PartMaster root, List<PartRevision> partRevisions, List<PartLink> partUsageLinks){

        List<PartRevision> releasedRevisions = root.getAllReleasedRevisions();

        // Take latest revision if not released,
        if(releasedRevisions.isEmpty()){
            releasedRevisions.add(root.getLastRevision());
        }

        for(PartRevision partRevision : releasedRevisions){
            PartIteration partIteration = partRevision.getLastCheckedInIteration();
            if(partIteration != null){

                List<PartRevision> copyPartRevisions = new ArrayList<>(partRevisions);
                copyPartRevisions.add(partRevision);

                for(PartUsageLink partUsageLink : partIteration.getComponents()){

                    PathChoice choice = new PathChoice(copyPartRevisions,new ArrayList<>(partUsageLinks),partUsageLink);
                    choices.add(choice);

                    List<PartLink> copyPartUsageLinks = new ArrayList<>(partUsageLinks);
                    copyPartUsageLinks.add(partUsageLink);
                    filterPartUsageLinksOnReleased(choices, partUsageLink.getComponent(), copyPartRevisions, copyPartUsageLinks);

                    // Dive into substitutes
                    for(PartSubstituteLink substituteLink:partUsageLink.getSubstitutes()){
                        List<PartLink> copyPartSubstituteLinks = new ArrayList<>(partUsageLinks);
                        copyPartSubstituteLinks.add(substituteLink);
                        filterPartUsageLinksOnReleased(choices, substituteLink.getComponent(), copyPartRevisions, copyPartSubstituteLinks);
                    }

                }


            }
        }

    }

    private void filterPartUsageLinksOnLatest(List<PathChoice> choices, PartMaster root, List<PartRevision> partRevisions, List<PartLink> partUsageLinks){

        PartRevision partRevision = root.getLastRevision();
        PartIteration partIteration = partRevision.getLastCheckedInIteration();

        if(partIteration != null){

            // Copy part masters and push current part master
            List<PartRevision> copyPartRevisions = new ArrayList<>(partRevisions);
            copyPartRevisions.add(partRevision);

            for(PartUsageLink partUsageLink : partIteration.getComponents()){

                PathChoice choice = new PathChoice(copyPartRevisions,new ArrayList<>(partUsageLinks),partUsageLink);
                choices.add(choice);

                List<PartLink> copyPartUsageLinks = new ArrayList<>(partUsageLinks);
                copyPartUsageLinks.add(partUsageLink);
                filterPartUsageLinksOnLatest(choices, partUsageLink.getComponent(),copyPartRevisions,copyPartUsageLinks);

                // Dive into substitutes
                for(PartSubstituteLink substituteLink:partUsageLink.getSubstitutes()){
                    List<PartLink> copyPartSubstituteLinks = new ArrayList<>(partUsageLinks);
                    copyPartSubstituteLinks.add(substituteLink);
                    filterPartUsageLinksOnLatest(choices, substituteLink.getComponent(),copyPartRevisions,copyPartSubstituteLinks);
                }

            }
        }
    }


    private void removeVoidChoices(List<PathChoice> choices) {
        ListIterator<PathChoice> ite = choices.listIterator();
        while(ite.hasNext()){
            PathChoice choice = ite.next();
            if(!choice.getPartUsageLink().isOptional() && choice.getPartUsageLink().getSubstitutes().isEmpty()){
                ite.remove();
            }
        }
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public List<PartIteration> findAllReleasedParts(ConfigurationItemKey ciKey) throws ConfigurationItemNotFoundException, UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException {
        User user = userManager.checkWorkspaceReadAccess(ciKey.getWorkspace());
        List<PartIteration> parts = new ArrayList<>();
        ConfigurationItemDAO configurationItemDAO = new ConfigurationItemDAO(new Locale(user.getLanguage()), em);
        ConfigurationItem configurationItem = configurationItemDAO.loadConfigurationItem(ciKey);
        findAllReleasedPartsRecursive(parts, configurationItem.getDesignItem());
        return parts;
    }

    private void findAllReleasedPartsRecursive(List<PartIteration> partIterations, PartMaster root) {
        List<PartRevision> releasedRevisions = root.getAllReleasedRevisions();

        if(releasedRevisions.isEmpty()){
            releasedRevisions.add(root.getLastRevision());
        }

        for(PartRevision partRevision : releasedRevisions){
            PartIteration partIteration = partRevision.getLastCheckedInIteration();
            if(partIteration != null && !partIterations.contains(partIteration)){
                partIterations.add(partIteration);
                for(PartUsageLink partUsageLink : partIteration.getComponents()){
                    findAllReleasedPartsRecursive(partIterations,partUsageLink.getComponent());
                }
            }
        }

    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public PartUsageLink filterProductStructure(PartUsageLink rootUsageLink, ConfigSpec configSpec, Integer pDepth) throws ConfigurationItemNotFoundException, WorkspaceNotFoundException, NotAllowedException, UserNotFoundException, UserNotActiveException, PartUsageLinkNotFoundException, AccessRightException {
        User user = userManager.checkWorkspaceReadAccess(rootUsageLink.getComponent().getWorkspaceId());
        int depth = (pDepth == null) ? -1 : pDepth;
        PartMaster component = rootUsageLink.getComponent();

        if(configSpec != null){
            filterPartConfigSpec(configSpec, component, depth, user);
        }else{
            filterPartConfigSpec(new LatestConfigSpec(user), component, depth, user);
        }

        return rootUsageLink;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public PartUsageLink getRootPartUsageLink(ConfigurationItemKey pKey) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ConfigurationItemNotFoundException {
        User user = userManager.checkWorkspaceReadAccess(pKey.getWorkspace());
        ConfigurationItem ci = new ConfigurationItemDAO(new Locale(user.getLanguage()), em).loadConfigurationItem(pKey);
        PartUsageLink rootUsageLink;
        rootUsageLink = new PartUsageLink();
        rootUsageLink.setId(-1);
        rootUsageLink.setAmount(1d);
        List<CADInstance> cads = new ArrayList<>();
        CADInstance cad = new CADInstance(0d, 0d, 0d, 0d, 0d, 0d);
        cad.setId(-1);
        cads.add(cad);
        rootUsageLink.setCadInstances(cads);
        rootUsageLink.setComponent(ci.getDesignItem());
        return rootUsageLink;
    }

    private void filterPartsUsageLinksConfigSpec(ConfigSpec configSpec, List<PartUsageLink> partUsageLinks, int pDepth, User user){
        for (PartUsageLink usageLink : partUsageLinks) {
            filterPartConfigSpec(configSpec, usageLink.getComponent(), pDepth, user);

            for (PartSubstituteLink subLink : usageLink.getSubstitutes()) {
                filterPartConfigSpec(configSpec, subLink.getSubstitute(), 0, user);
            }
        }
    }

    private void filterPartConfigSpec(ConfigSpec configSpec, PartMaster partMaster, int pDepth, User user){
        PartIteration partI = configSpec.filterConfigSpec(partMaster);
        int depth = pDepth;
        boolean canRead = false;

        if(partI!=null){
            try {
                // Check PartIteration access
                canRead = productManager.canUserAccess(user, partI.getKey());

                // Filter the childs and childs substitute
                if (canRead && depth != 0) {
                    depth--;
                    filterPartsUsageLinksConfigSpec(configSpec,partI.getComponents(),depth,user);
                }
            } catch (PartRevisionNotFoundException | PartIterationNotFoundException e) {
                LOGGER.log(Level.WARNING,null,e);
            }
        }

        // Check alternative part
        for (PartAlternateLink alternateLink : partMaster.getAlternates()) {
            filterPartConfigSpec(configSpec, alternateLink.getAlternate(), 0, user);
        }

        // Detach PartMaster before serve it
        if(partI!=null){
            detachPartIteration(partMaster,partI,canRead);
        }
    }

    private void detachPartIteration(PartMaster partMaster, PartIteration partI, boolean canRead){
        em.detach(partMaster);
        PartRevision partRevision = partI.getPartRevision();
        if (partMaster.getPartRevisions().size() > 1) {
            partMaster.getPartRevisions().retainAll(Collections.singleton(partRevision));
        }
        if (partRevision != null && partRevision.getNumberOfIterations() > 1) {
            partRevision.getPartIterations().retainAll(Collections.singleton(partI));
        }
        if(!canRead){
            partI.getComponents().clear();
        }
    }
}