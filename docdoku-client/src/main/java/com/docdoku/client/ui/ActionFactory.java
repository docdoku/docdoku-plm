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

package com.docdoku.client.ui;

import com.docdoku.client.backbone.ElementSelectedListener;

import javax.swing.*;


public interface ActionFactory extends ElementSelectedListener{

    public ActionFactory clone(ExplorerFrame pOwner);
    public Action getCreateDocMAction();
    public Action getCreateDocMTemplateAction();
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
    public Action getDistributeDocumentAction();
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
    public Action getDisplayShortcutsAction();
}


