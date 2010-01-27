package com.docdoku.client.actions;

import com.docdoku.client.data.CheckedOutTreeNode;
import com.docdoku.client.data.FolderTreeNode;
import com.docdoku.client.data.HomeTreeNode;
import com.docdoku.client.data.RootTreeNode;
import com.docdoku.client.data.TagRootTreeNode;
import com.docdoku.client.data.TagTreeNode;
import com.docdoku.client.data.TemplateTreeNode;
import com.docdoku.client.data.WorkflowModelTreeNode;
import com.docdoku.core.entities.User;
import java.util.HashMap;
import java.util.Map;

import javax.swing.*;

import com.docdoku.client.backbone.ElementSelectedEvent;
import com.docdoku.client.backbone.ElementSelectedEvent.ElementType;
import com.docdoku.client.ui.ActionFactory;
import com.docdoku.client.ui.ExplorerFrame;
import com.docdoku.client.data.MainModel;
import com.docdoku.core.entities.MasterDocument;

public class ActionFactoryImpl implements ActionFactory {
    
    private ExplorerFrame mOwner;
    private boolean mFolderSelected;
    private boolean mTagSelected;
    private boolean mMDocSelected;
    private boolean mWorkflowModelSelected;
    private boolean mMDocTemplateSelected;
    private Map<String, Action> mActions;
    
    public ActionFactoryImpl(ExplorerFrame pOwner) {
        mOwner = pOwner;
        mActions = new HashMap<String, Action>();
    }
    
    public ActionFactory clone(ExplorerFrame pOwner) {
        return new ActionFactoryImpl(pOwner);
    }
    
    public Action getCreateMDocAction() {
        Action action = mActions.get("CreateMDocAction");
        if (action == null) {
            action = new CreateMDocAction(mOwner);
            mActions.put("CreateMDocAction", action);
            action.setEnabled(false);
        }
        return action;
    }
    
    public Action getCreateMDocTemplateAction() {
        Action action = mActions.get("CreateMDocTemplateAction");
        if (action == null) {
            action = new CreateMDocTemplateAction(mOwner);
            mActions.put("CreateMDocTemplateAction", action);
        }
        return action;
    }
    
    public Action getCheckInAction() {
        Action action = mActions.get("CheckInAction");
        if (action == null) {
            action = new CheckInAction(mOwner);
            mActions.put("CheckInAction", action);
            action.setEnabled(false);
        }
        return action;
    }
    
    public Action getCheckOutAction() {
        Action action = mActions.get("CheckOutAction");
        if (action == null) {
            action = new CheckOutAction(mOwner);
            mActions.put("CheckOutAction", action);
            action.setEnabled(false);
        }
        return action;
    }
    
    public Action getUndoCheckOutAction() {
        Action action = mActions.get("UndoCheckOutAction");
        if (action == null) {
            action = new UndoCheckOutAction(mOwner);
            mActions.put("UndoCheckOutAction", action);
            action.setEnabled(false);
        }
        return action;
    }
    
    public Action getDeleteElementAction() {
        Action action = mActions.get("DeleteElementAction");
        if (action == null) {
            action = new DeleteElementAction(mOwner);
            mActions.put("DeleteElementAction", action);
            action.setEnabled(false);
        }
        return action;
    }
    
    public Action getRefreshAction() {
        Action action = mActions.get("RefreshAction");
        if (action == null) {
            action = new RefreshAction(mOwner);
            mActions.put("RefreshAction", action);
        }
        return action;
    }
    
    public Action getCreateFolderAction() {
        Action action = mActions.get("CreateFolderAction");
        if (action == null) {
            action = new CreateFolderAction(mOwner);
            mActions.put("CreateFolderAction", action);
            action.setEnabled(false);
        }
        return action;
    }
    
    public Action getEditElementAction() {
        Action action = mActions.get("EditElementAction");
        if (action == null) {
            action = new EditElementAction(mOwner);
            mActions.put("EditElementAction", action);
            action.setEnabled(false);
        }
        return action;
    }
    
