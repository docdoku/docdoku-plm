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

package com.docdoku.client.ui.doc;

import com.docdoku.client.ui.common.CloseButton;
import com.docdoku.client.ui.common.MaxLengthDocument;
import com.docdoku.client.ui.common.ViewFilesPanel;
import com.docdoku.core.document.DocumentIteration;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import com.docdoku.client.localization.I18N;
import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.JXTaskPaneContainer;

public class ViewDocDetailsDialog extends JDialog {
    private ViewDocMPanel mDocMPanel;
    private ViewDocPanel mDocPanel;
    private ViewFilesPanel mFilesPanel;
    private ViewAttributesPanel mAttributesPanel;
    private ViewLinksPanel mLinksPanel;
    private CloseButton mCloseButton;
    private DocumentIteration mWatchedDoc;
    private JTabbedPane mTabbedPane;
    private JTextArea mDescriptionValueArea;
    
    public ViewDocDetailsDialog(Dialog pOwner, DocumentIteration pWatchedDoc,ActionListener pDownloadAction, ActionListener pOpenAction) {
        super(pOwner, I18N.BUNDLE.getString("ViewDocDetailsDialog_title"), true);
        init(pWatchedDoc,pDownloadAction,pOpenAction);
        setLocationRelativeTo(pOwner);
    }
    
    public ViewDocDetailsDialog(Frame pOwner, DocumentIteration pWatchedDoc,ActionListener pDownloadAction, ActionListener pOpenAction) {
        super(pOwner, I18N.BUNDLE.getString("ViewDocDetailsDialog_title"), true);
        init(pWatchedDoc,pDownloadAction,pOpenAction);
        setLocationRelativeTo(pOwner);
    }
    
    private void init(DocumentIteration pWatchedDoc,ActionListener pDownloadAction, ActionListener pOpenAction) {
        mWatchedDoc = pWatchedDoc;
        mDocMPanel = new ViewDocMPanel(mWatchedDoc.getDocumentMaster());
        mDocPanel = new ViewDocPanel(mWatchedDoc);
        mCloseButton = new CloseButton(this, I18N.BUNDLE.getString("Close_button"));
        mFilesPanel = new ViewFilesPanel(mWatchedDoc,pDownloadAction,pOpenAction);
        mAttributesPanel = new ViewAttributesPanel(mWatchedDoc);
        mLinksPanel = new ViewLinksPanel(mWatchedDoc.getLinkedDocuments(),pDownloadAction,pOpenAction);
        mDescriptionValueArea = new JTextArea(new MaxLengthDocument(4096), mWatchedDoc.getDocumentMaster().getDescription(),5,35);
        mDescriptionValueArea.setLineWrap(true);
        mDescriptionValueArea.setWrapStyleWord(true);
        mDescriptionValueArea.setEditable(false);
        mTabbedPane = new JTabbedPane();
        createLayout();
        setVisible(true);
    }
    
    private void createLayout() {
        getRootPane().setDefaultButton(mCloseButton);
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        JXTaskPaneContainer taskPane = new JXTaskPaneContainer();
        taskPane.setBackground(Color.LIGHT_GRAY);
        JXTaskPane docMActionPane = new JXTaskPane();
        docMActionPane.setTitle(I18N.BUNDLE.getString("ViewDocDetailsTaskPane_document_title"));
        mTabbedPane.add(I18N.BUNDLE.getString("Main_border"), mDocMPanel);
        mTabbedPane.add(I18N.BUNDLE.getString("Description_border"), new JScrollPane(mDescriptionValueArea));
        docMActionPane.add(mTabbedPane);
        
        JXTaskPane docActionPane = new JXTaskPane();
        docActionPane.setTitle(I18N.BUNDLE.getString("ViewDocDetailsTaskPane_iteration_title"));
        docActionPane.add(mDocPanel);
        
        taskPane.add(docMActionPane);
        taskPane.add(docActionPane);
        
        mainPanel.add(taskPane, BorderLayout.NORTH);
        JTabbedPane tabs=new JTabbedPane(JTabbedPane.TOP);
        Image img =
                Toolkit.getDefaultToolkit().getImage(ViewDocDetailsDialog.class.getResource(
                        "/com/docdoku/client/resources/icons/paperclip.png"));
        tabs.addTab(I18N.BUNDLE.getString("Files_border"),new ImageIcon(img),mFilesPanel);
        
        img =
                Toolkit.getDefaultToolkit().getImage(ViewDocDetailsDialog.class.getResource(
                        "/com/docdoku/client/resources/icons/attributes.png"));
        tabs.addTab(I18N.BUNDLE.getString("Attributes_border"),new ImageIcon(img),mAttributesPanel);
        
        img =
                Toolkit.getDefaultToolkit().getImage(ViewDocDetailsDialog.class.getResource(
                        "/com/docdoku/client/resources/icons/link.png"));
        tabs.addTab(I18N.BUNDLE.getString("Links_border"),new ImageIcon(img),mLinksPanel);
        
        
        Box centerPanel = new Box(BoxLayout.Y_AXIS);
        centerPanel.add(Box.createVerticalStrut(15));
        centerPanel.add(tabs);
        
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        JPanel southPanel = new JPanel();
        southPanel.add(mCloseButton);
        mainPanel.add(southPanel, BorderLayout.SOUTH);
        setContentPane(mainPanel);
        pack();        
    }
}
