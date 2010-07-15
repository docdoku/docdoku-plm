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

package com.docdoku.client.ui.doc;

import com.docdoku.client.ui.common.EditFilesPanel;
import com.docdoku.client.ui.common.OKCancelPanel;
import com.docdoku.client.localization.I18N;
import com.docdoku.core.entities.BinaryResource;
import com.docdoku.core.entities.Document;
import com.docdoku.core.entities.DocumentToDocumentLink;

import com.docdoku.core.entities.InstanceAttribute;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.io.File;
import java.util.Map;

public class EditDocDialog extends JDialog implements ActionListener {
    private EditDocPanel mDocPanel;
    private EditFilesPanel mFilesPanel;
    private EditAttributesPanel mAttributesPanel;
    private EditLinksPanel mLinksPanel;
    private OKCancelPanel mOKCancelPanel;
    private Document mEditedDoc;
    private ActionListener mOKAction;

    public EditDocDialog(
            Frame pOwner,
            Document pEditedDoc,
            ActionListener pOKAction, ActionListener pEditFileAction,  ActionListener pScanAction, ActionListener pAddAttributeAction, ActionListener pAddLinkAction) {
        super(pOwner, I18N.BUNDLE.getString("EditDocDialog_title"), true);
        setLocationRelativeTo(pOwner);
        mEditedDoc = pEditedDoc;
        mDocPanel = new EditDocPanel(mEditedDoc);
        mOKCancelPanel = new OKCancelPanel(this, this);
        mFilesPanel = new EditFilesPanel(mEditedDoc, pEditFileAction, pScanAction);
        mAttributesPanel = new EditAttributesPanel(mEditedDoc, pAddAttributeAction);
        mLinksPanel = new EditLinksPanel(mEditedDoc,pAddLinkAction);
        mOKAction = pOKAction;
        createLayout();
        setVisible(true);
    }

    private void createLayout() {
        getRootPane().setDefaultButton(mOKCancelPanel.getOKButton());
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(mDocPanel, BorderLayout.NORTH);
        
        JTabbedPane tabs=new JTabbedPane(JTabbedPane.TOP);  
        Image img =
                Toolkit.getDefaultToolkit().getImage(EditDocDialog.class.getResource(
                        "/com/docdoku/client/resources/icons/paperclip.png"));
        tabs.addTab(I18N.BUNDLE.getString("Files_border"),new ImageIcon(img),mFilesPanel);
        
        img =
                Toolkit.getDefaultToolkit().getImage(EditDocDialog.class.getResource(
                        "/com/docdoku/client/resources/icons/attributes.png"));
        tabs.addTab(I18N.BUNDLE.getString("Attributes_border"),new ImageIcon(img),mAttributesPanel);
        
        img =
                Toolkit.getDefaultToolkit().getImage(EditDocDialog.class.getResource(
                        "/com/docdoku/client/resources/icons/link.png"));
        tabs.addTab(I18N.BUNDLE.getString("Links_border"),new ImageIcon(img),mLinksPanel);
        
        Box centerPanel = new Box(BoxLayout.Y_AXIS);
        centerPanel.add(Box.createVerticalStrut(15));
        centerPanel.add(tabs);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(mOKCancelPanel, BorderLayout.SOUTH);
        setContentPane(mainPanel);
        pack();
    }

    public Document getEditedDoc() {
        return mEditedDoc;
    }

    public Collection<BinaryResource> getFilesToRemove() {
        return mFilesPanel.getFilesToRemove();
    }

    public Collection<File> getFilesToAdd() {
        return mFilesPanel.getFilesToAdd();
    }

    public String getComment() {
        return mDocPanel.getComment();
    }

    public Map<String, InstanceAttribute> getAttributes() {
        return mAttributesPanel.getAttributes();
    }
    
    public DocumentToDocumentLink[] getLinks() {
        DocumentToDocumentLink[] links = new DocumentToDocumentLink[mLinksPanel.getLinksListModel().getSize()];
        for(int i=0;i<mLinksPanel.getLinksListModel().getSize();i++){
            links[i]=(DocumentToDocumentLink) mLinksPanel.getLinksListModel().get(i);
        }
        return links;
    }

    @Override
    public void actionPerformed(ActionEvent pAE) {
        mOKAction.actionPerformed(new ActionEvent(this, 0, null));
    }


}
