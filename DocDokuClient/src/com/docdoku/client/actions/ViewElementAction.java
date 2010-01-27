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
