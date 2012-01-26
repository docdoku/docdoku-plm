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

package com.docdoku.gwt.explorer.client.ui;

import com.docdoku.gwt.explorer.client.actions.Action;
import com.docdoku.gwt.explorer.client.data.ServiceLocator;
import com.docdoku.gwt.explorer.client.localization.ExplorerI18NConstants;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import java.util.Map;
import org.cobogw.gwt.user.client.ui.Button;


/**
 *
 * @author Florent GARIN
 */
public class SearchPanel extends HorizontalPanel{

    private Button m_searchBtn;
    private TextBox m_fullTextSearch;
    private Label m_moreOptionsLink;
    private ExplorerPage m_mainPage ;
    
    public SearchPanel(final Map<String,Action> cmds, ExplorerPage mainPage){
        ExplorerI18NConstants explorerConstants=ServiceLocator.getInstance().getExplorerI18NConstants();
        m_mainPage = mainPage;
        m_fullTextSearch=new TextBox();
        m_moreOptionsLink=new Label(explorerConstants.moreSearchOptions());
        m_moreOptionsLink.setStyleName("smallLinkAction");
        m_moreOptionsLink.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {
                m_mainPage.showCompleteSearchPanel();
            }
        }) ;
        m_searchBtn=new Button(explorerConstants.actionSearch());
        m_searchBtn.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {
                cmds.get("SearchCommand").execute(m_mainPage.getWorkspaceId(), null,
                null, null, null, null,null,null,null,null, m_fullTextSearch.getText());
            }
        });
        createLayout();
    }

    private void createLayout() {
        setSpacing(5);
        add(m_fullTextSearch);
        add(m_searchBtn);
        add(m_moreOptionsLink);
    }
}
