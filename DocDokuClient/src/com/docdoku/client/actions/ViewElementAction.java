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

package com.docdoku.client.actions;

import com.docdoku.client.localization.I18N;
import com.docdoku.client.ui.template.ViewMDocTemplateDetailsDialog;
import com.docdoku.core.entities.MasterDocument;
import com.docdoku.core.entities.MasterDocumentTemplate;
import com.docdoku.core.entities.WorkflowModel;
import com.docdoku.client.ui.ExplorerFrame;
import com.docdoku.client.ui.workflow.ViewWorkflowModelDetailsDialog;
import com.docdoku.client.ui.doc.ViewDocDetailsDialog;

import javax.swing.*;
import java.awt.event.*;

public class ViewElementAction extends ClientAbstractAction {

    public ViewElementAction(ExplorerFrame pOwner) {
        super(I18N.BUNDLE.getString("ViewElementDetails_title"), "/com/docdoku/client/resources/icons/view.png", pOwner);
        putValue(Action.SHORT_DESCRIPTION, I18N.BUNDLE.getString("ViewElementDetails_short_desc"));
        putValue(Action.LONG_DESCRIPTION, I18N.BUNDLE.getString("ViewElementDetails_long_desc"));
        putValue(Action.MNEMONIC_KEY, new Integer(I18N.getCharBundle("ViewElementDetails_mnemonic_key")));
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0));
        setLargeIcon("/com/docdoku/client/resources/icons/view_large.png");
    }

    public void actionPerformed(ActionEvent pAE) {

        WorkflowModel wfModel = mOwner.getSelectedWorkflowModel();
        MasterDocument mdoc = mOwner.getSelectedMDoc();
        MasterDocumentTemplate template = mOwner.getSelectedMDocTemplate();
        if(mdoc !=null){
            ActionListener downloadAction = new DownloadActionListener();
            ActionListener openAction = new OpenActionListener();
            new ViewDocDetailsDialog(mOwner, mdoc.getLastIteration(), downloadAction, openAction);
        }else if(wfModel != null){
            new ViewWorkflowModelDetailsDialog(mOwner, wfModel);
        }else if(template != null){
            ActionListener downloadAction = new DownloadActionListener();
            ActionListener openAction = new OpenActionListener();
            new ViewMDocTemplateDetailsDialog(mOwner, template,downloadAction, openAction);
        }


    }
}
