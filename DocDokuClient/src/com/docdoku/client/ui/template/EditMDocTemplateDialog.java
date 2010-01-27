package com.docdoku.client.ui.template;

import com.docdoku.client.localization.I18N;
import com.docdoku.client.ui.common.EditFilesPanel;
import com.docdoku.client.ui.common.OKCancelPanel;
import com.docdoku.core.entities.BinaryResource;
import com.docdoku.core.entities.InstanceAttributeTemplate;
import com.docdoku.core.entities.MasterDocumentTemplate;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.JPanel;


public class EditMDocTemplateDialog extends MDocTemplateDialog implements ActionListener{

    private MasterDocumentTemplate mTemplate;
    private EditMDocTemplatePanel mEditMDocTemplatePanel;
    private EditFilesPanel mEditFilesPanel;
    private EditAttributeTemplatesPanel mAttributesPanel;
    private ActionListener mAction;
    private OKCancelPanel mOKCancelPanel;
    
    public EditMDocTemplateDialog(Frame pOwner, ActionListener pOKAction, ActionListener pEditFileAction, ActionListener pAddAttributeAction, MasterDocumentTemplate pTemplate) {
        super(pOwner, I18N.BUNDLE.getString("EditMDocTemplate_title"));
        init(pOKAction, pEditFileAction, pAddAttributeAction, pTemplate);
    }

    public EditMDocTemplateDialog(Dialog pOwner, ActionListener pOKAction, ActionListener pEditFileAction, ActionListener pAddAttributeAction,MasterDocumentTemplate pTemplate) {
        super(pOwner, I18N.BUNDLE.getString("EditMDocTemplate_title"));
        init(pOKAction, pEditFileAction, pAddAttributeAction, pTemplate);
    }

    protected void init(ActionListener pOKAction, ActionListener pEditFileAction, ActionListener pAddAttributeAction, MasterDocumentTemplate pTemplate){
        mTemplate=pTemplate;
        mEditMDocTemplatePanel = new EditMDocTemplatePanel(mTemplate);
        mEditFilesPanel = new EditFilesPanel(pTemplate,pEditFileAction);
        mAttributesPanel = new EditAttributeTemplatesPanel(pTemplate, pAddAttributeAction);
        mAction = pOKAction;
        mOKCancelPanel = new OKCancelPanel(this, this);
        getRootPane().setDefaultButton(mOKCancelPanel.getOKButton());
        createLayout();
        setVisible(true);
    }

    public MasterDocumentTemplate getEditedTemplate(){
        return mTemplate;
    }
    
    public String getMask() {
        return mEditMDocTemplatePanel.getMask();
    }
    
    public String getDocumentType() {
        return mEditMDocTemplatePanel.getDocumentType();
    }
    public boolean isIdGenerated(){
        return mEditMDocTemplatePanel.isIdGenerated();
    }
    
    public Set<InstanceAttributeTemplate> getAttributeTemplates() {
        Set<InstanceAttributeTemplate> attrs = new HashSet<InstanceAttributeTemplate>();
        for(int i=0;i<mAttributesPanel.getAttributesListModel().getSize();i++){
            attrs.add((InstanceAttributeTemplate) mAttributesPanel.getAttributesListModel().get(i));
        }
        return attrs;
    }
    
    public Collection<BinaryResource> getFilesToRemove() {
        return mEditFilesPanel.getFilesToRemove();
    }
    
    public Collection<File> getFilesToAdd() {
        return mEditFilesPanel.getFilesToAdd();
    }
    
    public Map<BinaryResource, Long> getFilesToUpdate() {
        return mEditFilesPanel.getFilesToUpdate();
    }
    
    public void actionPerformed(ActionEvent pAE) {
        mAction.actionPerformed(new ActionEvent(this, 0, null));
    }
    
    protected JPanel getSouthPanel() {
        return mOKCancelPanel;
    }
    
    protected JPanel getFilesPanel() {
        return mEditFilesPanel;
    }
    
    protected JPanel getAttributesPanel() {
        return mAttributesPanel;
    }

    protected JPanel getMDocTemplatePanel() {
        return mEditMDocTemplatePanel;
    }
}