    public Action getCreateVersionAction() {
        Action action = mActions.get("CreateVersionAction");
        if (action == null) {
            action = new CreateVersionAction(mOwner);
            mActions.put("CreateVersionAction", action);
            action.setEnabled(false);
        }
        return action;
    }
    
    public Action getViewElementAction() {
        Action action = mActions.get("ViewElementAction");
        if (action == null) {
            action = new ViewElementAction(mOwner);
            mActions.put("ViewElementAction", action);
            action.setEnabled(false);
        }
        return action;
    }
    
    public Action getViewIterationsAction() {
        Action action = mActions.get("ViewIterationsAction");
        if (action == null) {
            action = new ViewIterationsAction(mOwner);
            mActions.put("ViewIterationsAction", action);
            action.setEnabled(false);
        }
        return action;
    }
    
    public Action getNotificationAction() {
        Action action = mActions.get("NotificationAction");
        if (action == null) {
            action = new NotificationAction(mOwner);
            mActions.put("NotificationAction", action);
            action.setEnabled(false);
        }
        return action;
    }
    
    public Action getEditUserAction() {
        Action action = mActions.get("EditUserAction");
        if (action == null) {
            action = new EditUserAction(mOwner);
            mActions.put("EditUserAction", action);
        }
        return action;
    }
    
    public Action getNewWinAction() {
        Action action = mActions.get("NewWinAction");
        if (action == null) {
            action = new NewWinAction(mOwner);
            mActions.put("NewWinAction", action);
        }
        return action;
    }
    
    public Action getExportAction() {
        Action action = mActions.get("ExportAction");
        if (action == null) {
            action = new ExportAction(mOwner);
            mActions.put("ExportAction", action);
            action.setEnabled(false);
        }
        return action;
    }
    
    public Action getCloseWinAction() {
        Action action = mActions.get("CloseWinAction");
        if (action == null) {
            action = new CloseWinAction(mOwner);
            mActions.put("CloseWinAction", action);
        }
        return action;
    }
    
    public Action getSettingAction() {
        Action action = mActions.get("SettingAction");
        if (action == null) {
            action = new SettingAction(mOwner);
            mActions.put("SettingAction", action);
        }
        return action;
    }
    
    public Action getApproveAction() {
        Action action = mActions.get("ApproveAction");
        if (action == null) {
            action = new ApproveAction(mOwner);
            mActions.put("ApproveAction", action);
            action.setEnabled(false);
        }
        return action;
    }
    
    public Action getRejectAction() {
        Action action = mActions.get("RejectAction");
        if (action == null) {
            action = new RejectAction(mOwner);
            mActions.put("RejectAction", action);
            action.setEnabled(false);
        }
        return action;
    }
    
    public Action getManageTagsAction() {
        Action action = mActions.get("ManageTagsAction");
        if (action == null) {
            action = new ManageTagsAction(mOwner);
            mActions.put("ManageTagsAction", action);
            action.setEnabled(false);
        }
        return action;
    }
    
    public Action getSearchAction() {
        Action action = mActions.get("SearchAction");
        if (action == null) {
            action = new SearchAction(mOwner);
            mActions.put("SearchAction", action);
        }
        return action;
    }
    
