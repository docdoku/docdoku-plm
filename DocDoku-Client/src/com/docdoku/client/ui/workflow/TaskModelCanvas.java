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

import com.docdoku.client.localization.I18N;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Toolkit;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import com.docdoku.client.ui.common.GUIConstants;
import com.docdoku.core.entities.TaskModel;


public class TaskModelCanvas extends JPanel{
    
    private TaskModel mTaskModel;
    private final static ImageIcon TASK_ICON;
    
    static {
        Image img = Toolkit.getDefaultToolkit().getImage(TaskModelCanvas.class.getResource("/com/docdoku/client/resources/icons/clipboard_large.png"));
        TASK_ICON = new ImageIcon(img);
    }
    
    public TaskModelCanvas(TaskModel pTaskModel) {
        mTaskModel=pTaskModel;
        createLayout();
    }
    
    private void createLayout(){
        
        setLayout(new GridBagLayout());
        JLabel titleLabel = new JLabel(mTaskModel.getTitle(), TASK_ICON, SwingConstants.CENTER);
        titleLabel.setToolTipText(mTaskModel.getInstructions());
        titleLabel.setVerticalTextPosition(AbstractButton.BOTTOM);
        titleLabel.setHorizontalTextPosition(AbstractButton.CENTER);
        
        JLabel workerValueLabel = new JLabel(mTaskModel.getWorker().getName());
        workerValueLabel.setToolTipText(mTaskModel.getWorker().getEmail());
        
        
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.weighty = 0;
        constraints.insets = GUIConstants.INSETS;
        constraints.gridheight = 1;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weightx = 0;
        constraints.fill = GridBagConstraints.NONE;
        add(titleLabel, constraints);
        
        constraints.gridwidth = 1;
        constraints.gridy = 1;
        add(new JLabel(I18N.BUNDLE.getString("Worker_label")), constraints);
        
        constraints.gridx = 1;
        add(workerValueLabel, constraints);
        
        setBorder(BorderFactory.createEtchedBorder());
    }
}
