/*
 * DocDoku, Professional Open Source
 * Copyright 2006, 2007, 2008, 2009, 2010, 2011 DocDoku SARL
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

package com.docdoku.client.actions;

import com.docdoku.client.ui.ExplorerFrame;
import com.docdoku.client.ui.workflow.SaveWorkflowModelDialog;
import com.docdoku.client.ui.workflow.WorkflowModelFrame;
import com.docdoku.core.workflow.WorkflowModel;

import javax.swing.*;
import java.awt.event.*;

import com.docdoku.client.localization.I18N;

public class SaveAsWorkflowModelActionListener implements ActionListener {
    
    
    public void actionPerformed(ActionEvent pAE) {
        final WorkflowModelFrame owner = (WorkflowModelFrame) pAE.getSource();
        final WorkflowModel model = owner.getWorkflowModel();
        ActionListener action = new ActionListener() {
            public void actionPerformed(ActionEvent pAE) {
                SaveWorkflowModelDialog source = (SaveWorkflowModelDialog) pAE.getSource();
                String id = source.getWorkflowModelId();
                model.setId(id);
                try{
                    MainController.getInstance().saveWorkflowModel(model);
                    owner.setTitle(source.getWorkflowModelId());
                    ExplorerFrame.unselectElementInAllFrame();
                }catch (Exception pEx) {
                    String message = pEx.getMessage()==null?I18N.BUNDLE
                            .getString("Error_unknown"):pEx.getMessage();
                    JOptionPane.showMessageDialog(null,
                            message, I18N.BUNDLE
                            .getString("Error_title"),
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        new SaveWorkflowModelDialog(owner, model, action);
    }
}
