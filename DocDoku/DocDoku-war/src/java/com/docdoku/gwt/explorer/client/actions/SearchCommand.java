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
import com.docdoku.gwt.explorer.client.ui.pagemanager.MDocSearchBackend;
import com.docdoku.gwt.explorer.client.util.HTMLUtil;
import com.docdoku.gwt.explorer.common.InstanceAttributeDTO;
import com.docdoku.gwt.explorer.common.MasterDocumentDTO;
import com.google.gwt.user.client.rpc.AsyncCallback;
import java.util.Date;

/**
 *
 * @author Florent GARIN
 */
public class SearchCommand implements Action {

    private ExplorerPage m_mainPage;

    public SearchCommand(ExplorerPage mainPage) {
        m_mainPage = mainPage;
    }

    public void execute(Object... userObject) {
        String workspaceId= (String) userObject[0];
        String mdocId= (String) userObject[1];
        String title= (String) userObject[2];
        String version= (String) userObject[3];
        String author= (String) userObject[4];
        String type= (String) userObject[5];
        Date fromDate= (Date) userObject[6];
        Date toDate= (Date) userObject[7];
        InstanceAttributeDTO[] attributes= (InstanceAttributeDTO[]) userObject[8];
        String[] tags= (String[]) userObject[9];
        String content= (String) userObject[10];

         m_mainPage.showSearchResult(new MDocSearchBackend(m_mainPage.getLogin(), workspaceId, mdocId, title, version, author, type, fromDate, toDate, attributes, tags, content));

    }
}
