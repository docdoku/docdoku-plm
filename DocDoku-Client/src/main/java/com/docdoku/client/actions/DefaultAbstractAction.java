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

package com.docdoku.client.actions;

import com.docdoku.client.ui.common.GUIConstants;

import javax.swing.*;
import java.awt.*;

public abstract class DefaultAbstractAction extends AbstractAction {


    public DefaultAbstractAction(
            String pName,
            String pImgPath) {
        super(pName);
        Image img = Toolkit.getDefaultToolkit().getImage(DefaultAbstractAction.class.getResource(pImgPath));
        ImageIcon imgIcon = new ImageIcon(img);
        putValue(Action.SMALL_ICON, imgIcon);
    }


    public void setLargeIcon(Icon pIcon){
        putValue(GUIConstants.LARGE_ICON, pIcon);
    }

    public void setLargeIcon(String pIcon){
        Image img = Toolkit.getDefaultToolkit().getImage(DefaultAbstractAction.class.getResource(pIcon));
        ImageIcon imgIcon = new ImageIcon(img);
        putValue(GUIConstants.LARGE_ICON, imgIcon);
    }
}