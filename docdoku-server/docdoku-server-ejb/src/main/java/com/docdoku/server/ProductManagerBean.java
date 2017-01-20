/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2017 DocDoku SARL
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
package com.docdoku.server;

import com.docdoku.core.change.ModificationNotification;
import com.docdoku.core.common.*;
import com.docdoku.core.configuration.*;
import com.docdoku.core.document.*;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.meta.*;
import com.docdoku.core.product.*;
import com.docdoku.core.product.PartIteration.Source;
import com.docdoku.core.query.PartSearchQuery;
import com.docdoku.core.query.Query;
import com.docdoku.core.query.QueryContext;
import com.docdoku.core.query.QueryResultRow;
import com.docdoku.core.security.ACL;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.*;
import com.docdoku.core.sharing.SharedEntityKey;
import com.docdoku.core.sharing.SharedPart;
import com.docdoku.core.util.NamingConvention;
import com.docdoku.core.util.Tools;
import com.docdoku.core.workflow.*;
import com.docdoku.server.configuration.PSFilterVisitor;
import com.docdoku.server.configuration.filter.LatestPSFilter;
import com.docdoku.server.configuration.filter.UpdatePartIterationPSFilter;
import com.docdoku.server.configuration.filter.WIPPSFilter;
import com.docdoku.server.dao.*;
import com.docdoku.server.esindexer.ESIndexer;
import com.docdoku.server.esindexer.ESSearcher;
import com.docdoku.server.events.*;
import com.docdoku.server.factory.ACLFactory;
import com.docdoku.server.validation.AttributesConsistencyUtils;

import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.enterprise.event.Event;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.text.ParseException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@DeclareRoles({UserGroupMapping.REGULAR_USER_ROLE_ID, UserGroupMapping.ADMIN_ROLE_ID})
@Local(IProductManagerLocal.class)
@Stateless(name = "ProductManagerBean")
public class ProductManagerBean implements IProductManagerLocal {

    @PersistenceContext
    private EntityManager em;

    @Inject
    private IMailerLocal mailer;

    @Inject
    private IUserManagerLocal userManager;

    @Inject
    private IContextManagerLocal contextManager;

    @Inject
    private IBinaryStorageManagerLocal storageManager;

    @Inject
    private ESIndexer esIndexer;

    @Inject
    private ESSearcher esSearcher;

    @Inject
    private IPSFilterManagerLocal psFilterManager;

    @Inject
    private Event<TagEvent> tagEvent;

    @Inject
    private Event<PartIterationEvent> partIterationEvent;

    @Inject
    private Event<PartRevisionEvent> partRevisionEvent;

    private static final Logger LOGGER = Logger.getLogger(ProductManagerBean.class.getName());

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public List<PartLink[]> findPartUsages(ConfigurationItemKey pKey, ProductStructureFilter filter, String search) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException, NotAllowedException, EntityConstraintException, PartMasterNotFoundException, ConfigurationItemNotFoundException, WorkspaceNotEnabledException {

        User user = userManager.checkWorkspaceReadAccess(pKey.getWorkspace());

        List<PartLink[]> usagePaths = new ArrayList<>();

        ConfigurationItemDAO configurationItemDAO = new ConfigurationItemDAO(new Locale(user.getLanguage()), em);
        ConfigurationItem ci = configurationItemDAO.loadConfigurationItem(pKey);

        PSFilterVisitor psFilterVisitor = new PSFilterVisitor(em, user, filter) {
            @Override
            public void onIndeterminateVersion(PartMaster partMaster, List<PartIteration> partIterations) throws NotAllowedException {
                // Unused here
            }

            @Override
            public void onUnresolvedVersion(PartMaster partMaster) throws NotAllowedException {
                // Unused here
            }

            @Override
            public void onIndeterminatePath(List<PartLink> pCurrentPath, List<PartIteration> pCurrentPathPartIterations) throws NotAllowedException {
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
                PartMaster pm = parts.get(parts.size() - 1);

                if (pm.getNumber().matches(search) || (pm.getName() != null && pm.getName().matches(search)) || Tools.getPathAsString(path).equals(search)) {
                    PartLink[] partLinks = path.toArray(new PartLink[path.size()]);
                    usagePaths.add(partLinks);
                }
                return true;
            }

        };

        psFilterVisitor.visit(ci.getDesignItem(), -1);

        return usagePaths;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public List<PartMaster> findPartMasters(String pWorkspaceId, String pPartNumber, String pPartName, int pMaxResults) throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceWriteAccess(pWorkspaceId);
        PartMasterDAO partMDAO = new PartMasterDAO(new Locale(user.getLanguage()), em);
        return partMDAO.findPartMasters(pWorkspaceId, pPartNumber, pPartName, pMaxResults);
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public ConfigurationItem createConfigurationItem(String pWorkspaceId, String pId, String pDescription, String pDesignItemNumber) throws UserNotFoundException, WorkspaceNotFoundException, AccessRightException, NotAllowedException, ConfigurationItemAlreadyExistsException, CreationException, PartMasterNotFoundException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceWriteAccess(pWorkspaceId);
        Locale locale = new Locale(user.getLanguage());
        checkNameValidity(pId, locale);

        ConfigurationItem ci = new ConfigurationItem(user, user.getWorkspace(), pId, pDescription);

        try {
            PartMaster designedPartMaster = new PartMasterDAO(locale, em).loadPartM(new PartMasterKey(pWorkspaceId, pDesignItemNumber));
            ci.setDesignItem(designedPartMaster);
            new ConfigurationItemDAO(locale, em).createConfigurationItem(ci);
            return ci;
        } catch (PartMasterNotFoundException e) {
            LOGGER.log(Level.FINEST, null, e);
            throw new PartMasterNotFoundException(locale, pDesignItemNumber);
        }

    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public PartMaster createPartMaster(String pWorkspaceId, String pNumber, String pName, boolean pStandardPart, String pWorkflowModelId, String pPartRevisionDescription, String templateId, Map<String, String> pACLUserEntries, Map<String, String> pACLUserGroupEntries, Map<String, Collection<String>> userRoleMapping, Map<String, Collection<String>> groupRoleMapping) throws NotAllowedException, UserNotFoundException, WorkspaceNotFoundException, AccessRightException, WorkflowModelNotFoundException, PartMasterAlreadyExistsException, CreationException, PartMasterTemplateNotFoundException, FileAlreadyExistsException, RoleNotFoundException, UserGroupNotFoundException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceWriteAccess(pWorkspaceId);
        Locale locale = new Locale(user.getLanguage());
        checkNameValidity(pNumber, locale);

        PartMaster pm = new PartMaster(user.getWorkspace(), pNumber, user);
        pm.setName(pName);
        pm.setStandardPart(pStandardPart);
        Date now = new Date();
        pm.setCreationDate(now);
        PartRevision newRevision = pm.createNextRevision(user);

        Collection<Task> runningTasks = null;
        if (pWorkflowModelId != null) {

            UserDAO userDAO = new UserDAO(locale, em);
            UserGroupDAO groupDAO = new UserGroupDAO(locale, em);
            RoleDAO roleDAO = new RoleDAO(locale, em);

            Map<Role, Collection<User>> roleUserMap = new HashMap<>();
            for (Map.Entry<String, Collection<String>> pair : userRoleMapping.entrySet()) {
                String roleName = pair.getKey();
                Collection<String> userLogins = pair.getValue();
                Role role = roleDAO.loadRole(new RoleKey(pWorkspaceId, roleName));
                Set<User> users = new HashSet<>();
                roleUserMap.put(role, users);
                for (String login : userLogins) {
                    User u = userDAO.loadUser(new UserKey(pWorkspaceId, login));
                    users.add(u);
                }
            }

            Map<Role, Collection<UserGroup>> roleGroupMap = new HashMap<>();
            for (Map.Entry<String, Collection<String>> pair : groupRoleMapping.entrySet()) {
                String roleName = pair.getKey();
                Collection<String> groupIds = pair.getValue();
                Role role = roleDAO.loadRole(new RoleKey(pWorkspaceId, roleName));
                Set<UserGroup> groups = new HashSet<>();
                roleGroupMap.put(role, groups);
                for (String groupId : groupIds) {
                    UserGroup g = groupDAO.loadUserGroup(new UserGroupKey(pWorkspaceId, groupId));
                    groups.add(g);
                }
            }

            WorkflowModel workflowModel = new WorkflowModelDAO(locale, em).loadWorkflowModel(new WorkflowModelKey(user.getWorkspaceId(), pWorkflowModelId));
            Workflow workflow = workflowModel.createWorkflow(roleUserMap, roleGroupMap);
            newRevision.setWorkflow(workflow);

            for (Task task : workflow.getTasks()) {
                if (!task.hasPotentialWorker()) {
                    throw new NotAllowedException(locale, "NotAllowedException56");
                }
            }

            runningTasks = workflow.getRunningTasks();
            for (Task runningTask : runningTasks) {
                runningTask.start();
            }
        }
        newRevision.setCheckOutUser(user);
        newRevision.setCheckOutDate(now);
        newRevision.setCreationDate(now);
        newRevision.setDescription(pPartRevisionDescription);
        PartIteration ite = newRevision.createNextIteration(user);
        ite.setCreationDate(now);

        if (templateId != null) {

            PartMasterTemplate partMasterTemplate = new PartMasterTemplateDAO(locale, em).loadPartMTemplate(new PartMasterTemplateKey(pWorkspaceId, templateId));

            if (!Tools.validateMask(partMasterTemplate.getMask(), pNumber)) {
                throw new NotAllowedException(locale, "NotAllowedException42");
            }

            pm.setType(partMasterTemplate.getPartType());
            pm.setAttributesLocked(partMasterTemplate.isAttributesLocked());

            List<InstanceAttribute> attrs = new ArrayList<>();
            for (InstanceAttributeTemplate attrTemplate : partMasterTemplate.getAttributeTemplates()) {
                InstanceAttribute attr = attrTemplate.createInstanceAttribute();
                attrs.add(attr);
            }
            ite.setInstanceAttributes(attrs);

            BinaryResourceDAO binDAO = new BinaryResourceDAO(locale, em);
            BinaryResource sourceFile = partMasterTemplate.getAttachedFile();

            if (sourceFile != null) {
                String fileName = sourceFile.getName();
                long length = sourceFile.getContentLength();
                Date lastModified = sourceFile.getLastModified();
                String fullName = pWorkspaceId + "/parts/" + pm.getNumber() + "/A/1/nativecad/" + fileName;
                BinaryResource targetFile = new BinaryResource(fullName, length, lastModified);
                binDAO.createBinaryResource(targetFile);
                ite.setNativeCADFile(targetFile);
                try {
                    storageManager.copyData(sourceFile, targetFile);
                } catch (StorageException e) {
                    LOGGER.log(Level.INFO, null, e);
                }
            }

        }

        if (pACLUserEntries != null && !pACLUserEntries.isEmpty() || pACLUserGroupEntries != null && !pACLUserGroupEntries.isEmpty()) {
            ACL acl = new ACLFactory(em).createACL(user.getWorkspace().getId(), pACLUserEntries, pACLUserGroupEntries);
            newRevision.setACL(acl);
        }

        new PartMasterDAO(locale, em).createPartM(pm);

        if (runningTasks != null) {
            mailer.sendApproval(runningTasks, newRevision);
        }

        return pm;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public PartRevision undoCheckOutPart(PartRevisionKey pPartRPK) throws NotAllowedException, PartRevisionNotFoundException, UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, AccessRightException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceReadAccess(pPartRPK.getPartMaster().getWorkspace());
        Locale locale = new Locale(user.getLanguage());

        PartRevisionDAO partRDAO = new PartRevisionDAO(locale, em);
        PartRevision partR = partRDAO.loadPartR(pPartRPK);
        if (partR.getACL() == null) {
            userManager.checkWorkspaceWriteAccess(pPartRPK.getWorkspaceId());
        }

        //Check access rights on partR
        if (!hasPartRevisionWriteAccess(user, partR)) {
            throw new AccessRightException(locale, user);
        }

        if (isCheckoutByUser(user, partR)) {
            if (partR.getLastIteration().getIteration() <= 1) {
                throw new NotAllowedException(locale, "NotAllowedException41");
            }
            PartIteration partIte = partR.removeLastIteration();
            partIterationEvent.select(new AnnotationLiteral<Removed>() {
            }).fire(new PartIterationEvent(partIte));

            PartIterationDAO partIDAO = new PartIterationDAO(locale, em);
            partIDAO.removeIteration(partIte);
            partR.setCheckOutDate(null);
            partR.setCheckOutUser(null);

            // Remove path to path links impacted by this change
            removeObsoletePathToPathLinks(user, pPartRPK.getWorkspaceId());

            for (Geometry file : partIte.getGeometries()) {
                try {
                    storageManager.deleteData(file);
                } catch (StorageException e) {
                    LOGGER.log(Level.INFO, null, e);
                }
            }

            for (BinaryResource file : partIte.getAttachedFiles()) {
                try {
                    storageManager.deleteData(file);
                } catch (StorageException e) {
                    LOGGER.log(Level.INFO, null, e);
                }
            }

            BinaryResource nativeCAD = partIte.getNativeCADFile();
            if (nativeCAD != null) {
                try {
                    storageManager.deleteData(nativeCAD);
                } catch (StorageException e) {
                    LOGGER.log(Level.INFO, null, e);
                }
            }

            return partR;
        } else {
            throw new NotAllowedException(locale, "NotAllowedException19");
        }
    }

