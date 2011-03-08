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
