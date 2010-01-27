package com.docdoku.client.ui.common;


import com.docdoku.core.entities.InstanceAttributeTemplate;
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
