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
package com.docdoku.server;

import com.docdoku.core.change.ModificationNotification;
import com.docdoku.core.common.*;
import com.docdoku.core.configuration.*;
import com.docdoku.core.document.DocumentIteration;
import com.docdoku.core.document.DocumentIterationKey;
import com.docdoku.core.document.DocumentLink;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.meta.*;
import com.docdoku.core.product.*;
import com.docdoku.core.product.PartIteration.Source;
import com.docdoku.core.query.PartSearchQuery;
import com.docdoku.core.query.Query;
import com.docdoku.core.query.QueryContext;
import com.docdoku.core.query.QueryResultRow;
import com.docdoku.core.security.ACL;
import com.docdoku.core.security.ACLUserEntry;
import com.docdoku.core.security.ACLUserGroupEntry;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.*;
import com.docdoku.core.sharing.SharedEntityKey;
import com.docdoku.core.sharing.SharedPart;
import com.docdoku.core.util.NamingConvention;
import com.docdoku.core.util.Tools;
import com.docdoku.core.workflow.*;
import com.docdoku.server.configuration.PSFilterVisitor;
import com.docdoku.server.configuration.filter.*;
import com.docdoku.server.configuration.spec.ProductInstanceConfigSpec;
import com.docdoku.server.dao.*;
import com.docdoku.server.esindexer.ESIndexer;
import com.docdoku.server.esindexer.ESSearcher;
import com.docdoku.server.events.CheckedIn;
import com.docdoku.server.events.PartIterationChangeEvent;
import com.docdoku.server.events.PartRevisionChangeEvent;
import com.docdoku.server.events.Removed;
import com.docdoku.server.factory.ACLFactory;

import javax.annotation.Resource;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.ejb.*;
import javax.enterprise.event.Event;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Inject;
import javax.jws.WebService;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.text.ParseException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@DeclareRoles({UserGroupMapping.REGULAR_USER_ROLE_ID,UserGroupMapping.ADMIN_ROLE_ID,UserGroupMapping.GUEST_PROXY_ROLE_ID})
@Local(IProductManagerLocal.class)
@Stateless(name = "ProductManagerBean")
@WebService(endpointInterface = "com.docdoku.core.services.IProductManagerWS")
public class ProductManagerBean implements IProductManagerWS, IProductManagerLocal {

    @PersistenceContext
    private EntityManager em;
    @Resource
    private SessionContext ctx;
    @EJB
    private IMailerLocal mailer;
    @EJB
    private IUserManagerLocal userManager;
    @EJB
    private IDataManagerLocal dataManager;
    @EJB
    private IProductBaselineManagerLocal productBaselineManager;
    @EJB
    private ESIndexer esIndexer;
    @EJB
    private ESSearcher esSearcher;


    @Inject
    private Event<PartIterationChangeEvent> partIterationEvent;
    @Inject
    private Event<PartRevisionChangeEvent> partRevisionEvent;

