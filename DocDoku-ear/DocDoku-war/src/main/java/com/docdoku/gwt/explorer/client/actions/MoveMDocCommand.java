/*
 * DocDoku, Professional Open Source
 * Copyright 2006, 2007, 2008, 2009, 2010, 2011 DocDoku SARL
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
import com.docdoku.gwt.explorer.client.ui.ExplorerPage;
import com.docdoku.gwt.explorer.client.util.HTMLUtil;
import com.docdoku.gwt.explorer.shared.MasterDocumentDTO;
import com.google.gwt.user.client.rpc.AsyncCallback;
import java.util.List;

/**
 *
 * @author manu
 */
public class MoveMDocCommand implements Action {

    private ExplorerPage mainPage;

    MoveMDocCommand(ExplorerPage mainPage) {
        this.mainPage = mainPage;
    }

    public void execute(Object... userObject) {
        // params :
        // userObject[0] : new folder
        // selected docs to delete are retrieved with the main page
        List<MasterDocumentDTO> selectedDocs = mainPage.getSelectedMDocs();
        for (MasterDocumentDTO doc : selectedDocs) {
            AsyncCallback<MasterDocumentDTO> callback = new AsyncCallback<MasterDocumentDTO>() {

                public void onFailure(Throwable caught) {
                    HTMLUtil.showError(caught.getMessage());
                }

                public void onSuccess(MasterDocumentDTO result) {
                    mainPage.refreshElementTable();
                }
            };
            ServiceLocator.getInstance().getExplorerService().moveMDoc((String) userObject[0], mainPage.getWorkspaceId(), doc.getId(), doc.getVersion(), callback);
        }
    }
}
