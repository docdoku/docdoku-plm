package com.docdoku.client.ui.common;


import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class OKButton extends JButton {

    public OKButton(String pLabel) {
        super(pLabel);
        Image img = Toolkit.getDefaultToolkit().getImage(OKButton.class.getResource("/com/docdoku/client/resources/icons/check.png"));
        ImageIcon closeIcon = new ImageIcon(img);
        setIcon(closeIcon);
    }

    public OKButton(final Window pFrame, String pLabel) {
        this(pLabel);
        addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent pAE) {
                pFrame.dispose();
            }
        });
    }
}
