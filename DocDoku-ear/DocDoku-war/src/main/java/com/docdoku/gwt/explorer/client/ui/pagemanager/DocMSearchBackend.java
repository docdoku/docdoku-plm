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

import com.docdoku.gwt.explorer.client.data.DocMSearchTableModel;
import com.docdoku.gwt.explorer.client.data.DocMTableModel;
import com.docdoku.gwt.explorer.client.data.ServiceLocator;
import com.docdoku.gwt.client.ui.widget.table.TableModel;
import com.docdoku.gwt.explorer.client.util.HTMLUtil;
import com.docdoku.server.rest.dto.InstanceAttributeDTO;
import com.docdoku.gwt.explorer.shared.DocMResponse;
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
public class DocMSearchBackend implements PageManagerBackend{

    private DocMTableModel model;
    private PageManager frontend;
    private String login ;
    private InternalCallback callback;

    // search :
    private String workspaceId;
    private String docMId;
    private String title;
    private String version;
    private String author;
    private String type;
    private Date creationFrom;
    private Date creationTo;
    private SearchQueryDTO.AbstractAttributeQueryDTO[] attributes;
    private String[] tags;
    private String content;

    public DocMSearchBackend(String login, String workspaceId, String docMId, String title, String version, String author, String type, Date creationFrom, Date creationTo, SearchQueryDTO.AbstractAttributeQueryDTO[] attributes, String[] tags, String content) {
        this.login = login;
        this.workspaceId = workspaceId;
        this.docMId = docMId;
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
        ServiceLocator.getInstance().getExplorerService().searchDocMs(workspaceId, docMId, title, version, author, type, creationFrom, creationTo, attributes, tags, content, startPoint, frontend.getPageSize(), callback);
    }

    public void fetchPreviousPage() {
        int startPoint = frontend.getOffsetForPageNumber(frontend.getCurrentPage() -1 ) ;
        ServiceLocator.getInstance().getExplorerService().searchDocMs(workspaceId, docMId, title, version, author, type, creationFrom, creationTo, attributes, tags, content, startPoint, frontend.getPageSize(), callback);
    }

    public void fetchFirstPage() {   
        ServiceLocator.getInstance().getExplorerService().searchDocMs(workspaceId, docMId, title, version, author, type, creationFrom, creationTo, attributes, tags, content, 0, frontend.getPageSize(), callback);
    }

    public void fetchLastPage() {
        int startPoint = frontend.getOffsetForPageNumber(frontend.getNumberOfPages() -1 ) ;
        ServiceLocator.getInstance().getExplorerService().searchDocMs(workspaceId, docMId, title, version, author, type, creationFrom, creationTo, attributes, tags, content, startPoint, frontend.getPageSize(), callback);
    }

    public void setFrontend(PageManager pm) {
        frontend = pm ;
    }

    public TableModel getTableModel() {
        return model ;
    }

    private void notifyFrontend(DocMResponse response) {
        frontend.dataReady(response);
    }

    private class InternalCallback implements AsyncCallback<DocMResponse> {

        public void onFailure(Throwable caught) {
            HTMLUtil.showError(caught.getMessage());
        }

        public void onSuccess(DocMResponse result) {
            model = new DocMSearchTableModel(result.getData(), login, true) ;
            notifyFrontend(result);
        }
    }

}
