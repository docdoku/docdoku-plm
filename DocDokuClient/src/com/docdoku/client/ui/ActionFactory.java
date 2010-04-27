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


