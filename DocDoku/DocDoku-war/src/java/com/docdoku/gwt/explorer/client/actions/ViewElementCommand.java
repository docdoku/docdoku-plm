/*
 * DocDoku, Professional Open Source
 * Copyright 2006, 2007, 2008, 2009 DocDoku SARL
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
import com.docdoku.gwt.explorer.common.MasterDocumentDTO;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;

/**
 *
 * @author Florent GARIN
 */
//TODO REMOVE THIS
public class ViewElementCommand implements Command {

    private ExplorerPage m_mainPage;

    public ViewElementCommand(ExplorerPage mainPage) {
        m_mainPage = mainPage;
    }

    public void execute() {
        MasterDocumentDTO mdoc = m_mainPage.getLastOpenedMDoc();
        String workspaceId = mdoc.getWorkspaceId();
        String id = mdoc.getId();
        String version = mdoc.getVersion();

        String webContext = GWT.getModuleBaseURL().split("/")[3];
        String webURL;
        if (webContext.equals("gwt-files")) {
            webURL = GWT.getModuleBaseURL().substring(0, GWT.getModuleBaseURL().indexOf("/", 7));
        } else {
            webURL = GWT.getModuleBaseURL().substring(0, GWT.getModuleBaseURL().indexOf("/", 7)) + "/" + webContext;
        }

        String url = webURL + "/documents/" + workspaceId + "/" + id + "/" + version;
        Window.open(url, "_blank", "");

    }
}
