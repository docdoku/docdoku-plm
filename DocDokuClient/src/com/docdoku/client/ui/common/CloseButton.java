package com.docdoku.client.ui.common;


import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class CloseButton extends JButton {

    public CloseButton(final Window pWindow, String pLabel) {
        super(pLabel);
        Image img = Toolkit.getDefaultToolkit().getImage(CloseButton.class.getResource("/com/docdoku/client/resources/icons/stop.png"));
        ImageIcon closeIcon = new ImageIcon(img);
        setIcon(closeIcon);
        addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent pAE) {
                pWindow.dispose();
            }
        });
    }
}
