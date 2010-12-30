/*
 * RejectTaskCommand.java
 * 
 * Copyright (c) 2009 Docdoku. All rights reserved.
 * 
 * This file is part of Docdoku.
 * 
 * Docdoku is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Docdoku is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Docdoku.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.docdoku.gwt.explorer.client.actions;

import com.docdoku.gwt.explorer.client.data.ServiceLocator;
import com.docdoku.gwt.explorer.client.ui.ExplorerPage;
import com.docdoku.gwt.explorer.client.ui.workflow.viewer.WorkflowGlassPanel;
import com.docdoku.gwt.explorer.client.util.HTMLUtil;
import com.docdoku.gwt.explorer.shared.MasterDocumentDTO;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 *
 * @author Emmanuel Nhan {@literal <emmanuel.nhan@insa-lyon.fr>}
 */
public class RejectTaskCommand implements Action{

    private ExplorerPage m_mainPage ;

    public RejectTaskCommand(ExplorerPage m_mainPage) {
        this.m_mainPage = m_mainPage;
    }

    // parametters :
    // 0 : workspace id
    // 1 : workflow id
    // 2 : activity step
    // 3 : task step
    // 4 : comment
    // 5 : Workflow vier to update
    public void execute(Object... userObject) {
        String workspaceId = (String) userObject[0];
        int workflowId = (Integer) userObject[1] ;
        int activityStep = (Integer) userObject[2] ;
        int taskStep = (Integer) userObject[3] ;
        String comment = (String) userObject[4] ;
        final WorkflowGlassPanel caller = (WorkflowGlassPanel) userObject[5] ;

        AsyncCallback<MasterDocumentDTO> callback = new AsyncCallback<MasterDocumentDTO>() {

            public void onFailure(Throwable caught) {
                HTMLUtil.showError(caught.getMessage());
            }

            public void onSuccess(MasterDocumentDTO result) {
                m_mainPage.setCurrentMDoc(result);
                caller.updateAfterAcceptOrReject(result);
            }
        };
        ServiceLocator.getInstance().getExplorerService().reject(workspaceId, workflowId, activityStep, taskStep, comment, callback);
    }

}
