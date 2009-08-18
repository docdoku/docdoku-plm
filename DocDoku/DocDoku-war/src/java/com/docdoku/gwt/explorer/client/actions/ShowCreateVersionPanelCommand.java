/*
 * ShowCreateVersionPanelCommand.java
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

package com.docdoku.gwt.explorer.client.actions;

import com.docdoku.gwt.explorer.client.ui.ExplorerPage;

/**
 *
 * @author Emmanuel Nhan {@literal <emmanuel.nhan@insa-lyon.fr>}
 */
public class ShowCreateVersionPanelCommand implements Action{

    private ExplorerPage m_mainPage ;

    ShowCreateVersionPanelCommand(ExplorerPage mainPage) {
        m_mainPage = mainPage;
    }

    public void execute(Object... userObject) {
        String workspaceId = (String) userObject[0] ;
        String mdocId = (String) userObject[1] ;
        String version = (String) userObject[2] ;

        m_mainPage.showCreateVersionPanel(workspaceId, mdocId, version);


    }

}
