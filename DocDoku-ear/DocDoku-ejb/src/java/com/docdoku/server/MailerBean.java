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

import com.docdoku.core.entities.MasterDocument;
import com.docdoku.core.entities.Task;
import com.docdoku.core.entities.User;
import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * Session class MailerBean
 *
 * @author Florent.Garin
 */
@Stateless(name="MailerBean")
public class MailerBean {
    
    private final static String BASE_NAME = "com.docdoku.server.templates.MailText";
    
    @Resource(name="mail/docdokuSMTP")
    private Session mailSession;
    
    @Resource(name="codebase")
    private String codebase;
    
    private final static Logger LOGGER = Logger.getLogger(MailerBean.class.getName());
    
    @Asynchronous
    public void sendStateNotification(User[] pSubscribers,
            MasterDocument pMasterDocument) {
        try {
            javax.mail.Message message = new MimeMessage(mailSession);
            
            for (int i = 0; i < pSubscribers.length; i++) {
                try {
                    message.addRecipient(javax.mail.Message.RecipientType.TO,
                            new InternetAddress(pSubscribers[i].getEmail(),
                            pSubscribers[i].getName()));
                    message.setSubject("State notification");
                    message.setSentDate(new Date());
                    message.setContent(getStateNotificationMessage(pMasterDocument, new Locale(pSubscribers[i].getLanguage())),
                            "text/html; charset=utf-8");
                    message.setFrom();
                    Transport.send(message);
                    LOGGER.info("Sending state notification emails");
                    LOGGER.info("for the document " + pMasterDocument.getLastIteration());
                    
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
    public void sendIterationNotification(User[] pSubscribers,
            MasterDocument pMasterDocument) {
        try{
            for (int i = 0; i < pSubscribers.length; i++) {
                try {
                    javax.mail.Message message = new MimeMessage(mailSession);
                    message.addRecipient(javax.mail.Message.RecipientType.TO,
                            new InternetAddress(pSubscribers[i].getEmail(),
                            pSubscribers[i].getName()));
                    message.setSubject("Iteration notification");
                    message.setSentDate(new Date());
                    message.setContent(getIterationNotificationMessage(pMasterDocument, new Locale(pSubscribers[i].getLanguage())),
                            "text/html; charset=utf-8");
                    message.setFrom();
                    Transport.send(message);
                    LOGGER.info("Sending iteration notification emails");
                    LOGGER.info("for the document " + pMasterDocument.getLastIteration());
                    
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
    public void sendApproval(Collection<Task> pRunningTasks,
            MasterDocument pMasterDocument) {
        try{
            for (Task task:pRunningTasks){
                try {
                    javax.mail.Message message = new MimeMessage(mailSession);
                    User worker = task.getWorker();
                    message.setRecipient(javax.mail.Message.RecipientType.TO,
                            new InternetAddress(worker.getEmail(), worker.getName()));
                    message.setSubject("Approval required");
                    message.setSentDate(new Date());
                    message.setContent(getApprovalRequiredMessage(task, pMasterDocument, new Locale(worker.getLanguage())),
                            "text/html; charset=utf-8");
                    message.setFrom();
                    Transport.send(message);
                    LOGGER.info("Sending approval required emails");
                    LOGGER.info("for the document " + pMasterDocument.getLastIteration());
                } catch (UnsupportedEncodingException pUEEx) {
                    LOGGER.warning("Mail address format error.");
                    LOGGER.warning(pUEEx.getMessage());
                }
            }
        }catch (MessagingException pMEx) {
            LOGGER.severe("Message format error.");
            LOGGER.severe("Approval can't be sent.");
            LOGGER.severe(pMEx.getMessage());
        }
    }
    
    private String getApprovalRequiredMessage(Task pTask,
            MasterDocument pMasterDocument, Locale pLocale) {
        String voteURL = codebase + "/action/vote";
        Object[] args =
        {voteURL, pMasterDocument.getWorkspaceId(), pTask.getWorkflowId(), pTask.getActivityStep(),pTask.getNum(), pTask.getTitle(), getURL(pMasterDocument),pMasterDocument,pTask.getInstructions()};
        ResourceBundle bundle = ResourceBundle.getBundle(BASE_NAME, pLocale);
        return MessageFormat.format(bundle.getString("Approval_text"), args);
    }
    
    private String getIterationNotificationMessage(MasterDocument pMasterDocument, Locale pLocale){
        
        Object[] args =
        {
            pMasterDocument,
            pMasterDocument.getLastIteration().getCreationDate(),
            new Integer(pMasterDocument.getLastIteration().getIteration()),
            pMasterDocument.getLastIteration().getAuthor(), getURL(pMasterDocument)};
        ResourceBundle bundle = ResourceBundle.getBundle(BASE_NAME, pLocale);
        return MessageFormat.format(bundle.getString("IterationNotification_text"), args);
        
    }
    
    private String getStateNotificationMessage(MasterDocument pMasterDocument, Locale pLocale){
        
        Object[] args =
        {
            pMasterDocument,
            pMasterDocument.getLastIteration().getCreationDate(),
            getURL(pMasterDocument)};
        ResourceBundle bundle = ResourceBundle.getBundle(BASE_NAME, pLocale);
        return MessageFormat.format(bundle.getString("StateNotification_text"), args);
        
    }
    
    private String getURL(MasterDocument pMDoc) {
        String workspace=pMDoc.getWorkspaceId();
        String mdocID=pMDoc.getId();
        return codebase + "/documents/" + workspace + "/" + mdocID + "/" + pMDoc.getVersion();
    }
}
