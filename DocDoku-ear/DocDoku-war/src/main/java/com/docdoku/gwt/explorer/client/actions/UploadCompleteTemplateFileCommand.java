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
import com.docdoku.server.rest.dto.DocumentMasterTemplateDTO;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 *
 * @author Florent GARIN
 */
public class UploadCompleteTemplateFileCommand implements Action {

    private ExplorerPage m_mainPage;

    public UploadCompleteTemplateFileCommand(ExplorerPage mainPage) {
        m_mainPage = mainPage;
    }

    @Override
    public void execute(Object... userObject) {
        AsyncCallback<DocumentMasterTemplateDTO> callback = new AsyncCallback<DocumentMasterTemplateDTO>() {

            @Override
            public void onSuccess(DocumentMasterTemplateDTO template) {
                m_mainPage.setEditTemplateFiles(template.getAttachedFiles());
            }

            @Override
            public void onFailure(Throwable caught) {
                HTMLUtil.showError(caught.getMessage());
            }
        };
        DocumentMasterTemplateDTO template = m_mainPage.getLastOpenedDocMTemplate();
        ServiceLocator.getInstance().getExplorerService().getDocMTemplate(template.getWorkspaceId(), template.getId(), callback);

    }
}