    public Action getCreateWorkflowModelAction() {
        Action action = mActions.get("CreateWorkflowModelAction");
        if (action == null) {
            action = new CreateWorkflowModelAction(mOwner);
            mActions.put("CreateWorkflowModelAction", action);
        }
        return action;
    }
    
    
    public void elementSelected(ElementSelectedEvent pElmentSelectedEvent) {
        Object selection = pElmentSelectedEvent.getElement();
        ElementType type = pElmentSelectedEvent.getElementType();
        
        switch (type) {
            case MasterDocument:
                if (selection != null) {
                    MasterDocument mdoc =((MasterDocument)selection);
                    switchOnMDocActions(mdoc);
                    mMDocSelected=true;
                } else {
                    switchOffMDocActions();
                    if(!mFolderSelected  && !mTagSelected)
                        mActions.get("DeleteElementAction").setEnabled(false);
                    mMDocSelected=false;
                }
                break;
            case WorkflowModel:
                if (selection != null) {
                    switchOnWorkflowModelActions();
                    mWorkflowModelSelected=true;
                } else {
                    switchOffWorkflowModelActions();
                    if(!mFolderSelected  && !mTagSelected)
                        mActions.get("DeleteElementAction").setEnabled(false);
                    mWorkflowModelSelected=false;
                }
                break;
            case MasterDocumentTemplate:
                if (selection != null) {
                    switchOnMDocTemplateActions();
                    mMDocTemplateSelected=true;
                } else {
                    switchOffMDocTemplateActions();
                    if(!mFolderSelected && !mTagSelected)
                        mActions.get("DeleteElementAction").setEnabled(false);
                    mMDocTemplateSelected=false;
                }
                break;
            case FolderTreeNode:
                if (selection != null) {
                    FolderTreeNode folderTreeNode =((FolderTreeNode)selection);
                    if(folderTreeNode instanceof HomeTreeNode)
                        switchOnHomeFolderActions();
                    else if(folderTreeNode instanceof TemplateTreeNode)
                        switchOnTemplateFolderActions();
                    else if(folderTreeNode instanceof TagTreeNode){
                        mTagSelected=true;
                        switchOnTagFolderActions();
                    }             
                    else if(folderTreeNode instanceof CheckedOutTreeNode)
                        switchOnCheckedOutFolderActions();
                    else if(folderTreeNode instanceof TagRootTreeNode)
                        switchOnTagRootFolderActions();
                    else if(folderTreeNode instanceof WorkflowModelTreeNode)
                        switchOnWorkflowFolderActions();
                    else if(folderTreeNode instanceof RootTreeNode)
                        switchOnRootFolderActions();
                    else{
                        mFolderSelected=true;
                        switchOnFolderActions();
                    }
                    
                } else {
                    switchOffFolderActions();
                    if(!mMDocSelected && !mWorkflowModelSelected &&!mMDocTemplateSelected)
                        mActions.get("DeleteElementAction").setEnabled(false);
                    mFolderSelected=false;
                    mTagSelected=false;
                }
                break;
        }
    }
    
    private void switchOffMDocActions(){
        mActions.get("CheckInAction").setEnabled(false);
        mActions.get("CheckOutAction").setEnabled(false);
        mActions.get("UndoCheckOutAction").setEnabled(false);
        mActions.get("CreateVersionAction").setEnabled(false);
        mActions.get("ViewIterationsAction").setEnabled(false);
        mActions.get("NotificationAction").setEnabled(false);
        mActions.get("ApproveAction").setEnabled(false);
        mActions.get("RejectAction").setEnabled(false);
        mActions.get("ManageTagsAction").setEnabled(false);
        mActions.get("EditElementAction").setEnabled(false);
        mActions.get("ViewElementAction").setEnabled(false);
    }
    private void switchOnMDocActions(MasterDocument pMDoc){
        boolean isCheckedOut = pMDoc.isCheckedOut();
        boolean hasItereation = pMDoc.getNumberOfIterations()!=0;
        boolean hasWorkflow = pMDoc.hasWorkflow();
        User currentUser= MainModel.getInstance().getUser();
        boolean hasCheckedOut=currentUser.equals(pMDoc.getCheckOutUser());
        
        mActions.get("CheckInAction").setEnabled(hasCheckedOut);
        mActions.get("CheckOutAction").setEnabled(!isCheckedOut);
        mActions.get("UndoCheckOutAction").setEnabled(hasCheckedOut);
        
        mActions.get("CreateVersionAction").setEnabled(hasItereation);
        mActions.get("ViewIterationsAction").setEnabled(hasItereation);
        mActions.get("ViewElementAction").setEnabled(hasItereation);
        
        mActions.get("ApproveAction").setEnabled(hasWorkflow);
        mActions.get("RejectAction").setEnabled(hasWorkflow);
        
        mActions.get("ManageTagsAction").setEnabled(true);
        mActions.get("NotificationAction").setEnabled(true);
        mActions.get("DeleteElementAction").setEnabled(true);
        mActions.get("EditElementAction").setEnabled(!isCheckedOut || hasCheckedOut);
        
    }
    
