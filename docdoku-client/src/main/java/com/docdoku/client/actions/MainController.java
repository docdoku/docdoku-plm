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
package com.docdoku.client.actions;

import com.docdoku.core.services.ApplicationException;
import com.docdoku.core.services.WorkflowModelNotFoundException;
import com.docdoku.core.services.NotAllowedException;
import com.docdoku.core.services.IUploadDownloadWS;
import com.docdoku.core.services.IDocumentManagerWS;
import com.docdoku.core.document.InstanceAttributeTemplate;
import com.docdoku.core.document.DocumentMasterTemplate;
import com.docdoku.core.document.DocumentMasterKey;
import com.docdoku.core.document.TagKey;
import com.docdoku.core.document.Folder;
import com.docdoku.core.meta.InstanceAttribute;
import com.docdoku.core.document.DocumentToDocumentLink;
import com.docdoku.core.security.ACLUserEntry;
import com.docdoku.core.security.ACLUserGroupEntry;
import com.docdoku.core.document.DocumentIterationKey;
import com.docdoku.core.common.BinaryResource;
import com.docdoku.core.document.DocumentIteration;
import com.docdoku.core.document.DocumentMaster;
import com.docdoku.core.workflow.TaskKey;
import com.docdoku.core.common.User;
import com.docdoku.core.workflow.ActivityModel;
import com.docdoku.core.workflow.WorkflowModel;
import com.docdoku.client.data.*;
import com.docdoku.client.localization.I18N;
import com.docdoku.client.ui.common.ProgressMonitorFileDataSource;
import com.docdoku.core.util.NamingConvention;
import com.docdoku.core.util.Tools;
import com.sun.xml.ws.developer.JAXWSProperties;
import java.awt.Component;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.activation.DataHandler;
import javax.swing.*;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.WebServiceException;

public class MainController {

    private IDocumentManagerWS mDocumentService;
    private IUploadDownloadWS mFileService;
    private static MainController sSingleton;

    public static MainController getInstance() {
        if (sSingleton == null) {
            throw new NotInitializedException(MainController.class.getName());
        }

        return sSingleton;
    }

    public static void init(IDocumentManagerWS pProductService, IUploadDownloadWS pFileService) {
        if (sSingleton == null) {
            sSingleton = new MainController(pProductService, pFileService);
        } else {
            throw new AlreadyInitializedException(MainController.class.getName());
        }
    }

    private MainController(IDocumentManagerWS pProductService, IUploadDownloadWS pFileService) {
        mDocumentService = pProductService;
        mFileService = pFileService;
    }

    public DocumentMaster approve(TaskKey pTaskKey, String pComment) throws Exception {
        try {
            System.out.println("Approving task " + pTaskKey);
            DocumentMaster newDocumentMaster;
            String workspaceId = MainModel.getInstance().getWorkspace().getId();
            newDocumentMaster = Tools.resetParentReferences(mDocumentService.approve(workspaceId, pTaskKey, pComment));
            MainModel.getInstance().updater.updateDocM(newDocumentMaster);
            return newDocumentMaster;
        } catch (WebServiceException pWSEx) {
            Throwable t = pWSEx.getCause();
            if (t instanceof Exception) {
                throw (Exception) t;
            } else {
                throw pWSEx;
            }
        }
    }

    public DocumentMaster reject(TaskKey pTaskKey, String pComment) throws Exception {
        try {
            System.out.println("Rejecting task " + pTaskKey);
            DocumentMaster newDocumentMaster;
            String workspaceId = MainModel.getInstance().getWorkspace().getId();
            newDocumentMaster = Tools.resetParentReferences(mDocumentService.reject(workspaceId, pTaskKey, pComment));
            MainModel.getInstance().updater.updateDocM(newDocumentMaster);
            return newDocumentMaster;
        } catch (WebServiceException pWSEx) {
            Throwable t = pWSEx.getCause();
            if (t instanceof Exception) {
                throw (Exception) t;
            } else {
                throw pWSEx;
            }
        }
    }

