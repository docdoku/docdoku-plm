/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2013 DocDoku SARL
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

import com.docdoku.client.localization.I18N;
import java.awt.event.ActionListener;
import javax.swing.*;
import java.awt.*;

public class WorkflowModelToolBar extends JToolBar {
    private JToggleButton mSelectButton;
    private JToggleButton mSerialButton;
    private JToggleButton mParallelButton;
    private JButton mSaveAsButton;
    private ButtonGroup mGroup = new ButtonGroup();

    public static final Image SERIAL_IMAGE = Toolkit.getDefaultToolkit().getImage(WorkflowModelToolBar.class.getResource(
                        "/com/docdoku/client/resources/icons/branch_element_serial.png"));

    public static final Image PARALLEL_IMAGE =
                Toolkit.getDefaultToolkit().getImage(WorkflowModelToolBar.class.getResource(
                        "/com/docdoku/client/resources/icons/branch_element_parallel.png"));
    
    public static final Image SAVE_AS_IMAGE =
                Toolkit.getDefaultToolkit().getImage(WorkflowModelToolBar.class.getResource(
                        "/com/docdoku/client/resources/icons/disk_blue_window.png"));
    
    public enum BehaviorMode {EDIT, SERIAL, PARALLEL};

    public WorkflowModelToolBar(ActionListener pSaveAsWorkflowModelAction) {
        super(null,JToolBar.VERTICAL);
        ImageIcon saveAsIcon = new ImageIcon(SAVE_AS_IMAGE);
        mSaveAsButton = new JButton(saveAsIcon);
        mSaveAsButton.setToolTipText(I18N.BUNDLE.getString("SaveAs_button"));
        mSaveAsButton.addActionListener(pSaveAsWorkflowModelAction);
        createLayout();
        createListener();
        setVisible(true);
    }

    public BehaviorMode getSelectedMode(){
        String actionCommand = mGroup.getSelection().getActionCommand();
        if(actionCommand.equals("edit"))
            return BehaviorMode.EDIT;
        else if(actionCommand.equals("serial"))
            return BehaviorMode.SERIAL;
        else if(actionCommand.equals("parallel"))
            return BehaviorMode.PARALLEL;

        assert false;
        return null;
    }

    private void createLayout() {
        Image img =
                Toolkit.getDefaultToolkit().getImage(WorkflowModelToolBar.class.getResource(
                        "/com/docdoku/client/resources/icons/hand_point.png"));
        ImageIcon selectIcon = new ImageIcon(img);
        mSelectButton = new JToggleButton(selectIcon);
        mSelectButton.setToolTipText(I18N.BUNDLE.getString("EditActivity_button"));

        ImageIcon serialIcon = new ImageIcon(SERIAL_IMAGE);
        mSerialButton = new JToggleButton(serialIcon);
        mSerialButton.setToolTipText(I18N.BUNDLE.getString("AddSerialActivity_button"));

        ImageIcon parallelIcon = new ImageIcon(PARALLEL_IMAGE);
        mParallelButton = new JToggleButton(parallelIcon);
        mParallelButton.setToolTipText(I18N.BUNDLE.getString("AddParallelActivity_button"));
        
        

        add(mSelectButton);
        add(mParallelButton);
        add(mSerialButton);
        addSeparator();
        add(mSaveAsButton);

        mSelectButton.setActionCommand("edit");
        mParallelButton.setActionCommand("parallel");
        mSerialButton.setActionCommand("serial");

        mSelectButton.setSelected(true);
    }

    private void createListener() {
        mGroup.add(mSelectButton);
        mGroup.add(mSerialButton);
        mGroup.add(mParallelButton);
    }

}