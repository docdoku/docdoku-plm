/*
 * DocDoku, Professional Open Source
 * Copyright 2006, 2007, 2008, 2009, 2010, 2011, 2012 DocDoku SARL
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

package com.docdoku.gwt.explorer.client.ui.doc;

import com.docdoku.gwt.explorer.client.data.ServiceLocator;
import com.docdoku.gwt.explorer.client.localization.ExplorerI18NConstants;
import com.docdoku.gwt.explorer.client.ui.widget.DataRoundedPanel;
import com.docdoku.gwt.explorer.shared.ACLDTO;
import com.docdoku.server.rest.dto.UserDTO;
import com.docdoku.gwt.explorer.shared.UserGroupDTO;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;
import com.google.gwt.user.client.ui.ListBox;

/**
 *
 * @author Florent GARIN
 */
public class SecurityPanel extends DataRoundedPanel{

    private ListBox m_usersAndGroups;
    private CheckBox m_useACL;
    private ListBox m_permission;

    private UserDTO[] m_userMSs;
    private UserGroupDTO[] m_groupMSs;
    



    private final ExplorerI18NConstants i18n = ServiceLocator.getInstance().getExplorerI18NConstants();

    public SecurityPanel(){
        super(ServiceLocator.getInstance().getExplorerI18NConstants().tabSecurity());
        createLayout();
        registerListeners();
    }

    private void createLayout() {
        m_useACL=new CheckBox(i18n.fieldLabeldefineACL());
        inputPanel.setWidget(0,0,m_useACL);

        m_usersAndGroups = new ListBox();
        m_usersAndGroups.setEnabled(m_useACL.getValue());
        m_usersAndGroups.setVisibleItemCount(8);
        inputPanel.setWidget(1,0,m_usersAndGroups);

        m_permission=new ListBox();
        m_permission.setEnabled(m_useACL.getValue());
        m_permission.setVisibleItemCount(1);
        m_permission.addItem(i18n.fieldPermForbidden(), ACLDTO.Permission.FORBIDDEN.name());
        m_permission.addItem(i18n.fieldPermReadOnly(), ACLDTO.Permission.READ_ONLY.name());
        m_permission.addItem(i18n.fieldPermFullAccess(), ACLDTO.Permission.FULL_ACCESS.name());

        inputPanel.setText(2,0,i18n.fieldLabelPermission());
        inputPanel.setWidget(2,1,m_permission);

        FlexCellFormatter cellFormatter = inputPanel.getFlexCellFormatter();
        cellFormatter.setColSpan(0, 0, 2);
        cellFormatter.setColSpan(1, 0, 2);


    }

    public void setUserMemberships(UserDTO[] userMSs){
        m_userMSs=userMSs;
        int index=0;
        for(UserDTO ms:userMSs){
            m_usersAndGroups.insertItem(i18n.principalUser() + " " + ms.getName(), ms.getMembership().name(), index);
            index++;
        }
    }

    public void setUserGroupMemberships(UserGroupDTO[] groupMSs){
        m_groupMSs=groupMSs;
        for(UserGroupDTO ms:groupMSs){
            m_usersAndGroups.addItem(i18n.principalGroup() + " " + ms.getId(), ms.getMembership().name());
        }
    }

    public void clearInputs(){
        m_usersAndGroups.clear();
        m_useACL.setValue(false);
        m_usersAndGroups.setEnabled(m_useACL.getValue());
        m_permission.setEnabled(m_useACL.getValue());
    }

    private void registerListeners() {

        m_usersAndGroups.addChangeHandler(new ChangeHandler() {

            public void onChange(ChangeEvent event) {
                String permValue=m_usersAndGroups.getValue(m_usersAndGroups.getSelectedIndex());
                int index=ACLDTO.Permission.valueOf(permValue).ordinal();
                m_permission.setSelectedIndex(index);
            }
        });
        m_useACL.addValueChangeHandler(new ValueChangeHandler<Boolean>(){

            public void onValueChange(ValueChangeEvent<Boolean> event) {
                m_permission.setEnabled(event.getValue());
                m_usersAndGroups.setEnabled(event.getValue());
            }

        });

        m_permission.addChangeHandler(new ChangeHandler() {

            public void onChange(ChangeEvent event) {
                String permValue = m_permission.getValue(m_permission.getSelectedIndex());
                m_usersAndGroups.setValue(m_usersAndGroups.getSelectedIndex(), permValue);
            }
        });
    }

    public ACLDTO getACL(){

        ACLDTO acl=null;
        if(m_useACL.getValue()){
            acl=new ACLDTO();
            int userIndex=0;
            int groupIndex=0;
            for(int i=0; i<m_usersAndGroups.getItemCount();i++){
                ACLDTO.Permission perm = ACLDTO.Permission.valueOf(m_usersAndGroups.getValue(i));
                String itemTxt = m_usersAndGroups.getItemText(i);
                if(itemTxt.startsWith(i18n.principalUser())){
                    acl.addUserEntry(m_userMSs[userIndex++].getLogin(), perm);
                }else if (itemTxt.startsWith(i18n.principalGroup())){
                    acl.addGroupEntry(m_groupMSs[groupIndex++].getId(), perm);
                }
            }
        }
        return acl;
    }
 
}
