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

import com.docdoku.core.*;
import com.docdoku.core.entities.*;
import com.docdoku.core.entities.keys.*;
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
import javax.ejb.EJBException;
import javax.ejb.Local;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jws.WebService;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.annotation.security.DeclareRoles;
import javax.persistence.NoResultException;

@DeclareRoles("users")
@Local(ICommandLocal.class)
@Stateless(name = "CommandBean")
@WebService(endpointInterface = "com.docdoku.core.ICommandWS")
public class CommandBean implements ICommandWS, ICommandLocal {

    @PersistenceContext
    private EntityManager em;
    @Resource
    private SessionContext ctx;
    @Resource(name = "vaultPath")
    private String vaultPath;
    @Resource(name = "jms/connectionFactory")
    private ConnectionFactory connectionFactory;
    @Resource(name = "jms/mailerQueue")
    private Queue mailerQueue;
    @Resource(name = "jms/indexerQueue")
    private Queue indexerQueue;
    @EJB
    private IIndexSearcherLocal indexSearcher;
    private DataManager dataManager;
    private final static Logger LOGGER = Logger.getLogger(CommandBean.class.getName());

    @PostConstruct
    public void init() {
        dataManager = new DataManagerImpl(new File(vaultPath));
    }

    public Account createAccount(String pLogin, String pName, String pEmail, String pLanguage, String pPassword) throws AccountAlreadyExistsException, CreationException {
        Date now = new Date();
        Account account = new Account(pLogin, pName, pEmail, pLanguage, now);
        new AccountDAO(new Locale(pLanguage), em).createAccount(account, pPassword);
        return account;
    }

    @RolesAllowed("users")
    public void addUserInGroup(BasicElementKey pGroupKey, String pLogin) throws AccessRightException, UserGroupNotFoundException, UserGroupNotFoundException, AccountNotFoundException, WorkspaceNotFoundException, UserAlreadyExistsException, FolderAlreadyExistsException, CreationException {
        Account account = checkAdmin(pGroupKey.getWorkspaceId());
        UserDAO userDAO = new UserDAO(new Locale(account.getLanguage()), em);
        User userToAdd = em.find(User.class, new UserKey(pGroupKey.getWorkspaceId(), pLogin));
        if (userToAdd == null) {
            Account userAccount = new AccountDAO(em).loadAccount(pLogin);
            Workspace workspace = em.getReference(Workspace.class, pGroupKey.getWorkspaceId());
            userToAdd = new User(workspace, userAccount.getLogin(), userAccount.getName(), userAccount.getEmail(), userAccount.getLanguage());
            userDAO.createUser(userToAdd);
        }
        UserGroupDAO groupDAO = new UserGroupDAO(new Locale(account.getLanguage()), em);
        UserGroup group = groupDAO.loadUserGroup(pGroupKey);

        userDAO.removeUserMembership(new WorkspaceUserMembershipKey(pGroupKey.getWorkspaceId(), pGroupKey.getWorkspaceId(), pLogin));
        group.addUser(userToAdd);
    }

    @RolesAllowed("users")
    public void addUserInWorkspace(String pWorkspaceId, String pLogin) throws AccessRightException, AccountNotFoundException, WorkspaceNotFoundException, UserAlreadyExistsException, FolderAlreadyExistsException, CreationException {
        Account account = checkAdmin(pWorkspaceId);
        UserDAO userDAO = new UserDAO(new Locale(account.getLanguage()), em);
        User userToAdd = em.find(User.class, new UserKey(pWorkspaceId, pLogin));
        Workspace workspace = em.getReference(Workspace.class, pWorkspaceId);
        if (userToAdd == null) {
            Account userAccount = new AccountDAO(em).loadAccount(pLogin);
            userToAdd = new User(workspace, userAccount.getLogin(), userAccount.getName(), userAccount.getEmail(), userAccount.getLanguage());
            userDAO.createUser(userToAdd);
        }
        userDAO.addUserMembership(workspace, userToAdd);
    }

    @RolesAllowed("users")
    public void removeUserFromGroup(BasicElementKey pGroupKey, String[] pLogins) throws AccessRightException, UserGroupNotFoundException, AccountNotFoundException, WorkspaceNotFoundException {
        Account account = checkAdmin(pGroupKey.getWorkspaceId());
        UserGroup group = new UserGroupDAO(new Locale(account.getLanguage()), em).loadUserGroup(pGroupKey);
        for (String login : pLogins) {
            User userToRemove = em.getReference(User.class, new UserKey(pGroupKey.getWorkspaceId(), login));
            group.removeUser(userToRemove);
        }
    }

    @RolesAllowed("users")
    public UserGroup createUserGroup(String pId, Workspace pWorkspace) throws UserGroupAlreadyExistsException, AccessRightException, AccountNotFoundException, CreationException {
        Account account = checkAdmin(pWorkspace);
        UserGroup groupToCreate = new UserGroup(pWorkspace, pId);
        UserGroupDAO groupDAO = new UserGroupDAO(new Locale(account.getLanguage()), em);
        groupDAO.createUserGroup(groupToCreate);
        groupDAO.addUserGroupMembership(pWorkspace, groupToCreate);
        return groupToCreate;
    }

    @RolesAllowed("users")
    public Workspace createWorkspace(String pID, Account pAdmin, String pDescription, Workspace.VaultType pVaultType, boolean pFolderLocked) throws WorkspaceAlreadyExistsException, FolderAlreadyExistsException, UserAlreadyExistsException, CreationException {
        Workspace workspace = new Workspace(pID, pAdmin, pDescription, pVaultType, pFolderLocked);
        new WorkspaceDAO(em).createWorkspace(workspace);
        User userToCreate = new User(workspace, pAdmin.getLogin(), pAdmin.getName(), pAdmin.getEmail(), pAdmin.getLanguage());
        UserDAO userDAO = new UserDAO(new Locale(pAdmin.getLanguage()), em);
        userDAO.createUser(userToCreate);
        userDAO.addUserMembership(workspace, userToCreate);
        return workspace;
    }

    @RolesAllowed("users")
    public Account getAccount(String pLogin) throws AccountNotFoundException {
        return new AccountDAO(em).loadAccount(pLogin);
    }

    @RolesAllowed("users")
    public Workspace[] getAdministratedWorkspaces() throws AccountNotFoundException {
        Account account = new AccountDAO(em).loadAccount(ctx.getCallerPrincipal().toString());
        return new AccountDAO(em).getAdministratedWorkspaces(account);
    }

    @RolesAllowed("users")
    public Workspace[] getWorkspaces() {
        User[] users = new UserDAO(em).getUsers(ctx.getCallerPrincipal().toString());
        Workspace[] workspaces = new Workspace[users.length];
        for (int i = 0; i < users.length; i++) {
            workspaces[i] = users[i].getWorkspace();
        }

        return workspaces;
    }

