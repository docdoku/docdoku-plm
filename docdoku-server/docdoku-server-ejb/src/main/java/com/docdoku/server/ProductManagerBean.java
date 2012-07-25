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

import com.docdoku.core.common.User;
import com.docdoku.core.product.ConfigSpec;
import com.docdoku.core.product.ConfigurationItem;
import com.docdoku.core.product.ConfigurationItemKey;
import com.docdoku.core.product.LatestConfigSpec;
import com.docdoku.core.product.PartIteration;
import com.docdoku.core.product.PartMaster;
import com.docdoku.core.product.PartMasterKey;
import com.docdoku.core.product.PartRevision;
import com.docdoku.core.product.PartUsageLink;
import com.docdoku.core.services.AccessRightException;
import com.docdoku.core.services.ConfigurationItemAlreadyExistsException;
import com.docdoku.core.services.ConfigurationItemNotFoundException;
import com.docdoku.core.services.CreationException;
import com.docdoku.core.services.IMailerLocal;
import com.docdoku.core.services.IProductManagerLocal;
import com.docdoku.core.services.IUserManagerLocal;
import com.docdoku.core.services.NotAllowedException;
import com.docdoku.core.services.PartMasterAlreadyExistsException;
import com.docdoku.core.services.UserNotActiveException;
import com.docdoku.core.services.UserNotFoundException;
import com.docdoku.core.services.WorkflowModelNotFoundException;
import com.docdoku.core.services.WorkspaceNotFoundException;
import com.docdoku.core.util.NamingConvention;
import com.docdoku.core.workflow.Task;
import com.docdoku.core.workflow.Workflow;
import com.docdoku.core.workflow.WorkflowModel;
import com.docdoku.core.workflow.WorkflowModelKey;
import com.docdoku.server.dao.ConfigurationItemDAO;
import com.docdoku.server.dao.PartMasterDAO;
import com.docdoku.server.dao.WorkflowModelDAO;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Local;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.annotation.security.DeclareRoles;
import javax.ejb.EJB;

@DeclareRoles("users")
@Local(IProductManagerLocal.class)
@Stateless(name = "ProductManagerBean")
public class ProductManagerBean implements IProductManagerLocal {

    @PersistenceContext
    private EntityManager em;
    @Resource
    private SessionContext ctx;
    @EJB
    private IMailerLocal mailer;
    @EJB
    private IUserManagerLocal userManager;
    private final static Logger LOGGER = Logger.getLogger(ProductManagerBean.class.getName());

    @RolesAllowed("users")
    @Override
    public PartMaster filterProductStructure(ConfigurationItemKey pKey, ConfigSpec configSpec) throws ConfigurationItemNotFoundException, WorkspaceNotFoundException, NotAllowedException, UserNotFoundException, UserNotActiveException {
        User user = userManager.checkWorkspaceReadAccess(pKey.getWorkspace());
        ConfigurationItem ci = new ConfigurationItemDAO(new Locale(user.getLanguage()), em).loadConfigurationItem(pKey);
        PartMaster root = ci.getDesignItem();
        em.detach(root);

        if (configSpec instanceof LatestConfigSpec) {
            //TODO retain only latest iteration
        }
        return root;
    }

