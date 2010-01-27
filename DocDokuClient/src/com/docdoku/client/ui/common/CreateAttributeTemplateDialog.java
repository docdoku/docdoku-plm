package com.docdoku.client.ui.common;

import com.docdoku.client.localization.I18N;
import java.awt.*;
import java.awt.event.ActionListener;


public class CreateAttributeTemplateDialog extends AttributeTemplateDialog{

    public CreateAttributeTemplateDialog(Frame pOwner, ActionListener pAction) {
        super(pOwner, I18N.BUNDLE.getString("AttributeCreation_title"));
        init(pAction);
    }

    public CreateAttributeTemplateDialog(Dialog pOwner, ActionListener pAction) {
        super(pOwner, I18N.BUNDLE.getString("AttributeCreation_title"));
        init(pAction);
    }


    @Override
    protected void init(ActionListener pAction){
        mEditAttributePanel = new EditAttributeTemplatePanel();
        super.init(pAction);
        mOKCancelPanel.setEnabled(false);
        setVisible(true);
    }
}
