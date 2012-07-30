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

import com.docdoku.core.services.*;
import com.docdoku.core.document.*;
import com.docdoku.core.common.*;
import com.docdoku.core.meta.*;
import com.docdoku.core.security.*;
import com.docdoku.core.workflow.*;
import com.docdoku.core.util.NamingConvention;
import com.docdoku.core.util.Tools;
import com.docdoku.server.dao.*;
import com.docdoku.server.vault.DataManager;
import com.docdoku.server.vault.filesystem.DataManagerImpl;
import java.io.File;
import java.text.ParseException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.jws.WebService;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.annotation.security.DeclareRoles;
import javax.persistence.NoResultException;


@DeclareRoles("users")
@Local(IDocumentManagerLocal.class)
@Stateless(name = "DocumentManagerBean")
@WebService(endpointInterface = "com.docdoku.core.services.IDocumentManagerWS")
public class DocumentManagerBean implements IDocumentManagerWS, IDocumentManagerLocal {

    @PersistenceContext
    private EntityManager em;
    @Resource
    private SessionContext ctx;
    @Resource(name = "vaultPath")
    private String vaultPath;
    @EJB
    private IUserManagerLocal userManager;
    @EJB
    private IMailerLocal mailer;
    @EJB
    private IndexerBean indexer;
    @EJB
    private IndexSearcherBean indexSearcher;
    private DataManager dataManager;
    private final static Logger LOGGER = Logger.getLogger(DocumentManagerBean.class.getName());

    @PostConstruct
    private void init() {
        dataManager = new DataManagerImpl(new File(vaultPath));
    }

    @RolesAllowed("users")
    @Override
    public File saveFileInTemplate(DocumentMasterTemplateKey pDocMTemplateKey, String pName, long pSize) throws WorkspaceNotFoundException, NotAllowedException, DocumentMasterTemplateNotFoundException, FileAlreadyExistsException, UserNotFoundException, UserNotActiveException, CreationException {
        User user = userManager.checkWorkspaceReadAccess(pDocMTemplateKey.getWorkspaceId());
        //TODO checkWorkspaceWriteAccess ?
        if (!NamingConvention.correct(pName)) {
            throw new NotAllowedException(Locale.getDefault(), "NotAllowedException9");
        }

        DocumentMasterTemplateDAO templateDAO = new DocumentMasterTemplateDAO(em);
        DocumentMasterTemplate template = templateDAO.loadDocMTemplate(pDocMTemplateKey);
        BinaryResource file = null;
        String fullName = template.getWorkspaceId() + "/templates/" + template.getId() + "/" + pName;

        for (BinaryResource bin : template.getAttachedFiles()) {
            if (bin.getFullName().equals(fullName)) {
                file = bin;
                break;
            }
        }

        if (file == null) {
            file = new BinaryResource(fullName, pSize);
            new BinaryResourceDAO(em).createBinaryResource(file);
            template.addFile(file);
        } else {
            file.setContentLength(pSize);
        }

        return dataManager.getVaultFile(file);
    }

    @RolesAllowed("users")
    @Override
    public File saveFileInDocument(DocumentIterationKey pDocPK, String pName, long pSize) throws WorkspaceNotFoundException, NotAllowedException, DocumentMasterNotFoundException, FileAlreadyExistsException, UserNotFoundException, UserNotActiveException, CreationException {
        User user = userManager.checkWorkspaceReadAccess(pDocPK.getWorkspaceId());
        if (!NamingConvention.correct(pName)) {
            throw new NotAllowedException(Locale.getDefault(), "NotAllowedException9");
        }

        DocumentMasterDAO docMDAO = new DocumentMasterDAO(em);
        DocumentMaster docM = docMDAO.loadDocM(new DocumentMasterKey(pDocPK.getWorkspaceId(), pDocPK.getDocumentMasterId(), pDocPK.getDocumentMasterVersion()));
        DocumentIteration document = docM.getIteration(pDocPK.getIteration());
        if (docM.isCheckedOut() && docM.getCheckOutUser().equals(user) && docM.getLastIteration().equals(document)) {
            BinaryResource file = null;
            String fullName = docM.getWorkspaceId() + "/documents/" + docM.getId() + "/" + docM.getVersion() + "/" + document.getIteration() + "/" + pName;

            for (BinaryResource bin : document.getAttachedFiles()) {
                if (bin.getFullName().equals(fullName)) {
                    file = bin;
                    break;
                }
            }
            if (file == null) {
                file = new BinaryResource(fullName, pSize);
                new BinaryResourceDAO(em).createBinaryResource(file);
                document.addFile(file);
            } else {
                file.setContentLength(pSize);
            }
            return dataManager.getVaultFile(file);
        } else {
            throw new NotAllowedException(Locale.getDefault(), "NotAllowedException4");
        }
    }

    @LogDocument
    @RolesAllowed("users")
    @Override
    public File getDataFile(String pFullName) throws WorkspaceNotFoundException, NotAllowedException, FileNotFoundException, UserNotFoundException, UserNotActiveException {
        User user = userManager.checkWorkspaceReadAccess(BinaryResource.parseWorkspaceId(pFullName));

        BinaryResourceDAO binDAO = new BinaryResourceDAO(new Locale(user.getLanguage()), em);
        BinaryResource file = binDAO.loadBinaryResource(pFullName);

        DocumentIteration document = binDAO.getDocumentOwner(file);
        if (document != null) {
            DocumentMaster docM = document.getDocumentMaster();
            String owner = docM.getLocation().getOwner();

            if (((owner != null) && (!owner.equals(user.getLogin()))) || (docM.isCheckedOut() && !docM.getCheckOutUser().equals(user) && docM.getLastIteration().equals(document))) {
                throw new NotAllowedException(new Locale(user.getLanguage()), "NotAllowedException34");
            } else {
                return dataManager.getDataFile(file);
            }
        } else {
            return dataManager.getDataFile(file);
        }
    }

    @RolesAllowed("users")
    @Override
    public User whoAmI(String pWorkspaceId) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException {
        return userManager.checkWorkspaceReadAccess(pWorkspaceId);
    }

