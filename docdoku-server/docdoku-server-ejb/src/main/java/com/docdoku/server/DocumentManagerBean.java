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

import com.docdoku.core.change.ChangeItem;
import com.docdoku.core.common.*;
import com.docdoku.core.document.*;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.gcm.GCMAccount;
import com.docdoku.core.log.DocumentLog;
import com.docdoku.core.meta.*;
import com.docdoku.core.product.PartRevision;
import com.docdoku.core.query.DocumentSearchQuery;
import com.docdoku.core.security.ACL;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.*;
import com.docdoku.core.sharing.SharedDocument;
import com.docdoku.core.sharing.SharedEntityKey;
import com.docdoku.core.util.NamingConvention;
import com.docdoku.core.util.Tools;
import com.docdoku.core.workflow.*;
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

@DeclareRoles({UserGroupMapping.REGULAR_USER_ROLE_ID, UserGroupMapping.ADMIN_ROLE_ID})
@Local(IDocumentManagerLocal.class)
@Stateless(name = "DocumentManagerBean")
public class DocumentManagerBean implements IDocumentManagerLocal {

    @PersistenceContext
    private EntityManager em;

    @Inject
    private IUserManagerLocal userManager;

    @Inject
    private IContextManagerLocal contextManager;

    @Inject
    private IMailerLocal mailer;

    @Inject
    private IGCMSenderLocal gcmNotifier;

    @Inject
    private ESIndexer esIndexer;

    @Inject
    private ESSearcher esSearcher;

    @Inject
    private IBinaryStorageManagerLocal storageManager;

    @Inject
    private Event<TagEvent> tagEvent;

    @Inject
    private Event<DocumentRevisionEvent> documentRevisionEvent;

    private static final Logger LOGGER = Logger.getLogger(DocumentManagerBean.class.getName());

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public BinaryResource saveFileInTemplate(DocumentMasterTemplateKey pDocMTemplateKey, String pName, long pSize) throws WorkspaceNotFoundException, NotAllowedException, DocumentMasterTemplateNotFoundException, FileAlreadyExistsException, UserNotFoundException, UserNotActiveException, CreationException, AccessRightException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceReadAccess(pDocMTemplateKey.getWorkspaceId());
        Locale locale = new Locale(user.getLanguage());
        checkNameFileValidity(pName, locale);

        DocumentMasterTemplateDAO templateDAO = new DocumentMasterTemplateDAO(locale, em);
        DocumentMasterTemplate template = templateDAO.loadDocMTemplate(pDocMTemplateKey);

