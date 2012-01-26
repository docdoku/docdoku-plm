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
import com.docdoku.gwt.explorer.shared.DocumentMasterDTO;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 *
 * @author Florent GARIN
 */
public class UploadCompleteDocFileCommand implements Action {

    private ExplorerPage m_mainPage;

    public UploadCompleteDocFileCommand(ExplorerPage mainPage) {
        m_mainPage = mainPage;
    }

    @Override
    public void execute(Object... userObject) {
        AsyncCallback<DocumentMasterDTO> callback = new AsyncCallback<DocumentMasterDTO>() {

            @Override
            public void onSuccess(DocumentMasterDTO docM) {
                DocumentDTO iteration = docM.getLastIteration();
                if(iteration!=null)
                    m_mainPage.setDocFiles(iteration.getAttachedFiles());
            }

            @Override
            public void onFailure(Throwable caught) {
                HTMLUtil.showError(caught.getMessage());
            }
        };
        DocumentMasterDTO m = m_mainPage.getLastOpenedDocM();
        ServiceLocator.getInstance().getExplorerService().getDocM(m.getWorkspaceId(), m.getId(), m.getVersion(), callback);

    }
}
