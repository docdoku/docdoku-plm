/*
 * MDocTagBackend.java
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

import com.docdoku.gwt.explorer.client.data.MDocTableModel;
import com.docdoku.gwt.explorer.client.data.ServiceLocator;
import com.docdoku.gwt.explorer.client.ui.widget.table.TableModel;
import com.docdoku.gwt.explorer.client.util.HTMLUtil;
import com.docdoku.gwt.explorer.common.MDocResponse;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 *
 * @author Emmanuel Nhan {@literal <emmanuel.nhan@insa-lyon.fr>}
 */
public class MDocTagBackend implements PageManagerBackend {

    private MDocTableModel model;
    private String login;
    private String tagLabel ;
    private String workspaceId ;
    private PageManager frontend ;
    private InternalCallback callback ;

    public MDocTagBackend(String login, String tagLabel, String workspaceId) {
        this.login = login;
        this.tagLabel = tagLabel;
        this.workspaceId = workspaceId;

        callback = new InternalCallback();
    }

    public void fetchNextPage() {
        int startOffset = frontend.getOffsetForPageNumber(frontend.getCurrentPage() + 1) ;
        ServiceLocator.getInstance().getExplorerService().findMDocsByTag(workspaceId, tagLabel, startOffset, frontend.getPageSize(), callback);
    }

    public void fetchPreviousPage() {
        int startOffset = frontend.getOffsetForPageNumber(frontend.getCurrentPage() -1) ;
        ServiceLocator.getInstance().getExplorerService().findMDocsByTag(workspaceId, tagLabel, startOffset, frontend.getPageSize(), callback);
    }

    public void fetchFirstPage() {
        ServiceLocator.getInstance().getExplorerService().findMDocsByTag(workspaceId, tagLabel, 0, frontend.getPageSize(), callback);
    }

    public void fetchLastPage() {
        int startOffset = frontend.getOffsetForPageNumber(frontend.getNumberOfPages() -1) ;
        ServiceLocator.getInstance().getExplorerService().findMDocsByTag(workspaceId, tagLabel, startOffset, frontend.getPageSize(), callback);
    }

    public void setFrontend(PageManager pm) {
        frontend = pm ;
    }

    public TableModel getTableModel() {
        return model ;
    }

    private void notifyFrontend(MDocResponse result) {
        frontend.dataReady(result);
    }

    private class InternalCallback implements AsyncCallback<MDocResponse> {

        public void onFailure(Throwable caught) {
            HTMLUtil.showError(caught.getMessage());
        }

        public void onSuccess(MDocResponse result) {
            model = new MDocTableModel(result.getData(), login, true);
            notifyFrontend(result);
        }
    }
}
