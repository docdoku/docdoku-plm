/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2013 DocDoku SARL
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

import com.docdoku.core.common.Account;
import com.docdoku.core.document.DocumentMaster;
import com.docdoku.core.workflow.Task;
import com.docdoku.core.common.User;
import com.docdoku.core.services.IMailerLocal;
import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.ejb.Asynchronous;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

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

    private String getPasswordRecoveryMessage(Account account, String pPasswordRRUuid, Locale pLocale) {
        String recoveryURL = codebase + "/faces/recoveryForm.xhtml?id=" + pPasswordRRUuid;
        Object[] args = {recoveryURL, account.getLogin()};
        ResourceBundle bundle = ResourceBundle.getBundle(BASE_NAME, pLocale);
        return MessageFormat.format(bundle.getString("Recovery_text"), args);
    }

    private String getApprovalRequiredMessage(Task pTask,
            DocumentMaster pDocumentMaster, Locale pLocale) {
        String voteURL = codebase + "/action/vote";
        Object[] args = {voteURL, pDocumentMaster.getWorkspaceId(), pTask.getWorkflowId(), pTask.getActivityStep(), pTask.getNum(), pTask.getTitle(), getURL(pDocumentMaster), pDocumentMaster, pTask.getInstructions()};
        ResourceBundle bundle = ResourceBundle.getBundle(BASE_NAME, pLocale);
        return MessageFormat.format(bundle.getString("Approval_text"), args);
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
