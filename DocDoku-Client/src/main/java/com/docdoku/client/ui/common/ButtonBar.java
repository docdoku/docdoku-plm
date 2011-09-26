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

    @Override
    public void setAction(Action pAction) {
        super.setAction(pAction);
        Icon icon= (Icon) pAction.getValue(GUIConstants.LARGE_ICON);
        if(icon != null)
            setIcon(icon);
        //setText("");
    }
}
