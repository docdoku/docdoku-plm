/*
 * DocDoku, Professional Open Source
 * Copyright 2006, 2007, 2008, 2009, 2010 DocDoku SARL
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
@Local(ICommandLocal.class)
@Stateless(name = "CommandBean")
@WebService(endpointInterface = "com.docdoku.core.services.ICommandWS")
public class CommandBean implements ICommandWS, ICommandLocal {

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
    private final static Logger LOGGER = Logger.getLogger(CommandBean.class.getName());

    @PostConstruct
    private void init() {
        dataManager = new DataManagerImpl(new File(vaultPath));
    }

    @RolesAllowed("users")
    @Override
    public File saveFileInTemplate(BasicElementKey pMDocTemplateKey, String pName, long pSize) throws WorkspaceNotFoundException, NotAllowedException, MasterDocumentTemplateNotFoundException, FileAlreadyExistsException, UserNotFoundException, UserNotActiveException, CreationException {
        User user = userManager.checkWorkspaceReadAccess(pMDocTemplateKey.getWorkspaceId());
        //TODO checkWorkspaceWriteAccess ?
        if (!NamingConvention.correct(pName)) {
            throw new NotAllowedException(Locale.getDefault(), "NotAllowedException9");
        }

        MasterDocumentTemplateDAO templateDAO = new MasterDocumentTemplateDAO(em);
        MasterDocumentTemplate template = templateDAO.loadMDocTemplate(pMDocTemplateKey);
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
    public File saveFileInDocument(DocumentKey pDocPK, String pName, long pSize) throws WorkspaceNotFoundException, NotAllowedException, MasterDocumentNotFoundException, FileAlreadyExistsException, UserNotFoundException, UserNotActiveException, CreationException {
        User user = userManager.checkWorkspaceReadAccess(pDocPK.getWorkspaceId());
        if (!NamingConvention.correct(pName)) {
            throw new NotAllowedException(Locale.getDefault(), "NotAllowedException9");
        }

        MasterDocumentDAO mdocDAO = new MasterDocumentDAO(em);
        MasterDocument mdoc = mdocDAO.loadMDoc(new MasterDocumentKey(pDocPK.getWorkspaceId(), pDocPK.getMasterDocumentId(), pDocPK.getMasterDocumentVersion()));
        Document document = mdoc.getIteration(pDocPK.getIteration());
        if (mdoc.isCheckedOut() && mdoc.getCheckOutUser().equals(user) && mdoc.getLastIteration().equals(document)) {
            BinaryResource file = null;
            String fullName = mdoc.getWorkspaceId() + "/documents/" + mdoc.getId() + "/" + mdoc.getVersion() + "/" + document.getIteration() + "/" + pName;

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

    @RolesAllowed("users")
    @Override
    public File getDataFile(String pFullName) throws WorkspaceNotFoundException, NotAllowedException, FileNotFoundException, UserNotFoundException, UserNotActiveException {
        User user = userManager.checkWorkspaceReadAccess(BinaryResource.parseWorkspaceId(pFullName));

        BinaryResourceDAO binDAO = new BinaryResourceDAO(new Locale(user.getLanguage()), em);
        BinaryResource file = binDAO.loadBinaryResource(pFullName);

        Document document = binDAO.getDocumentOwner(file);
        if (document != null) {
            MasterDocument mdoc = document.getMasterDocument();
            String owner = mdoc.getLocation().getOwner();

            if (((owner != null) && (!owner.equals(user.getLogin()))) || (mdoc.isCheckedOut() && !mdoc.getCheckOutUser().equals(user) && mdoc.getLastIteration().equals(document))) {
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
    public String[] getFolders(String pCompletePath) throws WorkspaceNotFoundException, FolderNotFoundException, UserNotFoundException, UserNotActiveException {
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
    public MasterDocument[] findMDocsByFolder(String pCompletePath) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException {
        String workspaceId = Folder.parseWorkspaceId(pCompletePath);
        User user = userManager.checkWorkspaceReadAccess(workspaceId);
        List<MasterDocument> mdocs = new MasterDocumentDAO(new Locale(user.getLanguage()), em).findMDocsByFolder(pCompletePath);
        ListIterator<MasterDocument> ite = mdocs.listIterator();
        Workspace wks = new WorkspaceDAO(new Locale(user.getLanguage()), em).loadWorkspace(workspaceId);
        boolean isAdmin = wks.getAdmin().getLogin().equals(user.getLogin());
        while (ite.hasNext()) {
            MasterDocument mdoc = ite.next();
            if (!isAdmin && mdoc.getACL() != null && !mdoc.getACL().hasReadAccess(user)) {
                ite.remove();
                continue;
            }
            if ((mdoc.isCheckedOut()) && (!mdoc.getCheckOutUser().equals(user))) {
                mdoc = mdoc.clone();
                mdoc.removeLastIteration();
                ite.set(mdoc);

            }
        }
        return mdocs.toArray(new MasterDocument[mdocs.size()]);
    }

    @RolesAllowed("users")
    @Override
    public MasterDocument[] findMDocsByTag(TagKey pKey) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException {
        String workspaceId = pKey.getWorkspaceId();
        User user = userManager.checkWorkspaceReadAccess(workspaceId);
        List<MasterDocument> mdocs = new MasterDocumentDAO(new Locale(user.getLanguage()), em).findMDocsByTag(new Tag(user.getWorkspace(), pKey.getLabel()));
        ListIterator<MasterDocument> ite = mdocs.listIterator();
        Workspace wks = new WorkspaceDAO(new Locale(user.getLanguage()), em).loadWorkspace(workspaceId);
        boolean isAdmin = wks.getAdmin().getLogin().equals(user.getLogin());
        while (ite.hasNext()) {
            MasterDocument mdoc = ite.next();
            if (!isAdmin && mdoc.getACL() != null && !mdoc.getACL().hasReadAccess(user)) {
                ite.remove();
                continue;
            }
            if ((mdoc.isCheckedOut()) && (!mdoc.getCheckOutUser().equals(user))) {
                mdoc = mdoc.clone();
                mdoc.removeLastIteration();
                ite.set(mdoc);

            }
        }
        return mdocs.toArray(new MasterDocument[mdocs.size()]);
    }

    @RolesAllowed("users")
    @Override
    public MasterDocument getMDoc(MasterDocumentKey pMDocPK) throws WorkspaceNotFoundException, MasterDocumentNotFoundException, NotAllowedException, UserNotFoundException, UserNotActiveException {
        User user = userManager.checkWorkspaceReadAccess(pMDocPK.getWorkspaceId());
        MasterDocument mdoc = new MasterDocumentDAO(new Locale(user.getLanguage()), em).loadMDoc(pMDocPK);
        String owner = mdoc.getLocation().getOwner();
        if ((owner != null) && (!owner.equals(user.getLogin()))) {
            throw new NotAllowedException(new Locale(user.getLanguage()), "NotAllowedException5");
        }

        if ((mdoc.isCheckedOut()) && (!mdoc.getCheckOutUser().equals(user))) {
            mdoc = mdoc.clone();
            mdoc.removeLastIteration();
        }
        return mdoc;
    }

    @RolesAllowed("users")
    @Override
    public MasterDocument[] getCheckedOutMDocs(String pWorkspaceId) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
        List<MasterDocument> mdocs = new MasterDocumentDAO(new Locale(user.getLanguage()), em).findCheckedOutMDocs(user);
        return mdocs.toArray(new MasterDocument[mdocs.size()]);
    }

    @RolesAllowed("users")
    @Override
    public Task[] getTasks(String pWorkspaceId) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
        return new TaskDAO(new Locale(user.getLanguage()), em).findTasks(user);
    }

    @RolesAllowed("users")
    @Override
    public MasterDocumentKey[] getIterationChangeEventSubscriptions(String pWorkspaceId) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
        return new SubscriptionDAO(em).getIterationChangeEventSubscriptions(user);
    }

    @RolesAllowed("users")
    @Override
    public MasterDocumentKey[] getStateChangeEventSubscriptions(String pWorkspaceId) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
        return new SubscriptionDAO(em).getStateChangeEventSubscriptions(user);
    }

    @RolesAllowed("users")
    @Override
    public String generateId(String pWorkspaceId, String pMDocTemplateId) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException, MasterDocumentTemplateNotFoundException {

        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
        MasterDocumentTemplate template = new MasterDocumentTemplateDAO(new Locale(user.getLanguage()), em).loadMDocTemplate(new BasicElementKey(user.getWorkspaceId(), pMDocTemplateId));

        String newId = null;
        try {
            String latestId = new MasterDocumentDAO(new Locale(user.getLanguage()), em).findLatestMDocId(pWorkspaceId, template.getDocumentType());
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
    public MasterDocument[] searchMDocs(SearchQuery pQuery) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException {
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

        List<MasterDocument> fetchedMDocs = new MasterDocumentDAO(new Locale(user.getLanguage()), em).searchMDocs(pQuery.getWorkspaceId(), pQuery.getMDocId(), pQuery.getTitle(), pQuery.getVersion(), pQuery.getAuthor(), pQuery.getType(), pQuery.getCreationDateFrom(),
                pQuery.getCreationDateTo(), tags, pQuery.getAttributes() != null ? Arrays.asList(pQuery.getAttributes()) : null);

        //preparing fulltext filtering
        Set<MasterDocumentKey> indexedKeys = null;
        if (fetchedMDocs.size() > 0 && pQuery.getContent() != null && !pQuery.getContent().equals("")) {
            indexedKeys = indexSearcher.searchInIndex(pQuery.getWorkspaceId(), pQuery.getContent());
        }

        Workspace wks = new WorkspaceDAO(new Locale(user.getLanguage()), em).loadWorkspace(pQuery.getWorkspaceId());
        boolean isAdmin = wks.getAdmin().getLogin().equals(user.getLogin());

        ListIterator<MasterDocument> ite = fetchedMDocs.listIterator();
        mdocBlock:
        while (ite.hasNext()) {
            MasterDocument mdoc = ite.next();
            if (indexedKeys != null && (!indexedKeys.contains(mdoc.getKey()))) {
                ite.remove();
                continue mdocBlock;
            }

            //TODO search should not fetch back private mdoc
            if ((mdoc.isCheckedOut()) && (!mdoc.getCheckOutUser().equals(user))) {
                mdoc = mdoc.clone();
                mdoc.removeLastIteration();
                ite.set(mdoc);
            }

            //Check acess rights
            if (!isAdmin && mdoc.getACL() != null && !mdoc.getACL().hasReadAccess(user)) {
                ite.remove();
                continue;
            }
        }
        return fetchedMDocs.toArray(new MasterDocument[fetchedMDocs.size()]);
    }

    @RolesAllowed("users")
    @Override
    public WorkflowModel[] getWorkflowModels(String pWorkspaceId) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
        return new WorkflowModelDAO(new Locale(user.getLanguage()), em).findAllWorkflowModels(pWorkspaceId);
    }

    @RolesAllowed("users")
    @Override
    public MasterDocumentTemplate[] getMDocTemplates(String pWorkspaceId) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
        return new MasterDocumentTemplateDAO(new Locale(user.getLanguage()), em).findAllMDocTemplates(pWorkspaceId);
    }

    @RolesAllowed("users")
    @Override
    public WorkflowModel getWorkflowModel(BasicElementKey pKey)
            throws WorkspaceNotFoundException, WorkflowModelNotFoundException, UserNotFoundException, UserNotActiveException {
        User user = userManager.checkWorkspaceReadAccess(pKey.getWorkspaceId());
        return new WorkflowModelDAO(new Locale(user.getLanguage()), em).loadWorkflowModel(pKey);
    }

    @RolesAllowed("users")
    @Override
    public MasterDocumentTemplate getMDocTemplate(BasicElementKey pKey)
            throws WorkspaceNotFoundException, MasterDocumentTemplateNotFoundException, UserNotFoundException, UserNotActiveException {
        User user = userManager.checkWorkspaceReadAccess(pKey.getWorkspaceId());
        return new MasterDocumentTemplateDAO(new Locale(user.getLanguage()), em).loadMDocTemplate(pKey);
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
    public MasterDocumentTemplate updateMDocTemplate(BasicElementKey pKey, String pDocumentType, String pMask, InstanceAttributeTemplate[] pAttributeTemplates, boolean idGenerated) throws WorkspaceNotFoundException, WorkspaceNotFoundException, AccessRightException, MasterDocumentTemplateNotFoundException, UserNotFoundException {
        User user = userManager.checkWorkspaceWriteAccess(pKey.getWorkspaceId());

        MasterDocumentTemplateDAO templateDAO = new MasterDocumentTemplateDAO(new Locale(user.getLanguage()), em);
        MasterDocumentTemplate template = templateDAO.loadMDocTemplate(pKey);
        Date now = new Date();
        template.setCreationDate(now);
        template.setAuthor(user);
        template.setDocumentType(pDocumentType);
        template.setMask(pMask);
        template.setIdGenerated(idGenerated);

        Set<InstanceAttributeTemplate> attrs = new HashSet<InstanceAttributeTemplate>();
        for (InstanceAttributeTemplate attr : pAttributeTemplates) {
            attr.setMasterDocumentTemplate(template);
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
    public void delWorkflowModel(BasicElementKey pKey) throws WorkspaceNotFoundException, AccessRightException, WorkflowModelNotFoundException, UserNotFoundException {
        User user = userManager.checkWorkspaceWriteAccess(pKey.getWorkspaceId());
        new WorkflowModelDAO(new Locale(user.getLanguage()), em).removeWorkflowModel(pKey);
    }

    @RolesAllowed("users")
    @Override
    public void delTag(TagKey pKey) throws WorkspaceNotFoundException, AccessRightException, TagNotFoundException, UserNotFoundException {
        User user = userManager.checkWorkspaceWriteAccess(pKey.getWorkspaceId());
        Locale userLocale = new Locale(user.getLanguage());
        Tag tagToRemove = new Tag(user.getWorkspace(), pKey.getLabel());
        List<MasterDocument> mdocs = new MasterDocumentDAO(userLocale, em).findMDocsByTag(tagToRemove);
        for (MasterDocument mdoc : mdocs) {
            mdoc.getTags().remove(tagToRemove);
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
    public MasterDocument createMDoc(String pParentFolder,
            String pMDocID, String pTitle, String pDescription, String pMDocTemplateId, String pWorkflowModelId, ACLUserEntry[] pACLUserEntries, ACLUserGroupEntry[] pACLUserGroupEntries) throws WorkspaceNotFoundException, WorkflowModelNotFoundException, NotAllowedException, MasterDocumentTemplateNotFoundException, AccessRightException, MasterDocumentAlreadyExistsException, FolderNotFoundException, FileAlreadyExistsException, UserNotFoundException, CreationException {
        User user = userManager.checkWorkspaceWriteAccess(Folder.parseWorkspaceId(pParentFolder));
        if (!NamingConvention.correct(pMDocID)) {
            throw new NotAllowedException(new Locale(user.getLanguage()), "NotAllowedException9");
        }
        Folder folder = new FolderDAO(new Locale(user.getLanguage()), em).loadFolder(pParentFolder);
        checkWritingRight(user, folder);

        MasterDocument mdoc;
        Document newDoc;

        if (pMDocTemplateId == null) {
            mdoc = new MasterDocument(user.getWorkspace(), pMDocID, user);
            newDoc = mdoc.createNextIteration(user);
            //specify an empty type instead of null
            //so the search will find it with the % character
            mdoc.setType("");
        } else {
            MasterDocumentTemplate template = new MasterDocumentTemplateDAO(new Locale(user.getLanguage()), em).loadMDocTemplate(new BasicElementKey(user.getWorkspaceId(), pMDocTemplateId));
            mdoc = new MasterDocument(user.getWorkspace(), pMDocID, user);
            mdoc.setType(template.getDocumentType());
            newDoc = mdoc.createNextIteration(user);

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

                String fullName = mdoc.getWorkspaceId() + "/documents/" + mdoc.getId() + "/A/1/" + fileName;
                BinaryResource targetFile = new BinaryResource(fullName, length);
                binDAO.createBinaryResource(targetFile);

                newDoc.addFile(targetFile);
                dataManager.copyData(sourceFile, targetFile);
            }
        }

        if (pWorkflowModelId != null) {
            WorkflowModel workflowModel = new WorkflowModelDAO(new Locale(user.getLanguage()), em).loadWorkflowModel(new BasicElementKey(user.getWorkspaceId(), pWorkflowModelId));
            Workflow workflow = workflowModel.createWorkflow();
            mdoc.setWorkflow(workflow);

            Collection<Task> runningTasks = workflow.getRunningTasks();
            for (Task runningTask : runningTasks) {
                runningTask.start();
            }
            mailer.sendApproval(runningTasks, mdoc);
        }

        mdoc.setTitle(pTitle);
        mdoc.setDescription(pDescription);

        if ((pACLUserEntries != null && pACLUserEntries.length > 0) || (pACLUserGroupEntries != null && pACLUserGroupEntries.length > 0)) {
            ACL acl = new ACL();
            if (pACLUserEntries != null) {
                for (ACLUserEntry entry : pACLUserEntries) {
                    acl.addEntry(em.getReference(User.class, new UserKey(user.getWorkspaceId(), entry.getPrincipalLogin())), entry.getPermission());
                }
            }

            if (pACLUserGroupEntries != null) {
                for (ACLUserGroupEntry entry : pACLUserGroupEntries) {
                    acl.addEntry(em.getReference(UserGroup.class, new BasicElementKey(user.getWorkspaceId(), entry.getPrincipalId())), entry.getPermission());
                }
            }
            mdoc.setACL(acl);
        }
        Date now = new Date();
        mdoc.setCreationDate(now);
        mdoc.setLocation(folder);
        mdoc.setCheckOutUser(user);
        mdoc.setCheckOutDate(now);
        newDoc.setCreationDate(now);
        MasterDocumentDAO mdocDAO = new MasterDocumentDAO(new Locale(user.getLanguage()), em);

        mdocDAO.createMDoc(mdoc);
        return mdoc;
    }

    @RolesAllowed("users")
    @Override
    public MasterDocumentTemplate createMDocTemplate(String pWorkspaceId, String pId, String pDocumentType,
            String pMask, InstanceAttributeTemplate[] pAttributeTemplates, boolean idGenerated) throws WorkspaceNotFoundException, AccessRightException, MasterDocumentTemplateAlreadyExistsException, UserNotFoundException, NotAllowedException, CreationException {
        User user = userManager.checkWorkspaceWriteAccess(pWorkspaceId);
        if (!NamingConvention.correct(pId)) {
            throw new NotAllowedException(new Locale(user.getLanguage()), "NotAllowedException9");
        }
        MasterDocumentTemplate template = new MasterDocumentTemplate(user.getWorkspace(), pId, user, pDocumentType, pMask);
        Date now = new Date();
        template.setCreationDate(now);
        template.setIdGenerated(idGenerated);

        Set<InstanceAttributeTemplate> attrs = new HashSet<InstanceAttributeTemplate>();
        for (InstanceAttributeTemplate attr : pAttributeTemplates) {
            attr.setMasterDocumentTemplate(template);
            attrs.add(attr);
        }
        template.setAttributeTemplates(attrs);

        new MasterDocumentTemplateDAO(new Locale(user.getLanguage()), em).createMDocTemplate(template);
        return template;
    }

    @RolesAllowed("users")
    @Override
    public MasterDocument moveMDoc(String pParentFolder, MasterDocumentKey pMDocPK) throws WorkspaceNotFoundException, MasterDocumentNotFoundException, NotAllowedException, AccessRightException, FolderNotFoundException, UserNotFoundException, UserNotActiveException {
        //TODO security check if both parameter belong to the same workspace
        User user = userManager.checkWorkspaceWriteAccess(pMDocPK.getWorkspaceId());
        Folder newLocation = new FolderDAO(new Locale(user.getLanguage()), em).loadFolder(pParentFolder);
        checkWritingRight(user, newLocation);
        MasterDocumentDAO mdocDAO = new MasterDocumentDAO(new Locale(user.getLanguage()), em);
        MasterDocument mdoc = mdocDAO.loadMDoc(pMDocPK);
        //Check access rights on mdoc
        Workspace wks = new WorkspaceDAO(new Locale(user.getLanguage()), em).loadWorkspace(pMDocPK.getWorkspaceId());
        boolean isAdmin = wks.getAdmin().getLogin().equals(user.getLogin());
        if (!isAdmin && mdoc.getACL() != null && !mdoc.getACL().hasWriteAccess(user)) {
            throw new AccessRightException(new Locale(user.getLanguage()), user);
        }
        Folder oldLocation = mdoc.getLocation();
        String owner = oldLocation.getOwner();
        if ((owner != null) && (!owner.equals(user.getLogin()))) {
            throw new NotAllowedException(new Locale(user.getLanguage()), "NotAllowedException6");
        } else {
            mdoc.setLocation(newLocation);
            if ((mdoc.isCheckedOut()) && (!mdoc.getCheckOutUser().equals(user))) {
                mdoc = mdoc.clone();
                mdoc.removeLastIteration();
            }
            return mdoc;
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
    @Override
    public MasterDocument approve(String pWorkspaceId, TaskKey pTaskKey, String pComment)
            throws WorkspaceNotFoundException, TaskNotFoundException, NotAllowedException, UserNotFoundException, UserNotActiveException {
        //TODO no check is made that pTaskKey is from the same workspace than pWorkspaceId
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);

        Task task = new TaskDAO(new Locale(user.getLanguage()), em).loadTask(pTaskKey);
        Workflow workflow = task.getActivity().getWorkflow();
        MasterDocument mdoc = new WorkflowDAO(em).getTarget(workflow);


        if (!task.getWorker().equals(user)) {
            throw new NotAllowedException(new Locale(user.getLanguage()), "NotAllowedException14");
        }

        if (!workflow.getRunningTasks().contains(task)) {
            throw new NotAllowedException(new Locale(user.getLanguage()), "NotAllowedException15");
        }

        if (mdoc.isCheckedOut()) {
            throw new NotAllowedException(new Locale(user.getLanguage()), "NotAllowedException16");
        }

        int previousStep = workflow.getCurrentStep();
        task.approve(pComment, mdoc.getLastIteration().getIteration());
        int currentStep = workflow.getCurrentStep();

        User[] subscribers = new SubscriptionDAO(em).getStateChangeEventSubscribers(mdoc);

        if (previousStep != currentStep && subscribers.length != 0) {
            mailer.sendStateNotification(subscribers, mdoc);
        }

        Collection<Task> runningTasks = workflow.getRunningTasks();
        for (Task runningTask : runningTasks) {
            runningTask.start();
        }
        mailer.sendApproval(runningTasks, mdoc);
        return mdoc;
    }

    @RolesAllowed("users")
    @Override
    public MasterDocument reject(String pWorkspaceId, TaskKey pTaskKey, String pComment)
            throws WorkspaceNotFoundException, TaskNotFoundException, NotAllowedException, UserNotFoundException, UserNotActiveException {
        //TODO no check is made that pTaskKey is from the same workspace than pWorkspaceId
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);

        Task task = new TaskDAO(new Locale(user.getLanguage()), em).loadTask(pTaskKey);
        Workflow workflow = task.getActivity().getWorkflow();
        MasterDocument mdoc = new WorkflowDAO(em).getTarget(workflow);

        if (!task.getWorker().equals(user)) {
            throw new NotAllowedException(new Locale(user.getLanguage()), "NotAllowedException14");
        }

        if (!workflow.getRunningTasks().contains(task)) {
            throw new NotAllowedException(new Locale(user.getLanguage()), "NotAllowedException15");
        }

        if (mdoc.isCheckedOut()) {
            throw new NotAllowedException(new Locale(user.getLanguage()), "NotAllowedException16");
        }

        task.reject(pComment, mdoc.getLastIteration().getIteration());
        return mdoc;
    }

    @RolesAllowed("users")
    @Override
    public MasterDocument checkOut(MasterDocumentKey pMDocPK)
            throws WorkspaceNotFoundException, NotAllowedException, MasterDocumentNotFoundException, AccessRightException, FileAlreadyExistsException, UserNotFoundException, CreationException {
        User user = userManager.checkWorkspaceWriteAccess(pMDocPK.getWorkspaceId());
        MasterDocumentDAO mdocDAO = new MasterDocumentDAO(new Locale(user.getLanguage()), em);
        MasterDocument mdoc = mdocDAO.loadMDoc(pMDocPK);
        //Check access rights on mdoc
        Workspace wks = new WorkspaceDAO(new Locale(user.getLanguage()), em).loadWorkspace(pMDocPK.getWorkspaceId());
        boolean isAdmin = wks.getAdmin().getLogin().equals(user.getLogin());
        if (!isAdmin && mdoc.getACL() != null && !mdoc.getACL().hasWriteAccess(user)) {
            throw new AccessRightException(new Locale(user.getLanguage()), user);
        }
        String owner = mdoc.getLocation().getOwner();
        if ((owner != null) && (!owner.equals(user.getLogin()))) {
            throw new NotAllowedException(new Locale(user.getLanguage()), "NotAllowedException5");
        }

        if (mdoc.isCheckedOut()) {
            throw new NotAllowedException(new Locale(user.getLanguage()), "NotAllowedException37");
        }

        Document beforeLastDocument = mdoc.getLastIteration();

        Document newDoc = mdoc.createNextIteration(user);
        //We persist the doc as a workaround for a bug which was introduced
        //since glassfish 3 that set the DTYPE to null in the instance attribute table
        em.persist(newDoc);
        Date now = new Date();
        newDoc.setCreationDate(now);
        mdoc.setCheckOutUser(user);
        mdoc.setCheckOutDate(now);

        if (beforeLastDocument != null) {
            BinaryResourceDAO binDAO = new BinaryResourceDAO(new Locale(user.getLanguage()), em);
            for (BinaryResource sourceFile : beforeLastDocument.getAttachedFiles()) {
                String fileName = sourceFile.getName();
                long length = sourceFile.getContentLength();
                String fullName = mdoc.getWorkspaceId() + "/documents/" + mdoc.getId() + "/" + mdoc.getVersion() + "/" + newDoc.getIteration() + "/" + fileName;
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

        return mdoc;
    }

    @RolesAllowed("users")
    @Override
    public MasterDocument saveTags(MasterDocumentKey pMDocPK, String[] pTags)
            throws WorkspaceNotFoundException, NotAllowedException, MasterDocumentNotFoundException, AccessRightException, UserNotFoundException {
        User user = userManager.checkWorkspaceWriteAccess(pMDocPK.getWorkspaceId());
        Locale userLocale = new Locale(user.getLanguage());
        MasterDocumentDAO mdocDAO = new MasterDocumentDAO(userLocale, em);
        MasterDocument mdoc = mdocDAO.loadMDoc(pMDocPK);
        String owner = mdoc.getLocation().getOwner();
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

        mdoc.setTags(tags);

        if ((mdoc.isCheckedOut()) && (!mdoc.getCheckOutUser().equals(user))) {
            mdoc = mdoc.clone();
            mdoc.removeLastIteration();
        }
        return mdoc;
    }

    @RolesAllowed("users")
    @Override
    public MasterDocument undoCheckOut(MasterDocumentKey pMDocPK)
            throws WorkspaceNotFoundException, MasterDocumentNotFoundException, NotAllowedException, UserNotFoundException, UserNotActiveException {
        User user = userManager.checkWorkspaceReadAccess(pMDocPK.getWorkspaceId());
        MasterDocumentDAO mdocDAO = new MasterDocumentDAO(new Locale(user.getLanguage()), em);
        MasterDocument mdoc = mdocDAO.loadMDoc(pMDocPK);
        if (mdoc.isCheckedOut() && mdoc.getCheckOutUser().equals(user)) {
            Document doc = mdoc.removeLastIteration();
            for (BinaryResource file : doc.getAttachedFiles()) {
                dataManager.delData(file);
            }

            DocumentDAO docDAO = new DocumentDAO(em);
            docDAO.removeDoc(doc);
            mdoc.setCheckOutDate(null);
            mdoc.setCheckOutUser(null);
            return mdoc;
        } else {
            throw new NotAllowedException(new Locale(user.getLanguage()), "NotAllowedException19");
        }
    }

    @RolesAllowed("users")
    @Override
    public MasterDocument checkIn(MasterDocumentKey pMDocPK)
            throws WorkspaceNotFoundException, NotAllowedException, MasterDocumentNotFoundException, AccessRightException, UserNotFoundException {
        User user = userManager.checkWorkspaceWriteAccess(pMDocPK.getWorkspaceId());
        MasterDocumentDAO mdocDAO = new MasterDocumentDAO(new Locale(user.getLanguage()), em);
        MasterDocument mdoc = mdocDAO.loadMDoc(pMDocPK);
        //Check access rights on mdoc
        Workspace wks = new WorkspaceDAO(new Locale(user.getLanguage()), em).loadWorkspace(pMDocPK.getWorkspaceId());
        boolean isAdmin = wks.getAdmin().getLogin().equals(user.getLogin());
        if (!isAdmin && mdoc.getACL() != null && !mdoc.getACL().hasWriteAccess(user)) {
            throw new AccessRightException(new Locale(user.getLanguage()), user);
        }
        if (mdoc.isCheckedOut() && mdoc.getCheckOutUser().equals(user)) {
            User[] subscribers = new SubscriptionDAO(em).getIterationChangeEventSubscribers(mdoc);

            mdoc.setCheckOutDate(null);
            mdoc.setCheckOutUser(null);

            if (subscribers.length != 0) {
                mailer.sendIterationNotification(subscribers, mdoc);
            }

            for (BinaryResource bin : mdoc.getLastIteration().getAttachedFiles()) {
                File physicalFile = dataManager.getDataFile(bin);
                indexer.addToIndex(bin.getFullName(), physicalFile.getPath());
            }

            return mdoc;
        } else {
            throw new NotAllowedException(new Locale(user.getLanguage()), "NotAllowedException20");
        }
    }

    @RolesAllowed("users")
    @Override
    public MasterDocumentKey[] delFolder(String pCompletePath)
            throws WorkspaceNotFoundException, NotAllowedException, AccessRightException, UserNotFoundException, FolderNotFoundException {
        User user = userManager.checkWorkspaceWriteAccess(Folder.parseWorkspaceId(pCompletePath));
        FolderDAO folderDAO = new FolderDAO(new Locale(user.getLanguage()), em);
        Folder folder = folderDAO.loadFolder(pCompletePath);
        String owner = folder.getOwner();
        checkFolderStructureRight(user);
        if (((owner != null) && (!owner.equals(user.getLogin()))) || (folder.isRoot()) || folder.isHome()) {
            throw new NotAllowedException(new Locale(user.getLanguage()), "NotAllowedException21");
        } else {
            List<MasterDocument> mdocs = folderDAO.removeFolder(folder);
            MasterDocumentKey[] pks = new MasterDocumentKey[mdocs.size()];
            int i = 0;
            for (MasterDocument mdoc : mdocs) {
                pks[i++] = mdoc.getKey();
                for (Document doc : mdoc.getDocumentIterations()) {
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
    public MasterDocumentKey[] moveFolder(String pCompletePath, String pDestParentFolder, String pDestFolder)
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
            List<MasterDocument> mdocs = folderDAO.moveFolder(folder, newFolder);
            MasterDocumentKey[] pks = new MasterDocumentKey[mdocs.size()];
            int i = 0;
            for (MasterDocument mdoc : mdocs) {
                pks[i++] = mdoc.getKey();
            }
            return pks;
        }
    }

    @RolesAllowed("users")
    @Override
    public void delMDoc(MasterDocumentKey pMDocPK)
            throws WorkspaceNotFoundException, NotAllowedException, MasterDocumentNotFoundException, AccessRightException, UserNotFoundException {
        User user = userManager.checkWorkspaceWriteAccess(pMDocPK.getWorkspaceId());
        MasterDocumentDAO mdocDAO = new MasterDocumentDAO(new Locale(user.getLanguage()), em);
        MasterDocument mdoc = mdocDAO.loadMDoc(pMDocPK);
        //Check access rights on
        Workspace wks = new WorkspaceDAO(new Locale(user.getLanguage()), em).loadWorkspace(pMDocPK.getWorkspaceId());
        boolean isAdmin = wks.getAdmin().getLogin().equals(user.getLogin());
        if (!isAdmin && mdoc.getACL() != null && !mdoc.getACL().hasWriteAccess(user)) {
            throw new AccessRightException(new Locale(user.getLanguage()), user);
        }
        String owner = mdoc.getLocation().getOwner();
        if ((owner != null) && (!owner.equals(user.getLogin()))) {
            throw new NotAllowedException(new Locale(user.getLanguage()), "NotAllowedException22");
        }

        mdocDAO.removeMDoc(mdoc);

        for (Document doc : mdoc.getDocumentIterations()) {
            for (BinaryResource file : doc.getAttachedFiles()) {
                indexer.removeFromIndex(file.getFullName());
                dataManager.delData(file);
            }
        }
    }

    @RolesAllowed("users")
    @Override
    public void delMDocTemplate(BasicElementKey pKey)
            throws WorkspaceNotFoundException, AccessRightException, MasterDocumentTemplateNotFoundException, UserNotFoundException {
        User user = userManager.checkWorkspaceWriteAccess(pKey.getWorkspaceId());
        MasterDocumentTemplateDAO templateDAO = new MasterDocumentTemplateDAO(new Locale(user.getLanguage()), em);
        MasterDocumentTemplate template = templateDAO.removeMDocTemplate(pKey);
        for (BinaryResource file : template.getAttachedFiles()) {
            dataManager.delData(file);
        }
    }

    @RolesAllowed("users")
    @Override
    public MasterDocument removeFileFromDocument(String pFullName) throws WorkspaceNotFoundException, MasterDocumentNotFoundException, NotAllowedException, AccessRightException, FileNotFoundException, UserNotFoundException, UserNotActiveException {
        User user = userManager.checkWorkspaceReadAccess(BinaryResource.parseWorkspaceId(pFullName));

        BinaryResourceDAO binDAO = new BinaryResourceDAO(new Locale(user.getLanguage()), em);
        BinaryResource file = binDAO.loadBinaryResource(pFullName);

        Document document = binDAO.getDocumentOwner(file);
        MasterDocument mdoc = document.getMasterDocument();
        //check access rights on mdoc ?
        if (mdoc.isCheckedOut() && mdoc.getCheckOutUser().equals(user) && mdoc.getLastIteration().equals(document)) {
            dataManager.delData(file);
            document.removeFile(file);
            binDAO.removeBinaryResource(file);
            return mdoc;
        } else {
            throw new NotAllowedException(new Locale(user.getLanguage()), "NotAllowedException24");
        }
    }

    @RolesAllowed("users")
    @Override
    public MasterDocumentTemplate removeFileFromTemplate(String pFullName) throws WorkspaceNotFoundException, MasterDocumentTemplateNotFoundException, AccessRightException, FileNotFoundException, UserNotFoundException, UserNotActiveException {
        User user = userManager.checkWorkspaceReadAccess(BinaryResource.parseWorkspaceId(pFullName));
        //TODO checkWorkspaceWriteAccess ?
        BinaryResourceDAO binDAO = new BinaryResourceDAO(new Locale(user.getLanguage()), em);
        BinaryResource file = binDAO.loadBinaryResource(pFullName);

        MasterDocumentTemplate template = binDAO.getTemplateOwner(file);
        dataManager.delData(file);
        template.removeFile(file);
        binDAO.removeBinaryResource(file);
        return template;
    }

    @RolesAllowed("users")
    @Override
    public MasterDocument updateDoc(DocumentKey pKey, String pRevisionNote, InstanceAttribute[] pAttributes, DocumentKey[] pLinkKeys) throws WorkspaceNotFoundException, NotAllowedException, MasterDocumentNotFoundException, AccessRightException, UserNotFoundException {
        User user = userManager.checkWorkspaceWriteAccess(pKey.getWorkspaceId());
        MasterDocumentDAO mdocDAO = new MasterDocumentDAO(new Locale(user.getLanguage()), em);
        MasterDocument mdoc = mdocDAO.loadMDoc(new MasterDocumentKey(pKey.getWorkspaceId(), pKey.getMasterDocumentId(), pKey.getMasterDocumentVersion()));
        //check access rights on mdoc ?
        if (mdoc.isCheckedOut() && mdoc.getCheckOutUser().equals(user) && mdoc.getLastIteration().getKey().equals(pKey)) {
            Document doc = mdoc.getLastIteration();
            Set<DocumentToDocumentLink> links = new HashSet<DocumentToDocumentLink>();
            for (DocumentKey key : pLinkKeys) {
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
            return mdoc;

        } else {
            throw new NotAllowedException(new Locale(user.getLanguage()), "NotAllowedException25");
        }

    }

    @RolesAllowed("users")
    @Override
    public MasterDocument[] createVersion(MasterDocumentKey pOriginalMDocPK,
            String pTitle, String pDescription, String pWorkflowModelId, ACLUserEntry[] pACLUserEntries, ACLUserGroupEntry[] pACLUserGroupEntries) throws WorkspaceNotFoundException, NotAllowedException, MasterDocumentNotFoundException, WorkflowModelNotFoundException, AccessRightException, MasterDocumentAlreadyExistsException, FileAlreadyExistsException, UserNotFoundException, CreationException {
        User user = userManager.checkWorkspaceWriteAccess(pOriginalMDocPK.getWorkspaceId());
        MasterDocumentDAO mdocDAO = new MasterDocumentDAO(new Locale(user.getLanguage()), em);
        MasterDocument originalMDoc = mdocDAO.loadMDoc(pOriginalMDocPK);
        Folder folder = originalMDoc.getLocation();
        checkWritingRight(user, folder);

        if (originalMDoc.isCheckedOut()) {
            throw new NotAllowedException(new Locale(user.getLanguage()), "NotAllowedException26");
        }

        if (originalMDoc.getNumberOfIterations() == 0) {
            throw new NotAllowedException(new Locale(user.getLanguage()), "NotAllowedException27");
        }

        Version version = new Version(originalMDoc.getVersion());
        version.increase();
        MasterDocument mdoc = new MasterDocument(originalMDoc.getWorkspace(), originalMDoc.getId(), version, user);
        mdoc.setType(originalMDoc.getType());
        //create the first iteration which is a copy of the last one of the original mdoc
        //of course we duplicate the iteration only if it exists !
        Document lastDoc = originalMDoc.getLastIteration();
        Document firstIte = mdoc.createNextIteration(user);
        if (lastDoc != null) {
            BinaryResourceDAO binDAO = new BinaryResourceDAO(new Locale(user.getLanguage()), em);
            for (BinaryResource sourceFile : lastDoc.getAttachedFiles()) {
                String fileName = sourceFile.getName();
                long length = sourceFile.getContentLength();
                String fullName = mdoc.getWorkspaceId() + "/documents/" + mdoc.getId() + "/" + version + "/1/" + fileName;
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
            WorkflowModel workflowModel = new WorkflowModelDAO(new Locale(user.getLanguage()), em).loadWorkflowModel(new BasicElementKey(user.getWorkspaceId(), pWorkflowModelId));
            Workflow workflow = workflowModel.createWorkflow();
            mdoc.setWorkflow(workflow);

            Collection<Task> runningTasks = workflow.getRunningTasks();
            for (Task runningTask : runningTasks) {
                runningTask.start();
            }
            mailer.sendApproval(runningTasks, mdoc);
        }
        mdoc.setTitle(pTitle);
        mdoc.setDescription(pDescription);
        if ((pACLUserEntries != null && pACLUserEntries.length > 0) || (pACLUserGroupEntries != null && pACLUserGroupEntries.length > 0)) {
            ACL acl = new ACL();
            if (pACLUserEntries != null) {
                for (ACLUserEntry entry : pACLUserEntries) {
                    acl.addEntry(em.getReference(User.class, new UserKey(user.getWorkspaceId(), entry.getPrincipalLogin())), entry.getPermission());
                }
            }

            if (pACLUserGroupEntries != null) {
                for (ACLUserGroupEntry entry : pACLUserGroupEntries) {
                    acl.addEntry(em.getReference(UserGroup.class, new BasicElementKey(user.getWorkspaceId(), entry.getPrincipalId())), entry.getPermission());
                }
            }
            mdoc.setACL(acl);
        }
        Date now = new Date();
        mdoc.setCreationDate(now);
        mdoc.setLocation(folder);
        mdoc.setCheckOutUser(user);
        mdoc.setCheckOutDate(now);
        firstIte.setCreationDate(now);

        mdocDAO.createMDoc(mdoc);
        return new MasterDocument[]{originalMDoc, mdoc};
    }

    @RolesAllowed("users")
    @Override
    public void subscribeToStateChangeEvent(MasterDocumentKey pMDocPK) throws WorkspaceNotFoundException, NotAllowedException, MasterDocumentNotFoundException, UserNotFoundException, UserNotActiveException {
        User user = userManager.checkWorkspaceReadAccess(pMDocPK.getWorkspaceId());
        MasterDocument mdoc = new MasterDocumentDAO(new Locale(user.getLanguage()), em).loadMDoc(pMDocPK);
        String owner = mdoc.getLocation().getOwner();
        if ((owner != null) && (!owner.equals(user.getLogin()))) {
            throw new NotAllowedException(new Locale(user.getLanguage()), "NotAllowedException30");
        }

        new SubscriptionDAO(em).createStateChangeSubscription(new StateChangeSubscription(user, mdoc));
    }

    @RolesAllowed("users")
    @Override
    public void unsubscribeToStateChangeEvent(MasterDocumentKey pMDocPK) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException {
        User user = userManager.checkWorkspaceReadAccess(pMDocPK.getWorkspaceId());
        SubscriptionKey key = new SubscriptionKey(user.getWorkspaceId(), user.getLogin(), pMDocPK.getWorkspaceId(), pMDocPK.getId(), pMDocPK.getVersion());
        new SubscriptionDAO(em).removeStateChangeSubscription(key);
    }

    @RolesAllowed("users")
    @Override
    public void subscribeToIterationChangeEvent(MasterDocumentKey pMDocPK) throws WorkspaceNotFoundException, NotAllowedException, MasterDocumentNotFoundException, UserNotFoundException, UserNotActiveException {
        User user = userManager.checkWorkspaceReadAccess(pMDocPK.getWorkspaceId());
        MasterDocument mdoc = new MasterDocumentDAO(new Locale(user.getLanguage()), em).getMDocRef(pMDocPK);
        String owner = mdoc.getLocation().getOwner();
        if ((owner != null) && (!owner.equals(user.getLogin()))) {
            throw new NotAllowedException(new Locale(user.getLanguage()), "NotAllowedException30");
        }

        new SubscriptionDAO(em).createIterationChangeSubscription(new IterationChangeSubscription(user, mdoc));
    }

    @RolesAllowed("users")
    @Override
    public void unsubscribeToIterationChangeEvent(MasterDocumentKey pMDocPK) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException {
        User user = userManager.checkWorkspaceReadAccess(pMDocPK.getWorkspaceId());
        SubscriptionKey key = new SubscriptionKey(user.getWorkspaceId(), user.getLogin(), pMDocPK.getWorkspaceId(), pMDocPK.getId(), pMDocPK.getVersion());
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
