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

package com.docdoku.client.ui.template;

import com.docdoku.client.localization.I18N;
import com.docdoku.client.ui.common.CloseButton;
import com.docdoku.client.ui.common.ViewFilesPanel;
import com.docdoku.core.document.DocumentMasterTemplate;

import java.awt.*;
import java.awt.event.ActionListener;
import javax.swing.JPanel;


public class ViewDocMTemplateDetailsDialog extends DocMTemplateDialog{

    private CloseButton mCloseButton;
    private ViewFilesPanel mFilesPanel;
    private ViewAttributesPanel mAttributesPanel;
    private ViewDocMTemplatePanel mDocMTemplatePanel;
            
    public ViewDocMTemplateDetailsDialog(Frame pOwner, DocumentMasterTemplate pTemplate, ActionListener pDownloadAction, ActionListener pOpenAction) {
        super(pOwner, I18N.BUNDLE.getString("ViewDocMTemplateDetailsDialog_title"));
        init(pTemplate, pDownloadAction, pOpenAction);
    }

    public ViewDocMTemplateDetailsDialog(Dialog pOwner, DocumentMasterTemplate pTemplate, ActionListener pDownloadAction, ActionListener pOpenAction) {
        super(pOwner, I18N.BUNDLE.getString("ViewDocMTemplateDetailsDialog_title"));
        init(pTemplate, pDownloadAction, pOpenAction);
    }

    protected void init(DocumentMasterTemplate pTemplate, ActionListener pDownloadAction, ActionListener pOpenAction){
        mDocMTemplatePanel = new ViewDocMTemplatePanel(pTemplate);
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

    protected JPanel getDocMTemplatePanel() {
        return mDocMTemplatePanel;
    }
}
