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
import com.docdoku.core.document.*;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.meta.InstanceAttribute;
import com.docdoku.core.product.*;
import com.docdoku.core.security.ACL;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.IDataManagerLocal;
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
    @EJB
    private IDataManagerLocal dataManager;

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
        Locale userLocale = new Locale(user.getLanguage());
        return new ProductInstanceMasterDAO(userLocale, em).loadProductInstanceMaster(productInstanceMasterKey);
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public List<ProductInstanceIteration> getProductInstanceIterations(ProductInstanceMasterKey productInstanceMasterKey) throws ProductInstanceMasterNotFoundException, UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException {
        User user = userManager.checkWorkspaceReadAccess(productInstanceMasterKey.getInstanceOf().getWorkspace());
        Locale userLocale = new Locale(user.getLanguage());
        ProductInstanceMaster productInstanceMaster = new ProductInstanceMasterDAO(userLocale, em).loadProductInstanceMaster(productInstanceMasterKey);
        return productInstanceMaster.getProductInstanceIterations();
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public ProductInstanceIteration getProductInstanceIteration(ProductInstanceIterationKey productInstanceIterationKey) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ProductInstanceIterationNotFoundException, ProductInstanceMasterNotFoundException {
        User user = userManager.checkWorkspaceReadAccess(productInstanceIterationKey.getProductInstanceMaster().getInstanceOf().getWorkspace());
        Locale userLocale = new Locale(user.getLanguage());
        return new ProductInstanceIterationDAO(userLocale, em).loadProductInstanceIteration(productInstanceIterationKey);
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public List<BaselinedPart> getProductInstanceIterationBaselinedPart(ProductInstanceIterationKey productInstanceIterationKey) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ProductInstanceIterationNotFoundException, ProductInstanceMasterNotFoundException {
        User user = userManager.checkWorkspaceReadAccess(productInstanceIterationKey.getProductInstanceMaster().getInstanceOf().getWorkspace());
        Locale userLocale = new Locale(user.getLanguage());
        ProductInstanceIteration productInstanceIteration = new ProductInstanceIterationDAO(userLocale, em).loadProductInstanceIteration(productInstanceIterationKey);
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
    public ProductInstanceMaster createProductInstance(String workspaceId, ConfigurationItemKey configurationItemKey, String serialNumber, int baselineId, Map<String, ACL.Permission> userEntries, Map<String, ACL.Permission> groupEntries, List<InstanceAttribute> attributes, DocumentIterationKey[] links, String[] documentLinkComments) throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException, ConfigurationItemNotFoundException, BaselineNotFoundException, CreationException, ProductInstanceAlreadyExistsException, NotAllowedException {
        User user = userManager.checkWorkspaceWriteAccess(configurationItemKey.getWorkspace());
        Locale userLocale = new Locale(user.getLanguage());
        ProductInstanceMasterDAO productInstanceMasterDAO = new ProductInstanceMasterDAO(userLocale, em);

        checkNameValidity(serialNumber,userLocale);

        try {// Check if ths product instance already exist
            ProductInstanceMaster productInstanceMaster = productInstanceMasterDAO.loadProductInstanceMaster(new ProductInstanceMasterKey(serialNumber, configurationItemKey.getWorkspace(), configurationItemKey.getId()));
            throw new ProductInstanceAlreadyExistsException(userLocale, productInstanceMaster);
        } catch (ProductInstanceMasterNotFoundException e) {
            LOGGER.log(Level.FINEST, null, e);
        }

        ConfigurationItem configurationItem = new ConfigurationItemDAO(em).loadConfigurationItem(configurationItemKey);
        ProductInstanceMaster productInstanceMaster = new ProductInstanceMaster(configurationItem, serialNumber);

        if (!userEntries.isEmpty() || !groupEntries.isEmpty()) {
            ACLFactory aclFactory = new ACLFactory(em);
            ACL acl = aclFactory.createACLFromPermissions(workspaceId, userEntries, groupEntries);
            productInstanceMaster.setAcl(acl);
        }
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
        DocumentLinkDAO linkDAO = new DocumentLinkDAO(userLocale, em);
        if (links != null) {
            ArrayList<DocumentIterationKey> linkKeys = new ArrayList<>(Arrays.asList(links));
            ArrayList<DocumentIterationKey> currentLinkKeys = new ArrayList<>();

            Set<DocumentLink> currentLinks = new HashSet<>(productInstanceIteration.getLinkedDocuments());

            for (DocumentLink link : currentLinks) {
                productInstanceIteration.getLinkedDocuments().remove(link);
            }

            int counter = 0;
            for (DocumentIterationKey link : linkKeys) {
                DocumentLink newLink = new DocumentLink(em.getReference(DocumentIteration.class, link));
                newLink.setComment(documentLinkComments[counter]);
                linkDAO.createLink(newLink);
                productInstanceIteration.getLinkedDocuments().add(newLink);
                counter++;
            }
        }


        return productInstanceMaster;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public ProductInstanceMaster updateProductInstance(String workspaceId, int iteration, String iterationNote, ConfigurationItemKey configurationItemKey, String serialNumber, int baselineId, List<InstanceAttribute> attributes, DocumentIterationKey[] links, String[] documentLinkComments) throws ProductInstanceMasterNotFoundException, UserNotFoundException, AccessRightException, WorkspaceNotFoundException, ProductInstanceIterationNotFoundException, UserNotActiveException {
        User user = userManager.checkWorkspaceReadAccess(workspaceId);
        Locale userLocale = new Locale(user.getLanguage());
        ProductInstanceMasterDAO productInstanceMasterDAO = new ProductInstanceMasterDAO(userLocale, em);
        ProductInstanceMasterKey pInstanceIterationKey = new ProductInstanceMasterKey(serialNumber, workspaceId, configurationItemKey.getId());
        ProductInstanceMaster productInstanceMaster = productInstanceMasterDAO.loadProductInstanceMaster(pInstanceIterationKey);

        ProductInstanceIteration lastIteration = productInstanceMaster.getLastIteration();

        ProductInstanceIteration productInstanceIteration = productInstanceMaster.getProductInstanceIterations().get(iteration - 1);
        // Check the access to the product instance
        checkProductInstanceWriteAccess(workspaceId, productInstanceMaster, user);

        if (productInstanceIteration != null) {
            productInstanceIteration.setIterationNote(iterationNote);
            productInstanceIteration.setInstanceAttributes(attributes);
            productInstanceIteration.setSubstituteLinks(new HashSet<>(lastIteration.getSubstituteLinks()));
            productInstanceIteration.setOptionalUsageLinks(new HashSet<>(lastIteration.getOptionalUsageLinks()));

            DocumentLinkDAO linkDAO = new DocumentLinkDAO(userLocale, em);
            if (links != null) {
                ArrayList<DocumentIterationKey> linkKeys = new ArrayList<>(Arrays.asList(links));
                ArrayList<DocumentIterationKey> currentLinkKeys = new ArrayList<>();

                Set<DocumentLink> currentLinks = new HashSet<>(productInstanceIteration.getLinkedDocuments());

                for (DocumentLink link : currentLinks) {
                    productInstanceIteration.getLinkedDocuments().remove(link);
                }

                int counter = 0;
                for (DocumentIterationKey link : linkKeys) {
                    DocumentLink newLink = new DocumentLink(em.getReference(DocumentIteration.class, link));
                    newLink.setComment(documentLinkComments[counter]);
                    linkDAO.createLink(newLink);
                    productInstanceIteration.getLinkedDocuments().add(newLink);
                    counter++;
                }
            }

            return productInstanceMaster;

        } else {
            throw new ProductInstanceIterationNotFoundException(userLocale, new ProductInstanceIterationKey(serialNumber, configurationItemKey.getWorkspace(), configurationItemKey.getId(), iteration));
        }

    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public ProductInstanceMaster rebaseProductInstance(String workspaceId, String serialNumber, ConfigurationItemKey configurationItemKey, int baselineId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ProductInstanceMasterNotFoundException, AccessRightException, BaselineNotFoundException, NotAllowedException {
        User user = userManager.checkWorkspaceReadAccess(workspaceId);
        Locale userLocale = new Locale(user.getLanguage());
        ProductInstanceMasterDAO productInstanceMasterDAO = new ProductInstanceMasterDAO(userLocale, em);
        ProductInstanceMasterKey pInstanceIterationKey = new ProductInstanceMasterKey(serialNumber, workspaceId, configurationItemKey.getId());
        ProductInstanceMaster productInstanceMaster = productInstanceMasterDAO.loadProductInstanceMaster(pInstanceIterationKey);

        ProductInstanceIteration lastIteration = productInstanceMaster.getLastIteration();

        // Check the access to the product instance
        checkProductInstanceWriteAccess(workspaceId, productInstanceMaster, user);

        // Load the new baseline
        ProductBaselineDAO productBaselineDAO = new ProductBaselineDAO(userLocale,em);
        ProductBaseline baseline = productBaselineDAO.loadBaseline(baselineId);

        // Check valid parameters
        // Config key should be baseline product's one, same for product instance
        if(baseline.getConfigurationItem().getKey().equals(configurationItemKey)
                && baseline.getConfigurationItem().getKey().equals(productInstanceMaster.getInstanceOf().getKey())){


            // Create a new iteration
            ProductInstanceIteration nextIteration = productInstanceMaster.createNextIteration();
            new ProductInstanceIterationDAO(userLocale,em).createProductInstanceIteration(nextIteration);

            nextIteration.setIterationNote(lastIteration.getIterationNote());

            PartCollection partCollection = new PartCollection();
            new PartCollectionDAO(em).createPartCollection(partCollection);
            partCollection.setAuthor(user);
            partCollection.setCreationDate(new Date());

            for(BaselinedPart baselinedPart : baseline.getBaselinedParts().values()){
                partCollection.addBaselinedPart(baselinedPart.getTargetPart());
            }

            nextIteration.setPartCollection(partCollection);

            nextIteration.setBasedOn(baseline);
            nextIteration.setSubstituteLinks(new HashSet<>(baseline.getSubstituteLinks()));
            nextIteration.setOptionalUsageLinks(new HashSet<>(baseline.getOptionalUsageLinks()));

        }else{
            throw new NotAllowedException(userLocale,"NotAllowedException53");
        }

        return productInstanceMaster;

    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public ProductInstanceMaster removeFileFromProductInstanceIteration(String workspaceId, int iteration, String fullName, ProductInstanceMaster productInstanceMaster) throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException, UserNotActiveException {

        User user = userManager.checkWorkspaceReadAccess(workspaceId);
        Locale userLocale = new Locale(user.getLanguage());

        ProductInstanceIteration productInstanceIteration = productInstanceMaster.getProductInstanceIterations().get(iteration - 1);
        BinaryResourceDAO binDAO = new BinaryResourceDAO(userLocale, em);
        BinaryResource file = binDAO.loadBinaryResource(fullName);
        checkProductInstanceWriteAccess(workspaceId, productInstanceMaster, user);

        try {
            dataManager.deleteData(file);
        } catch (StorageException e) {
            LOGGER.log(Level.INFO, null, e);
        }
        productInstanceIteration.removeFile(file);
        binDAO.removeBinaryResource(file);
        return productInstanceMaster;

    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public BinaryResource renameFileInProductInstance(String pFullName, String pNewName, String serialNumber, String cId, int iteration) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, FileNotFoundException, ProductInstanceMasterNotFoundException, NotAllowedException, AccessRightException, FileAlreadyExistsException, CreationException, StorageException {
        User user = userManager.checkWorkspaceReadAccess(BinaryResource.parseWorkspaceId(pFullName));
        Locale userLocale = new Locale(user.getLanguage());

        BinaryResourceDAO binDAO = new BinaryResourceDAO(new Locale(user.getLanguage()), em);
        BinaryResource file = binDAO.loadBinaryResource(pFullName);
        if (file == null) {
            throw new FileNotFoundException(new Locale(user.getLanguage()), pFullName);
        }
        ProductInstanceMasterDAO productInstanceMasterDAO = new ProductInstanceMasterDAO(userLocale, em);
        ProductInstanceMasterKey pInstanceIterationKey = new ProductInstanceMasterKey(serialNumber, user.getWorkspaceId(), cId);
        ProductInstanceMaster productInstanceMaster = productInstanceMasterDAO.loadProductInstanceMaster(pInstanceIterationKey);
        checkNameFileValidity(pNewName, userLocale);

        if (binDAO.loadBinaryResource(file.getNewFullName(pNewName)) == null) {

            ProductInstanceIteration productInstanceIteration = productInstanceMaster.getProductInstanceIterations().get(iteration - 1);
            //check access rights on product instance
            checkProductInstanceWriteAccess(user.getWorkspaceId(), productInstanceMaster, user);

            dataManager.renameFile(file, pNewName);
            productInstanceIteration.removeFile(file);
            binDAO.removeBinaryResource(file);

            BinaryResource newFile = new BinaryResource(file.getNewFullName(pNewName), file.getContentLength(), file.getLastModified());
            binDAO.createBinaryResource(newFile);
            productInstanceIteration.addFile(newFile);
            return newFile;

        } else {
            throw new FileAlreadyExistsException(userLocale, pNewName);
        }

    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public void deleteProductInstance(String workspaceId, String configurationItemId, String serialNumber) throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException, UserNotActiveException, ProductInstanceMasterNotFoundException {
        User user = userManager.checkWorkspaceReadAccess(workspaceId);
        Locale userLocale = new Locale(user.getLanguage());
        ProductInstanceMasterDAO productInstanceMasterDAO = new ProductInstanceMasterDAO(userLocale, em);
        ProductInstanceMaster prodInstM = productInstanceMasterDAO.loadProductInstanceMaster(new ProductInstanceMasterKey(serialNumber, workspaceId, configurationItemId));
        checkProductInstanceWriteAccess(workspaceId,prodInstM,user);
        productInstanceMasterDAO.deleteProductInstanceMaster(prodInstM);
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public void updateACLForProductInstanceMaster(String workspaceId, String configurationItemId, String serialNumber, Map<String, String> userEntries, Map<String, String> groupEntries) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ProductInstanceMasterNotFoundException, AccessRightException {

        ACLFactory aclFactory = new ACLFactory(em);

        // Check the read access to the workspace
        User user = userManager.checkWorkspaceReadAccess(workspaceId);
        Locale userLocale = new Locale(user.getLanguage());
        // Load the product instance
        ProductInstanceMasterDAO productInstanceMasterDAO = new ProductInstanceMasterDAO(userLocale, em);
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

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID})
    @Override
    public BinaryResource saveFileInProductInstance(String workspaceId, ProductInstanceIterationKey pdtIterationKey, String fileName, int pSize) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, NotAllowedException, ProductInstanceMasterNotFoundException, AccessRightException, ProductInstanceIterationNotFoundException, FileAlreadyExistsException, CreationException {
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
        String fullName = workspaceId + "/product-instances/" + prodInstM.getSerialNumber() + "/iterations/" + productInstanceIteration.getIteration() + "/" + fileName;

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
            // Don't check access right because it is done before. (Is public or isShared)
            return new BinaryResourceDAO(em).loadBinaryResource(fullName);
        }

        User user = userManager.checkWorkspaceReadAccess(BinaryResource.parseWorkspaceId(fullName));
        Locale userLocale = new Locale(user.getLanguage());
        BinaryResourceDAO binDAO = new BinaryResourceDAO(userLocale, em);
        BinaryResource binaryResource = binDAO.loadBinaryResource(fullName);

        ProductInstanceIteration productInstanceIteration = binDAO.getProductInstanceIterationOwner(binaryResource);
        if (productInstanceIteration != null) {
            ProductInstanceMaster productInstanceMaster = productInstanceIteration.getProductInstanceMaster();

            if (isACLGrantReadAccess(user, productInstanceMaster)) {
                return binaryResource;
            } else {
                throw new NotAllowedException(userLocale, "NotAllowedException34");
            }
        } else {
            throw new FileNotFoundException(userLocale, fullName);
        }
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID})
    @Override
    public PathData addPathData(String workspaceId, String configurationItemId, String serialNumber, String path, List<InstanceAttribute> attributes, String description) throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException, ProductInstanceMasterNotFoundException, UserNotActiveException, NotAllowedException, PathDataAlreadyExistsException {
        User user = userManager.checkWorkspaceReadAccess(workspaceId);
        Locale locale = new Locale(user.getLanguage());

        // Load the product instance
        ProductInstanceMasterDAO productInstanceMasterDAO = new ProductInstanceMasterDAO(locale, em);
        ProductInstanceMaster prodInstM = productInstanceMasterDAO.loadProductInstanceMaster(new ProductInstanceMasterKey(serialNumber, workspaceId, configurationItemId));

        // Check the access to the product instance
        checkProductInstanceWriteAccess(workspaceId, prodInstM, user);

        // Check if not already a path data for this configuration
        for(PathData pathData:prodInstM.getPathDataList()){
            if(pathData.getPath().equals(path)){
                throw new PathDataAlreadyExistsException(locale,pathData);
            }
        }

        PathData pathData = new PathData();
        pathData.setPath(path);
        pathData.setInstanceAttributes(attributes);
        pathData.setDescription(description);

        PathDataDAO pathDataDAO = new PathDataDAO(locale, em);
        pathDataDAO.createPathData(pathData);

        prodInstM.getPathDataList().add(pathData);

        return pathData;
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID})
    @Override
    public PathData updatePathData(String workspaceId, String configurationItemId, String serialNumber, String path, int pathDataId, List<InstanceAttribute> attributes, String description, DocumentIterationKey[] pLinkKeys, String[] documentLinkComments) throws UserNotActiveException, WorkspaceNotFoundException, UserNotFoundException, ProductInstanceMasterNotFoundException, AccessRightException, NotAllowedException {

        User user = userManager.checkWorkspaceReadAccess(workspaceId);
        Locale locale = new Locale(user.getLanguage());

        // Load the product instance
        ProductInstanceMasterDAO productInstanceMasterDAO = new ProductInstanceMasterDAO(locale, em);
        ProductInstanceMaster prodInstM = productInstanceMasterDAO.loadProductInstanceMaster(new ProductInstanceMasterKey(serialNumber, workspaceId, configurationItemId));

        // Check the access to the product instance
        checkProductInstanceWriteAccess(workspaceId, prodInstM, user);

        PathData pathData = em.find(PathData.class, pathDataId);

        // This path data isn't owned by product master.
        if (!prodInstM.getPathDataList().contains(pathData)) {
            throw new NotAllowedException(locale, "NotAllowedException52");
        }

        pathData.setInstanceAttributes(attributes);
        pathData.setDescription(description);

        // Set links
        DocumentLinkDAO linkDAO = new DocumentLinkDAO(locale, em);

        ArrayList<DocumentIterationKey> linkKeys = new ArrayList<>(Arrays.asList(pLinkKeys));

        Set<DocumentLink> currentLinks = new HashSet<>(pathData.getLinkedDocuments());

        for (DocumentLink link : currentLinks) {
            pathData.getLinkedDocuments().remove(link);
        }

        int counter = 0;
        for (DocumentIterationKey link : linkKeys) {
            DocumentLink newLink = new DocumentLink(em.getReference(DocumentIteration.class, link));
            newLink.setComment(documentLinkComments[counter]);
            linkDAO.createLink(newLink);
            pathData.getLinkedDocuments().add(newLink);
            counter++;
        }

        return pathData;
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID})
    @Override
    public void deletePathData(String workspaceId, String configurationItemId, String serialNumber, int pathDataId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ProductInstanceMasterNotFoundException, AccessRightException, NotAllowedException {

        User user = userManager.checkWorkspaceReadAccess(workspaceId);
        Locale locale = new Locale(user.getLanguage());

        // Load the product instance
        ProductInstanceMasterDAO productInstanceMasterDAO = new ProductInstanceMasterDAO(locale, em);
        ProductInstanceMaster prodInstM = productInstanceMasterDAO.loadProductInstanceMaster(new ProductInstanceMasterKey(serialNumber, workspaceId, configurationItemId));

        checkProductInstanceWriteAccess(workspaceId, prodInstM, user);

        PathDataDAO pathDataDAO = new PathDataDAO(locale, em);
        PathData pathData = em.find(PathData.class, pathDataId);

        // This path data isn't owned by product master.
        if (!prodInstM.getPathDataList().contains(pathData)) {
            throw new NotAllowedException(locale, "NotAllowedException52");
        }

        prodInstM.getPathDataList().remove(pathData);

        for (BinaryResource file : pathData.getAttachedFiles()) {
            try {
                dataManager.deleteData(file);
            } catch (StorageException e) {
                LOGGER.log(Level.INFO, null, e);
            }
        }

        pathDataDAO.removePathData(pathData);
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID})
    @Override
    public PathData getPathData(String workspaceId, String configurationItemId, String serialNumber, int pathDataId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ProductInstanceMasterNotFoundException, AccessRightException, NotAllowedException {
        User user = userManager.checkWorkspaceReadAccess(workspaceId);
        Locale locale = new Locale(user.getLanguage());

        // Load the product instance
        ProductInstanceMasterDAO productInstanceMasterDAO = new ProductInstanceMasterDAO(locale, em);
        ProductInstanceMaster prodInstM = productInstanceMasterDAO.loadProductInstanceMaster(new ProductInstanceMasterKey(serialNumber, workspaceId, configurationItemId));

        // Check the access to the product instance
        checkProductInstanceReadAccess(workspaceId, prodInstM, user);

        PathData pathData = em.find(PathData.class, pathDataId);

        // This path data isn't owned by product master.
        if (!prodInstM.getPathDataList().contains(pathData)) {
            throw new NotAllowedException(locale, "NotAllowedException52");
        }

        return pathData;
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID})
    @Override
    public PathData getPathDataByPath(String workspaceId, String configurationItemId, String serialNumber, String path) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, AccessRightException, ProductInstanceMasterNotFoundException {
        User user = userManager.checkWorkspaceReadAccess(workspaceId);
        Locale locale = new Locale(user.getLanguage());

        // Load the product instance
        ProductInstanceMasterDAO productInstanceMasterDAO = new ProductInstanceMasterDAO(locale, em);
        ProductInstanceMaster prodInstM = productInstanceMasterDAO.loadProductInstanceMaster(new ProductInstanceMasterKey(serialNumber, workspaceId, configurationItemId));

        // Check the access to the product instance
        checkProductInstanceReadAccess(workspaceId, prodInstM, user);

        PathDataDAO pathDataDAO = new PathDataDAO(locale, em);

        return pathDataDAO.findByPathAndProductInstance(path, prodInstM);
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID})
    @Override
    public BinaryResource saveFileInPathData(String workspaceId, String configurationItemId, String serialNumber, int pathDataId, String fileName, int pSize) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, NotAllowedException, AccessRightException, ProductInstanceMasterNotFoundException, FileAlreadyExistsException, CreationException {
        // Check the read access to the workspace
        User user = userManager.checkWorkspaceReadAccess(workspaceId);
        Locale locale = new Locale(user.getLanguage());
        checkNameFileValidity(fileName, locale);

        // Load the product instance
        ProductInstanceMasterDAO productInstanceMasterDAO = new ProductInstanceMasterDAO(locale, em);
        ProductInstanceMaster prodInstM = productInstanceMasterDAO.loadProductInstanceMaster(new ProductInstanceMasterKey(serialNumber, workspaceId, configurationItemId));
        // Check the access to the product instance
        checkProductInstanceWriteAccess(workspaceId, prodInstM, user);

        // Load path data
        PathData pathData = em.find(PathData.class, pathDataId);

        // This path data isn't owned by product master.
        if (!prodInstM.getPathDataList().contains(pathData)) {
            throw new NotAllowedException(locale,"NotAllowedException52");
        }

        BinaryResource binaryResource = null;
        String fullName = workspaceId + "/product-instances/" + prodInstM.getSerialNumber() + "/pathdata/" + pathData.getId() + "/" + fileName;

        for (BinaryResource bin : pathData.getAttachedFiles()) {
            if (bin.getFullName().equals(fullName)) {
                binaryResource = bin;
                break;
            }
        }

        if (binaryResource == null) {
            binaryResource = new BinaryResource(fullName, pSize, new Date());
            new BinaryResourceDAO(locale, em).createBinaryResource(binaryResource);
            pathData.addFile(binaryResource);
        } else {
            binaryResource.setContentLength(pSize);
            binaryResource.setLastModified(new Date());
        }
        return binaryResource;
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID})
    @Override
    public BinaryResource getPathDataBinaryResource(String fullName) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, NotAllowedException, FileNotFoundException, AccessRightException {

        if (ctx.isCallerInRole(UserGroupMapping.GUEST_PROXY_ROLE_ID)) {
            // Don't check access right because it is done before. (Is public or isShared)
            return new BinaryResourceDAO(em).loadBinaryResource(fullName);
        }

        User user = userManager.checkWorkspaceReadAccess(BinaryResource.parseWorkspaceId(fullName));
        Locale userLocale = new Locale(user.getLanguage());
        BinaryResourceDAO binDAO = new BinaryResourceDAO(userLocale, em);
        BinaryResource binaryResource = binDAO.loadBinaryResource(fullName);

        PathData pathData = binDAO.getPathDataOwner(binaryResource);
        PathDataDAO pathDataDAO = new PathDataDAO(userLocale,em);

        if (pathData != null) {

            ProductInstanceMaster productInstanceMaster = pathDataDAO.findByPathData(pathData);

            String workspaceId = productInstanceMaster.getInstanceOf().getWorkspaceId();
            checkProductInstanceReadAccess(workspaceId, productInstanceMaster, user);

            return binaryResource;

        } else {
            throw new FileNotFoundException(userLocale, fullName);
        }
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID})
    @Override
    public BinaryResource renameFileInPathData(String workspaceId, String configurationItemId, String serialNumber, int pathDataId, String pFullName, String pNewName) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, FileNotFoundException, ProductInstanceMasterNotFoundException, NotAllowedException, AccessRightException, FileAlreadyExistsException, CreationException, StorageException {

        User user = userManager.checkWorkspaceReadAccess(BinaryResource.parseWorkspaceId(pFullName));
        Locale userLocale = new Locale(user.getLanguage());

        BinaryResourceDAO binDAO = new BinaryResourceDAO(new Locale(user.getLanguage()), em);
        BinaryResource file = binDAO.loadBinaryResource(pFullName);

        if (file == null) {
            throw new FileNotFoundException(userLocale, pFullName);
        }

        ProductInstanceMasterDAO productInstanceMasterDAO = new ProductInstanceMasterDAO(userLocale, em);
        ProductInstanceMasterKey pInstanceIterationKey = new ProductInstanceMasterKey(serialNumber, workspaceId, configurationItemId);
        ProductInstanceMaster productInstanceMaster = productInstanceMasterDAO.loadProductInstanceMaster(pInstanceIterationKey);

        checkNameFileValidity(pNewName, userLocale);

        if (binDAO.loadBinaryResource(file.getNewFullName(pNewName)) == null) {

            //check access rights on product master
            checkProductInstanceWriteAccess(user.getWorkspaceId(), productInstanceMaster, user);

            PathData pathData = em.find(PathData.class,pathDataId);

            // This path data isn't owned by product master.
            if (!productInstanceMaster.getPathDataList().contains(pathData)) {
                throw new NotAllowedException(userLocale,"NotAllowedException52");
            }

            dataManager.renameFile(file, pNewName);
            pathData.removeFile(file);
            binDAO.removeBinaryResource(file);

            BinaryResource newFile = new BinaryResource(file.getNewFullName(pNewName), file.getContentLength(), file.getLastModified());
            binDAO.createBinaryResource(newFile);
            pathData.addFile(newFile);
            return newFile;

        } else {
            throw new FileAlreadyExistsException(userLocale, pNewName);
        }
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID})
    @Override
    public ProductInstanceMaster removeFileFromPathData(String workspaceId, String configurationItemId, String serialNumber, int pathDataId, String fullName, ProductInstanceMaster productInstanceMaster) throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException, UserNotActiveException, NotAllowedException {

        User user = userManager.checkWorkspaceReadAccess(workspaceId);
        Locale userLocale = new Locale(user.getLanguage());

        BinaryResourceDAO binDAO = new BinaryResourceDAO(userLocale, em);
        BinaryResource file = binDAO.loadBinaryResource(fullName);
        checkProductInstanceWriteAccess(workspaceId, productInstanceMaster, user);

        PathData pathData = em.find(PathData.class,pathDataId);

        // This path data isn't owned by product master.
        if (!productInstanceMaster.getPathDataList().contains(pathData)) {
            throw new NotAllowedException(userLocale,"NotAllowedException52");
        }

        try {
            dataManager.deleteData(file);
        } catch (StorageException e) {
            LOGGER.log(Level.INFO, null, e);
        }
        pathData.removeFile(file);
        binDAO.removeBinaryResource(file);
        return productInstanceMaster;
    }

    private User checkProductInstanceReadAccess(String workspaceId, ProductInstanceMaster prodInstM, User user) throws AccessRightException, WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException {
        if (user.isAdministrator()) {
            // Check if it is the workspace's administrator
            return user;
        }
        if (prodInstM.getAcl() == null) {
            // Check if the item haven't ACL
            return userManager.checkWorkspaceReadAccess(workspaceId);
        } else if (prodInstM.getAcl().hasWriteAccess(user)) {
            // Check if there is a write access
            return user;
        } else {
            // Else throw a AccessRightException
            throw new AccessRightException(new Locale(user.getLanguage()), user);
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

    private boolean isACLGrantReadAccess(User user, ProductInstanceMaster productInstanceMaster) {
        return user.isAdministrator() || productInstanceMaster.getAcl().hasReadAccess(user);
    }

    private void checkNameValidity(String name, Locale locale) throws NotAllowedException {
        if (!NamingConvention.correct(name)) {
            throw new NotAllowedException(locale, "NotAllowedException9");
        }
    }

    private void checkNameFileValidity(String name, Locale locale) throws NotAllowedException {
        if (!NamingConvention.correctNameFile(name)) {
            throw new NotAllowedException(locale, "NotAllowedException9");
        }
    }
}