    private static final Logger LOGGER = Logger.getLogger(ProductManagerBean.class.getName());

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public List<PartLink[]> findPartUsages(ConfigurationItemKey pKey, PSFilter filter, String search) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException, NotAllowedException, EntityConstraintException, PartMasterNotFoundException, ConfigurationItemNotFoundException {

        User user = userManager.checkWorkspaceReadAccess(pKey.getWorkspace());

        List<PartLink[]> usagePaths = new ArrayList<>();

        ConfigurationItemDAO configurationItemDAO = new ConfigurationItemDAO(new Locale(user.getLanguage()), em);
        ConfigurationItem ci = configurationItemDAO.loadConfigurationItem(pKey);

        new PSFilterVisitor(em, user, filter, ci.getDesignItem(), null, -1) {
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
            public void onOptionalPath(List<PartLink> partLinks, List<PartIteration> partIterations) {
            }

            @Override
            public void onPathWalk(List<PartLink> path, List<PartMaster> parts) {
                PartMaster pm = parts.get(parts.size() - 1);

                if (pm.getNumber().matches(search) || (pm.getName() != null && pm.getName().matches(search)) || Tools.getPathAsString(path).equals(search)) {
                    PartLink[] partLinks = path.toArray(new PartLink[path.size()]);
                    usagePaths.add(partLinks);
                }
            }

        };

        return usagePaths;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public List<PartMaster> findPartMasters(String pWorkspaceId, String pPartNumber, int pMaxResults) throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException {
        User user = userManager.checkWorkspaceWriteAccess(pWorkspaceId);
        PartMasterDAO partMDAO = new PartMasterDAO(new Locale(user.getLanguage()), em);
        return partMDAO.findPartMasters(pWorkspaceId, pPartNumber, pMaxResults);
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public ConfigurationItem createConfigurationItem(String pWorkspaceId, String pId, String pDescription, String pDesignItemNumber) throws UserNotFoundException, WorkspaceNotFoundException, AccessRightException, NotAllowedException, ConfigurationItemAlreadyExistsException, CreationException, PartMasterNotFoundException {
        User user = userManager.checkWorkspaceWriteAccess(pWorkspaceId);
        Locale locale = new Locale(user.getLanguage());
        checkNameValidity(pId,locale);

        ConfigurationItem ci = new ConfigurationItem(user,user.getWorkspace(), pId, pDescription);

        try {
            PartMaster designedPartMaster = new PartMasterDAO(locale, em).loadPartM(new PartMasterKey(pWorkspaceId, pDesignItemNumber));
            ci.setDesignItem(designedPartMaster);
            new ConfigurationItemDAO(locale, em).createConfigurationItem(ci);
            return ci;
        } catch (PartMasterNotFoundException e) {
            LOGGER.log(Level.FINEST,null,e);
            throw new PartMasterNotFoundException(locale,pDesignItemNumber);
        }

    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public PartMaster createPartMaster(String pWorkspaceId, String pNumber, String pName, boolean pStandardPart, String pWorkflowModelId, String pPartRevisionDescription, String templateId, Map<String, String> roleMappings, ACLUserEntry[] pACLUserEntries, ACLUserGroupEntry[] pACLUserGroupEntries) throws NotAllowedException, UserNotFoundException, WorkspaceNotFoundException, AccessRightException, WorkflowModelNotFoundException, PartMasterAlreadyExistsException, CreationException, PartMasterTemplateNotFoundException, FileAlreadyExistsException, RoleNotFoundException {

        User user = userManager.checkWorkspaceWriteAccess(pWorkspaceId);
        Locale locale = new Locale(user.getLanguage());
        checkNameValidity(pNumber,locale);

        PartMaster pm = new PartMaster(user.getWorkspace(), pNumber, user);
        pm.setName(pName);
        pm.setStandardPart(pStandardPart);
        Date now = new Date();
        pm.setCreationDate(now);
        PartRevision newRevision = pm.createNextRevision(user);

        if (pWorkflowModelId != null) {

            UserDAO userDAO = new UserDAO(locale,em);
            RoleDAO roleDAO = new RoleDAO(locale,em);

            Map<Role,User> roleUserMap = new HashMap<>();

            for (Object o : roleMappings.entrySet()) {
                Map.Entry pairs = (Map.Entry) o;
                String roleName = (String) pairs.getKey();
                String userLogin = (String) pairs.getValue();
                User worker = userDAO.loadUser(new UserKey(user.getWorkspaceId(), userLogin));
                Role role = roleDAO.loadRole(new RoleKey(user.getWorkspaceId(), roleName));
                roleUserMap.put(role, worker);
            }

            WorkflowModel workflowModel = new WorkflowModelDAO(locale, em).loadWorkflowModel(new WorkflowModelKey(user.getWorkspaceId(), pWorkflowModelId));
            Workflow workflow = workflowModel.createWorkflow(roleUserMap);
            newRevision.setWorkflow(workflow);

            Collection<Task> runningTasks = workflow.getRunningTasks();
            for (Task runningTask : runningTasks) {
                runningTask.start();
            }

            mailer.sendApproval(runningTasks, newRevision);

        }
        newRevision.setCheckOutUser(user);
        newRevision.setCheckOutDate(now);
        newRevision.setCreationDate(now);
        newRevision.setDescription(pPartRevisionDescription);
        PartIteration ite = newRevision.createNextIteration(user);
        ite.setCreationDate(now);

        if(templateId != null){

            PartMasterTemplate partMasterTemplate = new PartMasterTemplateDAO(locale,em).loadPartMTemplate(new PartMasterTemplateKey(pWorkspaceId,templateId));
           
            if(!Tools.validateMask(partMasterTemplate.getMask(),pNumber)){
                throw new NotAllowedException(locale,"NotAllowedException42");
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

            if(sourceFile != null){
                String fileName = sourceFile.getName();
                long length = sourceFile.getContentLength();
                Date lastModified = sourceFile.getLastModified();
                String fullName = pWorkspaceId + "/parts/" + pm.getNumber() + "/A/1/nativecad/" + fileName;
                BinaryResource targetFile = new BinaryResource(fullName, length, lastModified);
                binDAO.createBinaryResource(targetFile);
                ite.setNativeCADFile(targetFile);
                try {
                    dataManager.copyData(sourceFile, targetFile);
                } catch (StorageException e) {
                    LOGGER.log(Level.INFO, null, e);
                }
            }

        }
        //TODO refactor call ACLFactory
        if ((pACLUserEntries != null && pACLUserEntries.length > 0) || (pACLUserGroupEntries != null && pACLUserGroupEntries.length > 0)) {
            ACL acl = new ACL();
             if (pACLUserEntries != null) {
                for (ACLUserEntry entry : pACLUserEntries) {
                    acl.addEntry(em.getReference(User.class, new UserKey(user.getWorkspaceId(), entry.getPrincipalLogin())), entry.getPermission());
                }
            }

            if (pACLUserGroupEntries != null) {
                for (ACLUserGroupEntry entry : pACLUserGroupEntries) {
                    acl.addEntry(em.getReference(UserGroup.class, new UserGroupKey(user.getWorkspaceId(), entry.getPrincipalId())), entry.getPermission());
                }
            }
            newRevision.setACL(acl);
            new ACLDAO(em).createACL(acl);
        }

        new PartMasterDAO(locale, em).createPartM(pm);
        return pm;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public PartRevision undoCheckOutPart(PartRevisionKey pPartRPK) throws NotAllowedException, PartRevisionNotFoundException, UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, AccessRightException {
        User user = userManager.checkWorkspaceReadAccess(pPartRPK.getPartMaster().getWorkspace());
        Locale locale = new Locale(user.getLanguage());

        PartRevisionDAO partRDAO = new PartRevisionDAO(locale, em);
        PartRevision partR = partRDAO.loadPartR(pPartRPK);

        //Check access rights on partR
        if (!hasPartRevisionWriteAccess(user,partR)) {
            throw new AccessRightException(locale, user);
        }

        if (isCheckoutByUser(user,partR)) {
            if(partR.getLastIteration().getIteration() <= 1) {
                throw new NotAllowedException(locale, "NotAllowedException41");
            }
            PartIteration partIte = partR.removeLastIteration();
            partIterationEvent.select(new AnnotationLiteral<Removed>(){}).fire(new PartIterationChangeEvent(partIte));

            for (Geometry file : partIte.getGeometries()) {
                try {
                    dataManager.deleteData(file);
                } catch (StorageException e) {
                    LOGGER.log(Level.INFO, null, e);
                }
            }

            for (BinaryResource file : partIte.getAttachedFiles()) {
                try {
                    dataManager.deleteData(file);
                } catch (StorageException e) {
                    LOGGER.log(Level.INFO, null, e);
                }
            }

            BinaryResource nativeCAD = partIte.getNativeCADFile();
            if (nativeCAD != null) {
                try {
                    dataManager.deleteData(nativeCAD);
                } catch (StorageException e) {
                    LOGGER.log(Level.INFO, null, e);
                }
            }

            PartIterationDAO partIDAO = new PartIterationDAO(locale,em);
            partIDAO.removeIteration(partIte);
            partR.setCheckOutDate(null);
            partR.setCheckOutUser(null);

            return partR;
        } else {
            throw new NotAllowedException(locale, "NotAllowedException19");
        }
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public PartRevision checkOutPart(PartRevisionKey pPartRPK) throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException, PartRevisionNotFoundException, NotAllowedException, FileAlreadyExistsException, CreationException {
        User user = userManager.checkWorkspaceWriteAccess(pPartRPK.getPartMaster().getWorkspace());
        Locale locale = new Locale(user.getLanguage());

        PartRevisionDAO partRDAO = new PartRevisionDAO(locale, em);
        PartRevision partR = partRDAO.loadPartR(pPartRPK);

        //Check access rights on partR
        if (!hasPartRevisionWriteAccess(user,partR)) {
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
                String fullName = partR.getWorkspaceId() + "/parts/" + partR.getPartNumber() + "/" + partR.getVersion() + "/" + newPartIteration.getIteration() + "/" + fileName;
                BinaryResource targetFile = new BinaryResource(fullName, length, lastModified);
                binDAO.createBinaryResource(targetFile);
                newPartIteration.addFile(targetFile);
            }

            List<PartUsageLink> components = new LinkedList<>();
            for (PartUsageLink usage : beforeLastPartIteration.getComponents()) {
                PartUsageLink newUsage = usage.clone();
                components.add(newUsage);
            }
            newPartIteration.setComponents(components);

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
        }

        return partR;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public PartRevision checkInPart(PartRevisionKey pPartRPK) throws PartRevisionNotFoundException, UserNotFoundException, WorkspaceNotFoundException, AccessRightException, NotAllowedException, ESServerException, EntityConstraintException, UserNotActiveException, PartMasterNotFoundException {
        User user = userManager.checkWorkspaceWriteAccess(pPartRPK.getPartMaster().getWorkspace());
        Locale locale = new Locale(user.getLanguage());

        PartRevisionDAO partRDAO = new PartRevisionDAO(locale, em);
        PartRevision partR = partRDAO.loadPartR(pPartRPK);

        //Check access rights on partR
        if (!hasPartRevisionWriteAccess(user, partR)) {
            throw new AccessRightException(locale, user);
        }

        if (isCheckoutByUser(user,partR)) {

            checkCyclicAssemblyForPartIteration(partR.getLastIteration());

            partR.setCheckOutDate(null);
            partR.setCheckOutUser(null);

            PartIteration lastIteration = partR.getLastIteration();
            lastIteration.setCheckInDate(new Date());

            for(PartIteration partIteration : partR.getPartIterations()){
                esIndexer.index(partIteration);                                                                         // Index all iterations in ElasticSearch (decrease old iteration boost factor)
            }
            partIterationEvent.select(new AnnotationLiteral<CheckedIn>(){}).fire(new PartIterationChangeEvent(lastIteration));
            return partR;
        } else {
            throw new NotAllowedException(locale, "NotAllowedException20");
        }

    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID,UserGroupMapping.GUEST_PROXY_ROLE_ID})
    @Override
    public BinaryResource getBinaryResource(String pFullName) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, FileNotFoundException, NotAllowedException, AccessRightException {

        if(ctx.isCallerInRole(UserGroupMapping.GUEST_PROXY_ROLE_ID)){
            // Don't check access right because it is do before. (Is public or isShared)
            return new BinaryResourceDAO(em).loadBinaryResource(pFullName);
        }

        User user = userManager.checkWorkspaceReadAccess(BinaryResource.parseWorkspaceId(pFullName));
        Locale userLocale = new Locale(user.getLanguage());
        BinaryResourceDAO binDAO = new BinaryResourceDAO(userLocale, em);
        BinaryResource binaryResource = binDAO.loadBinaryResource(pFullName);

        PartIteration partIte = binDAO.getPartOwner(binaryResource);
        if (partIte != null) {
            PartRevision partR = partIte.getPartRevision();

            if(isACLGrantReadAccess(user,partR)){
                if (isCheckoutByAnotherUser(user,partR) && partR.getLastIteration().equals(partIte)) {
                    throw new NotAllowedException(new Locale(user.getLanguage()), "NotAllowedException34");
                } else {
                    return binaryResource;
                }
            }else{
                throw new AccessRightException(userLocale,user);
            }
        } else {
            throw new FileNotFoundException(userLocale, pFullName);
        }
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID})
    @Override
    public BinaryResource getTemplateBinaryResource(String pFullName) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, FileNotFoundException {
        User user = userManager.checkWorkspaceReadAccess(BinaryResource.parseWorkspaceId(pFullName));
        Locale userLocale = new Locale(user.getLanguage());
        BinaryResourceDAO binDAO = new BinaryResourceDAO(userLocale, em);
        return binDAO.loadBinaryResource(pFullName);
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public BinaryResource saveNativeCADInPartIteration(PartIterationKey pPartIPK, String pName, long pSize) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, NotAllowedException, PartRevisionNotFoundException, FileAlreadyExistsException, CreationException {
        User user = userManager.checkWorkspaceReadAccess(pPartIPK.getWorkspaceId());
        Locale locale = new Locale(user.getLanguage());
        checkNameFileValidity(pName,locale);

        BinaryResourceDAO binDAO = new BinaryResourceDAO(locale, em);
        PartRevisionDAO partRDAO = new PartRevisionDAO(locale, em);
        PartRevision partR = partRDAO.loadPartR(pPartIPK.getPartRevision());
        PartIteration partI = partR.getIteration(pPartIPK.getIteration());
        if (isCheckoutByUser(user,partR) && partR.getLastIteration().equals(partI)) {
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

                try {
                    dataManager.deleteData(nativeCADBinaryResource);
                } catch (StorageException e) {
                    LOGGER.log(Level.INFO, null, e);
                }
                partI.setNativeCADFile(null);
                binDAO.removeBinaryResource(nativeCADBinaryResource);

                Set<BinaryResource> attachedFiles = new HashSet<>(partI.getAttachedFiles());
                for(BinaryResource attachedFile : attachedFiles){
                    try {
                        dataManager.deleteData(attachedFile);
                    } catch (StorageException e) {
                        LOGGER.log(Level.INFO, null, e);
                    }
                    partI.removeFile(attachedFile);
                }

                nativeCADBinaryResource = new BinaryResource(fullName, pSize, new Date());
                binDAO.createBinaryResource(nativeCADBinaryResource);
                partI.setNativeCADFile(nativeCADBinaryResource);
            }

            //Delete converted files if any
            List<Geometry> geometries = new ArrayList<>(partI.getGeometries());
            for(Geometry geometry : geometries){
                try {
                    dataManager.deleteData(geometry);
                } catch (StorageException e) {
                    LOGGER.log(Level.INFO, null, e);
                }
                partI.removeGeometry(geometry);
                binDAO.removeBinaryResource(geometry);
            }
            return nativeCADBinaryResource;
        } else {
            throw new NotAllowedException(locale, "NotAllowedException4");
        }

    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public BinaryResource saveGeometryInPartIteration(PartIterationKey pPartIPK, String pName, int quality, long pSize, double[] box) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, NotAllowedException, PartRevisionNotFoundException, FileAlreadyExistsException, CreationException {
        User user = userManager.checkWorkspaceReadAccess(pPartIPK.getWorkspaceId());
        Locale locale = new Locale(user.getLanguage());
        checkNameFileValidity(pName,locale);

        PartRevisionDAO partRDAO = new PartRevisionDAO(locale,em);
        PartRevision partR = partRDAO.loadPartR(pPartIPK.getPartRevision());
        PartIteration partI = partR.getIteration(pPartIPK.getIteration());
        if (isCheckoutByUser(user,partR) && partR.getLastIteration().equals(partI)) {
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
                new BinaryResourceDAO(locale,em).createBinaryResource(geometryBinaryResource);
                partI.addGeometry(geometryBinaryResource);
            } else {
                geometryBinaryResource.setContentLength(pSize);
                geometryBinaryResource.setQuality(quality);
                geometryBinaryResource.setLastModified(new Date());
            }

            if(box != null){
                geometryBinaryResource.setBox(box[0], box[1], box[2], box[3], box[4], box[5]);
            }

            return geometryBinaryResource;
        } else {
            throw new NotAllowedException(locale, "NotAllowedException4");
        }
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public BinaryResource saveFileInPartIteration(PartIterationKey pPartIPK, String pName, long pSize) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, NotAllowedException, PartRevisionNotFoundException, FileAlreadyExistsException, CreationException {
        User user = userManager.checkWorkspaceReadAccess(pPartIPK.getWorkspaceId());
        Locale locale = new Locale(user.getLanguage());
        checkNameFileValidity(pName,locale);

        PartRevisionDAO partRDAO = new PartRevisionDAO(locale,em);
        PartRevision partR = partRDAO.loadPartR(pPartIPK.getPartRevision());
        PartIteration partI = partR.getIteration(pPartIPK.getIteration());
        if (isCheckoutByUser(user,partR) && partR.getLastIteration().equals(partI)) {
            BinaryResource binaryResource = null;
            String fullName = partR.getWorkspaceId() + "/parts/" + partR.getPartNumber() + "/" + partR.getVersion() + "/" + partI.getIteration() + "/" + pName;

            for (BinaryResource bin : partI.getAttachedFiles()) {
                if (bin.getFullName().equals(fullName)) {
                    binaryResource = bin;
                    break;
                }
            }
            if (binaryResource == null) {
                binaryResource = new BinaryResource(fullName, pSize, new Date());
                new BinaryResourceDAO(locale,em).createBinaryResource(binaryResource);
                partI.addFile(binaryResource);
            } else {
                binaryResource.setContentLength(pSize);
                binaryResource.setLastModified(new Date());
            }
            return binaryResource;
        } else {
            throw new NotAllowedException(locale, "NotAllowedException4");
        }
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID,UserGroupMapping.ADMIN_ROLE_ID})
    @Override
    public List<ConfigurationItem> getConfigurationItems(String pWorkspaceId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException {
        Locale locale;
        if(!userManager.isCallerInRole(UserGroupMapping.ADMIN_ROLE_ID)){
            User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
            locale = new Locale(user.getLanguage());
        }else{
            locale = Locale.getDefault();
        }

        return new ConfigurationItemDAO(locale, em).findAllConfigurationItems(pWorkspaceId);
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID,UserGroupMapping.ADMIN_ROLE_ID})
    @Override
    public ConfigurationItem getConfigurationItem(ConfigurationItemKey ciKey) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ConfigurationItemNotFoundException {
        User user = userManager.checkWorkspaceReadAccess(ciKey.getWorkspace());
        Locale locale = new Locale(user.getLanguage());
        ConfigurationItem configurationItem = em.find(ConfigurationItem.class, ciKey);
        if(configurationItem == null){
            throw new ConfigurationItemNotFoundException(locale,ciKey.getId());
        }
        return  configurationItem;
    }

    /*
    * give pAttributes null for no modification, give an empty list for removing them
    * */
    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public PartRevision updatePartIteration(PartIterationKey pKey, String pIterationNote, Source source, List<PartUsageLink> pUsageLinks, List<InstanceAttribute> pAttributes, DocumentIterationKey[] pLinkKeys, String[] documentLinkComments)
            throws UserNotFoundException, WorkspaceNotFoundException, AccessRightException, NotAllowedException, PartRevisionNotFoundException, PartMasterNotFoundException, EntityConstraintException, UserNotActiveException {

        User user = userManager.checkWorkspaceWriteAccess(pKey.getWorkspaceId());
        Locale locale = new Locale(user.getLanguage());

        PartRevisionDAO partRDAO = new PartRevisionDAO(locale, em);
        PartRevision partRev = partRDAO.loadPartR(pKey.getPartRevision());

        //check access rights on partRevision
        if (!hasPartRevisionWriteAccess(user,partRev)) {
            throw new AccessRightException(locale, user);
        }

        PartMasterDAO partMDAO = new PartMasterDAO(locale, em);
        DocumentLinkDAO linkDAO = new DocumentLinkDAO(locale, em);
        PartIteration partIte = partRev.getLastIteration();

        if (isCheckoutByUser(user,partRev) && partIte.getKey().equals(pKey)) {
            if (pLinkKeys != null) {
                ArrayList<DocumentIterationKey> linkKeys = new ArrayList<>(Arrays.asList(pLinkKeys));
                ArrayList<DocumentIterationKey> currentLinkKeys = new ArrayList<>();

                Set<DocumentLink> currentLinks = new HashSet<>(partIte.getLinkedDocuments());

                for (DocumentLink link : currentLinks) {
                    partIte.getLinkedDocuments().remove(link);
                }

                int counter = 0;
                for (DocumentIterationKey link : linkKeys) {
                    DocumentLink newLink = new DocumentLink(em.getReference(DocumentIteration.class, link));
                    newLink.setComment(documentLinkComments[counter]);
                    linkDAO.createLink(newLink);
                    partIte.getLinkedDocuments().add(newLink);
                    counter++;
                }

            }
            if (pUsageLinks != null) {

                List<PartUsageLink> usageLinks = new LinkedList<>();
                for (PartUsageLink usageLink : pUsageLinks) {
                    PartUsageLink ul = new PartUsageLink();
                    ul.setAmount(usageLink.getAmount());
                    ul.setOptional(usageLink.isOptional());
                    ul.setCadInstances(usageLink.getCadInstances());
                    ul.setComment(usageLink.getComment());
                    ul.setReferenceDescription(usageLink.getReferenceDescription());
                    ul.setUnit(usageLink.getUnit());
                    PartMaster pm = usageLink.getComponent();
                    PartMaster component = partMDAO.loadPartM(new PartMasterKey(pm.getWorkspaceId(), pm.getNumber()));
                    ul.setComponent(component);
                    List<PartSubstituteLink> substitutes = new LinkedList<>();
                    for (PartSubstituteLink substitute : usageLink.getSubstitutes()) {
                        PartSubstituteLink sub = new PartSubstituteLink();
                        sub.setAmount(substitute.getAmount());
                        sub.setUnit(substitute.getUnit());
                        sub.setCadInstances(substitute.getCadInstances());
                        sub.setComment(substitute.getComment());
                        sub.setReferenceDescription(substitute.getReferenceDescription());
                        PartMaster pmSub = substitute.getSubstitute();
                        sub.setSubstitute(partMDAO.loadPartM(new PartMasterKey(pmSub.getWorkspaceId(), pmSub.getNumber())));
                        substitutes.add(sub);
                    }
                    ul.setSubstitutes(substitutes);
                    usageLinks.add(ul);
                }

                partIte.setComponents(usageLinks);

                checkCyclicAssemblyForPartIteration(partIte);

            }
            if (pAttributes != null) {

                List<InstanceAttribute> currentAttrs = partIte.getInstanceAttributes();
                if (partRev.isAttributesLocked()){
                    //Check attributs haven't changed
                    if (currentAttrs.size() != pAttributes.size()){
                        throw new NotAllowedException(locale, "NotAllowedException45");
                    } else {
                        for (int i=0;i<currentAttrs.size();i++){
                            InstanceAttribute currentAttr=currentAttrs.get(i);
                            InstanceAttribute newAttr = pAttributes.get(i);
                            if (newAttr == null
                                    || !newAttr.getName().equals(currentAttr.getName())
                                    || !newAttr.getClass().equals(currentAttr.getClass())){
                                //Attribut has been swapped with a new attributs or his type has changed
                                throw new NotAllowedException(locale, "NotAllowedException45");
                            }
                        }
                    }
                }

                for (int i=0;i<currentAttrs.size();i++) {
                    InstanceAttribute currentAttr=currentAttrs.get(i);

                    if(i<pAttributes.size()) {
                        InstanceAttribute newAttr = pAttributes.get(i);
                        if (currentAttr.getClass() != newAttr.getClass()) {
                            partIte.getInstanceAttributes().set(i,newAttr);
                        } else {
                            partIte.getInstanceAttributes().get(i).setName(newAttr.getName());
                            partIte.getInstanceAttributes().get(i).setValue(newAttr.getValue());
                            partIte.getInstanceAttributes().get(i).setMandatory(newAttr.isMandatory());
                        }
                    }else{
                        //no more attribute to add remove all of them still end of iteration
                        partIte.getInstanceAttributes().remove(partIte.getInstanceAttributes().size()-1);
                    }
                }
                for(int i=currentAttrs.size();i<pAttributes.size();i++){
                    InstanceAttribute newAttr = pAttributes.get(i);
                    partIte.getInstanceAttributes().add(newAttr);
                }
            }

            partIte.setIterationNote(pIterationNote);
            Date now = new Date();
            partIte.setModificationDate(now);
            partIte.setSource(source);
            return partRev;

        } else {
            throw new NotAllowedException(locale, "NotAllowedException25");
        }

    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID,UserGroupMapping.GUEST_PROXY_ROLE_ID})
    @Override
    public PartRevision getPartRevision(PartRevisionKey pPartRPK) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, PartRevisionNotFoundException, AccessRightException {

        if(ctx.isCallerInRole(UserGroupMapping.GUEST_PROXY_ROLE_ID)){
            PartRevision partRevision = new PartRevisionDAO(em).loadPartR(pPartRPK);
            if(partRevision.isCheckedOut()){
                em.detach(partRevision);
                partRevision.removeLastIteration();
            }
            return partRevision;
        }

        User user = checkPartRevisionReadAccess(pPartRPK);

        PartRevision partR = new PartRevisionDAO(new Locale(user.getLanguage()), em).loadPartR(pPartRPK);

        if (isCheckoutByAnotherUser(user,partR)) {
            em.detach(partR);
            partR.removeLastIteration();
        }
        return partR;
    }


    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID})
    @Override
    public List<ModificationNotification> getModificationNotifications(PartIterationKey pPartIPK) throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, PartRevisionNotFoundException, AccessRightException {
        PartRevisionKey partRevisionKey = pPartIPK.getPartRevision();
        User user = checkPartRevisionReadAccess(partRevisionKey);
        Locale locale = new Locale(user.getLanguage());
        return new ModificationNotificationDAO(locale,em).getModificationNotifications(pPartIPK);
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
    public void createModificationNotifications(PartIteration modifiedPartIteration) throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, PartRevisionNotFoundException, AccessRightException {
        //TODO insure access rights
        Set<PartIteration> impactedParts = new HashSet<>();
        impactedParts.addAll(getUsedByAsComponent(modifiedPartIteration.getKey()));
        impactedParts.addAll(getUsedByAsSubstitute(modifiedPartIteration.getKey()));

        ModificationNotificationDAO dao = new ModificationNotificationDAO(em);
        for (PartIteration impactedPart : impactedParts) {
            if(impactedPart.isLastIteration()) {
                ModificationNotification notification = new ModificationNotification();
                notification.setImpactedPart(impactedPart);
                notification.setModifiedPart(modifiedPartIteration);
                dao.createModificationNotification(notification);
            }
        }
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID})
    @Override
    public void updateModificationNotification(String pWorkspaceId, int pModificationNotificationId, String pAcknowledgementComment) throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException, PartRevisionNotFoundException {

        ModificationNotification modificationNotification = new ModificationNotificationDAO(em).getModificationNotification(pModificationNotificationId);
        PartIterationKey partIKey = modificationNotification.getImpactedPart().getKey();
        PartRevisionKey partRKey = partIKey.getPartRevision();

        User user = userManager.checkWorkspaceWriteAccess(partRKey.getPartMaster().getWorkspace());
        Locale locale = new Locale(user.getLanguage());

        PartRevisionDAO partRDAO = new PartRevisionDAO(locale, em);
        PartRevision partR = partRDAO.loadPartR(partRKey);

        //Check access rights on partR
        if (!hasPartRevisionWriteAccess(user,partR)) {
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
    public List<PartIteration> getUsedByAsComponent(PartIterationKey pPartIPK) throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, PartRevisionNotFoundException, AccessRightException {
        PartRevisionKey partRevisionKey = pPartIPK.getPartRevision();
        User user = checkPartRevisionReadAccess(partRevisionKey);
        Locale locale = new Locale(user.getLanguage());
        return new PartIterationDAO(locale,em).findUsedByAsComponent(partRevisionKey.getPartMaster());
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID})
    @Override
    public List<PartIteration> getUsedByAsSubstitute(PartIterationKey pPartIPK) throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, PartRevisionNotFoundException, AccessRightException {
        PartRevisionKey partRevisionKey = pPartIPK.getPartRevision();
        User user = checkPartRevisionReadAccess(partRevisionKey);
        Locale locale = new Locale(user.getLanguage());
        return new PartIterationDAO(locale,em).findUsedByAsSubstitute(partRevisionKey.getPartMaster());
    }




    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public PartIteration getPartIteration(PartIterationKey pPartIPK) throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, PartRevisionNotFoundException, AccessRightException, PartIterationNotFoundException {

        PartRevisionKey partRevisionKey = pPartIPK.getPartRevision();
        User user = checkPartRevisionReadAccess(partRevisionKey);
        Locale locale = new Locale(user.getLanguage());        
        PartIterationDAO partIterationDAO = new PartIterationDAO(locale,em);
        
        PartIteration partI = partIterationDAO.loadPartI(pPartIPK);
        PartRevision partR = partI.getPartRevision();
        partR.getIteration(pPartIPK.getIteration());
        PartIteration lastIteration = partR.getLastIteration();
        
        if(isCheckoutByAnotherUser(user,partR) && lastIteration.getKey().equals(pPartIPK)){
            throw new AccessRightException("NotAllowedException25");
        }

        return partI;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public void updatePartRevisionACL(String workspaceId, PartRevisionKey revisionKey, Map<String, String> pACLUserEntries, Map<String, String> pACLUserGroupEntries) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, PartRevisionNotFoundException, AccessRightException, DocumentRevisionNotFoundException {

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
    public void removeACLFromPartRevision(PartRevisionKey revisionKey) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, PartRevisionNotFoundException, AccessRightException {

        User user = userManager.checkWorkspaceReadAccess(revisionKey.getPartMaster().getWorkspace());
        Locale locale = new Locale(user.getLanguage());

        PartRevisionDAO partRevisionDAO = new PartRevisionDAO(locale,em);
        PartRevision partRevision = partRevisionDAO.loadPartR(revisionKey);

        if (isAuthor(user, partRevision) || user.isAdministrator()) {
            ACL acl = partRevision.getACL();
            if (acl != null) {
                new ACLDAO(em).removeACLEntries(acl);
                partRevision.setACL(null);
            }
        }else{
            throw new AccessRightException(locale, user);
        }

    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public List<PartRevision> searchPartRevisions(PartSearchQuery pQuery) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ESServerException {
        User user = userManager.checkWorkspaceReadAccess(pQuery.getWorkspaceId());
        List<PartRevision> fetchedPartRs = esSearcher.search(pQuery);
        // Get Search Results

        ListIterator<PartRevision> ite = fetchedPartRs.listIterator();
        while (ite.hasNext()) {
            PartRevision partR = ite.next();

            if (isCheckoutByAnotherUser(user,partR)) {
            // Remove CheckedOut PartRevision From Results
                em.detach(partR);
                partR.removeLastIteration();
            }

            if (!hasPartRevisionReadAccess(user,partR)) {
                ite.remove();
            }
        }
        return new ArrayList<>(fetchedPartRs);
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public PartMaster findPartMasterByCADFileName(String workspaceId, String cadFileName) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException {
        User user = userManager.checkWorkspaceReadAccess(workspaceId);
        Locale locale = new Locale(user.getLanguage());

        BinaryResource br  = new BinaryResourceDAO(locale,em).findNativeCadBinaryResourceInWorkspace(workspaceId,cadFileName);
        if(br == null){
            return null;
        }
        String partNumber = br.getOwnerId();
        PartMasterKey partMasterKey = new PartMasterKey(workspaceId,partNumber);
        try {
            return new PartMasterDAO(locale,em).loadPartM(partMasterKey);
        } catch (PartMasterNotFoundException e) {
            return null;
        }

    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public Conversion getConversion(PartIterationKey partIterationKey) throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, PartRevisionNotFoundException, AccessRightException, PartIterationNotFoundException {
        User user = checkPartRevisionReadAccess(partIterationKey.getPartRevision());
        Locale locale = new Locale(user.getLanguage());
        PartIterationDAO partIterationDAO = new PartIterationDAO(locale,em);
        PartIteration partIteration = partIterationDAO.loadPartI(partIterationKey);
        ConversionDAO conversionDAO = new ConversionDAO(locale,em);
        Conversion conversion = conversionDAO.findConversion(partIteration);
        return conversion;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @TransactionAttribute(value = TransactionAttributeType.REQUIRES_NEW)
    @Override
    public Conversion createConversion(PartIterationKey partIterationKey) throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, PartRevisionNotFoundException, AccessRightException, PartIterationNotFoundException, CreationException {
        User user = checkPartRevisionWriteAccess(partIterationKey.getPartRevision());
        Locale locale = new Locale(user.getLanguage());
        PartIterationDAO partIterationDAO = new PartIterationDAO(locale,em);
        PartIteration partIteration = partIterationDAO.loadPartI(partIterationKey);
        ConversionDAO conversionDAO = new ConversionDAO(locale,em);
        Conversion conversion = new Conversion(partIteration);
        conversionDAO.createConversion(conversion);
        return conversion;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @TransactionAttribute(value = TransactionAttributeType.REQUIRES_NEW)
    @Override
    public void removeConversion(PartIterationKey partIterationKey) throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, PartRevisionNotFoundException, AccessRightException, PartIterationNotFoundException {
        User user = checkPartRevisionWriteAccess(partIterationKey.getPartRevision());
        Locale locale = new Locale(user.getLanguage());
        PartIterationDAO partIterationDAO = new PartIterationDAO(locale,em);
        PartIteration partIteration = partIterationDAO.loadPartI(partIterationKey);
        ConversionDAO conversionDAO = new ConversionDAO(locale,em);
        Conversion conversion = conversionDAO.findConversion(partIteration);
        conversionDAO.deleteConversion(conversion);
    }
    
    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @TransactionAttribute(value = TransactionAttributeType.REQUIRES_NEW)
    @Override
    public void endConversion(PartIterationKey partIterationKey, boolean succeed) throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, PartRevisionNotFoundException, AccessRightException, PartIterationNotFoundException {
        User user = checkPartRevisionWriteAccess(partIterationKey.getPartRevision());
        Locale locale = new Locale(user.getLanguage());
        PartIterationDAO partIterationDAO = new PartIterationDAO(locale,em);
        PartIteration partIteration = partIterationDAO.loadPartI(partIterationKey);
        ConversionDAO conversionDAO = new ConversionDAO(locale,em);
        Conversion conversion = conversionDAO.findConversion(partIteration);
        conversion.setPending(false);
        conversion.setSucceed(succeed);
        conversion.setEndDate(new Date());
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public void updateACLForPartMasterTemplate(String pWorkspaceId, String templateId, Map<String, String> userEntries, Map<String, String> groupEntries) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, AccessRightException, PartMasterTemplateNotFoundException {

        ACLFactory aclFactory = new ACLFactory(em);

        // Check the read access to the workspace
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
        // Load the part template
        PartMasterTemplateKey pKey = new PartMasterTemplateKey(pWorkspaceId,templateId);
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
    public void removeACLFromPartMasterTemplate(String workspaceId, String partTemplateId) throws PartMasterNotFoundException, UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, PartMasterTemplateNotFoundException, AccessRightException {

        // Check the read access to the workspace
        User user = userManager.checkWorkspaceReadAccess(workspaceId);
        Locale locale = new Locale(user.getLanguage());
        // Load the part template
        PartMasterTemplateKey pKey = new PartMasterTemplateKey(workspaceId,partTemplateId);
        PartMasterTemplate partMaster = new PartMasterTemplateDAO(new Locale(user.getLanguage()), em).loadPartMTemplate(pKey);

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
    public PartRevision saveTags(PartRevisionKey revisionKey, String[] pTags) throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, PartRevisionNotFoundException, AccessRightException, TagException {

        User user = checkPartRevisionWriteAccess(revisionKey);

        Locale userLocale = new Locale(user.getLanguage());
        PartRevisionDAO partRevDAO = new PartRevisionDAO(userLocale, em);
        PartRevision partRevision = partRevDAO.loadPartR(revisionKey);

        Set<Tag> tags = new HashSet<>();
        if (pTags !=  null){
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

            partRevision.setTags(tags);

            if (isCheckoutByAnotherUser(user,partRevision)) {
                em.detach(partRevision);
                partRevision.removeLastIteration();
            }

            for (PartIteration partIteration:partRevision.getPartIterations()){
                esIndexer.index(partIteration);
            }
        }
        else{
            throw new TagException("null tag");
        }


        return partRevision;

    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public PartRevision removeTag(PartRevisionKey partRevisionKey, String tagName) throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, PartRevisionNotFoundException, AccessRightException {
        User user = checkPartRevisionWriteAccess(partRevisionKey);

        PartRevision partRevision = getPartRevision(partRevisionKey);
        Tag tagToRemove = new Tag(user.getWorkspace(), tagName);
        partRevision.getTags().remove(tagToRemove);

        if (isCheckoutByAnotherUser(user,partRevision)) {
            em.detach(partRevision);
            partRevision.removeLastIteration();
        }

        for (PartIteration partIteration:partRevision.getPartIterations()){
            esIndexer.index(partIteration);
        }
        return partRevision;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public PartRevision[] findPartRevisionsByTag(String workspaceId, String tagId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException {

        User user = userManager.checkWorkspaceReadAccess(workspaceId);

        List<PartRevision> partsRevision = new PartRevisionDAO(new Locale(user.getLanguage()),em).findPartByTag(new Tag(user.getWorkspace(), tagId));
        ListIterator<PartRevision> iterator = partsRevision.listIterator();
        while (iterator.hasNext()) {
            PartRevision partRevision = iterator.next();
            if (!hasPartRevisionReadAccess(user,partRevision)){
                iterator.remove();
            }else if(isCheckoutByAnotherUser(user,partRevision)){
                em.detach(partRevision);
                partRevision.removeLastIteration();
            }
        }
        return partsRevision.toArray(new PartRevision[partsRevision.size()]);
    }


    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public PartRevision[] getPartRevisionsWithReferenceOrName(String pWorkspaceId, String reference, int maxResults) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
        List<PartRevision> partRs = new PartRevisionDAO(new Locale(user.getLanguage()), em).findPartsRevisionsWithReferenceOrNameLike(pWorkspaceId, reference, maxResults);
        return partRs.toArray(new PartRevision[partRs.size()]);
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public PartRevision releasePartRevision(PartRevisionKey pRevisionKey) throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, PartRevisionNotFoundException, AccessRightException, NotAllowedException {
        User user = checkPartRevisionWriteAccess(pRevisionKey);                                                         // Check if the user can write the part
        Locale locale = new Locale(user.getLanguage());

        PartRevisionDAO partRevisionDAO = new PartRevisionDAO(locale,em);
        PartRevision partRevision = partRevisionDAO.loadPartR(pRevisionKey);

        if(partRevision.isCheckedOut()){
            throw new NotAllowedException(locale, "NotAllowedException46");
        }

        if (partRevision.getNumberOfIterations() == 0) {
            throw new NotAllowedException(locale, "NotAllowedException41");
        }

        if(partRevision.isObsolete()){
            throw new NotAllowedException(locale, "NotAllowedException38");
        }

        partRevision.release();
        return partRevision;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public PartRevision markPartRevisionAsObsolete(PartRevisionKey pRevisionKey) throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, PartRevisionNotFoundException, AccessRightException, NotAllowedException {
        User user = checkPartRevisionWriteAccess(pRevisionKey);                                                         // Check if the user can write the part
        Locale locale = new Locale(user.getLanguage());

        PartRevisionDAO partRevisionDAO = new PartRevisionDAO(locale,em);
        PartRevision partRevision = partRevisionDAO.loadPartR(pRevisionKey);

        if(!partRevision.isReleased()){
            throw new NotAllowedException(locale, "NotAllowedException36");
        }

        partRevision.markAsObsolete();
        return partRevision;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public PartRevision getLastReleasePartRevision(ConfigurationItemKey ciKey)
            throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ConfigurationItemNotFoundException, AccessRightException, PartRevisionNotFoundException {
        User user = userManager.checkWorkspaceReadAccess(ciKey.getWorkspace());
        Locale locale = new Locale(user.getLanguage());
        ConfigurationItem ci = new ConfigurationItemDAO(locale,em).loadConfigurationItem(ciKey);
        PartMaster partMaster = ci.getDesignItem();
        PartRevision lastReleasedRevision = partMaster.getLastReleasedRevision();
        if(lastReleasedRevision == null){
            throw new PartRevisionNotFoundException(locale,partMaster.getNumber(),"Released");
        }
        if(!canUserAccess(user, lastReleasedRevision.getKey())){
            throw new AccessRightException(locale,user);
        }
        return lastReleasedRevision;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public List<ProductBaseline> findBaselinesWherePartRevisionHasIterations(PartRevisionKey partRevisionKey) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, PartRevisionNotFoundException {
        User user = userManager.checkWorkspaceReadAccess(partRevisionKey.getPartMaster().getWorkspace());
        Locale locale = new Locale(user.getLanguage());
        PartRevision partRevision = new PartRevisionDAO(locale,em).loadPartR(partRevisionKey);
        return new ProductBaselineDAO(locale,em).findBaselineWherePartRevisionHasIterations(partRevision);
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public List<PartUsageLink> getComponents(PartIterationKey pPartIPK) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, PartIterationNotFoundException, NotAllowedException {
        User user = userManager.checkWorkspaceReadAccess(pPartIPK.getWorkspaceId());
        Locale locale = new Locale(user.getLanguage());
        PartIteration partI = new PartIterationDAO(locale, em).loadPartI(pPartIPK);
        PartRevision partR = partI.getPartRevision();

        if (isCheckoutByAnotherUser(user,partR) && partR.getLastIteration().equals(partI)) {
            throw new NotAllowedException(locale, "NotAllowedException34");
        }
        return partI.getComponents();
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public boolean partMasterExists(PartMasterKey partMasterKey) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException {
        User user = userManager.checkWorkspaceReadAccess(partMasterKey.getWorkspace());
        try {
            new PartMasterDAO(new Locale(user.getLanguage()), em).loadPartM(partMasterKey);
            return true;
        } catch (PartMasterNotFoundException e) {
            LOGGER.log(Level.FINEST,null,e);
            return false;
        }
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public void deleteConfigurationItem(ConfigurationItemKey configurationItemKey) throws UserNotFoundException, WorkspaceNotFoundException, AccessRightException, NotAllowedException, UserNotActiveException, ConfigurationItemNotFoundException, LayerNotFoundException, EntityConstraintException {
        User user = userManager.checkWorkspaceReadAccess(configurationItemKey.getWorkspace());
        Locale locale = new Locale(user.getLanguage());

        ProductBaselineDAO productBaselineDAO = new ProductBaselineDAO(locale,em);
        List<ProductBaseline> productBaselines = productBaselineDAO.findBaselines(configurationItemKey.getId(), configurationItemKey.getWorkspace());

        if(!productBaselines.isEmpty() ){
            throw new EntityConstraintException(locale,"EntityConstraintException4");
        }

        ProductInstanceMasterDAO productInstanceMasterDAO = new ProductInstanceMasterDAO(locale,em);
        List<ProductInstanceMaster> productInstanceMasters = productInstanceMasterDAO.findProductInstanceMasters(configurationItemKey.getId(), configurationItemKey.getWorkspace());

        if(!productInstanceMasters.isEmpty()){
            throw new EntityConstraintException(locale,"EntityConstraintException13");
        }

        new ConfigurationItemDAO(locale,em).removeConfigurationItem(configurationItemKey);
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public void deleteLayer(String workspaceId, int layerId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, LayerNotFoundException, AccessRightException {
        Layer layer = new LayerDAO(em).loadLayer(layerId);
        User user = userManager.checkWorkspaceWriteAccess(layer.getConfigurationItem().getWorkspaceId());
        new LayerDAO(new Locale(user.getLanguage()),em).deleteLayer(layer);
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public void removeCADFileFromPartIteration(PartIterationKey partIKey)
            throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, PartIterationNotFoundException, PartRevisionNotFoundException {
        User user = userManager.checkWorkspaceReadAccess(partIKey.getWorkspaceId());
        Locale locale = new Locale(user.getLanguage());

        PartRevisionDAO partRDAO = new PartRevisionDAO(locale, em);
        PartRevision partR = partRDAO.loadPartR(partIKey.getPartRevision());
        PartIteration partIteration = partR.getIteration(partIKey.getIteration());
        if (isCheckoutByUser(user,partR) && partR.getLastIteration().equals(partIteration)) {
            removeCADFile(partIteration);
        }
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public BinaryResource renameCADFileInPartIteration(String pFullName, String pNewName) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, NotAllowedException, FileNotFoundException, FileAlreadyExistsException, CreationException, StorageException {

        User user = userManager.checkWorkspaceReadAccess(BinaryResource.parseWorkspaceId(pFullName));
        Locale userLocale = new Locale(user.getLanguage());
        checkNameFileValidity(pNewName, userLocale);

        BinaryResourceDAO binDAO = new BinaryResourceDAO(userLocale, em);
        BinaryResource file = binDAO.loadBinaryResource(pFullName);
        PartIteration partIteration = binDAO.getPartOwner(file);

        PartRevision partR = partIteration.getPartRevision();

        if (isCheckoutByUser(user,partR) && partR.getLastIteration().equals(partIteration)) {

            dataManager.renameFile(file, pNewName);

            partIteration.setNativeCADFile(null);
            binDAO.removeBinaryResource(file);

            BinaryResource newFile = new BinaryResource(file.getNewFullName(pNewName),file.getContentLength(), file.getLastModified());

            binDAO.createBinaryResource(newFile);
            partIteration.setNativeCADFile(newFile);

            return newFile;
        } else{
            throw new NotAllowedException(userLocale, "NotAllowedException35");
        }

    }

    private void removeCADFile(PartIteration partIteration)
            throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, PartIterationNotFoundException {

        BinaryResource br = partIteration.getNativeCADFile();
        if(br != null){
            try {
                dataManager.deleteData(br);
            } catch (StorageException e) {
                LOGGER.log(Level.INFO, null, e);
            }
            partIteration.setNativeCADFile(null);
        }

        List<Geometry> geometries = new ArrayList<>(partIteration.getGeometries());
        for(Geometry geometry : geometries){
            try {
                dataManager.deleteData(geometry);
            } catch (StorageException e) {
                LOGGER.log(Level.INFO, null, e);
            }
            partIteration.removeGeometry(geometry);
        }

        Set<BinaryResource> attachedFiles = new HashSet<>(partIteration.getAttachedFiles());
        for(BinaryResource attachedFile : attachedFiles){
            try {
                dataManager.deleteData(attachedFile);
            } catch (StorageException e) {
                LOGGER.log(Level.INFO, null, e);
            }
            partIteration.removeFile(attachedFile);
        }
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public PartMaster getPartMaster(PartMasterKey pPartMPK) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, PartMasterNotFoundException {
        User user = userManager.checkWorkspaceReadAccess(pPartMPK.getWorkspace());
        PartMaster partM = new PartMasterDAO(new Locale(user.getLanguage()), em).loadPartM(pPartMPK);

        for (PartRevision partR : partM.getPartRevisions()) {
            if (isCheckoutByAnotherUser(user,partR)) {
                em.detach(partR);
                partR.removeLastIteration();
            }
        }
        return partM;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public List<Layer> getLayers(ConfigurationItemKey pKey) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException {
        User user = userManager.checkWorkspaceReadAccess(pKey.getWorkspace());
        return new LayerDAO(new Locale(user.getLanguage()), em).findAllLayers(pKey);
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public Layer getLayer(int pId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, LayerNotFoundException {
        Layer layer = new LayerDAO(em).loadLayer(pId);
        userManager.checkWorkspaceReadAccess(layer.getConfigurationItem().getWorkspaceId());
        return layer;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public Layer createLayer(ConfigurationItemKey pKey, String pName, String color) throws UserNotFoundException, WorkspaceNotFoundException, AccessRightException, ConfigurationItemNotFoundException {
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
    public Layer updateLayer(ConfigurationItemKey pKey, int pId, String pName, String color) throws UserNotFoundException, WorkspaceNotFoundException, AccessRightException, ConfigurationItemNotFoundException, LayerNotFoundException, UserNotActiveException {
        Layer layer = getLayer(pId);
        userManager.checkWorkspaceWriteAccess(layer.getConfigurationItem().getWorkspaceId());
        layer.setName(pName);
        layer.setColor(color);
        return layer;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public Marker createMarker(int pLayerId, String pTitle, String pDescription, double pX, double pY, double pZ) throws LayerNotFoundException, UserNotFoundException, WorkspaceNotFoundException, AccessRightException {
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
    public void deleteMarker(int pLayerId, int pMarkerId) throws WorkspaceNotFoundException, UserNotActiveException, LayerNotFoundException, UserNotFoundException, AccessRightException, MarkerNotFoundException {
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
    public PartMasterTemplate[] getPartMasterTemplates(String pWorkspaceId) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
        List<PartMasterTemplate> templates = new PartMasterTemplateDAO(new Locale(user.getLanguage()), em).findAllPartMTemplates(pWorkspaceId);

        ListIterator<PartMasterTemplate> ite = templates.listIterator();
        while (ite.hasNext()){
            PartMasterTemplate template = ite.next();
            if(!hasPartTemplateReadAccess(user, template)){
                ite.remove();
            }
        }

        return templates.toArray(new PartMasterTemplate[templates.size()]);
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public PartMasterTemplate getPartMasterTemplate(PartMasterTemplateKey pKey) throws WorkspaceNotFoundException, PartMasterTemplateNotFoundException, UserNotFoundException, UserNotActiveException {
        User user = userManager.checkWorkspaceReadAccess(pKey.getWorkspaceId());
        return new PartMasterTemplateDAO(new Locale(user.getLanguage()), em).loadPartMTemplate(pKey);
    }


    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public PartMasterTemplate createPartMasterTemplate(String pWorkspaceId, String pId, String pPartType, String pWorkflowModelId, String pMask, InstanceAttributeTemplate[] pAttributeTemplates, String[] lovNames, boolean idGenerated, boolean attributesLocked) throws WorkspaceNotFoundException, AccessRightException, PartMasterTemplateAlreadyExistsException, UserNotFoundException, NotAllowedException, CreationException, WorkflowModelNotFoundException, ListOfValuesNotFoundException {
        User user = userManager.checkWorkspaceWriteAccess(pWorkspaceId);
        Locale locale = new Locale(user.getLanguage());
        checkNameValidity(pId, locale);

        //Check pMask
        if (pMask!= null && !pMask.isEmpty() && !NamingConvention.correctNameMask(pMask)){
            throw new NotAllowedException(locale, "MaskCreationException");
        }

        PartMasterTemplate template = new PartMasterTemplate(user.getWorkspace(), pId, user, pPartType, pMask, attributesLocked);
        Date now = new Date();
        template.setCreationDate(now);
        template.setIdGenerated(idGenerated);
        LOVDAO lovDAO=new LOVDAO(locale,em);

        List<InstanceAttributeTemplate> attrs = new ArrayList<>();
        for(int i=0;i<pAttributeTemplates.length;i++){
            attrs.add(pAttributeTemplates[i]);
            if(pAttributeTemplates[i] instanceof ListOfValuesAttributeTemplate){
                ListOfValuesAttributeTemplate lovAttr=(ListOfValuesAttributeTemplate)pAttributeTemplates[i];
                ListOfValuesKey lovKey = new ListOfValuesKey(user.getWorkspaceId(), lovNames[i]);
                lovAttr.setLov(lovDAO.loadLOV(lovKey));
            }
        }
        template.setAttributeTemplates(attrs);

        if (pWorkflowModelId != null){
            WorkflowModel workflowModel = new WorkflowModelDAO(locale, em).loadWorkflowModel(new WorkflowModelKey(user.getWorkspaceId(), pWorkflowModelId));
            template.setWorkflowModel(workflowModel);
        }

        new PartMasterTemplateDAO(locale, em).createPartMTemplate(template);
        return template;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public PartMasterTemplate updatePartMasterTemplate(PartMasterTemplateKey pKey, String pPartType, String pWorkflowModelId, String pMask, InstanceAttributeTemplate[] pAttributeTemplates, String[] lovNames, boolean idGenerated, boolean attributesLocked) throws WorkspaceNotFoundException, AccessRightException, PartMasterTemplateNotFoundException, UserNotFoundException, WorkflowModelNotFoundException, UserNotActiveException, ListOfValuesNotFoundException {
        User user = userManager.checkWorkspaceReadAccess(pKey.getWorkspaceId());
        Locale locale = new Locale(user.getLanguage());

        PartMasterTemplateDAO templateDAO = new PartMasterTemplateDAO(new Locale(user.getLanguage()), em);
        PartMasterTemplate template = templateDAO.loadPartMTemplate(pKey);

        checkPartTemplateWriteAccess(template,user);

        Date now = new Date();
        template.setCreationDate(now);
        template.setAuthor(user);
        template.setPartType(pPartType);
        template.setMask(pMask);
        template.setIdGenerated(idGenerated);
        template.setAttributesLocked(attributesLocked);
        LOVDAO lovDAO=new LOVDAO(locale,em);

        List<InstanceAttributeTemplate> attrs = new ArrayList<>();
        for(int i=0;i<pAttributeTemplates.length;i++){
            attrs.add(pAttributeTemplates[i]);
            if(pAttributeTemplates[i] instanceof ListOfValuesAttributeTemplate){
                ListOfValuesAttributeTemplate lovAttr=(ListOfValuesAttributeTemplate)pAttributeTemplates[i];
                ListOfValuesKey lovKey = new ListOfValuesKey(user.getWorkspaceId(), lovNames[i]);
                lovAttr.setLov(lovDAO.loadLOV(lovKey));
            }
        }

        template.setAttributeTemplates(attrs);

        WorkflowModel workflowModel = null;
        if (pWorkflowModelId != null) {
            workflowModel = new WorkflowModelDAO(locale, em).loadWorkflowModel(new WorkflowModelKey(user.getWorkspaceId(), pWorkflowModelId));
        }
        template.setWorkflowModel(workflowModel);

        return template;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public void deletePartMasterTemplate(PartMasterTemplateKey pKey) throws WorkspaceNotFoundException, AccessRightException, PartMasterTemplateNotFoundException, UserNotFoundException, UserNotActiveException {
        User user = userManager.checkWorkspaceReadAccess(pKey.getWorkspaceId());
        PartMasterTemplateDAO templateDAO = new PartMasterTemplateDAO(new Locale(user.getLanguage()), em);

        PartMasterTemplate partMasterTemplate = templateDAO.loadPartMTemplate(pKey);
        checkPartTemplateWriteAccess(partMasterTemplate,user);

        PartMasterTemplate template = templateDAO.removePartMTemplate(pKey);
        BinaryResource file = template.getAttachedFile();
        if(file != null){
            try {
                dataManager.deleteData(file);
            } catch (StorageException e) {
                LOGGER.log(Level.INFO, null, e);
            }
        }
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public BinaryResource saveFileInTemplate(PartMasterTemplateKey pPartMTemplateKey, String pName, long pSize) throws WorkspaceNotFoundException, NotAllowedException, PartMasterTemplateNotFoundException, FileAlreadyExistsException, UserNotFoundException, UserNotActiveException, CreationException, AccessRightException {
        User user = userManager.checkWorkspaceReadAccess(pPartMTemplateKey.getWorkspaceId());
        Locale locale = new Locale(user.getLanguage());
        checkNameFileValidity(pName,locale);

        PartMasterTemplateDAO templateDAO = new PartMasterTemplateDAO(locale,em);
        PartMasterTemplate template = templateDAO.loadPartMTemplate(pPartMTemplateKey);

        checkPartTemplateWriteAccess(template,user);

        BinaryResource binaryResource = null;
        String fullName = template.getWorkspaceId() + "/part-templates/" + template.getId() + "/" + pName;

        BinaryResource bin = template.getAttachedFile();
        if(bin != null && bin.getFullName().equals(fullName)) {
            binaryResource = bin;
        }

        if (binaryResource == null) {
            binaryResource = new BinaryResource(fullName, pSize, new Date());
            new BinaryResourceDAO(locale,em).createBinaryResource(binaryResource);
            template.setAttachedFile(binaryResource);
        } else {
            binaryResource.setContentLength(pSize);
            binaryResource.setLastModified(new Date());
        }
        return binaryResource;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public PartMasterTemplate removeFileFromTemplate(String pFullName) throws WorkspaceNotFoundException, PartMasterTemplateNotFoundException, AccessRightException, FileNotFoundException, UserNotFoundException, UserNotActiveException {
        User user = userManager.checkWorkspaceReadAccess(BinaryResource.parseWorkspaceId(pFullName));
        BinaryResourceDAO binDAO = new BinaryResourceDAO(new Locale(user.getLanguage()), em);
        BinaryResource file = binDAO.loadBinaryResource(pFullName);

        PartMasterTemplate template = binDAO.getPartTemplateOwner(file);

        checkPartTemplateWriteAccess(template, user);

        try {
            dataManager.deleteData(file);
        } catch (StorageException e) {
            LOGGER.log(Level.INFO, null, e);
        }
        template.setAttachedFile(null);
        binDAO.removeBinaryResource(file);
        return template;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public BinaryResource renameFileInTemplate(String pFullName, String pNewName) throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException, FileNotFoundException, UserNotActiveException, FileAlreadyExistsException, CreationException, StorageException {
        User user = userManager.checkWorkspaceReadAccess(BinaryResource.parseWorkspaceId(pFullName));

        BinaryResourceDAO binDAO = new BinaryResourceDAO(new Locale(user.getLanguage()), em);
        BinaryResource file = binDAO.loadBinaryResource(pFullName);
        if (file == null){
            throw new FileNotFoundException(new Locale(user.getLanguage()),pFullName);
        }
        PartMasterTemplate template = binDAO.getPartTemplateOwner(file);

        checkPartTemplateWriteAccess(template,user);

        dataManager.renameFile(file, pNewName);

        template.setAttachedFile(null);
        binDAO.removeBinaryResource(file);

        BinaryResource newFile = new BinaryResource(file.getNewFullName(pNewName),file.getContentLength(), file.getLastModified());

        binDAO.createBinaryResource(newFile);
        template.setAttachedFile(newFile);

        return newFile;

    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public List<PartMaster> getPartMasters(String pWorkspaceId, int start, int pMaxResults) throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException, UserNotActiveException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
        return new PartMasterDAO(new Locale(user.getLanguage()), em).getPartMasters(pWorkspaceId, start, pMaxResults);
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public List<PartRevision> getPartRevisions(String pWorkspaceId, int start, int pMaxResults) throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException, UserNotActiveException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
        List<PartRevision> partRevisions = new PartRevisionDAO(new Locale(user.getLanguage()), em).getPartRevisions(pWorkspaceId, start, pMaxResults);
        List<PartRevision> filteredPartRevisions = new ArrayList<>();

        for(PartRevision partRevision : partRevisions){
            try{
                checkPartRevisionReadAccess(partRevision.getKey());

                if (isCheckoutByAnotherUser(user,partRevision)) {
                    em.detach(partRevision);
                    partRevision.removeLastIteration();
                }

                filteredPartRevisions.add(partRevision);

            } catch (AccessRightException | PartRevisionNotFoundException e) {
                LOGGER.log(Level.FINER,null,e);
            }
        }
        return filteredPartRevisions;
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID})
    @Override
    public int getPartsInWorkspaceCount(String pWorkspaceId) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
        return new PartRevisionDAO(new Locale(user.getLanguage()), em).getPartRevisionCountFiltered(user, pWorkspaceId);
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID,UserGroupMapping.ADMIN_ROLE_ID})
    @Override
    public int getTotalNumberOfParts(String pWorkspaceId) throws AccessRightException, WorkspaceNotFoundException, AccountNotFoundException, UserNotFoundException, UserNotActiveException {
        Locale locale;
        if(!userManager.isCallerInRole(UserGroupMapping.ADMIN_ROLE_ID)){
            User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
            locale = new Locale(user.getLanguage());
        }else{
            locale = Locale.getDefault();
        }
        //Todo count only part you can see
        return new PartRevisionDAO(locale, em).getTotalNumberOfParts(pWorkspaceId);
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public void deletePartMaster(PartMasterKey partMasterKey) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, PartMasterNotFoundException, EntityConstraintException, ESServerException {

        User user = userManager.checkWorkspaceReadAccess(partMasterKey.getWorkspace());
        Locale locale = new Locale(user.getLanguage());

        PartMasterDAO partMasterDAO = new PartMasterDAO(locale, em);
        PartUsageLinkDAO partUsageLinkDAO = new PartUsageLinkDAO(locale, em);
        ProductBaselineDAO productBaselineDAO = new ProductBaselineDAO(locale,em);
        ConfigurationItemDAO configurationItemDAO = new ConfigurationItemDAO(locale,em);
        PartMaster partMaster = partMasterDAO.loadPartM(partMasterKey);

        // check if part is linked to a product
        if(configurationItemDAO.isPartMasterLinkedToConfigurationItem(partMaster)){
            throw new EntityConstraintException(locale,"EntityConstraintException1");
        }

        // check if this part is in a partUsage
        if(partUsageLinkDAO.hasPartUsages(partMasterKey.getWorkspace(),partMasterKey.getNumber())){
            throw new EntityConstraintException(locale,"EntityConstraintException2");
        }

        // check if part is baselined
        if(productBaselineDAO.existBaselinedPart(partMasterKey.getWorkspace(),partMasterKey.getNumber())){
            throw new EntityConstraintException(locale,"EntityConstraintException5");
        }

        // delete CAD files attached with this partMaster
        // and notified remove part observers
        for (PartRevision partRevision : partMaster.getPartRevisions()) {
            partRevisionEvent.select(new AnnotationLiteral<Removed>(){}).fire(new PartRevisionChangeEvent(partRevision));
            for (PartIteration partIteration : partRevision.getPartIterations()) {
                try {
                    removeCADFile(partIteration);
                } catch (PartIterationNotFoundException e) {
                    LOGGER.log(Level.INFO, null, e);
                }
            }
        }

        // delete ElasticSearch Index for this revision iteration
        for (PartRevision partRevision : partMaster.getPartRevisions()) {
            for (PartIteration partIteration : partRevision.getPartIterations()) {
                esIndexer.delete(partIteration);
                // Remove ElasticSearch Index for this PartIteration
            }
        }

        // ok to delete
        partMasterDAO.removePartM(partMaster);
    }


    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public void deletePartRevision(PartRevisionKey partRevisionKey) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, PartRevisionNotFoundException, EntityConstraintException, ESServerException {

        User user = userManager.checkWorkspaceReadAccess(partRevisionKey.getPartMaster().getWorkspace());
        Locale locale = new Locale(user.getLanguage());

        PartMasterDAO partMasterDAO = new PartMasterDAO(locale, em);
        PartRevisionDAO partRevisionDAO = new PartRevisionDAO(locale, em);
        PartUsageLinkDAO partUsageLinkDAO = new PartUsageLinkDAO(locale, em);
        ProductBaselineDAO productBaselineDAO = new ProductBaselineDAO(locale,em);

        ConfigurationItemDAO configurationItemDAO = new ConfigurationItemDAO(locale,em);

        PartRevision partR = partRevisionDAO.loadPartR(partRevisionKey);
        PartMaster partMaster = partR.getPartMaster();
        boolean isLastRevision = partMaster.getPartRevisions().size() == 1;

        //TODO all the 3 removal restrictions may be performed
        //more precisely on PartRevision rather on PartMaster
        // check if part is linked to a product
        if(configurationItemDAO.isPartMasterLinkedToConfigurationItem(partMaster)){
            throw new EntityConstraintException(locale,"EntityConstraintException1");
        }
        // check if this part is in a partUsage
        if(partUsageLinkDAO.hasPartUsages(partMaster.getWorkspaceId(),partMaster.getNumber())){
            throw new EntityConstraintException(locale,"EntityConstraintException2");
        }

        // check if part is baselined
        if(productBaselineDAO.existBaselinedPart(partMaster.getWorkspaceId(),partMaster.getNumber())){
            throw new EntityConstraintException(locale,"EntityConstraintException5");
        }

        // delete ElasticSearch Index for this revision iteration
        for (PartIteration partIteration : partR.getPartIterations()) {
            esIndexer.delete(partIteration);
            // Remove ElasticSearch Index for this PartIteration
        }

        // delete CAD files attached with this partMaster
        for (PartIteration partIteration : partR.getPartIterations()) {
            try {
                removeCADFile(partIteration);
            } catch (PartIterationNotFoundException e) {
                LOGGER.log(Level.INFO, null, e);
            }
        }
        partRevisionEvent.select(new AnnotationLiteral<Removed>(){}).fire(new PartRevisionChangeEvent(partR));
        if(isLastRevision){
            partMasterDAO.removePartM(partMaster);
        }else{
            partMaster.removeRevision(partR);
            partRevisionDAO.removeRevision(partR);
        }

    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public int getNumberOfIteration(PartRevisionKey partRevisionKey) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, PartRevisionNotFoundException {
        User user = userManager.checkWorkspaceReadAccess(partRevisionKey.getPartMaster().getWorkspace());
        return new PartRevisionDAO(new Locale(user.getLanguage()), em).loadPartR(partRevisionKey).getLastIterationNumber();
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public PartRevision createPartRevision(PartRevisionKey revisionKey, String pDescription, String pWorkflowModelId, ACLUserEntry[] pACLUserEntries, ACLUserGroupEntry[] pACLUserGroupEntries, Map<String, String> roleMappings) throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException, PartRevisionNotFoundException, NotAllowedException, FileAlreadyExistsException, CreationException, RoleNotFoundException, WorkflowModelNotFoundException, PartRevisionAlreadyExistsException {
        User user = userManager.checkWorkspaceWriteAccess(revisionKey.getPartMaster().getWorkspace());
        Locale locale = new Locale(user.getLanguage());

        PartRevisionDAO partRevisionDAO = new PartRevisionDAO(locale,em);

        PartRevision originalPartR = partRevisionDAO.loadPartR(revisionKey);

        if(originalPartR.isCheckedOut()){
            throw new NotAllowedException(locale, "NotAllowedException40");
        }

        if (originalPartR.getNumberOfIterations() == 0) {
            throw new NotAllowedException(locale, "NotAllowedException41");
        }

        PartRevision partR = originalPartR.getPartMaster().createNextRevision(user);

        PartIteration lastPartI = originalPartR.getLastIteration();
        PartIteration firstPartI = partR.createNextIteration(user);


        if(lastPartI != null){

            BinaryResourceDAO binDAO = new BinaryResourceDAO(locale, em);
            for (BinaryResource sourceFile : lastPartI.getAttachedFiles()) {
                String fileName = sourceFile.getName();
                long length = sourceFile.getContentLength();
                Date lastModified = sourceFile.getLastModified();
                String fullName = partR.getWorkspaceId() + "/parts/" + partR.getPartNumber() + "/" + partR.getVersion() + "/1/"+  fileName;
                BinaryResource targetFile = new BinaryResource(fullName, length, lastModified);
                binDAO.createBinaryResource(targetFile);
                firstPartI.addFile(targetFile);
                try {
                    dataManager.copyData(sourceFile, targetFile);
                } catch (StorageException e) {
                    LOGGER.log(Level.INFO, null, e);
                }
            }

            // copy components
            List<PartUsageLink> components = new LinkedList<>();
            for (PartUsageLink usage : lastPartI.getComponents()) {
                PartUsageLink newUsage = usage.clone();
                components.add(newUsage);
            }
            firstPartI.setComponents(components);

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
                    dataManager.copyData(sourceFile, targetFile);
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
                    dataManager.copyData(nativeCADFile, targetFile);
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


        if (pWorkflowModelId != null) {

            UserDAO userDAO = new UserDAO(locale,em);
            RoleDAO roleDAO = new RoleDAO(locale,em);

            Map<Role,User> roleUserMap = new HashMap<>();

            for (Object o : roleMappings.entrySet()) {
                Map.Entry pairs = (Map.Entry) o;
                String roleName = (String) pairs.getKey();
                String userLogin = (String) pairs.getValue();
                User worker = userDAO.loadUser(new UserKey(originalPartR.getWorkspaceId(), userLogin));
                Role role = roleDAO.loadRole(new RoleKey(originalPartR.getWorkspaceId(), roleName));
                roleUserMap.put(role, worker);
            }

            WorkflowModel workflowModel = new WorkflowModelDAO(locale, em).loadWorkflowModel(new WorkflowModelKey(user.getWorkspaceId(), pWorkflowModelId));
            Workflow workflow = workflowModel.createWorkflow(roleUserMap);
            partR.setWorkflow(workflow);

            Collection<Task> runningTasks = workflow.getRunningTasks();
            for (Task runningTask : runningTasks) {
                runningTask.start();
            }
            mailer.sendApproval(runningTasks, partR);
        }

        partR.setDescription(pDescription);

        if ((pACLUserEntries != null && pACLUserEntries.length > 0) || (pACLUserGroupEntries != null && pACLUserGroupEntries.length > 0)) {
            ACL acl = new ACL();
            if (pACLUserEntries != null) {
                for (ACLUserEntry entry : pACLUserEntries) {
                    acl.addEntry(em.getReference(User.class, new UserKey(user.getWorkspaceId(), entry.getPrincipalLogin())), entry.getPermission());
                }
            }

            if (pACLUserGroupEntries != null) {
                for (ACLUserGroupEntry entry : pACLUserGroupEntries) {
                    acl.addEntry(em.getReference(UserGroup.class, new UserGroupKey(user.getWorkspaceId(), entry.getPrincipalId())), entry.getPermission());
                }
            }
            partR.setACL(acl);
        }
        Date now = new Date();
        partR.setCreationDate(now);
        partR.setCheckOutUser(user);
        partR.setCheckOutDate(now);
        firstPartI.setCreationDate(now);

        partRevisionDAO.createPartR(partR);

        return partR;

    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public String generateId(String pWorkspaceId, String pPartMTemplateId) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException, PartMasterTemplateNotFoundException {

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
            LOGGER.log(Level.WARNING,"Different mask has been used for the same document type",ex);
        } catch (NoResultException ex) {
            LOGGER.log(Level.FINE,"No document of the specified type has been created",ex);
        }
        return newId;

    }


    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID,UserGroupMapping.ADMIN_ROLE_ID})
    @Override
    public long getDiskUsageForPartsInWorkspace(String pWorkspaceId) throws WorkspaceNotFoundException, AccessRightException, AccountNotFoundException {
        Account account = userManager.checkAdmin(pWorkspaceId);
        return new PartMasterDAO(new Locale(account.getLanguage()), em).getDiskUsageForPartsInWorkspace(pWorkspaceId);
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID,UserGroupMapping.ADMIN_ROLE_ID})
    @Override
    public long getDiskUsageForPartTemplatesInWorkspace(String pWorkspaceId) throws WorkspaceNotFoundException, AccessRightException, AccountNotFoundException {
        Account account = userManager.checkAdmin(pWorkspaceId);
        return new PartMasterDAO(new Locale(account.getLanguage()), em).getDiskUsageForPartTemplatesInWorkspace(pWorkspaceId);
    }

    @Override
    public PartRevision[] getCheckedOutPartRevisions(String pWorkspaceId) throws WorkspaceNotFoundException, AccessRightException, AccountNotFoundException, UserNotFoundException, UserNotActiveException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
        List<PartRevision> partRevisions = new PartRevisionDAO(new Locale(user.getLanguage()), em).findCheckedOutPartRevisionsForUser(pWorkspaceId, user.getLogin());
        return partRevisions.toArray(new PartRevision[partRevisions.size()]);
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID,UserGroupMapping.ADMIN_ROLE_ID})
    @Override
    public PartRevision[] getAllCheckedOutPartRevisions(String pWorkspaceId) throws WorkspaceNotFoundException, AccessRightException, AccountNotFoundException {
        Account account = userManager.checkAdmin(pWorkspaceId);
        List<PartRevision> partRevisions = new PartRevisionDAO(new Locale(account.getLanguage()), em).findAllCheckedOutPartRevisions(pWorkspaceId);
        return partRevisions.toArray(new PartRevision[partRevisions.size()]);
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID})
    @Override
    public SharedPart createSharedPart(PartRevisionKey pPartRevisionKey, String pPassword, Date pExpireDate) throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException, PartRevisionNotFoundException, UserNotActiveException {
        User user = userManager.checkWorkspaceWriteAccess(pPartRevisionKey.getPartMaster().getWorkspace());
        SharedPart sharedPart = new SharedPart(user.getWorkspace(), user, pExpireDate, pPassword, getPartRevision(pPartRevisionKey));
        SharedEntityDAO sharedEntityDAO = new SharedEntityDAO(new Locale(user.getLanguage()),em);
        sharedEntityDAO.createSharedPart(sharedPart);
        return sharedPart;
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID})
    @Override
    public void deleteSharedPart(SharedEntityKey pSharedEntityKey) throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException, SharedEntityNotFoundException {
        User user = userManager.checkWorkspaceWriteAccess(pSharedEntityKey.getWorkspace());
        SharedEntityDAO sharedEntityDAO = new SharedEntityDAO(new Locale(user.getLanguage()),em);
        SharedPart sharedPart = sharedEntityDAO.loadSharedPart(pSharedEntityKey.getUuid());
        sharedEntityDAO.deleteSharedPart(sharedPart);
    }



    @RolesAllowed({UserGroupMapping.GUEST_PROXY_ROLE_ID,UserGroupMapping.REGULAR_USER_ROLE_ID,UserGroupMapping.ADMIN_ROLE_ID})
    @Override
    public boolean canAccess(PartRevisionKey partRKey) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, PartRevisionNotFoundException {
        PartRevision partRevision;
        if(ctx.isCallerInRole(UserGroupMapping.GUEST_PROXY_ROLE_ID)){
            partRevision = new PartRevisionDAO(em).loadPartR(partRKey);
            return partRevision.isPublicShared();
        }

        User user = userManager.checkWorkspaceReadAccess(partRKey.getPartMaster().getWorkspace());
        return  canUserAccess(user, partRKey);
    }
    @RolesAllowed({UserGroupMapping.GUEST_PROXY_ROLE_ID,UserGroupMapping.REGULAR_USER_ROLE_ID,UserGroupMapping.ADMIN_ROLE_ID})
    @Override
    public boolean canAccess(PartIterationKey partIKey) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, PartRevisionNotFoundException, PartIterationNotFoundException {
        PartRevision partRevision;
        if(ctx.isCallerInRole(UserGroupMapping.GUEST_PROXY_ROLE_ID)){
            partRevision = new PartRevisionDAO(em).loadPartR(partIKey.getPartRevision());
            return partRevision.isPublicShared() && partRevision.getLastCheckedInIteration().getIteration() >= partIKey.getIteration();
        }

        User user = userManager.checkWorkspaceReadAccess(partIKey.getWorkspaceId());
        return canUserAccess(user, partIKey);
    }
    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID,UserGroupMapping.ADMIN_ROLE_ID})
    @Override
    public boolean canUserAccess(User user, PartRevisionKey partRKey) throws PartRevisionNotFoundException {
        PartRevision partRevision = new PartRevisionDAO(new Locale(user.getLanguage()), em).loadPartR(partRKey);
        return hasPartRevisionReadAccess(user, partRevision);
    }
    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID,UserGroupMapping.ADMIN_ROLE_ID})
    @Override
    public boolean canUserAccess(User user, PartIterationKey partIKey) throws PartRevisionNotFoundException, PartIterationNotFoundException {
        PartRevisionDAO partRevisionDAO = new PartRevisionDAO(new Locale(user.getLanguage()), em);
        PartRevision partR = partRevisionDAO.loadPartR(partIKey.getPartRevision());
        return hasPartRevisionReadAccess(user, partR) &&
                (!partRevisionDAO.isCheckedOutIteration(partIKey) ||
                        user.equals(partR.getCheckOutUser()));
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID,UserGroupMapping.ADMIN_ROLE_ID})
    @Override
    public User checkPartRevisionReadAccess(PartRevisionKey partRevisionKey) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, PartRevisionNotFoundException, AccessRightException {
        User user = userManager.checkWorkspaceReadAccess(partRevisionKey.getPartMaster().getWorkspace());
        if(!canUserAccess(user, partRevisionKey)){
            throw new AccessRightException(new Locale(user.getLanguage()),user);
        }
        return user;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public Component filterProductStructure(ConfigurationItemKey ciKey, PSFilter filter, List<PartLink> path, Integer pDepth) throws ConfigurationItemNotFoundException, WorkspaceNotFoundException, NotAllowedException, UserNotFoundException, UserNotActiveException, PartUsageLinkNotFoundException, AccessRightException, PartMasterNotFoundException, EntityConstraintException {

        User user = userManager.checkWorkspaceReadAccess(ciKey.getWorkspace());
        Locale locale = new Locale(user.getLanguage());

        PartMaster root = null;

        if(path == null){
            ConfigurationItem ci = new ConfigurationItemDAO(locale, em).loadConfigurationItem(ciKey);
            root = ci.getDesignItem();
        }

        PSFilterVisitor visitor = new PSFilterVisitor(em, user, filter, root , path, pDepth) {
            @Override
            public void onIndeterminateVersion(PartMaster partMaster, List<PartIteration> partIterations)  throws NotAllowedException{
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

        return visitor.getComponent();

    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public List<PartLink> decodePath(ConfigurationItemKey ciKey, String path) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, PartUsageLinkNotFoundException, ConfigurationItemNotFoundException {

        User user = userManager.checkWorkspaceReadAccess(ciKey.getWorkspace());

        if(path == null){
            throw new IllegalArgumentException("Path cannot be null");
        }
        List<PartLink> decodedPath = new ArrayList<>();

        PartLink rootPartUsageLink = getRootPartUsageLink(ciKey);
        decodedPath.add(rootPartUsageLink);

        if(path.equals("-1")){
            return decodedPath;
        }

        // Remove the -1- in front of string
        String[] split = path.substring(3).split("-");

        for(String codeAndId:split){

            int id = Integer.valueOf(codeAndId.substring(1));

            if(codeAndId.startsWith("u")){
                decodedPath.add(getPartUsageLink(user,id));
            }else if(codeAndId.startsWith("s")) {
                decodedPath.add(getPartSubstituteLink(user,id));
            }else{
                throw new IllegalArgumentException("Missing code");
            }

        }

        return decodedPath;
    }



    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public List<PartRevision> searchPartRevisions(String workspaceId, Query query) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException {
        User user = userManager.checkWorkspaceReadAccess(workspaceId);
        QueryDAO queryDAO = new QueryDAO(new Locale(user.getLanguage()), em);

        WorkspaceDAO workspaceDAO = new WorkspaceDAO(new Locale(user.getLanguage()),em);
        Workspace workspace = workspaceDAO.loadWorkspace(workspaceId);

        List<PartRevision> parts = queryDAO.runQuery(workspace,query);

        ListIterator<PartRevision> ite = parts.listIterator();

        while (ite.hasNext()) {
            PartRevision partR = ite.next();

            if (isCheckoutByAnotherUser(user,partR)) {
                em.detach(partR);
                partR.removeLastIteration();
            }

            if (partR.getLastIteration()!=null && !hasPartRevisionReadAccess(user,partR)) {
                ite.remove();
            }
        }

        return parts;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public Query getQuery(String workspaceId, int queryId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException {

        User user = userManager.checkWorkspaceReadAccess(workspaceId);

        QueryDAO queryDAO = new QueryDAO(new Locale(user.getLanguage()),em);
        Query query = queryDAO.loadQuery(queryId);

        if(!query.getAuthor().getWorkspace().getId().equals(workspaceId)){
            userManager.checkWorkspaceReadAccess(query.getAuthor().getWorkspace().getId());
        }

        return query;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public void createQuery(String workspaceId, Query query) throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException, QueryAlreadyExistsException, CreationException {
        User user = userManager.checkWorkspaceWriteAccess(workspaceId);
        Locale locale = new Locale(user.getLanguage());
        QueryDAO queryDAO = new QueryDAO(locale,em);

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
    public void deleteQuery(String workspaceId, int queryId) throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException {
        User user = userManager.checkWorkspaceWriteAccess(workspaceId);
        Locale locale = new Locale(user.getLanguage());
        QueryDAO queryDAO = new QueryDAO(locale,em);
        Query query = queryDAO.loadQuery(queryId);

        Workspace workspace = query.getAuthor().getWorkspace();
        User userInQueryWorkspace = userManager.checkWorkspaceWriteAccess(workspace.getId());

        if(query.getAuthor().equals(userInQueryWorkspace) || userInQueryWorkspace.isAdministrator()){
            queryDAO.removeQuery(query);
        }else{
            throw new AccessRightException(locale,userInQueryWorkspace);
        }
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public List<Query> getQueries(String workspaceId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException {
        User user = userManager.checkWorkspaceReadAccess(workspaceId);
        QueryDAO queryDAO = new QueryDAO(new Locale(user.getLanguage()),em);
        return queryDAO.loadQueries(workspaceId);
    }

    private PartUsageLink getPartUsageLink(User user, int id) throws PartUsageLinkNotFoundException {
        PartUsageLinkDAO partUsageLinkDAO = new PartUsageLinkDAO(new Locale(user.getLanguage()),em);
        return partUsageLinkDAO.loadPartUsageLink(id);
    }

    private PartSubstituteLink getPartSubstituteLink(User user, int id) throws PartUsageLinkNotFoundException {
        PartUsageLinkDAO partUsageLinkDAO = new PartUsageLinkDAO(new Locale(user.getLanguage()),em);
        return partUsageLinkDAO.loadPartSubstituteLink(id);
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public PartLink getRootPartUsageLink(ConfigurationItemKey pKey) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ConfigurationItemNotFoundException {
        User user = userManager.checkWorkspaceReadAccess(pKey.getWorkspace());
        ConfigurationItem ci = new ConfigurationItemDAO(new Locale(user.getLanguage()), em).loadConfigurationItem(pKey);

        PartLink rootUsageLink = new PartLink() {
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

        return rootUsageLink;
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID,UserGroupMapping.ADMIN_ROLE_ID})
    @Override
    public void checkCyclicAssemblyForPartIteration(PartIteration partIteration) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, NotAllowedException, EntityConstraintException, PartMasterNotFoundException {

        PartMaster partMaster = partIteration.getPartRevision().getPartMaster();
        Workspace workspace = partMaster.getWorkspace();

        User user = userManager.checkWorkspaceReadAccess(workspace.getId());

        // Navigate the WIP
        new PSFilterVisitor(em, user, new UpdatePartIterationPSFilter(user,partIteration), partMaster, null, -1) {
            @Override
            public void onIndeterminateVersion(PartMaster partMaster, List<PartIteration> partIterations)  throws NotAllowedException{
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

    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public PSFilter getPSFilter(ConfigurationItemKey ciKey, String filterType) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ProductInstanceMasterNotFoundException, BaselineNotFoundException {

        User user = userManager.checkWorkspaceReadAccess(ciKey.getWorkspace());

        if(filterType==null){
            return new WIPPSFilter(user);
        }

        PSFilter filter;

        switch (filterType) {

            case "wip":
            case "undefined":
                filter = new WIPPSFilter(user);
                break;
            case "latest":
                filter = new LatestPSFilter(user);
                break;
            case "released":
                filter = new ReleasedPSFilter(user);
                break;
            case "latest-released":
                filter = new LatestReleasedPSFilter(user);
                break;
            default:
                if(filterType.startsWith("pi-")){
                    String serialNumber = filterType.substring(3);
                    filter = getConfigSpecForProductInstance(ciKey, serialNumber);
                }else{
                    filter = getConfigSpecForBaseline(Integer.parseInt(filterType));
                }
                break;
        }
        return filter;
    }


    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public List<InstanceAttributeDescriptor> getInstanceAttributesInWorkspace(String workspaceId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException {
        userManager.checkWorkspaceReadAccess(workspaceId);
        InstanceAttributeDAO instanceAttributeDAO = new InstanceAttributeDAO(em);
        List<InstanceAttributeDescriptor> instanceAttributesInWorkspace = instanceAttributeDAO.getInstanceAttributesInWorkspace(workspaceId);
        return instanceAttributesInWorkspace;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public List<QueryResultRow> filterProductBreakdownStructure(String workspaceId, Query query) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, BaselineNotFoundException, ProductInstanceMasterNotFoundException, ConfigurationItemNotFoundException, NotAllowedException, PartMasterNotFoundException, EntityConstraintException {
        List<QueryResultRow> rows = new ArrayList<>();
        for(QueryContext queryContext :query.getContexts()){
            rows.addAll(filterPBS(workspaceId, queryContext));
        }
        return rows;
    }

    @Override
    public Query loadQuery(String workspaceId,int queryId) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException {
        User user = userManager.checkWorkspaceReadAccess(workspaceId);
        QueryDAO queryDAO = new QueryDAO(new Locale(user.getLanguage()), em);
        return queryDAO.loadQuery(queryId);
    }

    private List<QueryResultRow> filterPBS(String workspaceId, QueryContext queryContext) throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, BaselineNotFoundException, ProductInstanceMasterNotFoundException, ConfigurationItemNotFoundException, NotAllowedException, EntityConstraintException, PartMasterNotFoundException {
        User user = userManager.checkWorkspaceReadAccess(workspaceId);

        String configurationItemId = queryContext.getConfigurationItemId();
        String serialNumber = queryContext.getSerialNumber();

        ConfigurationItemKey ciKey = new ConfigurationItemKey(workspaceId, configurationItemId);
        Locale locale = new Locale(user.getLanguage());

        List<QueryResultRow> rows = new ArrayList<>();

        PSFilter filter = serialNumber != null ?  getPSFilter(ciKey, "pi-"+serialNumber) : getPSFilter(ciKey, "latest");

        ConfigurationItem ci = new ConfigurationItemDAO(locale, em).loadConfigurationItem(ciKey);
        PartMaster root = ci.getDesignItem();

        new PSFilterVisitor(em, user, filter, root, null, -1) {
            @Override
            public void onIndeterminateVersion(PartMaster partMaster, List<PartIteration> partIterations)  throws NotAllowedException{
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
                QueryResultRow row = new QueryResultRow();
                double totalAmount = 1;
                for (PartLink pl : path){
                    if (pl.getUnit() == null) {
                        totalAmount *= pl.getAmount();
                    }
                }
                int depth = parts.size();
                PartMaster part = parts.get(parts.size()-1);
                List<PartIteration> partIterations = filter.filter(part);
                if(partIterations.size() != 0) {
                    PartRevision partRevision = partIterations.get(0).getPartRevision();
                    row.setPartRevision(partRevision);
                    row.setDepth(depth);
                    row.setContext(queryContext);
                    row.setAmount(totalAmount);
                    rows.add(row);
                }
            }

            @Override
            public void onUnresolvedVersion(PartMaster partMaster) {

            }
        };

        return rows;
    }
    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID})
    @Override
    public List<PartIteration> getInversePartsLink(DocumentIterationKey docKey) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, PartIterationNotFoundException, PartRevisionNotFoundException, DocumentIterationNotFoundException {
        User user = userManager.checkWorkspaceReadAccess(docKey.getWorkspaceId());

        Locale locale = new Locale(user.getLanguage());

        DocumentIteration documentIteration = new DocumentRevisionDAO(locale, em).loadDocI(docKey);

        DocumentLinkDAO documentLinkDAO = new DocumentLinkDAO(locale,em);
        List<PartIteration> iterations = documentLinkDAO.getInversePartsLinks(documentIteration);
        ListIterator<PartIteration> ite = iterations.listIterator();

        while(ite.hasNext()){
            PartIteration next = ite.next();
            if(!canAccess(next.getKey())){
                ite.remove();
            }
        }
        return iterations;
    }



    private PSFilter getConfigSpecForBaseline(int baselineId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, BaselineNotFoundException {
        return productBaselineManager.getBaselinePSFilter(baselineId);
    }

    private PSFilter getConfigSpecForProductInstance(ConfigurationItemKey ciKey, String serialNumber) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ProductInstanceMasterNotFoundException {
        User user = userManager.checkWorkspaceReadAccess(ciKey.getWorkspace());
        ProductInstanceMasterKey pimk = new ProductInstanceMasterKey(serialNumber,ciKey);
        ProductInstanceMaster productIM = new ProductInstanceMasterDAO(em).loadProductInstanceMaster(pimk);
        ProductInstanceIteration productII = productIM.getLastIteration();
        return new ProductInstanceConfigSpec(productII,user);
    }

    private User checkPartRevisionWriteAccess(PartRevisionKey partRevisionKey) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, PartRevisionNotFoundException, AccessRightException {
        String workspaceId = partRevisionKey.getPartMaster().getWorkspace();
        User user = userManager.checkWorkspaceReadAccess(workspaceId);
        if(user.isAdministrator()){                                                                                     // Check if it is the workspace's administrator
            return user;
        }
        PartRevision partRevision = new PartRevisionDAO(em).loadPartR(partRevisionKey);
        if(partRevision.getACL()==null){                                                                                // Check if the part haven't ACL
            return userManager.checkWorkspaceWriteAccess(workspaceId);
        }
        if(partRevision.getACL().hasWriteAccess(user)){                                                                 // Check if the ACL grant write access
            return user;
        }
        throw new AccessRightException(new Locale(user.getLanguage()),user);                                            // Else throw a AccessRightException
    }

    private User checkPartTemplateWriteAccess(PartMasterTemplate template, User user) throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException {

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
     * @param user A user which have read access to the workspace
     * @param partRevision The part revision wanted
     * @return True if access is granted, False otherwise
     */
    private boolean hasPartRevisionReadAccess(User user, PartRevision partRevision){
        return user.isAdministrator() || isACLGrantReadAccess(user,partRevision);
    }

    private boolean hasPartTemplateReadAccess(User user, PartMasterTemplate template){
        return user.isAdministrator() || isACLGrantReadAccess(user,template);
    }
    /**
     * Say if a user, which have access to the workspace, have write access to a part revision
     * @param user A user which have read access to the workspace
     * @param partRevision The part revision wanted
     * @return True if access is granted, False otherwise
     */
    private boolean hasPartRevisionWriteAccess(User user, PartRevision partRevision){
        return user.isAdministrator() || isACLGrantWriteAccess(user,partRevision);
    }

    private boolean isAuthor(User user, PartRevision partRevision){
        return partRevision.getAuthor().getLogin().equals(user.getLogin());
    }
    private boolean isACLGrantReadAccess(User user, PartRevision partRevision){
        return partRevision.getACL()==null || partRevision.getACL().hasReadAccess(user);
    }
    private boolean isACLGrantReadAccess(User user, PartMasterTemplate template){
        return template.getAcl()==null || template.getAcl().hasReadAccess(user);
    }
    private boolean isACLGrantWriteAccess(User user, PartRevision partRevision){
        return partRevision.getACL()==null || partRevision.getACL().hasWriteAccess(user);
    }
    private boolean isCheckoutByUser(User user, PartRevision partRevision){
        return partRevision.isCheckedOut() && partRevision.getCheckOutUser().equals(user);
    }
    private boolean isCheckoutByAnotherUser(User user, PartRevision partRevision){
        return partRevision.isCheckedOut() && !partRevision.getCheckOutUser().equals(user);
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
//TODO when using layers and markers, check for concordance
//TODO add a method to update a marker
//TODO use dozer