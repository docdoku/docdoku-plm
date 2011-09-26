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

import com.docdoku.client.localization.I18N;
import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JInternalFrame;
import javax.swing.JLayeredPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;


public class HelpTip extends JInternalFrame {
    
    private JTextArea mTextArea;
    private Component mAttachedComponent;
    private final static Color BACKGROUND_COLOR=new Color(255,255,224);
    
    public HelpTip(Component pAttachedComponent) {
        super(I18N.BUNDLE.getString("Help_title"),true,true);
        mAttachedComponent = pAttachedComponent;
        mTextArea = new JTextArea();
        mTextArea.setColumns(20);
        mTextArea.setRows(8);
        mTextArea.setLineWrap(true);
        mTextArea.setWrapStyleWord(true);
        mTextArea.setEditable(false);
        mTextArea.setBackground(BACKGROUND_COLOR);
        JScrollPane scrollPane = new JScrollPane(mTextArea);
        add(scrollPane);
        
        Icon icon =new ImageIcon(
                Toolkit.getDefaultToolkit().getImage(HelpTip.class.getResource(
                        "/com/docdoku/client/resources/icons/lightbulb_on.png")));
        setFrameIcon(icon);
        JDialog owner = (JDialog) SwingUtilities.getAncestorOfClass(JDialog.class, mAttachedComponent);
        owner.getLayeredPane().add(this, JLayeredPane.POPUP_LAYER);
        
        createListener();
    }
    
    private void createListener(){
        mAttachedComponent.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentMoved(ComponentEvent e) {
                if (isShowing()) {
                    computeAndSetLocation();
                }
            }
        });
    }
    
    private void computeAndSetLocation() {
        Point location = mAttachedComponent.getLocation();
        setLocation(location.x-getPreferredSize().width,location.y + mAttachedComponent.getSize().height);
    }
    
    public void setText(String text) {
        mTextArea.setText(text);
        pack();
    }

    @Override
    public void setVisible(boolean show) {
        if (show) {
            computeAndSetLocation();
        }
        super.setVisible(show);
    }
    
}
