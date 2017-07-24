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

import com.docdoku.core.common.Account;
import com.docdoku.core.common.User;
import com.docdoku.core.common.Workspace;
import com.docdoku.core.document.DocumentRevision;
import com.docdoku.core.meta.Tag;
import com.docdoku.core.product.PartRevision;
import com.docdoku.core.services.IMailerLocal;
import com.docdoku.core.services.IPlatformOptionsManagerLocal;
import com.docdoku.core.util.FileIO;
import com.docdoku.core.workflow.Task;

import javax.annotation.Resource;
import javax.ejb.Asynchronous;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Session class MailerBean
 *
 * @author Florent.Garin
 */
@Local(IMailerLocal.class)
@Stateless(name = "MailerBean")
public class MailerBean implements IMailerLocal {

    private static final String BASE_NAME = "com.docdoku.server.templates.MailText";

    @Inject
    private ConfigManager configManager;

    @Inject
    private IPlatformOptionsManagerLocal platformOptionsManager;

    @Resource(name = "mail/docdokuSMTP")
    private Session mailSession;

    private static final Logger LOGGER = Logger.getLogger(MailerBean.class.getName());

    @Asynchronous
    @Override
    public void sendStateNotification(Collection<User> pSubscribers,
                                      DocumentRevision pDocumentRevision) {

        LOGGER.info("Sending state notification emails \n\tfor the document " + pDocumentRevision.getLastIteration());

        try {
            for (User pSubscriber : pSubscribers) {
                sendStateNotification(pSubscriber, pDocumentRevision);
            }
        } catch (MessagingException pMEx) {
            logMessagingException(pMEx);
        }
    }

    @Asynchronous
    @Override
    public void sendIterationNotification(Collection<User> pSubscribers,
                                          DocumentRevision pDocumentRevision) {

        LOGGER.info("Sending iteration notification emails \n\tfor the document " + pDocumentRevision.getLastIteration());

        try {
            for (User pSubscriber : pSubscribers) {
                sendIterationNotification(pSubscriber, pDocumentRevision);
            }
        } catch (MessagingException pMEx) {
            logMessagingException(pMEx);
        }
    }

    @Asynchronous
    @Override
    public void sendTaggedNotification(Collection<User> pSubscribers, DocumentRevision pDocR, Tag pTag) {

        LOGGER.info("Sending tagged notification emails \n\tfor the document " + pDocR.getLastIteration());

        try {
            for (User pSubscriber : pSubscribers) {
                sendTaggedNotification(pSubscriber, pDocR, pTag);
            }
        } catch (MessagingException pMEx) {
            logMessagingException(pMEx);
        }
    }

    @Asynchronous
    @Override
    public void sendTaggedNotification(Collection<User> pSubscribers, PartRevision pPartR, Tag pTag) {

        LOGGER.info("Sending tagged notification emails \n\tfor the part " + pPartR.getLastIteration());

        try {
            for (User pSubscriber : pSubscribers) {
                sendTaggedNotification(pSubscriber, pPartR, pTag);
            }
        } catch (MessagingException pMEx) {
            logMessagingException(pMEx);
        }
    }

    @Asynchronous
    @Override
    public void sendUntaggedNotification(Collection<User> pSubscribers, DocumentRevision pDocR, Tag pTag) {

        LOGGER.info("Sending untagged notification emails \n\tfor the document " + pDocR.getLastIteration());

        try {
            for (User pSubscriber : pSubscribers) {
                sendUntaggedNotification(pSubscriber, pDocR, pTag);
            }
        } catch (MessagingException pMEx) {
            logMessagingException(pMEx);
        }
    }

    @Asynchronous
    @Override
    public void sendUntaggedNotification(Collection<User> pSubscribers, PartRevision pPartR, Tag pTag) {

        LOGGER.info("Sending untagged notification emails \n\tfor the part " + pPartR.getLastIteration());

        try {
            for (User pSubscriber : pSubscribers) {
                sendUntaggedNotification(pSubscriber, pPartR, pTag);
            }
        } catch (MessagingException pMEx) {
            logMessagingException(pMEx);
        }
    }

    @Asynchronous
    @Override
    public void sendApproval(Collection<Task> pRunningTasks,
                             DocumentRevision pDocumentRevision) {

        LOGGER.info("Sending approval emails \n\tfor the document " + pDocumentRevision.getLastIteration());

        try {
            for (Task task : pRunningTasks) {
                sendApproval(task, pDocumentRevision);
            }
        } catch (MessagingException pMEx) {
            logMessagingException(pMEx);
        }
    }

