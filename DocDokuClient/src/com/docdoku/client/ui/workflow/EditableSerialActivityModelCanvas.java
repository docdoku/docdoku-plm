/*
 * DocDoku, Professional Open Source
 * Copyright 2006, 2007, 2008, 2009, 2010 DocDoku SARL
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

package com.docdoku.client.ui.workflow;



import com.docdoku.core.entities.SerialActivityModel;
import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.BorderFactory;


public class EditableSerialActivityModelCanvas extends SerialActivityModelCanvas implements MouseListener {


    public EditableSerialActivityModelCanvas(SerialActivityModel pActivity) {
        super(pActivity);
        createLayout();
        setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
        addMouseListener(this);
    }                                        

    public void setSerialActivityModel(SerialActivityModel pActivity) {
        mActivityModel=pActivity;
    }

    public void refresh(){
        removeAll();
        createLayout();
        revalidate();
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
        setBorder(BorderFactory.createLineBorder(Color.GRAY,2));
    }

    public void mouseExited(MouseEvent e) {
        setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
    }
}
