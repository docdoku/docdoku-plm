/*
 * DocDoku, Professional Open Source
 * Copyright 2006, 2007, 2008, 2009, 2010, 2011, 2012 DocDoku SARL
 *
 * This file is part of DocDoku.
 *
 * DocDoku is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DocDoku is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with DocDoku.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.docdoku.server;

import com.docdoku.core.common.BinaryResource;
import com.docdoku.core.common.User;
import com.docdoku.core.common.Workspace;
import com.docdoku.core.meta.InstanceAttribute;
import com.docdoku.core.product.CADInstance;
import com.docdoku.core.product.ConfigSpec;
import com.docdoku.core.product.ConfigurationItem;
import com.docdoku.core.product.ConfigurationItemKey;
import com.docdoku.core.product.Geometry;
import com.docdoku.core.product.LatestConfigSpec;
import com.docdoku.core.product.Layer;
import com.docdoku.core.product.Marker;
import com.docdoku.core.product.PartAlternateLink;
import com.docdoku.core.product.PartIteration;
import com.docdoku.core.product.PartIteration.Source;
import com.docdoku.core.product.PartIterationKey;
import com.docdoku.core.product.PartMaster;
import com.docdoku.core.product.PartMasterKey;
import com.docdoku.core.product.PartRevision;
import com.docdoku.core.product.PartRevisionKey;
import com.docdoku.core.product.PartSubstituteLink;
import com.docdoku.core.product.PartUsageLink;
import com.docdoku.core.services.*;
import com.docdoku.core.util.NamingConvention;
import com.docdoku.core.workflow.Task;
import com.docdoku.core.workflow.Workflow;
import com.docdoku.core.workflow.WorkflowModel;
import com.docdoku.core.workflow.WorkflowModelKey;
import com.docdoku.server.dao.BinaryResourceDAO;
import com.docdoku.server.dao.ConfigurationItemDAO;
import com.docdoku.server.dao.LayerDAO;
import com.docdoku.server.dao.MarkerDAO;
import com.docdoku.server.dao.PartIterationDAO;
import com.docdoku.server.dao.PartMasterDAO;
import com.docdoku.server.dao.PartRevisionDAO;
import com.docdoku.server.dao.PartUsageLinkDAO;
import com.docdoku.server.dao.WorkflowModelDAO;
import com.docdoku.server.dao.WorkspaceDAO;
import com.docdoku.server.vault.DataManager;
import com.docdoku.server.vault.filesystem.DataManagerImpl;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Local;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.annotation.security.DeclareRoles;
import javax.ejb.EJB;
import javax.jws.WebService;

@DeclareRoles("users")
@Local(IProductManagerLocal.class)
@Stateless(name = "ProductManagerBean")
@WebService(endpointInterface = "com.docdoku.core.services.IProductManagerWS")
public class ProductManagerBean implements IProductManagerWS, IProductManagerLocal {

    @PersistenceContext
    private EntityManager em;
    @Resource
    private SessionContext ctx;
    @Resource(name = "vaultPath")
    private String vaultPath;
    @EJB
    private IMailerLocal mailer;
    @EJB
    private IUserManagerLocal userManager;
    private final static Logger LOGGER = Logger.getLogger(ProductManagerBean.class.getName());
    private DataManager dataManager;

    @PostConstruct
    private void init() {
        dataManager = new DataManagerImpl(new File(vaultPath));
    }

    @RolesAllowed("users")
    @Override
    public List<PartUsageLink[]> findPartUsages(ConfigurationItemKey pKey, PartMasterKey pPartMKey) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException{
        User user = userManager.checkWorkspaceReadAccess(pKey.getWorkspace());
        PartUsageLinkDAO linkDAO = new PartUsageLinkDAO(new Locale(user.getLanguage()), em);
        List<PartUsageLink[]> usagePaths = linkDAO.findPartUsagePaths(pPartMKey);
        //TODO filter by configuration item
        return usagePaths;
    }
    
    
    @RolesAllowed("users")
    @Override
    public PartUsageLink filterProductStructure(ConfigurationItemKey pKey, ConfigSpec configSpec, Integer partUsageLink, Integer depth) throws ConfigurationItemNotFoundException, WorkspaceNotFoundException, NotAllowedException, UserNotFoundException, UserNotActiveException, PartUsageLinkNotFoundException {
        User user = userManager.checkWorkspaceReadAccess(pKey.getWorkspace());
        PartUsageLink rootUsageLink;


        if (partUsageLink == null || partUsageLink == -1) {
            ConfigurationItem ci = new ConfigurationItemDAO(new Locale(user.getLanguage()), em).loadConfigurationItem(pKey);
            rootUsageLink = new PartUsageLink();
            rootUsageLink.setId(-1);
            rootUsageLink.setAmount(1d);
            List<CADInstance> cads = new ArrayList<CADInstance>();
            cads.add(new CADInstance(0d,0d,0d,0d,0d,0d,CADInstance.Positioning.ABSOLUTE));
            rootUsageLink.setCadInstances(cads);
            rootUsageLink.setComponent(ci.getDesignItem());
        } else {
            rootUsageLink = new PartUsageLinkDAO(new Locale(user.getLanguage()), em).loadPartUsageLink(partUsageLink);
        }

        em.detach(rootUsageLink.getComponent());

        if (configSpec instanceof LatestConfigSpec) {
            if (depth == null) {
                filterLatestConfigSpec(rootUsageLink.getComponent(), -1);
            } else {
                filterLatestConfigSpec(rootUsageLink.getComponent(), depth);
            }
        }
        return rootUsageLink;
    }

    private PartMaster filterLatestConfigSpec(PartMaster root, int depth) {
        PartRevision partR = root.getLastRevision();
        PartIteration partI = null;

        if (partR != null) {
            partI = partR.getLastIteration();
        }

        if (root.getPartRevisions().size() > 1) {
            root.getPartRevisions().retainAll(Collections.singleton(partR));
        }
        if (partR != null && partR.getNumberOfIterations() > 1) {
            partR.getPartIterations().retainAll(Collections.singleton(partI));
        }

        if (partI != null) {
            if (depth != 0) {
                depth--;
                for (PartUsageLink usageLink : partI.getComponents()) {
                    filterLatestConfigSpec(usageLink.getComponent(), depth);

                    for (PartSubstituteLink subLink : usageLink.getSubstitutes()) {
                        filterLatestConfigSpec(subLink.getSubstitute(), 0);
                    }
                }
            }
        }

        for (PartAlternateLink alternateLink : root.getAlternates()) {
            filterLatestConfigSpec(alternateLink.getAlternate(), 0);
        }
        return root;
    }

    @RolesAllowed("users")
    @Override
    public ConfigurationItem createConfigurationItem(String pWorkspaceId, String pId, String pDescription, String pDesignItemNumber) throws UserNotFoundException, WorkspaceNotFoundException, AccessRightException, NotAllowedException, ConfigurationItemAlreadyExistsException, CreationException {

        User user = userManager.checkWorkspaceWriteAccess(pWorkspaceId);
        if (!NamingConvention.correct(pId)) {
            throw new NotAllowedException(new Locale(user.getLanguage()), "NotAllowedException9");
        }
        ConfigurationItem ci = new ConfigurationItem(user.getWorkspace(), pId, pDescription);
        ci.setDesignItem(em.getReference(PartMaster.class, new PartMasterKey(pWorkspaceId, pDesignItemNumber)));

        new ConfigurationItemDAO(new Locale(user.getLanguage()), em).createConfigurationItem(ci);
        return ci;
    }

    @RolesAllowed("users")
    @Override
    public PartMaster createPartMaster(String pWorkspaceId, String pNumber, String pName, String pPartMasterDescription, boolean pStandardPart, String pWorkflowModelId, String pPartRevisionDescription) throws NotAllowedException, UserNotFoundException, WorkspaceNotFoundException, AccessRightException, WorkflowModelNotFoundException, PartMasterAlreadyExistsException, CreationException {

        User user = userManager.checkWorkspaceWriteAccess(pWorkspaceId);
        if (!NamingConvention.correct(pNumber)) {
            throw new NotAllowedException(new Locale(user.getLanguage()), "NotAllowedException9");
        }

        PartMaster pm = new PartMaster(user.getWorkspace(), pNumber, user);
        pm.setName(pName);
        pm.setStandardPart(pStandardPart);
        pm.setDescription(pPartMasterDescription);
        Date now = new Date();
        pm.setCreationDate(now);
        PartRevision newRevision = pm.createNextRevision(user);

        if (pWorkflowModelId != null) {
            WorkflowModel workflowModel = new WorkflowModelDAO(new Locale(user.getLanguage()), em).loadWorkflowModel(new WorkflowModelKey(user.getWorkspaceId(), pWorkflowModelId));
            Workflow workflow = workflowModel.createWorkflow();
            newRevision.setWorkflow(workflow);

            Collection<Task> runningTasks = workflow.getRunningTasks();
            for (Task runningTask : runningTasks) {
                runningTask.start();
            }
            //TODO adapt to Part
            //mailer.sendApproval(runningTasks, newRevision);
        }
        newRevision.setCheckOutUser(user);
        newRevision.setCheckOutDate(now);
        newRevision.setCreationDate(now);
        newRevision.setDescription(pPartRevisionDescription);
        PartIteration ite = newRevision.createNextIteration(user);
        ite.setCreationDate(now);

        PartMasterDAO partMDAO = new PartMasterDAO(new Locale(user.getLanguage()), em);
        partMDAO.createPartM(pm);

        return pm;
    }

    @RolesAllowed("users")
    @Override
    public PartRevision undoCheckOutPart(PartRevisionKey pPartRPK) throws NotAllowedException, PartRevisionNotFoundException, UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException {
        User user = userManager.checkWorkspaceReadAccess(pPartRPK.getPartMaster().getWorkspace());
        PartRevisionDAO partRDAO = new PartRevisionDAO(new Locale(user.getLanguage()), em);
        PartRevision partR = partRDAO.loadPartR(pPartRPK);
        if (partR.isCheckedOut() && partR.getCheckOutUser().equals(user)) {
            PartIteration partIte = partR.removeLastIteration();
            for (Geometry file : partIte.getGeometries()) {
                dataManager.delData(file);
            }

            for (BinaryResource file : partIte.getAttachedFiles()) {
                dataManager.delData(file);
            }

            PartIterationDAO partIDAO = new PartIterationDAO(em);
            partIDAO.removeIteration(partIte);
            partR.setCheckOutDate(null);
            partR.setCheckOutUser(null);
            return partR;
        } else {
            throw new NotAllowedException(new Locale(user.getLanguage()), "NotAllowedException19");
        }
    }

    @RolesAllowed("users")
    @Override
    public PartRevision checkInPart(PartRevisionKey pPartRPK) throws PartRevisionNotFoundException, UserNotFoundException, WorkspaceNotFoundException, AccessRightException, NotAllowedException {
        User user = userManager.checkWorkspaceWriteAccess(pPartRPK.getPartMaster().getWorkspace());
        PartRevisionDAO partRDAO = new PartRevisionDAO(new Locale(user.getLanguage()), em);
        PartRevision partR = partRDAO.loadPartR(pPartRPK);
        //Check access rights on docM
        Workspace wks = new WorkspaceDAO(new Locale(user.getLanguage()), em).loadWorkspace(pPartRPK.getPartMaster().getWorkspace());
        boolean isAdmin = wks.getAdmin().getLogin().equals(user.getLogin());
        if (!isAdmin) {
            throw new AccessRightException(new Locale(user.getLanguage()), user);
        }
        if (partR.isCheckedOut() && partR.getCheckOutUser().equals(user)) {
            partR.setCheckOutDate(null);
            partR.setCheckOutUser(null);

            return partR;
        } else {
            throw new NotAllowedException(new Locale(user.getLanguage()), "NotAllowedException20");
        }
    }

    @RolesAllowed("users")
    @Override
    public File getDataFile(String pFullName) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, FileNotFoundException, NotAllowedException {
        User user = userManager.checkWorkspaceReadAccess(BinaryResource.parseWorkspaceId(pFullName));
        Locale userLocale = new Locale(user.getLanguage());
        BinaryResourceDAO binDAO = new BinaryResourceDAO(userLocale, em);
        BinaryResource file = binDAO.loadBinaryResource(pFullName);

        PartIteration partIte = binDAO.getPartOwner(file);
        if (partIte != null) {
            PartRevision partR = partIte.getPartRevision();

            if ((partR.isCheckedOut() && !partR.getCheckOutUser().equals(user) && partR.getLastIteration().equals(partIte))) {
                throw new NotAllowedException(new Locale(user.getLanguage()), "NotAllowedException34");
            } else {
                return dataManager.getDataFile(file);
            }
        } else {
            throw new FileNotFoundException(userLocale, pFullName);
        }
    }

    @RolesAllowed("users")
    @Override
    public File saveGeometryInPartIteration(PartIterationKey pPartIPK, String pName, int quality, long pSize) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, NotAllowedException, PartRevisionNotFoundException, FileAlreadyExistsException, CreationException {
        User user = userManager.checkWorkspaceReadAccess(pPartIPK.getWorkspaceId());
        if (!NamingConvention.correct(pName)) {
            throw new NotAllowedException(Locale.getDefault(), "NotAllowedException9");
        }

        PartRevisionDAO partRDAO = new PartRevisionDAO(em);
        PartRevision partR = partRDAO.loadPartR(pPartIPK.getPartRevision());
        PartIteration partI = partR.getIteration(pPartIPK.getIteration());
        if (partR.isCheckedOut() && partR.getCheckOutUser().equals(user) && partR.getLastIteration().equals(partI)) {
            Geometry file = null;
            String fullName = partR.getWorkspaceId() + "/parts/" + partR.getPartNumber() + "/" + partR.getVersion() + "/" + partI.getIteration() + "/" + pName;

            for (Geometry geo : partI.getGeometries()) {
                if (geo.getFullName().equals(fullName)) {
                    file = geo;
                    break;
                }
            }
            if (file == null) {
                file = new Geometry(quality, fullName, pSize);
                new BinaryResourceDAO(em).createBinaryResource(file);
                partI.addGeometry(file);
            } else {
                file.setContentLength(pSize);
                file.setQuality(quality);
            }
            return dataManager.getVaultFile(file);
        } else {
            throw new NotAllowedException(Locale.getDefault(), "NotAllowedException4");
        }
    }

    @RolesAllowed("users")
    @Override
    public File saveFileInPartIteration(PartIterationKey pPartIPK, String pName, long pSize) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, NotAllowedException, PartRevisionNotFoundException, FileAlreadyExistsException, CreationException {
        User user = userManager.checkWorkspaceReadAccess(pPartIPK.getWorkspaceId());
        if (!NamingConvention.correct(pName)) {
            throw new NotAllowedException(Locale.getDefault(), "NotAllowedException9");
        }

        PartRevisionDAO partRDAO = new PartRevisionDAO(em);
        PartRevision partR = partRDAO.loadPartR(pPartIPK.getPartRevision());
        PartIteration partI = partR.getIteration(pPartIPK.getIteration());
        if (partR.isCheckedOut() && partR.getCheckOutUser().equals(user) && partR.getLastIteration().equals(partI)) {
            BinaryResource file = null;
            String fullName = partR.getWorkspaceId() + "/parts/" + partR.getPartNumber() + "/" + partR.getVersion() + "/" + partI.getIteration() + "/" + pName;

            for (BinaryResource bin : partI.getAttachedFiles()) {
                if (bin.getFullName().equals(fullName)) {
                    file = bin;
                    break;
                }
            }
            if (file == null) {
                file = new BinaryResource(fullName, pSize);
                new BinaryResourceDAO(em).createBinaryResource(file);
                partI.addFile(file);
            } else {
                file.setContentLength(pSize);
            }
            return dataManager.getVaultFile(file);
        } else {
            throw new NotAllowedException(Locale.getDefault(), "NotAllowedException4");
        }
    }

    @RolesAllowed("users")
    @Override
    public List<ConfigurationItem> getConfigurationItems(String pWorkspaceId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
        return new ConfigurationItemDAO(new Locale(user.getLanguage()), em).findAllConfigurationItems(pWorkspaceId);
    }

    @RolesAllowed("users")
    @Override
    public PartRevision updatePartIteration(PartIterationKey pKey, String pIterationNote, Source source, List<PartUsageLink> pUsageLinks, List<InstanceAttribute> pAttributes) throws UserNotFoundException, WorkspaceNotFoundException, AccessRightException, NotAllowedException, PartRevisionNotFoundException {
        User user = userManager.checkWorkspaceWriteAccess(pKey.getWorkspaceId());
        PartRevisionDAO partRDAO = new PartRevisionDAO(new Locale(user.getLanguage()), em);
        PartRevision partRev = partRDAO.loadPartR(pKey.getPartRevision());
        PartIteration partIte = partRev.getLastIteration();
        //check access rights on partM ?
        if (partRev.isCheckedOut() && partRev.getCheckOutUser().equals(user) && partIte.getKey().equals(pKey)) {
            partIte.setComponents(pUsageLinks);
            // set doc for all attributes
            Map<String, InstanceAttribute> attrs = new HashMap<String, InstanceAttribute>();
            for (InstanceAttribute attr : pAttributes) {
                attrs.put(attr.getName(), attr);
            }

            Set<InstanceAttribute> currentAttrs = new HashSet<InstanceAttribute>(partIte.getInstanceAttributes().values());
            for (InstanceAttribute attr : currentAttrs) {
                if (!attrs.containsKey(attr.getName())) {
                    partIte.getInstanceAttributes().remove(attr.getName());
                }
            }

            for (InstanceAttribute attr : attrs.values()) {
                if (!partIte.getInstanceAttributes().containsKey(attr.getName())) {
                    partIte.getInstanceAttributes().put(attr.getName(), attr);
                } else {
                    partIte.getInstanceAttributes().get(attr.getName()).setValue(attr.getValue());
                }
            }

            partIte.setIterationNote(pIterationNote);
            partIte.setSource(source);
            return partRev;

        } else {
            throw new NotAllowedException(new Locale(user.getLanguage()), "NotAllowedException25");
        }

    }

    @RolesAllowed("users")
    @Override
    public List<Layer> getLayers(ConfigurationItemKey pKey) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException {
        User user = userManager.checkWorkspaceReadAccess(pKey.getWorkspace());
        return new LayerDAO(new Locale(user.getLanguage()), em).findAllLayers(pKey);
    }

    @RolesAllowed("users")
    @Override
    public Layer getLayer(int pId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, LayerNotFoundException {
        Layer layer = new LayerDAO(em).loadLayer(pId);
        User user = userManager.checkWorkspaceReadAccess(layer.getConfigurationItem().getWorkspaceId());
        return layer;
    }

    @RolesAllowed("users")
    @Override
    public Layer createLayer(ConfigurationItemKey pKey, String pName) throws UserNotFoundException, WorkspaceNotFoundException, AccessRightException, ConfigurationItemNotFoundException {
        User user = userManager.checkWorkspaceWriteAccess(pKey.getWorkspace());
        ConfigurationItem ci = new ConfigurationItemDAO(new Locale(user.getLanguage()), em).loadConfigurationItem(pKey);
        Layer layer = new Layer(pName, user, ci);
        Date now = new Date();
        layer.setCreationDate(now);

        new LayerDAO(new Locale(user.getLanguage()), em).createLayer(layer);
        return layer;
    }

    @Override
    public Layer updateLayer(ConfigurationItemKey pKey, int pId, String pName) throws UserNotFoundException, WorkspaceNotFoundException, AccessRightException, ConfigurationItemNotFoundException, LayerNotFoundException, UserNotActiveException {
        User user = userManager.checkWorkspaceWriteAccess(pKey.getWorkspace());
        Layer layer = getLayer(pId);
        layer.setName(pName);
        return layer;
    }

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
}
//TODO when using layers and markers, check for concordance
//TODO add a method to update a marker
//TODO use dozer