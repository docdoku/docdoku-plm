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

package com.docdoku.client.actions;

import com.docdoku.client.data.CheckedOutTreeNode;
import com.docdoku.client.data.FolderTreeNode;
import com.docdoku.client.data.HomeTreeNode;
import com.docdoku.client.data.RootTreeNode;
import com.docdoku.client.data.TagRootTreeNode;
import com.docdoku.client.data.TagTreeNode;
import com.docdoku.client.data.TemplateTreeNode;
import com.docdoku.client.data.WorkflowModelTreeNode;
import com.docdoku.core.common.User;
import java.util.HashMap;
import java.util.Map;

import javax.swing.*;

import com.docdoku.client.backbone.ElementSelectedEvent;
import com.docdoku.client.backbone.ElementSelectedEvent.ElementType;
import com.docdoku.client.ui.ActionFactory;
import com.docdoku.client.ui.ExplorerFrame;
import com.docdoku.client.data.MainModel;
import com.docdoku.core.document.DocumentMaster;

public class ActionFactoryImpl implements ActionFactory {
    
    private ExplorerFrame mOwner;
    private boolean mFolderSelected;
    private boolean mTagSelected;
    private boolean mDocMSelected;
    private boolean mWorkflowModelSelected;
    private boolean mDocMTemplateSelected;
    private Map<String, Action> mActions;
    
    public ActionFactoryImpl(ExplorerFrame pOwner) {
        mOwner = pOwner;
        mActions = new HashMap<String, Action>();
    }

    @Override
    public ActionFactory clone(ExplorerFrame pOwner) {
        return new ActionFactoryImpl(pOwner);
    }

    @Override
    public Action getCreateDocMAction() {
        Action action = mActions.get("CreateDocMAction");
        if (action == null) {
            action = new CreateDocMAction(mOwner);
            mActions.put("CreateDocMAction", action);
            action.setEnabled(false);
        }
        return action;
    }

    @Override
    public Action getCreateDocMTemplateAction() {
        Action action = mActions.get("CreateDocMTemplateAction");
        if (action == null) {
            action = new CreateDocMTemplateAction(mOwner);
            mActions.put("CreateDocMTemplateAction", action);
        }
        return action;
    }

    @Override
    public Action getCheckInAction() {
        Action action = mActions.get("CheckInAction");
        if (action == null) {
            action = new CheckInAction(mOwner);
            mActions.put("CheckInAction", action);
            action.setEnabled(false);
        }
        return action;
    }

    @Override
    public Action getCheckOutAction() {
        Action action = mActions.get("CheckOutAction");
        if (action == null) {
            action = new CheckOutAction(mOwner);
            mActions.put("CheckOutAction", action);
            action.setEnabled(false);
        }
        return action;
    }

    @Override
    public Action getUndoCheckOutAction() {
        Action action = mActions.get("UndoCheckOutAction");
        if (action == null) {
            action = new UndoCheckOutAction(mOwner);
            mActions.put("UndoCheckOutAction", action);
            action.setEnabled(false);
        }
        return action;
    }

    @Override
    public Action getDeleteElementAction() {
        Action action = mActions.get("DeleteElementAction");
        if (action == null) {
            action = new DeleteElementAction(mOwner);
            mActions.put("DeleteElementAction", action);
            action.setEnabled(false);
        }
        return action;
    }

    @Override
    public Action getRefreshAction() {
        Action action = mActions.get("RefreshAction");
        if (action == null) {
            action = new RefreshAction(mOwner);
            mActions.put("RefreshAction", action);
        }
        return action;
    }

    @Override
    public Action getCreateFolderAction() {
        Action action = mActions.get("CreateFolderAction");
        if (action == null) {
            action = new CreateFolderAction(mOwner);
            mActions.put("CreateFolderAction", action);
            action.setEnabled(false);
        }
        return action;
    }

    @Override
    public Action getEditElementAction() {
        Action action = mActions.get("EditElementAction");
        if (action == null) {
            action = new EditElementAction(mOwner);
            mActions.put("EditElementAction", action);
            action.setEnabled(false);
        }
        return action;
    }

    @Override
    public Action getCreateVersionAction() {
        Action action = mActions.get("CreateVersionAction");
        if (action == null) {
            action = new CreateVersionAction(mOwner);
            mActions.put("CreateVersionAction", action);
            action.setEnabled(false);
        }
        return action;
    }

    @Override
    public Action getViewElementAction() {
        Action action = mActions.get("ViewElementAction");
        if (action == null) {
            action = new ViewElementAction(mOwner);
            mActions.put("ViewElementAction", action);
            action.setEnabled(false);
        }
        return action;
    }

    @Override
    public Action getViewIterationsAction() {
        Action action = mActions.get("ViewIterationsAction");
        if (action == null) {
            action = new ViewIterationsAction(mOwner);
            mActions.put("ViewIterationsAction", action);
            action.setEnabled(false);
        }
        return action;
    }