    @RolesAllowed("users")
    @Override
    public ConfigurationItem createConfigurationItem(String pWorkspaceId, String pId, String pDescription, String pDesignItemNumber) throws UserNotFoundException, WorkspaceNotFoundException, AccessRightException, NotAllowedException, ConfigurationItemAlreadyExistsException, CreationException  {
    
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
    public PartMaster createPasterMaster(String pWorkspaceId, String pNumber, String pName, String pPartMasterDescription, boolean pStandardPart, String pWorkflowModelId, String pPartRevisionDescription) throws NotAllowedException, UserNotFoundException, WorkspaceNotFoundException, AccessRightException, WorkflowModelNotFoundException, PartMasterAlreadyExistsException, CreationException {
        
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
        PartRevision newRevision=pm.createNextRevision(user);

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
        PartIteration ite=newRevision.createNextIteration(user);
        ite.setCreationDate(now);
        
        PartMasterDAO partMDAO = new PartMasterDAO(new Locale(user.getLanguage()), em);
        partMDAO.createPartM(pm);
        
        return pm;
    }
    
    
    
//    
//    @RolesAllowed("users")
//    @Override
//    public DocumentMaster updatePartIteration(PartIterationKey pKey, String pRevisionNote, Source source, List<PartUsageLink> pUsageLinks, List<InstanceAttribute> pAttributes) {
//        User user = userManager.checkWorkspaceWriteAccess(pKey.getWorkspaceId());
//        DocumentMasterDAO docMDAO = new DocumentMasterDAO(new Locale(user.getLanguage()), em);
//        DocumentMaster docM = docMDAO.loadDocM(new DocumentMasterKey(pKey.getWorkspaceId(), pKey.getDocumentMasterId(), pKey.getDocumentMasterVersion()));
//        //check access rights on docM ?
//        if (docM.isCheckedOut() && docM.getCheckOutUser().equals(user) && docM.getLastIteration().getKey().equals(pKey)) {
//            DocumentIteration doc = docM.getLastIteration();
//            
//            Set<DocumentToDocumentLink> links = new HashSet<DocumentToDocumentLink>();
//            for (DocumentIterationKey key : pLinkKeys) {
//                links.add(new DocumentToDocumentLink(doc, key));
//            }
//            Set<DocumentToDocumentLink> linksToRemove = new HashSet<DocumentToDocumentLink>(doc.getLinkedDocuments());
//            linksToRemove.removeAll(links);
//
//            DocumentToDocumentLinkDAO linkDAO = new DocumentToDocumentLinkDAO(em);
//            for (DocumentToDocumentLink linkToRemove : linksToRemove) {
//                linkDAO.removeLink(linkToRemove);
//            }
//
//            // set doc for all attributes
//            
//            Map<String, InstanceAttribute> attrs = new HashMap<String, InstanceAttribute>();
//            for (InstanceAttribute attr : pAttributes) {
//                //attr.setDocument(doc);
//                attrs.put(attr.getName(), attr);
//            }
//
//            Set<InstanceAttribute> currentAttrs = new HashSet<InstanceAttribute>(doc.getInstanceAttributes().values());
//            //attrsToRemove.removeAll(attrs.values());
//
//            for(InstanceAttribute attr:currentAttrs){
//                if(!attrs.containsKey(attr.getName())){
//                    doc.getInstanceAttributes().remove(attr.getName());
//                }
//            }
//
//            
//            //InstanceAttributeDAO attrDAO = new InstanceAttributeDAO(em);
//            /*
//            for (InstanceAttribute attrToRemove : attrsToRemove) {
//                attrDAO.removeAttribute(attrToRemove);
//            }
//            */
//
//            for(InstanceAttribute attr:attrs.values()){
//                if(!doc.getInstanceAttributes().containsKey(attr.getName())){
//                    doc.getInstanceAttributes().put(attr.getName(), attr);
//                }else{
//                    doc.getInstanceAttributes().get(attr.getName()).setValue(attr.getValue());
//                }
//            }
//            
//            //Set<InstanceAttribute> attrsToCreate = new HashSet<InstanceAttribute>(attrs.values());
//            //attrsToCreate.removeAll(doc.getInstanceAttributes().values());
//
//            /*
//            for (InstanceAttribute attrToCreate : attrsToCreate) {
//                attrDAO.createAttribute(attrToCreate);
//            }
//            */
//            doc.setRevisionNote(pRevisionNote);
//            doc.setLinkedDocuments(links);
//            //doc.setInstanceAttributes(attrs);
//            return docM;
//
//        } else {
//            throw new NotAllowedException(new Locale(user.getLanguage()), "NotAllowedException25");
//        }
//
//    }
    
}
