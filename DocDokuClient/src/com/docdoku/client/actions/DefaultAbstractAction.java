package com.docdoku.client.actions;

import com.docdoku.client.ui.common.GUIConstants;

import javax.swing.*;
import java.awt.*;

public abstract class DefaultAbstractAction extends AbstractAction {


    public DefaultAbstractAction(
            String pName,
            String pImgPath) {
        super(pName);
        Image img = Toolkit.getDefaultToolkit().getImage(DefaultAbstractAction.class.getResource(pImgPath));
        ImageIcon imgIcon = new ImageIcon(img);
        putValue(Action.SMALL_ICON, imgIcon);
    }


    public void setLargeIcon(Icon pIcon){
        putValue(GUIConstants.LARGE_ICON, pIcon);
    }

    public void setLargeIcon(String pIcon){
        Image img = Toolkit.getDefaultToolkit().getImage(DefaultAbstractAction.class.getResource(pIcon));
        ImageIcon imgIcon = new ImageIcon(img);
        putValue(GUIConstants.LARGE_ICON, imgIcon);
    }
}