package com.docdoku.client.ui.common;

import javax.swing.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class ButtonBar
        extends JButton
        implements MouseListener, FocusListener {
    private JLabel mStatusLabel;

    public ButtonBar(JLabel pStatusLabel) {
        mStatusLabel = pStatusLabel;
        setVerticalTextPosition(AbstractButton.BOTTOM);
        setHorizontalTextPosition(AbstractButton.CENTER);
        addFocusListener(this);
        addMouseListener(this);
    }

    public void mouseClicked(MouseEvent e) {
    };
    public void mousePressed(MouseEvent e) {
    };
    public void mouseReleased(MouseEvent e) {
    };
    public void mouseEntered(MouseEvent e) {
        switchOn();
    };
    public void mouseExited(MouseEvent e) {
        switchOff();
    };

    public void focusGained(FocusEvent pFE) {
        switchOn();
    }

    public void focusLost(FocusEvent e) {
        switchOff();
    };

    private void switchOn() {
        requestFocusInWindow();
        Action action = getAction();
        if (action != null) {
            Object message = action.getValue(Action.LONG_DESCRIPTION);
            mStatusLabel.setText(message.toString());
        }
    }

    private void switchOff() {
        mStatusLabel.setText(" ");
    }

    public void setAction(Action pAction) {
        super.setAction(pAction);
        Icon icon= (Icon) pAction.getValue(GUIConstants.LARGE_ICON);
        if(icon != null)
            setIcon(icon);
        //setText("");
    }
}