    private void switchOffWorkflowModelActions(){
        mActions.get("EditElementAction").setEnabled(false);
        mActions.get("ViewElementAction").setEnabled(false);
    }
    private void switchOnWorkflowModelActions(){
        mActions.get("EditElementAction").setEnabled(true);
        mActions.get("ViewElementAction").setEnabled(true);
        mActions.get("DeleteElementAction").setEnabled(true);
    }
    
    private void switchOffMDocTemplateActions(){
        mActions.get("EditElementAction").setEnabled(false);
        mActions.get("ViewElementAction").setEnabled(false);
    }
    private void switchOnMDocTemplateActions(){
        mActions.get("EditElementAction").setEnabled(true);
        mActions.get("ViewElementAction").setEnabled(true);
        mActions.get("DeleteElementAction").setEnabled(true);
    }
    
    private void switchOnRootFolderActions(){
        mActions.get("CreateMDocAction").setEnabled(true);
        mActions.get("ExportAction").setEnabled(true);
        mActions.get("CreateFolderAction").setEnabled(true);
        mActions.get("DeleteElementAction").setEnabled(false);
    }
    
    private void switchOnHomeFolderActions(){
        mActions.get("CreateMDocAction").setEnabled(true);
        mActions.get("ExportAction").setEnabled(false);
        mActions.get("CreateFolderAction").setEnabled(true);
        mActions.get("DeleteElementAction").setEnabled(false);
    }
    
    private void switchOnWorkflowFolderActions(){
        mActions.get("CreateMDocAction").setEnabled(false);
        mActions.get("ExportAction").setEnabled(false);
        mActions.get("CreateFolderAction").setEnabled(false);
        mActions.get("DeleteElementAction").setEnabled(false);
    }
    
    private void switchOnTemplateFolderActions(){
        mActions.get("CreateMDocAction").setEnabled(false);
        mActions.get("ExportAction").setEnabled(false);
        mActions.get("CreateFolderAction").setEnabled(false);
        mActions.get("DeleteElementAction").setEnabled(false);
    }
    
    private void switchOnTagRootFolderActions(){
        mActions.get("CreateMDocAction").setEnabled(false);
        mActions.get("ExportAction").setEnabled(false);
        mActions.get("CreateFolderAction").setEnabled(false);
        mActions.get("DeleteElementAction").setEnabled(false);
    }
    
    private void switchOnTagFolderActions(){
        mActions.get("CreateMDocAction").setEnabled(false);
        mActions.get("ExportAction").setEnabled(false);
        mActions.get("CreateFolderAction").setEnabled(false);
        mActions.get("DeleteElementAction").setEnabled(true);
    }
    
    private void switchOnCheckedOutFolderActions(){
        mActions.get("CreateMDocAction").setEnabled(false);
        mActions.get("ExportAction").setEnabled(false);
        mActions.get("CreateFolderAction").setEnabled(false);
        mActions.get("DeleteElementAction").setEnabled(false);
    }
    
    private void switchOnFolderActions(){
        mActions.get("CreateMDocAction").setEnabled(true);
        mActions.get("ExportAction").setEnabled(true);
        mActions.get("CreateFolderAction").setEnabled(true);
        mActions.get("DeleteElementAction").setEnabled(true);
    }
    
    private void switchOffFolderActions(){
        mActions.get("CreateMDocAction").setEnabled(false);
        mActions.get("ExportAction").setEnabled(false);
        mActions.get("CreateFolderAction").setEnabled(false);
    }
}
