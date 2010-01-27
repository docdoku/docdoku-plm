package com.docdoku.client.ui.common;


import javax.swing.*;

public class ButtonMenu extends JMenuItem {
    private JLabel mStatusLabel;

    public ButtonMenu(JLabel pStatusLabel) {
        mStatusLabel = pStatusLabel;
    }

    public void menuSelectionChanged(boolean pIsIncluded) {
        if (pIsIncluded) {
            Action action = getAction();
            if (action != null) {
                Object message = action.getValue(Action.LONG_DESCRIPTION);
                mStatusLabel.setText(message.toString());
            }
        } else
            mStatusLabel.setText(" ");

        super.menuSelectionChanged(pIsIncluded);
    }

}