    public DocumentMaster saveTags(DocumentMaster pDocumentMaster, String[] pTags) throws Exception {
        try {
            System.out.println("Saving tags of document master " + pDocumentMaster);
            DocumentMaster newDocumentMaster;
            newDocumentMaster = Tools.resetParentReferences(mDocumentService.saveTags(pDocumentMaster.getKey(), pTags));
            MainModel.getInstance().updater.updateDocM(newDocumentMaster);
            return newDocumentMaster;
        } catch (WebServiceException pWSEx) {
            Throwable t = pWSEx.getCause();
            if (t instanceof Exception) {
                throw (Exception) t;
            } else {
                throw pWSEx;
            }
        }
    }

    public DocumentMaster checkIn(DocumentMaster pDocumentMaster) throws Exception {
        try {
            System.out.println("Checking In document master " + pDocumentMaster);
            DocumentMaster newDocumentMaster;
            newDocumentMaster = Tools.resetParentReferences(mDocumentService.checkIn(pDocumentMaster.getKey()));
            MainModel.getInstance().updater.checkIn(newDocumentMaster);
            return newDocumentMaster;
        } catch (WebServiceException pWSEx) {
            Throwable t = pWSEx.getCause();
            if (t instanceof Exception) {
                throw (Exception) t;
            } else {
                throw pWSEx;
            }
        }
    }

    public DocumentMaster checkOut(DocumentMaster pDocumentMaster) throws Exception {
        try {
            System.out.println("Checking Out document master " + pDocumentMaster);
            DocumentMaster newDocumentMaster;
            newDocumentMaster = Tools.resetParentReferences(mDocumentService.checkOut(pDocumentMaster.getKey()));
            MainModel.getInstance().updater.checkOut(newDocumentMaster);
            return newDocumentMaster;
        } catch (WebServiceException pWSEx) {
            Throwable t = pWSEx.getCause();
            if (t instanceof Exception) {
                throw (Exception) t;
            } else {
                throw pWSEx;
            }
        }
    }

    public DocumentMaster undoCheckOut(DocumentMaster pDocumentMaster) throws Exception {
        try {
            System.out.println("Undoing Check Out document master " + pDocumentMaster);
            DocumentMaster newDocumentMaster;
            newDocumentMaster = Tools.resetParentReferences(mDocumentService.undoCheckOut(pDocumentMaster.getKey()));
            MainModel.getInstance().updater.undoCheckOut(newDocumentMaster);
            return newDocumentMaster;
        } catch (WebServiceException pWSEx) {
            Throwable t = pWSEx.getCause();
            if (t instanceof Exception) {
                throw (Exception) t;
            } else {
                throw pWSEx;
            }
        }
    }

    public void delDocM(DocumentMaster pDocumentMaster) throws Exception {
        try {
            System.out.println("Deleting document master " + pDocumentMaster);
            mDocumentService.deleteDocumentMaster(pDocumentMaster.getKey());
            MainModel.getInstance().updater.delDocM(pDocumentMaster);
        } catch (WebServiceException pWSEx) {
            Throwable t = pWSEx.getCause();
            if (t instanceof Exception) {
                throw (Exception) t;
            } else {
                throw pWSEx;
            }
        }
    }

    public void delDocMTemplate(DocumentMasterTemplate pDocMTemplate) throws Exception {
        try {
            System.out.println("Deleting document master template " + pDocMTemplate);
            mDocumentService.deleteDocumentMasterTemplate(pDocMTemplate.getKey());
            MainModel.getInstance().updater.delDocMTemplate(pDocMTemplate);
        } catch (WebServiceException pWSEx) {
            Throwable t = pWSEx.getCause();
            if (t instanceof Exception) {
                throw (Exception) t;
            } else {
                throw pWSEx;
            }
        }
    }

