package com.docdoku.client.actions;

import com.docdoku.client.data.*;
import com.docdoku.client.localization.I18N;
import com.docdoku.client.ui.common.ProgressMonitorFileDataSource;
import com.docdoku.core.*;
import com.docdoku.core.entities.*;
import com.docdoku.core.entities.keys.*;
import com.docdoku.core.util.Tools;
import com.sun.xml.ws.developer.JAXWSProperties;
import java.awt.Component;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Set;
import javax.activation.DataHandler;
import javax.swing.*;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.WebServiceException;

public class MainController {

    private ICommandWS mCommandService;
    private com.docdoku.client.ws.proxies.uploaddownloadservice.UploadDownload mFileService;
    private static MainController sSingleton;

    public static MainController getInstance() {
        if (sSingleton == null) {
            throw new NotInitializedException(MainController.class.getName());
        }

        return sSingleton;
    }

    public static void init(ICommandWS pCommandService, com.docdoku.client.ws.proxies.uploaddownloadservice.UploadDownload pFileService) {
        if (sSingleton == null) {
            sSingleton = new MainController(pCommandService, pFileService);
        } else {
            throw new AlreadyInitializedException(MainController.class.getName());
        }
    }

    private MainController(ICommandWS pCommandService, com.docdoku.client.ws.proxies.uploaddownloadservice.UploadDownload pFileService) {
        mCommandService = pCommandService;
        mFileService = pFileService;
    }

    public MasterDocument approve(TaskKey pTaskKey, String pComment) throws Exception {
        try {
            System.out.println("Approving task " + pTaskKey);
            MasterDocument newMasterDocument;
            String workspaceId = MainModel.getInstance().getWorkspace().getId();
            newMasterDocument = Tools.resetParentReferences(mCommandService.approve(workspaceId, pTaskKey, pComment));
            MainModel.getInstance().updater.updateMDoc(newMasterDocument);
            return newMasterDocument;
        } catch (WebServiceException pWSEx) {
            Throwable t = pWSEx.getCause();
            if (t instanceof Exception) {
                throw (Exception) t;
            } else {
                throw pWSEx;
            }
        }
    }

    public MasterDocument reject(TaskKey pTaskKey, String pComment) throws Exception {
        try {
            System.out.println("Rejecting task " + pTaskKey);
            MasterDocument newMasterDocument;
            String workspaceId = MainModel.getInstance().getWorkspace().getId();
            newMasterDocument = Tools.resetParentReferences(mCommandService.reject(workspaceId, pTaskKey, pComment));
            MainModel.getInstance().updater.updateMDoc(newMasterDocument);
            return newMasterDocument;
        } catch (WebServiceException pWSEx) {
            Throwable t = pWSEx.getCause();
            if (t instanceof Exception) {
                throw (Exception) t;
            } else {
                throw pWSEx;
            }
        }
    }

    public MasterDocument saveTags(MasterDocument pMasterDocument, String[] pTags) throws Exception {
        try {
            System.out.println("Saving tags of master document " + pMasterDocument);
            MasterDocument newMasterDocument;
            newMasterDocument = Tools.resetParentReferences(mCommandService.saveTags(pMasterDocument.getKey(), pTags));
            MainModel.getInstance().updater.updateMDoc(newMasterDocument);
            return newMasterDocument;
        } catch (WebServiceException pWSEx) {
            Throwable t = pWSEx.getCause();
            if (t instanceof Exception) {
                throw (Exception) t;
            } else {
                throw pWSEx;
            }
        }
    }

    public MasterDocument checkIn(MasterDocument pMasterDocument) throws Exception {
        try {
            System.out.println("Checking In master document " + pMasterDocument);
            MasterDocument newMasterDocument;
            newMasterDocument = Tools.resetParentReferences(mCommandService.checkIn(pMasterDocument.getKey()));
            MainModel.getInstance().updater.checkIn(newMasterDocument);
            return newMasterDocument;
        } catch (WebServiceException pWSEx) {
            Throwable t = pWSEx.getCause();
            if (t instanceof Exception) {
                throw (Exception) t;
            } else {
                throw pWSEx;
            }
        }
    }

