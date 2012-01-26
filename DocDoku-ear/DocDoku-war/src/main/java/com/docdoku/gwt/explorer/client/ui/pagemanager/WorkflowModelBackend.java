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

package com.docdoku.gwt.explorer.client.ui.pagemanager;

import com.docdoku.gwt.explorer.client.data.ServiceLocator;
import com.docdoku.gwt.explorer.client.data.WorkflowModelTableModel;
import com.docdoku.gwt.client.ui.widget.table.TableModel;
import com.docdoku.gwt.explorer.client.util.HTMLUtil;
import com.docdoku.gwt.explorer.shared.WorkflowResponse;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 *
 * @author Emmanuel Nhan {@literal <emmanuel.nhan@insa-lyon.fr>}
 */
public class WorkflowModelBackend implements PageManagerBackend{

    private String workspaceId ;
    private WorkflowModelTableModel model ;
    private PageManager frontend ;
    private InternalCallback callback ;

    public WorkflowModelBackend(String workspaceId) {
        this.workspaceId = workspaceId;
        callback = new InternalCallback() ;
    }

    public void fetchNextPage() {
        int startPoint = frontend.getOffsetForPageNumber(frontend.getCurrentPage() +1) ;
        ServiceLocator.getInstance().getExplorerService().getWorkflowModels(workspaceId, startPoint, frontend.getPageSize(), callback);
    }

    public void fetchPreviousPage() {
        int startPoint = frontend.getOffsetForPageNumber(frontend.getCurrentPage() -1) ;
        ServiceLocator.getInstance().getExplorerService().getWorkflowModels(workspaceId, startPoint, frontend.getPageSize(), callback);
    }

    public void fetchFirstPage() {
        ServiceLocator.getInstance().getExplorerService().getWorkflowModels(workspaceId, 0, frontend.getPageSize(), callback);
    }

    public void fetchLastPage() {
        int startPoint = frontend.getOffsetForPageNumber(frontend.getNumberOfPages() -1) ;
        ServiceLocator.getInstance().getExplorerService().getWorkflowModels(workspaceId, startPoint, frontend.getPageSize(), callback);
    }

    public void setFrontend(PageManager pm) {
        frontend = pm ;
    }

    public TableModel getTableModel() {
        return model ;
    }

    private void notifyFrontend(WorkflowResponse result) {
        frontend.dataReady(result);
    }

    private class InternalCallback implements AsyncCallback<WorkflowResponse> {

        public void onFailure(Throwable caught) {
            HTMLUtil.showError(caught.getMessage());
        }

        public void onSuccess(WorkflowResponse result) {
            model = new WorkflowModelTableModel(result.getData());
            notifyFrontend(result);
        }
    }

}