    @Asynchronous
    @Override
    public void sendApproval(Collection<Task> pRunningTasks, PartRevision partRevision) {

        LOGGER.info("Sending approval required emails \n\tfor the part " + partRevision.getLastIteration());

        try {
            for (Task task : pRunningTasks) {
                sendApproval(task, partRevision);
            }
        } catch (MessagingException pMEx) {
            logMessagingException(pMEx);
        }
    }

    @Asynchronous
    @Override
    public void sendPasswordRecovery(Account account, String recoveryUUID) {

        LOGGER.info("Sending recovery message \n\tfor the user which login is " + account.getLogin());

        Locale locale = new Locale(account.getLanguage());
        Object[] args = {
                getRecoveryUrl(recoveryUUID),
                account.getLogin()
        };

        String subject = getString("Recovery_title", locale);
        String body = getBody("Recovery_text", args, locale);

        try {
            sendMessage(account, subject, body);
        } catch (MessagingException pMEx) {
            logMessagingException(pMEx);
        }
    }

    @Asynchronous
    @Override
    public void sendWorkspaceDeletionNotification(Account admin, String workspaceId) {

        LOGGER.info("Sending workspace deletion notification message \n\tfor the user which login is " + admin.getLogin());

        Locale locale = new Locale(admin.getLanguage());

        Object[] args = {
                workspaceId
        };

        String subject = getSubject("WorkspaceDeletion_title", locale);
        String body = getBody("WorkspaceDeletion_text", args, locale);

        try {
            sendMessage(admin, subject, body);
        } catch (MessagingException pMEx) {
            logMessagingException(pMEx);
        }
    }

    @Asynchronous
    @Override
    public void sendWorkspaceDeletionErrorNotification(Account admin, String workspaceId) {

        LOGGER.info("Sending workspace deletion error notification message \n\tfor the user which login is " + admin.getLogin());

        Locale locale = new Locale(admin.getLanguage());

        Object[] args = {
                workspaceId
        };

        String subject = getSubject("WorkspaceDeletion_title", locale);
        String body = getBody("WorkspaceDeletionError_text", args, locale);

        try {
            sendMessage(admin, subject, body);
        } catch (MessagingException pMEx) {
            logMessagingException(pMEx);
        }
    }


    @Asynchronous
    @Override
    public void sendPartRevisionWorkflowRelaunchedNotification(PartRevision partRevision) {

        Workspace workspace = partRevision.getPartMaster().getWorkspace();
        Account admin = workspace.getAdmin();
        User author = partRevision.getAuthor();

        LOGGER.info("Sending workflow relaunch notification email \n\tfor the part " + partRevision.getLastIteration() + " to admin: " + admin.getLogin());

        // Mail both workspace admin and partRevision author
        sendWorkflowRelaunchedNotification(admin.getName(), admin.getEmail(), admin.getLanguage(), workspace.getId(), partRevision);


        if (!admin.getLogin().equals(author.getLogin())) {
            LOGGER.info("Sending workflow relaunch notification email \n\tfor the part " + partRevision.getLastIteration() + " to user: " + author.getLogin());
            sendWorkflowRelaunchedNotification(author.getName(), author.getEmail(), author.getLanguage(), workspace.getId(), partRevision);
        }

    }

    @Asynchronous
    @Override
    public void sendDocumentRevisionWorkflowRelaunchedNotification(DocumentRevision documentRevision) {
        Workspace workspace = documentRevision.getDocumentMaster().getWorkspace();
        Account admin = workspace.getAdmin();
        User author = documentRevision.getAuthor();

        // Mail both workspace admin and documentMaster author
        sendWorkflowRelaunchedNotification(admin.getName(), admin.getEmail(), admin.getLanguage(), workspace.getId(), documentRevision);

        if (!admin.getLogin().equals(author.getLogin())) {
            sendWorkflowRelaunchedNotification(author.getName(), author.getEmail(), author.getLanguage(), workspace.getId(), documentRevision);
        }
    }

    @Asynchronous
    @Override
    public void sendWorkspaceIndexationSuccess(Account account, String workspaceId, String extraMessage) {

        Locale locale = new Locale(account.getLanguage());

        Object[] args = {
                workspaceId,
                extraMessage
        };

        String subject = getSubject("Indexer_success_title", locale);
        String body = getBody("Indexer_success_text", args, locale);

        try {
            sendMessage(account, subject, body);
        } catch (MessagingException pMEx) {
            logMessagingException(pMEx);
        }
    }

