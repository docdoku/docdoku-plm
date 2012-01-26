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

import com.docdoku.gwt.explorer.client.data.ServiceLocator;
import com.docdoku.gwt.explorer.client.localization.ExplorerI18NConstants;
import com.docdoku.gwt.explorer.client.ui.pagemanager.PageHandler;
import com.docdoku.gwt.explorer.client.ui.pagemanager.PageManager;
import com.docdoku.gwt.explorer.client.ui.pagemanager.PageManagerEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;

/**
 *
 * @author Emmanuel Nhan {@literal <emmanuel.nhan@insa-lyon.fr>}
 */
public class TableNavigator extends Composite implements PageHandler{

    private Grid mainPanel ;

    private Label infos ;
    private Label first;
    private Label last;
    private Label next;
    private Label previous;

    public TableNavigator(final PageManager pm) {
        ExplorerI18NConstants constants = ServiceLocator.getInstance().getExplorerI18NConstants();
        mainPanel = new Grid(1, 5);
        infos = new Label() ;
        first = new Label(constants.navigateBeginning());
        next = new Label(constants.navigateNext());
        last = new Label(constants.navigateEnd());
        previous = new Label(constants.navigatePrevious());
        mainPanel.setWidget(0, 0, infos);
        mainPanel.setWidget(0,1,first);
        mainPanel.setWidget(0,2,previous);
        mainPanel.setWidget(0,3,next);
        mainPanel.setWidget(0,4,last);
        initWidget(mainPanel);

        infos.setVisible(false);
        first.setVisible(false);
        previous.setVisible(false);
        next.setVisible(false);
        last.setVisible(false);

        first.setStyleName("normalLinkAction");
        last.setStyleName("normalLinkAction");
        next.setStyleName("normalLinkAction");
        previous.setStyleName("normalLinkAction");

        first.addStyleName("clickableLabel");
        last.addStyleName("clickableLabel");
        next.addStyleName("clickableLabel");
        previous.addStyleName("clickableLabel");

        first.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {
                pm.fetchFirst();
            }
        });

        last.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {
                pm.fetchLast();
            }
        });

        previous.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {
                pm.fetchPrevious();
            }
        });

        next.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {
                pm.fetchNext();
            }
        });

    }

    public void onPageChanged(PageManagerEvent event) {
        // update visibility
        infos.setText(event.getStart()+" - "+event.getEnd() + " " + ServiceLocator.getInstance().getExplorerI18NConstants().ofDocumentsLabel() + " "+ event.getTotal()) ;
        first.setVisible(event.getCurrentPage() > 0);
        previous.setVisible(event.getCurrentPage() > 0);
        next.setVisible(event.getCurrentPage() < event.getNumberOfPages() -1);
        last.setVisible(event.getCurrentPage() < event.getNumberOfPages() -1);
        infos.setVisible(event.getNumberOfPages() > 1);
    }



    

}
