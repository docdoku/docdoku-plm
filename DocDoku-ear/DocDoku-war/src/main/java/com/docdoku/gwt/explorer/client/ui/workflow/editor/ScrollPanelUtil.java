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

package com.docdoku.gwt.explorer.client.ui.workflow.editor;

import com.google.gwt.user.client.ui.ScrollPanel;

/**
 * This class aims to easily determine weather a position is visible in a scrollPanel
 *
 * An instance of ScrollPanelUtil is linked to the ScrollPanel.
 * 
 * @author Emmanuel Nhan {@literal <emmanuel.nhan@insa-lyon.fr>}
 */
public class ScrollPanelUtil {

    private ScrollPanel scrollPanel;

    public ScrollPanel getScrollPanel() {
        return scrollPanel;
    }

    public void setScrollPanel(ScrollPanel scrollPanel) {
        this.scrollPanel = scrollPanel;
    }

    /**
     *
     * @param x the x coordinate of the rect shape
     * @param y the y coordinate of the rect shape
     * @param width the width of the rect shape
     * @return
     */
    public boolean isOverScrollPanel(int x, int y, int width) {
        boolean leftSide = x > scrollPanel.getAbsoluteLeft() && x < scrollPanel.getAbsoluteLeft() + scrollPanel.getOffsetWidth();
        boolean rightSide = x + width > scrollPanel.getAbsoluteLeft() && x + width < scrollPanel.getAbsoluteLeft() + scrollPanel.getOffsetWidth();
        return leftSide && rightSide;
    }

    public int findAcceptableX(int i, int offsetWidth) {
        if (i < scrollPanel.getAbsoluteLeft()) {
            return scrollPanel.getAbsoluteLeft();
        } else if (i + offsetWidth > scrollPanel.getAbsoluteLeft() + scrollPanel.getOffsetWidth()) {
            return scrollPanel.getAbsoluteLeft() + scrollPanel.getOffsetWidth() - offsetWidth;
        }
        return i;
    }
}
