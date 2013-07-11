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

import com.docdoku.core.common.Account;
import com.docdoku.core.common.User;
import com.docdoku.core.common.Workspace;
import com.docdoku.core.document.DocumentMaster;
import com.docdoku.core.product.PartRevision;
import com.docdoku.core.services.IMailerLocal;
import com.docdoku.core.workflow.Task;

import javax.annotation.Resource;
import javax.ejb.Asynchronous;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Logger;

/**
 * Session class MailerBean
 *
 * @author Florent.Garin
 */
@Local(IMailerLocal.class)
@Stateless(name = "MailerBean")
public class MailerBean implements IMailerLocal {

    private final static String BASE_NAME = "com.docdoku.server.templates.MailText";
    @Resource(name = "mail/docdokuSMTP")
    private Session mailSession;
    @Resource(name = "codebase")
    private String codebase;
    private final static Logger LOGGER = Logger.getLogger(MailerBean.class.getName());

    @Asynchronous
    @Override
    public void sendStateNotification(User[] pSubscribers,
            DocumentMaster pDocumentMaster) {
        try {
            javax.mail.Message message = new MimeMessage(mailSession);

            for (int i = 0; i < pSubscribers.length; i++) {
                try {
                    message.addRecipient(javax.mail.Message.RecipientType.TO,
                            new InternetAddress(pSubscribers[i].getEmail(),
                            pSubscribers[i].getName()));
                    message.setSubject("State notification");
                    message.setSentDate(new Date());
                    message.setContent(getStateNotificationMessage(pDocumentMaster, new Locale(pSubscribers[i].getLanguage())),
                            "text/html; charset=utf-8");
                    message.setFrom();
                    Transport.send(message);
                    LOGGER.info("Sending state notification emails");
                    LOGGER.info("for the document " + pDocumentMaster.getLastIteration());

                } catch (UnsupportedEncodingException pUEEx) {
                    LOGGER.warning("Mail address format error.");
                    LOGGER.warning(pUEEx.getMessage());
                }
            }
        } catch (MessagingException pMEx) {
            LOGGER.severe("Message format error.");
            LOGGER.severe("Notifications can't be sent.");
            LOGGER.severe(pMEx.getMessage());
        }
    }

    @Asynchronous
    @Override
    public void sendIterationNotification(User[] pSubscribers,
            DocumentMaster pDocumentMaster) {
        try {
            for (int i = 0; i < pSubscribers.length; i++) {
                try {
                    javax.mail.Message message = new MimeMessage(mailSession);
                    message.addRecipient(javax.mail.Message.RecipientType.TO,
                            new InternetAddress(pSubscribers[i].getEmail(),
                            pSubscribers[i].getName()));
                    message.setSubject("Iteration notification");
                    message.setSentDate(new Date());
                    message.setContent(getIterationNotificationMessage(pDocumentMaster, new Locale(pSubscribers[i].getLanguage())),
                            "text/html; charset=utf-8");
                    message.setFrom();
                    Transport.send(message);
                    LOGGER.info("Sending iteration notification emails");
                    LOGGER.info("for the document " + pDocumentMaster.getLastIteration());

                } catch (UnsupportedEncodingException pUEEx) {
                    LOGGER.warning("Mail address format error.");
                    LOGGER.warning(pUEEx.getMessage());
                }
            }
        } catch (MessagingException pMEx) {
            LOGGER.severe("Message format error.");
            LOGGER.severe("Notifications can't be sent.");
            LOGGER.severe(pMEx.getMessage());
        }
    }

