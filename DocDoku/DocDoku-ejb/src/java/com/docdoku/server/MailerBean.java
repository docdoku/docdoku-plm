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
import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJBException;
import javax.ejb.MessageDriven;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * Entity class MailerBean
 *
 * @author Florent.Garin
 */
@MessageDriven(name = "MailerBean", activationConfig =  {
    @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge"),
    @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class MailerBean implements MessageListener {
    
    private final static String BASE_NAME = "com.docdoku.server.templates.MailText";
    
    @Resource(name="mail/docdokuSMTP")
    private Session mailSession;
    
    @Resource(name="codebase")
    private String codebase;
    
    
    public void onMessage(Message message) {     
        try {
            ObjectMessage objMsg = (ObjectMessage)message;
            String messageType = objMsg.getStringProperty("messageType");
            Object[] obj = (Object[])objMsg.getObject();
            if(messageType.equals("StateNotification")){
                sendStateNotification((User[])obj[0],(MasterDocument)obj[1]);
            }else if(messageType.equals("Approval")){
                sendApproval((Collection<Task>)obj[0], (MasterDocument)obj[1]);
            }else if(messageType.equals("IterationNotification")){
                sendIterationNotification((User[])obj[0],(MasterDocument)obj[1]);
            }
        } catch (JMSException ex) {
            throw new EJBException(ex);
        }
        
    }
    
    
    private void sendStateNotification(User[] pSubscribers,
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
                    System.out.println("Sending state notification emails");
                    System.out.println("for the document " + pMasterDocument.getLastIteration());
                    
                } catch (UnsupportedEncodingException pUEEx) {
                    System.err.println("Mail address format error.");
                    System.err.println(pUEEx.getMessage());
                }
            }
        } catch (MessagingException pMEx) {
            System.err.println("Message format error.");
            System.err.println("Notifications can't be sent.");
            System.err.println(pMEx.getMessage());
        }
    }
    
    private void sendIterationNotification(User[] pSubscribers,
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
                    System.out.println("Sending iteration notification emails");
                    System.out.println("for the document " + pMasterDocument.getLastIteration());
                    
                } catch (UnsupportedEncodingException pUEEx) {
                    System.err.println("Mail address format error.");
                    System.err.println(pUEEx.getMessage());
                }
            }
        } catch (MessagingException pMEx) {
            System.err.println("Message format error.");
            System.err.println("Notifications can't be sent.");
            System.err.println(pMEx.getMessage());
        }
    }
    
    private void sendApproval(Collection<Task> pRunningTasks,
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
                    System.out.println("Sending approval required emails");
                    System.out.println("for the document " + pMasterDocument.getLastIteration());
                } catch (UnsupportedEncodingException pUEEx) {
                    System.err.println("Mail address format error.");
                    System.err.println(pUEEx.getMessage());
                }
            }
        }catch (MessagingException pMEx) {
            System.err.println("Message format error.");
            System.err.println("Approval can't be sent.");
            System.err.println(pMEx.getMessage());
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
