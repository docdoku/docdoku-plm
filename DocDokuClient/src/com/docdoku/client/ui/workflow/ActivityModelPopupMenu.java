package com.docdoku.client.ui.workflow;

import com.docdoku.client.localization.I18N;
import javax.swing.*;
import java.awt.event.ActionListener;


public class ActivityModelPopupMenu extends JPopupMenu{

    private JMenuItem mEdit;
    private JMenuItem mDelete;

    private ActionListener mEditActivityModelAction;
    private ActionListener mDeleteActivityModelAction;

    public ActivityModelPopupMenu(ActionListener pEditActivityModelAction, ActionListener pDeleteActivityModelAction){
        mEditActivityModelAction=pEditActivityModelAction;
        mDeleteActivityModelAction=pDeleteActivityModelAction;
        
        mEdit=new JMenuItem(I18N.BUNDLE.getString("Edit_title"));
        mDelete=new JMenuItem(I18N.BUNDLE.getString("Delete_title"));

        add(mEdit);
        add(mDelete);
        
        mEdit.setMnemonic('d');
        mDelete.setMnemonic('e');

        createListener();
    }

    private void createListener(){
        mEdit.addActionListener(mEditActivityModelAction);
        mDelete.addActionListener(mDeleteActivityModelAction);
    }

}