        checkDocumentTemplateWriteAccess(template, user);

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
            new BinaryResourceDAO(locale, em).createBinaryResource(binaryResource);
            template.addFile(binaryResource);
        } else {
            binaryResource.setContentLength(pSize);
            binaryResource.setLastModified(new Date());
        }
        return binaryResource;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public BinaryResource saveFileInDocument(DocumentIterationKey pDocPK, String pName, long pSize) throws WorkspaceNotFoundException, NotAllowedException, DocumentRevisionNotFoundException, FileAlreadyExistsException, UserNotFoundException, UserNotActiveException, CreationException, AccessRightException, WorkspaceNotEnabledException {
        User user = checkDocumentRevisionWriteAccess(new DocumentRevisionKey(pDocPK.getWorkspaceId(), pDocPK.getDocumentMasterId(), pDocPK.getDocumentRevisionVersion()));
        Locale locale = new Locale(user.getLanguage());
        checkNameFileValidity(pName, locale);

        DocumentRevisionDAO docRDAO = new DocumentRevisionDAO(locale, em);
        DocumentRevision docR = docRDAO.loadDocR(new DocumentRevisionKey(pDocPK.getWorkspaceId(), pDocPK.getDocumentMasterId(), pDocPK.getDocumentRevisionVersion()));
        DocumentIteration document = docR.getIteration(pDocPK.getIteration());

        if (isCheckoutByUser(user, docR) && docR.getLastIteration().equals(document)) {
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
                new BinaryResourceDAO(locale, em).createBinaryResource(binaryResource);
                document.addFile(binaryResource);
            } else {
                binaryResource.setContentLength(pSize);
                binaryResource.setLastModified(new Date());
            }
            return binaryResource;
        } else {
            throw new NotAllowedException(locale, "NotAllowedException4");
        }
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public void setDocumentPublicShared(DocumentRevisionKey pDocRPK, boolean isPublicShared) throws AccessRightException, NotAllowedException, WorkspaceNotFoundException, UserNotFoundException, DocumentRevisionNotFoundException, UserNotActiveException, WorkspaceNotEnabledException {
        DocumentRevision documentRevision = getDocumentRevision(pDocRPK);
        documentRevision.setPublicShared(isPublicShared);
    }

    @LogDocument
    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID})
    @Override
    public BinaryResource getBinaryResource(String pFullName) throws WorkspaceNotFoundException, NotAllowedException, FileNotFoundException, UserNotFoundException, UserNotActiveException, AccessRightException, WorkspaceNotEnabledException {

        User user = userManager.checkWorkspaceReadAccess(BinaryResource.parseWorkspaceId(pFullName));
        Locale userLocale = new Locale(user.getLanguage());
        BinaryResourceDAO binDAO = new BinaryResourceDAO(userLocale, em);
        BinaryResource binaryResource = binDAO.loadBinaryResource(pFullName);

        DocumentIteration document = binDAO.getDocumentHolder(binaryResource);
        if (document != null) {
            DocumentRevision docR = document.getDocumentRevision();

            if (user.isAdministrator()) {
                return binaryResource;
            }

            if (isACLGrantReadAccess(user, docR)) {
                if ((isInAnotherUserHomeFolder(user, docR) || isCheckoutByAnotherUser(user, docR)) && docR.getLastIteration().equals(document)) {
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
    public String[] getFolders(String pCompletePath) throws WorkspaceNotFoundException, FolderNotFoundException, UserNotFoundException, UserNotActiveException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceReadAccess(Folder.parseWorkspaceId(pCompletePath));

        Folder folder = em.find(Folder.class, pCompletePath);
        if (folder == null) {
            throw new FolderNotFoundException(new Locale(user.getLanguage()), pCompletePath);
        }

        Folder[] subFolders = new FolderDAO(new Locale(user.getLanguage()), em).getSubFolders(pCompletePath);
        String[] shortNames = new String[subFolders.length];
        int i = 0;
        for (Folder f : subFolders) {
            shortNames[i++] = f.getShortName();
        }
        return shortNames;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public DocumentRevision[] findDocumentRevisionsByFolder(String pCompletePath) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException, WorkspaceNotEnabledException {
        String workspaceId = Folder.parseWorkspaceId(pCompletePath);
        User user = userManager.checkWorkspaceReadAccess(workspaceId);
        List<DocumentRevision> docRs = new DocumentRevisionDAO(new Locale(user.getLanguage()), em).findDocRsByFolder(pCompletePath);
        ListIterator<DocumentRevision> ite = docRs.listIterator();
        while (ite.hasNext()) {
            DocumentRevision docR = ite.next();
            if (!hasDocumentRevisionReadAccess(user, docR)) {
                ite.remove();
            } else if (isCheckoutByAnotherUser(user, docR)) {
                em.detach(docR);
                docR.removeLastIteration();
            }
        }
        return docRs.toArray(new DocumentRevision[docRs.size()]);
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public DocumentRevision[] findDocumentRevisionsByTag(TagKey pKey) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException, WorkspaceNotEnabledException {
        String workspaceId = pKey.getWorkspace();
        User user = userManager.checkWorkspaceReadAccess(workspaceId);
        List<DocumentRevision> docRs = new DocumentRevisionDAO(new Locale(user.getLanguage()), em).findDocRsByTag(new Tag(user.getWorkspace(), pKey.getLabel()));
        ListIterator<DocumentRevision> ite = docRs.listIterator();
        while (ite.hasNext()) {
            DocumentRevision docR = ite.next();
            if (!hasDocumentRevisionReadAccess(user, docR)) {
                ite.remove();
            } else if (isCheckoutByAnotherUser(user, docR)) {
                em.detach(docR);
                docR.removeLastIteration();
            }
        }
        return docRs.toArray(new DocumentRevision[docRs.size()]);
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID})
    @Override
    public DocumentRevision getDocumentRevision(DocumentRevisionKey pDocRPK) throws WorkspaceNotFoundException, DocumentRevisionNotFoundException, NotAllowedException, UserNotFoundException, UserNotActiveException, AccessRightException, WorkspaceNotEnabledException {

        User user = userManager.checkWorkspaceReadAccess(pDocRPK.getDocumentMaster().getWorkspace());
        Locale userLocale = new Locale(user.getLanguage());
        DocumentRevision docR = new DocumentRevisionDAO(userLocale, em).loadDocR(pDocRPK);
        if (isAnotherUserHomeFolder(user, docR.getLocation())) {
            throw new NotAllowedException(userLocale, "NotAllowedException5");
        }

        if (hasDocumentRevisionReadAccess(user, docR)) {
            if (isCheckoutByAnotherUser(user, docR)) {
                em.detach(docR);
                docR.removeLastIteration();
            }
            return docR;

        } else {
            throw new AccessRightException(userLocale, user);
        }

    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID})
    @Override
    public DocumentIteration findDocumentIterationByBinaryResource(BinaryResource pBinaryResource) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceReadAccess(pBinaryResource.getWorkspaceId());
        DocumentRevisionDAO documentRevisionDAO = new DocumentRevisionDAO(new Locale(user.getLanguage()), em);
        return documentRevisionDAO.findDocumentIterationByBinaryResource(pBinaryResource);
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public void updateDocumentACL(String pWorkspaceId, DocumentRevisionKey docKey, Map<String, String> pACLUserEntries, Map<String, String> pACLUserGroupEntries) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, DocumentRevisionNotFoundException, AccessRightException, NotAllowedException, WorkspaceNotEnabledException {
        User user = checkDocumentRevisionWriteAccess(docKey);
        Locale userLocale = new Locale(user.getLanguage());
        ACLFactory aclFactory = new ACLFactory(em);

        DocumentRevisionDAO documentRevisionDAO = new DocumentRevisionDAO(userLocale, em);
        DocumentRevision docR = documentRevisionDAO.loadDocR(docKey);
        if (user.isAdministrator() || isAuthor(user, docR)) {

            if (docR.getACL() == null) {
                ACL acl = aclFactory.createACL(pWorkspaceId, pACLUserEntries, pACLUserGroupEntries);
                docR.setACL(acl);

            } else {
                aclFactory.updateACL(pWorkspaceId, docR.getACL(), pACLUserEntries, pACLUserGroupEntries);
            }

        } else {
            throw new AccessRightException(userLocale, user);
        }
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public void updateACLForDocumentMasterTemplate(String pWorkspaceId, String pDocMTemplateId, Map<String, String> userEntries, Map<String, String> userGroupEntries) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, AccessRightException, NotAllowedException, DocumentMasterTemplateNotFoundException, WorkspaceNotEnabledException {
        ACLFactory aclFactory = new ACLFactory(em);

        // Check the read access to the workspace
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
        // Load the documentTemplateModel
        Locale locale = new Locale(user.getLanguage());
        DocumentMasterTemplate docTemplate = new DocumentMasterTemplateDAO(locale, em).loadDocMTemplate(new DocumentMasterTemplateKey(user.getWorkspaceId(), pDocMTemplateId));
        // Check the access to the documentTemplate
        checkDocumentTemplateWriteAccess(docTemplate, user);
        if (docTemplate.getAcl() == null) {
            ACL acl = aclFactory.createACL(pWorkspaceId, userEntries, userGroupEntries);
            docTemplate.setAcl(acl);
        } else {
            aclFactory.updateACL(pWorkspaceId, docTemplate.getAcl(), userEntries, userGroupEntries);
        }
    }


    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public void removeACLFromDocumentRevision(DocumentRevisionKey documentRevisionKey) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, DocumentRevisionNotFoundException, AccessRightException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceReadAccess(documentRevisionKey.getDocumentMaster().getWorkspace());

        Locale locale = new Locale(user.getLanguage());
        DocumentRevision docR = new DocumentRevisionDAO(locale, em).getDocRRef(documentRevisionKey);

        if (user.isAdministrator() || isAuthor(user, docR)) {
            ACL acl = docR.getACL();
            if (acl != null) {
                new ACLDAO(em).removeACLEntries(acl);
                docR.setACL(null);
            }
        } else {
            throw new AccessRightException(locale, user);
        }
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public void removeACLFromDocumentMasterTemplate(String pWorkspaceId, String documentTemplateId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, AccessRightException, DocumentMasterTemplateNotFoundException, WorkspaceNotEnabledException {

        // Check the read access to the workspace
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
        // Load the documentTemplateModel
        Locale locale = new Locale(user.getLanguage());
        DocumentMasterTemplate docTemplate = new DocumentMasterTemplateDAO(locale, em).loadDocMTemplate(new DocumentMasterTemplateKey(user.getWorkspaceId(), documentTemplateId));

        // Check the access to the workflow
        checkDocumentTemplateWriteAccess(docTemplate, user);

        ACL acl = docTemplate.getAcl();
        if (acl != null) {
            new ACLDAO(em).removeACLEntries(acl);
            docTemplate.setAcl(null);
        }

    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public DocumentRevision[] getAllDocumentsInWorkspace(String workspaceId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceReadAccess(workspaceId);
        List<DocumentRevision> docRs = new DocumentRevisionDAO(new Locale(user.getLanguage()), em).getAllDocumentRevisions(workspaceId);
        List<DocumentRevision> documentRevisions = new ArrayList<>();
        for (DocumentRevision docR : docRs) {
            if (isCheckoutByAnotherUser(user, docR)) {
                em.detach(docR);
                docR.removeLastIteration();
            }
            documentRevisions.add(docR);
        }
        return documentRevisions.toArray(new DocumentRevision[documentRevisions.size()]);
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public DocumentRevision[] getFilteredDocumentsInWorkspace(String workspaceId, int start, int pMaxResults) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceReadAccess(workspaceId);
        List<DocumentRevision> docRs = new DocumentRevisionDAO(new Locale(user.getLanguage()), em).getDocumentRevisionsFiltered(user, workspaceId, start, pMaxResults);
        List<DocumentRevision> documentRevisions = new ArrayList<>();
        for (DocumentRevision docR : docRs) {
            if (isCheckoutByAnotherUser(user, docR)) {
                em.detach(docR);
                docR.removeLastIteration();
            }
            documentRevisions.add(docR);
        }
        return documentRevisions.toArray(new DocumentRevision[documentRevisions.size()]);
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID, UserGroupMapping.ADMIN_ROLE_ID})
    @Override
    public int getDocumentsInWorkspaceCount(String workspaceId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, WorkspaceNotEnabledException, AccountNotFoundException {

        int count;

        if (contextManager.isCallerInRole(UserGroupMapping.ADMIN_ROLE_ID)) {
            Account adminAccount = new AccountDAO(em).loadAccount(contextManager.getCallerPrincipalLogin());
            Locale locale = new Locale(adminAccount.getLanguage());
            count = new DocumentRevisionDAO(locale, em).getTotalNumberOfDocuments(workspaceId);
        } else {
            User user = userManager.checkWorkspaceReadAccess(workspaceId);
            Locale locale = new Locale(user.getLanguage());
            if (user.isAdministrator()) {
                count = new DocumentRevisionDAO(locale, em).getTotalNumberOfDocuments(workspaceId);
            } else {
                count = new DocumentRevisionDAO(locale, em).getDocumentRevisionsCountFiltered(user, workspaceId);
            }
        }

        return count;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public DocumentRevision[] getCheckedOutDocumentRevisions(String pWorkspaceId) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
        List<DocumentRevision> docRs = new DocumentRevisionDAO(new Locale(user.getLanguage()), em).findCheckedOutDocRs(user);
        return docRs.toArray(new DocumentRevision[docRs.size()]);
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public Task[] getTasks(String pWorkspaceId) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
        return new TaskDAO(new Locale(user.getLanguage()), em).findTasks(user);
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public DocumentRevisionKey[] getIterationChangeEventSubscriptions(String pWorkspaceId) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
        return new SubscriptionDAO(em).getIterationChangeEventSubscriptions(user);
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID})
    @Override
    public DocumentRevisionKey[] getStateChangeEventSubscriptions(String pWorkspaceId) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
        return new SubscriptionDAO(em).getStateChangeEventSubscriptions(user);
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public boolean isUserStateChangeEventSubscribedForGivenDocument(String pWorkspaceId, DocumentRevision docR) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
        return new SubscriptionDAO(em).isUserStateChangeEventSubscribedForGivenDocument(user, docR);
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public boolean isUserIterationChangeEventSubscribedForGivenDocument(String pWorkspaceId, DocumentRevision docR) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
        return new SubscriptionDAO(em).isUserIterationChangeEventSubscribedForGivenDocument(user, docR);
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public DocumentRevision[] getDocumentRevisionsWithReferenceOrTitle(String pWorkspaceId, String search, int maxResults) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
        List<DocumentRevision> docRs = new DocumentRevisionDAO(new Locale(user.getLanguage()), em).findDocsRevisionsWithReferenceOrTitleLike(pWorkspaceId, search, maxResults);
        return docRs.toArray(new DocumentRevision[docRs.size()]);
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public String generateId(String pWorkspaceId, String pDocMTemplateId) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException, DocumentMasterTemplateNotFoundException, WorkspaceNotEnabledException {

        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
        Locale locale = new Locale(user.getLanguage());
        DocumentMasterTemplate template = new DocumentMasterTemplateDAO(locale, em).loadDocMTemplate(new DocumentMasterTemplateKey(user.getWorkspaceId(), pDocMTemplateId));

        String newId = null;
        try {
            String latestId = new DocumentRevisionDAO(locale, em).findLatestDocMId(pWorkspaceId, template.getDocumentType());
            String inputMask = template.getMask();
            String convertedMask = Tools.convertMask(inputMask);
            newId = Tools.increaseId(latestId, convertedMask);
        } catch (NoResultException ex) {
            LOGGER.log(Level.FINER, null, ex);
            //may happen when no document of the specified type has been created
        } catch (ParseException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            //may happen when a different mask has been used for the same document type
        }
        return newId;

    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public DocumentRevision[] searchDocumentRevisions(DocumentSearchQuery pQuery) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException, ESServerException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceReadAccess(pQuery.getWorkspaceId());
        List<DocumentRevision> fetchedDocRs = esSearcher.search(pQuery);                                                // Get Search Results
        List<DocumentRevision> docList = new ArrayList<>();

        if (!fetchedDocRs.isEmpty()) {
            for (DocumentRevision docR : fetchedDocRs) {
                DocumentRevision filteredDocR = applyDocumentRevisionReadAccess(user, docR);
                if (filteredDocR != null) {
                    docList.add(filteredDocR);
                }
            }

            return docList.toArray(new DocumentRevision[docList.size()]);
        }
        return new DocumentRevision[0];
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public DocumentMasterTemplate[] getDocumentMasterTemplates(String pWorkspaceId) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
        List<DocumentMasterTemplate> templates = new DocumentMasterTemplateDAO(new Locale(user.getLanguage()), em).findAllDocMTemplates(pWorkspaceId);

        ListIterator<DocumentMasterTemplate> ite = templates.listIterator();

        while (ite.hasNext()) {
            DocumentMasterTemplate template = ite.next();
            if (!hasDocumentMasterTemplateReadAccess(template, user)) {
                ite.remove();
            }
        }

        return templates.toArray(new DocumentMasterTemplate[templates.size()]);

    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public DocumentMasterTemplate getDocumentMasterTemplate(DocumentMasterTemplateKey pKey)
            throws WorkspaceNotFoundException, DocumentMasterTemplateNotFoundException, UserNotFoundException, UserNotActiveException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceReadAccess(pKey.getWorkspaceId());
        return new DocumentMasterTemplateDAO(new Locale(user.getLanguage()), em).loadDocMTemplate(pKey);
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public DocumentMasterTemplate updateDocumentMasterTemplate(DocumentMasterTemplateKey pKey, String pDocumentType, String pWorkflowModelId, String pMask, List<InstanceAttributeTemplate> pAttributeTemplates, String[] lovNames, boolean idGenerated, boolean attributesLocked) throws WorkspaceNotFoundException, AccessRightException, DocumentMasterTemplateNotFoundException, UserNotFoundException, WorkflowModelNotFoundException, UserNotActiveException, ListOfValuesNotFoundException, NotAllowedException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceReadAccess(pKey.getWorkspaceId());
        Locale locale = new Locale(user.getLanguage());

        DocumentMasterTemplateDAO templateDAO = new DocumentMasterTemplateDAO(new Locale(user.getLanguage()), em);
        DocumentMasterTemplate template = templateDAO.loadDocMTemplate(pKey);

        checkDocumentTemplateWriteAccess(template, user);

        Date now = new Date();
        template.setModificationDate(now);
        template.setAuthor(user);
        template.setDocumentType(pDocumentType);
        template.setMask(pMask);
        template.setIdGenerated(idGenerated);
        template.setAttributesLocked(attributesLocked);
        LOVDAO lovDAO = new LOVDAO(locale, em);

        List<InstanceAttributeTemplate> attrs = new ArrayList<>();
        for (int i = 0; i < pAttributeTemplates.size(); i++) {
            pAttributeTemplates.get(i).setLocked(attributesLocked);
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

        WorkflowModel workflowModel = null;
        if (pWorkflowModelId != null) {
            workflowModel = new WorkflowModelDAO(locale, em).loadWorkflowModel(new WorkflowModelKey(user.getWorkspaceId(), pWorkflowModelId));
        }
        template.setWorkflowModel(workflowModel);

        return template;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public void deleteTag(TagKey pKey) throws WorkspaceNotFoundException, AccessRightException, TagNotFoundException, UserNotFoundException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceWriteAccess(pKey.getWorkspace());
        Locale userLocale = new Locale(user.getLanguage());
        Tag tagToRemove = new Tag(user.getWorkspace(), pKey.getLabel());
        List<DocumentRevision> docRs = new DocumentRevisionDAO(userLocale, em).findDocRsByTag(tagToRemove);
        for (DocumentRevision docR : docRs) {
            docR.getTags().remove(tagToRemove);
        }
        List<ChangeItem> changeItems = new ChangeItemDAO(userLocale, em).findChangeItemByTag(pKey.getWorkspace(), tagToRemove);
        for (ChangeItem changeItem : changeItems) {
            changeItem.getTags().remove(tagToRemove);
        }

        List<PartRevision> partRevisions = new PartRevisionDAO(userLocale, em).findPartByTag(tagToRemove);
        for (PartRevision partRevision : partRevisions) {
            partRevision.getTags().remove(tagToRemove);
        }

        tagEvent.select(new AnnotationLiteral<Removed>() {
        }).fire(new TagEvent(tagToRemove));

        new TagDAO(userLocale, em).removeTag(pKey);
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public void createTag(String pWorkspaceId, String pLabel) throws WorkspaceNotFoundException, AccessRightException, CreationException, TagAlreadyExistsException, UserNotFoundException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceWriteAccess(pWorkspaceId);
        Locale userLocale = new Locale(user.getLanguage());
        TagDAO tagDAO = new TagDAO(userLocale, em);
        Tag tag = new Tag(user.getWorkspace(), pLabel);
        tagDAO.createTag(tag);
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public DocumentRevision createDocumentMaster(String pParentFolder, String pDocMId, String pTitle, String pDescription, String pDocMTemplateId, String pWorkflowModelId, Map<String, String> aclUserEntries, Map<String, String> aclGroupEntries, Map<String, Collection<String>> userRoleMapping, Map<String, Collection<String>> groupRoleMapping) throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException, NotAllowedException, FolderNotFoundException, DocumentMasterTemplateNotFoundException, FileAlreadyExistsException, CreationException, DocumentRevisionAlreadyExistsException, RoleNotFoundException, WorkflowModelNotFoundException, DocumentMasterAlreadyExistsException, UserGroupNotFoundException, WorkspaceNotEnabledException {

        User user = userManager.checkWorkspaceWriteAccess(Folder.parseWorkspaceId(pParentFolder));
        Locale locale = new Locale(user.getLanguage());
        checkNameValidity(pDocMId, locale);

        Folder folder = new FolderDAO(locale, em).loadFolder(pParentFolder);
        checkFolderWritingRight(user, folder);

        DocumentMaster docM;
        DocumentRevision docR;
        DocumentIteration newDoc;

        DocumentMasterDAO docMDAO = new DocumentMasterDAO(locale, em);
        if (pDocMTemplateId == null) {
            docM = new DocumentMaster(user.getWorkspace(), pDocMId, user);
            //specify an empty type instead of null
            //so the search will find it with the % character
            docM.setType("");
            docMDAO.createDocM(docM);
            docR = docM.createNextRevision(user);
            newDoc = docR.createNextIteration(user);
        } else {
            DocumentMasterTemplate template = new DocumentMasterTemplateDAO(locale, em).loadDocMTemplate(new DocumentMasterTemplateKey(user.getWorkspaceId(), pDocMTemplateId));

            if (!Tools.validateMask(template.getMask(), pDocMId)) {
                throw new NotAllowedException(locale, "NotAllowedException42");
            }

            docM = new DocumentMaster(user.getWorkspace(), pDocMId, user);
            docM.setType(template.getDocumentType());
            docM.setAttributesLocked(template.isAttributesLocked());

            docMDAO.createDocM(docM);
            docR = docM.createNextRevision(user);
            newDoc = docR.createNextIteration(user);


            List<InstanceAttribute> attrs = new ArrayList<>();
            for (InstanceAttributeTemplate attrTemplate : template.getAttributeTemplates()) {
                InstanceAttribute attr = attrTemplate.createInstanceAttribute();
                attrs.add(attr);
            }
            newDoc.setInstanceAttributes(attrs);

            BinaryResourceDAO binDAO = new BinaryResourceDAO(locale, em);
            for (BinaryResource sourceFile : template.getAttachedFiles()) {
                String fileName = sourceFile.getName();
                long length = sourceFile.getContentLength();
                Date lastModified = sourceFile.getLastModified();
                String fullName = docM.getWorkspaceId() + "/documents/" + docM.getId() + "/A/1/" + fileName;
                BinaryResource targetFile = new BinaryResource(fullName, length, lastModified);
                binDAO.createBinaryResource(targetFile);

                newDoc.addFile(targetFile);
                try {
                    storageManager.copyData(sourceFile, targetFile);
                } catch (StorageException e) {
                    LOGGER.log(Level.INFO, null, e);
                }
            }
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
                Role role = roleDAO.loadRole(new RoleKey(Folder.parseWorkspaceId(pParentFolder), roleName));
                Set<User> users = new HashSet<>();
                roleUserMap.put(role, users);
                for (String login : userLogins) {
                    User u = userDAO.loadUser(new UserKey(Folder.parseWorkspaceId(pParentFolder), login));
                    users.add(u);
                }
            }

            Map<Role, Collection<UserGroup>> roleGroupMap = new HashMap<>();
            for (Map.Entry<String, Collection<String>> pair : groupRoleMapping.entrySet()) {
                String roleName = pair.getKey();
                Collection<String> groupIds = pair.getValue();
                Role role = roleDAO.loadRole(new RoleKey(Folder.parseWorkspaceId(pParentFolder), roleName));
                Set<UserGroup> groups = new HashSet<>();
                roleGroupMap.put(role, groups);
                for (String groupId : groupIds) {
                    UserGroup g = groupDAO.loadUserGroup(new UserGroupKey(Folder.parseWorkspaceId(pParentFolder), groupId));
                    groups.add(g);
                }
            }

            WorkflowModel workflowModel = new WorkflowModelDAO(locale, em).loadWorkflowModel(new WorkflowModelKey(user.getWorkspaceId(), pWorkflowModelId));
            Workflow workflow = workflowModel.createWorkflow(roleUserMap, roleGroupMap);
            docR.setWorkflow(workflow);

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

        docR.setTitle(pTitle);
        docR.setDescription(pDescription);

        if (aclUserEntries != null && !aclUserEntries.isEmpty() || aclGroupEntries != null && !aclGroupEntries.isEmpty()) {
            ACL acl = new ACLFactory(em).createACL(user.getWorkspace().getId(), aclUserEntries, aclGroupEntries);
            docR.setACL(acl);
        }

        Date now = new Date();
        docM.setCreationDate(now);
        docR.setCreationDate(now);
        docR.setLocation(folder);
        docR.setCheckOutUser(user);
        docR.setCheckOutDate(now);
        newDoc.setCreationDate(now);
        new DocumentRevisionDAO(locale, em).createDocR(docR);

        if (runningTasks != null) {
            mailer.sendApproval(runningTasks, docR);
        }
        return docR;
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID, UserGroupMapping.ADMIN_ROLE_ID})
    @Override
    public DocumentRevision[] getAllCheckedOutDocumentRevisions(String pWorkspaceId) throws WorkspaceNotFoundException, AccountNotFoundException, AccessRightException {
        Account account = userManager.checkAdmin(pWorkspaceId);
        List<DocumentRevision> docRs = new DocumentRevisionDAO(new Locale(account.getLanguage()), em).findAllCheckedOutDocRevisions(pWorkspaceId);
        return docRs.toArray(new DocumentRevision[docRs.size()]);
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public DocumentMasterTemplate createDocumentMasterTemplate(String pWorkspaceId, String pId, String pDocumentType, String pWorkflowModelId,
                                                               String pMask, List<InstanceAttributeTemplate> pAttributeTemplates, String[] lovNames, boolean idGenerated, boolean attributesLocked) throws WorkspaceNotFoundException, AccessRightException, DocumentMasterTemplateAlreadyExistsException, UserNotFoundException, NotAllowedException, CreationException, WorkflowModelNotFoundException, ListOfValuesNotFoundException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceWriteAccess(pWorkspaceId);
        Locale locale = new Locale(user.getLanguage());
        checkNameValidity(pId, locale);

        //Check pMask
        if (pMask != null && !pMask.isEmpty() && !NamingConvention.correctNameMask(pMask)) {
            throw new NotAllowedException(locale, "MaskCreationException");
        }

        DocumentMasterTemplate template = new DocumentMasterTemplate(user.getWorkspace(), pId, user, pDocumentType, pMask);
        Date now = new Date();
        template.setCreationDate(now);
        template.setIdGenerated(idGenerated);
        template.setAttributesLocked(attributesLocked);

        LOVDAO lovDAO = new LOVDAO(locale, em);

        List<InstanceAttributeTemplate> attrs = new ArrayList<>();
        for (int i = 0; i < pAttributeTemplates.size(); i++) {
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

        if (pWorkflowModelId != null) {
            WorkflowModel workflowModel = new WorkflowModelDAO(locale, em).loadWorkflowModel(new WorkflowModelKey(user.getWorkspaceId(), pWorkflowModelId));
            template.setWorkflowModel(workflowModel);
        }

        new DocumentMasterTemplateDAO(locale, em).createDocMTemplate(template);
        return template;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public DocumentRevision moveDocumentRevision(String pParentFolder, DocumentRevisionKey pDocRPK) throws WorkspaceNotFoundException, DocumentRevisionNotFoundException, NotAllowedException, AccessRightException, FolderNotFoundException, UserNotFoundException, UserNotActiveException, WorkspaceNotEnabledException {
        //TODO security check if both parameter belong to the same workspace
        User user = checkDocumentRevisionWriteAccess(pDocRPK);

        Folder newLocation = new FolderDAO(new Locale(user.getLanguage()), em).loadFolder(pParentFolder);
        checkFolderWritingRight(user, newLocation);
        DocumentRevisionDAO docRDAO = new DocumentRevisionDAO(new Locale(user.getLanguage()), em);
        DocumentRevision docR = docRDAO.loadDocR(pDocRPK);

        // You cannot move a document to someone else's home directory
        if (isInAnotherUserHomeFolder(user, docR)) {
            throw new NotAllowedException(new Locale(user.getLanguage()), "NotAllowedException6");
        } else {

            docR.setLocation(newLocation);

            if (isCheckoutByAnotherUser(user, docR)) {
                // won't persist newLocation if not flushing
                em.flush();
                em.detach(docR);
                docR.removeLastIteration();
            }

            DocumentIteration lastCheckedInIteration = docR.getLastCheckedInIteration();

            if (null != lastCheckedInIteration) {
                esIndexer.index(lastCheckedInIteration);
            }

            return docR;
        }
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public Folder createFolder(String pParentFolder, String pFolder)
            throws WorkspaceNotFoundException, NotAllowedException, AccessRightException, FolderNotFoundException, FolderAlreadyExistsException, UserNotFoundException, CreationException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceWriteAccess(Folder.parseWorkspaceId(pParentFolder));
        Locale locale = new Locale(user.getLanguage());
        checkNameValidity(pFolder, locale);

        FolderDAO folderDAO = new FolderDAO(locale, em);
        Folder folder = folderDAO.loadFolder(pParentFolder);
        checkFoldersStructureChangeRight(user);
        checkFolderWritingRight(user, folder);
        Folder newFolder = new Folder(pParentFolder, pFolder);
        folderDAO.createFolder(newFolder);
        return newFolder;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public DocumentRevision checkOutDocument(DocumentRevisionKey pDocRPK) throws WorkspaceNotFoundException, NotAllowedException, DocumentRevisionNotFoundException, AccessRightException, FileAlreadyExistsException, UserNotFoundException, UserNotActiveException, CreationException, WorkspaceNotEnabledException {
        User user = checkDocumentRevisionWriteAccess(pDocRPK);
        Locale locale = new Locale(user.getLanguage());
        DocumentRevisionDAO docRDAO = new DocumentRevisionDAO(locale, em);
        DocumentRevision docR = docRDAO.loadDocR(pDocRPK);
        //Check access rights on docR

        if (docR.isCheckedOut()) {
            throw new NotAllowedException(locale, "NotAllowedException37");
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
            BinaryResourceDAO binDAO = new BinaryResourceDAO(locale, em);
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
            List<InstanceAttribute> attrs = new ArrayList<>();
            for (InstanceAttribute attr : beforeLastDocument.getInstanceAttributes()) {
                InstanceAttribute newAttr = attr.clone();
                //Workaround for the NULL DTYPE bug
                attrDAO.createAttribute(newAttr);
                attrs.add(newAttr);
            }
            newDoc.setInstanceAttributes(attrs);
        }

        return docR;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public DocumentRevision saveTags(DocumentRevisionKey pDocRPK, String[] pTags) throws WorkspaceNotFoundException, NotAllowedException, DocumentRevisionNotFoundException, AccessRightException, UserNotFoundException, UserNotActiveException, ESServerException, WorkspaceNotEnabledException {
        User user = checkDocumentRevisionWriteAccess(pDocRPK);

        Locale userLocale = new Locale(user.getLanguage());
        DocumentRevisionDAO docRDAO = new DocumentRevisionDAO(userLocale, em);
        DocumentRevision docR = docRDAO.loadDocR(pDocRPK);

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

            Set<Tag> removedTags = new HashSet<>(docR.getTags());
            removedTags.removeAll(tags);
            Set<Tag> addedTags = docR.setTags(tags);

            for (Tag tag : removedTags) {
                tagEvent.select(new AnnotationLiteral<Untagged>() {
                }).fire(new TagEvent(tag, docR));
            }
            for (Tag tag : addedTags) {
                tagEvent.select(new AnnotationLiteral<Tagged>() {
                }).fire(new TagEvent(tag, docR));
            }
            if (isCheckoutByAnotherUser(user, docR)) {
                em.flush();
                em.detach(docR);
                docR.removeLastIteration();
            }

            for (DocumentIteration documentIteration : docR.getDocumentIterations()) {
                esIndexer.index(documentIteration);
            }
        } else {
            throw new IllegalArgumentException("pTags argument must not be null");
        }
        return docR;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public DocumentRevision removeTag(DocumentRevisionKey pDocRPK, String pTag)
            throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, AccessRightException, DocumentRevisionNotFoundException, NotAllowedException, ESServerException, WorkspaceNotEnabledException {

        User user = checkDocumentRevisionWriteAccess(pDocRPK);

        DocumentRevision docR = getDocumentRevision(pDocRPK);
        Tag tagToRemove = new Tag(user.getWorkspace(), pTag);
        docR.getTags().remove(tagToRemove);

        tagEvent.select(new AnnotationLiteral<Untagged>() {
        }).fire(new TagEvent(tagToRemove, docR));

        if (isCheckoutByAnotherUser(user, docR)) {
            em.detach(docR);
            docR.removeLastIteration();
        }

        for (DocumentIteration documentIteration : docR.getDocumentIterations()) {
            esIndexer.index(documentIteration);
        }
        return docR;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public DocumentRevision undoCheckOutDocument(DocumentRevisionKey pDocRPK) throws WorkspaceNotFoundException, DocumentRevisionNotFoundException, NotAllowedException, UserNotFoundException, UserNotActiveException, AccessRightException, WorkspaceNotEnabledException {

        User user = checkDocumentRevisionWriteAccess(pDocRPK);
        Locale userLocale = new Locale(user.getLanguage());

        DocumentRevision docR = new DocumentRevisionDAO(userLocale, em).loadDocR(pDocRPK);
        if (isCheckoutByUser(user, docR)) {
            if (docR.getLastIteration().getIteration() <= 1) {
                throw new NotAllowedException(userLocale, "NotAllowedException27");
            }
            DocumentIteration doc = docR.removeLastIteration();

            DocumentDAO docDAO = new DocumentDAO(em);
            docDAO.removeDoc(doc);
            docR.setCheckOutDate(null);
            docR.setCheckOutUser(null);

            for (BinaryResource file : doc.getAttachedFiles()) {
                try {
                    storageManager.deleteData(file);
                } catch (StorageException e) {
                    LOGGER.log(Level.INFO, null, e);
                }
            }

            return docR;
        } else {
            throw new NotAllowedException(userLocale, "NotAllowedException19");
        }
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public DocumentRevision checkInDocument(DocumentRevisionKey pDocRPK) throws WorkspaceNotFoundException, NotAllowedException, DocumentRevisionNotFoundException, AccessRightException, UserNotFoundException, UserNotActiveException, ESServerException, WorkspaceNotEnabledException {

        User user = checkDocumentRevisionWriteAccess(pDocRPK);
        Locale userLocale = new Locale(user.getLanguage());

        DocumentRevisionDAO docRDAO = new DocumentRevisionDAO(userLocale, em);
        DocumentRevision docR = docRDAO.loadDocR(pDocRPK);

        if (isCheckoutByUser(user, docR)) {
            SubscriptionDAO subscriptionDAO = new SubscriptionDAO(em);
            Collection<User> subscribers = subscriptionDAO.getIterationChangeEventSubscribers(docR);
            GCMAccount[] gcmAccounts = subscriptionDAO.getIterationChangeEventSubscribersGCMAccount(docR);

            docR.setCheckOutDate(null);
            docR.setCheckOutUser(null);

            DocumentIteration lastIteration = docR.getLastIteration();
            lastIteration.setCheckInDate(new Date());

            if (!subscribers.isEmpty()) {
                mailer.sendIterationNotification(subscribers, docR);
            }

            if (gcmAccounts.length != 0) {
                gcmNotifier.sendIterationNotification(gcmAccounts, docR);
            }

            esIndexer.index(lastIteration);

            return docR;
        } else {
            throw new NotAllowedException(userLocale, "NotAllowedException20");
        }
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public DocumentRevisionKey[] deleteFolder(String pCompletePath) throws WorkspaceNotFoundException, NotAllowedException, AccessRightException, UserNotFoundException, FolderNotFoundException, ESServerException, EntityConstraintException, UserNotActiveException, DocumentRevisionNotFoundException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceWriteAccess(Folder.parseWorkspaceId(pCompletePath));
        Locale userLocale = new Locale(user.getLanguage());

        FolderDAO folderDAO = new FolderDAO(userLocale, em);
        Folder folder = folderDAO.loadFolder(pCompletePath);
        checkFoldersStructureChangeRight(user);

        if (isAnotherUserHomeFolder(user, folder) || folder.isRoot() || folder.isHome()) {
            throw new NotAllowedException(userLocale, "NotAllowedException21");

        } else {
            return doFolderDeletion(folder, userLocale);
        }
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public DocumentRevisionKey[] deleteUserFolder(User pUser) throws WorkspaceNotFoundException, NotAllowedException, AccessRightException, UserNotFoundException, FolderNotFoundException, ESServerException, EntityConstraintException, UserNotActiveException, DocumentRevisionNotFoundException, WorkspaceNotEnabledException {

        User user = userManager.checkWorkspaceWriteAccess(pUser.getWorkspaceId());
        Locale userLocale = new Locale(user.getLanguage());
        String folderCompletePath = pUser.getWorkspaceId() + "/~" + pUser.getLogin();
        FolderDAO folderDAO = new FolderDAO(userLocale, em);
        Folder folder = folderDAO.loadFolder(folderCompletePath);

        if (!user.isAdministrator()) {
            throw new NotAllowedException(userLocale, "NotAllowedException21");
        }

        return doFolderDeletion(folder, userLocale);
    }

    private DocumentRevisionKey[] doFolderDeletion(Folder folder, Locale locale) throws EntityConstraintException, NotAllowedException, WorkspaceNotFoundException, ESServerException, AccessRightException, DocumentRevisionNotFoundException, UserNotActiveException, UserNotFoundException, WorkspaceNotEnabledException {
        FolderDAO folderDAO = new FolderDAO(locale, em);
        List<DocumentRevision> allDocRevision = folderDAO.findDocumentRevisionsInFolder(folder);
        List<DocumentRevisionKey> allDocRevisionKey = new ArrayList<>();

        for (DocumentRevision documentRevision : allDocRevision) {
            DocumentRevisionKey documentRevisionKey = documentRevision.getKey();
            deleteDocumentRevision(documentRevisionKey);
            allDocRevisionKey.add(documentRevisionKey);
        }
        folderDAO.removeFolder(folder);
        return allDocRevisionKey.toArray(new DocumentRevisionKey[allDocRevisionKey.size()]);
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public DocumentRevisionKey[] moveFolder(String pCompletePath, String pDestParentFolder, String pDestFolder) throws WorkspaceNotFoundException, NotAllowedException, AccessRightException, UserNotFoundException, FolderNotFoundException, CreationException, FolderAlreadyExistsException, WorkspaceNotEnabledException {
        //TODO security check if both parameter belong to the same workspace
        String workspace = Folder.parseWorkspaceId(pCompletePath);
        User user = userManager.checkWorkspaceWriteAccess(workspace);
        Locale userLocale = new Locale(user.getLanguage());

        FolderDAO folderDAO = new FolderDAO(userLocale, em);
        Folder folder = folderDAO.loadFolder(pCompletePath);
        checkFoldersStructureChangeRight(user);
        if (isAnotherUserHomeFolder(user, folder) || folder.isRoot() || folder.isHome()) {
            throw new NotAllowedException(userLocale, "NotAllowedException21");
        } else if (!workspace.equals(Folder.parseWorkspaceId(pDestParentFolder))) {
            throw new NotAllowedException(userLocale, "NotAllowedException23");
        } else {
            Folder newFolder = createFolder(pDestParentFolder, pDestFolder);
            List<DocumentRevision> docRs = folderDAO.moveFolder(folder, newFolder);
            DocumentRevisionKey[] pks = new DocumentRevisionKey[docRs.size()];
            int i = 0;

            List<DocumentIteration> lastCheckedInIterations = new ArrayList<>();

            for (DocumentRevision docR : docRs) {
                pks[i++] = docR.getKey();
                DocumentIteration lastCheckedInIteration = docR.getLastCheckedInIteration();
                if (null != lastCheckedInIteration) {
                    lastCheckedInIterations.add(lastCheckedInIteration);
                }
            }

            if (!lastCheckedInIterations.isEmpty()) {
                esIndexer.indexMultiple(lastCheckedInIterations);
            }

            return pks;
        }
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public void deleteDocumentRevision(DocumentRevisionKey pDocRPK) throws WorkspaceNotFoundException, NotAllowedException, DocumentRevisionNotFoundException, AccessRightException, UserNotFoundException, UserNotActiveException, ESServerException, EntityConstraintException, WorkspaceNotEnabledException {

        User user = checkDocumentRevisionWriteAccess(pDocRPK);
        Locale locale = new Locale(user.getLanguage());

        DocumentMasterDAO documentMasterDAO = new DocumentMasterDAO(locale, em);
        DocumentRevisionDAO docRDAO = new DocumentRevisionDAO(locale, em);
        DocumentLinkDAO documentLinkDAO = new DocumentLinkDAO(locale, em);

        DocumentRevision docR = docRDAO.loadDocR(pDocRPK);
        if (!user.isAdministrator() && isInAnotherUserHomeFolder(user, docR)) {
            throw new NotAllowedException(locale, "NotAllowedException22");
        }

        DocumentBaselineDAO documentBaselineDAO = new DocumentBaselineDAO(em, locale);
        if (documentBaselineDAO.existBaselinedDocument(pDocRPK.getWorkspaceId(), pDocRPK.getDocumentMasterId(), pDocRPK.getVersion())) {
            throw new EntityConstraintException(locale, "EntityConstraintException6");
        }

        for (DocumentRevision documentRevision : docR.getDocumentMaster().getDocumentRevisions()) {
            if (!documentLinkDAO.getInverseDocumentsLinks(documentRevision).isEmpty()) {
                throw new EntityConstraintException(locale, "EntityConstraintException17");
            }
            if (!documentLinkDAO.getInversePartsLinks(documentRevision).isEmpty()) {

                throw new EntityConstraintException(locale, "EntityConstraintException18");
            }
            if (!documentLinkDAO.getInverseProductInstanceIteration(documentRevision).isEmpty()) {
                throw new EntityConstraintException(locale, "EntityConstraintException19");
            }
            if (!documentLinkDAO.getInversefindPathData(documentRevision).isEmpty()) {
                throw new EntityConstraintException(locale, "EntityConstraintException20");
            }

        }
        ChangeItemDAO changeItemDAO = new ChangeItemDAO(locale, em);
        if (changeItemDAO.hasChangeItems(pDocRPK)) {
            throw new EntityConstraintException(locale, "EntityConstraintException7");
        }

        DocumentMaster documentMaster = docR.getDocumentMaster();
        boolean isLastRevision = documentMaster.getDocumentRevisions().size() == 1;
        if (isLastRevision) {
            documentMasterDAO.removeDocM(documentMaster);
        } else {
            docRDAO.removeRevision(docR);
        }

        for (DocumentIteration doc : docR.getDocumentIterations()) {
            for (BinaryResource file : doc.getAttachedFiles()) {
                try {
                    storageManager.deleteData(file);
                } catch (StorageException e) {
                    LOGGER.log(Level.INFO, null, e);
                }

                esIndexer.delete(doc);
            }
        }
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public void deleteDocumentMasterTemplate(DocumentMasterTemplateKey pKey)
            throws WorkspaceNotFoundException, AccessRightException, DocumentMasterTemplateNotFoundException, UserNotFoundException, UserNotActiveException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceReadAccess(pKey.getWorkspaceId());
        DocumentMasterTemplateDAO templateDAO = new DocumentMasterTemplateDAO(new Locale(user.getLanguage()), em);

        DocumentMasterTemplate documentMasterTemplate = templateDAO.loadDocMTemplate(pKey);
        checkDocumentTemplateWriteAccess(documentMasterTemplate, user);

        DocumentMasterTemplate template = templateDAO.removeDocMTemplate(pKey);

        for (BinaryResource file : template.getAttachedFiles()) {
            try {
                storageManager.deleteData(file);
            } catch (StorageException e) {
                LOGGER.log(Level.INFO, null, e);
            }
        }
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public DocumentRevision removeFileFromDocument(String pFullName) throws WorkspaceNotFoundException, DocumentRevisionNotFoundException, NotAllowedException, AccessRightException, FileNotFoundException, UserNotFoundException, UserNotActiveException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceReadAccess(BinaryResource.parseWorkspaceId(pFullName));
        Locale userLocale = new Locale(user.getLanguage());

        BinaryResourceDAO binDAO = new BinaryResourceDAO(userLocale, em);
        BinaryResource file = binDAO.loadBinaryResource(pFullName);

        DocumentIteration document = binDAO.getDocumentHolder(file);
        DocumentRevision docR = document.getDocumentRevision();

        //check access rights on docR
        user = checkDocumentRevisionWriteAccess(docR.getKey());

        if (isCheckoutByUser(user, docR) && docR.getLastIteration().equals(document)) {
            document.removeFile(file);
            binDAO.removeBinaryResource(file);

            try {
                storageManager.deleteData(file);
            } catch (StorageException e) {
                LOGGER.log(Level.INFO, null, e);
            }

            return docR;

        } else {
            throw new NotAllowedException(userLocale, "NotAllowedException24");
        }
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public BinaryResource renameFileInDocument(String pFullName, String pNewName) throws WorkspaceNotFoundException, DocumentRevisionNotFoundException, NotAllowedException, AccessRightException, FileNotFoundException, UserNotFoundException, UserNotActiveException, FileAlreadyExistsException, CreationException, StorageException, WorkspaceNotEnabledException {

        User user = userManager.checkWorkspaceReadAccess(BinaryResource.parseWorkspaceId(pFullName));
        Locale userLocale = new Locale(user.getLanguage());
        checkNameFileValidity(pNewName, userLocale);

        BinaryResourceDAO binDAO = new BinaryResourceDAO(userLocale, em);
        BinaryResource file = binDAO.loadBinaryResource(pFullName);

        try {

            binDAO.loadBinaryResource(file.getNewFullName(pNewName));
            throw new FileAlreadyExistsException(userLocale, pNewName);

        } catch (FileNotFoundException e) {

            DocumentIteration document = binDAO.getDocumentHolder(file);
            DocumentRevision docR = document.getDocumentRevision();

            //check access rights on docR
            user = checkDocumentRevisionWriteAccess(docR.getKey());

            if (isCheckoutByUser(user, docR) && docR.getLastIteration().equals(document)) {
                storageManager.renameFile(file, pNewName);
                document.removeFile(file);
                binDAO.removeBinaryResource(file);
                BinaryResource newFile = new BinaryResource(file.getNewFullName(pNewName), file.getContentLength(), file.getLastModified());
                binDAO.createBinaryResource(newFile);
                document.addFile(newFile);
                return newFile;
            } else {
                throw new NotAllowedException(userLocale, "NotAllowedException29");
            }
        }

    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public DocumentMasterTemplate removeFileFromTemplate(String pFullName) throws WorkspaceNotFoundException, DocumentMasterTemplateNotFoundException, AccessRightException, FileNotFoundException, UserNotFoundException, UserNotActiveException, StorageException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceReadAccess(BinaryResource.parseWorkspaceId(pFullName));

        BinaryResourceDAO binDAO = new BinaryResourceDAO(new Locale(user.getLanguage()), em);
        BinaryResource file = binDAO.loadBinaryResource(pFullName);

        DocumentMasterTemplate template = binDAO.getDocumentTemplateHolder(file);
        checkDocumentTemplateWriteAccess(template, user);

        template.removeFile(file);
        binDAO.removeBinaryResource(file);

        try {
            storageManager.deleteData(file);
        } catch (StorageException e) {
            LOGGER.log(Level.INFO, null, e);
        }

        return template;
    }

    @Override
    public BinaryResource renameFileInTemplate(String pFullName, String pNewName) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, NotAllowedException, FileNotFoundException, AccessRightException, FileAlreadyExistsException, CreationException, StorageException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceReadAccess(BinaryResource.parseWorkspaceId(pFullName));
        Locale userLocale = new Locale(user.getLanguage());
        checkNameFileValidity(pNewName, userLocale);

        BinaryResourceDAO binDAO = new BinaryResourceDAO(userLocale, em);
        BinaryResource file = binDAO.loadBinaryResource(pFullName);

        try {
            binDAO.loadBinaryResource(file.getNewFullName(pNewName));
            throw new FileAlreadyExistsException(userLocale, pNewName);
        } catch (FileNotFoundException e) {
            DocumentMasterTemplate template = binDAO.getDocumentTemplateHolder(file);

            checkDocumentTemplateWriteAccess(template, user);

            storageManager.renameFile(file, pNewName);
            template.removeFile(file);
            binDAO.removeBinaryResource(file);

            BinaryResource newFile = new BinaryResource(file.getNewFullName(pNewName), file.getContentLength(), file.getLastModified());
            binDAO.createBinaryResource(newFile);
            template.addFile(newFile);
            return newFile;
        }

    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public DocumentRevision updateDocument(DocumentIterationKey iKey, String pRevisionNote, List<InstanceAttribute> pAttributes, DocumentRevisionKey[] pLinkKeys, String[] documentLinkComments) throws WorkspaceNotFoundException, NotAllowedException, DocumentRevisionNotFoundException, AccessRightException, UserNotFoundException, UserNotActiveException, WorkspaceNotEnabledException {
        DocumentRevisionKey rKey = new DocumentRevisionKey(iKey.getWorkspaceId(), iKey.getDocumentMasterId(), iKey.getDocumentRevisionVersion());
        User user = checkDocumentRevisionWriteAccess(rKey);
        Locale userLocale = new Locale(user.getLanguage());

        DocumentRevisionDAO docRDAO = new DocumentRevisionDAO(userLocale, em);
        DocumentLinkDAO linkDAO = new DocumentLinkDAO(userLocale, em);
        DocumentRevision docR = docRDAO.loadDocR(rKey);
        //check access rights on docR ?
        if (isCheckoutByUser(user, docR) && docR.getLastIteration().getKey().equals(iKey)) {
            DocumentIteration doc = docR.getLastIteration();

            if (pLinkKeys != null) {
                Set<DocumentLink> currentLinks = new HashSet<>(doc.getLinkedDocuments());

                for (DocumentLink link : currentLinks) {
                    doc.getLinkedDocuments().remove(link);
                }

                int counter = 0;
                for (DocumentRevisionKey link : pLinkKeys) {
                    if (!link.equals(iKey.getDocumentRevision())) {
                        DocumentLink newLink = new DocumentLink(docRDAO.loadDocR(link));
                        newLink.setComment(documentLinkComments[counter]);
                        linkDAO.createLink(newLink);
                        doc.getLinkedDocuments().add(newLink);
                        counter++;
                    }
                }
            }

            if (pAttributes != null) {
                List<InstanceAttribute> currentAttrs = doc.getInstanceAttributes();
                boolean valid = AttributesConsistencyUtils.hasValidChange(pAttributes, docR.isAttributesLocked(), currentAttrs);
                if (!valid) {
                    throw new NotAllowedException(userLocale, "NotAllowedException59");
                }
                doc.setInstanceAttributes(pAttributes);
            }

            doc.setRevisionNote(pRevisionNote);
            Date now = new Date();
            doc.setModificationDate(now);
            //doc.setLinkedDocuments(links);
            return docR;

        } else {
            throw new NotAllowedException(userLocale, "NotAllowedException25");
        }

    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public DocumentRevision[] createDocumentRevision(DocumentRevisionKey pOriginalDocRPK, String pTitle, String pDescription, String pWorkflowModelId, Map<String, String> aclUserEntries, Map<String, String> aclGroupEntries, Map<String, Collection<String>> userRoleMapping, Map<String, Collection<String>> groupRoleMapping) throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException, NotAllowedException, DocumentRevisionAlreadyExistsException, CreationException, WorkflowModelNotFoundException, RoleNotFoundException, DocumentRevisionNotFoundException, FileAlreadyExistsException, UserGroupNotFoundException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceWriteAccess(pOriginalDocRPK.getDocumentMaster().getWorkspace());
        Locale locale = new Locale(user.getLanguage());
        DocumentRevisionDAO docRDAO = new DocumentRevisionDAO(locale, em);
        DocumentRevision originalDocR = docRDAO.loadDocR(pOriginalDocRPK);
        DocumentMaster docM = originalDocR.getDocumentMaster();
        Folder folder = originalDocR.getLocation();
        checkFolderWritingRight(user, folder);

        if (originalDocR.isCheckedOut()) {
            throw new NotAllowedException(locale, "NotAllowedException26");
        }

        if (originalDocR.getNumberOfIterations() == 0) {
            throw new NotAllowedException(locale, "NotAllowedException27");
        }

        DocumentRevision docR = docM.createNextRevision(user);

        //create the first iteration which is a copy of the last one of the original docR
        //of course we duplicate the iteration only if it exists !
        DocumentIteration lastDoc = originalDocR.getLastIteration();
        DocumentIteration firstIte = docR.createNextIteration(user);
        if (lastDoc != null) {
            BinaryResourceDAO binDAO = new BinaryResourceDAO(locale, em);
            for (BinaryResource sourceFile : lastDoc.getAttachedFiles()) {
                String fileName = sourceFile.getName();
                long length = sourceFile.getContentLength();
                Date lastModified = sourceFile.getLastModified();
                String fullName = docR.getWorkspaceId() + "/documents/" + docR.getId() + "/" + docR.getVersion() + "/1/" + fileName;
                BinaryResource targetFile = new BinaryResource(fullName, length, lastModified);
                binDAO.createBinaryResource(targetFile);
                firstIte.addFile(targetFile);
                try {
                    storageManager.copyData(sourceFile, targetFile);
                } catch (StorageException e) {
                    LOGGER.log(Level.INFO, null, e);
                }
            }

            Set<DocumentLink> links = new HashSet<>();
            for (DocumentLink link : lastDoc.getLinkedDocuments()) {
                DocumentLink newLink = link.clone();
                links.add(newLink);
            }
            firstIte.setLinkedDocuments(links);

            List<InstanceAttribute> attrs = new ArrayList<>();
            for (InstanceAttribute attr : lastDoc.getInstanceAttributes()) {
                InstanceAttribute clonedAttribute = attr.clone();
                attrs.add(clonedAttribute);
            }
            firstIte.setInstanceAttributes(attrs);
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
                Role role = roleDAO.loadRole(new RoleKey(pOriginalDocRPK.getDocumentMaster().getWorkspace(), roleName));
                Set<User> users = new HashSet<>();
                roleUserMap.put(role, users);
                for (String login : userLogins) {
                    User u = userDAO.loadUser(new UserKey(pOriginalDocRPK.getDocumentMaster().getWorkspace(), login));
                    users.add(u);
                }
            }

            Map<Role, Collection<UserGroup>> roleGroupMap = new HashMap<>();
            for (Map.Entry<String, Collection<String>> pair : groupRoleMapping.entrySet()) {
                String roleName = pair.getKey();
                Collection<String> groupIds = pair.getValue();
                Role role = roleDAO.loadRole(new RoleKey(pOriginalDocRPK.getDocumentMaster().getWorkspace(), roleName));
                Set<UserGroup> groups = new HashSet<>();
                roleGroupMap.put(role, groups);
                for (String groupId : groupIds) {
                    UserGroup g = groupDAO.loadUserGroup(new UserGroupKey(pOriginalDocRPK.getDocumentMaster().getWorkspace(), groupId));
                    groups.add(g);
                }
            }

            WorkflowModel workflowModel = new WorkflowModelDAO(locale, em).loadWorkflowModel(new WorkflowModelKey(user.getWorkspaceId(), pWorkflowModelId));
            Workflow workflow = workflowModel.createWorkflow(roleUserMap, roleGroupMap);
            docR.setWorkflow(workflow);

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

        docR.setTitle(pTitle);
        docR.setDescription(pDescription);

        if (aclUserEntries != null && !aclUserEntries.isEmpty() || aclGroupEntries != null && !aclGroupEntries.isEmpty()) {
            ACLFactory aclFactory = new ACLFactory(em);
            ACL acl = aclFactory.createACL(docR.getWorkspaceId(), aclUserEntries, aclGroupEntries);
            docR.setACL(acl);
        }

        Date now = new Date();
        docR.setCreationDate(now);
        docR.setLocation(folder);
        docR.setCheckOutUser(user);
        docR.setCheckOutDate(now);
        firstIte.setCreationDate(now);
        firstIte.setModificationDate(now);

        docRDAO.createDocR(docR);

        if (runningTasks != null) {
            mailer.sendApproval(runningTasks, docR);
        }

        return new DocumentRevision[]{originalDocR, docR};
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public void subscribeToStateChangeEvent(DocumentRevisionKey pDocRPK) throws WorkspaceNotFoundException, NotAllowedException, DocumentRevisionNotFoundException, UserNotFoundException, UserNotActiveException, AccessRightException, WorkspaceNotEnabledException {
        User user = checkDocumentRevisionReadAccess(pDocRPK);
        Locale userLocale = new Locale(user.getLanguage());
        DocumentRevision docR = new DocumentRevisionDAO(userLocale, em).loadDocR(pDocRPK);
        if (isAnotherUserHomeFolder(user, docR.getLocation())) {
            throw new NotAllowedException(userLocale, "NotAllowedException30");
        }

        new SubscriptionDAO(em).createStateChangeSubscription(new StateChangeSubscription(user, docR));
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public void unsubscribeToStateChangeEvent(DocumentRevisionKey pDocRPK) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException, AccessRightException, DocumentRevisionNotFoundException, WorkspaceNotEnabledException {
        User user = checkDocumentRevisionReadAccess(pDocRPK);
        SubscriptionKey key = new SubscriptionKey(user.getWorkspaceId(), user.getLogin(), pDocRPK.getDocumentMaster().getWorkspace(), pDocRPK.getDocumentMaster().getId(), pDocRPK.getVersion());
        new SubscriptionDAO(em).removeStateChangeSubscription(key);
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public void subscribeToIterationChangeEvent(DocumentRevisionKey pDocRPK) throws WorkspaceNotFoundException, NotAllowedException, DocumentRevisionNotFoundException, UserNotFoundException, UserNotActiveException, AccessRightException, WorkspaceNotEnabledException {
        User user = checkDocumentRevisionReadAccess(pDocRPK);
        Locale userLocale = new Locale(user.getLanguage());
        DocumentRevision docR = new DocumentRevisionDAO(userLocale, em).getDocRRef(pDocRPK);
        if (isAnotherUserHomeFolder(user, docR.getLocation())) {
            throw new NotAllowedException(userLocale, "NotAllowedException30");
        }

        new SubscriptionDAO(em).createIterationChangeSubscription(new IterationChangeSubscription(user, docR));
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public void unsubscribeToIterationChangeEvent(DocumentRevisionKey pDocRPK) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException, AccessRightException, DocumentRevisionNotFoundException, WorkspaceNotEnabledException {
        User user = checkDocumentRevisionReadAccess(pDocRPK);
        SubscriptionKey key = new SubscriptionKey(user.getWorkspaceId(), user.getLogin(), pDocRPK.getDocumentMaster().getWorkspace(), pDocRPK.getDocumentMaster().getId(), pDocRPK.getVersion());
        new SubscriptionDAO(em).removeIterationChangeSubscription(key);
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID, UserGroupMapping.ADMIN_ROLE_ID})
    @Override
    public String[] getTags(String pWorkspaceId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
        Tag[] tags = new TagDAO(new Locale(user.getLanguage()), em).findAllTags(pWorkspaceId);

        String[] labels = new String[tags.length];
        int i = 0;
        for (Tag t : tags) {
            labels[i++] = t.getLabel();
        }
        return labels;
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID, UserGroupMapping.ADMIN_ROLE_ID})
    @Override
    public long getDiskUsageForDocumentsInWorkspace(String pWorkspaceId) throws WorkspaceNotFoundException, AccessRightException, AccountNotFoundException {
        Account account = userManager.checkAdmin(pWorkspaceId);
        DocumentRevisionDAO documentRevisionDAO = new DocumentRevisionDAO(new Locale(account.getLanguage()), em);
        return documentRevisionDAO.getDiskUsageForDocumentsInWorkspace(pWorkspaceId);
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID, UserGroupMapping.ADMIN_ROLE_ID})
    @Override
    public long getDiskUsageForDocumentTemplatesInWorkspace(String pWorkspaceId) throws WorkspaceNotFoundException, AccessRightException, AccountNotFoundException {
        Account account = userManager.checkAdmin(pWorkspaceId);
        DocumentRevisionDAO documentRevisionDAO = new DocumentRevisionDAO(new Locale(account.getLanguage()), em);
        return documentRevisionDAO.getDiskUsageForDocumentTemplatesInWorkspace(pWorkspaceId);
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID})
    @Override
    public SharedDocument createSharedDocument(DocumentRevisionKey pDocRPK, String pPassword, Date pExpireDate) throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException, DocumentRevisionNotFoundException, UserNotActiveException, NotAllowedException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceWriteAccess(pDocRPK.getDocumentMaster().getWorkspace());
        SharedDocument sharedDocument = new SharedDocument(user.getWorkspace(), user, pExpireDate, pPassword, getDocumentRevision(pDocRPK));
        SharedEntityDAO sharedEntityDAO = new SharedEntityDAO(new Locale(user.getLanguage()), em);
        sharedEntityDAO.createSharedDocument(sharedDocument);
        return sharedDocument;
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID})
    @Override
    public void deleteSharedDocument(SharedEntityKey sharedEntityKey) throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException, SharedEntityNotFoundException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceWriteAccess(sharedEntityKey.getWorkspace());
        SharedEntityDAO sharedEntityDAO = new SharedEntityDAO(new Locale(user.getLanguage()), em);
        SharedDocument sharedDocument = sharedEntityDAO.loadSharedDocument(sharedEntityKey.getUuid());
        sharedEntityDAO.deleteSharedDocument(sharedDocument);
    }


    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID, UserGroupMapping.ADMIN_ROLE_ID})
    @Override
    public boolean canAccess(DocumentRevisionKey docRKey) throws DocumentRevisionNotFoundException, UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceReadAccess(docRKey.getDocumentMaster().getWorkspace());
        return canUserAccess(user, docRKey);
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID, UserGroupMapping.ADMIN_ROLE_ID})
    @Override
    public boolean canAccess(DocumentIterationKey docIKey) throws DocumentRevisionNotFoundException, UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceReadAccess(docIKey.getWorkspaceId());
        return canUserAccess(user, docIKey);
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID, UserGroupMapping.ADMIN_ROLE_ID})
    @Override
    public boolean canUserAccess(User user, DocumentRevisionKey docRKey) throws DocumentRevisionNotFoundException {
        DocumentRevision docRevision = new DocumentRevisionDAO(new Locale(user.getLanguage()), em).loadDocR(docRKey);
        return hasDocumentRevisionReadAccess(user, docRevision);
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID, UserGroupMapping.ADMIN_ROLE_ID})
    @Override
    public boolean canUserAccess(User user, DocumentIterationKey docIKey) throws DocumentRevisionNotFoundException {
        DocumentRevision docRevision = new DocumentRevisionDAO(new Locale(user.getLanguage()), em).loadDocR(docIKey.getDocumentRevision());
        return hasDocumentRevisionReadAccess(user, docRevision) &&
                (docRevision.getLastIteration().getIteration() > docIKey.getIteration() ||
                        !isCheckoutByAnotherUser(user, docRevision));
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID})
    @Override
    public List<DocumentIteration> getInverseDocumentsLink(DocumentRevisionKey docKey) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, DocumentRevisionNotFoundException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceReadAccess(docKey.getWorkspaceId());

        Locale locale = new Locale(user.getLanguage());

        DocumentRevision documentRevision = new DocumentRevisionDAO(locale, em).loadDocR(docKey);

        DocumentLinkDAO documentLinkDAO = new DocumentLinkDAO(locale, em);
        List<DocumentIteration> iterations = documentLinkDAO.getInverseDocumentsLinks(documentRevision);

        ListIterator<DocumentIteration> ite = iterations.listIterator();

        while (ite.hasNext()) {
            DocumentIteration next = ite.next();
            if (!canAccess(next.getKey())) {
                ite.remove();
            }
        }

        return iterations;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public DocumentRevision[] getDocumentRevisionsWithAssignedTasksForGivenUser(String pWorkspaceId, String assignedUserLogin)
            throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
        List<DocumentRevision> docRs = new DocumentRevisionDAO(new Locale(user.getLanguage()), em).findDocsWithAssignedTasksForGivenUser(pWorkspaceId, assignedUserLogin);

        ListIterator<DocumentRevision> ite = docRs.listIterator();
        while (ite.hasNext()) {
            DocumentRevision docR = ite.next();
            if (!hasDocumentRevisionReadAccess(user, docR)) {
                ite.remove();
            } else if (isCheckoutByAnotherUser(user, docR)) {
                em.detach(docR);
                docR.removeLastIteration();
            }
        }

        return docRs.toArray(new DocumentRevision[docRs.size()]);
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public DocumentRevision[] getDocumentRevisionsWithOpenedTasksForGivenUser(String pWorkspaceId, String assignedUserLogin)
            throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
        List<DocumentRevision> docRs = new DocumentRevisionDAO(new Locale(user.getLanguage()), em).findDocsWithOpenedTasksForGivenUser(pWorkspaceId, assignedUserLogin);

        ListIterator<DocumentRevision> ite = docRs.listIterator();
        while (ite.hasNext()) {
            DocumentRevision docR = ite.next();
            if (!hasDocumentRevisionReadAccess(user, docR)) {
                ite.remove();
            } else if (isCheckoutByAnotherUser(user, docR)) {
                em.detach(docR);
                docR.removeLastIteration();
            }
        }

        return docRs.toArray(new DocumentRevision[docRs.size()]);
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public void createDocumentLog(DocumentLog log) {
        em.persist(log);
    }


    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public DocumentRevision releaseDocumentRevision(DocumentRevisionKey pRevisionKey) throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, DocumentRevisionNotFoundException, AccessRightException, NotAllowedException, WorkspaceNotEnabledException {
        User user = checkDocumentRevisionWriteAccess(pRevisionKey); // Check if the user can write the document
        Locale locale = new Locale(user.getLanguage());

        DocumentRevisionDAO documentRevisionDAO = new DocumentRevisionDAO(locale, em);
        DocumentRevision documentRevision = documentRevisionDAO.loadDocR(pRevisionKey);

        if (documentRevision.isCheckedOut()) {
            throw new NotAllowedException(locale, "NotAllowedException63");
        }

        if (documentRevision.getNumberOfIterations() == 0) {
            throw new NotAllowedException(locale, "NotAllowedException27");
        }

        if (documentRevision.isObsolete()) {
            throw new NotAllowedException(locale, "NotAllowedException64");
        }

        documentRevision.release(user);
        return documentRevision;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public DocumentRevision markDocumentRevisionAsObsolete(DocumentRevisionKey pRevisionKey) throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, DocumentRevisionNotFoundException, AccessRightException, NotAllowedException, WorkspaceNotEnabledException {
        User user = checkDocumentRevisionWriteAccess(pRevisionKey); // Check if the user can write the document
        Locale locale = new Locale(user.getLanguage());

        DocumentRevisionDAO documentRevisionDAO = new DocumentRevisionDAO(locale, em);
        DocumentRevision documentRevision = documentRevisionDAO.loadDocR(pRevisionKey);

        if (!documentRevision.isReleased()) {
            throw new NotAllowedException(locale, "NotAllowedException65");
        }

        documentRevision.markAsObsolete(user);
        return documentRevision;
    }

    @Override
    public void logDocument(String fullName, String event) throws FileNotFoundException {

        String userLogin = contextManager.getCallerPrincipalLogin();


        BinaryResourceDAO binDAO = new BinaryResourceDAO(em);
        BinaryResource file = binDAO.loadBinaryResource(fullName);
        DocumentIteration document = binDAO.getDocumentHolder(file);

        if (document != null) {
            DocumentLog log = new DocumentLog();
            log.setUserLogin(userLogin);
            log.setLogDate(new Date());
            log.setDocumentWorkspaceId(document.getWorkspaceId());
            log.setDocumentId(document.getId());
            log.setDocumentVersion(document.getVersion());
            log.setDocumentIteration(document.getIteration());
            log.setEvent(event);
            log.setInfo(fullName);
            createDocumentLog(log);
        }

    }

    /**
     * Apply read access policy on a document revision
     *
     * @param user             The user to test
     * @param documentRevision The document revision to test
     * @return The readable document revision or null if the user has no access to it.
     */
    private DocumentRevision applyDocumentRevisionReadAccess(User user, DocumentRevision documentRevision) {
        if (hasDocumentRevisionReadAccess(user, documentRevision)) {
            if (!isCheckoutByAnotherUser(user, documentRevision)) {
                return documentRevision;
            }
            em.detach(documentRevision);
            documentRevision.removeLastIteration();
            return documentRevision;
        }
        return null;
    }


    /**
     * Check if the current account have read access on a document revision.
     *
     * @param documentRevisionKey The key of the document revision.
     * @return The user if he has read access to the document revision.
     * @throws UserNotFoundException             If there are any User matching the Workspace and the current account login.
     * @throws UserNotActiveException            If the user is not actif.
     * @throws WorkspaceNotFoundException        If the workspace doesn't exist.
     * @throws AccessRightException              If the user has no read access to the document revision.
     * @throws DocumentRevisionNotFoundException If the document revision doesn't exist.
     */
    private User checkDocumentRevisionReadAccess(DocumentRevisionKey documentRevisionKey) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, AccessRightException, DocumentRevisionNotFoundException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceReadAccess(documentRevisionKey.getDocumentMaster().getWorkspace());
        Locale locale = new Locale(user.getLanguage());
        DocumentRevision documentRevision = new DocumentRevisionDAO(locale, em).loadDocR(documentRevisionKey);

        if (!hasDocumentRevisionReadAccess(user, documentRevision)) {
            throw new AccessRightException(locale, user);
        }

        return user;
    }

    /**
     * Check if the current account user have write access on a document revision.
     *
     * @param documentRevisionKey The key of the document revision.
     * @return The user if he has write access to the document revision.
     * @throws UserNotFoundException             If there are any User matching the Workspace and the current account login.
     * @throws UserNotActiveException            If the user is not actif.
     * @throws WorkspaceNotFoundException        If the workspace doesn't exist.
     * @throws AccessRightException              If the user has no write access to the document revision.
     * @throws DocumentRevisionNotFoundException If the document revision doesn't exist.
     */
    private User checkDocumentRevisionWriteAccess(DocumentRevisionKey documentRevisionKey) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, AccessRightException, DocumentRevisionNotFoundException, NotAllowedException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceReadAccess(documentRevisionKey.getDocumentMaster().getWorkspace());
        Locale locale = new Locale(user.getLanguage());
        if (user.isAdministrator()) {
            return user;
        }
        DocumentRevision documentRevision = new DocumentRevisionDAO(locale, em).loadDocR(documentRevisionKey);

        if (documentRevision.getACL() == null) {
            return userManager.checkWorkspaceWriteAccess(documentRevisionKey.getDocumentMaster().getWorkspace());
        } else if (hasDocumentRevisionWriteAccess(user, documentRevision)) {
            return user;
        } else if (isInAnotherUserHomeFolder(user, documentRevision)) {
            throw new NotAllowedException(locale, "NotAllowedException5");
        } else {
            throw new AccessRightException(locale, user);
        }
    }


    /**
     * Check if a user, which have access to the workspace, have the right of change the folder structure.
     *
     * @param pUser A user which have read access to the workspace.
     * @throws NotAllowedException If access is deny.
     */
    private void checkFoldersStructureChangeRight(User pUser) throws NotAllowedException {
        Workspace wks = pUser.getWorkspace();
        if (wks.isFolderLocked() && !pUser.isAdministrator()) {
            throw new NotAllowedException(new Locale(pUser.getLanguage()), "NotAllowedException7");
        }
    }

    /**
     * Check if a user, which have access to the workspace, have write access in a folder
     *
     * @param pUser   A user which have read access to the workspace
     * @param pFolder The folder wanted
     * @return The folder access is granted.
     * @throws NotAllowedException If the folder access is deny.
     */
    private Folder checkFolderWritingRight(User pUser, Folder pFolder) throws NotAllowedException {
        if (isAnotherUserHomeFolder(pUser, pFolder)) {
            throw new NotAllowedException(new Locale(pUser.getLanguage()), "NotAllowedException33");
        }
        return pFolder;
    }


    /**
     * Say if a user, which have access to the workspace, have read access to a document revision
     *
     * @param user             A user which have read access to the workspace
     * @param documentRevision The document revision wanted
     * @return True if access is granted, False otherwise
     */
    private boolean hasDocumentRevisionReadAccess(User user, DocumentRevision documentRevision) {
        return documentRevision.isPublicShared() || hasPrivateDocumentRevisionReadAccess(user, documentRevision);
    }

    private boolean hasPrivateDocumentRevisionReadAccess(User user, DocumentRevision documentRevision) {
        return isInSameWorkspace(user, documentRevision) &&
                (user.isAdministrator() || isACLGrantReadAccess(user, documentRevision)) &&
                !isInAnotherUserHomeFolder(user, documentRevision);
    }

    private boolean hasDocumentMasterTemplateReadAccess(DocumentMasterTemplate template, User user) {
        return isInSameWorkspace(user, template) && (user.isAdministrator() || isACLGrantReadAccess(user, template));
    }

    /**
     * Say if a user, which have access to the workspace, have write access to a document revision
     *
     * @param user             A user which have read access to the workspace
     * @param documentRevision The document revision wanted
     * @return True if access is granted, False otherwise
     */
    private boolean hasDocumentRevisionWriteAccess(User user, DocumentRevision documentRevision) {
        return isInSameWorkspace(user, documentRevision) &&
                (user.isAdministrator() || isACLGrantWriteAccess(user, documentRevision)) &&
                !isInAnotherUserHomeFolder(user, documentRevision);
    }

    private boolean isInSameWorkspace(User user, DocumentRevision documentRevision) {
        return user.getWorkspaceId().equals(documentRevision.getWorkspaceId());
    }

    private boolean isInSameWorkspace(User user, DocumentMasterTemplate template) {
        return user.getWorkspaceId().equals(template.getWorkspaceId());
    }

    private boolean isAuthor(User user, DocumentRevision documentRevision) {
        return documentRevision.getAuthor().getLogin().equals(user.getLogin());
    }

    private boolean isACLGrantReadAccess(User user, DocumentRevision documentRevision) {
        return documentRevision.getACL() == null || documentRevision.getACL().hasReadAccess(user);
    }

    private boolean isACLGrantReadAccess(User user, DocumentMasterTemplate template) {
        return template.getAcl() == null || template.getAcl().hasReadAccess(user);
    }

    private boolean isACLGrantWriteAccess(User user, DocumentRevision documentRevision) {
        return documentRevision.getACL() == null || documentRevision.getACL().hasWriteAccess(user);
    }

    private boolean isAnotherUserHomeFolder(User user, Folder folder) {
        return folder.isPrivate() && !folder.getOwner().equals(user.getLogin());
    }

    private boolean isInAnotherUserHomeFolder(User user, DocumentRevision documentRevision) {
        return isAnotherUserHomeFolder(user, documentRevision.getLocation());
    }

    private boolean isCheckoutByUser(User user, DocumentRevision documentRevision) {
        return documentRevision.isCheckedOut() && documentRevision.getCheckOutUser().equals(user);
    }

    private boolean isCheckoutByAnotherUser(User user, DocumentRevision documentRevision) {
        return documentRevision.isCheckedOut() && !documentRevision.getCheckOutUser().equals(user);
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

    private User checkDocumentTemplateWriteAccess(DocumentMasterTemplate docTemplate, User user) throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException, WorkspaceNotEnabledException {
        if (user.isAdministrator()) {
            // Check if it is the workspace's administrator
            return user;
        }
        if (docTemplate.getAcl() == null) {
            // Check if the item haven't ACL
            return userManager.checkWorkspaceWriteAccess(docTemplate.getWorkspaceId());
        } else if (docTemplate.getAcl().hasWriteAccess(user)) {
            // Check if there is a write access
            return user;
        } else {
            // Else throw a AccessRightException
            throw new AccessRightException(new Locale(user.getLanguage()), user);
        }

    }
}