    public DocumentMaster moveDocM(String pParentFolder, DocumentMaster pDocumentMaster) throws Exception {
        try {
            System.out.println("Moving document master " + pDocumentMaster);
            DocumentMaster newDocumentMaster;
            newDocumentMaster = Tools.resetParentReferences(mDocumentService.moveDocumentMaster(pParentFolder, pDocumentMaster.getKey()));
            MainModel.getInstance().updater.moveDocM(newDocumentMaster);
            return newDocumentMaster;
        } catch (WebServiceException pWSEx) {
            Throwable t = pWSEx.getCause();
            if (t instanceof Exception) {
                throw (Exception) t;
            } else {
                throw pWSEx;
            }
        }
    }

    public void renameFolder(String pCompletePath, String pNewName) throws Exception {
        try {
            System.out.println("Renaming folder " + pCompletePath);
            Folder folder = new Folder(pCompletePath);
            mDocumentService.moveFolder(pCompletePath, folder.getParentFolder().getCompletePath(), pNewName);
            MainModel.getInstance().updater.renameFolder(pCompletePath, pNewName);
        } catch (WebServiceException pWSEx) {
            Throwable t = pWSEx.getCause();
            if (t instanceof Exception) {
                throw (Exception) t;
            } else {
                throw pWSEx;
            }
        }
    }
    
    public DocumentMasterKey[] delFolder(String pFolder) throws Exception {
        try {
            System.out.println("Deleting folder " + pFolder);
            DocumentMasterKey[] pks = mDocumentService.deleteFolder(pFolder);
            MainModel.getInstance().updater.delFolder(pFolder);
            return pks;
        } catch (WebServiceException pWSEx) {
            Throwable t = pWSEx.getCause();
            if (t instanceof Exception) {
                throw (Exception) t;
            } else {
                throw pWSEx;
            }
        }
    }

    public void saveFile(Component pParent, DocumentIteration pDocument, File pLocalFile) throws Exception {
        if (!NamingConvention.correct(pLocalFile.getName())) {
            throw new NotAllowedException(Locale.getDefault(), "NotAllowedException9");
        }
        MainModel model = MainModel.getInstance();
        String message = I18N.BUNDLE.getString("UploadMsg_part1") + " " + pLocalFile.getName() + " (" + (int) (pLocalFile.length() / 1024) + I18N.BUNDLE.getString("UploadMsg_part2");
        DataHandler data = new DataHandler(new ProgressMonitorFileDataSource(pParent, pLocalFile, message));
        try {
            Map<String, Object> ctxt = ((BindingProvider) mFileService).getRequestContext();
            try {
                if (ctxt.containsKey(JAXWSProperties.HTTP_CLIENT_STREAMING_CHUNK_SIZE)) {
                    mFileService.uploadToDocument(model.getWorkspace().getId(), pDocument.getDocumentMasterId(), pDocument.getDocumentMasterVersion(), pDocument.getIteration(), pLocalFile.getName(), data);
                } else {
                    //workaround mode
                    uploadFileWithServlet(pParent, pLocalFile, getServletURL(pDocument, pLocalFile));
                }

            } catch (Exception ex) {
                Throwable currentEx=ex;
                while(currentEx!=null){
                    if(currentEx instanceof InterruptedIOException)
                        throw ex;
                    if(currentEx instanceof ApplicationException)
                        throw ex;
                        
                    currentEx=currentEx.getCause();
                }
                
                //error encountered, try again, workaround mode
                if (ctxt.containsKey(JAXWSProperties.HTTP_CLIENT_STREAMING_CHUNK_SIZE)) {
                    System.out.println("Disabling chunked mode");
                    ctxt.remove(JAXWSProperties.HTTP_CLIENT_STREAMING_CHUNK_SIZE);
                    uploadFileWithServlet(pParent, pLocalFile, getServletURL(pDocument, pLocalFile));
                } else {
                    //we were already not using the chunked mode
                    //there's not much to do...
                    throw ex;
                }
            }
        } catch (WebServiceException pWSEx) {
            Throwable t = pWSEx.getCause();
            if (t instanceof Exception) {
                throw (Exception) t;
            } else {
                throw pWSEx;
            }
        }

    }

