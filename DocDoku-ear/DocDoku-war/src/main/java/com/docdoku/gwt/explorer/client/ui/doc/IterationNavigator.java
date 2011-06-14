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

package com.docdoku.gwt.explorer.client.ui.doc;

import com.docdoku.gwt.explorer.client.actions.Action;
import com.docdoku.gwt.explorer.client.data.ServiceLocator;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import org.cobogw.gwt.user.client.ui.RoundedPanel;

/**
 * IterationNavigator provides a panel to navigate between iterations
 * @author Emmanuel Nhan {@literal <emmanuel.nhan@insa-lyon.fr>}
 */
public class IterationNavigator extends Composite {

    private HorizontalPanel mainPanel;
    private HorizontalPanel iterationsPanel;
    private int currentIteration;
    private Image navigatePrevious;
    private Image navigateNext;
    private final Action action ;

    public IterationNavigator(final Action action) {
        this.action = action;
        setupUi();
        navigateNext.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {
                action.execute(currentIteration + 1);
            }
        });

        navigatePrevious.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {
                action.execute(currentIteration - 1);
            }
        });
    }

    private void setupUi() {
        mainPanel = new HorizontalPanel();
        initWidget(mainPanel);
        mainPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);

        navigatePrevious = new Image(ServiceLocator.getInstance().getExplorerImageBundle().getLeftImage());
        navigateNext = new Image(ServiceLocator.getInstance().getExplorerImageBundle().getRightImage());

        iterationsPanel = new HorizontalPanel();


        mainPanel.add(navigatePrevious);
        mainPanel.add(iterationsPanel);
        mainPanel.add(navigateNext);
        

    }

    public void setIterationsNumber(int i, int last) {
        iterationsPanel.clear();
        navigateNext.setVisible(i != last);
        navigatePrevious.setVisible(i != 0);
        int inf ;
        int sup ;
        if (last <= 5){
            inf = 0 ;
            sup = last ;
        }else{
            if ( i < 2){
                inf = 0 ;
                sup = 4 ;
            }else if (i > last -2){
                inf = last -4 ;
                sup = last ;
            }else{
                inf = i-2 ;
                sup = i+2 ;
            }
        }

        for (int j = inf; j <= sup; j++) {
            final int k = j ;
            RoundedPanel rp = new RoundedPanel(RoundedPanel.ALL, 5);

            Label l = null;
            if (j != last) {
                l = new Label(ServiceLocator.getInstance().getExplorerI18NConstants().iterationLabel() + (j+1));
            } else {
                l = new Label(ServiceLocator.getInstance().getExplorerI18NConstants().currentIterationLabel());
            }
            l.setStyleName("iteration-Element");
            if (i ==j){
                l.addStyleName("emphasis");
            }

            l.addClickHandler(new ClickHandler() {

                public void onClick(ClickEvent event) {
                    action.execute(k);
                }
            });
            
            rp.add(l);
            rp.setCornerStyleName("iteration-Corner");
            rp.setStyleName("iteration-RP");

            iterationsPanel.add(rp);

        }


        currentIteration = i;



    }

    public void addNavigateNextHandler(ClickHandler handler) {
        navigateNext.addClickHandler(handler);
    }

    public void addNavigatePreviousHandler(ClickHandler handler) {
        navigateNext.addClickHandler(handler);
    }
}