    @RolesAllowed("users")
    public UserGroup[] getUserGroups(String pWorkspaceId) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException {
        User user = checkWorkspaceReadAccess(pWorkspaceId);
        return new UserGroupDAO(new Locale(user.getLanguage()), em).findAllUserGroups(pWorkspaceId);
    }

    @RolesAllowed("users")
    public UserGroup getUserGroup(BasicElementKey pKey) throws WorkspaceNotFoundException, UserGroupNotFoundException, UserNotFoundException, UserNotActiveException {
        User user = checkWorkspaceReadAccess(pKey.getWorkspaceId());
        return new UserGroupDAO(new Locale(user.getLanguage()), em).loadUserGroup(pKey);
    }

    @RolesAllowed("users")
    public WorkspaceUserMembership[] getWorkspaceUserMemberships(String pWorkspaceId) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException {
        User user = checkWorkspaceReadAccess(pWorkspaceId);
        return new UserDAO(new Locale(user.getLanguage()), em).findAllWorkspaceUserMemberships(pWorkspaceId);
    }

    @RolesAllowed("users")
    public WorkspaceUserGroupMembership[] getWorkspaceUserGroupMemberships(String pWorkspaceId) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException {
        User user = checkWorkspaceReadAccess(pWorkspaceId);
        return new UserGroupDAO(new Locale(user.getLanguage()), em).findAllWorkspaceUserGroupMemberships(pWorkspaceId);
    }

    @RolesAllowed("users")
    public void grantUserAccess(String pWorkspaceId, String[] pLogins, boolean pReadOnly) throws AccessRightException, AccountNotFoundException, WorkspaceNotFoundException {
        Account account = checkAdmin(pWorkspaceId);
        UserDAO userDAO = new UserDAO(new Locale(account.getLanguage()), em);
        for (String login : pLogins) {
            WorkspaceUserMembership ms = userDAO.loadUserMembership(new WorkspaceUserMembershipKey(pWorkspaceId, pWorkspaceId, login));
            if (ms != null) {
                ms.setReadOnly(pReadOnly);
            }
        }
    }

    @RolesAllowed("users")
    public void grantGroupAccess(String pWorkspaceId, String[] pGroupIds, boolean pReadOnly) throws AccessRightException, AccountNotFoundException, WorkspaceNotFoundException {
        Account account = checkAdmin(pWorkspaceId);
        UserGroupDAO groupDAO = new UserGroupDAO(new Locale(account.getLanguage()), em);
        for (String id : pGroupIds) {
            WorkspaceUserGroupMembership ms = groupDAO.loadUserGroupMembership(new WorkspaceUserGroupMembershipKey(pWorkspaceId, pWorkspaceId, id));
            if (ms != null) {
                ms.setReadOnly(pReadOnly);
            }
        }
    }

    @RolesAllowed("users")
    public void activateUsers(String pWorkspaceId, String[] pLogins) throws AccessRightException, AccountNotFoundException, WorkspaceNotFoundException {
        Account account = checkAdmin(pWorkspaceId);
        Workspace workspace = em.getReference(Workspace.class, pWorkspaceId);
        UserDAO userDAO = new UserDAO(new Locale(account.getLanguage()), em);
        for (String login : pLogins) {
            User member = em.getReference(User.class, new UserKey(pWorkspaceId, login));
            userDAO.addUserMembership(workspace, member);
        }
    }

    @RolesAllowed("users")
    public void activateUserGroups(String pWorkspaceId, String[] pGroupIds) throws AccessRightException, AccountNotFoundException, WorkspaceNotFoundException {
        Account account = checkAdmin(pWorkspaceId);
        Workspace workspace = em.getReference(Workspace.class, pWorkspaceId);
        UserGroupDAO groupDAO = new UserGroupDAO(new Locale(account.getLanguage()), em);
        for (String id : pGroupIds) {
            UserGroup member = em.getReference(UserGroup.class, new BasicElementKey(pWorkspaceId, id));
            groupDAO.addUserGroupMembership(workspace, member);
        }
    }

    @RolesAllowed("users")
    public void passivateUserGroups(String pWorkspaceId, String[] pGroupIds) throws AccessRightException, AccountNotFoundException, WorkspaceNotFoundException {
        Account account = checkAdmin(pWorkspaceId);
        UserGroupDAO groupDAO = new UserGroupDAO(new Locale(account.getLanguage()), em);
        for (String id : pGroupIds) {
            groupDAO.removeUserGroupMembership(new WorkspaceUserGroupMembershipKey(pWorkspaceId, pWorkspaceId, id));
        }
    }

    @RolesAllowed("users")
    public void passivateUsers(String pWorkspaceId, String[] pLogins) throws AccessRightException, AccountNotFoundException, WorkspaceNotFoundException {
        Account account = checkAdmin(pWorkspaceId);
        UserDAO userDAO = new UserDAO(new Locale(account.getLanguage()), em);
        for (String login : pLogins) {
            userDAO.removeUserMembership(new WorkspaceUserMembershipKey(pWorkspaceId, pWorkspaceId, login));
        }
    }

    @RolesAllowed("users")
    public void removeUsers(String pWorkspaceId, String[] pLogins) throws UserNotFoundException, NotAllowedException, AccessRightException, AccountNotFoundException, WorkspaceNotFoundException, FolderNotFoundException {
        try{
        Account account = checkAdmin(pWorkspaceId);
        UserDAO userDAO = new UserDAO(new Locale(account.getLanguage()), em);
            for (String login : pLogins) {
                MasterDocument[] mdocs = userDAO.removeUser(new UserKey(pWorkspaceId, login));
                for (MasterDocument mdoc : mdocs) {
                    for (Document doc : mdoc.getDocumentIterations()) {
                        for (BinaryResource file : doc.getAttachedFiles()) {
                            sendMessageToIndexer(file.getFullName(), "", "remove");
                            dataManager.delData(file);
                        }
                    }
                }
            }
        }catch(UserNotFoundException ex){ctx.setRollbackOnly();throw ex;}
        catch(NotAllowedException ex){ctx.setRollbackOnly();throw ex;}
        catch(AccessRightException ex){ctx.setRollbackOnly();throw ex;}
        catch(AccountNotFoundException ex){ctx.setRollbackOnly();throw ex;}
        catch(WorkspaceNotFoundException ex){ctx.setRollbackOnly();throw ex;}
        catch(FolderNotFoundException ex){ctx.setRollbackOnly();throw ex;}
    }

    @RolesAllowed("users")
    public void removeUserGroups(String pWorkspaceId, String[] pIds) throws UserGroupNotFoundException, AccessRightException, AccountNotFoundException, WorkspaceNotFoundException {
        Account account = checkAdmin(pWorkspaceId);
        UserGroupDAO groupDAO = new UserGroupDAO(new Locale(account.getLanguage()), em);
        for (String id : pIds) {
            groupDAO.removeUserGroup(new BasicElementKey(pWorkspaceId, id));
        }
    }