    @RolesAllowed("users")
    @Override
    public Workspace getWorkspace(String pWorkspaceId) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
        return user.getWorkspace();
    }

    @RolesAllowed("users")
    @Override
    public String [] getFolders(String pCompletePath) throws WorkspaceNotFoundException, FolderNotFoundException, UserNotFoundException, UserNotActiveException {
        User user = userManager.checkWorkspaceReadAccess(Folder.parseWorkspaceId(pCompletePath));
        Folder[] subFolders = new FolderDAO(new Locale(user.getLanguage()), em).getSubFolders(pCompletePath);
        String[] shortNames = new String[subFolders.length];
        int i = 0;
        for (Folder f : subFolders) {
            shortNames[i++] = f.getShortName();
        }
        return shortNames;
    }

    @RolesAllowed("users")
    @Override
    public DocumentMaster[] findDocumentMastersByFolder(String pCompletePath) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException {
        String workspaceId = Folder.parseWorkspaceId(pCompletePath);
        User user = userManager.checkWorkspaceReadAccess(workspaceId);
        List<DocumentMaster> docMs = new DocumentMasterDAO(new Locale(user.getLanguage()), em).findDocMsByFolder(pCompletePath);
        ListIterator<DocumentMaster> ite = docMs.listIterator();
        Workspace wks = new WorkspaceDAO(new Locale(user.getLanguage()), em).loadWorkspace(workspaceId);
        boolean isAdmin = wks.getAdmin().getLogin().equals(user.getLogin());
        while (ite.hasNext()) {
            DocumentMaster docM = ite.next();
            if (!isAdmin && docM.getACL() != null && !docM.getACL().hasReadAccess(user)) {
                ite.remove();
                continue;
            }
            if ((docM.isCheckedOut()) && (!docM.getCheckOutUser().equals(user))) {
                docM = docM.clone();
                docM.removeLastIteration();
                ite.set(docM);

            }
        }
        return docMs.toArray(new DocumentMaster[docMs.size()]);
    }

    @RolesAllowed("users")
    @Override
    public DocumentMaster[] findDocumentMastersByTag(TagKey pKey) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException {
        String workspaceId = pKey.getWorkspaceId();
        User user = userManager.checkWorkspaceReadAccess(workspaceId);
        List<DocumentMaster> docMs = new DocumentMasterDAO(new Locale(user.getLanguage()), em).findDocMsByTag(new Tag(user.getWorkspace(), pKey.getLabel()));
        ListIterator<DocumentMaster> ite = docMs.listIterator();
        Workspace wks = new WorkspaceDAO(new Locale(user.getLanguage()), em).loadWorkspace(workspaceId);
        boolean isAdmin = wks.getAdmin().getLogin().equals(user.getLogin());
        while (ite.hasNext()) {
            DocumentMaster docM = ite.next();
            if (!isAdmin && docM.getACL() != null && !docM.getACL().hasReadAccess(user)) {
                ite.remove();
                continue;
            }
            if ((docM.isCheckedOut()) && (!docM.getCheckOutUser().equals(user))) {
                docM = docM.clone();
                docM.removeLastIteration();
                ite.set(docM);

            }
        }
        return docMs.toArray(new DocumentMaster[docMs.size()]);
    }

    @RolesAllowed("users")
    @Override
    public DocumentMaster getDocumentMaster(DocumentMasterKey pDocMPK) throws WorkspaceNotFoundException, DocumentMasterNotFoundException, NotAllowedException, UserNotFoundException, UserNotActiveException {
        User user = userManager.checkWorkspaceReadAccess(pDocMPK.getWorkspaceId());
        DocumentMaster docM = new DocumentMasterDAO(new Locale(user.getLanguage()), em).loadDocM(pDocMPK);
        String owner = docM.getLocation().getOwner();
        if ((owner != null) && (!owner.equals(user.getLogin()))) {
            throw new NotAllowedException(new Locale(user.getLanguage()), "NotAllowedException5");
        }

        if ((docM.isCheckedOut()) && (!docM.getCheckOutUser().equals(user))) {
            docM = docM.clone();
            docM.removeLastIteration();
        }
        return docM;
    }

    @RolesAllowed("users")
    @Override
    public DocumentMaster[] getCheckedOutDocumentMasters(String pWorkspaceId) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
        List<DocumentMaster> docMs = new DocumentMasterDAO(new Locale(user.getLanguage()), em).findCheckedOutDocMs(user);
        return docMs.toArray(new DocumentMaster[docMs.size()]);
    }

    @RolesAllowed("users")
    @Override
    public Task[] getTasks(String pWorkspaceId) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
        return new TaskDAO(new Locale(user.getLanguage()), em).findTasks(user);
    }

    @RolesAllowed("users")
    @Override
    public DocumentMasterKey[] getIterationChangeEventSubscriptions(String pWorkspaceId) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
        return new SubscriptionDAO(em).getIterationChangeEventSubscriptions(user);
    }

    @RolesAllowed("users")
    @Override
    public DocumentMasterKey[] getStateChangeEventSubscriptions(String pWorkspaceId) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
        return new SubscriptionDAO(em).getStateChangeEventSubscriptions(user);
    }

    @RolesAllowed("users")
    @Override
    public String generateId(String pWorkspaceId, String pDocMTemplateId) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException, DocumentMasterTemplateNotFoundException {

        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
        DocumentMasterTemplate template = new DocumentMasterTemplateDAO(new Locale(user.getLanguage()), em).loadDocMTemplate(new DocumentMasterTemplateKey(user.getWorkspaceId(), pDocMTemplateId));

        String newId = null;
        try {
            String latestId = new DocumentMasterDAO(new Locale(user.getLanguage()), em).findLatestDocMId(pWorkspaceId, template.getDocumentType());
            String inputMask = template.getMask();
            String convertedMask = Tools.convertMask(inputMask);
            newId = Tools.increaseId(latestId, convertedMask);
        } catch (ParseException ex) {
            //may happen when a different mask has been used for the same document type
        } catch (NoResultException ex) {
            //may happen when no document of the specified type has been created
        }
        return newId;

    }

    @RolesAllowed("users")
    @Override
    public DocumentMaster[] searchDocumentMasters(SearchQuery pQuery) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException {
        User user = userManager.checkWorkspaceReadAccess(pQuery.getWorkspaceId());
        //preparing tag filtering
        Set<Tag> tags = null;
        if (pQuery.getTags() != null) {
            Workspace wks = new Workspace();
            wks.setId(pQuery.getWorkspaceId());
            tags = new HashSet<Tag>();
            for (String label : pQuery.getTags()) {
                tags.add(new Tag(wks, label));
            }
        }

        List<DocumentMaster> fetchedDocMs = new DocumentMasterDAO(new Locale(user.getLanguage()), em).searchDocumentMasters(pQuery.getWorkspaceId(), pQuery.getDocMId(), pQuery.getTitle(), pQuery.getVersion(), pQuery.getAuthor(), pQuery.getType(), pQuery.getCreationDateFrom(),
                pQuery.getCreationDateTo(), tags, pQuery.getAttributes() != null ? Arrays.asList(pQuery.getAttributes()) : null);

        //preparing fulltext filtering
        Set<DocumentMasterKey> indexedKeys = null;
        if (fetchedDocMs.size() > 0 && pQuery.getContent() != null && !pQuery.getContent().equals("")) {
            indexedKeys = indexSearcher.searchInIndex(pQuery.getWorkspaceId(), pQuery.getContent());
        }

        Workspace wks = new WorkspaceDAO(new Locale(user.getLanguage()), em).loadWorkspace(pQuery.getWorkspaceId());
        boolean isAdmin = wks.getAdmin().getLogin().equals(user.getLogin());

        ListIterator<DocumentMaster> ite = fetchedDocMs.listIterator();
        docMBlock:
        while (ite.hasNext()) {
            DocumentMaster docM = ite.next();
            if (indexedKeys != null && (!indexedKeys.contains(docM.getKey()))) {
                ite.remove();
                continue docMBlock;
            }

            //TODO search should not fetch back private docM
            if ((docM.isCheckedOut()) && (!docM.getCheckOutUser().equals(user))) {
                docM = docM.clone();
                docM.removeLastIteration();
                ite.set(docM);
            }

            //Check acess rights
            if (!isAdmin && docM.getACL() != null && !docM.getACL().hasReadAccess(user)) {
                ite.remove();
                continue;
            }
        }
        return fetchedDocMs.toArray(new DocumentMaster[fetchedDocMs.size()]);
    }

    @RolesAllowed("users")
    @Override
    public WorkflowModel[] getWorkflowModels(String pWorkspaceId) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
        return new WorkflowModelDAO(new Locale(user.getLanguage()), em).findAllWorkflowModels(pWorkspaceId);
    }

    @RolesAllowed("users")
    @Override
    public DocumentMasterTemplate[] getDocumentMasterTemplates(String pWorkspaceId) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
        return new DocumentMasterTemplateDAO(new Locale(user.getLanguage()), em).findAllDocMTemplates(pWorkspaceId);
    }

    @RolesAllowed("users")
    @Override
    public WorkflowModel getWorkflowModel(WorkflowModelKey pKey)
            throws WorkspaceNotFoundException, WorkflowModelNotFoundException, UserNotFoundException, UserNotActiveException {
        User user = userManager.checkWorkspaceReadAccess(pKey.getWorkspaceId());
        return new WorkflowModelDAO(new Locale(user.getLanguage()), em).loadWorkflowModel(pKey);
    }

    @RolesAllowed("users")
    @Override
    public DocumentMasterTemplate getDocumentMasterTemplate(DocumentMasterTemplateKey pKey)
            throws WorkspaceNotFoundException, DocumentMasterTemplateNotFoundException, UserNotFoundException, UserNotActiveException {
        User user = userManager.checkWorkspaceReadAccess(pKey.getWorkspaceId());
        return new DocumentMasterTemplateDAO(new Locale(user.getLanguage()), em).loadDocMTemplate(pKey);
    }

    @RolesAllowed("users")
    @Override
    public WorkflowModel createWorkflowModel(String pWorkspaceId, String pId, String pFinalLifeCycleState, ActivityModel[] pActivityModels) throws WorkspaceNotFoundException, AccessRightException, UserNotFoundException, WorkflowModelAlreadyExistsException, CreationException {
        User user = userManager.checkWorkspaceWriteAccess(pWorkspaceId);

        Locale userLocale = new Locale(user.getLanguage());
        WorkflowModelDAO modelDAO = new WorkflowModelDAO(userLocale, em);
        WorkflowModel model = new WorkflowModel(user.getWorkspace(), pId, user, pFinalLifeCycleState, pActivityModels);
        Tools.resetParentReferences(model);
        Date now = new Date();
        model.setCreationDate(now);
        modelDAO.createWorkflowModel(model);
        return model;
    }

    @RolesAllowed("users")
    @Override
    public DocumentMasterTemplate updateDocumentMasterTemplate(DocumentMasterTemplateKey pKey, String pDocumentType, String pMask, InstanceAttributeTemplate[] pAttributeTemplates, boolean idGenerated) throws WorkspaceNotFoundException, WorkspaceNotFoundException, AccessRightException, DocumentMasterTemplateNotFoundException, UserNotFoundException {
        User user = userManager.checkWorkspaceWriteAccess(pKey.getWorkspaceId());

        DocumentMasterTemplateDAO templateDAO = new DocumentMasterTemplateDAO(new Locale(user.getLanguage()), em);
        DocumentMasterTemplate template = templateDAO.loadDocMTemplate(pKey);
        Date now = new Date();
        template.setCreationDate(now);
        template.setAuthor(user);
        template.setDocumentType(pDocumentType);
        template.setMask(pMask);
        template.setIdGenerated(idGenerated);

        Set<InstanceAttributeTemplate> attrs = new HashSet<InstanceAttributeTemplate>();
        for (InstanceAttributeTemplate attr : pAttributeTemplates) {
            attr.setDocumentMasterTemplate(template);
            attrs.add(attr);
        }

        Set<InstanceAttributeTemplate> attrsToRemove = new HashSet<InstanceAttributeTemplate>(template.getAttributeTemplates());
        attrsToRemove.removeAll(attrs);

        InstanceAttributeTemplateDAO attrDAO = new InstanceAttributeTemplateDAO(em);
        for (InstanceAttributeTemplate attrToRemove : attrsToRemove) {
            attrDAO.removeAttribute(attrToRemove);
        }

        template.setAttributeTemplates(attrs);
        return template;
    }

    @RolesAllowed("users")
    @Override
    public void deleteWorkflowModel(WorkflowModelKey pKey) throws WorkspaceNotFoundException, AccessRightException, WorkflowModelNotFoundException, UserNotFoundException {
        User user = userManager.checkWorkspaceWriteAccess(pKey.getWorkspaceId());
        new WorkflowModelDAO(new Locale(user.getLanguage()), em).removeWorkflowModel(pKey);
    }

    @RolesAllowed("users")
    @Override
    public void deleteTag(TagKey pKey) throws WorkspaceNotFoundException, AccessRightException, TagNotFoundException, UserNotFoundException {
        User user = userManager.checkWorkspaceWriteAccess(pKey.getWorkspaceId());
        Locale userLocale = new Locale(user.getLanguage());
        Tag tagToRemove = new Tag(user.getWorkspace(), pKey.getLabel());
        List<DocumentMaster> docMs = new DocumentMasterDAO(userLocale, em).findDocMsByTag(tagToRemove);
        for (DocumentMaster docM : docMs) {
            docM.getTags().remove(tagToRemove);
        }

        new TagDAO(userLocale, em).removeTag(pKey);
    }

    @Override
    public void createTag(String pWorkspaceId, String pLabel) throws WorkspaceNotFoundException, AccessRightException, CreationException, TagAlreadyExistsException, UserNotFoundException {
        User user = userManager.checkWorkspaceWriteAccess(pWorkspaceId);
        Locale userLocale = new Locale(user.getLanguage());
        TagDAO tagDAO = new TagDAO(userLocale, em);
        Tag tag = new Tag(user.getWorkspace(), pLabel);
        tagDAO.createTag(tag);
    }

    @RolesAllowed("users")
    @Override
    public DocumentMaster createDocumentMaster(String pParentFolder,
            String pDocMId, String pTitle, String pDescription, String pDocMTemplateId, String pWorkflowModelId, ACLUserEntry[] pACLUserEntries, ACLUserGroupEntry[] pACLUserGroupEntries) throws WorkspaceNotFoundException, WorkflowModelNotFoundException, NotAllowedException, DocumentMasterTemplateNotFoundException, AccessRightException, DocumentMasterAlreadyExistsException, FolderNotFoundException, FileAlreadyExistsException, UserNotFoundException, CreationException {
        User user = userManager.checkWorkspaceWriteAccess(Folder.parseWorkspaceId(pParentFolder));
        if (!NamingConvention.correct(pDocMId)) {
            throw new NotAllowedException(new Locale(user.getLanguage()), "NotAllowedException9");
        }
        Folder folder = new FolderDAO(new Locale(user.getLanguage()), em).loadFolder(pParentFolder);
        checkWritingRight(user, folder);

        DocumentMaster docM;
        DocumentIteration newDoc;

        if (pDocMTemplateId == null) {
            docM = new DocumentMaster(user.getWorkspace(), pDocMId, user);
            newDoc = docM.createNextIteration(user);
            //specify an empty type instead of null
            //so the search will find it with the % character
            docM.setType("");
        } else {
            DocumentMasterTemplate template = new DocumentMasterTemplateDAO(new Locale(user.getLanguage()), em).loadDocMTemplate(new DocumentMasterTemplateKey(user.getWorkspaceId(), pDocMTemplateId));
            docM = new DocumentMaster(user.getWorkspace(), pDocMId, user);
            docM.setType(template.getDocumentType());
            newDoc = docM.createNextIteration(user);

            Map<String, InstanceAttribute> attrs = new HashMap<String, InstanceAttribute>();
            for (InstanceAttributeTemplate attrTemplate : template.getAttributeTemplates()) {
                InstanceAttribute attr = attrTemplate.createInstanceAttribute();
                //attr.setDocument(newDoc);
                attrs.put(attr.getName(), attr);
            }
            newDoc.setInstanceAttributes(attrs);

            BinaryResourceDAO binDAO = new BinaryResourceDAO(new Locale(user.getLanguage()), em);
            for (BinaryResource sourceFile : template.getAttachedFiles()) {
                String fileName = sourceFile.getName();
                long length = sourceFile.getContentLength();

                String fullName = docM.getWorkspaceId() + "/documents/" + docM.getId() + "/A/1/" + fileName;
                BinaryResource targetFile = new BinaryResource(fullName, length);
                binDAO.createBinaryResource(targetFile);

                newDoc.addFile(targetFile);
                dataManager.copyData(sourceFile, targetFile);
            }
        }

        if (pWorkflowModelId != null) {
            WorkflowModel workflowModel = new WorkflowModelDAO(new Locale(user.getLanguage()), em).loadWorkflowModel(new WorkflowModelKey(user.getWorkspaceId(), pWorkflowModelId));
            Workflow workflow = workflowModel.createWorkflow();
            docM.setWorkflow(workflow);

            Collection<Task> runningTasks = workflow.getRunningTasks();
            for (Task runningTask : runningTasks) {
                runningTask.start();
            }
            mailer.sendApproval(runningTasks, docM);
        }

        docM.setTitle(pTitle);
        docM.setDescription(pDescription);

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
            docM.setACL(acl);
        }
        Date now = new Date();
        docM.setCreationDate(now);
        docM.setLocation(folder);
        docM.setCheckOutUser(user);
        docM.setCheckOutDate(now);
        newDoc.setCreationDate(now);
        DocumentMasterDAO docMDAO = new DocumentMasterDAO(new Locale(user.getLanguage()), em);

        docMDAO.createDocM(docM);
        return docM;
    }

    @RolesAllowed("users")
    @Override
    public DocumentMasterTemplate createDocumentMasterTemplate(String pWorkspaceId, String pId, String pDocumentType,
            String pMask, InstanceAttributeTemplate[] pAttributeTemplates, boolean idGenerated) throws WorkspaceNotFoundException, AccessRightException, DocumentMasterTemplateAlreadyExistsException, UserNotFoundException, NotAllowedException, CreationException {
        User user = userManager.checkWorkspaceWriteAccess(pWorkspaceId);
        if (!NamingConvention.correct(pId)) {
            throw new NotAllowedException(new Locale(user.getLanguage()), "NotAllowedException9");
        }
        DocumentMasterTemplate template = new DocumentMasterTemplate(user.getWorkspace(), pId, user, pDocumentType, pMask);
        Date now = new Date();
        template.setCreationDate(now);
        template.setIdGenerated(idGenerated);

        Set<InstanceAttributeTemplate> attrs = new HashSet<InstanceAttributeTemplate>();
        for (InstanceAttributeTemplate attr : pAttributeTemplates) {
            attr.setDocumentMasterTemplate(template);
            attrs.add(attr);
        }
        template.setAttributeTemplates(attrs);

        new DocumentMasterTemplateDAO(new Locale(user.getLanguage()), em).createDocMTemplate(template);
        return template;
    }

    @RolesAllowed("users")
    @Override
    public DocumentMaster moveDocumentMaster(String pParentFolder, DocumentMasterKey pDocMPK) throws WorkspaceNotFoundException, DocumentMasterNotFoundException, NotAllowedException, AccessRightException, FolderNotFoundException, UserNotFoundException, UserNotActiveException {
        //TODO security check if both parameter belong to the same workspace
        User user = userManager.checkWorkspaceWriteAccess(pDocMPK.getWorkspaceId());
        Folder newLocation = new FolderDAO(new Locale(user.getLanguage()), em).loadFolder(pParentFolder);
        checkWritingRight(user, newLocation);
        DocumentMasterDAO docMDAO = new DocumentMasterDAO(new Locale(user.getLanguage()), em);
        DocumentMaster docM = docMDAO.loadDocM(pDocMPK);
        //Check access rights on docM
        Workspace wks = new WorkspaceDAO(new Locale(user.getLanguage()), em).loadWorkspace(pDocMPK.getWorkspaceId());
        boolean isAdmin = wks.getAdmin().getLogin().equals(user.getLogin());
        if (!isAdmin && docM.getACL() != null && !docM.getACL().hasWriteAccess(user)) {
            throw new AccessRightException(new Locale(user.getLanguage()), user);
        }
        Folder oldLocation = docM.getLocation();
        String owner = oldLocation.getOwner();
        if ((owner != null) && (!owner.equals(user.getLogin()))) {
            throw new NotAllowedException(new Locale(user.getLanguage()), "NotAllowedException6");
        } else {
            docM.setLocation(newLocation);
            if ((docM.isCheckedOut()) && (!docM.getCheckOutUser().equals(user))) {
                docM = docM.clone();
                docM.removeLastIteration();
            }
            return docM;
        }
    }

    @RolesAllowed("users")
    @Override
    public Folder createFolder(String pParentFolder, String pFolder)
            throws WorkspaceNotFoundException, NotAllowedException, AccessRightException, FolderNotFoundException, FolderAlreadyExistsException, UserNotFoundException, CreationException {
        User user = userManager.checkWorkspaceWriteAccess(Folder.parseWorkspaceId(pParentFolder));
        if (!NamingConvention.correct(pFolder)) {
            throw new NotAllowedException(new Locale(user.getLanguage()), "NotAllowedException9");
        }
        FolderDAO folderDAO = new FolderDAO(new Locale(user.getLanguage()), em);
        Folder folder = folderDAO.loadFolder(pParentFolder);
        checkFolderStructureRight(user);
        checkWritingRight(user, folder);
        Folder newFolder = new Folder(pParentFolder, pFolder);
        folderDAO.createFolder(newFolder);
        return newFolder;
    }

    @RolesAllowed("users")
    @CheckActivity
    @Override
    public DocumentMaster approve(String pWorkspaceId, TaskKey pTaskKey, String pComment)
            throws WorkspaceNotFoundException, TaskNotFoundException, NotAllowedException, UserNotFoundException, UserNotActiveException {
        //TODO no check is made that pTaskKey is from the same workspace than pWorkspaceId
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);

        Task task = new TaskDAO(new Locale(user.getLanguage()), em).loadTask(pTaskKey);
        Workflow workflow = task.getActivity().getWorkflow();
        DocumentMaster docM = new WorkflowDAO(em).getTarget(workflow);


        if (!task.getWorker().equals(user)) {
            throw new NotAllowedException(new Locale(user.getLanguage()), "NotAllowedException14");
        }

        if (!workflow.getRunningTasks().contains(task)) {
            throw new NotAllowedException(new Locale(user.getLanguage()), "NotAllowedException15");
        }

        if (docM.isCheckedOut()) {
            throw new NotAllowedException(new Locale(user.getLanguage()), "NotAllowedException16");
        }

        int previousStep = workflow.getCurrentStep();
        task.approve(pComment, docM.getLastIteration().getIteration());
        int currentStep = workflow.getCurrentStep();

        User[] subscribers = new SubscriptionDAO(em).getStateChangeEventSubscribers(docM);

        if (previousStep != currentStep && subscribers.length != 0) {
            mailer.sendStateNotification(subscribers, docM);
        }

        Collection<Task> runningTasks = workflow.getRunningTasks();
        for (Task runningTask : runningTasks) {
            runningTask.start();
        }
        mailer.sendApproval(runningTasks, docM);
        return docM;
    }

    @RolesAllowed("users")
    @CheckActivity
    @Override
    public DocumentMaster reject(String pWorkspaceId, TaskKey pTaskKey, String pComment)
            throws WorkspaceNotFoundException, TaskNotFoundException, NotAllowedException, UserNotFoundException, UserNotActiveException {
        //TODO no check is made that pTaskKey is from the same workspace than pWorkspaceId
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);

        Task task = new TaskDAO(new Locale(user.getLanguage()), em).loadTask(pTaskKey);
        Workflow workflow = task.getActivity().getWorkflow();
        DocumentMaster docM = new WorkflowDAO(em).getTarget(workflow);

        if (!task.getWorker().equals(user)) {
            throw new NotAllowedException(new Locale(user.getLanguage()), "NotAllowedException14");
        }

        if (!workflow.getRunningTasks().contains(task)) {
            throw new NotAllowedException(new Locale(user.getLanguage()), "NotAllowedException15");
        }

        if (docM.isCheckedOut()) {
            throw new NotAllowedException(new Locale(user.getLanguage()), "NotAllowedException16");
        }

        task.reject(pComment, docM.getLastIteration().getIteration());
        return docM;
    }

    @RolesAllowed("users")
    @Override
    public DocumentMaster checkOut(DocumentMasterKey pDocMPK)
            throws WorkspaceNotFoundException, NotAllowedException, DocumentMasterNotFoundException, AccessRightException, FileAlreadyExistsException, UserNotFoundException, CreationException {
        User user = userManager.checkWorkspaceWriteAccess(pDocMPK.getWorkspaceId());
        DocumentMasterDAO docMDAO = new DocumentMasterDAO(new Locale(user.getLanguage()), em);
        DocumentMaster docM = docMDAO.loadDocM(pDocMPK);
        //Check access rights on docM
        Workspace wks = new WorkspaceDAO(new Locale(user.getLanguage()), em).loadWorkspace(pDocMPK.getWorkspaceId());
        boolean isAdmin = wks.getAdmin().getLogin().equals(user.getLogin());
        if (!isAdmin && docM.getACL() != null && !docM.getACL().hasWriteAccess(user)) {
            throw new AccessRightException(new Locale(user.getLanguage()), user);
        }
        String owner = docM.getLocation().getOwner();
        if ((owner != null) && (!owner.equals(user.getLogin()))) {
            throw new NotAllowedException(new Locale(user.getLanguage()), "NotAllowedException5");
        }

        if (docM.isCheckedOut()) {
            throw new NotAllowedException(new Locale(user.getLanguage()), "NotAllowedException37");
        }

        DocumentIteration beforeLastDocument = docM.getLastIteration();

        DocumentIteration newDoc = docM.createNextIteration(user);
        //We persist the doc as a workaround for a bug which was introduced
        //since glassfish 3 that set the DTYPE to null in the instance attribute table
        em.persist(newDoc);
        Date now = new Date();
        newDoc.setCreationDate(now);
        docM.setCheckOutUser(user);
        docM.setCheckOutDate(now);

        if (beforeLastDocument != null) {
            BinaryResourceDAO binDAO = new BinaryResourceDAO(new Locale(user.getLanguage()), em);
            for (BinaryResource sourceFile : beforeLastDocument.getAttachedFiles()) {
                String fileName = sourceFile.getName();
                long length = sourceFile.getContentLength();
                String fullName = docM.getWorkspaceId() + "/documents/" + docM.getId() + "/" + docM.getVersion() + "/" + newDoc.getIteration() + "/" + fileName;
                BinaryResource targetFile = new BinaryResource(fullName, length);
                binDAO.createBinaryResource(targetFile);
                newDoc.addFile(targetFile);
            }

            Set<DocumentToDocumentLink> links = new HashSet<DocumentToDocumentLink>();
            for (DocumentToDocumentLink link : beforeLastDocument.getLinkedDocuments()) {
                DocumentToDocumentLink newLink = link.clone();
                newLink.setFromDocument(newDoc);
                links.add(newLink);
            }
            newDoc.setLinkedDocuments(links);

            InstanceAttributeDAO attrDAO = new InstanceAttributeDAO(em);
            Map<String, InstanceAttribute> attrs = new HashMap<String, InstanceAttribute>();
            for (InstanceAttribute attr : beforeLastDocument.getInstanceAttributes().values()) {
                InstanceAttribute newAttr = attr.clone();
                //newAttr.setDocument(newDoc);
                //Workaround for the NULL DTYPE bug
                attrDAO.createAttribute(newAttr);
                attrs.put(newAttr.getName(), newAttr);
            }
            newDoc.setInstanceAttributes(attrs);
        }

        return docM;
    }

    @RolesAllowed("users")
    @Override
    public DocumentMaster saveTags(DocumentMasterKey pDocMPK, String[] pTags)
            throws WorkspaceNotFoundException, NotAllowedException, DocumentMasterNotFoundException, AccessRightException, UserNotFoundException {
        User user = userManager.checkWorkspaceWriteAccess(pDocMPK.getWorkspaceId());
        Locale userLocale = new Locale(user.getLanguage());
        DocumentMasterDAO docMDAO = new DocumentMasterDAO(userLocale, em);
        DocumentMaster docM = docMDAO.loadDocM(pDocMPK);
        String owner = docM.getLocation().getOwner();
        if ((owner != null) && (!owner.equals(user.getLogin()))) {
            throw new NotAllowedException(new Locale(user.getLanguage()), "NotAllowedException5");
        }

        HashSet<Tag> tags = new HashSet<Tag>();
        for (String label : pTags) {
            tags.add(new Tag(user.getWorkspace(), label));
        }

        TagDAO tagDAO = new TagDAO(userLocale, em);
        List<Tag> existingTags = Arrays.asList(tagDAO.findAllTags(user.getWorkspaceId()));

        Set<Tag> tagsToCreate = new HashSet<Tag>(tags);
        tagsToCreate.removeAll(existingTags);

        for (Tag t : tagsToCreate) {
            try {
                tagDAO.createTag(t);
            } catch (CreationException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            } catch (TagAlreadyExistsException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }
        }

        docM.setTags(tags);

        if ((docM.isCheckedOut()) && (!docM.getCheckOutUser().equals(user))) {
            docM = docM.clone();
            docM.removeLastIteration();
        }
        return docM;
    }

    @RolesAllowed("users")
    @Override
    public DocumentMaster undoCheckOut(DocumentMasterKey pDocMPK)
            throws WorkspaceNotFoundException, DocumentMasterNotFoundException, NotAllowedException, UserNotFoundException, UserNotActiveException {
        User user = userManager.checkWorkspaceReadAccess(pDocMPK.getWorkspaceId());
        DocumentMasterDAO docMDAO = new DocumentMasterDAO(new Locale(user.getLanguage()), em);
        DocumentMaster docM = docMDAO.loadDocM(pDocMPK);
        if (docM.isCheckedOut() && docM.getCheckOutUser().equals(user)) {
            DocumentIteration doc = docM.removeLastIteration();
            for (BinaryResource file : doc.getAttachedFiles()) {
                dataManager.delData(file);
            }

            DocumentDAO docDAO = new DocumentDAO(em);
            docDAO.removeDoc(doc);
            docM.setCheckOutDate(null);
            docM.setCheckOutUser(null);
            return docM;
        } else {
            throw new NotAllowedException(new Locale(user.getLanguage()), "NotAllowedException19");
        }
    }

    @RolesAllowed("users")
    @Override
    public DocumentMaster checkIn(DocumentMasterKey pDocMPK)
            throws WorkspaceNotFoundException, NotAllowedException, DocumentMasterNotFoundException, AccessRightException, UserNotFoundException {
        User user = userManager.checkWorkspaceWriteAccess(pDocMPK.getWorkspaceId());
        DocumentMasterDAO docMDAO = new DocumentMasterDAO(new Locale(user.getLanguage()), em);
        DocumentMaster docM = docMDAO.loadDocM(pDocMPK);
        //Check access rights on docM
        Workspace wks = new WorkspaceDAO(new Locale(user.getLanguage()), em).loadWorkspace(pDocMPK.getWorkspaceId());
        boolean isAdmin = wks.getAdmin().getLogin().equals(user.getLogin());
        if (!isAdmin && docM.getACL() != null && !docM.getACL().hasWriteAccess(user)) {
            throw new AccessRightException(new Locale(user.getLanguage()), user);
        }
        if (docM.isCheckedOut() && docM.getCheckOutUser().equals(user)) {
            User[] subscribers = new SubscriptionDAO(em).getIterationChangeEventSubscribers(docM);

            docM.setCheckOutDate(null);
            docM.setCheckOutUser(null);

            if (subscribers.length != 0) {
                mailer.sendIterationNotification(subscribers, docM);
            }

            for (BinaryResource bin : docM.getLastIteration().getAttachedFiles()) {
                File physicalFile = dataManager.getDataFile(bin);
                indexer.addToIndex(bin.getFullName(), physicalFile.getPath());
            }

            return docM;
        } else {
            throw new NotAllowedException(new Locale(user.getLanguage()), "NotAllowedException20");
        }
    }

    @RolesAllowed("users")
    @Override
    public DocumentMasterKey[] deleteFolder(String pCompletePath)
            throws WorkspaceNotFoundException, NotAllowedException, AccessRightException, UserNotFoundException, FolderNotFoundException {
        User user = userManager.checkWorkspaceWriteAccess(Folder.parseWorkspaceId(pCompletePath));
        FolderDAO folderDAO = new FolderDAO(new Locale(user.getLanguage()), em);
        Folder folder = folderDAO.loadFolder(pCompletePath);
        String owner = folder.getOwner();
        checkFolderStructureRight(user);
        if (((owner != null) && (!owner.equals(user.getLogin()))) || (folder.isRoot()) || folder.isHome()) {
            throw new NotAllowedException(new Locale(user.getLanguage()), "NotAllowedException21");
        } else {
            List<DocumentMaster> docMs = folderDAO.removeFolder(folder);
            DocumentMasterKey[] pks = new DocumentMasterKey[docMs.size()];
            int i = 0;
            for (DocumentMaster docM : docMs) {
                pks[i++] = docM.getKey();
                for (DocumentIteration doc : docM.getDocumentIterations()) {
                    for (BinaryResource file : doc.getAttachedFiles()) {
                        indexer.removeFromIndex(file.getFullName());
                        dataManager.delData(file);
                    }
                }
            }
            return pks;
        }
    }

    @RolesAllowed("users")
    @Override
    public DocumentMasterKey[] moveFolder(String pCompletePath, String pDestParentFolder, String pDestFolder)
            throws WorkspaceNotFoundException, NotAllowedException, AccessRightException, UserNotFoundException, FolderNotFoundException, CreationException, FolderAlreadyExistsException {
        //TODO security check if both parameter belong to the same workspace
        String workspace = Folder.parseWorkspaceId(pCompletePath);
        User user = userManager.checkWorkspaceWriteAccess(workspace);
        FolderDAO folderDAO = new FolderDAO(new Locale(user.getLanguage()), em);
        Folder folder = folderDAO.loadFolder(pCompletePath);
        String owner = folder.getOwner();
        checkFolderStructureRight(user);
        if (((owner != null) && (!owner.equals(user.getLogin()))) || (folder.isRoot()) || folder.isHome()) {
            throw new NotAllowedException(new Locale(user.getLanguage()), "NotAllowedException21");
        } else if (!workspace.equals(Folder.parseWorkspaceId(pDestParentFolder))) {
            throw new NotAllowedException(new Locale(user.getLanguage()), "NotAllowedException23");
        } else {
            Folder newFolder = createFolder(pDestParentFolder, pDestFolder);
            List<DocumentMaster> docMs = folderDAO.moveFolder(folder, newFolder);
            DocumentMasterKey[] pks = new DocumentMasterKey[docMs.size()];
            int i = 0;
            for (DocumentMaster docM : docMs) {
                pks[i++] = docM.getKey();
            }
            return pks;
        }
    }

    @RolesAllowed("users")
    @Override
    public void deleteDocumentMaster(DocumentMasterKey pDocMPK)
            throws WorkspaceNotFoundException, NotAllowedException, DocumentMasterNotFoundException, AccessRightException, UserNotFoundException {
        User user = userManager.checkWorkspaceWriteAccess(pDocMPK.getWorkspaceId());
        DocumentMasterDAO docMDAO = new DocumentMasterDAO(new Locale(user.getLanguage()), em);
        DocumentMaster docM = docMDAO.loadDocM(pDocMPK);
        //Check access rights on
        Workspace wks = new WorkspaceDAO(new Locale(user.getLanguage()), em).loadWorkspace(pDocMPK.getWorkspaceId());
        boolean isAdmin = wks.getAdmin().getLogin().equals(user.getLogin());
        if (!isAdmin && docM.getACL() != null && !docM.getACL().hasWriteAccess(user)) {
            throw new AccessRightException(new Locale(user.getLanguage()), user);
        }
        String owner = docM.getLocation().getOwner();
        if ((owner != null) && (!owner.equals(user.getLogin()))) {
            throw new NotAllowedException(new Locale(user.getLanguage()), "NotAllowedException22");
        }

        docMDAO.removeDocM(docM);

        for (DocumentIteration doc : docM.getDocumentIterations()) {
            for (BinaryResource file : doc.getAttachedFiles()) {
                indexer.removeFromIndex(file.getFullName());
                dataManager.delData(file);
            }
        }
    }

    @RolesAllowed("users")
    @Override
    public void deleteDocumentMasterTemplate(DocumentMasterTemplateKey pKey)
            throws WorkspaceNotFoundException, AccessRightException, DocumentMasterTemplateNotFoundException, UserNotFoundException {
        User user = userManager.checkWorkspaceWriteAccess(pKey.getWorkspaceId());
        DocumentMasterTemplateDAO templateDAO = new DocumentMasterTemplateDAO(new Locale(user.getLanguage()), em);
        DocumentMasterTemplate template = templateDAO.removeDocMTemplate(pKey);
        for (BinaryResource file : template.getAttachedFiles()) {
            dataManager.delData(file);
        }
    }

    @RolesAllowed("users")
    @Override
    public DocumentMaster removeFileFromDocument(String pFullName) throws WorkspaceNotFoundException, DocumentMasterNotFoundException, NotAllowedException, AccessRightException, FileNotFoundException, UserNotFoundException, UserNotActiveException {
        User user = userManager.checkWorkspaceReadAccess(BinaryResource.parseWorkspaceId(pFullName));

        BinaryResourceDAO binDAO = new BinaryResourceDAO(new Locale(user.getLanguage()), em);
        BinaryResource file = binDAO.loadBinaryResource(pFullName);

        DocumentIteration document = binDAO.getDocumentOwner(file);
        DocumentMaster docM = document.getDocumentMaster();
        //check access rights on docM ?
        if (docM.isCheckedOut() && docM.getCheckOutUser().equals(user) && docM.getLastIteration().equals(document)) {
            dataManager.delData(file);
            document.removeFile(file);
            binDAO.removeBinaryResource(file);
            return docM;
        } else {
            throw new NotAllowedException(new Locale(user.getLanguage()), "NotAllowedException24");
        }
    }

    @RolesAllowed("users")
    @Override
    public DocumentMasterTemplate removeFileFromTemplate(String pFullName) throws WorkspaceNotFoundException, DocumentMasterTemplateNotFoundException, AccessRightException, FileNotFoundException, UserNotFoundException, UserNotActiveException {
        User user = userManager.checkWorkspaceReadAccess(BinaryResource.parseWorkspaceId(pFullName));
        //TODO checkWorkspaceWriteAccess ?
        BinaryResourceDAO binDAO = new BinaryResourceDAO(new Locale(user.getLanguage()), em);
        BinaryResource file = binDAO.loadBinaryResource(pFullName);

        DocumentMasterTemplate template = binDAO.getTemplateOwner(file);
        dataManager.delData(file);
        template.removeFile(file);
        binDAO.removeBinaryResource(file);
        return template;
    }

    @RolesAllowed("users")
    @Override
    public DocumentMaster updateDocument(DocumentIterationKey pKey, String pRevisionNote, InstanceAttribute[] pAttributes, DocumentIterationKey[] pLinkKeys) throws WorkspaceNotFoundException, NotAllowedException, DocumentMasterNotFoundException, AccessRightException, UserNotFoundException {
        User user = userManager.checkWorkspaceWriteAccess(pKey.getWorkspaceId());
        DocumentMasterDAO docMDAO = new DocumentMasterDAO(new Locale(user.getLanguage()), em);
        DocumentMaster docM = docMDAO.loadDocM(new DocumentMasterKey(pKey.getWorkspaceId(), pKey.getDocumentMasterId(), pKey.getDocumentMasterVersion()));
        //check access rights on docM ?
        if (docM.isCheckedOut() && docM.getCheckOutUser().equals(user) && docM.getLastIteration().getKey().equals(pKey)) {
            DocumentIteration doc = docM.getLastIteration();
            
            Set<DocumentToDocumentLink> links = new HashSet<DocumentToDocumentLink>();
            for (DocumentIterationKey key : pLinkKeys) {
                links.add(new DocumentToDocumentLink(doc, key));
            }
            Set<DocumentToDocumentLink> linksToRemove = new HashSet<DocumentToDocumentLink>(doc.getLinkedDocuments());
            linksToRemove.removeAll(links);

            DocumentToDocumentLinkDAO linkDAO = new DocumentToDocumentLinkDAO(em);
            for (DocumentToDocumentLink linkToRemove : linksToRemove) {
                linkDAO.removeLink(linkToRemove);
            }

            // set doc for all attributes
            
            Map<String, InstanceAttribute> attrs = new HashMap<String, InstanceAttribute>();
            for (InstanceAttribute attr : pAttributes) {
                //attr.setDocument(doc);
                attrs.put(attr.getName(), attr);
            }

            Set<InstanceAttribute> currentAttrs = new HashSet<InstanceAttribute>(doc.getInstanceAttributes().values());
            //attrsToRemove.removeAll(attrs.values());

            for(InstanceAttribute attr:currentAttrs){
                if(!attrs.containsKey(attr.getName())){
                    doc.getInstanceAttributes().remove(attr.getName());
                }
            }

            
            //InstanceAttributeDAO attrDAO = new InstanceAttributeDAO(em);
            /*
            for (InstanceAttribute attrToRemove : attrsToRemove) {
                attrDAO.removeAttribute(attrToRemove);
            }
            */

            for(InstanceAttribute attr:attrs.values()){
                if(!doc.getInstanceAttributes().containsKey(attr.getName())){
                    doc.getInstanceAttributes().put(attr.getName(), attr);
                }else{
                    doc.getInstanceAttributes().get(attr.getName()).setValue(attr.getValue());
                }
            }
            
            //Set<InstanceAttribute> attrsToCreate = new HashSet<InstanceAttribute>(attrs.values());
            //attrsToCreate.removeAll(doc.getInstanceAttributes().values());

            /*
            for (InstanceAttribute attrToCreate : attrsToCreate) {
                attrDAO.createAttribute(attrToCreate);
            }
            */
            doc.setRevisionNote(pRevisionNote);
            doc.setLinkedDocuments(links);
            //doc.setInstanceAttributes(attrs);
            return docM;

        } else {
            throw new NotAllowedException(new Locale(user.getLanguage()), "NotAllowedException25");
        }

    }

    @RolesAllowed("users")
    @Override
    public DocumentMaster[] createVersion(DocumentMasterKey pOriginalDocMPK,
            String pTitle, String pDescription, String pWorkflowModelId, ACLUserEntry[] pACLUserEntries, ACLUserGroupEntry[] pACLUserGroupEntries) throws WorkspaceNotFoundException, NotAllowedException, DocumentMasterNotFoundException, WorkflowModelNotFoundException, AccessRightException, DocumentMasterAlreadyExistsException, FileAlreadyExistsException, UserNotFoundException, CreationException {
        User user = userManager.checkWorkspaceWriteAccess(pOriginalDocMPK.getWorkspaceId());
        DocumentMasterDAO docMDAO = new DocumentMasterDAO(new Locale(user.getLanguage()), em);
        DocumentMaster originalDocM = docMDAO.loadDocM(pOriginalDocMPK);
        Folder folder = originalDocM.getLocation();
        checkWritingRight(user, folder);

        if (originalDocM.isCheckedOut()) {
            throw new NotAllowedException(new Locale(user.getLanguage()), "NotAllowedException26");
        }

        if (originalDocM.getNumberOfIterations() == 0) {
            throw new NotAllowedException(new Locale(user.getLanguage()), "NotAllowedException27");
        }

        Version version = new Version(originalDocM.getVersion());
        version.increase();
        DocumentMaster docM = new DocumentMaster(originalDocM.getWorkspace(), originalDocM.getId(), version, user);
        docM.setType(originalDocM.getType());
        //create the first iteration which is a copy of the last one of the original docM
        //of course we duplicate the iteration only if it exists !
        DocumentIteration lastDoc = originalDocM.getLastIteration();
        DocumentIteration firstIte = docM.createNextIteration(user);
        if (lastDoc != null) {
            BinaryResourceDAO binDAO = new BinaryResourceDAO(new Locale(user.getLanguage()), em);
            for (BinaryResource sourceFile : lastDoc.getAttachedFiles()) {
                String fileName = sourceFile.getName();
                long length = sourceFile.getContentLength();
                String fullName = docM.getWorkspaceId() + "/documents/" + docM.getId() + "/" + version + "/1/" + fileName;
                BinaryResource targetFile = new BinaryResource(fullName, length);
                binDAO.createBinaryResource(targetFile);
                firstIte.addFile(targetFile);
                dataManager.copyData(sourceFile, targetFile);
            }

            Set<DocumentToDocumentLink> links = new HashSet<DocumentToDocumentLink>();
            for (DocumentToDocumentLink link : lastDoc.getLinkedDocuments()) {
                DocumentToDocumentLink newLink = link.clone();
                newLink.setFromDocument(firstIte);
                links.add(newLink);
            }
            firstIte.setLinkedDocuments(links);

            Map<String, InstanceAttribute> attrs = new HashMap<String, InstanceAttribute>();
            for (InstanceAttribute attr : lastDoc.getInstanceAttributes().values()) {
                InstanceAttribute clonedAttribute = attr.clone();
                //clonedAttribute.setDocument(firstIte);
                attrs.put(clonedAttribute.getName(), clonedAttribute);
            }
            firstIte.setInstanceAttributes(attrs);
        }

        if (pWorkflowModelId != null) {
            WorkflowModel workflowModel = new WorkflowModelDAO(new Locale(user.getLanguage()), em).loadWorkflowModel(new WorkflowModelKey(user.getWorkspaceId(), pWorkflowModelId));
            Workflow workflow = workflowModel.createWorkflow();
            docM.setWorkflow(workflow);

            Collection<Task> runningTasks = workflow.getRunningTasks();
            for (Task runningTask : runningTasks) {
                runningTask.start();
            }
            mailer.sendApproval(runningTasks, docM);
        }
        docM.setTitle(pTitle);
        docM.setDescription(pDescription);
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
            docM.setACL(acl);
        }
        Date now = new Date();
        docM.setCreationDate(now);
        docM.setLocation(folder);
        docM.setCheckOutUser(user);
        docM.setCheckOutDate(now);
        firstIte.setCreationDate(now);

        docMDAO.createDocM(docM);
        return new DocumentMaster[]{originalDocM, docM};
    }

    @RolesAllowed("users")
    @Override
    public void subscribeToStateChangeEvent(DocumentMasterKey pDocMPK) throws WorkspaceNotFoundException, NotAllowedException, DocumentMasterNotFoundException, UserNotFoundException, UserNotActiveException {
        User user = userManager.checkWorkspaceReadAccess(pDocMPK.getWorkspaceId());
        DocumentMaster docM = new DocumentMasterDAO(new Locale(user.getLanguage()), em).loadDocM(pDocMPK);
        String owner = docM.getLocation().getOwner();
        if ((owner != null) && (!owner.equals(user.getLogin()))) {
            throw new NotAllowedException(new Locale(user.getLanguage()), "NotAllowedException30");
        }

        new SubscriptionDAO(em).createStateChangeSubscription(new StateChangeSubscription(user, docM));
    }

    @RolesAllowed("users")
    @Override
    public void unsubscribeToStateChangeEvent(DocumentMasterKey pDocMPK) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException {
        User user = userManager.checkWorkspaceReadAccess(pDocMPK.getWorkspaceId());
        SubscriptionKey key = new SubscriptionKey(user.getWorkspaceId(), user.getLogin(), pDocMPK.getWorkspaceId(), pDocMPK.getId(), pDocMPK.getVersion());
        new SubscriptionDAO(em).removeStateChangeSubscription(key);
    }

    @RolesAllowed("users")
    @Override
    public void subscribeToIterationChangeEvent(DocumentMasterKey pDocMPK) throws WorkspaceNotFoundException, NotAllowedException, DocumentMasterNotFoundException, UserNotFoundException, UserNotActiveException {
        User user = userManager.checkWorkspaceReadAccess(pDocMPK.getWorkspaceId());
        DocumentMaster docM = new DocumentMasterDAO(new Locale(user.getLanguage()), em).getDocMRef(pDocMPK);
        String owner = docM.getLocation().getOwner();
        if ((owner != null) && (!owner.equals(user.getLogin()))) {
            throw new NotAllowedException(new Locale(user.getLanguage()), "NotAllowedException30");
        }

        new SubscriptionDAO(em).createIterationChangeSubscription(new IterationChangeSubscription(user, docM));
    }

    @RolesAllowed("users")
    @Override
    public void unsubscribeToIterationChangeEvent(DocumentMasterKey pDocMPK) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException {
        User user = userManager.checkWorkspaceReadAccess(pDocMPK.getWorkspaceId());
        SubscriptionKey key = new SubscriptionKey(user.getWorkspaceId(), user.getLogin(), pDocMPK.getWorkspaceId(), pDocMPK.getId(), pDocMPK.getVersion());
        new SubscriptionDAO(em).removeIterationChangeSubscription(key);
    }

    @RolesAllowed("users")
    @Override
    public User savePersonalInfo(String pWorkspaceId, String pName, String pEmail, String pLanguage) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
        user.setName(pName);
        user.setEmail(pEmail);
        user.setLanguage(pLanguage);
        return user;
    }

    @RolesAllowed("users")
    @Override
    public User[] getUsers(String pWorkspaceId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
        return new UserDAO(new Locale(user.getLanguage()), em).findAllUsers(pWorkspaceId);
    }

    @RolesAllowed("users")
    @Override
    public String[] getTags(String pWorkspaceId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
        Tag[] tags = new TagDAO(new Locale(user.getLanguage()), em).findAllTags(pWorkspaceId);

        String[] labels = new String[tags.length];
        int i = 0;
        for (Tag t : tags) {
            labels[i++] = t.getLabel();
        }
        return labels;
    }

    private Folder checkWritingRight(User pUser, Folder pFolder) throws NotAllowedException {
        if (pFolder.isPrivate() && (!pFolder.getOwner().equals(pUser.getLogin()))) {
            throw new NotAllowedException(new Locale(pUser.getLanguage()), "NotAllowedException33");
        }
        return pFolder;
    }

    private void checkFolderStructureRight(User pUser) throws NotAllowedException {
        Workspace wks = pUser.getWorkspace();
        if (wks.isFolderLocked() && (!pUser.isAdministrator())) {
            throw new NotAllowedException(new Locale(pUser.getLanguage()), "NotAllowedException7");
        }
    }
}
