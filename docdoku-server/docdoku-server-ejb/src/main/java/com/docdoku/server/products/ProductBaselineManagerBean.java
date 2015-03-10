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
import com.docdoku.core.configuration.BaselineConfigSpec;
import com.docdoku.core.configuration.BaselinedPart;
import com.docdoku.core.configuration.ConfigSpec;
import com.docdoku.core.configuration.ProductBaseline;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.product.*;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.IProductBaselineManagerLocal;
import com.docdoku.core.services.IProductManagerLocal;
import com.docdoku.core.services.IUserManagerLocal;
import com.docdoku.server.dao.ConfigurationItemDAO;
import com.docdoku.server.dao.PartIterationDAO;
import com.docdoku.server.dao.PartMasterDAO;
import com.docdoku.server.dao.ProductBaselineDAO;

import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@DeclareRoles({UserGroupMapping.REGULAR_USER_ROLE_ID, UserGroupMapping.ADMIN_ROLE_ID, UserGroupMapping.GUEST_PROXY_ROLE_ID})
@Local(IProductBaselineManagerLocal.class)
@Stateless(name = "ProductBaselineManagerBean")
public class ProductBaselineManagerBean implements IProductBaselineManagerLocal {
    @PersistenceContext
    private EntityManager em;
    @EJB
    private IUserManagerLocal userManager;
    @EJB
    private IProductManagerLocal productManager;

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public ProductBaseline createBaseline(ConfigurationItemKey configurationItemKey, String name, ProductBaseline.BaselineType pType, String description, List<PartIterationKey> partIterationKeys, List<String> substituteLinks, List<String> optionalUsageLinks) throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException, ConfigurationItemNotFoundException, PartRevisionNotReleasedException, PartIterationNotFoundException, UserNotActiveException, NotAllowedException, EntityConstraintException, PartMasterNotFoundException {

        User user = userManager.checkWorkspaceWriteAccess(configurationItemKey.getWorkspace());
        Locale locale = new Locale(user.getLanguage());

        ConfigurationItem configurationItem = new ConfigurationItemDAO(locale,em).loadConfigurationItem(configurationItemKey);

        ProductBaseline productBaseline = new ProductBaseline(configurationItem, name, pType, description);
        productBaseline.getPartCollection().setCreationDate(new Date());
        productBaseline.getPartCollection().setAuthor(user);
        productBaseline.getSubstituteLinks().addAll(substituteLinks);
        productBaseline.getOptionalUsageLinks().addAll(optionalUsageLinks);

        new ProductBaselineDAO(locale,em).createBaseline(productBaseline);

        switch (pType){

            case RELEASED: createReleasedBaseline(productBaseline, user, partIterationKeys); break;
            case LATEST: createLatestBaseline(productBaseline,user); break;

            // Implement existing configuration baseline

            default:
                throw new IllegalArgumentException();
                // Should not occur
        }

        return productBaseline;
    }

    private void createLatestBaseline(ProductBaseline productBaseline, User user) throws NotAllowedException, UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, PartIterationNotFoundException, PartRevisionNotReleasedException, EntityConstraintException, PartMasterNotFoundException {
        // Get the latest design part for the given product
        ConfigurationItem configurationItem = productBaseline.getConfigurationItem();
        PartRevision lastRevision = configurationItem.getDesignItem().getLastRevision();
        PartIteration baselinedIteration = lastRevision.getLastCheckedInIteration();
        fillLatestBaselineParts(productBaseline, baselinedIteration, user);
        BaselineConfigSpec baselineConfigSpec = new BaselineConfigSpec(productBaseline, user);
        checkCyclicAssembly(configurationItem.getWorkspaceId(),baselinedIteration.getPartRevision().getPartMaster(),baselinedIteration.getComponents(), baselineConfigSpec, new Locale(user.getLanguage()));
    }

    private void fillLatestBaselineParts(ProductBaseline productBaseline, PartIteration pIteration, User user) throws PartRevisionNotReleasedException, UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, PartIterationNotFoundException, NotAllowedException{

        Locale locale = new Locale(user.getLanguage());

        if(pIteration==null){
            throw new NotAllowedException(locale, "NotAllowedException1");
        }

        // Ignore already existing parts (unique part number)
        if(productBaseline.hasBasedLinedPart(pIteration.getWorkspaceId(), pIteration.getPartNumber())) {
            return;
        }

        // Add current
        productBaseline.addBaselinedPart(pIteration);

        // Iterate usage Links - recursive
        for(PartUsageLink partUsageLink : pIteration.getComponents()){
            PartRevision lastRevision = partUsageLink.getComponent().getLastRevision();
            PartIteration iteration = lastRevision.getLastCheckedInIteration();
            fillLatestBaselineParts(productBaseline, iteration, user);
        }

    }