    private void removeObsoletePathToPathLinks(User user, String workspaceId) throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, PartRevisionNotFoundException, AccessRightException, WorkspaceNotEnabledException {
        Locale locale = new Locale(user.getLanguage());
        ConfigurationItemDAO configurationItemDAO = new ConfigurationItemDAO(locale, em);
        List<ConfigurationItem> configurationItems = configurationItemDAO.findAllConfigurationItems(workspaceId);

        for (ConfigurationItem configurationItem : configurationItems) {
            List<PathToPathLink> pathToPathLinks = new ArrayList<>(configurationItem.getPathToPathLinks());
            for (PathToPathLink pathToPathLink : pathToPathLinks) {
                try {
                    decodePath(configurationItem.getKey(), pathToPathLink.getSourcePath());
                    decodePath(configurationItem.getKey(), pathToPathLink.getTargetPath());
                } catch (PartUsageLinkNotFoundException e) {
                    configurationItem.removePathToPathLink(pathToPathLink);
                } catch (ConfigurationItemNotFoundException e) {
                    // Should not be thrown
                    LOGGER.log(Level.SEVERE, null, e);
                }
            }
        }

    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public PartRevision checkOutPart(PartRevisionKey pPartRPK) throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException, PartRevisionNotFoundException, NotAllowedException, FileAlreadyExistsException, CreationException, UserNotActiveException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceReadAccess(pPartRPK.getPartMaster().getWorkspace());
        Locale locale = new Locale(user.getLanguage());

        PartRevisionDAO partRDAO = new PartRevisionDAO(locale, em);
        PartRevision partR = partRDAO.loadPartR(pPartRPK);
        if (partR.getACL() == null) {
            userManager.checkWorkspaceWriteAccess(pPartRPK.getWorkspaceId());
        }
        //Check access rights on partR
        if (!hasPartRevisionWriteAccess(user, partR)) {
            throw new AccessRightException(locale, user);
        }

        if (partR.isCheckedOut()) {
            throw new NotAllowedException(locale, "NotAllowedException37");
        }

        if (partR.isReleased() || partR.isObsolete()) {
            throw new NotAllowedException(locale, "NotAllowedException47");
        }

        PartIteration beforeLastPartIteration = partR.getLastIteration();

        PartIteration newPartIteration = partR.createNextIteration(user);
        //We persist the doc as a workaround for a bug which was introduced
        //since glassfish 3 that set the DTYPE to null in the instance attribute table
        em.persist(newPartIteration);
        Date now = new Date();
        newPartIteration.setCreationDate(now);
        partR.setCheckOutUser(user);
        partR.setCheckOutDate(now);

        if (beforeLastPartIteration != null) {
            BinaryResourceDAO binDAO = new BinaryResourceDAO(locale, em);
            for (BinaryResource sourceFile : beforeLastPartIteration.getAttachedFiles()) {
                String fileName = sourceFile.getName();
                long length = sourceFile.getContentLength();
                Date lastModified = sourceFile.getLastModified();
                String fullName = partR.getWorkspaceId() + "/parts/" + partR.getPartNumber() + "/" + partR.getVersion() + "/" + newPartIteration.getIteration() + "/attachedfiles/" + fileName;
                BinaryResource targetFile = new BinaryResource(fullName, length, lastModified);
                binDAO.createBinaryResource(targetFile);
                newPartIteration.addAttachedFile(targetFile);
            }

            newPartIteration.setComponents(new ArrayList<>(beforeLastPartIteration.getComponents()));

            for (Geometry sourceFile : beforeLastPartIteration.getGeometries()) {
                String fileName = sourceFile.getName();
                long length = sourceFile.getContentLength();
                int quality = sourceFile.getQuality();
                Date lastModified = sourceFile.getLastModified();
                String fullName = partR.getWorkspaceId() + "/parts/" + partR.getPartNumber() + "/" + partR.getVersion() + "/" + newPartIteration.getIteration() + "/" + fileName;
                Geometry targetFile = new Geometry(quality, fullName, length, lastModified);
                binDAO.createBinaryResource(targetFile);
                newPartIteration.addGeometry(targetFile);
            }

            BinaryResource nativeCADFile = beforeLastPartIteration.getNativeCADFile();
            if (nativeCADFile != null) {
                String fileName = nativeCADFile.getName();
                long length = nativeCADFile.getContentLength();
                Date lastModified = nativeCADFile.getLastModified();
                String fullName = partR.getWorkspaceId() + "/parts/" + partR.getPartNumber() + "/" + partR.getVersion() + "/" + newPartIteration.getIteration() + "/nativecad/" + fileName;
                BinaryResource targetFile = new BinaryResource(fullName, length, lastModified);
                binDAO.createBinaryResource(targetFile);
                newPartIteration.setNativeCADFile(targetFile);
            }

            Set<DocumentLink> links = new HashSet<>();
            for (DocumentLink link : beforeLastPartIteration.getLinkedDocuments()) {
                DocumentLink newLink = link.clone();
                links.add(newLink);
            }
            newPartIteration.setLinkedDocuments(links);

            InstanceAttributeDAO attrDAO = new InstanceAttributeDAO(em);
            List<InstanceAttribute> attrs = new ArrayList<>();
            for (InstanceAttribute attr : beforeLastPartIteration.getInstanceAttributes()) {
                InstanceAttribute newAttr = attr.clone();
                //Workaround for the NULL DTYPE bug
                attrDAO.createAttribute(newAttr);
                attrs.add(newAttr);
            }
            newPartIteration.setInstanceAttributes(attrs);

            List<InstanceAttributeTemplate> attrsTemplate = new ArrayList<>();
            for (InstanceAttributeTemplate attr : beforeLastPartIteration.getInstanceAttributeTemplates()) {
                InstanceAttributeTemplate newAttr = attr.clone();
                attrsTemplate.add(newAttr);
            }
            newPartIteration.setInstanceAttributeTemplates(attrsTemplate);

        }

        return partR;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public PartRevision checkInPart(PartRevisionKey pPartRPK) throws PartRevisionNotFoundException, UserNotFoundException, WorkspaceNotFoundException, AccessRightException, NotAllowedException, ESServerException, EntityConstraintException, UserNotActiveException, PartMasterNotFoundException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceReadAccess(pPartRPK.getPartMaster().getWorkspace());
        Locale locale = new Locale(user.getLanguage());

        PartRevisionDAO partRDAO = new PartRevisionDAO(locale, em);
        PartRevision partR = partRDAO.loadPartR(pPartRPK);
        if (partR.getACL() == null) {
            userManager.checkWorkspaceWriteAccess(pPartRPK.getWorkspaceId());
        }

        //Check access rights on partR
        if (!hasPartRevisionWriteAccess(user, partR)) {
            throw new AccessRightException(locale, user);
        }

        if (isCheckoutByUser(user, partR)) {

            checkCyclicAssemblyForPartIteration(partR.getLastIteration());

            partR.setCheckOutDate(null);
            partR.setCheckOutUser(null);

            PartIteration lastIteration = partR.getLastIteration();
            lastIteration.setCheckInDate(new Date());

            esIndexer.index(lastIteration);

            partIterationEvent.select(new AnnotationLiteral<CheckedIn>() {
            }).fire(new PartIterationEvent(lastIteration));
            return partR;
        } else {
            throw new NotAllowedException(locale, "NotAllowedException20");
        }

    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID})
    @Override
    public BinaryResource getBinaryResource(String pFullName) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, FileNotFoundException, NotAllowedException, AccessRightException, WorkspaceNotEnabledException {

        User user = userManager.checkWorkspaceReadAccess(BinaryResource.parseWorkspaceId(pFullName));
        Locale userLocale = new Locale(user.getLanguage());
        BinaryResourceDAO binDAO = new BinaryResourceDAO(userLocale, em);
        BinaryResource binaryResource = binDAO.loadBinaryResource(pFullName);

        PartIteration partIte = binDAO.getPartHolder(binaryResource);
        if (partIte != null) {
            PartRevision partR = partIte.getPartRevision();

            if (isACLGrantReadAccess(user, partR)) {
                if (isCheckoutByAnotherUser(user, partR) && partR.getLastIteration().equals(partIte)) {
                    throw new NotAllowedException(new Locale(user.getLanguage()), "NotAllowedException34");
                } else {
                    return binaryResource;
                }
            } else {
                throw new AccessRightException(userLocale, user);
            }
        } else {
            throw new FileNotFoundException(userLocale, pFullName);
        }
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID})
    @Override
    public BinaryResource getTemplateBinaryResource(String pFullName) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, FileNotFoundException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceReadAccess(BinaryResource.parseWorkspaceId(pFullName));
        Locale userLocale = new Locale(user.getLanguage());
        BinaryResourceDAO binDAO = new BinaryResourceDAO(userLocale, em);
        return binDAO.loadBinaryResource(pFullName);
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public BinaryResource saveNativeCADInPartIteration(PartIterationKey pPartIPK, String pName, long pSize) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, NotAllowedException, PartRevisionNotFoundException, FileAlreadyExistsException, CreationException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceReadAccess(pPartIPK.getWorkspaceId());
        Locale locale = new Locale(user.getLanguage());
        checkNameFileValidity(pName, locale);

        BinaryResourceDAO binDAO = new BinaryResourceDAO(locale, em);
        PartRevisionDAO partRDAO = new PartRevisionDAO(locale, em);
        PartRevision partR = partRDAO.loadPartR(pPartIPK.getPartRevision());
        PartIteration partI = partR.getIteration(pPartIPK.getIteration());

        if (isCheckoutByUser(user, partR) && partR.getLastIteration().equals(partI)) {
            String fullName = partR.getWorkspaceId() + "/parts/" + partR.getPartNumber() + "/" + partR.getVersion() + "/" + partI.getIteration() + "/nativecad/" + pName;
            BinaryResource nativeCADBinaryResource = partI.getNativeCADFile();

            if (nativeCADBinaryResource == null) {
                nativeCADBinaryResource = new BinaryResource(fullName, pSize, new Date());
                binDAO.createBinaryResource(nativeCADBinaryResource);
                partI.setNativeCADFile(nativeCADBinaryResource);

            } else if (nativeCADBinaryResource.getFullName().equals(fullName)) {
                nativeCADBinaryResource.setContentLength(pSize);
                nativeCADBinaryResource.setLastModified(new Date());

            } else {
                partI.setNativeCADFile(null);
                binDAO.removeBinaryResource(nativeCADBinaryResource);

                try {
                    storageManager.deleteData(nativeCADBinaryResource);
                } catch (StorageException e) {
                    LOGGER.log(Level.INFO, null, e);
                }

                nativeCADBinaryResource = new BinaryResource(fullName, pSize, new Date());
                binDAO.createBinaryResource(nativeCADBinaryResource);
                partI.setNativeCADFile(nativeCADBinaryResource);
            }

            //Delete converted files if any
            List<Geometry> geometries = new ArrayList<>(partI.getGeometries());
            for (Geometry geometry : geometries) {
                partI.removeGeometry(geometry);
                binDAO.removeBinaryResource(geometry);
                try {
                    storageManager.deleteData(geometry);
                } catch (StorageException e) {
                    LOGGER.log(Level.INFO, null, e);
                }
            }
            return nativeCADBinaryResource;
        } else {
            throw new NotAllowedException(locale, "NotAllowedException4");
        }

    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public BinaryResource saveGeometryInPartIteration(PartIterationKey pPartIPK, String pName, int quality, long pSize, double[] box) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, NotAllowedException, PartRevisionNotFoundException, FileAlreadyExistsException, CreationException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceReadAccess(pPartIPK.getWorkspaceId());
        Locale locale = new Locale(user.getLanguage());
        checkNameFileValidity(pName, locale);

        PartRevisionDAO partRDAO = new PartRevisionDAO(locale, em);
        PartRevision partR = partRDAO.loadPartR(pPartIPK.getPartRevision());
        PartIteration partI = partR.getIteration(pPartIPK.getIteration());
        if (isCheckoutByUser(user, partR) && partR.getLastIteration().equals(partI)) {
            Geometry geometryBinaryResource = null;
            String fullName = partR.getWorkspaceId() + "/parts/" + partR.getPartNumber() + "/" + partR.getVersion() + "/" + partI.getIteration() + "/" + pName;

            for (Geometry geo : partI.getGeometries()) {
                if (geo.getFullName().equals(fullName)) {
                    geometryBinaryResource = geo;
                    break;
                }
            }
            if (geometryBinaryResource == null) {
                geometryBinaryResource = new Geometry(quality, fullName, pSize, new Date());
                new BinaryResourceDAO(locale, em).createBinaryResource(geometryBinaryResource);
                partI.addGeometry(geometryBinaryResource);
            } else {
                geometryBinaryResource.setContentLength(pSize);
                geometryBinaryResource.setQuality(quality);
                geometryBinaryResource.setLastModified(new Date());
            }

            if (box != null) {
                geometryBinaryResource.setBox(box[0], box[1], box[2], box[3], box[4], box[5]);
            }

            return geometryBinaryResource;
        } else {
            throw new NotAllowedException(locale, "NotAllowedException4");
        }
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public BinaryResource saveFileInPartIteration(PartIterationKey pPartIPK, String pName, String subType, long pSize) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, NotAllowedException, PartRevisionNotFoundException, FileAlreadyExistsException, CreationException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceReadAccess(pPartIPK.getWorkspaceId());
        Locale locale = new Locale(user.getLanguage());
        checkNameFileValidity(pName, locale);

        PartRevisionDAO partRDAO = new PartRevisionDAO(locale, em);
        PartRevision partR = partRDAO.loadPartR(pPartIPK.getPartRevision());
        PartIteration partI = partR.getIteration(pPartIPK.getIteration());
        if (isCheckoutByUser(user, partR) && partR.getLastIteration().equals(partI)) {
            BinaryResource binaryResource = null;
            String fullName = partR.getWorkspaceId() + "/parts/" + partR.getPartNumber() + "/" + partR.getVersion() + "/" + partI.getIteration() + "/" + (subType != null ? subType + "/" : "") + pName;

            for (BinaryResource bin : partI.getAttachedFiles()) {
                if (bin.getFullName().equals(fullName)) {
                    binaryResource = bin;
                    break;
                }
            }
            if (binaryResource == null) {
                binaryResource = new BinaryResource(fullName, pSize, new Date());
                new BinaryResourceDAO(locale, em).createBinaryResource(binaryResource);
                partI.addAttachedFile(binaryResource);
            } else {
                binaryResource.setContentLength(pSize);
                binaryResource.setLastModified(new Date());
            }
            return binaryResource;
        } else {
            throw new NotAllowedException(locale, "NotAllowedException4");
        }
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID, UserGroupMapping.ADMIN_ROLE_ID})
    @Override
    public List<ConfigurationItem> getConfigurationItems(String pWorkspaceId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, WorkspaceNotEnabledException {
        Locale locale;
        if (!contextManager.isCallerInRole(UserGroupMapping.ADMIN_ROLE_ID)) {
            User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
            locale = new Locale(user.getLanguage());
        } else {
            locale = Locale.getDefault();
        }

        return new ConfigurationItemDAO(locale, em).findAllConfigurationItems(pWorkspaceId);
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID, UserGroupMapping.ADMIN_ROLE_ID})
    @Override
    public List<ConfigurationItem> searchConfigurationItems(String pWorkspaceId, String q) throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, WorkspaceNotEnabledException {
        List<ConfigurationItem> configurationItems = getConfigurationItems(pWorkspaceId);

        if (q == null || q.isEmpty()) {
            throw new IllegalArgumentException();
        }

        String qLower = q.toLowerCase();

        return configurationItems.stream()
                .filter(configurationItem -> configurationItem.getId().toLowerCase().contains(qLower))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID, UserGroupMapping.ADMIN_ROLE_ID})
    @Override
    public ConfigurationItem getConfigurationItem(ConfigurationItemKey ciKey) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ConfigurationItemNotFoundException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceReadAccess(ciKey.getWorkspace());
        Locale locale = new Locale(user.getLanguage());
        ConfigurationItem configurationItem = em.find(ConfigurationItem.class, ciKey);
        if (configurationItem == null) {
            throw new ConfigurationItemNotFoundException(locale, ciKey.getId());
        }
        return configurationItem;
    }

    /*
    * give pAttributes null for no modification, give an empty list for removing them
    * */
    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public PartRevision updatePartIteration(PartIterationKey pKey, String pIterationNote, Source source, List<PartUsageLink> pUsageLinks, List<InstanceAttribute> pAttributes, List<InstanceAttributeTemplate> pAttributeTemplates, DocumentRevisionKey[] pLinkKeys, String[] documentLinkComments, String[] lovNames)
            throws UserNotFoundException, WorkspaceNotFoundException, AccessRightException, NotAllowedException, PartRevisionNotFoundException, PartMasterNotFoundException, EntityConstraintException, UserNotActiveException, ListOfValuesNotFoundException, PartUsageLinkNotFoundException, DocumentRevisionNotFoundException, WorkspaceNotEnabledException {

        User user = userManager.checkWorkspaceReadAccess(pKey.getWorkspaceId());
        Locale locale = new Locale(user.getLanguage());
        DocumentRevisionDAO documentRevisionDAO = new DocumentRevisionDAO(locale, em);

        PartRevisionDAO partRDAO = new PartRevisionDAO(locale, em);
        PartRevision partRev = partRDAO.loadPartR(pKey.getPartRevision());
        if (partRev.getACL() == null) {
            userManager.checkWorkspaceWriteAccess(pKey.getWorkspaceId());
        }
        //check access rights on partRevision
        if (!hasPartRevisionWriteAccess(user, partRev)) {
            throw new AccessRightException(locale, user);
        }

        DocumentLinkDAO linkDAO = new DocumentLinkDAO(locale, em);
        PartIteration partIte = partRev.getLastIteration();

        if (isCheckoutByUser(user, partRev) && partIte.getKey().equals(pKey)) {

            // Update linked documents

            if (pLinkKeys != null) {
                Set<DocumentLink> currentLinks = new HashSet<>(partIte.getLinkedDocuments());

                for (DocumentLink link : currentLinks) {
                    partIte.getLinkedDocuments().remove(link);
                }

                int counter = 0;
                for (DocumentRevisionKey link : pLinkKeys) {
                    DocumentLink newLink = new DocumentLink(documentRevisionDAO.loadDocR(link));
                    newLink.setComment(documentLinkComments[counter]);
                    linkDAO.createLink(newLink);
                    partIte.getLinkedDocuments().add(newLink);
                    counter++;
                }

            }

            // Update attributes


            //should move that
            if (pAttributes != null) {
                List<InstanceAttribute> currentAttrs = partRev.getLastIteration().getInstanceAttributes();
                boolean valid = AttributesConsistencyUtils.hasValidChange(pAttributes, partRev.isAttributesLocked(), currentAttrs);
                if (!valid) {
                    throw new NotAllowedException(locale, "NotAllowedException59");
                }
                partRev.getLastIteration().setInstanceAttributes(pAttributes);
            }


            // Update attribute templates

            if (pAttributeTemplates != null) {

                LOVDAO lovDAO = new LOVDAO(locale, em);

                List<InstanceAttributeTemplate> templateAttrs = new ArrayList<>();
                for (int i = 0; i < pAttributeTemplates.size(); i++) {
                    templateAttrs.add(pAttributeTemplates.get(i));
                    if (pAttributeTemplates.get(i) instanceof ListOfValuesAttributeTemplate) {
                        ListOfValuesAttributeTemplate lovAttr = (ListOfValuesAttributeTemplate) pAttributeTemplates.get(i);
                        ListOfValuesKey lovKey = new ListOfValuesKey(user.getWorkspaceId(), lovNames[i]);
                        lovAttr.setLov(lovDAO.loadLOV(lovKey));
                    }
                }
                if (!AttributesConsistencyUtils.isTemplateAttributesValid(templateAttrs, false)) {
                    throw new NotAllowedException(locale, "NotAllowedException59");
                }
                partIte.setInstanceAttributeTemplates(pAttributeTemplates);
            }

            // Update structure
            if (pUsageLinks != null) {

                List<PartUsageLink> links = new ArrayList<>();
                for (PartUsageLink usageLink : pUsageLinks) {
                    PartUsageLink partUsageLink = findOrCreatePartLink(user, usageLink, partIte);
                    links.add(partUsageLink);
                }
                partIte.setComponents(links);

                PartUsageLinkDAO partUsageLinkDAO = new PartUsageLinkDAO(new Locale(user.getLanguage()), em);
                partUsageLinkDAO.removeOrphanPartLinks();
                removeObsoletePathToPathLinks(user, pKey.getWorkspaceId());
                checkCyclicAssemblyForPartIteration(partIte);

            }

            // Set note and date

            partIte.setIterationNote(pIterationNote);
            partIte.setModificationDate(new Date());
            partIte.setSource(source);

        } else {
            throw new NotAllowedException(locale, "NotAllowedException25", partIte.getPartNumber());
        }

        return partRev;

    }

    private PartUsageLink findOrCreatePartLink(User user, PartUsageLink partUsageLink, PartIteration partIte) throws PartUsageLinkNotFoundException {
        int id = partUsageLink.getId();
        PartUsageLink partLink;
        if (id != 0) {
            PartUsageLinkDAO partUsageLinkDAO = new PartUsageLinkDAO(new Locale(user.getLanguage()), em);
            partLink = partUsageLinkDAO.loadPartUsageLink(id);
        } else {
            partLink = createNewPartLink(partUsageLink, partIte);
        }
        return partLink;
    }

    private PartUsageLink createNewPartLink(PartUsageLink partUsageLink, PartIteration partIte) throws PartUsageLinkNotFoundException {

        PartUsageLink newLink = new PartUsageLink();

        newLink.setAmount(partUsageLink.getAmount());
        newLink.setOptional(partUsageLink.isOptional());
        newLink.setCadInstances(partUsageLink.getCadInstances());
        newLink.setComment(partUsageLink.getComment());
        newLink.setReferenceDescription(partUsageLink.getReferenceDescription());
        String linkUnit = partUsageLink.getUnit();
        newLink.setUnit(linkUnit != null && linkUnit.isEmpty() ? null : linkUnit);
        newLink.setComponent(partUsageLink.getComponent());

        List<PartSubstituteLink> substitutes = new ArrayList<>();

        for (PartSubstituteLink partSubstituteLink : partUsageLink.getSubstitutes()) {
            PartSubstituteLink newSubstituteLink = new PartSubstituteLink();
            newSubstituteLink.setAmount(partSubstituteLink.getAmount());
            newSubstituteLink.setCadInstances(partSubstituteLink.getCadInstances());
            newSubstituteLink.setComment(partSubstituteLink.getComment());
            newSubstituteLink.setReferenceDescription(partSubstituteLink.getReferenceDescription());
            String substituteUnit = partSubstituteLink.getUnit();
            newSubstituteLink.setUnit(substituteUnit != null && substituteUnit.isEmpty() ? null : substituteUnit);
            newSubstituteLink.setSubstitute(partSubstituteLink.getSubstitute());
            substitutes.add(newSubstituteLink);
        }

        newLink.setSubstitutes(substitutes);

        em.persist(newLink);

        return newLink;
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID})
    @Override
    public PartRevision getPartRevision(PartRevisionKey pPartRPK) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, PartRevisionNotFoundException, AccessRightException, WorkspaceNotEnabledException {

        User user = checkPartRevisionReadAccess(pPartRPK);

        PartRevision partR = new PartRevisionDAO(new Locale(user.getLanguage()), em).loadPartR(pPartRPK);

        if (isCheckoutByAnotherUser(user, partR)) {
            em.detach(partR);
            partR.removeLastIteration();
        }
        return partR;
    }


    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID})
    @Override
    public List<ModificationNotification> getModificationNotifications(PartIterationKey pPartIPK) throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, PartRevisionNotFoundException, AccessRightException, WorkspaceNotEnabledException {
        PartRevisionKey partRevisionKey = pPartIPK.getPartRevision();
        checkPartRevisionReadAccess(partRevisionKey);
        return new ModificationNotificationDAO(em).getModificationNotifications(pPartIPK);
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID})
    @Override
    public void removeModificationNotificationsOnIteration(PartIterationKey pPartIPK) {
        //TODO insure access rights
        new ModificationNotificationDAO(em).removeModificationNotifications(pPartIPK);
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID})
    @Override
    public void removeModificationNotificationsOnRevision(PartRevisionKey pPartRPK) {
        //TODO insure access rights
        new ModificationNotificationDAO(em).removeModificationNotifications(pPartRPK);
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID})
    @Override
    public void createModificationNotifications(PartIteration modifiedPartIteration) throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, PartRevisionNotFoundException, AccessRightException, WorkspaceNotEnabledException {
        //TODO insure access rights
        Set<PartIteration> impactedParts = new HashSet<>();
        impactedParts.addAll(getUsedByAsComponent(modifiedPartIteration.getPartRevisionKey()));
        impactedParts.addAll(getUsedByAsSubstitute(modifiedPartIteration.getPartRevisionKey()));

        ModificationNotificationDAO dao = new ModificationNotificationDAO(em);
        for (PartIteration impactedPart : impactedParts) {
            if (impactedPart.isLastIteration()) {
                ModificationNotification notification = new ModificationNotification();
                notification.setImpactedPart(impactedPart);
                notification.setModifiedPart(modifiedPartIteration);
                dao.createModificationNotification(notification);
            }
        }
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID})
    @Override
    public void updateModificationNotification(String pWorkspaceId, int pModificationNotificationId, String pAcknowledgementComment) throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException, PartRevisionNotFoundException, WorkspaceNotEnabledException {

        ModificationNotification modificationNotification = new ModificationNotificationDAO(em).getModificationNotification(pModificationNotificationId);
        PartIterationKey partIKey = modificationNotification.getImpactedPart().getKey();
        PartRevisionKey partRKey = partIKey.getPartRevision();

        User user = userManager.checkWorkspaceWriteAccess(partRKey.getPartMaster().getWorkspace());
        Locale locale = new Locale(user.getLanguage());

        PartRevisionDAO partRDAO = new PartRevisionDAO(locale, em);
        PartRevision partR = partRDAO.loadPartR(partRKey);

        //Check access rights on partR
        if (!hasPartRevisionWriteAccess(user, partR)) {
            throw new AccessRightException(locale, user);
        }
        Date now = new Date();
        modificationNotification.setAcknowledgementComment(pAcknowledgementComment);
        modificationNotification.setAcknowledged(true);
        modificationNotification.setAcknowledgementDate(now);
        modificationNotification.setAcknowledgementAuthor(user);

    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID})
    @Override
    public List<PartIteration> getUsedByAsComponent(PartRevisionKey partRevisionKey) throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, PartRevisionNotFoundException, AccessRightException, WorkspaceNotEnabledException {
        User user = checkPartRevisionReadAccess(partRevisionKey);
        Locale locale = new Locale(user.getLanguage());
        return new PartIterationDAO(locale, em).findUsedByAsComponent(partRevisionKey.getPartMaster());
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID})
    @Override
    public List<PartIteration> getUsedByAsSubstitute(PartRevisionKey partRevisionKey) throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, PartRevisionNotFoundException, AccessRightException, WorkspaceNotEnabledException {
        User user = checkPartRevisionReadAccess(partRevisionKey);
        Locale locale = new Locale(user.getLanguage());
        return new PartIterationDAO(locale, em).findUsedByAsSubstitute(partRevisionKey.getPartMaster());
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public PartIteration getPartIteration(PartIterationKey pPartIPK) throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, PartRevisionNotFoundException, AccessRightException, PartIterationNotFoundException, NotAllowedException, WorkspaceNotEnabledException {

        PartRevisionKey partRevisionKey = pPartIPK.getPartRevision();
        User user = checkPartRevisionReadAccess(partRevisionKey);
        Locale locale = new Locale(user.getLanguage());
        PartIterationDAO partIterationDAO = new PartIterationDAO(locale, em);

        PartIteration partI = partIterationDAO.loadPartI(pPartIPK);
        PartRevision partR = partI.getPartRevision();
        partR.getIteration(pPartIPK.getIteration());
        PartIteration lastIteration = partR.getLastIteration();

        if (isCheckoutByAnotherUser(user, partR) && lastIteration.getKey().equals(pPartIPK)) {
            throw new NotAllowedException(locale, "NotAllowedException25", partR.getPartMaster().getNumber());
        }

        return partI;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public void updatePartRevisionACL(String workspaceId, PartRevisionKey revisionKey, Map<String, String> pACLUserEntries, Map<String, String> pACLUserGroupEntries) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, PartRevisionNotFoundException, AccessRightException, DocumentRevisionNotFoundException, WorkspaceNotEnabledException {

        User user = userManager.checkWorkspaceReadAccess(workspaceId);
        Locale locale = new Locale(user.getLanguage());
        ACLFactory aclFactory = new ACLFactory(em);

        PartRevisionDAO partRevisionDAO = new PartRevisionDAO(locale, em);
        PartRevision partRevision = partRevisionDAO.loadPartR(revisionKey);

        if (isAuthor(user, partRevision) || user.isAdministrator()) {

            if (partRevision.getACL() == null) {
                ACL acl = aclFactory.createACL(workspaceId, pACLUserEntries, pACLUserGroupEntries);
                partRevision.setACL(acl);
            } else {
                aclFactory.updateACL(workspaceId, partRevision.getACL(), pACLUserEntries, pACLUserGroupEntries);
            }
        } else {
            throw new AccessRightException(locale, user);
        }

    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public void removeACLFromPartRevision(PartRevisionKey revisionKey) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, PartRevisionNotFoundException, AccessRightException, WorkspaceNotEnabledException {

        User user = userManager.checkWorkspaceReadAccess(revisionKey.getPartMaster().getWorkspace());
        Locale locale = new Locale(user.getLanguage());

        PartRevisionDAO partRevisionDAO = new PartRevisionDAO(locale, em);
        PartRevision partRevision = partRevisionDAO.loadPartR(revisionKey);

        if (isAuthor(user, partRevision) || user.isAdministrator()) {
            ACL acl = partRevision.getACL();
            if (acl != null) {
                new ACLDAO(em).removeACLEntries(acl);
                partRevision.setACL(null);
            }
        } else {
            throw new AccessRightException(locale, user);
        }

    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public List<PartRevision> searchPartRevisions(PartSearchQuery pQuery) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ESServerException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceReadAccess(pQuery.getWorkspaceId());
        List<PartRevision> fetchedPartRs = esSearcher.search(pQuery);
        // Get Search Results

        ListIterator<PartRevision> ite = fetchedPartRs.listIterator();
        while (ite.hasNext()) {
            PartRevision partR = ite.next();

            if (isCheckoutByAnotherUser(user, partR)) {
                // Remove CheckedOut PartRevision From Results
                em.detach(partR);
                partR.removeLastIteration();
            }

            if (!hasPartRevisionReadAccess(user, partR)) {
                ite.remove();
            }
        }
        return new ArrayList<>(fetchedPartRs);
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public PartMaster findPartMasterByCADFileName(String workspaceId, String cadFileName) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceReadAccess(workspaceId);
        Locale locale = new Locale(user.getLanguage());

        BinaryResource br = new BinaryResourceDAO(locale, em).findNativeCadBinaryResourceInWorkspace(workspaceId, cadFileName);
        if (br == null) {
            return null;
        }
        String partNumber = br.getHolderId();
        PartMasterKey partMasterKey = new PartMasterKey(workspaceId, partNumber);
        try {
            return new PartMasterDAO(locale, em).loadPartM(partMasterKey);
        } catch (PartMasterNotFoundException e) {
            return null;
        }

    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public Conversion getConversion(PartIterationKey partIterationKey) throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, PartRevisionNotFoundException, AccessRightException, PartIterationNotFoundException, WorkspaceNotEnabledException {
        User user = checkPartRevisionReadAccess(partIterationKey.getPartRevision());
        Locale locale = new Locale(user.getLanguage());
        PartIterationDAO partIterationDAO = new PartIterationDAO(locale, em);
        PartIteration partIteration = partIterationDAO.loadPartI(partIterationKey);
        ConversionDAO conversionDAO = new ConversionDAO(locale, em);
        return conversionDAO.findConversion(partIteration);
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @TransactionAttribute(value = TransactionAttributeType.REQUIRES_NEW)
    @Override
    public Conversion createConversion(PartIterationKey partIterationKey) throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, PartRevisionNotFoundException, AccessRightException, PartIterationNotFoundException, CreationException, WorkspaceNotEnabledException {
        User user = checkPartRevisionWriteAccess(partIterationKey.getPartRevision());
        Locale locale = new Locale(user.getLanguage());
        PartIterationDAO partIterationDAO = new PartIterationDAO(locale, em);
        PartIteration partIteration = partIterationDAO.loadPartI(partIterationKey);
        ConversionDAO conversionDAO = new ConversionDAO(locale, em);
        Conversion conversion = new Conversion(partIteration);
        conversionDAO.createConversion(conversion);
        return conversion;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @TransactionAttribute(value = TransactionAttributeType.REQUIRES_NEW)
    @Override
    public void removeConversion(PartIterationKey partIterationKey) throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, PartRevisionNotFoundException, AccessRightException, PartIterationNotFoundException, WorkspaceNotEnabledException {
        User user = checkPartRevisionWriteAccess(partIterationKey.getPartRevision());
        Locale locale = new Locale(user.getLanguage());
        PartIterationDAO partIterationDAO = new PartIterationDAO(locale, em);
        PartIteration partIteration = partIterationDAO.loadPartI(partIterationKey);
        ConversionDAO conversionDAO = new ConversionDAO(locale, em);
        Conversion conversion = conversionDAO.findConversion(partIteration);
        conversionDAO.deleteConversion(conversion);
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @TransactionAttribute(value = TransactionAttributeType.REQUIRES_NEW)
    @Override
    public void endConversion(PartIterationKey partIterationKey, boolean succeed) throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, PartRevisionNotFoundException, AccessRightException, PartIterationNotFoundException, WorkspaceNotEnabledException {
        User user = checkPartRevisionWriteAccess(partIterationKey.getPartRevision());
        Locale locale = new Locale(user.getLanguage());
        PartIterationDAO partIterationDAO = new PartIterationDAO(locale, em);
        PartIteration partIteration = partIterationDAO.loadPartI(partIterationKey);
        ConversionDAO conversionDAO = new ConversionDAO(locale, em);
        Conversion conversion = conversionDAO.findConversion(partIteration);
        conversion.setPending(false);
        conversion.setSucceed(succeed);
        conversion.setEndDate(new Date());
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @TransactionAttribute(value = TransactionAttributeType.REQUIRES_NEW)
    @Override
    public Import createImport(String workspaceId, String fileName) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, CreationException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceReadAccess(workspaceId);
        Locale locale = new Locale(user.getLanguage());
        Import importToCreate = new Import(user, fileName);
        ImportDAO importDAO = new ImportDAO(locale, em);
        importDAO.createImport(importToCreate);
        return importToCreate;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public List<Import> getImports(String workspaceId, String filename)
            throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, WorkspaceNotEnabledException {

        User user = userManager.checkWorkspaceReadAccess(workspaceId);
        Locale locale = new Locale(user.getLanguage());
        ImportDAO importDAO = new ImportDAO(locale, em);
        return importDAO.findImports(user, filename);
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public Import getImport(String workspaceId, String id) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, AccessRightException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceReadAccess(workspaceId);
        Locale locale = new Locale(user.getLanguage());
        ImportDAO importDAO = new ImportDAO(locale, em);
        Import anImport = importDAO.findImport(user, id);
        if (anImport.getUser().equals(user)) {
            return anImport;
        } else {
            throw new AccessRightException(locale, user);
        }
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @TransactionAttribute(value = TransactionAttributeType.REQUIRES_NEW)
    @Override
    public void endImport(String workspaceId, String id, ImportResult importResult) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, AccessRightException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceReadAccess(workspaceId);
        Locale locale = new Locale(user.getLanguage());
        ImportDAO importDAO = new ImportDAO(locale, em);
        Import anImport = importDAO.findImport(user, id);
        anImport.setPending(false);
        anImport.setEndDate(new Date());
        if (importResult != null) {
            anImport.setErrors(importResult.getErrors());
            anImport.setWarnings(importResult.getWarnings());
            anImport.setSucceed(importResult.isSucceed());
        } else {
            anImport.setSucceed(false);
        }
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public void removeImport(String workspaceId, String id) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, AccessRightException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceReadAccess(workspaceId);
        Locale locale = new Locale(user.getLanguage());
        ImportDAO importDAO = new ImportDAO(locale, em);
        Import anImport = importDAO.findImport(user, id);
        if (anImport.getUser().equals(user)) {
            importDAO.deleteImport(anImport);
        } else {
            throw new AccessRightException(locale, user);
        }
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public void updateACLForPartMasterTemplate(String pWorkspaceId, String templateId, Map<String, String> userEntries, Map<String, String> groupEntries) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, AccessRightException, PartMasterTemplateNotFoundException, WorkspaceNotEnabledException {

        ACLFactory aclFactory = new ACLFactory(em);

        // Check the read access to the workspace
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
        // Load the part template
        PartMasterTemplateKey pKey = new PartMasterTemplateKey(pWorkspaceId, templateId);
        PartMasterTemplate partMasterTemplate = new PartMasterTemplateDAO(new Locale(user.getLanguage()), em).loadPartMTemplate(pKey);

        // Check the access to the part template
        checkPartTemplateWriteAccess(partMasterTemplate, user);

        if (partMasterTemplate.getAcl() == null) {
            ACL acl = aclFactory.createACL(pWorkspaceId, userEntries, groupEntries);
            partMasterTemplate.setAcl(acl);
        } else {
            aclFactory.updateACL(pWorkspaceId, partMasterTemplate.getAcl(), userEntries, groupEntries);
        }
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public void removeACLFromPartMasterTemplate(String workspaceId, String partTemplateId) throws PartMasterNotFoundException, UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, PartMasterTemplateNotFoundException, AccessRightException, WorkspaceNotEnabledException {

        // Check the read access to the workspace
        User user = userManager.checkWorkspaceReadAccess(workspaceId);
        Locale locale = new Locale(user.getLanguage());
        // Load the part template
        PartMasterTemplateKey pKey = new PartMasterTemplateKey(workspaceId, partTemplateId);
        PartMasterTemplate partMaster = new PartMasterTemplateDAO(locale, em).loadPartMTemplate(pKey);

        // Check the access to the part template
        checkPartTemplateWriteAccess(partMaster, user);

        ACL acl = partMaster.getAcl();
        if (acl != null) {
            new ACLDAO(em).removeACLEntries(acl);
            partMaster.setAcl(null);
        }
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public PartRevision saveTags(PartRevisionKey revisionKey, String[] pTags) throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, PartRevisionNotFoundException, AccessRightException, WorkspaceNotEnabledException {

        User user = checkPartRevisionWriteAccess(revisionKey);

        Locale userLocale = new Locale(user.getLanguage());
        PartRevisionDAO partRevDAO = new PartRevisionDAO(userLocale, em);
        PartRevision partRevision = partRevDAO.loadPartR(revisionKey);

        Set<Tag> tags = new HashSet<>();
        if (pTags != null) {
            for (String label : pTags) {
                tags.add(new Tag(user.getWorkspace(), label));
            }

            TagDAO tagDAO = new TagDAO(userLocale, em);
            List<Tag> existingTags = Arrays.asList(tagDAO.findAllTags(user.getWorkspaceId()));

            Set<Tag> tagsToCreate = new HashSet<>(tags);
            tagsToCreate.removeAll(existingTags);

            for (Tag t : tagsToCreate) {
                try {
                    tagDAO.createTag(t);
                } catch (CreationException | TagAlreadyExistsException ex) {
                    LOGGER.log(Level.SEVERE, null, ex);
                }
            }

            Set<Tag> removedTags = new HashSet<>(partRevision.getTags());
            removedTags.removeAll(tags);

            Set<Tag> addedTags = partRevision.setTags(tags);

            for (Tag tag : removedTags) {
                tagEvent.select(new AnnotationLiteral<Untagged>() {
                }).fire(new TagEvent(tag, partRevision));
            }
            for (Tag tag : addedTags) {
                tagEvent.select(new AnnotationLiteral<Tagged>() {
                }).fire(new TagEvent(tag, partRevision));
            }

            if (isCheckoutByAnotherUser(user, partRevision)) {
                em.detach(partRevision);
                partRevision.removeLastIteration();
            }

            for (PartIteration partIteration : partRevision.getPartIterations()) {
                esIndexer.index(partIteration);
            }
        } else {
            throw new IllegalArgumentException("pTags argument must not be null");
        }

        return partRevision;

    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public PartRevision removeTag(PartRevisionKey partRevisionKey, String tagName) throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, PartRevisionNotFoundException, AccessRightException, WorkspaceNotEnabledException {
        User user = checkPartRevisionWriteAccess(partRevisionKey);

        PartRevision partRevision = getPartRevision(partRevisionKey);
        Tag tagToRemove = new Tag(user.getWorkspace(), tagName);
        partRevision.getTags().remove(tagToRemove);

        tagEvent.select(new AnnotationLiteral<Untagged>() {
        }).fire(new TagEvent(tagToRemove, partRevision));

        if (isCheckoutByAnotherUser(user, partRevision)) {
            em.detach(partRevision);
            partRevision.removeLastIteration();
        }

        for (PartIteration partIteration : partRevision.getPartIterations()) {
            esIndexer.index(partIteration);
        }
        return partRevision;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public PartRevision[] findPartRevisionsByTag(String workspaceId, String tagId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, WorkspaceNotEnabledException {

        User user = userManager.checkWorkspaceReadAccess(workspaceId);

        List<PartRevision> partsRevision = new PartRevisionDAO(new Locale(user.getLanguage()), em).findPartByTag(new Tag(user.getWorkspace(), tagId));
        ListIterator<PartRevision> iterator = partsRevision.listIterator();
        while (iterator.hasNext()) {
            PartRevision partRevision = iterator.next();
            if (!hasPartRevisionReadAccess(user, partRevision)) {
                iterator.remove();
            } else if (isCheckoutByAnotherUser(user, partRevision)) {
                em.detach(partRevision);
                partRevision.removeLastIteration();
            }
        }
        return partsRevision.toArray(new PartRevision[partsRevision.size()]);
    }


    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public PartRevision[] getPartRevisionsWithReferenceOrName(String pWorkspaceId, String reference, int maxResults) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
        List<PartRevision> partRs = new PartRevisionDAO(new Locale(user.getLanguage()), em).findPartsRevisionsWithReferenceOrNameLike(pWorkspaceId, reference, maxResults);
        return partRs.toArray(new PartRevision[partRs.size()]);
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public PartRevision releasePartRevision(PartRevisionKey pRevisionKey) throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, PartRevisionNotFoundException, AccessRightException, NotAllowedException, WorkspaceNotEnabledException {
        User user = checkPartRevisionWriteAccess(pRevisionKey);                                                         // Check if the user can write the part
        Locale locale = new Locale(user.getLanguage());

        PartRevisionDAO partRevisionDAO = new PartRevisionDAO(locale, em);
        PartRevision partRevision = partRevisionDAO.loadPartR(pRevisionKey);

        if (partRevision.isCheckedOut()) {
            throw new NotAllowedException(locale, "NotAllowedException46");
        }

        if (partRevision.getNumberOfIterations() == 0) {
            throw new NotAllowedException(locale, "NotAllowedException41");
        }

        if (partRevision.isObsolete()) {
            throw new NotAllowedException(locale, "NotAllowedException38");
        }

        partRevision.release(user);
        return partRevision;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public PartRevision markPartRevisionAsObsolete(PartRevisionKey pRevisionKey) throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, PartRevisionNotFoundException, AccessRightException, NotAllowedException, WorkspaceNotEnabledException {
        User user = checkPartRevisionWriteAccess(pRevisionKey);                                                         // Check if the user can write the part
        Locale locale = new Locale(user.getLanguage());

        PartRevisionDAO partRevisionDAO = new PartRevisionDAO(locale, em);
        PartRevision partRevision = partRevisionDAO.loadPartR(pRevisionKey);

        if (!partRevision.isReleased()) {
            throw new NotAllowedException(locale, "NotAllowedException36");
        }

        partRevision.markAsObsolete(user);
        return partRevision;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public PartRevision getLastReleasePartRevision(ConfigurationItemKey ciKey)
            throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ConfigurationItemNotFoundException, AccessRightException, PartRevisionNotFoundException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceReadAccess(ciKey.getWorkspace());
        Locale locale = new Locale(user.getLanguage());
        ConfigurationItem ci = new ConfigurationItemDAO(locale, em).loadConfigurationItem(ciKey);
        PartMaster partMaster = ci.getDesignItem();
        PartRevision lastReleasedRevision = partMaster.getLastReleasedRevision();
        if (lastReleasedRevision == null) {
            throw new PartRevisionNotFoundException(locale, partMaster.getNumber(), "Released");
        }
        if (!canUserAccess(user, lastReleasedRevision.getKey())) {
            throw new AccessRightException(locale, user);
        }
        return lastReleasedRevision;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public List<ProductBaseline> findBaselinesWherePartRevisionHasIterations(PartRevisionKey partRevisionKey) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, PartRevisionNotFoundException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceReadAccess(partRevisionKey.getPartMaster().getWorkspace());
        Locale locale = new Locale(user.getLanguage());
        PartRevision partRevision = new PartRevisionDAO(locale, em).loadPartR(partRevisionKey);
        return new ProductBaselineDAO(locale, em).findBaselineWherePartRevisionHasIterations(partRevision);
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public List<PartUsageLink> getComponents(PartIterationKey pPartIPK) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, PartIterationNotFoundException, NotAllowedException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceReadAccess(pPartIPK.getWorkspaceId());
        Locale locale = new Locale(user.getLanguage());
        PartIteration partI = new PartIterationDAO(locale, em).loadPartI(pPartIPK);
        PartRevision partR = partI.getPartRevision();

        if (isCheckoutByAnotherUser(user, partR) && partR.getLastIteration().equals(partI)) {
            throw new NotAllowedException(locale, "NotAllowedException34");
        }
        return partI.getComponents();
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public boolean partMasterExists(PartMasterKey partMasterKey) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceReadAccess(partMasterKey.getWorkspace());
        try {
            new PartMasterDAO(new Locale(user.getLanguage()), em).loadPartM(partMasterKey);
            return true;
        } catch (PartMasterNotFoundException e) {
            LOGGER.log(Level.FINEST, null, e);
            return false;
        }
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public void deleteConfigurationItem(ConfigurationItemKey configurationItemKey) throws UserNotFoundException, WorkspaceNotFoundException, AccessRightException, NotAllowedException, UserNotActiveException, ConfigurationItemNotFoundException, LayerNotFoundException, EntityConstraintException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceWriteAccess(configurationItemKey.getWorkspace());
        Locale locale = new Locale(user.getLanguage());

        ProductConfigurationDAO productConfigurationDAO = new ProductConfigurationDAO(locale, em);
        List<ProductConfiguration> productConfigurations = productConfigurationDAO.getAllProductConfigurationsByConfigurationItem(configurationItemKey);

        if (!productConfigurations.isEmpty()) {
            throw new EntityConstraintException(locale, "EntityConstraintException23");
        }

        ProductBaselineDAO productBaselineDAO = new ProductBaselineDAO(locale, em);
        List<ProductBaseline> productBaselines = productBaselineDAO.findBaselines(configurationItemKey.getId(), configurationItemKey.getWorkspace());

        if (!productBaselines.isEmpty()) {
            throw new EntityConstraintException(locale, "EntityConstraintException4");
        }

        ProductInstanceMasterDAO productInstanceMasterDAO = new ProductInstanceMasterDAO(locale, em);
        List<ProductInstanceMaster> productInstanceMasters = productInstanceMasterDAO.findProductInstanceMasters(configurationItemKey.getId(), configurationItemKey.getWorkspace());

        if (!productInstanceMasters.isEmpty()) {
            throw new EntityConstraintException(locale, "EntityConstraintException13");
        }

        new ConfigurationItemDAO(locale, em).removeConfigurationItem(configurationItemKey);
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public void deleteLayer(String workspaceId, int layerId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, LayerNotFoundException, AccessRightException, WorkspaceNotEnabledException {
        Layer layer = new LayerDAO(em).loadLayer(layerId);
        User user = userManager.checkWorkspaceWriteAccess(layer.getConfigurationItem().getWorkspaceId());
        new LayerDAO(new Locale(user.getLanguage()), em).deleteLayer(layer);
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public BinaryResource renameFileInPartIteration(String pSubType, String pFullName, String pNewName) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, NotAllowedException, FileNotFoundException, FileAlreadyExistsException, CreationException, StorageException, WorkspaceNotEnabledException {

        User user = userManager.checkWorkspaceReadAccess(BinaryResource.parseWorkspaceId(pFullName));
        Locale userLocale = new Locale(user.getLanguage());
        checkNameFileValidity(pNewName, userLocale);

        BinaryResourceDAO binDAO = new BinaryResourceDAO(userLocale, em);
        BinaryResource file = binDAO.loadBinaryResource(pFullName);
        PartIteration partIteration = binDAO.getPartHolder(file);

        PartRevision partR = partIteration.getPartRevision();

        if (isCheckoutByUser(user, partR) && partR.getLastIteration().equals(partIteration)) {

            storageManager.renameFile(file, pNewName);

            if (pSubType != null && "nativecad".equals(pSubType)) {
                partIteration.setNativeCADFile(null);
            } else {
                partIteration.removeAttachedFile(file);
            }
            binDAO.removeBinaryResource(file);

            BinaryResource newFile = new BinaryResource(file.getNewFullName(pNewName), file.getContentLength(), file.getLastModified());

            binDAO.createBinaryResource(newFile);

            if (pSubType != null && "nativecad".equals(pSubType)) {
                partIteration.setNativeCADFile(newFile);
            } else {
                partIteration.addAttachedFile(newFile);
            }

            return newFile;
        } else {
            throw new NotAllowedException(userLocale, "NotAllowedException35");
        }

    }

    private void removeCADFile(PartIteration partIteration)
            throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, PartIterationNotFoundException {

        // Delete native cad file
        BinaryResource br = partIteration.getNativeCADFile();
        if (br != null) {
            try {
                storageManager.deleteData(br);
            } catch (StorageException e) {
                LOGGER.log(Level.INFO, null, e);
            }
            partIteration.setNativeCADFile(null);
        }

        // Delete generated 3D files
        List<Geometry> geometries = new ArrayList<>(partIteration.getGeometries());
        for (Geometry geometry : geometries) {
            try {
                storageManager.deleteData(geometry);
            } catch (StorageException e) {
                LOGGER.log(Level.INFO, null, e);
            }
            partIteration.removeGeometry(geometry);
        }
    }

    private void removeAttachedFiles(PartIteration partIteration)
            throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, PartIterationNotFoundException {

        // Delete attached files
        for (BinaryResource file : partIteration.getAttachedFiles()) {
            try {
                storageManager.deleteData(file);
            } catch (StorageException e) {
                LOGGER.log(Level.INFO, null, e);
            }

            esIndexer.delete(partIteration);
        }
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public void removeFileInPartIteration(PartIterationKey pPartIPK, String pSubType, String pName)
            throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, PartIterationNotFoundException, FileNotFoundException, WorkspaceNotEnabledException {

        User user = userManager.checkWorkspaceReadAccess(pPartIPK.getWorkspaceId());

        PartIteration partIteration = new PartIterationDAO(new Locale(user.getLanguage()), em).loadPartI(pPartIPK);
        PartRevision partR = partIteration.getPartRevision();

        if (isCheckoutByUser(user, partR) && partR.getLastIteration().equals(partIteration)) {
            BinaryResourceDAO binDAO = new BinaryResourceDAO(new Locale(user.getLanguage()), em);
            BinaryResource file = binDAO.loadBinaryResource(pName);

            if (pSubType != null && "nativecad".equals(pSubType)) {
                partIteration.setNativeCADFile(null);
            } else {
                partIteration.removeAttachedFile(file);
            }

            try {
                storageManager.deleteData(file);
            } catch (StorageException e) {
                LOGGER.log(Level.INFO, null, e);
            }

            binDAO.removeBinaryResource(file);
        }

    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public void setPublicSharedPart(PartRevisionKey pPartRPK, boolean isPublicShared) throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, PartRevisionNotFoundException, AccessRightException, WorkspaceNotEnabledException {
        PartRevision partRevision = getPartRevision(pPartRPK);
        partRevision.setPublicShared(isPublicShared);
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public PartMaster getPartMaster(PartMasterKey pPartMPK) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, PartMasterNotFoundException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceReadAccess(pPartMPK.getWorkspace());
        PartMaster partM = new PartMasterDAO(new Locale(user.getLanguage()), em).loadPartM(pPartMPK);

        for (PartRevision partR : partM.getPartRevisions()) {
            if (isCheckoutByAnotherUser(user, partR)) {
                em.detach(partR);
                partR.removeLastIteration();
            }
        }
        return partM;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public List<Layer> getLayers(ConfigurationItemKey pKey) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceReadAccess(pKey.getWorkspace());
        return new LayerDAO(new Locale(user.getLanguage()), em).findAllLayers(pKey);
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public Layer getLayer(int pId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, LayerNotFoundException, WorkspaceNotEnabledException {
        Layer layer = new LayerDAO(em).loadLayer(pId);
        userManager.checkWorkspaceReadAccess(layer.getConfigurationItem().getWorkspaceId());
        return layer;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public Layer createLayer(ConfigurationItemKey pKey, String pName, String color) throws UserNotFoundException, WorkspaceNotFoundException, AccessRightException, ConfigurationItemNotFoundException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceWriteAccess(pKey.getWorkspace());
        Locale locale = new Locale(user.getLanguage());

        ConfigurationItem ci = new ConfigurationItemDAO(locale, em).loadConfigurationItem(pKey);
        Layer layer = new Layer(pName, user, ci, color);
        Date now = new Date();
        layer.setCreationDate(now);

        new LayerDAO(locale, em).createLayer(layer);
        return layer;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public Layer updateLayer(ConfigurationItemKey pKey, int pId, String pName, String color) throws UserNotFoundException, WorkspaceNotFoundException, AccessRightException, ConfigurationItemNotFoundException, LayerNotFoundException, UserNotActiveException, WorkspaceNotEnabledException {
        Layer layer = getLayer(pId);
        userManager.checkWorkspaceWriteAccess(layer.getConfigurationItem().getWorkspaceId());
        layer.setName(pName);
        layer.setColor(color);
        return layer;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public Marker createMarker(int pLayerId, String pTitle, String pDescription, double pX, double pY, double pZ) throws LayerNotFoundException, UserNotFoundException, WorkspaceNotFoundException, AccessRightException, WorkspaceNotEnabledException {
        Layer layer = new LayerDAO(em).loadLayer(pLayerId);
        User user = userManager.checkWorkspaceWriteAccess(layer.getConfigurationItem().getWorkspaceId());
        Marker marker = new Marker(pTitle, user, pDescription, pX, pY, pZ);
        Date now = new Date();
        marker.setCreationDate(now);

        new MarkerDAO(new Locale(user.getLanguage()), em).createMarker(marker);
        layer.addMarker(marker);
        return marker;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public void deleteMarker(int pLayerId, int pMarkerId) throws WorkspaceNotFoundException, UserNotActiveException, LayerNotFoundException, UserNotFoundException, AccessRightException, MarkerNotFoundException, WorkspaceNotEnabledException {
        Layer layer = new LayerDAO(em).loadLayer(pLayerId);
        User user = userManager.checkWorkspaceWriteAccess(layer.getConfigurationItem().getWorkspaceId());
        Locale locale = new Locale(user.getLanguage());
        Marker marker = new MarkerDAO(locale, em).loadMarker(pMarkerId);

        if (layer.getMarkers().contains(marker)) {
            layer.removeMarker(marker);
            em.flush();
            new MarkerDAO(locale, em).removeMarker(pMarkerId);
        } else {
            throw new MarkerNotFoundException(locale, pMarkerId);
        }

    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public PartMasterTemplate[] getPartMasterTemplates(String pWorkspaceId) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
        List<PartMasterTemplate> templates = new PartMasterTemplateDAO(new Locale(user.getLanguage()), em).findAllPartMTemplates(pWorkspaceId);

        ListIterator<PartMasterTemplate> ite = templates.listIterator();
        while (ite.hasNext()) {
            PartMasterTemplate template = ite.next();
            if (!hasPartTemplateReadAccess(user, template)) {
                ite.remove();
            }
        }

        return templates.toArray(new PartMasterTemplate[templates.size()]);
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public PartMasterTemplate getPartMasterTemplate(PartMasterTemplateKey pKey) throws WorkspaceNotFoundException, PartMasterTemplateNotFoundException, UserNotFoundException, UserNotActiveException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceReadAccess(pKey.getWorkspaceId());
        return new PartMasterTemplateDAO(new Locale(user.getLanguage()), em).loadPartMTemplate(pKey);
    }


    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public PartMasterTemplate createPartMasterTemplate(String pWorkspaceId, String pId, String pPartType, String pWorkflowModelId, String pMask, List<InstanceAttributeTemplate> pAttributeTemplates, String[] lovNames, List<InstanceAttributeTemplate> pAttributeInstanceTemplates, String[] instanceLovNames, boolean idGenerated, boolean attributesLocked) throws WorkspaceNotFoundException, AccessRightException, PartMasterTemplateAlreadyExistsException, UserNotFoundException, NotAllowedException, CreationException, WorkflowModelNotFoundException, ListOfValuesNotFoundException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceWriteAccess(pWorkspaceId);
        Locale locale = new Locale(user.getLanguage());
        checkNameValidity(pId, locale);

        //Check pMask
        if (pMask != null && !pMask.isEmpty() && !NamingConvention.correctNameMask(pMask)) {
            throw new NotAllowedException(locale, "MaskCreationException");
        }

        PartMasterTemplate template = new PartMasterTemplate(user.getWorkspace(), pId, user, pPartType, pMask, attributesLocked);
        Date now = new Date();
        template.setCreationDate(now);
        template.setIdGenerated(idGenerated);
        LOVDAO lovDAO = new LOVDAO(locale, em);

        List<InstanceAttributeTemplate> attrs = new ArrayList<>();
        for (int i = 0; i < pAttributeTemplates.size(); i++) {
            if (attributesLocked) {
                pAttributeTemplates.get(i).setLocked(attributesLocked);
            }
            attrs.add(pAttributeTemplates.get(i));
            if (pAttributeTemplates.get(i) instanceof ListOfValuesAttributeTemplate) {
                ListOfValuesAttributeTemplate lovAttr = (ListOfValuesAttributeTemplate) pAttributeTemplates.get(i);
                ListOfValuesKey lovKey = new ListOfValuesKey(user.getWorkspaceId(), lovNames[i]);
                lovAttr.setLov(lovDAO.loadLOV(lovKey));
            }
        }
        if (!AttributesConsistencyUtils.isTemplateAttributesValid(attrs, attributesLocked)) {
            throw new NotAllowedException(locale, "NotAllowedException59");
        }
        template.setAttributeTemplates(attrs);

        List<InstanceAttributeTemplate> instanceAttrs = new ArrayList<>();
        for (int i = 0; i < pAttributeInstanceTemplates.size(); i++) {
            instanceAttrs.add(pAttributeInstanceTemplates.get(i));
            if (pAttributeInstanceTemplates.get(i) instanceof ListOfValuesAttributeTemplate) {
                ListOfValuesAttributeTemplate lovAttr = (ListOfValuesAttributeTemplate) pAttributeInstanceTemplates.get(i);
                ListOfValuesKey lovKey = new ListOfValuesKey(user.getWorkspaceId(), instanceLovNames[i]);
                lovAttr.setLov(lovDAO.loadLOV(lovKey));
            }
        }
        if (!AttributesConsistencyUtils.isTemplateAttributesValid(instanceAttrs, false)) {
            throw new NotAllowedException(locale, "NotAllowedException59");
        }
        template.setAttributeInstanceTemplates(instanceAttrs);

        if (pWorkflowModelId != null) {
            WorkflowModel workflowModel = new WorkflowModelDAO(locale, em).loadWorkflowModel(new WorkflowModelKey(user.getWorkspaceId(), pWorkflowModelId));
            template.setWorkflowModel(workflowModel);
        }

        new PartMasterTemplateDAO(locale, em).createPartMTemplate(template);
        return template;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public PartMasterTemplate updatePartMasterTemplate(PartMasterTemplateKey pKey, String pPartType, String pWorkflowModelId, String pMask, List<InstanceAttributeTemplate> pAttributeTemplates, String[] lovNames, List<InstanceAttributeTemplate> pAttributeInstanceTemplates, String[] instanceLovNames, boolean idGenerated, boolean attributesLocked) throws WorkspaceNotFoundException, AccessRightException, PartMasterTemplateNotFoundException, UserNotFoundException, WorkflowModelNotFoundException, UserNotActiveException, ListOfValuesNotFoundException, NotAllowedException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceReadAccess(pKey.getWorkspaceId());
        Locale locale = new Locale(user.getLanguage());

        PartMasterTemplateDAO templateDAO = new PartMasterTemplateDAO(new Locale(user.getLanguage()), em);
        PartMasterTemplate template = templateDAO.loadPartMTemplate(pKey);

        checkPartTemplateWriteAccess(template, user);

        Date now = new Date();
        template.setModificationDate(now);
        template.setAuthor(user);
        template.setPartType(pPartType);
        template.setMask(pMask);
        template.setIdGenerated(idGenerated);
        template.setAttributesLocked(attributesLocked);
        LOVDAO lovDAO = new LOVDAO(locale, em);

        List<InstanceAttributeTemplate> attrs = new ArrayList<>();
        for (int i = 0; i < pAttributeTemplates.size(); i++) {
            if (attributesLocked) {
                pAttributeTemplates.get(i).setLocked(attributesLocked);
            }
            attrs.add(pAttributeTemplates.get(i));
            if (pAttributeTemplates.get(i) instanceof ListOfValuesAttributeTemplate) {
                ListOfValuesAttributeTemplate lovAttr = (ListOfValuesAttributeTemplate) pAttributeTemplates.get(i);
                ListOfValuesKey lovKey = new ListOfValuesKey(user.getWorkspaceId(), lovNames[i]);
                lovAttr.setLov(lovDAO.loadLOV(lovKey));
            }
        }
        if (!AttributesConsistencyUtils.isTemplateAttributesValid(attrs, attributesLocked)) {
            throw new NotAllowedException(locale, "NotAllowedException59");
        }
        template.setAttributeTemplates(attrs);

        List<InstanceAttributeTemplate> instanceAttrs = new ArrayList<>();
        for (int i = 0; i < pAttributeInstanceTemplates.size(); i++) {
            instanceAttrs.add(pAttributeInstanceTemplates.get(i));
            if (pAttributeInstanceTemplates.get(i) instanceof ListOfValuesAttributeTemplate) {
                ListOfValuesAttributeTemplate lovAttr = (ListOfValuesAttributeTemplate) pAttributeInstanceTemplates.get(i);
                ListOfValuesKey lovKey = new ListOfValuesKey(user.getWorkspaceId(), instanceLovNames[i]);
                lovAttr.setLov(lovDAO.loadLOV(lovKey));
            }
        }
        if (!AttributesConsistencyUtils.isTemplateAttributesValid(instanceAttrs, false)) {
            throw new NotAllowedException(locale, "NotAllowedException59");
        }
        template.setAttributeInstanceTemplates(instanceAttrs);

        WorkflowModel workflowModel = null;
        if (pWorkflowModelId != null) {
            workflowModel = new WorkflowModelDAO(locale, em).loadWorkflowModel(new WorkflowModelKey(user.getWorkspaceId(), pWorkflowModelId));
        }
        template.setWorkflowModel(workflowModel);

        return template;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public void deletePartMasterTemplate(PartMasterTemplateKey pKey) throws WorkspaceNotFoundException, AccessRightException, PartMasterTemplateNotFoundException, UserNotFoundException, UserNotActiveException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceReadAccess(pKey.getWorkspaceId());
        PartMasterTemplateDAO templateDAO = new PartMasterTemplateDAO(new Locale(user.getLanguage()), em);

        PartMasterTemplate partMasterTemplate = templateDAO.loadPartMTemplate(pKey);
        checkPartTemplateWriteAccess(partMasterTemplate, user);

        PartMasterTemplate template = templateDAO.removePartMTemplate(pKey);
        BinaryResource file = template.getAttachedFile();
        if (file != null) {
            try {
                storageManager.deleteData(file);
            } catch (StorageException e) {
                LOGGER.log(Level.INFO, null, e);
            }
        }
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public BinaryResource saveFileInTemplate(PartMasterTemplateKey pPartMTemplateKey, String pName, long pSize) throws WorkspaceNotFoundException, NotAllowedException, PartMasterTemplateNotFoundException, FileAlreadyExistsException, UserNotFoundException, UserNotActiveException, CreationException, AccessRightException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceReadAccess(pPartMTemplateKey.getWorkspaceId());
        Locale locale = new Locale(user.getLanguage());
        checkNameFileValidity(pName, locale);

        PartMasterTemplateDAO templateDAO = new PartMasterTemplateDAO(locale, em);
        PartMasterTemplate template = templateDAO.loadPartMTemplate(pPartMTemplateKey);

        checkPartTemplateWriteAccess(template, user);

        BinaryResource binaryResource = null;
        String fullName = template.getWorkspaceId() + "/part-templates/" + template.getId() + "/" + pName;

        BinaryResource bin = template.getAttachedFile();
        if (bin != null && bin.getFullName().equals(fullName)) {
            binaryResource = bin;
        } else if (bin != null && !bin.getFullName().equals(fullName)) {
            try {
                storageManager.deleteData(bin);
            } catch (StorageException e) {
                LOGGER.log(Level.WARNING, "Could not delete attached file", e);
            }
        }

        if (binaryResource == null) {
            binaryResource = new BinaryResource(fullName, pSize, new Date());
            new BinaryResourceDAO(locale, em).createBinaryResource(binaryResource);
            template.setAttachedFile(binaryResource);
        } else {
            binaryResource.setContentLength(pSize);
            binaryResource.setLastModified(new Date());
        }
        return binaryResource;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public PartMasterTemplate removeFileFromTemplate(String pFullName) throws WorkspaceNotFoundException, PartMasterTemplateNotFoundException, AccessRightException, FileNotFoundException, UserNotFoundException, UserNotActiveException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceReadAccess(BinaryResource.parseWorkspaceId(pFullName));
        BinaryResourceDAO binDAO = new BinaryResourceDAO(new Locale(user.getLanguage()), em);
        BinaryResource file = binDAO.loadBinaryResource(pFullName);

        PartMasterTemplate template = binDAO.getPartTemplateHolder(file);
        checkPartTemplateWriteAccess(template, user);

        template.setAttachedFile(null);
        binDAO.removeBinaryResource(file);

        try {
            storageManager.deleteData(file);
        } catch (StorageException e) {
            LOGGER.log(Level.INFO, null, e);
        }
        return template;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public BinaryResource renameFileInTemplate(String pFullName, String pNewName) throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException, FileNotFoundException, UserNotActiveException, FileAlreadyExistsException, CreationException, StorageException, NotAllowedException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceReadAccess(BinaryResource.parseWorkspaceId(pFullName));
        Locale userLocale = new Locale(user.getLanguage());
        checkNameFileValidity(pNewName, userLocale);

        BinaryResourceDAO binDAO = new BinaryResourceDAO(new Locale(user.getLanguage()), em);
        BinaryResource file = binDAO.loadBinaryResource(pFullName);

        PartMasterTemplate template = binDAO.getPartTemplateHolder(file);

        checkPartTemplateWriteAccess(template, user);

        storageManager.renameFile(file, pNewName);

        template.setAttachedFile(null);
        binDAO.removeBinaryResource(file);

        BinaryResource newFile = new BinaryResource(file.getNewFullName(pNewName), file.getContentLength(), file.getLastModified());

        binDAO.createBinaryResource(newFile);
        template.setAttachedFile(newFile);

        return newFile;

    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public List<PartMaster> getPartMasters(String pWorkspaceId, int start, int pMaxResults) throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException, UserNotActiveException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
        return new PartMasterDAO(new Locale(user.getLanguage()), em).getPartMasters(pWorkspaceId, start, pMaxResults);
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public List<PartRevision> getPartRevisions(String pWorkspaceId, int start, int pMaxResults) throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException, UserNotActiveException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
        List<PartRevision> partRevisions;

        if (pMaxResults == 0) {
            partRevisions = new PartRevisionDAO(new Locale(user.getLanguage()), em).getAllPartRevisions(pWorkspaceId);
        } else {
            partRevisions = new PartRevisionDAO(new Locale(user.getLanguage()), em).getPartRevisions(pWorkspaceId, start, pMaxResults);
        }

        List<PartRevision> filteredPartRevisions = new ArrayList<>();

        for (PartRevision partRevision : partRevisions) {
            try {
                checkPartRevisionReadAccess(partRevision.getKey());

                if (isCheckoutByAnotherUser(user, partRevision)) {
                    em.detach(partRevision);
                    partRevision.removeLastIteration();
                }

                filteredPartRevisions.add(partRevision);

            } catch (AccessRightException | PartRevisionNotFoundException e) {
                LOGGER.log(Level.FINER, null, e);
            }
        }
        return filteredPartRevisions;
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID, UserGroupMapping.ADMIN_ROLE_ID})
    @Override
    public int getPartsInWorkspaceCount(String pWorkspaceId) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException, WorkspaceNotEnabledException, AccountNotFoundException {

        int count;

        if (contextManager.isCallerInRole(UserGroupMapping.ADMIN_ROLE_ID)) {
            Account adminAccount = new AccountDAO(em).loadAccount(contextManager.getCallerPrincipalLogin());
            Locale locale = new Locale(adminAccount.getLanguage());
            count = new PartRevisionDAO(locale, em).getTotalNumberOfParts(pWorkspaceId);
        } else {
            User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
            Locale locale = new Locale(user.getLanguage());
            if (user.isAdministrator()) {
                count = new PartRevisionDAO(locale, em).getTotalNumberOfParts(pWorkspaceId);
            } else {
                count = new PartRevisionDAO(locale, em).getPartRevisionCountFiltered(user, pWorkspaceId);
            }
        }

        return count;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public void deletePartRevision(PartRevisionKey partRevisionKey) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, PartRevisionNotFoundException, EntityConstraintException, ESServerException, AccessRightException, WorkspaceNotEnabledException {

        User user = userManager.checkWorkspaceReadAccess(partRevisionKey.getPartMaster().getWorkspace());
        Locale locale = new Locale(user.getLanguage());

        PartMasterDAO partMasterDAO = new PartMasterDAO(locale, em);
        PartRevisionDAO partRevisionDAO = new PartRevisionDAO(locale, em);
        PartUsageLinkDAO partUsageLinkDAO = new PartUsageLinkDAO(locale, em);
        ProductBaselineDAO productBaselineDAO = new ProductBaselineDAO(locale, em);

        ConfigurationItemDAO configurationItemDAO = new ConfigurationItemDAO(locale, em);

        PartRevision partR = partRevisionDAO.loadPartR(partRevisionKey);
        if (!hasPartOrWorkspaceWriteAccess(user, partR)) {
            throw new AccessRightException(locale, user);
        }
        PartMaster partMaster = partR.getPartMaster();
        boolean isLastRevision = partMaster.getPartRevisions().size() == 1;

        //TODO all the 3 removal restrictions may be performed
        //more precisely on PartRevision rather on PartMaster
        // check if part is linked to a product
        if (configurationItemDAO.isPartMasterLinkedToConfigurationItem(partMaster)) {
            throw new EntityConstraintException(locale, "EntityConstraintException1");
        }
        // check if this part is in a partUsage
        if (partUsageLinkDAO.hasPartUsages(partMaster.getWorkspaceId(), partMaster.getNumber())) {
            throw new EntityConstraintException(locale, "EntityConstraintException2");
        }

        // check if this part is in a partSubstitute
        if (partUsageLinkDAO.hasPartSubstitutes(partMaster.getWorkspaceId(), partMaster.getNumber())) {
            throw new EntityConstraintException(locale, "EntityConstraintException22");
        }

        // check if part is baselined
        if (productBaselineDAO.existBaselinedPart(partMaster.getWorkspaceId(), partMaster.getNumber())) {
            throw new EntityConstraintException(locale, "EntityConstraintException5");
        }

        ChangeItemDAO changeItemDAO = new ChangeItemDAO(locale, em);
        if (changeItemDAO.hasChangeItems(partRevisionKey)) {
            throw new EntityConstraintException(locale, "EntityConstraintException21");
        }

        // delete ElasticSearch Index for this revision iteration
        for (PartIteration partIteration : partR.getPartIterations()) {
            esIndexer.delete(partIteration);
            // Remove ElasticSearch Index for this PartIteration
        }

        partRevisionEvent.select(new AnnotationLiteral<Removed>() {
        }).fire(new PartRevisionEvent(partR));

        if (isLastRevision) {
            partMasterDAO.removePartM(partMaster);
        } else {
            partMaster.removeRevision(partR);
            partRevisionDAO.removeRevision(partR);
        }

        // delete CAD and other files attached with this partMaster
        for (PartIteration partIteration : partR.getPartIterations()) {
            try {
                removeCADFile(partIteration);
                removeAttachedFiles(partIteration);
            } catch (PartIterationNotFoundException e) {
                LOGGER.log(Level.INFO, null, e);
            }
        }
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public int getNumberOfIteration(PartRevisionKey partRevisionKey) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, PartRevisionNotFoundException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceReadAccess(partRevisionKey.getPartMaster().getWorkspace());
        return new PartRevisionDAO(new Locale(user.getLanguage()), em).loadPartR(partRevisionKey).getLastIterationNumber();
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public PartRevision createPartRevision(PartRevisionKey revisionKey, String pDescription, String pWorkflowModelId, Map<String, String> pACLUserEntries, Map<String, String> pACLUserGroupEntries, Map<String, Collection<String>> userRoleMapping, Map<String, Collection<String>> groupRoleMapping) throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException, PartRevisionNotFoundException, NotAllowedException, FileAlreadyExistsException, CreationException, RoleNotFoundException, WorkflowModelNotFoundException, PartRevisionAlreadyExistsException, UserGroupNotFoundException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceWriteAccess(revisionKey.getPartMaster().getWorkspace());
        Locale locale = new Locale(user.getLanguage());

        PartRevisionDAO partRevisionDAO = new PartRevisionDAO(locale, em);

        PartRevision originalPartR = partRevisionDAO.loadPartR(revisionKey);

        if (originalPartR.isCheckedOut()) {
            throw new NotAllowedException(locale, "NotAllowedException40");
        }

        if (originalPartR.getNumberOfIterations() == 0) {
            throw new NotAllowedException(locale, "NotAllowedException41");
        }

        PartRevision partR = originalPartR.getPartMaster().createNextRevision(user);

        PartIteration lastPartI = originalPartR.getLastIteration();
        PartIteration firstPartI = partR.createNextIteration(user);


        if (lastPartI != null) {

            BinaryResourceDAO binDAO = new BinaryResourceDAO(locale, em);
            for (BinaryResource sourceFile : lastPartI.getAttachedFiles()) {
                String fileName = sourceFile.getName();
                long length = sourceFile.getContentLength();
                Date lastModified = sourceFile.getLastModified();
                String fullName = partR.getWorkspaceId() + "/parts/" + partR.getPartNumber() + "/" + partR.getVersion() + "/1/" + fileName;
                BinaryResource targetFile = new BinaryResource(fullName, length, lastModified);
                binDAO.createBinaryResource(targetFile);
                firstPartI.addAttachedFile(targetFile);
                try {
                    storageManager.copyData(sourceFile, targetFile);
                } catch (StorageException e) {
                    LOGGER.log(Level.INFO, null, e);
                }
            }

            // Copy usage links
            // Create new p2p links

            List<PartUsageLink> newComponents = new LinkedList<>();
            List<PartUsageLink> oldComponents = lastPartI.getComponents();
            for (PartUsageLink usage : lastPartI.getComponents()) {
                PartUsageLink newUsage = usage.clone();
                //PartUsageLink is shared among PartIteration hence there is no cascade persist
                //so we need to persist them explicitly
                em.persist(newUsage);
                newComponents.add(newUsage);
            }
            firstPartI.setComponents(newComponents);
            //flush to ensure the new PartUsageLinks have their id generated
            em.flush();
            PathToPathLinkDAO pathToPathLinkDAO = new PathToPathLinkDAO(locale, em);
            pathToPathLinkDAO.cloneAndUpgradePathToPathLinks(oldComponents, newComponents);

            // copy geometries
            for (Geometry sourceFile : lastPartI.getGeometries()) {
                String fileName = sourceFile.getName();
                long length = sourceFile.getContentLength();
                int quality = sourceFile.getQuality();
                Date lastModified = sourceFile.getLastModified();
                String fullName = partR.getWorkspaceId() + "/parts/" + partR.getPartNumber() + "/" + partR.getVersion() + "/1/" + fileName;
                Geometry targetFile = new Geometry(quality, fullName, length, lastModified);
                binDAO.createBinaryResource(targetFile);
                firstPartI.addGeometry(targetFile);
                try {
                    storageManager.copyData(sourceFile, targetFile);
                } catch (StorageException e) {
                    LOGGER.log(Level.INFO, null, e);
                }
            }

            BinaryResource nativeCADFile = lastPartI.getNativeCADFile();
            if (nativeCADFile != null) {
                String fileName = nativeCADFile.getName();
                long length = nativeCADFile.getContentLength();
                Date lastModified = nativeCADFile.getLastModified();
                String fullName = partR.getWorkspaceId() + "/parts/" + partR.getPartNumber() + "/" + partR.getVersion() + "/1/nativecad/" + fileName;
                BinaryResource targetFile = new BinaryResource(fullName, length, lastModified);
                binDAO.createBinaryResource(targetFile);
                firstPartI.setNativeCADFile(targetFile);
                try {
                    storageManager.copyData(nativeCADFile, targetFile);
                } catch (StorageException e) {
                    LOGGER.log(Level.INFO, null, e);
                }
            }


            Set<DocumentLink> links = new HashSet<>();
            for (DocumentLink link : lastPartI.getLinkedDocuments()) {
                DocumentLink newLink = link.clone();
                links.add(newLink);
            }
            firstPartI.setLinkedDocuments(links);

            List<InstanceAttribute> attrs = new ArrayList<>();
            for (InstanceAttribute attr : lastPartI.getInstanceAttributes()) {
                InstanceAttribute clonedAttribute = attr.clone();
                attrs.add(clonedAttribute);
            }
            firstPartI.setInstanceAttributes(attrs);

        }


        Collection<Task> runningTasks = null;
        if (pWorkflowModelId != null) {

            UserDAO userDAO = new UserDAO(locale, em);
            UserGroupDAO groupDAO = new UserGroupDAO(locale, em);
            RoleDAO roleDAO = new RoleDAO(locale, em);

            Map<Role, Collection<User>> roleUserMap = new HashMap<>();
            for (Map.Entry<String, Collection<String>> pair : userRoleMapping.entrySet()) {
                String roleName = pair.getKey();
                Collection<String> userLogins = pair.getValue();
                Role role = roleDAO.loadRole(new RoleKey(originalPartR.getWorkspaceId(), roleName));
                Set<User> users = new HashSet<>();
                roleUserMap.put(role, users);
                for (String login : userLogins) {
                    User u = userDAO.loadUser(new UserKey(originalPartR.getWorkspaceId(), login));
                    users.add(u);
                }
            }

            Map<Role, Collection<UserGroup>> roleGroupMap = new HashMap<>();
            for (Map.Entry<String, Collection<String>> pair : groupRoleMapping.entrySet()) {
                String roleName = pair.getKey();
                Collection<String> groupIds = pair.getValue();
                Role role = roleDAO.loadRole(new RoleKey(originalPartR.getWorkspaceId(), roleName));
                Set<UserGroup> groups = new HashSet<>();
                roleGroupMap.put(role, groups);
                for (String groupId : groupIds) {
                    UserGroup g = groupDAO.loadUserGroup(new UserGroupKey(originalPartR.getWorkspaceId(), groupId));
                    groups.add(g);
                }
            }

            WorkflowModel workflowModel = new WorkflowModelDAO(locale, em).loadWorkflowModel(new WorkflowModelKey(user.getWorkspaceId(), pWorkflowModelId));
            Workflow workflow = workflowModel.createWorkflow(roleUserMap, roleGroupMap);
            partR.setWorkflow(workflow);

            for (Task task : workflow.getTasks()) {
                if (!task.hasPotentialWorker()) {
                    throw new NotAllowedException(locale, "NotAllowedException56");
                }
            }

            runningTasks = workflow.getRunningTasks();
            for (Task runningTask : runningTasks) {
                runningTask.start();
            }
        }

        partR.setDescription(pDescription);

        if (pACLUserEntries != null && !pACLUserEntries.isEmpty() || pACLUserGroupEntries != null && !pACLUserGroupEntries.isEmpty()) {
            ACL acl = new ACLFactory(em).createACL(user.getWorkspace().getId(), pACLUserEntries, pACLUserGroupEntries);
            partR.setACL(acl);
        }

        Date now = new Date();
        partR.setCreationDate(now);
        partR.setCheckOutUser(user);
        partR.setCheckOutDate(now);
        firstPartI.setCreationDate(now);
        firstPartI.setModificationDate(now);

        partRevisionDAO.createPartR(partR);

        if (runningTasks != null) {
            mailer.sendApproval(runningTasks, partR);
        }

        return partR;

    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public String generateId(String pWorkspaceId, String pPartMTemplateId) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException, PartMasterTemplateNotFoundException, WorkspaceNotEnabledException {

        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
        Locale locale = new Locale(user.getLanguage());
        PartMasterTemplateKey partMasterTemplateKey = new PartMasterTemplateKey(user.getWorkspaceId(), pPartMTemplateId);
        PartMasterTemplate template = new PartMasterTemplateDAO(locale, em).loadPartMTemplate(partMasterTemplateKey);

        String newId = null;
        try {
            String latestId = new PartMasterDAO(locale, em).findLatestPartMId(pWorkspaceId, template.getPartType());
            String inputMask = template.getMask();
            String convertedMask = Tools.convertMask(inputMask);
            newId = Tools.increaseId(latestId, convertedMask);
        } catch (ParseException ex) {
            LOGGER.log(Level.WARNING, "Different mask has been used for the same document type", ex);
        } catch (NoResultException ex) {
            LOGGER.log(Level.FINE, "No document of the specified type has been created", ex);
        }
        return newId;

    }


    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID, UserGroupMapping.ADMIN_ROLE_ID})
    @Override
    public long getDiskUsageForPartsInWorkspace(String pWorkspaceId) throws WorkspaceNotFoundException, AccessRightException, AccountNotFoundException {
        Account account = userManager.checkAdmin(pWorkspaceId);
        return new PartMasterDAO(new Locale(account.getLanguage()), em).getDiskUsageForPartsInWorkspace(pWorkspaceId);
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID, UserGroupMapping.ADMIN_ROLE_ID})
    @Override
    public long getDiskUsageForPartTemplatesInWorkspace(String pWorkspaceId) throws WorkspaceNotFoundException, AccessRightException, AccountNotFoundException {
        Account account = userManager.checkAdmin(pWorkspaceId);
        return new PartMasterDAO(new Locale(account.getLanguage()), em).getDiskUsageForPartTemplatesInWorkspace(pWorkspaceId);
    }

    @Override
    public PartRevision[] getCheckedOutPartRevisions(String pWorkspaceId) throws WorkspaceNotFoundException, AccessRightException, AccountNotFoundException, UserNotFoundException, UserNotActiveException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
        List<PartRevision> partRevisions = new PartRevisionDAO(new Locale(user.getLanguage()), em).findCheckedOutPartRevisionsForUser(pWorkspaceId, user.getLogin());
        return partRevisions.toArray(new PartRevision[partRevisions.size()]);
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID, UserGroupMapping.ADMIN_ROLE_ID})
    @Override
    public PartRevision[] getAllCheckedOutPartRevisions(String pWorkspaceId) throws WorkspaceNotFoundException, AccessRightException, AccountNotFoundException {
        Account account = userManager.checkAdmin(pWorkspaceId);
        List<PartRevision> partRevisions = new PartRevisionDAO(new Locale(account.getLanguage()), em).findAllCheckedOutPartRevisions(pWorkspaceId);
        return partRevisions.toArray(new PartRevision[partRevisions.size()]);
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID})
    @Override
    public SharedPart createSharedPart(PartRevisionKey pPartRevisionKey, String pPassword, Date pExpireDate) throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException, PartRevisionNotFoundException, UserNotActiveException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceWriteAccess(pPartRevisionKey.getPartMaster().getWorkspace());
        SharedPart sharedPart = new SharedPart(user.getWorkspace(), user, pExpireDate, pPassword, getPartRevision(pPartRevisionKey));
        SharedEntityDAO sharedEntityDAO = new SharedEntityDAO(new Locale(user.getLanguage()), em);
        sharedEntityDAO.createSharedPart(sharedPart);
        return sharedPart;
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID})
    @Override
    public void deleteSharedPart(SharedEntityKey pSharedEntityKey) throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException, SharedEntityNotFoundException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceWriteAccess(pSharedEntityKey.getWorkspace());
        SharedEntityDAO sharedEntityDAO = new SharedEntityDAO(new Locale(user.getLanguage()), em);
        SharedPart sharedPart = sharedEntityDAO.loadSharedPart(pSharedEntityKey.getUuid());
        sharedEntityDAO.deleteSharedPart(sharedPart);
    }


    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID, UserGroupMapping.ADMIN_ROLE_ID})
    @Override
    public boolean canAccess(PartRevisionKey partRKey) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, PartRevisionNotFoundException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceReadAccess(partRKey.getPartMaster().getWorkspace());
        return canUserAccess(user, partRKey);
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID, UserGroupMapping.ADMIN_ROLE_ID})
    @Override
    public boolean canAccess(PartIterationKey partIKey) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, PartRevisionNotFoundException, PartIterationNotFoundException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceReadAccess(partIKey.getWorkspaceId());
        return canUserAccess(user, partIKey);
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID, UserGroupMapping.ADMIN_ROLE_ID})
    @Override
    public boolean canUserAccess(User user, PartRevisionKey partRKey) throws PartRevisionNotFoundException {
        PartRevision partRevision = new PartRevisionDAO(new Locale(user.getLanguage()), em).loadPartR(partRKey);
        return hasPartRevisionReadAccess(user, partRevision);
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID, UserGroupMapping.ADMIN_ROLE_ID})
    @Override
    public boolean canUserAccess(User user, PartIterationKey partIKey) throws PartRevisionNotFoundException, PartIterationNotFoundException {
        PartRevisionDAO partRevisionDAO = new PartRevisionDAO(new Locale(user.getLanguage()), em);
        PartRevision partR = partRevisionDAO.loadPartR(partIKey.getPartRevision());
        return hasPartRevisionReadAccess(user, partR) &&
                (!partRevisionDAO.isCheckedOutIteration(partIKey) ||
                        user.equals(partR.getCheckOutUser()));
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID, UserGroupMapping.ADMIN_ROLE_ID})
    @Override
    public User checkPartRevisionReadAccess(PartRevisionKey partRevisionKey) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, PartRevisionNotFoundException, AccessRightException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceReadAccess(partRevisionKey.getPartMaster().getWorkspace());
        if (!canUserAccess(user, partRevisionKey)) {
            throw new AccessRightException(new Locale(user.getLanguage()), user);
        }
        return user;
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID, UserGroupMapping.ADMIN_ROLE_ID})
    @Override
    public boolean canWrite(PartRevisionKey partRKey) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, PartRevisionNotFoundException, AccessRightException, WorkspaceNotEnabledException {

        String workspace = partRKey.getPartMaster().getWorkspace();

        User user = userManager.checkWorkspaceReadAccess(workspace);

        if (user.isAdministrator()) {
            return true;
        }

        PartRevision partRevision;

        try {
            partRevision = getPartRevision(partRKey);
        } catch (AccessRightException e) {
            return false;
        }

        if (partRevision.getACL() != null) {
            if (partRevision.getACL().hasWriteAccess(user)) {
                return true;
            }
            return false;
        }

        try {
            userManager.checkWorkspaceWriteAccess(workspace);
            return true;
        } catch (AccessRightException e) {
            return false;
        }

    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public Component filterProductStructure(ConfigurationItemKey ciKey, ProductStructureFilter filter, List<PartLink> path, Integer pDepth) throws ConfigurationItemNotFoundException, WorkspaceNotFoundException, NotAllowedException, UserNotFoundException, UserNotActiveException, PartUsageLinkNotFoundException, AccessRightException, PartMasterNotFoundException, EntityConstraintException, WorkspaceNotEnabledException {

        User user = userManager.checkWorkspaceReadAccess(ciKey.getWorkspace());
        Locale locale = new Locale(user.getLanguage());


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
                // Unused here
                return true;
            }

            @Override
            public void onUnresolvedVersion(PartMaster partMaster) {
                // Unused here
            }
        };

        if (path == null) {
            ConfigurationItem ci = new ConfigurationItemDAO(locale, em).loadConfigurationItem(ciKey);
            psFilterVisitor.visit(ci.getDesignItem(), pDepth);
        } else {
            psFilterVisitor.visit(path, pDepth);
        }

        return psFilterVisitor.getComponent();

    }


    @Override
    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID, UserGroupMapping.ADMIN_ROLE_ID})
    public Set<PartRevision> getWritablePartRevisionsFromPath(ConfigurationItemKey configurationItemKey, String path) throws EntityConstraintException, PartMasterNotFoundException, NotAllowedException, UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, ConfigurationItemNotFoundException, PartUsageLinkNotFoundException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceReadAccess(configurationItemKey.getWorkspace());
        Set<PartRevision> partRevisions = new HashSet<>();
        PSFilterVisitor psFilterVisitor = new PSFilterVisitor(em, user, new WIPPSFilter(user)) {
            @Override
            public void onIndeterminateVersion(PartMaster partMaster, List<PartIteration> partIterations) throws NotAllowedException {

            }

            @Override
            public void onUnresolvedVersion(PartMaster partMaster) throws NotAllowedException {

            }

            @Override
            public void onIndeterminatePath(List<PartLink> pCurrentPath, List<PartIteration> pCurrentPathPartIterations) throws NotAllowedException {

            }

            @Override
            public void onUnresolvedPath(List<PartLink> pCurrentPath, List<PartIteration> partIterations) throws NotAllowedException {

            }

            @Override
            public void onBranchDiscovered(List<PartLink> pCurrentPath, List<PartIteration> copyPartIteration) {

            }

            @Override
            public void onOptionalPath(List<PartLink> path, List<PartIteration> partIterations) {

            }

            @Override
            public boolean onPathWalk(List<PartLink> path, List<PartMaster> parts) {
                PartMaster pm = parts.get(parts.size() - 1);
                try {
                    if (!hasPartRevisionReadAccess(user, pm.getLastRevision())) {
                        //Don't visit this branch anymore
                        return false;
                    }
                    if (!hasPartOrWorkspaceWriteAccess(user, pm.getLastRevision())) {
                        return true;
                    }
                    partRevisions.add(pm.getLastRevision());

                } catch (WorkspaceNotFoundException | WorkspaceNotEnabledException e) {
                    LOGGER.log(Level.SEVERE, "Could not check access to part revision", e);
                    return false;
                }
                return true;
            }
        };

        psFilterVisitor.visit(decodePath(configurationItemKey, path), null);
        return partRevisions;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public Component filterProductStructureOnLinkType(ConfigurationItemKey ciKey, ProductStructureFilter filter, String configSpecType, String path, String linkType) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ConfigurationItemNotFoundException, PartUsageLinkNotFoundException, ProductInstanceMasterNotFoundException, BaselineNotFoundException, WorkspaceNotEnabledException {

        User user = userManager.checkWorkspaceReadAccess(ciKey.getWorkspace());
        Locale locale = new Locale(user.getLanguage());
        ConfigurationItem ci = new ConfigurationItemDAO(locale, em).loadConfigurationItem(ciKey);

        PathToPathLinkDAO pathToPathLinkDAO = new PathToPathLinkDAO(locale, em);

        Component component = new Component();
        component.setUser(user);
        component.setComponents(new ArrayList<>());

        Set<String> links = new HashSet<>();

        if (path == null) { // If path is null => get Root links embedded as subComponents of a virtual component
            List<PathToPathLink> rootPathToPathLinks;

            if (configSpecType.startsWith("pi-")) {
                String serialNumber = configSpecType.substring(3);
                ProductInstanceMasterKey productInstanceMasterKey = new ProductInstanceMasterKey(serialNumber, ciKey.getWorkspace(), ciKey.getId());
                ProductInstanceIteration pii = new ProductInstanceMasterDAO(locale, em).loadProductInstanceMaster(productInstanceMasterKey).getLastIteration();
                rootPathToPathLinks = pathToPathLinkDAO.findRootPathToPathLinks(pii, linkType);

            } else if (!"wip".equals(configSpecType) && !"latest".equals(configSpecType) && !"released".equals(configSpecType)) {
                int baselineId = 0;
                try {
                    baselineId = Integer.parseInt(configSpecType);
                } catch (NumberFormatException e) {
                    LOGGER.log(Level.FINEST, null, e);
                }
                ProductBaseline pb = new ProductBaselineDAO(locale, em).loadBaseline(baselineId);
                rootPathToPathLinks = pathToPathLinkDAO.findRootPathToPathLinks(pb, linkType);

            } else {
                rootPathToPathLinks = pathToPathLinkDAO.findRootPathToPathLinks(ci, linkType);
            }

            if (rootPathToPathLinks.size() > 0) {
                for (PathToPathLink link : rootPathToPathLinks) {
                    links.add(link.getSourcePath());
                }

                PartMaster virtualPartMaster = new PartMaster(ci.getWorkspace(), linkType, user);
                PartRevision virtualPartRevision = new PartRevision(virtualPartMaster, user);
                virtualPartMaster.getPartRevisions().add(virtualPartRevision);
                PartIteration virtualPartIteration = new PartIteration(virtualPartRevision, user);
                virtualPartRevision.getPartIterations().add(virtualPartIteration);

                component.setPartMaster(virtualPartMaster);
                component.setRetainedIteration(virtualPartIteration);
                component.setPath(new ArrayList<>());
                component.setVirtual(true);

                component.getPath().add(new PartLink() {
                    @Override
                    public int getId() {
                        return 0;
                    }

                    @Override
                    public Character getCode() {
                        return null;
                    }

                    @Override
                    public String getFullId() {
                        return null;
                    }

                    @Override
                    public double getAmount() {
                        return 1;
                    }

                    @Override
                    public String getUnit() {
                        return null;
                    }

                    @Override
                    public String getComment() {
                        return null;
                    }

                    @Override
                    public boolean isOptional() {
                        return false;
                    }

                    @Override
                    public PartMaster getComponent() {
                        return virtualPartMaster;
                    }

                    @Override
                    public List<PartSubstituteLink> getSubstitutes() {
                        return null;
                    }

                    @Override
                    public String getReferenceDescription() {
                        return linkType;
                    }

                    @Override
                    public List<CADInstance> getCadInstances() {
                        return null;
                    }
                });
            }

        } else { // If path is not null => get next path to path links

            if ("wip".equals(configSpecType) || "latest".equals(configSpecType) || "released".equals(configSpecType)) {
                List<PathToPathLink> sourcesPathToPathLinksInProduct = pathToPathLinkDAO.getSourcesPathToPathLinksInProduct(ci, linkType, path);
                for (PathToPathLink link : sourcesPathToPathLinksInProduct) {
                    links.add(link.getTargetPath());
                }
                List<PartLink> decodedSourcePath = decodePath(ciKey, path);
                PartLink link = decodedSourcePath.get(decodedSourcePath.size() - 1);
                List<PartIteration> partIterations = filter.filter(link.getComponent());
                PartIteration retainedIteration = partIterations.get(partIterations.size() - 1);

                component.setPath(decodedSourcePath);
                component.setPartMaster(link.getComponent());
                component.setRetainedIteration(retainedIteration);

            } else {
                ProductBaseline productBaseline;
                ProductBaselineDAO productBaselineDAO = new ProductBaselineDAO(locale, em);

                if (configSpecType.startsWith("pi-")) {
                    String serialNumber = configSpecType.substring(3);
                    productBaseline = productBaselineDAO.findLastBaselineWithSerialNumber(ciKey, serialNumber);

                } else {
                    int baselineId = 0;
                    try {
                        baselineId = Integer.parseInt(configSpecType);
                    } catch (NumberFormatException e) {
                        LOGGER.log(Level.FINEST, null, e);
                    }
                    productBaseline = productBaselineDAO.loadBaseline(baselineId);
                }

                List<PathToPathLink> sourcesPathToPathLinksInProduct = pathToPathLinkDAO.getSourcesPathToPathLinksInBaseline(productBaseline, linkType, path);
                for (PathToPathLink link : sourcesPathToPathLinksInProduct) {
                    links.add(link.getTargetPath());
                }
                List<PartLink> decodedSourcePath = decodePath(ciKey, path);
                PartLink link = decodedSourcePath.get(decodedSourcePath.size() - 1);
                List<PartIteration> partIterations = filter.filter(link.getComponent());
                PartIteration retainedIteration = partIterations.get(partIterations.size() - 1);

                component.setPath(decodedSourcePath);
                component.setPartMaster(link.getComponent());
                component.setRetainedIteration(retainedIteration);
            }
        }

        // Iterate the list and populate sub components

        for (String link : links) {
            Component subComponent = new Component();

            List<PartLink> decodedPath = decodePath(ciKey, link);
            subComponent.setPath(decodedPath);

            PartLink partLink = decodedPath.get(decodedPath.size() - 1);
            subComponent.setPartMaster(partLink.getComponent());

            // TODO: determine if we do not need to add the subcomponent if there is no retainedIteration
            List<PartIteration> partIterations = filter.filter(partLink.getComponent());
            if (partIterations.size() > 0) {
                PartIteration retainedIteration = partIterations.get(partIterations.size() - 1);
                subComponent.setRetainedIteration(retainedIteration);
            }

            subComponent.setUser(user);
            subComponent.setComponents(new ArrayList<>());
            component.addComponent(subComponent);
        }

        return component;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public List<PartLink> decodePath(ConfigurationItemKey ciKey, String path) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, PartUsageLinkNotFoundException, ConfigurationItemNotFoundException, WorkspaceNotEnabledException {

        User user = userManager.checkWorkspaceReadAccess(ciKey.getWorkspace());

        if (path == null) {
            throw new IllegalArgumentException("Path cannot be null");
        }
        List<PartLink> decodedPath = new ArrayList<>();

        PartLink rootPartUsageLink = getRootPartUsageLink(ciKey);
        decodedPath.add(rootPartUsageLink);

        if ("-1".equals(path)) {
            return decodedPath;
        }

        // Remove the -1- in front of string
        String[] split = path.substring(3).split("-");

        for (String codeAndId : split) {

            int id = Integer.valueOf(codeAndId.substring(1));

            if (codeAndId.startsWith("u")) {
                decodedPath.add(getPartUsageLink(user, id));
            } else if (codeAndId.startsWith("s")) {
                decodedPath.add(getPartSubstituteLink(user, id));
            } else {
                throw new IllegalArgumentException("Missing code");
            }

        }

        return decodedPath;
    }


    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public List<PartRevision> searchPartRevisions(String workspaceId, Query query) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceReadAccess(workspaceId);
        Locale locale = new Locale(user.getLanguage());

        WorkspaceDAO workspaceDAO = new WorkspaceDAO(locale, em);
        Workspace workspace = workspaceDAO.loadWorkspace(workspaceId);

        QueryDAO queryDAO = new QueryDAO(locale, em);
        List<PartRevision> parts = queryDAO.runQuery(workspace, query);

        ListIterator<PartRevision> ite = parts.listIterator();

        while (ite.hasNext()) {
            PartRevision partR = ite.next();

            if (isCheckoutByAnotherUser(user, partR)) {
                em.detach(partR);
                partR.removeLastIteration();
            }

            if (partR.getLastIteration() != null && !hasPartRevisionReadAccess(user, partR)) {
                ite.remove();
            }
        }

        return parts;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public Query getQuery(String workspaceId, int queryId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, WorkspaceNotEnabledException {

        User user = userManager.checkWorkspaceReadAccess(workspaceId);

        QueryDAO queryDAO = new QueryDAO(new Locale(user.getLanguage()), em);
        Query query = queryDAO.loadQuery(queryId);

        if (!query.getAuthor().getWorkspace().getId().equals(workspaceId)) {
            userManager.checkWorkspaceReadAccess(query.getAuthor().getWorkspace().getId());
        }

        return query;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public void createQuery(String workspaceId, Query query) throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException, QueryAlreadyExistsException, CreationException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceWriteAccess(workspaceId);
        Locale locale = new Locale(user.getLanguage());
        QueryDAO queryDAO = new QueryDAO(locale, em);

        Query existingQuery = queryDAO.findQueryByName(workspaceId, query.getName());
        if (existingQuery != null) {
            deleteQuery(workspaceId, existingQuery.getId());
        }

        query.setAuthor(user);
        query.setCreationDate(new Date());
        queryDAO.createQuery(query);
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public void deleteQuery(String workspaceId, int queryId) throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceWriteAccess(workspaceId);
        Locale locale = new Locale(user.getLanguage());
        QueryDAO queryDAO = new QueryDAO(locale, em);
        Query query = queryDAO.loadQuery(queryId);

        Workspace workspace = query.getAuthor().getWorkspace();
        User userInQueryWorkspace = userManager.checkWorkspaceWriteAccess(workspace.getId());

        if (query.getAuthor().equals(userInQueryWorkspace) || userInQueryWorkspace.isAdministrator()) {
            queryDAO.removeQuery(query);
        } else {
            throw new AccessRightException(locale, userInQueryWorkspace);
        }
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public List<Query> getQueries(String workspaceId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceReadAccess(workspaceId);
        QueryDAO queryDAO = new QueryDAO(new Locale(user.getLanguage()), em);
        return queryDAO.loadQueries(workspaceId);
    }

    private PartUsageLink getPartUsageLink(User user, int id) throws PartUsageLinkNotFoundException {
        PartUsageLinkDAO partUsageLinkDAO = new PartUsageLinkDAO(new Locale(user.getLanguage()), em);
        return partUsageLinkDAO.loadPartUsageLink(id);
    }

    private PartSubstituteLink getPartSubstituteLink(User user, int id) throws PartUsageLinkNotFoundException {
        PartUsageLinkDAO partUsageLinkDAO = new PartUsageLinkDAO(new Locale(user.getLanguage()), em);
        return partUsageLinkDAO.loadPartSubstituteLink(id);
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public PartLink getRootPartUsageLink(ConfigurationItemKey pKey) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ConfigurationItemNotFoundException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceReadAccess(pKey.getWorkspace());
        ConfigurationItem ci = new ConfigurationItemDAO(new Locale(user.getLanguage()), em).loadConfigurationItem(pKey);

        return new PartLink() {
            @Override
            public int getId() {
                return 1;
            }

            @Override
            public Character getCode() {
                return '-';
            }

            @Override
            public String getFullId() {
                return "-1";
            }

            @Override
            public double getAmount() {
                return 1;
            }

            @Override
            public String getUnit() {
                return null;
            }

            @Override
            public String getComment() {
                return null;
            }

            @Override
            public boolean isOptional() {
                return false;
            }

            @Override
            public PartMaster getComponent() {
                return ci.getDesignItem();
            }

            @Override
            public List<PartSubstituteLink> getSubstitutes() {
                return null;
            }

            @Override
            public String getReferenceDescription() {
                return null;
            }

            @Override
            public List<CADInstance> getCadInstances() {
                List<CADInstance> cads = new ArrayList<>();
                CADInstance cad = new CADInstance(0d, 0d, 0d, 0d, 0d, 0d);
                cad.setId(0);
                cads.add(cad);
                return cads;
            }
        };

    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID, UserGroupMapping.ADMIN_ROLE_ID})
    @Override
    public void checkCyclicAssemblyForPartIteration(PartIteration partIteration) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, NotAllowedException, EntityConstraintException, PartMasterNotFoundException, WorkspaceNotEnabledException {

        PartMaster partMaster = partIteration.getPartRevision().getPartMaster();
        Workspace workspace = partMaster.getWorkspace();

        User user = userManager.checkWorkspaceReadAccess(workspace.getId());

        // Navigate the WIP
        PSFilterVisitor psFilterVisitor = new PSFilterVisitor(em, user, new UpdatePartIterationPSFilter(user, partIteration)) {
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
                // Unused here
                return true;
            }

            @Override
            public void onUnresolvedVersion(PartMaster partMaster) {
                // Unused here
            }
        };

        psFilterVisitor.visit(partMaster, -1);

    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public ProductStructureFilter getLatestCheckedInPSFilter(String workspaceId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceReadAccess(workspaceId);
        return new LatestPSFilter(user);
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public boolean hasModificationNotification(ConfigurationItemKey ciKey) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ConfigurationItemNotFoundException, NotAllowedException, EntityConstraintException, PartMasterNotFoundException, WorkspaceNotEnabledException {


        User user = userManager.checkWorkspaceReadAccess(ciKey.getWorkspace());
        Locale locale = new Locale(user.getLanguage());
        ConfigurationItemDAO configurationItemDAO = new ConfigurationItemDAO(locale, em);
        ConfigurationItem configurationItem = configurationItemDAO.loadConfigurationItem(ciKey);

        ModificationNotificationDAO modificationNotificationDAO = new ModificationNotificationDAO(em);

        List<String> visitedPartNumbers = new ArrayList<>();
        LatestPSFilter latestPSFilter = new LatestPSFilter(user, true);
        final boolean[] hasModificationNotification = {false};

        PSFilterVisitor psFilterVisitor = new PSFilterVisitor(em, user, latestPSFilter) {
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
                PartMaster partMaster = parts.get(parts.size() - 1);
                if (!visitedPartNumbers.contains(partMaster.getNumber()) && !hasModificationNotification[0]) {
                    visitedPartNumbers.add(partMaster.getNumber());
                    List<PartIteration> partIterations = latestPSFilter.filter(partMaster);
                    // As we use a latest checked-in filter, partIterations array can be empty
                    if (!partIterations.isEmpty()) {
                        PartIteration partIteration = partIterations.get(partIterations.size() - 1);
                        if (modificationNotificationDAO.hasModificationNotifications(partIteration.getKey())) {
                            hasModificationNotification[0] = true;
                        }
                    }
                }
                return true;
            }

            @Override
            public void onUnresolvedVersion(PartMaster partMaster) {
                // Unused here
            }
        };

        psFilterVisitor.visit(configurationItem.getDesignItem(), -1);

        return hasModificationNotification[0];
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public List<InstanceAttribute> getPartIterationsInstanceAttributesInWorkspace(String workspaceId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, WorkspaceNotEnabledException {
        userManager.checkWorkspaceReadAccess(workspaceId);
        InstanceAttributeDAO instanceAttributeDAO = new InstanceAttributeDAO(em);
        return instanceAttributeDAO.getPartIterationsInstanceAttributesInWorkspace(workspaceId);
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public List<InstanceAttribute> getPathDataInstanceAttributesInWorkspace(String workspaceId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, WorkspaceNotEnabledException {
        userManager.checkWorkspaceReadAccess(workspaceId);
        InstanceAttributeDAO instanceAttributeDAO = new InstanceAttributeDAO(em);
        return instanceAttributeDAO.getPathDataInstanceAttributesInWorkspace(workspaceId);
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public List<QueryResultRow> filterProductBreakdownStructure(String workspaceId, Query query) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, BaselineNotFoundException, ProductInstanceMasterNotFoundException, ConfigurationItemNotFoundException, NotAllowedException, PartMasterNotFoundException, EntityConstraintException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceReadAccess(workspaceId);
        List<QueryResultRow> rows = new ArrayList<>();
        for (QueryContext queryContext : query.getContexts()) {
            rows.addAll(filterPBS(workspaceId, queryContext, user));
        }
        return rows;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public List<BinaryResource> getBinaryResourceFromBaseline(int baselineId) {
        List<BinaryResource> binaryResources = new ArrayList<>();

        ProductBaselineDAO productBaselineDAO = new ProductBaselineDAO(em);
        for (Map.Entry<BaselinedDocumentKey, BaselinedDocument> doc : productBaselineDAO.findBaselineById(baselineId).getBaselinedDocuments().entrySet()) {
            for (BinaryResource binary : doc.getValue().getTargetDocument().getAttachedFiles()) {
                if (!binaryResources.contains(binary)) {
                    binaryResources.add(binary);
                }
            }
        }
        return binaryResources;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public Map<String, Set<BinaryResource>> getBinariesInTree(Integer baselineId, String workspaceId, ConfigurationItemKey ciKey, ProductStructureFilter psFilter, boolean exportNativeCADFiles, boolean exportDocumentLinks) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ConfigurationItemNotFoundException, NotAllowedException, EntityConstraintException, PartMasterNotFoundException, WorkspaceNotEnabledException {

        User user = userManager.checkWorkspaceReadAccess(workspaceId);
        Map<String, Set<BinaryResource>> result = new HashMap<>();

        Locale locale = new Locale(user.getLanguage());

        ConfigurationItem ci = new ConfigurationItemDAO(locale, em).loadConfigurationItem(ciKey);
        PartMaster root = ci.getDesignItem();


        PSFilterVisitor psFilterVisitor = new PSFilterVisitor(em, user, psFilter) {
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
                PartMaster part = parts.get(parts.size() - 1);
                List<PartIteration> partIterations = psFilter.filter(part);

                if (!partIterations.isEmpty()) {

                    PartIteration partIteration = partIterations.get(0);
                    String partFolderName = partIteration.toString();
                    Set<BinaryResource> binaryResources = result.get(partFolderName);

                    if (binaryResources == null) {
                        binaryResources = new HashSet<>();
                        result.put(partFolderName, binaryResources);
                    }

                    if (exportNativeCADFiles) {
                        BinaryResource nativeCADFile = partIteration.getNativeCADFile();
                        if (nativeCADFile != null) {
                            binaryResources.add(nativeCADFile);
                        }

                        if (exportDocumentLinks) {
                            for (BinaryResource attachedFile : partIteration.getAttachedFiles()) {
                                if (attachedFile != null) {
                                    binaryResources.add(attachedFile);
                                }
                            }
                        }
                    }

                    if (exportDocumentLinks && baselineId == null) {
                        Set<DocumentLink> linkedDocuments = partIteration.getLinkedDocuments();

                        for (DocumentLink documentLink : linkedDocuments) {

                            DocumentIteration lastCheckedInIteration = documentLink.getTargetDocument().getLastCheckedInIteration();

                            if (null != lastCheckedInIteration) {

                                String linkedDocumentFolderName = "links/" + lastCheckedInIteration.toString();

                                Set<BinaryResource> linkedBinaryResources = result.get(linkedDocumentFolderName);

                                if (linkedBinaryResources == null) {
                                    linkedBinaryResources = new HashSet<>();
                                    result.put(linkedDocumentFolderName, linkedBinaryResources);
                                }

                                Set<BinaryResource> attachedFiles = lastCheckedInIteration.getAttachedFiles();

                                for (BinaryResource binary : attachedFiles) {
                                    if (!linkedBinaryResources.contains(binary)) {
                                        linkedBinaryResources.add(binary);
                                    }
                                }

                            }
                        }
                    }

                }
                return true;
            }

            @Override
            public void onUnresolvedVersion(PartMaster partMaster) {
                // Unused here
            }
        };

        psFilterVisitor.visit(root, -1);

        return result;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public ProductBaseline loadProductBaselineForProductInstanceMaster(ConfigurationItemKey ciKey, String serialNumber) throws ProductInstanceMasterNotFoundException, UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceReadAccess(ciKey.getWorkspace());
        return new ProductBaselineDAO(new Locale(user.getLanguage()), em).findLastBaselineWithSerialNumber(ciKey, serialNumber);
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public Query loadQuery(String workspaceId, int queryId) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceReadAccess(workspaceId);
        QueryDAO queryDAO = new QueryDAO(new Locale(user.getLanguage()), em);
        return queryDAO.loadQuery(queryId);
    }

    private List<QueryResultRow> filterPBS(String workspaceId, QueryContext queryContext, User user) throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, BaselineNotFoundException, ProductInstanceMasterNotFoundException, ConfigurationItemNotFoundException, NotAllowedException, EntityConstraintException, PartMasterNotFoundException, WorkspaceNotEnabledException {

        String configurationItemId = queryContext.getConfigurationItemId();
        String serialNumber = queryContext.getSerialNumber();

        ConfigurationItemKey ciKey = new ConfigurationItemKey(workspaceId, configurationItemId);
        Locale locale = new Locale(user.getLanguage());

        List<QueryResultRow> rows = new ArrayList<>();

        ProductStructureFilter filter = serialNumber != null ? psFilterManager.getPSFilter(ciKey, "pi-" + serialNumber, false) : psFilterManager.getPSFilter(ciKey, "latest", false);

        ProductInstanceIteration productInstanceIteration = null;
        if (serialNumber != null) {
            ProductInstanceMaster productIM = new ProductInstanceMasterDAO(em).loadProductInstanceMaster(new ProductInstanceMasterKey(serialNumber, ciKey));
            productInstanceIteration = productIM.getLastIteration();
        }

        ConfigurationItem ci = new ConfigurationItemDAO(locale, em).loadConfigurationItem(ciKey);
        PartMaster root = ci.getDesignItem();

        List<PathToPathLink> pathToPathLinks = ci.getPathToPathLinks();
        PathDataIterationDAO pathDataIterationDAO = new PathDataIterationDAO(em);

        List<PathDataIteration> lastPathDataIterations = pathDataIterationDAO.getLastPathDataIterations(productInstanceIteration);
        Map<String, PathDataIteration> lastPathDataIterationsMap = new HashMap<>();

        for (PathDataIteration iteration : lastPathDataIterations) {
            lastPathDataIterationsMap.put(iteration.getPathDataMaster().getPath(), iteration);
        }

        final ProductInstanceIteration finalProductInstanceIteration = productInstanceIteration;

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
                QueryResultRow row = new QueryResultRow();
                double totalAmount = 1;
                for (PartLink pl : path) {
                    if (pl.getUnit() == null) {
                        totalAmount *= pl.getAmount();
                    }
                }
                String pathAsString = Tools.getPathAsString(path);
                row.setPath(pathAsString);
                int depth = parts.size() - 1;
                PartMaster part = parts.get(parts.size() - 1);
                List<PartIteration> partIterations = filter.filter(part);
                if (!partIterations.isEmpty()) {
                    PartRevision partRevision = partIterations.get(0).getPartRevision();
                    row.setPartRevision(partRevision);
                    row.setDepth(depth);
                    row.setContext(queryContext);
                    row.setAmount(totalAmount);

                    // try block and decodePath method are time consuming (db access) May need refactor
                    for (PathToPathLink pathToPathLink : pathToPathLinks) {
                        try {
                            if (pathToPathLink.getSourcePath().equals(pathAsString)) {
                                row.addSource(pathToPathLink.getType(), decodePath(ciKey, pathToPathLink.getTargetPath()));
                            }
                            if (pathToPathLink.getTargetPath().equals(pathAsString)) {
                                row.addTarget(pathToPathLink.getType(), decodePath(ciKey, pathToPathLink.getTargetPath()));
                            }
                        } catch (WorkspaceNotFoundException | WorkspaceNotEnabledException | UserNotFoundException | ConfigurationItemNotFoundException | PartUsageLinkNotFoundException | UserNotActiveException e) {
                            LOGGER.log(Level.SEVERE, null, e);
                        }
                    }

                    if (finalProductInstanceIteration != null) {
                        row.setPathDataIteration(lastPathDataIterationsMap.get(pathAsString));
                    }
                    rows.add(row);
                }
                return true;
            }

            @Override
            public void onUnresolvedVersion(PartMaster partMaster) {
                // Unused here
            }
        };

        psFilterVisitor.visit(root, -1);
        return rows;
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID})
    @Override
    public List<PartIteration> getInversePartsLink(DocumentRevisionKey docKey) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, DocumentRevisionNotFoundException, PartIterationNotFoundException, PartRevisionNotFoundException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceReadAccess(docKey.getWorkspaceId());

        Locale locale = new Locale(user.getLanguage());

        DocumentRevision documentRevision = new DocumentRevisionDAO(locale, em).loadDocR(docKey);

        DocumentLinkDAO documentLinkDAO = new DocumentLinkDAO(locale, em);
        List<PartIteration> iterations = documentLinkDAO.getInversePartsLinks(documentRevision);
        ListIterator<PartIteration> ite = iterations.listIterator();

        while (ite.hasNext()) {
            PartIteration next = ite.next();
            if (!canAccess(next.getKey())) {
                ite.remove();
            }
        }
        return iterations;
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID})
    @Override
    public Set<ProductInstanceMaster> getInverseProductInstancesLink(DocumentRevisionKey docKey) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, DocumentRevisionNotFoundException, PartIterationNotFoundException, PartRevisionNotFoundException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceReadAccess(docKey.getWorkspaceId());

        Locale locale = new Locale(user.getLanguage());

        DocumentRevision documentRevision = new DocumentRevisionDAO(locale, em).loadDocR(docKey);

        DocumentLinkDAO documentLinkDAO = new DocumentLinkDAO(locale, em);
        List<ProductInstanceIteration> iterations = documentLinkDAO.getInverseProductInstanceIteration(documentRevision);
        Set<ProductInstanceMaster> productInstanceMasterSet = new HashSet<>();
        for (ProductInstanceIteration productInstanceIteration : iterations) {
            productInstanceMasterSet.add(productInstanceIteration.getProductInstanceMaster());

        }
        return productInstanceMasterSet;
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID})
    @Override
    public Set<PathDataMaster> getInversePathDataLink(DocumentRevisionKey docKey) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, DocumentRevisionNotFoundException, PartIterationNotFoundException, PartRevisionNotFoundException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceReadAccess(docKey.getWorkspaceId());

        Locale locale = new Locale(user.getLanguage());

        DocumentRevision documentRevision = new DocumentRevisionDAO(locale, em).loadDocR(docKey);

        DocumentLinkDAO documentLinkDAO = new DocumentLinkDAO(locale, em);
        List<PathDataIteration> pathDataIterations = documentLinkDAO.getInversefindPathData(documentRevision);
        Set<PathDataMaster> productInstanceMasterSet = new HashSet<>();
        for (PathDataIteration pathDataIteration : pathDataIterations) {
            productInstanceMasterSet.add(pathDataIteration.getPathDataMaster());

        }
        return productInstanceMasterSet;
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID})
    @Override
    public PathToPathLink createPathToPathLink(String workspaceId, String configurationItemId, String type, String pathFrom, String pathTo, String description) throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException, ConfigurationItemNotFoundException, PathToPathLinkAlreadyExistsException, CreationException, PathToPathCyclicException, PartUsageLinkNotFoundException, UserNotActiveException, NotAllowedException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceWriteAccess(workspaceId);
        Locale locale = new Locale(user.getLanguage());

        // Load the product
        ConfigurationItem ci = new ConfigurationItemDAO(locale, em).loadConfigurationItem(new ConfigurationItemKey(workspaceId, configurationItemId));

        if (type == null || type.isEmpty()) {
            throw new NotAllowedException(locale, "NotAllowedException54");
        }

        if (pathFrom != null && pathTo != null && pathFrom.equals(pathTo)) {
            throw new NotAllowedException(locale, "NotAllowedException57");
        }

        // Decode the paths to insure path validity
        List<PartLink> sourcePath = decodePath(ci.getKey(), pathFrom);
        List<PartLink> targetPath = decodePath(ci.getKey(), pathTo);

        // Check for substitute linking
        PartLink sourceLink = sourcePath.get(sourcePath.size() - 1);
        PartLink targetLink = targetPath.get(targetPath.size() - 1);

        if (sourceLink.getSubstitutes() != null && sourceLink.getSubstitutes().contains(targetLink) || targetLink.getSubstitutes() != null && targetLink.getSubstitutes().contains(sourceLink)) {
            throw new NotAllowedException(locale, "NotAllowedException58");
        }

        PathToPathLink pathToPathLink = new PathToPathLink(type, pathFrom, pathTo, description);
        PathToPathLinkDAO pathToPathLinkDAO = new PathToPathLinkDAO(locale, em);
        PathToPathLink samePathToPathLink = pathToPathLinkDAO.getSamePathToPathLink(ci, pathToPathLink);

        if (samePathToPathLink != null) {
            throw new PathToPathLinkAlreadyExistsException(locale, pathToPathLink);
        }

        pathToPathLinkDAO.createPathToPathLink(pathToPathLink);

        ci.addPathToPathLink(pathToPathLink);

        checkCyclicPathToPathLink(ci, pathToPathLink, user, new ArrayList<>());

        return pathToPathLink;
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID})
    @Override
    public PathToPathLink updatePathToPathLink(String workspaceId, String configurationItemId, int pathToPathLinkId, String description)
            throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException, ConfigurationItemNotFoundException, PartUsageLinkNotFoundException, UserNotActiveException, NotAllowedException, PathToPathLinkNotFoundException, WorkspaceNotEnabledException {

        User user = userManager.checkWorkspaceWriteAccess(workspaceId);
        Locale locale = new Locale(user.getLanguage());

        // Load the product
        ConfigurationItem ci = new ConfigurationItemDAO(locale, em).loadConfigurationItem(new ConfigurationItemKey(workspaceId, configurationItemId));

        PathToPathLinkDAO pathToPathLinkDAO = new PathToPathLinkDAO(locale, em);
        PathToPathLink pathToPathLink = pathToPathLinkDAO.loadPathToPathLink(pathToPathLinkId);

        if (!ci.getPathToPathLinks().contains(pathToPathLink)) {
            throw new NotAllowedException(locale, "NotAllowedException60");
        }

        pathToPathLink.setDescription(description);

        return pathToPathLink;
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID})
    @Override
    public List<String> getPathToPathLinkTypes(String workspaceId, String configurationItemId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ConfigurationItemNotFoundException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceReadAccess(workspaceId);
        Locale locale = new Locale(user.getLanguage());
        ConfigurationItem configurationItem = new ConfigurationItemDAO(locale, em).loadConfigurationItem(new ConfigurationItemKey(workspaceId, configurationItemId));
        return new PathToPathLinkDAO(locale, em).getDistinctPathToPathLinkTypes(configurationItem);
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID})
    @Override
    public List<PathToPathLink> getPathToPathLinkFromSourceAndTarget(String workspaceId, String configurationItemId, String sourcePath, String targetPath) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ConfigurationItemNotFoundException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceReadAccess(workspaceId);
        Locale locale = new Locale(user.getLanguage());
        ConfigurationItem configurationItem = new ConfigurationItemDAO(locale, em).loadConfigurationItem(new ConfigurationItemKey(workspaceId, configurationItemId));
        return new PathToPathLinkDAO(locale, em).getPathToPathLinkFromSourceAndTarget(configurationItem, sourcePath, targetPath);
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID})
    @Override
    public ProductInstanceMaster findProductByPathMaster(String workspaceId, PathDataMaster pathDataMaster) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceReadAccess(workspaceId);
        PathDataMasterDAO pathDataMasterDAO = new PathDataMasterDAO(new Locale(user.getLanguage()), em);
        return pathDataMasterDAO.findByPathData(pathDataMaster);
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID})
    @Override
    public PartMaster getPartMasterFromPath(String workspaceId, String configurationItemId, String partPath) throws ConfigurationItemNotFoundException, UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, PartUsageLinkNotFoundException, WorkspaceNotEnabledException {
        ConfigurationItem configurationItem = new ConfigurationItemDAO(em).loadConfigurationItem(new ConfigurationItemKey(workspaceId, configurationItemId));
        List<PartLink> path = decodePath(configurationItem.getKey(), partPath);
        return path.get(path.size() - 1).getComponent();
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID})
    @Override
    public void deletePathToPathLink(String workspaceId, String configurationItemId, int pathToPathLinkId) throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException, ConfigurationItemNotFoundException, PathToPathLinkNotFoundException, WorkspaceNotEnabledException {

        User user = userManager.checkWorkspaceWriteAccess(workspaceId);
        Locale locale = new Locale(user.getLanguage());

        // Load the product
        ConfigurationItem ci = new ConfigurationItemDAO(locale, em).loadConfigurationItem(new ConfigurationItemKey(workspaceId, configurationItemId));

        PathToPathLinkDAO pathToPathLinkDAO = new PathToPathLinkDAO(locale, em);
        PathToPathLink pathToPathLink = pathToPathLinkDAO.loadPathToPathLink(pathToPathLinkId);
        ci.removePathToPathLink(pathToPathLink);
        pathToPathLinkDAO.removePathToPathLink(pathToPathLink);

    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID})
    @Override
    public List<DocumentIterationLink> getDocumentLinksAsDocumentIterations(String workspaceId, String configurationItemId, String configSpec, PartIterationKey partIterationKey) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, BaselineNotFoundException, PartIterationNotFoundException, ProductInstanceMasterNotFoundException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceReadAccess(workspaceId);
        Locale locale = new Locale(user.getLanguage());

        PartIterationDAO partIterationDAO = new PartIterationDAO(locale, em);
        PartIteration partIteration = partIterationDAO.loadPartI(partIterationKey);

        if (null == configSpec) {
            throw new IllegalArgumentException("Config spec cannot be null");
        }

        ProductBaseline baseline;

        if (configSpec.startsWith("pi-")) {
            String serialNumber = configSpec.substring(3);
            ProductInstanceMasterDAO productInstanceMasterDAO = new ProductInstanceMasterDAO(locale, em);
            ProductInstanceMaster productInstanceMaster = productInstanceMasterDAO.loadProductInstanceMaster(new ProductInstanceMasterKey(serialNumber, workspaceId, configurationItemId));
            baseline = productInstanceMaster.getLastIteration().getBasedOn();
        } else {
            int baselineId = Integer.parseInt(configSpec);
            ProductBaselineDAO productBaselineDAO = new ProductBaselineDAO(locale, em);
            baseline = productBaselineDAO.loadBaseline(baselineId);
        }

        DocumentCollection documentCollection = baseline.getDocumentCollection();
        Map<BaselinedDocumentKey, BaselinedDocument> baselinedDocuments = documentCollection.getBaselinedDocuments();
        List<DocumentIterationLink> documentIterationLinks = new ArrayList<>();

        for (Map.Entry<BaselinedDocumentKey, BaselinedDocument> map : baselinedDocuments.entrySet()) {
            BaselinedDocument baselinedDocument = map.getValue();
            DocumentIteration targetDocument = baselinedDocument.getTargetDocument();
            for (DocumentLink documentLink : partIteration.getLinkedDocuments()) {
                if (documentLink.getTargetDocument().getKey().equals(targetDocument.getDocumentRevisionKey())) {
                    documentIterationLinks.add(new DocumentIterationLink(documentLink, targetDocument));
                }
            }
        }

        return documentIterationLinks;
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID})
    @Override
    public PartIteration findPartIterationByBinaryResource(BinaryResource binaryResource) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, WorkspaceNotEnabledException {
        BinaryResourceDAO binaryResourceDAO;
        User user = userManager.checkWorkspaceReadAccess(binaryResource.getWorkspaceId());
        binaryResourceDAO = new BinaryResourceDAO(new Locale(user.getLanguage()), em);
        return binaryResourceDAO.getPartHolder(binaryResource);
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public PartRevision[] getPartRevisionsWithAssignedTasksForGivenUser(String pWorkspaceId, String assignedUserLogin)
            throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
        List<PartRevision> partRs = new PartRevisionDAO(new Locale(user.getLanguage()), em).findPartsWithAssignedTasksForGivenUser(pWorkspaceId, assignedUserLogin);

        ListIterator<PartRevision> ite = partRs.listIterator();
        while (ite.hasNext()) {
            PartRevision partR = ite.next();
            if (!hasPartRevisionReadAccess(user, partR)) {
                ite.remove();
            } else if (isCheckoutByAnotherUser(user, partR)) {
                em.detach(partR);
                partR.removeLastIteration();
            }
        }

        return partRs.toArray(new PartRevision[partRs.size()]);
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public PartRevision[] getPartRevisionsWithOpenedTasksForGivenUser(String pWorkspaceId, String assignedUserLogin)
            throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
        List<PartRevision> partRs = new PartRevisionDAO(new Locale(user.getLanguage()), em).findPartsWithOpenedTasksForGivenUser(pWorkspaceId, assignedUserLogin);

        ListIterator<PartRevision> ite = partRs.listIterator();
        while (ite.hasNext()) {
            PartRevision partR = ite.next();
            if (!hasPartRevisionReadAccess(user, partR)) {
                ite.remove();
            } else if (isCheckoutByAnotherUser(user, partR)) {
                em.detach(partR);
                partR.removeLastIteration();
            }
        }

        return partRs.toArray(new PartRevision[partRs.size()]);
    }

    private void checkCyclicPathToPathLink(ConfigurationItem ci, PathToPathLink startLink, User user, List<PathToPathLink> visitedLinks) throws PathToPathCyclicException {
        Locale locale = new Locale(user.getLanguage());
        PathToPathLinkDAO pathToPathLinkDAO = new PathToPathLinkDAO(locale, em);

        List<PathToPathLink> nextPathToPathLinks = pathToPathLinkDAO.getNextPathToPathLinkInProduct(ci, startLink);
        for (PathToPathLink nextPathToPathLink : nextPathToPathLinks) {
            if (visitedLinks.contains(nextPathToPathLink)) {
                throw new PathToPathCyclicException(locale);
            }
            visitedLinks.add(nextPathToPathLink);
            checkCyclicPathToPathLink(ci, startLink, user, visitedLinks);
        }
    }

    private User checkPartRevisionWriteAccess(PartRevisionKey partRevisionKey) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, PartRevisionNotFoundException, AccessRightException, WorkspaceNotEnabledException {
        String workspaceId = partRevisionKey.getPartMaster().getWorkspace();
        User user = userManager.checkWorkspaceReadAccess(workspaceId);
        if (user.isAdministrator()) {                                                                                     // Check if it is the workspace's administrator
            return user;
        }
        PartRevision partRevision = new PartRevisionDAO(em).loadPartR(partRevisionKey);
        if (partRevision.getACL() == null) {                                                                                // Check if the part haven't ACL
            return userManager.checkWorkspaceWriteAccess(workspaceId);
        }
        if (partRevision.getACL().hasWriteAccess(user)) {                                                                 // Check if the ACL grant write access
            return user;
        }
        throw new AccessRightException(new Locale(user.getLanguage()), user);                                            // Else throw a AccessRightException
    }

    private User checkPartTemplateWriteAccess(PartMasterTemplate template, User user) throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException, WorkspaceNotEnabledException {

        if (user.isAdministrator()) {
            // Check if it is the workspace's administrator
            return user;
        }
        if (template.getAcl() == null) {
            // Check if the item haven't ACL
            return userManager.checkWorkspaceWriteAccess(template.getWorkspaceId());
        } else if (template.getAcl().hasWriteAccess(user)) {
            // Check if there is a write access
            return user;
        } else {
            // Else throw a AccessRightException
            throw new AccessRightException(new Locale(user.getLanguage()), user);
        }


    }

    /**
     * Say if a user, which have access to the workspace, have read access to a part revision
     *
     * @param user         A user which have read access to the workspace
     * @param partRevision The part revision wanted
     * @return True if access is granted, False otherwise
     */
    private boolean hasPartRevisionReadAccess(User user, PartRevision partRevision) {
        return user.isAdministrator() || isACLGrantReadAccess(user, partRevision);
    }

    private boolean hasPartTemplateReadAccess(User user, PartMasterTemplate template) {
        return user.isAdministrator() || isACLGrantReadAccess(user, template);
    }

    /**
     * Say if a user, which have access to the workspace, have write access to a part revision
     *
     * @param user         A user which have read access to the workspace
     * @param partRevision The part revision wanted
     * @return True if access is granted, False otherwise
     */
    private boolean hasPartRevisionWriteAccess(User user, PartRevision partRevision) {
        return user.isAdministrator() || isACLGrantWriteAccess(user, partRevision);
    }

    /**
     * Say if a user can write on a part Revision through ACL or Workspace Access
     *
     * @param user         A user with at least Read access to the workspace
     * @param partRevision the part revision to access
     * @return True if access is granted, False otherwise
     * @throws WorkspaceNotFoundException
     */
    private boolean hasPartOrWorkspaceWriteAccess(User user, PartRevision partRevision) throws WorkspaceNotFoundException, WorkspaceNotEnabledException {
        return partRevision.getACL() == null ?
                userManager.hasWorkspaceWriteAccess(user, partRevision.getWorkspaceId()) : partRevision.getACL().hasWriteAccess(user);
    }

    private boolean isAuthor(User user, PartRevision partRevision) {
        return partRevision.getAuthor().getLogin().equals(user.getLogin());
    }

    private boolean isACLGrantReadAccess(User user, PartRevision partRevision) {
        return partRevision.getACL() == null || partRevision.getACL().hasReadAccess(user);
    }

    private boolean isACLGrantReadAccess(User user, PartMasterTemplate template) {
        return template.getAcl() == null || template.getAcl().hasReadAccess(user);
    }

    private boolean isACLGrantWriteAccess(User user, PartRevision partRevision) {
        return partRevision.getACL() == null || partRevision.getACL().hasWriteAccess(user);
    }

    private boolean isCheckoutByUser(User user, PartRevision partRevision) {
        return partRevision.isCheckedOut() && partRevision.getCheckOutUser().equals(user);
    }

    private boolean isCheckoutByAnotherUser(User user, PartRevision partRevision) {
        return partRevision.isCheckedOut() && !partRevision.getCheckOutUser().equals(user);
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
}
//TODO when using layers and markers, check for concordance
//TODO add a method to update a marker
//TODO use dozer
