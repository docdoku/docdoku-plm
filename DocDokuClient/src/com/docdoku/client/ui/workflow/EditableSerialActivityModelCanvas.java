package com.docdoku.client.ui.workflow;


import com.docdoku.client.ui.workflow.SerialActivityModelCanvas;

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