    @Override
    public Action getNotificationAction() {
        Action action = mActions.get("NotificationAction");
        if (action == null) {
            action = new NotificationAction(mOwner);
            mActions.put("NotificationAction", action);
            action.setEnabled(false);
        }
        return action;
    }

    @Override
    public Action getEditUserAction() {
        Action action = mActions.get("EditUserAction");
        if (action == null) {
            action = new EditUserAction(mOwner);
            mActions.put("EditUserAction", action);
        }
        return action;
    }

    @Override
    public Action getNewWinAction() {
        Action action = mActions.get("NewWinAction");
        if (action == null) {
            action = new NewWinAction(mOwner);
            mActions.put("NewWinAction", action);
        }
        return action;
    }

    @Override
    public Action getExportAction() {
        Action action = mActions.get("ExportAction");
        if (action == null) {
            action = new ExportAction(mOwner);
            mActions.put("ExportAction", action);
            action.setEnabled(false);
        }
        return action;
    }

    @Override
    public Action getCloseWinAction() {
        Action action = mActions.get("CloseWinAction");
        if (action == null) {
            action = new CloseWinAction(mOwner);
            mActions.put("CloseWinAction", action);
        }
        return action;
    }

    @Override
    public Action getSettingAction() {
        Action action = mActions.get("SettingAction");
        if (action == null) {
            action = new SettingAction(mOwner);
            mActions.put("SettingAction", action);
        }
        return action;
    }

    @Override
    public Action getApproveAction() {
        Action action = mActions.get("ApproveAction");
        if (action == null) {
            action = new ApproveAction(mOwner);
            mActions.put("ApproveAction", action);
            action.setEnabled(false);
        }
        return action;
    }

    @Override
    public Action getRejectAction() {
        Action action = mActions.get("RejectAction");
        if (action == null) {
            action = new RejectAction(mOwner);
            mActions.put("RejectAction", action);
            action.setEnabled(false);
        }
        return action;
    }

    @Override
    public Action getDistributeDocumentAction() {
        Action action = mActions.get("DistributeDocumentAction");
        if (action == null) {
            action = new DistributeDocumentAction(mOwner);
            mActions.put("DistributeDocumentAction", action);
            action.setEnabled(false);
        }
        return action;
    }

    @Override
    public Action getManageTagsAction() {
        Action action = mActions.get("ManageTagsAction");
        if (action == null) {
            action = new ManageTagsAction(mOwner);
            mActions.put("ManageTagsAction", action);
            action.setEnabled(false);
        }
        return action;
    }

    @Override
    public Action getSearchAction() {
        Action action = mActions.get("SearchAction");
        if (action == null) {
            action = new SearchAction(mOwner);
            mActions.put("SearchAction", action);
        }
        return action;
    }

    @Override
    public Action getCreateWorkflowModelAction() {
        Action action = mActions.get("CreateWorkflowModelAction");
        if (action == null) {
            action = new CreateWorkflowModelAction(mOwner);
            mActions.put("CreateWorkflowModelAction", action);
        }
        return action;
    }

    @Override
    public Action getDisplayShortcutsAction() {
        Action action = mActions.get("DisplayShortcutsAction");
        if (action == null) {
            action = new DisplayShortcutsAction(mOwner);
            mActions.put("DisplayShortcutsAction", action);
        }
        return action;
    }

