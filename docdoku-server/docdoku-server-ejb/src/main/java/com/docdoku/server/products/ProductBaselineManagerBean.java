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
import com.docdoku.core.common.Workspace;
import com.docdoku.core.configuration.*;
import com.docdoku.core.document.DocumentIteration;
import com.docdoku.core.document.DocumentLink;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.product.*;
import com.docdoku.core.security.ACL;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.IProductBaselineManagerLocal;
import com.docdoku.core.services.IProductBaselineManagerWS;
import com.docdoku.core.services.IProductManagerLocal;
import com.docdoku.core.services.IUserManagerLocal;
import com.docdoku.server.configuration.PSFilterVisitor;
import com.docdoku.server.configuration.filter.LatestPSFilter;
import com.docdoku.server.configuration.filter.ReleasedPSFilter;
import com.docdoku.server.configuration.spec.ProductBaselineConfigSpec;
import com.docdoku.server.configuration.spec.ProductBaselineCreationConfigSpec;
import com.docdoku.server.dao.*;
import com.docdoku.server.factory.ACLFactory;

import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.jws.WebService;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.*;

@DeclareRoles({UserGroupMapping.REGULAR_USER_ROLE_ID, UserGroupMapping.ADMIN_ROLE_ID, UserGroupMapping.GUEST_PROXY_ROLE_ID})
@Local(IProductBaselineManagerLocal.class)
@Stateless(name = "ProductBaselineManagerBean")
@WebService(endpointInterface = "com.docdoku.core.services.IProductBaselineManagerWS")
public class ProductBaselineManagerBean implements IProductBaselineManagerLocal,IProductBaselineManagerWS {

