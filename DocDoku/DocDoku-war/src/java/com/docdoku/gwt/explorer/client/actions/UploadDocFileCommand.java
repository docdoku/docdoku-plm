/*
 * DocDoku, Professional Open Source
 * Copyright 2006, 2007, 2008, 2009, 2010 DocDoku SARL
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
import com.docdoku.gwt.explorer.common.MasterDocumentDTO;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FormPanel;
import java.io.File;

/**
 *
 * @author Florent GARIN
 */
public class UploadDocFileCommand implements Action {

    private ExplorerPage m_mainPage;

    public UploadDocFileCommand(ExplorerPage mainPage) {
        m_mainPage = mainPage;
    }

    public void execute(Object... userObject) {
        FileUpload upload = m_mainPage.getEditDocFilesPanel().getFileUpload();
        FormPanel form = m_mainPage.getEditDocFilesPanel().getForm();
        MasterDocumentDTO mdoc = m_mainPage.getLastOpenedMDoc();
        //TODO make it relative
        String fileName=upload.getFilename();
        int index=fileName.lastIndexOf('/');
        if(index==-1)
            index=fileName.lastIndexOf('\\');

        if(index!=-1)
            fileName=fileName.substring(index+1);

        String webappContext = HTMLUtil.getWebContext();
        String url = "/" + (webappContext==null?"":webappContext+"/") + "files/" + URL.encode(mdoc.getWorkspaceId()) + "/" + "documents/" + URL.encode(mdoc.getId()) + "/" + mdoc.getVersion() + "/" + mdoc.getLastIteration().getIteration() + "/" + URL.encode(fileName);
        form.setAction(url);
        form.submit();
    }
}