    @Asynchronous
    @Override
    public void sendApproval(Collection<Task> pRunningTasks,
            DocumentMaster pDocumentMaster) {
        try {
            for (Task task : pRunningTasks) {
                try {
                    javax.mail.Message message = new MimeMessage(mailSession);
                    User worker = task.getWorker();
                    message.setRecipient(javax.mail.Message.RecipientType.TO,
                            new InternetAddress(worker.getEmail(), worker.getName()));
                    message.setSubject("Approval required");
                    message.setSentDate(new Date());
                    message.setContent(getApprovalRequiredMessage(task, pDocumentMaster, new Locale(worker.getLanguage())),
                            "text/html; charset=utf-8");
                    message.setFrom();
                    Transport.send(message);
                    LOGGER.info("Sending approval required emails");
                    LOGGER.info("for the document " + pDocumentMaster.getLastIteration());
                } catch (UnsupportedEncodingException pUEEx) {
                    LOGGER.warning("Mail address format error.");
                    LOGGER.warning(pUEEx.getMessage());
                }
            }
        } catch (MessagingException pMEx) {
            LOGGER.severe("Message format error.");
            LOGGER.severe("Approval can't be sent.");
            LOGGER.severe(pMEx.getMessage());
        }
    }

    @Asynchronous
    @Override
    public void sendPasswordRecovery(Account account, String passwordRRUuid) {
        try {
            javax.mail.Message message = new MimeMessage(mailSession);
            message.setRecipient(javax.mail.Message.RecipientType.TO,
                    new InternetAddress(account.getEmail(), account.getName()));
            message.setSubject("Password recovery");
            message.setSentDate(new Date());
            message.setContent(getPasswordRecoveryMessage(account, passwordRRUuid, new Locale(account.getLanguage())),
                    "text/html; charset=utf-8");
            message.setFrom();
            Transport.send(message);
            LOGGER.info("Sending recovery message");
            LOGGER.info("for the user which login is " + account.getLogin());
        } catch (UnsupportedEncodingException pUEEx) {
            LOGGER.warning("Mail address format error.");
            LOGGER.warning(pUEEx.getMessage());
        } catch (MessagingException pMEx) {
            LOGGER.severe("Message format error.");
            LOGGER.severe("Recovery message can't be sent.");
            LOGGER.severe(pMEx.getMessage());
        }
    }

    @Asynchronous
    @Override
    public void sendApproval(Collection<Task> pRunningTasks, PartRevision partRevision) {
        try {
            for (Task task : pRunningTasks) {
                try {
                    javax.mail.Message message = new MimeMessage(mailSession);
                    User worker = task.getWorker();
                    message.setRecipient(javax.mail.Message.RecipientType.TO,
                            new InternetAddress(worker.getEmail(), worker.getName()));
                    message.setSubject("Approval required");
                    message.setSentDate(new Date());
                    message.setContent(getApprovalRequiredMessage(task, partRevision, new Locale(worker.getLanguage())),
                            "text/html; charset=utf-8");
                    message.setFrom();
                    Transport.send(message);
                    LOGGER.info("Sending approval required emails");
                    LOGGER.info("for the part " + partRevision.getLastIteration());
                } catch (UnsupportedEncodingException pUEEx) {
                    LOGGER.warning("Mail address format error.");
                    LOGGER.warning(pUEEx.getMessage());
                }
            }
        } catch (MessagingException pMEx) {
            LOGGER.severe("Message format error.");
            LOGGER.severe("Approval can't be sent.");
            LOGGER.severe(pMEx.getMessage());
        }

    }

    @Asynchronous
    @Override
    public void sendWorkspaceDeletionNotification(Account admin, String workspaceId) {
        try {
            javax.mail.Message message = new MimeMessage(mailSession);
            message.setRecipient(javax.mail.Message.RecipientType.TO,
                    new InternetAddress(admin.getEmail(),admin.getName()));
            message.setSubject("Workspace deletion");
            message.setSentDate(new Date());
            message.setContent(getWorkspaceDeletionMessage(workspaceId,new Locale(admin.getLanguage())),
                    "text/html; charset=utf-8");
            message.setFrom();
            Transport.send(message);
        } catch (UnsupportedEncodingException pUEEx) {
            LOGGER.warning("Mail address format error.");
            LOGGER.warning(pUEEx.getMessage());
        } catch (MessagingException pMEx) {
            LOGGER.severe("Message format error.");
            LOGGER.severe(pMEx.getMessage());
        }
    }