    @Asynchronous
    @Override
    public void sendWorkspaceIndexationFailure(Account account, String workspaceId, String extraMessage) {
        Locale locale = new Locale(account.getLanguage());

        Object[] args = {
                workspaceId,
                extraMessage
        };

        String subject = getSubject("Indexer_failure_title", locale);
        String body = getBody("Indexer_failure_text", args, locale);

        try {
            sendMessage(account, subject, body);
        } catch (MessagingException pMEx) {
            logMessagingException(pMEx);
        }
    }

    @Override
    public void sendBulkIndexationSuccess(Account account) {
        Locale locale = new Locale(account.getLanguage());

        Object[] args = {};

        String subject = getSubject("Indexer_bulk_success_title", locale);
        String body = getBody("Indexer_bulk_success_text", args, locale);

        try {
            sendMessage(account, subject, body);
        } catch (MessagingException pMEx) {
            logMessagingException(pMEx);
        }
    }

    @Override
    public void sendBulkIndexationFailure(Account account, String failureMessage) {
        Locale locale = new Locale(account.getLanguage());
        Object[] args = {
                failureMessage
        };
        String subject = getSubject("Indexer_bulk_failure_title", locale);
        String body = getBody("Indexer_bulk_failure_text", args, locale);

        try {
            sendMessage(account, subject, body);
        } catch (MessagingException pMEx) {
            logMessagingException(pMEx);
        }
    }

    @Asynchronous
    @Override
    public void sendCredential(Account account) {

        Locale locale = new Locale(account.getLanguage());

        String accountDisabledMessage = "";
        if (!account.isEnabled()) {
            switch (platformOptionsManager.getWorkspaceCreationStrategy()) {
                case ADMIN_VALIDATION:
                    accountDisabledMessage = getString("SignUp_AccountDisabled_text", locale);
                    break;
            }
        }

        Object[] args = {
                account.getLogin(),
                configManager.getCodebase(),
                accountDisabledMessage
        };

        String subject = getSubject("SignUp_success_title", locale);
        String body = getBody("SignUp_success_text", args, locale);

        try {
            sendMessage(account, subject, body);
        } catch (MessagingException pMEx) {
            logMessagingException(pMEx);
        }
    }

    private void sendStateNotification(User pSubscriber,
                                       DocumentRevision pDocumentRevision) throws MessagingException {

        LOGGER.info("Sending state notification emails \n\tfor the document " + pDocumentRevision.getLastIteration() + " to user " + pSubscriber.getLogin());

        Locale locale = new Locale(pSubscriber.getLanguage());
        String stateName = pDocumentRevision.getLifeCycleState();
        stateName = (stateName != null && !stateName.isEmpty()) ? stateName : getString("FinalState_name", locale);

        Object[] args = {
                pDocumentRevision,
                pDocumentRevision.getLastIteration().getCreationDate(),
                getDocumentRevisionPermalinkURL(pDocumentRevision),
                stateName
        };

        String subject = getSubject("StateNotification_title", locale);
        String body = getBody("StateNotification_text", args, locale);

        sendMessage(pSubscriber, subject, body);
    }

    private void sendIterationNotification(User pSubscriber,
                                           DocumentRevision pDocumentRevision) throws MessagingException {

        LOGGER.info("Sending iteration notification emails \n\tfor the document " + pDocumentRevision.getLastIteration());

        Locale locale = new Locale(pSubscriber.getLanguage());

        Object[] args = {
                pDocumentRevision,
                pDocumentRevision.getLastIteration().getCreationDate(),
                pDocumentRevision.getLastIteration().getIteration(),
                pDocumentRevision.getLastIteration().getAuthor(),
                getDocumentRevisionPermalinkURL(pDocumentRevision)
        };

        String subject = getSubject("IterationNotification_title", locale);
        String body = getBody("IterationNotification_text", args, locale);

        sendMessage(pSubscriber, subject, body);

    }

    private void sendTaggedNotification(User pSubscriber, DocumentRevision pDocumentRevision, Tag pTag) throws MessagingException {
        sendTaggedNotification(pSubscriber, pDocumentRevision, pTag, true);
    }

    private void sendUntaggedNotification(User pSubscriber, DocumentRevision pDocumentRevision, Tag pTag) throws MessagingException {
        sendTaggedNotification(pSubscriber, pDocumentRevision, pTag, false);
    }

