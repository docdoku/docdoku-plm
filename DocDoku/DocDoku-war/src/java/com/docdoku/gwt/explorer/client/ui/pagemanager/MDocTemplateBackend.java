/*
 * MDocTemplateBackend.java
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
package com.docdoku.gwt.explorer.client.ui.pagemanager;

import com.docdoku.gwt.explorer.client.data.MDocTemplateTableModel;
import com.docdoku.gwt.explorer.client.data.ServiceLocator;
import com.docdoku.gwt.explorer.client.ui.widget.table.TableModel;
import com.docdoku.gwt.explorer.client.util.HTMLUtil;
import com.docdoku.gwt.explorer.common.MDocTemplateResponse;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 *
 * @author Emmanuel Nhan {@literal <emmanuel.nhan@insa-lyon.fr>}
 */
public class MDocTemplateBackend implements PageManagerBackend {

    private String workspaceId;
    private PageManager frontend;
    private MDocTemplateTableModel model;
    private InternalCallback callback;

    public MDocTemplateBackend(String workspaceId) {
        this.workspaceId = workspaceId;
        callback = new InternalCallback();
    }

    public void fetchNextPage() {
        int startPoint = frontend.getOffsetForPageNumber(frontend.getCurrentPage() +1) ;
        ServiceLocator.getInstance().getExplorerService().getMDocTemplates(workspaceId, startPoint, frontend.getPageSize(), callback);
    }

    public void fetchPreviousPage() {
        int startPoint = frontend.getOffsetForPageNumber(frontend.getCurrentPage() -1) ;
        ServiceLocator.getInstance().getExplorerService().getMDocTemplates(workspaceId, startPoint, frontend.getPageSize(), callback);
    }

    public void fetchFirstPage() {
        ServiceLocator.getInstance().getExplorerService().getMDocTemplates(workspaceId, 0, frontend.getPageSize(), callback);
    }

    public void fetchLastPage() {
        int startPoint = frontend.getOffsetForPageNumber(frontend.getNumberOfPages() -1) ;
        ServiceLocator.getInstance().getExplorerService().getMDocTemplates(workspaceId, startPoint, frontend.getPageSize(), callback);
    }

    public void setFrontend(PageManager pm) {
        frontend = pm ;
    }

    public TableModel getTableModel() {
        return model;
    }

    private void notifyFrontend(MDocTemplateResponse result) {
        frontend.dataReady(result);
    }

    private class InternalCallback implements AsyncCallback<MDocTemplateResponse> {

        public void onFailure(Throwable caught) {
            HTMLUtil.showError(caught.getMessage());
        }

        public void onSuccess(MDocTemplateResponse result) {
            model = new MDocTemplateTableModel(result.getData());
            notifyFrontend(result);
        }
    }
}