    private String getServletURL(DocumentIteration pDoc, File pLocalFile) throws UnsupportedEncodingException {
        MainModel model = MainModel.getInstance();
        return Config.getHTTPCodebase() + "files/" + URLEncoder.encode(model.getWorkspace().getId(), "UTF-8") + "/" + "documents/" + URLEncoder.encode(pDoc.getDocumentMasterId(), "UTF-8") + "/" + pDoc.getDocumentMasterVersion() + "/" + pDoc.getIteration() + "/" + URLEncoder.encode(pLocalFile.getName(), "UTF-8");
    }

    public void saveFile(Component pParent, DocumentMasterTemplate pTemplate, File pLocalFile) throws Exception {
        if (!NamingConvention.correct(pLocalFile.getName())) {
            throw new NotAllowedException(Locale.getDefault(), "NotAllowedException9");
        }
        MainModel model = MainModel.getInstance();
        String message = I18N.BUNDLE.getString("UploadMsg_part1") + " " + pLocalFile.getName() + " (" + (int) (pLocalFile.length() / 1024) + I18N.BUNDLE.getString("UploadMsg_part2");
        DataHandler data = new DataHandler(new ProgressMonitorFileDataSource(pParent, pLocalFile, message));
        try {
            Map<String, Object> ctxt = ((BindingProvider) mFileService).getRequestContext();
            try {
                if (ctxt.containsKey(JAXWSProperties.HTTP_CLIENT_STREAMING_CHUNK_SIZE)) {
                    mFileService.uploadToTemplate(model.getWorkspace().getId(), pTemplate.getId(), pLocalFile.getName(), data);
                } else {
                    //workaround mode
                    uploadFileWithServlet(pParent, pLocalFile, getServletURL(pTemplate, pLocalFile));
                }
            } catch (Exception ex) {
                if (ex.getCause() instanceof InterruptedIOException) {
                    throw ex;
                }
                //error encountered, try again, workaround mode

                if (ctxt.containsKey(JAXWSProperties.HTTP_CLIENT_STREAMING_CHUNK_SIZE)) {
                    System.out.println("Disabling chunked mode");
                    ctxt.remove(JAXWSProperties.HTTP_CLIENT_STREAMING_CHUNK_SIZE);
                    uploadFileWithServlet(pParent, pLocalFile, getServletURL(pTemplate, pLocalFile));
                } else {
                    //we were already not using the chunked mode
                    //there's not much to do...
                    throw ex;
                }
            }
        } catch (WebServiceException pWSEx) {
            Throwable t = pWSEx.getCause();
            if (t instanceof Exception) {
                throw (Exception) t;
            } else {
                throw pWSEx;
            }
        }
    }

    private String getServletURL(DocumentMasterTemplate pTemplate, File pLocalFile) throws UnsupportedEncodingException {
        MainModel model = MainModel.getInstance();
        return Config.getHTTPCodebase() + "files/" + URLEncoder.encode(model.getWorkspace().getId(), "UTF-8") + "/" + "templates/" + URLEncoder.encode(pTemplate.getId(), "UTF-8") + "/" + URLEncoder.encode(pLocalFile.getName(), "UTF-8");
    }

