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

package com.docdoku.client.ui.common;


import com.docdoku.core.document.InstanceAttributeTemplate;
import javax.swing.*;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;



public abstract class AttributeTemplateDialog extends JDialog implements ActionListener {

    protected EditAttributeTemplatePanel mEditAttributePanel;
    protected OKCancelPanel mOKCancelPanel;
    protected ActionListener mAction;

    public AttributeTemplateDialog(Frame pOwner, String pTitle) {
        super(pOwner, pTitle, true);
        setLocationRelativeTo(pOwner);
    }

    public AttributeTemplateDialog(Dialog pOwner, String pTitle) {
        super(pOwner, pTitle, true);
        setLocationRelativeTo(pOwner);
    }

    protected void init(ActionListener pAction){
        mOKCancelPanel = new OKCancelPanel(this, this);
        mAction = pAction;
        createLayout();
        createListener();
    }

    private void createLayout() {
        getRootPane().setDefaultButton(mOKCancelPanel.getOKButton());
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(mEditAttributePanel, BorderLayout.CENTER);
        mainPanel.add(mOKCancelPanel, BorderLayout.SOUTH);
        setContentPane(mainPanel);
        pack();
    }

    private void createListener() {
        DocumentListener listener = new DocumentListener() {
            public void insertUpdate(DocumentEvent pDE) {
                mOKCancelPanel.setEnabled(true);
            }

            public void removeUpdate(DocumentEvent pDE) {
                int length = pDE.getDocument().getLength();
                if (length == 0)
                    mOKCancelPanel.setEnabled(false);
            }

            public void changedUpdate(DocumentEvent pDE) {
            }
        };
        mEditAttributePanel.getNameText().getDocument().addDocumentListener(listener);
    }

    public String getAttributeName() {
        return mEditAttributePanel.getNameAttribute();
    }

    public InstanceAttributeTemplate.AttributeType getAttributeType() {
        return mEditAttributePanel.geType();
    }

    public void actionPerformed(ActionEvent pAE) {
        mAction.actionPerformed(new ActionEvent(this, 0, null));
    }
}
