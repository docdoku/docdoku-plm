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