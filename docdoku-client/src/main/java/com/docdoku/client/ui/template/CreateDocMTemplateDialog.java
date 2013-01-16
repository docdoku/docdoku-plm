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
import com.docdoku.core.document.InstanceAttributeTemplate;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;


public class CreateDocMTemplateDialog extends DocMTemplateDialog implements ActionListener{

    private CreateDocMTemplatePanel mCreateDocMTemplatePanel;
    private EditFilesPanel mEditFilesPanel;
    private EditAttributeTemplatesPanel mAttributesPanel;
    private ActionListener mAction;
    private OKCancelPanel mOKCancelPanel;
    
    public CreateDocMTemplateDialog(Frame pOwner, ActionListener pOKAction, ActionListener pEditFileAction, ActionListener pScanAction, ActionListener pAddAttributeAction) {
        super(pOwner, I18N.BUNDLE.getString("CreateDocMTemplate_title"));
        init(pOKAction,pEditFileAction, pScanAction, pAddAttributeAction);
    }

    public CreateDocMTemplateDialog(Dialog pOwner, ActionListener pOKAction, ActionListener pEditFileAction, ActionListener pScanAction, ActionListener pAddAttributeAction) {
        super(pOwner, I18N.BUNDLE.getString("CreateDocMTemplate_title"));
        init(pOKAction,pEditFileAction, pScanAction, pAddAttributeAction);
    }

    protected void init(ActionListener pOKAction, ActionListener pEditFileAction, ActionListener pScanAction, ActionListener pAddAttributeAction){
        mCreateDocMTemplatePanel = new CreateDocMTemplatePanel();
        mEditFilesPanel = new EditFilesPanel(pEditFileAction, pScanAction);
        mAttributesPanel = new EditAttributeTemplatesPanel(pAddAttributeAction);
        mAction = pOKAction;
        mOKCancelPanel = new OKCancelPanel(this, this);
        getRootPane().setDefaultButton(mOKCancelPanel.getOKButton());
        createLayout();
        mOKCancelPanel.setEnabled(false);
        createListener();
        setVisible(true);
    }
    
    private void createListener() {
        DocumentListener listener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent pDE) {
                mOKCancelPanel.setEnabled(true);
            }
            
            @Override
            public void removeUpdate(DocumentEvent pDE) {
                int length = pDE.getDocument().getLength();
                if (length == 0)
                    mOKCancelPanel.setEnabled(false);
            }
            
            @Override
            public void changedUpdate(DocumentEvent pDE) {
            }
        };
        mCreateDocMTemplatePanel.getIdText().getDocument().addDocumentListener(listener);
    }
    
    public String getDocMTemplateId() {
        return mCreateDocMTemplatePanel.getId();
    }
    
    public String getMask() {
        return mCreateDocMTemplatePanel.getMask();
    }
    
    public String getDocumentType() {
        return mCreateDocMTemplatePanel.getDocumentType();
    }
    
    public boolean isIdGenerated(){
        return mCreateDocMTemplatePanel.isIdGenerated();
    }
    
    public Set<InstanceAttributeTemplate> getAttributeTemplates() {
        Set<InstanceAttributeTemplate> attrs = new HashSet<InstanceAttributeTemplate>();
        for(int i=0;i<mAttributesPanel.getAttributesListModel().getSize();i++){
            attrs.add((InstanceAttributeTemplate) mAttributesPanel.getAttributesListModel().get(i));
        }
        return attrs;
    }
    
    public Collection<File> getFilesToAdd() {
        return mEditFilesPanel.getFilesToAdd();
    }
    
    
    @Override
    public void actionPerformed(ActionEvent pAE) {
        mAction.actionPerformed(new ActionEvent(this, 0, null));
    }

    @Override
    protected JPanel getSouthPanel() {
        return mOKCancelPanel;
    }
    
    @Override
    protected JPanel getFilesPanel() {
        return mEditFilesPanel;
    }
    
    @Override
    protected JPanel getAttributesPanel() {
        return mAttributesPanel;
    }

    @Override
    protected JPanel getDocMTemplatePanel() {
        return mCreateDocMTemplatePanel;
    }
    
}
