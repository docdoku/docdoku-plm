/*
 * DocDoku, Professional Open Source
 * Copyright 2006, 2007, 2008, 2009, 2010 DocDoku SARL
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

package com.docdoku.client.ui;


import javax.swing.*;

public class ExplorerPopupMenu extends JPopupMenu {

    public ExplorerPopupMenu() {
        super("PopupMenu");

    }

    public void setActions(ActionFactory pActionFactory) {
        add(pActionFactory.getCreateMDocAction());
        add(pActionFactory.getCreateFolderAction());
        addSeparator();
        add(pActionFactory.getCheckInAction());
        add(pActionFactory.getCheckOutAction());
        add(pActionFactory.getUndoCheckOutAction());
        addSeparator();
        add(pActionFactory.getDeleteElementAction());
        add(pActionFactory.getEditElementAction());

    }
}