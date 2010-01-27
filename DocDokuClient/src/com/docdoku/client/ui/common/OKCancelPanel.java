package com.docdoku.client.ui.common;


import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import com.docdoku.client.localization.I18N;
import com.docdoku.client.ui.common.CloseButton;
import com.docdoku.client.ui.common.OKButton;

public class OKCancelPanel extends JPanel {
    private OKButton mOK;
    private CloseButton mCancel;

    public OKCancelPanel(Window pFrame, ActionListener pActionOK) {
        this(pFrame, pActionOK, true);
    }
    
    public OKCancelPanel(Window pFrame, ActionListener pActionOK, boolean pDisposeOnOK) {
        super(new FlowLayout(FlowLayout.CENTER));
        if(pDisposeOnOK)
        	mOK = new OKButton(pFrame, I18N.BUNDLE.getString("Ok_button"));
        else
        	mOK = new OKButton(I18N.BUNDLE.getString("Ok_button"));
        mCancel = new CloseButton(pFrame, I18N.BUNDLE.getString("Cancel_button"));
        createLayout();
        createListener(pActionOK);
    }

    public void setEnabled(boolean pValue) {
        mOK.setEnabled(pValue);
    }

    private void createLayout() {
        add(mOK);
        add(mCancel);
    }

    private void createListener(ActionListener pActionOK) {
        mOK.addActionListener(pActionOK);
    }

    public JButton getOKButton() {
        return mOK;
    }
}
