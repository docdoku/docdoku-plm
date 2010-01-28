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
package com.docdoku.gwt.explorer.client.actions;

import com.docdoku.gwt.explorer.client.data.ServiceLocator;
import com.docdoku.gwt.explorer.client.localization.ExplorerI18NConstants;
import com.docdoku.gwt.explorer.client.ui.ExplorerPage;
import com.docdoku.gwt.explorer.client.util.HTMLUtil;
import com.docdoku.gwt.explorer.common.MasterDocumentDTO;
import com.docdoku.gwt.explorer.common.MasterDocumentTemplateDTO;
import com.docdoku.gwt.explorer.common.WorkflowModelDTO;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.TreeItem;
import java.util.List;

/**
 *
 * @author Florent GARIN
 */
public class DeleteElementCommand implements Action {

    private ExplorerPage m_mainPage;

    private final ExplorerI18NConstants i18n = ServiceLocator.getInstance().getExplorerI18NConstants();

    public DeleteElementCommand(ExplorerPage mainPage) {
        m_mainPage = mainPage;
    }

    public void execute(Object... userObject) {

        List<MasterDocumentDTO> selectedMDocs=m_mainPage.getSelectedMDocs();
        List<MasterDocumentTemplateDTO> selectedTemplates= m_mainPage.getSelectedMDocTemplates();
        List<WorkflowModelDTO> selectedWorkflows = m_mainPage.getSelectedWorkflowModels();
        
        final TreeItem selectedItem = m_mainPage.getSelectedFolderTreeItem();
        String selectedFolder = m_mainPage.getSelectedFolderPath();
        String selectedTag = m_mainPage.getSelectedTag();

        if(selectedMDocs.isEmpty() && selectedTemplates.isEmpty() && selectedWorkflows.isEmpty()){
            if(selectedFolder !=null){
                AsyncCallback<MasterDocumentDTO[]> callback = new AsyncCallback<MasterDocumentDTO[]>() {

                    public void onSuccess(MasterDocumentDTO[] mdocs) {
                        TreeItem parentItem=selectedItem.getParentItem();
                        m_mainPage.selectFolderTreeItem(parentItem);
                        m_mainPage.refreshTreeItem(parentItem);
                    }

                    public void onFailure(Throwable caught) {
                        HTMLUtil.showError(caught.getMessage());
                    }
                };
                boolean confirm = Window.confirm(i18n.confirmDelFolder());
                if(confirm)
                    ServiceLocator.getInstance().getExplorerService().delFolder(selectedFolder, callback);
            }
            if(selectedTag !=null){
                AsyncCallback<Void> callback = new AsyncCallback<Void>() {

                    public void onSuccess(Void arg) {
                        m_mainPage.selectFolderTreeItem(selectedItem.getParentItem());
                        m_mainPage.refreshTags();
                    }

                    public void onFailure(Throwable caught) {
                        HTMLUtil.showError(caught.getMessage());
                    }
                };
                boolean confirm = Window.confirm(i18n.confirmDelTag());
                if(confirm)
                    ServiceLocator.getInstance().getExplorerService().delTag(m_mainPage.getWorkspaceId(),selectedTag, callback);
            }
        }
        for (MasterDocumentDTO mdoc : selectedMDocs) {
            String workspaceId = mdoc.getWorkspaceId();
            String id = mdoc.getId();
            String version = mdoc.getVersion();
            AsyncCallback<Void> callback = new AsyncCallback<Void>() {

                public void onSuccess(Void arg) {
                    m_mainPage.refreshElementTable();
                }

                public void onFailure(Throwable caught) {
                    HTMLUtil.showError(caught.getMessage());
                }
            };
            ServiceLocator.getInstance().getExplorerService().delMDoc(workspaceId, id, version, callback);
        }

        for (MasterDocumentTemplateDTO template : selectedTemplates) {
            String workspaceId = template.getWorkspaceId();
            String id = template.getId();
            AsyncCallback<Void> callback = new AsyncCallback<Void>() {

                public void onSuccess(Void arg) {
                    m_mainPage.refreshElementTable();
                }

                public void onFailure(Throwable caught) {
                    HTMLUtil.showError(caught.getMessage());
                }
            };
            ServiceLocator.getInstance().getExplorerService().delMDocTemplate(workspaceId, id, callback);
        }

        for (WorkflowModelDTO workflow : selectedWorkflows) {
            String workspaceId = workflow.getWorkspaceId();
            String id = workflow.getId();
            AsyncCallback<Void> callback = new AsyncCallback<Void>() {

                public void onSuccess(Void arg) {
                    m_mainPage.refreshElementTable();
                }

                public void onFailure(Throwable caught) {
                    HTMLUtil.showError(caught.getMessage());
                }
            };
            ServiceLocator.getInstance().getExplorerService().delWorkflowModel(workspaceId, id, callback);
        }
    }
}
