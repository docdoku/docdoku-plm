/*
 * MDocSearchBackend.java
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

import com.docdoku.gwt.explorer.client.data.MDocSearchTableModel;
import com.docdoku.gwt.explorer.client.data.MDocTableModel;
import com.docdoku.gwt.explorer.client.data.ServiceLocator;
import com.docdoku.gwt.client.ui.widget.table.TableModel;
import com.docdoku.gwt.explorer.client.util.HTMLUtil;
import com.docdoku.gwt.explorer.shared.InstanceAttributeDTO;
import com.docdoku.gwt.explorer.shared.MDocResponse;
import com.docdoku.gwt.explorer.shared.SearchQueryDTO;
import com.google.gwt.user.client.rpc.AsyncCallback;
import java.util.Date;

/*
 * TODO : do it better with a query object :) 
 *
 */
/**
 *
 * @author Emmanuel Nhan {@literal <emmanuel.nhan@insa-lyon.fr>}
 */
public class MDocSearchBackend implements PageManagerBackend{

    private MDocTableModel model ;
    private PageManager frontend ;
    private String login ;
    private InternalCallback callback ;

    // search :
    private String workspaceId ;
    private String mdocId;
    private String title;
    private String version ;
    private String author ;
    private String type ;
    private Date creationFrom;
    private Date creationTo ;
    private SearchQueryDTO.AbstractAttributeQueryDTO[] attributes;
    private String[] tags ;
    private String content ;

    public MDocSearchBackend(String login, String workspaceId, String mdocId, String title, String version, String author, String type, Date creationFrom, Date creationTo, SearchQueryDTO.AbstractAttributeQueryDTO[] attributes, String[] tags, String content) {
        this.login = login;
        this.workspaceId = workspaceId;
        this.mdocId = mdocId;
        this.title = title;
        this.version = version;
        this.author = author;
        this.type = type;
        this.creationFrom = creationFrom;
        this.creationTo = creationTo;
        this.attributes = attributes;
        this.tags = tags;
        this.content = content;

        callback = new InternalCallback() ;
    }

    
    

    public void fetchNextPage() {
        int startPoint = frontend.getOffsetForPageNumber(frontend.getCurrentPage() +1 ) ;
        ServiceLocator.getInstance().getExplorerService().searchMDocs(workspaceId, mdocId, title, version, author, type, creationFrom, creationTo, attributes, tags, content, startPoint, frontend.getPageSize(), callback);
    }

    public void fetchPreviousPage() {
        int startPoint = frontend.getOffsetForPageNumber(frontend.getCurrentPage() -1 ) ;
        ServiceLocator.getInstance().getExplorerService().searchMDocs(workspaceId, mdocId, title, version, author, type, creationFrom, creationTo, attributes, tags, content, startPoint, frontend.getPageSize(), callback);
    }

    public void fetchFirstPage() {   
        ServiceLocator.getInstance().getExplorerService().searchMDocs(workspaceId, mdocId, title, version, author, type, creationFrom, creationTo, attributes, tags, content, 0, frontend.getPageSize(), callback);
    }

    public void fetchLastPage() {
        int startPoint = frontend.getOffsetForPageNumber(frontend.getNumberOfPages() -1 ) ;
        ServiceLocator.getInstance().getExplorerService().searchMDocs(workspaceId, mdocId, title, version, author, type, creationFrom, creationTo, attributes, tags, content, startPoint, frontend.getPageSize(), callback);
    }

    public void setFrontend(PageManager pm) {
        frontend = pm ;
    }

    public TableModel getTableModel() {
        return model ;
    }

    private void notifyFrontend(MDocResponse response) {
        frontend.dataReady(response);
    }

    private class InternalCallback implements AsyncCallback<MDocResponse> {

        public void onFailure(Throwable caught) {
            HTMLUtil.showError(caught.getMessage());
        }

        public void onSuccess(MDocResponse result) {
            model = new MDocSearchTableModel(result.getData(), login, true) ;
            notifyFrontend(result);
        }
    }

}
