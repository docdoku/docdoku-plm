package com.docdoku.client.ui.workflow;

import javax.swing.*;
import java.awt.*;


public class TaskListCellRenderer extends DefaultListCellRenderer {
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        super.getListCellRendererComponent(list,value,index,isSelected,cellHasFocus);
        Image
                img =
                Toolkit.getDefaultToolkit().getImage(TaskListCellRenderer.class.getResource("/com/docdoku/client/resources/icons/clipboard.png"));
        ImageIcon userIcon = new ImageIcon(img);
        setIcon(userIcon);
        return this;
    }


}
