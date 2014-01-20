/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2013 DocDoku SARL
 *
 * This file is part of DocDokuPLM.
 *
 * DocDokuPLM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DocDokuPLM is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with DocDokuPLM.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.docdoku.client.ui.common;

import com.docdoku.client.localization.I18N;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;

public class WebLink extends JLabel {

    public WebLink() {
        init();
    }

    public WebLink(String pLabel) {
        super("<html><a href=\"#\">" + pLabel + "</a>");
        init();
    }
    
    public WebLink(String pLabel, Icon icon) {
        super("<html><a href=\"#\">" + pLabel + "</a>",icon, JLabel.CENTER);
        setVerticalTextPosition(JLabel.BOTTOM);
        setHorizontalTextPosition(JLabel.CENTER);
        init();
    }



    public WebLink(String pLabel, String pTarget) {
        this(pLabel);
        try {
            setTargetLink(new URI(pTarget));
        } catch (Exception pEx) {
            System.err.println(pEx.getMessage());
        }
    }

    public WebLink(String pLabel, URI pTarget) {
        this(pLabel);
        setTargetLink(pTarget);
    }

    public void setLink(String pLabel, String pTarget) {
        setText("<html><a href=\"#\">" + pLabel + "</a>");
        try {
            setTargetLink(new URI(pTarget));
        } catch (Exception pEx) {
            System.err.println(pEx.getMessage());
        }
    }

    private void init() {
        addMouseListener(new MouseAdapter() {

            @Override
            public void mouseEntered(MouseEvent e) {
                setCursor(new Cursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        });
    }

    private void setTargetLink(final URI pTarget) {
        addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent pEvent) {
                try {
                    Desktop.getDesktop().browse(pTarget);
                } catch (Exception pEx) {
                    String message = pEx.getMessage() == null ? I18N.BUNDLE.getString("Error_unknown") : pEx.getMessage();
                    JOptionPane.showMessageDialog(null,
                            message, I18N.BUNDLE.getString("Error_title"),
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }
}
