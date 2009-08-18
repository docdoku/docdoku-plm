/*
 * TableNavigatorPanel.java
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
package com.docdoku.gwt.explorer.client.ui;

import com.docdoku.gwt.explorer.client.data.ServiceLocator;
import com.docdoku.gwt.explorer.client.localization.ExplorerI18NConstants;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;

/**
 *
 * @author Emmanuel Nhan {@literal <emmanuel.nhan@insa-lyon.fr>}
 */
public class TableNavigatorPanel extends Composite implements TableListener {

    private HorizontalPanel mainPanel;
    private Label first;
    private Label last;
    private Label next;
    private Label previous;

    public TableNavigatorPanel(final Table table) {
        ExplorerI18NConstants constants = ServiceLocator.getInstance().getExplorerI18NConstants();
        mainPanel = new HorizontalPanel();
        first = new Label(constants.navigateBeginning());
        next = new Label(constants.navigateNext());
        last = new Label(constants.navigateEnd());
        previous = new Label(constants.navigatePrevious());
        mainPanel.add(first);
        mainPanel.add(previous);
        mainPanel.add(next);
        mainPanel.add(last);
        initWidget(mainPanel);

        first.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {
                table.showFirstPage();
            }
        });

        next.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {
                table.showNextPage();
            }
        });

        previous.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {
                table.showPreviousPage();
            }
        });

        last.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {
                table.showLastPage();
            }
        });

        first.setStyleName("normalLinkAction");
        last.setStyleName("normalLinkAction");
        next.setStyleName("normalLinkAction");
        previous.setStyleName("normalLinkAction");

        first.addStyleName("clickableLabel");
        last.addStyleName("clickableLabel");
        next.addStyleName("clickableLabel");
        previous.addStyleName("clickableLabel");


    }

    public void onPageChanged(TableEvent event) {
        Table t = event.getRealSource();
        first.setVisible(true);
        last.setVisible(true);
        previous.setVisible(true);
        next.setVisible(true);
        if (t.getNumberOfPages() != 0) {
            if (t.getCurrentPage() == 0) {
                first.setVisible(false);
                previous.setVisible(false);
            }

            if (t.getCurrentPage() == t.getNumberOfPages() - 1) {
                last.setVisible(false);
                next.setVisible(false);
            }
        } else {
            first.setVisible(false);
            last.setVisible(false);
            previous.setVisible(false);
            next.setVisible(false);
        }


    }
}