    public MasterDocument checkOut(MasterDocument pMasterDocument) throws Exception {
        try {
            System.out.println("Checking Out master document " + pMasterDocument);
            MasterDocument newMasterDocument;
            newMasterDocument = Tools.resetParentReferences(mCommandService.checkOut(pMasterDocument.getKey()));
            MainModel.getInstance().updater.checkOut(newMasterDocument);
            return newMasterDocument;
        } catch (WebServiceException pWSEx) {
            Throwable t = pWSEx.getCause();
            if (t instanceof Exception) {
                throw (Exception) t;
            } else {
                throw pWSEx;
            }
        }
    }

    public MasterDocument undoCheckOut(MasterDocument pMasterDocument) throws Exception {
        try {
            System.out.println("Undoing Check Out master document " + pMasterDocument);
            MasterDocument newMasterDocument;
            newMasterDocument = Tools.resetParentReferences(mCommandService.undoCheckOut(pMasterDocument.getKey()));
            MainModel.getInstance().updater.undoCheckOut(newMasterDocument);
            return newMasterDocument;
        } catch (WebServiceException pWSEx) {
            Throwable t = pWSEx.getCause();
            if (t instanceof Exception) {
                throw (Exception) t;
            } else {
                throw pWSEx;
            }
        }
    }

    public void delMDoc(MasterDocument pMasterDocument) throws Exception {
        try {
            System.out.println("Deleting master document " + pMasterDocument);
            mCommandService.delMDoc(pMasterDocument.getKey());
            MainModel.getInstance().updater.delMDoc(pMasterDocument);
        } catch (WebServiceException pWSEx) {
            Throwable t = pWSEx.getCause();
            if (t instanceof Exception) {
                throw (Exception) t;
            } else {
                throw pWSEx;
            }
        }
    }

    public void delMDocTemplate(MasterDocumentTemplate pMDocTemplate) throws Exception {
        try {
            System.out.println("Deleting master document template " + pMDocTemplate);
            mCommandService.delMDocTemplate(pMDocTemplate.getKey());
            MainModel.getInstance().updater.delMDocTemplate(pMDocTemplate);
        } catch (WebServiceException pWSEx) {
            Throwable t = pWSEx.getCause();
            if (t instanceof Exception) {
                throw (Exception) t;
            } else {
                throw pWSEx;
            }
        }
    }

    public MasterDocument moveMDoc(String pParentFolder, MasterDocument pMasterDocument) throws Exception {
        try {
            System.out.println("Moving master document " + pMasterDocument);
            MasterDocument newMasterDocument;
            newMasterDocument = Tools.resetParentReferences(mCommandService.moveMDoc(pParentFolder, pMasterDocument.getKey()));
            MainModel.getInstance().updater.moveMDoc(newMasterDocument);
            return newMasterDocument;
        } catch (WebServiceException pWSEx) {
            Throwable t = pWSEx.getCause();
            if (t instanceof Exception) {
                throw (Exception) t;
            } else {
                throw pWSEx;
            }
        }
    }

