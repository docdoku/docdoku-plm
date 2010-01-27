package com.docdoku.client.ui;

import com.docdoku.client.backbone.ElementSelectedListener;
import javax.swing.*;


public interface ActionFactory extends ElementSelectedListener{

    public ActionFactory clone(ExplorerFrame pOwner);
    public Action getCreateMDocAction();
    public Action getCreateMDocTemplateAction();
    public Action getCheckInAction();
    public Action getCheckOutAction();
    public Action getUndoCheckOutAction();
    public Action getDeleteElementAction();
    public Action getRefreshAction();
    public Action getCreateFolderAction();
    public Action getEditElementAction();
    public Action getCreateVersionAction();
    public Action getViewElementAction();
    public Action getViewIterationsAction();
    public Action getNotificationAction();
    public Action getEditUserAction();
    public Action getNewWinAction();
    public Action getExportAction();
    public Action getCloseWinAction();
    public Action getSettingAction();
    public Action getApproveAction();
    public Action getRejectAction();
    public Action getManageTagsAction();
    public Action getSearchAction();
    public Action getCreateWorkflowModelAction();
}


