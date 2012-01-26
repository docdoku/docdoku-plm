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

import com.docdoku.gwt.explorer.client.ui.ExplorerPage;
import com.docdoku.gwt.explorer.client.util.HTMLUtil;
import com.docdoku.gwt.explorer.shared.DocumentMasterTemplateDTO;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FormPanel;

/**
 *
 * @author Florent GARIN
 */
public class UploadTemplateFileCommand implements Action {

    private ExplorerPage m_mainPage;

    public UploadTemplateFileCommand(ExplorerPage mainPage) {
        m_mainPage = mainPage;
    }

    @Override
    public void execute(Object... userObject) {
        FileUpload upload = m_mainPage.getEditTemplateFilesPanel().getFileUpload();
        FormPanel form = m_mainPage.getEditTemplateFilesPanel().getForm();
        DocumentMasterTemplateDTO template = m_mainPage.getLastOpenedDocMTemplate();

        String webappContext = HTMLUtil.getWebContext();
        String url = "/" + (webappContext==null?"":webappContext+"/") + "files/" + URL.encode(template.getWorkspaceId()) + "/" + "templates/" + URL.encode(template.getId()) + "/" + URL.encode(upload.getFilename());
        form.setAction(url);
        form.submit();
    }
}
