/*
 * DocDoku, Professional Open Source
 * Copyright 2006, 2007, 2008, 2009, 2010, 2011, 2012 DocDoku SARL
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

import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;


public class HelpButton extends JLabel implements MouseListener{
   
    private String mText;
    private HelpTip mHelpTip;
    private boolean mState;
    
    private final static Icon ON =new ImageIcon(
                Toolkit.getDefaultToolkit().getImage(HelpButton.class.getResource(
                        "/com/docdoku/client/resources/icons/lightbulb_on.png")));
    private final static Icon OFF =new ImageIcon(
                Toolkit.getDefaultToolkit().getImage(HelpButton.class.getResource(
                        "/com/docdoku/client/resources/icons/lightbulb.png")));
    
    public HelpButton(String pText) {
        super(OFF);
        mText=pText;
        addMouseListener(this);
    }

    public void mouseClicked(MouseEvent e) {
       if(mHelpTip==null){
           mHelpTip=new HelpTip(this);
           mHelpTip.setText(mText);
           mHelpTip.addInternalFrameListener(new InternalFrameAdapter() {
               @Override
               public void internalFrameClosing(InternalFrameEvent e){
                   switchState();
               }
           });
       }
       switchState();     
    }

    private void switchState(){
       mState=!mState;
       mHelpTip.setVisible(mState);
       if(mState)
           setIcon(ON);
       else
           setIcon(OFF);
    }
    
    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }
    
}