    private void createReleasedBaseline(ProductBaseline productBaseline, User user, List<PartIterationKey> partIterationKeys) throws PartRevisionNotReleasedException, UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, NotAllowedException, PartIterationNotFoundException, EntityConstraintException, PartMasterNotFoundException {

        Locale locale = new Locale(user.getLanguage());

        // Get the latest released design part for the given product and check in overrides
        ConfigurationItem configurationItem = productBaseline.getConfigurationItem();

        PartRevision lastReleasedRevision = configurationItem.getDesignItem().getLastReleasedRevision();

        if(lastReleasedRevision==null){
            throw new PartRevisionNotReleasedException(locale, configurationItem.getDesignItem().getNumber());
        }

        PartIteration partIteration;
        PartIterationKey overridePartIterationKey = findOverride(partIterationKeys, lastReleasedRevision);

        if(overridePartIterationKey != null){
            partIteration = new PartIterationDAO(locale, em).loadPartI(overridePartIterationKey);
        }else{
            partIteration = lastReleasedRevision.getLastCheckedInIteration();
        }

        fillReleasedBaselineParts(productBaseline, partIteration, user, partIterationKeys);

        BaselineConfigSpec baselineConfigSpec = new BaselineConfigSpec(productBaseline, user);
        checkCyclicAssembly(configurationItem.getWorkspaceId(), partIteration.getPartRevision().getPartMaster(), partIteration.getComponents(), baselineConfigSpec, new Locale(user.getLanguage()));

    }


    private void fillReleasedBaselineParts(ProductBaseline productBaseline, PartIteration iteration, User user, List<PartIterationKey> partIterationKeys) throws PartRevisionNotReleasedException, UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, PartIterationNotFoundException, NotAllowedException{

        Locale locale = new Locale(user.getLanguage());

        if(iteration==null){
            throw new NotAllowedException(locale, "NotAllowedException1");
        }

        // Ignore already existing parts
        if(productBaseline.hasBasedLinedPart(iteration.getWorkspaceId(), iteration.getPartNumber())) {
            return;
        }

        // Add current
        productBaseline.addBaselinedPart(iteration);

        // Iterate usage Links - recursive
        for(PartUsageLink partUsageLink : iteration.getComponents()){

            PartRevision lastReleasedRevision = partUsageLink.getComponent().getLastReleasedRevision();

            if(lastReleasedRevision==null){
                throw new PartRevisionNotReleasedException(locale,  partUsageLink.getComponent().getNumber());
            }

            PartIteration partIteration;
            PartIterationKey overridePartIterationKey = findOverride(partIterationKeys, lastReleasedRevision);

            if(overridePartIterationKey != null){
                partIteration = new PartIterationDAO(locale, em).loadPartI(overridePartIterationKey);
                if(!partIteration.getPartRevision().isReleased()){
                    throw new PartRevisionNotReleasedException(locale, partIteration.getPartNumber());
                }
            }else{
                partIteration = lastReleasedRevision.getLastCheckedInIteration();
            }

            fillReleasedBaselineParts(productBaseline, partIteration, user, partIterationKeys);
        }

    }

    private PartIterationKey findOverride(List<PartIterationKey> partIterationKeys, PartRevision lastReleasedRevision) {
        for(PartIterationKey partIterationKey:partIterationKeys){
            if(partIterationKey.getPartMasterNumber().equals(lastReleasedRevision.getPartMasterNumber())){
                return partIterationKey;
            }
        }
        return null;
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
    public void deleteBaseline(int baselineId) throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException, BaselineNotFoundException {
        ProductBaselineDAO productBaselineDAO = new ProductBaselineDAO(em);
        ProductBaseline productBaseline = productBaselineDAO.loadBaseline(baselineId);
        userManager.checkWorkspaceWriteAccess(productBaseline.getConfigurationItem().getWorkspaceId());
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

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID,UserGroupMapping.ADMIN_ROLE_ID})
    @Override
    public void checkCyclicAssembly(String workspaceId, PartMaster root, List<PartUsageLink> usageLinks, ConfigSpec ci, Locale locale) throws EntityConstraintException, PartMasterNotFoundException {

        for(PartUsageLink usageLink:usageLinks){
            if(root.getNumber().equals(usageLink.getComponent().getNumber())){
                throw new EntityConstraintException(locale,"EntityConstraintException12");
            }
            for(PartSubstituteLink substituteLink:usageLink.getSubstitutes()){
                if(root.getNumber().equals(substituteLink.getSubstitute().getNumber())){
                    throw new EntityConstraintException(locale,"EntityConstraintException12");
                }
            }
        }

        for(PartUsageLink usageLink: usageLinks){
            PartMaster pm = new PartMasterDAO(locale,em).loadPartM(new PartMasterKey(workspaceId,usageLink.getComponent().getNumber()));
            PartIteration partIteration = ci.filterConfigSpec(pm);
            checkCyclicAssembly(workspaceId,root,partIteration.getComponents(),ci, locale);

            for(PartSubstituteLink substituteLink:usageLink.getSubstitutes()){
                PartMaster substitute = new PartMasterDAO(locale,em).loadPartM(new PartMasterKey(workspaceId,substituteLink.getSubstitute().getNumber()));
                PartIteration substituteIteration = ci.filterConfigSpec(substitute);
                checkCyclicAssembly(workspaceId, root, substituteIteration.getComponents(),ci, locale);
            }
        }

    }
}