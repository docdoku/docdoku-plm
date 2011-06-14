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

package com.docdoku.gwt.explorer.client;

import com.docdoku.gwt.explorer.client.actions.ActionMap;
import com.docdoku.gwt.explorer.client.ui.ExplorerPage;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.i18n.client.Dictionary;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootPanel;

/**
 *
 * @author Florent GARIN
 */


public class ExplorerEntryPoint implements EntryPoint {

    private Dictionary m_inputs;
    private ExplorerPage m_mainPage;
    public void onModuleLoad() {
    
        m_inputs = Dictionary.getDictionary("inputs");
        String workspaceID = m_inputs.get("workspaceID");
        String login = m_inputs.get("login");
        Window.setMargin("8px");
        ActionMap cmds=new ActionMap();
        m_mainPage=new ExplorerPage(workspaceID, login);
        cmds.init(m_mainPage);
        m_mainPage.init(cmds);
        RootPanel.get("content").add(m_mainPage);
       
    }
}
