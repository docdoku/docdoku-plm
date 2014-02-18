/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2013 DocDoku SARL
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
import com.docdoku.core.document.*;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.gcm.GCMAccount;
import com.docdoku.core.meta.InstanceAttribute;
import com.docdoku.core.meta.InstanceAttributeTemplate;
import com.docdoku.core.meta.Tag;
import com.docdoku.core.meta.TagKey;
import com.docdoku.core.query.DocumentSearchQuery;
import com.docdoku.core.security.ACL;
import com.docdoku.core.security.ACLUserEntry;
import com.docdoku.core.security.ACLUserGroupEntry;
import com.docdoku.core.services.*;
import com.docdoku.core.sharing.SharedDocument;
import com.docdoku.core.sharing.SharedEntityKey;
import com.docdoku.core.util.NamingConvention;
import com.docdoku.core.util.Tools;
import com.docdoku.core.workflow.*;
import com.docdoku.server.dao.*;
import com.docdoku.server.esindexer.ESIndexer;

import javax.annotation.Resource;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.jws.WebService;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.text.ParseException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@DeclareRoles({"users","admin","guest-proxy"})
@Local(IDocumentManagerLocal.class)
@Stateless(name = "DocumentManagerBean")
@WebService(endpointInterface = "com.docdoku.core.services.IDocumentManagerWS")
public class DocumentManagerBean implements IDocumentManagerWS, IDocumentManagerLocal {

    @PersistenceContext
    private EntityManager em;

    @Resource
    private SessionContext ctx;

    @EJB
    private IUserManagerLocal userManager;

    @EJB
    private IMailerLocal mailer;

    @EJB
    private IGCMSenderLocal gcmNotifier;

    @EJB
    private ESIndexer esIndexer;

    @EJB
    private IDataManagerLocal dataManager;

    private final static Logger LOGGER = Logger.getLogger(DocumentManagerBean.class.getName());

    @RolesAllowed("users")
    @Override
    public BinaryResource saveFileInTemplate(DocumentMasterTemplateKey pDocMTemplateKey, String pName, long pSize) throws WorkspaceNotFoundException, NotAllowedException, DocumentMasterTemplateNotFoundException, FileAlreadyExistsException, UserNotFoundException, UserNotActiveException, CreationException, AccessRightException {
        User user = userManager.checkWorkspaceWriteAccess(pDocMTemplateKey.getWorkspaceId());

        if (!NamingConvention.correctNameFile(pName)) {
            throw new NotAllowedException(Locale.getDefault(), "NotAllowedException9");
        }

        DocumentMasterTemplateDAO templateDAO = new DocumentMasterTemplateDAO(em);
        DocumentMasterTemplate template = templateDAO.loadDocMTemplate(pDocMTemplateKey);
        BinaryResource binaryResource = null;
        String fullName = template.getWorkspaceId() + "/document-templates/" + template.getId() + "/" + pName;

        for (BinaryResource bin : template.getAttachedFiles()) {
            if (bin.getFullName().equals(fullName)) {
                binaryResource = bin;
                break;
            }
        }

        if (binaryResource == null) {
            binaryResource = new BinaryResource(fullName, pSize, new Date());
            new BinaryResourceDAO(em).createBinaryResource(binaryResource);
            template.addFile(binaryResource);
        } else {
            binaryResource.setContentLength(pSize);
            binaryResource.setLastModified(new Date());
        }
        return binaryResource;
    }

    @RolesAllowed("users")
    @Override
    public BinaryResource saveFileInDocument(DocumentIterationKey pDocPK, String pName, long pSize) throws WorkspaceNotFoundException, NotAllowedException, DocumentRevisionNotFoundException, FileAlreadyExistsException, UserNotFoundException, UserNotActiveException, CreationException, AccessRightException {
        User user = checkDocumentRevisionWriteAccess(new DocumentRevisionKey(pDocPK.getWorkspaceId(), pDocPK.getDocumentMasterId(), pDocPK.getDocumentRevisionVersion()));
        if (!NamingConvention.correctNameFile(pName)) {
            throw new NotAllowedException(Locale.getDefault(), "NotAllowedException9");
        }

        DocumentRevisionDAO docRDAO = new DocumentRevisionDAO(em);
        DocumentRevision docR = docRDAO.loadDocR(new DocumentRevisionKey(pDocPK.getWorkspaceId(), pDocPK.getDocumentMasterId(), pDocPK.getDocumentRevisionVersion()));
        DocumentIteration document = docR.getIteration(pDocPK.getIteration());

        if (docR.isCheckedOut() && docR.getCheckOutUser().equals(user) && docR.getLastIteration().equals(document)) {
            BinaryResource binaryResource = null;
            String fullName = docR.getWorkspaceId() + "/documents/" + docR.getId() + "/" + docR.getVersion() + "/" + document.getIteration() + "/" + pName;

            for (BinaryResource bin : document.getAttachedFiles()) {
                if (bin.getFullName().equals(fullName)) {
                    binaryResource = bin;
                    break;
                }
            }
            if (binaryResource == null) {
                binaryResource = new BinaryResource(fullName, pSize, new Date());
                new BinaryResourceDAO(em).createBinaryResource(binaryResource);
                document.addFile(binaryResource);
            } else {
                binaryResource.setContentLength(pSize);
                binaryResource.setLastModified(new Date());
            }
            return binaryResource;
        } else {
            throw new NotAllowedException(Locale.getDefault(), "NotAllowedException4");
        }
    }

    @LogDocument
    @RolesAllowed({"users","guest-proxy"})
    @Override
    public BinaryResource getBinaryResource(String pFullName) throws WorkspaceNotFoundException, NotAllowedException, FileNotFoundException, UserNotFoundException, UserNotActiveException, AccessRightException {

        if(ctx.isCallerInRole("guest-proxy")){
            return new BinaryResourceDAO(em).loadBinaryResource(pFullName);
        }

        User user = userManager.checkWorkspaceReadAccess(BinaryResource.parseWorkspaceId(pFullName));

        BinaryResourceDAO binDAO = new BinaryResourceDAO(new Locale(user.getLanguage()), em);
        BinaryResource binaryResource = binDAO.loadBinaryResource(pFullName);

        DocumentIteration document = binDAO.getDocumentOwner(binaryResource);
        if (document != null) {
            DocumentRevision docR = document.getDocumentRevision();

            if((docR.getACL() != null && docR.getACL().hasReadAccess(user)) || docR.getACL() == null){
                String owner = docR.getLocation().getOwner();

                if (((owner != null) && (!owner.equals(user.getLogin()))) || (docR.isCheckedOut() && !docR.getCheckOutUser().equals(user) && docR.getLastIteration().equals(document))) {
                    throw new NotAllowedException(new Locale(user.getLanguage()), "NotAllowedException34");
                } else {
                    return binaryResource;
                }
            }else{
                throw new AccessRightException(new Locale(user.getLanguage()),user);
            }

        } else {
            return binaryResource;
        }
    }

    @RolesAllowed({"users"})
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
    public DocumentRevision[] findDocumentRevisionsByFolder(String pCompletePath) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException {
        String workspaceId = Folder.parseWorkspaceId(pCompletePath);
        User user = userManager.checkWorkspaceReadAccess(workspaceId);
        List<DocumentRevision> docRs = new DocumentRevisionDAO(new Locale(user.getLanguage()), em).findDocRsByFolder(pCompletePath);
        ListIterator<DocumentRevision> ite = docRs.listIterator();
        Workspace wks = new WorkspaceDAO(new Locale(user.getLanguage()), em).loadWorkspace(workspaceId);
        boolean isAdmin = wks.getAdmin().getLogin().equals(user.getLogin());
        while (ite.hasNext()) {
            DocumentRevision docR = ite.next();
            if (!isAdmin && docR.getACL() != null && !docR.getACL().hasReadAccess(user)) {
                ite.remove();
            }else if ((docR.isCheckedOut()) && (!docR.getCheckOutUser().equals(user))) {
                docR = docR.clone();
                docR.removeLastIteration();
                ite.set(docR);
            }
        }
        return docRs.toArray(new DocumentRevision[docRs.size()]);
    }

