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
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;


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