    @RolesAllowed("users")
    public void updateWorkspace(Workspace pWorkspace) throws AccessRightException, AccountNotFoundException, WorkspaceNotFoundException {
        Account account = checkAdmin(pWorkspace.getId());
        new WorkspaceDAO(new Locale(account.getLanguage()), em).updateWorkspace(pWorkspace);
    }

    @RolesAllowed("users")
    public void updateAccount(String pName, String pEmail, String pLanguage, String pPassword) throws AccountNotFoundException {
        AccountDAO accountDAO = new AccountDAO(new Locale(pLanguage), em);
        Account account = accountDAO.loadAccount(ctx.getCallerPrincipal().toString());
        account.setName(pName);
        account.setEmail(pEmail);
        account.setLanguage(pLanguage);
        if (pPassword != null) {
            accountDAO.updateCredential(account.getLogin(), pPassword);
        }
    }

    @RolesAllowed("users")
    public File saveFileInTemplate(BasicElementKey pMDocTemplateKey, String pName, long pSize) throws WorkspaceNotFoundException, NotAllowedException, MasterDocumentTemplateNotFoundException, FileAlreadyExistsException, UserNotFoundException, UserNotActiveException, CreationException {
        User user = checkWorkspaceReadAccess(pMDocTemplateKey.getWorkspaceId());
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
    public File saveFileInDocument(DocumentKey pDocPK, String pName, long pSize) throws WorkspaceNotFoundException, NotAllowedException, MasterDocumentNotFoundException, FileAlreadyExistsException, UserNotFoundException, UserNotActiveException, CreationException {
        User user = checkWorkspaceReadAccess(pDocPK.getWorkspaceId());
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
    public File getDataFile(String pFullName) throws WorkspaceNotFoundException, NotAllowedException, FileNotFoundException, UserNotFoundException, UserNotActiveException {
        User user = checkWorkspaceReadAccess(BinaryResource.parseWorkspaceId(pFullName));

        BinaryResourceDAO binDAO = new BinaryResourceDAO(new Locale(user.getLanguage()), em);
        BinaryResource file = binDAO.loadBinaryResource(pFullName);

        Document document = binDAO.getDocumentOwner(file);
        if (document != null) {
            MasterDocument mdoc = document.getMasterDocument();
            String owner = mdoc.getLocation().getOwner();

            if (((owner != null) && (!owner.equals(user.getLogin()))) || (mdoc.isCheckedOut() && !mdoc.getCheckOutUser().equals(user) && mdoc.getLastIteration().equals(document))) {
                throw new NotAllowedException(Locale.getDefault(), "NotAllowedException34");
            } else {
                return dataManager.getDataFile(file);
            }
        } else {
            return dataManager.getDataFile(file);
        }
    }

    @RolesAllowed("users")
    public User whoAmI(String pWorkspaceId) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException {
        return checkWorkspaceReadAccess(pWorkspaceId);
    }

    @RolesAllowed("users")
    public Workspace getWorkspace(String pWorkspaceId) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException {
        User user = checkWorkspaceReadAccess(pWorkspaceId);
        return user.getWorkspace();
    }

    @RolesAllowed("users")
    public String[] getFolders(String pCompletePath) throws WorkspaceNotFoundException, FolderNotFoundException, UserNotFoundException, UserNotActiveException {
        User user = checkWorkspaceReadAccess(Folder.parseWorkspaceId(pCompletePath));
        Folder[] subFolders = new FolderDAO(new Locale(user.getLanguage()), em).getSubFolders(pCompletePath);
        String[] shortNames = new String[subFolders.length];
        int i = 0;
        for (Folder f : subFolders) {
            shortNames[i++] = f.getShortName();
        }
        return shortNames;
    }

    @RolesAllowed("users")
    public MasterDocument[] findMDocsByFolder(String pCompletePath) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException {
        String workspaceId=Folder.parseWorkspaceId(pCompletePath);
        User user = checkWorkspaceReadAccess(workspaceId);
        List<MasterDocument> mdocs = new MasterDocumentDAO(new Locale(user.getLanguage()), em).findMDocsByFolder(pCompletePath);
        ListIterator<MasterDocument> ite=mdocs.listIterator();
        Workspace wks = new WorkspaceDAO(new Locale(user.getLanguage()), em).loadWorkspace(workspaceId);
        boolean isAdmin=wks.getAdmin().getLogin().equals(user.getLogin());
        while (ite.hasNext()) {
            MasterDocument mdoc=ite.next();
            if(!isAdmin && mdoc.getACL() !=null && !mdoc.getACL().hasReadAccess(user)){
                ite.remove();
                continue;
            }
            if ((mdoc.isCheckedOut()) && (!mdoc.getCheckOutUser().equals(user))) {
                mdoc=mdoc.clone();
                mdoc.removeLastIteration();
                ite.set(mdoc);
                
            }
        }
        return mdocs.toArray(new MasterDocument[mdocs.size()]);
    }

    @RolesAllowed("users")
    public MasterDocument[] findMDocsByTag(TagKey pKey) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException {
        String workspaceId=pKey.getWorkspaceId();
        User user = checkWorkspaceReadAccess(workspaceId);
        List<MasterDocument> mdocs = new MasterDocumentDAO(new Locale(user.getLanguage()), em).findMDocsByTag(new Tag(user.getWorkspace(), pKey.getLabel()));
        ListIterator<MasterDocument> ite=mdocs.listIterator();
        Workspace wks = new WorkspaceDAO(new Locale(user.getLanguage()), em).loadWorkspace(workspaceId);
        boolean isAdmin=wks.getAdmin().getLogin().equals(user.getLogin());
        while (ite.hasNext()) {
            MasterDocument mdoc=ite.next();
            if(!isAdmin && mdoc.getACL() !=null && !mdoc.getACL().hasReadAccess(user)){
                ite.remove();
                continue;
            }
            if ((mdoc.isCheckedOut()) && (!mdoc.getCheckOutUser().equals(user))) {
                mdoc=mdoc.clone();
                mdoc.removeLastIteration();
                ite.set(mdoc);

            }
        }
        return mdocs.toArray(new MasterDocument[mdocs.size()]);
    }

    @RolesAllowed("users")
    public MasterDocument getMDoc(MasterDocumentKey pMDocPK) throws WorkspaceNotFoundException, MasterDocumentNotFoundException, NotAllowedException, UserNotFoundException, UserNotActiveException {
        User user = checkWorkspaceReadAccess(pMDocPK.getWorkspaceId());
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
    public MasterDocument[] getCheckedOutMDocs(String pWorkspaceId) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException {
        User user = checkWorkspaceReadAccess(pWorkspaceId);
        List<MasterDocument> mdocs =  new MasterDocumentDAO(new Locale(user.getLanguage()), em).findCheckedOutMDocs(user);
        return mdocs.toArray(new MasterDocument[mdocs.size()]);
    }

    @RolesAllowed("users")
    public Task[] getTasks(String pWorkspaceId) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException {
        User user = checkWorkspaceReadAccess(pWorkspaceId);
        return new TaskDAO(new Locale(user.getLanguage()), em).findTasks(user);
    }

    @RolesAllowed("users")
    public MasterDocumentKey[] getIterationChangeEventSubscriptions(String pWorkspaceId) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException {
        User user = checkWorkspaceReadAccess(pWorkspaceId);
        return new SubscriptionDAO(em).getIterationChangeEventSubscriptions(user);
    }

    @RolesAllowed("users")
    public MasterDocumentKey[] getStateChangeEventSubscriptions(String pWorkspaceId) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException {
        User user = checkWorkspaceReadAccess(pWorkspaceId);
        return new SubscriptionDAO(em).getStateChangeEventSubscriptions(user);
    }

    @RolesAllowed("users")
    public String generateId(String pWorkspaceId, String pMDocTemplateId) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException, MasterDocumentTemplateNotFoundException {

        User user = checkWorkspaceReadAccess(pWorkspaceId);
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
    public MasterDocument[] searchMDocs(SearchQuery pQuery) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException {
        User user = checkWorkspaceReadAccess(pQuery.getWorkspaceId());
        List<MasterDocument> fetchedMDocs = new MasterDocumentDAO(new Locale(user.getLanguage()), em).searchMDocs(pQuery.getWorkspaceId(), pQuery.getMDocId(), pQuery.getTitle(), pQuery.getVersion(), pQuery.getAuthor(), pQuery.getType(), pQuery.getCreationDateFrom(),
                pQuery.getCreationDateTo());

        //preparing tag filtering
        Set<Tag> tags = null;
        if (fetchedMDocs.size() > 0 && pQuery.getTags() != null) {
            Workspace wks = new Workspace();
            wks.setId(pQuery.getWorkspaceId());
            tags = new HashSet<Tag>();
            for (String label : pQuery.getTags()) {
                tags.add(new Tag(wks, label));
            }
        }

        //preparing fulltext filtering
        Set<MasterDocumentKey> indexedKeys = null;
        if (fetchedMDocs.size() > 0 && pQuery.getContent() != null && !pQuery.getContent().equals("")) {
            indexedKeys = indexSearcher.searchInIndex(pQuery.getWorkspaceId(), pQuery.getContent());
        }

        Workspace wks = new WorkspaceDAO(new Locale(user.getLanguage()), em).loadWorkspace(pQuery.getWorkspaceId());
        boolean isAdmin=wks.getAdmin().getLogin().equals(user.getLogin());

        ListIterator<MasterDocument> ite=fetchedMDocs.listIterator();
        mdocBlock:
        while (ite.hasNext()) {
            MasterDocument mdoc=ite.next();
            if (indexedKeys != null && (!indexedKeys.contains(mdoc.getKey()))) {
                ite.remove();
                continue mdocBlock;
            }

            if (tags != null && (!mdoc.getTags().containsAll(tags))){
                ite.remove();
                continue mdocBlock;
            }

            //TODO search should not be based on checked out (especially by someone else) doc working copy
            Document doc = mdoc.getLastIteration();
            if (pQuery.getAttributes() != null) {
                if (doc == null) {
                    ite.remove();
                    continue mdocBlock;
                }
                for (SearchQuery.AbstractAttributeQuery attrQuery : pQuery.getAttributes()) {
                    InstanceAttribute docAttr = doc.getInstanceAttributes().get(attrQuery.getName());
                    if (docAttr == null ){
                        ite.remove();
                        continue mdocBlock;
                    }
                    if (!attrQuery.attributeMatches(docAttr)) {
                        ite.remove();
                        continue mdocBlock;
                    }
                }
            }

            //TODO search should not fetch back private mdoc
            if ((mdoc.isCheckedOut()) && (!mdoc.getCheckOutUser().equals(user))) {
                mdoc=mdoc.clone();
                mdoc.removeLastIteration();
                ite.set(mdoc);
            }

            //Check acess rights
            if(!isAdmin && mdoc.getACL() !=null && !mdoc.getACL().hasReadAccess(user)){
                ite.remove();
                continue;
            }
        }
        return fetchedMDocs.toArray(new MasterDocument[fetchedMDocs.size()]);
    }

    @RolesAllowed("users")
    public WorkflowModel[] getWorkflowModels(String pWorkspaceId) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException {
        User user = checkWorkspaceReadAccess(pWorkspaceId);
        return new WorkflowModelDAO(new Locale(user.getLanguage()), em).findAllWorkflowModels(pWorkspaceId);
    }

    @RolesAllowed("users")
    public MasterDocumentTemplate[] getMDocTemplates(String pWorkspaceId) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException {
        User user = checkWorkspaceReadAccess(pWorkspaceId);
        return new MasterDocumentTemplateDAO(new Locale(user.getLanguage()), em).findAllMDocTemplates(pWorkspaceId);
    }

    @RolesAllowed("users")
    public WorkflowModel getWorkflowModel(BasicElementKey pKey)
            throws WorkspaceNotFoundException, WorkflowModelNotFoundException, UserNotFoundException, UserNotActiveException {
        User user = checkWorkspaceReadAccess(pKey.getWorkspaceId());
        return new WorkflowModelDAO(new Locale(user.getLanguage()), em).loadWorkflowModel(pKey);
    }

    @RolesAllowed("users")
    public MasterDocumentTemplate getMDocTemplate(BasicElementKey pKey)
            throws WorkspaceNotFoundException, MasterDocumentTemplateNotFoundException, UserNotFoundException, UserNotActiveException {
        User user = checkWorkspaceReadAccess(pKey.getWorkspaceId());
        return new MasterDocumentTemplateDAO(new Locale(user.getLanguage()), em).loadMDocTemplate(pKey);
    }

    @RolesAllowed("users")
    public WorkflowModel createWorkflowModel(String pWorkspaceId, String pId, String pFinalLifeCycleState, ActivityModel[] pActivityModels) throws WorkspaceNotFoundException, AccessRightException, UserNotFoundException, UserNotActiveException, WorkflowModelAlreadyExistsException, CreationException {
        User user = checkWorkspaceWriteAccess(pWorkspaceId);

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
    public MasterDocumentTemplate updateMDocTemplate(BasicElementKey pKey, String pDocumentType, String pMask, InstanceAttributeTemplate[] pAttributeTemplates, boolean idGenerated) throws WorkspaceNotFoundException, WorkspaceNotFoundException, AccessRightException, MasterDocumentTemplateNotFoundException, UserNotFoundException, UserNotActiveException {
        User user = checkWorkspaceWriteAccess(pKey.getWorkspaceId());

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
    public void delWorkflowModel(BasicElementKey pKey) throws WorkspaceNotFoundException, AccessRightException, WorkflowModelNotFoundException, UserNotFoundException, UserNotActiveException {
        User user = checkWorkspaceWriteAccess(pKey.getWorkspaceId());
        new WorkflowModelDAO(new Locale(user.getLanguage()), em).removeWorkflowModel(pKey);
    }

    @RolesAllowed("users")
    public void delTag(TagKey pKey) throws WorkspaceNotFoundException, AccessRightException, TagNotFoundException, UserNotFoundException, UserNotActiveException {
        User user = checkWorkspaceWriteAccess(pKey.getWorkspaceId());
        Locale userLocale = new Locale(user.getLanguage());
        Tag tagToRemove = new Tag(user.getWorkspace(), pKey.getLabel());
        List<MasterDocument> mdocs = new MasterDocumentDAO(userLocale, em).findMDocsByTag(tagToRemove);
        for (MasterDocument mdoc : mdocs) {
            mdoc.getTags().remove(tagToRemove);
        }

        new TagDAO(userLocale, em).removeTag(pKey);
    }

    public void createTag(String pWorkspaceId, String pLabel) throws WorkspaceNotFoundException, AccessRightException, CreationException, TagAlreadyExistsException, UserNotFoundException, UserNotActiveException {
        User user = checkWorkspaceWriteAccess(pWorkspaceId);
        Locale userLocale = new Locale(user.getLanguage());
        TagDAO tagDAO = new TagDAO(userLocale, em);
        Tag tag = new Tag(user.getWorkspace(), pLabel);
        tagDAO.createTag(tag);
    }

    @RolesAllowed("users")
    public MasterDocument createMDoc(String pParentFolder,
            String pMDocID, String pTitle, String pDescription, String pMDocTemplateId, String pWorkflowModelId, ACLUserEntry[] pACLUserEntries, ACLUserGroupEntry[] pACLUserGroupEntries) throws WorkspaceNotFoundException, WorkflowModelNotFoundException, NotAllowedException, MasterDocumentTemplateNotFoundException, AccessRightException, MasterDocumentAlreadyExistsException, FolderNotFoundException, FileAlreadyExistsException, UserNotFoundException, UserNotActiveException, CreationException {
        User user = checkWorkspaceWriteAccess(Folder.parseWorkspaceId(pParentFolder));
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
                attr.setDocument(newDoc);
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
            mdoc.setWorkflow(workflowModel.createWorkflow());
        }

        mdoc.setTitle(pTitle);
        mdoc.setDescription(pDescription);

        if((pACLUserEntries !=null && pACLUserEntries.length>0) || (pACLUserGroupEntries!=null && pACLUserGroupEntries.length>0)){
            ACL acl=new ACL();
            if(pACLUserEntries !=null){
                for(ACLUserEntry entry:pACLUserEntries)
                    acl.addEntry(em.getReference(User.class, new UserKey(user.getWorkspaceId(),entry.getPrincipalLogin())), entry.getPermission());
            }

            if(pACLUserGroupEntries !=null){
               for(ACLUserGroupEntry entry:pACLUserGroupEntries)
                    acl.addEntry(em.getReference(UserGroup.class, new BasicElementKey(user.getWorkspaceId(),entry.getPrincipalId())), entry.getPermission());
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
    public MasterDocumentTemplate createMDocTemplate(String pWorkspaceId, String pId, String pDocumentType,
            String pMask, InstanceAttributeTemplate[] pAttributeTemplates, boolean idGenerated) throws WorkspaceNotFoundException, AccessRightException, MasterDocumentTemplateAlreadyExistsException, UserNotFoundException, UserNotActiveException, CreationException {
        User user = checkWorkspaceWriteAccess(pWorkspaceId);
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
    public MasterDocument moveMDoc(String pParentFolder, MasterDocumentKey pMDocPK) throws WorkspaceNotFoundException, MasterDocumentNotFoundException, NotAllowedException, AccessRightException, FolderNotFoundException, UserNotFoundException, UserNotActiveException {
        //TODO security check if both parameter belong to the same workspace
        User user = checkWorkspaceWriteAccess(pMDocPK.getWorkspaceId());
        Folder newLocation = new FolderDAO(new Locale(user.getLanguage()), em).loadFolder(pParentFolder);
        checkWritingRight(user, newLocation);
        MasterDocumentDAO mdocDAO = new MasterDocumentDAO(new Locale(user.getLanguage()), em);
        MasterDocument mdoc = mdocDAO.loadMDoc(pMDocPK);
        //Check access rights on mdoc
        Workspace wks = new WorkspaceDAO(new Locale(user.getLanguage()), em).loadWorkspace(pMDocPK.getWorkspaceId());
        boolean isAdmin=wks.getAdmin().getLogin().equals(user.getLogin());
        if(!isAdmin && mdoc.getACL() !=null && !mdoc.getACL().hasWriteAccess(user)){
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
    public void createFolder(String pParentFolder, String pFolder)
            throws WorkspaceNotFoundException, NotAllowedException, AccessRightException, FolderNotFoundException, FolderAlreadyExistsException, UserNotFoundException, UserNotActiveException, CreationException {
        User user = checkWorkspaceWriteAccess(Folder.parseWorkspaceId(pParentFolder));
        if (!NamingConvention.correct(pFolder)) {
            throw new NotAllowedException(new Locale(user.getLanguage()), "NotAllowedException9");
        }
        FolderDAO folderDAO = new FolderDAO(new Locale(user.getLanguage()), em);
        Folder folder = folderDAO.loadFolder(pParentFolder);
        checkFolderStructureRight(user);
        checkWritingRight(user, folder);
        folderDAO.createFolder(new Folder(pParentFolder, pFolder));
    }

    @RolesAllowed("users")
    public MasterDocument approve(String pWorkspaceId, TaskKey pTaskKey, String pComment)
            throws WorkspaceNotFoundException, TaskNotFoundException, NotAllowedException, UserNotFoundException, UserNotActiveException {
        User user = checkWorkspaceReadAccess(pWorkspaceId);

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
            sendStateNotification(subscribers, mdoc);
        }

        Collection<Task> runningTasks = workflow.getRunningTasks();
        for (Task runningTask : runningTasks) {
            runningTask.start();
        }
        sendApproval(runningTasks, mdoc);
        return mdoc;
    }

    @RolesAllowed("users")
    public MasterDocument reject(String pWorkspaceId, TaskKey pTaskKey, String pComment)
            throws WorkspaceNotFoundException, TaskNotFoundException, NotAllowedException, UserNotFoundException, UserNotActiveException {
        User user = checkWorkspaceReadAccess(pWorkspaceId);

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
    public MasterDocument checkOut(MasterDocumentKey pMDocPK)
            throws WorkspaceNotFoundException, NotAllowedException, MasterDocumentNotFoundException, AccessRightException, FileAlreadyExistsException, UserNotFoundException, UserNotActiveException, CreationException {
        User user = checkWorkspaceWriteAccess(pMDocPK.getWorkspaceId());
        MasterDocumentDAO mdocDAO = new MasterDocumentDAO(new Locale(user.getLanguage()), em);
        MasterDocument mdoc = mdocDAO.loadMDoc(pMDocPK);
        //Check access rights on mdoc
        Workspace wks = new WorkspaceDAO(new Locale(user.getLanguage()), em).loadWorkspace(pMDocPK.getWorkspaceId());
        boolean isAdmin=wks.getAdmin().getLogin().equals(user.getLogin());
        if(!isAdmin && mdoc.getACL() !=null && !mdoc.getACL().hasWriteAccess(user)){
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
                //TODO create a hardlink or at least a softlink when available in Java 7
                //dataManager.copyData(sourceFile, targetFile);
            }

            Set<DocumentToDocumentLink> links = new HashSet<DocumentToDocumentLink>();
            for (DocumentToDocumentLink link : beforeLastDocument.getLinkedDocuments()) {
                DocumentToDocumentLink newLink = link.clone();
                newLink.setFromDocument(newDoc);
                links.add(newLink);
            }
            newDoc.setLinkedDocuments(links);

            Map<String, InstanceAttribute> attrs = new HashMap<String, InstanceAttribute>();
            for (InstanceAttribute attr : beforeLastDocument.getInstanceAttributes().values()) {
                InstanceAttribute newAttr = attr.clone();
                newAttr.setDocument(newDoc);
                attrs.put(newAttr.getName(), newAttr);
            }
            newDoc.setInstanceAttributes(attrs);
        }
        return mdoc;
    }

    @RolesAllowed("users")
    public MasterDocument saveTags(MasterDocumentKey pMDocPK, String[] pTags)
            throws WorkspaceNotFoundException, NotAllowedException, MasterDocumentNotFoundException, AccessRightException, UserNotFoundException, UserNotActiveException {
        User user = checkWorkspaceWriteAccess(pMDocPK.getWorkspaceId());
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
    public MasterDocument undoCheckOut(MasterDocumentKey pMDocPK)
            throws WorkspaceNotFoundException, MasterDocumentNotFoundException, NotAllowedException, UserNotFoundException, UserNotActiveException {
        User user = checkWorkspaceReadAccess(pMDocPK.getWorkspaceId());
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
    public MasterDocument checkIn(MasterDocumentKey pMDocPK)
            throws WorkspaceNotFoundException, NotAllowedException, MasterDocumentNotFoundException, AccessRightException, UserNotFoundException, UserNotActiveException {
        User user = checkWorkspaceWriteAccess(pMDocPK.getWorkspaceId());
        MasterDocumentDAO mdocDAO = new MasterDocumentDAO(new Locale(user.getLanguage()), em);
        MasterDocument mdoc = mdocDAO.loadMDoc(pMDocPK);
        //Check access rights on mdoc
        Workspace wks = new WorkspaceDAO(new Locale(user.getLanguage()), em).loadWorkspace(pMDocPK.getWorkspaceId());
        boolean isAdmin=wks.getAdmin().getLogin().equals(user.getLogin());
        if(!isAdmin && mdoc.getACL() !=null && !mdoc.getACL().hasWriteAccess(user)){
            throw new AccessRightException(new Locale(user.getLanguage()), user);
        }
        if (mdoc.isCheckedOut() && mdoc.getCheckOutUser().equals(user)) {
            User[] subscribers = new SubscriptionDAO(em).getIterationChangeEventSubscribers(mdoc);

            mdoc.setCheckOutDate(null);
            mdoc.setCheckOutUser(null);

            if (subscribers.length != 0) {
                sendIterationNotification(subscribers, mdoc);
            }

            for (BinaryResource bin : mdoc.getLastIteration().getAttachedFiles()) {
                File physicalFile = dataManager.getDataFile(bin);
                sendMessageToIndexer(bin.getFullName(), physicalFile.getPath(), "add");
            }

            return mdoc;
        } else {
            throw new NotAllowedException(new Locale(user.getLanguage()), "NotAllowedException20");
        }
    }

    @RolesAllowed("users")
    public MasterDocumentKey[] delFolder(String pCompletePath)
            throws WorkspaceNotFoundException, NotAllowedException, AccessRightException, UserNotFoundException, UserNotActiveException, FolderNotFoundException {
        User user = checkWorkspaceWriteAccess(Folder.parseWorkspaceId(pCompletePath));
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
                        sendMessageToIndexer(file.getFullName(), "", "remove");
                        dataManager.delData(file);
                    }
                }
            }
            return pks;
        }
    }

    @RolesAllowed("users")
    public void delMDoc(MasterDocumentKey pMDocPK)
            throws WorkspaceNotFoundException, NotAllowedException, MasterDocumentNotFoundException, AccessRightException, UserNotFoundException, UserNotActiveException {
        User user = checkWorkspaceWriteAccess(pMDocPK.getWorkspaceId());
        MasterDocumentDAO mdocDAO = new MasterDocumentDAO(new Locale(user.getLanguage()), em);
        MasterDocument mdoc = mdocDAO.loadMDoc(pMDocPK);
        //Check access rights on
        Workspace wks = new WorkspaceDAO(new Locale(user.getLanguage()), em).loadWorkspace(pMDocPK.getWorkspaceId());
        boolean isAdmin=wks.getAdmin().getLogin().equals(user.getLogin());
        if(!isAdmin && mdoc.getACL() !=null && !mdoc.getACL().hasWriteAccess(user)){
            throw new AccessRightException(new Locale(user.getLanguage()), user);
        }
        String owner = mdoc.getLocation().getOwner();
        if ((owner != null) && (!owner.equals(user.getLogin()))) {
            throw new NotAllowedException(new Locale(user.getLanguage()), "NotAllowedException22");
        }

        mdocDAO.removeMDoc(mdoc);

        for (Document doc : mdoc.getDocumentIterations()) {
            for (BinaryResource file : doc.getAttachedFiles()) {
                sendMessageToIndexer(file.getFullName(), "", "remove");
                dataManager.delData(file);
            }
        }
    }

    @RolesAllowed("users")
    public void delMDocTemplate(BasicElementKey pKey)
            throws WorkspaceNotFoundException, AccessRightException, MasterDocumentTemplateNotFoundException, UserNotFoundException, UserNotActiveException {
        User user = checkWorkspaceWriteAccess(pKey.getWorkspaceId());
        MasterDocumentTemplateDAO templateDAO = new MasterDocumentTemplateDAO(new Locale(user.getLanguage()), em);
        MasterDocumentTemplate template = templateDAO.removeMDocTemplate(pKey);
        for (BinaryResource file : template.getAttachedFiles()) {
            dataManager.delData(file);
        }
    }

    @RolesAllowed("users")
    public MasterDocument removeFileFromDocument(String pFullName) throws WorkspaceNotFoundException, MasterDocumentNotFoundException, NotAllowedException, AccessRightException, FileNotFoundException, UserNotFoundException, UserNotActiveException {
        User user = checkWorkspaceReadAccess(BinaryResource.parseWorkspaceId(pFullName));

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
    public MasterDocumentTemplate removeFileFromTemplate(String pFullName) throws WorkspaceNotFoundException, MasterDocumentTemplateNotFoundException, AccessRightException, FileNotFoundException, UserNotFoundException, UserNotActiveException {
        User user = checkWorkspaceReadAccess(BinaryResource.parseWorkspaceId(pFullName));
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
    public MasterDocument updateDoc(DocumentKey pKey, String pRevisionNote, InstanceAttribute[] pAttributes, DocumentKey[] pLinkKeys) throws WorkspaceNotFoundException, NotAllowedException, MasterDocumentNotFoundException, AccessRightException, UserNotFoundException, UserNotActiveException {
        User user = checkWorkspaceWriteAccess(pKey.getWorkspaceId());
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
            Map<String,InstanceAttribute>  attrs = new HashMap<String, InstanceAttribute>();
            for (InstanceAttribute attr : pAttributes) {
                attr.setDocument(doc);
                attrs.put(attr.getName(),attr);
            }


            Set<InstanceAttribute> attrsToRemove = new HashSet<InstanceAttribute>(doc.getInstanceAttributes().values());
            attrsToRemove.removeAll(attrs.values());

            InstanceAttributeDAO attrDAO = new InstanceAttributeDAO(em);
            for (InstanceAttribute attrToRemove : attrsToRemove) {
                attrDAO.removeAttribute(attrToRemove);
            }

                        
            doc.setRevisionNote(pRevisionNote);
            doc.setLinkedDocuments(links);
            doc.setInstanceAttributes(attrs);
            return mdoc;

        } else {
            throw new NotAllowedException(new Locale(user.getLanguage()), "NotAllowedException25");
        }

    }

    @RolesAllowed("users")
    public MasterDocument[] createVersion(MasterDocumentKey pOriginalMDocPK,
            String pTitle, String pDescription, String pWorkflowModelId, ACLUserEntry[] pACLUserEntries, ACLUserGroupEntry[] pACLUserGroupEntries) throws WorkspaceNotFoundException, NotAllowedException, MasterDocumentNotFoundException, WorkflowModelNotFoundException, AccessRightException, MasterDocumentAlreadyExistsException, FileAlreadyExistsException, UserNotFoundException, UserNotActiveException, CreationException {
        User user = checkWorkspaceWriteAccess(pOriginalMDocPK.getWorkspaceId());
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
                clonedAttribute.setDocument(firstIte);
                attrs.put(clonedAttribute.getName(), clonedAttribute);
            }
            firstIte.setInstanceAttributes(attrs);
        }

        if (pWorkflowModelId != null) {
            WorkflowModel workflowModel = new WorkflowModelDAO(new Locale(user.getLanguage()), em).loadWorkflowModel(new BasicElementKey(user.getWorkspaceId(), pWorkflowModelId));
            mdoc.setWorkflow(workflowModel.createWorkflow());
        }
        mdoc.setTitle(pTitle);
        mdoc.setDescription(pDescription);
        if((pACLUserEntries !=null && pACLUserEntries.length >0) || (pACLUserGroupEntries!=null && pACLUserGroupEntries.length >0)){
            ACL acl=new ACL();
            if(pACLUserEntries !=null){
                for(ACLUserEntry entry:pACLUserEntries)
                    acl.addEntry(em.getReference(User.class, new UserKey(user.getWorkspaceId(),entry.getPrincipalLogin())), entry.getPermission());
            }

            if(pACLUserGroupEntries !=null){
               for(ACLUserGroupEntry entry:pACLUserGroupEntries)
                    acl.addEntry(em.getReference(UserGroup.class, new BasicElementKey(user.getWorkspaceId(),entry.getPrincipalId())), entry.getPermission());
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
    public void subscribeToStateChangeEvent(MasterDocumentKey pMDocPK) throws WorkspaceNotFoundException, NotAllowedException, MasterDocumentNotFoundException, UserNotFoundException, UserNotActiveException {
        User user = checkWorkspaceReadAccess(pMDocPK.getWorkspaceId());
        MasterDocument mdoc = new MasterDocumentDAO(new Locale(user.getLanguage()), em).loadMDoc(pMDocPK);
        String owner = mdoc.getLocation().getOwner();
        if ((owner != null) && (!owner.equals(user.getLogin()))) {
            throw new NotAllowedException(new Locale(user.getLanguage()), "NotAllowedException30");
        }

        new SubscriptionDAO(em).createStateChangeSubscription(new StateChangeSubscription(user, mdoc));
    }

    @RolesAllowed("users")
    public void unsubscribeToStateChangeEvent(MasterDocumentKey pMDocPK) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException {
        User user = checkWorkspaceReadAccess(pMDocPK.getWorkspaceId());
        SubscriptionKey key = new SubscriptionKey(user.getWorkspaceId(), user.getLogin(), pMDocPK.getWorkspaceId(), pMDocPK.getId(), pMDocPK.getVersion());
        new SubscriptionDAO(em).removeStateChangeSubscription(key);
    }

    @RolesAllowed("users")
    public void subscribeToIterationChangeEvent(MasterDocumentKey pMDocPK) throws WorkspaceNotFoundException, NotAllowedException, MasterDocumentNotFoundException, UserNotFoundException, UserNotActiveException {
        User user = checkWorkspaceReadAccess(pMDocPK.getWorkspaceId());
        MasterDocument mdoc = new MasterDocumentDAO(new Locale(user.getLanguage()), em).getMDocRef(pMDocPK);
        String owner = mdoc.getLocation().getOwner();
        if ((owner != null) && (!owner.equals(user.getLogin()))) {
            throw new NotAllowedException(new Locale(user.getLanguage()), "NotAllowedException30");
        }

        new SubscriptionDAO(em).createIterationChangeSubscription(new IterationChangeSubscription(user, mdoc));
    }

    @RolesAllowed("users")
    public void unsubscribeToIterationChangeEvent(MasterDocumentKey pMDocPK) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException {
        User user = checkWorkspaceReadAccess(pMDocPK.getWorkspaceId());
        SubscriptionKey key = new SubscriptionKey(user.getWorkspaceId(), user.getLogin(), pMDocPK.getWorkspaceId(), pMDocPK.getId(), pMDocPK.getVersion());
        new SubscriptionDAO(em).removeIterationChangeSubscription(key);
    }

    @RolesAllowed("users")
    public User savePersonalInfo(String pWorkspaceId, String pName, String pEmail, String pLanguage) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException {
        User user = checkWorkspaceReadAccess(pWorkspaceId);
        user.setName(pName);
        user.setEmail(pEmail);
        user.setLanguage(pLanguage);
        return user;
    }

    @RolesAllowed("users")
    public User[] getUsers(String pWorkspaceId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException {
        User user = checkWorkspaceReadAccess(pWorkspaceId);
        return new UserDAO(new Locale(user.getLanguage()), em).findAllUsers(pWorkspaceId);
    }

    @RolesAllowed("users")
    public String[] getTags(String pWorkspaceId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException {
        User user = checkWorkspaceReadAccess(pWorkspaceId);
        Tag[] tags = new TagDAO(new Locale(user.getLanguage()), em).findAllTags(pWorkspaceId);

        String[] labels = new String[tags.length];
        int i = 0;
        for (Tag t : tags) {
            labels[i++] = t.getLabel();
        }
        return labels;
    }

    private User checkWorkspaceReadAccess(String pWorkspaceId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException {
        String login = ctx.getCallerPrincipal().toString();
        UserDAO userDAO = new UserDAO(em);
        WorkspaceUserMembership userMS = userDAO.loadUserMembership(new WorkspaceUserMembershipKey(pWorkspaceId, pWorkspaceId, login));
        if (userMS != null) {
            return userMS.getMember();
        }
        Workspace wks = new WorkspaceDAO(em).loadWorkspace(pWorkspaceId);
        User user = userDAO.loadUser(new UserKey(pWorkspaceId, login));
        if (wks.getAdmin().getLogin().equals(login)) {
            return user;
        }
        WorkspaceUserGroupMembership[] groupMS = new UserGroupDAO(em).getUserGroupMemberships(pWorkspaceId, user);
        if (groupMS.length > 0) {
            return user;
        } else {
            throw new UserNotActiveException(Locale.getDefault(), login);
        }
    }

    private User checkWorkspaceWriteAccess(String pWorkspaceId) throws UserNotFoundException, WorkspaceNotFoundException, AccessRightException {
        String login = ctx.getCallerPrincipal().toString();
        UserDAO userDAO = new UserDAO(em);

        Workspace wks = new WorkspaceDAO(em).loadWorkspace(pWorkspaceId);
        User user = userDAO.loadUser(new UserKey(pWorkspaceId, login));
        if (wks.getAdmin().getLogin().equals(login)) {
            return user;
        }

        WorkspaceUserMembership userMS = userDAO.loadUserMembership(new WorkspaceUserMembershipKey(pWorkspaceId, pWorkspaceId, login));
        if (userMS != null) {
            if (userMS.isReadOnly()) {
                throw new AccessRightException(new Locale(user.getLanguage()), user);
            } else {
                return userMS.getMember();
            }
        }

        WorkspaceUserGroupMembership[] groupMS = new UserGroupDAO(em).getUserGroupMemberships(pWorkspaceId, user);
        for (WorkspaceUserGroupMembership ms : groupMS) {
            if (!ms.isReadOnly()) {
                return user;
            }
        }
        throw new AccessRightException(new Locale(user.getLanguage()), user);
    }

    private Folder checkWritingRight(User pUser, Folder pFolder) throws NotAllowedException {
        if (pFolder.isPrivate() && (!pFolder.getOwner().equals(pUser.getLogin()))) {
            throw new NotAllowedException(new Locale(pUser.getLanguage()), "NotAllowedException33");
        }
        return pFolder;
    }

    private Account checkAdmin(Workspace pWorkspace) throws AccessRightException, AccountNotFoundException {
        Account account = new AccountDAO(em).loadAccount(ctx.getCallerPrincipal().toString());
        if (!pWorkspace.getAdmin().equals(account)) {
            throw new AccessRightException(new Locale(account.getLanguage()), account);
        }
        return account;
    }

    private Account checkAdmin(String pWorkspaceId) throws AccessRightException, AccountNotFoundException, WorkspaceNotFoundException {
        Account account = new AccountDAO(em).loadAccount(ctx.getCallerPrincipal().toString());
        Workspace wks = new WorkspaceDAO(new Locale(account.getLanguage()), em).loadWorkspace(pWorkspaceId);
        if (!wks.getAdmin().equals(account)) {
            throw new AccessRightException(new Locale(account.getLanguage()), account);
        }
        return account;
    }

    private void checkFolderStructureRight(User pUser) throws NotAllowedException {
        Workspace wks = pUser.getWorkspace();
        if (wks.isFolderLocked() && (!pUser.isAdministrator())) {
            throw new NotAllowedException(new Locale(pUser.getLanguage()), "NotAllowedException7");
        }
    }

    private void sendStateNotification(User[] pSubscribers, MasterDocument pMasterDocument) {
        sendMessageToMailer("StateNotification", pSubscribers, pMasterDocument);
    }

    private void sendApproval(Collection runningTasks, MasterDocument mdoc) {
        sendMessageToMailer("Approval", runningTasks, mdoc);
    }

    private void sendIterationNotification(User[] subscribers, MasterDocument mdoc) {
        sendMessageToMailer("IterationNotification", subscribers, mdoc);
    }

    private void sendMessageToMailer(String pMessageType, Object... pArgs) {
        try {
            Connection connection = connectionFactory.createConnection();
            Session session = connection.createSession(false,
                    Session.AUTO_ACKNOWLEDGE);
            MessageProducer messageProducer = session.createProducer(mailerQueue);
            ObjectMessage message = session.createObjectMessage();
            message.setStringProperty("messageType", pMessageType);
            message.setObject(pArgs);
            messageProducer.send(message);
            session.close();
            connection.close();
        } catch (JMSException ex) {
            throw new EJBException(ex);
        }
    }

    private void sendMessageToIndexer(String pFullName, String pPathName, String pAction) {
        try {
            Connection connection = connectionFactory.createConnection();
            Session session = connection.createSession(false,
                    Session.AUTO_ACKNOWLEDGE);
            MessageProducer messageProducer = session.createProducer(indexerQueue);
            Message message = session.createMessage();
            message.setStringProperty("fullName", pFullName);
            message.setStringProperty("pathName", pPathName);
            message.setStringProperty("action", pAction);
            messageProducer.send(message);
            session.close();
            connection.close();
        } catch (JMSException ex) {
            throw new EJBException(ex);
        }
    }
}