    public MasterDocumentKey[] delFolder(String pFolder) throws Exception {
        try {
            System.out.println("Deleting folder " + pFolder);
            MasterDocumentKey[] pks = mCommandService.delFolder(pFolder);
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

    public void saveFile(Component pParent, Document pDocument, File pLocalFile) throws Exception {
        MainModel model = MainModel.getInstance();
        String message = I18N.BUNDLE.getString("UploadMsg_part1") + " " + pLocalFile.getName() + " (" + (int) (pLocalFile.length() / 1024) + I18N.BUNDLE.getString("UploadMsg_part2");
        DataHandler data = new DataHandler(new ProgressMonitorFileDataSource(pParent, pLocalFile, message));
        try {
            Map<String, Object> ctxt = ((BindingProvider) mFileService).getRequestContext();
            try {
                if (ctxt.containsKey(JAXWSProperties.HTTP_CLIENT_STREAMING_CHUNK_SIZE)) {
                    mFileService.uploadToDocument(model.getWorkspace().getId(), pDocument.getMasterDocumentId(), pDocument.getMasterDocumentVersion(), pDocument.getIteration(), pLocalFile.getName(), data);
                } else {
                    //workaround mode
                    uploadFileWithServlet(pParent, pLocalFile, getServletURL(pDocument, pLocalFile));
                }

            } catch (Exception ex) {
                if (ex.getCause() instanceof InterruptedIOException) {
                    throw ex;
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

    private String getServletURL(Document pDoc, File pLocalFile) throws UnsupportedEncodingException {
        MainModel model = MainModel.getInstance();
        return Config.getHTTPCodebase() + "files/" + URLEncoder.encode(model.getWorkspace().getId(), "UTF-8") + "/" + "documents/" + URLEncoder.encode(pDoc.getMasterDocumentId(), "UTF-8") + "/" + pDoc.getMasterDocumentVersion() + "/" + pDoc.getIteration() + "/" + URLEncoder.encode(pLocalFile.getName(), "UTF-8");
    }

    public void saveFile(Component pParent, MasterDocumentTemplate pTemplate, File pLocalFile) throws Exception {
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

    private String getServletURL(MasterDocumentTemplate pTemplate, File pLocalFile) throws UnsupportedEncodingException {
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

        } finally {
            out.close();
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
            mCommandService.createFolder(pParentFolder, pFolder);
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

    public MasterDocument createMDoc(String pParentFolder,
            String pMDocID,
            String pTitle,
            String pDescription,
            MasterDocumentTemplate pTemplate,
            WorkflowModel pWorkflowModel) throws Exception {
        try {
            System.out.println("Saving master document " + pMDocID + " version " + "A" + " with title " + pTitle + ", template " + pTemplate + " and description " + pDescription + " in " + pParentFolder);
            MasterDocument newMasterDocument;
            //TODO ACL
            ACLUserEntry[] userEntries=null;
            ACLUserGroupEntry[] groupEntries=null;

            newMasterDocument = Tools.resetParentReferences(mCommandService.createMDoc(pParentFolder, pMDocID, pTitle, pDescription, pTemplate == null ? null : pTemplate.getId(), pWorkflowModel == null ? null : pWorkflowModel.getId(),userEntries,groupEntries));
            MainModel.getInstance().updater.createMDocInFolder(newMasterDocument);
            return newMasterDocument;
        } catch (WebServiceException pWSEx) {
            Throwable t = pWSEx.getCause();
            if (t instanceof Exception) {
                throw (Exception) t;
            } else {
                throw pWSEx;
            }
        }
    }

    public MasterDocumentTemplate createMDocTemplate(String pId, String pDocumentType,
            String pMask, Set<InstanceAttributeTemplate> pAttributeTemplates, boolean pIdGenerated) throws Exception {
        try {
            System.out.println("Saving master document template " + pId + " with mask " + pMask);
            String workspaceId = MainModel.getInstance().getWorkspace().getId();
            MasterDocumentTemplate newTemplate;
            newTemplate = Tools.resetParentReferences(mCommandService.createMDocTemplate(workspaceId, pId, pDocumentType, pMask, pAttributeTemplates.toArray(new InstanceAttributeTemplate[pAttributeTemplates.size()]), pIdGenerated));
            MainModel.getInstance().updater.createMDocTemplate(newTemplate);
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

    public MasterDocumentTemplate updateMDocTemplate(MasterDocumentTemplate pTemplate, String pDocumentType, String pMask, Set<InstanceAttributeTemplate> pAttributeTemplates, boolean pIdGenerated) throws Exception {
        try {
            System.out.println("Saving master document template " + pTemplate);
            MasterDocumentTemplate newTemplate;
            newTemplate = Tools.resetParentReferences(mCommandService.updateMDocTemplate(pTemplate.getKey(), pDocumentType, pMask, pAttributeTemplates.toArray(new InstanceAttributeTemplate[pAttributeTemplates.size()]), pIdGenerated));
            MainModel.getInstance().updater.updateMDocTemplate(newTemplate);
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

    public MasterDocument[] createVersion(MasterDocument pMasterDocument,
            String pTitle,
            String pDescription,
            WorkflowModel pWorkflowModel) throws Exception {
        try {
            System.out.println("Creating new version to master document " + pMasterDocument + " with title " + pTitle + " and description " + pDescription);
            //TODO ACL
            ACLUserEntry[] userEntries=null;
            ACLUserGroupEntry[] groupEntries=null;

            MasterDocument[] originalAndNewMDoc = Tools.resetParentReferences(mCommandService.createVersion(pMasterDocument.getKey(), pTitle, pDescription, pWorkflowModel == null ? null : pWorkflowModel.getId(),userEntries,groupEntries));
            MainModel.getInstance().updater.makeNewVersion(originalAndNewMDoc);
            return originalAndNewMDoc;
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
            User user = mCommandService.savePersonalInfo(workspaceId, pName, pEmail, pLanguage);
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

    public void subscribeStateNotification(MasterDocument pMasterDocument) throws Exception {
        try {
            System.out.println("Subscribe state notification on master document " + pMasterDocument);
            mCommandService.subscribeToStateChangeEvent(pMasterDocument.getKey());
            MainModel.getInstance().updater.addStateNotification(pMasterDocument);
        } catch (WebServiceException pWSEx) {
            Throwable t = pWSEx.getCause();
            if (t instanceof Exception) {
                throw (Exception) t;
            } else {
                throw pWSEx;
            }
        }
    }

    public void unsubscribeStateNotification(MasterDocument pMasterDocument) throws Exception {
        try {
            System.out.println("Unsubscribe state notification on master document " + pMasterDocument);
            mCommandService.unsubscribeToStateChangeEvent(pMasterDocument.getKey());
            MainModel.getInstance().updater.removeStateNotification(pMasterDocument);
        } catch (WebServiceException pWSEx) {
            Throwable t = pWSEx.getCause();
            if (t instanceof Exception) {
                throw (Exception) t;
            } else {
                throw pWSEx;
            }
        }
    }

    public void subscribeIterationNotification(MasterDocument pMasterDocument) throws Exception {
        try {
            System.out.println("Subscribe iteration notification on master document " + pMasterDocument);
            mCommandService.subscribeToIterationChangeEvent(pMasterDocument.getKey());
            MainModel.getInstance().updater.addIterationNotification(pMasterDocument);
        } catch (WebServiceException pWSEx) {
            Throwable t = pWSEx.getCause();
            if (t instanceof Exception) {
                throw (Exception) t;
            } else {
                throw pWSEx;
            }
        }
    }

    public void unsubscribeIterationNotification(MasterDocument pMasterDocument) throws Exception {
        try {
            System.out.println("Unsubscribe iteration notification on master document " + pMasterDocument);
            mCommandService.unsubscribeToIterationChangeEvent(pMasterDocument.getKey());
            MainModel.getInstance().updater.removeIterationNotification(pMasterDocument);
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
            mCommandService.delWorkflowModel(pWorkflowModel.getKey());
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
            mCommandService.delTag(new TagKey(workspaceId, pTag));
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
                mCommandService.delWorkflowModel(pWorkflowModel.getKey());
            } catch (WorkflowModelNotFoundException pWNFEx) {

            }
            ActivityModel[] activityModels = pWorkflowModel.getActivityModels().toArray(new ActivityModel[pWorkflowModel.getActivityModels().size()]);
            model = Tools.resetParentReferences(mCommandService.createWorkflowModel(pWorkflowModel.getWorkspaceId(), pWorkflowModel.getId(), pWorkflowModel.getFinalLifeCycleState(), activityModels));
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
            MasterDocument newMDoc;
            newMDoc = Tools.resetParentReferences(mCommandService.removeFileFromDocument(pFile.getFullName()));
            MainModel.getInstance().updater.updateMDoc(newMDoc);
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
            MasterDocumentTemplate newTemplate;
            newTemplate = Tools.resetParentReferences(mCommandService.removeFileFromTemplate(pFile.getFullName()));
            MainModel.getInstance().updater.updateMDocTemplate(newTemplate);
        } catch (WebServiceException pWSEx) {
            Throwable t = pWSEx.getCause();
            if (t instanceof Exception) {
                throw (Exception) t;
            } else {
                throw pWSEx;
            }
        }
    }

    public Document updateDoc(Document pDocument, String pComment, InstanceAttribute[] pAttributes, DocumentToDocumentLink[] pLinks) throws Exception {
        try {
            System.out.println("Updating document " + pDocument);
            MasterDocument newMDoc;
            DocumentKey docKey = pDocument.getKey();
            
            DocumentKey[] linkKeys = new DocumentKey[pLinks.length];
            for (int i = 0; i<pLinks.length;i++) {
                linkKeys[i]=pLinks[i].getToDocumentKey();
            }

            newMDoc = Tools.resetParentReferences(mCommandService.updateDoc(docKey, pComment, pAttributes, linkKeys));
            MainModel.getInstance().updater.updateMDoc(newMDoc);
            return newMDoc.getIteration(docKey.getIteration());
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
            ((BindingProvider) mCommandService).getRequestContext().put(BindingProvider.USERNAME_PROPERTY, login);
            ((BindingProvider) mCommandService).getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, password);
            ((BindingProvider) mFileService).getRequestContext().put(BindingProvider.USERNAME_PROPERTY, login);
            ((BindingProvider) mFileService).getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, password);
            mCommandService.whoAmI(workspaceId);
            MainModel.init(login, password, workspaceId, mCommandService, mFileService);
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