    @RolesAllowed("users")
    @Override
    public DocumentRevision[] findDocumentRevisionsByTag(TagKey pKey) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException {
        String workspaceId = pKey.getWorkspaceId();
        User user = userManager.checkWorkspaceReadAccess(workspaceId);
        List<DocumentRevision> docRs = new DocumentRevisionDAO(new Locale(user.getLanguage()), em).findDocRsByTag(new Tag(user.getWorkspace(), pKey.getLabel()));
        ListIterator<DocumentRevision> ite = docRs.listIterator();
        Workspace wks = new WorkspaceDAO(new Locale(user.getLanguage()), em).loadWorkspace(workspaceId);
        boolean isAdmin = wks.getAdmin().getLogin().equals(user.getLogin());
        while (ite.hasNext()) {
            DocumentRevision docR = ite.next();
            if (!isAdmin && docR.getACL() != null && !docR.getACL().hasReadAccess(user)) {
                ite.remove();
            }else if ((docR.isCheckedOut()) && (!docR.getCheckOutUser().equals(user))) {
                docR = docR.clone();
                docR.removeLastIteration();
                ite.set(docR);
            }
        }
        return docRs.toArray(new DocumentRevision[docRs.size()]);
    }

    @RolesAllowed({"users","guest-proxy"})
    @Override
    public DocumentRevision getDocumentRevision(DocumentRevisionKey pDocRPK) throws WorkspaceNotFoundException, DocumentRevisionNotFoundException, NotAllowedException, UserNotFoundException, UserNotActiveException, AccessRightException{
        if(ctx.isCallerInRole("guest-proxy")){
            DocumentRevision documentRevision = new DocumentRevisionDAO(em).loadDocR(pDocRPK);
            if(documentRevision.isCheckedOut()){
                em.detach(documentRevision);
                documentRevision.removeLastIteration();
            }
            return documentRevision;
        }

        User user = userManager.checkWorkspaceReadAccess(pDocRPK.getWorkspaceId());
        DocumentRevision docR = new DocumentRevisionDAO(new Locale(user.getLanguage()), em).loadDocR(pDocRPK);
        String owner = docR.getLocation().getOwner();
        if ((owner != null) && (!owner.equals(user.getLogin()))) {
            throw new NotAllowedException(new Locale(user.getLanguage()), "NotAllowedException5");
        }

        if((docR.getACL() != null && docR.getACL().hasReadAccess(user)) || docR.getACL() == null){

            if ((docR.isCheckedOut()) && (!docR.getCheckOutUser().equals(user))) {
                docR = docR.clone();
                docR.removeLastIteration();
            }
            return docR;

        }else{
            throw new AccessRightException(new Locale(user.getLanguage()),user);
        }

    }

    @RolesAllowed({"users","guest-proxy"})
    @Override
    public DocumentIteration findDocumentIterationByBinaryResource(BinaryResource pBinaryResource) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException {

        if(ctx.isCallerInRole("guest-proxy")){
            return  new DocumentRevisionDAO(em).findDocumentIterationByBinaryResource(pBinaryResource);
        }

        User user = userManager.checkWorkspaceReadAccess(pBinaryResource.getWorkspaceId());
        DocumentRevisionDAO documentMasterDAO = new DocumentRevisionDAO(new Locale(user.getLanguage()),em);
        return  documentMasterDAO.findDocumentIterationByBinaryResource(pBinaryResource);
    }

    @RolesAllowed("users")
    @Override
    public void updateDocumentACL(String pWorkspaceId, DocumentRevisionKey docKey, Map<String,String> pACLUserEntries, Map<String,String> pACLUserGroupEntries) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, DocumentRevisionNotFoundException, AccessRightException {

        User user = checkDocumentRevisionWriteAccess(docKey);

        DocumentRevisionDAO documentRevisionDAO = new DocumentRevisionDAO(new Locale(user.getLanguage()), em);
        DocumentRevision docR = documentRevisionDAO.getDocRRef(docKey);
        Workspace wks = new WorkspaceDAO(em).loadWorkspace(pWorkspaceId);

        if (docR.getAuthor().getLogin().equals(user.getLogin()) || wks.getAdmin().getLogin().equals(user.getLogin())) {

            if (docR.getACL() == null) {
                ACL acl = new ACL();
                if (pACLUserEntries != null) {
                    for (Map.Entry<String, String> entry : pACLUserEntries.entrySet()) {
                        acl.addEntry(em.getReference(User.class,new UserKey(pWorkspaceId,entry.getKey())),ACL.Permission.valueOf(entry.getValue()));
                    }
                }

                if (pACLUserGroupEntries != null) {
                    for (Map.Entry<String, String> entry : pACLUserGroupEntries.entrySet()) {
                        acl.addEntry(em.getReference(UserGroup.class,new UserGroupKey(pWorkspaceId,entry.getKey())),ACL.Permission.valueOf(entry.getValue()));
                    }
                }

                new ACLDAO(em).createACL(acl);
                docR.setACL(acl);

            }else{
                if (pACLUserEntries != null) {
                    for (ACLUserEntry entry : docR.getACL().getUserEntries().values()) {
                        ACL.Permission newPermission = ACL.Permission.valueOf(pACLUserEntries.get(entry.getPrincipalLogin()));
                        if(newPermission != null){
                            entry.setPermission(newPermission);
                        }
                    }
                }

                if (pACLUserGroupEntries != null) {
                    for (ACLUserGroupEntry entry : docR.getACL().getGroupEntries().values()) {
                        ACL.Permission newPermission = ACL.Permission.valueOf(pACLUserGroupEntries.get(entry.getPrincipalId()));
                        if(newPermission != null){
                            entry.setPermission(newPermission);
                        }
                    }
                }
            }

        }else {
                throw new AccessRightException(new Locale(user.getLanguage()), user);
        }
    }

    @RolesAllowed("users")
    @Override
    public void removeACLFromDocumentRevision(DocumentRevisionKey documentRevisionKey) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, DocumentRevisionNotFoundException, AccessRightException {

        User user = userManager.checkWorkspaceReadAccess(documentRevisionKey.getDocumentMaster().getWorkspace());

        DocumentRevisionDAO documentRevisionDAO = new DocumentRevisionDAO(new Locale(user.getLanguage()), em);
        DocumentRevision docR = documentRevisionDAO.getDocRRef(documentRevisionKey);
        Workspace wks = new WorkspaceDAO(em).loadWorkspace(documentRevisionKey.getDocumentMaster().getWorkspace());

        if (docR.getAuthor().getLogin().equals(user.getLogin()) || wks.getAdmin().getLogin().equals(user.getLogin())) {
            ACL acl = docR.getACL();
            if (acl != null) {
                new ACLDAO(em).removeACLEntries(acl);
                docR.setACL(null);
            }
        }else{
            throw new AccessRightException(new Locale(user.getLanguage()), user);
        }
    }

    @Override
    public DocumentRevision[] getAllDocumentsInWorkspace(String workspaceId, int start, int pMaxResults) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException {

        User user = userManager.checkWorkspaceReadAccess(workspaceId);
        List<DocumentRevision> docRs = new DocumentRevisionDAO(new Locale(user.getLanguage()), em).getDocumentRevisionsFiltered(user, workspaceId, start, pMaxResults);
        ListIterator<DocumentRevision> ite = docRs.listIterator();

        while (ite.hasNext()) {
            DocumentRevision docR = ite.next();

            if ((docR.isCheckedOut()) && (!docR.getCheckOutUser().equals(user))) {
                docR = docR.clone();
                docR.removeLastIteration();
                ite.set(docR);
            }

        }
        return docRs.toArray(new DocumentRevision[docRs.size()]);
    }

    @Override
    public int getDocumentsInWorkspaceCount(String workspaceId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException {
        User user = userManager.checkWorkspaceReadAccess(workspaceId);
        return new DocumentRevisionDAO(new Locale(user.getLanguage()), em).getDocumentRevisionsCountFiltered(user, workspaceId);
    }

    @RolesAllowed("users")
    @Override
    public DocumentRevision[] getCheckedOutDocumentRevisions(String pWorkspaceId) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException{        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
        List<DocumentRevision> docRs = new DocumentRevisionDAO(new Locale(user.getLanguage()), em).findCheckedOutDocRs(user);
        return docRs.toArray(new DocumentRevision[docRs.size()]);
    }

    @RolesAllowed("users")
    @Override
    public Task[] getTasks(String pWorkspaceId) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
        return new TaskDAO(new Locale(user.getLanguage()), em).findTasks(user);
    }

