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

import com.docdoku.core.common.BinaryResource;
import com.docdoku.core.common.User;
import com.docdoku.core.configuration.*;
import com.docdoku.core.document.DocumentIteration;
import com.docdoku.core.document.DocumentRevision;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.meta.InstanceAttribute;
import com.docdoku.core.product.*;
import com.docdoku.core.security.ACL;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.IProductInstanceManagerLocal;
import com.docdoku.core.services.IUserManagerLocal;
import com.docdoku.core.util.NamingConvention;
import com.docdoku.server.LogDocument;
import com.docdoku.server.dao.*;
import com.docdoku.server.factory.ACLFactory;

import javax.annotation.Resource;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@DeclareRoles({UserGroupMapping.REGULAR_USER_ROLE_ID, UserGroupMapping.ADMIN_ROLE_ID, UserGroupMapping.GUEST_PROXY_ROLE_ID})
@Local(IProductInstanceManagerLocal.class)
@Stateless(name = "ProductInstanceManagerBean")
public class ProductInstanceManagerBean implements IProductInstanceManagerLocal {
    @PersistenceContext
    private EntityManager em;
    @EJB
    private IUserManagerLocal userManager;

    @Resource
    private SessionContext ctx;

    private static final Logger LOGGER = Logger.getLogger(ProductInstanceManagerBean.class.getName());

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public List<ProductInstanceMaster> getProductInstanceMasters(String workspaceId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException {
        userManager.checkWorkspaceReadAccess(workspaceId);
        return new ProductInstanceMasterDAO(em).findProductInstanceMasters(workspaceId);
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public List<ProductInstanceMaster> getProductInstanceMasters(ConfigurationItemKey configurationItemKey) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException {
        userManager.checkWorkspaceReadAccess(configurationItemKey.getWorkspace());
        return new ProductInstanceMasterDAO(em).findProductInstanceMasters(configurationItemKey.getId(), configurationItemKey.getWorkspace());
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public ProductInstanceMaster getProductInstanceMaster(ProductInstanceMasterKey productInstanceMasterKey) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ProductInstanceMasterNotFoundException {
        User user = userManager.checkWorkspaceReadAccess(productInstanceMasterKey.getInstanceOf().getWorkspace());
        Locale userLocal = new Locale(user.getLanguage());
        return new ProductInstanceMasterDAO(userLocal, em).loadProductInstanceMaster(productInstanceMasterKey);
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public List<ProductInstanceIteration> getProductInstanceIterations(ProductInstanceMasterKey productInstanceMasterKey) throws ProductInstanceMasterNotFoundException, UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException {
        User user = userManager.checkWorkspaceReadAccess(productInstanceMasterKey.getInstanceOf().getWorkspace());
        Locale userLocal = new Locale(user.getLanguage());
        ProductInstanceMaster productInstanceMaster = new ProductInstanceMasterDAO(userLocal, em).loadProductInstanceMaster(productInstanceMasterKey);
        return productInstanceMaster.getProductInstanceIterations();
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public ProductInstanceIteration getProductInstanceIteration(ProductInstanceIterationKey productInstanceIterationKey) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ProductInstanceIterationNotFoundException, ProductInstanceMasterNotFoundException {
        User user = userManager.checkWorkspaceReadAccess(productInstanceIterationKey.getProductInstanceMaster().getInstanceOf().getWorkspace());
        Locale userLocal = new Locale(user.getLanguage());
        return new ProductInstanceIterationDAO(userLocal, em).loadProductInstanceIteration(productInstanceIterationKey);
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public List<BaselinedPart> getProductInstanceIterationBaselinedPart(ProductInstanceIterationKey productInstanceIterationKey) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ProductInstanceIterationNotFoundException, ProductInstanceMasterNotFoundException {
        User user = userManager.checkWorkspaceReadAccess(productInstanceIterationKey.getProductInstanceMaster().getInstanceOf().getWorkspace());
        Locale userLocal = new Locale(user.getLanguage());
        ProductInstanceIteration productInstanceIteration = new ProductInstanceIterationDAO(userLocal, em).loadProductInstanceIteration(productInstanceIterationKey);
        return new ArrayList<>(productInstanceIteration.getPartCollection().getBaselinedParts().values());
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public List<BaselinedPart> getProductInstanceIterationPartWithReference(ProductInstanceIterationKey productInstanceIterationKey, String q, int maxResults) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ProductInstanceIterationNotFoundException, ProductInstanceMasterNotFoundException {
        User user = userManager.checkWorkspaceReadAccess(productInstanceIterationKey.getProductInstanceMaster().getInstanceOf().getWorkspace());
        ProductInstanceIterationDAO productInstanceIterationDAO = new ProductInstanceIterationDAO(new Locale(user.getLanguage()), em);
        ProductInstanceIteration productInstanceIteration = productInstanceIterationDAO.loadProductInstanceIteration(productInstanceIterationKey);
        return productInstanceIterationDAO.findBaselinedPartWithReferenceLike(productInstanceIteration.getPartCollection().getId(), q, maxResults);
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public ProductInstanceMaster createProductInstance(String workspaceId, ConfigurationItemKey configurationItemKey, String serialNumber, int baselineId, Map<String, ACL.Permission> userEntries, Map<String, ACL.Permission> groupEntries, List<InstanceAttribute> attributes) throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException, ConfigurationItemNotFoundException, BaselineNotFoundException, CreationException, ProductInstanceAlreadyExistsException {
        User user = userManager.checkWorkspaceWriteAccess(configurationItemKey.getWorkspace());
        Locale userLocal = new Locale(user.getLanguage());
        ProductInstanceMasterDAO productInstanceMasterDAO = new ProductInstanceMasterDAO(userLocal, em);

        try {// Check if ths product instance already exist
            ProductInstanceMaster productInstanceMaster = productInstanceMasterDAO.loadProductInstanceMaster(new ProductInstanceMasterKey(serialNumber, configurationItemKey.getWorkspace(), configurationItemKey.getId()));
            throw new ProductInstanceAlreadyExistsException(userLocal, productInstanceMaster);
        } catch (ProductInstanceMasterNotFoundException e) {
            LOGGER.log(Level.FINEST, null, e);
        }

        ConfigurationItem configurationItem = new ConfigurationItemDAO(em).loadConfigurationItem(configurationItemKey);
        ProductInstanceMaster productInstanceMaster = new ProductInstanceMaster(configurationItem, serialNumber);

        ACLFactory aclFactory = new ACLFactory(em);
        ACL acl = aclFactory.createACLFromPermissions(workspaceId, userEntries, groupEntries);
        productInstanceMaster.setAcl(acl);

        ProductInstanceIteration productInstanceIteration = productInstanceMaster.createNextIteration();
        productInstanceIteration.setIterationNote("Initial");

        PartCollection partCollection = new PartCollection();
        new PartCollectionDAO(em).createPartCollection(partCollection);
        partCollection.setAuthor(user);
        partCollection.setCreationDate(new Date());

        ProductBaseline productBaseline = new ProductBaselineDAO(em).loadBaseline(baselineId);
        productInstanceIteration.setBasedOn(productBaseline);
        productInstanceIteration.setSubstituteLinks(new HashSet<>(productBaseline.getSubstituteLinks()));
        productInstanceIteration.setOptionalUsageLinks(new HashSet<>(productBaseline.getOptionalUsageLinks()));

        productInstanceMasterDAO.createProductInstanceMaster(productInstanceMaster);


        for (BaselinedPart baselinedPart : productBaseline.getBaselinedParts().values()) {
            partCollection.addBaselinedPart(baselinedPart.getTargetPart());
        }
        productInstanceIteration.setPartCollection(partCollection);

        productInstanceIteration.setInstanceAttributes(attributes);
        return productInstanceMaster;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public ProductInstanceIteration updateProductInstance(ConfigurationItemKey configurationItemKey, String serialNumber, String iterationNote, List<PartIterationKey> partIterationKeys, List<InstanceAttribute> attributes) throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException, ProductInstanceMasterNotFoundException, PartIterationNotFoundException {
        User user = userManager.checkWorkspaceWriteAccess(configurationItemKey.getWorkspace());
        Locale userLocal = new Locale(user.getLanguage());
        ProductInstanceMasterDAO productInstanceMasterDAO = new ProductInstanceMasterDAO(userLocal, em);
        ProductInstanceMaster productInstanceMaster = productInstanceMasterDAO.loadProductInstanceMaster(new ProductInstanceMasterKey(serialNumber, configurationItemKey.getWorkspace(), configurationItemKey.getId()));

        ProductInstanceIteration lastIteration = productInstanceMaster.getLastIteration();

        ProductInstanceIteration productInstanceIteration = productInstanceMaster.createNextIteration();
        new ProductInstanceIterationDAO(userLocal, em).createProductInstanceIteration(productInstanceIteration);

        productInstanceIteration.setIterationNote(iterationNote);
        PartCollection partCollection = new PartCollection();
        new PartCollectionDAO(em).createPartCollection(partCollection);

        partCollection.setAuthor(user);
        partCollection.setCreationDate(new Date());
        for (PartIterationKey partIterationKey : partIterationKeys) {
            partCollection.addBaselinedPart(new PartIterationDAO(userLocal, em).loadPartI(partIterationKey));
        }
        productInstanceIteration.setPartCollection(partCollection);

        // TODO : use params instead of recopy.
        productInstanceIteration.setBasedOn(lastIteration.getBasedOn());
        productInstanceIteration.setSubstituteLinks(new HashSet<>(lastIteration.getSubstituteLinks()));
        productInstanceIteration.setOptionalUsageLinks(new HashSet<>(lastIteration.getOptionalUsageLinks()));

        productInstanceIteration.setInstanceAttributes(attributes);
        return productInstanceIteration;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public void deleteProductInstance(String workspaceId, String configurationItemId, String serialNumber) throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException, UserNotActiveException, ProductInstanceMasterNotFoundException {
        User user = userManager.checkWorkspaceWriteAccess(workspaceId);
        Locale userLocal = new Locale(user.getLanguage());
        ProductInstanceMasterDAO productInstanceMasterDAO = new ProductInstanceMasterDAO(userLocal, em);
        ProductInstanceMaster prodInstM = productInstanceMasterDAO.loadProductInstanceMaster(new ProductInstanceMasterKey(serialNumber, workspaceId, configurationItemId));
        productInstanceMasterDAO.deleteProductInstanceMaster(prodInstM);
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public void updateACLForProductInstanceMaster(String workspaceId, String configurationItemId, String serialNumber, Map<String, String> userEntries, Map<String, String> groupEntries) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ProductInstanceMasterNotFoundException, AccessRightException {

        ACLFactory aclFactory = new ACLFactory(em);

        // Check the read access to the workspace
        User user = userManager.checkWorkspaceReadAccess(workspaceId);
        Locale userLocal = new Locale(user.getLanguage());
        // Load the product instance
        ProductInstanceMasterDAO productInstanceMasterDAO = new ProductInstanceMasterDAO(userLocal, em);
        ProductInstanceMaster prodInstM = productInstanceMasterDAO.loadProductInstanceMaster(new ProductInstanceMasterKey(serialNumber, workspaceId, configurationItemId));


        // Check the access to the part template
        checkProductInstanceWriteAccess(workspaceId, prodInstM, user);

        if (prodInstM.getAcl() == null) {
            ACL acl = aclFactory.createACL(workspaceId, userEntries, groupEntries);
            prodInstM.setAcl(acl);
        } else {
            aclFactory.updateACL(workspaceId, prodInstM.getAcl(), userEntries, groupEntries);
        }
    }

    private User checkProductInstanceWriteAccess(String workspaceId, ProductInstanceMaster prodInstM, User user) throws AccessRightException, WorkspaceNotFoundException, UserNotFoundException {
        if (user.isAdministrator()) {
            // Check if it is the workspace's administrator
            return user;
        }
        if (prodInstM.getAcl() == null) {
            // Check if the item haven't ACL
            return userManager.checkWorkspaceWriteAccess(workspaceId);
        } else if (prodInstM.getAcl().hasWriteAccess(user)) {
            // Check if there is a write access
            return user;
        } else {
            // Else throw a AccessRightException
            throw new AccessRightException(new Locale(user.getLanguage()), user);
        }
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public void removeACLFromProductInstanceMaster(String workspaceId, String configurationItemId, String serialNumber) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, AccessRightException, ProductInstanceMasterNotFoundException {

        // Check the read access to the workspace
        User user = userManager.checkWorkspaceReadAccess(workspaceId);
        Locale locale = new Locale(user.getLanguage());
        // Load the product instance
        ProductInstanceMasterDAO productInstanceMasterDAO = new ProductInstanceMasterDAO(locale, em);
        ProductInstanceMaster prodInstM = productInstanceMasterDAO.loadProductInstanceMaster(new ProductInstanceMasterKey(serialNumber, workspaceId, configurationItemId));


        // Check the access to the product instance
        checkProductInstanceWriteAccess(workspaceId, prodInstM, user);

        ACL acl = prodInstM.getAcl();
        if (acl != null) {
            new ACLDAO(em).removeACLEntries(acl);
            prodInstM.setAcl(null);
        }
    }

    @Override
    public BinaryResource saveFileInProductInstance(String workspaceId,ProductInstanceIterationKey pdtIterationKey, String fileName, int pSize) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, NotAllowedException, ProductInstanceMasterNotFoundException, AccessRightException, ProductInstanceIterationNotFoundException, FileAlreadyExistsException, CreationException {
        // Check the read access to the workspace
        User user = userManager.checkWorkspaceReadAccess(workspaceId);
        Locale locale = new Locale(user.getLanguage());
        checkNameFileValidity(fileName, locale);

        // Load the product instance
        ProductInstanceMasterDAO productInstanceMasterDAO = new ProductInstanceMasterDAO(locale, em);
        ProductInstanceMaster prodInstM = productInstanceMasterDAO.loadProductInstanceMaster(new ProductInstanceMasterKey(pdtIterationKey.getProductInstanceMaster().getSerialNumber(), workspaceId, pdtIterationKey.getProductInstanceMaster().getInstanceOf().getId()));
        // Check the access to the product instance
        checkProductInstanceWriteAccess(workspaceId, prodInstM, user);

        // Load the product instance iteration
        ProductInstanceIteration productInstanceIteration = this.getProductInstanceIteration(pdtIterationKey);


         BinaryResource binaryResource = null;
         String fullName = workspaceId + "/product-instances/"+prodInstM.getSerialNumber()+"/iterations/"+productInstanceIteration.getIteration()+"/"+fileName ;

         for (BinaryResource bin : productInstanceIteration.getAttachedFiles()) {
         if (bin.getFullName().equals(fullName)) {
         binaryResource = bin;
         break;
         }
         }

         if (binaryResource == null) {
         binaryResource = new BinaryResource(fullName, pSize, new Date());
         new BinaryResourceDAO(locale, em).createBinaryResource(binaryResource);
             productInstanceIteration.addFile(binaryResource);
         } else {
         binaryResource.setContentLength(pSize);
         binaryResource.setLastModified(new Date());
         }
         return binaryResource;

    }


    @LogDocument
    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID, UserGroupMapping.GUEST_PROXY_ROLE_ID})
    @Override
    public BinaryResource getBinaryResource(String fullName) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, AccessRightException, FileNotFoundException, NotAllowedException {

        if (ctx.isCallerInRole(UserGroupMapping.GUEST_PROXY_ROLE_ID)) {
            // Don't check access right because it is do before. (Is public or isShared)
            return new BinaryResourceDAO(em).loadBinaryResource(fullName);
        }

        User user = userManager.checkWorkspaceReadAccess(BinaryResource.parseWorkspaceId(fullName));
        Locale userLocale = new Locale(user.getLanguage());
        BinaryResourceDAO binDAO = new BinaryResourceDAO(userLocale, em);
        BinaryResource binaryResource = binDAO.loadBinaryResource(fullName);

        ProductInstanceIteration  productInstanceIteration = binDAO.getProductInstanceIterationOwner(binaryResource);
        if (productInstanceIteration != null) {
            ProductInstanceMaster productInstanceMaster = productInstanceIteration.getProductInstanceMaster();

            if(isACLGrantReadAccess(user,productInstanceMaster)) {
                return binaryResource;
            }else {
                    throw new NotAllowedException(new Locale(user.getLanguage()), "NotAllowedException34");
                }
        } else {
            throw new FileNotFoundException(userLocale, fullName);
        }
    }

    private boolean isACLGrantReadAccess(User user, ProductInstanceMaster productInstanceMaster) {
      return user.isAdministrator() || productInstanceMaster.getAcl().hasReadAccess(user);
    }

    private void checkNameFileValidity(String name, Locale locale) throws NotAllowedException {
        if (!NamingConvention.correctNameFile(name)) {
            throw new NotAllowedException(locale, "NotAllowedException9");
        }
    }
}