    @Asynchronous
    @Override
    public void sendPartRevisionWorkflowRelaunchedNotification(PartRevision partRevision) {

        Workspace workspace = partRevision.getPartMaster().getWorkspace();
        Account admin = workspace.getAdmin();
        User author = partRevision.getAuthor();

        // Mail both workspace admin and partRevision author
        sendWorkflowRelaunchedNotification(admin.getName(), admin.getEmail(), admin.getLanguage(), workspace.getId(), partRevision);

        if(!admin.getLogin().equals(author.getLogin())){
            sendWorkflowRelaunchedNotification(author.getName(),author.getEmail(), author.getLanguage(), workspace.getId(), partRevision);
        }

    }

    @Asynchronous
    @Override
    public void sendDocumentMasterWorkflowRelaunchedNotification(DocumentMaster documentMaster) {
        Workspace workspace = documentMaster.getWorkspace();
        Account admin = workspace.getAdmin();
        User author = documentMaster.getAuthor();

        // Mail both workspace admin and documentMaster author
        sendWorkflowRelaunchedNotification(admin.getName(), admin.getEmail(), admin.getLanguage(), workspace.getId(), documentMaster);

        if(!admin.getLogin().equals(author.getLogin())){
            sendWorkflowRelaunchedNotification(author.getName(),author.getEmail(), author.getLanguage(), workspace.getId(), documentMaster);
        }
    }

    private void sendWorkflowRelaunchedNotification(String userName, String userEmail, String userLanguage, String workspaceId, PartRevision partRevision){
        try {
            javax.mail.Message message = new MimeMessage(mailSession);
            message.setRecipient(javax.mail.Message.RecipientType.TO,
                    new InternetAddress(userEmail,userName));
            message.setSubject("Workflow Relaunched");
            message.setSentDate(new Date());
            message.setContent(getPartRevisionWorkflowRelaunchedMessage(workspaceId, partRevision.getPartNumber(), partRevision.getVersion(), partRevision.getWorkflow().getLifeCycleState(), new Locale(userLanguage)),
                    "text/html; charset=utf-8");
            message.setFrom();
            Transport.send(message);
        } catch (UnsupportedEncodingException pUEEx) {
            LOGGER.warning("Mail address format error.");
            LOGGER.warning(pUEEx.getMessage());
        } catch (MessagingException pMEx) {
            LOGGER.severe("Message format error.");
            LOGGER.severe("Cannot send workflow relaunched notification.");
            LOGGER.severe(pMEx.getMessage());
        }
    }


    private String getPartRevisionWorkflowRelaunchedMessage(String workspaceId, String number, String version, String lifeCycleState, Locale pLocale) {
        Object[] args = {number+"-"+version,workspaceId,lifeCycleState};
        ResourceBundle bundle = ResourceBundle.getBundle(BASE_NAME, pLocale);
        return MessageFormat.format(bundle.getString("PartRevision_workflow_relaunched_text"), args);
    }


    private void sendWorkflowRelaunchedNotification(String userName, String userEmail,  String userLanguage, String workspaceId, DocumentMaster documentMaster){
        try {
            javax.mail.Message message = new MimeMessage(mailSession);
            message.setRecipient(javax.mail.Message.RecipientType.TO,
                    new InternetAddress(userEmail,userName));
            message.setSubject("Workflow Relaunched");
            message.setSentDate(new Date());
            message.setContent(getDocumentMasterWorkflowRelaunchedMessage(workspaceId, documentMaster.getId(), documentMaster.getVersion() , documentMaster.getWorkflow().getLifeCycleState(), new Locale(userLanguage)),
                    "text/html; charset=utf-8");
            message.setFrom();
            Transport.send(message);
        } catch (UnsupportedEncodingException pUEEx) {
            LOGGER.warning("Mail address format error.");
            LOGGER.warning(pUEEx.getMessage());
        } catch (MessagingException pMEx) {
            LOGGER.severe("Message format error.");
            LOGGER.severe("Cannot send workflow relaunched notification.");
            LOGGER.severe(pMEx.getMessage());
        }
    }