    private void uploadFileWithServlet(final Component pParent, File pLocalFile, String pURL) throws IOException {
        System.out.println("Uploading file with servlet");
        InputStream in = null;
        OutputStream out = null;
        HttpURLConnection conn = null;
        try {
            //Hack for NTLM proxy
            //perform a head method to negociate the NTLM proxy authentication
            performHeadHTTPMethod(pURL);

            MainModel model = MainModel.getInstance();
            URL url = new URL(pURL);
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setAllowUserInteraction(true);
            conn.setRequestProperty("Connection", "Keep-Alive");
            byte[] encoded = org.apache.commons.codec.binary.Base64.encodeBase64((model.getLogin() + ":" + model.getPassword()).getBytes("ISO-8859-1"));
            conn.setRequestProperty("Authorization", "Basic " + new String(encoded, "US-ASCII"));

            String lineEnd = "\r\n";
            String twoHyphens = "--";
            String boundary = "--------------------" + Long.toString(System.currentTimeMillis(), 16);
            byte[] header = (twoHyphens + boundary + lineEnd + "Content-Disposition: form-data; name=\"upload\";" + " filename=\"" + pLocalFile + "\"" + lineEnd + lineEnd).getBytes("ISO-8859-1");
            byte[] footer = (lineEnd + twoHyphens + boundary + twoHyphens + lineEnd).getBytes("ISO-8859-1");

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
            //conn.setRequestProperty("Content-Length",len + "");
            long len = header.length + pLocalFile.length() + footer.length;
            conn.setFixedLengthStreamingMode((int) len);
            out = new BufferedOutputStream(conn.getOutputStream(), Config.BUFFER_CAPACITY);
            out.write(header);

            byte[] data = new byte[Config.CHUNK_SIZE];
            int length;
            in = new ProgressMonitorInputStream(pParent,
                    I18N.BUNDLE.getString("UploadMsg_part1") + " " + pLocalFile.getName() + " (" + (int) (pLocalFile.length() / 1024) + I18N.BUNDLE.getString("UploadMsg_part2"), new BufferedInputStream(new FileInputStream(pLocalFile), Config.BUFFER_CAPACITY));
            while ((length = in.read(data)) != -1) {
                out.write(data, 0, length);
            }

            out.write(footer);
            out.flush();

            int code = conn.getResponseCode();
            System.out.println("Upload HTTP response code: " + code);
            if (code != 200) {
                //TODO create a more suitable exception
                throw new IOException();
            }
            out.close();
        } catch (InterruptedIOException pEx) {
            throw pEx;
        } catch (IOException pEx) {
            out.close();
            throw pEx;
        } finally {
            in.close();
            conn.disconnect();
        }
    }