    private void sendTaggedNotification(User pSubscriber,
                                        DocumentRevision pDocumentRevision, Tag pTag, boolean tagged) throws MessagingException {
        LOGGER.info("Sending tag notification emails \n\tfor the document " + pDocumentRevision.getLastIteration() + " to subscriber : " + pSubscriber.getLogin());

        Locale locale = new Locale(pSubscriber.getLanguage());
        String subject = getSubject("TagNotification_title", locale);

        Object[] args = {
                pTag,
                pDocumentRevision,
                getDocumentRevisionPermalinkURL(pDocumentRevision)
        };

        String body = getBody(tagged ? "TagNotificationTagged_text" : "TagNotificationUntagged_text", args, locale);

        sendMessage(pSubscriber, subject, body);
    }

    private void sendTaggedNotification(User pSubscriber, PartRevision pPartRevision, Tag pTag) throws MessagingException {
        sendTaggedNotification(pSubscriber, pPartRevision, pTag, true);
    }

    private void sendUntaggedNotification(User pSubscriber, PartRevision pPartRevision, Tag pTag) throws MessagingException {
        sendTaggedNotification(pSubscriber, pPartRevision, pTag, false);
    }

    private void sendTaggedNotification(User pSubscriber, PartRevision pPartRevision, Tag pTag, boolean tagged) throws MessagingException {
        LOGGER.info("Sending tag notification emails \n\tfor the part " + pPartRevision.getLastIteration());

        Locale locale = new Locale(pSubscriber.getLanguage());
        Object[] args = {
                pTag,
                pPartRevision,
                getPartRevisionPermalinkURL(pPartRevision)
        };

        String subject = getSubject("TagNotification_title", locale);
        String body = getBody(tagged ? "TagNotificationTagged_text" : "TagNotificationUnTagged_text", args, locale);

        sendMessage(pSubscriber, subject, body);
    }

    private void sendApproval(Task task, DocumentRevision pDocumentRevision) throws MessagingException {

        LOGGER.info("Sending approval required emails \n\tfor the document " + pDocumentRevision.getLastIteration());

        Set<User> workers = new HashSet<>();
        workers.addAll(task.getAssignedUsers());
        task.getAssignedGroups().forEach(g -> workers.addAll(g.getUsers()));

        for (User worker : workers) {
            sendApprovalToUser(worker, task, pDocumentRevision);
        }
    }

    private void sendApproval(Task task, PartRevision partRevision) throws MessagingException {

        LOGGER.info("Sending approval required emails \n\tfor the part " + partRevision.getLastIteration());

        Set<User> workers = new HashSet<>();
        workers.addAll(task.getAssignedUsers());
        task.getAssignedGroups().forEach(g -> workers.addAll(g.getUsers()));

        for (User worker : workers) {
            sendApprovalToUser(worker, task, partRevision);
        }
    }


    private void sendApprovalToUser(User worker, Task task, DocumentRevision pDocumentRevision) throws MessagingException {

        LOGGER.info("Sending approval email \n\tfor the document " + pDocumentRevision.getLastIteration() + " to user: " + worker.getLogin());

        Locale locale = new Locale(worker.getLanguage());

        String subject = getSubject("Approval_title", locale);

        Object[] args = {
                task.getTitle(),
                getDocumentRevisionPermalinkURL(pDocumentRevision),
                pDocumentRevision.getKey(),
                task.getInstructions() == null ? "-" : task.getInstructions(),
                getTaskUrl(task, pDocumentRevision.getWorkspaceId())
        };

        String body = getBody("Approval_document_text", args, locale);

        sendMessage(worker, subject, body);
    }


    private void sendApprovalToUser(User worker, Task pTask, PartRevision partRevision) throws MessagingException {

        LOGGER.info("Sending approval email \n\tfor the part " + partRevision.getLastIteration() + " to user: " + worker.getLogin());

        Locale locale = new Locale(worker.getLanguage());

        String subject = getSubject("Approval_title", locale);

        Object[] args = {
                pTask.getTitle(),
                getPartRevisionPermalinkURL(partRevision),
                partRevision.getKey(),
                pTask.getInstructions() == null ? "-" : pTask.getInstructions(),
                getTaskUrl(pTask, partRevision.getWorkspaceId())
        };

        String body = getBody("Approval_part_text", args, locale);

        sendMessage(worker, subject, body);

    }


