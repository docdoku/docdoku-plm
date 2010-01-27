package com.docdoku.client.ui.template;

import com.docdoku.client.localization.I18N;
import com.docdoku.client.ui.common.CloseButton;
import com.docdoku.client.ui.common.EditFilesPanel;
import com.docdoku.client.ui.common.ViewFilesPanel;
import com.docdoku.core.entities.MasterDocumentTemplate;

import java.awt.*;
import java.awt.event.ActionListener;
import javax.swing.JPanel;


public class ViewMDocTemplateDetailsDialog extends MDocTemplateDialog{

    private CloseButton mCloseButton;
    private ViewFilesPanel mFilesPanel;
    private ViewAttributesPanel mAttributesPanel;
    private ViewMDocTemplatePanel mMDocTemplatePanel;
            
    public ViewMDocTemplateDetailsDialog(Frame pOwner, MasterDocumentTemplate pTemplate, ActionListener pDownloadAction, ActionListener pOpenAction) {
        super(pOwner, I18N.BUNDLE.getString("ViewMDocTemplateDetailsDialog_title"));
        init(pTemplate, pDownloadAction, pOpenAction);
    }

    public ViewMDocTemplateDetailsDialog(Dialog pOwner, MasterDocumentTemplate pTemplate, ActionListener pDownloadAction, ActionListener pOpenAction) {
        super(pOwner, I18N.BUNDLE.getString("ViewMDocTemplateDetailsDialog_title"));
        init(pTemplate, pDownloadAction, pOpenAction);
    }

    protected void init(MasterDocumentTemplate pTemplate, ActionListener pDownloadAction, ActionListener pOpenAction){
        mMDocTemplatePanel = new ViewMDocTemplatePanel(pTemplate);
        mFilesPanel = new ViewFilesPanel(pTemplate,pDownloadAction,pOpenAction);
        mAttributesPanel = new ViewAttributesPanel(pTemplate);
        createLayout();
        setVisible(true);
    }
    
    protected JPanel getSouthPanel() {
        mCloseButton = new CloseButton(this, I18N.BUNDLE.getString("Close_button"));
        JPanel southPanel = new JPanel();
        southPanel.add(mCloseButton);
        return southPanel;
    }
    
    protected JPanel getFilesPanel() {
        return mFilesPanel;
    }
    
    protected JPanel getAttributesPanel() {
        return mAttributesPanel;
    }

    protected JPanel getMDocTemplatePanel() {
        return mMDocTemplatePanel;
    }
}
