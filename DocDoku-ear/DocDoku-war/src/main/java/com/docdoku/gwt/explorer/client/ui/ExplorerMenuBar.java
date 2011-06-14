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

package com.docdoku.gwt.explorer.client.ui;

import com.docdoku.gwt.explorer.client.actions.Action;
import com.docdoku.gwt.explorer.client.data.ServiceLocator;
import com.docdoku.gwt.explorer.client.localization.ExplorerI18NConstants;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import java.util.Map;
import org.cobogw.gwt.user.client.ui.Button;

/**
 *
 * @author Emmanuel Nhan
 */
public class ExplorerMenuBar extends Composite{
    private Button m_deleteBtn;
    private ExplorerPage m_mainPage;
    private Label m_selectAll ;
    private Label m_selectNone ;
    private Map<String, Action> m_cmds;
    private final ExplorerI18NConstants i18n = ServiceLocator.getInstance().getExplorerI18NConstants();
    private HorizontalPanel buttonBar;
    private FlexTable mainPanel ;
    private boolean selectionTop ;

    public ExplorerMenuBar(final Map<String, Action> cmds, ExplorerPage mainPage, boolean selectionTop){
        mainPanel = new FlexTable() ;
        initWidget(mainPanel);
        setStyleName("myMenuBar");
        this.selectionTop = selectionTop;
        m_mainPage = mainPage;
        m_cmds = cmds;

        m_deleteBtn = new Button(i18n.actionRemove());
        m_deleteBtn.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {
                cmds.get("DeleteElementCommand").execute();
            }
        });


        buttonBar= new HorizontalPanel();

        m_selectAll = new Label(i18n.selectAllLabel());
        m_selectAll.setStyleName("clickableLabel");
        m_selectAll.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {
                m_mainPage.selectAllElementsInTable();
            }
        });
        m_selectNone = new Label(i18n.selectNoneLabel());
        m_selectNone.setStyleName("clickableLabel");
        m_selectNone.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {
                m_mainPage.unselectAllElementsInTable();
            }
        });
        HorizontalPanel selection = new HorizontalPanel();
        selection.add(new Label(i18n.selectLabel()+": "));
        selection.add(m_selectAll);
        selection.add(m_selectNone);
        
        buttonBar.add(m_deleteBtn);
        if (!selectionTop){
            mainPanel.setWidget(0, 0, buttonBar);
            mainPanel.setWidget(1, 0, selection);
        }else{
            mainPanel.setWidget(0, 0, selection);
            mainPanel.setWidget(1, 0, buttonBar);
        }
        

    }

    public void addExtension(Widget w) {
        if(!selectionTop){
            mainPanel.setWidget(0,1, w);
            mainPanel.getCellFormatter().setHorizontalAlignment(0, 1, HasHorizontalAlignment.ALIGN_RIGHT);
        }else{
            mainPanel.setWidget(1,1, w);
            mainPanel.getCellFormatter().setHorizontalAlignment(1, 1, HasHorizontalAlignment.ALIGN_RIGHT);
        }
    }

}
