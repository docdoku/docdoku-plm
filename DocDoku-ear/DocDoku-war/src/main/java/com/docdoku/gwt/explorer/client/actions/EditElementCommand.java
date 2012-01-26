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

package com.docdoku.gwt.explorer.client.actions;

import com.docdoku.gwt.explorer.client.data.ServiceLocator;
import com.docdoku.gwt.explorer.client.ui.ExplorerPage;
import com.docdoku.gwt.explorer.client.util.HTMLUtil;
import com.docdoku.gwt.explorer.shared.DocumentDTO;
import com.docdoku.gwt.explorer.shared.InstanceAttributeDTO;
import com.docdoku.gwt.explorer.shared.DocumentMasterDTO;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 *
 * @author Florent GARIN
 */
public class EditElementCommand implements Action {

    private ExplorerPage m_mainPage;
    
    public EditElementCommand(ExplorerPage mainPage) {
        m_mainPage = mainPage;
    }

    public void execute(Object... userObject) {
        DocumentMasterDTO docM=m_mainPage.getLastOpenedDocM();
        String workspaceId=docM.getWorkspaceId();
        String id=docM.getId();
        String version=docM.getVersion();

        int iteration =docM.getLastIteration().getIteration();
        String revisionNote=(String) userObject[0];
        InstanceAttributeDTO[] attributes=(InstanceAttributeDTO[]) userObject[1];
        DocumentDTO[] links=(DocumentDTO[]) userObject[2];

        AsyncCallback<DocumentMasterDTO> callback = new AsyncCallback<DocumentMasterDTO>() {

            public void onSuccess(DocumentMasterDTO result) {
                m_mainPage.refreshElementTable();
                m_mainPage.showTablePanel();
            }

            public void onFailure(Throwable caught) {
                HTMLUtil.showError(caught.getMessage());
            }
        };
        
        ServiceLocator.getInstance().getExplorerService().updateDoc(workspaceId, id, version, iteration,revisionNote, attributes, links, callback);

        
    }

}