    @Override
    public void elementSelected(ElementSelectedEvent pElementSelectedEvent) {
        Object selection = pElementSelectedEvent.getElement();
        ElementType type = pElementSelectedEvent.getElementType();
        
        switch (type) {
            case MASTER_DOCUMENT:
                if (selection != null) {
                    DocumentMaster docM =((DocumentMaster)selection);
                    switchOnDocMActions(docM);
                    mDocMSelected=true;
                } else {
                    switchOffDocMActions();
                    if(!mFolderSelected  && !mTagSelected)
                        mActions.get("DeleteElementAction").setEnabled(false);
                    mDocMSelected=false;
                }
                break;
            case WORKFLOW_MODEL:
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
            case MASTER_DOCUMENT_TEMPLATE:
                if (selection != null) {
                    switchOnDocMTemplateActions();
                    mDocMTemplateSelected=true;
                } else {
                    switchOffDocMTemplateActions();
                    if(!mFolderSelected && !mTagSelected)
                        mActions.get("DeleteElementAction").setEnabled(false);
                    mDocMTemplateSelected=false;
                }
                break;
            case FOLDER_TREE_NODE:
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
                    if(!mDocMSelected && !mWorkflowModelSelected &&!mDocMTemplateSelected)
                        mActions.get("DeleteElementAction").setEnabled(false);
                    mFolderSelected=false;
                    mTagSelected=false;
                }
                break;
        }
    }
    
    private void switchOffDocMActions(){
        mActions.get("CheckInAction").setEnabled(false);
        mActions.get("CheckOutAction").setEnabled(false);
        mActions.get("UndoCheckOutAction").setEnabled(false);
        mActions.get("CreateVersionAction").setEnabled(false);
        mActions.get("ViewIterationsAction").setEnabled(false);
        mActions.get("NotificationAction").setEnabled(false);
        mActions.get("ApproveAction").setEnabled(false);
        mActions.get("RejectAction").setEnabled(false);
        mActions.get("ManageTagsAction").setEnabled(false);
        mActions.get("DistributeDocumentAction").setEnabled(false);
        mActions.get("EditElementAction").setEnabled(false);
        mActions.get("ViewElementAction").setEnabled(false);
    }
    private void switchOnDocMActions(DocumentMaster pDocM){
        boolean isCheckedOut = pDocM.isCheckedOut();
        boolean hasItereation = pDocM.getNumberOfIterations()!=0;
        boolean hasWorkflow = pDocM.hasWorkflow();
        User currentUser= MainModel.getInstance().getUser();
        boolean hasCheckedOut=currentUser.equals(pDocM.getCheckOutUser());
        
        mActions.get("CheckInAction").setEnabled(hasCheckedOut);
        mActions.get("CheckOutAction").setEnabled(!isCheckedOut);
        mActions.get("UndoCheckOutAction").setEnabled(hasCheckedOut);
        
        mActions.get("CreateVersionAction").setEnabled(hasItereation);
        mActions.get("ViewIterationsAction").setEnabled(hasItereation);
        mActions.get("ViewElementAction").setEnabled(hasItereation);
        
        mActions.get("ApproveAction").setEnabled(hasWorkflow);
        mActions.get("RejectAction").setEnabled(hasWorkflow);
        
        mActions.get("ManageTagsAction").setEnabled(true);
        mActions.get("DistributeDocumentAction").setEnabled(true);
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
    
    private void switchOffDocMTemplateActions(){
        mActions.get("EditElementAction").setEnabled(false);
        mActions.get("ViewElementAction").setEnabled(false);
    }
    private void switchOnDocMTemplateActions(){
        mActions.get("EditElementAction").setEnabled(true);
        mActions.get("ViewElementAction").setEnabled(true);
        mActions.get("DeleteElementAction").setEnabled(true);
    }
    
    private void switchOnRootFolderActions(){
        mActions.get("CreateDocMAction").setEnabled(true);
        mActions.get("ExportAction").setEnabled(true);
        mActions.get("CreateFolderAction").setEnabled(true);
        mActions.get("DeleteElementAction").setEnabled(false);
    }
    
    private void switchOnHomeFolderActions(){
        mActions.get("CreateDocMAction").setEnabled(true);
        mActions.get("ExportAction").setEnabled(false);
        mActions.get("CreateFolderAction").setEnabled(true);
        mActions.get("DeleteElementAction").setEnabled(false);
    }
    
    private void switchOnWorkflowFolderActions(){
        mActions.get("CreateDocMAction").setEnabled(false);
        mActions.get("ExportAction").setEnabled(false);
        mActions.get("CreateFolderAction").setEnabled(false);
        mActions.get("DeleteElementAction").setEnabled(false);
    }
    
    private void switchOnTemplateFolderActions(){
        mActions.get("CreateDocMAction").setEnabled(false);
        mActions.get("ExportAction").setEnabled(false);
        mActions.get("CreateFolderAction").setEnabled(false);
        mActions.get("DeleteElementAction").setEnabled(false);
    }
    
    private void switchOnTagRootFolderActions(){
        mActions.get("CreateDocMAction").setEnabled(false);
        mActions.get("ExportAction").setEnabled(false);
        mActions.get("CreateFolderAction").setEnabled(false);
        mActions.get("DeleteElementAction").setEnabled(false);
    }
    
    private void switchOnTagFolderActions(){
        mActions.get("CreateDocMAction").setEnabled(false);
        mActions.get("ExportAction").setEnabled(false);
        mActions.get("CreateFolderAction").setEnabled(false);
        mActions.get("DeleteElementAction").setEnabled(true);
    }
    
    private void switchOnCheckedOutFolderActions(){
        mActions.get("CreateDocMAction").setEnabled(false);
        mActions.get("ExportAction").setEnabled(false);
        mActions.get("CreateFolderAction").setEnabled(false);
        mActions.get("DeleteElementAction").setEnabled(false);
    }
    
    private void switchOnFolderActions(){
        mActions.get("CreateDocMAction").setEnabled(true);
        mActions.get("ExportAction").setEnabled(true);
        mActions.get("CreateFolderAction").setEnabled(true);
        mActions.get("DeleteElementAction").setEnabled(true);
    }
    
    private void switchOffFolderActions(){
        mActions.get("CreateDocMAction").setEnabled(false);
        mActions.get("ExportAction").setEnabled(false);
        mActions.get("CreateFolderAction").setEnabled(false);
    }


}