    @PersistenceContext
    private EntityManager em;
    @EJB
    private IUserManagerLocal userManager;
    @EJB
    private IProductManagerLocal productManager;

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public ProductBaseline createBaseline(ConfigurationItemKey ciKey, String name, ProductBaseline.BaselineType pType, String description, List<PartIterationKey> partIterationKeys, List<String> substituteLinks, List<String> optionalUsageLinks) throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException, ConfigurationItemNotFoundException, PartRevisionNotReleasedException, PartIterationNotFoundException, UserNotActiveException, NotAllowedException, EntityConstraintException, PartMasterNotFoundException, CreationException {

        User user = userManager.checkWorkspaceWriteAccess(ciKey.getWorkspace());
        Locale locale = new Locale(user.getLanguage());

        ConfigurationItemDAO configurationItemDAO = new ConfigurationItemDAO(locale, em);
        ConfigurationItem configurationItem = configurationItemDAO.loadConfigurationItem(ciKey);

        List<PartIteration> partIterations = new ArrayList<>();
        for(PartIterationKey piKey: partIterationKeys){
            partIterations.add(em.find(PartIteration.class,piKey));
        }

        ProductBaselineCreationConfigSpec filter = new ProductBaselineCreationConfigSpec(user,pType,partIterations,substituteLinks,optionalUsageLinks);

        new PSFilterVisitor(em, user, filter, configurationItem.getDesignItem(), null, -1) {
            @Override
            public void onIndeterminateVersion(PartMaster partMaster, List<PartIteration> partIterations) throws NotAllowedException{
                throw new NotAllowedException(locale,"NotAllowedException48");
            }

            @Override
            public void onUnresolvedVersion(PartMaster partMaster) throws NotAllowedException {
                throw new NotAllowedException(locale,"NotAllowedException49");
            }

            @Override
            public void onIndeterminatePath(List<PartLink> pCurrentPath, List<PartIteration> pCurrentPathPartIterations) throws NotAllowedException {
                throw new NotAllowedException(locale,"NotAllowedException50");
            }

            @Override
            public void onUnresolvedPath(List<PartLink> pCurrentPath, List<PartIteration> partIterations) throws NotAllowedException {
                throw new NotAllowedException(locale,"NotAllowedException51");
            }

            @Override
            public void onBranchDiscovered(List<PartLink> pCurrentPath, List<PartIteration> copyPartIteration) {
            }

            @Override
            public void onOptionalPath(List<PartLink> partLinks, List<PartIteration> partIterations) {
            }

            @Override
            public void onPathWalk(List<PartLink> path, List<PartMaster> parts) {
            }

        };

        // Visitor has finished, and should have thrown an exception if errors
        ProductBaseline baseline = new ProductBaseline(user,configurationItem, name, pType, description);
        new PartCollectionDAO(em).createPartCollection(baseline.getPartCollection());
        new DocumentCollectionDAO(em).createDocumentCollection(baseline.getDocumentCollection());

        baseline.getPartCollection().setCreationDate(new Date());
        baseline.getPartCollection().setAuthor(user);

        baseline.getDocumentCollection().setCreationDate(new Date());
        baseline.getDocumentCollection().setAuthor(user);


        for(PartIteration partIteration: filter.getRetainedPartIterations() ){
            baseline.addBaselinedPart(partIteration);
            for (DocumentLink docLink :partIteration.getLinkedDocuments()){
                DocumentIteration docI = docLink.getTargetDocument().getLastCheckedInIteration();
                if(docI!=null)
                    baseline.addBaselinedDocument(docI);
            }
        }

        baseline.getSubstituteLinks().addAll(filter.getRetainedSubstituteLinks());
        baseline.getOptionalUsageLinks().addAll(filter.getRetainedOptionalUsageLinks());

        new ProductBaselineDAO(locale, em).createBaseline(baseline);

        return baseline;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public List<ProductBaseline> getAllBaselines(String workspaceId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException {
        userManager.checkWorkspaceReadAccess(workspaceId);
        return new ProductBaselineDAO(em).findBaselines(workspaceId);
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public List<ProductBaseline> getBaselines(ConfigurationItemKey configurationItemKey) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException {
        userManager.checkWorkspaceReadAccess(configurationItemKey.getWorkspace());
        return new ProductBaselineDAO(em).findBaselines(configurationItemKey.getId(), configurationItemKey.getWorkspace());
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public void deleteBaseline(String pWorkspaceId,int baselineId) throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException, BaselineNotFoundException, UserNotActiveException, EntityConstraintException {

        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
        Locale locale = new Locale(user.getLanguage());

        ProductBaselineDAO productBaselineDAO = new ProductBaselineDAO(locale,em);
        ProductBaseline productBaseline = productBaselineDAO.loadBaseline(baselineId);

        userManager.checkWorkspaceWriteAccess(productBaseline.getConfigurationItem().getWorkspaceId());

        // Check for product instances based on this baseline

        ProductInstanceIterationDAO productInstanceIterationDAO = new ProductInstanceIterationDAO(locale,em);

        if(productInstanceIterationDAO.isBaselinedUsed(productBaseline)){
            throw new EntityConstraintException(locale,"EntityConstraintException16");
        }

        productBaselineDAO.deleteBaseline(productBaseline);

    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public ProductBaseline getBaseline(int baselineId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, BaselineNotFoundException {
        ProductBaseline productBaseline = new ProductBaselineDAO(em).loadBaseline(baselineId);
        userManager.checkWorkspaceReadAccess(productBaseline.getConfigurationItem().getWorkspaceId());
        return productBaseline;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public ProductBaseline getBaselineById(int baselineId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException {
        ProductBaselineDAO productBaselineDAO = new ProductBaselineDAO(em);
        ProductBaseline productBaseline = productBaselineDAO.findBaselineById(baselineId);
        Workspace workspace = productBaseline.getConfigurationItem().getWorkspace();
        userManager.checkWorkspaceReadAccess(workspace.getId());
        return productBaseline;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public List<BaselinedPart> getBaselinedPartWithReference(int baselineId, String q, int maxResults) throws BaselineNotFoundException, UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException {
        ProductBaselineDAO productBaselineDAO = new ProductBaselineDAO(em);
        ProductBaseline productBaseline = productBaselineDAO.loadBaseline(baselineId);
        User user = userManager.checkWorkspaceReadAccess(productBaseline.getConfigurationItem().getWorkspaceId());
        return new ProductBaselineDAO(new Locale(user.getLanguage()), em).findBaselinedPartWithReferenceLike(productBaseline.getPartCollection().getId(), q, maxResults);
    }


    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public List<PathChoice> getBaselineCreationPathChoices(ConfigurationItemKey ciKey, ProductBaseline.BaselineType type) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ConfigurationItemNotFoundException, PartMasterNotFoundException, NotAllowedException, EntityConstraintException {

        User user = userManager.checkWorkspaceReadAccess(ciKey.getWorkspace());

        ConfigurationItemDAO configurationItemDAO = new ConfigurationItemDAO(new Locale(user.getLanguage()), em);
        ConfigurationItem configurationItem = configurationItemDAO.loadConfigurationItem(ciKey);

        PSFilter filter;

        if(type == null || type.equals(ProductBaseline.BaselineType.RELEASED)){
            filter = new ReleasedPSFilter(user, true);
        }else{
            filter = new LatestPSFilter(user, true);
        }

        List<PathChoice> choices = new ArrayList<>();

        new PSFilterVisitor(em, user, filter, configurationItem.getDesignItem(), null, -1) {

            @Override
            public void onIndeterminateVersion(PartMaster partMaster, List<PartIteration> partIterations)  throws NotAllowedException{
            }

            @Override
            public void onIndeterminatePath(List<PartLink> pCurrentPath, List<PartIteration> pCurrentPathPartIterations) {
                List<PartRevision> partRevisions = new ArrayList<>();
                for (PartIteration partIteration : pCurrentPathPartIterations) {
                    partRevisions.add(partIteration.getPartRevision());
                }
                choices.add(new PathChoice(partRevisions, pCurrentPath));
            }

            @Override
            public void onUnresolvedPath(List<PartLink> pCurrentPath, List<PartIteration> partIterations) throws NotAllowedException {

            }

            @Override
            public void onBranchDiscovered(List<PartLink> pCurrentPath, List<PartIteration> copyPartIteration) {
            }

            @Override
            public void onOptionalPath(List<PartLink> pCurrentPath, List<PartIteration> pCurrentPathPartIterations) {
                List<PartRevision> partRevisions = new ArrayList<>();
                for (PartIteration partIteration : pCurrentPathPartIterations) {
                    partRevisions.add(partIteration.getPartRevision());
                }
                choices.add(new PathChoice(partRevisions, pCurrentPath));
            }

            @Override
            public void onPathWalk(List<PartLink> path, List<PartMaster> parts) {

            }

            @Override
            public void onUnresolvedVersion(PartMaster partMaster) {
            }
        };

        return choices;
    }


    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public List<PartIteration> getBaselineCreationVersionsChoices(ConfigurationItemKey ciKey) throws ConfigurationItemNotFoundException, UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, PartMasterNotFoundException, NotAllowedException, EntityConstraintException {

        User user = userManager.checkWorkspaceReadAccess(ciKey.getWorkspace());

        ConfigurationItemDAO configurationItemDAO = new ConfigurationItemDAO(new Locale(user.getLanguage()), em);
        ConfigurationItem configurationItem = configurationItemDAO.loadConfigurationItem(ciKey);

        Set<PartIteration> parts = new HashSet<>();

        PSFilter filter = new ReleasedPSFilter(user, true);

        new PSFilterVisitor(em, user, filter, configurationItem.getDesignItem(), null, -1) {
            @Override
            public void onIndeterminateVersion(PartMaster partMaster, List<PartIteration> partIterations) throws NotAllowedException {
                parts.addAll(partIterations);
            }

            @Override
            public void onIndeterminatePath(List<PartLink> pCurrentPath, List<PartIteration> pCurrentPathPartIterations) {
            }

            @Override
            public void onUnresolvedPath(List<PartLink> pCurrentPath, List<PartIteration> partIterations) throws NotAllowedException {

            }

            @Override
            public void onBranchDiscovered(List<PartLink> pCurrentPath, List<PartIteration> copyPartIteration) {
            }

            @Override
            public void onOptionalPath(List<PartLink> partLinks, List<PartIteration> partIterations) {

            }

            @Override
            public void onPathWalk(List<PartLink> path, List<PartMaster> parts) {

            }

            @Override
            public void onUnresolvedVersion(PartMaster partMaster) {

            }
        };

        return new ArrayList<>(parts);
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public PSFilter getBaselinePSFilter(int baselineId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, BaselineNotFoundException {
        ProductBaselineDAO productBaselineDAO = new ProductBaselineDAO(em);
        ProductBaseline productBaseline = productBaselineDAO.loadBaseline(baselineId);
        User user = userManager.checkWorkspaceReadAccess(productBaseline.getConfigurationItem().getWorkspaceId());
        return new ProductBaselineConfigSpec(productBaseline, user);
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public ProductConfiguration createProductConfiguration(ConfigurationItemKey ciKey, String name, String description, List<String> substituteLinks, List<String> optionalUsageLinks, Map<String,ACL.Permission> userEntries, Map<String,ACL.Permission> groupEntries) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ConfigurationItemNotFoundException, CreationException, AccessRightException {
        User user = userManager.checkWorkspaceWriteAccess(ciKey.getWorkspace());

        Locale locale = new Locale(user.getLanguage());
        ProductConfigurationDAO productConfigurationDAO = new ProductConfigurationDAO(locale,em);

        ConfigurationItemDAO configurationItemDAO = new ConfigurationItemDAO(new Locale(user.getLanguage()), em);
        ConfigurationItem configurationItem = configurationItemDAO.loadConfigurationItem(ciKey);

        ProductConfiguration productConfiguration = new ProductConfiguration(user,configurationItem, name,description,null);

        if (!userEntries.isEmpty() || !groupEntries.isEmpty()) {
            ACLFactory aclFactory = new ACLFactory(em);
            ACL acl = aclFactory.createACLFromPermissions(ciKey.getWorkspace(), userEntries, groupEntries);
            productConfiguration.setAcl(acl);
        }
        productConfiguration.setOptionalUsageLinks(new HashSet<>(optionalUsageLinks));
        productConfiguration.setSubstituteLinks(new HashSet<>(substituteLinks));

        productConfigurationDAO.createProductConfiguration(productConfiguration);
        return productConfiguration;
    }
    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public List<ProductConfiguration> getAllProductConfigurations(String workspaceId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException {
        User user = userManager.checkWorkspaceReadAccess(workspaceId);
        Locale locale = new Locale(user.getLanguage());
        ProductConfigurationDAO productConfigurationDAO = new ProductConfigurationDAO(locale,em);
        List<ProductConfiguration> productConfigurations = productConfigurationDAO.getAllProductConfigurations(workspaceId);

        ListIterator<ProductConfiguration> ite = productConfigurations.listIterator();

        while(ite.hasNext()){
            ProductConfiguration next = ite.next();
            try {
                checkProductConfigurationReadAccess(workspaceId, next, user);
            } catch (AccessRightException e) {
                ite.remove();
            }
        }

        return productConfigurations;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public List<ProductConfiguration> getAllProductConfigurationsByConfigurationItemId(ConfigurationItemKey ciKey) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ConfigurationItemNotFoundException {
        User user = userManager.checkWorkspaceReadAccess(ciKey.getWorkspace());
        Locale locale = new Locale(user.getLanguage());

        ProductConfigurationDAO productConfigurationDAO = new ProductConfigurationDAO(locale,em);
        List<ProductConfiguration> productConfigurations = productConfigurationDAO.getAllProductConfigurationsByConfigurationItem(ciKey);

        ListIterator<ProductConfiguration> ite = productConfigurations.listIterator();

        while(ite.hasNext()){
            ProductConfiguration next = ite.next();
            try {
                checkProductConfigurationReadAccess(ciKey.getWorkspace(), next, user);
            } catch (AccessRightException e) {
                ite.remove();
            }
        }

        return productConfigurations;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public ProductConfiguration getProductConfiguration(ConfigurationItemKey ciKey, int productConfigurationId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ProductConfigurationNotFoundException, AccessRightException {
        User user = userManager.checkWorkspaceReadAccess(ciKey.getWorkspace());
        Locale locale = new Locale(user.getLanguage());
        ProductConfigurationDAO productConfigurationDAO = new ProductConfigurationDAO(locale,em);
        ProductConfiguration productConfiguration = productConfigurationDAO.getProductConfiguration(productConfigurationId);

        String workspaceId = productConfiguration.getConfigurationItem().getWorkspace().getId();
        user = userManager.checkWorkspaceReadAccess(workspaceId);
        checkProductConfigurationReadAccess(workspaceId, productConfiguration, user);

        return productConfiguration;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public ProductConfiguration updateProductConfiguration(ConfigurationItemKey ciKey, int productConfigurationId, String name, String description, List<String> substituteLinks, List<String> optionalUsageLinks) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ProductConfigurationNotFoundException, AccessRightException {
        User user = userManager.checkWorkspaceReadAccess(ciKey.getWorkspace());
        Locale locale = new Locale(user.getLanguage());
        ProductConfigurationDAO productConfigurationDAO = new ProductConfigurationDAO(locale,em);
        ProductConfiguration productConfiguration = productConfigurationDAO.getProductConfiguration(productConfigurationId);

        String workspaceId = productConfiguration.getConfigurationItem().getWorkspace().getId();
        user = userManager.checkWorkspaceReadAccess(workspaceId);
        checkProductConfigurationWriteAccess(workspaceId,productConfiguration,user);

        productConfiguration.setName(name);
        productConfiguration.setDescription(description);
        productConfiguration.setSubstituteLinks(new HashSet<>(substituteLinks));
        productConfiguration.setOptionalUsageLinks(new HashSet<>(optionalUsageLinks));

        return null;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public void deleteProductConfiguration(ConfigurationItemKey ciKey, int productConfigurationId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ProductConfigurationNotFoundException, AccessRightException {
        User user = userManager.checkWorkspaceReadAccess(ciKey.getWorkspace());
        Locale locale = new Locale(user.getLanguage());
        ProductConfigurationDAO productConfigurationDAO = new ProductConfigurationDAO(locale,em);
        ProductConfiguration productConfiguration = productConfigurationDAO.getProductConfiguration(productConfigurationId);

        String workspaceId = productConfiguration.getConfigurationItem().getWorkspace().getId();
        user = userManager.checkWorkspaceReadAccess(workspaceId);
        checkProductConfigurationWriteAccess(workspaceId,productConfiguration,user);

        productConfigurationDAO.deleteProductConfiguration(productConfiguration);
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public void updateACLForConfiguration(ConfigurationItemKey ciKey, int productConfigurationId, Map<String, String> userEntries, Map<String, String> groupEntries) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ProductConfigurationNotFoundException, AccessRightException {

        ACLFactory aclFactory = new ACLFactory(em);

        // Check the read access to the workspace
        User user = userManager.checkWorkspaceReadAccess(ciKey.getWorkspace());
        Locale userLocale = new Locale(user.getLanguage());

        ProductConfigurationDAO productConfigurationDAO = new ProductConfigurationDAO(userLocale,em);
        ProductConfiguration productConfiguration = productConfigurationDAO.getProductConfiguration(productConfigurationId);

        String workspaceId = productConfiguration.getConfigurationItem().getWorkspaceId();
        user = userManager.checkWorkspaceReadAccess(workspaceId);

        checkProductConfigurationWriteAccess(workspaceId, productConfiguration, user);

        if (productConfiguration.getAcl() == null) {
            ACL acl = aclFactory.createACL(workspaceId, userEntries, groupEntries);
            productConfiguration.setAcl(acl);
        } else {
            aclFactory.updateACL(workspaceId, productConfiguration.getAcl(), userEntries, groupEntries);
        }
    }

    private User checkProductConfigurationWriteAccess(String workspaceId, ProductConfiguration productConfiguration, User user) throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException {
        if (user.isAdministrator()) {
            // Check if it is the workspace's administrator
            return user;
        }
        if (productConfiguration.getAcl() == null) {
            // Check if the item haven't ACL
            return userManager.checkWorkspaceWriteAccess(workspaceId);
        } else if (productConfiguration.getAcl().hasWriteAccess(user)) {
            // Check if there is a write access
            return user;
        } else {
            // Else throw a AccessRightException
            throw new AccessRightException(new Locale(user.getLanguage()), user);
        }
    }



    private User checkProductConfigurationReadAccess(String workspaceId, ProductConfiguration productConfiguration, User user) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, AccessRightException {
        if (user.isAdministrator()) {
            // Check if it is the workspace's administrator
            return user;
        }
        if (productConfiguration.getAcl() == null) {
            // Check if the item haven't ACL
            return userManager.checkWorkspaceReadAccess(workspaceId);
        } else if (productConfiguration.getAcl().hasReadAccess(user)) {
            // Check if there is a write access
            return user;
        } else {
            // Else throw a AccessRightException
            throw new AccessRightException(new Locale(user.getLanguage()), user);
        }
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public void removeACLFromConfiguration(ConfigurationItemKey ciKey, int productConfigurationId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ProductConfigurationNotFoundException, AccessRightException {

        User user = userManager.checkWorkspaceReadAccess(ciKey.getWorkspace());
        Locale userLocale = new Locale(user.getLanguage());

        ProductConfigurationDAO productConfigurationDAO = new ProductConfigurationDAO(userLocale,em);
        ProductConfiguration productConfiguration = productConfigurationDAO.getProductConfiguration(productConfigurationId);

        String workspaceId = productConfiguration.getConfigurationItem().getWorkspaceId();
        user = userManager.checkWorkspaceReadAccess(workspaceId);

        checkProductConfigurationWriteAccess(workspaceId,productConfiguration,user);

        ACL acl = productConfiguration.getAcl();
        if (acl != null) {
            new ACLDAO(em).removeACLEntries(acl);
            productConfiguration.setAcl(null);
        }

    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public List<PartRevision> getObsoletePartRevisionsInBaseline(String workspaceId, int baselineId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, BaselineNotFoundException {
        User user = userManager.checkWorkspaceReadAccess(workspaceId);
        Locale locale = new Locale(user.getLanguage());
        ProductBaselineDAO productBaselineDAO = new ProductBaselineDAO(locale, em);
        ProductBaseline baseline = productBaselineDAO.loadBaseline(baselineId);
        List<PartRevision> obsoletePartsInBaseline = productBaselineDAO.findObsoletePartsInBaseline(workspaceId, baseline);
        return obsoletePartsInBaseline;
    }

}