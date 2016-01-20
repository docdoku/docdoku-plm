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
import com.docdoku.core.document.DocumentLink;
import com.docdoku.core.document.DocumentRevision;
import com.docdoku.core.document.DocumentRevisionKey;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.meta.InstanceAttribute;
import com.docdoku.core.product.*;
import com.docdoku.core.security.ACL;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.IDataManagerLocal;
import com.docdoku.core.services.IProductInstanceManagerLocal;
import com.docdoku.core.services.IUserManagerLocal;
import com.docdoku.core.util.NamingConvention;
import com.docdoku.core.util.Tools;
import com.docdoku.server.LogDocument;
import com.docdoku.server.configuration.PSFilterVisitor;
import com.docdoku.server.configuration.spec.ProductBaselineConfigSpec;
import com.docdoku.server.dao.*;
import com.docdoku.server.factory.ACLFactory;
import com.docdoku.server.validation.AttributesConsistencyUtils;

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
        User user = userManager.checkWorkspaceReadAccess(workspaceId);
        Locale locale = new Locale(user.getLanguage());

        ProductInstanceMasterDAO productInstanceMasterDAO = new ProductInstanceMasterDAO(locale, em);
        List<ProductInstanceMaster> productInstanceMasters = productInstanceMasterDAO.findProductInstanceMasters(workspaceId);

        ListIterator<ProductInstanceMaster> ite = productInstanceMasters.listIterator();

        while(ite.hasNext()){
            ProductInstanceMaster next = ite.next();
            try {
                checkProductInstanceReadAccess(workspaceId, next, user);
            } catch (AccessRightException e) {
                ite.remove();
            }
        }

        return productInstanceMasters;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public List<ProductInstanceMaster> getProductInstanceMasters(ConfigurationItemKey configurationItemKey) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException {
        User user = userManager.checkWorkspaceReadAccess(configurationItemKey.getWorkspace());
        Locale locale = new Locale(user.getLanguage());

        ProductInstanceMasterDAO productInstanceMasterDAO = new ProductInstanceMasterDAO(locale, em);
        List<ProductInstanceMaster> productInstanceMasters = productInstanceMasterDAO.findProductInstanceMasters(configurationItemKey.getId(), configurationItemKey.getWorkspace());

        ListIterator<ProductInstanceMaster> ite = productInstanceMasters.listIterator();

        while(ite.hasNext()){
            ProductInstanceMaster next = ite.next();
            try {
                checkProductInstanceReadAccess(configurationItemKey.getWorkspace(), next, user);
            } catch (AccessRightException e) {
                ite.remove();
            }
        }

        return productInstanceMasters;
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
    public ProductInstanceMaster createProductInstance(String workspaceId, ConfigurationItemKey configurationItemKey, String serialNumber, int baselineId, Map<String, ACL.Permission> userEntries, Map<String, ACL.Permission> groupEntries, List<InstanceAttribute> attributes, DocumentRevisionKey[] links, String[] documentLinkComments) throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException, ConfigurationItemNotFoundException, BaselineNotFoundException, CreationException, ProductInstanceAlreadyExistsException, NotAllowedException, EntityConstraintException, UserNotActiveException, PathToPathLinkAlreadyExistsException, PartMasterNotFoundException, ProductInstanceMasterNotFoundException {
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
        Date now = new Date();
        ProductInstanceIteration productInstanceIteration = productInstanceMaster.createNextIteration();
        productInstanceIteration.setIterationNote("Initial");
        productInstanceIteration.setAuthor(user);
        productInstanceIteration.setCreationDate(now);
        productInstanceIteration.setModificationDate(now);

        PartCollection partCollection = new PartCollection();
        new PartCollectionDAO(em).createPartCollection(partCollection);
        partCollection.setAuthor(user);
        partCollection.setCreationDate(now);

        DocumentCollection documentCollection = new DocumentCollection();
        new DocumentCollectionDAO(em).createDocumentCollection(documentCollection);
        documentCollection.setAuthor(user);
        documentCollection.setCreationDate(now);

        ProductBaseline productBaseline = new ProductBaselineDAO(em).loadBaseline(baselineId);
        productInstanceIteration.setBasedOn(productBaseline);
        productInstanceIteration.setSubstituteLinks(new HashSet<>(productBaseline.getSubstituteLinks()));
        productInstanceIteration.setOptionalUsageLinks(new HashSet<>(productBaseline.getOptionalUsageLinks()));

        productInstanceMasterDAO.createProductInstanceMaster(productInstanceMaster);

        for (BaselinedPart baselinedPart : productBaseline.getBaselinedParts().values()) {
            partCollection.addBaselinedPart(baselinedPart.getTargetPart());
        }

        for (BaselinedDocument baselinedDocument : productBaseline.getBaselinedDocuments().values()) {
            documentCollection.addBaselinedDocument(baselinedDocument.getTargetDocument());
        }

        productInstanceIteration.setPartCollection(partCollection);
        productInstanceIteration.setDocumentCollection(documentCollection);

        productInstanceIteration.setInstanceAttributes(attributes);
        DocumentLinkDAO linkDAO = new DocumentLinkDAO(userLocale, em);

        if (links != null) {
            Set<DocumentLink> currentLinks = new HashSet<>(productInstanceIteration.getLinkedDocuments());

            for (DocumentLink link : currentLinks) {
                productInstanceIteration.getLinkedDocuments().remove(link);
            }

            int counter = 0;
            for (DocumentRevisionKey link : links) {
                DocumentLink newLink = new DocumentLink(em.getReference(DocumentRevision.class, link));
                newLink.setComment(documentLinkComments[counter]);
                linkDAO.createLink(newLink);
                productInstanceIteration.getLinkedDocuments().add(newLink);
                counter++;
            }
        }

        copyPathToPathLinks(user,productInstanceIteration);

        return productInstanceMaster;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public ProductInstanceMaster updateProductInstance(String workspaceId, int iteration, String iterationNote, ConfigurationItemKey configurationItemKey, String serialNumber, int baselineId, List<InstanceAttribute> attributes, DocumentRevisionKey[] links, String[] documentLinkComments) throws ProductInstanceMasterNotFoundException, UserNotFoundException, AccessRightException, WorkspaceNotFoundException, ProductInstanceIterationNotFoundException, UserNotActiveException, BaselineNotFoundException {
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
            productInstanceIteration.setModificationDate(new Date());
            DocumentLinkDAO linkDAO = new DocumentLinkDAO(userLocale, em);
            if (links != null) {

                Set<DocumentLink> currentLinks = new HashSet<>(productInstanceIteration.getLinkedDocuments());

                for (DocumentLink link : currentLinks) {
                    productInstanceIteration.getLinkedDocuments().remove(link);
                }

                int counter = 0;
                for (DocumentRevisionKey link : links) {
                    DocumentLink newLink = new DocumentLink(em.getReference(DocumentRevision.class, link));
                    newLink.setComment(documentLinkComments[counter]);
                    linkDAO.createLink(newLink);
                    productInstanceIteration.getLinkedDocuments().add(newLink);
                    counter++;
                }
            }
            ProductBaseline productBaseline = new ProductBaselineDAO(em).loadBaseline(baselineId);
            for (BaselinedDocument baselinedDocument : productBaseline.getBaselinedDocuments().values()) {
                if(!productBaseline.getDocumentCollection().hasBaselinedDocument(baselinedDocument.getTargetDocument().getDocumentRevisionKey())){
                    productBaseline.getDocumentCollection().addBaselinedDocument(baselinedDocument.getTargetDocument());
                }

            }

            return productInstanceMaster;

        } else {
            throw new ProductInstanceIterationNotFoundException(userLocale, new ProductInstanceIterationKey(serialNumber, configurationItemKey.getWorkspace(), configurationItemKey.getId(), iteration));
        }

    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public ProductInstanceMaster rebaseProductInstance(String workspaceId, String serialNumber, ConfigurationItemKey configurationItemKey, int baselineId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ProductInstanceMasterNotFoundException, AccessRightException, BaselineNotFoundException, NotAllowedException, ConfigurationItemNotFoundException, PathToPathLinkAlreadyExistsException, PartMasterNotFoundException, CreationException, EntityConstraintException {
        User user = userManager.checkWorkspaceReadAccess(workspaceId);
        Locale userLocale = new Locale(user.getLanguage());
        ProductInstanceMasterDAO productInstanceMasterDAO = new ProductInstanceMasterDAO(userLocale, em);
        ProductInstanceMasterKey pInstanceIterationKey = new ProductInstanceMasterKey(serialNumber, workspaceId, configurationItemKey.getId());
        ProductInstanceMaster productInstanceMaster = productInstanceMasterDAO.loadProductInstanceMaster(pInstanceIterationKey);

        ProductInstanceIteration lastIteration = productInstanceMaster.getLastIteration();

        // Check the access to the product instance
        checkProductInstanceWriteAccess(workspaceId, productInstanceMaster, user);

        // Load the new baseline
        ProductBaselineDAO productBaselineDAO = new ProductBaselineDAO(userLocale, em);
        ProductBaseline baseline = productBaselineDAO.loadBaseline(baselineId);

        // Check valid parameters
        // Config key should be baseline product's one, same for product instance
        if (baseline.getConfigurationItem().getKey().equals(configurationItemKey)
                && baseline.getConfigurationItem().getKey().equals(productInstanceMaster.getInstanceOf().getKey())) {


            // Create a new iteration
            ProductInstanceIteration nextIteration = productInstanceMaster.createNextIteration();
            new ProductInstanceIterationDAO(userLocale, em).createProductInstanceIteration(nextIteration);

            nextIteration.setIterationNote(lastIteration.getIterationNote());

            Date now = new Date();

            PartCollection partCollection = new PartCollection();
            new PartCollectionDAO(em).createPartCollection(partCollection);
            partCollection.setAuthor(user);
            partCollection.setCreationDate(now);

            DocumentCollection documentCollection = new DocumentCollection();
            new DocumentCollectionDAO(em).createDocumentCollection(documentCollection);
            documentCollection.setAuthor(user);
            documentCollection.setCreationDate(now);

            nextIteration.setAuthor(user);
            nextIteration.setCreationDate(now);
            nextIteration.setModificationDate(now);

            for (BaselinedPart baselinedPart : baseline.getBaselinedParts().values()) {
                partCollection.addBaselinedPart(baselinedPart.getTargetPart());
            }

            for (BaselinedDocument baselinedDocument : baseline.getBaselinedDocuments().values()) {
                documentCollection.addBaselinedDocument(baselinedDocument.getTargetDocument());
            }

            nextIteration.setPartCollection(partCollection);
            nextIteration.setDocumentCollection(documentCollection);

            nextIteration.setBasedOn(baseline);
            nextIteration.setSubstituteLinks(new HashSet<>(baseline.getSubstituteLinks()));
            nextIteration.setOptionalUsageLinks(new HashSet<>(baseline.getOptionalUsageLinks()));

            Set<DocumentLink> linkedDocuments = lastIteration.getLinkedDocuments();
            Set<DocumentLink> newLinks = new HashSet<>();
            for(DocumentLink link : linkedDocuments){
                newLinks.add(link.clone());
            }
            nextIteration.setLinkedDocuments(newLinks);

            copyPathToPathLinks(user, nextIteration);
            copyPathDataMasterList(workspaceId, user, lastIteration, nextIteration);

        } else {
            throw new NotAllowedException(userLocale, "NotAllowedException53");
        }

        return productInstanceMaster;

    }

    private void copyPathDataMasterList(String workspaceId, User user, ProductInstanceIteration lastIteration, ProductInstanceIteration nextIteration) throws NotAllowedException, EntityConstraintException, PartMasterNotFoundException {

        List<PathDataMaster> pathDataMasterList = new ArrayList<>();
        ProductBaseline productBaseline = nextIteration.getBasedOn();
        PartMaster partMaster = productBaseline.getConfigurationItem().getDesignItem();
        String serialNumber = lastIteration.getSerialNumber();
        BinaryResourceDAO binDAO = new BinaryResourceDAO(new Locale(user.getLanguage()), em);

        PSFilter filter = new ProductBaselineConfigSpec(productBaseline,user);

        PSFilterVisitor psFilterVisitor = new PSFilterVisitor(em, user, filter) {
            @Override
            public void onIndeterminateVersion(PartMaster partMaster, List<PartIteration> partIterations) throws NotAllowedException {
                // Unused here
            }

            @Override
            public void onIndeterminatePath(List<PartLink> pCurrentPath, List<PartIteration> pCurrentPathPartIterations) {
                // Unused here
            }

            @Override
            public void onUnresolvedPath(List<PartLink> pCurrentPath, List<PartIteration> partIterations) throws NotAllowedException {
                // Unused here
            }

            @Override
            public void onBranchDiscovered(List<PartLink> pCurrentPath, List<PartIteration> copyPartIteration) {
                // Unused here
            }

            @Override
            public void onOptionalPath(List<PartLink> path, List<PartIteration> partIterations) {
                // Unused here
            }

            @Override
            public boolean onPathWalk(List<PartLink> path, List<PartMaster> parts) {
                // Find pathData in previous iteration which is on this path. Copy it.
                String pathAsString = Tools.getPathAsString(path);
                for (PathDataMaster pathDataMaster : lastIteration.getPathDataMasterList()) {
                    if (pathAsString.equals(pathDataMaster.getPath())) {
                        pathDataMasterList.add(clonePathDataMaster(pathDataMaster));
                    }
                }
                return true;
            }

            private PathDataMaster clonePathDataMaster(PathDataMaster pathDataMaster) {
                PathDataMaster clone = new PathDataMaster();

                // Need to persist and flush to get an id
                em.persist(clone);
                em.flush();

                clone.setPath(pathDataMaster.getPath());

                List<PathDataIteration> pathDataIterations = new ArrayList<>();
                for (PathDataIteration pathDataIteration : pathDataMaster.getPathDataIterations()) {
                    PathDataIteration clonedIteration = clonePathDataIteration(workspaceId, clone, pathDataIteration);
                    pathDataIterations.add(clonedIteration);
                }
                clone.setPathDataIterations(pathDataIterations);

                return clone;
            }

            private PathDataIteration clonePathDataIteration(String workspaceId, PathDataMaster newPathDataMaster, PathDataIteration pathDataIteration) {
                PathDataIteration clone = new PathDataIteration();

                clone.setPathDataMaster(newPathDataMaster);
                clone.setDateIteration(pathDataIteration.getDateIteration());
                clone.setIteration(pathDataIteration.getIteration());
                clone.setIterationNote(pathDataIteration.getIterationNote());

                // Attributes
                List<InstanceAttribute> clonedAttributes = new ArrayList<>();
                for (InstanceAttribute attribute : pathDataIteration.getInstanceAttributes()) {
                    InstanceAttribute clonedAttribute = attribute.clone();
                    clonedAttributes.add(clonedAttribute);
                }
                clone.setInstanceAttributes(clonedAttributes);

                // Attached files
                for (BinaryResource sourceFile : pathDataIteration.getAttachedFiles()) {
                    String fileName = sourceFile.getName();
                    long length = sourceFile.getContentLength();
                    Date lastModified = sourceFile.getLastModified();
                    String fullName = workspaceId + "/product-instances/" + serialNumber + "/pathdata/" + newPathDataMaster.getId() + "/iterations/" + pathDataIteration.getIteration() + '/' + fileName;
                    BinaryResource targetFile = new BinaryResource(fullName, length, lastModified);
                    try {
                        copyBinary(sourceFile, targetFile);
                        clone.getAttachedFiles().add(targetFile);
                    } catch (FileAlreadyExistsException | CreationException e) {
                        LOGGER.log(Level.FINEST, null, e);
                    }
                }

                // Linked documents
                Set<DocumentLink> newLinks = new HashSet<>();
                for (DocumentLink documentLink : pathDataIteration.getLinkedDocuments()) {
                    newLinks.add(documentLink.clone());
                }
                clone.setLinkedDocuments(newLinks);

                return clone;
            }

            private void copyBinary(BinaryResource sourceFile, BinaryResource targetFile) throws FileAlreadyExistsException, CreationException {
                binDAO.createBinaryResource(targetFile);
                try {
                    dataManager.copyData(sourceFile, targetFile);
                } catch (StorageException e) {
                    LOGGER.log(Level.INFO, null, e);
                }
            }

            @Override
            public void onUnresolvedVersion(PartMaster partMaster) {
                // Unused here
            }
        };

        psFilterVisitor.visit(partMaster, -1);

        nextIteration.setPathDataMasterList(pathDataMasterList);

    }

    private void copyPathToPathLinks(User user, ProductInstanceIteration productInstanceIteration) throws PathToPathLinkAlreadyExistsException, CreationException, UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, ConfigurationItemNotFoundException, BaselineNotFoundException, ProductInstanceMasterNotFoundException, NotAllowedException, EntityConstraintException, PartMasterNotFoundException {
        PathToPathLinkDAO pathToPathLinkDAO = new PathToPathLinkDAO(new Locale(user.getLanguage()), em);
        List<PathToPathLink> links = productInstanceIteration.getBasedOn().getPathToPathLinks();
        for(PathToPathLink link : links){
            PathToPathLink clone = link.clone();
            pathToPathLinkDAO.createPathToPathLink(clone);
            productInstanceIteration.addPathToPathLink(clone);
        }
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public ProductInstanceMaster removeFileFromProductInstanceIteration(String workspaceId, int iteration, String fullName, ProductInstanceMasterKey productInstanceMasterKey) throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException, UserNotActiveException, FileNotFoundException, ProductInstanceMasterNotFoundException {

        User user = userManager.checkWorkspaceReadAccess(workspaceId);
        Locale userLocale = new Locale(user.getLanguage());
        ProductInstanceMaster productInstanceMaster =  getProductInstanceMaster(productInstanceMasterKey);

        ProductInstanceIteration productInstanceIteration = productInstanceMaster.getProductInstanceIterations().get(iteration - 1);
        BinaryResourceDAO binDAO = new BinaryResourceDAO(userLocale, em);
        BinaryResource file = binDAO.loadBinaryResource(fullName);
        checkProductInstanceWriteAccess(workspaceId, productInstanceMaster, user);

        productInstanceIteration.removeFile(file);
        binDAO.removeBinaryResource(file);

        try {
            dataManager.deleteData(file);
        } catch (StorageException e) {
            LOGGER.log(Level.INFO, null, e);
        }

        return productInstanceMaster;

    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public BinaryResource renameFileInProductInstance(String pFullName, String pNewName, String serialNumber, String cId, int iteration) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, FileNotFoundException, ProductInstanceMasterNotFoundException, NotAllowedException, AccessRightException, FileAlreadyExistsException, CreationException, StorageException {
        User user = userManager.checkWorkspaceReadAccess(BinaryResource.parseWorkspaceId(pFullName));
        Locale userLocale = new Locale(user.getLanguage());

        BinaryResourceDAO binDAO = new BinaryResourceDAO(new Locale(user.getLanguage()), em);
        BinaryResource file = binDAO.loadBinaryResource(pFullName);

        ProductInstanceMasterDAO productInstanceMasterDAO = new ProductInstanceMasterDAO(userLocale, em);
        ProductInstanceMasterKey pInstanceIterationKey = new ProductInstanceMasterKey(serialNumber, user.getWorkspaceId(), cId);
        ProductInstanceMaster productInstanceMaster = productInstanceMasterDAO.loadProductInstanceMaster(pInstanceIterationKey);
        checkNameFileValidity(pNewName, userLocale);

        try{

            binDAO.loadBinaryResource(file.getNewFullName(pNewName));
            throw new FileAlreadyExistsException(userLocale, pNewName);

        }catch(FileNotFoundException e){

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
        }

    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public void deleteProductInstance(String workspaceId, String configurationItemId, String serialNumber) throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException, UserNotActiveException, ProductInstanceMasterNotFoundException {
        User user = userManager.checkWorkspaceReadAccess(workspaceId);
        Locale userLocale = new Locale(user.getLanguage());
        ProductInstanceMasterDAO productInstanceMasterDAO = new ProductInstanceMasterDAO(userLocale, em);
        ProductInstanceMaster prodInstM = productInstanceMasterDAO.loadProductInstanceMaster(new ProductInstanceMasterKey(serialNumber, workspaceId, configurationItemId));
        checkProductInstanceWriteAccess(workspaceId, prodInstM, user);

        productInstanceMasterDAO.deleteProductInstanceMaster(prodInstM);

        for (ProductInstanceIteration pii : prodInstM.getProductInstanceIterations()) {
            for (BinaryResource file : pii.getAttachedFiles()) {
                try {
                    dataManager.deleteData(file);
                } catch (StorageException e) {
                    LOGGER.log(Level.INFO, null, e);
                }
            }
        }
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
    public PathDataMaster addNewPathDataIteration(String workspaceId, String configurationItemId, String serialNumber, int pathDataId, String path, List<InstanceAttribute> attributes, String note, DocumentRevisionKey[] links, String[] documentLinkComments) throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException, ProductInstanceMasterNotFoundException, UserNotActiveException, NotAllowedException, PathDataAlreadyExistsException, FileAlreadyExistsException, CreationException {
        User user = userManager.checkWorkspaceReadAccess(workspaceId);
        Locale locale = new Locale(user.getLanguage());

        // Load the product instance
        ProductInstanceMasterDAO productInstanceMasterDAO = new ProductInstanceMasterDAO(locale, em);
        ProductInstanceMaster prodInstM = productInstanceMasterDAO.loadProductInstanceMaster(new ProductInstanceMasterKey(serialNumber, workspaceId, configurationItemId));

        // Check the access to the product instance
        checkProductInstanceWriteAccess(workspaceId, prodInstM, user);

        PathDataMasterDAO pathDataMasterDAO = new PathDataMasterDAO(locale, em);
        ProductInstanceIteration prodInstI = prodInstM.getLastIteration();

        // Check if not already a path data for this configuration
        for (PathDataMaster pathDataMaster : prodInstI.getPathDataMasterList()) {
            if (pathDataMaster.getPath() != null && pathDataMaster.getPath().equals(path)) {
                pathDataMaster = pathDataMasterDAO.findByPathIdAndProductInstanceIteration(pathDataId, prodInstI);
                BinaryResourceDAO binDAO = new BinaryResourceDAO(locale, em);
                Set<BinaryResource> sourceFiles = pathDataMaster.getLastIteration().getAttachedFiles();
                Set<BinaryResource> targetFiles = new HashSet<>();

                if (pathDataMaster.getLastIteration() != null) {
                    int iteration = pathDataMaster.getLastIteration().getIteration() + 1;
                    if (!sourceFiles.isEmpty()) {
                        for (BinaryResource sourceFile : sourceFiles) {


                            String fileName = sourceFile.getName();
                            long length = sourceFile.getContentLength();
                            Date lastModified = sourceFile.getLastModified();
                            String fullName = workspaceId + "/product-instances/" + serialNumber + "/pathdata/" + pathDataId + "/iterations/" + iteration + '/' + fileName;
                            BinaryResource targetFile = new BinaryResource(fullName, length, lastModified);
                            binDAO.createBinaryResource(targetFile);
                            targetFiles.add(targetFile);
                            try {
                                dataManager.copyData(sourceFile, targetFile);
                            } catch (StorageException e) {
                                LOGGER.log(Level.INFO, null, e);
                            }
                        }
                    }
                }


                PathDataIteration pathDataIteration = pathDataMaster.createNextIteration();
                pathDataIteration.setInstanceAttributes(attributes);
                pathDataIteration.setIterationNote(note);
                pathDataIteration = createDocumentLink(locale, pathDataIteration, links, documentLinkComments);
                pathDataIteration.setAttachedFiles(targetFiles);
                PathDataIterationDAO pathDataIterationDAO = new PathDataIterationDAO(em);
                pathDataIterationDAO.createPathDataIteration(pathDataIteration);

                return pathDataMaster;
            }
        }

        // TODO: remove those lines if never used

        PathDataMaster pathDataMaster = new PathDataMaster();
        pathDataMaster.setPath(path);

        PathDataIteration pathDataIteration = pathDataMaster.createNextIteration();
        pathDataIteration.setInstanceAttributes(attributes);
        pathDataIteration.setIterationNote(note);
        pathDataMasterDAO.createPathData(pathDataMaster);
        PathDataIterationDAO pathDataIterationDAO = new PathDataIterationDAO(em);
        pathDataIteration = this.createDocumentLink(locale, pathDataIteration, links, documentLinkComments);
        pathDataIterationDAO.createPathDataIteration(pathDataIteration);

        prodInstI.getPathDataMasterList().add(pathDataMaster);

        return pathDataMaster;
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID})
    @Override
    public PathDataMaster updatePathData(String workspaceId, String configurationItemId, String serialNumber, int pathDataMasterId, int iteration, List<InstanceAttribute> attributes, String note, DocumentRevisionKey[] pLinkKeys, String[] documentLinkComments) throws UserNotActiveException, WorkspaceNotFoundException, UserNotFoundException, ProductInstanceMasterNotFoundException, AccessRightException, NotAllowedException {

        User user = userManager.checkWorkspaceReadAccess(workspaceId);
        Locale locale = new Locale(user.getLanguage());

        // Load the product instance
        ProductInstanceMasterDAO productInstanceMasterDAO = new ProductInstanceMasterDAO(locale, em);
        ProductInstanceMaster prodInstM = productInstanceMasterDAO.loadProductInstanceMaster(new ProductInstanceMasterKey(serialNumber, workspaceId, configurationItemId));

        // Check the access to the product instance
        checkProductInstanceWriteAccess(workspaceId, prodInstM, user);

        PathDataMaster pathDataMaster = em.find(PathDataMaster.class, pathDataMasterId);
        PathDataIteration pathDataIteration = pathDataMaster.getPathDataIterations().get(iteration - 1);

        // This path data isn't owned by product master.
        if (!prodInstM.getLastIteration().getPathDataMasterList().contains(pathDataMaster)) {
            throw new NotAllowedException(locale, "NotAllowedException52");
        }

        boolean valid = AttributesConsistencyUtils.hasValidChange(attributes,false,pathDataIteration.getInstanceAttributes());
        if(!valid) {
            throw new NotAllowedException(locale, "NotAllowedException59");
        }
        pathDataIteration.setInstanceAttributes(attributes);
        pathDataIteration.setIterationNote(note);

        // Set links
        DocumentLinkDAO linkDAO = new DocumentLinkDAO(locale, em);

        Set<DocumentLink> currentLinks = new HashSet<>(pathDataIteration.getLinkedDocuments());

        for (DocumentLink link : currentLinks) {
            pathDataIteration.getLinkedDocuments().remove(link);
        }

        int counter = 0;
        for (DocumentRevisionKey link : pLinkKeys) {
            DocumentLink newLink = new DocumentLink(em.getReference(DocumentRevision.class, link));
            newLink.setComment(documentLinkComments[counter]);
            linkDAO.createLink(newLink);
            pathDataIteration.getLinkedDocuments().add(newLink);
            counter++;
        }

        return pathDataMaster;
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

        PathDataMasterDAO pathDataMasterDAO = new PathDataMasterDAO(locale, em);
        PathDataMaster pathDataMaster = em.find(PathDataMaster.class, pathDataId);

        ProductInstanceIteration prodInstI = prodInstM.getLastIteration();

        // This path data isn't owned by product master.
        if (!prodInstI.getPathDataMasterList().contains(pathDataMaster)) {
            throw new NotAllowedException(locale, "NotAllowedException52");
        }

        prodInstI.getPathDataMasterList().remove(pathDataMaster);
        pathDataMasterDAO.removePathData(pathDataMaster);

        for(PathDataIteration pathDataIteration : pathDataMaster.getPathDataIterations()) {
            for (BinaryResource file : pathDataIteration.getAttachedFiles()) {
                try {
                    dataManager.deleteData(file);
                } catch (StorageException e) {
                    LOGGER.log(Level.INFO, null, e);
                }
            }
        }
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID})
    @Override
    public PathDataMaster getPathDataByPath(String workspaceId, String configurationItemId, String serialNumber, String pathAsString) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, AccessRightException, ProductInstanceMasterNotFoundException {
        User user = userManager.checkWorkspaceReadAccess(workspaceId);
        Locale locale = new Locale(user.getLanguage());

        // Load the product instance
        ProductInstanceMasterDAO productInstanceMasterDAO = new ProductInstanceMasterDAO(locale, em);
        ProductInstanceMaster prodInstM = productInstanceMasterDAO.loadProductInstanceMaster(new ProductInstanceMasterKey(serialNumber, workspaceId, configurationItemId));

        // Check the access to the product instance
        checkProductInstanceReadAccess(workspaceId, prodInstM, user);

        ProductInstanceIteration prodInstI = prodInstM.getLastIteration();
        PathDataMasterDAO pathDataMasterDAO = new PathDataMasterDAO(locale, em);

        return pathDataMasterDAO.findByPathAndProductInstanceIteration(pathAsString, prodInstI);
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID})
    @Override
    public BinaryResource saveFileInPathData(String workspaceId, String configurationItemId, String serialNumber, int pathDataId, int iteration, String fileName, int pSize) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, NotAllowedException, AccessRightException, ProductInstanceMasterNotFoundException, FileAlreadyExistsException, CreationException {
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
        PathDataMaster pathDataMaster = em.find(PathDataMaster.class, pathDataId);
        PathDataIteration pathDataIteration = pathDataMaster.getPathDataIterations().get(iteration - 1);

        // This path data isn't owned by product master.
        if (!prodInstM.getLastIteration().getPathDataMasterList().contains(pathDataMaster)) {
            throw new NotAllowedException(locale, "NotAllowedException52");
        }

        BinaryResource binaryResource = null;
        String fullName = workspaceId + "/product-instances/" + prodInstM.getSerialNumber() + "/pathdata/" + pathDataMaster.getId() + "/iterations/" + iteration + '/' + fileName;

        for (BinaryResource bin : pathDataIteration.getAttachedFiles()) {
            if (bin.getFullName().equals(fullName)) {
                binaryResource = bin;
                break;
            }
        }

        if (binaryResource == null) {
            binaryResource = new BinaryResource(fullName, pSize, new Date());
            new BinaryResourceDAO(locale, em).createBinaryResource(binaryResource);
            pathDataIteration.addFile(binaryResource);
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

        PathDataIteration pathDataIteration = binDAO.getPathDataOwner(binaryResource);
        PathDataMaster pathDataMaster = pathDataIteration.getPathDataMaster();
        PathDataMasterDAO pathDataMasterDAO = new PathDataMasterDAO(userLocale, em);

        if (pathDataMaster != null) {

            ProductInstanceMaster productInstanceMaster = pathDataMasterDAO.findByPathData(pathDataMaster);

            String workspaceId = productInstanceMaster.getInstanceOf().getWorkspaceId();
            checkProductInstanceReadAccess(workspaceId, productInstanceMaster, user);

            return binaryResource;

        } else {
            throw new FileNotFoundException(userLocale, fullName);
        }
    }


    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID})
    @Override
    public BinaryResource renameFileInPathData(String workspaceId, String configurationItemId, String serialNumber, int pathDataId, int iteration, String pFullName, String pNewName) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, FileNotFoundException, ProductInstanceMasterNotFoundException, NotAllowedException, AccessRightException, FileAlreadyExistsException, CreationException {

        User user = userManager.checkWorkspaceReadAccess(BinaryResource.parseWorkspaceId(pFullName));
        Locale userLocale = new Locale(user.getLanguage());

        BinaryResourceDAO binDAO = new BinaryResourceDAO(new Locale(user.getLanguage()), em);
        BinaryResource file = binDAO.loadBinaryResource(pFullName);

        ProductInstanceMasterDAO productInstanceMasterDAO = new ProductInstanceMasterDAO(userLocale, em);
        ProductInstanceMasterKey pInstanceIterationKey = new ProductInstanceMasterKey(serialNumber, workspaceId, configurationItemId);
        ProductInstanceMaster productInstanceMaster = productInstanceMasterDAO.loadProductInstanceMaster(pInstanceIterationKey);

        checkNameFileValidity(pNewName, userLocale);

        try{
            binDAO.loadBinaryResource(file.getNewFullName(pNewName));
            throw new FileAlreadyExistsException(userLocale, pNewName);
        }catch(FileNotFoundException e){

            //check access rights on product master
            checkProductInstanceWriteAccess(user.getWorkspaceId(), productInstanceMaster, user);

            PathDataMaster pathDataMaster = em.find(PathDataMaster.class, pathDataId);

            //allowed on last iteration only
            if (pathDataMaster.getPathDataIterations().size() != (iteration - 1)) {
                throw new NotAllowedException(userLocale, "NotAllowedException55");
            }

            PathDataIteration pathDataIteration = pathDataMaster.getPathDataIterations().get(iteration - 1);

            // This path data isn't owned by product master.
            if (!productInstanceMaster.getLastIteration().getPathDataMasterList().contains(pathDataMaster)) {
                throw new NotAllowedException(userLocale, "NotAllowedException52");
            }

            try {
                dataManager.renameFile(file, pNewName);
                pathDataIteration.removeFile(file);
                binDAO.removeBinaryResource(file);

                BinaryResource newFile = new BinaryResource(file.getNewFullName(pNewName), file.getContentLength(), file.getLastModified());
                binDAO.createBinaryResource(newFile);
                pathDataIteration.addFile(newFile);
                return newFile;


            } catch (StorageException se) {
                LOGGER.log(Level.INFO, null, se);
                return null;
            }

        }
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID})
    @Override
    public ProductInstanceMaster removeFileFromPathData(String workspaceId, String configurationItemId, String serialNumber, int pathDataId, int iteration, String fullName, ProductInstanceMaster productInstanceMaster) throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException, UserNotActiveException, NotAllowedException, FileNotFoundException {

        User user = userManager.checkWorkspaceReadAccess(workspaceId);
        Locale userLocale = new Locale(user.getLanguage());

        BinaryResourceDAO binDAO = new BinaryResourceDAO(userLocale, em);
        BinaryResource file = binDAO.loadBinaryResource(fullName);
        checkProductInstanceWriteAccess(workspaceId, productInstanceMaster, user);

        PathDataMaster pathDataMaster = em.find(PathDataMaster.class, pathDataId);
        PathDataIteration pathDataIteration = pathDataMaster.getPathDataIterations().get(iteration - 1);

        // This path data isn't owned by product master.
        if (!productInstanceMaster.getLastIteration().getPathDataMasterList().contains(pathDataMaster)) {
            throw new NotAllowedException(userLocale, "NotAllowedException52");
        }

        pathDataIteration.removeFile(file);
        binDAO.removeBinaryResource(file);

        try {
            dataManager.deleteData(file);
        } catch (StorageException e) {
            LOGGER.log(Level.INFO, null, e);
        }

        return productInstanceMaster;
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID})
    @Override
    public BinaryResource saveFileInPathDataIteration(String workspaceId, String configurationItemId, String serialNumber, int path, int iteration, String fileName, int pSize) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, NotAllowedException, AccessRightException, ProductInstanceMasterNotFoundException, FileAlreadyExistsException, CreationException {
        // Check the read access to the workspace
        User user = userManager.checkWorkspaceReadAccess(workspaceId);
        Locale locale = new Locale(user.getLanguage());
        checkNameFileValidity(fileName, locale);

        // Load the product instance
        ProductInstanceMasterDAO productInstanceMasterDAO = new ProductInstanceMasterDAO(locale, em);
        ProductInstanceMaster prodInstM = productInstanceMasterDAO.loadProductInstanceMaster(new ProductInstanceMasterKey(serialNumber, workspaceId, configurationItemId));
        // Check the access to the product instance
        checkProductInstanceWriteAccess(workspaceId, prodInstM, user);

        ProductInstanceIteration prodInstI = prodInstM.getLastIteration();

        // Load path data master
        PathDataMasterDAO pathDataMasterDAO = new PathDataMasterDAO(locale, em);
        PathDataMaster pathDataMaster = pathDataMasterDAO.findByPathIdAndProductInstanceIteration(path, prodInstI);

        PathDataIteration pathDataIteration = pathDataMaster.getPathDataIterations().get(iteration - 1);
        // This path data isn't owned by product master.
        if (!prodInstI.getPathDataMasterList().contains(pathDataMaster)) {
            throw new NotAllowedException(locale, "NotAllowedException52");
        }

        BinaryResource binaryResource = null;
        String fullName = workspaceId + "/product-instances/" + prodInstM.getSerialNumber() + "/pathdata/" + pathDataMaster.getId() + "/iterations/" + iteration + '/' + fileName;

        for (BinaryResource bin : pathDataIteration.getAttachedFiles()) {
            if (bin.getFullName().equals(fullName)) {
                binaryResource = bin;
                break;
            }
        }

        if (binaryResource == null) {
            binaryResource = new BinaryResource(fullName, pSize, new Date());
            new BinaryResourceDAO(locale, em).createBinaryResource(binaryResource);
            pathDataIteration.addFile(binaryResource);
        } else {
            binaryResource.setContentLength(pSize);
            binaryResource.setLastModified(new Date());
        }
        return binaryResource;
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID})
    @Override
    public PathDataMaster createPathDataMaster(String workspaceId, String configurationItemId, String serialNumber, String path, List<InstanceAttribute> attributes, String iterationNote) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ProductInstanceMasterNotFoundException, AccessRightException {
        User user = userManager.checkWorkspaceReadAccess(workspaceId);
        Locale locale = new Locale(user.getLanguage());

        // Load the product instance
        ProductInstanceMasterDAO productInstanceMasterDAO = new ProductInstanceMasterDAO(locale, em);
        ProductInstanceMaster prodInstM = productInstanceMasterDAO.loadProductInstanceMaster(new ProductInstanceMasterKey(serialNumber, workspaceId, configurationItemId));

        // Check the access to the product instance
        checkProductInstanceWriteAccess(workspaceId, prodInstM, user);

        PathDataMasterDAO pathDataMasterDAO = new PathDataMasterDAO(locale, em);

        PathDataMaster pathDataMaster = new PathDataMaster();
        pathDataMaster.setPath(path);
        pathDataMasterDAO.createPathData(pathDataMaster);

        ProductInstanceIteration prodInstI = prodInstM.getLastIteration();

        // Check if not already a path data for this configuration
        for (PathDataMaster master : prodInstI.getPathDataMasterList()) {
            if (master.getPath()!= null && master.getPath().equals(path)) {
                PathDataIteration pathDataIteration = pathDataMaster.createNextIteration();
                pathDataIteration.setInstanceAttributes(attributes);
                pathDataIteration.setIterationNote(iterationNote);
                PathDataIterationDAO pathDataIterationDAO = new PathDataIterationDAO(em);
                pathDataIterationDAO.createPathDataIteration(pathDataIteration);

                return pathDataMaster;
            }
        }
        PathDataIteration pathDataIteration = pathDataMaster.createNextIteration();
        pathDataIteration.setInstanceAttributes(attributes);
        pathDataIteration.setIterationNote(iterationNote);
        PathDataIterationDAO pathDataIterationDAO = new PathDataIterationDAO(em);
        pathDataIterationDAO.createPathDataIteration(pathDataIteration);
        prodInstI.getPathDataMasterList().add(pathDataMaster);
        return pathDataMaster;
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID})
    @Override
    public PathToPathLink getPathToPathLink(String workspaceId, String configurationItemId, String serialNumber, int pathToPathLinkId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ProductInstanceMasterNotFoundException, AccessRightException, PathToPathLinkNotFoundException {
        User user = userManager.checkWorkspaceReadAccess(workspaceId);
        Locale locale = new Locale(user.getLanguage());
        // Load the product instance
        ProductInstanceMasterDAO productInstanceMasterDAO = new ProductInstanceMasterDAO(locale, em);
        ProductInstanceMaster prodInstM = productInstanceMasterDAO.loadProductInstanceMaster(new ProductInstanceMasterKey(serialNumber, workspaceId, configurationItemId));

        checkProductInstanceReadAccess(workspaceId, prodInstM, user);
        PathToPathLinkDAO pathToPathLinkDAO = new PathToPathLinkDAO(locale, em);
        return pathToPathLinkDAO.loadPathToPathLink(pathToPathLinkId);
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID})
    @Override
    public List<String> getPathToPathLinkTypes(String workspaceId, String configurationItemId, String serialNumber) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ProductInstanceMasterNotFoundException, AccessRightException {

        User user = userManager.checkWorkspaceReadAccess(workspaceId);
        Locale locale = new Locale(user.getLanguage());
        // Load the product instance
        ProductInstanceMasterDAO productInstanceMasterDAO = new ProductInstanceMasterDAO(locale, em);
        ProductInstanceMaster prodInstM = productInstanceMasterDAO.loadProductInstanceMaster(new ProductInstanceMasterKey(serialNumber, workspaceId, configurationItemId));

        checkProductInstanceReadAccess(workspaceId,prodInstM,user);
        return new PathToPathLinkDAO(locale, em).getDistinctPathToPathLinkTypes(prodInstM.getLastIteration());
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID})
    @Override
    public List<PathToPathLink> getPathToPathLinks(String workspaceId, String configurationItemId, String serialNumber) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ProductInstanceMasterNotFoundException, AccessRightException {

        User user = userManager.checkWorkspaceReadAccess(workspaceId);
        Locale locale = new Locale(user.getLanguage());
        // Load the product instance
        ProductInstanceMasterDAO productInstanceMasterDAO = new ProductInstanceMasterDAO(locale, em);
        ProductInstanceMaster prodInstM = productInstanceMasterDAO.loadProductInstanceMaster(new ProductInstanceMasterKey(serialNumber, workspaceId, configurationItemId));

        checkProductInstanceReadAccess(workspaceId,prodInstM,user);
        return new PathToPathLinkDAO(locale, em).getDistinctPathToPathLink(prodInstM.getLastIteration());
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID})
    @Override
    public List<PathToPathLink> getPathToPathLinkFromSourceAndTarget(String workspaceId, String configurationItemId, String serialNumber, String sourcePath, String targetPath) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ProductInstanceMasterNotFoundException, AccessRightException {
        User user = userManager.checkWorkspaceReadAccess(workspaceId);
        Locale locale = new Locale(user.getLanguage());
        // Load the product instance
        ProductInstanceMasterDAO productInstanceMasterDAO = new ProductInstanceMasterDAO(locale, em);
        ProductInstanceMaster prodInstM = productInstanceMasterDAO.loadProductInstanceMaster(new ProductInstanceMasterKey(serialNumber, workspaceId, configurationItemId));
        checkProductInstanceReadAccess(workspaceId, prodInstM, user);

        return new PathToPathLinkDAO(locale, em).getPathToPathLinkFromSourceAndTarget(prodInstM.getLastIteration(), sourcePath, targetPath);
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID})
    @Override
    public List<PathToPathLink> getRootPathToPathLinks(String workspaceId, String configurationItemId, String serialNumber, String type) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ProductInstanceMasterNotFoundException, AccessRightException {
        User user = userManager.checkWorkspaceReadAccess(workspaceId);
        Locale locale = new Locale(user.getLanguage());
        // Load the product instance
        ProductInstanceMasterDAO productInstanceMasterDAO = new ProductInstanceMasterDAO(locale, em);
        ProductInstanceMaster prodInstM = productInstanceMasterDAO.loadProductInstanceMaster(new ProductInstanceMasterKey(serialNumber, workspaceId, configurationItemId));
        checkProductInstanceReadAccess(workspaceId, prodInstM, user);
        return new PathToPathLinkDAO(locale, em).findRootPathToPathLinks(prodInstM.getLastIteration(), type);
    }

    @Override
    public List<ProductInstanceMaster> getProductInstanceMasters(PartRevision pPartRevision) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException {
        String workspaceId = pPartRevision.getWorkspaceId();
        User user = userManager.checkWorkspaceReadAccess(workspaceId);
        List<ProductInstanceMaster> productInstanceMasters = new ProductInstanceMasterDAO(em).findProductInstanceMasters(pPartRevision);
        ListIterator<ProductInstanceMaster> ite = productInstanceMasters.listIterator();

        while(ite.hasNext()){
            ProductInstanceMaster next = ite.next();
            try {
                checkProductInstanceWriteAccess(workspaceId, next, user);
            } catch (AccessRightException e) {
                ite.remove();
            }
        }

        return productInstanceMasters;

    }

    private User checkProductInstanceReadAccess(String workspaceId, ProductInstanceMaster prodInstM, User user) throws AccessRightException, WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException {
        if (user.isAdministrator()) {
            // Check if the user is workspace administrator
            return user;
        }
        if (prodInstM.getAcl() == null) {
            // Check if the item has no ACL
            return userManager.checkWorkspaceReadAccess(workspaceId);
        } else if (prodInstM.getAcl().hasReadAccess(user)) {
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
            throw new NotAllowedException(locale, "NotAllowedException9", name);
        }
    }

    private void checkNameFileValidity(String name, Locale locale) throws NotAllowedException {
        if (name != null) {
            name = name.trim();
        }
        if (!NamingConvention.correctNameFile(name)) {
            throw new NotAllowedException(locale, "NotAllowedException9", name);
        }
    }

    private PathDataIteration createDocumentLink(Locale locale, PathDataIteration pathDataIteration, DocumentRevisionKey[] links, String[] documentLinkComments) {
        DocumentLinkDAO linkDAO = new DocumentLinkDAO(locale, em);
        if (links != null) {
            Set<DocumentLink> currentLinks = new HashSet<>(pathDataIteration.getLinkedDocuments());

            for (DocumentLink link : currentLinks) {
                pathDataIteration.getLinkedDocuments().remove(link);
            }

            int counter = 0;
            for (DocumentRevisionKey link : links) {
                DocumentLink newLink = new DocumentLink(em.getReference(DocumentRevision.class, link));
                newLink.setComment(documentLinkComments[counter]);
                linkDAO.createLink(newLink);
                pathDataIteration.getLinkedDocuments().add(newLink);
                counter++;
            }
            return pathDataIteration;
        }
        return pathDataIteration;
    }
}
