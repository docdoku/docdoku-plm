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

package com.docdoku.gwt.explorer.client.ui.widget;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import org.cobogw.gwt.user.client.ui.RoundedPanel;

/**
 *
 * @author Florent Garin
 */
public class DataRoundedPanel extends Composite {

    protected FlexTable inputPanel;
    protected RoundedPanel rp;
    protected Label headerLabel;
    protected VerticalPanel vp;

    private final static int HEADER_HEIGHT=22;
    
    public DataRoundedPanel() {

        rp = new RoundedPanel(RoundedPanel.ALL);
        rp.setCornerStyleName("my-Input-Zone-Corner");
        rp.setStyleName("my-Input-Zone-RP");
        inputPanel = new FlexTable();
        inputPanel.setCellSpacing(8);
        inputPanel.setStyleName("my-Input-Zone-Element");
        rp.setWidget(inputPanel);
        //rp.setHeight("100%");
        initWidget(rp);
        //setHeight("100%");
    }

    public void setHeight(int height){
        if(getWidget()==rp){
            inputPanel.setHeight(height+"px");
        }
        else{
            rp.setHeight(HEADER_HEIGHT+"px");
            inputPanel.setHeight((height-HEADER_HEIGHT)+"px");
        }
            
    }
    
    public DataRoundedPanel(String header, int corners) {
        
        vp = new VerticalPanel();
        rp = new RoundedPanel(corners);
        rp.setStyleName("my-Input-Zone-RP-Advanced");
        headerLabel=new Label(header);
        headerLabel.setStyleName("my-Input-Zone-Header");
        rp.setWidget(headerLabel);
        rp.setCornerStyleName("my-Input-Zone-Corner");

        inputPanel = new FlexTable();
        inputPanel.setCellSpacing(8);
        inputPanel.setStyleName("my-Input-Zone-Element-Advanced");

        vp.add(rp);
        vp.add(inputPanel);
        vp.setStyleName("my-Input-Zone");
        //vp.setHeight("100%");
        initWidget(vp);
        //setHeight("100%");
        
    }
    public DataRoundedPanel(String header) {
        this(header,RoundedPanel.TOP);
    }
}