    private void performHeadHTTPMethod(String pURL) throws MalformedURLException, IOException {
        MainModel model = MainModel.getInstance();
        URL url = new URL(pURL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setUseCaches(false);
        conn.setAllowUserInteraction(true);
        conn.setRequestProperty("Connection", "Keep-Alive");
        byte[] encoded = org.apache.commons.codec.binary.Base64.encodeBase64((model.getLogin() + ":" + model.getPassword()).getBytes("ISO-8859-1"));
        conn.setRequestProperty("Authorization", "Basic " + new String(encoded, "US-ASCII"));
        conn.setRequestMethod("HEAD");
        conn.connect();
        int code = conn.getResponseCode();
        System.out.println("Head HTTP response code: " + code);
    }

    public void createFolder(String pParentFolder, String pFolder) throws Exception {
        try {
            System.out.println("Creating folder " + pFolder + " in " + pParentFolder);
            mDocumentService.createFolder(pParentFolder, pFolder);
            MainModel.getInstance().updater.createFolder(pParentFolder, pFolder);
        } catch (WebServiceException pWSEx) {
            Throwable t = pWSEx.getCause();
            if (t instanceof Exception) {
                throw (Exception) t;
            } else {
                throw pWSEx;
            }
        }
    }

    public DocumentMaster createDocM(String pParentFolder,
            String pDocMId,
            String pTitle,
            String pDescription,
            DocumentMasterTemplate pTemplate,
            WorkflowModel pWorkflowModel) throws Exception {
        try {
            System.out.println("Saving document master " + pDocMId + " version " + "A" + " with title " + pTitle + ", template " + pTemplate + " and description " + pDescription + " in " + pParentFolder);
            DocumentMaster newDocumentMaster;
            //TODO ACL
            ACLUserEntry[] userEntries = null;
            ACLUserGroupEntry[] groupEntries = null;

            newDocumentMaster = Tools.resetParentReferences(mDocumentService.createDocumentMaster(pParentFolder, pDocMId, pTitle, pDescription, pTemplate == null ? null : pTemplate.getId(), pWorkflowModel == null ? null : pWorkflowModel.getId(), userEntries, groupEntries));
            MainModel.getInstance().updater.createDocMInFolder(newDocumentMaster);
            return newDocumentMaster;
        } catch (WebServiceException pWSEx) {
            Throwable t = pWSEx.getCause();
            if (t instanceof Exception) {
                throw (Exception) t;
            } else {
                throw pWSEx;
            }
        }
    }

    public DocumentMasterTemplate createDocMTemplate(String pId, String pDocumentType,
            String pMask, Set<InstanceAttributeTemplate> pAttributeTemplates, boolean pIdGenerated) throws Exception {
        try {
            System.out.println("Saving document master template " + pId + " with mask " + pMask);
            String workspaceId = MainModel.getInstance().getWorkspace().getId();
            DocumentMasterTemplate newTemplate;
            newTemplate = Tools.resetParentReferences(mDocumentService.createDocumentMasterTemplate(workspaceId, pId, pDocumentType, pMask, pAttributeTemplates.toArray(new InstanceAttributeTemplate[pAttributeTemplates.size()]), pIdGenerated));
            MainModel.getInstance().updater.createDocMTemplate(newTemplate);
            return newTemplate;
        } catch (WebServiceException pWSEx) {
            Throwable t = pWSEx.getCause();
            if (t instanceof Exception) {
                throw (Exception) t;
            } else {
                throw pWSEx;
            }
        }
    }

    public DocumentMasterTemplate updateDocMTemplate(DocumentMasterTemplate pTemplate, String pDocumentType, String pMask, Set<InstanceAttributeTemplate> pAttributeTemplates, boolean pIdGenerated) throws Exception {
        try {
            System.out.println("Saving document master template " + pTemplate);
            DocumentMasterTemplate newTemplate;
            newTemplate = Tools.resetParentReferences(mDocumentService.updateDocumentMasterTemplate(pTemplate.getKey(), pDocumentType, pMask, pAttributeTemplates.toArray(new InstanceAttributeTemplate[pAttributeTemplates.size()]), pIdGenerated));
            MainModel.getInstance().updater.updateDocMTemplate(newTemplate);
            return newTemplate;
        } catch (WebServiceException pWSEx) {
            Throwable t = pWSEx.getCause();
            if (t instanceof Exception) {
                throw (Exception) t;
            } else {
                throw pWSEx;
            }
        }
    }

    public DocumentMaster[] createVersion(DocumentMaster pDocumentMaster,
            String pTitle,
            String pDescription,
            WorkflowModel pWorkflowModel) throws Exception {
        try {
            System.out.println("Creating new version to document master " + pDocumentMaster + " with title " + pTitle + " and description " + pDescription);
            //TODO ACL
            ACLUserEntry[] userEntries = null;
            ACLUserGroupEntry[] groupEntries = null;

            DocumentMaster[] originalAndNewDocM = Tools.resetParentReferences(mDocumentService.createVersion(pDocumentMaster.getKey(), pTitle, pDescription, pWorkflowModel == null ? null : pWorkflowModel.getId(), userEntries, groupEntries));
            MainModel.getInstance().updater.makeNewVersion(originalAndNewDocM);
            return originalAndNewDocM;
        } catch (WebServiceException pWSEx) {
            Throwable t = pWSEx.getCause();
            if (t instanceof Exception) {
                throw (Exception) t;
            } else {
                throw pWSEx;
            }
        }
    }

    public User savePersonalInfo(String pName, String pEmail, String pLanguage) throws Exception {
        try {
            System.out.println("Saving personnal informations");
            String workspaceId = MainModel.getInstance().getWorkspace().getId();
            User user = mDocumentService.savePersonalInfo(workspaceId, pName, pEmail, pLanguage);
            MainModel.getInstance().updater.updateUser(user);
            return user;
        } catch (WebServiceException pWSEx) {
            Throwable t = pWSEx.getCause();
            if (t instanceof Exception) {
                throw (Exception) t;
            } else {
                throw pWSEx;
            }
        }
    }

    public void subscribeStateNotification(DocumentMaster pDocumentMaster) throws Exception {
        try {
            System.out.println("Subscribe state notification on document master " + pDocumentMaster);
            mDocumentService.subscribeToStateChangeEvent(pDocumentMaster.getKey());
            MainModel.getInstance().updater.addStateNotification(pDocumentMaster);
        } catch (WebServiceException pWSEx) {
            Throwable t = pWSEx.getCause();
            if (t instanceof Exception) {
                throw (Exception) t;
            } else {
                throw pWSEx;
            }
        }
    }

    public void unsubscribeStateNotification(DocumentMaster pDocumentMaster) throws Exception {
        try {
            System.out.println("Unsubscribe state notification on document master " + pDocumentMaster);
            mDocumentService.unsubscribeToStateChangeEvent(pDocumentMaster.getKey());
            MainModel.getInstance().updater.removeStateNotification(pDocumentMaster);
        } catch (WebServiceException pWSEx) {
            Throwable t = pWSEx.getCause();
            if (t instanceof Exception) {
                throw (Exception) t;
            } else {
                throw pWSEx;
            }
        }
    }

    public void subscribeIterationNotification(DocumentMaster pDocumentMaster) throws Exception {
        try {
            System.out.println("Subscribe iteration notification on document master " + pDocumentMaster);
            mDocumentService.subscribeToIterationChangeEvent(pDocumentMaster.getKey());
            MainModel.getInstance().updater.addIterationNotification(pDocumentMaster);
        } catch (WebServiceException pWSEx) {
            Throwable t = pWSEx.getCause();
            if (t instanceof Exception) {
                throw (Exception) t;
            } else {
                throw pWSEx;
            }
        }
    }

    public void unsubscribeIterationNotification(DocumentMaster pDocumentMaster) throws Exception {
        try {
            System.out.println("Unsubscribe iteration notification on document master " + pDocumentMaster);
            mDocumentService.unsubscribeToIterationChangeEvent(pDocumentMaster.getKey());
            MainModel.getInstance().updater.removeIterationNotification(pDocumentMaster);
        } catch (WebServiceException pWSEx) {
            Throwable t = pWSEx.getCause();
            if (t instanceof Exception) {
                throw (Exception) t;
            } else {
                throw pWSEx;
            }
        }
    }

    public void delWorkflowModel(WorkflowModel pWorkflowModel) throws Exception {
        try {
            System.out.println("Deleting workflow model " + pWorkflowModel);
            mDocumentService.deleteWorkflowModel(pWorkflowModel.getKey());
            MainModel.getInstance().updater.delWorkflowModel(pWorkflowModel);
        } catch (WebServiceException pWSEx) {
            Throwable t = pWSEx.getCause();
            if (t instanceof Exception) {
                throw (Exception) t;
            } else {
                throw pWSEx;
            }
        }
    }

    public void delTag(String pTag) throws Exception {
        try {
            System.out.println("Deleting tag " + pTag);
            String workspaceId = MainModel.getInstance().getWorkspace().getId();
            mDocumentService.deleteTag(new TagKey(workspaceId, pTag));
            MainModel.getInstance().updater.delTag(pTag);
        } catch (WebServiceException pWSEx) {
            Throwable t = pWSEx.getCause();
            if (t instanceof Exception) {
                throw (Exception) t;
            } else {
                throw pWSEx;
            }
        }
    }

    public WorkflowModel saveWorkflowModel(WorkflowModel pWorkflowModel) throws Exception {
        try {
            System.out.println("Saving workflow model " + pWorkflowModel);
            WorkflowModel model;

            //TODO remove and create in the same tx
            try {
                mDocumentService.deleteWorkflowModel(pWorkflowModel.getKey());
            } catch (WorkflowModelNotFoundException pWNFEx) {
            }
            ActivityModel[] activityModels = pWorkflowModel.getActivityModels().toArray(new ActivityModel[pWorkflowModel.getActivityModels().size()]);
            model = Tools.resetParentReferences(mDocumentService.createWorkflowModel(pWorkflowModel.getWorkspaceId(), pWorkflowModel.getId(), pWorkflowModel.getFinalLifeCycleState(), activityModels));
            MainModel.getInstance().updater.saveWorkflowModel(model);
            return model;
        } catch (WebServiceException pWSEx) {
            Throwable t = pWSEx.getCause();
            if (t instanceof Exception) {
                throw (Exception) t;
            } else {
                throw pWSEx;
            }
        }
    }

    public void removeFileFromDocument(BinaryResource pFile) throws Exception {
        try {
            System.out.println("Removing file " + pFile + " from document");
            DocumentMaster newDocM;
            newDocM = Tools.resetParentReferences(mDocumentService.removeFileFromDocument(pFile.getFullName()));
            MainModel.getInstance().updater.updateDocM(newDocM);
        } catch (WebServiceException pWSEx) {
            Throwable t = pWSEx.getCause();
            if (t instanceof Exception) {
                throw (Exception) t;
            } else {
                throw pWSEx;
            }
        }
    }

    public void removeFileFromTemplate(BinaryResource pFile) throws Exception {
        try {
            System.out.println("Removing file " + pFile + " from document template");
            DocumentMasterTemplate newTemplate;
            newTemplate = Tools.resetParentReferences(mDocumentService.removeFileFromTemplate(pFile.getFullName()));
            MainModel.getInstance().updater.updateDocMTemplate(newTemplate);
        } catch (WebServiceException pWSEx) {
            Throwable t = pWSEx.getCause();
            if (t instanceof Exception) {
                throw (Exception) t;
            } else {
                throw pWSEx;
            }
        }
    }

    public DocumentIteration updateDoc(DocumentIteration pDocument, String pComment, InstanceAttribute[] pAttributes, DocumentToDocumentLink[] pLinks) throws Exception {
        try {
            System.out.println("Updating document " + pDocument);
            DocumentMaster newDocM;
            DocumentIterationKey docKey = pDocument.getKey();

            DocumentIterationKey[] linkKeys = new DocumentIterationKey[pLinks.length];
            for (int i = 0; i < pLinks.length; i++) {
                linkKeys[i] = pLinks[i].getToDocumentKey();
            }

            newDocM = Tools.resetParentReferences(mDocumentService.updateDocument(docKey, pComment, pAttributes, linkKeys));
            MainModel.getInstance().updater.updateDocM(newDocM);
            return newDocM.getIteration(docKey.getIteration());
        } catch (WebServiceException pWSEx) {
            Throwable t = pWSEx.getCause();
            if (t instanceof Exception) {
                throw (Exception) t;
            } else {
                throw pWSEx;
            }
        }
    }

    public void login(String login, String password, String workspaceId) throws Exception {
        try {
            ((BindingProvider) mDocumentService).getRequestContext().put(BindingProvider.USERNAME_PROPERTY, login);
            ((BindingProvider) mDocumentService).getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, password);
            ((BindingProvider) mFileService).getRequestContext().put(BindingProvider.USERNAME_PROPERTY, login);
            ((BindingProvider) mFileService).getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, password);
            mDocumentService.whoAmI(workspaceId);
            MainModel.init(login, password, workspaceId, mDocumentService, mFileService);
        } catch (WebServiceException pWSEx) {
            Throwable t = pWSEx.getCause();
            if (t instanceof Exception) {
                throw (Exception) t;
            } else {
                throw pWSEx;
            }
        }
    }
}
