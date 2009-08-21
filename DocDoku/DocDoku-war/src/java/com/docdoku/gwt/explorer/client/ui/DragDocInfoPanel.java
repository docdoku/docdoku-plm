/*
 * DragDocInfoPanel.java
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
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.InlineLabel;

/**
 *
 * @author Emmanuel Nhan {@literal <emmanuel.nhan@insa-lyon.fr>}
 */
public class DragDocInfoPanel extends Composite{
    
    private InlineLabel label ;

    public DragDocInfoPanel(int nb) {
        HorizontalPanel mainPanel = new HorizontalPanel() ;
        initWidget(mainPanel);
        setStyleName("dnd-infosPanel");
        mainPanel.addStyleName("dnd-onMove");
        label = new InlineLabel(nb + " "+ ServiceLocator.getInstance().getExplorerI18NConstants().documentsLabel());
        mainPanel.add(label);
    }


}
