package com.docdoku.client.ui.workflow;

import javax.swing.*;
import java.awt.*;


public class HorizontalSeparatorCanvas extends JPanel {

    private int mRank;

    public HorizontalSeparatorCanvas(int pRank){
        mRank=pRank;
        setPreferredSize(new Dimension(10, 81));
    }

    public int getRank(){
        return mRank;
    }

    protected void paintComponent(Graphics g) {
        int y = getHeight() / 2;
        g.fillRect(0, y, getWidth(), 2);
    }
}