    private String getDocumentMasterWorkflowRelaunchedMessage(String workspaceId, String docMid, String version, String lifeCycleState, Locale pLocale) {
        Object[] args = {docMid+"-"+version,workspaceId,lifeCycleState};
        ResourceBundle bundle = ResourceBundle.getBundle(BASE_NAME, pLocale);
        return MessageFormat.format(bundle.getString("DocumentMaster_workflow_relaunched_text"), args);
    }

    private String getWorkspaceDeletionMessage(String workspaceId, Locale pLocale) {
        Object[] args = {workspaceId};
        ResourceBundle bundle = ResourceBundle.getBundle(BASE_NAME, pLocale);
        return MessageFormat.format(bundle.getString("WorkspaceDeletion_text"), args);
    }

    private String getApprovalRequiredMessage(Task pTask, PartRevision partRevision, Locale pLocale) {
        String voteURL = codebase + "/action/vote";
        String instructions = pTask.getInstructions()==null?"-":pTask.getInstructions();
        Object[] args = {voteURL, partRevision.getWorkspaceId(), pTask.getWorkflowId(), pTask.getActivityStep(), pTask.getNum(), pTask.getTitle(), getURL(partRevision), partRevision, instructions};
        ResourceBundle bundle = ResourceBundle.getBundle(BASE_NAME, pLocale);
        return MessageFormat.format(bundle.getString("Approval_part_text"), args);
    }

    private String getURL(PartRevision partRevision) {
        String workspace = partRevision.getWorkspaceId();
        String docMId = partRevision.getPartNumber();
        return codebase + "/parts/" + workspace + "/" + docMId + "/" + partRevision.getVersion();
    }

    private String getPasswordRecoveryMessage(Account account, String pPasswordRRUuid, Locale pLocale) {
        String recoveryURL = codebase + "/faces/recoveryForm.xhtml?id=" + pPasswordRRUuid;
        Object[] args = {recoveryURL, account.getLogin()};
        ResourceBundle bundle = ResourceBundle.getBundle(BASE_NAME, pLocale);
        return MessageFormat.format(bundle.getString("Recovery_text"), args);
    }

    private String getApprovalRequiredMessage(Task pTask,
            DocumentMaster pDocumentMaster, Locale pLocale) {
        String voteURL = codebase + "/action/vote";
        String instructions = pTask.getInstructions()==null?"-":pTask.getInstructions();
        Object[] args = {voteURL, pDocumentMaster.getWorkspaceId(), pTask.getWorkflowId(), pTask.getActivityStep(), pTask.getNum(), pTask.getTitle(), getURL(pDocumentMaster), pDocumentMaster, instructions};
        ResourceBundle bundle = ResourceBundle.getBundle(BASE_NAME, pLocale);
        return MessageFormat.format(bundle.getString("Approval_document_text"), args);
    }

    private String getIterationNotificationMessage(DocumentMaster pDocumentMaster, Locale pLocale) {

        Object[] args = {
            pDocumentMaster,
            pDocumentMaster.getLastIteration().getCreationDate(),
            new Integer(pDocumentMaster.getLastIteration().getIteration()),
            pDocumentMaster.getLastIteration().getAuthor(), getURL(pDocumentMaster)};
        ResourceBundle bundle = ResourceBundle.getBundle(BASE_NAME, pLocale);
        return MessageFormat.format(bundle.getString("IterationNotification_text"), args);

    }

    private String getStateNotificationMessage(DocumentMaster pDocumentMaster, Locale pLocale) {

        Object[] args = {
            pDocumentMaster,
            pDocumentMaster.getLastIteration().getCreationDate(),
            getURL(pDocumentMaster)};
        ResourceBundle bundle = ResourceBundle.getBundle(BASE_NAME, pLocale);
        return MessageFormat.format(bundle.getString("StateNotification_text"), args);

    }

    private String getURL(DocumentMaster pDocM) {
        String workspace = pDocM.getWorkspaceId();
        String docMId = pDocM.getId();
        return codebase + "/documents/" + workspace + "/" + docMId + "/" + pDocM.getVersion();
    }
}
