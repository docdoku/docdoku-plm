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

import com.docdoku.core.common.*;
import com.docdoku.core.configuration.ProductBaseline;
import com.docdoku.core.document.DocumentIteration;
import com.docdoku.core.document.DocumentIterationKey;
import com.docdoku.core.document.DocumentLink;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.meta.InstanceAttribute;
import com.docdoku.core.meta.InstanceAttributeTemplate;
import com.docdoku.core.product.*;
import com.docdoku.core.product.PartIteration.Source;
import com.docdoku.core.query.PartSearchQuery;
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
import com.docdoku.server.dao.*;
import com.docdoku.server.esindexer.ESIndexer;
import com.docdoku.server.esindexer.ESSearcher;

import javax.annotation.Resource;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.ejb.*;
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
    private ESIndexer esIndexer;
    @EJB
    private ESSearcher esSearcher;

    private static final Logger LOGGER = Logger.getLogger(ProductManagerBean.class.getName());

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public List<PartUsageLink[]> findPartUsages(ConfigurationItemKey pKey, PartMasterKey pPartMKey) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException {
        User user = userManager.checkWorkspaceReadAccess(pKey.getWorkspace());
        PartUsageLinkDAO linkDAO = new PartUsageLinkDAO(new Locale(user.getLanguage()), em);
        List<PartUsageLink[]> usagePaths = linkDAO.findPartUsagePaths(pPartMKey);
        //TODO filter by configuration item
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

        ConfigurationItem ci = new ConfigurationItem(user.getWorkspace(), pId, pDescription);

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

            Map<String, InstanceAttribute> attrs = new HashMap<>();
            for (InstanceAttributeTemplate attrTemplate : partMasterTemplate.getAttributeTemplates()) {
                InstanceAttribute attr = attrTemplate.createInstanceAttribute();
                attrs.put(attr.getName(), attr);
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

        if (partR.isReleased()) {
            throw new NotAllowedException(locale, "NotAllowedException11");
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
            Map<String, InstanceAttribute> attrs = new HashMap<>();
            for (InstanceAttribute attr : beforeLastPartIteration.getInstanceAttributes().values()) {
                InstanceAttribute newAttr = attr.clone();
                //Workaround for the NULL DTYPE bug
                attrDAO.createAttribute(newAttr);
                attrs.put(newAttr.getName(), newAttr);
            }
            newPartIteration.setInstanceAttributes(attrs);
        }

        return partR;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public PartRevision checkInPart(PartRevisionKey pPartRPK) throws PartRevisionNotFoundException, UserNotFoundException, WorkspaceNotFoundException, AccessRightException, NotAllowedException, ESServerException {
        User user = userManager.checkWorkspaceWriteAccess(pPartRPK.getPartMaster().getWorkspace());
        Locale locale = new Locale(user.getLanguage());

        PartRevisionDAO partRDAO = new PartRevisionDAO(locale, em);
        PartRevision partR = partRDAO.loadPartR(pPartRPK);

        //Check access rights on partR
        if (!hasPartRevisionWriteAccess(user, partR)) {
            throw new AccessRightException(locale, user);
        }

        if (isCheckoutByUser(user,partR)) {
            partR.setCheckOutDate(null);
            partR.setCheckOutUser(null);

            for(PartIteration partIteration : partR.getPartIterations()){
                esIndexer.index(partIteration);                                                                         // Index all iterations in ElasticSearch (decrease old iteration boost factor)
            }
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
                geometryBinaryResource.setBox(box[0],box[1],box[2],box[3],box[4],box[5]);
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

    /*
    * give pAttributes null for no modification, give an empty list for removing them
    * */
    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public PartRevision updatePartIteration(PartIterationKey pKey, String pIterationNote, Source source, List<PartUsageLink> pUsageLinks, List<InstanceAttribute> pAttributes, DocumentIterationKey[] pLinkKeys)
            throws UserNotFoundException, WorkspaceNotFoundException, AccessRightException, NotAllowedException, PartRevisionNotFoundException, PartMasterNotFoundException {

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
                Set<DocumentIterationKey> linkKeys = new HashSet<>(Arrays.asList(pLinkKeys));
                Set<DocumentIterationKey> currentLinkKeys = new HashSet<>();

                Set<DocumentLink> currentLinks = new HashSet<>(partIte.getLinkedDocuments());

                for (DocumentLink link : currentLinks) {
                    DocumentIterationKey linkKey = link.getTargetDocumentKey();
                    if (!linkKeys.contains(linkKey)) {
                        partIte.getLinkedDocuments().remove(link);
                    } else {
                        currentLinkKeys.add(linkKey);
                    }
                }

                for (DocumentIterationKey link : linkKeys) {
                    if (!currentLinkKeys.contains(link)) {
                        DocumentLink newLink = new DocumentLink(em.getReference(DocumentIteration.class, link));
                        linkDAO.createLink(newLink);
                        partIte.getLinkedDocuments().add(newLink);
                    }
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
            }
            if (pAttributes != null) {
                // set doc for all attributes
                Map<String, InstanceAttribute> attrs = new HashMap<>();
                for (InstanceAttribute attr : pAttributes) {
                    attrs.put(attr.getName(), attr);
                }

                Set<InstanceAttribute> currentAttrs = new HashSet<>(partIte.getInstanceAttributes().values());
                if (partRev.getPartMaster().isAttributesLocked()){
                    //Check attributs haven't changed
                    if (currentAttrs.size() != attrs.size()){
                        throw new NotAllowedException(locale, "NotAllowedException45");
                    } else {
                        for (InstanceAttribute attr:currentAttrs){
                            InstanceAttribute newVersion = attrs.get(attr.getName());
                            if (newVersion == null
                                    || newVersion.getClass().equals(attr.getClass()) == false){
                                //Attribut has been swapped with a new attributs or his type has changed
                                throw new NotAllowedException(locale, "NotAllowedException45");
                            }
                        }
                    }
                }

                for (InstanceAttribute attr : currentAttrs) {
                    if (!attrs.containsKey(attr.getName())) {
                        partIte.getInstanceAttributes().remove(attr.getName());
                    }
                }

                for (InstanceAttribute attr : attrs.values()) {
                    if(!partIte.getInstanceAttributes().containsKey(attr.getName())){
                        partIte.getInstanceAttributes().put(attr.getName(), attr);
                    }else if(partIte.getInstanceAttributes().get(attr.getName()).getClass() != attr.getClass()){
                        partIte.getInstanceAttributes().remove(attr.getName());
                        partIte.getInstanceAttributes().put(attr.getName(), attr);
                    }else{
                        partIte.getInstanceAttributes().get(attr.getName()).setValue(attr.getValue());
                    }
                }
            }

            partIte.setIterationNote(pIterationNote);
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

        PartRevisionDAO partRevisionDAO = new PartRevisionDAO(locale,em);
        PartRevision partRevision = partRevisionDAO.loadPartR(revisionKey);

        if (isAuthor(user,partRevision) || user.isAdministrator()) {
            if (partRevision.getACL() == null) {
                ACL acl = new ACL();

                if (pACLUserEntries != null) {
                    for (Map.Entry<String, String> entry : pACLUserEntries.entrySet()) {
                        acl.addEntry(em.getReference(User.class,new UserKey(workspaceId,entry.getKey())),ACL.Permission.valueOf(entry.getValue()));
                    }
                }

                if (pACLUserGroupEntries != null) {
                    for (Map.Entry<String, String> entry : pACLUserGroupEntries.entrySet()) {
                        acl.addEntry(em.getReference(UserGroup.class,new UserGroupKey(workspaceId,entry.getKey())),ACL.Permission.valueOf(entry.getValue()));
                    }
                }

                new ACLDAO(em).createACL(acl);
                partRevision.setACL(acl);

            }else{
                if (pACLUserEntries != null) {
                    for (ACLUserEntry entry : partRevision.getACL().getUserEntries().values()) {
                        ACL.Permission newPermission = ACL.Permission.valueOf(pACLUserEntries.get(entry.getPrincipalLogin()));
                        if(newPermission != null){
                            entry.setPermission(newPermission);
                        }
                    }
                }

                if (pACLUserGroupEntries != null) {
                    for (ACLUserGroupEntry entry : partRevision.getACL().getGroupEntries().values()) {
                        ACL.Permission newPermission = ACL.Permission.valueOf(pACLUserGroupEntries.get(entry.getPrincipalId()));
                        if(newPermission != null){
                            entry.setPermission(newPermission);
                        }
                    }
                }
            }

        }else {
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
    public PartRevision[] getPartRevisionsWithReference(String pWorkspaceId, String reference, int maxResults) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
        List<PartRevision> partRs = new PartRevisionDAO(new Locale(user.getLanguage()), em).findPartsRevisionsWithReferenceLike(pWorkspaceId, reference, maxResults);
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
            throw new NotAllowedException(locale, "NotAllowedException40");
        }

        if (partRevision.getNumberOfIterations() == 0) {
            throw new NotAllowedException(locale, "NotAllowedException41");
        }

        partRevision.release();
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

    @Override
    public void deleteConfigurationItem(ConfigurationItemKey configurationItemKey) throws UserNotFoundException, WorkspaceNotFoundException, AccessRightException, NotAllowedException, UserNotActiveException, ConfigurationItemNotFoundException, LayerNotFoundException, EntityConstraintException {
        User user = userManager.checkWorkspaceReadAccess(configurationItemKey.getWorkspace());
        Locale locale = new Locale(user.getLanguage());
        ProductBaselineDAO productBaselineDAO = new ProductBaselineDAO(locale,em);
        List<ProductBaseline> productBaselines = productBaselineDAO.findBaselines(configurationItemKey.getId(), configurationItemKey.getWorkspace());
        if(!productBaselines.isEmpty() ){
            throw new EntityConstraintException(locale,"EntityConstraintException4");
        }
        new ConfigurationItemDAO(locale,em).removeConfigurationItem(configurationItemKey);
    }

    @Override
    public void deleteLayer(String workspaceId, int layerId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, LayerNotFoundException, AccessRightException {
        Layer layer = new LayerDAO(em).loadLayer(layerId);
        User user = userManager.checkWorkspaceWriteAccess(layer.getConfigurationItem().getWorkspaceId());
        new LayerDAO(new Locale(user.getLanguage()),em).deleteLayer(layer);
    }

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
        return new PartMasterTemplateDAO(new Locale(user.getLanguage()), em).findAllPartMTemplates(pWorkspaceId);
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public PartMasterTemplate getPartMasterTemplate(PartMasterTemplateKey pKey) throws WorkspaceNotFoundException, PartMasterTemplateNotFoundException, UserNotFoundException, UserNotActiveException {
        User user = userManager.checkWorkspaceReadAccess(pKey.getWorkspaceId());
        return new PartMasterTemplateDAO(new Locale(user.getLanguage()), em).loadPartMTemplate(pKey);
    }


    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public PartMasterTemplate createPartMasterTemplate(String pWorkspaceId, String pId, String pPartType, String pMask, InstanceAttributeTemplate[] pAttributeTemplates, boolean idGenerated, boolean attributesLocked) throws WorkspaceNotFoundException, AccessRightException, PartMasterTemplateAlreadyExistsException, UserNotFoundException, NotAllowedException, CreationException {
        User user = userManager.checkWorkspaceWriteAccess(pWorkspaceId);
        Locale locale = new Locale(user.getLanguage());
        checkNameValidity(pId,locale);

        PartMasterTemplate template = new PartMasterTemplate(user.getWorkspace(), pId, user, pPartType, pMask, attributesLocked);
        Date now = new Date();
        template.setCreationDate(now);
        template.setIdGenerated(idGenerated);

        Set<InstanceAttributeTemplate> attrs = new HashSet<>();
        Collections.addAll(attrs, pAttributeTemplates);
        template.setAttributeTemplates(attrs);

        new PartMasterTemplateDAO(locale, em).createPartMTemplate(template);
        return template;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public PartMasterTemplate updatePartMasterTemplate(PartMasterTemplateKey pKey, String pPartType, String pMask, InstanceAttributeTemplate[] pAttributeTemplates, boolean idGenerated, boolean attributesLocked) throws WorkspaceNotFoundException, AccessRightException, PartMasterTemplateNotFoundException, UserNotFoundException {
        User user = userManager.checkWorkspaceWriteAccess(pKey.getWorkspaceId());

        PartMasterTemplateDAO templateDAO = new PartMasterTemplateDAO(new Locale(user.getLanguage()), em);
        PartMasterTemplate template = templateDAO.loadPartMTemplate(pKey);
        Date now = new Date();
        template.setCreationDate(now);
        template.setAuthor(user);
        template.setPartType(pPartType);
        template.setMask(pMask);
        template.setIdGenerated(idGenerated);
        template.setAttributesLocked(attributesLocked);

        Set<InstanceAttributeTemplate> attrs = new HashSet<>();
        Collections.addAll(attrs, pAttributeTemplates);

        Set<InstanceAttributeTemplate> attrsToRemove = new HashSet<>(template.getAttributeTemplates());
        attrsToRemove.removeAll(attrs);

        InstanceAttributeTemplateDAO attrDAO = new InstanceAttributeTemplateDAO(em);
        for (InstanceAttributeTemplate attrToRemove : attrsToRemove) {
            attrDAO.removeAttribute(attrToRemove);
        }

        template.setAttributeTemplates(attrs);
        return template;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public void deletePartMasterTemplate(PartMasterTemplateKey pKey) throws WorkspaceNotFoundException, AccessRightException, PartMasterTemplateNotFoundException, UserNotFoundException {
        User user = userManager.checkWorkspaceWriteAccess(pKey.getWorkspaceId());
        PartMasterTemplateDAO templateDAO = new PartMasterTemplateDAO(new Locale(user.getLanguage()), em);
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
    public BinaryResource saveFileInTemplate(PartMasterTemplateKey pPartMTemplateKey, String pName, long pSize) throws WorkspaceNotFoundException, NotAllowedException, PartMasterTemplateNotFoundException, FileAlreadyExistsException, UserNotFoundException, UserNotActiveException, CreationException {
        User user = userManager.checkWorkspaceReadAccess(pPartMTemplateKey.getWorkspaceId());
        //TODO checkWorkspaceWriteAccess ?
        Locale locale = new Locale(user.getLanguage());
        checkNameFileValidity(pName,locale);

        PartMasterTemplateDAO templateDAO = new PartMasterTemplateDAO(locale,em);
        PartMasterTemplate template = templateDAO.loadPartMTemplate(pPartMTemplateKey);
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
        //TODO checkWorkspaceWriteAccess ?
        BinaryResourceDAO binDAO = new BinaryResourceDAO(new Locale(user.getLanguage()), em);
        BinaryResource file = binDAO.loadBinaryResource(pFullName);

        PartMasterTemplate template = binDAO.getPartTemplateOwner(file);
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
        for (PartRevision partRevision : partMaster.getPartRevisions()) {
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

            Map<String, InstanceAttribute> attrs = new HashMap<>();
            for (InstanceAttribute attr : lastPartI.getInstanceAttributes().values()) {
                InstanceAttribute clonedAttribute = attr.clone();
                attrs.put(clonedAttribute.getName(), clonedAttribute);
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
        return  canUserAccess(user,partRKey);
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
        return canUserAccess(user,partIKey);
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
                (!partRevisionDAO.isCheckoutedIteration(partIKey) ||
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

    /**
     * Say if a user, which have access to the workspace, have read access to a part revision
     * @param user A user which have read access to the workspace
     * @param partRevision The part revision wanted
     * @return True if access is granted, False otherwise
     */
    private boolean hasPartRevisionReadAccess(User user, PartRevision partRevision){
        return user.isAdministrator() || isACLGrantReadAccess(user,partRevision);
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