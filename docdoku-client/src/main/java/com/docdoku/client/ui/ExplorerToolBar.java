/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2013 DocDoku SARL
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

import com.docdoku.client.ui.common.ButtonBar;

import javax.swing.*;

public class ExplorerToolBar extends JToolBar {

    private ButtonBar mNewDoc;
    private ButtonBar mEditDoc;
    private ButtonBar mCheckIn;
    private ButtonBar mCheckOut;
    private ButtonBar mUndoCheckOut;
    private ButtonBar mViewDoc;
    private ButtonBar mRefresh;


    public ExplorerToolBar(JLabel pStatusLabel) {
        super("ToolBar");

        mNewDoc = new ButtonBar(pStatusLabel);
        mEditDoc = new ButtonBar(pStatusLabel);
        mCheckIn = new ButtonBar(pStatusLabel);
        mCheckOut = new ButtonBar(pStatusLabel);
        mUndoCheckOut = new ButtonBar(pStatusLabel);
        mViewDoc = new ButtonBar(pStatusLabel);
        mRefresh = new ButtonBar(pStatusLabel);
        setRollover(true);

        add(mNewDoc);
        add(mEditDoc);
        addSeparator();

        add(mCheckIn);
        add(mCheckOut);
        add(mUndoCheckOut);
        addSeparator();

        add(mViewDoc);
        addSeparator();

        add(mRefresh);
    }

    public void setActions(ActionFactory pActionFactory) {
        mNewDoc.setAction(pActionFactory.getCreateDocMAction());
        mEditDoc.setAction(pActionFactory.getEditElementAction());
        mCheckIn.setAction(pActionFactory.getCheckInAction());
        mCheckOut.setAction(pActionFactory.getCheckOutAction());
        mUndoCheckOut.setAction(pActionFactory.getUndoCheckOutAction());
        mViewDoc.setAction(pActionFactory.getViewElementAction());
        mRefresh.setAction(pActionFactory.getRefreshAction());
    }
}