/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2013 DocDoku SARL
 *
 * This file is part of DocDokuPLM.
 *
 * DocDokuPLM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DocDokuPLM is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with DocDokuPLM.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.docdoku.client.ui.template;

import com.docdoku.client.localization.I18N;
import com.docdoku.client.ui.common.EditFilesPanel;
import com.docdoku.client.ui.common.OKCancelPanel;
import com.docdoku.core.common.BinaryResource;
import com.docdoku.core.document.DocumentMasterTemplate;
import com.docdoku.core.meta.InstanceAttributeTemplate;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class EditDocMTemplateDialog extends DocMTemplateDialog implements ActionListener{

    private DocumentMasterTemplate mTemplate;
    private EditDocMTemplatePanel mEditDocMTemplatePanel;
    private EditFilesPanel mEditFilesPanel;
    private EditAttributeTemplatesPanel mAttributesPanel;
    private ActionListener mAction;
    private OKCancelPanel mOKCancelPanel;
    
    public EditDocMTemplateDialog(Frame pOwner, ActionListener pOKAction, ActionListener pEditFileAction, ActionListener pScanAction, ActionListener pAddAttributeAction, DocumentMasterTemplate pTemplate) {
        super(pOwner, I18N.BUNDLE.getString("EditDocMTemplate_title"));
        init(pOKAction, pEditFileAction, pScanAction, pAddAttributeAction, pTemplate);
    }

    public EditDocMTemplateDialog(Dialog pOwner, ActionListener pOKAction, ActionListener pEditFileAction, ActionListener pScanAction, ActionListener pAddAttributeAction,DocumentMasterTemplate pTemplate) {
        super(pOwner, I18N.BUNDLE.getString("EditDocMTemplate_title"));
        init(pOKAction, pEditFileAction, pScanAction, pAddAttributeAction, pTemplate);
    }

    protected void init(ActionListener pOKAction, ActionListener pEditFileAction, ActionListener pScanAction, ActionListener pAddAttributeAction, DocumentMasterTemplate pTemplate){
        mTemplate=pTemplate;
        mEditDocMTemplatePanel = new EditDocMTemplatePanel(mTemplate);
        mEditFilesPanel = new EditFilesPanel(pTemplate, pEditFileAction, pScanAction);
        mAttributesPanel = new EditAttributeTemplatesPanel(pTemplate, pAddAttributeAction);
        mAction = pOKAction;
        mOKCancelPanel = new OKCancelPanel(this, this);
        getRootPane().setDefaultButton(mOKCancelPanel.getOKButton());
        createLayout();
        setVisible(true);
    }

    public DocumentMasterTemplate getEditedTemplate(){
        return mTemplate;
    }
    
    public String getMask() {
        return mEditDocMTemplatePanel.getMask();
    }
    
    public String getDocumentType() {
        return mEditDocMTemplatePanel.getDocumentType();
    }
    public boolean isIdGenerated(){
        return mEditDocMTemplatePanel.isIdGenerated();
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

    protected JPanel getDocMTemplatePanel() {
        return mEditDocMTemplatePanel;
    }
}