    @RolesAllowed("users")
    @Override
    public DocumentRevisionKey[] getIterationChangeEventSubscriptions(String pWorkspaceId) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
        return new SubscriptionDAO(em).getIterationChangeEventSubscriptions(user);
    }

    @RolesAllowed({"users"})
    @Override
    public DocumentRevisionKey[] getStateChangeEventSubscriptions(String pWorkspaceId) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
        return new SubscriptionDAO(em).getStateChangeEventSubscriptions(user);
    }


    @RolesAllowed("users")
    @Override
    public boolean isUserStateChangeEventSubscribedForGivenDocument(String pWorkspaceId, DocumentRevision docR) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
        return new SubscriptionDAO(em).isUserStateChangeEventSubscribedForGivenDocument(user, docR);
    }

    @RolesAllowed("users")
    @Override
    public boolean isUserIterationChangeEventSubscribedForGivenDocument(String pWorkspaceId, DocumentRevision docR) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
        return  new SubscriptionDAO(em).isUserIterationChangeEventSubscribedForGivenDocument(user, docR);
    }

    @RolesAllowed("users")
    @Override
    public DocumentRevision[] getDocumentRevisionsWithAssignedTasksForGivenUser(String pWorkspaceId, String assignedUserLogin) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
        List<DocumentRevision> docRs = new DocumentRevisionDAO(new Locale(user.getLanguage()), em).findDocsWithAssignedTasksForGivenUser(pWorkspaceId, assignedUserLogin);
        return docRs.toArray(new DocumentRevision[docRs.size()]);
    }

    @RolesAllowed("users")
    @Override
    public DocumentRevision[] getDocumentRevisionsWithOpenedTasksForGivenUser(String pWorkspaceId, String assignedUserLogin) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
        List<DocumentRevision> docRs = new DocumentRevisionDAO(new Locale(user.getLanguage()), em).findDocsWithOpenedTasksForGivenUser(pWorkspaceId, assignedUserLogin);
        return docRs.toArray(new DocumentRevision[docRs.size()]);
    }

    @RolesAllowed("users")
    @Override
    public DocumentRevision[] getDocumentRevisionsWithReference(String pWorkspaceId, String reference, int maxResults) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
        List<DocumentRevision> docRs = new DocumentRevisionDAO(new Locale(user.getLanguage()), em).findDocsRevisionsWithReferenceLike(pWorkspaceId, reference, maxResults);
        return docRs.toArray(new DocumentRevision[docRs.size()]);
    }

    @RolesAllowed("users")
    @Override
    public String generateId(String pWorkspaceId, String pDocMTemplateId) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException, DocumentMasterTemplateNotFoundException {

        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
        DocumentMasterTemplate template = new DocumentMasterTemplateDAO(new Locale(user.getLanguage()), em).loadDocMTemplate(new DocumentMasterTemplateKey(user.getWorkspaceId(), pDocMTemplateId));

        String newId = null;
        try {
            String latestId = new DocumentRevisionDAO(new Locale(user.getLanguage()), em).findLatestDocMId(pWorkspaceId, template.getDocumentType());
            String inputMask = template.getMask();
            String convertedMask = Tools.convertMask(inputMask);
            newId = Tools.increaseId(latestId, convertedMask);
        } catch (ParseException | NoResultException ex) {
            //may happen when a different mask has been used for the same document type
            //or
            //may happen when no document of the specified type has been created
        }
        return newId;

    }

    @RolesAllowed("users")
    @Override
    public DocumentRevision[] searchDocumentRevisions(DocumentSearchQuery pQuery) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException, IndexerServerException {
        User user = userManager.checkWorkspaceReadAccess(pQuery.getWorkspaceId());
        List<DocumentRevision> fetchedDocRs = esIndexer.search(pQuery);                                                 // Get Search Results

        if(!fetchedDocRs.isEmpty()){
            Workspace wks = new WorkspaceDAO(new Locale(user.getLanguage()), em).loadWorkspace(pQuery.getWorkspaceId());
            boolean isAdmin = wks.getAdmin().getLogin().equals(user.getLogin());

            ListIterator<DocumentRevision> ite = fetchedDocRs.listIterator();
            while (ite.hasNext()) {
                DocumentRevision docR = ite.next();

                if (!isAdmin && docR.getACL() != null && !docR.getACL().hasReadAccess(user)) {                          // Check Rigth Acces
                    ite.remove();
                    continue;
                }

                if(docR.getLocation().isPrivate() && (!docR.getLocation().getOwner().equals(user.getLogin())) ){        // Remove Private DocMaster From Results
                    ite.remove();
                    continue;
                }

                if ((docR.isCheckedOut()) && (!docR.getCheckOutUser().equals(user))) {                                  // Remove CheckedOut DocMaster From Results
                    docR = docR.clone();
                    docR.removeLastIteration();
                    ite.set(docR);
                }
            }
            return fetchedDocRs.toArray(new DocumentRevision[fetchedDocRs.size()]);
        }
        return new DocumentRevision[0];
    }

    @RolesAllowed("users")
    @Override
    public DocumentMasterTemplate[] getDocumentMasterTemplates(String pWorkspaceId) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
        return new DocumentMasterTemplateDAO(new Locale(user.getLanguage()), em).findAllDocMTemplates(pWorkspaceId);
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
    public DocumentMasterTemplate updateDocumentMasterTemplate(DocumentMasterTemplateKey pKey, String pDocumentType, String pMask, InstanceAttributeTemplate[] pAttributeTemplates, boolean idGenerated, boolean attributesLocked) throws WorkspaceNotFoundException, AccessRightException, DocumentMasterTemplateNotFoundException, UserNotFoundException {
        User user = userManager.checkWorkspaceWriteAccess(pKey.getWorkspaceId());

        DocumentMasterTemplateDAO templateDAO = new DocumentMasterTemplateDAO(new Locale(user.getLanguage()), em);
        DocumentMasterTemplate template = templateDAO.loadDocMTemplate(pKey);
        Date now = new Date();
        template.setCreationDate(now);
        template.setAuthor(user);
        template.setDocumentType(pDocumentType);
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

    @RolesAllowed("users")
    @Override
    public void deleteTag(TagKey pKey) throws WorkspaceNotFoundException, AccessRightException, TagNotFoundException, UserNotFoundException {
        User user = userManager.checkWorkspaceWriteAccess(pKey.getWorkspaceId());
        Locale userLocale = new Locale(user.getLanguage());
        Tag tagToRemove = new Tag(user.getWorkspace(), pKey.getLabel());
        List<DocumentRevision> docRs = new DocumentRevisionDAO(userLocale, em).findDocRsByTag(tagToRemove);
        for (DocumentRevision docR : docRs) {
            docR.getTags().remove(tagToRemove);
        }

        new TagDAO(userLocale, em).removeTag(pKey);
    }

    @RolesAllowed("users")
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
    public DocumentRevision createDocumentMaster(String pParentFolder, String pDocMId, String pTitle, String pDescription, String pDocMTemplateId, String pWorkflowModelId, ACLUserEntry[] pACLUserEntries, ACLUserGroupEntry[] pACLUserGroupEntries, Map<String, String> roleMappings) throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException, NotAllowedException, FolderNotFoundException, DocumentMasterTemplateNotFoundException, FileAlreadyExistsException, CreationException, DocumentRevisionAlreadyExistsException, RoleNotFoundException, WorkflowModelNotFoundException, DocumentMasterAlreadyExistsException {

        User user = userManager.checkWorkspaceWriteAccess(Folder.parseWorkspaceId(pParentFolder));
        if (!NamingConvention.correct(pDocMId)) {
            throw new NotAllowedException(new Locale(user.getLanguage()), "NotAllowedException9");
        }
        Folder folder = new FolderDAO(new Locale(user.getLanguage()), em).loadFolder(pParentFolder);
        checkWritingRight(user, folder);

        DocumentMaster docM;
        DocumentRevision docR;
        DocumentIteration newDoc;

        DocumentMasterDAO docMDAO = new DocumentMasterDAO(new Locale(user.getLanguage()), em);
        if (pDocMTemplateId == null) {
            docM = new DocumentMaster(user.getWorkspace(), pDocMId, user);
            //specify an empty type instead of null
            //so the search will find it with the % character
            docM.setType("");
            docMDAO.createDocM(docM);
            docR = docM.createNextRevision(user);
            newDoc = docR.createNextIteration(user);
        } else {
            DocumentMasterTemplate template = new DocumentMasterTemplateDAO(new Locale(user.getLanguage()), em).loadDocMTemplate(new DocumentMasterTemplateKey(user.getWorkspaceId(), pDocMTemplateId));
            docM = new DocumentMaster(user.getWorkspace(), pDocMId, user);
            docM.setType(template.getDocumentType());
            docM.setAttributesLocked(template.isAttributesLocked());

            docMDAO.createDocM(docM);
            docR = docM.createNextRevision(user);
            newDoc = docR.createNextIteration(user);


            Map<String, InstanceAttribute> attrs = new HashMap<>();
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
                Date lastModified = sourceFile.getLastModified();
                String fullName = docM.getWorkspaceId() + "/documents/" + docM.getId() + "/A/1/" + fileName;
                BinaryResource targetFile = new BinaryResource(fullName, length, lastModified);
                binDAO.createBinaryResource(targetFile);

                newDoc.addFile(targetFile);
                try {
                    dataManager.copyData(sourceFile, targetFile);
                } catch (StorageException e) {
                    e.printStackTrace();
                }
            }
        }

        if (pWorkflowModelId != null) {

            UserDAO userDAO = new UserDAO(new Locale(user.getLanguage()),em);
            RoleDAO roleDAO = new RoleDAO(new Locale(user.getLanguage()),em);

            Map<Role,User> roleUserMap = new HashMap<>();

            for (Object o : roleMappings.entrySet()) {
                Map.Entry pairs = (Map.Entry) o;
                String roleName = (String) pairs.getKey();
                String userLogin = (String) pairs.getValue();
                User worker = userDAO.loadUser(new UserKey(Folder.parseWorkspaceId(pParentFolder), userLogin));
                Role role = roleDAO.loadRole(new RoleKey(Folder.parseWorkspaceId(pParentFolder), roleName));
                roleUserMap.put(role, worker);
            }

            WorkflowModel workflowModel = new WorkflowModelDAO(new Locale(user.getLanguage()), em).loadWorkflowModel(new WorkflowModelKey(user.getWorkspaceId(), pWorkflowModelId));
            Workflow workflow = workflowModel.createWorkflow(roleUserMap);
            docR.setWorkflow(workflow);

            Collection<Task> runningTasks = workflow.getRunningTasks();
            for (Task runningTask : runningTasks) {
                runningTask.start();
            }
            mailer.sendApproval(runningTasks, docR);
        }

        docR.setTitle(pTitle);
        docR.setDescription(pDescription);

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
            docR.setACL(acl);
        }
        Date now = new Date();
        docM.setCreationDate(now);
        docR.setCreationDate(now);
        docR.setLocation(folder);
        docR.setCheckOutUser(user);
        docR.setCheckOutDate(now);
        newDoc.setCreationDate(now);
        DocumentRevisionDAO docRDAO = new DocumentRevisionDAO(new Locale(user.getLanguage()), em);
        docRDAO.createDocR(docR);
        return docR;
    }

    @RolesAllowed({"users","admin"})
    @Override
    public DocumentRevision[] getAllCheckedOutDocumentRevisions(String pWorkspaceId) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
        List<DocumentRevision> docRs = new DocumentRevisionDAO(new Locale(user.getLanguage()), em).findAllCheckedOutDocRevisions(pWorkspaceId);
        return docRs.toArray(new DocumentRevision[docRs.size()]);
    }

    @RolesAllowed("users")
    @Override
    public DocumentMasterTemplate createDocumentMasterTemplate(String pWorkspaceId, String pId, String pDocumentType,
            String pMask, InstanceAttributeTemplate[] pAttributeTemplates, boolean idGenerated, boolean attributesLocked) throws WorkspaceNotFoundException, AccessRightException, DocumentMasterTemplateAlreadyExistsException, UserNotFoundException, NotAllowedException, CreationException {
        User user = userManager.checkWorkspaceWriteAccess(pWorkspaceId);
        if (!NamingConvention.correct(pId)) {
            throw new NotAllowedException(new Locale(user.getLanguage()), "NotAllowedException9");
        }
        DocumentMasterTemplate template = new DocumentMasterTemplate(user.getWorkspace(), pId, user, pDocumentType, pMask);
        Date now = new Date();
        template.setCreationDate(now);
        template.setIdGenerated(idGenerated);
        template.setAttributesLocked(attributesLocked);

        Set<InstanceAttributeTemplate> attrs = new HashSet<>();
        Collections.addAll(attrs, pAttributeTemplates);
        template.setAttributeTemplates(attrs);

        new DocumentMasterTemplateDAO(new Locale(user.getLanguage()), em).createDocMTemplate(template);
        return template;
    }

    @RolesAllowed("users")
    @Override
    public DocumentRevision moveDocumentRevision(String pParentFolder, DocumentRevisionKey pDocRPK) throws WorkspaceNotFoundException, DocumentRevisionNotFoundException, NotAllowedException, AccessRightException, FolderNotFoundException, UserNotFoundException, UserNotActiveException {
        //TODO security check if both parameter belong to the same workspace
        User user = checkDocumentRevisionWriteAccess(pDocRPK);

        Folder newLocation = new FolderDAO(new Locale(user.getLanguage()), em).loadFolder(pParentFolder);
        checkWritingRight(user, newLocation);
        DocumentRevisionDAO docRDAO = new DocumentRevisionDAO(new Locale(user.getLanguage()), em);
        DocumentRevision docR = docRDAO.loadDocR(pDocRPK);

        Folder oldLocation = docR.getLocation();
        String owner = oldLocation.getOwner();
        if ((owner != null) && (!owner.equals(user.getLogin()))) {
            throw new NotAllowedException(new Locale(user.getLanguage()), "NotAllowedException6");
        } else {
            docR.setLocation(newLocation);
            if ((docR.isCheckedOut()) && (!docR.getCheckOutUser().equals(user))) {
                docR = docR.clone();
                docR.removeLastIteration();
            }
            return docR;
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
    public DocumentRevision approveTaskOnDocument(String pWorkspaceId, TaskKey pTaskKey, String pComment, String pSignature) throws WorkspaceNotFoundException, TaskNotFoundException, NotAllowedException, UserNotFoundException, UserNotActiveException, WorkflowNotFoundException {
        //TODO no check is made that pTaskKey is from the same workspace than pWorkspaceId
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);

        Task task = new TaskDAO(new Locale(user.getLanguage()), em).loadTask(pTaskKey);
        Workflow workflow = task.getActivity().getWorkflow();
        DocumentRevision docR = new WorkflowDAO(em).getDocumentTarget(workflow);

        if(docR == null){
            throw new WorkflowNotFoundException(new Locale(user.getLanguage()),workflow.getId());
        }

        if (!task.getWorker().equals(user)) {
            throw new NotAllowedException(new Locale(user.getLanguage()), "NotAllowedException14");
        }

        if (!workflow.getRunningTasks().contains(task)) {
            throw new NotAllowedException(new Locale(user.getLanguage()), "NotAllowedException15");
        }

        if (docR.isCheckedOut()) {
            throw new NotAllowedException(new Locale(user.getLanguage()), "NotAllowedException16");
        }

        int previousStep = workflow.getCurrentStep();
        task.approve(pComment, docR.getLastIteration().getIteration(), pSignature);
        int currentStep = workflow.getCurrentStep();

        if (previousStep != currentStep){

            SubscriptionDAO subscriptionDAO = new SubscriptionDAO(em);

            User[] subscribers = subscriptionDAO.getStateChangeEventSubscribers(docR);
            if (subscribers.length != 0) {
                mailer.sendStateNotification(subscribers, docR);
            }

            GCMAccount[] gcmAccounts = subscriptionDAO.getStateChangeEventSubscribersGCMAccount(docR);
            if (gcmAccounts.length != 0) {
                gcmNotifier.sendStateNotification(gcmAccounts, docR);
            }

        }

        Collection<Task> runningTasks = workflow.getRunningTasks();
        for (Task runningTask : runningTasks) {
            runningTask.start();
        }
        mailer.sendApproval(runningTasks, docR);
        return docR;
    }

    @RolesAllowed("users")
    @CheckActivity
    @Override
    public DocumentRevision rejectTaskOnDocument(String pWorkspaceId, TaskKey pTaskKey, String pComment, String pSignature) throws WorkspaceNotFoundException, TaskNotFoundException, NotAllowedException, UserNotFoundException, UserNotActiveException, WorkflowNotFoundException {
        //TODO no check is made that pTaskKey is from the same workspace than pWorkspaceId
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);

        Task task = new TaskDAO(new Locale(user.getLanguage()), em).loadTask(pTaskKey);
        Workflow workflow = task.getActivity().getWorkflow();
        DocumentRevision docR = new WorkflowDAO(em).getDocumentTarget(workflow);

        if(docR == null){
            throw new WorkflowNotFoundException(new Locale(user.getLanguage()),workflow.getId());
        }

        if (!task.getWorker().equals(user)) {
            throw new NotAllowedException(new Locale(user.getLanguage()), "NotAllowedException14");
        }

        if (!workflow.getRunningTasks().contains(task)) {
            throw new NotAllowedException(new Locale(user.getLanguage()), "NotAllowedException15");
        }

        if (docR.isCheckedOut()) {
            throw new NotAllowedException(new Locale(user.getLanguage()), "NotAllowedException16");
        }

        task.reject(pComment, docR.getLastIteration().getIteration(), pSignature);

        // Relaunch Workflow ?
        Activity currentActivity = task.getActivity();

        if(currentActivity.isStopped() && currentActivity.getRelaunchActivity() != null){

            WorkflowDAO workflowDAO = new WorkflowDAO(em);

            int relaunchActivityStep  = currentActivity.getRelaunchActivity().getStep();

            // Clone new workflow
            Workflow relaunchedWorkflow  = workflow.clone();
            workflowDAO.createWorkflow(relaunchedWorkflow);

            // Move aborted workflow in docR list
            workflow.abort();
            docR.addAbortedWorkflows(workflow);

            // Set new workflow on document
            docR.setWorkflow(relaunchedWorkflow);

            // Reset some properties
            relaunchedWorkflow.relaunch(relaunchActivityStep);

            // Send mails for running tasks
            mailer.sendApproval(relaunchedWorkflow.getRunningTasks(), docR);

            // Send notification for relaunch
            mailer.sendDocumentRevisionWorkflowRelaunchedNotification(docR);

        }

        return docR;
    }

    @RolesAllowed("users")
    @Override
    public DocumentRevision checkOutDocument(DocumentRevisionKey pDocRPK) throws WorkspaceNotFoundException, NotAllowedException, DocumentRevisionNotFoundException, AccessRightException, FileAlreadyExistsException, UserNotFoundException, UserNotActiveException, CreationException {

        User user = checkDocumentRevisionWriteAccess(pDocRPK);
        DocumentRevisionDAO docRDAO = new DocumentRevisionDAO(new Locale(user.getLanguage()), em);
        DocumentRevision docR = docRDAO.loadDocR(pDocRPK);
        //Check access rights on docR
        Workspace wks = new WorkspaceDAO(new Locale(user.getLanguage()), em).loadWorkspace(pDocRPK.getWorkspaceId());
        boolean isAdmin = wks.getAdmin().getLogin().equals(user.getLogin());

        String owner = docR.getLocation().getOwner();
        if ((owner != null) && (!owner.equals(user.getLogin()))) {
            throw new NotAllowedException(new Locale(user.getLanguage()), "NotAllowedException5");
        }

        if (docR.isCheckedOut()) {
            throw new NotAllowedException(new Locale(user.getLanguage()), "NotAllowedException37");
        }

        DocumentIteration beforeLastDocument = docR.getLastIteration();

        DocumentIteration newDoc = docR.createNextIteration(user);
        //We persist the doc as a workaround for a bug which was introduced
        //since glassfish 3 that set the DTYPE to null in the instance attribute table
        em.persist(newDoc);
        Date now = new Date();
        newDoc.setCreationDate(now);
        docR.setCheckOutUser(user);
        docR.setCheckOutDate(now);

        if (beforeLastDocument != null) {
            BinaryResourceDAO binDAO = new BinaryResourceDAO(new Locale(user.getLanguage()), em);
            for (BinaryResource sourceFile : beforeLastDocument.getAttachedFiles()) {
                String fileName = sourceFile.getName();
                long length = sourceFile.getContentLength();
                Date lastModified = sourceFile.getLastModified();
                String fullName = docR.getWorkspaceId() + "/documents/" + docR.getId() + "/" + docR.getVersion() + "/" + newDoc.getIteration() + "/" + fileName;
                BinaryResource targetFile = new BinaryResource(fullName, length, lastModified);
                binDAO.createBinaryResource(targetFile);
                newDoc.addFile(targetFile);
            }

            Set<DocumentLink> links = new HashSet<>();
            for (DocumentLink link : beforeLastDocument.getLinkedDocuments()) {
                DocumentLink newLink = link.clone();
                links.add(newLink);
            }
            newDoc.setLinkedDocuments(links);

            InstanceAttributeDAO attrDAO = new InstanceAttributeDAO(em);
            Map<String, InstanceAttribute> attrs = new HashMap<>();
            for (InstanceAttribute attr : beforeLastDocument.getInstanceAttributes().values()) {
                InstanceAttribute newAttr = attr.clone();
                //Workaround for the NULL DTYPE bug
                attrDAO.createAttribute(newAttr);
                attrs.put(newAttr.getName(), newAttr);
            }
            newDoc.setInstanceAttributes(attrs);
        }

        return docR;
    }

    @RolesAllowed("users")
    @Override
    public DocumentRevision saveTags(DocumentRevisionKey pDocRPK, String[] pTags) throws WorkspaceNotFoundException, NotAllowedException, DocumentRevisionNotFoundException, AccessRightException, UserNotFoundException, UserNotActiveException, IndexerServerException {
        User user = checkDocumentRevisionWriteAccess(pDocRPK);

        Locale userLocale = new Locale(user.getLanguage());
        DocumentRevisionDAO docRDAO = new DocumentRevisionDAO(userLocale, em);
        DocumentRevision docR = docRDAO.loadDocR(pDocRPK);
        String owner = docR.getLocation().getOwner();
        if ((owner != null) && (!owner.equals(user.getLogin()))) {
            throw new NotAllowedException(new Locale(user.getLanguage()), "NotAllowedException5");
        }

        HashSet<Tag> tags = new HashSet<>();
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

        docR.setTags(tags);

        if ((docR.isCheckedOut()) && (!docR.getCheckOutUser().equals(user))) {
            docR = docR.clone();
            docR.removeLastIteration();
        }

        for (DocumentIteration documentIteration:docR.getDocumentIterations()){
            esIndexer.index(documentIteration);
        }
        return docR;
    }

    /**
     * Remove a tag from a DocumentMaster
     * @param pDocRPK DocumentRevision with the tag to remove
     * @param pTag The tag to remove
     * @return The DocumentMaster without the tag
     * @throws WorkspaceNotFoundException
     * @throws NotAllowedException
     * @throws DocumentRevisionNotFoundException
     * @throws AccessRightException
     * @throws UserNotFoundException
     * @throws UserNotActiveException
     */
    @RolesAllowed("users")
    public DocumentRevision removeTag(DocumentRevisionKey pDocRPK, String pTag)
            throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, AccessRightException, DocumentRevisionNotFoundException, NotAllowedException, IndexerServerException {

        User user = checkDocumentRevisionWriteAccess(pDocRPK);

        DocumentRevision docR = getDocumentRevision(pDocRPK);
        Tag tagToRemove = new Tag(user.getWorkspace(), pTag);
        docR.getTags().remove(tagToRemove);

        if ((docR.isCheckedOut()) && (!docR.getCheckOutUser().equals(user))) {
            docR = docR.clone();
            docR.removeLastIteration();
        }

        for (DocumentIteration documentIteration:docR.getDocumentIterations()){
            esIndexer.index(documentIteration);
        }
        return docR;
    }

    @RolesAllowed("users")
    @Override
    public DocumentRevision undoCheckOutDocument(DocumentRevisionKey pDocRPK) throws WorkspaceNotFoundException, DocumentRevisionNotFoundException, NotAllowedException, UserNotFoundException, UserNotActiveException, AccessRightException {

        User user = checkDocumentRevisionWriteAccess(pDocRPK);

        DocumentRevisionDAO docRDAO = new DocumentRevisionDAO(new Locale(user.getLanguage()), em);
        DocumentRevision docR = docRDAO.loadDocR(pDocRPK);
        if (docR.isCheckedOut() && docR.getCheckOutUser().equals(user)) {
            if(docR.getLastIteration().getIteration() <= 1) {
                throw new NotAllowedException(new Locale(user.getLanguage()), "NotAllowedException27");
            }
            DocumentIteration doc = docR.removeLastIteration();
            for (BinaryResource file : doc.getAttachedFiles()) {
                try {
                    dataManager.deleteData(file);
                } catch (StorageException e) {
                    e.printStackTrace();
                }
            }

            DocumentDAO docDAO = new DocumentDAO(em);
            docDAO.removeDoc(doc);
            docR.setCheckOutDate(null);
            docR.setCheckOutUser(null);
            return docR;
        } else {
            throw new NotAllowedException(new Locale(user.getLanguage()), "NotAllowedException19");
        }
    }

    @RolesAllowed("users")
    @Override
    public DocumentRevision checkInDocument(DocumentRevisionKey pDocRPK) throws WorkspaceNotFoundException, NotAllowedException, DocumentRevisionNotFoundException, AccessRightException, UserNotFoundException, UserNotActiveException, IndexerServerException {

        User user = checkDocumentRevisionWriteAccess(pDocRPK);

        DocumentRevisionDAO docRDAO = new DocumentRevisionDAO(new Locale(user.getLanguage()), em);
        DocumentRevision docR = docRDAO.loadDocR(pDocRPK);

        if (docR.isCheckedOut() && docR.getCheckOutUser().equals(user)) {
            SubscriptionDAO subscriptionDAO = new SubscriptionDAO(em);
            User[] subscribers = subscriptionDAO.getIterationChangeEventSubscribers(docR);
            GCMAccount[] gcmAccounts = subscriptionDAO.getIterationChangeEventSubscribersGCMAccount(docR);

            docR.setCheckOutDate(null);
            docR.setCheckOutUser(null);

            if (subscribers.length != 0) {
                mailer.sendIterationNotification(subscribers, docR);
            }

            if (gcmAccounts.length != 0) {
                gcmNotifier.sendIterationNotification(gcmAccounts, docR);
            }

            //esIndexer.index(docM.getLastIteration());                                                                 // Index the last iteration in ElasticSearch
            for(DocumentIteration docIteration :docR.getDocumentIterations()){
                esIndexer.index(docIteration);                                                                          // Index all iterations in ElasticSearch (decrease old iteration boost factor)
            }

            return docR;
        } else {
            throw new NotAllowedException(new Locale(user.getLanguage()), "NotAllowedException20");
        }
    }

    @RolesAllowed("users")
    @Override
    public DocumentRevisionKey[] deleteFolder(String pCompletePath) throws WorkspaceNotFoundException, NotAllowedException, AccessRightException, UserNotFoundException, FolderNotFoundException, IndexerServerException {
        User user = userManager.checkWorkspaceWriteAccess(Folder.parseWorkspaceId(pCompletePath));
        FolderDAO folderDAO = new FolderDAO(new Locale(user.getLanguage()), em);
        Folder folder = folderDAO.loadFolder(pCompletePath);
        String owner = folder.getOwner();
        checkFolderStructureRight(user);
        if (((owner != null) && (!owner.equals(user.getLogin()))) || (folder.isRoot()) || folder.isHome()) {
            throw new NotAllowedException(new Locale(user.getLanguage()), "NotAllowedException21");
        } else {
            List<DocumentRevision> docRs = folderDAO.removeFolder(folder);
            DocumentRevisionKey[] pks = new DocumentRevisionKey[docRs.size()];
            int i = 0;
            for (DocumentRevision docR : docRs) {
                pks[i++] = docR.getKey();
                for (DocumentIteration doc : docR.getDocumentIterations()) {
                    for (BinaryResource file : doc.getAttachedFiles()) {
                        //indexer.removeFromIndex(file.getFullName());
                        try {
                            dataManager.deleteData(file);
                        } catch (StorageException e) {
                            e.printStackTrace();
                        }
                    }
                    esIndexer.delete(doc);                                                                              // Remove ElasticSearch Index for this DocumentIteration
                }
            }
            return pks;
        }
    }

    @RolesAllowed("users")
    @Override
    public DocumentRevisionKey[] moveFolder(String pCompletePath, String pDestParentFolder, String pDestFolder) throws WorkspaceNotFoundException, NotAllowedException, AccessRightException, UserNotFoundException, FolderNotFoundException, CreationException, FolderAlreadyExistsException {
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
            List<DocumentRevision> docRs = folderDAO.moveFolder(folder, newFolder);
            DocumentRevisionKey[] pks = new DocumentRevisionKey[docRs.size()];
            int i = 0;
            for (DocumentRevision docR : docRs) {
                pks[i++] = docR.getKey();
            }
            return pks;
        }
    }

    @RolesAllowed("users")
    @Override
    public void deleteDocumentRevision(DocumentRevisionKey pDocRPK) throws WorkspaceNotFoundException, NotAllowedException, DocumentRevisionNotFoundException, AccessRightException, UserNotFoundException, UserNotActiveException, IndexerServerException {

        User user = checkDocumentRevisionWriteAccess(pDocRPK);
        DocumentMasterDAO documentMasterDAO = new DocumentMasterDAO(new Locale(user.getLanguage()), em);
        DocumentRevisionDAO docRDAO = new DocumentRevisionDAO(new Locale(user.getLanguage()), em);
        DocumentRevision docR = docRDAO.loadDocR(pDocRPK);
        DocumentMaster documentMaster = docR.getDocumentMaster();
        boolean isLastRevision = documentMaster.getDocumentRevisions().size() == 1;

        String owner = docR.getLocation().getOwner();
        if ((owner != null) && (!owner.equals(user.getLogin()))) {
            throw new NotAllowedException(new Locale(user.getLanguage()), "NotAllowedException22");
        }

        for (DocumentIteration doc : docR.getDocumentIterations()) {
            for (BinaryResource file : doc.getAttachedFiles()) {
                //indexer.removeFromIndex(file.getFullName());
                try {
                    dataManager.deleteData(file);
                } catch (StorageException e) {
                    e.printStackTrace();
                }

                esIndexer.delete(doc);                                                                                  // Remove ElasticSearch Index for this DocumentIteration
            }
        }

        if(isLastRevision){
            documentMasterDAO.removeDocM(documentMaster);
        }else{
            documentMaster.removeRevision(docR);
            docRDAO.removeRevision(docR);
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
            try {
                dataManager.deleteData(file);
            } catch (StorageException e) {
                e.printStackTrace();
            }
        }
    }

    @RolesAllowed("users")
    @Override
    public DocumentRevision removeFileFromDocument(String pFullName) throws WorkspaceNotFoundException, DocumentRevisionNotFoundException, NotAllowedException, AccessRightException, FileNotFoundException, UserNotFoundException, UserNotActiveException {
        User user = userManager.checkWorkspaceReadAccess(BinaryResource.parseWorkspaceId(pFullName));

        BinaryResourceDAO binDAO = new BinaryResourceDAO(new Locale(user.getLanguage()), em);
        BinaryResource file = binDAO.loadBinaryResource(pFullName);

        DocumentIteration document = binDAO.getDocumentOwner(file);
        DocumentRevision docR = document.getDocumentRevision();

        //check access rights on docR
        user = checkDocumentRevisionWriteAccess(docR.getKey());

        if (docR.isCheckedOut() && docR.getCheckOutUser().equals(user) && docR.getLastIteration().equals(document)) {
            try {
                dataManager.deleteData(file);
            } catch (StorageException e) {
                e.printStackTrace();
            }
            document.removeFile(file);
            binDAO.removeBinaryResource(file);
            return docR;
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

        DocumentMasterTemplate template = binDAO.getDocumentTemplateOwner(file);
        try {
            dataManager.deleteData(file);
        } catch (StorageException e) {
            e.printStackTrace();
        }
        template.removeFile(file);
        binDAO.removeBinaryResource(file);
        return template;
    }

    @RolesAllowed("users")
    @Override
    public DocumentRevision updateDocument(DocumentIterationKey iKey, String pRevisionNote, InstanceAttribute[] pAttributes, DocumentIterationKey[] pLinkKeys) throws WorkspaceNotFoundException, NotAllowedException, DocumentRevisionNotFoundException, AccessRightException, UserNotFoundException, UserNotActiveException {
        DocumentRevisionKey rKey = new DocumentRevisionKey(iKey.getWorkspaceId(), iKey.getDocumentMasterId(), iKey.getDocumentRevisionVersion());
        User user = checkDocumentRevisionWriteAccess(rKey);

        DocumentRevisionDAO docRDAO = new DocumentRevisionDAO(new Locale(user.getLanguage()), em);
        DocumentLinkDAO linkDAO = new DocumentLinkDAO(new Locale(user.getLanguage()), em);
        DocumentRevision docR = docRDAO.loadDocR(rKey);
        //check access rights on docR ?
        if (docR.isCheckedOut() && docR.getCheckOutUser().equals(user) && docR.getLastIteration().getKey().equals(iKey)) {
            DocumentIteration doc = docR.getLastIteration();

            Set<DocumentIterationKey> linkKeys = new HashSet<>(Arrays.asList(pLinkKeys));
            Set<DocumentIterationKey> currentLinkKeys = new HashSet<>();

            Set<DocumentLink> currentLinks = new HashSet<>(doc.getLinkedDocuments());

            for(DocumentLink link:currentLinks){
                DocumentIterationKey linkKey = link.getTargetDocumentKey();
                if(!linkKeys.contains(linkKey)){
                    doc.getLinkedDocuments().remove(link);
                }else
                    currentLinkKeys.add(linkKey);
            }

            for(DocumentIterationKey link:linkKeys){
                if(!currentLinkKeys.contains(link)){
                    DocumentLink newLink = new DocumentLink(em.getReference(DocumentIteration.class,link));
                    linkDAO.createLink(newLink);
                    doc.getLinkedDocuments().add(newLink);
                }
            }

            
            Map<String, InstanceAttribute> attrs = new HashMap<>();
            for (InstanceAttribute attr : pAttributes) {
                attrs.put(attr.getName(), attr);
            }

            Set<InstanceAttribute> currentAttrs = new HashSet<>(doc.getInstanceAttributes().values());

            for(InstanceAttribute attr:currentAttrs){
                if(!attrs.containsKey(attr.getName())){
                    doc.getInstanceAttributes().remove(attr.getName());
                }
            }

            for(InstanceAttribute attr:attrs.values()){
                if(!doc.getInstanceAttributes().containsKey(attr.getName())){
                    doc.getInstanceAttributes().put(attr.getName(), attr);
                }else if(doc.getInstanceAttributes().get(attr.getName()).getClass() != attr.getClass()){
                    doc.getInstanceAttributes().remove(attr.getName());
                    doc.getInstanceAttributes().put(attr.getName(), attr);
                }else{
                    doc.getInstanceAttributes().get(attr.getName()).setValue(attr.getValue());
                }
            }

            doc.setRevisionNote(pRevisionNote);
            //doc.setLinkedDocuments(links);
            return docR;

        } else {
            throw new NotAllowedException(new Locale(user.getLanguage()), "NotAllowedException25");
        }

    }

    @RolesAllowed("users")
    @Override
    public DocumentRevision[] createDocumentRevision(DocumentRevisionKey pOriginalDocRPK, String pTitle, String pDescription, String pWorkflowModelId, ACLUserEntry[] pACLUserEntries, ACLUserGroupEntry[] pACLUserGroupEntries, Map<String,String> roleMappings) throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException, NotAllowedException, DocumentRevisionAlreadyExistsException, CreationException, WorkflowModelNotFoundException, RoleNotFoundException, DocumentRevisionNotFoundException, FileAlreadyExistsException {
        User user = userManager.checkWorkspaceWriteAccess(pOriginalDocRPK.getWorkspaceId());
        DocumentRevisionDAO docRDAO = new DocumentRevisionDAO(new Locale(user.getLanguage()), em);
        DocumentRevision originalDocR = docRDAO.loadDocR(pOriginalDocRPK);
        DocumentMaster docM = originalDocR.getDocumentMaster();
        Folder folder = originalDocR.getLocation();
        checkWritingRight(user, folder);

        if (originalDocR.isCheckedOut()) {
            throw new NotAllowedException(new Locale(user.getLanguage()), "NotAllowedException26");
        }

        if (originalDocR.getNumberOfIterations() == 0) {
            throw new NotAllowedException(new Locale(user.getLanguage()), "NotAllowedException27");
        }

        DocumentRevision docR = docM.createNextRevision(user);

        //create the first iteration which is a copy of the last one of the original docR
        //of course we duplicate the iteration only if it exists !
        DocumentIteration lastDoc = originalDocR.getLastIteration();
        DocumentIteration firstIte = docR.createNextIteration(user);
        if (lastDoc != null) {
            BinaryResourceDAO binDAO = new BinaryResourceDAO(new Locale(user.getLanguage()), em);
            for (BinaryResource sourceFile : lastDoc.getAttachedFiles()) {
                String fileName = sourceFile.getName();
                long length = sourceFile.getContentLength();
                Date lastModified = sourceFile.getLastModified();
                String fullName = docR.getWorkspaceId() + "/documents/" + docR.getId() + "/" + docR.getVersion() + "/1/" + fileName;
                BinaryResource targetFile = new BinaryResource(fullName, length, lastModified);
                binDAO.createBinaryResource(targetFile);
                firstIte.addFile(targetFile);
                try {
                    dataManager.copyData(sourceFile, targetFile);
                } catch (StorageException e) {
                    e.printStackTrace();
                }
            }

            Set<DocumentLink> links = new HashSet<>();
            for (DocumentLink link : lastDoc.getLinkedDocuments()) {
                DocumentLink newLink = link.clone();
                links.add(newLink);
            }
            firstIte.setLinkedDocuments(links);

            Map<String, InstanceAttribute> attrs = new HashMap<>();
            for (InstanceAttribute attr : lastDoc.getInstanceAttributes().values()) {
                InstanceAttribute clonedAttribute = attr.clone();
                //clonedAttribute.setDocument(firstIte);
                attrs.put(clonedAttribute.getName(), clonedAttribute);
            }
            firstIte.setInstanceAttributes(attrs);
        }

        if (pWorkflowModelId != null) {

            UserDAO userDAO = new UserDAO(new Locale(user.getLanguage()),em);
            RoleDAO roleDAO = new RoleDAO(new Locale(user.getLanguage()),em);

            Map<Role,User> roleUserMap = new HashMap<>();

            for (Object o : roleMappings.entrySet()) {
                Map.Entry pairs = (Map.Entry) o;
                String roleName = (String) pairs.getKey();
                String userLogin = (String) pairs.getValue();
                User worker = userDAO.loadUser(new UserKey(pOriginalDocRPK.getWorkspaceId(), userLogin));
                Role role = roleDAO.loadRole(new RoleKey(pOriginalDocRPK.getWorkspaceId(), roleName));
                roleUserMap.put(role, worker);
            }

            WorkflowModel workflowModel = new WorkflowModelDAO(new Locale(user.getLanguage()), em).loadWorkflowModel(new WorkflowModelKey(user.getWorkspaceId(), pWorkflowModelId));
            Workflow workflow = workflowModel.createWorkflow(roleUserMap);
            docR.setWorkflow(workflow);

            Collection<Task> runningTasks = workflow.getRunningTasks();
            for (Task runningTask : runningTasks) {
                runningTask.start();
            }
            mailer.sendApproval(runningTasks, docR);
        }
        docR.setTitle(pTitle);
        docR.setDescription(pDescription);
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
            docR.setACL(acl);
        }
        Date now = new Date();
        docR.setCreationDate(now);
        docR.setLocation(folder);
        docR.setCheckOutUser(user);
        docR.setCheckOutDate(now);
        firstIte.setCreationDate(now);

        docRDAO.createDocR(docR);
        return new DocumentRevision[]{originalDocR, docR};
    }

    @RolesAllowed("users")
    @Override
    public void subscribeToStateChangeEvent(DocumentRevisionKey pDocRPK) throws WorkspaceNotFoundException, NotAllowedException, DocumentRevisionNotFoundException, UserNotFoundException, UserNotActiveException, AccessRightException {
        User user = checkDocumentRevisionReadAccess(pDocRPK);
        DocumentRevision docR = new DocumentRevisionDAO(new Locale(user.getLanguage()), em).loadDocR(pDocRPK);
        String owner = docR.getLocation().getOwner();
        if ((owner != null) && (!owner.equals(user.getLogin()))) {
            throw new NotAllowedException(new Locale(user.getLanguage()), "NotAllowedException30");
        }

        new SubscriptionDAO(em).createStateChangeSubscription(new StateChangeSubscription(user, docR));
    }

    @RolesAllowed("users")
    @Override
    public void unsubscribeToStateChangeEvent(DocumentRevisionKey pDocRPK) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException, AccessRightException, DocumentRevisionNotFoundException {
        User user = checkDocumentRevisionReadAccess(pDocRPK);
        SubscriptionKey key = new SubscriptionKey(user.getWorkspaceId(), user.getLogin(), pDocRPK.getDocumentMaster().getWorkspace(), pDocRPK.getDocumentMaster().getId(), pDocRPK.getVersion());
        new SubscriptionDAO(em).removeStateChangeSubscription(key);
    }

    @RolesAllowed("users")
    @Override
    public void subscribeToIterationChangeEvent(DocumentRevisionKey pDocRPK) throws WorkspaceNotFoundException, NotAllowedException, DocumentRevisionNotFoundException, UserNotFoundException, UserNotActiveException, AccessRightException {
        User user = checkDocumentRevisionReadAccess(pDocRPK);
        DocumentRevision docR = new DocumentRevisionDAO(new Locale(user.getLanguage()), em).getDocRRef(pDocRPK);
        String owner = docR.getLocation().getOwner();
        if ((owner != null) && (!owner.equals(user.getLogin()))) {
            throw new NotAllowedException(new Locale(user.getLanguage()), "NotAllowedException30");
        }

        new SubscriptionDAO(em).createIterationChangeSubscription(new IterationChangeSubscription(user, docR));
    }

    @RolesAllowed("users")
    @Override
    public void unsubscribeToIterationChangeEvent(DocumentRevisionKey pDocRPK) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException, AccessRightException, DocumentRevisionNotFoundException {
        User user = checkDocumentRevisionReadAccess(pDocRPK);
        SubscriptionKey key = new SubscriptionKey(user.getWorkspaceId(), user.getLogin(), pDocRPK.getDocumentMaster().getWorkspace(), pDocRPK.getDocumentMaster().getId(), pDocRPK.getVersion());
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

    @RolesAllowed({"users","admin"})
    @Override
    public User[] getUsers(String pWorkspaceId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
        return new UserDAO(new Locale(user.getLanguage()), em).findAllUsers(pWorkspaceId);
    }

    @RolesAllowed("users")
    @Override
    public User[] getReachableUsers() throws AccountNotFoundException {
        String callerLogin = ctx.getCallerPrincipal().getName();
        Account account = new AccountDAO(em).loadAccount(callerLogin);
        return new UserDAO(new Locale(account.getLanguage()), em).findReachableUsersForCaller(callerLogin);
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

    @RolesAllowed({"users","admin"})
    @Override
    public int getDocumentsCountInWorkspace(String pWorkspaceId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
        DocumentRevisionDAO documentRevisionDAO = new DocumentRevisionDAO(new Locale(user.getLanguage()),em);
        return documentRevisionDAO.getDocumentsCountInWorkspace(pWorkspaceId);
    }

    @RolesAllowed({"users","admin"})
    @Override
    public Long getDiskUsageForDocumentsInWorkspace(String pWorkspaceId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
        DocumentRevisionDAO documentRevisionDAO = new DocumentRevisionDAO(new Locale(user.getLanguage()),em);
        return documentRevisionDAO.getDiskUsageForDocumentsInWorkspace(pWorkspaceId);
    }

    @RolesAllowed({"users","admin"})
    @Override
    public Long getDiskUsageForDocumentTemplatesInWorkspace(String pWorkspaceId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
        DocumentRevisionDAO documentRevisionDAO = new DocumentRevisionDAO(new Locale(user.getLanguage()),em);
        return documentRevisionDAO.getDiskUsageForDocumentTemplatesInWorkspace(pWorkspaceId);
    }

    @RolesAllowed({"users"})
    @Override
    public SharedDocument createSharedDocument(DocumentRevisionKey pDocRPK, String pPassword, Date pExpireDate) throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException, DocumentRevisionNotFoundException, UserNotActiveException, NotAllowedException {
        User user = userManager.checkWorkspaceWriteAccess(pDocRPK.getDocumentMaster().getWorkspace());
        SharedDocument sharedDocument = new SharedDocument(user.getWorkspace(), user, pExpireDate, pPassword, getDocumentRevision(pDocRPK));
        SharedEntityDAO sharedEntityDAO = new SharedEntityDAO(new Locale(user.getLanguage()),em);
        sharedEntityDAO.createSharedDocument(sharedDocument);
        return sharedDocument;
    }

    @RolesAllowed({"users"})
    @Override
    public void deleteSharedDocument(SharedEntityKey sharedEntityKey) throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException, SharedEntityNotFoundException {
        User user = userManager.checkWorkspaceWriteAccess(sharedEntityKey.getWorkspace());
        SharedEntityDAO sharedEntityDAO = new SharedEntityDAO(new Locale(user.getLanguage()),em);
        SharedDocument sharedDocument = sharedEntityDAO.loadSharedDocument(sharedEntityKey.getUuid());
        sharedEntityDAO.deleteSharedDocument(sharedDocument);
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

    private User checkDocumentRevisionWriteAccess(DocumentRevisionKey documentRevisionKey) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, AccessRightException, DocumentRevisionNotFoundException {

        User user = userManager.checkWorkspaceReadAccess(documentRevisionKey.getDocumentMaster().getWorkspace());
        if(user.isAdministrator()){
            return user;
        }
        DocumentRevision documentRevision = new DocumentRevisionDAO(em).loadDocR(documentRevisionKey);

        if(documentRevision.getACL()==null){
            return userManager.checkWorkspaceWriteAccess(documentRevisionKey.getDocumentMaster().getWorkspace());
        }else if(documentRevision.getACL().hasWriteAccess(user)){
            return user;
        }else{
            throw new AccessRightException(new Locale(user.getLanguage()),user);
        }
    }

    private User checkDocumentRevisionReadAccess(DocumentRevisionKey documentRevisionKey) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, AccessRightException, DocumentRevisionNotFoundException {

        User user = userManager.checkWorkspaceReadAccess(documentRevisionKey.getDocumentMaster().getWorkspace());
        if(user.isAdministrator()){
            return user;
        }
        DocumentRevision documentRevision = new DocumentRevisionDAO(em).loadDocR(documentRevisionKey);

        if(documentRevision.getACL()==null){
            return user;
        }else{
            if(documentRevision.getACL().hasReadAccess(user)){
                return user;
            }else{
                throw new AccessRightException(new Locale(user.getLanguage()),user);
            }
        }

    }
}
