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
        mNewDoc.setAction(pActionFactory.getCreateMDocAction());
        mEditDoc.setAction(pActionFactory.getEditElementAction());
        mCheckIn.setAction(pActionFactory.getCheckInAction());
        mCheckOut.setAction(pActionFactory.getCheckOutAction());
        mUndoCheckOut.setAction(pActionFactory.getUndoCheckOutAction());
        mViewDoc.setAction(pActionFactory.getViewElementAction());
        mRefresh.setAction(pActionFactory.getRefreshAction());
    }
}