    private void sendWorkflowRelaunchedNotification(String userName, String userEmail, String userLanguage, String workspaceId, PartRevision partRevision) {

        Locale locale = new Locale(userLanguage);

        Object[] args = {
                partRevision.getPartNumber() + "-" + partRevision.getVersion(),
                workspaceId,
                partRevision.getWorkflow().getLifeCycleState()
        };

        String subject = getSubject("PartRevision_workflow_relaunched_title", locale);
        String body = getBody("PartRevision_workflow_relaunched_text", args, locale);

        try {
            sendMessage(userEmail, userName, subject, body);
        } catch (MessagingException pMEx) {
            logMessagingException(pMEx);
        }
    }


    private void sendWorkflowRelaunchedNotification(String userName, String userEmail, String userLanguage, String workspaceId, DocumentRevision documentRevision) {


        Locale locale = new Locale(userLanguage);

        Object[] args = {
                documentRevision.getId() + "-" + documentRevision.getVersion(),
                workspaceId,
                documentRevision.getWorkflow().getLifeCycleState()
        };

        String subject = getSubject("DocumentRevision_workflow_relaunched_title", locale);
        String body = getBody("DocumentRevision_workflow_relaunched_text", args, locale);

        try {
            sendMessage(userEmail, userName, subject, body);
        } catch (MessagingException pMEx) {
            logMessagingException(pMEx);
        }
    }

    // URIs
    // todo : move to properties file and format
    private String getDocumentRevisionPermalinkURL(DocumentRevision pDocR) {
        return configManager.getCodebase() + "/documents/index.html#" + pDocR.getWorkspaceId() + "/" + FileIO.encode(pDocR.getId()) + "/" + pDocR.getVersion();
    }

    private String getPartRevisionPermalinkURL(PartRevision pPartR) {
        return configManager.getCodebase() + "/parts/index.html#" + pPartR.getWorkspaceId() + "/" + FileIO.encode(pPartR.getPartNumber()) + "/" + pPartR.getVersion();
    }

    private String getTaskUrl(Task pTask, String workspaceId) {
        return configManager.getCodebase() + "/change-management/index.html#" + workspaceId + "/tasks/" + pTask.getWorkflowId() + "-" + pTask.getActivityStep() + "-" + pTask.getNum();
    }

    private String getRecoveryUrl(String uuid) {
        return configManager.getCodebase() + "/index.html#recover/" + uuid;
    }


    // Log shortcuts
    private void logMessagingException(MessagingException pMEx) {
        String logMessage = "Message format error. \n\tMail can't be sent. \n\t" + pMEx.getMessage();
        LOGGER.log(Level.SEVERE, logMessage, pMEx);
    }

    // Template utils methods

    private ResourceBundle getBundle(Locale pLocale) {
        return ResourceBundle.getBundle(BASE_NAME, pLocale);
    }

    private String getString(String string, Locale pLocale) {
        return getBundle(pLocale).getString(string);
    }

    private String format(String string, Object[] args, Locale pLocale) {
        return MessageFormat.format(getString(string, pLocale), args);
    }

    private String getBody(String string, Object[] args, Locale pLocale) {
        String mailBodyTemplate = getString("MailBodyTemplate", pLocale);
        String body = format(string, args, pLocale);
        return MessageFormat.format(mailBodyTemplate, body);
    }

    private String getSubject(String string, Locale pLocale) {
        String mailSubjectTemplate = getString("MailSubjectTemplate", pLocale);
        return mailSubjectTemplate + " " + getString(string, pLocale);
    }

    private void sendMessage(Account account, String subject, String content) throws MessagingException {
        sendMessage(account.getEmail(), account.getName(), subject, content);
    }

    private void sendMessage(User user, String subject, String content) throws MessagingException {
        sendMessage(user.getEmail(), user.getName(), subject, content);
    }

    private void sendMessage(String email, String name, String subject, String content) throws MessagingException {

        if (email == null || email.isEmpty()) {
            LOGGER.log(Level.WARNING, "Cannot send mail, email is empty");
            return;
        }

        try {
            InternetAddress emailAddress = new InternetAddress(email, name);
            Message message = new MimeMessage(mailSession);
            message.addRecipient(Message.RecipientType.TO, emailAddress);
            message.setSubject(subject);
            message.setSentDate(new Date());
            message.setContent(content, "text/html; charset=utf-8");
            message.setFrom();
            Transport.send(message);
        } catch (UnsupportedEncodingException e) {
            String logMessage = "Unsupported encoding: " + e.getMessage();
            LOGGER.log(Level.SEVERE, logMessage, e);
        }

    }

}
